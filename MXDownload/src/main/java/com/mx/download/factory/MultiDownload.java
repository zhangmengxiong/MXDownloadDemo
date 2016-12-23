package com.mx.download.factory;

import com.mx.download.factory.run.MultiDownloadRun;
import com.mx.download.model.ConfigBean;
import com.mx.download.model.DownChipBean;
import com.mx.download.model.DownType;
import com.mx.download.model.InfoBean;
import com.mx.download.model.SaveBean;
import com.mx.download.utils.FileUtil;
import com.mx.download.utils.IDownLoadCall;
import com.mx.download.utils.Log;
import com.mx.download.utils.Utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 多线程下载执行器
 * <p>
 * 创建人： zhangmengxiong
 * 创建时间： 2016-11-22.
 * 联系方式: zmx_final@163.com
 */

public class MultiDownload implements IDownload {
    private static final long SLEEP_TIME = 1000;

    private IDownLoadCall downloadCall; // 回调对象
    private String fromUrl; // 下载地址
    private Executor executor; // 多线程池
    private File positionFile;// 记录下载位置文件
    private DownChipBean[] chipBeans;// 结束位置
    private AtomicBoolean isUserCancel = new AtomicBoolean(false);

    private File cacheFile;
    private File desFile;
    private int errorNo = 0;
    private int retryMax = 3;
    private volatile SpeedInterceptor speedInterceptor;
    private InfoBean infoBean;

    @Override
    public void setInfo(ConfigBean configBean, InfoBean status) {
        infoBean = status;

        this.retryMax = configBean.getMaxRetryCount();
        this.executor = configBean.getExecutorService();
        this.fromUrl = configBean.getFromUrl();
        this.downloadCall = configBean.getDownLoadCall();
        speedInterceptor = new SpeedInterceptor(configBean.getLimitSpeed());

        cacheFile = new File(configBean.getTempFile());
        desFile = new File(configBean.getToPath());
        positionFile = new File(configBean.getCacheFile());// 创建缓存文件，用于记录下载位置

        isUserCancel.set(false);
    }

    @Override
    public void prepareSave() throws Exception {
        if (infoBean.getTotalSize() + 1024 * 1024 * 5 > cacheFile.getFreeSpace()) {
            Log.v("剩余磁盘容量：" + Utils.formatSize(cacheFile.getFreeSpace()));
            throw new Exception("磁盘容量不足！");
        }
    }

    @Override
    public void prepareHistory() throws Exception {
        if (positionFile.exists() && positionFile.length() > 0) // 如果缓存文件已经存在，表明之前已经下载过一部分
        {
            boolean reset = false;

            SaveBean saveMod = FileUtil.readDownloadPosition(positionFile);// 读取缓存文件中的下载位置，即每个下载线程的开始位置和结束位置，将读取到的下载位置写入到开始数组和结束数组
            if (saveMod != null) {
                if (infoBean.getTotalSize() != saveMod.fileSize) {
                    Log.v("网络上文件的大小和本地断点记录不一样，重置下载：" + desFile.getName());
                    reset = true;
                }

                if (!infoBean.getLastModify().equals(saveMod.LastModify)) {
                    Log.v("网络上文件的修改时间和本地断点记录不一样，重置下载：" + desFile.getName());
                    reset = true;
                }

                if (saveMod.type != DownType.TYPE_MULITY) {
                    Log.v("下载断点类型和记录不一致，重置下载：" + desFile.getName());
                    reset = true;
                }

                if (reset) {
                    infoBean.setDownloadSize(0);
                    FileUtil.resetFile(cacheFile);
                    FileUtil.resetFile(positionFile);
                } else {
                    chipBeans = saveMod.downChipBeen;
                    infoBean.setDownloadSize(saveMod.completeSize);
                }
            }
        }
        Log.v("下载状态 = " + infoBean.getFormatStatusString());
    }

    @Override
    public void prepareFirstInit() throws Exception {
        if (infoBean.getDownloadSize() <= 0) // 如果是刚开始下载
        {
            chipBeans = FileUtil.getDownloadPosition(infoBean.getTotalSize());// 获取下载位置

            // 创建新文件
            FileUtil.createFile(cacheFile);
            RandomAccessFile accessFile = new RandomAccessFile(cacheFile.getAbsolutePath(), "rw");
            accessFile.setLength(infoBean.getTotalSize());
            accessFile.close();
        }
    }

    @Override
    public void startDownload() throws Exception {
        infoBean.cleanSpeed();
        infoBean.computeSpeed();

        MultiDownloadRun[] downloadThread = new MultiDownloadRun[chipBeans.length];
        for (int i = 0; i < chipBeans.length; i++) {
            downloadThread[i] = new MultiDownloadRun(fromUrl, cacheFile.getAbsolutePath(), chipBeans[i], speedInterceptor);
            executor.execute(downloadThread[i]);// 启动线程，开始下载
        }
        Log.v("Start Download Source : " + fromUrl);

        boolean isError = false;
        // 向缓存文件循环写入下载文件位置信息
        boolean stop = false;
        while (!stop) {
            // 校验用户退出响应
            if (isUserCancel.get()) break;
            stop = true;

            long downSize = 0L;
            for (int i = 0; i < chipBeans.length; i++)// 判断是否所有下载线程都执行结束
            {
                if (downloadThread[i].isInError()) {// 下载失败 重试
                    downloadThread[i].stop();
                    downloadThread[i] = new MultiDownloadRun(fromUrl, cacheFile.getAbsolutePath(), chipBeans[i], speedInterceptor);
                    errorNo++;
                    executor.execute(downloadThread[i]);
                }
                if (!downloadThread[i].isDownloadOver()) {
                    stop = false;// 只要有一个下载线程没有执行结束，则文件还没有下载完毕
                }

                downSize = downSize + chipBeans[i].completeSize;
            }
            try {
                infoBean.setDownloadSize(downSize);
                infoBean.computeSpeed();
                speedInterceptor.setCurrentSpeed((int) (infoBean.getSpeed() / 1024f));
                if (downloadCall != null) {
                    downloadCall.onProgressUpdate(infoBean);
                }

                Thread.sleep(SLEEP_TIME);// 每隔0.5秒更新一次下载位置信息
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (errorNo >= retryMax) {
                isError = true;
                break;
            }
        }
        updatePosition();// 更新下载位置信息

        FileUtil.writeMulityPosition(positionFile, chipBeans, infoBean);
        boolean isDownFinish = true;
        for (int i = 0; i < chipBeans.length; i++)// 判断是否所有下载线程都执行结束
        {
            if (!chipBeans[i].isComplete()) {
                isDownFinish = false;
            }
            downloadThread[i].stop();
        }

        if (isError) {
            throw new Exception("下载重试次数超过" + retryMax + "次，下载失败！");
        }

        if (isUserCancel.get()) {
            downloadCall.onCancel(fromUrl);
        } else if (isDownFinish) {
            Log.v("下载完成！");

            FileUtil.deleteFile(positionFile);//  positionFile.delete();// 删除下载位置缓存文件
            FileUtil.resetFile(desFile);//desFile.delete();
            cacheFile.renameTo(desFile);
            FileUtil.chmod("777", desFile);

            if (downloadCall != null) {
                downloadCall.onSuccess(fromUrl);
            }
        }
    }

    /**
     * 计算下载完成的百分比
     */
    private synchronized void updatePosition() {
        long finishLength = 0L;
        for (DownChipBean chipBeen : chipBeans) {
            finishLength = finishLength + chipBeen.completeSize;
        }

        infoBean.setDownloadSize(finishLength);
        infoBean.cleanSpeed();
        if (downloadCall != null) {
            downloadCall.onProgressUpdate(infoBean);
        }
    }

    @Override
    public void cancel() {
        isUserCancel.set(true);
    }
}

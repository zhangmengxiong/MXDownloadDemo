package com.mx.download.factory;

import com.mx.download.factory.run.MultiDownloadRun;
import com.mx.download.model.ChipSaveMod;
import com.mx.download.model.DownChipBean;
import com.mx.download.model.DownloadBean;
import com.mx.download.model.UrlInfoBean;
import com.mx.download.utils.FileUtil;
import com.mx.download.utils.IDownLoadCall;
import com.mx.download.utils.Log;
import com.mx.download.utils.Utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.Executor;

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
    private volatile boolean isUserCancel = false;

    private File cacheFile;
    private File desFile;
    private int errorNo = 0;
    private int retryMax = 3;
    private UrlInfoBean urlInfoBean;

    @Override
    public void setInfo(DownloadBean downloadBean, UrlInfoBean status) {
        urlInfoBean = status;

        this.retryMax = downloadBean.getMaxRetryCount();
        this.executor = downloadBean.getExecutorService();
        this.fromUrl = downloadBean.getFromUrl();
        this.downloadCall = downloadBean.getDownLoadCall();
        cacheFile = new File(downloadBean.getTempFile());
        desFile = new File(downloadBean.getToPath());
        positionFile = new File(downloadBean.getCacheFile());// 创建缓存文件，用于记录下载位置

        isUserCancel = false;
    }

    @Override
    public void prepareSave() throws Exception {
        if (urlInfoBean.getTotalSize() + 1024 * 1024 * 5 > cacheFile.getFreeSpace()) {
            Log.v("剩余磁盘容量：" + Utils.formatSize(cacheFile.getFreeSpace()));
            throw new Exception("磁盘容量不足！");
        }
    }

    @Override
    public void prepareHistory() throws Exception {
        if (urlInfoBean.isChunked || urlInfoBean.getTotalSize() <= 0) return;

        if (positionFile.exists() && positionFile.length() > 0) // 如果缓存文件已经存在，表明之前已经下载过一部分
        {
            boolean reset = false;

            ChipSaveMod saveMod = FileUtil.readDownloadPosition(positionFile);// 读取缓存文件中的下载位置，即每个下载线程的开始位置和结束位置，将读取到的下载位置写入到开始数组和结束数组
            if (saveMod != null) {
                if (urlInfoBean.getTotalSize() != saveMod.fileSize) {
                    Log.v("网络上文件的大小和本地断点记录不一样，重置下载：" + desFile.getName());
                    reset = true;
                }

                if (!urlInfoBean.getLastModify().equals(saveMod.LastModify)) {
                    Log.v("网络上文件的修改时间和本地断点记录不一样，重置下载：" + desFile.getName());
                    reset = true;
                }
                if (reset) {
                    urlInfoBean.setDownloadSize(0);
                    FileUtil.resetFile(cacheFile);
                    FileUtil.resetFile(positionFile);
                } else {
                    chipBeans = saveMod.downChipBeen;
                    urlInfoBean.setDownloadSize(saveMod.completeSize);
                }
            }
        }
        Log.v("下载状态 = " + urlInfoBean.getFormatStatusString());
    }

    @Override
    public void prepareFirstInit() throws Exception {
        if (urlInfoBean.getDownloadSize() <= 0) // 如果是刚开始下载
        {
            chipBeans = FileUtil.getDownloadPosition(urlInfoBean.getTotalSize());// 获取下载位置

            // 创建新文件
            FileUtil.createFile(cacheFile);
            RandomAccessFile accessFile = new RandomAccessFile(cacheFile.getAbsolutePath(), "rw");
            accessFile.setLength(urlInfoBean.getTotalSize());
            accessFile.close();
        }
    }

    @Override
    public void startDownload() throws Exception {
        MultiDownloadRun[] downloadThread = new MultiDownloadRun[chipBeans.length];
        for (int i = 0; i < chipBeans.length; i++) {
            downloadThread[i] = new MultiDownloadRun(fromUrl, cacheFile.getAbsolutePath(), chipBeans[i]);
            executor.execute(downloadThread[i]);// 启动线程，开始下载
        }
        Log.v("Start Download Source : " + fromUrl);

        boolean isError = false;
        // 向缓存文件循环写入下载文件位置信息
        boolean stop = false;
        while (!stop) {
            // 校验用户退出响应
            if (isUserCancel) break;
            stop = true;

            long downSize = 0L;
            for (int i = 0; i < chipBeans.length; i++)// 判断是否所有下载线程都执行结束
            {
                if (downloadThread[i].isInError()) {// 下载失败 重试
                    downloadThread[i].stop();
                    downloadThread[i] = new MultiDownloadRun(fromUrl, cacheFile.getAbsolutePath(), chipBeans[i]);
                    errorNo++;
                    executor.execute(downloadThread[i]);
                }
                if (!downloadThread[i].isDownloadOver()) {
                    stop = false;// 只要有一个下载线程没有执行结束，则文件还没有下载完毕
                }

                downSize = downSize + chipBeans[i].completeSize;
            }
            try {
                urlInfoBean.setDownloadSize(downSize);
                if (downloadCall != null) {
                    downloadCall.onProgressUpdate(urlInfoBean);
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

        FileUtil.writeMulityPosition(positionFile, chipBeans, urlInfoBean);
        boolean isDownFinish = true;
        for (int i = 0; i < chipBeans.length; i++)// 判断是否所有下载线程都执行结束
        {
            if (!chipBeans[i].isComplete()) {
                isDownFinish = false;
            }
            downloadThread[i].stop();
        }

        if (isError) {
            Log.v("下载重试次数超过10次，下载失败！");
            throw new Exception("下载重试次数超过10次，下载失败！");
        }

        if (isUserCancel) {
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

        urlInfoBean.setDownloadSize(finishLength);

        if (downloadCall != null) {
            downloadCall.onProgressUpdate(urlInfoBean);
        }
    }

    @Override
    public void cancel() {
        isUserCancel = true;
    }
}
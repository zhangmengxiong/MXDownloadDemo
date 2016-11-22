package com.mx.download.factory;

import com.mx.download.MXDownload;
import com.mx.download.factory.run.MulityDownloadRun;
import com.mx.download.model.ChipSaveMod;
import com.mx.download.model.DownChipBean;
import com.mx.download.model.DownloadBean;
import com.mx.download.model.DownloadStatus;
import com.mx.download.utils.FileUtil;
import com.mx.download.utils.IDownLoadCall;
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

public class MultyDownload implements IDownload {
    private static final long SLEEP_TIME = 1000;

    private IDownLoadCall downloadCall;
    private String fromUrl;
    private Executor executor;
    private File positionFile;// 记录下载位置文件
    private DownChipBean[] chipBeens;// 结束位置
    private volatile boolean isUserCancel = false;

    private File cacheFile;
    private File desFile;
    private int errorNo = 0;
    private int retryMax = 3;
    private DownloadStatus downloadStatus;

    @Override
    public void setInfo(DownloadBean downloadBean, DownloadStatus status) {
        downloadStatus = status;

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
        if (MXDownload.DEBUG)
            System.out.println("下载文件大小：" + downloadStatus.getFormatTotalSize());
        if (MXDownload.DEBUG)
            System.out.println("剩余磁盘容量：" + Utils.formatSize(cacheFile.getFreeSpace()));
        if (downloadStatus.getTotalSize() + 1024 * 1024 * 5 > cacheFile.getFreeSpace()) {
            throw new Exception("磁盘容量不足！");
        }
    }

    @Override
    public void prepareHistory() throws Exception {
        if (downloadStatus.isChunked || downloadStatus.getTotalSize() <= 0) return;

        if (positionFile.exists() && positionFile.length() > 0) // 如果缓存文件已经存在，表明之前已经下载过一部分
        {
            boolean reset = false;

            ChipSaveMod saveMod = FileUtil.readDownloadPosition(positionFile);// 读取缓存文件中的下载位置，即每个下载线程的开始位置和结束位置，将读取到的下载位置写入到开始数组和结束数组
            if (saveMod != null) {
                if (downloadStatus.getTotalSize() != saveMod.fileSize) {
                    if (MXDownload.DEBUG)
                        System.out.print("网络上文件的大小和本地断点记录不一样，重置下载：" + desFile.getName());
                    reset = true;
                }

                if (!downloadStatus.getLastModify().equals(saveMod.LastModify)) {
                    if (MXDownload.DEBUG)
                        System.out.print("网络上文件的修改时间和本地断点记录不一样，重置下载：" + desFile.getName());
                    reset = true;
                }
                if (reset) {
                    downloadStatus.setDownloadSize(0);
                    FileUtil.resetFile(cacheFile);
                    FileUtil.resetFile(positionFile);
                } else {
                    chipBeens = saveMod.downChipBeen;
                    downloadStatus.setDownloadSize(saveMod.completeSize);
                }
            }
        }
        if (MXDownload.DEBUG)
            System.out.println("下载状态 = " + downloadStatus.getFormatStatusString());
    }

    @Override
    public void prepareFirstInit() throws Exception {
        if (downloadStatus.getDownloadSize() <= 0) // 如果是刚开始下载
        {
            chipBeens = FileUtil.getDownloadPosition(downloadStatus.getTotalSize());// 获取下载位置

            // 创建新文件
            FileUtil.createFile(cacheFile);
            RandomAccessFile accessFile = new RandomAccessFile(cacheFile.getAbsolutePath(), "rw");
            accessFile.setLength(downloadStatus.getTotalSize());
            accessFile.close();
        }
    }

    @Override
    public void startDownload() throws Exception {
        MulityDownloadRun[] downloadThread = new MulityDownloadRun[chipBeens.length];
        for (int i = 0; i < chipBeens.length; i++) {
            downloadThread[i] = new MulityDownloadRun(fromUrl, cacheFile.getAbsolutePath(), chipBeens[i]);
            executor.execute(downloadThread[i]);// 启动线程，开始下载
        }
        if (MXDownload.DEBUG) System.out.println("Start Download Source : " + fromUrl);

        boolean isError = false;
        // 向缓存文件循环写入下载文件位置信息
        boolean stop = false;
        while (!stop) {
            // 校验用户退出响应
            if (isUserCancel) break;
            stop = true;

            long downSize = 0L;
            for (int i = 0; i < chipBeens.length; i++)// 判断是否所有下载线程都执行结束
            {
                if (downloadThread[i].isInError()) {// 下载失败 重试
                    downloadThread[i].stop();
                    downloadThread[i] = new MulityDownloadRun(fromUrl, cacheFile.getAbsolutePath(), chipBeens[i]);
                    errorNo++;
                    executor.execute(downloadThread[i]);
                }
                if (!downloadThread[i].isDownloadOver()) {
                    stop = false;// 只要有一个下载线程没有执行结束，则文件还没有下载完毕
                }

                downSize = downSize + chipBeens[i].completeSize;
            }
            try {
                downloadStatus.setDownloadSize(downSize);
                if (downloadCall != null) {
                    downloadCall.onProgressUpdate(downloadStatus);
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

        FileUtil.writeMulityPosition(positionFile, chipBeens, downloadStatus);
        boolean isDownFinish = true;
        for (int i = 0; i < chipBeens.length; i++)// 判断是否所有下载线程都执行结束
        {
            if (!chipBeens[i].isComplete()) {
                isDownFinish = false;
            }
            downloadThread[i].stop();
        }

        if (isError) {
            System.out.println("下载重试次数超过10次，下载失败！");
            throw new Exception("下载重试次数超过10次，下载失败！");
        }

        if (isUserCancel) {
            downloadCall.onCancel(fromUrl);
        } else if (isDownFinish) {
            System.out.println("下载完成！");

            FileUtil.deleteFile(positionFile);//  positionFile.delete();// 删除下载位置缓存文件
            FileUtil.resetFile(desFile);//desFile.delete();
            cacheFile.renameTo(desFile);
            FileUtil.chmod("777", desFile);

            if (downloadCall != null) {
                downloadCall.onFinish(fromUrl);
            }
        }
    }

    /**
     * 计算下载完成的百分比
     */
    private synchronized void updatePosition() {
        long finishLength = 0L;
        for (DownChipBean chipBeen : chipBeens) {
            finishLength = finishLength + chipBeen.completeSize;
        }

        downloadStatus.setDownloadSize(finishLength);

        if (downloadCall != null) {
            downloadCall.onProgressUpdate(downloadStatus);
        }
    }

    @Override
    public void cancel() {
        isUserCancel = true;
    }
}

package com.mx.download.factory;

import com.mx.download.MXDownload;
import com.mx.download.factory.run.SingleDownloadRun;
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
 * 创建人： zhangmengxiong
 * 创建时间： 2016-11-22.
 * 联系方式: zmx_final@163.com
 */

public class SingleDownload implements IDownload {
    private static final long SLEEP_TIME = 1000;

    private IDownLoadCall downloadCall;
    private String fromUrl;
    private Executor executor;
    private File positionFile;// 记录下载位置文件
    private DownChipBean chipBean;// 结束位置
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
        if (downloadStatus.getTotalSize() <= 0) return;

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
                    chipBean = saveMod.downChipBeen[0];
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
            chipBean = new DownChipBean();// 获取下载位置
            chipBean.start = 0;
            chipBean.end = downloadStatus.getTotalSize();

            // 创建新文件
            FileUtil.createFile(cacheFile);
            RandomAccessFile accessFile = new RandomAccessFile(cacheFile.getAbsolutePath(), "rw");
            accessFile.setLength(downloadStatus.getTotalSize());
            accessFile.close();
        }
    }

    @Override
    public void startDownload() throws Exception {
        SingleDownloadRun downloadThread = new SingleDownloadRun(fromUrl, cacheFile.getAbsolutePath(), chipBean);
        executor.execute(downloadThread);

        if (MXDownload.DEBUG) System.out.println("Start Download Source : " + fromUrl);

        boolean isError = false;
        // 向缓存文件循环写入下载文件位置信息
        boolean stop = false;
        while (!stop) {
            // 校验用户退出响应
            if (isUserCancel) break;
            stop = true;

            if (downloadThread.isInError()) {// 下载失败 重试
                downloadThread.stop();
                downloadThread = new SingleDownloadRun(fromUrl, cacheFile.getAbsolutePath(), chipBean);
                errorNo++;
                executor.execute(downloadThread);
            }
            if (!downloadThread.isDownloadOver()) {
                stop = false;// 只要有一个下载线程没有执行结束，则文件还没有下载完毕
            }
            try {
                downloadStatus.setDownloadSize(chipBean.completeSize);
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

        FileUtil.writeSinglePosition(positionFile, chipBean, downloadStatus);
        downloadThread.stop();

        if (isError) {
            System.out.println("下载重试次数超过10次，下载失败！");
            throw new Exception("下载重试次数超过10次，下载失败！");
        }

        if (isUserCancel) {
            downloadCall.onCancel(fromUrl);
        } else if (chipBean.isComplete()) {
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
        downloadStatus.setDownloadSize(chipBean.completeSize);

        if (downloadCall != null) {
            downloadCall.onProgressUpdate(downloadStatus);
        }
    }

    @Override
    public void cancel() {
        isUserCancel = true;
    }
}

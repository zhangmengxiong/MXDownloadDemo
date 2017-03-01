package com.mx.download.factory;

import com.mx.download.factory.run.NoHistoryDownloadRun;
import com.mx.download.model.ConfigBean;
import com.mx.download.model.DownChipBean;
import com.mx.download.model.DownInfo;
import com.mx.download.utils.FileUtil;
import com.mx.download.utils.IDownLoadCall;
import com.mx.download.utils.Log;
import com.mx.download.utils.Utils;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 无断点的下载器
 * 当下载不支持Accept-Ranges: bytes 时，用这个下载器
 * <p>
 * 创建人： zhangmengxiong
 * 创建时间： 2016-12-23.
 * 联系方式: zmx_final@163.com
 */
public class NoHistoryDownload implements IDownload {
    private static final long SLEEP_TIME = 1000;

    private IDownLoadCall downloadCall;
    private String fromUrl;
    private Executor executor;
    private File positionFile;// 记录下载位置文件
    private DownChipBean chipBean;// 结束位置
    private AtomicBoolean isUserCancel = new AtomicBoolean(false);

    private File cacheFile;
    private File desFile;
    private int errorNo = 0;
    private int retryMax = 3;
    private volatile SpeedInterceptor speedInterceptor;
    private DownInfo downInfo;
    private int TIME_OUT;

    @Override
    public void setInfo(ConfigBean configBean, DownInfo info) {
        downInfo = info;

        this.TIME_OUT = configBean.getTimeOut();
        this.retryMax = configBean.getMaxRetryCount();
        this.executor = configBean.getExecutorService();
        this.fromUrl = configBean.getFromUrl();
        this.downloadCall = configBean.getDownLoadCall();
        cacheFile = new File(configBean.getTempFile());
        desFile = new File(configBean.getToPath());
        positionFile = new File(configBean.getCacheFile());// 创建缓存文件，用于记录下载位置
        speedInterceptor = new SpeedInterceptor(configBean.getLimitSpeed());
        isUserCancel.set(false);
    }

    @Override
    public void prepareSave() throws Exception {
        if (downInfo.totalSize + 1024 * 1024 * 5 > cacheFile.getFreeSpace()) {
            Log.v("剩余磁盘容量：" + Utils.formatSize(cacheFile.getFreeSpace()));
            throw new Exception("磁盘容量不足！");
        }
    }

    @Override
    public void prepareHistory() throws Exception {

    }

    @Override
    public void prepareFirstInit() throws Exception {
        chipBean = new DownChipBean();// 获取下载位置
        chipBean.start = 0;
        chipBean.end = 0;
        if (downInfo.totalSize > 0) {
            chipBean.end = downInfo.totalSize;
        }
    }

    @Override
    public void startDownload() throws Exception {
        downInfo.cleanSpeed();
        downInfo.computeSpeed();

        NoHistoryDownloadRun downloadThread = new NoHistoryDownloadRun(fromUrl, cacheFile.getAbsolutePath(), chipBean, speedInterceptor, TIME_OUT);
        executor.execute(downloadThread);

        Log.v("Start Download Source : " + fromUrl);

        boolean isError = false;
        // 向缓存文件循环写入下载文件位置信息
        boolean stop = false;
        while (!stop) {
            // 校验用户退出响应
            if (isUserCancel.get()) break;
            stop = true;

            if (downloadThread.isInError()) {// 下载失败 重试
                downloadThread.stop();
                downloadThread = new NoHistoryDownloadRun(fromUrl, cacheFile.getAbsolutePath(), chipBean, speedInterceptor, TIME_OUT);
                errorNo++;
                executor.execute(downloadThread);
            }
            if (!downloadThread.isDownloadOver()) {
                stop = false;// 只要有一个下载线程没有执行结束，则文件还没有下载完毕
            }
            try {
                downInfo.downloadSize = chipBean.completeSize;
                downInfo.computeSpeed();
                speedInterceptor.setCurrentSpeed((int) (downInfo.curSpeedSize / 1024f));
                if (downloadCall != null) {
                    downloadCall.onProgressUpdate(fromUrl, downInfo.infoBean);
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

        FileUtil.writeSinglePosition(positionFile, chipBean, downInfo);
        downloadThread.stop();

        if (isError) {
            throw new Exception("下载重试次数超过" + retryMax + "次，下载失败！");
        }

        if (isUserCancel.get()) {
            downloadCall.onCancel(fromUrl);
        } else if (chipBean.isComplete()) {
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
        downInfo.downloadSize = chipBean.completeSize;
        downInfo.cleanSpeed();
        if (downloadCall != null) {
            downloadCall.onProgressUpdate(fromUrl, downInfo.infoBean);
        }
    }

    @Override
    public void cancel() {
        isUserCancel.set(true);
    }
}

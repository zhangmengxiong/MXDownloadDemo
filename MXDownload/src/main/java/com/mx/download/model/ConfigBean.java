package com.mx.download.model;

import android.text.TextUtils;

import com.mx.download.utils.IDownLoadCall;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.text.TextUtils.concat;

/**
 * 下载信息保存对象
 * <p>
 * 创建人： zhangmengxiong
 * 创建时间： 2016-11-18.
 * 联系方式: zmx_final@163.com
 */

public class ConfigBean {
    private static final String TMP_SUFFIX = ".tmp";  //temp file
    private static final String CACHE_SUFFIX = ".cache";  //last modify file

    private ExecutorService executorService = null;
    private int MAX_THREADS = 3;
    private int MAX_RETRY_COUNT = -1;

    private IDownLoadCall iDownLoadCall;
    private String fromUrl;
    private String toPath;

    private String tempFile;
    private String cacheFile;
    private int limitSpeed;
    private int timeOut;

    public ConfigBean() {
    }

    public void setFromUrl(String fromUrl) {
        this.fromUrl = fromUrl;
    }

    public void setToPath(String toPath) {
        this.toPath = toPath;
        if (!TextUtils.isEmpty(toPath)) {
            tempFile = concat(toPath, TMP_SUFFIX).toString();
            cacheFile = concat(toPath, CACHE_SUFFIX).toString();
        }
    }

    public String getFromUrl() {
        return fromUrl;
    }

    public String getToPath() {
        return toPath;
    }

    public String getTempFile() {
        return tempFile;
    }

    public String getCacheFile() {
        return cacheFile;
    }

    public void setMaxThreads(int max) {
        if (max < 1) max = 1;
        this.MAX_THREADS = max;
    }

    public boolean isSingleThread() {
        return MAX_THREADS == 1;
    }

    public void setMaxRetryCount(int max) {
        this.MAX_RETRY_COUNT = max;
    }

    public int getMaxRetryCount() {
        return (MAX_RETRY_COUNT < 0 ? MAX_THREADS * 2 : MAX_RETRY_COUNT);
    }

    public void addCall(IDownLoadCall iDownLoadCall) {
        this.iDownLoadCall = iDownLoadCall;
    }

    public IDownLoadCall getDownLoadCall() {
        return iDownLoadCall;
    }

    public synchronized ExecutorService getExecutorService() {
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(MAX_THREADS + 1);
        }
        return executorService;
    }

    public void setLimitSpeed(int limitSpeed) {
        this.limitSpeed = limitSpeed;
    }

    public int getLimitSpeed() {
        return limitSpeed;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public int getTimeOut() {
        return timeOut > 0 ? timeOut : 1000 * 30;
    }
}

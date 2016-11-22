package com.mx.download;

import android.text.TextUtils;

import com.mx.download.utils.IDownLoadCall;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.text.TextUtils.concat;

/**
 * 创建人： zhangmengxiong
 * 创建时间： 2016-11-18.
 * 联系方式: zmx_final@163.com
 */

class DownloadHelper {
    private static final String TMP_SUFFIX = ".tmp";  //temp file
    private static final String CACHE_SUFFIX = ".cache";  //last modify file

    private ExecutorService executorService = Executors.newFixedThreadPool(3);
    private int MAX_THREADS = 3;
    private int MAX_RETRY_COUNT = -1;

    private IDownLoadCall iDownLoadCall;
    private String fromUrl;
    private String toPath;

    private String tempFile;
    private String cacheFile;


    DownloadHelper() {
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

    String getFromUrl() {
        return fromUrl;
    }

    String getToPath() {
        return toPath;
    }

    String getTempFile() {
        return tempFile;
    }

    String getCacheFile() {
        return cacheFile;
    }

    void setMaxThreads(int max) {
        if (max < 1) max = 1;
        this.MAX_THREADS = max;
    }

    boolean isSingleThread() {
        return MAX_THREADS == 1;
    }

    void setMaxRetryCount(int max) {
        this.MAX_RETRY_COUNT = max;
    }

    public int getMaxRetryCount() {
        return (MAX_RETRY_COUNT < 0 ? MAX_THREADS * 3 : MAX_RETRY_COUNT);
    }

    void addCall(IDownLoadCall iDownLoadCall) {
        this.iDownLoadCall = iDownLoadCall;
    }

    IDownLoadCall getDownLoadCall() {
        return iDownLoadCall;
    }

    synchronized ExecutorService getExecutorService() {
        if (executorService == null || executorService.isShutdown())
            executorService = Executors.newFixedThreadPool(MAX_THREADS + 1);
        return executorService;
    }
}

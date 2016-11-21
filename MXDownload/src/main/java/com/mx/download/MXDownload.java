package com.mx.download;

import android.os.Handler;
import android.os.Looper;

import com.mx.download.model.DownloadStatus;
import com.mx.download.utils.IDownLoadCall;

public class MXDownload {
    private static final String TAG = MXDownload.class.getSimpleName();
    public static boolean DEBUG = true;
    private DownloadHelper downloadHelper;
    private Download download;
    private Handler mHandler;

    public static MXDownload getInstance() {
        return new MXDownload();
    }

    private MXDownload() {
        downloadHelper = new DownloadHelper();
    }

    public MXDownload download(String fromUrl) {
        downloadHelper.setFromUrl(fromUrl);
        return this;
    }

    public MXDownload save(String toPath) {
        downloadHelper.setToPath(toPath);
        return this;
    }

    /**
     * 同步回调方法
     *
     * @param iDownLoadCall
     * @return
     */
    public MXDownload addCall(final IDownLoadCall iDownLoadCall) {
        downloadHelper.addCall(iDownLoadCall);
        return this;
    }

    /**
     * 主线程回调方法
     *
     * @param iDownLoadCall
     * @return
     */
    public MXDownload addMainThreadCall(final IDownLoadCall iDownLoadCall) {
        if (mHandler == null) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            } else {
                mHandler = new Handler();
            }
        }

        if (iDownLoadCall != null)
            downloadHelper.addCall(new IDownLoadCall() {
                @Override
                public void onPrepare(final String url) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            iDownLoadCall.onPrepare(url);
                        }
                    });
                }

                @Override
                public void onStart(final DownloadStatus status) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            iDownLoadCall.onStart(status);
                        }
                    });
                }

                @Override
                public void onError(final Throwable th) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            iDownLoadCall.onError(th);
                        }
                    });
                }

                @Override
                public void onProgressUpdate(final DownloadStatus status) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            iDownLoadCall.onProgressUpdate(status);
                        }
                    });
                }

                @Override
                public void onFinish(final String url) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            iDownLoadCall.onFinish(url);
                        }
                    });
                }

                @Override
                public void onCancel(final String url) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            iDownLoadCall.onCancel(url);
                        }
                    });
                }
            });
        return this;
    }

    public MXDownload maxThread(int max) {
        downloadHelper.setMaxThreads(max);
        return this;
    }

    public MXDownload maxRetryCount(int max) {
        downloadHelper.setMaxRetryCount(max);
        return this;
    }

    public MXDownload start() {
        downloadHelper.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    cancel();
                    download = new Download(downloadHelper);
                    download.startRun();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (downloadHelper.getDownLoadCall() != null)
                        downloadHelper.getDownLoadCall().onError(e);
                } finally {
                    cancel();

                    downloadHelper.getExecutorService().shutdownNow();
                }
            }
        });
        return this;
    }

    public void cancel() {
        if (download != null)
            download.cancel();
    }
}

package com.mx.download;

import android.os.Handler;
import android.os.Looper;

import com.mx.download.model.DownloadBean;
import com.mx.download.model.DownloadStatus;
import com.mx.download.utils.IDownLoadCall;

public class MXDownload {
    private final String TAG = MXDownload.class.getSimpleName();
    public static boolean DEBUG = true;
    private DownloadBean downloadBean;
    private IDownLoadCall iDownLoadCall;
    private Download download;
    private Handler mHandler;

    public static MXDownload getInstance() {
        return new MXDownload();
    }

    private MXDownload() {
        downloadBean = new DownloadBean();
    }

    public MXDownload download(String fromUrl) {
        downloadBean.setFromUrl(fromUrl);
        return this;
    }

    public MXDownload save(String toPath) {
        downloadBean.setToPath(toPath);
        return this;
    }

    public MXDownload singleThread() {
        downloadBean.setMaxThreads(1);
        return this;
    }

    /**
     * 同步回调方法
     *
     * @param iDownLoadCall
     * @return
     */
    public MXDownload addCall(IDownLoadCall iDownLoadCall) {
        this.iDownLoadCall = iDownLoadCall;
        downloadBean.addCall(iDownLoadCall);
        return this;
    }

    /**
     * 主线程回调方法
     *
     * @param call
     * @return
     */
    public MXDownload addMainThreadCall(IDownLoadCall call) {
        this.iDownLoadCall = call;
        if (iDownLoadCall != null) {
            if (mHandler == null) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    Looper.prepare();
                    mHandler = new Handler();
                    Looper.loop();
                } else {
                    mHandler = new Handler();
                }
            }

            downloadBean.addCall(new IDownLoadCall() {
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
        }
        return this;
    }

    public MXDownload maxThread(int max) {
        downloadBean.setMaxThreads(max);
        return this;
    }

    public MXDownload maxRetryCount(int max) {
        downloadBean.setMaxRetryCount(max);
        return this;
    }

    public MXDownload start() {
        if (download != null) return this;
        downloadBean.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                synchronized (TAG) {
                    if (download != null) return;
                    try {
                        download = new Download(downloadBean);
                        download.startRun();
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (downloadBean.getDownLoadCall() != null)
                            downloadBean.getDownLoadCall().onError(e);
                    } finally {
                        cancel();
                        download = null;
                        downloadBean.getExecutorService().shutdownNow();
                    }
                }
            }
        });
        return this;
    }

    public void cancel() {
        try {
            if (download != null)
                download.cancel();
        } catch (Exception ignored) {
        }
    }
}

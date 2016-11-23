package com.mx.download;

import android.os.Handler;
import android.os.Looper;

import com.mx.download.model.DownloadBean;
import com.mx.download.model.UrlInfoBean;
import com.mx.download.utils.IDownLoadCall;

/**
 * 下载器入口类
 */
public class MXDownload {
    private final String TAG = MXDownload.class.getSimpleName();
    public static boolean DEBUG = false;

    private DownloadBean downloadBean; // 下载信息对象
    private IDownLoadCall iDownLoadCall; // 回调对象
    private Download download; // 下载方法
    private Handler mHandler; // 主线程回调句柄

    /**
     * 每次都是新的变量~
     *
     * @return
     */
    public static MXDownload getInstance() {
        return new MXDownload();
    }

    private MXDownload() {
        downloadBean = new DownloadBean();
    }

    /**
     * 设置下载地址
     *
     * @param fromUrl
     * @return
     */
    public MXDownload download(String fromUrl) {
        downloadBean.setFromUrl(fromUrl);
        return this;
    }

    /**
     * 保存路径，需是全路径！
     *
     * @param toPath
     * @return
     */
    public MXDownload save(String toPath) {
        downloadBean.setToPath(toPath);
        return this;
    }

    /**
     * 单线程模式
     *
     * @return
     */
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
                public void onStart(final UrlInfoBean status) {
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
                public void onProgressUpdate(final UrlInfoBean status) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            iDownLoadCall.onProgressUpdate(status);
                        }
                    });
                }

                @Override
                public void onSuccess(final String url) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            iDownLoadCall.onSuccess(url);
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

    /**
     * 设置多线程数量
     *
     * @param max
     * @return
     */
    public MXDownload maxThread(int max) {
        downloadBean.setMaxThreads(max);
        return this;
    }

    /**
     * 设置报错时重试次数！
     *
     * @param max
     * @return
     */
    public MXDownload maxRetryCount(int max) {
        downloadBean.setMaxRetryCount(max);
        return this;
    }

    /**
     * 开始启动下载！
     *
     * @return
     */
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
                        if (downloadBean.getDownLoadCall() != null)
                            downloadBean.getDownLoadCall().onFinish();
                        downloadBean.getExecutorService().shutdownNow();
                    }
                }
            }
        });
        return this;
    }

    /**
     * 取消下载
     */
    public void cancel() {
        try {
            if (download != null)
                download.cancel();
        } catch (Exception ignored) {
        }
    }

    /**
     * 设置调试模式
     *
     * @param d
     */
    public static void setDebug(boolean d) {
        MXDownload.DEBUG = d;
    }
}

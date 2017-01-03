package com.mx.download;

import android.os.Handler;
import android.os.Looper;

import com.mx.download.model.ConfigBean;
import com.mx.download.model.InfoBean;
import com.mx.download.utils.IDownLoadCall;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 下载器入口类
 */
public class MXDownload {
    private final String TAG = MXDownload.class.getSimpleName();
    public static boolean DEBUG = false;

    private ConfigBean configBean; // 下载信息对象
    private IDownLoadCall iDownLoadCall; // 回调对象
    private Download download; // 下载方法
    private Handler mHandler; // 主线程回调句柄
    private AtomicBoolean isInDownload = new AtomicBoolean(false);

    /**
     * 每次都是新的变量~
     *
     * @return
     */
    public static MXDownload getInstance() {
        return new MXDownload();
    }

    private MXDownload() {
        configBean = new ConfigBean();
    }

    /**
     * 设置下载地址
     *
     * @param fromUrl
     * @return
     */
    public MXDownload download(String fromUrl) {
        configBean.setFromUrl(fromUrl);
        return this;
    }

    /**
     * 保存路径，需是全路径！
     *
     * @param toPath
     * @return
     */
    public MXDownload save(String toPath) {
        configBean.setToPath(toPath);
        return this;
    }

    /**
     * 单线程模式
     *
     * @return
     */
    public MXDownload singleThread() {
        configBean.setMaxThreads(1);
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
        configBean.addCall(iDownLoadCall);
        return this;
    }

    /**
     * 主线程回调方法
     *
     * @param call
     * @return
     */
    public MXDownload addAsyncCall(IDownLoadCall call) {
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

            configBean.addCall(new IDownLoadCall() {
                @Override
                public void onPrepare(final String url) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (iDownLoadCall != null)
                                iDownLoadCall.onPrepare(url);
                        }
                    });
                }

                @Override
                public void onStart(final InfoBean infoBean) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (iDownLoadCall != null)
                                iDownLoadCall.onStart(infoBean);
                        }
                    });
                }

                @Override
                public void onError(final Throwable th) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (iDownLoadCall != null)
                                iDownLoadCall.onError(th);
                        }
                    });
                }

                @Override
                public void onProgressUpdate(final InfoBean infoBean) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (iDownLoadCall != null)
                                iDownLoadCall.onProgressUpdate(infoBean);
                        }
                    });
                }

                @Override
                public void onSuccess(final String url) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (iDownLoadCall != null)
                                iDownLoadCall.onSuccess(url);
                        }
                    });
                }

                @Override
                public void onCancel(final String url) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (iDownLoadCall != null)
                                iDownLoadCall.onCancel(url);
                        }
                    });
                }

                @Override
                public void onFinish() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (iDownLoadCall != null)
                                iDownLoadCall.onFinish();
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
        configBean.setMaxThreads(max);
        return this;
    }

    /**
     * 限速下载
     *
     * @param speed KB/s
     * @return
     */
    public MXDownload limitSpeed(int speed) {
        configBean.setLimitSpeed(speed);
        return this;
    }

    /**
     * 设置报错时重试次数！
     *
     * @param max
     * @return
     */
    public MXDownload maxRetryCount(int max) {
        configBean.setMaxRetryCount(max);
        return this;
    }

    /**
     * 开始启动下载！
     *
     * @return
     */
    public synchronized MXDownload start() {
        if (configBean.getFromUrl() == null)
            throw new NullPointerException("下载地址为空，请调用方法：download() 设置下载地址。");

        if (configBean.getToPath() == null)
            throw new NullPointerException("保存地址为空，请调用方法：save() 设置下载地址。");

        if (isRunning()) return this;
        isInDownload.set(true);
        configBean.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    download = new Download(configBean);
                    download.startRun();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (configBean.getDownLoadCall() != null)
                        configBean.getDownLoadCall().onError(e);
                } finally {
                    cancel();
                    if (configBean.getDownLoadCall() != null)
                        configBean.getDownLoadCall().onFinish();

                    isInDownload.set(false);
                    configBean.getExecutorService().shutdownNow();
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

    public boolean isRunning() {
        return isInDownload.get();
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

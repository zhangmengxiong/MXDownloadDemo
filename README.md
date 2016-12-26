# MXDownloadDemo
简单的一个Android平台多线程下载demo
用法：
```Java
	MXDownload mxDownload = MXDownload.getInstance()
                            .download("http://bos.pgzs.com/sjapp91/pcsuite/plugin/91assistant_pc_008.exe") // 下载源地址
                            .save("/sdcard/aaa.exe") // 保存路径
                            .maxThread(3) // 最大线程
                            .limitSpeed(500) // 限速，单位：KB/s
                            .maxRetryCount(3) // 下载报错最大重试次数
                            .singleThread() // 单线程下载，跟maxThread(1)是一样的效果
                            // 主线程中回调函数
                            .addAsyncCall(new IDownLoadCall() {
                                @Override
                                public void onPrepare(String url) {
                                    // 开始下载线程
                                    Log.v("proc", "onPrepare");
                                }

                                @Override
                                public void onStart(InfoBean status) {
                                    // 开始下载
                                    Log.v("proc", "onStart");
                                }

                                @Override
                                public void onError(Throwable th) {
                                    // 下载失败
                                    Log.v("proc", "onError");
                                }

                                @Override
                                public void onProgressUpdate(InfoBean status) {
                                    // 下载进度更新
                                    Log.v("proc", status.getFormatStatusString());
                                }

                                @Override
                                public void onSuccess(String url) {
                                    // 下载成功
                                    Log.v("proc", "onSuccess");
                                }

                                @Override
                                public void onFinish() {
                                    // 下载结束，不管下载成功还是失败都会调用
                                    Log.v("proc", "onFinish");
                                }

                                @Override
                                public void onCancel(String url) {
                                    // 用户取消了
                                    Log.v("proc", "onCancel");
                                }
                            })
                            .addCall()// 下载线程中的回调函数
                            .start();
```
取消下载的方法：
```Java
			mxDownload.cancel();
```
下载进度中有以下信息可以直接获得：
获得当前瞬时网速
```Java
			InfoBean.getFormatSpeed(); // 20.5 KB/s
```
获得下载大小信息
```Java
			InfoBean.getFormatTotalSize(); // 10 MB 文件总大小
			InfoBean.getFormatDownloadSize(); // 5.65 MB 当前下载的大小
			InfoBean.getFormatStatusString(); // 5.65 MB/10 MB 当前下载的大小
```
获取下载进度：
```Java
			InfoBean.getPercent(); // 0.5 对应 50%
```

注意:
1：addAsyncCall 与 addCall 只能选择一个设置回调
2：maxThread 与 singleThread 只能调用其中一个方法，同时调用时后调用的生效
3：addAsyncCall 为异步回调，回调函数为main线程中运行
	 addCall 为同步回调，回调函数在异步线程中运行。


### 联系方式： zmx_final@163.com

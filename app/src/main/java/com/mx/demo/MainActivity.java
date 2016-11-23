package com.mx.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mx.download.MXDownload;
import com.mx.download.model.UrlInfoBean;
import com.mx.download.utils.IDownLoadCall;

import java.io.File;

public class MainActivity extends Activity {

    ProgressBar progressBar;

    TextView curSize;

    TextView maxSize;

    Button start;

    Button stop;

    MXDownload mxDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        curSize = (TextView) findViewById(R.id.curSize);
        maxSize = (TextView) findViewById(R.id.maxSize);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        MXDownload.setDebug(true);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mxDownload == null) {
                    new File("/sdcard/weixin.apk").delete();
                    mxDownload = MXDownload.getInstance()
//                            .download("http://a6.pc6.com/kha5/laojiumen.360.apk") // 200M
//                            .download("http://www.baidu.com")
                            .download("https://downpack.baidu.com/appsearch_AndroidPhone_1012271b.apk") // 6M
//                            .download("http://bos.pgzs.com/sjapp91/pcsuite/plugin/91assistant_pc_008.exe")
                            .save("/sdcard/weixin.apk")
                            .maxThread(3)
                            .maxRetryCount(3)
//                            .singleThread()
                            .addMainThreadCall(new IDownLoadCall() {
                                @Override
                                public void onPrepare(String url) {
                                    Log.v("proc", "onPrepare");
                                    progressBar.setProgress(0);
                                    progressBar.setMax(100);
                                }

                                @Override
                                public void onStart(UrlInfoBean status) {
                                    Log.v("proc", "onStart");
                                    progressBar.setProgress((int) (status.getPercent() * 100));
                                }

                                @Override
                                public void onError(Throwable th) {
                                    Log.v("proc", "onError");
                                }

                                @Override
                                public void onProgressUpdate(UrlInfoBean status) {
                                    Log.v("proc", status.getFormatStatusString());
                                    progressBar.setProgress((int) (status.getPercent() * 100));
                                    curSize.setText(status.getFormatDownloadSize());
                                    maxSize.setText(status.getFormatTotalSize());
                                }

                                @Override
                                public void onSuccess(String url) {
                                    Log.v("proc", "onSuccess");
                                }

                                @Override
                                public void onFinish() {

                                }

                                @Override
                                public void onCancel(String url) {
                                    Log.v("proc", "onCancel");
                                }
                            })
                            .start();
                } else {
                    mxDownload.start();
                }
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mxDownload != null) mxDownload.cancel();
            }
        });
    }
}

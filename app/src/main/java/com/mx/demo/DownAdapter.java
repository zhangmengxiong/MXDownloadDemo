package com.mx.demo;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mx.download.MXDownload;
import com.mx.download.model.InfoBean;
import com.mx.download.utils.IDownLoadCall;

import java.io.File;
import java.util.ArrayList;

/**
 * 创建人： zhangmengxiong
 * 创建时间： 2016-11-23.
 * 联系方式: zmx_final@163.com
 */

public class DownAdapter extends BaseAdapter {
    private ArrayList<ItemBean> arrayList;

    public DownAdapter(ArrayList<ItemBean> list) {
        arrayList = list;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public ItemBean getItem(int i) {
        return arrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ItemBean bean = getItem(i);
        ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, viewGroup, false);
            viewHolder.name = (TextView) view.findViewById(R.id.name);
            viewHolder.info = (TextView) view.findViewById(R.id.info);
            viewHolder.progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            viewHolder.start = (Button) view.findViewById(R.id.start);
            viewHolder.stop = (Button) view.findViewById(R.id.stop);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.bean = bean;
        viewHolder.name.setText(bean.NAME);
        viewHolder.start.setTag(viewHolder);

        viewHolder.start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ViewHolder viewHolder = (ViewHolder) view.getTag();
                ItemBean bean = viewHolder.bean;
                if (bean.mxDownload == null) {
                    new File(bean.SAVE).delete();
                    bean.mxDownload = MXDownload.getInstance()
                            .download(bean.URL) // 200M
//                            .download("https://downpack.baidu.com/appsearch_AndroidPhone_1012271b.apk") // 6M
//                            .download("http://bos.pgzs.com/sjapp91/pcsuite/plugin/91assistant_pc_008.exe")
//                            .download("http://p.gdown.baidu.com/208d79de0e27195f1538926fee90cd006fd83c5ede297065043615fd7fa4ce901fa4a5267970131df4cc5f34cd34cfc7baa9f64924b13f56ced2c34faa295baed1ce6fbd53eb610636bbcc7be4a2b1c38f498ee916dfa66171a4395e1ff8116e596ec5c35fcb3eb2") //400M
                            .save(bean.SAVE)
                            .maxThread(3)
//                            .maxRetryCount(3)
//                            .singleThread()
                            .addMainThreadCall(new IDownLoadCall() {
                                @Override
                                public void onPrepare(String url) {
                                    Log.v("proc", "onPrepare");
                                    viewHolder.progressBar.setProgress(0);
                                    viewHolder.progressBar.setMax(100);
                                }

                                @Override
                                public void onStart(InfoBean status) {
                                    Log.v("proc", "onStart");
                                    viewHolder.progressBar.setProgress((int) (status.getPercent() * 100));
                                }

                                @Override
                                public void onError(Throwable th) {
                                    Log.v("proc", "onError");
                                }

                                @Override
                                public void onProgressUpdate(InfoBean status) {
                                    Log.v("proc", status.getFormatStatusString());
                                    viewHolder.progressBar.setProgress((int) (status.getPercent() * 100));
                                    viewHolder.info.setText(status.getFormatStatusString());
                                }

                                @Override
                                public void onSuccess(String url) {
                                    Log.v("proc", "onSuccess");
                                }

                                @Override
                                public void onFinish() {
                                    Log.v("proc", "onFinish");
                                }

                                @Override
                                public void onCancel(String url) {
                                    Log.v("proc", "onCancel");
                                }
                            })
                            .start();
                } else {
                    bean.mxDownload.start();
                }
            }
        });

        viewHolder.stop.setTag(bean);
        viewHolder.stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ItemBean bean = (ItemBean) view.getTag();
                if (bean.mxDownload != null) bean.mxDownload.cancel();
            }
        });

        return view;
    }

    class ViewHolder {
        ProgressBar progressBar;
        TextView info, name;
        Button start, stop;
        ItemBean bean;
    }
}

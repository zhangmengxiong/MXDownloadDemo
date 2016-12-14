package com.mx.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.mx.download.MXDownload;

import java.util.ArrayList;

public class MainActivity extends Activity {

    ListView listView;
    DownAdapter downAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);
        listView.setItemsCanFocus(true);
        MXDownload.setDebug(true);

        ArrayList<ItemBean> list = new ArrayList<>();
        list.add(new ItemBean("1", "http://culturetv.hanyastar.com.cn/standard/mp4/341/457/460/4834_2014-12-19.mp4", "/sdcard/MX/1.apk"));
        list.add(new ItemBean("2", "http://bos.pgzs.com/sjapp91/pcsuite/plugin/91assistant_pc_008.exe", "/sdcard/MX/2.apk"));
        list.add(new ItemBean("3", "http://p.gdown.baidu.com/208d79de0e27195f1538926fee90cd006fd83c5ede297065043615fd7fa4ce901fa4a5267970131df4cc5f34cd34cfc7baa9f64924b13f56ced2c34faa295baed1ce6fbd53eb610636bbcc7be4a2b1c38f498ee916dfa66171a4395e1ff8116e596ec5c35fcb3eb2", "/sdcard/MX/3.apk"));
        list.add(new ItemBean("4", "http://bos.pgzs.com/sjapp91/pcsuite/plugin/91assistant_pc_008.exe", "/sdcard/MX/4.apk"));
        list.add(new ItemBean("5", "http://bos.pgzs.com/sjapp91/pcsuite/plugin/91assistant_pc_008.exe", "/sdcard/MX/5.apk"));
        list.add(new ItemBean("6", "http://bos.pgzs.com/sjapp91/pcsuite/plugin/91assistant_pc_008.exe", "/sdcard/MX/6.apk"));

        downAdapter = new DownAdapter(list);
        listView.setAdapter(downAdapter);

    }
}

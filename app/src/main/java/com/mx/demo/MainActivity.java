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
        list.add(new ItemBean("1", "http://culturetv.hanyastar.com.cn/standard/mp4/337/343/901/6008_2015-04-10.mp4", "/sdcard/MX/1.apk"));
        list.add(new ItemBean("2", "http://culturetv.hanyastar.com.cn/standard/mp4/341/358/451/4721_2014-10-09.mp4", "/sdcard/MX/2.apk"));
        list.add(new ItemBean("3", "http://culturetv.hanyastar.com.cn/standard/mp4/339/373/414/35314_2015-06-01.mp4", "/sdcard/MX/3.apk"));
        list.add(new ItemBean("4", "http://wpc.124CE.sigmacdn.net/002DCC/nc/headers.html?url=http://wpc.124CE.sigmacdn.net/crossdomain.xml&req=GET", "/sdcard/MX/4.apk"));

        downAdapter = new DownAdapter(list);
        listView.setAdapter(downAdapter);

    }
}

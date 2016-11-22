package com.mx.download.model;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * 下载碎片
 * Created by zmx_f on 2016-5-12.
 */
public class DownChipBean implements Serializable {
    public long start = 0L;
    public long end = 0L;

    public long completeSize = 0L;
    public int index;

    public DownChipBean() {
    }

    public DownChipBean(long s, long e) {
        start = s;
        end = e;
    }

    public boolean isComplete() {
        return completeSize >= end - start;
    }

    public long getSeek() {
        long seek = (start + completeSize - 1);
        if (seek <= 0) seek = 0;
        if (seek >= end) seek = end;
        return seek;
    }

    @Override
    public String toString() {
        float p = (100 * completeSize / (float) (end - start));
        if (isComplete()) p = 100;
        return ("[线程 -" + index + "- 下载 " + p + "%]");
    }

    public String toSaveString() {
        return (start + "##" + end + "##" + completeSize + "##" + index);
    }

    public static DownChipBean fromString(String s) {
        if (TextUtils.isEmpty(s)) return null;
        try {
            DownChipBean bean = new DownChipBean();
            String[] strings = s.split("##");
            if (strings.length != 4) return null;

            bean.start = Long.valueOf(strings[0]);
            bean.end = Long.valueOf(strings[1]);
            bean.completeSize = Long.valueOf(strings[2]);
            bean.index = Integer.valueOf(strings[3]);

            return bean;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addDownloadSize(int length) {
        completeSize = completeSize + length;
        if (completeSize > end - start) completeSize = end - start;
    }
}

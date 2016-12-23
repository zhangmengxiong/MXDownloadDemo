package com.mx.download.model;

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
        return ("[thread " + index + " --> " + p + "%]");
    }

    /**
     * 新增下载完成的长度
     *
     * @param length
     */
    public void addDownloadSize(int length) {
        completeSize = completeSize + length;
        if (end > 0 && completeSize > end - start) completeSize = end - start;
    }
}

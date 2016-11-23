package com.mx.download.utils;

import com.mx.download.MXDownload;

/**
 * 创建人： zhangmengxiong
 * 创建时间： 2016-11-22.
 * 联系方式: zmx_final@163.com
 */

public class Log {
    private static final String TAG = "MXDownload";

    public static void v(String msg) {
        if (MXDownload.DEBUG)
            android.util.Log.v(TAG, msg);
    }
}

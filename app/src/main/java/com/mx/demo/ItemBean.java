package com.mx.demo;

import com.mx.download.MXDownload;

/**
 * 创建人： zhangmengxiong
 * 创建时间： 2016-11-23.
 * 联系方式: zmx_final@163.com
 */

public class ItemBean {
    public ItemBean(String n, String u, String s) {
        NAME = n;
        URL = u;
        SAVE = s;
    }

    String NAME;
    String URL;
    String SAVE;
    MXDownload mxDownload;
}

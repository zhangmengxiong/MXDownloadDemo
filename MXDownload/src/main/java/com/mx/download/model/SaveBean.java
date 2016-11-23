package com.mx.download.model;

import java.io.Serializable;

/**
 * 断点信息保存到文件的对象
 * Created by zmx_f on 2016-5-12.
 */
public class SaveBean implements Serializable {
    public DownType type = DownType.TYPE_MULITY;
    public long fileSize;
    public long completeSize;
    public String LastModify = null;
    public DownChipBean[] downChipBeen;
}

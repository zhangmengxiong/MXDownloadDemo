package com.mx.download.factory;

import com.mx.download.model.DownloadBean;
import com.mx.download.model.DownloadStatus;

/**
 * 创建人： zhangmengxiong
 * 创建时间： 2016-11-22.
 * 联系方式: zmx_final@163.com
 */

public interface IDownload {

    void setInfo(DownloadBean bean, DownloadStatus status);

    void prepareSave() throws Exception;

    void prepareHistory() throws Exception;

    void prepareFirstInit() throws Exception;

    void startDownload() throws Exception;

    void cancel();
}

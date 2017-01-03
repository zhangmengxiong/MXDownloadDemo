package com.mx.download.factory;

import com.mx.download.model.ConfigBean;
import com.mx.download.model.DownInfo;

/**
 * 模板模式的接口定义~
 * 创建人： zhangmengxiong
 * 创建时间： 2016-11-22.
 * 联系方式: zmx_final@163.com
 */

public interface IDownload {

    /**
     * 设置下载信息
     *
     * @param bean
     * @param info
     */
    void setInfo(ConfigBean bean, DownInfo info);

    /**
     * 准备下载的文件
     *
     * @throws Exception
     */
    void prepareSave() throws Exception;

    /**
     * 读取下载历史记录
     *
     * @throws Exception
     */
    void prepareHistory() throws Exception;

    /**
     * 如果是第一次下载，则初始化一下
     *
     * @throws Exception
     */
    void prepareFirstInit() throws Exception;

    /**
     * 开始下载
     *
     * @throws Exception
     */
    void startDownload() throws Exception;

    /**
     * 取消下载
     */
    void cancel();
}

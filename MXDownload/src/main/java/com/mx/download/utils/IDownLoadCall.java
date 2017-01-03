package com.mx.download.utils;

import com.mx.download.model.InfoBean;

public abstract class IDownLoadCall {

    /**
     * 开始初始化
     *
     * @param fromUrl 下载地址
     */
    public void onPrepare(String fromUrl) {
    }

    /**
     * 开始下载
     *
     * @param fromUrl  下载地址
     * @param infoBean 下载的信息
     */
    public void onStart(String fromUrl, InfoBean infoBean) {
    }

    /**
     * 下载进度更新
     *
     * @param fromUrl  下载地址
     * @param infoBean
     */
    public void onProgressUpdate(String fromUrl, InfoBean infoBean) {
    }

    /**
     * 下载失败
     *
     * @param fromUrl 下载地址
     * @param th      错误消息
     */
    public void onError(String fromUrl, Throwable th) {
    }

    /**
     * 下载成功
     *
     * @param fromUrl 下载地址
     */
    public void onSuccess(String fromUrl) {
    }

    /**
     * 下载进程结束
     *
     * @param fromUrl 下载地址
     */
    public void onFinish(String fromUrl) {
    }

    /**
     * 下载被手动终止
     *
     * @param fromUrl 下载地址
     */
    public void onCancel(String fromUrl) {
    }
}

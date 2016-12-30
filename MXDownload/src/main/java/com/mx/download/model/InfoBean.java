package com.mx.download.model;

import com.mx.download.utils.Utils;

/**
 * 创建人： zhangmengxiong
 * 创建时间： 2016-12-30.
 * 联系方式: zmx_final@163.com
 */

public class InfoBean {
    private DownInfo downInfo;

    InfoBean(DownInfo bean) {
        downInfo = bean;
    }

    public String getEtag() {
        return downInfo.Etag;
    }

    /**
     * 获取服务器文件最后修改的时间
     *
     * @return
     */
    public String getLastModify() {
        return downInfo.lastModify;
    }

    /**
     * 获取已下载大小
     *
     * @return 单位：Byte
     */
    public long getDownloadSize() {
        return downInfo.downloadSize;
    }

    /**
     * 获取下载总大小
     *
     * @return 单位：Byte
     */
    public long getTotalSize() {
        return downInfo.totalSize;
    }

    /**
     * 获得格式化的总Size
     *
     * @return example: 2KB , 10MB
     */
    public String getFormatTotalSize() {
        return Utils.formatSize(downInfo.totalSize);
    }

    /**
     * 获得格式化的下载完成的大小
     *
     * @return example: 2KB , 10MB
     */
    public String getFormatDownloadSize() {
        return Utils.formatSize(downInfo.downloadSize);
    }

    /**
     * 获得格式化的状态字符串
     *
     * @return example: 2MB/36MB
     */
    public String getFormatStatusString() {
        return Utils.formatSize(downInfo.downloadSize) + "/" + Utils.formatSize(downInfo.totalSize);
    }

    /**
     * 获得下载的百分比, 保留两位小数
     *
     * @return example: 5.25%
     */
    public float getPercent() {
        float result;
        if (downInfo.totalSize <= 0L) {
            result = 0.0f;
        } else {
            result = downInfo.downloadSize * 1.0f / downInfo.totalSize;
        }
        return result;
    }

    /**
     * 获取下载速度
     *
     * @return
     */
    public String getFormatSpeed() {
        return Utils.formatSpeed(downInfo.curSpeedSize);
    }

    /**
     * 返回即时下载速度
     * 单位：Bytes/s
     *
     * @return
     */
    public float getSpeed() {
        return downInfo.curSpeedSize;
    }
}

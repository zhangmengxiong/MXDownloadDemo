package com.mx.download.model;


import com.mx.download.utils.Utils;

/**
 * 保存从下载地址读取出来的信息的对象
 * <p>
 * User: zhangmengxiong
 * Date: 2016-07-15
 * Time: 15:48
 * 表示下载状态, 如果isChunked为true, totalSize 可能不存在
 */
public class InfoBean {
    public boolean isChunked = false; // 未知大小的文件
    private String lastModify = "";
    private long totalSize = 0L;
    private long downloadSize = 0L;
    public boolean isAcceptRanges = true;

    private long timeTag = 0L;
    private long sizeTag = 0L;
    private long speed = 0L;

    public InfoBean() {
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getDownloadSize() {
        return downloadSize;
    }

    public void setDownloadSize(long downloadSize) {
        this.downloadSize = downloadSize;
    }

    public void setLastModify(String lastModify) {
        this.lastModify = lastModify;
    }

    public String getLastModify() {
        return lastModify;
    }

    /**
     * 获得格式化的总Size
     *
     * @return example: 2KB , 10MB
     */
    public String getFormatTotalSize() {
        return Utils.formatSize(totalSize);
    }

    public String getFormatDownloadSize() {
        return Utils.formatSize(downloadSize);
    }

    /**
     * 获得格式化的状态字符串
     *
     * @return example: 2MB/36MB
     */
    public String getFormatStatusString() {
        return getFormatDownloadSize() + "/" + getFormatTotalSize();
    }

    public boolean isChunked() {
        return isChunked;
    }

    public boolean isSupportRanges() {
        return isAcceptRanges;
    }

    /**
     * 计算网速
     */
    public void computeSpeed() {
        long timeDiff = Math.abs(Utils.currentCPUTimeMillis() - timeTag); //单位：秒
        long sizeDiff = (downloadSize - sizeTag); // Bytes

        if (timeDiff > 2) {
            if (timeTag > 0 && sizeTag > 0) {
                speed = sizeDiff / timeDiff;
            }

            timeTag = Utils.currentCPUTimeMillis();
            sizeTag = downloadSize;
        }
    }

    /**
     * 清理网速计算标记
     */
    public void cleanSpeed() {
        timeTag = 0L;
        sizeTag = 0L;
        speed = 0L;
    }

    /**
     * 获取下载速度
     * 单位：KB/s
     *
     * @return
     */
    public String getFormatSpeed() {
        return Utils.formatSpeed(getSpeed());
    }

    /**
     * 返回即时下载速度
     * 单位：Bytes/s
     *
     * @return
     */
    public long getSpeed() {
        if (timeTag <= 0 || sizeTag <= 0 || Math.abs(timeTag - Utils.currentCPUTimeMillis()) > 3)
            speed = 0L;
        return speed;
    }

    /**
     * 获得下载的百分比, 保留两位小数
     *
     * @return example: 5.25%
     */
    public float getPercent() {
        float result;
        if (totalSize == 0L) {
            result = 0.0f;
        } else {
            result = downloadSize * 1.0f / totalSize;
        }
        return result;
    }
}

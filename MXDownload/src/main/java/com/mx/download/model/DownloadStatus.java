package com.mx.download.model;

import static com.mx.download.utils.Utils.formatSize;


/**
 * User: Season(ssseasonnn@gmail.com)
 * Date: 2016-07-15
 * Time: 15:48
 * 表示下载状态, 如果isChunked为true, totalSize 可能不存在
 */
public class DownloadStatus {
    public boolean isChunked = false; // 未知大小的文件
    private long totalSize = 0L;
    private long downloadSize = 0L;

    public DownloadStatus() {
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

    /**
     * 获得格式化的总Size
     *
     * @return example: 2KB , 10MB
     */
    public String getFormatTotalSize() {
        return formatSize(totalSize);
    }

    public String getFormatDownloadSize() {
        return formatSize(downloadSize);
    }

    /**
     * 获得格式化的状态字符串
     *
     * @return example: 2MB/36MB
     */
    public String getFormatStatusString() {
        return getFormatDownloadSize() + "/" + getFormatTotalSize();
    }

    /**
     * 获得下载的百分比, 保留两位小数
     *
     * @return example: 5.25%
     */
    public Double getPercent() {
        Double result;
        if (totalSize == 0L) {
            result = 0.0;
        } else {
            result = downloadSize * 1.0 / totalSize;
        }
        return result;
    }

    public boolean isComplete() {
        return totalSize == downloadSize;
    }
}

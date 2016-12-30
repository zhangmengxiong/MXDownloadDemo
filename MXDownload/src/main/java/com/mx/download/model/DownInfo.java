package com.mx.download.model;


/**
 * 保存从下载地址读取出来的信息的对象
 * <p>
 * User: zhangmengxiong
 * Date: 2016-07-15
 * Time: 15:48
 * 表示下载状态, 如果isChunked为true, totalSize 可能不存在
 */
public class DownInfo {
    public boolean isUnknownSize = false; // 未知大小的文件
    public String lastModify = "";
    public String Etag = "";
    public long totalSize = 0L; // 总大小
    public long downloadSize = 0L; // 下载完成大小
    public boolean isAcceptRanges = true; // 是否可以断点下载
    public float curSpeedSize = 0f; // 当前下载速度

    private NetSpeedBean netSpeedBean = new NetSpeedBean(); // 网速计数器
    private InfoBean infoBean; // 回调返回对象

    /**
     * 计算网速
     */
    public void computeSpeed() {
        netSpeedBean.addNode(downloadSize);
        curSpeedSize = netSpeedBean.getAverageSpeed();
    }

    /**
     * 清理网速计算标记
     */
    public void cleanSpeed() {
        netSpeedBean.resetSpeed();
        curSpeedSize = 0f;
    }

    public boolean compareLastModify(String s) {
        return ("" + lastModify).equals(s);
    }

    public boolean compareEtag(String s) {
        return ("" + Etag).equals(s);
    }

    public InfoBean getInfoBean() {
        if (infoBean == null) infoBean = new InfoBean(this);
        return infoBean;
    }
}

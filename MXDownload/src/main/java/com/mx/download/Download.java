//开始下载文件
package com.mx.download;

import com.mx.download.factory.IDownload;
import com.mx.download.factory.MultyDownload;
import com.mx.download.factory.SingleDownload;
import com.mx.download.model.DownloadBean;
import com.mx.download.model.DownloadStatus;
import com.mx.download.utils.FileUtil;
import com.mx.download.utils.Utils;

import java.io.File;

/**
 * 下载器
 *
 * @author zmx
 */
class Download {

    private DownloadBean downloadBean;
    private IDownload iDownload;
    private String fromUrl;
    private File positionFile;// 记录下载位置文件
    private boolean isSingleThread = false;

    private volatile boolean isUserCancel = false;
    private File cacheFile;
    private DownloadStatus downloadStatus;

    Download(DownloadBean downloadBean) {
        this.downloadBean = downloadBean;
        this.fromUrl = downloadBean.getFromUrl();
        cacheFile = new File(downloadBean.getTempFile());
        positionFile = new File(downloadBean.getCacheFile());// 创建缓存文件，用于记录下载位置
        isSingleThread = downloadBean.isSingleThread();

        isUserCancel = false;
    }

    void startRun() throws Exception {
        // 第一步 创建下载的文件
        prepareFile();

        // 第二部 获取服务器信息
        prepareUrl();

        if (isSingleThread) {
            // 单线程下载器
            iDownload = new SingleDownload();
        } else {
            // 默认多线程下载
            iDownload = new MultyDownload();
        }

        iDownload.setInfo(downloadBean, downloadStatus);

        // 第三步 判断磁盘容量
        iDownload.prepareSave();

        // 第四步 读取历史下载记录
        iDownload.prepareHistory();

        // 第五步 如果是第一次下载，则初始化下载的数据
        iDownload.prepareFirstInit();

        if (MXDownload.DEBUG)
            System.out.println("下载：" + fromUrl + " 初始化成功:" + downloadStatus.getFormatStatusString());

        // 校验用户退出响应
        if (checkCancel()) return;

        iDownload.startDownload();
    }

    private void prepareFile() throws Exception {
        FileUtil.chmod("777", cacheFile);
        FileUtil.chmod("777", positionFile);

        if (!cacheFile.exists()) {
            FileUtil.createFile(cacheFile);
        }
        if (!positionFile.exists()) {
            FileUtil.createFile(positionFile);
        }
    }

    private void prepareUrl() throws Exception {
        downloadStatus = Utils.getFileSize(fromUrl);
        if (downloadStatus == null) {
            throw new Exception("获取服务器信息失败！");
        }
        if (downloadStatus.isChunked()) {
            System.out.println("下载资源大小未知！");
        }
        if (!downloadStatus.isSupportRanges()) {
            System.out.println("下载资源不支持断点下载！");
        }
        System.out.println("下载大小:" + downloadStatus.getFormatTotalSize());
    }

    private boolean checkCancel() {
        return isUserCancel;
    }

    void cancel() {
        isUserCancel = true;
        if (iDownload != null) iDownload.cancel();
    }
}

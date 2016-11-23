//开始下载文件
package com.mx.download;

import com.mx.download.factory.IDownload;
import com.mx.download.factory.MultiDownload;
import com.mx.download.factory.SingleDownload;
import com.mx.download.model.DownloadBean;
import com.mx.download.model.UrlInfoBean;
import com.mx.download.utils.FileUtil;
import com.mx.download.utils.IDownLoadCall;
import com.mx.download.utils.Log;
import com.mx.download.utils.Utils;

import java.io.File;

/**
 * 下载器
 *
 * @author zmx
 */
class Download {
    private IDownLoadCall iDownLoadCall;
    private DownloadBean downloadBean;
    private IDownload iDownload;
    private String fromUrl;
    private File positionFile;// 记录下载位置文件
    private boolean isSingleThread = false;

    private volatile boolean isUserCancel = false;
    private File cacheFile;
    private UrlInfoBean urlInfoBean;

    Download(DownloadBean downloadBean) {
        this.downloadBean = downloadBean;

        iDownLoadCall = downloadBean.getDownLoadCall();
        fromUrl = downloadBean.getFromUrl();
        cacheFile = new File(downloadBean.getTempFile());
        positionFile = new File(downloadBean.getCacheFile());// 创建缓存文件，用于记录下载位置
        isSingleThread = downloadBean.isSingleThread();

        isUserCancel = false;
    }

    void startRun() throws Exception {
        if (iDownLoadCall != null) iDownLoadCall.onPrepare(fromUrl);

        // 第一步 创建下载的文件
        prepareFile();

        // 第二部 获取服务器信息
        prepareUrl();

        if (isSingleThread) {
            // 单线程下载器
            iDownload = new SingleDownload();
        } else {
            // 默认多线程下载
            iDownload = new MultiDownload();
        }

        iDownload.setInfo(downloadBean, urlInfoBean);

        // 第三步 判断磁盘容量
        iDownload.prepareSave();

        // 第四步 读取历史下载记录
        iDownload.prepareHistory();

        // 第五步 如果是第一次下载，则初始化下载的数据
        iDownload.prepareFirstInit();

        Log.v("下载：" + fromUrl + " 初始化成功:" + urlInfoBean.getFormatStatusString());

        // 校验用户退出响应
        if (isUserCancel) {
            if (iDownLoadCall != null)
                iDownLoadCall.onCancel(fromUrl);
            return;
        }

        if (iDownLoadCall != null) iDownLoadCall.onStart(urlInfoBean);

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
        urlInfoBean = Utils.getFileSize(fromUrl);
        if (urlInfoBean == null) {
            throw new Exception("获取服务器信息失败！");
        }
        if (urlInfoBean.isChunked()) {
            Log.v("下载资源大小未知！");
        }
        if (!urlInfoBean.isSupportRanges()) {
            Log.v("下载资源不支持断点下载！");
        }
    }

    void cancel() {
        isUserCancel = true;
        if (iDownload != null) iDownload.cancel();
    }
}

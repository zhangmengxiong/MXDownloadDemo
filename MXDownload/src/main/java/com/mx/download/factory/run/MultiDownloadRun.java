//下载线程
package com.mx.download.factory.run;

import com.mx.download.model.DownChipBean;
import com.mx.download.utils.Log;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 多线程下载的下载器
 */
public class MultiDownloadRun implements Runnable {
    private static final int TIME_OUT = 15 * 1000;// 超时
    private String sourceUrl;   // 下载资源路径
    private String savePath;    // 保存路径
    private String saveFile;    // 保存的文件
    private DownChipBean chipBeen;// 下载位置变量
    private boolean isStop = false;// 该线程外部停止标记
    private boolean errorTag = false;// 该线程外部停止标记

    public MultiDownloadRun(String fromUrl, String savePath, DownChipBean chipBeen) {
        this.sourceUrl = fromUrl;
        this.savePath = savePath;
        this.chipBeen = chipBeen;
        this.isStop = false;
        this.errorTag = false;

        saveFile = new File(savePath).getName();
    }

    @Override
    public void run() {
        // 执行前第一次判断是否下载完，如果下载完了 就直接返回
        if (chipBeen.isComplete()) {
            isStop = false;
            errorTag = false;
            return;
        }
        // 如果用户取消了  直接返回
        if (isStop) {
            Log.v(saveFile + " -- " + chipBeen + "被终止");
            return;
        }
        Log.v(saveFile + " -- " + chipBeen + "开始执行");

        SaveFile saveFile = null;
        try {
            saveFile = new SaveFile(savePath, chipBeen.getSeek(), chipBeen.end);

            URL url = new URL(this.sourceUrl);// 创建URL对象
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();// 创建URL连接
            conn.setConnectTimeout(TIME_OUT);// 设置连接超时时间为10000ms
            conn.setReadTimeout(TIME_OUT); // 设置读取数据超时时间为10000ms
            String property = "bytes=" + chipBeen.getSeek() + "-" + chipBeen.end;// 开始下载位置
            conn.setRequestProperty("RANGE", property);
            InputStream is = conn.getInputStream(); // 获取文件输入流，读取文件内容
            byte[] buff = new byte[1024 * 16];// 创建缓冲区
            int length;
            while (((length = is.read(buff)) > 0)) {
                saveFile.write(buff, length); // 写入文件内容
                chipBeen.addDownloadSize(length); // 新增下载完成的长度
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(this.saveFile + " -- " + chipBeen + "出现错误！即将退出线程。");
            errorTag = true;
        } finally {
            if (saveFile != null) {
                saveFile.close();// 关闭打开的文件
            }
        }
        Log.v(this.saveFile + " -- " + "执行结束:" + chipBeen);
    }

    public boolean isDownloadOver()// 返回该线程下载是否完成的标志
    {
        return chipBeen.isComplete();
    }

    public void stop() {
        this.isStop = true;
    }

    public boolean isInError() {
        return errorTag;
    }
}

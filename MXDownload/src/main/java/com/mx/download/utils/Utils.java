package com.mx.download.utils;

import com.mx.download.model.InfoBean;

import java.io.Closeable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

public class Utils {
    public static String formatSize(long size) {
        String hrSize;

        double k = size / 1024.0;
        double m = ((size / 1024.0) / 1024.0);
        double g = (((size / 1024.0) / 1024.0) / 1024.0);
        double t = ((((size / 1024.0) / 1024.0) / 1024.0) / 1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if (t > 1) {
            hrSize = dec.format(t).concat(" TB");
        } else if (g > 1) {
            hrSize = dec.format(g).concat(" GB");
        } else if (m > 1) {
            hrSize = dec.format(m).concat(" MB");
        } else {
            hrSize = dec.format(k).concat(" KB");
        }
        return hrSize;
    }

    public static String formatSpeed(float size) {
        String hrSize;

        double k = size / 1024.0;
        double m = (k / 1024.0);
        double g = (m / 1024.0);
        double t = (g / 1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if (t > 1) {
            hrSize = dec.format(t).concat(" TB/s");
        } else if (g > 1) {
            hrSize = dec.format(g).concat(" GB/s");
        } else if (m > 1) {
            hrSize = dec.format(m).concat(" MB/s");
        } else {
            hrSize = dec.format(k).concat(" KB/s");
        }
        return hrSize;
    }

    public static void closeSilent(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }

    static boolean isChunked(HttpURLConnection connection) {
        String range = connection.getHeaderField("Transfer-Encoding");
        return range != null && range.equalsIgnoreCase("chunked");
    }

    static long getContentLength(HttpURLConnection connection) {
        long l;
        try {
            l = Long.parseLong(connection.getHeaderField("Content-Length"));
        } catch (Exception ignored) {
            l = -1;
        }
        return l;
    }

    static String getLastModify(HttpURLConnection connection) {
        return connection.getHeaderField("Last-Modified");
    }

    static boolean isAcceptRanges(HttpURLConnection connection) {
        String range = connection.getHeaderField("Accept-Ranges");
        return range != null && range.equalsIgnoreCase("bytes");
    }

    /**
     * 从网络上获取文件的大小
     *
     * @param fromUrl
     * @return
     */
    public static InfoBean getFileSize(String fromUrl) {
        InfoBean status = null;
        try {
            URL url = new URL(fromUrl);// 获取资源路径
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();// 创建URL连接
            int stateCode = conn.getResponseCode();// 获取响应信息
            if (stateCode == HttpURLConnection.HTTP_OK) {
                status = new InfoBean();
                status.setLastModify(getLastModify(conn));

                long maxSize = getContentLength(conn);
                status.isChunked = (isChunked(conn) || maxSize <= 0);
                status.isAcceptRanges = isAcceptRanges(conn);

                status.setDownloadSize(0L);
                status.setTotalSize(maxSize);
            }
        } catch (Exception e) {
            e.printStackTrace();
            status = null;
        }
        return status;
    }

    /**
     * 获取当前CPU的时间点
     * 单位：秒
     *
     * @return
     */
    public static float currentCPUTimeMillis() {
        return (System.nanoTime() / 1000000000f);
    }
}

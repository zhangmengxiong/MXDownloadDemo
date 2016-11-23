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
        } else if (k > 1) {
            hrSize = dec.format(k).concat(" KB");
        } else {
            hrSize = dec.format(size).concat(" Bytes");
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
        String length = connection.getHeaderField("Content-Length");
        return Long.parseLong((length == null || length.length() <= 0) ? "-1" : length);
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
}

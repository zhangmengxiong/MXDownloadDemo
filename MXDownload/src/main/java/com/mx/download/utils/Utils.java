package com.mx.download.utils;

import com.mx.download.model.DownloadStatus;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {
    public static String formatSize(long size) {
        String hrSize;

        double b = size;
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
            hrSize = dec.format(b).concat(" Bytes");
        }
        return hrSize;
    }

    public static String formatSpeed(long speed) {
        return formatSpeed(speed) + "/s";
    }

    public static String longToGMT(long lastModify) {
        Date d = new Date(lastModify);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(d);
    }

    public static long GMTToLong(String GMT) throws ParseException {
        if (GMT == null || "".equals(GMT)) {
            return new Date().getTime();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = sdf.parse(GMT);
        return date.getTime();
    }

    public static void close(Closeable closeable) throws IOException {
        if (closeable != null) {
            closeable.close();
        }
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
    public static DownloadStatus getFileSize(String fromUrl) {
        DownloadStatus status = null;
        try {
            URL url = new URL(fromUrl);// 获取资源路径
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();// 创建URL连接
            int stateCode = conn.getResponseCode();// 获取响应信息
            if (stateCode == HttpURLConnection.HTTP_OK) {
                status = new DownloadStatus();
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

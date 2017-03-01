package com.mx.download.utils;

import android.text.TextUtils;

import com.mx.download.model.DownInfo;

import java.io.Closeable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

public class Utils {
    public static String formatSize(long size) {
        if (size < 0) size = 0L;
        String hrSize;

        double k = size / 1024.0;
        double m = (k / 1024.0);
        double g = (m / 1024.0);
        double t = (g / 1024.0);

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
        if (size < 0) size = 0L;
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

    private static boolean isChunked(HttpURLConnection connection) {
        String result = null;
        for (String s : connection.getHeaderFields().keySet()) {
            if (!TextUtils.isEmpty(s) && s.equalsIgnoreCase("Transfer-Encoding")) {
                result = connection.getHeaderField(s).trim();
                break;
            }
        }

        return result != null && result.equalsIgnoreCase("chunked");
    }

    private static long getContentLength(HttpURLConnection connection) {
        String result = null;
        for (String s : connection.getHeaderFields().keySet()) {
            if (!TextUtils.isEmpty(s) && s.equalsIgnoreCase("Content-Length")) {
                result = connection.getHeaderField(s).trim();
                break;
            }
        }

        try {
            return Long.parseLong(result);
        } catch (Exception ignored) {
        }
        return 0L;
    }

    private static String getLastModify(HttpURLConnection connection) {
        String result = null;
        for (String s : connection.getHeaderFields().keySet()) {
            if (!TextUtils.isEmpty(s) && s.equalsIgnoreCase("Last-Modified")) {
                result = connection.getHeaderField(s).trim();
                break;
            }
        }
        return result;
    }

    private static String getEtag(HttpURLConnection connection) {
        String result = null;
        for (String s : connection.getHeaderFields().keySet()) {
            if (!TextUtils.isEmpty(s) && s.equalsIgnoreCase("etag")) {
                result = connection.getHeaderField(s).trim();
                break;
            }
        }
        return result;
    }

    private static boolean isAcceptRanges(HttpURLConnection connection) {
        String result = null;
        for (String s : connection.getHeaderFields().keySet()) {
            if (!TextUtils.isEmpty(s) && s.equalsIgnoreCase("Accept-Ranges")) {
                result = connection.getHeaderField(s).trim();
                break;
            }
        }

        return result != null && result.equalsIgnoreCase("bytes");
    }

    /**
     * 从网络上获取文件的大小
     *
     * @param fromUrl
     * @return
     */
    public static DownInfo getFileSize(String fromUrl, int time_out) {
        DownInfo info = null;
        try {
            URL url = new URL(fromUrl);// 获取资源路径
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();// 创建URL连接
            conn.setRequestProperty("Accept-Ranges", "bytes");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setReadTimeout(time_out);
            conn.setConnectTimeout(time_out);
            conn.connect();
            int stateCode = conn.getResponseCode();// 获取响应信息
            if (stateCode == HttpURLConnection.HTTP_OK) {
                info = new DownInfo();
                info.lastModify = (getLastModify(conn));
                info.Etag = (getEtag(conn));

                long maxSize = getContentLength(conn);
                info.isUnknownSize = (isChunked(conn) || maxSize <= 0);
                info.isAcceptRanges = isAcceptRanges(conn);

                info.downloadSize = 0;
                info.totalSize = (maxSize <= 0 ? 0 : maxSize);

                Log.v(conn.getHeaderFields().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            info = null;
        }
        return info;
    }
}

package com.mx.download.utils;

import com.mx.download.model.ChipSaveMod;
import com.mx.download.model.DownChipBean;
import com.mx.download.model.DownloadStatus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by zmx_f on 2016-5-12.
 */
public class FileUtil {
    /**
     * 获取缓存文件路径
     *
     * @param toPath
     * @return
     */
    public static String getDownCacheFile(String toPath) {
        return toPath + ".tmp";
    }

    /**
     * 获取断点续传文件路径
     *
     * @param toPath
     * @return
     */
    public static String getPositionFile(String toPath) {
        return toPath + ".pos";
    }

    /**
     * 将文件分段，返回分段的数组
     *
     * @param fileLength
     * @return
     */
    public static DownChipBean[] getDownloadPosition(long fileLength) {
        if (fileLength <= 0) {
            return null;
        }

        int len = getFragmentSize(fileLength);

        DownChipBean[] result = new DownChipBean[len];

        for (int i = 0; i < len; i++) {
            result[i] = new DownChipBean();
            result[i].index = i;

            long size = i * (fileLength / len);
            result[i].start = size;
            // 设置最后一个结束点的位置
            if (i == len - 1) {
                result[i].end = fileLength;
            } else {
                size = (i + 1) * (fileLength / len);
                result[i].end = size;
            }
        }
        return result;
    }

    /**
     * 获取碎片数量
     *
     * @param l
     * @return
     */
    private static int getFragmentSize(long l) {
        int mb = (int) (l / ((float) 1024 * 1024));
        int size = 1;
        if (mb > 10) {
            size = (mb / 20) + 1;
        }
        if (size > 10) size = 10;
        return size;
    }

    /**
     * 将文件大小重置为0
     *
     * @param file
     */
    public static void resetFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) return;
        RandomAccessFile file1 = null;
        try {
            file1 = new RandomAccessFile(file, "rw");
            file1.setLength(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (file1 != null) {
                    file1.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 从文件中读取断点信息
     *
     * @param positionFile
     * @return
     */
    public static ChipSaveMod readDownloadPosition(File positionFile) {
        ChipSaveMod result = new ChipSaveMod();

        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(positionFile));
            int spilSize = Integer.valueOf(dis.readLine());// 获取下载位置的数目，即有多少个开始位置，多少个结束位置
            result.fileSize = Long.valueOf(dis.readLine());
            result.completeSize = Long.valueOf(dis.readLine());
            result.LastModify = dis.readLine();

            result.downChipBeen = new DownChipBean[spilSize];
            for (int i = 0; i < spilSize; i++) {
                result.downChipBeen[i] = DownChipBean.fromString(dis.readLine());
                if (result.downChipBeen[i] == null) return null;
            }

            dis.close();
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    /**
     * 将断点信息写入文件
     *
     * @param positionFile
     * @param chipBeens
     * @param status
     */
    public static void writeDownloadPosition(File positionFile, DownChipBean[] chipBeens, DownloadStatus status) {
        try {
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(positionFile));
            dos.writeBytes("" + chipBeens.length + "\r\n");
            dos.writeBytes("" + status.getTotalSize() + "\r\n");
            dos.writeBytes("" + status.getLastModify() + "\r\n");

            long complete = 0L;
            for (DownChipBean chipBeen : chipBeens) {
                complete = complete + chipBeen.completeSize;
            }
            dos.writeBytes("" + complete + "\r\n");

            for (DownChipBean chipBeen : chipBeens) {
                dos.writeBytes(chipBeen.toSaveString() + "\r\n");
            }
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除文件
     *
     * @param file
     */
    public static void deleteFile(File file) {
        if (file != null && file.exists()) file.delete();
    }

    /**
     * 创建一个大小为0的文件
     *
     * @param file
     * @throws IOException
     */
    public static boolean createFile(File file) throws IOException {
        if (file == null) return false;
        File file1 = file.getParentFile();
        if (!file1.exists()) file1.mkdirs();
        return file.createNewFile();
    }

    public static void chmod(String s, File desFile) {
        try {
            String command = "chmod " + s + " " + desFile.getAbsolutePath();
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

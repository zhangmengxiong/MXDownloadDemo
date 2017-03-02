package com.mx.download.utils;

import com.mx.download.model.DownChipBean;
import com.mx.download.model.DownInfo;
import com.mx.download.model.DownType;
import com.mx.download.model.SaveBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;

/**
 * Created by zmx_f on 2016-5-12.
 */
public class FileUtil {
    /**
     * 将文件分段，返回分段的数组
     *
     * @param maxSize
     * @return
     */
    public static DownChipBean[] getDownloadPosition(long maxSize) {
        if (maxSize <= 0) {
            return null;
        }

        int len = getFragmentSize(maxSize);

        DownChipBean[] result = new DownChipBean[len];
        long preEnd = 0L;
        for (int i = 0; i < len; i++) {
            result[i] = new DownChipBean();
            result[i].index = i;

            long size = i * (maxSize / len);
            if (preEnd > 0) size = preEnd + 1;
            result[i].start = size;
            // 设置最后一个结束点的位置
            if (i == len - 1) {
                result[i].end = maxSize - 1;
            } else {
                size = (i + 1) * (maxSize / len);
                result[i].end = size;
            }
            preEnd = result[i].end;
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
        int mb = (int) (l / (1024 * 1024));
        int size = 1;
        if (mb > 10) {
            size = (mb / 20) + 1;
        }
        if (size > 20) size = 20;
        if (size < 1) size = 1;
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
    public static SaveBean readDownloadPosition(File positionFile) {
        try {
            ObjectInputStream oin = new ObjectInputStream(new FileInputStream(positionFile));
            Object obj = oin.readObject(); // 没有强制转换到Person类型
            oin.close();
            return (SaveBean) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将断点信息写入文件
     *
     * @param positionFile
     * @param been
     * @param bean
     */
    public static void writeMulityPosition(File positionFile, DownChipBean[] been, DownInfo bean) {
        try {
            SaveBean saveMod = new SaveBean();
            saveMod.downChipBeen = been;
            saveMod.type = DownType.TYPE_MULITY;
            saveMod.LastModify = bean.lastModify;
            saveMod.Etag = bean.Etag;
            saveMod.fileSize = bean.totalSize;
            saveMod.completeSize = bean.downloadSize;

            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(positionFile));
            stream.writeObject(saveMod);
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将断点信息写入文件
     *
     * @param positionFile
     * @param chipBean
     * @param bean
     */
    public static void writeSinglePosition(File positionFile, DownChipBean chipBean, DownInfo bean) {
        try {
            SaveBean saveMod = new SaveBean();
            saveMod.downChipBeen = new DownChipBean[]{chipBean};
            saveMod.type = DownType.TYPE_SINGLE;
            saveMod.LastModify = bean.lastModify;
            saveMod.Etag = bean.Etag;
            saveMod.fileSize = bean.totalSize;
            saveMod.completeSize = bean.downloadSize;

            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(positionFile));
            stream.writeObject(saveMod);
            stream.close();
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

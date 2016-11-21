//写入文件内容
package com.mx.download;

import com.mx.download.utils.Utils;

import java.io.RandomAccessFile;

class SaveFile {
    private RandomAccessFile save;// 保存的文件
    private long maxSize = 0L;
    private long curSize = 0L;

    SaveFile(String path, long start, long end) throws Exception {
        save = new RandomAccessFile(path, "rws");
        save.seek(start);

        maxSize = end - start + 1;
    }

    void write(byte[] buff, int length) throws Exception// 写入文件内容
    {

        if (curSize + length > maxSize) {
            length = (int) (maxSize - curSize);
            curSize = maxSize;
        } else {
            curSize = curSize + length;
        }
        save.write(buff, 0, length);
    }

    void close()// 关闭文件
    {
        Utils.closeSilent(save);
    }
}

//写入文件内容
package com.mx.download.factory.run;

import com.mx.download.utils.Utils;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

class SaveFile {
    private RandomAccessFile save = null;// 保存的文件
    private FileChannel saveChannel = null;
    private MappedByteBuffer saveBuffer = null;
    private long maxSize = 0L;
    private long curSize = 0L;

    SaveFile(String path, long start, long end) throws Exception {
        maxSize = end - start;
        save = new RandomAccessFile(path, "rws");
        saveChannel = save.getChannel();
        saveBuffer = saveChannel.map(FileChannel.MapMode.READ_WRITE, start, maxSize);
//        save.seek(start);
        if (end <= 0) maxSize = -1;
    }

    void write(byte[] buff, int length) throws Exception// 写入文件内容
    {
        if (maxSize > 0) {
            if (curSize + length > maxSize) {
                length = (int) (maxSize - curSize);
                curSize = maxSize;
            } else {
                curSize = curSize + length;
            }
        }
//        save.write(buff, 0, length);
        saveBuffer.put(buff, 0, length);
    }

    void close()// 关闭文件
    {
        Utils.closeSilent(save);
        Utils.closeSilent(saveChannel);
    }
}

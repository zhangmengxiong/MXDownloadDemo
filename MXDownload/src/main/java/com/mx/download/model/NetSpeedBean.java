package com.mx.download.model;

import java.util.LinkedList;

/**
 * 网速计算帮助类
 * 创建人： zhangmengxiong
 * 创建时间： 2016-12-21.
 * 联系方式: zmx_final@163.com
 */
class NetSpeedBean {
    private static final int MAX_SIZE = 4;
    private final Object SYNC_OBJ = new Object();
    private LinkedList<NodeBean> speedQueue = new LinkedList<>();

    /**
     * 新增一个节点！
     *
     * @param size
     */
    void addNode(long size) {
        synchronized (SYNC_OBJ) {
            if (speedQueue.size() >= MAX_SIZE) {
                speedQueue.removeFirst();
            }
            speedQueue.add(new NodeBean(currentCPUTimeMillis(), size));
        }
    }

    /**
     * 重置计数器
     */
    void resetSpeed() {
        synchronized (SYNC_OBJ) {
            speedQueue.clear();
        }
    }

    /**
     * 获取最后x个节点的平均网速
     *
     * @return
     */
    float getAverageSpeed() {
        NodeBean first, last;
        synchronized (SYNC_OBJ) {
            if (speedQueue.size() < 2) return 0f;
            first = speedQueue.getLast();
            last = speedQueue.getFirst();
        }

        float timeDiff = Math.abs(first.TIME_TAG - last.TIME_TAG); //单位：秒
        long sizeDiff = Math.abs(first.SIZE_TAG - last.SIZE_TAG); // Bytes
        return (timeDiff > 0) ? (sizeDiff / timeDiff) : 0f;
    }

    /**
     * 获取当前CPU的时间点
     * 单位：秒
     *
     * @return
     */
    private float currentCPUTimeMillis() {
        return (System.nanoTime() / 1000000000f);
    }

    /**
     * 节点类
     */
    private class NodeBean {
        NodeBean(float timeTag, long sizeTag) {
            TIME_TAG = timeTag;
            SIZE_TAG = sizeTag;
        }

        float TIME_TAG = 0f;
        long SIZE_TAG = 0L;
    }
}

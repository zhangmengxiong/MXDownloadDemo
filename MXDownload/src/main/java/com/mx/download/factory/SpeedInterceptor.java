package com.mx.download.factory;

/**
 * 网速拦截器~
 * 原理：动态调整下载线程sleep的时间
 * <p>
 * 创建人： zhangmengxiong
 * 创建时间： 2016-12-14.
 * 联系方式: zmx_final@163.com
 */

public class SpeedInterceptor {
    private volatile int maxSpeed = 0;
    private volatile int sleepTime = 0;

    /**
     * @param maxSpeed 单位：KB/s
     */
    SpeedInterceptor(int maxSpeed) {
        this.maxSpeed = maxSpeed;
        if (maxSpeed > 0) {
            sleepTime = 500;
        }
    }

    /**
     * @param curSpeed 单位：KB/s
     */
    void setCurrentSpeed(int curSpeed) {
        if (maxSpeed <= 0) return;
        int diff = Math.abs(maxSpeed - curSpeed);
        if (diff < 5) return;
        diff = (int) Math.sqrt(diff);

        sleepTime += ((curSpeed > maxSpeed) ? diff : -1 * diff);
        if (sleepTime <= 0) {
            sleepTime = 0;
        }
        if (sleepTime > 1000) {
            sleepTime = 990;
        }
    }

    /**
     * 拦截！
     *
     * @throws InterruptedException
     */
    public void interceptor() throws InterruptedException {
        if (sleepTime > 0) {
            Thread.sleep(sleepTime);
        }
    }
}

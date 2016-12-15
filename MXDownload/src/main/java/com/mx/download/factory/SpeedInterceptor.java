package com.mx.download.factory;

/**
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
    }

    /**
     * @param curSpeed 单位：KB/s
     */
    void setCurrentSpeed(int curSpeed) {
        if (maxSpeed <= 0) return;
        if (Math.abs(maxSpeed - curSpeed) < 10) return;

        sleepTime += (curSpeed > maxSpeed) ? 5 : -5;
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

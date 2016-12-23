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
            sleepTime = 100;
        }
    }

    /**
     * @param curSpeed 单位：KB/s
     */
    void setCurrentSpeed(int curSpeed) {
        if (maxSpeed <= 0) return;
        int diff = Math.abs(maxSpeed - curSpeed);
        if (diff < 20) return;

        if (diff > 400) {
            diff = 5;
        } else if (diff > 200) {
            diff = 3;
        } else if (diff > 100) {
            diff = 2;
        } else {
            diff = 1;
        }

        sleepTime += ((curSpeed > maxSpeed) ? diff : -diff);
        if (sleepTime <= 0) {
            sleepTime = 0;
        }
        if (sleepTime > 1000) {
            sleepTime = 900;
        }
    }

    /**
     * 拦截！
     */
    public void interceptor() {
        if (sleepTime > 0) {
            try {
                Thread.sleep(sleepTime);
            } catch (Exception ignored) {
            }
        }
    }
}

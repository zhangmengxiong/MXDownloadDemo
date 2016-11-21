package com.mx.download.utils;

public enum DownStatus {
	/**
	 * 排队等待下载中...
	 */
	WAIT_DOWN,
	/**
	 * 初始化准备中
	 */
	PREPARE, 
	/**
	 * 正在下载
	 */
	DOWNING,
	/**
	 * 下载出错
	 */
	ERROR, 
	/**
	 * 下载完成
	 */
	FINISH, 
	/**
	 * 被终止
	 */
	USER_STOP
}

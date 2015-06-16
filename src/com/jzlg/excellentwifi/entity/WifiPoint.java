package com.jzlg.excellentwifi.entity;

/**
 * 雷达WIFI信息点坐标
 * 
 * @author 宋春鹏
 *
 */
public class WifiPoint {

	private int x;

	private int y;

	public WifiPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

}

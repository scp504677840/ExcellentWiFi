package com.jzlg.excellentwifi.entity;

import org.litepal.crud.DataSupport;

/**
 * WIFI信号强弱历史图
 * 
 * @author
 *
 */
public class WIFILine extends DataSupport {

	private String macAddress;
	private int seconds;
	private int level;

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public int getSeconds() {
		return seconds;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

}

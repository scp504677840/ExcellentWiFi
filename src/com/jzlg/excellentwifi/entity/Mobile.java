package com.jzlg.excellentwifi.entity;

import java.util.Date;

import org.litepal.crud.DataSupport;

/**
 * 手机信息实体类
 * 
 * @author 宋春鹏
 *
 */
public class Mobile extends DataSupport {
	private String moblie_mac;// 手机MAC地址
	private String moblie_ip;// 手机IP地址
	private String moblie_date;// 时间
	private Wifi wifi;// Wifi类

	public String getMoblie_mac() {
		return moblie_mac;
	}

	public void setMoblie_mac(String moblie_mac) {
		this.moblie_mac = moblie_mac;
	}

	public String getMoblie_ip() {
		return moblie_ip;
	}

	public void setMoblie_ip(String moblie_ip) {
		this.moblie_ip = moblie_ip;
	}

	public String getMoblie_date() {
		return moblie_date;
	}

	public void setMoblie_date(String moblie_date) {
		this.moblie_date = moblie_date;
	}

	public Wifi getWifi() {
		return wifi;
	}

	public void setWifi(Wifi wifi) {
		this.wifi = wifi;
	}
}
package com.jzlg.excellentwifi.entity;

import java.util.ArrayList;
import java.util.List;

import org.litepal.crud.DataSupport;

/**
 * WIFI信息实体类
 * 
 * @author 宋春鹏
 *
 */
public class Wifi extends DataSupport {
	private String wifi_mac;// MAC地址
	private String wifi_pwd;// 密码
	private double wifi_latitude;// 纬度
	private double wifi_longitude;// 经度
	private String wifi_addrstr;// 地址

	private List<Mobile> mobileList = new ArrayList<Mobile>();

	public String getWifi_mac() {
		return wifi_mac;
	}

	public void setWifi_mac(String wifi_mac) {
		this.wifi_mac = wifi_mac;
	}

	public String getWifi_pwd() {
		return wifi_pwd;
	}

	public void setWifi_pwd(String wifi_pwd) {
		this.wifi_pwd = wifi_pwd;
	}

	public double getWifi_latitude() {
		return wifi_latitude;
	}

	public void setWifi_latitude(double wifi_latitude) {
		this.wifi_latitude = wifi_latitude;
	}

	public double getWifi_longitude() {
		return wifi_longitude;
	}

	public void setWifi_longitude(double wifi_longitude) {
		this.wifi_longitude = wifi_longitude;
	}

	public String getWifi_addrstr() {
		return wifi_addrstr;
	}

	public void setWifi_addrstr(String wifi_addrstr) {
		this.wifi_addrstr = wifi_addrstr;
	}

	public List<Mobile> getMobileList() {
		return mobileList;
	}

	public void setMobileList(List<Mobile> mobileList) {
		this.mobileList = mobileList;
	}

}
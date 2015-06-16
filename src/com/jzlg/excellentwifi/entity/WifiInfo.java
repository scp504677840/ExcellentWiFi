package com.jzlg.excellentwifi.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * WIFI经纬度覆盖物类
 * 
 * @author 宋春鹏
 *
 */
public class WifiInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 420265804065545702L;
	private String name;
	private double latitude;
	private double longitude;

	public WifiInfo(String name, double latitude, double longitude) {
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
}
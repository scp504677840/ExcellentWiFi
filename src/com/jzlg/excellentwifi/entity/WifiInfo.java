package com.jzlg.excellentwifi.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WifiInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 420265804065545702L;
	private String name;
	private double latitude;
	private double longitude;

	public static List<WifiInfo> infos = new ArrayList<WifiInfo>();
	static {
		infos.add(new WifiInfo("地点1", 30.4477, 112.262936));
		infos.add(new WifiInfo("地点2", 30.3377, 112.252936));
		infos.add(new WifiInfo("地点3", 30.2277, 112.242936));
		infos.add(new WifiInfo("地点4", 30.1177, 112.232936));
	}

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
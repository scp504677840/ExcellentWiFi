package com.jzlg.excellentwifi.dao;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import com.jzlg.excellentwifi.entity.Wifi;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class WifiDao {
	SQLiteDatabase db = Connector.getDatabase();
	public static void main(String[] args) {
		Wifi wifi = new Wifi();
		wifi.setWifi_mac("123456789");
		wifi.updateAll("wifi_mac","123");
		wifi.setToDefault("");
		wifi.save();
		
		ContentValues values = new ContentValues();
		values.put("wifi_mac", "987654321");
		DataSupport.updateAll(Wifi.class, values, "wifi_mac=?","123");
	}
	
	public void insert(){
		
	}
}

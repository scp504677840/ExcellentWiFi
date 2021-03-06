package com.jzlg.excellentwifi.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.jzlg.excellentwifi.R;

/**
 * SSID:Service Set ID 服务集识别码 WEP:Wired Equivalent Privacy无线安全
 * 2种强度：40bits和104bits WEP2：128bit WAP/WAP2无线安全 WPS WMM
 * 
 * @author 郭旭、宋春鹏、王大伟
 *
 */
public class WifiAdmin {
	// 定义一个WifiManager
	private WifiManager mWifiManager;
	// 定义一个WifiInfo对象
	private WifiInfo mWifiInfo;
	// 扫描出的网络连接列表
	private List<ScanResult> mWifiList;
	// 网络连接列表
	private List<WifiConfiguration> mWifiConfigurations;
	// WifiLock[阻止Wifi进入睡眠状态，使Wifi一直处于活跃状态]
	WifiLock mWifiLock;
	private Context mContext;
	private WifiConfiguration mWifiConfig;
	private LinearLayout mShowCView;
	private PopupWindow mShowCPopu = null;

	public WifiAdmin(Context context) {
		// 获取WifiManager对象
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		// 获取WifiInfo对象
		mWifiInfo = mWifiManager.getConnectionInfo();
		mContext = context;
	}

	public WifiManager getWifiManager(){
		return mWifiManager;
	}
	/**
	 * 打开wifi
	 */
	public void openWifi() {
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	/**
	 * 关闭wifi
	 */
	public void stopWifi() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}

	/**
	 * 检查Wifi当前状态
	 * 
	 * @return Wifi状态码
	 */
	public int checkState() {
		return mWifiManager.getWifiState();
	}

	/**
	 * 锁定WifiLock
	 */
	public void acquireWifiLock() {
		mWifiLock.acquire();
	}

	/**
	 * 释放WifiLock
	 */
	public void releaseWifiLock() {
		// 判断是否锁定
		if (mWifiLock.isHeld()) {
			mWifiLock.acquire();
		}
	}

	/**
	 * 创建一个WifiLock
	 */
	public void createWifiLock() {
		mWifiLock = mWifiManager.createWifiLock("test");
	}

	/**
	 * 得到配置好的网络
	 * 
	 * @return WifiConfiguration
	 */
	public List<WifiConfiguration> getConfiguration() {
		return mWifiConfigurations;
	}

	/**
	 * 指定配置好的网络进行连接
	 * 
	 * @param index
	 *            ID
	 */
	public void connetionConfiguration(int index) {
		if (index > mWifiConfigurations.size()) {
			return;
		}
		// 连接配置好指定ID网络
		mWifiManager.enableNetwork(mWifiConfigurations.get(index).networkId,
				true);
	}

	/**
	 * 开始扫描网络
	 */
	public void startScan() {
		mWifiManager.startScan();
		// 得到扫描结果
		mWifiList = mWifiManager.getScanResults();
		// 得到配置好的网络连接
		mWifiConfigurations = mWifiManager.getConfiguredNetworks();
	}

	/**
	 * 查找已经设置好的Wifi
	 * 
	 * @param ssid
	 * @return
	 */
	public boolean getHistoryWifiConfig(String ssid) {
		mWifiConfigurations = mWifiManager.getConfiguredNetworks();
		for (int i = 0; i < mWifiConfigurations.size(); i++) {
			mWifiManager.removeNetwork(mWifiConfigurations.get(i).networkId);
			if (("\"" + ssid + "\"").equals(mWifiConfigurations.get(i).SSID)) {
				connetionConfiguration(i);
				return false;
			}
		}
		return true;
	}

	/**
	 * 得到网络列表
	 * 
	 * @return ScanResult
	 */
	public List<ScanResult> getWifiList() {
		return mWifiList;
	}

	/**
	 * 获取MacAddress
	 * 
	 * @return MacAddress
	 */
	public String getMacAddress() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
	}

	/**
	 * 获取BSSID
	 * 
	 * @return BSSID
	 */
	public String getBSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
	}

	/**
	 * 获取IP地址
	 * 
	 * @return IP地址
	 */
	public int getIpAddress() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
	}

	/**
	 * 获取连接的ID
	 * 
	 * @return NetworkId
	 */
	public int getNetworkId() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
	}

	/**
	 * 获取连接速度
	 * 
	 * @return LinkSpeed
	 */
	public int getLinkSpeed() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getLinkSpeed();
	}

	/**
	 * 获取WifiInfo的所有信息
	 * 
	 * @return WifiInfo的所有信息
	 */
	public String getWifiInfo() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
	}

	/**
	 * SSID是否隐藏
	 * 
	 * @return 隐藏返回true否则返回false，默认是false
	 */
	public boolean getHiddenSSID() {
		return (mWifiInfo == null) ? false : mWifiInfo.getHiddenSSID();
	}

	/**
	 * 添加一个网络并连接
	 * 
	 * @param wifiConfiguration
	 */
	public void addWifi(WifiConfiguration wifiConfiguration) {
		int wcgId = mWifiManager.addNetwork(wifiConfiguration);
		boolean boo = mWifiManager.enableNetwork(wcgId, true);
		if (!boo) {
			removeWifi(0);
			Toast.makeText(mContext, "密码错误", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 删除已连接的网络
	 * 
	 * @param netId
	 */
	public void removeWifi(int netId) {
		mWifiManager.removeNetwork(netId);
	}

	/**
	 * 添加一个网络并连接
	 * 
	 * @param wcg
	 */
	public void addNetwork(WifiConfiguration wcg) {
		int wcgID = mWifiManager.addNetwork(wcg);
		mWifiConfigurations = getConfiguration();
		if (!mWifiManager.enableNetwork(wcgID, true)) {
			Toast.makeText(mContext, "密码错误", 0).show();
		}
	}

	/**
	 * 断开当前网络连接
	 * 
	 * @param netId
	 *            网络ID
	 */
	public void disConnectionWifi() {
		if (getNetworkId() != -1) {
			mWifiManager.disableNetwork(getNetworkId());
			mWifiManager.disconnect();
		}
	}

	public WifiConfiguration createWifiInfo(String SSID, String Password,
			int Type) {
		WifiConfiguration config = new WifiConfiguration();
		config.SSID = "\"" + SSID + "\"";
		config.allowedPairwiseCiphers
				.set(WifiConfiguration.PairwiseCipher.CCMP);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		config.allowedPairwiseCiphers
				.set(WifiConfiguration.PairwiseCipher.TKIP);
		if (Type == 1)// Data.WIFICIPHER_NOPASS)
		{
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.wepTxKeyIndex = 0;
		}
		if (Type == 2)// Data.WIFICIPHER_WEP)
		{
			config.hiddenSSID = true;
			config.wepKeys[0] = "\"" + Password + "\"";
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (Type == 3)// Data.WIFICIPHER_WPA)
		{
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.status = WifiConfiguration.Status.ENABLED;
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		}
		return config;
	}

	public void showLoadingPop(final String ssid) {
		Button btn_ok, btn_cancel;
		TextView title;
		final EditText password;
		if (mShowCView == null) {
			mShowCView = (LinearLayout) LayoutInflater.from(mContext).inflate(
					R.layout.layout_wifi_pop, null);
			mShowCView.setBackgroundResource(R.drawable.wifi_tv_show);
		}
		if (mShowCPopu == null) {
			mShowCPopu = new PopupWindow(mShowCView, LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
		}
		title = (TextView) mShowCView.findViewById(R.id.pop_title);
		password = (EditText) mShowCView.findViewById(R.id.pop_password);
		btn_ok = (Button) mShowCView.findViewById(R.id.pop_bt_ok);
		btn_cancel = (Button) mShowCView.findViewById(R.id.pop_bt_cancel);
		mShowCPopu.setFocusable(true);
		mShowCPopu.setOutsideTouchable(true);
		// mShowCPopu.setBackgroundDrawable(new BitmapDrawable());
		title.setText("" + ssid);
		// 连接按钮
		btn_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String passwords = password.getText().toString().trim();
				if (passwords != null && !passwords.equals("")) {
					addNetwork(createWifiInfo(ssid, passwords, 3));
					WifiEntity.wifiPwd=password.getText().toString();
					mShowCPopu.dismiss();
				}
			}
		});
		// 取消按钮
		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				password.setText("");
				mShowCPopu.dismiss();
			}
		});
		mShowCPopu.showAtLocation(mShowCView, Gravity.CENTER, 0, 0);
	}
	
	public WifiConfiguration tryToConnect(String SSID, String Password,
			int Type) {
		WifiConfiguration config = new WifiConfiguration();
		config.SSID = "\"" + SSID + "\"";
		config.allowedPairwiseCiphers
				.set(WifiConfiguration.PairwiseCipher.CCMP);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		config.allowedPairwiseCiphers
				.set(WifiConfiguration.PairwiseCipher.TKIP);
		if (Type == 1)// Data.WIFICIPHER_NOPASS)
		{
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.wepTxKeyIndex = 0;
		}
		if (Type == 2)// Data.WIFICIPHER_WEP)
		{
			config.hiddenSSID = true;
			config.wepKeys[0] = "\"" + Password + "\"";
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (Type == 3)// Data.WIFICIPHER_WPA)
		{
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.status = WifiConfiguration.Status.ENABLED;
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		}
		return config;
	}
	
	public int getWifiMMType(ScanResult result){
		if(result.capabilities.contains("WPA2-PSK"))
			return 3;
		else if(result.capabilities.contains("WPA-PSK"))
			return 3;
		else if(result.capabilities.contains("WEP"))
			return 2;
		else
			return 1;
	}
	
	public String getFileContent(Context context,String filename){
		String content="";
		FileReader reader = null;
		BufferedReader br = null;
		File file = new File("/sdcard", filename);
		if (file.exists()) {
			try {
				reader = new FileReader(file);
				br = new BufferedReader(reader);
				char[] buffer = new char[1024];
				br.read(buffer);
				content = new String(buffer);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				try {
					br.close();
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return content;
	}
	
	public boolean isConnect(Context context){
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return info.isConnected();
	}
	
	public int theMMIntension(String password){
		int intension = 0;
		int size = password.length();
		if(size<8){
			return 1;
		}else if(size<10){
			return 2;
		}else if(size<12){
			return 3;
		}
		return intension;
	}

}

package com.jzlg.excellentwifi.activity;

import com.jzlg.excellentwifi.R;

import android.R.integer;
import android.app.ActionBar;
import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * WIFI位置
 * 
 * @author
 *
 */
public class WifiLocation extends Activity {
	private ActionBar actionBar;
	private TextView wifiStrength;
	private ImageView wifiStrengthImg;
	private Handler refresh;
	private RefreshC refreshC;
	private boolean isRefresh = true;// 是否刷新
	private WifiManager wifi;
	private int min = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_wifi_location);
		initView();
	}

	private void initView() {
		actionBar = getActionBar();
		actionBar.setTitle("Wifi位置");
		actionBar.setLogo(R.drawable.left_menu_wifilocation_white);
		actionBar.setDisplayHomeAsUpEnabled(true);// 开启导航图标
		wifi = (WifiManager) getSystemService(WIFI_SERVICE);
		wifiStrength = (TextView) findViewById(R.id.wifi_location_strength);
		wifiStrengthImg = (ImageView) findViewById(R.id.wifi_location_strengthimg);
		refresh = new Refresh();
		refreshC = new RefreshC();
		refreshC.start();
	}

	class Refresh extends Handler {
		@Override
		public void handleMessage(Message msg) {
			String str = (String) msg.obj;
			int max = Integer.valueOf(str);
			int abs = Math.abs(max);
			wifiStrength.setText(abs+"");
			// wifiStrength.setText("");
			String yl = "您正在远离目标";
			String fj = "目标就在附近";
//			if (min == 0)
//				min = abs;
//			if (min > abs)
//				min = abs;
//			if (abs - min > 35)
//				wifiStrength.setText(yl + abs);
//			if (min <= 35)
//				wifiStrength.setText(fj + abs);
		}

	}

	class RefreshC extends Thread {
		@Override
		public void run() {
			while (isRefresh) {
				int s = wifi.getConnectionInfo().getRssi();// 获取信号强度
				Message msg = refresh.obtainMessage();
				msg.obj = s + "";
				try {
					Thread.sleep(1000);
					refresh.sendMessage(msg);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// 菜单选项事件
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}
		return true;
	}

	@Override
	protected void onStart() {
		int rssi = wifi.getConnectionInfo().getRssi();
		if (rssi != -999 || rssi != -9999) {
			isRefresh = true;

		}
		super.onStart();
	}

	@Override
	protected void onStop() {
		isRefresh = false;
		super.onStop();
	}
}

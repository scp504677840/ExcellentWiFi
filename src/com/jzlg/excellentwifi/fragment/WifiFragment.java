package com.jzlg.excellentwifi.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jzlg.excellentwifi.R;
import com.jzlg.excellentwifi.utils.WifiAdmin;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;

/**
 * WIFIFragment
 * 
 * @author 
 *
 */
public class WifiFragment extends Fragment implements OnClickListener,
		OnItemClickListener, OnScrollListener {
	private View view;
	private ImageButton onoffWifi;
	private WifiAdmin mWifiAdmin;
	private List<Map<String, Object>> listdata;
	private SimpleAdapter mSimpleAdapter;
	private List<ScanResult> list;
	private Context mContext;
	private ListView listView;

	public WifiFragment(Context context) {
		mContext = context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.layout_wifi, container, false);
		initView();
		initEvent();
		return view;
	}

	// 初始化事件
	private void initEvent() {
		listView.setOnItemClickListener(this);
		listView.setOnScrollListener(this);
		onoffWifi.setOnClickListener(this);
	}

	// 初始化组件
	private void initView() {
		onoffWifi = (ImageButton) view.findViewById(R.id.main_tab_wifi_onoff);
		// 初始化WifiAdmin
		mWifiAdmin = new WifiAdmin(mContext);
		listView = (ListView) view.findViewById(R.id.main_tab_wifi_listview);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	// 扫描网络
	private void saomiao() {
		listdata = new ArrayList<Map<String, Object>>();
		// 适配器
		mSimpleAdapter = new SimpleAdapter(view.getContext(), getWfData(),
				R.layout.item_wifi, new String[] { "ssid", "levelimg" },
				new int[] { R.id.item_wifi_ssid, R.id.item_wifi_level });
		// 加载适配器
		listView.setAdapter(mSimpleAdapter);
	}

	private List<Map<String, Object>> getDatas() {
		for (int i = 0; i < 20; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("ssid", "我是WIFI" + i);
			map.put("levelimg", R.drawable.ic_launcher);
			listdata.add(map);
		}
		return listdata;
	}

	// 数据源
	private List<Map<String, Object>> getWfData() {
		if (mWifiAdmin.checkState() == 3) {
			mWifiAdmin.startScan();
			// 扫描结果列表
			list = mWifiAdmin.getWifiList();
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					ScanResult sc = list.get(i);
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("ssid", sc.SSID);
					int level = sc.level;
					if (Math.abs(level) <= 45) {
						map.put("levelimg", R.drawable.wifi01);
					} else if (Math.abs(level) <= 65) {
						map.put("levelimg", R.drawable.wifi02);
					} else if (Math.abs(level) <= 80) {
						map.put("levelimg", R.drawable.wifi03);
					} else if (Math.abs(level) <= 100) {
						map.put("levelimg", R.drawable.wifi04);
					} else {
						map.put("levelimg", R.drawable.wifi05);
					}
					listdata.add(map);
				}
			}
		}
		return listdata;
	}

	// 点击事件
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.main_tab_wifi_onoff:
			if (mWifiAdmin.checkState() != 3) {
				mWifiAdmin.openWifi();
			}
			if (mWifiAdmin.checkState() == 3) {
				saomiao();
			}
			break;

		default:
			break;
		}
	}

	// 单个条目被点击事件
	@Override
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {
		int loac = position - list.size();

		if (loac < 0) {
			final ScanResult sr = list.get(position);
			Log.i(sr.capabilities, sr.SSID + "");

			boolean c = mWifiAdmin.getHistoryWifiConfig(sr.SSID);
			if (c) {
				// 如果没有输入密码 且配置列表中没有该WIFI
				/* WIFICIPHER_WPA 加密 */
				if (sr.capabilities.contains("WPA2-PSK")) {
					Log.i("SSID", "WPA2-PSK");
					mWifiAdmin.showLoadingPop(sr.SSID);
				} else if (sr.capabilities.contains("WPA-PSK")) {
					Log.i("SSID", "WPA-PSK");
					mWifiAdmin.showLoadingPop(sr.SSID);
				} else if (sr.capabilities.contains("WEP")) {
					/* WIFICIPHER_WEP 加密 */
					Log.i("SSID", "WEP");
					mWifiAdmin.addNetwork(mWifiAdmin.createWifiInfo(sr.SSID,
							"", 2));
				} else {
					/* WIFICIPHER_OPEN NOPASSWORD 开放无加密 */
					Log.i("SSID", "WU");
					mWifiAdmin.addNetwork(mWifiAdmin.createWifiInfo(sr.SSID,
							"", 1));
				}
			}
		}
	}

	/**
	 * firstVisibleItem:最上面呈现的ITEM visibleItemCount:正在呈现给用户看条目有多少
	 * totalItemCount：总共有多少条
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		Log.i("onScroll", "firstVisibleItem：" + firstVisibleItem
				+ "visibleItemCount：" + visibleItemCount + "totalItemCount:"
				+ totalItemCount);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
		case SCROLL_STATE_FLING:
			break;
		case SCROLL_STATE_IDLE:
			break;
		case SCROLL_STATE_TOUCH_SCROLL:
			break;
		default:
			break;
		}
	}

}

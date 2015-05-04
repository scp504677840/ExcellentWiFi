package com.jzlg.excellentwifi.activity;


import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.jzlg.excellentwifi.R;
import com.jzlg.excellentwifi.entity.WifiInfo;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MapActivity extends BaseActivity {
	private Context context;
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private LocationMode mLocationMode;
	private LocationClient mLocationClient;
	private MyLocationListener mLocationListener;
	private BitmapDescriptor mIconLocation;
	private float mCurrentX;
	private double mLatitude;
	private double mLongitude;
	public boolean isFirstIn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要在setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.layout_map);
		this.context = this;
		// 初始化组件
		initView();
		// 初始化定位
		initLocation();
		// 初始化覆盖物
		initMarker();
		// 点击事件
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {

				Bundle bundle = marker.getExtraInfo();
				WifiInfo info = (WifiInfo) bundle.getSerializable("info");

				InfoWindow infoWindow;
				TextView tv = new TextView(context);
				tv.setBackgroundResource((R.drawable.map_tishi));
				tv.setPadding(30, 20, 30, 50);
				tv.setText(info.getName());
				tv.setTextColor(Color.parseColor("#ffffff"));
				BitmapDescriptor tvbit = BitmapDescriptorFactory.fromView(tv);
				final LatLng latLng = marker.getPosition();
				// Point point = mBaiduMap.getProjection()
				// .toScreenLocation(latLng);
				// point.y -= 50;// 设置偏移量
				// point.x -= 10;
				// LatLng ll =
				// mBaiduMap.getProjection().fromScreenLocation(point);
				infoWindow = new InfoWindow(tvbit, latLng, -50,
						new OnInfoWindowClickListener() {

							@Override
							public void onInfoWindowClick() {
								mBaiduMap.hideInfoWindow();
							}
						});
				mBaiduMap.showInfoWindow(infoWindow);

				return true;
			}
		});

		// 点击地图时，marker消失
		mBaiduMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public boolean onMapPoiClick(MapPoi mapPoi) {
				return false;
			}

			@Override
			public void onMapClick(LatLng latLng) {
				mBaiduMap.hideInfoWindow();
			}
		});
	}

	private void initMarker() {
	}

	// 定位初始化
	private void initLocation() {
		// 默认模式
		mLocationMode = LocationMode.NORMAL;

		mLocationClient = new LocationClient(this);
		// 定位监听器
		mLocationListener = new MyLocationListener();
		// 注册
		mLocationClient.registerLocationListener(mLocationListener);
		// 设置一些必要的配置
		setLocationOption();

		// 初始化导航图标
		mIconLocation = BitmapDescriptorFactory
				.fromResource(R.drawable.map_daohang);

		mLocationClient.start();
	}

	// 定位设置
	private void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		option.setAddrType("all");// 返回的定位结果包含地址信息
		option.setIsNeedAddress(true);// 位置，一定要设置，否则后面得不到地址
		option.setOpenGps(true);// 打开GPS
		option.setScanSpan(1000);// 多长时间进行一次请求
		// option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//
		// 精确定位
		mLocationClient.setLocOption(option);// 使用设置
	}

	private void initView() {
		// 获取地图控件引用
		mMapView = (MapView) findViewById(R.id.map_bmapView);
		mBaiduMap = mMapView.getMap();

		// 设置地图放大缩小参数
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
		mBaiduMap.setMapStatus(msu);
	}

	// 菜单
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_map, menu);
		return true;
	}

	// 菜单点击方法
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// 普通
		case R.id.map_common:
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
			break;
		// 卫星
		case R.id.map_site:
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
			break;
		// 实时
		case R.id.map_traffic:
			// 是否显示了实时地图
			if (mBaiduMap.isTrafficEnabled()) {
				mBaiduMap.setTrafficEnabled(false);
				item.setTitle("实时交通off");
			} else {
				mBaiduMap.setTrafficEnabled(true);
				item.setTitle("实时交通on");
			}
			break;
		// 我的位置
		case R.id.map_location:
			centerToMyLocation();
			break;
		// 普通模式
		case R.id.map_sensormode_normal:
			mLocationMode = LocationMode.NORMAL;
			break;
		// 跟随模式
		case R.id.map_sensormode_following:
			mLocationMode = LocationMode.FOLLOWING;
			break;
		// 罗盘模式
		case R.id.map_sensormode_compass:
			mLocationMode = LocationMode.COMPASS;
			break;
		// 覆盖物
		case R.id.map_add_overlay:
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// 定位到我的位置
	private void centerToMyLocation() {
		LatLng latLng = new LatLng(mLatitude, mLongitude);
		// 设置经纬度
		MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
		mBaiduMap.animateMapStatus(msu);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mLocationClient.stop();
		mBaiduMap.setMyLocationEnabled(false);
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
		mMapView = null;
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
		
	}

	@Override
	protected void onStart() {
		super.onStart();
		// 开启定位允许
		mBaiduMap.setMyLocationEnabled(true);
		if (!mLocationClient.isStarted()) {
			mLocationClient.start();
		}

		
	}

	@Override
	protected void onStop() {
		super.onStop();
		// 停止定位
		mBaiduMap.setMyLocationEnabled(false);
		mLocationClient.stop();
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
		
	}

	private class MyLocationListener implements BDLocationListener {

		// 定位成功之后的回调
		@Override
		public void onReceiveLocation(BDLocation location) {
			MyLocationData data = new MyLocationData.Builder()// 经度
					.direction(mCurrentX)//
					.accuracy(location.getRadius())//
					.latitude(location.getLatitude())//
					.longitude(location.getLongitude())//
					.build();

			// 设置我的定位
			mBaiduMap.setMyLocationData(data);

			// 设置自定义图标
			MyLocationConfiguration config = new MyLocationConfiguration(
					mLocationMode, true, mIconLocation);

			mBaiduMap.setMyLocationConfigeration(config);

			// 取出经纬度
			mLatitude = location.getLatitude();
			mLongitude = location.getLongitude();

			// 是否是第一次定位
			if (isFirstIn) {
				LatLng latLng = new LatLng(location.getLatitude(),
						location.getLongitude());

				// 设置经纬度
				MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);

				mBaiduMap.animateMapStatus(msu);
				isFirstIn = false;

			}

			// Toast.makeText(
			// context,
			// "定位成功：纬度" + location.getLatitude() + "经度："
			// + location.getLongitude() + "地址是："
			// + location.getAddrStr() + "城市是："
			// + location.getCity(), 0).show();

		}
	}

}

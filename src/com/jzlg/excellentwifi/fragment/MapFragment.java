package com.jzlg.excellentwifi.fragment;

import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.jzlg.excellentwifi.R;
import com.jzlg.excellentwifi.Impl.MyOrientationListener;
import com.jzlg.excellentwifi.Impl.MyOrientationListener.OnOrientationListener;
import com.jzlg.excellentwifi.entity.WifiInfo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * WIFI地理位置在百度地图上显示
 * 
 * @author
 *
 */
public class MapFragment extends Fragment {
	private View view;
	private Context mContext;
	private MapView mMapView;
	private BaiduMap mBaiduMap;

	// 定位相关
	private LocationClient mLocationClient;
	private MyLocationListener mLocationListener;
	private boolean isFirstIn = true;
	private double mLatitude;// 纬度
	private double mLongitude;// 经度

	// 自定义定位图标
	private BitmapDescriptor mIconLocation;
	private MyOrientationListener myOrientationListener;
	private float mCurrentX;
	private LocationMode mLocationMode;

	// 覆盖物相关
	private BitmapDescriptor mMarker;

	public MapFragment(Context context) {
		mContext = context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要在setContentView方法之前实现
		SDKInitializer.initialize(mContext);
		view = inflater.inflate(R.layout.layout_map, container, false);
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
				TextView tv = new TextView(view.getContext());
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
		return view;
	}

	private void initView() {
		// 获取地图控件引用
		mMapView = (MapView) view.findViewById(R.id.map_bmapView);
		mBaiduMap = mMapView.getMap();

		// 设置地图放大缩小参数
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
		mBaiduMap.setMapStatus(msu);
	}

	// 初始化覆盖物
	private void initMarker() {
		mMarker = BitmapDescriptorFactory.fromResource(R.drawable.map_marke);
		addOverlay(WifiInfo.infos);// 添加覆盖物
	}

	// 定位初始化
	private void initLocation() {
		// 默认模式
		mLocationMode = LocationMode.NORMAL;

		mLocationClient = new LocationClient(view.getContext());
		// 定位监听器
		mLocationListener = new MyLocationListener();
		// 注册
		mLocationClient.registerLocationListener(mLocationListener);
		// 设置一些必要的配置
		setLocationOption();

		// 初始化导航图标
		mIconLocation = BitmapDescriptorFactory
				.fromResource(R.drawable.map_daohang);

		myOrientationListener = new MyOrientationListener(mContext);
		myOrientationListener
				.setOnOrientationListener(new OnOrientationListener() {

					@Override
					public void onOrientationChanged(float x) {
						mCurrentX = x;
					}
				});

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
		// option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//精确定位
		mLocationClient.setLocOption(option);// 使用设置
	}

	// 菜单
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.menu_map, menu);
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
			addOverlay(WifiInfo.infos);
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// 添加覆盖物
	private void addOverlay(List<WifiInfo> infos) {
		mBaiduMap.clear();// 清楚图层
		// 定义经纬度
		LatLng latLng = null;
		Marker marker = null;
		OverlayOptions options;
		for (WifiInfo info : infos) {
			// 经纬度
			latLng = new LatLng(info.getLatitude(), info.getLongitude());
			// 图标
			options = new MarkerOptions().position(latLng).icon(mMarker)
					.zIndex(5);
			marker = (Marker) mBaiduMap.addOverlay(options);
			Bundle bundle = new Bundle();
			bundle.putSerializable("info", info);
			marker.setExtraInfo(bundle);
		}

		// 移动地图位置
		MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);

		mBaiduMap.setMapStatus(msu);

	}

	// 定位到我的位置
	private void centerToMyLocation() {
		LatLng latLng = new LatLng(mLatitude, mLongitude);
		// 设置经纬度
		MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
		mBaiduMap.animateMapStatus(msu);
	}

	@Override
	public void onDestroy() {
		mLocationClient.stop();
		mBaiduMap.setMyLocationEnabled(false);
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
	}

	@Override
	public void onResume() {
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
		super.onResume();
	}

	@Override
	public void onStart() {
		// 开启定位允许
		mBaiduMap.setMyLocationEnabled(true);
		if (!mLocationClient.isStarted()) {
			mLocationClient.start();
		}
		// 开启方向传感器
		myOrientationListener.start();

		super.onStart();
	}

	@Override
	public void onStop() {
		// 停止定位
		mBaiduMap.setMyLocationEnabled(false);
		mLocationClient.stop();
		// 停止方向传感器
		myOrientationListener.stop();

		super.onStop();
	}

	@Override
	public void onPause() {
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
		super.onPause();
	}

	private class MyLocationListener implements BDLocationListener {

		// 定位成功之后的回调
		@Override
		public void onReceiveLocation(BDLocation location) {
			MyLocationData data = new MyLocationData.Builder()//
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

		}
	}
}

package com.jzlg.excellentwifi.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.wifi.ScanResult;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.jzlg.excellentwifi.BuildConfig;
import com.jzlg.excellentwifi.R;
import com.jzlg.excellentwifi.entity.WifiPoint;
import com.jzlg.excellentwifi.utils.WifiAdmin;

/**
 * 雷达扫描
 * 
 * @author
 *
 */
public class RadarView extends BaseView {

	public static final String TAG = "RadarView";
	public static final boolean D = BuildConfig.DEBUG;

	private float offsetArgs = 0;
	private boolean isSearching = false;
	private Bitmap bitmap;
	private Bitmap bitmap1;
	private Bitmap bitmap2;
	private Bitmap bitmap3;
	private Paint paint;
	private WifiAdmin mWifiAdmin = new WifiAdmin(getContext());

	// 点
	List<WifiPoint> wifiPoints = new ArrayList<WifiPoint>();

	public boolean isSearching() {
		return isSearching;
	}

	public void setSearching(boolean isSearching) {
		this.isSearching = isSearching;
		offsetArgs = 0;
		if (mWifiAdmin.checkState() != 3) {
			mWifiAdmin.openWifi();
		}
		invalidate();
	}

	public RadarView(Context context) {
		super(context);
		initBitmap();
	}

	public RadarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initBitmap();
	}

	public RadarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initBitmap();
	}

	private void initBitmap() {
		if (bitmap == null) {
			bitmap = Bitmap.createBitmap(BitmapFactory.decodeResource(
					this.getResources(), R.drawable.radar_gplus_search_bg));
		}
		if (bitmap1 == null) {
			bitmap1 = Bitmap.createBitmap(BitmapFactory.decodeResource(
					this.getResources(), R.drawable.radar_locus_round_click));
		}
		if (bitmap2 == null) {
			bitmap2 = Bitmap.createBitmap(BitmapFactory.decodeResource(
					this.getResources(), R.drawable.radar_gplus_search_args));
		}
		if (bitmap3 == null) {
			bitmap3 = Bitmap.createBitmap(BitmapFactory.decodeResource(
					this.getResources(), R.drawable.radar_wifi_point));
		}
	}

	boolean isCanvas = false;// 是否绘制

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		// 画笔
		paint = new Paint();
		paint.setAntiAlias(true);// 使用抗锯齿功能
		paint.setStrokeWidth(3);// 设置笔触的宽度
		paint.setStyle(Style.FILL);// 设置填充样式为描边STROKE:空心，FILL：圆心
		paint.setColor(Color.BLUE);
		canvas.drawBitmap(bitmap, getWidth() / 2 - bitmap.getWidth() / 2,
				getHeight() / 2 - bitmap.getHeight() / 2, null);

		// 当转角为0时不绘制
		if (offsetArgs == 0) {
			isCanvas = false;
		}

		if (offsetArgs % 60 == 0 && offsetArgs != 0) {
			wifiPoint();
			isCanvas = true;
		}
		if (isCanvas) {
			drawPoint(canvas);
		}

		if (isSearching) {

			Rect rMoon = new Rect(getWidth() / 2 - bitmap2.getWidth(),
					getHeight() / 2, getWidth() / 2, getHeight() / 2
							+ bitmap2.getHeight());
			// 以视图中心为轴心
			canvas.rotate(offsetArgs, getWidth() / 2, getHeight() / 2);
			canvas.drawBitmap(bitmap2, null, rMoon, null);
			if (offsetArgs == 720) {
				offsetArgs = 0;
				isSearching = false;
				drawPoint(canvas);
			}
			offsetArgs = offsetArgs + 1;

		} else {

			canvas.drawBitmap(bitmap2, getWidth() / 2 - bitmap2.getWidth(),
					getHeight() / 2, null);
		}

		canvas.drawBitmap(bitmap1, getWidth() / 2 - bitmap1.getWidth() / 2,
				getHeight() / 2 - bitmap1.getHeight() / 2, null);

		if (isSearching)
			invalidate();
	}

	public void canvasRefresh(Canvas canvas) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);// 使用抗锯齿功能
		paint.setStrokeWidth(3);// 设置笔触的宽度
		paint.setStyle(Style.FILL);// 设置填充样式为描边STROKE:空心，FILL：圆心
		paint.setColor(Color.BLUE);
		canvas.drawCircle(50, 50, 10, paint);
	}

	// 根据WIFI信号强度绘制点
	private void drawPoint(Canvas canvas) {
		for (int i = 0; i < wifiPoints.size(); i++) {
			WifiPoint point = wifiPoints.get(i);
			canvas.drawBitmap(bitmap3, point.getX(), point.getY(), null);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			handleActionDownEvenet(event);
			return true;
		case MotionEvent.ACTION_MOVE:
			return true;
		case MotionEvent.ACTION_UP:
			return true;
		}
		return super.onTouchEvent(event);
	}

	private void handleActionDownEvenet(MotionEvent event) {
		RectF rectF = new RectF(getWidth() / 2 - bitmap1.getWidth() / 2,
				getHeight() / 2 - bitmap1.getHeight() / 2, getWidth() / 2
						+ bitmap1.getWidth() / 2, getHeight() / 2
						+ bitmap1.getHeight() / 2);

		if (rectF.contains(event.getX(), event.getY())) {
			if (D)
				Log.d(TAG, "点击搜索按钮");
			if (!isSearching()) {
				setSearching(true);
			} else {
				setSearching(false);
			}
		}
	}

	private List<ScanResult> wifiList;
	private WifiPoint wifiPoint;

	// WIFI相关
	private void wifiPoint() {
		wifiPoints = new ArrayList<WifiPoint>();
		// 开启扫描WIFI
		mWifiAdmin.startScan();
		// 得到WIFI列表
		wifiList = mWifiAdmin.getWifiList();
		if (wifiList != null) {
			for (int i = 0; i < wifiList.size(); i++) {
				ScanResult scanResult = wifiList.get(i);
				int level = scanResult.level;
				int x = getWidth() / 2;
				int y = getHeight() / 2;
				int absLevel = Math.abs(level);
				if (absLevel <= 45) {
					int[] coordinate = randomD(1);
					x = coordinate[0];
					y = coordinate[1];
				} else if (absLevel <= 65) {
					int[] coordinate = randomD(2);
					x = coordinate[0];
					y = coordinate[1];
				} else if (absLevel <= 80) {
					int[] coordinate = randomD(3);
					x = coordinate[0];
					y = coordinate[1];
				} else {
					int[] coordinate = randomD(4);
					x = coordinate[0];
					y = coordinate[1];
				}
				wifiPoint = new WifiPoint(x, y);
				wifiPoints.add(wifiPoint);
			}
		}
	}

	// 得到随机象限和距离
	private int[] randomD(int strength) {
		int[] coordinate = new int[2];
		int Longdistance = 0;// 定义横向距离
		int Widedistance = 0;// 定义纵向距离
		int Lran = (int) (Math.random() * 50);// 0-45
		int Wran = (int) (Math.random() * 50);// 0-45
		// 根据信号强度给定适合的距离
		if (strength == 1) {
			Longdistance = Lran + 10;
			Widedistance = Wran + 10;
		} else if (strength == 2) {
			Longdistance = Lran + 60;
			Widedistance = Wran + 60;
		} else if (strength == 3) {
			Longdistance = Lran + 90;
			Widedistance = Wran + 90;
		} else {// 80-90
			Longdistance = Lran + 120;
			Widedistance = Wran + 120;
		}
		// 接下来是象限的判断
		int fnum = (int) (Math.random() * 10 + Math.random() * 10 + 10);
		if (fnum <= 14) {// 10 11 12 13 14第一象限
			coordinate[0] = getWidth() / 2 + bitmap1.getWidth() / 2
					+ Longdistance;
			coordinate[1] = getHeight() / 2 - bitmap1.getHeight() / 2
					- Widedistance;
		} else if (fnum <= 20) {// 15 16 17 18 19 20第二象限
			coordinate[0] = getWidth() / 2 + bitmap1.getWidth() / 2
					+ Longdistance;
			coordinate[1] = getHeight() / 2 + bitmap1.getHeight() / 2
					+ Widedistance;
		} else if (fnum <= 25) {// 21 22 23 24 25第三象限
			coordinate[0] = getWidth() / 2 - bitmap1.getWidth() / 2
					- Longdistance;
			coordinate[1] = getHeight() / 2 + bitmap1.getHeight() / 2
					+ Widedistance;
		} else {// 26 27 28 29 30第四象限
			coordinate[0] = getWidth() / 2 - bitmap1.getWidth() / 2
					- Longdistance;
			coordinate[1] = getHeight() / 2 - bitmap1.getHeight() / 2
					- Widedistance;
		}
		return coordinate;
	}

}
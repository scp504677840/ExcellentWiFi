package com.jzlg.excellentwifi.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.jzlg.excellentwifi.BuildConfig;
import com.jzlg.excellentwifi.R;
import com.jzlg.excellentwifi.entity.WifiPoint;

public class RadarView extends BaseView {

	public static final String TAG = "RadarView";
	public static final boolean D = BuildConfig.DEBUG;

	@SuppressWarnings("unused")
	private long TIME_DIFF = 1500;

	int[] lineColor = new int[] { 0x7B, 0x7B, 0x7B };
	int[] innerCircle0 = new int[] { 0xb9, 0xff, 0xFF };
	int[] innerCircle1 = new int[] { 0xdf, 0xff, 0xFF };
	int[] innerCircle2 = new int[] { 0xec, 0xff, 0xFF };

	int[] argColor = new int[] { 0xF3, 0xf3, 0xfa };

	private float offsetArgs = 0;
	private boolean isSearching = false;
	private Bitmap bitmap;
	private Bitmap bitmap1;
	private Bitmap bitmap2;
	private Bitmap bitmap3;
	private Paint paint;

	List<WifiPoint> list = new ArrayList<WifiPoint>();
	private int count = -1;
	private boolean isDrow = true;// 是否绘制

	public boolean isSearching() {
		return isSearching;
	}

	public void setSearching(boolean isSearching) {
		this.isSearching = isSearching;
		offsetArgs = 0;
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
					this.getResources(), R.drawable.radar_locus_round_click));
		}

		list.add(new WifiPoint(100, 200));
		list.add(new WifiPoint(150, 250));
		list.add(new WifiPoint(100, 260));
		list.add(new WifiPoint(50, 100));
	}

	Canvas canvasS;

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		canvasS = canvas;
		// 画笔
		paint = new Paint();
		paint.setAntiAlias(true);// 使用抗锯齿功能
		paint.setStrokeWidth(3);// 设置笔触的宽度
		paint.setStyle(Style.FILL);// 设置填充样式为描边STROKE:空心，FILL：圆心
		paint.setColor(Color.BLUE);
		canvas.drawBitmap(bitmap, getWidth() / 2 - bitmap.getWidth() / 2,
				getHeight() / 2 - bitmap.getHeight() / 2, null);

		// drawPoint(canvas);
		// canvas.drawCircle(100, 200, 20, paint);
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				Message message = new Message();
				message.what = 1;
				message.obj = canvas;
				Bundle data = new Bundle();
				data.putFloat("jiaodu", offsetArgs);
				message.setData(data);
				handler.sendMessage(message);
			}
		});
		thread.start();

		if (isSearching) {

			Rect rMoon = new Rect(getWidth() / 2 - bitmap2.getWidth(),
					getHeight() / 2, getWidth() / 2, getHeight() / 2
							+ bitmap2.getHeight());
			// 以视图中心为轴心
			canvas.rotate(offsetArgs, getWidth() / 2, getHeight() / 2);
			canvas.drawBitmap(bitmap2, null, rMoon, null);
			if (offsetArgs == 360) {
				offsetArgs = 0;
			}
			offsetArgs = offsetArgs + 3;
			Log.i("角度", "转角度数：" + offsetArgs);

		} else {

			canvas.drawBitmap(bitmap2, getWidth() / 2 - bitmap2.getWidth(),
					getHeight() / 2, null);
		}

		canvas.drawBitmap(bitmap1, getWidth() / 2 - bitmap1.getWidth() / 2,
				getHeight() / 2 - bitmap1.getHeight() / 2, null);

		if (isSearching)
			invalidate();
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Log.i("运行了", "1233");
				// Canvas canvas = (Canvas) msg.obj;
				canvasS.drawCircle(100, 100, 20, paint);
				break;

			default:
				break;
			}

		};
	};

	private void drawPoint(Canvas canvas) {
		for (int i = 0; i < list.size(); i++) {
			WifiPoint point = list.get(i);
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
		// 绘制弧
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
}
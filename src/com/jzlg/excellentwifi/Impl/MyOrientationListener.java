package com.jzlg.excellentwifi.Impl;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * 方向传感器
 * 
 * @author Administrator
 *
 */
public class MyOrientationListener implements SensorEventListener {
	private SensorManager mSensorManager;// 传感器管理者
	private Context context;
	private Sensor mSensor;// 传感器

	private float lastX;

	public MyOrientationListener(Context context) {
		this.context = context;
	}

	/**
	 * 经度发生改变时
	 */
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	// 开始监听
	public void start() {
		mSensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);

		if (mSensorManager != null) {
			// 获取方向传感器
			mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		}
		if (mSensor != null) {
			// samplingPeriodUs经度
			mSensorManager.registerListener(this, mSensor,
					SensorManager.SENSOR_DELAY_UI);
		}
	}

	// 结束监听
	public void stop() {
		mSensorManager.unregisterListener(this);
	}

	/**
	 * 方向发送变化
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		// 如果是方向传感器
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			float x = event.values[SensorManager.DATA_X];
			// float y = event.values[SensorManager.DATA_Y];
			// float z = event.values[SensorManager.DATA_Z];

			// 如果x-lastX大于1.0就通知主界面进行更新
			if (Math.abs(x - lastX) > 1.0) {
				if (mOnOrientationListener != null) {
					mOnOrientationListener.onOrientationChanged(x);
				}
			}

			lastX = x;

		}

	}

	private OnOrientationListener mOnOrientationListener;

	public void setOnOrientationListener(
			OnOrientationListener mOnOrientationListener) {
		this.mOnOrientationListener = mOnOrientationListener;
	}

	public interface OnOrientationListener {
		void onOrientationChanged(float x);
	}

}

package com.jzlg.excellentwifi.activity;

import com.jzlg.excellentwifi.utils.ActivityCollector;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

/**
 * Activity基类
 * 
 * @author 
 *
 */
public class BaseActivity extends FragmentActivity {
	private int count = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 隐藏标题栏
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 获取当前实例的类名
		Log.i("BaseActivity", getClass().getSimpleName());
		ActivityCollector.addActivity(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivityCollector.removeActivity(this);
	}

	@Override
	public void onBackPressed() {
		if (count == 1) {
			count--;
			ActivityCollector.finishAll();
		} else {
			count++;
			Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
		}
	}

}

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
 * @author 宋春鹏
 *
 */
public class BaseActivity extends FragmentActivity {
	private long time = 0;

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
		if (System.currentTimeMillis() - time < 3000) {
			time = 0;
			ActivityCollector.finishAll();
		} else {
			time = System.currentTimeMillis();
			Toast.makeText(this, "再按一次返回键退出程序", Toast.LENGTH_SHORT).show();
		}
	}

}

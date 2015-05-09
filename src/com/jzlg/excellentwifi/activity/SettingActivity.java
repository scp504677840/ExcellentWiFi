package com.jzlg.excellentwifi.activity;

import com.jzlg.excellentwifi.R;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;

/**
 * 设置
 * 
 * @author 
 *
 */
public class SettingActivity extends Activity {
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_setting);
		initView();
	}

	// 初始化控件
	private void initView() {
		actionBar = getActionBar();
		actionBar.setTitle("设置");
		actionBar.setLogo(R.drawable.left_menu_setting_white);
		actionBar.setDisplayHomeAsUpEnabled(true);// 开启导航图标
	}

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

}

package com.jzlg.excellentwifi.activity;

import com.jzlg.excellentwifi.R;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;

/**
 * 密码强度
 * 
 * @author 
 *
 */
public class PwdStrengthActivity extends Activity {
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_wifi_pwdstrength);
		initView();
	}

	// 初始化控件
	private void initView() {
		actionBar = getActionBar();
		actionBar.setTitle("密码强度");
		actionBar.setLogo(R.drawable.left_menu_pwdstrength_white);
		actionBar.setDisplayHomeAsUpEnabled(true);
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
}

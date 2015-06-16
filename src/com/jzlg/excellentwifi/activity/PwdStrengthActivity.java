package com.jzlg.excellentwifi.activity;

import com.jzlg.excellentwifi.R;
import com.jzlg.excellentwifi.utils.WifiAdmin;
import com.jzlg.excellentwifi.utils.WifiEntity;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 密码强度
 * 
 * @author 郭旭、宋春鹏、王大伟
 *
 */
public class PwdStrengthActivity extends Activity implements OnClickListener {
	private ActionBar actionBar;
	private ImageButton btnStart;
	private ProgressBar progressBar;
	private TextView textView;
	private int intension = 0;
	private String password = WifiEntity.wifiPwd;
	private Handler handler;

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
		btnStart = (ImageButton) findViewById(R.id.wifi_pwdstrength_start);
		btnStart.setOnClickListener(this);
		progressBar = (ProgressBar) findViewById(R.id.wifi_pwdstrength_progressbar);
		textView = (TextView) findViewById(R.id.wifi_pwdstrength_resulut);
		
		handler=new Handler(){
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case 1:
					int resID = R.drawable.wifi_pwdstrength_64x64_levle1;
					int colorID = Color.GREEN;
					String pwdText = "密码强度：弱";
					intension = new WifiAdmin(PwdStrengthActivity.this).theMMIntension(password);
					if (intension==1) {
						//采用默认值
					}
					if (intension==2) {
						resID = R.drawable.wifi_pwdstrength_64x64_levle1;
						colorID = Color.YELLOW;
						pwdText = "密码强度：中等";
					} 
					if(intension==3){
						resID = R.drawable.wifi_pwdstrength_64x64_levle3;
						colorID = Color.RED;
						pwdText = "密码强度：强";
					}
					btnStart.setBackgroundResource(resID);
					textView.setTextColor(colorID);
					textView.setText(pwdText);
					break;
				}
			}
		};
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.wifi_pwdstrength_start:
			new Thread(new PwdStreng()).start();
			break;
		default:
			break;
		}
	}

	class PwdStreng implements Runnable {
		public void run() {
			for (int i = 1; i <= 20; i++) {
				int value=i*5;
				progressBar.setProgress(value);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Message message = new Message();
			message.what = 1;
			handler.sendMessage(message);
		}
	}
}

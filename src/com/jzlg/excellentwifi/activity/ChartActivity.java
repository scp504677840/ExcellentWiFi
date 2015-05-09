package com.jzlg.excellentwifi.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.litepal.crud.DataSupport;

import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

import com.jzlg.excellentwifi.R;
import com.jzlg.excellentwifi.entity.WIFILine;

import android.R.integer;
import android.app.ActionBar;
import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * 图表
 * 
 * @author 
 *
 */
public class ChartActivity extends Activity implements
		LineChartOnValueSelectListener {
	private LineChartView mChartView;

	private ValueShape shape = ValueShape.CIRCLE;// CIRCLE:圆形;DIAMOND:菱形;SQUARE:正方形
	private boolean isCubic = true;// 是否是曲线
	private boolean isFilled = false;// 是否填充
	private boolean hasLabels = true;// 是否显示数值
	private boolean hasLabelsOnlyForSelected = false;// 仅仅对选中的点进行显示数值
	private boolean hasLines = true;// 线
	private boolean hasPoints = true;// 点
	private LineChartData data;
	private boolean hasAxes = true;
	private boolean hasAxesNames = true;
	private ActionBar actionBar;
	private WifiManager wifi;// WIFI管理
	private boolean isRefresh = true;// 是否刷新
	private WIFILine mWifiLine;// WIFI信号描述实体类
	private WifiInfo connectionInfo;// WIFI配置信息
	private ArrayList<Float> listLevel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_chart);
		initView();
		initData();
		initEvent();
	}

	private void initEvent() {
		mChartView.setOnValueTouchListener(this);
	}

	private void initData() {
		generateValues();
		generateData();
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				initData();
				break;
			default:
				break;
			}
		}
	};

	Thread thread = new Thread(new Runnable() {

		@SuppressWarnings("static-access")
		@Override
		public void run() {
			while (isRefresh) {
				doWifi();// 处理好数据
				Message message = new Message();
				message.what = 1;
				try {
					thread.sleep(1000);
					handler.handleMessage(message);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	});

	private void initView() {
		actionBar = getActionBar();
		actionBar.setTitle("信号走势");
		actionBar.setLogo(R.drawable.left_menu_levels_white);
		actionBar.setDisplayHomeAsUpEnabled(true);// 开启导航图标
		mChartView = (LineChartView) findViewById(R.id.chart_linechart);
		wifi = (WifiManager) getSystemService(WIFI_SERVICE);
		connectionInfo = wifi.getConnectionInfo();
		listLevel = new ArrayList<Float>();
		mWifiLine = new WIFILine();
		thread.start();// 开启线程
	}

	// 对WIFI的信号进行处理
	private void doWifi() {
//		int seconds = new Date().getSeconds();// 秒
		int level = connectionInfo.getRssi();
//		int count = mWifiLine.count(WIFILine.class);
//		if (count > 60) {
//			WIFILine findFirst = mWifiLine.findFirst(WIFILine.class);
//			findFirst.delete();
//		}
		if (listLevel.size() > 60) {
			listLevel.remove(0);
		}
		listLevel.add(Float.valueOf(level+""));
//		mWifiLine.setMacAddress(connectionInfo.getMacAddress());
//		mWifiLine.setSeconds(seconds);
//		mWifiLine.setLevel(level);
//		mWifiLine.save();// 保存数据
	}

	// 绘制数据
	private void generateData() {
		List<Line> lines = new ArrayList<Line>();
		for (int i = 0; i < 1; i++) {
			// 数据源
			List<PointValue> values = new ArrayList<PointValue>();
			for (int j = 0; j < listLevel.size(); j++) {
				values.add(new PointValue(j, listLevel.get(j)));
			}
			// 定义行
			Line line = new Line(values);
			// 设置行的颜色
			line.setColor(ChartUtils.COLORS[i]);
			// 形状
			line.setShape(shape);
			// 是否是曲线
			line.setCubic(isCubic);
			// 填充
			line.setFilled(isFilled);
			// 标签
			line.setHasLabels(hasLabels);
			// 仅仅对选中的设置标签
			line.setHasLabelsOnlyForSelected(hasLabelsOnlyForSelected);
			// 折线
			line.setHasLines(hasLines);
			// 点
			line.setHasPoints(hasPoints);
			lines.add(line);
		}

		data = new LineChartData();
		data.setLines(lines);

		if (hasAxes) {
			Axis axisX = new Axis();
			Axis axisY = new Axis().setHasLines(true);
			if (hasAxesNames) {
				axisX.setName("时间(s)");
				axisY.setName("信号强度");
			}
			data.setAxisXBottom(axisX);
			data.setAxisYLeft(axisY);
		} else {
			data.setAxisXBottom(null);
			data.setAxisYLeft(null);
		}

		data.setBaseValue(Float.NEGATIVE_INFINITY);
		mChartView.setLineChartData(data);
	}

	@SuppressWarnings("static-access")
	private void generateValues() {
		List<WIFILine> findAll = mWifiLine.findAll(WIFILine.class);
//		listLevel = new ArrayList<Float>();
//		for (int j = 0; j < 1; ++j) {
//			listLevel.add(Float.valueOf(mWifiLine.findLast(WIFILine.class)+""));
//			listLevel.add(Float.valueOf(wifi.getConnectionInfo().getRssi()+""));
//		}
	}

	// 触摸事件
	@Override
	public void onValueDeselected() {
	}

	@Override
	public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
		// Toast.makeText(
		// ChartActivity.this,
		// "值Selected: " + value + "线lineIndex:" + lineIndex
		// + "点pointIndex:" + pointIndex, Toast.LENGTH_SHORT)
		// .show();
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
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStart() {
		isRefresh = true;// 开始刷新
		mWifiLine.deleteAll(WIFILine.class);
		super.onStart();
	}

	@Override
	protected void onStop() {
		isRefresh = false;// 停止刷新
		mWifiLine.deleteAll(WIFILine.class);
		super.onStop();
	}

}

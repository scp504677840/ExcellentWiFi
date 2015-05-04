package com.jzlg.excellentwifi.activity;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

import com.jzlg.excellentwifi.R;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * 图表
 * 
 * @author Administrator
 *
 */
public class ChartActivity extends Activity implements
		LineChartOnValueSelectListener {
	private LineChartView mChartView;
	private int numberOfLines = 1;// 当前线的数量
	private int maxNumberOfLines = 4;// 最大线的数量
	private int numberOfPoints = 12;// 最大点的数量
	private float[][] randomNumbersTab = new float[maxNumberOfLines][numberOfPoints];

	private ValueShape shape = ValueShape.CIRCLE;// CIRCLE:圆形;DIAMOND:菱形;SQUARE:正方形
	private boolean isCubic = false;// 是否是曲线
	private boolean isFilled = false;// 是否填充
	private boolean hasLabels = true;// 是否显示数值
	private boolean hasLabelsOnlyForSelected = false;// 仅仅对选中的点进行显示数值
	private boolean hasLines = true;// 线
	private boolean hasPoints = true;// 点
	private LineChartData data;
	private boolean hasAxes = true;
	private boolean hasAxesNames = true;
	private ActionBar actionBar;

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

	private void initView() {
		actionBar = getActionBar();
		actionBar.setTitle("信号走势");
		actionBar.setLogo(R.drawable.left_menu_levels_white);
		actionBar.setDisplayHomeAsUpEnabled(true);// 开启导航图标
		mChartView = (LineChartView) findViewById(R.id.chart_linechart);
	}

	private void generateData() {
		List<Line> lines = new ArrayList<Line>();
		// numberOfLines=1;numberOfPoints=12
		for (int i = 0; i < numberOfLines; i++) {
			// 数据源
			List<PointValue> values = new ArrayList<PointValue>();
			for (int j = 0; j < numberOfPoints; j++) {
				values.add(new PointValue(j, randomNumbersTab[i][j]));
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

	// 获取随机数并存储在4行12列的二维数组中
	private void generateValues() {
		// maxNumberOfLines=4;numberOfPoints=12
		for (int i = 0; i < numberOfLines; ++i) {
			for (int j = 0; j < numberOfPoints; ++j) {
				randomNumbersTab[i][j] = (float) Math.random() * 100f;
			}
		}
	}

	// 触摸事件
	@Override
	public void onValueDeselected() {
	}

	@Override
	public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
		Toast.makeText(
				ChartActivity.this,
				"值Selected: " + value + "线lineIndex:" + lineIndex
						+ "点pointIndex:" + pointIndex, Toast.LENGTH_SHORT)
				.show();
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

}

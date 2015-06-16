package com.jzlg.excellentwifi.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.jzlg.excellentwifi.R;
import com.jzlg.excellentwifi.utils.DepthPageTransformer;
import com.jzlg.excellentwifi.utils.ViewPagerCompat;
import com.jzlg.excellentwifi.utils.ViewPagerCompat.OnPageChangeListener;

/**
 * 引导页
 * 
 * @author 宋春鹏
 *
 */
public class GuideActivity extends BaseActivity implements OnPageChangeListener {
	private ViewPagerCompat mViewPager;
	private PagerAdapter mPagerAdapter;
	private List<ImageView> imageViews;
	private Button toMain;
	private int[] mImgIds = new int[] { R.drawable.guide01, R.drawable.guide02 };
	private int mPosition;// 当前页面位置
	private boolean isFirst;// 是否是第一次进入
	private SharedPreferences sharedPreferences;
	private Editor edit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_guide);
		init();
	}

	private void init() {
		toMain = (Button) findViewById(R.id.guide_tomain);
		mViewPager = (ViewPagerCompat) findViewById(R.id.guide_viewpager);
		sharedPreferences = getSharedPreferences("isFirst", MODE_PRIVATE);
		isFirst = sharedPreferences.getBoolean("isFirst", true);

		if (!isFirst) {
			mImgIds = new int[] { R.drawable.guide03 };
			toMain.setVisibility(View.VISIBLE);
		}
		imageViews = new ArrayList<ImageView>();
		for (int i = 0; i < mImgIds.length; i++) {
			ImageView imageView = new ImageView(this);
			imageView.setImageResource(mImgIds[i]);
			imageView.setScaleType(ScaleType.CENTER_CROP);
			imageViews.add(imageView);
		}
		mViewPager.setPageTransformer(true, new DepthPageTransformer());// 设置动画
		mPagerAdapter = new com.jzlg.excellentwifi.adapter.PagerAdapter(this,
				imageViews);// 适配器
		mViewPager.setAdapter(mPagerAdapter);// 加载适配器

		// 当引导页大于1时，我们给它设置页面改变事件，也就是用户是第一次进入应用
		if (mImgIds.length > 1) {
			mViewPager.setOnPageChangeListener(this);
		}

		toMain.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				edit = sharedPreferences.edit();
				edit.putBoolean("isFirst", false);
				edit.commit();
				edit.clear();
				// 滑至最后一页跳转首页
				Intent intent = new Intent(GuideActivity.this,
						MainActivity.class);
				GuideActivity.this.startActivity(intent);
				GuideActivity.this.finish();
			}
		});
	}

	// 当前新的页面被选中时
	@Override
	public void onPageSelected(int position) {
		mPosition = position;
		if (mImgIds.length - 1 == position) {
			toMain.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 当滑动状态进行改变时 1:开始滑动 2:停止滑动 0:什么都没做
	 */
	@Override
	public void onPageScrollStateChanged(int state) {
		switch (state) {
		case 1:// 开始滑动
			toMain.setVisibility(View.GONE);
			break;
		case 2:// 停止滑动
			break;
		case 0:// 什么都没做
			if (mPosition == mImgIds.length - 1) {
				toMain.setVisibility(View.VISIBLE);
			}
			break;

		default:
			break;
		}
	}

	/**
	 * 当页面被滑动时 position：当前页面，及你点击滑动的页面 positionOffset：当前页面偏移的百分比
	 * positionOffsetPixels：当前页面偏移的像素位置
	 */
	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	}

}

package com.jzlg.excellentwifi.activity;

import com.jzlg.excellentwifi.R;
import com.jzlg.excellentwifi.utils.DepthPageTransformer;
import com.jzlg.excellentwifi.utils.ViewPagerCompat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

/**
 * 引导页
 * 
 * @author 
 *
 */
public class GuideActivity extends BaseActivity {
	private ViewPagerCompat mViewPager;
	private Button tomain;
	private int[] mImgIds = new int[] { R.drawable.guide01, R.drawable.guide02 };

	// private List<ImageView> mImageViews = new ArrayList<ImageView>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_guide);
		init();
	}

	private void init() {
		tomain = (Button) findViewById(R.id.guide_tomain);
		tomain.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 滑至最后一页跳转首页
				Intent intent = new Intent(GuideActivity.this,
						MainActivity.class);
				GuideActivity.this.startActivity(intent);
				GuideActivity.this.finish();
			}
		});
		mViewPager = (ViewPagerCompat) findViewById(R.id.guide_viewpager);
		// 设置动画
		mViewPager.setPageTransformer(true, new DepthPageTransformer());
		mViewPager.setAdapter(new PagerAdapter() {
			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				if (position > 0) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// 滑至最后一页跳转首页
					Intent intent = new Intent(GuideActivity.this,
							MainActivity.class);
					GuideActivity.this.startActivity(intent);
					GuideActivity.this.finish();
				}
				ImageView imageView = new ImageView(GuideActivity.this);
				imageView.setImageResource(mImgIds[position]);
				imageView.setScaleType(ScaleType.CENTER_CROP);// 主要是为了让控件不变形
				container.addView(imageView);
				return imageView;
			}

			@Override
			public void destroyItem(ViewGroup container, int position,
					Object object) {
				container.removeView((View) object);
			}

			@Override
			public boolean isViewFromObject(View view, Object object) {
				return view == object;
			}

			// 有多少页
			@Override
			public int getCount() {
				return mImgIds.length;
			}
		});

	}

}

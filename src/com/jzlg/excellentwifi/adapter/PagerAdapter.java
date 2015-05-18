package com.jzlg.excellentwifi.adapter;

import java.util.List;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PagerAdapter extends android.support.v4.view.PagerAdapter {

	private Context context;
	private List<ImageView> list;

	public PagerAdapter(Context context, List<ImageView> list) {
		this.context = context;
		this.list = list;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewGroup)container).removeView(list.get(position));
	}

	// 加载View
	@Override
	public Object instantiateItem(View container, int position) {
		((ViewGroup)container).addView(list.get(position));
		return list.get(position);
	}

	// 判断当前的View是否是我们需要的View
	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return (arg0 == arg1);
	}

}

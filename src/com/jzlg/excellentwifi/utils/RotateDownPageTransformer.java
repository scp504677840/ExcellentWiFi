package com.jzlg.excellentwifi.utils;

import com.nineoldandroids.view.ViewHelper;

import android.support.v4.view.ViewPager.PageTransformer;
import android.view.View;

/**
 * PageView切换动画
 * 
 * @author
 *
 */
public class RotateDownPageTransformer implements PageTransformer {

	// 旋转
	private static final float MAX_ROTATE = 20f;
	private float mRot;

	// A页角度变化-20~0 B页角度变化 0~20
	@Override
	public void transformPage(View view, float position) {
		int pageWidth = view.getWidth();
		if (position < -1) {
			ViewHelper.setRotation(view, 0);

		} else if (position <= 0) {// A页0.0~1

			// 0~-20
			mRot = position * MAX_ROTATE;
			ViewHelper.setPivotX(view, pageWidth / 2);
			ViewHelper.setPivotY(view, view.getMeasuredHeight());
			ViewHelper.setRotation(view, mRot);
		} else if (position <= 1) {// B页1~0.0
			// 20 ~ 0
			mRot = position * MAX_ROTATE;
			ViewHelper.setPivotX(view, pageWidth / 2);
			ViewHelper.setPivotY(view, view.getMeasuredHeight());
			ViewHelper.setRotation(view, mRot);
		} else {
			ViewHelper.setRotation(view, 0);
		}

	}

}

package com.jzlg.excellentwifi.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * ¿◊¥Ô…®√ËBaseView
 * 
 * @author 
 *
 */
public class BaseView extends RelativeLayout {
	private Context mContext;

	public BaseView(Context context) {
		super(context);
		mContext = context;
	}

	public BaseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public BaseView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

}

package com.jzlg.excellentwifi.fragment;

import com.jzlg.excellentwifi.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 关于我们
 * 
 * @author 宋春鹏
 *
 */
public class MoreFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.layout_more, container, false);
	}
}

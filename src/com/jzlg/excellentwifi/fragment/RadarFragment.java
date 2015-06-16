package com.jzlg.excellentwifi.fragment;

import com.jzlg.excellentwifi.R;
import com.jzlg.excellentwifi.activity.RadarView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * À×´ï
 * 
 * @author ËÎ´ºÅô
 *
 */
public class RadarFragment extends Fragment {
	private View view;
	private RadarView radarView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.layout_radar, container, false);
		initView();
		return view;
	}

	private void initView() {
		radarView = (RadarView) view.findViewById(R.id.radar_view);
		radarView.setWillNotDraw(false);
	}
}

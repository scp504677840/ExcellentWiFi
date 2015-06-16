package com.jzlg.excellentwifi.utils;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

/**
 * 活动管理容器
 * 
 * @author 宋春鹏
 *
 */
public class ActivityCollector {
	public static List<Activity> activities = new ArrayList<Activity>();

	/**
	 * 添加活动
	 * 
	 * @param activity
	 *            活动
	 */
	public static void addActivity(Activity activity) {
		activities.add(activity);
	}

	/**
	 * 清除一个活动
	 * 
	 * @param activity
	 *            活动
	 */
	public static void removeActivity(Activity activity) {
		activities.remove(activity);
	}

	/**
	 * 销毁所有活动
	 */
	public static void finishAll() {
		for (Activity activity : activities) {
			if (!activity.isFinishing()) {
				activity.finish();
			}
		}
	}

}

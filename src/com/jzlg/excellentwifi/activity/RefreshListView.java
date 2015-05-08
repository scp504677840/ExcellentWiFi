package com.jzlg.excellentwifi.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.jzlg.excellentwifi.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public class RefreshListView extends ListView implements OnScrollListener {

	private View header;// 顶部布局文件
	private int headerHeight;// 顶部布局文件的高度
	private int firstVisibleItem;// 当前第一个可见的item的位置
	private int scrollState;// 当前滚动状态
	private boolean isRemark;// 标识，当前listview最顶端按下的
	private int starY;// 按下时Y值
	private int state;// 当前的状态
	private final int NONE = 0;// 正常状态
	private final int PULL = 1;// 提示下拉状态
	private final int RELESE = 2;// 提示释放状态
	private final int REFRESHING = 3;// 正在刷新状态
	private IRefreshListener iRefreshListener;//刷新数据的接口

	public RefreshListView(Context context) {
		super(context);
		initView(context);
	}

	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public RefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(context);
	}

	/**
	 * 初始化界面，添加顶部布局文件到listview
	 * 
	 * @param context
	 */
	private void initView(Context context) {

		header = LayoutInflater.from(context).inflate(
				R.layout.layout_listview_header, null);
		measureView(header);
		headerHeight = header.getMeasuredHeight();
		topPadding(-headerHeight);
		this.addHeaderView(header);// 添加顶部布局文件
		this.setOnScrollListener(this);
	}

	/**
	 * 通知父布局此布局大小
	 * 
	 * @param view
	 */
	private void measureView(View view) {
		ViewGroup.LayoutParams p = view.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		// spec:外边距;padding:内边距;childDimension:子布局的宽度
		int width = ViewGroup.getChildMeasureSpec(0, 0, p.width);
		int height;
		int tempHeight = p.height;
		if (tempHeight > 0) {
			height = MeasureSpec.makeMeasureSpec(tempHeight,
					MeasureSpec.EXACTLY);
		} else {
			height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}

		view.measure(width, height);
	}

	/**
	 * 设置header布局的上边距
	 * 
	 * @param topPadding
	 */
	private void topPadding(int topPadding) {
		header.setPadding(header.getPaddingLeft(), topPadding,
				header.getPaddingRight(), header.getPaddingBottom());
		header.invalidate();
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		this.firstVisibleItem = firstVisibleItem;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		this.scrollState = scrollState;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (firstVisibleItem == 0) {
				isRemark = true;
				starY = (int) ev.getY();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			onMove(ev);
			break;
		case MotionEvent.ACTION_UP:
			if (state == RELESE) {
				state = REFRESHING;

				// 加载最新数据
				refreshViewByState();
				iRefreshListener.onRefresh();
			} else if (state == PULL) {
				state = NONE;
				isRemark = false;
				refreshViewByState();
			}
			break;

		default:
			break;
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * 判断移动过程中的操作
	 * 
	 * @param ev
	 */
	private void onMove(MotionEvent ev) {
		if (!isRemark) {
			return;
		}
		int tempY = (int) ev.getY();// 当前移动到什么位置
		int space = tempY - starY;// 当前移动的距离
		int topPadding = space - headerHeight;
		switch (state) {
		case NONE:
			if (space > 0) {
				state = PULL;
				refreshViewByState();
			}
			break;
		case PULL:
			topPadding(topPadding);
			if (space > headerHeight + 30
					&& scrollState == SCROLL_STATE_TOUCH_SCROLL) {
				state = RELESE;
				refreshViewByState();
			}
			break;
		case RELESE:
			topPadding(topPadding);
			if (space < headerHeight + 30) {
				state = PULL;
				refreshViewByState();
			} else if (space <= 0) {
				state = NONE;
				isRemark = false;
				refreshViewByState();
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 根据当前状态改变界面显示
	 */
	private void refreshViewByState() {
		TextView tip = (TextView) header
				.findViewById(R.id.listview_header_right_tip_refresh);
		ImageView arrow = (ImageView) header
				.findViewById(R.id.listview_header_left_arrow);
		ProgressBar progressBar = (ProgressBar) header
				.findViewById(R.id.listview_header_left_progress);
		RotateAnimation anim1 = new RotateAnimation(0, 180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		anim1.setDuration(1000);// 设置时间间隔500毫秒
		anim1.setFillAfter(true);
		RotateAnimation anim2 = new RotateAnimation(180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		anim1.setDuration(1000);// 设置时间间隔500毫秒
		anim1.setFillAfter(true);
		switch (state) {
		case NONE:
			arrow.clearAnimation();// 移除动画
			topPadding(-headerHeight);
			break;
		case PULL:
			arrow.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			tip.setText("下拉可以刷新");
			arrow.clearAnimation();
			arrow.setAnimation(anim2);
			break;
		case RELESE:
			arrow.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			tip.setText("松开可以刷新");
			arrow.clearAnimation();
			arrow.setAnimation(anim1);
			break;
		case REFRESHING:
			topPadding(50);
			arrow.setVisibility(View.GONE);
			progressBar.setVisibility(View.VISIBLE);
			tip.setText("正在刷新...");
			arrow.clearAnimation();
			break;

		default:
			break;
		}
	}

	/**
	 * 获取完数据
	 */
	public void refreshComplete() {
		state = NONE;
		isRemark = false;
		refreshViewByState();
		TextView lastUpdateTime = (TextView) header
				.findViewById(R.id.listview_header_right_tip_time);
		SimpleDateFormat smf = new SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss");
		Date date = new Date(System.currentTimeMillis());

		String time = smf.format(date);
		lastUpdateTime.setText(time);
	}
	
	
	public void setInterface(IRefreshListener iRefreshListener){
		this.iRefreshListener = iRefreshListener;
	}
	
	/**
	 * 刷新数据接口
	 * @author Administrator
	 *
	 */
	public interface IRefreshListener{
		public void onRefresh();
	}

}

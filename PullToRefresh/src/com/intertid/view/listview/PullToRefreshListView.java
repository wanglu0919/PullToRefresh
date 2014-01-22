package com.intertid.view.listview;

import java.util.Date;

import com.intertid.pulltorefresh.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ProgressBar;

/**
 * 下拉刷新组件
 * 
 * @author wanglu 泰得利通
 * 
 */
public class PullToRefreshListView extends ListView implements OnScrollListener {

	private PullToRefreshListener pullToRefreshListener;
	private int visibleFristItemIndex;
	private View headView;
	private ImageView arrow;
	private ProgressBar progressBar;
	private TextView title;
	private TextView lastUpdate;
	private Animation animation;
	private Animation reversAnimation;
	private static final int PULL_TO_REFRESH = 0;// 下拉刷新
	private static final int REFRESHING = 1;// 正在刷新
	private static final int RELEASE_TOREFRESH = 2;// 释放刷新
	private static final int DONE = 3;// 刷新完成
	private boolean isRecodeTouch;// 是否开始记录了坐标
	private static int state;
	private float startY;
	private int headViewHight;
	private boolean isReleaseBack;// 是否是又释放刷新返回到下拉刷新状态

	public PullToRefreshListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public PullToRefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PullToRefreshListView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		LayoutInflater layoutInflater = LayoutInflater.from(context);

		headView = layoutInflater.inflate(R.layout.header, null);
		arrow = (ImageView) headView.findViewById(R.id.arrow);
		progressBar = (ProgressBar) headView.findViewById(R.id.progressBar);
		title = (TextView) headView.findViewById(R.id.title);
		lastUpdate = (TextView) headView.findViewById(R.id.last_update);
		arrow.setMinimumHeight(70);
		arrow.setMinimumWidth(50);

		measureHeadView(headView);
		headViewHight = headView.getMeasuredHeight();
		int headViewWidth = headView.getMeasuredWidth();

		headView.setPadding(0, -1 * headViewHight, 0, 0);

		addHeaderView(headView);

		invalidate();// 重绘界面

		setOnScrollListener(this);// 添加滑动监听

		animation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

		animation.setDuration(300);
		animation.setFillAfter(true);
		animation.setInterpolator(new LinearInterpolator());

		reversAnimation = new RotateAnimation(180, 0,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);

		reversAnimation.setDuration(300);
		reversAnimation.setFillAfter(true);
		reversAnimation.setInterpolator(new LinearInterpolator());

		state = DONE;

	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		if (pullToRefreshListener != null) {

			switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (this.visibleFristItemIndex == 0 && !isRecodeTouch) {// 第一项可见并且没有开始记录
					startY = ev.getY();
					isRecodeTouch = true;
				}

				break;
			case MotionEvent.ACTION_MOVE:

				float tempY = ev.getY();
				if (this.visibleFristItemIndex == 0 && !isRecodeTouch) {// 第一项可见并且没有开始记录
					startY = ev.getY();
					isRecodeTouch = true;
				}

				if (state != REFRESHING) {// 不是正在刷状态进行处理

					if (state == PULL_TO_REFRESH) {// 下拉刷新状态

						if (tempY - startY > 0
								&& tempY - startY > headViewHight) {// 下拉状态变成释放刷新状态

							state = RELEASE_TOREFRESH;
							changeState();

						}

						if (tempY - startY < 0) {// 由下拉刷新到完成，用户向上推
							state = DONE;
							changeState();
						}

					}

					if (state == RELEASE_TOREFRESH) {// 释放刷新状态
						if (tempY - startY > 0
								&& tempY - startY < headViewHight) {// 释放刷新到刷新状态
							isReleaseBack = true;
							state = PULL_TO_REFRESH;
							changeState();

						}

						if (tempY - startY <= 0) {

							state = DONE;
							changeState();
						}

					}

					if (state == DONE) {// 刷新完成状态
						if (tempY - startY > 0) {

							state = PULL_TO_REFRESH;

							changeState();
						}
					}

					if (state == PULL_TO_REFRESH || state == RELEASE_TOREFRESH) {
						headView.setPadding(0,
								(int) ((tempY - startY) - headViewHight), 0, 0);
					}

				}

				break;
			case MotionEvent.ACTION_UP:

				if (state != REFRESHING) {
					if (state == DONE) {
						// ����Ҫ����
					}

					if (state == PULL_TO_REFRESH) {
						state = DONE;

						changeState();
					}

					if (state == RELEASE_TOREFRESH) {
						state = REFRESHING;

						changeState();

						onRefresh();// ˢ�� �õ����������
					}
				}

				break;

			}

		}

		return super.onTouchEvent(ev);
	}

	private void onRefresh() {

		if (pullToRefreshListener != null) {
			pullToRefreshListener.onRefresh();
		}

	}

	/**
	 * 状态改变 wanglu 泰得利通
	 */
	private void changeState() {
		switch (state) {
		case PULL_TO_REFRESH:

			arrow.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			title.setVisibility(View.VISIBLE);
			lastUpdate.setVisibility(View.VISIBLE);

			title.setText("PULL_TO_REFRESH");
			arrow.clearAnimation();

			if (isReleaseBack) {
				arrow.startAnimation(animation);
				isReleaseBack = false;
			}
			break;
		case REFRESHING:
			arrow.setVisibility(View.GONE);
			progressBar.setVisibility(View.VISIBLE);
			title.setVisibility(View.VISIBLE);
			lastUpdate.setVisibility(View.VISIBLE);

			title.setText("REFRESHING");
			arrow.clearAnimation();

			headView.setPadding(0, 0, 0, 0);

			break;
		case RELEASE_TOREFRESH:

			arrow.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			title.setVisibility(View.VISIBLE);
			lastUpdate.setVisibility(View.VISIBLE);

			title.setText("RELEASE_TOREFRESH");
			arrow.clearAnimation();
			arrow.startAnimation(reversAnimation);
			break;
		case DONE:
			arrow.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			title.setVisibility(View.VISIBLE);
			lastUpdate.setVisibility(View.VISIBLE);

			title.setText("完成");
			arrow.clearAnimation();

			headView.setPadding(0, -1 * headViewHight, 0, 0);

			break;

		}
	}

	/**
	 * 测量handView wanglu 泰得利通
	 * 
	 * @param headView2
	 */
	private void measureHeadView(View headView) {

		ViewGroup.LayoutParams lp = headView.getLayoutParams();// 获取headView布局

		if (lp == null) {
			lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}

		int childMeasureWidth = ViewGroup.getChildMeasureSpec(0, 0, lp.width);
		int childMeasureHeight;

		if (lp.height > 0) {
			childMeasureHeight = MeasureSpec.makeMeasureSpec(lp.height,
					MeasureSpec.EXACTLY);
		} else {
			childMeasureHeight = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}

		headView.measure(childMeasureWidth, childMeasureHeight);

	}

	public interface PullToRefreshListener {
		public void onRefresh();
	}

	public void setPullToRefreshListener(
			PullToRefreshListener pullToRefreshListener) {
		this.pullToRefreshListener = pullToRefreshListener;
	}

	/**
	 * 刷新完成 wanglu 泰得利通
	 */
	public void onRefreshComplete() {
		state = DONE;
		changeState();

		lastUpdate.setText("上次更新时间" + new Date().toLocaleString());
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		visibleFristItemIndex = visibleItemCount;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		lastUpdate.setText("上次更新时间 " + new Date().toLocaleString());
		super.setAdapter(adapter);
	}

}

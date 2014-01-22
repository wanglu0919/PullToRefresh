package com.intertid.pulltorefresh;

import java.util.ArrayList;
import java.util.List;

import com.intertid.view.listview.PullToRefreshListView;
import com.intertid.view.listview.PullToRefreshListView.PullToRefreshListener;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MainActivity extends Activity {

	private PullToRefreshListView pullToRefreshListView;
	private List<String> data = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		initData();
		pullToRefreshListView = (PullToRefreshListView) findViewById(R.id.main_lv);
	   final  MListAdapter mAdapter=new MListAdapter();
		pullToRefreshListView.setAdapter(mAdapter);

		pullToRefreshListView
				.setPullToRefreshListener(new PullToRefreshListener() {

					@Override
					public void onRefresh() {// 刷新回调

						new AsyncTask<Void, Void, Void>() {

							@Override
							protected void onPostExecute(Void result) {
								super.onPostExecute(result);
								mAdapter.notifyDataSetChanged();
								
								pullToRefreshListView.onRefreshComplete();
							}

							@Override
							protected Void doInBackground(Void... params) {
								SystemClock.sleep(2000);
								data.add("增加数据1");
								data.add("增加数据2");
								data.add("增加数据3");
								

								return null;
							}
						}.execute(null);

					}
				});

	}

	static class ViewHolder {
		TextView tv;
	}

	private final class MListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {

			return data.get(position);
		}

		@Override
		public long getItemId(int position) {

			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			String content = data.get(position);
			ViewHolder viewHolder = null;
			if (convertView == null) {
				TextView textView = new TextView(getApplicationContext());
				textView.setTextColor(Color.WHITE);
				viewHolder = new ViewHolder();
				viewHolder.tv = textView;

				convertView = textView;
				convertView.setTag(viewHolder);

			} else {

				viewHolder = (ViewHolder) convertView.getTag();
			}

			viewHolder.tv.setText(content);

			return convertView;
		}

	}

	private void initData() {
		data.add("a");
		data.add("b");
		data.add("c");

	}

}

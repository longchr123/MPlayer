package com.lcr.fragment;

import java.util.ArrayList;
import java.util.List;

import io.vov.vitamio.activity.InitActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lcr.adapter.HomeAdapter;
import com.lcr.adapter.WorksAdapter;
import com.lcr.bean.VideoInfo;
import com.lcr.config.Config;
import com.lcr.mplay.R;
import com.lcr.mplay.VideoPlayActivity;
import com.lcr.widget.PullToRefreshLayout;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.shizhefei.fragment.LazyFragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MyWorksFragment extends LazyFragment implements OnItemClickListener {

	private ListView mListView;
	private WorksAdapter adapter;
	private String url;
	private List<VideoInfo> videoInfos;
	
	@Override
	protected void onCreateViewLazy(Bundle savedInstanceState) {
		super.onCreateViewLazy(savedInstanceState);
		setContentView(R.layout.fragment_my_works);
		init();
		getData();
	}

	private void init() {
		mListView=(ListView)findViewById(R.id.lv_works);
		mListView.setOnItemClickListener(this);
		url = "http://"+Config.localhost+":8080/axis2/services/XPlayServer/getAllMyVideo";
	}
	
	/**
	 * 
	 * @param pullToRefreshLayout
	 *            刷新布局，用于设置刷新状态
	 * @param state
	 *            等于1头部加载，等于2尾部加载
	 * @param url
	 *            请求网址
	 * @param num1
	 *            请求一次加载的数据量
	 * @param index1
	 *            从什么地方加载 1表时刷新
	 */
	private void getData() {
		final Gson gson = new Gson();
		HttpUtils http = new HttpUtils();
		http.send(HttpRequest.HttpMethod.GET, url,
				new RequestCallBack<String>() {
					@Override
					public void onLoading(long total, long current,
							boolean isUploading) {
					}

					@Override
					public void onSuccess(ResponseInfo<String> responseInfo) {
						if (!responseInfo.result.contains("<ns:return>")) {
							return;
						}
						String string = responseInfo.result
								.split("<ns:return>")[1];
						string = string.split("</ns:return>")[0];
						videoInfos = gson.fromJson(string,
								new TypeToken<List<VideoInfo>>() {
								}.getType());
						adapter=new WorksAdapter(getActivity(), videoInfos, R.layout.item_works_1);
						mListView.setAdapter(adapter);
					}

					@Override
					public void onStart() {
					}

					@Override
					public void onFailure(HttpException error, String msg) {
						Log.e("1111111", msg);
					}
				});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		VideoInfo item = videoInfos.get(position);
		Intent intent = new Intent(getActivity(), VideoPlayActivity.class);
		intent.setData(Uri.parse(item.getUrl().replace("localhost",
				Config.localhost)));
		Bundle extras = new Bundle();
		intent.putExtra("position", position);
		extras.putParcelableArrayList("mVideoInfos",
				(ArrayList<VideoInfo>) videoInfos);
		intent.putExtras(extras);
		startActivity(intent);
	}
}

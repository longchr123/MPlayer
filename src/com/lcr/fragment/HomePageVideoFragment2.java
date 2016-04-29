package com.lcr.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lcr.adapter.HomeAdapter2;
import com.lcr.bean.VideoInfo;
import com.lcr.config.Config;
import com.lcr.mplay.R;
import com.lcr.mplay.VideoPlayActivity;
import com.lcr.widget.PullToRefreshLayout;
import com.lcr.widget.PullToRefreshLayout.OnRefreshListener;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

public class HomePageVideoFragment2 extends Fragment implements
		OnRefreshListener,OnItemClickListener{

	private View mContentView;
	private List<com.lcr.bean.VideoInfo> mVideoInfos;
	private List<com.lcr.bean.VideoInfo> videoInfos;// 替换用的
	private HomeAdapter2 mHomeAdapter;
	private ListView mListView;
	private PullToRefreshLayout mPtrlVideo;
	private int index = 0;
	private String url;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContentView = inflater.inflate(R.layout.fragment_home_page2, null);
		init();
		getData(null, 0, 1, 1);
		return mContentView;
	}

	private void init() {
		mListView = (ListView) mContentView
				.findViewById(R.id.list_view);
		mListView.setOnItemClickListener(this);
		mPtrlVideo = (PullToRefreshLayout) mContentView
				.findViewById(R.id.fra_home_ptrl2);
		mPtrlVideo.setOnRefreshListener(this);
		url = "http://" + Config.localhost
				+ ":8080/axis2/services/XPlayServer/getPartVideoList";
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		VideoInfo item = mVideoInfos.get(position);
		Intent intent = new Intent(getActivity(), VideoPlayActivity.class);
		intent.setData(Uri.parse(item.getUrl().replace("localhost",
				Config.localhost)));
		Bundle extras = new Bundle();
		intent.putExtra("position", position);
		extras.putParcelableArrayList("mVideoInfos",
				(ArrayList<VideoInfo>) mVideoInfos);
		intent.putExtras(extras);
		startActivity(intent);
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
	private void getData(final PullToRefreshLayout pullToRefreshLayout,
			final int state, int num1, int index1) {
		final Gson gson = new Gson();
		videoInfos = new ArrayList<VideoInfo>();
		HttpUtils http = new HttpUtils();
		RequestParams params = new RequestParams();
		params.addBodyParameter("num", num1 * 12 + "");
		if (state == 2) {
			params.addBodyParameter("index", index1 + "");
		} else {
			params.addBodyParameter("index", "1");
		}
		http.send(HttpRequest.HttpMethod.POST, url, params,
				new RequestCallBack<String>() {
					@Override
					public void onLoading(long total, long current,
							boolean isUploading) {
					}

					@Override
					public void onSuccess(ResponseInfo<String> responseInfo) {
						if (!responseInfo.result.contains("<ns:return>")
								&& pullToRefreshLayout != null) {
							mPtrlVideo
									.refreshFinish(PullToRefreshLayout.SUCCEED);
							mPtrlVideo
									.loadmoreFinish(PullToRefreshLayout.SUCCEED);
							return;
						}
						String string = responseInfo.result
								.split("<ns:return>")[1];
						string = string.split("</ns:return>")[0];
						videoInfos = gson.fromJson(string,
								new TypeToken<List<VideoInfo>>() {
								}.getType());
						if (videoInfos != null && state == 1) {
							int _id = mVideoInfos.get(0).get_Id();
							// 头部加载
							for (VideoInfo videoInfo : videoInfos) {
								if (_id != videoInfo.get_Id()) {
									mVideoInfos.add(0, videoInfo);
								} else {
									break;
								}
							}
						} else if (videoInfos != null && state == 2) {
							// 尾部加载
							for (VideoInfo videoInfo : videoInfos) {
								mVideoInfos.add(videoInfo);
							}
						} else {
							mVideoInfos = videoInfos;
						}
						mHomeAdapter = new HomeAdapter2(getActivity(),
								mVideoInfos, R.layout.item_home_2);
						mListView.setAdapter(mHomeAdapter);
						index = mVideoInfos.size() + 1;
						if (pullToRefreshLayout != null) {
							mPtrlVideo
									.refreshFinish(PullToRefreshLayout.SUCCEED);
							mPtrlVideo
									.loadmoreFinish(PullToRefreshLayout.SUCCEED);
						}
					}

					@Override
					public void onStart() {
					}

					@Override
					public void onFailure(HttpException error, String msg) {
						System.out.println(msg.toString());
						mPtrlVideo.refreshFinish(PullToRefreshLayout.FAIL);
						mPtrlVideo.loadmoreFinish(PullToRefreshLayout.FAIL);
					}
				});
	}
	
	@Override
	public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
		getData(pullToRefreshLayout, 1, 1, 1);
	}

	@Override
	public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
		getData(pullToRefreshLayout, 2, 1, index);
	}
}

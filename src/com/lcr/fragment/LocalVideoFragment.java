package com.lcr.fragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.lcr.bean.VideoInfo;
import com.lcr.mplay.R;
import com.lcr.mplay.VideoPlayActivity;
import com.lcr.utils.FormatUtils;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class LocalVideoFragment extends Fragment implements OnItemClickListener{

	private View mContentView;

	private ListView mVideoListView;
	private TextView mUnfindTextView;
	private List<VideoInfo> items;
	private FormatUtils utils;
	
	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (items!=null&&items.size()>0) {
				mVideoListView.setAdapter(new VideoListAdapter());
				mUnfindTextView.setVisibility(View.INVISIBLE);
			}else {
				mUnfindTextView.setVisibility(View.VISIBLE);
			}
		};
	};
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContentView = inflater.inflate(R.layout.fragment_local_video, null);
		mVideoListView = (ListView) mContentView.findViewById(R.id.lv_video_list);
		mUnfindTextView = (TextView) mContentView.findViewById(R.id.tv_unfind);
		items=new ArrayList<VideoInfo>();
		utils=new FormatUtils();
		// 获得本地视频列表
		getAllVideo();
		
		mVideoListView.setOnItemClickListener(this);
		return mContentView;
	}
	
	/**
	 * 媒体扫描器工具：
		1.手机在可读状态下，系统会发一个广播给媒体扫描器，将手机里的把有多媒体信息扫描归类到数据中。
		2.下载一个视频时，下载完成了。可以向媒体扫描器发一个广播，让安扫描指定目录。
		3.把视频拖到sd卡下，但是没有数据，需要发广播通知扫描。a.重起， b.sd插拔，c. 手动让模拟器发广播
	 */
	private void getAllVideo() {

		new Thread() {
			@Override
			public void run() {
				ContentResolver resolver = getActivity().getContentResolver();
				Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				// 需要得到——标题，时间，大小，路径
				String[] projection = { MediaStore.Video.Media.TITLE,
						MediaStore.Video.Media.DURATION,
						MediaStore.Video.Media.SIZE,
						MediaStore.Video.Media.DATA };
				Cursor cursor = resolver.query(uri, projection, null, null,
						null);
				
				while (cursor.moveToNext()) {
					VideoInfo item = new VideoInfo();
					
					long size = cursor.getLong(2);
					//屏蔽视频大小小于3MB的小文件
					if (size<3*1024*1204) {
						continue;//按运行效果看：已经屏蔽了一个系统自带的小视频
					}
					
					String title = cursor.getString(0);
					item.setTitle(title);
					//视频已删除，但数据库还在，所以无法利用duration来屏蔽只有几秒的视频，所以只能用size
					String duration = cursor.getString(1);
					item.setDuration(duration);
					
					item.setSize(size+"");
					String data = cursor.getString(3);
					item.setUrl(data);
					items.add(item);
				}
				handler.sendEmptyMessage(0);
			}
		}.start();

	}

	
	private class VideoListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			ViewHolder holder;
			if (convertView!=null) {
				view=convertView;
				holder=(ViewHolder) view.getTag();
			}else {
				view=View.inflate(getActivity(), R.layout.videolist_item, null);
				holder=new ViewHolder();
				holder.nameTextView=(TextView) view.findViewById(R.id.tv_name);
				holder.sizeTextView=(TextView) view.findViewById(R.id.tv_size);
				holder.durationTextView=(TextView) view.findViewById(R.id.tv_duration);
				view.setTag(holder);
			}
			VideoInfo item=items.get(position);
			holder.nameTextView.setText(item.getTitle());
			//将byte转为相应的值
			holder.sizeTextView.setText(Formatter.formatFileSize(getActivity(),Integer.parseInt(item.getSize()))+"");
			//将ms转为可视化时间
			holder.durationTextView.setText(utils.stringForTime(Integer.parseInt(item.getDuration())));
			return view;
		}
		
	}
	
	class ViewHolder{
		TextView nameTextView;
		TextView sizeTextView;
		TextView durationTextView;
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		VideoInfo item=items.get(position);
		Intent intent =new Intent(getActivity(),VideoPlayActivity.class);
		intent.setData(Uri.parse(item.getUrl()));
		Bundle extras=new Bundle();
		intent.putExtra("position", position);
		extras.putSerializable("videlist", (Serializable) items);
		intent.putExtras(extras);
		startActivity(intent);
		
	}
	
	
}

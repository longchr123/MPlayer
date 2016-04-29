package com.yixia.camera.demo.ui.record;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lcr.mplay.R;
import com.yixia.camera.demo.log.Logger;
import com.yixia.camera.demo.po.Video;
import com.yixia.camera.demo.ui.BaseActivity;
import com.yixia.camera.demo.ui.record.adapter.ImportVideoSelectionAdapter;
import com.yixia.weibo.sdk.util.ConvertToUtils;
import com.yixia.weibo.sdk.util.DeviceUtils;
import com.yixia.weibo.sdk.util.FileUtils;
import com.yixia.weibo.sdk.util.StringUtils;

public class ImportVideoSelectActivity extends BaseActivity implements OnItemClickListener, OnClickListener {

	private static final String[] VIDEO_PROJECT = { MediaStore.Video.Media._ID, MediaStore.Video.Media.DATE_MODIFIED, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.DATA };
	/** 一行显示个数 */
	private static final int ROW_ITEM_COUNT = 3;

	private TextView titleLeft, titleText, titleRightText;
	private ImageView titleRight;
	private ListView videoListView;
	//加载进度条
	protected View mProgressView;
	protected TextView mNothing;
	private long mLastVideoDate = -1;
	/** 指定文件夹 */
	private String mFromFolder, mFolderName;
	private int mItemHeight;
	private ImportVideoSelectionAdapter selectionAdapter;
	private ArrayList<VideoRow> videoList;

	public static final class VideoRow {
		public String date;
		public long added;
		public List<Video> images;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_import_video_selection);
		mFromFolder = getIntent().getStringExtra("folder");
		mFolderName = getIntent().getStringExtra("foldername");
		initView();
	}

	private void initView() {
		mItemHeight = (DeviceUtils.getScreenWidth(this) - (ConvertToUtils.dipToPX(this, 5) * 4)) / 3;
		mProgressView = findViewById(R.id.loading);
		mNothing = (TextView) findViewById(R.id.nodata);
		titleLeft = (TextView) findViewById(R.id.titleLeft);
		titleLeft.setOnClickListener(this);
		titleText = (TextView) findViewById(R.id.titleText);
		titleText.setText(mFolderName);
		titleRight = (ImageView) findViewById(R.id.titleRight);
		titleRightText = (TextView) findViewById(R.id.titleRightTextView);
		videoListView = (ListView) findViewById(R.id.folder_list);
		videoListView.setOnItemClickListener(this);
		selectionAdapter = new ImportVideoSelectionAdapter(this, mItemHeight);
		videoListView.setAdapter(selectionAdapter);

		videoList = loadData();
		selectionAdapter.updateVideoData(videoList);
	}

	private ArrayList<VideoRow> loadData() {
		// long startTime = System.currentTimeMillis();
		// 取出视频数据
		String selection = "";
		if (mLastVideoDate > -1) {
			selection = MediaColumns.DATE_MODIFIED + " < " + mLastVideoDate;
		}

		// 读取指定文件夹下的图片，读取所有，不区分文件夹
		if (StringUtils.isNotEmpty(mFromFolder)) {
			if (StringUtils.isNotEmpty(selection)) {
				selection += " AND ";
			}
			selection += MediaColumns.DATA + " like '" + mFromFolder + "%'";
		}

		ContentResolver mContentResolver = getContentResolver();

		// //统计系统视频数量
		// Cursor cursor1 =
		// mContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
		// null, null, null, MediaColumns.DATE_MODIFIED + " DESC");
		// Logger.e("[ImportVideoSelectionActivity]onPaged..." +
		// cursor1.getCount());
		// cursor1.close();

		// DATE_MODIFIED是以秒为单位
		Cursor cursor = mContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, VIDEO_PROJECT, selection, null, MediaColumns.DATE_MODIFIED + " DESC");
		HashMap<String, ArrayList<Video>> mDataResult = new HashMap<String, ArrayList<Video>>();
		ArrayList<Video> images = new ArrayList<Video>();

		if (cursor != null) {
			int idxId = cursor.getColumnIndex(BaseColumns._ID);
			int idxModified = cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED);
			int indxSize = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
			int idxData = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
			while (cursor.moveToNext()) {

				String path = cursor.getString(idxData);
				if (StringUtils.isNotEmpty(path)) {
					File file = new File(path);
					if (file != null && file.canRead()) {
						String folder = file.getParent();

						Logger.e("samuel", "1111folder>>>>>" + folder + ">>>>path>>>" + path);

						if (folder.toLowerCase(Locale.CHINESE).equals(mFromFolder.toLowerCase(Locale.CHINESE))) {
							Logger.e("samuel", "22222folder>>>>>" + folder + ">>>>path>>>" + path);
							long _id = cursor.getLong(idxId);
							long modified = cursor.getLong(idxModified);
							int duration = cursor.getInt(indxSize);
							// String date = DateUtil.formatDate(modified *
							// 1000L);

							int orientation = 0;
							if (!path.contains("miaopai/theme")) {
								if (DeviceUtils.hasJellyBeanMr1()) {
									MediaMetadataRetriever metadata = new MediaMetadataRetriever();
									try {
										metadata.setDataSource(path);
										orientation = ConvertToUtils.toInt(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION), 0);
									} catch (Exception e) {
										Logger.d("path = " + path);
									}
								}

								if (FileUtils.checkFile(path)) {
									// if
									// (StringUtils.isNotEmpty(mFromFolder)
									// && !IsUtils.equals(mFromFolder, new
									// File(path).getParent())) {
									// continue;
									// }

									// if (mDataResult.containsKey(date)) {
									// images = mDataResult.get(date);
									// } else {
									// if (countRows(mDataResult) >=
									// mPageCount)
									// break;
									// images = new
									// ArrayList<ImportVideoSelectionActivity.Video>();
									// mDataResult.put(date, images);
									// }
									if (mLastVideoDate == -1 || modified < mLastVideoDate) {
										mLastVideoDate = modified;
									}
									if (duration >= 3 * 1000) {
										Video video = new Video(path, modified, duration);
										video._id = _id;
										video.orientation = orientation;
										images.add(video);
									}
								}
							}

						}
					}
				}
			}
			cursor.close();
		}

		int countRows = countRows(mDataResult);
		// 按时间排序
		Collections.sort(images, new Comparator<Video>() {

			@Override
			public int compare(Video lhs, Video rhs) {
				if (lhs.added == rhs.added)
					return 0;
				else
					return rhs.added > lhs.added ? 1 : -1;
			}

		});

		// 将数据拆分成一行3个来显示，模拟GridView的效果
		ArrayList<VideoRow> result = new ArrayList<VideoRow>();
		VideoRow row = null;
		for (int i = 0; i < images.size(); i++) {
			if (i % 3 == 0) {
				row = new VideoRow();
				row.images = new ArrayList<Video>();
				result.add(row);
			}
			if (row != null && row.images != null) {
				row.images.add(images.get(i));
			}
		}
		return result;
	}

	/** 计算总行数 */
	private static int countRows(HashMap<String, ArrayList<Video>> mDataResult) {
		int result = 0;
		for (ArrayList<Video> videos : mDataResult.values()) {
			if (videos != null) {
				result += videos.size() / ROW_ITEM_COUNT;
				if (videos.size() % ROW_ITEM_COUNT != 0)
					result += 1;
			}
		}
		return result;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.titleLeft:
			onBackPressed();
			break;

		default:
			break;
		}
	}

}

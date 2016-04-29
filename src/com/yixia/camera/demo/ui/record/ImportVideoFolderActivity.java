package com.yixia.camera.demo.ui.record;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.yixia.camera.demo.po.VideoThumb;
import com.yixia.camera.demo.ui.BaseActivity;
import com.yixia.camera.demo.ui.record.adapter.ImportVideoFolderAdapter;
import com.yixia.weibo.sdk.VCamera;
import com.yixia.weibo.sdk.util.ConvertToUtils;
import com.yixia.weibo.sdk.util.DeviceUtils;
import com.yixia.weibo.sdk.util.FileUtils;
import com.yixia.weibo.sdk.util.StringUtils;

/**
 * 视频列表文件夹展示
 * 
 * @author leichunguan
 * 
 */
public class ImportVideoFolderActivity extends BaseActivity implements OnClickListener, OnItemClickListener{

	/** Gif图片Selection条件 */
	public static final String QUERY_SELECTION = MediaStore.Images.Media.DATA + " like '%.gif'";
	/** 字段 */
	public static final String[] QUERY_PROJECTION = { MediaStore.MediaColumns.DATA, MediaStore.MediaColumns._ID };

	private static final String[] VIDEO_PROJECT = { MediaStore.Video.Media._ID, MediaStore.Video.Media.DATE_MODIFIED, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.DATA };

	/** 是否来自多格 */
	private boolean mFromMulti;

	public static final class VideoFolder extends VideoThumb {
		/** 文件夹名称 */
		public String name;
		/** 文件夹文件数量 */
		public int count = 0;
		/** 文件夹路径 */
		public String path;

		public Video video;
	}
	
	private ListView mListView;
//加载进度条
	protected View mProgressView;
	protected TextView mNothing;
	protected View titleLayout;
	protected TextView titleLeft, titleText;
	protected ImageView titleRight;
	private ArrayList<VideoFolder> folderlist;
	
	private ImportVideoFolderAdapter videoFolderAdapter;
	
	private static final int REQUEST_CODE_MULTI = 200;
	private static final int REQUEST_CODE = 201;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_import_image_folder);
		mFromMulti = getIntent().getBooleanExtra("fromMulti", false);
		videoFolderAdapter = new ImportVideoFolderAdapter(this);
		initView();
	}

	private void initView(){
		mListView = (ListView) findViewById(R.id.folder_list);
		mListView.setAdapter(videoFolderAdapter);
		mProgressView = findViewById(R.id.loading);
		mNothing = (TextView) findViewById(R.id.nodata);
		titleLayout = findViewById(R.id.title_layout);
		titleText = (TextView) findViewById(R.id.titleText);
		View vtl = findViewById(R.id.titleLeft);
		if (vtl instanceof TextView) {
			titleLeft = (TextView) vtl;
		}
		View vtr = findViewById(R.id.titleRight);
		if (vtr instanceof ImageView) {
			titleRight = (ImageView) vtr;
		}
		titleLeft.setOnClickListener(this);
		mListView.setOnItemClickListener(this);
		mListView.setSelector(R.drawable.import_folder_selector);
		titleText.setText(R.string.record_camera_import_video_folder_title);
	}

		protected List<VideoFolder> loadData() throws Exception {

			final String cameraFolder = VCamera.getVideoCachePath();
			Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, VIDEO_PROJECT, null, null, MediaStore.MediaColumns.DATE_MODIFIED + " DESC");
			HashMap<String, VideoFolder> mDataResult = new HashMap<String, VideoFolder>();
			if (cursor != null) {

				int idxId = cursor.getColumnIndex(MediaStore.MediaColumns._ID);
				int idxModified = cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED);
				int indxSize = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
				int idxData = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);

				while (cursor.moveToNext()) {
					long _id = cursor.getLong(idxId);
					long modified = cursor.getLong(idxModified);
					int duration = cursor.getInt(indxSize);
					String path = cursor.getString(idxData);
					if (StringUtils.isNotEmpty(path)) {
						File file = new File(path);
						if (file != null && file.canRead()) {
							String folder = file.getParent();
							String folder2 = file.getParent() + File.separator;//原来的对比有问题,需要加上\才能匹配对比
							Logger.e("MM", "folder2=" + folder2);
							String folderKey = folder.toLowerCase(Locale.CHINESE);
							// 过滤掉临时文件夹
							if (StringUtils.isNotEmpty(folder) && (!folder.startsWith(cameraFolder) || StringUtils.equals(cameraFolder, folder))) {
								// 过滤掉临时文件夹,过滤草稿箱视频
								if (StringUtils.isNotEmpty(folder) && (!folder.startsWith(cameraFolder) && !StringUtils.equals(cameraFolder, folder2))) {
									Logger.e("samuel", "folder>>>>>" + folder + ">>>>path>>>" + path);

									boolean isMoreThan3s = duration >= 3 * 1000;

									VideoFolder iFolder;
									if (!mDataResult.containsKey(folderKey)) {
										iFolder = new VideoFolder();
										iFolder._id = _id;
										iFolder.path = folder;
										iFolder.name = FileUtils.getName(folder);
										iFolder.url = path;

										if (isMoreThan3s) {
											iFolder.video = getVideo(_id, path, modified, duration);
										}

										mDataResult.put(folderKey, iFolder);
									} else {
										iFolder = mDataResult.get(folderKey);
										if (iFolder.video == null && isMoreThan3s) {
											iFolder.video = getVideo(_id, path, modified, duration);
										}
									}

									if (iFolder != null && isMoreThan3s) {
										iFolder.count++;
									}
								}
							}
						}
					}
				}
				cursor.close();
			}

			HashSet<VideoFolder> resultSet = new HashSet<VideoFolder>(mDataResult.values());
			List<VideoFolder> result = new ArrayList<VideoFolder>(resultSet);

			Iterator<VideoFolder> it = result.iterator();
			while (it.hasNext()) {
				VideoFolder folder = it.next();
				if (folder.count == 0) {
					it.remove();
				}
			}

			// 排序
			Collections.sort(result, new Comparator<VideoFolder>() {

				@Override
				public int compare(VideoFolder lhs, VideoFolder rhs) {
					return lhs.name.compareTo(rhs.name);
				}

			});

			return result;
		}

		private Video getVideo(long id, String path, long modified, long duration) {
			MediaMetadataRetriever metadata = new MediaMetadataRetriever();
			int orientation = 0;
			if (DeviceUtils.hasJellyBeanMr1()) {
				try {
					metadata.setDataSource(path);
					orientation = ConvertToUtils.toInt(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION), 0);
				} catch (Exception e) {
					Logger.e("path = " + path);
				}
			}

			Video video = new Video(path, modified, duration);
			video._id = id;
			video.orientation = orientation;
			return video;
		}
		
		@Override
		protected void onResume() {
		  // TODO Auto-generated method stub
		  super.onResume();
		  try {
	      folderlist = (ArrayList<VideoFolder>) loadData();
	      videoFolderAdapter.updateVideoFolderData(folderlist);
      } catch (Exception e) {
	      e.printStackTrace();
      }
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.titleLeft:
				 onBackPressed();
				 break;
			}
		}
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Logger.e("MM", "onItemClick");
			final VideoFolder item = folderlist.get(position);
			if (item != null && StringUtils.isNotEmpty(item.path)) {
				Intent intent = new Intent(this, ImportVideoSelectActivity.class);
				Bundle bundle = this.getIntent().getExtras();
				if (bundle == null) {
					bundle = new Bundle();
				}
				bundle.putString("folder", item.path);
				bundle.putString("foldername", item.name);
				intent.putExtras(bundle);
				if (mFromMulti) {
					startActivityForResult(intent, REQUEST_CODE_MULTI);
				} else {
					startActivityForResult(intent, REQUEST_CODE);
				}
			}
		}

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			if (resultCode == Activity.RESULT_OK) {
				if (requestCode == REQUEST_CODE_MULTI) {
					setResult(Activity.RESULT_OK, data);
					finish();
				} else if (requestCode == REQUEST_CODE) {
					onBackPressed();
				}
			}
		}

	@Override
	public void finish() {
		super.finish();
	}
}

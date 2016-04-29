package com.yixia.camera.demo.ui.record;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lcr.mplay.R;
import com.yixia.camera.demo.common.CommonIntentExtra;
import com.yixia.camera.demo.log.Logger;
import com.yixia.camera.demo.po.POThemeSingle;
import com.yixia.camera.demo.ui.BaseActivity;
import com.yixia.camera.demo.ui.record.helper.ThemeHelper;
import com.yixia.camera.demo.ui.record.views.ThemeGroupLayout;
import com.yixia.camera.demo.ui.record.views.ThemeView;
import com.yixia.camera.demo.util.Constant;
import com.yixia.videoeditor.adapter.UtilityAdapter;
import com.yixia.weibo.sdk.FFMpegUtils;
import com.yixia.weibo.sdk.OnAssetDownloadListener;
import com.yixia.weibo.sdk.OnStoreDataLoadListener;
import com.yixia.weibo.sdk.VideoProcessEngine;
import com.yixia.weibo.sdk.model.MediaObject;
import com.yixia.weibo.sdk.model.MediaThemeObject;
import com.yixia.weibo.sdk.model.VideoEffectFuncModel;
import com.yixia.weibo.sdk.model.VideoEffectModel;
import com.yixia.weibo.sdk.model.VideoEffectModel$DownloadStatus;
import com.yixia.weibo.sdk.model.VideoEffectStoreModel;
import com.yixia.weibo.sdk.model.VideoFuncList;
import com.yixia.weibo.sdk.model.VideoMusicModel;
import com.yixia.weibo.sdk.util.ConvertToUtils;
import com.yixia.weibo.sdk.util.DeviceUtils;
import com.yixia.weibo.sdk.util.IsUtils;
import com.yixia.weibo.sdk.util.StringUtils;
import com.yixia.weibo.sdk.util.ToastUtils;

public class MediaPreviewActivity extends BaseActivity implements
		OnClickListener {

	/** 开始转码 */
	private static final int HANDLER_ENCODING_START = 100;
	/** 转码进度 */
	private static final int HANDLER_ENCODING_PROGRESS = 101;
	/** 转码结束 */
	private static final int HANDLER_ENCODING_END = 102;
	/** 无主题放的位置 */
	private final static int NO_THEME_INDEX = 0;

	private LinearLayout mLinearLayout;

	/** 需要下载或在正在下载的视频 */
	public ArrayList<ThemeView> mDownloaderViews = new ArrayList<ThemeView>();

	/** 播放按钮、主题音量按钮 */
	private ImageView mPlayStatus;
	/** 上一步、下一步 */
	private TextView mTitleLeft, mTitleNext, mVideoPreviewMusic,
			localMusicText;
	/** 主题音乐，原声音 */
	private CheckBox mThemeVolumn, mVideoVolumn;
	/** 正在加载 */
	private View mLoadingView;
	/** 主题、滤镜容器 */
	private View mThemeLayout, mFilterLayout;

	/** 主题容器 */
	private ThemeGroupLayout mThemes, mFilters;
	/** MV主题 */
	private SurfaceView mThemeSufaceView;

	/** 主题缓存的目录 */
	private File mThemeCacheDir;
	/** 当前主题 */
	private VideoEffectModel mCurrentTheme;

	/** 导出视频，导出封面 */
	private String mVideoPath;
	/** 临时合并ts流 */
	private String mVideoTempPath;
	/** 当前音乐名称 */
	private String mCurrentMusicTitle;
	/** 是否需要回复播放 */
	private boolean mNeedResume;
	/** 是否停止播放 */
	private boolean mStopPlayer;
	/** 是否正在转码 */
	private boolean mStartEncoding;
	/** 窗体宽度 */
	/** 分块边距，默认10dip */
	private int mLeftMargin;
	/** 视频时长 */
	/** 视频信息 */
	private MediaObject mMediaObject;
	private boolean isImportVideo;

	private VideoProcessEngine videoProcessEngine;

	private VideoFuncList videoFuncList;
	private TextView themeStore, musicStore;
	private LinearLayout musicListLayout, localmusicLayout;
	private Map<Integer, TextView> musicViews, localMusicViews;
	private TextView clickedMusicView, clickedLocalMusicView;
	private String clickedMusicString;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mMediaObject = (MediaObject) getIntent().getSerializableExtra(
				CommonIntentExtra.EXTRA_MEDIA_OBJECT);
		isImportVideo = getIntent().getBooleanExtra(
				CommonIntentExtra.EXTRA_MEDIA_IMPORT_VIDEO, false);
		if (mMediaObject == null) {
			Toast.makeText(this, R.string.record_read_object_faild,
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 防止锁屏

		prepareActivity();
		prepareViews();
	}

	/** 预处理参数 */
	private boolean prepareActivity() {
		// 获取传入参数
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState()) && !isExternalStorageRemovable())
			mThemeCacheDir = new File(getExternalCacheDir(), "Theme");
		else
			mThemeCacheDir = new File(getCacheDir(), "Theme");
		mLeftMargin = ConvertToUtils.dipToPX(this, 8);

		mVideoPath = mMediaObject.getOutputVideoPath();
		if (StringUtils.isNotEmpty(mVideoPath)) {
			mCoverPath = mVideoPath.replace(".mp4", ".jpg");
		}
		mVideoTempPath = getIntent().getStringExtra("output");

		return true;
	}

	/** 预处理UI相关 */
	private void prepareViews() {
		setContentView(R.layout.activity_media_preview);

		// 绑定控件
		mPlayStatus = (ImageView) findViewById(R.id.play_status);
		mThemeSufaceView = (SurfaceView) findViewById(R.id.preview_theme);
		mTitleLeft = (TextView) findViewById(R.id.titleLeft);
		mTitleNext = (TextView) findViewById(R.id.titleRight);
		mVideoPreviewMusic = (TextView) findViewById(R.id.video_preview_music);
		mThemes = (ThemeGroupLayout) findViewById(R.id.themes);
		mFilters = (ThemeGroupLayout) findViewById(R.id.filters);
		mThemeVolumn = (CheckBox) findViewById(R.id.video_preview_theme_volume);
		mVideoVolumn = (CheckBox) findViewById(R.id.video_preview_video_volume);
		mLoadingView = findViewById(R.id.loading);
		mThemeLayout = findViewById(R.id.theme_layout);
		mFilterLayout = findViewById(R.id.filter_layout);

		mLinearLayout = (LinearLayout) findViewById(R.id.download_themes);
		themeStore = (TextView) findViewById(R.id.titleText);
		themeStore.setOnClickListener(this);
		musicStore = (TextView) findViewById(R.id.titleText2);
		musicStore.setOnClickListener(this);
		localMusicText = (TextView) findViewById(R.id.localmusic);
		localMusicText.setOnClickListener(this);

		musicListLayout = (LinearLayout) findViewById(R.id.download_music);
		localmusicLayout = (LinearLayout) findViewById(R.id.local_musiclayout);

		mVideoPreviewMusic.setOnClickListener(this);
		mTitleLeft.setOnClickListener(this);
		mTitleNext.setOnClickListener(this);
		// mTitleText.setOnClickListener(this);
		// mTitleText2.setOnClickListener(this);
		mThemeSufaceView.setOnClickListener(this);
		findViewById(R.id.tab_theme).setOnClickListener(this);
		findViewById(R.id.tab_filter).setOnClickListener(this);
		mThemeVolumn.setOnClickListener(this);
		mVideoVolumn.setOnClickListener(this);

		// mTitleText.setText(R.string.record_camera_preview_title);
		mTitleNext.setText(R.string.record_camera_preview_next);
		/** 设置播放区域 */
		View preview_layout = findViewById(R.id.preview_layout);
		LinearLayout.LayoutParams mPreviewParams = (LinearLayout.LayoutParams) preview_layout
				.getLayoutParams();
		mPreviewParams.height = DeviceUtils.getScreenWidth(this);

		videoProcessEngine = VideoProcessEngine.createVideoProcessEngine(
				mVideoTempPath, mThemeSufaceView.getHolder(), this);
		videoProcessEngine.setVideoAuthor("Mr.Dracy");
		// 不添加以下两行代码，视频不会进行裁剪，使用滤镜后按原宽高输出
		videoProcessEngine.setIsImportVideo(isImportVideo);
		videoProcessEngine.setMediaObject(mMediaObject);
		loadThemes();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mThemeSufaceView != null && mNeedResume && mCurrentTheme != null) {
			restart();
		}
		mNeedResume = false;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (videoProcessEngine != null) {
			mNeedResume = true;
			releaseVideo();
		}
	}

	/**
	 * 获取音乐列表完毕后，逐个添加到显示布局中 包含了已下载主题、本地主题和在线主题
	 * 
	 * @param resultData
	 */
	private void processMusicData(final List<VideoMusicModel> resultData) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (resultData != null && resultData.size() > 0) {
					musicViews = new HashMap<Integer, TextView>();
					for (VideoMusicModel theme : resultData) {
						TextView musicView = new TextView(
								MediaPreviewActivity.this);
						musicView.setTextColor(getResources().getColor(
								R.color.white));
						if (theme.effectID == VideoEffectModel.EFFECT_ID_NONE_MUSIC) {
							musicView.setText(theme.effectNameChinese);
							android.util.Log.e("miaopai", "none music");
						} else if (theme.isDownloaded()) {
							musicView.setTextColor(getResources().getColor(
									R.color.yellow));
							musicView
									.setText(theme.effectNameChinese + "(已下载)");
						} else if (theme.isOnline()) {
							musicView.setText(theme.effectNameChinese + "(在线)");
						} else {
							musicView.setText(theme.effectNameChinese + "(本地)");
						}

						musicView
								.setOnClickListener(mMusicDownloadClickListener);
						musicView
								.setOnLongClickListener(onlineMusicLongClickListener);
						musicView.setTag(theme);
						musicViews.put(theme.effectID, musicView);
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.MATCH_PARENT,
								LinearLayout.LayoutParams.WRAP_CONTENT);// mThemeItemWH,
						// mThemeItemWH
						lp.topMargin = 20;
						if (theme.effectID == videoProcessEngine
								.getCurrentVideoMusicEffectId()) {
							musicView.setTextColor(getResources().getColor(
									R.color.green));
						}
						musicListLayout.addView(musicView, lp);
					}
				}
			}
		});
	}

	/**
	 * 显示在线主题商店
	 */
	public void showThemeStore() {
		if (mLinearLayout != null && mLinearLayout.getChildCount() > 0) {
			if (mLinearLayout.isShown()) {
				mLinearLayout.setVisibility(View.GONE);
			} else {
				mLinearLayout.setVisibility(View.VISIBLE);
			}
			return;
		}
		// 如果主题商店中没有主题，需要从网络获取在线主题数据
		videoProcessEngine.loadEffectStore(VideoEffectFuncModel.THEME_TYPE_MV,
				new com.yixia.weibo.sdk.OnStoreDataLoadListener() {

					// @Override
					// public void onCompeltedFromNet(VideoEffectStoreModel
					// resultData, boolean isSuccess, int errorCode) {
					// if (isSuccess) {
					// processData(resultData.getVideoEffectModels());
					// }
					// }
					//
					// @Override
					// public void onCompeltedFromCache(VideoEffectStoreModel
					// resultData) {
					// processData(resultData.getVideoEffectModels());
					// }

					@Override
					public void onCompeltedFromCache(Object arg0) {
						// TODO Auto-generated method stub
						VideoEffectStoreModel resultData = (VideoEffectStoreModel) arg0;
						processData(resultData.getVideoEffectModels());
					}

					@Override
					public void onCompeltedFromNet(Object arg0, boolean arg1,
							int arg2) {
						// TODO Auto-generated method stub
						if (arg1) {
							VideoEffectStoreModel resultData = (VideoEffectStoreModel) arg0;
							processData(resultData.getVideoEffectModels());
						}
					}
				});
	}

	/**
	 * 取回在线的主题商店的数据，逐个添加到相关布局中
	 * 
	 * @param resultData
	 */
	private void processData(final List<VideoEffectModel> resultData) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {

				mLinearLayout.setVisibility(View.VISIBLE);

				for (VideoEffectModel theme : resultData) {
					ThemeView themeView = new ThemeView(
							MediaPreviewActivity.this, theme);
					if (StringUtils.isNotEmpty(theme.effectIconPath)) {
						// themeView.getIcon().setImagePath(theme.effectIconPath);
						themeView.getIcon().setImageResource(R.drawable.empty);
					}

					themeView.setOnClickListener(mThemeDownloadClickListener);
					themeView.setTag(theme);
					Logger.d("videoProcessEngine.theme.previewVideoPath "
							+ theme.previewVideoPath);
					Logger.d("videoProcessEngine.theme.effectPreviewVideoIcon "
							+ theme.effectPreviewVideoIcon);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);// mThemeItemWH,
					// mThemeItemWH
					mLinearLayout.addView(themeView, lp);

				}

			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.titleText:
			showThemeStore();
			break;
		case R.id.titleText2:
			showMusicList();
			break;
		case R.id.localmusic:
			showLocalMusicList();
			break;
		case R.id.titleLeft:
			finish();
			break;
		case R.id.titleRight:
			startEncoding();
			break;
		case R.id.preview_theme:// 点击暂停视频播放
			if (isPlaying())
				stopVideo();
			else
				startVideo();
			break;
		case R.id.video_preview_theme_volume:// 静音主题音
			// 隐藏动画
			ToastUtils
					.showToastImage(
							this,
							mThemeVolumn.isChecked() ? R.drawable.priview_theme_volumn_close
									: R.drawable.priview_theme_volumn_open);
			videoProcessEngine.applyMute(mThemeVolumn.isChecked(),
					mVideoVolumn.isChecked());
			break;
		case R.id.video_preview_video_volume:// 静音原声
			ToastUtils
					.showToastImage(
							this,
							mVideoVolumn.isChecked() ? R.drawable.priview_orig_volumn_close
									: R.drawable.priview_orig_volumn_open);
			videoProcessEngine.applyMute(mThemeVolumn.isChecked(),
					mVideoVolumn.isChecked());
			break;
		case R.id.tab_theme:
			mThemeLayout.setVisibility(View.VISIBLE);
			mFilterLayout.setVisibility(View.GONE);
			break;
		case R.id.tab_filter:
			mThemeLayout.setVisibility(View.GONE);
			mFilterLayout.setVisibility(View.VISIBLE);
			break;

		case R.id.video_preview_music:// 弹出音乐列表
			break;
		}
	}

	private List<VideoMusicModel> localMusicList;

	private void showLocalMusicList() {
		if (localmusicLayout != null && localmusicLayout.getChildCount() > 0) {
			if (localmusicLayout.isShown()) {
				localmusicLayout.setVisibility(View.GONE);
			} else {
				musicListLayout.setVisibility(View.GONE);
				localmusicLayout.setVisibility(View.VISIBLE);
			}
			return;
		}
		if (videoProcessEngine != null && videoProcessEngine.isPlaying()) {
			mNeedResume = true;
			releaseVideo();
		}

		localmusicLayout.setVisibility(View.VISIBLE);

		videoProcessEngine
				.loadLocalMusic(
						"",
						new com.yixia.weibo.sdk.VideoProcessEngine$OnLocalMusicLoadListener() {

							// @Override
							// public void onSuccess(ArrayList<VideoMusicModel>
							// mlocalMusicList) {
							// Logger.e("resultData.size() " +
							// mlocalMusicList.size());
							// localMusicList = mlocalMusicList;
							// processLocalMusicData(localMusicList);
							// }

							@Override
							public void onFailed() {
								runOnUiThread(new Runnable() {

									@Override
									public void run() {
										// TODO Auto-generated method stub
										Toast.makeText(
												MediaPreviewActivity.this,
												"当前文件夹下未找到mp3音乐文件",
												Toast.LENGTH_SHORT).show();
										localmusicLayout
												.setVisibility(View.GONE);
									}
								});
							}

							@Override
							public void onSuccess(ArrayList mlocalMusicList) {
								// TODO Auto-generated method stub
								Logger.e("resultData.size() "
										+ mlocalMusicList.size());
								localMusicList = mlocalMusicList;
								processLocalMusicData(localMusicList);
							}

						});
	}

	private void processLocalMusicData(
			final List<VideoMusicModel> localMusicData) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				if (localMusicData != null && localMusicData.size() > 0) {
					localMusicViews = new HashMap<Integer, TextView>();
					for (VideoMusicModel theme : localMusicData) {
						TextView localmusicView = new TextView(
								MediaPreviewActivity.this);
						localmusicView.setTextColor(getResources().getColor(
								R.color.white));
						localmusicView.setText(theme.effectNameChinese);

						localmusicView
								.setOnClickListener(mLocalMusicClickListener);
						localmusicView.setTag(theme);
						localMusicViews.put(theme.effectID, localmusicView);
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.MATCH_PARENT,
								LinearLayout.LayoutParams.WRAP_CONTENT);// mThemeItemWH,
						// mThemeItemWH
						lp.topMargin = 20;
						localmusicLayout.addView(localmusicView, lp);
					}
				}
			}
		});
	}

	/**
	 * 显示音乐列表 根据用户权限控制是否显示在线的音乐列表
	 */
	private void showMusicList() {
		Logger.e("videoProcessEngine.getCurrentVideoMusicEffectId() "
				+ videoProcessEngine.getCurrentVideoMusicEffectId());
		Logger.e("videoProcessEngine.musicName() "
				+ videoProcessEngine.getVideoMusicModel(videoProcessEngine
						.getCurrentVideoMusicEffectId()).musicName);
		Logger.e("videoProcessEngine.musicPath() "
				+ videoProcessEngine.getVideoMusicModel(videoProcessEngine
						.getCurrentVideoMusicEffectId()).musicPath);
		// Logger.e("videoProcessEngine.getCurrentVideoMusicEffectId() " +
		// videoProcessEngine.getCurrentVideoMusicEffectId());
		// Logger.e("videoProcessEngine.getCurrentVideoMusicEffectId() " +
		// videoProcessEngine.getVideoMusicModel(videoProcessEngine.getCurrentVideoMusicEffectId()).musicName);

		// if (musicListLayout != null && musicListLayout.getChildCount() > 0) {
		// if (musicListLayout.isShown()) {
		// musicListLayout.setVisibility(View.GONE);
		// } else {
		// localmusicLayout.setVisibility(View.GONE);
		// musicListLayout.setVisibility(View.VISIBLE);
		// updateMusiclistView();
		// }
		//
		// return;
		// }
		if (musicListLayout != null) {
			musicListLayout.removeAllViews();
			if (musicListLayout.isShown()) {
				musicListLayout.setVisibility(View.GONE);
				return;
			} else {
				localmusicLayout.setVisibility(View.GONE);
				musicListLayout.setVisibility(View.VISIBLE);
				updateMusiclistView();
			}
		}

		if (videoProcessEngine != null && videoProcessEngine.isPlaying()) {
			mNeedResume = true;
			releaseVideo();
		}

		musicListLayout.setVisibility(View.VISIBLE);

		videoProcessEngine.loadMusicStore(new OnStoreDataLoadListener() {

			// @Override
			// public void onCompeltedFromCache(List<VideoMusicModel>
			// resultData) {
			//
			// }
			//
			// @Override
			// public void onCompeltedFromNet(List<VideoMusicModel> resultData,
			// boolean isSuccess, int errorCode) {
			// Logger.e("resultData.size() " + resultData.size());
			// mList = resultData;
			// processMusicData(resultData);
			//
			// }

			@Override
			public void onCompeltedFromCache(Object arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onCompeltedFromNet(Object resultData, boolean arg1,
					int arg2) {
				// TODO Auto-generated method stub
				ArrayList<VideoMusicModel> mResultData = (ArrayList<VideoMusicModel>) resultData;
				Logger.e("resultData.size() " + mResultData.size());
				mList = mResultData;
				processMusicData(mResultData);
			}
		});
	}

	/**
	 * 更新音乐列表的界面显示，当前选中音乐为彩色字体颜色，其他的为白色字体颜色
	 */
	private void updateMusiclistView() {
		if (musicViews != null && musicViews.size() > 0) {
			Set<Entry<Integer, TextView>> set = musicViews.entrySet();
			for (Entry entry : set) {
				TextView musicText = (TextView) entry.getValue();
				musicText.setTextColor(getResources().getColor(R.color.white));
			}
			TextView t = musicViews.get(videoProcessEngine
					.getCurrentVideoMusicEffectId());
			if (t != null) {
				t.setTextColor(getResources().getColor(R.color.green));
			}
		}
	}

	/** 开始转码 */
	private void startEncoding() {

		stopVideo();
		releaseVideo();

		// 更新静音
		if (mMediaObject != null && mMediaObject.mThemeObject != null) {
			mMediaObject.mThemeObject.mThemeMute = mThemeVolumn.isChecked();
			mMediaObject.mThemeObject.mOrgiMute = mVideoVolumn.isChecked();
		}
		// 检测是否需要重新编译
		mStartEncoding = true;
		showProgress("", getString(R.string.record_preview_encoding));
		videoProcessEngine
				.saveVideoToPath(mVideoPath, mOnVideoEncodingListener);

	}

	/** 加载主题 */
	private void loadThemes() {
		if (isFinishing() || mStartEncoding)
			return;

		new android.os.AsyncTask<Void, Void, File>() {

			@Override
			protected File doInBackground(Void... params) {
				videoFuncList = videoProcessEngine
						.getVideoFunctions(MediaPreviewActivity.this);
				if (videoFuncList != null) {
					mThemeCacheDir = videoProcessEngine.getThemeCacheDir();

					videoProcessEngine.init(new File(mThemeCacheDir,
							ThemeHelper.THEME_VIDEO_COMMON).getAbsolutePath(),
							1500, mMediaObject.getDuration());
					return videoProcessEngine.getThemeDefaultDir();
				}
				return null;
			}

			@Override
			protected void onPostExecute(File result) {
				super.onPostExecute(result);
				File themeDir = result;
				if (themeDir != null && !isFinishing() && videoFuncList != null
						&& videoFuncList.effectFuncModels != null
						&& videoFuncList.effectFuncModels.size() > 1) {
					/** 循环添加单个主题到主题容器中 */
					mThemes.removeAllViews();
					mFilters.removeAllViews();
					for (VideoEffectFuncModel funcModels : (ArrayList<VideoEffectFuncModel>) videoFuncList.effectFuncModels) {
						if (funcModels != null
								&& funcModels.currentEffectModels != null
								&& funcModels.currentEffectModels.size() > 0) {
							for (VideoEffectModel effect : (ArrayList<VideoEffectModel>) funcModels.currentEffectModels) {
								switch (funcModels.effectFuncTypeID) {
								case VideoEffectFuncModel.THEME_TYPE_MV:
									addThemeItem(effect, -1);
									break;
								case VideoEffectFuncModel.THEME_TYPE_FILTER:
									addThemeItem(mFilters, effect, -1);
									break;
								}
							}
						}

					}
					int defaultIndex = NO_THEME_INDEX;
					mCurrentTheme = null;
					mThemes.getChildAt(defaultIndex).performClick();// 默认选中无主题
				}
			}

		}.execute();
	}

	private void refreshThemeView(ThemeView themeView, VideoEffectModel theme) {
		themeView.setTheme(theme);
		if (StringUtils.isNotEmpty(theme.effectIconPath)) {
			if (theme.effectIconPath.startsWith("http://")
					|| theme.effectIconPath.startsWith("https://")) {
				themeView.getIcon().setImageResource(R.drawable.empty);
			} else {
				themeView.getIcon().setImagePath(theme.effectIconPath);
			}
			if (theme.isOnline() || theme.isDownloading()) {
				mDownloaderViews.add(themeView);
				themeView
						.setOnClickListener(mThemeRecommendDownloadClickListener);
			} else {
				themeView.setOnClickListener(mThemeClickListener);
				mDownloaderViews.remove(themeView);
			}

			// 刷新按钮状态
			themeView.refreshView();
		}
	}

	private ThemeView addThemeItem(ThemeGroupLayout layout,
			VideoEffectModel theme, int index) {
		ThemeView themeView = new ThemeView(MediaPreviewActivity.this, theme);
		if (StringUtils.isNotEmpty(theme.effectIconPath)) {
			if (theme.effectIconPath.startsWith("http://")
					|| theme.effectIconPath.startsWith("https://")) {
				themeView.getIcon().setImageResource(R.drawable.empty);
			} else {
				themeView.getIcon().setImagePath(theme.effectIconPath);
			}

		} else {
			themeView.getIcon().setImageResource(R.drawable.empty);
		}

		if (theme.isOnline() || theme.isDownloading()) {
			mDownloaderViews.add(themeView);
			themeView.setOnClickListener(mThemeRecommendDownloadClickListener);
		} else {
			themeView.setOnClickListener(mThemeClickListener);
		}

		// 刷新按钮状态
		themeView.refreshView();

		themeView.setTag(theme);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);// mThemeItemWH,
		// mThemeItemWH
		lp.leftMargin = mLeftMargin;
		if (index == -1)
			layout.addView(themeView, lp);
		else
			layout.addView(themeView, index, lp);
		return themeView;
	}

	/** 添加当个主题到UI上 */
	private ThemeView addThemeItem(VideoEffectModel theme, int index) {
		return addThemeItem(mThemes, theme, index);
	}

	/** 重新播放 */
	private synchronized void restart() {
		mStopPlayer = false;
		mHandler.removeMessages(UtilityAdapter.NOTIFYVALUE_PLAYFINISH);
		mHandler.sendEmptyMessageDelayed(UtilityAdapter.NOTIFYVALUE_PLAYFINISH,
				100);
	}

	private void releaseVideo() {
		videoProcessEngine.release();
		mPlayStatus.setVisibility(View.GONE);
	}

	/** 开始播放 */
	private void startVideo() {
		mStopPlayer = false;
		// mThemeSufaceView.start();
		videoProcessEngine.resume();
		mPlayStatus.setVisibility(View.GONE);
	}

	/** 暂停播放 */
	private void stopVideo() {
		mStopPlayer = true;
		// mThemeSufaceView.pause();
		videoProcessEngine.pause();
		mPlayStatus.setVisibility(View.VISIBLE);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_ENCODING_START:
				if (!isFinishing()) {
					showProgress("",
							getString(R.string.record_preview_encoding));
					// WindowManager.LayoutParams lp =
					// dialog.getWindow().getAttributes();
					// lp.y = -ConvertToUtils.dipToPX(MediaPreviewActivity.this,
					// 49 + 30);
					// dialog.getWindow().setAttributes(lp);
					// showProgressLayout(false, false,
					// getString(R.string.progressbar_message_preview_making));
					// releaseVideo();
					// mThemeSufaceView.startEncoding();
					videoProcessEngine.saveVideoToPath(mVideoPath,
							mOnVideoEncodingListener);
					sendEmptyMessage(HANDLER_ENCODING_PROGRESS);
				}
				break;
			case HANDLER_ENCODING_PROGRESS:// 读取进度
				int progress = UtilityAdapter
						.FilterParserInfo(UtilityAdapter.FILTERINFO_PROGRESS);
				mOnVideoEncodingListener.onProgressChanged(progress);
				if (progress == -1) {
					mOnVideoEncodingListener.onFailed(progress);
				} else if (progress < 100) {
					sendEmptyMessageDelayed(HANDLER_ENCODING_PROGRESS, 200);
				} else {
					mOnVideoEncodingListener.onSuccess();
				}
				break;
			case HANDLER_ENCODING_END:

				break;
			case UtilityAdapter.NOTIFYVALUE_BUFFEREMPTY:
				showLoading();
				break;
			case UtilityAdapter.NOTIFYVALUE_BUFFERFULL:
				hideLoading();
				break;
			case UtilityAdapter.NOTIFYVALUE_PLAYFINISH:
				/** 播放完成时报告 */
				if (!isFinishing() && !mStopPlayer) {
					showLoading();
					videoProcessEngine.restart();
					mPlayStatus.setVisibility(View.GONE);
				}
				break;
			case UtilityAdapter.NOTIFYVALUE_HAVEERROR:
				/** 无法播放时报告 */
				if (!isFinishing()) {
					Toast.makeText(MediaPreviewActivity.this,
							R.string.record_preview_theme_load_faild,
							Toast.LENGTH_SHORT).show();
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	/** 显示加载中 */
	private void showLoading() {
		// showProgress("", getString(R.string.record_preview_building));
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mLoadingView != null)
					mLoadingView.setVisibility(View.VISIBLE);

			}
		});

	}

	/** 隐藏加载中 */
	private void hideLoading() {
		if (mLoadingView != null)
			mLoadingView.setVisibility(View.GONE);
	}

	/** 是否正在播放 */
	private boolean isPlaying() {
		return videoProcessEngine.isPlaying();
	}

	/** 响应主题点击事件 */
	private OnClickListener mThemeClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			VideoEffectModel theme = (VideoEffectModel) v.getTag();
			processTheme(theme, v);
		}
	};

	private void processTheme(VideoEffectModel theme, View v) {

		Toast.makeText(this, theme.toString(), 0).show();
		if (theme == null || mMediaObject == null)
			return;

		if (theme.effectID == VideoMusicModel.EFFECT_ID_STORE) {
			showThemeStore();
			return;
		}

		if (theme.effectID == VideoEffectModel.EFFECT_ID_MIAOPAI) {
			Intent i = new Intent(Intent.ACTION_VIEW);

			i.setData(Uri.parse("miaopai://square.app/start"));

			startActivity(i);
			return;
		}
		// if (theme.effectID == VideoMusicModel.EFFECT_ID_NONE){
		//
		// }

		if (mCurrentTheme == null
				|| !IsUtils.equals(mCurrentTheme.effectName, theme.effectName)) {
			String key = theme.effectName;
			mThemes.mObservable.notifyObservers(new String[] { key,
					String.valueOf(theme.isMV()) });

			((ThemeGroupLayout) v.getParent()).mObservable
					.notifyObservers(new String[] { key,
							String.valueOf(theme.isMV()) });

			mCurrentTheme = theme;

			if (mMediaObject.mThemeObject == null)
				mMediaObject.mThemeObject = new MediaThemeObject();

			mStopPlayer = false;
			showLoading();
			mPlayStatus.setVisibility(View.GONE);

			// videoProcessEngine.applyEffect(theme, mOnVideoPlayListener);
			videoProcessEngine.applyEffect(theme.effectType, theme.effectID,
					mOnVideoPlayListener);
			if (theme.isMV()) {
				mCurrentMusicTitle = mCurrentTheme.musicTitle;
				updateMusicTextView();

				// 清空静音状态
				if (theme.effectName.contains("Empty")
						|| theme.effectName.contains("Filter")) {
					mThemeVolumn.setChecked(true);
					mVideoVolumn.setChecked(false);
				} else {
					mThemeVolumn.setChecked(false);
					mVideoVolumn.setChecked(true);
				}
				videoProcessEngine.applyMute(mThemeVolumn.isChecked(),
						mVideoVolumn.isChecked());
				// 清除滤镜的选中状态
				// if (mFilters != null) {
				// mFilters.mObservable.notifyObservers(POThemeSingle.THEME_EMPTY);
				// }
			}
			// 滤镜 去掉滤镜附加概念，等于一个主题，可以附加音乐
			if (theme.isFilter()) {
				mCurrentMusicTitle = mCurrentTheme.musicTitle;
				updateMusicTextView();

				// 清空静音状态
				// mThemeVolumn.setChecked(false);
				if (mThemes != null) {
					mThemes.mObservable.notifyObservers(new String[] {
							POThemeSingle.THEME_EMPTY, "true" });
				}
			}
			Logger.e("theme.musicName " + theme.musicName);
			Logger.e("theme.effectID " + theme.effectID);
		}
	}

	/** 响应主题点击事件 */
	private OnClickListener mThemeRecommendDownloadClickListener = new OnClickListener() {

		@Override
		public void onClick(final View v) {
			final VideoEffectModel theme = (VideoEffectModel) v.getTag();
			if (theme == null)
				return;
			if (theme.downloadStatus == com.yixia.weibo.sdk.model.VideoEffectModel$DownloadStatus.DOWNLOADING
					|| theme.downloadStatus == com.yixia.weibo.sdk.model.VideoEffectModel$DownloadStatus.DOWNLOADED) {
				processTheme(theme, v);
				return;
			}

			videoProcessEngine.downloadEffectAsset(
					VideoEffectFuncModel.THEME_TYPE_RECOMMEND_MV,
					theme.effectID, new OnAssetDownloadListener() {

						@Override
						public void onSuccess() {
						}

						@Override
						public void onProgressChanged(int progress) {
							theme.downloadProgress = progress;
							theme.downloadStatus = com.yixia.weibo.sdk.model.VideoEffectModel$DownloadStatus.DOWNLOADING;
							runOnUiThread(new Runnable() {
								public void run() {
									((ThemeView) v).refreshView();
								}
							});
						}

						@Override
						public void onFailed(int errorCode) {

							if (MediaPreviewActivity.this != null) {
								ToastUtils.showToast(MediaPreviewActivity.this,
										R.string.download_error + " errorCode");
							}

						}

						@Override
						public void onSuccess(
								final VideoEffectModel videoEffectModel) {
							Logger.e("videoProcessEngine videoEffectModel.musicName "
									+ videoEffectModel.musicName);
							theme.downloadStatus = com.yixia.weibo.sdk.model.VideoEffectModel$DownloadStatus.DOWNLOADED;
							runOnUiThread(new Runnable() {
								public void run() {
									ToastUtils.showToast(v.getContext(), "下载成功");
									// addThemeItem(videoEffectModel, -1);
									// addThemeItem(videoProcessEngine.addThemeToList(theme),
									// -1);

									theme.copyTheme(videoEffectModel);
									refreshThemeView((ThemeView) v, theme);
								}
							});

						}
					});

		}
	};

	private List<VideoMusicModel> mList;

	/** 更新音乐名称 */
	private void updateMusicTextView() {
		if (StringUtils.isEmpty(mCurrentMusicTitle)) {
			mVideoPreviewMusic.setText(R.string.record_preview_music_nothing);
			mThemeVolumn.setVisibility(View.GONE);
		} else {
			mVideoPreviewMusic.setText(mCurrentMusicTitle);
			mThemeVolumn.setVisibility(View.VISIBLE);
		}
	}

	public static boolean isExternalStorageRemovable() {
		if (DeviceUtils.hasGingerbread())
			return Environment.isExternalStorageRemovable();
		else
			return Environment.MEDIA_REMOVED.equals(Environment
					.getExternalStorageState());
	}

	private com.yixia.weibo.sdk.VideoProcessEngine$OnVideoEncodingListener mOnVideoEncodingListener = new com.yixia.weibo.sdk.VideoProcessEngine$OnVideoEncodingListener() {

		@Override
		public void onSuccess() {
			// 获取视频真实时长
			new CaptureThumbnailsTask().execute();
			videoProcessEngine.release();
		}

		@Override
		public void onProgressChanged(int progress) {
			if (mProgressDialog != null) {
				mProgressDialog.setMessage(getString(
						R.string.record_preview_encoding_format, progress));
			}
		}

		@Override
		public void onFailed(int errorCode) {
			hideProgress();
			mStartEncoding = false;
		}
	};

	/**
	 * 生成视频截图
	 * 
	 * @author lisz
	 *
	 */
	private String mCoverPath;

	public class CaptureThumbnailsTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			Boolean result = FFMpegUtils.captureThumbnails(mVideoPath,
					mCoverPath, String.format("%dx%d", 480, 480));
			// 检查图片大小和视频文件大小
			File videoFile = new File(mVideoPath);
			if (videoFile == null || !videoFile.exists()
					|| videoFile.length() < 50 * 1024) {
				result = false;
			}
			return result;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			hideProgress();
			mStartEncoding = false;
			startActivity(new Intent(MediaPreviewActivity.this,
					VideoPlayerActivity.class).putExtra(
					Constant.RECORD_VIDEO_PATH, mVideoPath).putExtra(
					Constant.RECORD_VIDEO_CAPTURE, mCoverPath));
			finish();
		}
	}

	/**
	 * 应用音乐主题回调
	 */
	private com.yixia.weibo.sdk.VideoProcessEngine$OnMusicApplyListener mOnMusicApplyListener = new com.yixia.weibo.sdk.VideoProcessEngine$OnMusicApplyListener() {

		@Override
		public void onSuccess(String currentMusicName) {
			mCurrentMusicTitle = currentMusicName;
			updateMusicTextView();
			musicListLayout.setVisibility(View.GONE);
		}

		@Override
		public void downloadAssetsProcess(final int progress) {
			runOnUiThread(new Runnable() {
				public void run() {
					clickedMusicView.setText(clickedMusicString + "---"
							+ progress + "%");
				}
			});
		}

		@Override
		public void downloadAssetsSuccess(
				final VideoEffectModel videoEffectModel) {
			runOnUiThread(new Runnable() {
				public void run() {
					ToastUtils
							.showToast(clickedMusicView.getContext(), "下载成功!");
					// addThemeItem(videoEffectModel, -1);
					clickedMusicView.setTextColor(getResources().getColor(
							R.color.yellow));
					clickedMusicView.setText(clickedMusicString + "(已下载)");
					clickedMusicView.setTag(videoEffectModel);

					clickedMusicView.performClick();
				}
			});
		}

		@Override
		public void onFailed(int errorCode) {
			if (MediaPreviewActivity.this != null) {
				ToastUtils.showToast(MediaPreviewActivity.this,
						R.string.download_error + " errorCode");
			}
		}

	};

	private com.yixia.weibo.sdk.VideoProcessEngine$OnVideoPlayListener mOnVideoPlayListener = new com.yixia.weibo.sdk.VideoProcessEngine$OnVideoPlayListener() {

		@Override
		public void onError(int errorCode) {
			mHandler.sendEmptyMessage(UtilityAdapter.NOTIFYVALUE_HAVEERROR);
		}

		@Override
		public void onBufferStart() {
			mHandler.sendEmptyMessage(UtilityAdapter.NOTIFYVALUE_BUFFEREMPTY);
		}

		@Override
		public void onBufferEnd() {
			mHandler.sendEmptyMessage(UtilityAdapter.NOTIFYVALUE_BUFFERFULL);
		}

		@Override
		public void onCompletion() {
			mHandler.sendEmptyMessage(UtilityAdapter.NOTIFYVALUE_PLAYFINISH);
		}
	};

	/** 在线主题点击事件 */
	private OnClickListener mThemeDownloadClickListener = new OnClickListener() {

		@Override
		public void onClick(final View v) {
			final VideoEffectModel theme = (VideoEffectModel) v.getTag();

			if (theme == null) {
				return;
			}

			if (videoFuncList != null && videoFuncList.effectFuncModels != null
					&& videoFuncList.effectFuncModels.size() > 0) {
				for (VideoEffectFuncModel videoEffectFuncModel : (ArrayList<VideoEffectFuncModel>) videoFuncList.effectFuncModels) {
					if (videoEffectFuncModel != null
							&& videoEffectFuncModel.currentEffectModels != null
							&& videoEffectFuncModel.currentEffectModels.size() > 0) {
						for (VideoEffectModel effect : (ArrayList<VideoEffectModel>) videoEffectFuncModel.currentEffectModels) {
							if (effect.effectName
									.equalsIgnoreCase(theme.effectName)) {
								ToastUtils.showToast(MediaPreviewActivity.this,
										"主题已存在");
								return;
							}
						}
					}
				}
			}

			if (videoProcessEngine.isDownloadedTheme(theme)) {
				// processTheme(theme);
				addThemeItem(videoProcessEngine.addThemeToList(theme), -1);
				mLinearLayout.setVisibility(View.GONE);
				return;
			}

			videoProcessEngine.downloadEffectAsset(
					VideoEffectFuncModel.THEME_TYPE_MV, theme.effectID,
					new OnAssetDownloadListener() {

						@Override
						public void onSuccess() {
						}

						@Override
						public void onProgressChanged(int progress) {
							theme.downloadProgress = progress;
							theme.downloadStatus = VideoEffectModel$DownloadStatus.DOWNLOADING;
						}

						@Override
						public void onFailed(int errorCode) {

							if (MediaPreviewActivity.this != null) {
								ToastUtils.showToast(MediaPreviewActivity.this,
										R.string.download_error + " errorCode");
							}
						}

						@Override
						public void onSuccess(
								final VideoEffectModel videoEffectModel) {
							runOnUiThread(new Runnable() {
								public void run() {
									ToastUtils.showToast(v.getContext(), "下载成功");
									// addThemeItem(videoEffectModel, -1);
									addThemeItem(videoProcessEngine
											.addThemeToList(videoEffectModel),
											mThemes.getChildCount() - 1);
								}
							});

						}
					});

		}
	};

	/**
	 * 音乐列表的点击事件 包含了本地音乐和在线音乐的处理
	 */
	private OnClickListener mMusicDownloadClickListener = new OnClickListener() {

		@Override
		public void onClick(final View v) {
			clickedMusicView = (TextView) v;
			final VideoMusicModel theme = (VideoMusicModel) v.getTag();
			clickedMusicString = ((TextView) v).getText().toString();
			if (theme == null)
				return;
			Logger.e("theme.getGroup() " + theme.getGroup());

			mPlayStatus.setVisibility(View.GONE);

			videoProcessEngine.applyMusic(theme, mOnVideoPlayListener,
					mOnMusicApplyListener);
			mThemeVolumn.setChecked(false);
			Logger.e("[mMusicDownloadClickListener]videoProcessEngine.getCurrentVideoMusicEffectId() "
					+ videoProcessEngine.getCurrentVideoMusicEffectId());
			Logger.e("[mMusicDownloadClickListener]videoProcessEngine.getCurrentVideoMusicEffectId() "
					+ videoProcessEngine.getVideoMusicModel(videoProcessEngine
							.getCurrentVideoMusicEffectId()).musicName);
		}
	};

	/**
	 * 在线音乐列表的长按事件
	 */
	private OnLongClickListener onlineMusicLongClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {

			clickedMusicView = (TextView) v;
			final VideoMusicModel theme = (VideoMusicModel) v.getTag();
			clickedMusicString = ((TextView) v).getText().toString();
			if (theme == null)
				return false;
			// 未转码
			new AlertDialog.Builder(MediaPreviewActivity.this)
					.setTitle(R.string.hint)
					.setMessage(R.string.delete_onlinemusic_message)
					.setNegativeButton(
							R.string.record_camera_cancel_dialog_yes,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									if (theme.effectID == videoProcessEngine
											.getCurrentVideoMusicEffectId()) {
										Toast.makeText(
												MediaPreviewActivity.this,
												"不能删除正在使用的音乐",
												Toast.LENGTH_SHORT).show();
										return;
									}
									final boolean result = videoProcessEngine
											.deleteOnlineMusic(theme);
									runOnUiThread(new Runnable() {

										@Override
										public void run() {
											if (result) {
												musicListLayout
														.setVisibility(View.GONE);
												Toast.makeText(
														MediaPreviewActivity.this,
														"删除成功",
														Toast.LENGTH_SHORT)
														.show();
											} else {
												Toast.makeText(
														MediaPreviewActivity.this,
														"删除失败",
														Toast.LENGTH_SHORT)
														.show();

											}
										}
									});
								}

							})
					.setPositiveButton(R.string.record_camera_cancel_dialog_no,
							null).setCancelable(false).show();
			return true;
		}
	};

	private OnClickListener mLocalMusicClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			clickedLocalMusicView = (TextView) v;
			final VideoMusicModel theme = (VideoMusicModel) v.getTag();
			clickedMusicString = ((TextView) v).getText().toString();
			if (theme == null)
				return;
			mCurrentMusicTitle = clickedMusicString;
			updateMusicTextView();
			Logger.e("theme.getMusicName() " + theme.musicName);

			mPlayStatus.setVisibility(View.GONE);

			videoProcessEngine.applyMusic(theme, mOnVideoPlayListener);
			mThemeVolumn.setChecked(false);

			// mCurrentTheme.setMusicName(theme.musicName);
			// mCurrentTheme.setMusicPah(theme.musicPath);
			// mCurrentTheme.setMusicTitle(theme.musicTitle);
			// mPlayStatus.setVisibility(View.GONE);
			// Logger.d("mCurrentTheme.effectType",
			// ""+mCurrentTheme.effectType);
			// Logger.d("mCurrentTheme.effectID", ""+mCurrentTheme.effectID);
			// videoProcessEngine.applyEffect(mCurrentTheme.effectType,
			// mCurrentTheme.effectID, mOnVideoPlayListener);
		}
	};
}

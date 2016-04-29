package com.yixia.camera.demo.ui.record;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lcr.mplay.R;
import com.yixia.camera.demo.VCameraDemoApplication;
import com.yixia.camera.demo.common.CommonIntentExtra;
import com.yixia.camera.demo.log.Logger;
import com.yixia.camera.demo.os.ThreadTask;
import com.yixia.camera.demo.ui.BaseActivity;
import com.yixia.camera.demo.ui.record.helper.RecorderHelper;
import com.yixia.camera.demo.ui.record.views.TextureVideoView.OnPlayStateListener;
import com.yixia.camera.demo.ui.record.views.VideoSelectionView;
import com.yixia.camera.demo.ui.record.views.VideoSelectionView.OnBackgroundColorListener;
import com.yixia.camera.demo.ui.record.views.VideoSelectionView.OnSeekBarChangeListener;
import com.yixia.camera.demo.ui.record.views.VideoSelectionView.OnSwich60sListener;
import com.yixia.camera.demo.ui.record.views.VideoSelectionView.OnVideoChangeScaleTypeListener;
import com.yixia.camera.demo.ui.record.views.VideoViewTouch;
import com.yixia.camera.demo.ui.widget.ProgressWheel;
import com.yixia.camera.demo.util.MediaUtils;
import com.yixia.videoeditor.adapter.UtilityAdapter;
import com.yixia.weibo.sdk.FFMpegUtils;
import com.yixia.weibo.sdk.VCamera;
import com.yixia.weibo.sdk.api.HttpRequest;
import com.yixia.weibo.sdk.model.MediaObject;
import com.yixia.weibo.sdk.model.MediaObject.*;
import com.yixia.weibo.sdk.util.ConvertToUtils;
import com.yixia.weibo.sdk.util.DeviceUtils;
import com.yixia.weibo.sdk.util.FileUtils;
import com.yixia.weibo.sdk.util.StringUtils;
import com.yixia.weibo.sdk.util.ToastUtils;

public class ImportVideoActivity extends BaseActivity implements OnPreparedListener, OnPlayStateListener, OnInfoListener, OnVideoSizeChangedListener, OnErrorListener, OnSeekCompleteListener, OnClickListener, OnSeekBarChangeListener, OnSwich60sListener, OnBackgroundColorListener, OnVideoChangeScaleTypeListener {

	/** 显示正在加载 */
	private View mVideoLoading;
	/** 播放控件 */
	private VideoViewTouch mVideoView;
	/** 显示播放 */
	private ImageView mPlayController;
	/** 操作提示 */
	private ImageView mTipsMove;
	/** 视频区域选择 */
	private VideoSelectionView mVideoSelection;
	/** 操作提示文字 */
	private View mTipMoveText;
	/** 首次进入页面提示文字 */
	private TextView mTipsSelect;
	private LinearLayout mPreviewLinearLayout;
	protected TextView titleText, titleRightTextView;
	protected TextView titleLeft, titleRight;
	/** 屏幕的宽度 */
	private int mWindowWidth;
	/** 播放路径 */
	private String mSourcePath;
	protected ProgressWheel mProgressWheel;
	/** 中间画面拖动提示 */
	//	private boolean mAreaTips;
	private static final String DESKTOP_USERAGENT = "Mozilla/5.0 (X11; " + "Linux x86_64) AppleWebKit/534.24 (KHTML, like Gecko) " + "Chrome/11.0.696.34 Safari/534.24";
	private boolean mIsFitCenter, mIsWhiteBackground;
	protected MediaObject mMediaObject;
	/** 视频旋转角度 */
	private int mVideoRotation = 0;
	/** 视频临时目录 */
	private String mTargetPath;
	/** 预先裁剪是否完成 */
	private boolean mTempVideoTranscodeFinishd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_import_video);
		initIntent();
		if (mMediaObject == null) {
			String dirName = System.currentTimeMillis() + "";
			String directory = VCamera.getVideoCachePath();
			if (StringUtils.isNotEmpty(mTargetPath)) {
				File f = new File(mTargetPath);
				if (!f.exists()) {
					f.mkdirs();
				}
				dirName = f.getName();
				directory = f.getParent() + "/";
			}
			mTargetPath = directory + dirName;
			mMediaObject = new MediaObject(directory, dirName, RecorderHelper.getVideoBitrate(), MediaObject.MEDIA_PART_TYPE_IMPORT_VIDEO);
		}
		initView();
	}

	private void initIntent() {
		mVideoRotation = getIntent().getIntExtra("orientation", 0);
		/** 存储 */
		mTargetPath = getIntent().getStringExtra("target");
	}

	private void initView() {

		titleLeft = (TextView) findViewById(R.id.titleLeft);
		titleText = (TextView) findViewById(R.id.titleText);
		titleRightTextView = (TextView) findViewById(R.id.titleRightTextView);

		mVideoLoading = findViewById(R.id.video_loading);
		mVideoView = (VideoViewTouch) findViewById(R.id.preview);
		mPreviewLinearLayout = (LinearLayout) mVideoView.getParent();
		mPlayController = (ImageView) findViewById(R.id.play_controller);
		mVideoSelection = (VideoSelectionView) findViewById(R.id.video_selection_view);
		mTipsMove = (ImageView) findViewById(R.id.tips_move);
		mTipMoveText = findViewById(R.id.tips_move_text);
		mTipsSelect = (TextView) findViewById(R.id.tip_import_video_select);

		//		if (PreferenceUtils.getBoolean(PreferenceKeys.IMPORT_VIDEO_TIPS, true)) {
		//			showFirstTips();
		//		}

		// mChangeVideoviewMode = (CheckBox)
		// findViewById(R.id.change_videoview_mode);
		// mChangeVideoviewMode.setOnCheckedChangeListener(this);

		// 设置60s权限
		//		 mVideoSelection.setCan60s(RecorderHelper.getMaxDuration() > 10 * 1000);

		mVideoView.setOnPreparedListener(this);
		// mPlayController.setOnClickListener(this);
		mVideoView.setOnPlayStateListener(this);
		mVideoView.setOnTouchEventListener(mOnVideoTouchListener);
		mVideoView.setOnInfoListener(this);
		mVideoView.setOnVideoSizeChangedListener(this);
		mVideoView.setOnErrorListener(this);
		mVideoView.setOnSeekCompleteListener(this);
		titleLeft.setOnClickListener(this);

		titleRightTextView.setOnClickListener(this);
		titleRightTextView.setText(R.string.nexttip);
		titleRightTextView.setCompoundDrawables(null, null, null, null);

		mVideoSelection.setOnSeekBarChangeListener(this);
		mVideoSelection.setOnSwich60sListener(this);
		mVideoSelection.setOnBackgroundColorListener(this);
		mVideoSelection.setOnVideoChangeScaleTypeListener(this);

		initSurfaceView();

		titleText.setText(R.string.record_camera_import_title6);
		//		if (mFromMulti) {
		//			titleRight.setText(R.string.record_camera_preview_next);
		//		} else {
		// titleRight.setText(R.string.record_camera_preview_title);
		//		}

		parseIntentUrl(getIntent());
	}

	private void initSurfaceView() {
		int w = DeviceUtils.getScreenWidth(this);// 屏幕宽度

		// 宽高一致
		View preview_layout = findViewById(R.id.preview_layout);
		LinearLayout.LayoutParams mParams = (LinearLayout.LayoutParams) preview_layout.getLayoutParams();
		mParams.height = w;
		preview_layout.setVisibility(View.VISIBLE);

		View cropView = findViewById(R.id.cropView);
		int cropHeight = (int) (mWindowWidth * 1.0f * 9 / 16);
		int topMargin = ConvertToUtils.dipToPX(this, 49) + (mWindowWidth - cropHeight) / 2;
		RelativeLayout.LayoutParams cropViewParam = (RelativeLayout.LayoutParams) cropView.getLayoutParams();
		cropViewParam.width = mWindowWidth;
		cropViewParam.height = cropHeight;
		cropViewParam.topMargin = topMargin;
		cropView.setLayoutParams(cropViewParam);
	}

	/**
	 * 解析url
	 * 
	 * @param intent
	 * @return -1 解析失败 0 正在解析 1解析成功
	 */
	private void parseIntentUrl(Intent intent) {
		if (intent != null) {
			try {
				mSourcePath = intent.getStringExtra("source");
				if (StringUtils.isEmpty(mSourcePath)) {
					Uri uri = intent.getData();
					if (uri == null) {
						Bundle b = intent.getExtras();
						Object o = b.get(Intent.EXTRA_STREAM);
						uri = Uri.parse(o.toString());
					}
					if (uri != null) {
						if (uri.getScheme().startsWith("file")) {
							mSourcePath = uri.toString();
						} else {
							ContentResolver contentResolver = getContentResolver();
							Cursor cursor = contentResolver.query(uri, null, null, null, null);
							cursor.moveToFirst();
							if (cursor != null) {
								cursor.moveToFirst();
								int index2 = cursor.getColumnIndex("mime_type");
								String type = cursor.getString(index2);
								if (type != null && type.indexOf("video") != -1) {

								} else {
									return;
								}
								int index = cursor.getColumnIndex("_data");
								if (index > -1) {
									if (cursor.getString(index) != null) {
										mSourcePath = cursor.getString(index);
									}
								} else {

								}
								cursor.close();
							}
						}
					}
				}
			} catch (Exception e) {
			}
		}
		// 本地地址检测是否存在
		if (StringUtils.isEmpty(mSourcePath) || (MediaUtils.isNative(mSourcePath) && !new File(mSourcePath).exists())) {
			ToastUtils.showToast(ImportVideoActivity.this, R.string.record_camera_import_video_exists);
			finish();
		} else {
			if (mSourcePath.toLowerCase().endsWith(".gif")) {
				importGif(mSourcePath);
			} else if (getIntent().getBooleanExtra("parse", false)) {
				parseShortVideo(mSourcePath);
			} else {
				mVideoView.setVideoPath(mSourcePath);
			}
		}
	}

	/** 解析短视频 */
	private void parseShortVideo(final String url) {
		if (StringUtils.isNotEmpty(url)) {
			new ThreadTask<Void, Void, String>() {

				@Override
				protected void onPreExecute() {
					super.onPreExecute();
					mVideoLoading.setVisibility(View.GONE);
					ProgressDialog dialog = showEncodingDialog(getString(R.string.video_layout_loading));
					if (dialog != null) {
						if (mProgressWheel != null) {
							mProgressWheel.spin();
						}
						dialog.setCancelable(true);
						dialog.setOnCancelListener(new OnCancelListener() {

							@Override
							public void onCancel(DialogInterface dialog) {
								ToastUtils.showToast(ImportVideoActivity.this, R.string.record_camera_import_video_faild);
								finish();
							}
						});
					}
				}

				@Override
				protected String doInBackground(Void... params) {
					// 导入美拍视频
					if (url.indexOf("meipai.com/media/") > -1) {
						String html = getRequestString(url);
						if (StringUtils.isNotEmpty(html)) {
							String video = StringUtils.substring(html, "data-video=\"", "\"");
							if (StringUtils.isNotEmpty(video) && video.endsWith(".mp4")) {
								return video;
							}
						}
					}

					return null;
				}

				@Override
				protected void onPostExecute(String result) {
					super.onPostExecute(result);
					if (mProgressWheel != null) {
						mProgressWheel.stopSpinning();
					}
					hideProgress();
					if (!isFinishing() && StringUtils.isNotEmpty(result)) {
						mVideoLoading.setVisibility(View.VISIBLE);
						mSourcePath = result;
						mVideoView.setVideoPath(mSourcePath);
					} else {
						ToastUtils.showToast(ImportVideoActivity.this, R.string.record_camera_import_video_faild);
						finish();
					}
				}

			}.execute();
		}
	}

	public static String getRequestString(String url) {
		try {
			HttpRequest request = HttpRequest.get(url).acceptGzipEncoding().uncompress(true).trustAllCerts().trustAllHosts().readTimeout(10000);
			request.userAgent(DESKTOP_USERAGENT);
			//uncompress(true). acceptGzipEncoding().userAgent(USER_AGENT_IPAD).
			return request.body();
		} catch (Exception ex) {
			Logger.e(ex);
		} catch (OutOfMemoryError ex) {
			Logger.e(ex);
		}
		return "";
	}

	/** 导入Gif转换 */
	private void importGif(final String url) {
		if (StringUtils.isNotEmpty(url)) {
			new ThreadTask<Void, Void, File>() {

				@Override
				protected void onPreExecute() {
					super.onPreExecute();
					mVideoLoading.setVisibility(View.GONE);
					ProgressDialog dialog = showEncodingDialog(getString(R.string.record_camera_import_type_convert));
					if (dialog != null) {
						if (mProgressWheel != null) {
							mProgressWheel.spin();
						}
						dialog.setCancelable(true);
						dialog.setOnCancelListener(new OnCancelListener() {

							@Override
							public void onCancel(DialogInterface dialog) {
								ToastUtils.showToast(ImportVideoActivity.this, R.string.record_camera_import_gif_faild);
								finish();
							}
						});
					}
				}

				@Override
				protected File doInBackground(Void... params) {
					File mThumbCacheDir = VCameraDemoApplication.getGifCacheDirectory();
					String key = Crypto.md5(url);
					final File thumbDir = new File(mThumbCacheDir, key);
					if (!thumbDir.exists())
						thumbDir.mkdirs();
					final File thumbFile = new File(thumbDir, "0.mp4");

					// 在线的先下载回来
					String gifFile = url;
					if (url.startsWith("http://") || url.startsWith("https://") || !MediaUtils.isNative(url)) {
						// 在线的gif应该每次都重新下载
						gifFile = FileUtils.concatPath(thumbDir.getPath(), "0.gif");
						FileUtils.deleteFile(gifFile);
						FileUtils.deleteFile(thumbFile);
						if (!FileUtils.checkFile(gifFile)) {
							HttpRequest request = HttpRequest.get(url);
							try {
								request.receive(new File(gifFile), null);
								if (request.ok()) {
									// gif图入口
									//									if (!isFinishing()) {
									//										UploaderHelper.saveImageStone(ImportVideoActivity.this, gifFile);
									//									}
								}
							} catch (Exception e) {
								Logger.e(e);
							}
							if (!FileUtils.checkFile(gifFile)) {
								gifFile = url;
							}
						}
					} else {
						// 本地的使用缓存
						if (thumbFile.exists() && thumbFile.canRead() && thumbFile.length() > 0) {
							return thumbFile;
						}
					}

					// 检测FPS
					float fps = 25;// (float) FFMpegUtils.getVideoInfo(gifFile,
					// "fps", 25.0f);
					// int duration1 = FFMpegUtils.getVideoDuration(gifFile, 0);
					// if (fps > 30.0f)
					// fps = 30.0f;
					String target = FileUtils.concatPath(thumbDir.getPath(), "temp.ts");
					String cmd = String.format("ffmpeg %s -i \"%s\" -vf \"crop=in_w-mod(in_w\\,16):in_h-mod(in_h\\,16):0:0\" -r %.2f %s -b:v 4m -f mpegts \"%s\"", FFMpegUtils.getLogCommand(), gifFile, fps, FFMpegUtils.getVCodecCommand(), target);
					Logger.e("[ImportVideoActivity]gif-ts:" + cmd);
					// String cmd =
					// String.format("ffmpeg %s -i \"%s\" -vf \"crop=in_w-mod(in_w\\,16):in_h-mod(in_h\\,16):0:0\" %s -b:v 2m -f mpegts \"%s\"",
					// FFMpegUtils.getLogCommand(), url,
					// FFMpegUtils.getVCodecCommand(), target);
					if (UtilityAdapter.FFmpegRun("", cmd) == 0) {
						// 检测Gif是否大于3秒
						// {"ver":1,"format":"mov,mp4,m4a,3gp,3g2,mj2","file":"/Users/barry/Downloads/opengl.mp4","duration":2921.89,"starttime":0.00,"bitrate":10755,"vcodec":"h264","vprofile":"Main","vcodectag":"avc1","pixfmt":"yuv420p","colorspace":"bt709","width":1280,"height":720,"fps":29.94,"acodec":"aac","acodectag":"mp4a","samplerate":44100,"channels":2,"samplefmt":8,"vstreamcnt":1,"astreamcnt":1,"streamcnt":2}
						int duration = FFMpegUtils.getVideoDuration(target, 0);
						if (duration > 0) {
							String copy;
							if (duration < 3000) {// 不足3秒复制N份，循环播放，保证每一个gif都可以导入
								int count = 3000 / duration;
								copy = "concat:" + target;
								for (int i = 0; i < count; i++) {
									copy += "|" + target;
								}
							} else {
								copy = target;
							}
							//							// 复制一份
							//							cmd = String.format("ffmpeg %s -i \"%s\" -f s16le -ar 44100 -ac 1 -i empty:// -vcodec copy %s -b:a 128k -absf aac_adtstoasc -f mp4 -movflags faststart \"%s\"", FFMpegUtils.getLogCommand(), copy, FFMpegUtils.getACodecCommand(), thumbFile.getPath());
							//							// cmd =
							//							// String.format("ffmpeg %s -i \"%s\" -vcodec copy -f mp4 -movflags faststart \"%s\"",
							//							// FFMpegUtils.getLogCommand(), copy,
							//							// thumbFile.getPath());
							//							Logger.e("[ImportVideoActivity]ts-mp4:" + cmd);
							//							if (UtilityAdapter.FFmpegRun("", cmd) != 0) {
							////									UtilityAdapter.stopEncodingLog(Logger.IsDebug);
							////									CrashUncaughtException.sendFfmpegLog();
							//								return null;
							//							} else {
							//								FileUtils.deleteFile(target);// 删除临时文件
							//							}
						}
						return thumbFile;
					} else {
						//							UtilityAdapter.stopEncodingLog(Logger.getIsDebug());
						//							CrashUncaughtException.sendFfmpegLog();
					}
					return null;
				}

				@Override
				protected void onPostExecute(File result) {
					super.onPostExecute(result);
					if (mProgressWheel != null) {
						mProgressWheel.stopSpinning();
					}
					hideProgress();
					if (!isFinishing() && result != null) {
						mVideoLoading.setVisibility(View.VISIBLE);
						mSourcePath = result.getPath();
						mVideoView.setVideoPath(mSourcePath);
					} else {
						ToastUtils.showToast(ImportVideoActivity.this, R.string.record_camera_import_gif_faild);
						finish();
					}
				}

			}.execute();
		}
	}

	//	/** 隐藏提示信息 */
	//	private void hideTips() {
	//		if (!isFinishing()) {
	//			mAreaTips = false;
	//			mTipsMove.setVisibility(View.GONE);
	//			mTipMoveText.clearAnimation();
	//			mTipMoveText.setAnimation(null);
	//			mTipMoveText.setVisibility(View.GONE);
	//
	//			if (mVideoView.getCanScrollX()) {
	//				PreferenceUtils.put(PreferenceKeys.VIDEO_EDIT_TIPS_LR, false);
	//			} else if (mVideoView.getCanScrollY()) {
	//				PreferenceUtils.put(PreferenceKeys.VIDEO_EDIT_TIPS_TB, false);
	//			}
	//		}
	//	}

	private VideoViewTouch.OnTouchEventListener mOnVideoTouchListener = new VideoViewTouch.OnTouchEventListener() {

		@Override
		public boolean onClick() {
			//			if (mAreaTips) {
			//				hideTips();
			//			}

			if (mVideoView.isPlaying()) {
				mVideoView.pauseClearDelayed();
			} else {
				mVideoView.start();
				mHandler.sendEmptyMessage(HANDLE_PROGRESS);
				// mVideoView.loopDelayed(mVideoSelection.getStartTime(),
				// mVideoSelection.getEndTime());
			}
			return true;
		}

		@Override
		public void onVideoViewDown() {
		}

		@Override
		public void onVideoViewUp() {

		}
	};

	public ProgressDialog showEncodingDialog(String message) {
		Logger.d("[RecordBaseActivity]showEncodingDialog...");
		ProgressDialog dialog = showProgress("", "");
		View convertView = LayoutInflater.from(this).inflate(R.layout.dialog_record_transcoding, null);
		//		mProgressTextView = (TextView) convertView.findViewById(android.R.id.message);
		mProgressWheel = (ProgressWheel) convertView.findViewById(R.id.progress);
		dialog.setContentView(convertView);
		if (StringUtils.isNotEmpty(message)) {
			mProgressWheel.setProgressEx(0);//TODO:
		}
		// mProgressDialog.getWindow().setBackgroundDrawableResource(R.drawable.uploader_dialog_bg);
		dialog.setCancelable(false);
		return dialog;
	}

	/** 显示进度 */
	private static final int HANDLE_PROGRESS = 1;
	/** 选区延迟检测 */
	private static final int HANDLE_SEEKTO = 2;

	/**
	 * 重置时间前的开始时间（关键帧的问题，导致可能需要重新设置开始和结束时间）
	 */
	private int mPreChangedStartTime;
	/**
	 * 重置时间前的结束时间
	 */
	private int mPreChangedEndTime;
	/**
	 * 是否重置时间标记
	 */
	private boolean mIsChangeTime;

	private long lastPosition = 0;

	/**
	 * 更新进度线的位置
	 */
	private void setLinePosition() {
		if (mVideoView != null) {

			int startTime = mVideoSelection.getStartTime();
			int endTime = mVideoSelection.getEndTime();

			long position = mVideoView.getCurrentPosition();
			if (lastPosition != 0 && Math.abs(position - lastPosition) > 500) {

				mPreChangedStartTime = startTime;
				mPreChangedEndTime = endTime;

				endTime = (int) position + endTime - startTime;
				startTime = (int) position;
				mVideoSelection.setStartTime(startTime);
				mVideoSelection.setEndTime(endTime);

				mVideoSelection.setStartTime(startTime);
				mVideoSelection.setEndTime(endTime);

				mIsChangeTime = true;
			}
			lastPosition = position;

			if (mVideoSelection != null) {
				if (mVideoSelection.mVideoSelection != null) {
					mVideoSelection.mVideoSelection.setLinePosition(position, startTime, endTime);
				}
			}
		}
	}

	/** 视频暂停时 把进度线也隐藏 */
	private void clearLine() {
		if (mVideoSelection != null) {
			if (mVideoSelection.mVideoSelection != null) {
				mVideoSelection.mVideoSelection.clearLine();
			}
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_PROGRESS:
				if (mVideoView.isPlaying()) {
					// 播放到结束时间的位置 则暂停不要循环
					long position = mVideoView.getCurrentPosition();

					if ((position >= mVideoSelection.getEndTime() && (lastPosition != 0 && Math.abs(position - lastPosition) < 500)) || position == mVideoView.getDuration()) {
						Logger.e("simon", "step1");
						if (mIsChangeTime) {
							Logger.e("simon", "当前重设的历史StartTime>>" + mPreChangedStartTime + ">>>当前记录的历史endTime>>>" + mPreChangedEndTime);
							mVideoSelection.setStartTime(mPreChangedStartTime);
							mVideoSelection.setEndTime(mPreChangedEndTime);
							mIsChangeTime = false;
						}
						Logger.e("simon", "暂停了?position ::" + position + "endTime::" + mVideoSelection.getEndTime() + "view.getDuration::" + mVideoView.getDuration());
						final int startTime = mVideoSelection.getStartTime();
						mVideoView.pauseClearDelayed();
						mVideoView.seekTo(startTime);
					} else {
						Logger.e("simon", "step2");
						setLinePosition();
						sendEmptyMessageDelayed(HANDLE_PROGRESS, 20);
					}
					setProgress();
				} else if (mVideoView.isPaused()) {
					Logger.e("simon", "step3");
					if (mIsChangeTime) {
						Logger.e("", "当前重设的历史StartTime>>" + mPreChangedStartTime + ">>>当前记录的历史endTime>>>" + mPreChangedEndTime);
						mVideoSelection.setStartTime(mPreChangedStartTime);
						mVideoSelection.setEndTime(mPreChangedEndTime);
						mIsChangeTime = false;
					}
					final int startTime = mVideoSelection.getStartTime();
					mVideoView.seekTo(startTime);
					setProgress();
				}
				break;
			case HANDLE_SEEKTO:
				if (!isFinishing()) {
					final int startTime = mVideoSelection.getStartTime();
					// Log.e("simon","HANDLE_SEEKTO::StartTime>>"+startTime+">>>endTime>>>"+mVideoSelection.getEndTime());
					if (mVideoView.isPlaying())
						mVideoView.loopDelayed(startTime, mVideoSelection.getEndTime());
					else
						mVideoView.seekTo(startTime);
					setProgress();
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	private long setProgress() {
		// if (mVideoView == null)
		return 0;
	}

	private int scale;

	@Override
	public void onChanged(int scale) {
		this.scale = scale;
		setVideoMode(scale);
	}

	@Override
	public void onChanged(boolean isWhiteBackground) {
		// TODO Auto-generated method stub
		if (isWhiteBackground) {
			mPreviewLinearLayout.setBackgroundColor(getResources().getColor(R.color.white));
			mIsWhiteBackground = true;
		} else {
			mIsWhiteBackground = false;
			mPreviewLinearLayout.setBackgroundColor(getResources().getColor(R.color.black));
		}
	}

	@Override
	public void onChanged() {
		// TODO Auto-generated method stub
		if (mVideoView != null) {
			mVideoView.pauseClearDelayed();
			mVideoView.seekTo(0);
		}
	}

	@Override
	public void onProgressChanged() {
		// TODO Auto-generated method stub
		// 拖动手柄或者滚动缩略图片的时候把视频停止 并把指针移到起始位置
		if (mVideoView != null) {
			// hideFirstTips();
			if (mVideoView.isPlaying()) {
				mVideoView.pauseClearDelayed();
			}
			int startTime = mVideoSelection.getStartTime();

			// Log.e("simon","onProgressChanged::StartTime>>"+startTime+">>>endTime>>>"+mVideoSelection.getEndTime());

			mVideoView.seekTo(startTime);
			setProgress();
			// if (mVideoTime != null && mVideoSelection != null) {
			// mVideoTime.setText(getString(R.string.left_second_tips,
			// mVideoSelection.getVideoCutTime()));
			// }
		}
		// if (mHandler.hasMessages(HANDLE_SEEKTO))
		// mHandler.removeMessages(HANDLE_SEEKTO);
		// mHandler.sendEmptyMessageDelayed(HANDLE_SEEKTO, 200);
	}

	@Override
	public void onProgressEnd() {
		// TODO Auto-generated method stub
		if (mHandler.hasMessages(HANDLE_SEEKTO))
			mHandler.removeMessages(HANDLE_SEEKTO);
		mHandler.sendEmptyMessageDelayed(HANDLE_SEEKTO, 20);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.titleLeft:
			onBackPressed();
			break;
		case R.id.titleRightTextView:
			startEncoding();
			break;
		}
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		// TODO Auto-generated method stub
		Logger.e("simon", "[ImportVideoActivity]onSeekComplete...");
		// mVideoView.start();
		lastPosition = 0;
		mPreChangedStartTime = 0;
		mPreChangedEndTime = 0;
		mIsChangeTime = false;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onStateChanged(boolean isPlaying) {
		// TODO Auto-generated method stub
		if (isPlaying) {
			mHandler.removeMessages(HANDLE_PROGRESS);
			mHandler.sendEmptyMessage(HANDLE_PROGRESS);
			mPlayController.setVisibility(View.GONE);
		} else {
			clearLine();
			mHandler.removeMessages(HANDLE_PROGRESS);
			mPlayController.setVisibility(View.VISIBLE);
		}
	}

	/** 视频时长 */
	private int mDuration = -1;

	@Override
	public void onPrepared(MediaPlayer mp) {
		// 检测
		mVideoLoading.setVisibility(View.GONE);
		mDuration = mVideoView.getDuration();

		if (mDuration < 3000) {
			ToastUtils.showToast(ImportVideoActivity.this, R.string.video_import_duration_too_short);
			finish();
			return;
		}

		// mVideoView.resize();
		// if (mVideoView.getCropX() == 0 && mVideoView.getCropY() == 0) {
		// mVideoView.centerXY(); 
		// }
		setVideoMode(scale);
		mVideoSelection.init(FileUtils.getCacheDiskPath(this, "thumbs"), mSourcePath, mDuration, RecorderHelper.getMaxDuration() > 10 * 1000 ? 60 * 1000 : 10 * 1000, 3 * 1000);

		// mVideoView.loopDelayed(mVideoSelection.getStartTime(),
		// mVideoSelection.getEndTime());
		mVideoView.start();
		//		mAreaTips = false;
		//		if (mVideoView.getCanScrollX()) {
		//			mAreaTips = PreferenceUtils.getBoolean(PreferenceKeys.VIDEO_EDIT_TIPS_LR, true);
		//			mTipsMove.setImageResource(R.drawable.record_tips_move_lr);
		//		} else if (mVideoView.getCanScrollY()) {
		//			mAreaTips = PreferenceUtils.getBoolean(PreferenceKeys.VIDEO_EDIT_TIPS_TB, true);
		//			mTipsMove.setImageResource(R.drawable.record_tips_move_tb);
		//		}
		//
		//		if (mAreaTips) {
		//			// mTipsMove.setVisibility(View.VISIBLE);
		//			// AnimationHelper.animationTips(this, mTipMoveText);
		//		} else {
		//			hideTips();
		//		}
	}

	private void setVideoMode(int scale) {
		if (scale == VideoSelectionView.FIT_XY) {
			mVideoView.resize();
			if (mVideoView.getCropX() == 0 && mVideoView.getCropY() == 0) {
				mVideoView.centerXY();
			}
			mIsFitCenter = false;
			mPreviewLinearLayout.setGravity(Gravity.NO_GRAVITY);
		} else if (scale == VideoSelectionView.FIT_CENTER) {
			mVideoView.fitCenter();
			mPreviewLinearLayout.setGravity(Gravity.CENTER);
			mIsFitCenter = true;
		}
	}

	/** 下一步转码 */
	@SuppressLint("NewApi")
	private void startEncoding() {
		// 检测磁盘空间
		if (!VCameraDemoApplication.isAvailableSpace()) {
			// ToastUtils.showToastErrorTip(R.string.record_check_available_faild);
			return;
		}

		if (mVideoSelection != null) {
			mVideoSelection.killSnapImage();
		}
		// ffmpeg -i 1.mp4 -vcodec copy -acodec copy -vbsf h264_mp4toannexb 1.ts
		// 将视频转成ts
		if (mMediaObject != null) {
			// 生成片段信息
			com.yixia.weibo.sdk.model.MediaObject$MediaPart part = mMediaObject.getLastPart();
			if (part == null) {
				part = mMediaObject.buildMediaPart(-1, ".mp4");
			}

			// 暂停播放
			mVideoView.pauseClearDelayed();

			Logger.e("samuel", " mVideoSelection.getStartTime()" + mVideoSelection.getStartTime() + "<><>mPreStartTime::" + mPreChangedStartTime);

			final com.yixia.weibo.sdk.model.MediaObject$MediaPart mediaPart = part;
			final int videoWidth = mVideoView.getVideoWidth();
			final int videoHeight = mVideoView.getVideoHeight();
			final int cropX = mVideoView.getCropX();
			final int cropY = mVideoView.getCropY();
			final float scale = mVideoView.getScale();

			int startTimetmp = 0;
			int endTimetmp = 0;
			if (mIsChangeTime) {
				startTimetmp = mPreChangedStartTime;
				endTimetmp = mPreChangedEndTime;
			} else {
				startTimetmp = mVideoSelection.getStartTime();
				endTimetmp = mVideoSelection.getEndTime();
			}

			final int startTime = startTimetmp;
			final int endTime = endTimetmp;
			final String output = mediaPart.mediaPath;

			part.duration = endTime - startTime;
			mTempVideoTranscodeFinishd = false;

			Logger.e("startTime / 1000F, (endTime - startTime) / 1000F " + startTime / 1000F + "," + mVideoSelection.getVideoTime() / 1000F);

			new ThreadTask<Void, Void, Boolean>() {

				@Override
				protected void onPreExecute() {
					super.onPreExecute();
					showDialog();
				}

				@Override
				protected Boolean doInBackground(Void... params) {

					// //强制停止截图
					// mVideoSelection.stopCaptureThumbnails();

					// 检测是否正在截取缩略图，是的话要等切完了再转
					while (mVideoSelection.isThumbLoading()) {
						SystemClock.sleep(500);
					}

					// //检测是否完成
					// while (UtilityAdapter.FFmpegIsRunning("filter_main")) {
					// SystemClock.sleep(1000);
					// }

					// 校验视频是否旋转
					// if (mSourcePath != null &&
					// !mSourcePath.startsWith("http://") &&
					// !mSourcePath.startsWith("https://")) {
					// if (DeviceUtils.hasJellyBeanMr1()) {
					// try {
					// MediaMetadataRetriever metadata = new
					// MediaMetadataRetriever();
					// metadata.setDataSource(mSourcePath);
					// mVideoRotation =
					// ConvertToUtils.toInt(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION),
					// -1);
					// // int duration =
					// ConvertToUtils.toInt(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION),
					// -1);
					// } catch (Exception e) {
					// Logger.e(e);
					// }
					// } else {
					// mVideoRotation =
					// UtilityAdapter.VideoGetMetadataRotate(mSourcePath);
					// }
					// }
					if (mVideoRotation <= 0) {
						mVideoRotation = UtilityAdapter.VideoGetMetadataRotate(mSourcePath);
					}

					// mEstimateTargetVideoSize = (endTime - startTime);
					// startEstimateProgress(mEstimateTargetVideoSize);
					String cutpath = mediaPart.mediaPath;// + ".mp4";
					if (StringUtils.isNotEmpty(cutpath)) {
						File f = new File(cutpath);
						if (!f.exists()) {
							String parentPath = f.getParent();
							File file = new File(parentPath);
							if (!file.exists()) {
								try {
									//按照指定的路径创建文件夹  
									file.mkdirs();
								} catch (Exception e) {
									// TODO: handle exception  
								}
							}
							File dir = new File(cutpath);
							if (!dir.exists()) {
								try {
									//在指定的文件夹中创建文件  
									dir.createNewFile();
								} catch (Exception e) {
								}
							}
						}
					}
					// ========== 先切割 -vcodec copy -acodec copy -vbsf
					// h264_mp4toannexb
					String cmd = String.format("ffmpeg %s -ss %.1f -i \"%s\" -t %.1f -vcodec copy -acodec copy  -f mp4 -movflags faststart \"%s\"", FFMpegUtils.getLogCommand(), startTime / 1000F, mSourcePath, mVideoSelection.getVideoTime() / 1000F, cutpath);

					boolean result = UtilityAdapter.FFmpegRun("", cmd) == 0;
					//					if (!result){
					//						UtilityAdapter.stopEncodingLog(Logger.getIsDebug());
					//						CrashUncaughtException.sendFfmpegLog();
					//					}
					return result;
				}

				@Override
				protected void onPostExecute(Boolean result) {
					super.onPostExecute(result);
					hideDialog();
					if (!isFinishing()) {
						if (result) {
							mMediaObject.cropX = cropX;
							mMediaObject.cropY = cropY;
							mMediaObject.videoWidth = videoWidth;
							mMediaObject.videoHeight = videoHeight;
							mMediaObject.mVideoRotation = mVideoRotation;
							mMediaObject.scale = scale;
							mMediaObject.mIsFitCenter = mIsFitCenter;
							mMediaObject.mIsWhiteBackground = mIsWhiteBackground;
							MediaObject.writeFile(mMediaObject);
							Intent intent = new Intent(ImportVideoActivity.this, MediaPreviewActivity.class);
							Bundle bundle = getIntent().getExtras();
							if (bundle == null) {
								bundle = new Bundle();
							}

							bundle.putSerializable(CommonIntentExtra.EXTRA_MEDIA_OBJECT, mMediaObject);
							bundle.putString("output", mediaPart.mediaPath);
							// bundle.putInt("cropX", cropX);
							// bundle.putInt("cropY", cropY);
							// bundle.putInt("videoWidth", videoWidth);
							// bundle.putInt("videoHeight", videoHeight);
							// bundle.putFloat("scale", scale);
							// bundle.putInt("rotation", mVideoRotation);
							// bundle.putBoolean("is_fitcenter",
							// mIsFitCenter);
							// bundle.putBoolean("is_white_background",
							// mIsWhiteBackground);
							bundle.putBoolean(CommonIntentExtra.EXTRA_MEDIA_IMPORT_VIDEO, true);
							intent.putExtras(bundle);
							startActivity(intent);
							//							// 画面从右到左
							//							overridePendingTransition(R.anim.activity_right_in, R.anim.activity_right_out);
						} else {
							ToastUtils.showToast(ImportVideoActivity.this, R.string.video_transcoding_faild);
						}
					}
				}
			}.execute();
		}
	}

	@Override
	public void onBackPressed() {
		hideDialog();
		// 删除临时文件
		if (mMediaObject != null) {
			mMediaObject.cancel();
		}
		finish();
	}

	ProgressDialog mEncodingProgressDialog;

	private void showDialog() {
		if (isFinishing()) {
			return;
		}
		if (mEncodingProgressDialog == null)
			mEncodingProgressDialog = showVideoProcessDialog(this, this.getResources().getString(R.string.dialog_encoding_text));
		mEncodingProgressDialog.show();
	}

	private void hideDialog() {
		if (!isFinishing() && mEncodingProgressDialog != null && mEncodingProgressDialog.isShowing()) {
			mEncodingProgressDialog.dismiss();
			mEncodingProgressDialog = null;
		}
	}

	public static ProgressDialog showVideoProcessDialog(Context mContext, String text) {
		ProgressDialog dialog = new ProgressDialog(mContext);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setIndeterminate(true);
		dialog.show();
		View convertView = LayoutInflater.from(mContext).inflate(R.layout.dialog_encoding_novalue, null);
		dialog.setContentView(convertView);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		TextView textview = (TextView) convertView.findViewById(R.id.text);
		if (StringUtils.isEmpty(text)) {
			textview.setVisibility(View.GONE);
		} else {
			textview.setVisibility(View.VISIBLE);
			textview.setText(text);
		}
		return dialog;
	}

}

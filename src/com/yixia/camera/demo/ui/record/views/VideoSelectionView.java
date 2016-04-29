package com.yixia.camera.demo.ui.record.views;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lcr.mplay.R;
import com.yixia.camera.demo.log.Logger;
import com.yixia.camera.demo.ui.record.Crypto;
import com.yixia.camera.demo.ui.record.views.HorizontalScrollViewEx.OnFlingListener;
import com.yixia.videoeditor.adapter.UtilityAdapter;
import com.yixia.weibo.sdk.FFMpegUtils;
import com.yixia.weibo.sdk.util.ConvertToUtils;
import com.yixia.weibo.sdk.util.DeviceUtils;
import com.yixia.weibo.sdk.util.FileUtils;
import com.yixia.weibo.sdk.util.StringUtils;

public class VideoSelectionView extends RelativeLayout implements OnFlingListener, android.widget.CompoundButton.OnCheckedChangeListener {
	/** 背景图片滚动区域 */
	private HorizontalScrollViewEx mVideoThumbnailsBackground;
	/** 时间显示 */
	protected TextView mStartTimeView, mSelectionTimeView, mEndTimeView;
	/** 截图 */
	protected LinearLayout mVideoThumbnails;
	/** 左边手柄，右边手柄 */
	private View mSeekLeft, mSeekRight;
	/** 选择区域 */
	public SelectionView mVideoSelection;

	private View videoModeControllerLayout;

	private boolean startEncoding;

	public void setStartEncoding(boolean startEncoding) {
		this.startEncoding = startEncoding;
	}

	private CheckBox mChangeVideoviewMode;

	/**
	 * 设置背景颜色 黑，白
	 */
	private RadioButton mCheckBoxBackgroudBlack, mCheckBoxBackgroudWhite;

	private RadioGroup mRadioGroupBackground;

	/** 拖动 */
	protected OnSeekBarChangeListener mOnSeekBarChangeListener;

	protected OnSwich60sListener mOnSwich60sListener;
	/**
	 * 改变背景
	 */
	protected OnBackgroundColorListener mOnBackgroundColorListener;

	/**
	 * 改变视频大小
	 */
	protected OnVideoChangeScaleTypeListener onVideoChangeScaleTypeListener;

	public void setOnVideoChangeScaleTypeListener(OnVideoChangeScaleTypeListener onVideoChangeScaleTypeListener) {
		this.onVideoChangeScaleTypeListener = onVideoChangeScaleTypeListener;
	}

	/** 边距信息设置 */
	private RelativeLayout.LayoutParams mSeekLeftLayoutParams, mSeekRightLayoutParams;
	/** 处理左右滑动 */
	private GestureDetector mGestureDetector;
	/** 截图数组 */
	private ArrayList<Thumb> thumbs;
	/** 本视频缓存路径 */
	private File mThubmDir;

	private ImageView mRecord60s;
	// private TextView mVideoTime;

	/** 视频路径 */
	private String videoPath;
	/** 开始时间，结束时间 */
	protected int mSpace, mStartTime, mEndTime;

	/** 最大宽度，时长、窗口宽度 */
	protected int mMaxWidth, mMinWidth, mDuration, mWindowWidth, mSeekMargin, mThumbWidth;

	protected int maxDuration, minDuration;

	/** 截图索引，配合mPageSize实现分页效果 */
	// private int mStartIndex = 1;
	/** 每次截图10张图 */
	private int mPageSize = 10;
	/** 每张图代表的时间 */
	protected float mSecondWidth;
	/** 是否初始化完成 */
	private boolean mPrepared;
	/** 左右手柄是否获得焦点 */
	private boolean mLeftFocus, mRightFocus;
	/** 是否正在截图 */
	private volatile boolean mThumbLoading;
	/** HorizontalScrollViewEx默认会触发一次 */
	private boolean mFirstFlingScrollChange = true;

	private Context mContext;

	public VideoSelectionView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public VideoSelectionView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	/** 初始化 */
	private void init() {
		mPrepared = false;
		mThumbWidth = ConvertToUtils.dipToPX(getContext(), 56);
		mSpace = ConvertToUtils.dipToPX(getContext(), 20);
		mSeekMargin = ConvertToUtils.dipToPX(getContext(), 3);
		LayoutInflater.from(getContext()).inflate(R.layout.view_video_selection, this);

		mChangeVideoviewMode = (CheckBox) findViewById(R.id.change_videoview_mode);
		mChangeVideoviewMode.setOnCheckedChangeListener(this);

		mRadioGroupBackground = (RadioGroup) findViewById(R.id.radiogroup_background);
		mCheckBoxBackgroudBlack = (RadioButton) findViewById(R.id.backgroud_black);
		mCheckBoxBackgroudBlack.setOnCheckedChangeListener(this);
		mCheckBoxBackgroudWhite = (RadioButton) findViewById(R.id.backgroud_white);
		mCheckBoxBackgroudWhite.setOnCheckedChangeListener(this);

		videoModeControllerLayout = findViewById(R.id.video_mode_controller_layout);

//		mRecord60s = (ImageView) findViewById(R.id.record_60s);
		// mVideoTime = (TextView) findViewById(R.id.video_time);

		mSeekLeft = findViewById(R.id.video_selection_seek_left);
		mSeekRight = findViewById(R.id.video_selection_seek_right);
		mVideoThumbnailsBackground = (HorizontalScrollViewEx) findViewById(R.id.video_thumbnails_background);
		mStartTimeView = (TextView) findViewById(R.id.start_time);
		mSelectionTimeView = (TextView) findViewById(R.id.selection_time);
		mEndTimeView = (TextView) findViewById(R.id.end_time);
		mVideoThumbnails = (LinearLayout) findViewById(R.id.video_thumbnails);
		mVideoSelection = (SelectionView) findViewById(R.id.video_selection);

		mVideoThumbnailsBackground.setOnFlingListener(this);
		mSeekLeft.setOnTouchListener(mSeekOnTouchListener);
		mSeekRight.setOnTouchListener(mSeekOnTouchListener);
		mGestureDetector = new GestureDetector(getContext(), new VideoSelectionGestureListener(this));
		mSeekLeftLayoutParams = (RelativeLayout.LayoutParams) mSeekLeft.getLayoutParams();
		mSeekRightLayoutParams = (RelativeLayout.LayoutParams) mSeekRight.getLayoutParams();
	}

	/** 开始时间 */
	public int getStartTime() {
		return mStartTime;
	}

	public void setStartTime(int time) {
		this.mStartTime = time;
	}

	public void setEndTime(int time) {
		this.mEndTime = time;
	}

	/** 结束时间 */
	public int getEndTime() {
		return mEndTime;
	}

	public String getVideoCutTime() {
		int time = mEndTime - mStartTime;

		return gennerTimeSencond(time < 3 * 1000 ? 3 * 1000 : time);
	}

	public int getVideoTime() {
		int time = mEndTime - mStartTime;

		return time < 3000 ? 3000 : time;
	}

	private void set60sVideo(int maxDuration, File mThumbCacheDir) {

		this.mSecondWidth = mWindowWidth / (maxDuration / 1000F);// 480 / 10
		// ，相当于每48像素表示1秒
		this.mMaxWidth = mDuration * mWindowWidth / maxDuration;
		this.mMinWidth = minDuration * mWindowWidth / maxDuration;

		// 设置截图滚动范围
		ViewGroup.LayoutParams lp = mVideoThumbnails.getLayoutParams();
		int top = ConvertToUtils.dipToPX(mContext, 18);
		lp.width = mMaxWidth + top;
		mVideoThumbnails.setLayoutParams(lp);

		mStartTime = 0;

		//
		mEndTime = maxDuration;
		mVideoSelection.setMinRightMargin(0);
		mVideoSelection.setLeftMargin(0);
		mSeekLeftLayoutParams.leftMargin = 0;
		mSeekRightLayoutParams.rightMargin = 0;
		mSeekRight.setLayoutParams(mSeekRightLayoutParams);

		if (mSeekLeft.getWidth() > 0) {
			int margin = mSeekLeft.getWidth() / 2 + mSeekMargin;
			mVideoSelection.setMargin(margin);
			// 设置背景图边距

			if (mDuration > 10 * 1000) {
				// mVideoThumbnailsBackground.setPadding(0,
				// mVideoThumbnailsBackground.getPaddingTop(), 0,
				// mVideoThumbnailsBackground.getPaddingBottom());
			}

		}

		mVideoSelection.invalidate();
		refreshTimes();

		if (mOnSwich60sListener != null) {
			mOnSwich60sListener.onChanged();
		}

		mThumbLoading = false;

		if (mThumbCacheDir == null) {
			mPrepared = true;
		} else {
			if (!mThumbCacheDir.exists())
				mThumbCacheDir.mkdirs();
			prepareThumbs(mThumbCacheDir, videoPath);
		}

		// if (mVideoTime != null && mVideoSelection != null) {
		// mVideoTime.setText(mContext.getString(R.string.left_second_tips,
		// getVideoCutTime()));
		// }
	}

	/**
	 * 
	 * @param duration
	 *            视频时长
	 * @param maxDuration
	 *            最大选择时长 10 * 1000
	 * @param minDuration
	 *            最小时长 3 * 1000
	 */
	public void init(final File mThumbCacheDir, String videoPath, int duration, int maxDuration, int minDuration) {

		this.videoPath = videoPath;
		// mWindowWidth = DeviceUtils.getScreenWidth(getContext()) -
		// mSeekLeft.getWidth() - mSeekMargin - mSeekMargin;
		int top = ConvertToUtils.dipToPX(mContext, 18);
		mWindowWidth = DeviceUtils.getScreenWidth(getContext()) - top * 2;
		this.mDuration = duration;
		this.maxDuration = maxDuration;
		this.minDuration = minDuration;

		if (duration < maxDuration)
			maxDuration = duration;

		set60sVideo(maxDuration, mThumbCacheDir);
		//
		// //设置60s
		// if (isCan60s()) {
		// mRecord60s.setVisibility(View.VISIBLE);
		// mRecord60s.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// isUse60s = !isUse60s;
		// set60sVideo(60 * 1000, mImageFetcher, mThumbCacheDir, isUse60s);
		// }
		// });
		// } else {
		// mRecord60s.setVisibility(View.INVISIBLE);
		// }
	}

	/** 加载截图 */
	private void prepareThumbs(File mThumbCacheDir, final String videoPath) {
		// 检测是否是否已经截图过了
		String key = Crypto.md5(videoPath);
		final File thubmDir = new File(mThumbCacheDir, key);
		if (!thubmDir.exists())
			thubmDir.mkdirs();

		thumbs = new ArrayList<Thumb>();
		int left = 0;
		// int width = mMaxWidth - mSeekMargin - mSeekMargin;
		int width = mMaxWidth;
		do {
			boolean firstDraw = false;
			int position = (int) (left / mSecondWidth * 1000F);
			left += mThumbWidth;
			if (left <= width) {
				firstDraw = true;
			}
			// mSecondWidth = mWindowWidth / (maxDuration / 1000F);//480 / 10
			// ，相当于每48像素表示1秒
			int index = thumbs.size() + 1;
			String thumbPath = FileUtils.concatPath(thubmDir.getPath(), "thumb_" + index + ".jpg");
			if (position > mDuration) {
				position = mDuration;
			}
			thumbs.add(new Thumb(firstDraw, thumbPath, index, position));
		} while (left < mMaxWidth);

		this.mThubmDir = thubmDir;

		loadVideoThumbImageView(thumbs, videoPath);
		if (thubmDir.exists() && thubmDir.canRead() && thubmDir.isDirectory() && thubmDir.list() != null && thubmDir.list().length > 0) {
			// 不需要截图
			checkLoadThumb();
			return;
		}

		loadThumb(1, 0);
	}

	/** 是否正在加载缩略图 */
	public boolean isThumbLoading() {
		return mThumbLoading;
	}

	private int thumbsIndex;

	/** 加载截图 */
	private boolean loadThumb(final int startIndex, final int startTime) {
		if (thumbs == null || StringUtils.isEmpty(videoPath) || mThumbLoading || startEncoding)
			return false;

		if (UtilityAdapter.FFmpegIsRunning("snapimage")) {
			return false;
		}

		Logger.d("loadThumb...");

		mThumbLoading = true;
		// 批量截图
		final int total = thumbs.size();
		int count = (startIndex + mPageSize) <= (total + 1) ? mPageSize : (total - (startIndex - 1));
		// Logger.e("[VideoSelectionView]loadThumb...startIndex:" + startIndex +
		// " mPageSize:" + mPageSize + " count:" + count + " total:" + total);

		// 检测是否已经截过了
		// for (int i = startIndex, j = startIndex + count; i < j; i++) {
		// String thumbPath = FileUtils.concatPath(mThubmDir.getPath(), "thumb_"
		// + i + ".jpg");
		// File f = new File(thumbPath);
		// if (f != null && f.exists() && f.canRead() && f.length() > 0) {
		// count = i - startIndex;
		// break;
		// }
		// }
		if (count <= 0) {
			mThumbLoading = false;
			return false;
		}

		final int vframes = count;
		final float r = mThumbWidth / mSecondWidth;

		new AsyncTask<Void, Void, Boolean>() {

			@Override
      protected Boolean doInBackground(Void... params) {
				if (mThubmDir == null)
					return false;

				String tempVideo = FileUtils.concatPath(mThubmDir.getPath(), "temp.mp4");
				String cmd;

				if (Thread.currentThread().isInterrupted())
					return false;

				// 检测是否有ffmpeg命令正在执行
				while (UtilityAdapter.FFmpegIsRunning("filter_main")) {
					if (Thread.currentThread().isInterrupted())
						return false;
					SystemClock.sleep(1000);
				}
				if (startTime > 0F) {
					FileUtils.deleteFile(tempVideo);
					// 快速截图方法：先切视频，后截图
					cmd = String.format("ffmpeg %s -ss %.2f -i \"%s\" -t %.2f -vcodec copy -acodec copy \"%s\"", FFMpegUtils.getLogCommand(), startTime / 1000F, videoPath, (vframes + 1) * r, tempVideo);
					if (UtilityAdapter.FFmpegRun("", cmd) != 0) {
//						UtilityAdapter.stopEncodingLog(Logger.getIsDebug());
//						CrashUncaughtException.sendFfmpegLog();
						return false;
					}
				} else {
					tempVideo = videoPath;
				}

				int mVideoRotation = 0;
				// 校验视频是否旋转
				if (videoPath != null) {
					if (DeviceUtils.hasJellyBeanMr1()) {
						try {
							MediaMetadataRetriever metadata = new MediaMetadataRetriever();
							metadata.setDataSource(videoPath);
							mVideoRotation = ConvertToUtils.toInt(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION), 0);
							// int duration =
							// ConvertToUtils.toInt(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION),
							// -1);
						} catch (Exception e) {
							Logger.e(e);
						}
					} else {
						mVideoRotation = UtilityAdapter.VideoGetMetadataRotate(videoPath);
					}
				}

				Logger.e("mVideoRotation = " + mVideoRotation);

				if (Thread.currentThread().isInterrupted())
					return false;

				StringBuffer ffmpeg = new StringBuffer();
				if (mVideoRotation != 0) {
					ffmpeg.append(" -vf ");
					switch ((mVideoRotation + 360) % 360) {
					case 90:
						ffmpeg.append("transpose=1 ");
						break;
					case 270:
						ffmpeg.append("transpose=2 ");
						break;
					case 180:
						ffmpeg.append("\"vflip,hflip\" ");
						break;
					}
				}

				cmd = String.format("ffmpeg %s -i \"%s\" -r 1/%.1f -start_number %d -vframes %d -s %dx%d %s \"%s", FFMpegUtils.getLogCommand(), tempVideo, r, startIndex, vframes, mThumbWidth, mThumbWidth, ffmpeg, mThubmDir.getPath()) + File.separator + "thumb_%d.jpg\"";

				Logger.e("cmd = " + cmd);

				boolean result = (UtilityAdapter.FFmpegRun("snapimage", cmd) == 0);
//				if (!result){
//					UtilityAdapter.stopEncodingLog(Logger.getIsDebug());
//					CrashUncaughtException.sendFfmpegLog();
//				}
				thumbsIndex = 1;
				// 异步截图
				while (true) {
					if (Thread.currentThread().isInterrupted()) {
						UtilityAdapter.FFmpegKill("snapimage");
						return false;
					}
					if (!UtilityAdapter.FFmpegIsRunning("snapimage")) {
						break;
					}

					File f = new File((mThubmDir.getPath() + File.separator), String.format("thumb_%d.jpg", thumbsIndex));
					if (f.exists() && f.canRead() && f.length() > 0) {
						if (getContext() != null) {
							Activity activity = (Activity) getContext();
							if (activity != null && !activity.isFinishing()) {
								activity.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										checkLoadThumb2();
									}
								});
							}
						}
						thumbsIndex++;
					} else {
						try {
							Thread.sleep(100);
						} catch (Exception e) {
							// TODO: handle exception
						}

					}

				}
				return result;
			}

			@Override
      protected void onPreExecute() {
	      // TODO Auto-generated method stub
	      super.onPreExecute();
      }

			@Override
      protected void onPostExecute(Boolean result) {
	      // TODO Auto-generated method stub
	      super.onPostExecute(result);
				mThumbLoading = false;
				if (result && getContext() != null && !Thread.currentThread().isInterrupted()) {
					Activity activity = (Activity) getContext();
					if (activity != null && !activity.isFinishing()) {
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								checkLoadThumb();
							}
						});
					}
				}
			}
		}.execute();
		
//		mImportHelper.loadVideoThumb(new OnCaptureThumbnailsListener() {
//
//			@Override
//			public boolean doInBackground() {
//				if (mThubmDir == null)
//					return false;
//
//				String tempVideo = FileUtils.concatPath(mThubmDir.getPath(), "temp.mp4");
//				String cmd;
//
//				if (Thread.currentThread().isInterrupted())
//					return false;
//
//				// 检测是否有ffmpeg命令正在执行
//				while (UtilityAdapter.FFmpegIsRunning("filter_main")) {
//					if (Thread.currentThread().isInterrupted())
//						return false;
//					SystemClock.sleep(1000);
//				}
//				if (startTime > 0F) {
//					FileUtils.deleteFile(tempVideo);
//					// 快速截图方法：先切视频，后截图
//					cmd = String.format("ffmpeg %s -ss %.2f -i \"%s\" -t %.2f -vcodec copy -acodec copy \"%s\"", FFMpegUtils.getLogCommand(), startTime / 1000F, videoPath, (vframes + 1) * r, tempVideo);
//					if (UtilityAdapter.FFmpegRun("", cmd) != 0) {
//						UtilityAdapter.stopEncodingLog(Logger.getIsDebug());
//						CrashUncaughtException.sendFfmpegLog();
//						return false;
//					}
//				} else {
//					tempVideo = videoPath;
//				}
//
//				int mVideoRotation = 0;
//				// 校验视频是否旋转
//				if (videoPath != null) {
//					if (DeviceUtils.hasJellyBeanMr1()) {
//						try {
//							MediaMetadataRetriever metadata = new MediaMetadataRetriever();
//							metadata.setDataSource(videoPath);
//							mVideoRotation = ConvertToUtils.toInt(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION), 0);
//							// int duration =
//							// ConvertToUtils.toInt(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION),
//							// -1);
//						} catch (Exception e) {
//							Logger.e(e);
//						}
//					} else {
//						mVideoRotation = UtilityAdapter.VideoGetMetadataRotate(videoPath);
//					}
//				}
//
//				Logger.systemErr("mVideoRotation = " + mVideoRotation);
//
//				if (Thread.currentThread().isInterrupted())
//					return false;
//
//				StringBuffer ffmpeg = new StringBuffer();
//				if (mVideoRotation != 0) {
//					ffmpeg.append(" -vf ");
//					switch ((mVideoRotation + 360) % 360) {
//					case 90:
//						ffmpeg.append("transpose=1 ");
//						break;
//					case 270:
//						ffmpeg.append("transpose=2 ");
//						break;
//					case 180:
//						ffmpeg.append("\"vflip,hflip\" ");
//						break;
//					}
//				}
//
//				cmd = String.format("ffmpeg %s -i \"%s\" -r 1/%.1f -start_number %d -vframes %d -s %dx%d %s \"%s", FFMpegUtils.getLogCommand(), tempVideo, r, startIndex, vframes, mThumbWidth, mThumbWidth, ffmpeg, mThubmDir.getPath()) + File.separator + "thumb_%d.jpg\"";
//
//				Logger.systemErr("cmd = " + cmd);
//
//				boolean result = (UtilityAdapter.FFmpegRun("snapimage", cmd) == 0);
//				if (!result){
//					UtilityAdapter.stopEncodingLog(Logger.getIsDebug());
//					CrashUncaughtException.sendFfmpegLog();
//				}
//				thumbsIndex = 1;
//				// 异步截图
//				while (true) {
//					if (Thread.currentThread().isInterrupted()) {
//						UtilityAdapter.FFmpegKill("snapimage");
//						return false;
//					}
//					if (!UtilityAdapter.FFmpegIsRunning("snapimage")) {
//						break;
//					}
//
//					File f = new File((mThubmDir.getPath() + File.separator), String.format("thumb_%d.jpg", thumbsIndex));
//					if (f.exists() && f.canRead() && f.length() > 0) {
//						if (getContext() != null) {
//							Activity activity = (Activity) getContext();
//							if (activity != null && !activity.isFinishing()) {
//								activity.runOnUiThread(new Runnable() {
//									@Override
//									public void run() {
//										checkLoadThumb2();
//									}
//								});
//							}
//						}
//						thumbsIndex++;
//					} else {
//						try {
//							Thread.sleep(100);
//						} catch (Exception e) {
//							// TODO: handle exception
//						}
//
//					}
//
//				}
//				return result;
//			}
//
//			@Override
//			public void onPostExecute(boolean result) {
//				mThumbLoading = false;
//				if (result && getContext() != null && !Thread.currentThread().isInterrupted()) {
//					Activity activity = (Activity) getContext();
//					if (activity != null && !activity.isFinishing()) {
//						activity.runOnUiThread(new Runnable() {
//							@Override
//							public void run() {
//								checkLoadThumb();
//							}
//						});
//					}
//				}
//			}
//
//			@Override
//			public void onShutdown() {
//				mThumbLoading = false;
//			}
//		});

		// new AsyncTask<Void, Void, Boolean>() {
		//
		// @Override
		// protected Boolean doInBackground(Void... params) {
		// String tempVideo = FileUtils.concatPath(mThubmDir.getPath(),
		// "temp.mp4");
		// String cmd;
		// if (startTime > 0F) {
		// FileUtils.deleteFile(tempVideo);
		// //快速截图方法：先切视频，后截图
		// cmd =
		// String.format("ffmpeg %s -ss %.2f -i \"%s\" -t %.2f -vcodec copy -acodec copy \"%s\"",
		// FFMpegUtils.getLogCommand(), startTime / 1000F, videoPath, (vframes +
		// 1) * r, tempVideo);
		// if (UtilityAdapter.FFmpegRun("", cmd) != 0) {
		// return false;
		// }
		// } else {
		// tempVideo = videoPath;
		// }
		// cmd =
		// String.format("ffmpeg %s -i \"%s\" -r 1/%.1f -start_number %d -vframes %d -s %dx%d \"%s",
		// FFMpegUtils.getLogCommand(), tempVideo, r, startIndex, vframes,
		// mThumbWidth, mThumbWidth, mThubmDir.getPath()) + File.separator +
		// "thumb_%d.jpg\"";
		// return (UtilityAdapter.FFmpegRun("", cmd) == 0);
		// }
		//
		// @Override
		// protected void onPostExecute(Boolean result) {
		// super.onPostExecute(result);
		// mThumbLoading = false;
		// if (result) {
		// checkLoadThumb();
		// }
		// }
		//
		// }.execute();
		return true;
	}

	public void killSnapImage() {
		UtilityAdapter.FFmpegKill("snapimage");
	}

	/** 强制停止截图 */
	public void stopCaptureThumbnails() {
//		if (mImportHelper != null) {
//			mImportHelper.onDestory();
//		}
	}

	/** 加载缩略图 */
	private void loadVideoThumbImageView(ArrayList<Thumb> thumbs, String videoPath) {
		if (thumbs != null && thumbs.size() > 0 && StringUtils.isNotEmpty(videoPath)) {
			mVideoThumbnails.removeAllViews();
			int top = ConvertToUtils.dipToPX(mContext, 18);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(top, LayoutParams.WRAP_CONTENT);
			mVideoThumbnails.addView(new TextView(mContext), lp);
			int width = 0;
			for (Thumb thumb : thumbs) {
				VideoThumbImageView view = new VideoThumbImageView(getContext(), thumb.thumbPath, mWindowWidth, thumb.index, thumb.startTime);
				lp = new LinearLayout.LayoutParams(mThumbWidth, mThumbWidth);
				// 处理一张图片显示不下的问题，也能防止滚越界的情况
				if (width + mThumbWidth <= mMaxWidth) {
					lp.width = mThumbWidth;
					view.setScaleType(ScaleType.FIT_XY);
				} else {
					lp.width = mMaxWidth - width;
					view.setScaleType(ScaleType.CENTER_CROP);
				}
				if (thumb.firstDraw)
					view.loadImage();
				mVideoThumbnails.addView(view, lp);
				width += mThumbWidth;
			}
			lp = new LinearLayout.LayoutParams(top, LayoutParams.WRAP_CONTENT);
			mVideoThumbnails.addView(new TextView(mContext), lp);
		}
		mPrepared = true;
	}

	/** 从左往右滑 */
	private void onLeftToRight(int distance) {
		if (mLeftFocus) {
			// 左边指针往右移
			if (mSeekLeftLayoutParams != null) {
				// 检查必须大于3秒
				if (mVideoSelection.getCurrentWidth() - distance < mMinWidth)
					distance = mVideoSelection.getCurrentWidth() - mMinWidth;
				if (distance > 0) {
					mSeekLeftLayoutParams.leftMargin += distance;
					mVideoSelection.setLeftMargin(mSeekLeftLayoutParams.leftMargin);
					mSeekLeft.setLayoutParams(mSeekLeftLayoutParams);
					updateTimes();
				}
			}
		} else if (mRightFocus) {
			// 右边指针往右移
			if (mSeekRightLayoutParams != null && mSeekRightLayoutParams.rightMargin > mVideoSelection.getMinRightMargin()) {
				if (mSeekRightLayoutParams.rightMargin - distance < mVideoSelection.getMinRightMargin())// 处理最右
					distance = mSeekRightLayoutParams.rightMargin - mVideoSelection.getMinRightMargin();
				if (distance != 0) {
					mSeekRightLayoutParams.rightMargin -= distance;
					mVideoSelection.setRightMargin(mSeekRightLayoutParams.rightMargin);
					mSeekRight.setLayoutParams(mSeekRightLayoutParams);
					updateTimes();
				}
			}
		}
	}

	/** 从右往左滑动 */
	private void onRightToLeft(int distance) {
		if (mLeftFocus) {
			// 左边指针往左移
			if (mSeekLeftLayoutParams != null && mSeekLeftLayoutParams.leftMargin > 0) {
				if (mSeekLeftLayoutParams.leftMargin - distance < 0)// 处理最左
					distance = mSeekLeftLayoutParams.leftMargin;
				if (distance != 0) {
					mSeekLeftLayoutParams.leftMargin -= distance;
					mVideoSelection.setLeftMargin(mSeekLeftLayoutParams.leftMargin);// +
					// mSeekLeft.getWidth()
					// /
					// 2
					mSeekLeft.setLayoutParams(mSeekLeftLayoutParams);
					updateTimes();
				}
			}
		} else if (mRightFocus) {
			// 右边指针往左移
			if (mSeekRightLayoutParams != null) {
				// 检查必须大于3秒
				if (mVideoSelection.getCurrentWidth() - distance < mMinWidth)
					distance = mVideoSelection.getCurrentWidth() - mMinWidth;
				if (distance > 0) {

					mSeekRightLayoutParams.rightMargin += distance;
					mVideoSelection.setRightMargin(mSeekRightLayoutParams.rightMargin);
					mSeekRight.setLayoutParams(mSeekRightLayoutParams);
					updateTimes();
				}
			}
		}
	}

	/** 选区指针识别 */
	private OnTouchListener mSeekOnTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (v == mSeekLeft) {
					mLeftFocus = true;
					mSeekLeft.setPressed(true);
				} else if (v == mSeekRight) {
					mRightFocus = true;
					mSeekRight.setPressed(true);
				}

			}

			if (mGestureDetector != null && mGestureDetector.onTouchEvent(event))
				return true;

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_UP:
				if (v == mSeekLeft) {
					mSeekLeft.setPressed(false);
				} else if (v == mSeekRight) {
					mSeekRight.setPressed(false);
				}
				mLeftFocus = false;
				mRightFocus = false;
				if (mOnSeekBarChangeListener != null)
					mOnSeekBarChangeListener.onProgressEnd();
				break;
			}
			return true;
		}
	};

	/** 更新时间 */
	protected boolean updateTimes() {
		if (mVideoSelection.getCurrentWidth() > 0 && mPrepared) {
			int start = (int) ((mVideoSelection.getLeftMargin() + mVideoThumbnailsBackground.getScrollX()) * 1000F / mSecondWidth);
			int end = start + (int) ((mVideoSelection.getCurrentWidth()) * 1000.0F / mSecondWidth);
			if (mStartTime != start || mEndTime != end) {
				mStartTime = start;
				mEndTime = end;
				refreshTimes();
				return true;
			}
			// mEndTime = mStartTime + (int) ((mVideoSelection.getWidth() -
			// mSpace) * 1000.0F / SECOND_WIDTH);
		}
		return false;
	}

	protected final AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(12, true);

	/** 更新时间显示 */
	protected void refreshTimes() {
		int total = (mEndTime - mStartTime);
		if (total > mDuration)
			total = mDuration;
		else if (total < 3000)
			total = 3000;
		// String.format("%.1fs/%ss"
		mSelectionTimeView.setText(String.format("%s秒", gennerTimeSencond(mEndTime - mStartTime)));
		String text = mSelectionTimeView.getText().toString();
		int index = TextUtils.isEmpty(text) ? -1 : text.indexOf('/');
		if (index > -1) {
			SpannableStringBuilder nameStyle = new SpannableStringBuilder(text);
			nameStyle.setSpan(sizeSpan, index, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			mSelectionTimeView.setText(nameStyle);
		}
		mStartTimeView.setText(gennerTime(mStartTime));
		mEndTimeView.setText(gennerTime(mEndTime));
	}

	/** 格式化输出时间 */
	private static String gennerTime(int totalMillis) {
		int minutes = totalMillis / 60000;// 求分钟
		int seconds = (totalMillis / 1000) % 60;// 求秒
		int millis = totalMillis % 1000;
		return String.format("%02d:%02d.%d", minutes, seconds, millis / 100);
	}

	/** 格式化输出时间 */
	private String gennerTimeSencond(int totalMillis) {
		// int minutes = totalMillis / 60000;// 求分钟

		if (totalMillis > maxDuration) {
			totalMillis = maxDuration;
		}

		if (totalMillis > mDuration) {
			totalMillis = mDuration;
		}
		if (totalMillis < minDuration)
			totalMillis = minDuration;

		int seconds = (totalMillis / 1000);// 求秒
		int millis = totalMillis % 1000;

		return String.format("%d.%d", seconds, millis / 100);
		//		return String.format("%d", seconds);
	}

	/** 监听滑动底部图片 */
	@Override
	public void onFlingScrollChange() {
		if (mPrepared) {
			if (updateTimes()) {
				if (mFirstFlingScrollChange) {
					mFirstFlingScrollChange = false;
				} else {
					if (mOnSeekBarChangeListener != null) {
						mOnSeekBarChangeListener.onProgressChanged();
					}
					// if (mVideoTime != null) {
					// mVideoTime.setText(mContext.getString(R.string.left_second_tips,
					// getVideoCutTime()));
					// }
				}
				checkLoadThumb();
			}
		}
	}

	/** 滚过去 */
	public void seekTo(int position) {
		// 时间转距离坐标
		int x = (int) (position / 1000F * mSecondWidth);
		mVideoThumbnailsBackground.smoothScrollTo(x, 0);
	}

	public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
		this.mOnSeekBarChangeListener = l;
	}

	public void setOnSwich60sListener(OnSwich60sListener l) {
		this.mOnSwich60sListener = l;
	}

	public void setOnBackgroundColorListener(OnBackgroundColorListener onBackgroundColorListener) {
		this.mOnBackgroundColorListener = onBackgroundColorListener;
	}

	/** 检测是否需要加载新的图片 */
	public void checkLoadThumb() {
		// 更新图片
		if (mVideoThumbnails != null && !mThumbLoading) {
			final int scrollX = mVideoThumbnailsBackground.getScrollX();
			for (int i = 0; i < mVideoThumbnails.getChildCount(); i++) {

				if (i > 0 && i < mVideoThumbnails.getChildCount() - 1) {

					VideoThumbImageView v = (VideoThumbImageView) mVideoThumbnails.getChildAt(i);
					if (v != null) {
						int right = (int) (v.getThumbIndex() * mThumbWidth);
						int left = (int) (right - mThumbWidth);
						if (right < scrollX)
							continue;
						// Logger.e("[VideoThumbImageView]left:" + left +
						// ":right:" + right + " scrollY:" + scrollX + " " + new
						// File(v.getThumbPath()).getName());
						if (left > scrollX + mWindowWidth)
							break;

						if (v.needLoad()) {
							// 需要加载截图并且需要显示显示出来
							if (v.checkThumb()) {
								v.loadImage();
							} else {
								loadThumb(v.getThumbIndex(), v.getThumbPosition());
							}
						}
					}
				}

			}
		}
	}

	private void checkLoadThumb2() {
		// 更新图片
		if (mVideoThumbnails != null) {
			final int scrollX = mVideoThumbnailsBackground.getScrollX();
			for (int i = 0; i < mVideoThumbnails.getChildCount(); i++) {
				if (i > 0 && i < mVideoThumbnails.getChildCount() - 1) {
					VideoThumbImageView v = (VideoThumbImageView) mVideoThumbnails.getChildAt(i);
					if (v != null) {
						int right = (int) (v.getThumbIndex() * mThumbWidth);
						int left = (int) (right - mThumbWidth);
						if (right < scrollX)
							continue;
						// Logger.e("[VideoThumbImageView]left:" + left +
						// ":right:" + right + " scrollY:" + scrollX + " " + new
						// File(v.getThumbPath()).getName());
						if (left > scrollX + mWindowWidth)
							break;

						if (v.needLoad()) {
							// 需要加载截图并且需要显示显示出来
							if (v.checkThumb()) {
								v.loadImage();
							} else {
								// loadThumb(v.getThumbIndex(),
								// v.getThumbPosition());
							}
						}
					}
				}

			}
		}
	}

	/** 截图 */
	private static class Thumb {
		public boolean firstDraw;
		public String thumbPath;
		public int index;
		public int startTime;

		public Thumb(boolean firstDraw, String thumbPath, int index, int startTime) {
			this.firstDraw = firstDraw;
			this.thumbPath = thumbPath;
			this.index = index;
			this.startTime = startTime;
		}
	}

	/** 横向滚动事件监听 */
	public interface OnSeekBarChangeListener {
		void onProgressChanged();

		void onProgressEnd();
	}

	public interface OnSwich60sListener {
		void onChanged();
	}

	public interface OnBackgroundColorListener {
		void onChanged(boolean isWhiteBackground);
	}

	public final static int FIT_XY = 0;
	public final static int FIT_CENTER = 1;

	public interface OnVideoChangeScaleTypeListener {
		void onChanged(int scale);
	}

	/** 时间选区手势识别 */
	private static class VideoSelectionGestureListener extends SimpleOnGestureListener {

		private final WeakReference<VideoSelectionView> mView;

		public VideoSelectionGestureListener(VideoSelectionView view) {
			mView = new WeakReference<VideoSelectionView>(view);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX1, float distanceY1) {

			float mNewX = e2.getX();
			float mNewY = e2.getY();
			float mOldX = e1.getX();
			float mOldY = e1.getY();
			float distanceX = mNewX - mOldX;
			float distanceY = mNewY - mOldY;
			VideoSelectionView view = mView.get();
			if (view != null) {
				// if (Math.abs(mNewY - mOldY) > Math.abs(mNewX - mOldX)) {
				// } else {
				if (mNewX > mOldX) {
					// 从左至右
					view.onLeftToRight(Math.abs((int) distanceX));
				} else {
					// 从右至左
					view.onRightToLeft(Math.abs((int) distanceX));
				}
				if (view.mOnSeekBarChangeListener != null)
					view.mOnSeekBarChangeListener.onProgressChanged();

				// if (view.mVideoTime != null && view != null) {
				// view.mVideoTime.setText(view.mContext.getString(R.string.left_second_tips,
				// view.getVideoCutTime()));
				// }
				// }
			}
			//
			// mOldX = mNewX;
			// mOldY = mNewY;

			return super.onScroll(e1, e2, distanceX, distanceY);
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}
	}

	public void setVideoModeControllerLayoutVisibility(int visibility) {
		mChangeVideoviewMode.setVisibility(visibility);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		switch (buttonView.getId()) {
		case R.id.backgroud_white:
			if (isChecked) {
				mOnBackgroundColorListener.onChanged(true);
			}

			break;
		case R.id.backgroud_black:
			if (isChecked) {
				mOnBackgroundColorListener.onChanged(false);
			}

			break;
		case R.id.change_videoview_mode:
			try {
				if (isChecked) {
					mRadioGroupBackground.setVisibility(View.INVISIBLE);
					onVideoChangeScaleTypeListener.onChanged(FIT_XY);
				} else {
					mRadioGroupBackground.setVisibility(View.VISIBLE);
					onVideoChangeScaleTypeListener.onChanged(FIT_CENTER);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}

			break;

		default:
			break;
		}

	}
}

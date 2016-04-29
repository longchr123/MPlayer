package com.yixia.camera.demo.ui.record;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lcr.mplay.R;
import com.yixia.camera.demo.common.CommonIntentExtra;
import com.yixia.camera.demo.log.Logger;
import com.yixia.camera.demo.ui.BaseActivity;
import com.yixia.camera.demo.ui.record.views.ProgressView;
import com.yixia.videoeditor.adapter.UtilityAdapter;
import com.yixia.weibo.sdk.MediaRecorderBase;
import com.yixia.weibo.sdk.MediaRecorderNative;
import com.yixia.weibo.sdk.MediaRecorderSystem;
import com.yixia.weibo.sdk.VCamera;
import com.yixia.weibo.sdk.model.MediaObject;
import com.yixia.weibo.sdk.model.MediaObject$MediaPart;
import com.yixia.weibo.sdk.util.ConvertToUtils;
import com.yixia.weibo.sdk.util.DeviceUtils;
import com.yixia.weibo.sdk.util.FileUtils;

/**
 * 视频录制
 * 
 * @author yixia.com
 *
 */
public class MediaRecorderActivity extends BaseActivity implements 
		com.yixia.weibo.sdk.MediaRecorderBase$OnErrorListener, OnClickListener, 
		com.yixia.weibo.sdk.MediaRecorderBase$OnPreparedListener, 
		com.yixia.weibo.sdk.MediaRecorderBase$OnEncodeListener {

	/** 录制最长时间 */
	public final static int RECORD_TIME_MAX = 10 * 1000;
	/** 录制最小时间 */
	public final static int RECORD_TIME_MIN = 3 * 1000;
	/** 刷新进度条 */
	private static final int HANDLE_INVALIDATE_PROGRESS = 0;
	/** 延迟拍摄停止 */
	private static final int HANDLE_STOP_RECORD = 1;
	/** 对焦 */
	private static final int HANDLE_HIDE_RECORD_FOCUS = 2;

	/** 下一步 */
	private ImageView mTitleNext;
	/** 对焦图标-带动画效果 */
	private ImageView mFocusImage;
	/** 前后摄像头切换 */
	private CheckBox mCameraSwitch;
	/** 回删按钮、延时按钮、滤镜按钮 */
	private CheckedTextView mRecordDelete;
	/** 闪光灯 */
	private CheckBox mRecordLed;
	/** 拍摄按钮 */
	private ImageView mRecordController;
	/** 导入视频*/
	private ImageView mImportVideo;

	/** 底部条 */
	private RelativeLayout mBottomLayout;
	/** 摄像头数据显示画布 */
	private SurfaceView mSurfaceView;
	/** 录制进度 */
	private ProgressView mProgressView;
	/** 对焦动画 */
	private Animation mFocusAnimation;

	/** SDK视频录制对象 */
	private MediaRecorderBase mMediaRecorder;
	/** 视频信息 */
	private MediaObject mMediaObject;

	/** 需要重新编译（拍摄新的或者回删） */
	private boolean mRebuild;
	/** on */
	private boolean mCreated;
	/** 是否是点击状态 */
	private volatile boolean mPressedStatus;
	/** 是否已经释放 */
	private volatile boolean mReleased;
	/** 对焦图片宽度 */
	private int mFocusWidth;
	/** 底部背景色 */
	private int mBackgroundColorNormal, mBackgroundColorPress;
	/** 屏幕宽度 */
	private int mWindowWidth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mCreated = false;
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 防止锁屏
		loadIntent();
		loadViews();
		mCreated = true;

	}

	/** 加载传入的参数 */
	private void loadIntent() {
		mWindowWidth = DeviceUtils.getScreenWidth(this);

		mFocusWidth = ConvertToUtils.dipToPX(this, 64);
		mBackgroundColorNormal = getResources().getColor(R.color.black);//camera_bottom_bg
		mBackgroundColorPress = getResources().getColor(R.color.camera_bottom_press_bg);
	}

	/** 加载视图 */
	private void loadViews() {
		setContentView(R.layout.activity_media_recorder);
		// ~~~ 绑定控件
		mSurfaceView = (SurfaceView) findViewById(R.id.record_preview);
		mCameraSwitch = (CheckBox) findViewById(R.id.record_camera_switcher);
		mTitleNext = (ImageView) findViewById(R.id.title_next);
		mFocusImage = (ImageView) findViewById(R.id.record_focusing);
		mProgressView = (ProgressView) findViewById(R.id.record_progress);
		mRecordDelete = (CheckedTextView) findViewById(R.id.record_delete);
		mRecordController = (ImageView) findViewById(R.id.record_controller);
		mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
		mRecordLed = (CheckBox) findViewById(R.id.record_camera_led);
		mImportVideo = (ImageView) findViewById(R.id.importVideo_btn);
		mImportVideo.setOnClickListener(this);
		// ~~~ 绑定事件
		if (DeviceUtils.hasICS()) {
			mSurfaceView.setOnTouchListener(mOnSurfaveViewTouchListener);
		}

		mTitleNext.setOnClickListener(this);
		findViewById(R.id.title_back).setOnClickListener(this);
		mRecordDelete.setOnClickListener(this);
		mBottomLayout.setOnTouchListener(mOnVideoControllerTouchListener);

		// ~~~ 设置数据

		//是否支持前置摄像头
		if (MediaRecorderBase.isSupportFrontCamera()) {
			mCameraSwitch.setOnClickListener(this);
		} else {
			mCameraSwitch.setVisibility(View.GONE);
		}
		//是否支持闪光灯
		if (DeviceUtils.isSupportCameraLedFlash(getPackageManager())) {
			mRecordLed.setOnClickListener(this);
		} else {
			mRecordLed.setVisibility(View.GONE);
		}

		try {
			mFocusImage.setImageResource(R.drawable.video_focus);
			//			mFocusImage.setVisibility(View.VISIBLE);
		} catch (OutOfMemoryError e) {
			Logger.e(e);
		}

		mProgressView.setMaxDuration(RECORD_TIME_MAX);
		initSurfaceView();
	}

	/** 初始化画布 */
	private void initSurfaceView() {
		final int w = DeviceUtils.getScreenWidth(this);
		// 底部工具栏距顶部高度为屏幕宽度 ，这样就保证了上面的区域是一个正方形
		((RelativeLayout.LayoutParams) mBottomLayout.getLayoutParams()).topMargin = w;
		int width = w;
		int height = w * 4 / 3;
		//
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
		lp.width = width;
		lp.height = height;
		mSurfaceView.setLayoutParams(lp);
	}

	/** 初始化拍摄SDK */
	private void initMediaRecorder() {
		mMediaRecorder = new MediaRecorderNative();
		mRebuild = true;

		mMediaRecorder.setOnErrorListener(this);
		mMediaRecorder.setOnEncodeListener(this);
		File f = new File(VCamera.getVideoCachePath());
		if (!FileUtils.checkFile(f)) {
			f.mkdirs();
		}
		String key = String.valueOf(System.currentTimeMillis());
		mMediaObject = mMediaRecorder.setOutputDirectory(key, VCamera.getVideoCachePath() + key);
		mMediaRecorder.setOnSurfaveViewTouchListener(mSurfaceView);
		mMediaRecorder.setSurfaceHolder(mSurfaceView.getHolder());
		mMediaRecorder.prepare();
	}

	/** 点击屏幕对焦 */
	private View.OnTouchListener mOnSurfaveViewTouchListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (mMediaRecorder == null || !mCreated) {
				return false;
			}

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// 检测是否手动对焦
				showFocusImage(event);
				if (!mMediaRecorder.onTouch(event, new AutoFocusCallback() {
					@Override
					public void onAutoFocus(boolean success, Camera camera) {
						mFocusImage.setVisibility(View.GONE);

					}
				})) {
					return true;
				} else {
					mFocusImage.setVisibility(View.GONE);
				}
				mMediaRecorder.setAutoFocus();
				break;
			}
			return true;
		}

	};

	/** 点击屏幕录制 */
	private View.OnTouchListener mOnVideoControllerTouchListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (mMediaRecorder == null) {
				return false;
			}

			//			Logger.e("[MediaRecorderActivity]event.getAction() " + event.getAction());

			switch (event.getAction()) {
			//				case MotionEvent.ACTION_MOVE:
			//
			//					if (mMediaObject.getDuration() >= RECORD_TIME_MAX) {
			//						stopRecord();
			//						mTitleNext.performClick();
			//					}
			//					break;

			case MotionEvent.ACTION_DOWN:
				// 如果视频时间已经超过了最大视频时间的话，那么就表示视频已录制完成，返回true
				// 如果正处在回删状态的话，点击取消回删，返回true
				// 否则就是处于准备录的状态，点击一下，开始录视频
				
				//检测是否手动对焦
				//判断是否已经超时
				//					Logger.e("[MediaRecorderActivity]mMediaObject.getDuration() " + mMediaObject.getDuration());
				if (mMediaObject.getDuration() >= RECORD_TIME_MAX) {
					return true;
				}

				//取消回删
				if (cancelDelete())
					return true;

				startRecord();

				break;

			case MotionEvent.ACTION_UP:
				// 暂停
				if (mPressedStatus) {
					stopRecord();

					//检测是否已经完成
					if (mMediaObject.getDuration() >= RECORD_TIME_MAX) {
						mTitleNext.performClick();
					}
				}
				break;
			}
			return true;
		}

	};

	@Override
	public void onResume() {
		super.onResume();
		UtilityAdapter.freeFilterParser();
		UtilityAdapter.initFilterParser();

		if (mMediaRecorder == null) {
			initMediaRecorder();
		} else {
			// 关闭闪光灯
			mRecordLed.setChecked(false);
			mMediaRecorder.prepare();
			// 设置进度条的状态
			mProgressView.setData(mMediaObject);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		stopRecord();
		UtilityAdapter.freeFilterParser();
		if (!mReleased) {
			if (mMediaRecorder != null)
				mMediaRecorder.release();
		}
		mReleased = false;
	}

	/** 手动对焦 */
	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private boolean checkCameraFocus(MotionEvent event) {
		mFocusImage.setVisibility(View.GONE);
		float x = event.getX();
		float y = event.getY();
		float touchMajor = event.getTouchMajor();
		float touchMinor = event.getTouchMinor();

		Logger.e("touchMajor = " + touchMajor);
		Logger.e("touchMinor = " + touchMinor);

		Rect touchRect = new Rect((int) (x - touchMajor / 2), (int) (y - touchMinor / 2), (int) (x + touchMajor / 2), (int) (y + touchMinor / 2));
		//The direction is relative to the sensor orientation, that is, what the sensor sees. The direction is not affected by the rotation or mirroring of setDisplayOrientation(int). Coordinates of the rectangle range from -1000 to 1000. (-1000, -1000) is the upper left point. (1000, 1000) is the lower right point. The width and height of focus areas cannot be 0 or negative.
		//No matter what the zoom level is, (-1000,-1000) represents the top of the currently visible camera frame

		Logger.e("touchRect = " + touchRect);
		Logger.e("mWindowWidth = " + mWindowWidth);

		if (touchRect.right > 1000)
			touchRect.right = 1000;
		if (touchRect.bottom > 1000)
			touchRect.bottom = 1000;
		if (touchRect.left < 0)
			touchRect.left = 0;
		if (touchRect.right < 0)
			touchRect.right = 0;

		if (touchRect.left >= touchRect.right || touchRect.top >= touchRect.bottom)
			return false;

		ArrayList<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
		focusAreas.add(new Camera.Area(touchRect, 1000));
		if (!mMediaRecorder.manualFocus(new Camera.AutoFocusCallback() {

			@Override
			public void onAutoFocus(boolean success, Camera camera) {
				//				if (success) {
				mFocusImage.setVisibility(View.GONE);
				//				}
			}
		}, focusAreas)) {
			mFocusImage.setVisibility(View.GONE);
		}

		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mFocusImage.getLayoutParams();
		int left = touchRect.left - (mFocusWidth / 2);//(int) x - (focusingImage.getWidth() / 2);
		int top = touchRect.top - (mFocusWidth / 2);//(int) y - (focusingImage.getHeight() / 2);
		if (left < 0)
			left = 0;
		else if (left + mFocusWidth > mWindowWidth)
			left = mWindowWidth - mFocusWidth;
		if (top + mFocusWidth > mWindowWidth)
			top = mWindowWidth - mFocusWidth;

		lp.leftMargin = left;
		lp.topMargin = top;
		mFocusImage.setLayoutParams(lp);
		mFocusImage.setVisibility(View.VISIBLE);

		if (mFocusAnimation == null)
			mFocusAnimation = AnimationUtils.loadAnimation(this, R.anim.record_focus);

		mFocusImage.startAnimation(mFocusAnimation);

		mHandler.sendEmptyMessageDelayed(HANDLE_HIDE_RECORD_FOCUS, 3500);//最多3.5秒也要消失
		return true;
	}

	/** 显示对焦图片 */
	private void showFocusImage(MotionEvent e) {

		int x = Math.round(e.getX());
		int y = Math.round(e.getY());
		int focusWidth = 100;
		int focusHeight = 100;
		int previewWidth = mSurfaceView.getWidth();
		Rect touchRect = new Rect();

		mMediaRecorder.calculateTapArea(focusWidth, focusHeight, 1f, x, y, previewWidth, previewWidth, touchRect);

		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mFocusImage.getLayoutParams();
		int left = touchRect.left - (mFocusWidth / 2);//(int) x - (focusingImage.getWidth() / 2);
		int top = touchRect.top - (mFocusWidth / 2);//(int) y - (focusingImage.getHeight() / 2);
		if (left < 0)
			left = 0;
		else if (left + mFocusWidth > mWindowWidth)
			left = mWindowWidth - mFocusWidth;
		if (top + mFocusWidth > mWindowWidth)
			top = mWindowWidth - mFocusWidth;

		lp.leftMargin = left;
		lp.topMargin = top;

		Logger.e("left =  " + left);
		Logger.e("top =  " + top);

		mFocusImage.setLayoutParams(lp);
		mFocusImage.setVisibility(View.VISIBLE);

		if (mFocusAnimation == null)
			mFocusAnimation = AnimationUtils.loadAnimation(this, R.anim.record_focus);

		mFocusImage.startAnimation(mFocusAnimation);

		mHandler.sendEmptyMessageDelayed(HANDLE_HIDE_RECORD_FOCUS, 3500);//最多3.5秒也要消失
	}

	/** 开始录制 */
	private void startRecord() {
		if (mMediaRecorder != null) {
			com.yixia.weibo.sdk.model.MediaObject$MediaPart part = mMediaRecorder.startRecord();
			if (part == null) {
				return;
			}

			//如果使用MediaRecorderSystem，不能在中途切换前后摄像头，否则有问题
			if (mMediaRecorder instanceof MediaRecorderSystem) {
				mCameraSwitch.setVisibility(View.GONE);
			}
			mProgressView.setData(mMediaObject);
		}

		mRebuild = true;
		mPressedStatus = true;
		mRecordController.setImageResource(R.drawable.record_controller_press);
		mBottomLayout.setBackgroundColor(mBackgroundColorPress);

		if (mHandler != null) {
			mHandler.removeMessages(HANDLE_INVALIDATE_PROGRESS);
			mHandler.sendEmptyMessage(HANDLE_INVALIDATE_PROGRESS);

			mHandler.removeMessages(HANDLE_STOP_RECORD);
			mHandler.sendEmptyMessageDelayed(HANDLE_STOP_RECORD, RECORD_TIME_MAX - mMediaObject.getDuration());
		}
		mRecordDelete.setVisibility(View.GONE);
		mCameraSwitch.setEnabled(false);
		mRecordLed.setEnabled(false);
	}

	@Override
	public void onBackPressed() {
		// 如果处于回删状态的话，点击取消回删
		if (mRecordDelete != null && mRecordDelete.isChecked()) {
			cancelDelete();
			return;
		}

		if (mMediaObject != null && mMediaObject.getDuration() > 1) {
			// 未转码
			new AlertDialog.Builder(this)
					.setTitle(R.string.hint)
					.setMessage(R.string.record_camera_exit_dialog_message)
					.setNegativeButton(
							R.string.record_camera_cancel_dialog_yes,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mMediaObject.delete();
									finish();
								}

							})
					.setPositiveButton(R.string.record_camera_cancel_dialog_no,
							null).setCancelable(false).show();
			return;
		}
		// 如果没有录制视频的话，直接退出
		
		//停止消息推送轮询
		VCamera.stopPollingService();
		finish();
	}

	/** 停止录制 */
	private void stopRecord() {
		
		mPressedStatus = false;
		mRecordController.setImageResource(R.drawable.record_controller_normal);
		mBottomLayout.setBackgroundColor(mBackgroundColorNormal);

		if (mMediaRecorder != null) {
			mMediaRecorder.stopRecord();
		}

		mRecordDelete.setVisibility(View.VISIBLE);
		mCameraSwitch.setEnabled(true);
		mRecordLed.setEnabled(true);

		mHandler.removeMessages(HANDLE_STOP_RECORD);
		checkStatus();
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if (mHandler.hasMessages(HANDLE_STOP_RECORD)) {
			mHandler.removeMessages(HANDLE_STOP_RECORD);
		}

		//处理开启回删后其他点击操作
		if (id != R.id.record_delete) {
			if (mMediaObject != null) {
				com.yixia.weibo.sdk.model.MediaObject$MediaPart part = mMediaObject.getCurrentPart();
				if (part != null) {
					if (part.remove) {
						part.remove = false;
						mRecordDelete.setChecked(false);
						if (mProgressView != null)
							mProgressView.invalidate();
					}
				}
			}
		}

		switch (id) {
		case R.id.title_back:
			onBackPressed();
			break;
		case R.id.record_camera_switcher:// 前后摄像头切换
			if (mRecordLed.isChecked()) {
				if (mMediaRecorder != null) {
					mMediaRecorder.toggleFlashMode();
				}
				mRecordLed.setChecked(false);
			}

			if (mMediaRecorder != null) {
				mMediaRecorder.switchCamera();
			}

			if (mMediaRecorder.isFrontCamera()) {
				mRecordLed.setEnabled(false);
			} else {
				mRecordLed.setEnabled(true);
			}
			break;
		case R.id.record_camera_led://闪光灯
			//开启前置摄像头以后不支持开启闪光灯
			if (mMediaRecorder != null) {
				if (mMediaRecorder.isFrontCamera()) {
					return;
				}
			}

			if (mMediaRecorder != null) {
				mMediaRecorder.toggleFlashMode();
			}
			break;
		case R.id.title_next:// 停止录制
			mMediaRecorder.startEncoding();
			break;
		case R.id.record_delete:
			//取消回删
			if (mMediaObject != null) {
				com.yixia.weibo.sdk.model.MediaObject$MediaPart part = mMediaObject.getCurrentPart();
				if (part != null) {
					if (part.remove) {
						mRebuild = true;
						part.remove = false;
						backRemove();
						mRecordDelete.setChecked(false);
					} else {
						part.remove = true;
						mRecordDelete.setChecked(true);
					}
				}
				if (mProgressView != null)
					mProgressView.invalidate();
				//检测按钮状态
				checkStatus();
			}
			break;
		case R.id.importVideo_btn:
			startActivity(new Intent(MediaRecorderActivity.this, ImportVideoFolderActivity.class));
			break;
		}
	}

	/** 回删 */
	public boolean backRemove() {
		if (mMediaObject != null && mMediaObject.mediaList != null) {
			int size = mMediaObject.mediaList.size();
			if (size > 0) {
				com.yixia.weibo.sdk.model.MediaObject$MediaPart part = (MediaObject$MediaPart) mMediaObject.mediaList
						.get(size - 1);
				mMediaObject.removePart(part, true);

				if (mMediaObject.mediaList.size() > 0)
					mMediaObject.mCurrentPart = (MediaObject$MediaPart) mMediaObject.mediaList
							.get(mMediaObject.mediaList.size() - 1);
				else
					mMediaObject.mCurrentPart = null;
				return true;
			}
		}
		return false;
	}

	/** 取消回删 */
	private boolean cancelDelete() {
		if (mMediaObject != null) {
			MediaObject$MediaPart part = mMediaObject.getCurrentPart();
			if (part != null && part.remove) {
				part.remove = false;
				mRecordDelete.setChecked(false);

				if (mProgressView != null)
					mProgressView.invalidate();

				return true;
			}
		}
		return false;
	}

	/** 检查录制时间，显示/隐藏下一步按钮   显示/隐藏回删按钮*/
	private int checkStatus() {
		int duration = 0;
		if (!isFinishing() && mMediaObject != null) {
			duration = mMediaObject.getDuration();
			if (duration < RECORD_TIME_MIN) {
				if (duration == 0) {
					mCameraSwitch.setVisibility(View.VISIBLE);
					mRecordDelete.setVisibility(View.GONE);
				}
				//视频必须大于3秒
				if (mTitleNext.getVisibility() != View.INVISIBLE)
					mTitleNext.setVisibility(View.INVISIBLE);
			} else {
				//下一步
				if (mTitleNext.getVisibility() != View.VISIBLE) {
					mTitleNext.setVisibility(View.VISIBLE);
				}
			}
		}
		return duration;
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_STOP_RECORD:
				stopRecord();
				mTitleNext.performClick();
				break;
			case HANDLE_INVALIDATE_PROGRESS:
				if (mMediaRecorder != null && !isFinishing()) {
					if (mProgressView != null)
						mProgressView.invalidate();
					//					if (mPressedStatus)
					//						titleText.setText(String.format("%.1f", mMediaRecorder.getDuration() / 1000F));
					if (mPressedStatus)
						sendEmptyMessageDelayed(0, 30);
				}
				break;
			}
		}
	};

	@Override
	public void onEncodeStart() {
		showProgress("", getString(R.string.record_camera_progress_message));
	}

	@Override
	public void onEncodeProgress(int progress) {
		//		Logger.e("[MediaRecorderActivity]onEncodeProgress..." + progress);
	}

	/** 转码完成 */
	@Override
	public void onEncodeComplete() {
		hideProgress();
		Intent intent = new Intent(this, MediaPreviewActivity.class);
		Bundle bundle = getIntent().getExtras();
		if (bundle == null)
			bundle = new Bundle();
		bundle.putSerializable(CommonIntentExtra.EXTRA_MEDIA_OBJECT, mMediaObject);
		bundle.putString("output", mMediaObject.getOutputTempVideoPath());
		bundle.putBoolean("Rebuild", mRebuild);
		intent.putExtras(bundle);
		startActivity(intent);
		mRebuild = false;
	}
	
	/**
	 * 转码失败
	 * 	检查sdcard是否可用，检查分块是否存在
	 */
	@Override
	public void onEncodeError() {
		hideProgress();
		Toast.makeText(this, R.string.record_video_transcoding_faild, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onVideoError(int what, int extra) {

	}

	@Override
	public void onAudioError(int what, String message) {

	}

	@Override
	public void onPrepared() {

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}

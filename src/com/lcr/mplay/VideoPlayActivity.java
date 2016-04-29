package com.lcr.mplay;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.lcr.bean.VideoInfo;
import com.lcr.config.Config;
import com.lcr.utils.DisplayUtil;
import com.lcr.utils.FormatUtils;
import com.lcr.widget.VideoView;

public class VideoPlayActivity extends BaseActivity {

	private VideoView mVideoView;
	private Uri uri;
	private TextView mTitleVideoTextView;
	private TextView mSystemTimeTextView;
	private ImageView mBatteryView;

	private Button mVoiceButton;
	private SeekBar mVoiceSeekBar;
	private Button mSwitchButton;

	private TextView mCurrentTimeTextView;
	private SeekBar mVideoSeekBar;
	private TextView mDurationTextView;

	private Button mExitButton;
	private Button mPreButton;
	private Button mPlayButton;
	private Button mNextButton;
	private Button mScreenButton;

	private LinearLayout mControlLinearLayout;

	private boolean isPlaying;
	private boolean isShowControl;
	private boolean isFullScreen;
	private boolean isMute;// ��ǰ�Ƿ���,trueΪ��
	private boolean isNetUri;// �Ƿ���������Դ

	private FormatUtils utils;

	private final int PROGRESS = 1;// ���½���
	private final int DELAYED_HIDECONTROL = 2;// ��ʱ���ؿ������
	private final int FULL_SCREEN = 3;// ȫ��
	private final int DEFAULT_SCREEN = 4;// Ĭ����Ļ
	private final int FINISH = 5;// �رյ�ǰ������
	private final int SCREEN_CHANGE = 6;

	private boolean isDestroyed;// �жϵ�ǰActivity�Ƿ�����
	private BatteryBroadcastReceiver mBatteryBroadcastReceiver;
	private int level;// ����0-100
	private WindowManager wm;
	private AudioManager am;// ����������С
	private int currentVolume;
	private int maxVolume;// �������,һ����0-15

	private List<VideoInfo> items;
	private int position;// ��Ƶ�б��λ��
	private GestureDetector detector;

	private LinearLayout mLoadingProgressBar;
	private LayoutParams mLayoutParams;

	private Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case PROGRESS:
				// ��ֹ���л�������ʱ���ֿ�ָ��
				if (mVideoView != null) {
					// �õ���Ƶ �ĵ�ǰ���Ž���
					int currentPosition = mVideoView.getCurrentPosition();
//					mVideoView.seekTo(currentPosition+1);
//					currentPosition = mVideoView.getCurrentPosition();
					mCurrentTimeTextView.setText(utils
							.stringForTime(currentPosition));
					// 2.SeekBar���ȸ���
					mVideoSeekBar.setProgress(currentPosition);

					// ��Ϣ��ѭ��,
					if (!isDestroyed) {
						handler.removeMessages(PROGRESS);// ����������д��룬����һ��Ϊhandler.sendEmptyMessage(PROGRESS);�ֻ��Ῠ���������Ҫ����ˢ�¾�Ҫȥ�����д��롣����
						handler.sendEmptyMessageDelayed(PROGRESS, 100);
					}

					// ���õ����ı仯
					setBattery();

					// ������ʾ��ǰ�ֻ���ʱ��
					mSystemTimeTextView.setText(utils.getSystemTime());

					// ������״̬�²����û������
					if (isNetUri) {
						// ���û������0-100
						int percentage = mVideoView.getBufferPercentage();
						int total = percentage * (mVideoSeekBar.getMax());
						int secondaryProgress = total / 100;// �������ֵ
						mVideoSeekBar.setSecondaryProgress(secondaryProgress);
					} else {
						mVideoSeekBar.setSecondaryProgress(0);
					}

				}
				break;

			case DELAYED_HIDECONTROL:
				hideControl();
				break;

			case FINISH:// �رյ�ǰҳ��
				if (mVideoView != null) {
					mVideoView = null;
				}
				finish();
				break;

			case SCREEN_CHANGE:
				setContentView();
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitleBar(View.GONE);
		initViews();
		getData();
		initBattery();
		setData();
		setListener();
	}

	private void setData() {
		if (items != null && items.size() > 0) {
			VideoInfo item = items.get(position);
			// �߼���VideoView����ַ�����ײ�ȥ���룬�ٴ�����
			mVideoView.setVideoPath(item.getUrl().replace("localhost", Config.localhost));
			isNetUri = utils.isNetUri(item.getUrl().replace("localhost", Config.localhost));
			mTitleVideoTextView.setText(item.getTitle());
		} else if (uri != null) {
			// �߼���VideoView����ַ�����ײ�ȥ���룬�ٴ�����
			mVideoView.setVideoURI(uri);
			isNetUri = utils.isNetUri(uri.toString());
			mTitleVideoTextView.setText(uri.toString());
			// �ӵ�����������ô����ʱ��������һ������һ�����ɵ��
			mPreButton.setBackgroundResource(R.drawable.btn_back_no_enable);
			mPreButton.setEnabled(false);
			mNextButton
					.setBackgroundResource(R.drawable.btn_forward_not_enable);
			mNextButton.setEnabled(false);
		}
	}

	// ��ô��������
	@SuppressWarnings("unchecked")
	private void getData() {

		items = getIntent().getParcelableArrayListExtra("mVideoInfos");
		position = getIntent().getIntExtra("position", 0);
		// �õ����ŵ�ַ-���Ե��������-�ļ��й������������������QQ�ռ�ȡ�
		uri = getIntent().getData();

	}

	/**
	 * ���������仯
	 */
	private void initBattery() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		mBatteryBroadcastReceiver = new BatteryBroadcastReceiver();
		registerReceiver(mBatteryBroadcastReceiver, filter);
	}

	private class BatteryBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// �õ�������ֵ0-100
			level = intent.getIntExtra("level", 0);
		}
	}

	/**
	 * ���õ����仯
	 */
	private void setBattery() {
		if (level <= 0) {
			mBatteryView.setImageResource(R.drawable.ic_battery_0);
		} else if (level <= 10) {
			mBatteryView.setImageResource(R.drawable.ic_battery_10);
		} else if (level <= 20) {
			mBatteryView.setImageResource(R.drawable.ic_battery_20);
		} else if (level <= 40) {
			mBatteryView.setImageResource(R.drawable.ic_battery_40);
		} else if (level <= 60) {
			mBatteryView.setImageResource(R.drawable.ic_battery_60);
		} else if (level <= 80) {
			mBatteryView.setImageResource(R.drawable.ic_battery_80);
		} else if (level <= 100) {
			mBatteryView.setImageResource(R.drawable.ic_battery_100);
		}
	};

	private void setListener() {

		mPlayButton.setOnClickListener(mOnClickListener);
		mNextButton.setOnClickListener(mOnClickListener);
		mPreButton.setOnClickListener(mOnClickListener);
		mVoiceButton.setOnClickListener(mOnClickListener);
		mSwitchButton.setOnClickListener(mOnClickListener);
		mExitButton.setOnClickListener(mOnClickListener);
		mScreenButton.setOnClickListener(mOnClickListener);

		// ������Ƶ�Ƿ�׼������-��ʼ����
		mVideoView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				mVideoView.start();
				isPlaying = true;
				// �õ���Ƶ��ʱ��
				int duration = mVideoView.getDuration();
				mDurationTextView.setText(utils.stringForTime(duration));
				// 1.��Ƶ����ʱ��Ҫ����SeekBar
				mVideoSeekBar.setMax(duration);

				// һ������������ؿ������
				hideControl();

				// ���ؼ���Ч��
				mLoadingProgressBar.setVisibility(View.GONE);

				// ��ʼ���½���
				handler.sendEmptyMessage(PROGRESS);
			}
		});

		// ����������Ƶʱ�������󲢽��д���
		mVideoView.setOnInfoListener(new OnInfoListener() {

			@Override
			public boolean onInfo(MediaPlayer mp, int what, int extra) {
				switch (what) {
				case MediaPlayer.MEDIA_INFO_BUFFERING_START:// ����
					mLoadingProgressBar.setVisibility(View.VISIBLE);
					break;

				// ��һ�������������״�����͵��Զ���VideoViewʵ��MediaPlayer��setOnSeekCompleteListener()�ӿڡ�
				case MediaPlayer.MEDIA_INFO_BUFFERING_END:// ��������
					mLoadingProgressBar.setVisibility(View.GONE);
					break;

				default:
					break;
				}
				return true;
			}
		});

		// ��������Ƶ�������֮��ʱ�Ķ��������Բ�����һ����Ƶ��Ҳ�����ز�
		mVideoView.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				playNextVideo();
			}
		});

		/*
		 * ��Ƶ���ų�������Щԭ�� 1.��ʽ��֧�֣�---���ܲ����� 2.����---�ز� 3.���������пհ�---�޸�
		 * 4.������һ��������Ƶ���ļ���
		 */
		mVideoView.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				startVitamioPlayer();

				return true;
			}
		});

		// ����ϵͳ�Դ��Ŀ�����
		// mVideoView.setMediaController(new MediaController(this));

		// ����SeekBar�϶���Ƶ�ļ���
		mVideoSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			// ��ָ�뿪�ؼ�ʱ
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				sendDelayedhideControlPlayer();
			}

			// ��ָ��ʼ�϶�ʱ
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// �������ؿ������
				removeDelayedhideControlPlayer();
			}

			// ��״̬�����ı�ʱ 1.����2.seekBarλ�ã���Ƶ������seekBar����һһ��Ӧ,3.��ָ����ʱΪtrue
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					mVideoView.seekTo(progress);
				}
			}
		});

	}

	/**
	 * �������ܲ�����
	 */
	protected void startVitamioPlayer() {
		// VideoInfo item=items.get(position);
		Intent intent = new Intent(VideoPlayActivity.this,
				VitamioPlayActivity.class);
		Bundle extras = new Bundle();
		intent.putExtra("position", position);
//		extras.putSerializable("videlist", (Serializable) items);
		extras.putParcelableArrayList("videlist", (ArrayList<VideoInfo>)items);
		intent.putExtras(extras);
		intent.setData(uri);// һ��Ҫ��
		startActivity(intent);
		// �رյ�ǰActivity--��������Ҫ��ʱ�������ϵͳ������
		handler.sendEmptyMessageDelayed(FINISH, 2000);
	}

	OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			removeDelayedhideControlPlayer();
			sendDelayedhideControlPlayer();
			switch (v.getId()) {
			case R.id.btn_play:
				startOrPause();
				break;

			case R.id.btn_next:
				playNextVideo();
				break;

			case R.id.btn_pre:
				playPreVideo();
				break;
			case R.id.btn_voice:
				isMute = !isMute;
				updateVolume(currentVolume);
				break;
			case R.id.btn_switch:
				AlertDialog.Builder builder = new AlertDialog.Builder(
						VideoPlayActivity.this).setMessage(
						"当前是系统播放器，是否切换至万能播放器").setNegativeButton("否", null);
				builder.setPositiveButton("确定",
						new AlertDialog.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								startVitamioPlayer();
							}
						}).setCancelable(false).show();
				break;

			case R.id.btn_exit:
				handler.sendEmptyMessageDelayed(FINISH, 2000);
				break;

			case R.id.btn_screen:
				if (!isFullScreen) {
					VideoPlayActivity.this
							.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				} else {
					VideoPlayActivity.this
							.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
				break;
			default:
				break;
			}
		}

	};
	private int screenHeight;
	private int screenWidth;

	private void initViews() {
		// ���õ�ǰ������Ƶ����Ƶ������
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		isDestroyed = false;
		mVideoView = (VideoView) findViewById(R.id.videoview);
		mTitleVideoTextView = (TextView) findViewById(R.id.tv_video_title);
		mSystemTimeTextView = (TextView) findViewById(R.id.tv_systemtime);
		mBatteryView = (ImageView) findViewById(R.id.iv_battery);
		mVoiceButton = (Button) findViewById(R.id.btn_voice);
		mVoiceSeekBar = (SeekBar) findViewById(R.id.sb_voice);
		mSwitchButton = (Button) findViewById(R.id.btn_switch);

		mCurrentTimeTextView = (TextView) findViewById(R.id.tv_crrent_time);
		mVideoSeekBar = (SeekBar) findViewById(R.id.sb_video);
		mDurationTextView = (TextView) findViewById(R.id.tv_duration);

		mExitButton = (Button) findViewById(R.id.btn_exit);
		mPreButton = (Button) findViewById(R.id.btn_pre);
		mPlayButton = (Button) findViewById(R.id.btn_play);
		mNextButton = (Button) findViewById(R.id.btn_next);
		mScreenButton = (Button) findViewById(R.id.btn_screen);

		mControlLinearLayout = (LinearLayout) findViewById(R.id.ll_control_play);

		mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		mLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

		wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		screenHeight = wm.getDefaultDisplay().getHeight();
		screenWidth = wm.getDefaultDisplay().getWidth();

		utils = new FormatUtils();
		mLoadingProgressBar = (LinearLayout) findViewById(R.id.ll_loading);

		detector = new GestureDetector(this,
				new GestureDetector.SimpleOnGestureListener() {

					@Override
					public void onLongPress(MotionEvent e) {
						startOrPause();
						super.onLongPress(e);
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						if (isFullScreen) {
							setVideoType(DEFAULT_SCREEN);
							isFullScreen = false;
						} else {
							setVideoType(FULL_SCREEN);
							isFullScreen = true;
						}

						return super.onDoubleTap(e);
					}

					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						if (isShowControl) {
							// ���Ƴ���Ϣ
							removeDelayedhideControlPlayer();
							hideControl();
						} else {
							showControl();
							// ����ָ̧��ʱ������һ����Ϣ�������ؿ������
							sendDelayedhideControlPlayer();
						}
						return super.onSingleTapConfirmed(e);
					}
				});

		am = (AudioManager) getSystemService(AUDIO_SERVICE);
		currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		mVoiceSeekBar.setMax(maxVolume);
		mVoiceSeekBar.setProgress(currentVolume);
		mVoiceSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				sendDelayedhideControlPlayer();
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				removeDelayedhideControlPlayer();
			}

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				if (arg2) {
					updateVolume(arg1);
				}
			}
		});
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		LayoutParams controlParams = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		LayoutParams vParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
				DisplayUtil.dip2px(this, 230));
		vParams.topMargin=DisplayUtil.dip2px(this, 20);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			mControlLinearLayout.setLayoutParams(mLayoutParams);
			mVideoView.setLayoutParams(mLayoutParams);
			mScreenButton
					.setBackgroundResource(R.drawable.menubar_fullscreen_exit);
			isFullScreen=true;
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			controlParams.height = DisplayUtil.dip2px(VideoPlayActivity.this,
					220);
			mControlLinearLayout.setLayoutParams(controlParams);
			mVideoView.setLayoutParams(vParams);
			mScreenButton
					.setBackgroundResource(R.drawable.menubar_fullscreen_enter);
			isFullScreen=false;
		}
	}

	/**
	 * ���������ķ���:һ���ǣ�0-15 arg1��Ҫ���ڵ�������
	 */
	protected void updateVolume(int arg1) {
		if (isMute) {// ����
			// ��3Ϊ1Ҳͬʱ�����ϵͳ��������ͼƬ��0����
			am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
			mVoiceSeekBar.setProgress(0);
		} else if (changeVolem) {
			// ��3Ϊ1Ҳͬʱ�����ϵͳ��������ͼƬ��0����
			am.setStreamVolume(AudioManager.STREAM_MUSIC, arg1, 0);
			mVoiceSeekBar.setProgress(arg1);
		} else if (!changeVolem) {
			setScreenBrightness(arg1 * 10);
		}
		// ��ʱcurrentVolume��Ϊ0
		currentVolume = arg1;
	}
	
	protected void updateVideoProgress(int arg1) {
		System.err.println("--------------mVideoSeekBar"+arg1);
		if ((mVideoSeekBar.getProgress()+arg1*6)<mVideoSeekBar.getMax()) {
			mVideoView.seekTo(mVideoSeekBar.getProgress()+arg1*6);
			mVideoSeekBar.setProgress(mVideoSeekBar.getProgress()+arg1*6);
		}
	}

	private float startY;
	private float endY;
	private float startX;
	private float endX;
	private float audioTouchRang;// ��Ļ�����ķ�Χ
	private int mVol;// ��ǰ����
	private boolean changeVolem;

	// ����onTouch�¼�
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		detector.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			removeDelayedhideControlPlayer();
			startY = event.getY();
			startX = event.getX();
			// �����Χ���Ե��������ڵķ�Χ��Ҳ���Ե������ȵ��ڵķ�Χ
			audioTouchRang = Math.min(screenWidth, screenHeight);
			mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
			break;

		case MotionEvent.ACTION_MOVE:
			endY = event.getY();
			endX = event.getX();
			float distance = startY - endY;
			float distanceX = startX - endX;
			// ������Ļ��������
			float datel = distance / audioTouchRang;
			float volume = datel * maxVolume + mVol;

			if (startX <= screenWidth / 2&& Math.abs(distanceX)<10) {
				//��������
				changeVolem = true;
			} else if(startX > screenWidth / 2 && Math.abs(distanceX)<10) {
				//������Ļ����
				changeVolem = false;
			}
			
			// ��Ļ�Ƿ�ֵ0-maxVolume֮��
			float volems = Math.min(Math.max(0, volume), maxVolume);
			// ��������
			if (volems != 0 && distanceX<10) {
				updateVolume((int) volems);
			}
			if (Math.abs(distance)<20 &&  Math.abs(distanceX)>10) {
				//��������
				updateVideoProgress((int)(-distanceX+10));
			}
			break;
		case MotionEvent.ACTION_UP:
			sendDelayedhideControlPlayer();
			break;

		default:
			break;
		}
		return true;
	}

	@Override
	public View setContentView() {
		return View.inflate(this, R.layout.activity_video_play, null);
	}

	@Override
	public void RightButtonClick() {
	}

	@Override
	public void leftButtonClick() {
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isDestroyed = true;
		// ȡ��ע���������
		unregisterReceiver(mBatteryBroadcastReceiver);
		mBatteryBroadcastReceiver = null;
	}

	// ���ð�ť��״̬����������һ����Ƶ����һ����ťӦ���ǲ��ɵ��
	private void setPlayOrPasueStatus() {
		if (position == 0) {
			mPreButton.setBackgroundResource(R.drawable.btn_back_no_enable);
			mPreButton.setEnabled(false);
		} else if (position == items.size() - 1) {
			mNextButton
					.setBackgroundResource(R.drawable.btn_forward_not_enable);
			mNextButton.setEnabled(false);
		} else {
			mPreButton.setBackgroundResource(R.drawable.btn_pre_selector);
			mPreButton.setEnabled(true);
			mNextButton.setBackgroundResource(R.drawable.btn_next_selector);
			mNextButton.setEnabled(true);
		}
	}

	private void playNextVideo() {
		// 1.û����һ����Ƶ �ˣ��˳�������
		// 2.����оͲ�����һ����Ƶ
		if (items != null && items.size() > 0) {
			position++;
			if (position < items.size()) {
				VideoInfo item = items.get(position);
				mVideoView.setVideoPath(item.getUrl().replace("localhost", Config.localhost));
				isNetUri = utils.isNetUri(item.getUrl().replace("localhost", Config.localhost));
				mTitleVideoTextView.setText(item.getTitle());
				// ��������һ����Ƶ����һ����ťӦ���ǲ��ɵ��
				setPlayOrPasueStatus();
			} else {
				position = items.size() - 1;
				SimpleToast("���һ����Ƶ��");
				finish();
			}
		} else if (uri != null) {
			// �ӵ�����������ô����ʱitems==null��uri��Ϊnull
			SimpleToast("�������һ����Ƶ��");
			finish();
		}
	}

	private void playPreVideo() {
		// 1.û����һ����Ƶ �ˣ��˳�������
		// 2.����оͲ�����һ����Ƶ
		if (items != null && items.size() > 0) {
			position--;
			if (position >= 0) {
				VideoInfo item = items.get(position);
				mVideoView.setVideoPath(item.getUrl().replace("localhost", Config.localhost));
				isNetUri = utils.isNetUri(item.getUrl().replace("localhost", Config.localhost));
				mTitleVideoTextView.setText(item.getTitle());
				// ��������һ����Ƶ����һ����ťӦ���ǲ��ɵ��
				setPlayOrPasueStatus();
			} else {
				position = 0;
				SimpleToast("��һ����Ƶ��");
			}
		}
	}

	// ��������ʾ�������
	private void hideControl() {
		mControlLinearLayout.setVisibility(View.GONE);
		isShowControl = false;
	}

	private void showControl() {
		mControlLinearLayout.setVisibility(View.VISIBLE);
		isShowControl = true;
	}

	/**
	 * ��Ƶ�Ĳ��Ż���ͣ
	 */
	private void startOrPause() {
		if (isPlaying) {
			mVideoView.pause();
			mPlayButton.setBackgroundResource(R.drawable.btn_play_normal);
		} else {
			mVideoView.start();
			mPlayButton.setBackgroundResource(R.drawable.btn_pause_normal);
		}
		isPlaying = !isPlaying;
	}

	// ������Ƶ�ĵ����ͣ�ȫ����Ĭ��,���Ǵ˹���δ��ɣ�����Ƶ�ĵڶ������һ�ڵ�26���ӿ�ʼ
	private void setVideoType(int type) {
		switch (type) {
		case FULL_SCREEN:
			mVideoView.setVideoSize(screenWidth, screenHeight);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			isFullScreen = true;
			break;
		case DEFAULT_SCREEN:
			int videoWidth = 0;
			int videoHeight = 0;
			// �������ƵӦ�����ö��
			int width = mVideoView.getWidth();
			int height = mVideoView.getHeight();
			if (isDestroyed) {
				mVideoView.setVideoSize(width, height);
				getWindow()
						.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
			mVideoView.setVideoSize(width, height);
			isFullScreen = false;
			break;

		default:
			break;
		}
	}

	// �Ƴ���Ϣ
	protected void removeDelayedhideControlPlayer() {
		handler.removeMessages(DELAYED_HIDECONTROL);
	}

	private void sendDelayedhideControlPlayer() {
		handler.sendEmptyMessageDelayed(DELAYED_HIDECONTROL, 5000);
	}

	/**
	 * ��õ�ǰ��Ļ���ȵ�ģʽ SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1 Ϊ�Զ�������Ļ����
	 * SCREEN_BRIGHTNESS_MODE_MANUAL=0 Ϊ�ֶ�������Ļ����
	 */
	private int getScreenMode() {
		int screenMode = 0;
		try {
			screenMode = Settings.System.getInt(getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE);
		} catch (Exception localException) {

		}
		return screenMode;
	}

	/**
	 * ��õ�ǰ��Ļ����ֵ 0--255
	 */
	private int getScreenBrightness() {
		int screenBrightness = 255;
		try {
			screenBrightness = Settings.System.getInt(getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS);
		} catch (Exception localException) {

		}
		return screenBrightness;
	}

	/**
	 * ���õ�ǰ��Ļ���ȵ�ģʽ SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1 Ϊ�Զ�������Ļ����
	 * SCREEN_BRIGHTNESS_MODE_MANUAL=0 Ϊ�ֶ�������Ļ����
	 */
	private void setScreenMode(int paramInt) {
		try {
			Settings.System.putInt(getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE, paramInt);
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	/**
	 * ���õ�ǰ��Ļ����ֵ 0--255
	 */
	private void saveScreenBrightness(int paramInt) {
		try {
			Settings.System.putInt(getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS, paramInt);
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	/**
	 * ���浱ǰ����Ļ����ֵ����ʹ֮��Ч
	 */
	private void setScreenBrightness(int paramInt) {
		Window localWindow = getWindow();
		WindowManager.LayoutParams localLayoutParams = localWindow
				.getAttributes();
		float f = paramInt / 255.0F;
		localLayoutParams.screenBrightness = f;
		localWindow.setAttributes(localLayoutParams);
	}
}

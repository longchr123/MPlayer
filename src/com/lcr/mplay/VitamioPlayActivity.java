package com.lcr.mplay;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.widget.VideoView;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.lcr.bean.VideoInfo;
import com.lcr.config.Config;
import com.lcr.utils.FormatUtils;

/**
 * ��ϵͳд�Ĳ����� ʵ���߼��� 1.ʵ��ϵͳ�Դ���VideoView���� 2.�Զ��岥�����������ò��� 3.���ò��Ű�ť 4.������ʱ�������ŵĵ�ǰʱ��
 * 5.����SeekBar����ת���� 6.���������仯,7.�Զ�������һ����Ƶ����������Ƶ������л���8.˫����Ļ���Ŵ���С�� 9.���ò�����.
 * 10.�϶���ʱ�������������С&AudioManager
 * 11.������磨��ͼ�⣩�е��ô˲�����������Ƶʱuri=getIntent��getUrl������ִ�У���poisitonΪ0�����¼���ťӦ�ò�����
 * 
 * @author Administrator
 * 
 */
public class VitamioPlayActivity extends BaseActivity {

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
	private boolean isNetUri;// �жϵ�ǰ�Ƿ����������

	private FormatUtils utils;

	private final int PROGRESS = 1;// ���½���
	private final int DELAYED_HIDECONTROL = 2;// ��ʱ���ؿ������
	private final int FULL_SCREEN = 3;// ȫ��
	private final int DEFAULT_SCREEN = 4;// Ĭ����Ļ
	private final int FINISH = 5;

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

	private Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case PROGRESS:
				// �õ���Ƶ �ĵ�ǰ���Ž���

				int currentPosition = (int) mVideoView.getCurrentPosition();
				mCurrentTimeTextView.setText(utils
						.stringForTime(currentPosition));
				// 2.SeekBar���ȸ���
				mVideoSeekBar.setProgress(currentPosition);

				// ��Ϣ��ѭ��,
				if (!isDestroyed) {
					handler.removeMessages(PROGRESS);// ����������д��룬����һ��Ϊhandler.sendEmptyMessage(PROGRESS);�ֻ��Ῠ���������Ҫ����ˢ�¾�Ҫȥ�����д��롣����
					handler.sendEmptyMessageDelayed(PROGRESS, 1000);
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

				break;

			case DELAYED_HIDECONTROL:
				hideControl();
				break;
			case FINISH:
				if (mVideoView != null) {
					mVideoView = null;
				}
				finish();
				break;
			default:
				break;
			}
		}
	};

	private boolean isLoadedLib;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ���ؽ�����
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		isLoadedLib = true;
		setTitleBar(View.GONE);
		initViews();
		getUrl();
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
			mTitleVideoTextView.setText(uri.toString());
			isNetUri = utils.isNetUri(uri.toString());
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
	private void getUrl() {
		items =getIntent().getParcelableArrayListExtra("videlist");
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

		// ������Ƶ�Ƿ�׼������-��ʼ����
		mVideoView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(io.vov.vitamio.MediaPlayer mp) {
//				mp.setPlaybackSpeed(1.5f);
				mVideoView.start();
				isPlaying = true;
				// �õ���Ƶ��ʱ��
				int duration = (int) mVideoView.getDuration();
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
			public boolean onInfo(io.vov.vitamio.MediaPlayer mp, int what,
					int extra) {
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
			public void onCompletion(io.vov.vitamio.MediaPlayer mp) {
				playNextVideo();
			}
		});

		/*
		 * ��Ƶ���ų�������Щԭ�� 1.��ʽ��֧�֣�---���ܲ����� 2.����---�ز� 3.���������пհ�---�޸�
		 * 4.������һ��������Ƶ���ļ���
		 */
		mVideoView.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(io.vov.vitamio.MediaPlayer mp, int what,
					int extra) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						VitamioPlayActivity.this).setMessage("���ų����Ƿ��˳�������")
						.setNegativeButton("��", null);
				builder.setPositiveButton("ȷ��",
						new AlertDialog.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								handler.sendEmptyMessage(FINISH);
							}
						}).setCancelable(false).show();

				return false;
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
						VitamioPlayActivity.this).setMessage(
						"��ǰ�����ܲ��������Ƿ��л���ϵͳ������").setNegativeButton("��", null);
				builder.setPositiveButton("ȷ��",
						new AlertDialog.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								startVideoPlayer();
							}
						}).setCancelable(false).show();
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

	/**
	 * ���������ķ���:һ���ǣ�0-15 arg1��Ҫ���ڵ�������
	 */
	protected void updateVolume(int arg1) {
		if (isMute) {// ����
			// ��3Ϊ1Ҳͬʱ�����ϵͳ��������ͼƬ��0����
			am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
			mVoiceSeekBar.setProgress(0);
		} else {
			// ��3Ϊ1Ҳͬʱ�����ϵͳ��������ͼƬ��0����
			am.setStreamVolume(AudioManager.STREAM_MUSIC, arg1, 0);
			mVoiceSeekBar.setProgress(arg1);
		}
		// ��ʱcurrentVolume��Ϊ0
		currentVolume = arg1;
	}

	private float startY;
	private float endY;
	private float audioTouchRang;// ��Ļ�����ķ�Χ
	private int mVol;// ��ǰ����

	// ����onTouch�¼�
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		detector.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			removeDelayedhideControlPlayer();
			startY = event.getY();
			audioTouchRang = Math.min(screenWidth, screenHeight);
			mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
			break;
		case MotionEvent.ACTION_MOVE:
			endY = event.getY();
			float distance = startY - endY;
			// ������Ļ��������
			float datel = distance / audioTouchRang;
			float volume = datel * maxVolume + mVol;
			// ��Ļ�Ƿ�ֵ0-maxVolume֮��
			float volems = Math.min(Math.max(0, volume), maxVolume);
			if (volems != 0) {
				updateVolume((int) volems);
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
		if (!LibsChecker.checkVitamioLibs(this)) {
			return null;
		}
		return View.inflate(this, R.layout.activity_vitamio_play, null);
	}

	@Override
	public void RightButtonClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void leftButtonClick() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// ��ֹ��û���ؿ�ʱ�͵��õ���onDestroy��������û��ע��mBatteryBroadcastReceiver������
		if (isLoadedLib) {
			isDestroyed = true;
			// ȡ��ע���������
			unregisterReceiver(mBatteryBroadcastReceiver);
			mBatteryBroadcastReceiver = null;
		}

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
			// mVideoView.setVideoSize(screenWidth, screenHeight);
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
				// mVideoView.setVideoSize(width, height);
				getWindow()
						.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
			// mVideoView.setVideoSize(width, height);
			isFullScreen = false;
			break;

		default:
			break;
		}
	}

	protected void startVideoPlayer() {
		VideoInfo item = items.get(position);
		Intent intent = new Intent(VitamioPlayActivity.this,
				VideoPlayActivity.class);
		Bundle extras = new Bundle();
		intent.putExtra("position", position);
		extras.putParcelableArrayList("videlist", (ArrayList<VideoInfo>)items);
		intent.putExtras(extras);
		intent.setData(uri);// һ��Ҫ��
		startActivity(intent);
		// �رյ�ǰActivity--��������Ҫ��ʱ�������ϵͳ������
		handler.sendEmptyMessageDelayed(FINISH, 2000);
	}

	// �Ƴ���Ϣ
	protected void removeDelayedhideControlPlayer() {
		handler.removeMessages(DELAYED_HIDECONTROL);
	}

	private void sendDelayedhideControlPlayer() {
		handler.sendEmptyMessageDelayed(DELAYED_HIDECONTROL, 5000);
	}
}

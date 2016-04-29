package com.yixia.camera.demo.ui.record;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.SimpleFormatter;

import org.kobjects.base64.Base64;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lcr.config.Config;
import com.lcr.mplay.MainActivity;
import com.lcr.mplay.R;
import com.lcr.utils.DisplayUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.yixia.camera.demo.ui.BaseActivity;
import com.yixia.camera.demo.ui.widget.SurfaceVideoView;
import com.yixia.camera.demo.util.Constant;
import com.yixia.weibo.sdk.util.DeviceUtils;
import com.yixia.weibo.sdk.util.StringUtils;

/**
 * 通用单独播放界面
 * 
 * @author tangjun
 * 
 */
public class VideoPlayerActivity extends BaseActivity implements
		SurfaceVideoView.OnPlayStateListener, OnErrorListener,
		OnPreparedListener, OnClickListener, OnCompletionListener,
		OnInfoListener {

	/** 播放控件 */
	private SurfaceVideoView mVideoView;
	/** 暂停按钮 */
	private View mPlayerStatus;
	private View mLoading;

	/** 播放路径 */
	private String mPath;
	/** 视频截图路径 */
	private String mCoverPath;

	/** 是否需要回复播放 */
	private boolean mNeedResume;

	private Button mBack;
	private Button mMyVideo;
	private Button mPushVideo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 防止锁屏
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mPath = getIntent().getStringExtra(Constant.RECORD_VIDEO_PATH);
		mCoverPath = getIntent().getStringExtra(Constant.RECORD_VIDEO_CAPTURE);
		if (StringUtils.isEmpty(mPath)) {
			finish();
			return;
		}

		setContentView(R.layout.activity_video_player);
		mVideoView = (SurfaceVideoView) findViewById(R.id.videoview);
		mPlayerStatus = findViewById(R.id.play_status);
		mLoading = findViewById(R.id.loading);

		mBack = (Button) findViewById(R.id.btn_back);
		mMyVideo = (Button) findViewById(R.id.btn_my_video);
		mPushVideo = (Button) findViewById(R.id.btn_push_video);
		mBack.setOnClickListener(this);
		mMyVideo.setOnClickListener(this);
		mPushVideo.setOnClickListener(this);

		mVideoView.setOnPreparedListener(this);
		mVideoView.setOnPlayStateListener(this);
		mVideoView.setOnErrorListener(this);
		mVideoView.setOnClickListener(this);
		mVideoView.setOnInfoListener(this);
		mVideoView.setOnCompletionListener(this);

		mVideoView.getLayoutParams().height = DeviceUtils.getScreenWidth(this);

		findViewById(R.id.root).setOnClickListener(this);
		mVideoView.setVideoPath(mPath);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mVideoView != null && mNeedResume) {
			mNeedResume = false;
			if (mVideoView.isRelease())
				mVideoView.reOpen();
			else
				mVideoView.start();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mVideoView != null) {
			if (mVideoView.isPlaying()) {
				mNeedResume = true;
				mVideoView.pause();
			}
		}
	}

	@Override
	protected void onDestroy() {
		if (mVideoView != null) {
			mVideoView.release();
			mVideoView = null;
		}
		super.onDestroy();
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mVideoView.setVolume(SurfaceVideoView.getSystemVolumn(this));
		mVideoView.start();
		// new Handler().postDelayed(new Runnable() {
		//
		// @SuppressWarnings("deprecation")
		// @Override
		// public void run() {
		// if (DeviceUtils.hasJellyBean()) {
		// mVideoView.setBackground(null);
		// } else {
		// mVideoView.setBackgroundDrawable(null);
		// }
		// }
		// }, 300);
		mLoading.setVisibility(View.GONE);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		switch (event.getKeyCode()) {// 跟随系统音量走
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_VOLUME_UP:
			mVideoView.dispatchKeyEvent(this, event);
			break;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onStateChanged(boolean isPlaying) {
		mPlayerStatus.setVisibility(isPlaying ? View.GONE : View.VISIBLE);
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		if (!isFinishing()) {
			// 播放失败
		}
		finish();
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.root:
			finish();
			break;
		case R.id.videoview:
			if (mVideoView.isPlaying())
				mVideoView.pause();
			else
				mVideoView.start();
			break;
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_my_video:
			Intent intent = new Intent(VideoPlayerActivity.this,
					MainActivity.class);
			intent.putExtra("myworks", true);
			startActivity(intent);
			break;
		case R.id.btn_push_video:
			// 服务器端地址
			String url = "http://" + Config.localhost
					+ ":8080/axis2/services/XPlayServer/upload";
			String imgPath = mPath.replace(".mp4", ".jpg");
			// 保存第一帧到本地
			saveImg(imgPath);
			showDialog(url, imgPath);
			break;
		}
	}

	private void saveImg(String imgPath) {
		Bitmap bitmap = null;
		bitmap = ThumbnailUtils.createVideoThumbnail(mPath,
				MediaStore.Images.Thumbnails.MICRO_KIND);
		int px=DisplayUtil.dip2px(VideoPlayerActivity.this,300);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, px, px,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(new File(imgPath));
			bitmap.compress(CompressFormat.JPEG, 100, out);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}finally{
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			bitmap.recycle();
		}
	}

	private AlertDialog dialog = null;
	private EditText title;
	private EditText des;
	private String titleString, desString;
	private void showDialog(final String url, final String imgPath) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				VideoPlayerActivity.this);
		View view2 = getLayoutInflater().from(VideoPlayerActivity.this)
				.inflate(R.layout.dialog_info, null);
		title = (EditText) view2.findViewById(R.id.text_title);
		des = (EditText) view2.findViewById(R.id.text_des);
		view2.findViewById(R.id.btn_upload).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						titleString = title.getText().toString().trim();
						desString = des.getText().toString().trim();
						// 上传视频至服务器
						uploadFile(url, imgPath);
						dialog.dismiss();
					}
				});
		view2.findViewById(R.id.btn_cancel_upload).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
		builder.setView(view2);
		dialog = builder.create();
		dialog.show();
	}

	private void uploadFile(String url, String imgPath) {
		byte[] bytes = getBytes(mPath);
		byte[] bytes2 = getBytes(imgPath);
		String strData = new String(Base64.encode(bytes));
		String strData2 = new String(Base64.encode(bytes2));
		HttpUtils httpUtils = new HttpUtils();
		RequestParams params = new RequestParams();
		params.addBodyParameter("outputStream1", strData);
		params.addBodyParameter("outputStream2", strData2);
		params.addBodyParameter("filename", getFileName(mPath));
		params.addBodyParameter("title", titleString);
		params.addBodyParameter("description", desString);
		params.addBodyParameter("duration", "00:00:10");
		params.addBodyParameter("updatetime", getTime());
		params.addBodyParameter("size", "3.0MB");
		params.addBodyParameter("user_id", "123456789");
		httpUtils.send(HttpMethod.POST, url, params,
				new RequestCallBack<String>() {

					@Override
					public void onStart() {
						super.onStart();
						mPushVideo.setText("正在上传....");
					}

					@Override
					public void onLoading(long total, long current,
							boolean isUploading) {
						super.onLoading(total, current, isUploading);
						System.out.println(current + "/" + total);
						mPushVideo.setText("正在上传...." + current + "/" + total);
					}

					@Override
					public void onSuccess(ResponseInfo<String> arg0) {
						mPushVideo.setText("上传成功");
					}

					@Override
					public void onFailure(HttpException arg0, String arg1) {
						mPushVideo.setText("上传失败");
					}
				});
	}

	/**
	 * 获得指定文件的byte数组
	 */
	private byte[] getBytes(String filePath) {
		byte[] buffer = null;
		try {
			File file = new File(filePath);
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(10000);
			byte[] b = new byte[1000];
			int n;
			while ((n = fis.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			fis.close();
			bos.close();
			buffer = bos.toByteArray();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer;
	}

	/**
	 * 根据路径获取文件名
	 * 
	 * @param path
	 * @return
	 */
	public String getFileName(String path) {
		String[] s = path.split("/");
		return s[s.length - 1];
	}

	public String getTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return sdf.format(new Date());

	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (!isFinishing())
			mVideoView.reOpen();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		switch (what) {
		case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
			// 音频和视频数据不正确
			break;
		case MediaPlayer.MEDIA_INFO_BUFFERING_START:
			if (!isFinishing())
				mVideoView.pause();
			break;
		case MediaPlayer.MEDIA_INFO_BUFFERING_END:
			if (!isFinishing())
				mVideoView.start();
			break;
		case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
			if (DeviceUtils.hasJellyBean()) {
				mVideoView.setBackground(null);
			} else {
				mVideoView.setBackgroundDrawable(null);
			}
			break;
		}
		return false;
	}

}

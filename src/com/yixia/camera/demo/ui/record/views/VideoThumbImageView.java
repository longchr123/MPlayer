package com.yixia.camera.demo.ui.record.views;

import java.io.File;

import com.yixia.camera.demo.log.Logger;
import com.yixia.weibo.sdk.util.FileUtils;
import com.yixia.weibo.sdk.util.StringUtils;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.widget.ImageView;

/**
 * 视频截图
 * 
 * @author tangjun
 *
 */
public class VideoThumbImageView extends ImageView {

	/** 截图存放路径 */
	private String mThumbPath;
	/** 屏幕宽度 */
	private int mWindowWidth;
	/** 索引 */
	private int mIndex;
	/** 当前时间 */
	private int mPosition;
	/** 是否已经加载了图片 */
	private boolean mNeedLoad;

	public VideoThumbImageView(Context context, String thumbPath, int mWindowWidth, int index, int position) {
		super(context);
		this.mWindowWidth = mWindowWidth;
		//		this.mThumbPosition = position;
		this.mThumbPath = thumbPath;
		this.mNeedLoad = true;
		this.mIndex = index;
		this.mPosition = position;
		//		this.mVideoPath = videoPath;
		//		int wh = ConvertToUtils.dipToPX(getContext(), 56);
		//		mThumbWH = String.format("%dx%d", wh, wh);
		//		if (firstDraw)
		//			loadImage();
	}

	//	public void scrollY() {
	//		//是否已经截图并且加载了，是否正在处理
	//		if (!mScroll && StringUtils.isNotEmpty(mThumbPath) && StringUtils.isNotEmpty(mVideoPath)) {
	//			mScroll = true;
	//
	//			//检测是否在显示区域
	//			Rect rect = new Rect();
	//			getGlobalVisibleRect(rect);
	//
	//			if (rect.right <= mWindowWidth) {
	//				loadImage();
	//			} else {
	//				mScroll = false;
	//			}
	//		}
	//	}
	//	public String getVideoPath() {
	//		return mThumbPath;
	//	}

	/** 获取截图对应的时间戳 */
	public int getThumbPosition() {
		return mPosition;
	}

	public int getThumbIndex() {
		return mIndex;
	}

	public String getThumbPath() {
		return mThumbPath;
	}

	public void log() {
		Rect rect = new Rect();
		getGlobalVisibleRect(rect);
		Logger.d("[VideoThumbImageView]mNeedLoad:" + mNeedLoad + " checkVisible:" + checkVisible() + ":getLeft:" + getLeft() + ":getRight:" + getRight() + " checkThumb:" + checkThumb() + " " + new File(mThumbPath).getName());
	}

	/** 检测是否需要显示 */
	public boolean checkVisible() {
		if (StringUtils.isNotEmpty(mThumbPath)) {
			Rect rect = new Rect();
			getGlobalVisibleRect(rect);

			if (rect.right <= mWindowWidth && rect.right > 0)
				return true;
		}
		return false;
	}

	/** 检测是否需要截图 */
	public boolean checkThumb() {
		File f = new File(mThumbPath);
		if (f.exists() && f.canRead() && f.length() > 0) {
			return true;
		}
		return false;
	}

	/** 检查是否需要加载 */
	public boolean needLoad() {
		return mNeedLoad;
	}

	public static Uri getFileUri(String path) {

		Logger.e("simon", "getFile Uri>>>" + path);

		return Uri.parse("file:///" + path);
	}
	
	/** 开始截图 */
	public void loadImage() {
		if (FileUtils.checkFile(mThumbPath)) {
			setImageURI(getFileUri(mThumbPath));
			mNeedLoad = false;
		}
	}

	//		else {
	//			new AsyncTask<Void, Void, Boolean>() {
	//
	//				@Override
	//				protected Boolean doInBackground(Void... params) {
	//					String cmd = String.format("ffmpeg %s -ss %.3f -i \"%s\" -s %s -vframes 1 \"%s\"", FFMpegUtils.getLogCommand(), mThumbPosition / 1000F, mVideoPath, mThumbWH, mThumbPath);
	//					return UtilityAdapter.FFmpegRun("", cmd) == 0;
	//				}
	//
	//				@Override
	//				protected void onPostExecute(Boolean result) {
	//					super.onPostExecute(result);
	//					//					if (result && new File(mMediaPart.thumbPath).exists()) {
	//					//						loadLocalImage(mMediaPart.thumbPath);
	//					//					} else {
	//					//						//mIconView.setImageResource(R.drawable.video_part_thumb_default);
	//					//					}
	//					if (mImageFetcher != null)
	//						mImageFetcher.loadLocalImage(mThumbPath, VideoThumbImageView.this);
	//
	//					mScroll = false;
	//				}
	//			}.execute();
	//		}
}

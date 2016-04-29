package com.yixia.camera.demo.ui.record.views;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.yixia.camera.demo.log.Logger;
import com.yixia.weibo.sdk.util.DeviceUtils;

public class VideoViewTouch extends TextureVideoView {

	private float mScale = 1.0F;
	private int mWindowWidth;
	private boolean mCanScrollX;
	private boolean mCanScrollY;
	private LinearLayout.LayoutParams mLayoutParams;
	private OnTouchEventListener mOnTouchEventListener;
	private GestureDetector mGestureDetector;

	public VideoViewTouch(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public VideoViewTouch(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void initVideoView() {
		super.initVideoView();
		mWindowWidth = DeviceUtils.getScreenWidth(getContext());
		mGestureDetector = new GestureDetector(getContext(), new VideoViewGestureListener(this));
	}

	public void setOnTouchEventListener(OnTouchEventListener l) {
		mOnTouchEventListener = l;
	}

	public boolean getCanScrollY() {
		return mCanScrollY;
	}

	public boolean getCanScrollX() {
		return mCanScrollX;
	}

	public void setCanScrollY(boolean can) {
		mCanScrollY = can;
	}

	public void setCanScrollX(boolean can) {
		mCanScrollX = can;
	}

	public int getCropX() {
		if (mLayoutParams != null) {
			//			return (int) (Math.abs(mLayoutParams.leftMargin) * 1.0F / mLayoutParams.width * getVideoWidth());
			return Math.abs(mLayoutParams.leftMargin);
		}
		return 0;
	}

	public int getCropY() {
		if (mLayoutParams != null) {
			//			return (int) (Math.abs(mLayoutParams.topMargin) * 1.0F / mLayoutParams.height * getVideoHeight());
			return Math.abs(mLayoutParams.topMargin);
		}
		return 0;
	}

	public float getScale() {
		return mScale;
	}

	public void resize() {
		if (mLayoutParams == null)
			mLayoutParams = (LinearLayout.LayoutParams) getLayoutParams();

		int videoWidth = getVideoWidth();
		int videoHeight = getVideoHeight();
		float videoAspectRatio = videoWidth * 1.0F / videoHeight;
		//		Logger.e("[VideoViewTouch]videoWidth:" + videoWidth + " videoHeight:" + videoHeight + " mWindowWidth:" + mWindowWidth);

		if (videoWidth > videoHeight) {
			mLayoutParams.height = mWindowWidth;
			mLayoutParams.width = (int) (mWindowWidth * videoAspectRatio);
			setCanScrollX(true);
			mScale = mLayoutParams.width / videoWidth;
		} else if (videoWidth == videoHeight) {
			mLayoutParams.width = mLayoutParams.height = mWindowWidth;
		} else {
			//竖向
			mLayoutParams.width = mWindowWidth;
			mLayoutParams.height = (int) (mLayoutParams.width / videoAspectRatio);
			setCanScrollY(true);
			mScale = mLayoutParams.height / videoHeight;
		}

		//		//支持判断手机屏幕宽高
		//		if ((videoWidth >= videoHeight && videoWidth >= mWindowWidth) || (videoWidth <= videoHeight && videoHeight <= mWindowWidth)) {
		//			mLayoutParams.height = mWindowWidth;
		//			mLayoutParams.width = (int) (mWindowWidth * videoAspectRatio);
		//			if (videoWidth > videoHeight)
		//				setCanScrollX(true);
		//		} else {
		//			mLayoutParams.width = mWindowWidth;
		//			mLayoutParams.height = (int) (mWindowWidth / videoAspectRatio);
		//			if (videoWidth < videoHeight)
		//				setCanScrollY(true);
		//		}

		setLayoutParams(mLayoutParams);

		Logger.e("[VideoEditActivity]videoWidth:" + videoWidth + "x" + videoHeight + " windowWidth:" + mWindowWidth + " :" + mLayoutParams.width + "x" + mLayoutParams.height);
	}

	/** 居中显示，需要在resize之后调用 */
	public void centerXY() {
		if (mLayoutParams != null) {
			if (mCanScrollX) {
				//横向居中
				mLayoutParams.leftMargin -= (mLayoutParams.width - mWindowWidth) / 2;
				setLayoutParams(mLayoutParams);
			} else if (mCanScrollY) {
				mLayoutParams.topMargin -= (mLayoutParams.height - mWindowWidth) / 2;
				setLayoutParams(mLayoutParams);
			}
		}
	}

	public void fitCenter() {
		if (mLayoutParams != null) {
			mLayoutParams.topMargin = 0;
			mLayoutParams.leftMargin = 0;
			if (mCanScrollX) {
				//横向居中
				mLayoutParams.width = mWindowWidth;
				mLayoutParams.height = mWindowWidth * getVideoHeight() / getVideoWidth();
				setLayoutParams(mLayoutParams);
			} else if (mCanScrollY) {
				mLayoutParams.height = mWindowWidth;
				mLayoutParams.width = mWindowWidth * getVideoWidth() / getVideoHeight();
				setLayoutParams(mLayoutParams);
			}
			
		}
	}

	public int getViewHeight() {
		if (mLayoutParams != null)
			return mLayoutParams.height;
		return 0;
	}

	private float mOldX, mOldY;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mGestureDetector.onTouchEvent(event))
			return true;

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				//			mOldX = event.getRawX();
				//			mOldY = event.getRawY();
				//			if (mLayoutParams == null)
				//				mLayoutParams = (LinearLayout.LayoutParams) getLayoutParams();
				//			return true;
				//		case MotionEvent.ACTION_MOVE:
				//			float mNewX = event.getRawX();
				//			float mNewY = event.getRawY();
				//			float distanceX = mNewX - mOldX;
				//			float distanceY = mNewY - mOldY;
				//			if (Math.abs(mNewY - mOldY) > Math.abs(mNewX - mOldX)) {
				//				//上下
				//				if (mNewY > mOldY) {
				//					//从上到下
				//					onTopToBottom(Math.abs((int) distanceY));
				//				} else {
				//					//从下到上
				//					onBottomToTop(Math.abs((int) distanceY));
				//				}
				//			} else {
				//				if (mNewX > mOldX) {
				//					//从左至右
				//					onLeftToRight(Math.abs((int) distanceX));
				//				} else {
				//					//从右至左
				//					onRightToLeft(Math.abs((int) distanceX));
				//				}
				//			}
				//
				//			mOldX = mNewX;
				//			mOldY = mNewY;
				break;
			case MotionEvent.ACTION_UP:
				if (mOnTouchEventListener != null)
					mOnTouchEventListener.onVideoViewUp();
				break;
		}

		return super.onTouchEvent(event);
	}

	private void onLeftToRight(int distance) {
		if (mCanScrollX) {
			final int absLeftMargin = Math.abs(mLayoutParams.leftMargin);
			if (absLeftMargin > 0) {
				if (mLayoutParams.leftMargin + distance > 0)
					distance = -mLayoutParams.leftMargin;
				mLayoutParams.leftMargin += distance;
				setLayoutParams(mLayoutParams);
			}
		}
	}

	private void onRightToLeft(int distance) {
		//		Logger.e("[VideoView]onRightToLeft  leftMargin:" + mLayoutParams.leftMargin + " width:" + mLayoutParams.width + " mWindowWidth:" + mWindowWidth);
		if (mCanScrollX) {
			final int absLeftMargin = Math.abs(mLayoutParams.leftMargin);
			if (absLeftMargin + mWindowWidth < mLayoutParams.width) {
				if (distance + absLeftMargin + mWindowWidth > mLayoutParams.width)
					distance = mLayoutParams.width - absLeftMargin - mWindowWidth;
				mLayoutParams.leftMargin += -distance;
				setLayoutParams(mLayoutParams);
			}
		}
	}

	private void onTopToBottom(int distance) {
		//		Logger.e("[VideoView]onTopToBottom  topMargin:" + mLayoutParams.topMargin + " width:" + mLayoutParams.height + " mWindowWidth:" + mWindowWidth + " distance:" + distance + " mCanScrollY:" + mCanScrollY);
		if (mCanScrollY) {
			final int absTopMargin = Math.abs(mLayoutParams.topMargin);
			if (absTopMargin > 0) {
				if (mLayoutParams.topMargin + distance > 0)
					distance = -mLayoutParams.topMargin;
				mLayoutParams.topMargin += distance;
				setLayoutParams(mLayoutParams);
			}
		}
	}

	private void onBottomToTop(int distance) {
		//		Logger.e("[VideoView]onRightToLeft  topMargin:" + mLayoutParams.topMargin + " width:" + mLayoutParams.height + " mWindowWidth:" + mWindowWidth + " distance:" + distance + " mCanScrollY:" + mCanScrollY);
		if (mCanScrollY) {
			final int absTopMargin = Math.abs(mLayoutParams.topMargin);
			if (absTopMargin + mWindowWidth < mLayoutParams.height) {
				if (distance + absTopMargin + mWindowWidth > mLayoutParams.height)
					distance = mLayoutParams.height - absTopMargin - mWindowWidth;
				mLayoutParams.topMargin += -distance;
				setLayoutParams(mLayoutParams);
			}
			//			mLayoutParams.topMargin += (int) -distance;
			//			setLayoutParams(mLayoutParams);
		}
	}

	public static interface OnTouchEventListener {
		public void onVideoViewDown();

		public void onVideoViewUp();

		public boolean onClick();
	}

	private static class VideoViewGestureListener extends SimpleOnGestureListener {

		private final WeakReference<VideoViewTouch> mView;

		public VideoViewGestureListener(VideoViewTouch view) {
			mView = new WeakReference<VideoViewTouch>(view);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX1, float distanceY1) {

			float mNewX = e2.getX();
			float mNewY = e2.getY();
			float mOldX = e1.getX();
			float mOldY = e1.getY();
			float distanceX = mNewX - mOldX;
			float distanceY = mNewY - mOldY;
			VideoViewTouch videoView = mView.get();
			if (videoView != null) {
				if (Math.abs(mNewY - mOldY) > Math.abs(mNewX - mOldX)) {
					//上下
					if (mNewY > mOldY) {
						//从上到下
						videoView.onTopToBottom(Math.abs((int) distanceY));
					} else {
						//从下到上
						videoView.onBottomToTop(Math.abs((int) distanceY));
					}
				} else {
					if (mNewX > mOldX) {
						//从左至右
						videoView.onLeftToRight(Math.abs((int) distanceX));
					} else {
						//从右至左
						videoView.onRightToLeft(Math.abs((int) distanceX));
					}
				}
			}
			//
			//			mOldX = mNewX;
			//			mOldY = mNewY;

			return super.onScroll(e1, e2, distanceX, distanceY);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			VideoViewTouch videoView = mView.get();
			if (videoView != null) {
				if (videoView.mOnTouchEventListener != null) {
					if (videoView.mOnTouchEventListener.onClick())
						return true;
				}
				if (videoView.isPlaying())
					videoView.pause();
				else
					videoView.start();
				return true;
			}
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			VideoViewTouch videoView = mView.get();
			if (videoView != null) {
				if (videoView.mOnTouchEventListener != null)
					videoView.mOnTouchEventListener.onVideoViewDown();
			}
			return true;
		}
	}
}

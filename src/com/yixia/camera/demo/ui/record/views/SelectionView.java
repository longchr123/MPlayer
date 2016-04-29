package com.yixia.camera.demo.ui.record.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.lcr.mplay.R;

public class SelectionView extends View {
	/** 进度条 */
	private Paint mPaint;
	private Paint mMaskPaint;
	private int mMargin, mMinRightMargin, mLeftMargin, mRightMargin;
	private Paint mLinePaint;
	private long mPosition;
	private int mStarTime;
	private int mEndTime;
	private boolean mClearLineStatus = false;
	private boolean isDrawMargin;

	public SelectionView(Context context) {
		this(context, null);
	}

	public SelectionView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mPaint = new Paint();
		mPaint.setColor(getResources().getColor(R.color.translucent_background_75));
		mPaint.setStyle(Paint.Style.FILL);

		mMaskPaint = new Paint();
		mMaskPaint.setColor(getResources().getColor(R.color.import_video_thumb_mask));
		mMaskPaint.setStyle(Paint.Style.FILL);

		mLinePaint = new Paint();
		mLinePaint.setColor(getResources().getColor(R.color.white));
		mLinePaint.setStyle(Paint.Style.STROKE);
		mLinePaint.setStrokeWidth((float) 5.0);
	}

	public void setMargin(int margin) {
		if (mMargin == 0 && margin > 0)
			mMargin = margin;
	}

	/** 设置左边距 */
	public void setLeftMargin(int leftMargin) {
		mLeftMargin = leftMargin;
		invalidate();
	}

	/** 设置右边距 */
	public void setRightMargin(int rightMargin) {
		mRightMargin = rightMargin;
		invalidate();
	}

	public int getLeftMargin() {
		return mLeftMargin;
	}

	public int getRightMargin() {
		return mRightMargin;
	}

	/** 获取当前的宽度 */
	public int getCurrentWidth() {
		return getWidth() - mLeftMargin - mRightMargin - mMargin - mMargin;
	}

	/** 设置最小右边距 */
	public void setMinRightMargin(int rightMargin) {
		this.mMinRightMargin = rightMargin;
		setRightMargin(rightMargin);
	}

	public int getMinRightMargin() {
		return mMinRightMargin;
	}

	/**
	 * 设置进度线
	 * @param position 当前位置
	 * @param starTime 起始时间
	 * @param endTime  结束时间
	 */
	public void setLinePosition(long position, int starTime, int endTime) {
		this.mPosition = position;
		this.mStarTime = starTime;
		this.mEndTime = endTime;
		mClearLineStatus = false;
		invalidate();
	}

	/** 清除进度线*/
	public void clearLine() {
		mClearLineStatus = true;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mMargin > 0) {
			int width = getMeasuredWidth();
			final int height = getMeasuredHeight();
			//画左边阴影
//			if (mLeftMargin > 0) {
//				Logger.systemErr("mLeftMargin");
//				canvas.drawRect(0, 0, mLeftMargin + mMargin, height, mPaint);
//			}
//
//			//画右边阴影
//			if (mRightMargin > 0 || mMinRightMargin > 0) {
//				Logger.systemErr("mRightMargin > 0 || mMinRightMargin > 0");
//				canvas.drawRect(width - mMargin - mRightMargin, 0, width, height, mPaint);
//			}

			canvas.drawRect(0, 0, mLeftMargin + mMargin, height, mPaint);
			canvas.drawRect(width - mMargin - mRightMargin, 0, width, height, mPaint);
			
//			if (!(mLeftMargin > 0) && !(mRightMargin > 0 || mMinRightMargin > 0)) {
//				isDrawMargin = true;
				
//			}

			//覆盖右边不应该显示的图片
			if (mMinRightMargin > 0) {
				canvas.drawRect(width - mMinRightMargin - mMargin, 0, width - mMargin, height, mMaskPaint);
			}
			//添加进度线条
			if (!mClearLineStatus) {
				int mWidth = getCurrentWidth();
				if (mWidth > 0 && mPosition > 0 && mEndTime > 0) {
					int delayMillis = mEndTime - mStarTime;
					if (delayMillis > 0) {
//						if (delayMillis > RecorderHelper.getMaxDuration()) {
//							if (mPosition > RecorderHelper.getMaxDuration()) {
//								
//								mPosition = mPosition - (int) (mPosition / delayMillis) * delayMillis;
//							}
//						} else {
		  					if (mStarTime != 0) {
								mPosition = mPosition - mStarTime;
							}
//						}
					}
					
					
					float x = (mWidth * mPosition / delayMillis) + mMargin + mLeftMargin;
					
//					android.util.Log.e("simon","setLinePosition::StartTime>>"+mStarTime+">>>endTime>>>"+mEndTime+">>>position>>"+mPosition);
//				
//					android.util.Log.e("simon","XXXXXX>>"+x+">>>mMargin>>>"+mMargin+">>>mLeftMargin>>"+mLeftMargin);
//					
//					android.util.Log.e("simon","mWidth>>"+mWidth+">>>mPosition>>>"+mPosition+">>>delayMillis>>"+delayMillis);
					
					
					canvas.drawLine(x, 0, x, height, mLinePaint);
				}
			}
		}
	}
}

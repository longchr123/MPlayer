package com.yixia.camera.demo.ui.record.views;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

public class HorizontalScrollViewEx extends HorizontalScrollView {

	private OnFlingListener mOnFlingListener;

	public HorizontalScrollViewEx(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public HorizontalScrollViewEx(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setOnFlingListener(OnFlingListener l) {
		this.mOnFlingListener = l;
	}

	@Override
	protected int computeHorizontalScrollOffset() {
		if (mOnFlingListener != null)
			mOnFlingListener.onFlingScrollChange();
		return super.computeHorizontalScrollOffset();
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		if (mOnFlingListener != null)
			mOnFlingListener.onFlingScrollChange();
	  super.onScrollChanged(l, t, oldl, oldt);
	}
	
	public static interface OnFlingListener {
		void onFlingScrollChange();
	}
}

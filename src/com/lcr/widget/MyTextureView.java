package com.lcr.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;

public class MyTextureView extends TextureView {

	public MyTextureView(Context context) {
		super(context);
	}

	public MyTextureView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		//在控件不可见时进行销毁
		if (getSurfaceTextureListener()!=null&&getSurfaceTexture()!=null) {
			getSurfaceTextureListener().onSurfaceTextureDestroyed(getSurfaceTexture());
		}
	}

}

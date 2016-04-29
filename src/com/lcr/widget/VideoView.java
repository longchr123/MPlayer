package com.lcr.widget;

import android.content.Context;
import android.util.AttributeSet;

public class VideoView extends android.widget.VideoView{

	
	public VideoView(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
	}

	/**
	 * ������Ƶ�Ŀ�͸�
	 * @param width
	 * @param height
	 */
	public void setVideoSize( int width,int height) {
		android.view.ViewGroup.LayoutParams layoutParams=getLayoutParams();
		layoutParams.width=width;
		layoutParams.height=height;
		setLayoutParams(layoutParams);
	}

}

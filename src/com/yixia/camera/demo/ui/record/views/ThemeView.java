package com.yixia.camera.demo.ui.record.views;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lcr.mplay.R;
import com.yixia.weibo.sdk.model.VideoEffectModel;
import com.yixia.weibo.sdk.util.IsUtils;

public class ThemeView extends RelativeLayout implements Observer {

	/** 图标 */
	private ImageView mSelectedIcon, isDownloadImageview;
	private BitmapImageView mIcon;
	/** 标题 */
	private TextView mTitle, progressTitle;
	/** 当前主题 */
	private VideoEffectModel mTheme;

	public VideoEffectModel getTheme() {
  	return mTheme;
  }

	public void setTheme(VideoEffectModel mTheme) {
		this.mTheme = mTheme;
	}

	public ThemeView(Context context, VideoEffectModel theme) {
		super(context);
		this.mTheme = theme;

		LayoutInflater.from(context).inflate(R.layout.view_theme_item, this);

		isDownloadImageview = (ImageView) findViewById(R.id.icon_need_download);

		progressTitle = (TextView) findViewById(R.id.progress);

		mIcon = (BitmapImageView) findViewById(R.id.icon);
		mSelectedIcon = (ImageView) findViewById(R.id.selected);
		mTitle = (TextView) findViewById(R.id.title);

		if (theme.isDownloaded()){
			mTitle.setText(mTheme.effectNameChinese+" downlaoded");	
		}else if (theme.isLocal()){
			mTitle.setText(mTheme.effectNameChinese+" local");
		}
		else {
			mTitle.setText(mTheme.effectNameChinese);
		}
		

		if (!mTheme.isMV()) {
			//高级编辑全部变成方的
			// if (mTheme.isWatermark() || mTheme.isSoundEffect() || mTheme.isFilter() || mTheme.isSpeed())
			mSelectedIcon.setImageResource(R.drawable.record_theme_square_selected);
		}
		if (mTheme.isEmpty() && mTheme.isMV()) {
			mSelectedIcon.setVisibility(View.VISIBLE);
		}
	}

	public void setProgress(int progress){
		progressTitle.setText("" + progress);
	}
	
	/** 刷新视频 */
	public void refreshView() {
		if (mTheme != null) {
			if (mTheme.isDownloading()) {
				//正在下载
				progressTitle.setText("" + mTheme.downloadProgress);
				isDownloadImageview.setVisibility(View.GONE);
			} else if (mTheme.isOnline()) {
				//需要显示下载按钮
				isDownloadImageview.setVisibility(View.VISIBLE);
				progressTitle.setVisibility(View.GONE);
			} else {
				//已经下载，或已经是内置的
				isDownloadImageview.setVisibility(View.GONE);
			}

		}
	}

	/** 获取主题图标 */
	public BitmapImageView getIcon() {
		return mIcon;
	}

	@Override
	public void update(Observable observable, Object data) {

		try {
			if (data != null && mTheme != null) {// && !IsUtils.equals(mTheme.themeName, "default")
				String[] strs = (String[]) data;

				if (strs != null && strs.length > 1) {
					if (strs[1].equalsIgnoreCase("true")) {
						if (IsUtils.equals(mTheme.effectName, strs[0]) && mTheme.isMV()) {
							//mRootView.setBackgroundColor(mColorSelected);
							mSelectedIcon.setVisibility(View.VISIBLE);
						} else if (mTheme.isMV()) {
							//				mRootView.setBackgroundColor(mColorNormal);
							mSelectedIcon.setVisibility(View.GONE);
						}
					} else {
						if (IsUtils.equals(mTheme.effectName, strs[0])) {
							//mRootView.setBackgroundColor(mColorSelected);
							mSelectedIcon.setVisibility(View.VISIBLE);
						} else {
							//				mRootView.setBackgroundColor(mColorNormal);
							mSelectedIcon.setVisibility(View.GONE);
						}
					}
				}

			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}
}

package com.lcr.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap.Config;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.lcr.bean.VideoInfo;
import com.lcr.mplay.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.yixia.camera.demo.VCameraDemoApplication;


public class HomeAdapter extends CommonAdapter{

	private DisplayImageOptions mDisplayImageOptions;
	private boolean isCBVisible;
	private List<com.lcr.bean.VideoInfo> mSelcetedVideoInfos;
	
	@SuppressWarnings("unchecked")
	public HomeAdapter(Context context, List mDatas, int itemLayoutId,boolean isCBVisible) {
		super(context, mDatas, itemLayoutId);
		this.isCBVisible=isCBVisible;
		mSelcetedVideoInfos=new ArrayList<VideoInfo>();
	}

	@Override
	public void convert(ViewHolder helper, Object item) {
		final VideoInfo  item2=(VideoInfo)item;
		ImageView img=(ImageView)helper.getView(R.id.img_home_item_1);
		TextView text=(TextView)helper.getView(R.id.text_home_item_1);
		String url=((VideoInfo)item).getThum_url().replace("localhost", com.lcr.config.Config.localhost);
//		mImageLoader.displayImage(url,img, mDisplayImageOptions);
		((VCameraDemoApplication) VCameraDemoApplication.getContext()).setImge(url, img);
		text.setText(((VideoInfo)item).getDescription());
		CheckBox cb=(CheckBox) helper.getView(R.id.cb_home_item_1);
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mSelcetedVideoInfos.add((VideoInfo) item2);
				}
			}
		});
		if (isCBVisible) {
			cb.setVisibility(View.VISIBLE);
		}else {
			cb.setVisibility(View.GONE);
			cb.setChecked(false);
		}
	}

	public boolean isCBVisible() {
		return isCBVisible;
	}

	public void setCBVisible(boolean isCBVisible) {
		this.isCBVisible = isCBVisible;
	}
	
	public List<VideoInfo> getSelectedVideos(){
		return mSelcetedVideoInfos;
	}
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		mSelcetedVideoInfos=null;
		mSelcetedVideoInfos=new ArrayList<VideoInfo>();
	}
	
	
}

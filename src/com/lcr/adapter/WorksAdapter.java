package com.lcr.adapter;

import java.net.URL;
import java.util.List;

import com.lcr.bean.VideoInfo;
import com.lcr.mplay.R;
import com.lcr.utils.ThumbnailUtil;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.yixia.camera.demo.VCameraDemoApplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.widget.ImageView;
import android.widget.TextView;


public class WorksAdapter extends CommonAdapter{

	private LruCache<String, Bitmap> mMemoryCache;
	private Bitmap bitmap;
	private Thread thread;
	
//	private Handler handler=new Handler(){
//		public void handleMessage(android.os.Message msg) {
//			notifyDataSetChanged();
//		};
//	};
	
	public WorksAdapter(Context context, List mDatas, int itemLayoutId) {
		super(context, mDatas, itemLayoutId);
		int size=(int) (Runtime.getRuntime().maxMemory()/8);
		mMemoryCache=new LruCache<String, Bitmap>(size){
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes()*value.getHeight()/1024;
			};
		};
	}

	@Override
	public void convert(ViewHolder helper, Object item) {
		ImageView img=(ImageView)helper.getView(R.id.img_works_item_1);
		TextView title=(TextView)helper.getView(R.id.text_title_works_item_1);
		TextView des=(TextView)helper.getView(R.id.text_des_works_item_1);
		final String url=((VideoInfo)item).getThum_url().replace("localhost", com.lcr.config.Config.localhost);
		des.setText(((VideoInfo)item).getDescription());
		title.setText(((VideoInfo)item).getTitle());
//		mImageLoader.displayImage(url,img, mDisplayImageOptions);
		((VCameraDemoApplication) VCameraDemoApplication.getContext()).setImge(url, img);
//		bitmap=getBitmapFromMemCache(url);
//		if (bitmap==null) {
////			bitmap=getBitmapFromHttp(url);
//			thread=new Thread(){
//				public void run() {
//					bitmap=ThumbnailUtil.createVideoThumbnail(url, 80,80);
//					addBitmapToMemoryCache(url, bitmap);
//					handler.sendEmptyMessage(0);
//				};
//			};
//			thread.start();
//			
//		}
//		if (bitmap!=null) {
//			img.setImageBitmap(bitmap);
////			title.setText(bitmap.getRowBytes()*bitmap.getHeight()/1024+"");
//			title.setText(((VideoInfo)item).getTitle());
//		}
	}
	
	public Bitmap getBitmapFromHttp(String url){
		Bitmap bitmap=null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(url);  
        // 取得视频的长度(单位为毫秒)  
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);  
        // 取得视频的长度(单位为秒)  
        int seconds = Integer.valueOf(time) / 1000;  
        bitmap = retriever.getFrameAtTime();
		return bitmap;
	}
	
	 public synchronized void addBitmapToMemoryCache(String key, Bitmap bitmap) {
	        if (mMemoryCache.get(key) == null) {
	            if (key != null && bitmap != null)
	                mMemoryCache.put(key, bitmap);
	        }
	    }

	    public synchronized Bitmap getBitmapFromMemCache(String key) {
	        Bitmap bm = mMemoryCache.get(key);
	        if (key != null) {
	            return bm;
	        }
	        return null;
	    }
}

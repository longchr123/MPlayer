package com.lcr.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.view.Surface;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.lcr.bean.VideoInfo;
import com.lcr.mplay.R;
import com.lcr.utils.ThumbnailUtil;
import com.lcr.widget.MyTextureView;

public class HomeAdapter2 extends CommonAdapter implements
		SurfaceTextureListener, OnBufferingUpdateListener,
		OnCompletionListener, OnPreparedListener, OnVideoSizeChangedListener{

	private String url;
	private MediaPlayer mMediaPlayer;
	private LruCache<String, Bitmap> mMemoryCache;
	private Bitmap bitmap;
	private Thread thread;
	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			notifyDataSetChanged();
		};
	};

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HomeAdapter2(Context context, List mDatas, int itemLayoutId) {
		super(context, mDatas, itemLayoutId);
		int size=(int) (Runtime.getRuntime().maxMemory()/8);
		mMemoryCache=new LruCache<String, Bitmap>(size){
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes()*value.getHeight()/1024;
			};
		};
	}

	@Override
	public void convert(final ViewHolder helper, Object item) {
		TextView des = (TextView) helper.getView(R.id.text_content);
		final ImageView img = (ImageView) helper.getView(R.id.img_icon);
		final MyTextureView textureView= (MyTextureView) helper.getView(R.id.texturevideo);
		TextView user=(TextView)helper.getView(R.id.text_user);
		TextView comment=(TextView)helper.getView(R.id.text_comment);
		url = ((VideoInfo) item).getUrl().replace("localhost",
				com.lcr.config.Config.localhost);
		des.setText(((VideoInfo) item).getDescription());
		
		bitmap=getBitmapFromMemCache(url);
		if (bitmap==null) {
			thread=new Thread(){
				public void run() {
					bitmap=ThumbnailUtil.createVideoThumbnail(url, 288,200);
					addBitmapToMemoryCache(url, bitmap);
					handler.sendEmptyMessage(0);
				};
			};
			thread.start();
			
		}
		if (bitmap!=null) {
			img.setImageBitmap(bitmap);
		}
		
		img.setVisibility(View.VISIBLE);
		textureView.setVisibility(View.GONE);
		img.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				textureView.setVisibility(View.VISIBLE);
				textureView.setOnClickListener(this);
				textureView.setSurfaceTextureListener(HomeAdapter2.this);
				img.setVisibility(View.GONE);
				mMediaPlayer = new MediaPlayer();
				try {
					mMediaPlayer.setDataSource(url);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		textureView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				v.setVisibility(View.GONE);
				img.setVisibility(View.VISIBLE);
				mMediaPlayer.stop();
			}
		});
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
	
	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
			int height) {
		Surface s = new Surface(surface);
		try {
			mMediaPlayer.setSurface(s);
			mMediaPlayer.prepare();
			mMediaPlayer.setOnBufferingUpdateListener(this);
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.setOnVideoSizeChangedListener(this);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.start();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		mMediaPlayer.stop();
		return true;
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
	}

	@Override
	public void onPrepared(MediaPlayer mp) {

	}

	@Override
	public void onCompletion(MediaPlayer mp) {
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
	}

}

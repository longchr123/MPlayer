package com.lcr.utils;

import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;

public class ThumbnailUtil extends ThumbnailUtils {

	public static Bitmap createVideoThumbnail(String url, int width, int height) {
		Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		int kind = MediaStore.Video.Thumbnails.MINI_KIND;
		try {
			if (Build.VERSION.SDK_INT >= 14) {
				retriever.setDataSource(url, new HashMap<String, String>());
			} else {
				retriever.setDataSource(url);
			}
			bitmap = retriever.getFrameAtTime();
			
		} catch (IllegalArgumentException ex) {
		} catch (RuntimeException ex) {
		} finally {
			try {
				retriever.release();
			} catch (RuntimeException ex) {
			}
		}
		bitmap=BitmapUtil.scaleImage(bitmap, width, height);
//		if (kind == Images.Thumbnails.MICRO_KIND && bitmap != null) {
//			bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
//					ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
//		}
		return bitmap;
	}
}

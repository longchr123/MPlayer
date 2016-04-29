package com.yixia.camera.demo.ui.record.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lcr.mplay.R;
import com.yixia.camera.demo.VCameraDemoApplication;
import com.yixia.camera.demo.log.Logger;
import com.yixia.camera.demo.po.Video;
import com.yixia.camera.demo.ui.record.Crypto;
import com.yixia.camera.demo.ui.record.ImportVideoFolderActivity.VideoFolder;
import com.yixia.camera.demo.util.ViewHolderUtils;
import com.yixia.weibo.sdk.FFMpegUtils;
import com.yixia.weibo.sdk.util.FileUtils;
import com.yixia.weibo.sdk.util.StringUtils;

public class ImportVideoFolderAdapter extends BaseAdapter {
	
	private Context mContext;
	private List<VideoFolder> videoFolderList;
	private static Map<ImageView, String> mImageViews;
	/** 缩略图缓存目录 */
	private File mThumbCacheDir;
	private static final String[] THUMB_PROJECT = { MediaStore.Video.Thumbnails.DATA, MediaStore.Video.Thumbnails.VIDEO_ID };
	
	public ImportVideoFolderAdapter(Context context) {
		this.mContext = context;
		videoFolderList = new ArrayList<VideoFolder>();
		mImageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
		mThumbCacheDir = VCameraDemoApplication.getThumbCacheDirectory();
		if (mThumbCacheDir != null && !mThumbCacheDir.exists()) {
			mThumbCacheDir.mkdirs();
		}
	}
	
	public void updateVideoFolderData(List<VideoFolder> result){
		this.videoFolderList = result;
		this.notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return videoFolderList.size();
	}

	@Override
	public Object getItem(int position) {
		return videoFolderList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_import_image_folder, null);
		}

		final VideoFolder item = (VideoFolder) getItem(position);
		ImageView icon = ViewHolderUtils.getView(convertView, R.id.icon);
		TextView title = ViewHolderUtils.getView(convertView, R.id.title);
		TextView count = ViewHolderUtils.getView(convertView, R.id.count);

		if (StringUtils.isNotEmpty(item.url)) {
			if (item.faild) {

			} else {
				/** 加载视频截图 */
				loadVideoThumb(icon, item.video);
			}
		}
		icon.setTag(item);

		title.setText(item.name);// + "\n" + item.path);//
		count.setText(item.count + "");

		return convertView;
	}

	public Uri getFileUri(String path) {

		Logger.e("simon", "getFile Uri>>>" + path);

		return Uri.parse("file:///" + path);
	}
	
	private void loadVideoThumb(final ImageView view, final Video video){
		if (mThumbCacheDir == null || video == null || StringUtils.isEmpty(video.url)) {
			return;
		}

		final String videoPath = video.url;

		mImageViews.put(view, videoPath);

		if (StringUtils.isNotEmpty(video.thumb)) {
			view.setImageURI(getFileUri(video.thumb));
			return;
		}
		new Thread(new Runnable() {

			/** 现实缩略图 */
			private void showThumb() {
				if (video != null && !Thread.currentThread().isInterrupted() && mContext != null) {
					((Activity) mContext).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							String tag = mImageViews.get(view);
							if (tag != null && tag.equals(videoPath)) {
								// if (video.equals(view.getTag())) {
								// Logger.e("[ImportHelper]loadVideoThumb...1"
								// + video.faild);
								if (video.faild) {
									view.setImageResource(R.drawable.import_image_default);
								} else if (StringUtils.isNotEmpty(video.thumb)) {
									view.setImageURI(getFileUri(video.thumb));
								}
							}
						}
					});
				}
			}

			@Override
			public void run() {
				// Logger.e("[ImportHelper]loadVideoThumb...2" +
				// video.faild);
				// 先检测截图缓存文件夹是否已经存在截图
				final String key = Crypto.md5(videoPath);
				final File thumbFile = new File(mThumbCacheDir, key + ".jpg");
				if (FileUtils.checkFile(thumbFile)) {
					video.thumb = thumbFile.getPath();
					showThumb();
					return;
				}

				// Logger.e("[ImportHelper]loadVideoThumb...3" +
				// video.faild);

				// 从系统相册缓冲中取
				final ContentResolver mContentResolver = mContext.getContentResolver();
				if (mContentResolver != null) {
					Cursor thumbCursor = mContentResolver.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, THUMB_PROJECT, MediaStore.Video.Thumbnails.VIDEO_ID + "=" + video._id, null, null);
					if (thumbCursor != null) {
						// 检测有没有截图，没有截图马上截图
						if (thumbCursor.getCount() == 0) {
							try {
								Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(mContentResolver, video._id, MediaStore.Video.Thumbnails.MINI_KIND, null);
								if (bitmap != null) {
									if (!bitmap.isRecycled())
										bitmap.recycle();
									bitmap = null;
								}
							} catch (OutOfMemoryError e) {
								Logger.e(e);
							} catch (Exception e) {
								Logger.e(e);
							}
							thumbCursor.close();
							thumbCursor = mContentResolver.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, THUMB_PROJECT, MediaStore.Video.Thumbnails.VIDEO_ID + "=" + video._id, null, null);
						}

						if (thumbCursor != null && thumbCursor.moveToFirst()) {
							String thumb = thumbCursor.getString(0);
							thumbCursor.close();
							if (FileUtils.checkFile(thumb)) {
								video.thumb = thumb;
								showThumb();
								return;
							}
						}
						if (thumbCursor != null && !thumbCursor.isClosed())
							thumbCursor.close();
					}
				}

				if (Thread.currentThread().isInterrupted())
					return;

				// 系统截图失败，调用FFMPEG截图，截图第一帧
				try {
					Logger.e("samuel", "调用ffmpeg截屏");
					if (FFMpegUtils.captureThumbnails(videoPath, thumbFile.getPath(), video.orientation)) {
						if (Thread.currentThread().isInterrupted())
							return;
						if (FileUtils.checkFile(thumbFile)) {
							video.thumb = thumbFile.getPath();
							showThumb();
							return;
						}
					}
				} catch (Exception e) {
					Logger.e(e);
				}

				if (Thread.currentThread().isInterrupted())
					return;

				// 截图完全失败，视频有问题
				video.faild = true;
				showThumb();
			}
		}).run();
		
	}

}

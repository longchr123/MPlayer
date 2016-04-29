package com.yixia.camera.demo;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;

import com.lcr.mplay.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.yixia.camera.demo.service.AssertService;
import com.yixia.weibo.sdk.VCamera;
import com.yixia.weibo.sdk.util.DeviceUtils;
import com.yixia.weibo.sdk.util.FileUtils;
import com.yixia.weibo.sdk.util.ToastUtils;

public class VCameraDemoApplication extends Application {

	private static VCameraDemoApplication application;
	public static ImageLoader mImageLoader = ImageLoader.getInstance();// 初始化获取实例
	private static DisplayImageOptions mDisplayImageOptions;

	@Override
	public void onCreate() {
		super.onCreate();
		application = this;
		initImageLoader();
		// 设置拍摄视频缓存路径
		File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		if (DeviceUtils.isZte()) {
			if (dcim.exists()) {
				VCamera.setVideoCachePath(dcim + "/Camera/VCameraDemo/");
			} else {
				VCamera.setVideoCachePath(dcim.getPath().replace("/sdcard/", "/sdcard-ext/") + "/Camera/VCameraDemo/");
			}
		} else {
			VCamera.setVideoCachePath(dcim + "/Camera/VCameraDemo/");
		}
		// 开启log输出,ffmpeg输出到logcat
		VCamera.setDebugMode(true);
		// 初始化拍摄SDK，必须
		VCamera.initialize(this);

		//解压assert里面的文件
		startService(new Intent(this, AssertService.class));
	}

	public static Context getContext() {
		return application;
	}
	
	public final static int AVAILABLE_SPACE = 200;//M
	
	/**
	 * 检测用户手机是否剩余可用空间200M以上
	 * @return
	 */
	public static boolean isAvailableSpace() {
		if (application == null) {
			return false;
		}
		//检测磁盘空间
		if (FileUtils.showFileAvailable(application) < AVAILABLE_SPACE) {
			ToastUtils.showToast(application, application.getString(R.string.record_check_available_faild, AVAILABLE_SPACE));
			return false;
		}

		return true;
	}

	public static File getGifCacheDirectory() {
		if (application != null)
			return FileUtils.getCacheDiskPath(application, "gif");//vineApplication.getExternalCacheDir() + "/cache/gif/";
		return null;
	}
	
	/** 
	 * 视频截图目录 
	 */
	public static File getThumbCacheDirectory() {
		if (application != null)
			return FileUtils.getCacheDiskPath(application, "thumbs");//vineApplication.getExternalCacheDir() + "/cache/thumbs/";
		return null;
	}
	
	public void initImageLoader(){
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext()).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.memoryCacheExtraOptions(96, 120).build();// 初始化图片加载器的配置
		// Initialize ImageLoader with configuration.
		mImageLoader.init(config);

		// 使用DisplayImageOption.Builder()创建DisplayImageOptions
		mDisplayImageOptions = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.ic_launcher)
				// 设置图片下载期间显示的图片
				.showImageForEmptyUri(R.drawable.ic_launcher)
				// 设置图片Uri为空或是错误的时候显示的图片
				.showImageOnFail(R.drawable.ic_launcher)
				// 设置图片加载或解码过程中发生错误显示的图片
				.cacheInMemory(true)
				// 设置下载的图片是否缓存在内存中
				.cacheOnDisc(true)
				// 设置下载的图片是否缓存在SD卡中
				// .displayer(new RoundedBitmapDisplayer(20)) // 设置成圆角图片
				.bitmapConfig(Config.RGB_565)
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT).build(); // 创建配置过的DisplayImageOption对象
		// 上面的默认图片或者无法加载情况下的图片开发者可以自己设置，当然，可以设置不同的显示图片。我这里为了方便，采用同一幅图片作为默认图片
	}
	public static void setImge(String url,ImageView v){
		mImageLoader.displayImage(url,v, mDisplayImageOptions);
	}
}

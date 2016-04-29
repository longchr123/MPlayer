package com.yixia.camera.demo.po;

import java.util.Locale;

public class VideoThumb {

	public String url;
	public String thumb;
	public long _id;
	/** 视频有问题，截图失败 */
	public boolean faild;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.toLowerCase(Locale.CHINESE).hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VideoThumb other = (VideoThumb) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.toLowerCase(Locale.CHINESE).equals(other.url.toLowerCase(Locale.CHINESE)))
			return false;
		return true;
	}

}

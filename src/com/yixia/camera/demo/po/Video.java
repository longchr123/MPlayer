package com.yixia.camera.demo.po;

public final class Video extends VideoThumb {
	public boolean checked;
	public com.yixia.weibo.sdk.model.MediaObject$MediaPart part;
	public long added;
	public int duration;
	public long size;
	public int orientation;
	/** md5值 */
	public String key;

	// public Video(String url) {
	// this.url = url;
	// }

	public Video(String url, long added, long size) {
		this.url = url;
		this.added = added;
		this.size = size;
	}

	public Video(String url, long added, int duration) {
		this.url = url;
		this.added = added;
		this.duration = duration;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && getClass() == obj.getClass() && url != null
				&& url.equals(((Video) obj).url)) {
			// 继续比较相等性或直接返回true
			return true;
		}
		return false;
	}
}

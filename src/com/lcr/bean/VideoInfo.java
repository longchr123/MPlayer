package com.lcr.bean;

import java.io.Serializable;


import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

@SuppressWarnings("serial")
public class  VideoInfo implements Parcelable{

	private int _id;

	private String url;

	private int cate_id;

	private int user_id;

	private String title;

	private String description;

	private String duration;

	private String thum_url;

	private String updatetime;
	
	private String size;

	public VideoInfo() {
		super();
	}

	public VideoInfo(int _id, String url, int cate_id, int user_id,
			String title, String description, String duration, String thum_url,
			String updatetime, String size) {
		super();
		this._id = _id;
		this.url = url;
		this.cate_id = cate_id;
		this.user_id = user_id;
		this.title = title;
		this.description = description;
		this.duration = duration;
		this.thum_url = thum_url;
		this.updatetime = updatetime;
		this.size = size;
	}

	public void set_Id(int id) {
		this._id = id;
	}

	public int get_Id() {
		return this._id;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return this.url;
	}

	public void setCate_id(int cate_id) {
		this.cate_id = cate_id;
	}

	public int getCate_id() {
		return this.cate_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}

	public int getUser_id() {
		return this.user_id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return this.title;
	}

	public void setDescription(String desciption) {
		this.description = desciption;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getDuration() {
		return this.duration;
	}

	public void setThum_url(String thum_url) {
		this.thum_url = thum_url;
	}

	public String getThum_url() {
		return this.thum_url;
	}

	public void setUpdatetime(String updatetime) {
		this.updatetime = updatetime;
	}

	public String getUpdatetime() {
		return this.updatetime;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}
	
	@Override
	public String toString() {
		return " [id=" + _id + ", url=" + url + ", cate_id=" + cate_id
				+ ", user_id=" + user_id + ", title=" + title + ", desciption="
				+ description + ", duration=" + duration + ", thum_url="
				+ thum_url + ", updatetime=" + updatetime + ", size=" + size
				+ "]";
	}

	public static final Parcelable.Creator<VideoInfo> CREATOR = new Creator<VideoInfo>() {
		@Override
		public VideoInfo createFromParcel(Parcel source) {
			VideoInfo video = new VideoInfo(source.readInt(),source.readString(),source.readInt(),source.readInt()
					,source.readString(), source.readString(),source.readString(),source.readString(),source.readString(),source.readString());
			return video;
		}

		@Override
		public VideoInfo[] newArray(int size) {
			return new VideoInfo[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(_id);
		dest.writeString(url);
		dest.writeInt(cate_id);
		dest.writeInt(user_id);
		dest.writeString(title);
		dest.writeString(description);
		dest.writeString(duration);
		dest.writeString(thum_url);
		dest.writeString(updatetime);
		dest.writeString(size);
	}
}

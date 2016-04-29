package com.lcr.bean;

import java.io.Serializable;

import android.graphics.Bitmap;

public class UserInfo implements Serializable {

	private String username;
	private Bitmap usericon;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Bitmap getUsericon() {
		return usericon;
	}

	public void setUsericon(Bitmap usericon) {
		this.usericon = usericon;
	}

}

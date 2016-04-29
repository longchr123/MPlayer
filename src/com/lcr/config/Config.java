package com.lcr.config;

import com.lcr.mplay.R;

public class Config {

	public static String localhost="192.168.22.1";
	
	public static final boolean SHOW_LOGS = true;
	
	/**背景存储的数据*/
	public static String bgKey = "BackgroundSelectedItem";
	public static int[] bgIds = { R.drawable.bg_1, R.drawable.bg_2 };
	public static String[] bgItems = { "仰望天空", "天空蓝" };
	
	/**布局格式切换*/
	public static String formatKey = "FormatSelectedItem";
	public static String[] formatItems = { "瀑布流格式", "单列播放格式" };
	public static int[] formatIds = { 0,1 };
}

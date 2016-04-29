package com.lcr.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;


public class FormatUtils {

	private StringBuilder mFormatBuilder;
	private Formatter mFormatter;
	
	public  FormatUtils() {
		mFormatBuilder=new StringBuilder();
		mFormatter=new Formatter(mFormatBuilder,Locale.getDefault());
	}
	
	/**
	 * �Ѻ���תΪ1:20:30��ʽ
	 * @param time
	 * @return
	 */
	public String  stringForTime(int time) {
		int totalSeconds=time/1000;
		int seconds=totalSeconds%60;
		int minutes=(totalSeconds/60)%60;
		int hours=totalSeconds/3600;
		mFormatBuilder.setLength(0);
		if (hours>0) {
			return mFormatter.format("%d:%02d:%02d",hours,minutes,seconds).toString();
		}else{
			return mFormatter.format("%02d:%02d",minutes,seconds).toString();
		}
	}
	
	
	/**
	 * ������ת����  00:00:00
	 * */
	public static String msToMin(long ms) {
		long hour =  ms/1000/60/60;
		long min = ms/1000/60%60;
		long s = ms/1000%60;
		
		String length = "";
		if (hour<10) {
			length+="0"+hour;
		}else{
			length+=hour;
		}
		length+=":";
		
		if (min<10) {
			length+="0"+min;
		}else{
			length+=min;
		}
		length+=":";
		
		if (s<10) {
			length+="0"+s;
		}else{
			length+=s;
		}
		return length;
	}
	
	
	/**
	 * �õ���ǰϵͳʱ��
	 * @return
	 */
	public String getSystemTime() {
		SimpleDateFormat format=new SimpleDateFormat("HH:mm:ss");
		return format.format(new Date());
	}
	
	//�ж��Ƿ���������Դ
	public boolean isNetUri(String path){
		boolean result = false;
		if (path !=null &&path.contains("http")||path.contains("rtsp")||path.contains("MMS")) {
			result=true;
		}
		return result;
	}
	
	
}

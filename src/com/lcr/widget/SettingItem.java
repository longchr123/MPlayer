package com.lcr.widget;

import com.lcr.mplay.R;

import android.R.integer;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SettingItem extends RelativeLayout {

	 private TextView mTitleText;
	    private TextView mContentText;
	    private ImageView mImg;
	
	    private void init(Context context){
	        //最后一个参数：添加谁进来，就是R.layout.setting_item的父亲，也就是说把布局文件挂载在传进来的这个控件上
	        View view=View.inflate(context,R.layout.setting_item,this);
	        mTitleText= (TextView)view.findViewById(R.id.text_title);
	        mContentText= (TextView) view.findViewById(R.id.text_content);
	        mImg=(ImageView) view.findViewById(R.id.img);
	    }
	    //在代码实例化中使用
	    public SettingItem(Context context) {
	        super(context);
	        init(context);
	    }
	    //在布局文件实例化时使用
	    public SettingItem(Context context, AttributeSet attrs) {
	        super(context, attrs);
	        init(context);
	        //attrs与values中自建的attrs相关联，以命名空间的方法取出title的值
	        String name=attrs.getAttributeValue("http://schemas.android.com/apk/res/com.lcr.mplay","titleText");
	        mTitleText.setText(name);
	        String content=attrs.getAttributeValue("http://schemas.android.com/apk/res/com.lcr.mplay","contentText");
	        mContentText.setText(content);
 	        int img=attrs.getAttributeIntValue("http://schemas.android.com/apk/res/com.lcr.mplay","img", R.drawable.ic_launcher);
 	        mImg.setBackgroundResource(img);
	    }
	    //要设置样式的时候使用
	    public SettingItem(Context context, AttributeSet attrs, int defStyleAttr) {
	        super(context, attrs, defStyleAttr);
	        init(context);
	    }
	    //设置组合控件的状态信息
	    public void setContent(String text){
	        mContentText.setText(text);
	    }
	    public void setTitle(String text){
	    	mTitleText.setText(text);
	    }
	    public void setImg(int res){
	    	mImg.setBackgroundResource(res);
	    }
}

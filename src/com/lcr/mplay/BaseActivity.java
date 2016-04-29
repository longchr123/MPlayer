package com.lcr.mplay;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lcr.utils.FormatUtils;

public abstract class BaseActivity extends Activity implements OnClickListener {

	private Button mLeftButton;
	private Button mRightButton;
	private TextView mTitleTextView;
	private LinearLayout mChildLinearLayout;
	private FrameLayout mTitleFrameLayout;
	public  FormatUtils utils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_base);
		initViews();
		setOnClickListener();
	}

	private void setOnClickListener() {
		mLeftButton.setOnClickListener(this);
		mRightButton.setOnClickListener(this);
	}

	private void initViews() {
		utils=new FormatUtils();
		mLeftButton = (Button) findViewById(R.id.btn_left);
		mRightButton = (Button) findViewById(R.id.btn_right);
		mTitleTextView = (TextView) findViewById(R.id.tv_title);
		mChildLinearLayout=(LinearLayout) findViewById(R.id.ll_child);
		mTitleFrameLayout=(FrameLayout) findViewById(R.id.fl_titlebar);
		//����Ӳ���
		View child=setContentView();
		if (child!=null) {
			LayoutParams params=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		mChildLinearLayout.addView(child,params);
		}
		
	}

	public abstract View setContentView() ;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_left:
			leftButtonClick();
			break;
		case R.id.btn_right:
			RightButtonClick();
			break;
		default:
			break;
		}
	}
	
	
	/**
	 * ���ð�ť����ʾ״̬
	 */
	public void setLeftButtonVisiblity(int visibility) {
		mLeftButton.setVisibility(visibility);
	}
	public void setRightButtonVisiblity(int visibility) {
		mRightButton.setVisibility(visibility);
	}

	public void setTitle(String title) {
		mTitleTextView.setText(title);
	}
	
	//���ñ������Ƿ�����
	public void setTitleBar(int visibility) {
		mTitleFrameLayout.setVisibility(visibility);
	}
	
	
	/**
	 * ������ʵ�ֵ���ұ߰�ť
	 */
	public abstract void RightButtonClick();

	public abstract void leftButtonClick();

	/**
	 * �������Ĳ���
	 * @param s
	 */
	public void SimpleToast(String s){
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}
	
}

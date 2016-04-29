package com.lcr.mplay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.lcr.config.Config;
import com.lcr.fragment.HomePageVideoFragment;
import com.lcr.fragment.HomePageVideoFragment2;
import com.lcr.fragment.LocalVideoFragment;
import com.lcr.fragment.MyWorksFragment;
import com.lcr.fragment.SettingFragment;
import com.lcr.widget.CustomViewPager;
import com.yixia.camera.demo.ui.record.MediaRecorderActivity;

public class MainActivity extends FragmentActivity implements
		OnCheckedChangeListener{

	private int PAGE_SIZE = 4;

	private HomePageVideoFragment mHomePageFragment;
	private HomePageVideoFragment2 mHomePageFragment2;
	private LocalVideoFragment mLocalVideoFragment;
	private MyWorksFragment mMyWorksFragment;
	private SettingFragment mSettingFragment;
	private TextView mTitleText;
	public CustomViewPager mViewPager;
	private DrawerLayout mDrawerLayout;
	public RadioGroup mGroup;
	private boolean isOpen;
	private View mLeftDrawer;
	private TextView mUserText;
	private SharedPreferences mSharedPreferences;
	private Editor mEditor;
	public LinearLayout mPlayLayout;
	public Button mPlayButton;
	public Button mCancleButton;
	
	private RadioButton mHomeButton;
	private RadioButton mLocalButton;
	private RadioButton mRecoderButton;
	private RadioButton mWorksButton;
	private RadioButton mSettingButton;
	
	private int mBGImage;
	private int[] ids = { R.drawable.bg_1, R.drawable.bg_2 };
	
	private int mFormat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initViews();
		getDatas();
		initFragment();
		bindFragmentToVp();
		setOnClick();
		// 判断是否从录音界面进入
		if (getIntent().getBooleanExtra("myworks", false)) {
			mWorksButton.setChecked(true);
		} else {
			mViewPager.setCurrentItem(0);
		}
		initDatas();
	}

	private void getDatas() {
		mSharedPreferences = getSharedPreferences("setting",
				this.MODE_PRIVATE);
		//设置用户设置的背景
		mBGImage=mSharedPreferences.getInt(Config.bgKey, 0);
		mFormat=mSharedPreferences.getInt(Config.formatKey, 0);
	}

	@SuppressWarnings({ "deprecation" })
	private void initDatas() {
		if (mBGImage==-1) {
			mViewPager.setBackgroundDrawable(Drawable
					.createFromPath("/sdcard/background.jpg"));
		}else {
			mViewPager.setBackgroundResource(ids[mBGImage]);
		}
	}

	private void setOnClick() {
		mGroup.setOnCheckedChangeListener(this);
	}

	private void initFragment() {
		mHomePageFragment = new HomePageVideoFragment();
		mHomePageFragment2 = new HomePageVideoFragment2();
		mLocalVideoFragment = new LocalVideoFragment();
		mMyWorksFragment = new MyWorksFragment();
		mSettingFragment = new SettingFragment();
		mViewPager = (CustomViewPager) findViewById(R.id.vp_menu);
		mViewPager.setOffscreenPageLimit(4);
		mViewPager.setPagingEnabled(false);
	}

	private void initViews() {
		mSharedPreferences = getSharedPreferences("xplay", MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
		mTitleText = (TextView) findViewById(R.id.text_title);
		mGroup = (RadioGroup) findViewById(R.id.group_menu);
		mHomeButton = (RadioButton) findViewById(R.id.btn_home);
		mLocalButton = (RadioButton) findViewById(R.id.btn_local);
		mRecoderButton = (RadioButton) findViewById(R.id.btn_recoder);
		mWorksButton = (RadioButton) findViewById(R.id.btn_works);
		mSettingButton = (RadioButton) findViewById(R.id.btn_setting);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mLeftDrawer = (View) findViewById(R.id.list_left_drawer);
		mPlayLayout=(LinearLayout) findViewById(R.id.layout_menu);
		mCancleButton=(Button) mPlayLayout.findViewById(R.id.layout_cancel);
		mPlayButton=(Button) mPlayLayout.findViewById(R.id.layout_play);
		mLeftDrawer.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		mUserText=(TextView) mLeftDrawer.findViewById(R.id.tv_item0);
		mUserText.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "mUserText", 1000).show();
			}
		});
		mUserText.setText(mSharedPreferences.getString("username", "用户名"));
	}

	/**
	 * 左上角的Menu点击事件
	 * @param view
	 */
	public void MenuInfo(View view) {
		if (mDrawerLayout.isDrawerOpen(mLeftDrawer)) {
			isOpen=true;
		}else {
			isOpen=false;
		}
		if (isOpen) {
			mDrawerLayout.closeDrawer(mLeftDrawer);
			isOpen=!isOpen;
		}else if(mUserText.getText().equals("用户名")){
			startActivity(new Intent(MainActivity.this,LoginActivity.class));
		}else {
			mDrawerLayout.openDrawer(mLeftDrawer);
			isOpen=!isOpen;
		}
		
	}

	private void bindFragmentToVp() {
		mViewPager.setAdapter(new FragmentPagerAdapter(
				getSupportFragmentManager()) {

			@Override
			public int getCount() {
				return PAGE_SIZE;
			}

			@Override
			public Fragment getItem(int arg0) {
				switch (arg0) {
				case 0:
					if (mFormat==0) {
						return mHomePageFragment;
					}else {
						return mHomePageFragment2;
					}
				case 1:
					return mLocalVideoFragment;
				case 2:
					return mMyWorksFragment;
				default:
					return mSettingFragment;
				}
			}
		});
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.btn_home:
			mViewPager.setCurrentItem(0);
			mTitleText.setText("首页");
			break;
		case R.id.btn_local:
			mViewPager.setCurrentItem(1);
			mTitleText.setText("本地视频");
			break;
		case R.id.btn_recoder:
			Intent intent = new Intent(MainActivity.this,
					MediaRecorderActivity.class);
			startActivity(intent);
		case R.id.btn_works:
			mViewPager.setCurrentItem(2);
			mTitleText.setText("我的作品");
			mWorksButton.setChecked(true);
			break;
		case R.id.btn_setting:
			mViewPager.setCurrentItem(3);
			mTitleText.setText("设置");
			break;
		}
	}
}

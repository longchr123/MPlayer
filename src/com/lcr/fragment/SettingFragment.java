package com.lcr.fragment;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lcr.config.Config;
import com.lcr.mplay.MainActivity;
import com.lcr.mplay.R;
import com.lcr.widget.SettingItem;

public class SettingFragment extends Fragment implements OnClickListener {

	private View mContentView;
	private SharedPreferences mSharedPreferences;
	private Editor mEditor;
	private SettingItem mSettingFormat;
	private SettingItem mSettingBackground;
	private int mSettingBackgroundSelectedItem;
	private int mSettingFormatSelectedItem;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			mContentView = inflater.inflate(R.layout.fragment_setting, null);
			init();
			mSharedPreferences = getActivity().getSharedPreferences("setting",
					getActivity().MODE_PRIVATE);
			mEditor = mSharedPreferences.edit();
			mSettingBackgroundSelectedItem = mSharedPreferences.getInt(
					Config.bgKey, 0);
			mSettingFormatSelectedItem= mSharedPreferences.getInt(
					Config.formatKey, 0);
		}
		return mContentView;
	}

	private void init() {
		mSettingBackground = (SettingItem) mContentView
				.findViewById(R.id.setting_layout_bg);
		mSettingFormat = (SettingItem) mContentView
				.findViewById(R.id.setting_layout_format);
		mSettingBackground.setOnClickListener(this);
		mSettingFormat.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setting_layout_format:
			ShowDialog(mSettingFormat, "首页面布局格式切换", Config.formatItems,
					Config.formatIds, mSettingFormatSelectedItem);
			break;
		case R.id.setting_layout_bg:
			ShowDialog(mSettingBackground, "设置背景", Config.bgItems,
					Config.bgIds, mSettingBackgroundSelectedItem);
			break;
		default:
			break;
		}
	}

	/**
	 * 
	 * @param settingItem
	 *            是点击了哪一个控件
	 * @param title
	 *            标题
	 * @param items
	 *            dialog中的item标题
	 * @param ids
	 *            资源id
	 * @param first
	 *            第一个选择的item
	 */
	public void ShowDialog(final SettingItem settingItem, String title,
			final String[] items, final int[] ids, int first) {
		AlertDialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(title);
		builder.setSingleChoiceItems(items, first,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						settingItem.setContent(items[which]);
						switch (settingItem.getId()) {
						case R.id.setting_layout_bg:
							((MainActivity) getActivity()).mViewPager
									.setBackgroundResource(ids[which]);
							mSettingBackgroundSelectedItem = which;
							mEditor.putInt(Config.bgKey, which).commit();
							break;
						case R.id.setting_layout_format:
							if (mSettingFormatSelectedItem != which) {
								mSettingFormatSelectedItem = which;
								mEditor.putInt(Config.formatKey, which)
										.commit();
								Toast.makeText(getActivity(),
										"模式已经设置，重起应用即可生效", Toast.LENGTH_SHORT)
										.show();
							}
							break;
						default:
							break;
						}
						dialog.dismiss();
					}
				});
		if (settingItem.getId() == R.id.setting_layout_bg) {
			builder.setPositiveButton("从相册中获取图片",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(Intent.ACTION_PICK);
							intent.setType("image/*");
							startActivityForResult(intent, 0);
							dialog.dismiss();
						}
					});
			builder.setNegativeButton("拍照",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String state = Environment
									.getExternalStorageState();
							if (state.equals(Environment.MEDIA_MOUNTED)) {
								Intent getImageByCamera = new Intent(
										"android.media.action.IMAGE_CAPTURE");
								startActivityForResult(getImageByCamera, 1);
							} else {
								Toast.makeText(getActivity(), "请确认已经插入SD卡",
										Toast.LENGTH_LONG).show();
							}
							dialog.dismiss();
						}
					});
		}
		dialog = builder.create();
		dialog.show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Bundle bundle = data.getExtras();
		Uri uri = data.getData();
		Bitmap photo = null;
		if (bundle != null) {
			photo = (Bitmap) bundle.get("data");
		} else if (uri != null) {
			try {
				photo = MediaStore.Images.Media.getBitmap(getActivity()
						.getContentResolver(), uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Toast.makeText(getActivity(), "err****", Toast.LENGTH_LONG).show();
			return;
		}

		try {
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream("/sdcard/background.jpg", false));
			photo.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 从相册中获取图片
		if (requestCode == 0) {
			((MainActivity) getActivity()).mViewPager
					.setBackgroundDrawable(new BitmapDrawable(photo));
		} else if (requestCode == 1) {
			// 从拍照中获取图片
			((MainActivity) getActivity()).mViewPager
					.setBackgroundDrawable(Drawable
							.createFromPath("/sdcard/background.jpg"));
		}
		mEditor.putInt(Config.bgKey, -1).commit();
	}

}

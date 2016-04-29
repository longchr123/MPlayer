package com.lcr.mplay;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lcr.config.Config;
import com.lcr.utils.MD5Util;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

public class LoginActivity extends Activity implements OnClickListener {

	private TextView mRegisterView;
	private EditText mUserText;
	private EditText mPwdText;
	private Button mLoginButton;
	private SharedPreferences mSharedPreferences;
	private Editor mEditor;
	private ImageView mImgIcon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		init();
	}

	private void init() {
		mSharedPreferences = getSharedPreferences("xplay", MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
		mRegisterView = (TextView) findViewById(R.id.text_register);
		mRegisterView.setOnClickListener(this);
		mLoginButton = (Button) findViewById(R.id.btn_login);
		mLoginButton.setOnClickListener(this);
		mUserText = (EditText) findViewById(R.id.et_name);
		mPwdText = (EditText) findViewById(R.id.et_pwd);
		mImgIcon = (ImageView) findViewById(R.id.img_icon);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.text_register:
			startActivity(new Intent(this, RegisterActivity.class));
			break;
		case R.id.btn_login:
			String pwd = null;
			try {
				pwd = MD5Util.md5Encode(mPwdText.getText().toString().trim());
			} catch (Exception e) {
				e.printStackTrace();
			}
			login(mUserText.getText().toString().trim(), pwd);
			break;
		default:
			break;
		}
	}

	private void login(final String username,String password) {
		//查询网络数据库，号码是否注册过了
		String url="http://"+Config.localhost+":8080/axis2/services/XPlayServer/loginCheck";
		HttpUtils httpUtils=new HttpUtils();
		RequestParams params=new RequestParams();
		params.addBodyParameter("username",username);
		params.addBodyParameter("password",password);
		httpUtils.send(HttpMethod.POST, url, params,new RequestCallBack<String>() {

			@Override
			public void onFailure(HttpException arg0, String arg1) {
			}

			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				String result = responseInfo.result
						.split("<ns:return>")[1];
				result = result.split("</ns:return>")[0];
				if (result.equals("0")) {
					Toast.makeText(LoginActivity.this, "用户不存在", 1000).show();
					return;
				}
				if (result.equals("1")) {
					Toast.makeText(LoginActivity.this, "密码不正确 ", 1000).show();
					return;
				}
				if (result.equals("2")) {
					Toast.makeText(LoginActivity.this, "用户已登录", 1000).show();
					return;
				}
				mEditor.putString("username", username).commit();
				startActivity(new Intent(LoginActivity.this,MainActivity.class));
			}
		});
	}

	private Bitmap drawableToBitamp(Drawable drawable) {
		Bitmap bitmap;
		int w = drawable.getIntrinsicWidth();
		int h = drawable.getIntrinsicHeight();
		System.out.println("Drawable转Bitmap");
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565;
		bitmap = Bitmap.createBitmap(w, h, config);
		// 注意，下面三行代码要用到，否在在View或者surfaceview里的canvas.drawBitmap会看不到图
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, w, h);
		drawable.draw(canvas);
		return bitmap;
	}

}

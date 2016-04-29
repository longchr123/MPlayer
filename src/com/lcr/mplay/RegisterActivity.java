package com.lcr.mplay;

import com.lcr.config.Config;
import com.lcr.utils.MD5Util;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity implements OnClickListener {

	private EditText mTelText;
	private EditText mUserText;
	private EditText mPwdText;
	private EditText mConfirmText;
	private Button mRegisterButton;
	private Button mCancleButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		mTelText = (EditText) findViewById(R.id.et_tel);
		mUserText = (EditText) findViewById(R.id.et_name);
		mPwdText = (EditText) findViewById(R.id.et_pwd);
		mConfirmText = (EditText) findViewById(R.id.et_pwd_confirm);
		mRegisterButton = (Button) findViewById(R.id.btn_login);
		mCancleButton = (Button) findViewById(R.id.btn_cancle);
		mRegisterButton.setOnClickListener(this);
		mCancleButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login:
			String tel = mTelText.getText().toString().trim();
			String user = mUserText.getText().toString().trim();
			String pwd = null;
			String confirm = null;
			try {
				pwd = MD5Util.md5Encode(mPwdText.getText().toString().trim());
				confirm = MD5Util.md5Encode(mConfirmText.getText()
						.toString().trim());
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!pwd.equals(confirm)) {
				Toast.makeText(this, "两次密码输入不同", 1000).show();
				return;
			}
			isExist(tel,user,pwd);
			break;
		case R.id.btn_cancle:
			finish();
			break;

		default:
			break;
		}
	}

	private void isExist(String tel,String username,String password) {
		//查询网络数据库，号码是否注册过了
		String url="http://"+Config.localhost+":8080/axis2/services/XPlayServer/registerCheck";
		HttpUtils httpUtils=new HttpUtils();
		RequestParams params=new RequestParams();
		params.addBodyParameter("tel", tel);
		params.addBodyParameter("username",username);
		params.addBodyParameter("password",password);
		httpUtils.send(HttpMethod.POST, url, params,new RequestCallBack<String>() {

			@Override
			public void onFailure(HttpException arg0, String arg1) {
				Log.e("Register", arg0.toString()+":   "+arg1);
			}

			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				String result = responseInfo.result
						.split("<ns:return>")[1];
				result = result.split("</ns:return>")[0];
				if (result.equals("0")) {
					Toast.makeText(RegisterActivity.this, "手机号已经注册过了，请直接登录", 1000).show();
					return;
				}
				if (result.equals("1")) {
					Toast.makeText(RegisterActivity.this, "用户名已存在", 1000).show();
					return;
				}
				if (result.equals("2")) {
					Toast.makeText(RegisterActivity.this, "注册成功，请登录", 1000).show();
					finish();
					return;
				}
				if (result.equals("3")) {
					Toast.makeText(RegisterActivity.this, "注册失败，请重新注册", 1000).show();
					return;
				}
			}
		});
	}
}

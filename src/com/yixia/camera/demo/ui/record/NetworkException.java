package com.yixia.camera.demo.ui.record;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class NetworkException {
	public static boolean isNetworkException(Exception e) {
		return (e instanceof SocketException) || (e instanceof SocketTimeoutException) || (e instanceof UnknownHostException);
	}
}
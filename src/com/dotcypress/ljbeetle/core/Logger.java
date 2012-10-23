package com.dotcypress.ljbeetle.core;

import android.util.Log;

public class Logger {
	private static final String LOG_TAG = "LjBeetle";

	public static void verbose(String message) {
		if (Config.DEBUG_MODE) {
			Log.v(LOG_TAG, message);
		}
	}

	public static void debug(String message) {
		if (Config.DEBUG_MODE) {
			Log.d(LOG_TAG, message);
		}
	}

	public static void info(String message) {
		if (Config.DEBUG_MODE) {
			Log.i(LOG_TAG, message);
		}
	}

	public static void warn(String message) {
		if (Config.DEBUG_MODE) {
			Log.w(LOG_TAG, message);
		}
	}

	public static void error(String message, Throwable e) {
		if (Config.DEBUG_MODE) {
			Log.e(LOG_TAG, message, e);
		}
	}
}

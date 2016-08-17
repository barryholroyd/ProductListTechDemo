package com.barryholroyd.walmartproducts;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Common support for both activities.
 *
 * @author Barry Holroyd
 */
public class Support
{
	static private String KEY_ID = null;

	/**
	 * This is a handle to the current activity.
	 *
	 * This would cause a memory leak after each device configuration change,
	 * except that it is re-initialized by each activity upon start up.
	 */
	static private Activity a = null;

	Support(Activity _a) {
		a = _a;
		KEY_ID = a.getPackageName() + "KEY_ID";
	}

	static public String getKeyId() { return KEY_ID; }
	static Activity getActivity() {
		if (a == null)
			throw new IllegalStateException("getActivity() called before activity initialized.");
		return a;
	}

	/**
	 * Logs an error; also displays a Toast if the call is from the main thread.
	 *
	 * @param msg the message to be logged.
	 */
	static public void loge(String msg) {
		Log.e(ActivityProductList.LOGTAG, msg);

		// Main/GUI thread id is 1.
		if (Thread.currentThread().getId() == 1)
			Toast.makeText(a, msg, Toast.LENGTH_LONG).show();
	}
	/**
	 * Logs a debug message.
	 *
	 * @param msg the message to be logged.
	 */
	static public void logd(String msg) {
		Log.d(ActivityProductList.LOGTAG, msg);
	}
}

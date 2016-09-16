package com.barryholroyd.walmartproducts;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

/**
 * Common support for both activities.
 *
 * @author Barry Holroyd
 */
public class Support
{
	/**
	 * "Intent extra" key: identifies product when starting ActivityProductInfo activity.
	 */
	static private String KEY_PRODUCTID = null;

	/**
	 * This is a handle to the current activity. It must be set in a constructor
	 * because the Activity instance hasn't yet been created a static initialization
	 * time.
	 * <p>
	 * This would cause a memory leak after each device configuration change,
	 * except that it is re-initialized by each activity upon start up.
	 */
	static private Activity a = null;

	Support(Activity _a) {
		a = _a;
		KEY_PRODUCTID = a.getPackageName() + "KEY_PRODUCTID";
	}

	static public String getKeyProductId() { return KEY_PRODUCTID; }
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

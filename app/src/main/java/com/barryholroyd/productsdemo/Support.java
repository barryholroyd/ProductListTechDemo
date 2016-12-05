package com.barryholroyd.productsdemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.Html;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Common support that can be provided with static methods.
 *
 * @author Barry Holroyd
 */
class Support
{
	/**
	 * Logs an error.
	 *
	 * @param msg the message to be logged.
	 */
	static void logw(String msg) { Log.w(ActivityProductList.LOGTAG, msg); }

	/**
	 * Logs an error.
	 *
	 * @param msg the message to be logged.
	 */
	static void loge(String msg) { Log.e(ActivityProductList.LOGTAG, msg); }

	/**
     * Logs a debug message.
     *
     * @param msg the message to be logged.
     */
    static void logd(String msg) { Log.d(ActivityProductList.LOGTAG, msg); }

    /**
     * Logs an information message.
     *
     * @param msg the message to be logged.
     */
    static void logi(String msg) { Log.i(ActivityProductList.LOGTAG, msg); }

    /**
	 * Workhorse trc method -- toggle-controlled logging.
     * <p>
     * This is called by module-specific trace() methods. Other than that,
	 * it should not be called directly.
	 * <p>
     * NTH: add screen for toggling trace method flags during runtime.
	 *
	 * @param flag print the message iff flag is "true".
	 * @param component the component being traced.
	 * @param msg the message to be logged.
	 */
	static void trc(boolean flag, String component, String msg) {
		if (flag) {
			Log.v(ActivityProductList.LOGTAG,
					String.format("TRACE [%-12s]: %s", component, msg));
		}
	}

    /**
     * Wrapper used by background methods which want to use the Activity passed to them
     * as a WeakReference.
     * <p>
     *     If the Activity has gone away (e.g., due to a device rotation), then
     *     the get() method should return null and an error message is printed to
     *     the log file. The caller of this method needs to check to see if the
     *     returned value is null or not.
     *
     * @param wrActivity    WeakReference for the Activity.
     * @param msg   error message to print out if the Activity has gone away.
     * @return  the Activity, if still valid; else null.
     */
    static Activity getActivity(WeakReference<Activity> wrActivity, String msg) {
        Activity a = wrActivity.get();
        if (a == null)
            Support.logw(msg);
        return a;
    }

    /**
	 * Truncate a Url or filename for an image to just the final component.
	 *
	 * @param name name to be truncated. Must have "images" just before the last component.
	 * @return the trailing component (e.g., image.jpeg, image-5).
     */
	static public String truncImageString(String name) {
		return name.replaceFirst(".*images/", ".../");
	}

	/**
	 * Translate HTML tags (embedded in text) to simple text.
	 *
	 * Poor man's version -- mostly, just removes tags.
	 *
	 * @param in    input text (e.g., pulled from JSON).
	 * @return      output text (without HTML tags).
	 */
	@SuppressWarnings("deprecation")
	static String htmlToText(String in) {
		if (in == null)
			return "";

        String s;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
			s = Html.fromHtml(in).toString();
		}
		else {
			s = Html.fromHtml(in, 0).toString();
		}
        return cleanUp(s);
	}

    /**
     * The JSON code returned contains non-ASCII characters in the short and long
     * product descriptions and the API doesn't describe what the encoding is, so
     * we simply deplete them from the text.
     *
     * @param s text which potentially contains non-ASCII characters.
     * @return  the same text, but with all non-ASCII characters sequences replaced with a space.
     */
    private static String cleanUp(String s) {
        return s.replaceAll("[\\u0080-\\uffff]+", " ");
    }

    /**
     * "Intent extra" key: identifies product info data when starting ActivityProductInfo activity.
     */
    static String getKeyProductInfo(Context c) { return c.getPackageName() + "_PRODUCTINFO"; }
}

package com.barryholroyd.prodlisthpdemo.support;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.util.Log;

import com.barryholroyd.prodlisthpdemo.ActivityProductList;

import java.lang.ref.WeakReference;

/**
 * Common support that can be provided with static methods.
 *
 * @author Barry Holroyd
 */
public class Support
{
	/**
	 * Logs an error.
	 *
	 * @param msg the message to be logged.
	 */
	static public void logw(String msg) { Log.w(ActivityProductList.LOGTAG, msg); }

	/**
	 * Logs an error.
	 *
	 * @param msg the message to be logged.
	 */
	static public void loge(String msg) { Log.e(ActivityProductList.LOGTAG, msg); }

	/**
     * Logs a debug message.
     *
     * @param msg the message to be logged.
     */
    static public void logd(String msg) { Log.d(ActivityProductList.LOGTAG, msg); }

    /**
     * Logs an information message.
     *
     * @param msg the message to be logged.
     */
    static public void logi(String msg) { Log.i(ActivityProductList.LOGTAG, msg); }

    /**
	 * Workhorse trc method -- toggle-controlled logging.
     * <p>
     * This is called by module-specific trace() methods. Other than that,
	 * it should not be called directly.
	 * <p>
	 *
	 * @param flag print the message iff flag is "true".
	 * @param component the component being traced.
	 * @param msg the message to be logged.
	 */
	static public void trc(boolean flag, String component, String msg) {
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
    static public Activity getActivity(WeakReference<Activity> wrActivity, String msg) {
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
		return name.replaceFirst(".*/", ".../").replaceFirst("\\?.*", "");
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
	static public String htmlToText(String in) {
		if (in == null)
			return "";

        String s;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
			s = Html.fromHtml(in).toString();
		}
		else {
			s = Html.fromHtml(in, 0).toString();
		}

		/*
		 * The first pass decodes special HTML sequences (e.g., &lt;br&gt; -> <br>) but
		 * then doesn't convert the tags to text. Running it through again handles that
		 * situation.
		 */
		s = Html.fromHtml(s).toString();
		return s;
	}

    /**
     * "Intent extra" key: identifies product info data when starting ActivityProductInfo activity.
	 *
	 * @param c standard Context instance from the current Activity.
     */
    static public String getKeyProductInfo(Context c) { return c.getPackageName() + "_PRODUCTINFO"; }
}

package com.barryholroyd.walmartproducts;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.util.Log;

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
	static void loge(String msg) { Log.e(ActivityProductList.LOGTAG, msg); }

	/**
	 * Logs a debug message.
	 *
	 * @param msg the message to be logged.
	 */
	static void logd(String msg) {
		Log.d(ActivityProductList.LOGTAG, msg);
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

        String s = null;
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

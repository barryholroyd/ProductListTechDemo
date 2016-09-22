package com.barryholroyd.walmartproducts;

import android.app.Activity;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Common support for both activities.
 *
 * @author Barry Holroyd
 */
public class Support
{
    /**
     * DEL: ?
     * Singleton.
     * <p>
     *     A singleton is implemented so that the current
     * </p>
     */
	static final Support instance = new Support();
	/**
	 * "Intent extra" key: identifies product info data when starting ActivityProductInfo activity.
	 */
	static private String KEY_PRODUCTINFO = "PRODUCT_INFO";

    static private final int TOASTER_Y_OFFSET_INCR = 50;
    static private final int TOASTER_Y_OFFSET_MAX  = TOASTER_Y_OFFSET_INCR * 5;
    private int toasterYoffset = 0;

	/**
	 * This is a handle to the current activity. It must be set in a constructor
	 * because the Activity instance hasn't yet been created a static initialization
	 * time.
	 * <p>
	 * This would cause a memory leak after each device configuration change,
	 * except that it is re-initialized by each activity upon start up.
	 *
	 * NTH: Change this approach. It apparently breaks Instant Run.
	 */
	private Activity a = null;
    private int counter = 0;

    private Support() {
        Log.d(ActivityProductList.LOGTAG, "*** Support()");
        counter++;
    }

    void printCounter() {
        Log.d(ActivityProductList.LOGTAG,
                String.format("*** Support.printCounter(): counter=%d\n", counter));
    }

    void init(Activity _a) {
        Log.d(ActivityProductList.LOGTAG,
                String.format("*** Support.init(): counter=%d\n", counter));
        a = _a;
		KEY_PRODUCTINFO = a.getPackageName() + "KEY_PRODUCTINFO";
	}

	String getKeyProductInfo() { return KEY_PRODUCTINFO; }
	Activity getActivity() {
		if (a == null)
			throw new IllegalStateException("getActivity() called before activity initialized.");
		return a;
	}

	/**
	 * Logs an error; also displays a Toast if the call is from the main thread.
	 *
	 * @param msg the message to be logged.
	 */
	void loge(String msg) {
		Log.e(ActivityProductList.LOGTAG, msg);

		// Main/GUI thread id is 1.
		if (Thread.currentThread().getId() == 1)
			Toast.makeText(a, msg, LENGTH_LONG).show();
	}
	/**
	 * Logs a debug message.
	 *
	 * @param msg the message to be logged.
	 */
	void logd(String msg) {
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
	String htmlToText(String in) {
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
    private String cleanUp(String s) {
        return s.replaceAll("[\\u0080-\\uffff]+", " ");
    }

	/**
	 * Display a pop-up message to the user.
	 */
	public void toaster(String msg) {
        toasterYoffset = (toasterYoffset > TOASTER_Y_OFFSET_MAX)
                ? 0 : toasterYoffset + TOASTER_Y_OFFSET_INCR;
        Toast toast = Toast.makeText(getActivity(), msg, LENGTH_LONG);
        toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, toasterYoffset);
        toast.show();
	}
}

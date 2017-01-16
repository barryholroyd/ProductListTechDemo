package com.barryholroyd.productlisttechdemo.support;

import android.app.Activity;
import android.view.Gravity;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Helper class to display toasts.
 * <p>
 * Each successive toast is positioned slightly below the previous toast; after five
 * toasts, it starts from the top again.
 * </p>
 */
public class Toaster {
    /** Distance in pixels between top edge of successive toasts. */
    private static final int YINC = 100;

    /** Maximum distance to offset before restarting at the top again. */
    private static final int YMAX  = YINC * 5;

    /** Current offset from the top position. */
    private static int offset = 0;

    /**
     * Display a pop-up message to the user.
     * <p>
     *     This can be called from either the foreground or the background. A
     *     weak reference is used for the Activity parameter so that we can avoid
     *     using an Activity which has gone away.
     *
     * @param wrActivity weak reference for the calling activity.
     * @param msg message to be displayed.
     */
    public static void display(WeakReference<Activity> wrActivity, final String msg) {
        offset = (offset > YMAX) ? 0 : offset + YINC;
        final Activity a = Support.getActivity(wrActivity,
                "Activity is gone so could not create toast. Msg: " + msg);
        if (a != null) {
            a.runOnUiThread(new Runnable() {
                public void run() {
                    final Toast toast = Toast.makeText(a, msg, LENGTH_LONG);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, offset);
                    toast.show();
                }
            });
        }
    }
}

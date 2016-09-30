package com.barryholroyd.walmartproducts;

import android.app.Activity;
import android.view.Gravity;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Helper class to display toasts.
 * <p>
 * Each successive toast is positioned slightly below the previous toast; after five
 * toasts, it starts from the top again.
 * </p>
 */
class Toaster {
    /** Distance in pixels between top edge of successive toasts. */
    static private final int YINC = 100;

    /** Maximum distance to offset before restarting at the top again. */
    static private final int YMAX  = YINC * 5;

    /** Current offset from the top position. */
    static private int offset = 0;

    /**
     * Display a pop-up message to the user.
     * <p>
     * This can be called from either the foreground or the background.
     *
     * @param a standard Activity.
     * @param msg message to be displayed.
     */
    static void display(final Activity a, final String msg) {
        offset = (offset > YMAX) ? 0 : offset + YINC;
        a.runOnUiThread(new Runnable() {
            public void run() {
                final Toast toast = Toast.makeText(a, msg, LENGTH_LONG);
                toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, offset);
                toast.show();
            }
        });
    }
}

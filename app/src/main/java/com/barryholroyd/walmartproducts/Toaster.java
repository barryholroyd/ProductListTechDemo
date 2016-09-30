package com.barryholroyd.walmartproducts;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Helper class to display toasts.
 */
//TBD: Test toaster.
class Toaster {
    static private final int YINC = 50;
    static private final int YMAX  = YINC * 5;
    private int offset = 0;
    private Activity a = null;

    static private int offset2 = 0;

    Toaster(Activity _a) { a = _a; }

    private Context getContext() {
        if (a == null) {
            throw new IllegalStateException("Toaster not initialized yet.");
        }
        return a;
    }

    /** DEL: ?
     * Display a pop-up message to the user.
     */
    void display(final String msg) {
        offset = (offset > YMAX) ? 0 : offset + YINC;
        a.runOnUiThread(new Runnable() {
            public void run() {
                final Toast toast = Toast.makeText(a, msg, LENGTH_LONG);
                toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, offset);
                toast.show();
            }
        });
    }

    /**
     * Display a pop-up message to the user.
     */
    static void display(final Activity a, final String msg) {
        offset2 = (offset2 > YMAX) ? 0 : offset2 + YINC;
        a.runOnUiThread(new Runnable() {
            public void run() {
                final Toast toast = Toast.makeText(a, msg, LENGTH_LONG);
                toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, offset2);
                toast.show();
            }
        });
    }
}

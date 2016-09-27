package com.barryholroyd.walmartproducts;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Helper class to display toasts.
 */

class Toaster {
    static private final int YINC = 50;
    static private final int YMAX  = YINC * 5;
    private int offset = 0;
    private Activity a = null;

    Toaster(Activity _a) { a = _a; }

    private Context getContext() {
        if (a == null) {
            throw new IllegalStateException("Toaster not initialized yet.");
        }
        return a;
    }

    /**
     * Display a pop-up message to the user.
     */
    void display(String msg) {
        offset = (offset > YMAX) ? 0 : offset + YINC;
        final Toast toast = Toast.makeText(getContext(), msg, LENGTH_LONG);
        toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, offset);

        a.runOnUiThread(new Runnable() {
            public void run() {
                toast.show();
            }
        });
    }
}

package com.barryholroyd.walmartproducts;

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
    private Context c = null;

    Toaster(Context _c) { c = _c; }

    private Context getContext() {
        if (c == null) {
            throw new IllegalStateException("Toaster not initialized yet.");
        }
        return c;
    }

    /**
     * Display a pop-up message to the user.
     */
    void display(String msg) {
        offset = (offset > YMAX) ? 0 : offset + YINC;
        Toast toast = Toast.makeText(getContext(), msg, LENGTH_LONG);
        toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, offset);
        toast.show();
    }
}

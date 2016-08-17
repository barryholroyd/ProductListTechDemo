package com.barryholroyd.walmartproducts;

import android.content.Context;

/**
 * Common support for both activities.
 *
 * @author Barry Holroyd
 */
public class Support
{
	static private String KEY_ID = null;

	Support(Context c) {
		KEY_ID = c.getPackageName() + "KEY_ID";
	}

	static public String getKeyId() { return KEY_ID; }
}

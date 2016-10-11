package com.barryholroyd.productsdemo;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * TBD:
 */

@RunWith(MockitoJUnitRunner.class)
public class MiscTests {
    static private final String FAKE_PACKAGE = "com.barryholroyd.fake";

    @Mock
    Context mMockContext;

    /**
     * Validate the building of the product info key string.
     * TBD: delete this if you can come up with a better Mockito example.
     */
    @Test
    public void when_RequestProductInfoKey_Expect_ProductInfoKey() {
        // DEL: create the mock context
        when(mMockContext.getPackageName()).thenReturn(FAKE_PACKAGE);

        // DEL: run the normal method and check result
        String s = Support.getKeyProductInfo(mMockContext);
        assertThat(s, is(FAKE_PACKAGE + "_PRODUCTINFO"));
    }

    // TBD: Test Support.getKeyProductInfo using fake Context.
    // DEL: Use Mockito
    // DEL: See https://developer.android.com/training/testing/unit-testing/local-unit-tests.html
    // DEL:     static String getKeyProductInfo(Context c) { return c.getPackageName() + "_PRODUCTINFO"; }

}

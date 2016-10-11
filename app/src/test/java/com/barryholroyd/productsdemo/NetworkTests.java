package com.barryholroyd.productsdemo;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.InputStream;

import static com.barryholroyd.productsdemo.GetProducts.makeUrl;

/**
 * Unit tests for network access.
 */
public class NetworkTests {

    /**
     * Ensure that we can set up a valid connection to the product demo web site.
     */
    @Test
    public void when_GetInputStream_Expect_NonNull() {
        String url = makeUrl(1, 2);
        InputStream is = null;
        try {
            is = NetworkSupport.getInputStreamFromUrl(url);
        }
        catch (NetworkSupportException nse) {
            // TBD: what to put here?
        }
        assertNotNull(is);
    }

    @Test
    public void when_BadUrl_Expect_Exception() {
        InputStream is = null;
        try {
            is = NetworkSupport.getInputStreamFromUrl("badurl");
        }
        catch (NetworkSupportException nse) {
            // TBD: what to put here?
        }
        // TBD: what to put here?
    }
}

package com.barryholroyd.walmartproducts;

import android.content.Context;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * General network support.
 */

public class NetworkSupport {
    /**
     * Get an input stream for the specified URL.
     *
     * @param urlStr the URL (in String form) to get the JSON product info. from.
     * @return the input stream created to read data from the specified URL.
     */
    static InputStream getInputStreamFromUrl(Context ctx, String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setReadTimeout(10000); // ms
            c.setConnectTimeout(15000); // ms
            c.setRequestMethod("GET");
            c.setDoInput(true);
            c.connect();
            c.getResponseCode();// NTH: check response code
            return c.getInputStream();
        }
        catch (MalformedURLException e) {
            Support.loge(String.format("getInputStreamFromUrl() - malformed url: %s", urlStr));
            return null;
        }
        catch (IOException e) {
            Support.loge(String.format("getInputStreamFromUrl() - IO Exception: %s", urlStr));
            (new Toaster(ctx)).display(String.format("IO Exception: %s", e.getMessage()));
            return null;
        }
    }
}

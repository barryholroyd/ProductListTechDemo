package com.barryholroyd.walmartproducts;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

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
    static InputStream getInputStreamFromUrl(Activity a, String urlStr) {
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
            (new Toaster(a)).display(String.format("IO Exception: %s", e.getMessage()));
            return null;
        }
    }

    /**
     * Get a bitmap from the network.
     *
     * @param a standard Activity instance.
     * @param urlStr    url to use to get the bitmap from the network.
     * @return  the bitmap obtained from the network.
     */
    static Bitmap getImageFromNetwork(Activity a, String urlStr) {
        InputStream is = NetworkSupport.getInputStreamFromUrl(a, urlStr);
        return BitmapFactory.decodeStream(is);
    }
}

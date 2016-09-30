package com.barryholroyd.walmartproducts;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
            int response;
            URL url = new URL(urlStr);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setReadTimeout(10000); // ms
            c.setConnectTimeout(15000); // ms
            c.setRequestMethod("GET");
            c.setDoInput(true);
            c.connect();
            response = c.getResponseCode();
            if (response != 200) {
                throw new NetworkSupportException(
                    String.format("Bad response code: %d", response));
            }
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
     * <p>
     * When the BitmapFactory reads an image file from the network (e.g., a jpeg file),
     * it decompresses the image and stores the decompressed image in a bitmap file in
     * a particular bitmap format (e.g., ARGB_8888). The uncompressed bitmap can be
     * substantially larger than the original compressed format.
     * <p>
     * The downloaded images will be used both on the product list page (where they will
     * appear as small thumbnails) and also on the product info page (where they will appear
     * individually, as somewhat larger thumbnails). The hmax and wmax values should be sized
     * to be slightly larger than the anticipated size of the images on the product info page.
     * </p>
     *
     * @param a standard Activity instance.
     * @param urlStr    url to use to get the bitmap from the network.
     * @param hmax  maximum height of the image (in pixels)
     * @param wmax  maximum width of the image (in pixels)
     * @return  the bitmap obtained from the network.
     */
    static Bitmap getImageFromNetwork(Activity a, String urlStr, int hmax, int wmax) {

        trace(String.format("Loading from network: %s", urlStr));
        InputStream is = NetworkSupport.getInputStreamFromUrl(a, urlStr);
        BitmapFactory.Options opts = setBmfOptions(a, is, hmax, wmax);

        InputStream is2 = NetworkSupport.getInputStreamFromUrl(a, urlStr);

        int av1 = getAvailable(is2);
        Bitmap bitmap = BitmapFactory.decodeStream(is2, null, opts);
        int av2 = getAvailable(is2);
        int av3 = getAvailable(is);
        Bitmap bitmaptmp = BitmapFactory.decodeStream(is, null, opts);
        int av4 = getAvailable(is);

        printAvailable("IS=is2 Pre-decode", av1);
        printAvailable("IS=is2 Post-decode", av2);
        printAvailable("IS=is  Pre-decode", av3);
        printAvailable("IS=is  Post-decode", av4);
        Support.logd(String.format("bitmap: %s", bitmap == null ? "null" : "not null"));
        Support.logd(String.format("bitmaptmp: %s", bitmaptmp == null ? "null" : "not null"));

        // TBD: May have to reset the input stream -- bitmap is null.
        // TBD: can mark it and reset it?
        if (bitmap == null)
            throw new RuntimeException("BITMAP IS NULL");

        // TBD: should close network stream?

        printBitmapInfo(urlStr, bitmap, opts);
        return bitmap;
    }

    /**
     * TBD: Document this.
     *
     * @param a
     * @param is
     * @param hmax
     * @param wmax
     * @return
     */
    static private BitmapFactory.Options setBmfOptions(
            Activity a, InputStream is, int hmax, int wmax) {
        BitmapFactory.Options opts = new BitmapFactory.Options();

        // Get information but don't read any data yet.
        opts.inJustDecodeBounds = true;
        int av1 = getAvailable(is);
        BitmapFactory.decodeStream(is, null, opts);
        int av2 = getAvailable(is);
        printAvailable("Before setBmfOptions decode", av1);
        printAvailable("After  setBmfOptions decode", av2);

        // Calculate the sample size.
        opts.inSampleSize = calculateInSampleSize(opts, hmax, wmax);

        // Restore opts so that data will be read the next time it is used.
        opts.inJustDecodeBounds = false;

        return opts;
    }

    static public void printAvailable(String tag, int available) { // DEL:
        String msg = String.format("IS Bytes Available (%s): %d", tag, available);
        Support.logd(msg);
    }

    static public int getAvailable(InputStream is) { // DEL:
        int available = -1;
        try { available = is.available(); } catch (Exception e) {}
        return available;
    }

    /**
     * TBD: document this.
     *
     * @param opts
     * @param hmax
     * @param wmax
     * @return
     */
    static private int calculateInSampleSize(BitmapFactory.Options opts, int hmax, int wmax) {
        // Raw height and width of the image.
        final int height = opts.outHeight;
        final int width  = opts.outWidth;
        int inSampleSize = 1;

        if (height > hmax || width > wmax) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of two and keeps
            // both height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= hmax && (halfWidth / inSampleSize) >= wmax) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    static private void printBitmapInfo(String url, Bitmap bitmap, BitmapFactory.Options opts) {
        Support.logd(String.format("Image File Name:      %s", url));
        Support.logd(String.format("Image File Mime Type: %s", opts.outMimeType));
        Support.logd(String.format("Bitmap Format:        %s", bitmap.getConfig()));
        Support.logd(String.format("Bitmap Size:          %d", bitmap.getByteCount()));
    }

    /**
     * Tracing method specific to the networking module.
     * Overall Log level must be "info" or higher.
     *
     * @param msg message to be logged.
     */
    static protected void trace(String msg) {
        Support.trace(Configure.Network.NM_TRACE, "Network Module", msg);
    }}

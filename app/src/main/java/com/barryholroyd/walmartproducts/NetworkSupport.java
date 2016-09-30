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
    static InputStream getInputStreamFromUrl(Activity a, String urlStr)
        throws NetworkSupportException {
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
                    String.format("getInputStreamFromUrl() - bad response code: %d", response));
            }
            return c.getInputStream();
        }
        catch (MalformedURLException e) {
            throw new NetworkSupportException(
                    String.format("getInputStreamFromUrl() - malformed url: %s", urlStr));
        }
        catch (IOException e) {
            throw new NetworkSupportException(
                    String.format("getInputStreamFromUrl() - IO Exception: %s", urlStr));
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
     * TBD: must use input stream twice -- performance impact.
     *
     * @param a standard Activity instance.
     * @param urlStr    url to use to get the bitmap from the network.
     * @param hmax  maximum height of the image (in pixels)
     * @param wmax  maximum width of the image (in pixels)
     * @return  the bitmap obtained from the network.
     */
    static Bitmap getImageFromNetwork(Activity a, String urlStr, int hmax, int wmax)
        throws NetworkSupportException {
        trace(String.format("Loading from network: %s", urlStr));

        BitmapFactory.Options opts = setBmfOptions(a, urlStr, hmax, wmax);

        Bitmap bitmap;
        try (InputStream is = NetworkSupport.getInputStreamFromUrl(a, urlStr)) {
            bitmap = BitmapFactory.decodeStream(is, null, opts);
        }
        catch (IOException ioe) {
            String msg = String.format(String.format(
                    "IOException: %s", ioe.getMessage()));
            throw new NetworkSupportException(msg);
        }

        if (bitmap == null)
            throw new NetworkSupportException("null bitmap");

        // TBD: should close network stream?

        logBitmapInfo(urlStr, bitmap, opts);
        return bitmap;
    }

    /**
     * TBD: Document this. Create opts.
     * Get information but don't read any data yet.
     *
     * @param a
     * @param urlStr
     * @param hmax
     * @param wmax
     * @return
     */
    static private BitmapFactory.Options setBmfOptions(
            Activity a, String urlStr, int hmax, int wmax)
        throws NetworkSupportException  {
        BitmapFactory.Options opts = new BitmapFactory.Options();

        try (InputStream is = NetworkSupport.getInputStreamFromUrl(a, urlStr)) {
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, opts);
        }
        catch (IOException ioe) {
            String msg = String.format(String.format(
                    "IOException: %s", ioe.getMessage()));
            throw new NetworkSupportException(msg);
        }

        // Calculate the sample size.
        opts.inSampleSize = calculateInSampleSize(opts, hmax, wmax);

        // Restore opts so that data will be read the next time it is used.
        opts.inJustDecodeBounds = false;

        return opts;
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

    /** Log basic information about a bitmap. */
    static private void logBitmapInfo(String url, Bitmap bitmap, BitmapFactory.Options opts) {
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
    static private void trace(String msg) {
        Support.trace(Configure.Network.NM_TRACE, "Network Module", msg);
    }}

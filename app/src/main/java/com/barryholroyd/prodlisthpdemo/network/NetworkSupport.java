package com.barryholroyd.prodlisthpdemo.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.barryholroyd.prodlisthpdemo.config.Settings;
import com.barryholroyd.prodlisthpdemo.support.Support;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import static com.barryholroyd.prodlisthpdemo.support.Support.truncImageString;

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
    public static InputStream getInputStreamFromUrl(String urlStr)
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
                    String.format(Locale.US, "getInputStreamFromUrl() - bad response code: %d", response));
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
     * individually, as somewhat larger thumbnails). The requestedHeight and requestedWidth values should be sized
     * to be slightly larger than the anticipated size of the images on the product info page.
     * </p>
     * The input stream must be opened and closed twice: once to get the size of the remote
     * image file on the network and once to read it.
     *
     * @param urlStr    url to use to get the bitmap from the network.
     * @param requestedHeight  maximum height of the image (in pixels)
     * @param requestedWidth  maximum width of the image (in pixels)
     * @return  the bitmap obtained from the network.
     */
    public static Bitmap getImageFromNetwork(String urlStr, int requestedHeight, int requestedWidth)
        throws NetworkSupportException {
        trace(String.format("Getting: %s", urlStr));

        BitmapFactory.Options opts = setBmfOptions(urlStr, requestedHeight, requestedWidth);

        Bitmap bitmap;
        try (InputStream is = NetworkSupport.getInputStreamFromUrl(urlStr)) {
            bitmap = BitmapFactory.decodeStream(is, null, opts);
        }
        catch (IOException ioe) {
            String msg = String.format("IOException: %s", ioe.getMessage());
            throw new NetworkSupportException(msg);
        }

        if (bitmap == null)
            throw new NetworkSupportException("null bitmap");

        trace(String.format("Found: %s", urlStr));
        logBitmapInfo(urlStr, bitmap, opts);
        return bitmap;
    }

    /**
     * Determine the scaling factor for the image at the provided url.
     *
     * The scaling factor should shrink the image, if/as necessary, so that
     * it is no smaller than, and not significantly larger than, the specified
     * dimensions.
     *
     * @param urlStr the url for the image.
     * @param hmax   the maximum height of the image.
     * @param wmax   the maximum width of the image.
     * @return standard BitmapFactory.Options instance.
     */
    private static BitmapFactory.Options setBmfOptions(
            String urlStr, int hmax, int wmax)
        throws NetworkSupportException  {
        BitmapFactory.Options opts = new BitmapFactory.Options();

        try (InputStream is = NetworkSupport.getInputStreamFromUrl(urlStr)) {
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, opts);
        }
        catch (IOException ioe) {
            String msg = String.format("IOException: %s", ioe.getMessage());
            throw new NetworkSupportException(msg);
        }

        // Calculate the sample size.
        opts.inSampleSize = calculateInSampleSize(opts, hmax, wmax);

        // Restore opts so that data will be read the next time it is used.
        opts.inJustDecodeBounds = false;

        return opts;
    }

    /**
     * Calculate the scaling factor for an image to make it a small as possible while still
     * fitting within the specified dimensions without losing any fidelity.
     * <p>
     * The algorithm is to iteratively divide each of the two dimensions in half until an
     * appropriate size is reached (at either of them). The resulting dimensions must not
     * be smaller than the specified dimensions, so the starting size is cut in half before
     * beginning the calculations so that the algorithm doesn't overshoot and make the image
     * too small.
     *
     * @param opts BitmapFactory.options containing the image's actual (pre-scaled)
     *             height and width.
     * @param requestedHeight requested height in pixels.
     * @param requestedWidth requested width in pixels.
     * @return the inSampleSize scaling factor to be passed to BitmapFactory.
     */
    private static int calculateInSampleSize(
            BitmapFactory.Options opts, int requestedHeight, int requestedWidth) {
        final int height = opts.outHeight;  // pre-scaled height of the image.
        final int width  = opts.outWidth;   // pre-scaled width of the image.
        int inSampleSize = 1;

        // If the image potentially needs to be shrunk.
        if (height > requestedHeight || width > requestedWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            /*
             * Calculate the largest inSampleSize value that is a power of two and keeps
             * both height and width larger than the requested height and width.
             */
            while ((halfHeight / inSampleSize) >= requestedHeight &&
                    (halfWidth / inSampleSize) >= requestedWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Log basic information about a bitmap.
     *
     * @param url       url for the image.
     * @param bitmap    the bitmap created from the image.
     * @param opts      the options used to create the bitmap.
     */
    private static void logBitmapInfo(String url, Bitmap bitmap, BitmapFactory.Options opts) {
        if (Settings.isAppTraceDetails()) {
            trace(String.format(Locale.US,
                    "  DETAILS: Url=%s Mime=%s BitmapFormat=%s BitmapSize=%d",
                    truncImageString(url), opts.outMimeType,
                    bitmap.getConfig(), bitmap.getByteCount()));
        }
    }

    /**
     * Tracing method specific to the networking module.
     *
     * @param msg message to be logged.
     */
    private static void trace(String msg) {
        Support.trc(Settings.isNetworkTrace(), "Network", msg);
    }
}

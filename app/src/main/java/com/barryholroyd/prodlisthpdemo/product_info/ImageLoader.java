package com.barryholroyd.prodlisthpdemo.product_info;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.barryholroyd.prodlisthpdemo.config.Settings;
import com.barryholroyd.prodlisthpdemo.R;
import com.barryholroyd.prodlisthpdemo.cache_disk.CacheDiskImage;
import com.barryholroyd.prodlisthpdemo.cache_memory.CacheMemoryImage;
import com.barryholroyd.prodlisthpdemo.network.NetworkSupport;
import com.barryholroyd.prodlisthpdemo.network.NetworkSupportException;
import com.barryholroyd.prodlisthpdemo.support.Support;
import com.barryholroyd.prodlisthpdemo.support.Toaster;

import java.lang.ref.WeakReference;

/**
 * Load and display an image.
 *
 * This class loads and displays the thumbnail image associated with each product.
 * When the app downloads the JSON data representing a list of products, it uses
 * this object to obtain the associated images.
 * <p>
 *     ImageLoader checks to see if the image is in the memory cache; if not, it uses
 *     a worker thread to check the disk cache; if the image isn't there, then the
 *     worker thread downloads it from the network.
 * <p>
 *     Regardless of how the image is obtained, it is displayed when it is available.
 *     In the case of the Product List page, it is displayed in the correct row; if
 *     that row is no longer being displayed, the image is cached but not displayed.
 */

public class ImageLoader {
    /*
     * We assume the BitmapFactory default image format of ARGB_8888 (4 bytes):
     * Image (bitmap) size: 100 * 100 * 4 = 40,000 bytes.
     */
    /** Max. number of height pixels in product image. */
    static private final int IMAGE_HSIZE = 100;

    /** Max. number of width pixels in product image. */
    static private final int IMAGE_WSIZE = 100;

    /** In-memory caching instance. */
    public static CacheMemoryImage cacheMemory = null;

    /** Disk caching instance (singleton). */
    private static CacheDiskImage cacheDiskImage = null;

    /** Blank image bitmap used to fill the header "image" slot. */
    private static final Bitmap blankImageBitmap =
            Bitmap.createBitmap(IMAGE_WSIZE, IMAGE_HSIZE, Bitmap.Config.ARGB_8888);

    /** Weak Reference for using the Activity in background threads. */
    private WeakReference<Activity> wrActivity = null;

    /** Resources hook. */
    private Resources resources = null;

    /**
     * imageUrl is the image url used to download a product's image. If this ImageLoader
     * instance is owned by a ViewHolder, then the main (foreground) thread could
     * reallocate the ViewHolder while the image is being loaded in the background,
     * resulting in the image being associated with the wrong row in the list
     * display.
     * <p>
     *     This imageUrl field contains the "correct" image imageUrl for the ViewHolder; if
     *     the ViewHolder gets reallocated, then the imageUrl will be updated appropriately.
     *     We should always use this field for loading the image, even if its value has
     *     changed.
     *
     * @see #urlChanged(String, String, String)
     */
    private String imageUrl;

    /**
     * Standard constructor.
     * <p>
     * Initializes caches and stores a WeakReference to the current Activity.
     * <p>
     *     The Activity instance is used to print Toasts from the background and
     *     to call runOnUiThread() from a worker thread. We use a WeakReference so
     *     that we can determine whether or not it is still valid (e.g., a device
     *     rotation will destroy the current Activity).
     * @param a    the current Activity.
     */
    public ImageLoader(Activity a) {
        wrActivity = new WeakReference<>(a);
        resources = a.getResources();
        if (cacheMemory == null) {
            cacheMemory = Settings.isMemoryCacheByBytes()
                    ? CacheMemoryImage.createWithBytes(Settings.getMemoryCacheSizeBytes())
                    : CacheMemoryImage.createWithPercent(Settings.getMemoryCacheSizePercent());
        }
        if (cacheDiskImage == null) {
            cacheDiskImage = CacheDiskImage.makeInstance(
                    a, Settings.CACHE_DIR, Settings.getDiskCacheSizeBytes());
        }
    }

    /**
     * Load an image into an ImageView.
     * <p>
     * Two different approaches are implemented: one threads-based and one AsyncTask-based.
     * <ol>
     *     <li> Attempt to load from memory cache.
     *     <li> If that fails, load from background. At each step, check to see if the
     *          URL loaded from is the same as the current URL (it may have changed if
     *          the containing ViewHolder got re-allocated). If it is the same, then load
     *          it into the ImageView (in the main/UI thread). If it is different, ignore
     *          and do nothing (the new URL will have already been either loaded or queued
     *          to be loaded).
     *     <ol>
     *         <li>Attempt to load from disk cache. If successful, add to memory cache.
     *         <li>Otherwise, attempt to load from network. If successful, add to memory
     *             cache and disk cache.
     *         <li>Otherwise, load a default place-holder image.
     *     </ol>
     * </ol>
     */
    public void load(ImageView iv, String url) {
        /*
         * Foreground: load from memory cache, if present.
         */
        Bitmap bitmap = cacheMemory.get(url);
        if (bitmap != null) {
            iv.setImageBitmap(bitmap);
            return;
        }

        /*
         * "imageUrl" is directly accessible by background threads.
         * It is possible that this might change before being used, but
         * it should still be used.
         */
        imageUrl = url;

        /*
         * Background: load from disk or network.
         * Both LiThread and LiAsyncTask have to be defined as nested classes so that
         * they can have access to "imageUrl". The current value of imageUrl
         * is passed in to be used for comparing to its value at a later time,
         * to see if its value has been changed by the main thread.
         */
        if (Settings.isAppUseThreads()) (new LiThread(iv, imageUrl)).start();
        else new LiAsyncTask(iv, imageUrl).execute();
    }

    /**
     * Threads-based version of loading the image in the background.
     *
     * @see #load(ImageView, String)
     */
    private class LiThread extends Thread
    {
        private final ImageView iv;
        private String origImageUrl;

        LiThread(ImageView _iv, String _origImageUrl) {
            iv= _iv;
            origImageUrl = _origImageUrl;
        }

        @Override
        public void run() {
            /*
             * We have already tried pulling the bitmap from the memory
             * cache (that happens in the foreground), but didn't find it
             * there.
             */
            Bitmap bitmap;

            // Check for a null imageUrl.
            bitmap = setImageNullCheck(imageUrl);
            if (bitmap != null) {
                setImageView(iv, getNoImageBitmap(resources));
                return;
            }

            // Try the disk cache.
            checkUrl("Pre-disk cache", origImageUrl, imageUrl);
            bitmap = setImageDiskCache(imageUrl);
            if (bitmap != null) {
                setImageView(iv, bitmap);
                return;
            }

            // Get the image from the network. Bitmap is guaranteed to be non-null.
            checkUrl("Pre-network", origImageUrl, imageUrl);
            bitmap = setImageNetwork(imageUrl);
            setImageView(iv, bitmap);
        }

        /** Set the ImageView on the main thread. */
        private void setImageView(final ImageView iv, final Bitmap bitmap) {
            Activity a = Support.getActivity(wrActivity,
                    "Activity gone: could not set ImageView");
            if (a != null) {
                a.runOnUiThread(new Runnable() {
                    public void run() {
                        iv.setImageBitmap(bitmap);
                    }
                });
            }
        }
    }

    /**
     * AsyncTask-based version of loading the image in the background.
     */
    private class LiAsyncTask extends AsyncTask<String, Void, Bitmap>
    {
        private final ImageView iv;
        private String origImageUrl;

        LiAsyncTask(ImageView _iv, String _origImageUrl) {
            iv= _iv;
            origImageUrl = _origImageUrl;
        }

        /**
         * Get the image bitmap from the disk cache or the network.
         *
         * @see #load(ImageView, String)
         * @param args  n/a - this is provided but ignored.
         * @return      image bitmap obtained.
         */
        @Override
        protected Bitmap doInBackground(String... args) {
            /*
             * We have already tried pulling the bitmap from the memory
             * cache (that happens in the foreground), but didn't find it
             * there.
             */
            Bitmap bitmap;

            // Check for a null origImageUrl.
            bitmap = setImageNullCheck(imageUrl);
            if (bitmap != null) { return bitmap; }

            // Try the disk cache.
            checkUrl("Pre-disk cache", origImageUrl, imageUrl);
            bitmap = setImageDiskCache(imageUrl);
            if (bitmap != null) { return bitmap; }

            // Get the image from the network. Bitmap is guaranteed to be non-null.
            checkUrl("Pre-network", origImageUrl, imageUrl);
            bitmap = setImageNetwork(imageUrl);
            return bitmap;
        }

        /**
         * Display the image.
         *
         * @param bitmap bitmap to be displayed.
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
                iv.setImageBitmap(bitmap);
        }
    }

    /**
     * If no image origImageUrl was provided, then try to provide a bitmap for default image.
     *
     * @param imageUrl url for the image.
     * @return    a default image bitmap if no image origImageUrl was provided; else null.
     *            null indicates that the caller should continue processing -- the origImageUrl is valid
     *            and we still need the bitmap.
     */
    private Bitmap setImageNullCheck(String imageUrl) {
        if (imageUrl == null) {
            trace("No image provided -- loading default image.");
            return getNoImageBitmap(resources);
        }
        return null;
    }

    /**
     * Attempt to get the image bitmap from the disk cache.
     *
     * If successful, add it to the memory cache.
     *
     * @param url origImageUrl for the image.
     * @return    bitmap from the image.
     */
    private Bitmap setImageDiskCache(String url) {
        Bitmap bitmap = cacheDiskImage.get(url);
        if (bitmap != null) {
            cacheMemory.add(url, bitmap);
        }
        return bitmap;
    }

    /**
     * Attempt to get the image bitmap from the network.
     *
     * If successful, add it to the memory and disk caches.
     *
     * @param origImageUrl origImageUrl for the image.
     * @return    bitmap from the image.
     */
    private Bitmap setImageNetwork(String origImageUrl) {
        Bitmap bitmap;
        try {
            bitmap = NetworkSupport.getImageFromNetwork(origImageUrl, IMAGE_HSIZE, IMAGE_WSIZE);
        }
        catch (NetworkSupportException nse) {
            String msg = String.format("NetworkSupportException: %s", nse.getMessage());
            Support.loge(msg);
            Toaster.display(wrActivity, msg);
            return getNoImageBitmap(resources);
        }

        /*
         * We pulled an image from the network. However, it is possible that the imageUrl
         * got changed at the last instant, in which case the image is no longer valid for
         * this ImageLoader. In that situation, display the default image for now and wait
         * for the later thread download the newer image. In rare cases, the default
         * image may stayed displayed. I believe this happens if this thread ends up
         * executing after the "other" thread. Pragmatically, that isn't a problem with
         * the memory and disk caches in place.
         */
        if (urlChanged("Network", origImageUrl, imageUrl)) {
            String stubUrl = Support.truncImageString(origImageUrl);
            if (Settings.isAppTraceDetails()) {
                trace(String.format("Loading default image instead of %s.", stubUrl));
            }
            return getNoImageBitmap(resources);
        }

        if (bitmap != null) {
            cacheMemory.add(origImageUrl, bitmap);
            cacheDiskImage.add(wrActivity, origImageUrl, bitmap);
        }
        else {
            bitmap = getNoImageBitmap(resources);
        }

        return bitmap;
    }

    /** Check to see if the value of imageUrl has changed.
     * <p>
     *     If its value has changed, just log the fact.
     *
     * @param origImageUrl original origImageUrl
     * @param imageUrl updated (potentially different) origImageUrl
     */
    private void checkUrl(String label, String origImageUrl, String imageUrl) {
        if (urlChanged(label, origImageUrl, imageUrl)) {
            if (Settings.isAppTraceDetails()) {
                trace(String.format("Loading new image instead: %s.", imageUrl));
            }
        }
    }
    private boolean urlChanged(String label, String origImageUrl, String imageUrl) {
        boolean changed = !origImageUrl.equals(imageUrl);
        if (changed) {
            String oldUrl = Support.truncImageString(origImageUrl);
            String newUrl = Support.truncImageString(imageUrl);
            trace(String.format(
                    "Image request has changed [%s]: orig=%s new=%s.",
                    label, oldUrl, newUrl));
        }
        return changed;
    }

    /** Get the default image. */
    static Bitmap getNoImageBitmap(Resources resources) {
            return BitmapFactory.decodeResource(resources, R.drawable.noimage);
    }

    /** Create a blank image. */
    static Bitmap getBlankImageBitmap() {
        return blankImageBitmap;
    }

    /**
     * Tracing method for this class.
     *
     * @param msg message to be logged.
     */
    private static void trace(String msg) {
        Support.trc(Settings.isImageLoaderTrace(), "Image Loader", msg);
    }
}

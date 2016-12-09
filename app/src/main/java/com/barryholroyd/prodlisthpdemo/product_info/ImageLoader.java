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
    static final int IMAGE_HSIZE = 100;

    /** Max. number of width pixels in product image. */
    static final int IMAGE_WSIZE = 100;

    /** In-memory caching instance. */
    public static CacheMemoryImage cacheMemory = null;

    /** Disk caching instance (singleton). */
    public static CacheDiskImage cacheDiskImage = null;

    /** Blank image bitmap used to fill the header "image" slot. */
    private static Bitmap blankImageBitmap =
            Bitmap.createBitmap(IMAGE_WSIZE, IMAGE_HSIZE, Bitmap.Config.ARGB_8888);

    /** Standard Activity instance. */
    Activity a;

    /** Weak Reference for using the Activity in background threads. */
    WeakReference<Activity> wrActivity = null;

    /** Resources hook. */
    Resources resources = null;

    /**
     * This stores the most current requested url for the ViewHolder instance.
     *
     * @see #checkUrlChanged(String, String, String)
     */
    private String currentUrl;

    /**
     * Standard constructor.
     * <p>
     * Initializes caches and stores a WeakReference to the current Activity.
     * <p>
     *     The Activity instance is used to print Toasts from the background and
     *     to call runOnUiThread() from a worker thread. We use a WeakReference so
     *     that we can determine whether or not it is still valid (e.g., a device
     *     rotation will destroy the current Activity).
     * @param _a    reference to the current Activity.
     */
    public ImageLoader(Activity _a) {
        a = _a;
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

        // "currentUrl" is directly accessible by background threads.
        currentUrl = url;

        /*
         * Background: load from disk or network.
         * Both LiThread and LiAsyncTask have to be defined as nested classes so that
         * they can have access to "currentUrl".
         */
        if (Settings.isAppUseThreads()) (new LiThread(iv, url)).start();
        else new LiAsyncTask(iv, url).execute();
    }

    /**
     * Threads-based version of loading the image in the background.
     *
     * @see #load(ImageView, String)
     */
    private class LiThread extends Thread
    {
        private ImageView iv;
        private String url;

        LiThread(ImageView _iv, String _url) {
            iv= _iv;
            url = _url;
        }

        @Override
        public void run() {
            /*
             * We have already tried pulling the bitmap from the memory
             * cache (that happens in the foreground), but didn't find it
             * there.
             */
            Bitmap bitmap;

            // Check for a null url.
            bitmap = setImageNullCheck(url);
            if (bitmap != null) {
                setImageView(iv, getNoImageBitmap(resources));
                return;
            }

            // Try the disk cache.
            url = checkUrlChanged("Pre-disk cache", url, currentUrl);
            bitmap = setImageDiskCache(url);
            if (bitmap != null) {
                setImageView(iv, bitmap);
                return;
            }

            // Get the image from the network. Bitmap is guaranteed to be non-null.
            url = checkUrlChanged("Pre-network", url, currentUrl);
            bitmap = setImageNetwork(url);
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
        private ImageView iv;
        private String url;

        LiAsyncTask(ImageView _iv, String _url) {
            iv= _iv;
            url = _url;
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

            // Check for a null url.
            bitmap = setImageNullCheck(url);
            if (bitmap != null) { return bitmap; }

            // Try the disk cache.
            url = checkUrlChanged("Pre-disk cache", url, currentUrl);
            bitmap = setImageDiskCache(url);
            if (bitmap != null) { return bitmap; }

            // Get the image from the network. Bitmap is guaranteed to be non-null.
            url = checkUrlChanged("Pre-network", url, currentUrl);
            bitmap = setImageNetwork(url);
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
     * If no image url was provided, then try to provide a bitmap for default image.
     *
     * @param url url for the image.
     * @return    a default image bitmap if no image url was provided; else null.
     *            null indicates that the caller should continue processing -- the url is valid
     *            and we still need the bitmap.
     */
    private Bitmap setImageNullCheck(String url) {
        if (url == null) {
            trace(String.format("No image provided -- loading default image."));
            return getNoImageBitmap(resources);
        }
        return null;
    }

    /**
     * Attempt to get the image bitmap from the disk cache.
     *
     * If successful, add it to the memory cache.
     *
     * @param url url for the image.
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
     * @param url url for the image.
     * @return    bitmap from the image.
     */
    private Bitmap setImageNetwork(String url) {
        Bitmap bitmap;
        try {
            bitmap = NetworkSupport.getImageFromNetwork(url, IMAGE_HSIZE, IMAGE_WSIZE);
        }
        catch (NetworkSupportException nse) {
            String msg = String.format(String.format(
                    "NetworkSupportException: %s", nse.getMessage()));
            Support.loge(msg);
            Toaster.display(wrActivity, msg);
            return getNoImageBitmap(resources);
        }

        // We may have obtained a bitmap, but has the url changed since we requested it?
        if (!url.equals(currentUrl)) {
                /*
                 * The image request changed at the last instant. Give up and let the
                 * later thread handling the newer image request get it loaded. In rare
                 * cases, the default image may stayed displayed. I believe this happens if
                 * this thread ends up executing after the "other" thread. Pragmatically,
                 * that isn't a problem with the memory and disk caches in place.
                 */
            String newUrl = Support.truncImageString(currentUrl);
            trace(String.format("Loading default image instead of %s.", newUrl));
            return getNoImageBitmap(resources);
        }

        if (bitmap != null) {
            cacheMemory.add(url, bitmap);
            cacheDiskImage.add(wrActivity, url, bitmap);
        }
        else {
            bitmap = getNoImageBitmap(resources);
        }

        return bitmap;
    }

    /** Check to see if the url has changed.
     * <p>
     * Even though {@code url} will have just been set to {@code currentUrl} before starting the
     * thread which calls this method, time may have passed and the value of
     * currentUrl may have changed. This can happen, for example, when the {@code ViewHolder}
     * gets recycled. All of its other fields will have been updated to reflect the
     * new row it is responsible for, but the image field may not have been updated
     * in time. When this occurs, we simply log the event and then try to load the
     * image from the new url instead. (That is a small optimization since that url
     * would also get loaded subsequently by a newer thread.) Most importantly, we
     * do <i>not</i> load the "old" image.
     * <p>
     * Since {@code url} is passed in to the constructor and stored locally, it retains
     * the original value. Since {@code currentUrl} is a field of {@code ProductListViewHolder},
     * its current value is accessible to the caller of this method
     * ({@code LiThread}'s {@code run()}).
     *
     * @param url original url
     * @param currentUrl updated (potentially different) url
     */
    private String checkUrlChanged(String label, String url, String currentUrl) {
        if (!url.equals(currentUrl)) {
            String oldUrl = Support.truncImageString(url);
            String newUrl = Support.truncImageString(currentUrl);
            trace(String.format(
                    "Image request has changed [%s]: old=%s new=%s.",
                    label, oldUrl, newUrl));
            return currentUrl;
        }
        else
            return url;
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
    static private void trace(String msg) {
        Support.trc(Settings.isImageLoaderTrace(), "Image Loader", msg);
    }
}

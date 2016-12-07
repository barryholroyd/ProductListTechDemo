package com.barryholroyd.productsdemo.product_info;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.barryholroyd.productsdemo.config.Settings;
import com.barryholroyd.productsdemo.R;
import com.barryholroyd.productsdemo.cache_disk.CacheDiskImage;
import com.barryholroyd.productsdemo.cache_memory.CacheMemoryImage;
import com.barryholroyd.productsdemo.network.NetworkSupport;
import com.barryholroyd.productsdemo.network.NetworkSupportException;
import com.barryholroyd.productsdemo.support.Support;
import com.barryholroyd.productsdemo.support.Toaster;

import java.lang.ref.WeakReference;

/**
 * Load
 */

public class ImageLoader {
    /*
     * Assuming the the BitmapFactory default image format of ARGB_8888 (4 bytes):
     * Image (bitmap) size: 100 * 100 * 4 = 40,000 bytes.
     */

    /** Max. number of height pixels in product image. */
    static final int IMAGE_HSIZE = 100;

    /** Max. number of width pixels in product image. */
    static final int IMAGE_WSIZE = 100;

    /** In-memory caching instance. */
    public static CacheMemoryImage cacheMemory = null;

    /** Disk caching instance (singleton). */
    private static CacheDiskImage cacheDiskImage = null;

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

    public ImageLoader(Activity _a) {
        a = _a;
        wrActivity = new WeakReference<>(a);
        resources = a.getResources();
        if (cacheMemory == null) {
            cacheMemory = Settings.isMemoryCacheBytes()
                    ? CacheMemoryImage.createWithBytes(Settings.getMemoryCacheSizeBytes())
                    : CacheMemoryImage.createWithPercent(Settings.getMemoryCacheSizePercent());
        }
        if (cacheDiskImage == null) {
            cacheDiskImage = CacheDiskImage.getInstance(
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

        // "currentUrl" is directly accessible by background threads
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

        @Override
        protected void onPostExecute(Bitmap bitmap) {
                iv.setImageBitmap(bitmap);
        }
    }

    private Bitmap setImageNullCheck(String url) {
        if (url == null) {
            trace(String.format("No image provided -- loading default image."));
            return getNoImageBitmap(resources);
        }
        return null;
    }

    private Bitmap setImageDiskCache(String url) {
        Bitmap bitmap = cacheDiskImage.get(url);
        if (bitmap != null) {
            cacheMemory.add(url, bitmap);
        }
        return bitmap;
    }

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
                 * NTH: display the proper image if the other thread has already run.
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
     * Even though "url" will have just been set to currentUrl before starting the
     * thread which calls this method, time may have passed and the value of
     * currentUrl may have changed. This can happen, for example, when the ViewHolder
     * gets recycled. All of its other fields will have been updated to reflect the
     * new row it is responsible for, but the image field may not have been updated
     * in time. When this occurs, we simply log the event and then try to load the
     * image from the new url instead. (That is a small optimization since that url
     * would also get loaded subsequently by a newer Thread.) Nost importantly, we
     * do *not* load the "old" image.
     * <p>
     * Since "url" is passed in to the constructor and stored locally, it retains
     * the original value. Since "currentUrl" is a field of ProductListViewHolder,
     * its current value is accessible to the caller of this method (LiThread's run()).
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

    /** Get the default "no image" image. */
    static Bitmap getNoImageBitmap(Resources resources) {
            return BitmapFactory.decodeResource(resources, R.drawable.noimage);
    }

    /** Create a blank image. */
    static Bitmap getBlankImageBitmap() {
        return blankImageBitmap;
    }

    /**
     * Tracing method for app overall.
     *
     * @param msg message to be logged.
     */
    static private void trace(String msg) {
        Support.trc(Settings.isImageLoaderTrace(), "Image Loader", msg);
    }
}

package com.barryholroyd.walmartproducts;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

/**
 * Load an image into an ImageView.
 * <p>
 * <ol>
 *     <li> Attempt to load from memory cache.
 *     <li> If that fails, load from background.
 *     <ol>
 *         <li>Attempt to load from disk cache.
 *         <li>If that fails, attempt to load from network.
 *         <li>When network load completes:
 *         <ol>
 *             <li>add to memory and disk caches
 *             <li>check to see if the URL loaded from is the same as the current
 *                 URL (it may have chanced if the containing ViewHolder got re-allocated).
 *             <li>If it is the same, then load it into the ImageView (in the main/UI thread).
 *             <li>If it is different, ignore and do nothing (the new URL will have already
 *                 been either loaded or queued to be loaded).
 *         </ol>
 *     </ol>
 * </ol>
 */
// TBD: Delete all of this?
public class ImageLoader {
//    /**
//     * If true, use threads to handle background loading; otherwise, use AsyncTask.
//     * I've implemented both approaches for practice.
//     */
//    static private final boolean USE_THREADS = true;
//
//    /** In-memory caching instance. */
//    static private ImageCacheMemory cacheMemory;
//
//    static {
//        cacheMemory = new ImageCacheMemory();
//        cacheMemory.setCacheSizePercentMaxMemory(10);
//
//    }
//
//    static void load(final Activity a, final ImageView iv, final String url) {
//        /*
//         * Foreground: load from memory cache, if present.
//         */
//        Bitmap bitmap = cacheMemory.get(url);
//        if (bitmap != null) {
//            iv.setImageBitmap(bitmap);
//            return;
//        }
//
//        /*
//         * Background: load from disk or network.
//         */
//        if (USE_THREADS) ImageLoaderThreads.load(a, iv, url, cacheMemory);
//        // TBD: else imageLoaderAsyncTask.load(iv, url);
//    }
}

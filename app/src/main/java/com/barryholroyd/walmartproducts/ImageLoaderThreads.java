package com.barryholroyd.walmartproducts;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Threads-based approach to loading images. Check the disk cache first; if that
 * doesn't work, then pull the image from the network.
 */

public class ImageLoaderThreads {
    static final String CACHE_DIRNAME_THREADS = "imageloader_cache_threads";

    static void load(Activity a, ImageView iv, String url, ImageCacheMemory cacheMemory) {
        Bitmap bitmap;

        // TBD: put this in the background

        // Try the disk cache.
        bitmap = ImageCacheDisk.getInstance(a, CACHE_DIRNAME_THREADS).get(a, url);
        if (bitmap != null) {
            cacheMemory.add(url, bitmap);
            iv.setImageBitmap(bitmap);
            return;
        }

        // Try the network.
        bitmap = NetworkSupport.getImageFromNetwork(a, url);

        prBitmapInfo(bitmap);
        if (bitmap != null) {
            cacheMemory.add(url, bitmap);
            ImageCacheDisk.add(a, url, bitmap);
            iv.setImageBitmap(bitmap);
            return;
        }

        Support.loge(String.format("ImageLoaderThreads() - Could not load image from %s\n", url));
    }
    static private void prBitmapInfo(Bitmap bitmap) {
        // DEL: when no longer needed
        Support.logd(String.format("Bitmap Config: %s", bitmap.getConfig()));
    }
}

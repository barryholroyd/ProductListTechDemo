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
        // TBD: put this in the background
        Bitmap bitmap = ImageCacheDisk.getInstance(a, CACHE_DIRNAME_THREADS).getImage(a, url);
        if (bitmap != null) {
            // TBD: add to cacheMemory
            // TBD: set imageView
        }
        // TBD: else go to network.
//        f.get
//        return NetworkSupport.getImageFromNetwork(a, filename);
//        else {
//            // create the file
//            Bitmap bitmap = NetworkSupport.getImageFromNetwork(a, filename);
//            // TBD: HERE
//        }

    }
}

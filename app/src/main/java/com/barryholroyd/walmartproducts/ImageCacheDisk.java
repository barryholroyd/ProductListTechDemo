package com.barryholroyd.walmartproducts;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Disk cache implementation.
 * <p>
 * This is based, in part, on the DiskLruCache implementation in the Android source code base.
 *
 * @see <a href="https://android.googlesource.com/platform/libcore/+/jb-mr2-release/luni/src/main/java/libcore/io/DiskLruCache.java">
 *      DiskLruCache Implementation</a>
 */
class ImageCacheDisk
{
    /**
     * Singleton.
     * <p>
     * A singleton is needed (as opposed to a set of static methods) because a Context instance
     * is needed to obtain the cache directory and that isn't available at static initialization
     * time (and we shouldn't store a Context instance on a static hook).
     * <p>
     * There are several approaches for implementing singletons. This one is used because
     * it can be lazily initialized (i.e., doesn't have to be initialized during static
     * initialization at class loading time). The double check lock problem is addressed by
     * making the instance "volatile" -- that ensure that the instance is fully initialized
     * before becoming visible to any other threads.
     */
    static volatile private ImageCacheDisk instance;

    static final File cacheDir;

    private Context ctx;

    private ImageCacheDisk(Context _ctx) {
        ctx = _ctx;
    }

    ImageCacheDisk getInstance(Context ctx) {
        if (instance != null)
            return instance;

        synchronized(ImageCacheDisk.class) {
            if (instance != null)
                instance = new ImageCacheDisk(ctx);
        }
        return instance;
    }

    /**
     * Get / create a unique subdirectory in the app's standard cache directory.
     * This uses external storage if there is any mounted; otherwise, it uses
     * internal storage.
     *
     * @param ctx   standard Context instance
     * @param dirName   name of cache subdirectory
     * @return  File handle for the cache subdirectory
     */
    static private File getDiskCacheDir(Context ctx, String dirName) {
        String cachePath = externalStorageAvailable()
                ? ctx.getExternalCacheDir().getPath()
                : ctx.getCacheDir().getPath();
        Support.logd("Cache Directory: " + cachePath + File.separator + dirName);
        return new File(cachePath + File.separator + dirName);
    }

    /**
     * Test for the presence of external storage.
     * <p>
     * isExternalStorageRemovable() only returns false if it is built-in.
     * If it isn't built-in, check to see if some external storage has been mounted.
     *
     * @return true iff external storage is present.
     */
    static private boolean externalStorageAvailable() {
        return !Environment.isExternalStorageRemovable() ||
                Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}

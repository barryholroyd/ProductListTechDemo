package com.barryholroyd.walmartproducts;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Disk cache implementation.
 * <p>
 * This is based, in part, on the DiskLruCache implementation in the Android source code base.
 *
 * @see <a href="https://android.googlesource.com/platform/libcore/+/jb-mr2-release/luni/src/main/java/libcore/io/DiskLruCache.java">
 *      DiskLruCache Implementation</a>
 */
final class ImageCacheDisk
{
    /**
     * Singleton.
     * <p>
     * A singleton is used (as opposed to a set of static methods) because an Activity instance
     * is needed to obtain the cache directory and that isn't available at static initialization
     * time (and we shouldn't store an Activity instance on a static hook). Beyond that, we don't
     * need separate instances.
     * <p>
     * There are several approaches for implementing singletons. This one is used because
     * it can be lazily initialized (i.e., doesn't have to be initialized during static
     * initialization at class loading time). The double check lock problem is addressed by
     * making the instance "volatile" -- that ensure that the instance is fully initialized
     * before becoming visible to any other threads.
     */
    static volatile private ImageCacheDisk instance;

    /** Debugging flag. */
    static boolean imageCacheDiskTrace = false;

    /** Full file name for the cache subdirectory. */
    final String cacheDirName;

    /** File handle for the disk cache subdirectory. */
    final File cacheDir;

    /**
     * Constructor.
     * <p>
     * We pass in the subdirectory name so that different apps can provide different
     * cache subdirectories.
     *
     * @param a   standard Activity instance.
     * @param cacheSubdirName  subdirectory name for the cache -- unique to this app/usage.
     */
    private ImageCacheDisk(Activity a, String cacheSubdirName) {
        cacheDirName = getDiskCacheDirName(a, cacheSubdirName);
        cacheDir = new File(cacheDirName);
        if (cacheDir.exists()) {
            trace(String.format("cache directory already exists: %s", cacheDirName));
            if (!cacheDir.isDirectory()) {
                throw new ImageDiskCacheException(
                        String.format("Disk cache exists but is not a directory: %s",
                                cacheDirName));
            }
            return;
        }
        trace(String.format("cache directory being created: %s", cacheDirName));
        if (!cacheDir.mkdirs()) {
            throw new RuntimeException("Could not create disk cache directory.");
        }
    }

    /**
     * Standard singleton getInstance() method.
     * <p>
     * We avoid the double check lock issue by making the "instance" field "volatile".
     * @param a   standard Activity instance.
     * @param cacheDirName  subdirectory name for the cache -- unique to this app/usage.
     * @return  singleton instance.
     */
    static ImageCacheDisk getInstance(Activity a, String cacheDirName) {
        if (instance != null)
            return instance;

        synchronized(ImageCacheDisk.class) {
            if (instance == null)
                instance = new ImageCacheDisk(a, cacheDirName);
        }
        return instance;
    }

    /**
     * Get the image from the disk, if it is present on disk.
     * If not, get it from the network and cache it.
     *
     * @param url url for the bitmap.
     * @return bitmap obtained from the URL.
     */
    Bitmap get(String url) {
        Entry entry = getEntry(url);
        String filename = entry.getImageFilenameLong();
        File f = new File(filename);
        if (f.exists()) {
            trace(String.format("Getting [found]: %s", url));
            if (f.isFile()) {
                Bitmap bitmap = BitmapFactory.decodeFile(filename);
                if (bitmap == null) {
                    throw new RuntimeException("Could not obtain bitmap from factory.");
                }
                return bitmap;
            }
            else {
                throw new RuntimeException("Bad file: " + f.getName());
            }
        }
        trace(String.format("Getting [not found]: %s", url));
        return null;
    }

    void add(Activity a, String url, Bitmap bitmap) {
        Entry entry = getEntry(url);
        String filename = entry.getImageFilenameLong();

        Support.logd(String.format("Adding [file=%s]: %s", filename, url));

        // TBD: check the overall space used so far.

        try (FileOutputStream fos = new FileOutputStream(filename)) {
            // PNG is the preferred format; that will also cause the second parameter, quality,
            // to be ignored.
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos)) {
                String msg = String.format(
                        "ImageCacheDisk - file could not be written out: %s",
                        filename);
                (new Toaster(a)).display(msg);
                Support.loge(msg);
            }
        }
        catch (IOException ioe) {
            String msg = String.format("ImageCacheDisk - IO Exception: %s", filename);
            (new Toaster(a)).display(msg);
            Support.loge(msg);
        }
    }

    /**
     * Get / create a unique subdirectory in the app's standard cache directory.
     * This uses external storage if there is any mounted; otherwise, it uses
     * internal storage.
     *
     * @param a   standard Context instance.
     * @param cacheSubdirName  subdirectory name for the cache -- unique to this app/usage.
     * @return  File handle for the cache subdirectory
     */
    private String getDiskCacheDirName(Activity a, String cacheSubdirName) {
        String cachePath = externalStorageAvailable()
                ? a.getExternalCacheDir().getPath()
                : a.getCacheDir().getPath();
        return cachePath + File.separator + cacheSubdirName;
    }

    /**
     * Test for the presence of external storage.
     * <p>
     * isExternalStorageRemovable() only returns false if it is built-in.
     * If it isn't built-in, check to see if some external storage has been mounted.
     *
     * @return true iff external storage is present.
     */
    private boolean externalStorageAvailable() {
        return !Environment.isExternalStorageRemovable() ||
                Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /** Mapping of URLs to Entrys.
     */
    static private HashMap<String,Entry> entryHm = new HashMap<>();

    /**
     * Get the Entry for the specified URL.
     * Creat the Entry, if necessary.
     *
     * @param url   url of the image to be loaded.
     * @return  Entry representing the image to be loaded.
     */
    private Entry getEntry(String url) {
        Entry entry = entryHm.get(url);
        if (entry != null)
            return entry;

        entry = new Entry(url);
        entryHm.put(url, entry);
        return entry;
    }

    private void trace(String msg) {
        Support.trace(imageCacheDiskTrace, "Cache Disk", msg);
    }

    /**
     * Counter value provides last part of filename.
     * Can create a few billion unique filenames.
     * Must be outside of the Entry definition, since it is static and Entry isn't.
     */
    static private int entryCounter = 0;
    private class Entry
    {
        private String url;
        static final private String FILENAME_BASE = "image";
        private long id;
        private String fname;

        Entry(String _url) {
            url = _url;
            id  = entryCounter++;
            fname = String.format("%s-%d", FILENAME_BASE, id);
        }

        String getImageFilenameShort() {
            return fname;
        }

        String getImageFilenameLong() {
            return cacheDirName + File.separator + fname;
        }
    }
}

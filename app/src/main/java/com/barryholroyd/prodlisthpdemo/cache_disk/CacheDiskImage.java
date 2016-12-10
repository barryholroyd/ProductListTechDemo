package com.barryholroyd.prodlisthpdemo.cache_disk;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.barryholroyd.prodlisthpdemo.config.Settings;
import com.barryholroyd.prodlisthpdemo.support.Support;
import com.barryholroyd.prodlisthpdemo.support.Toaster;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

import static com.barryholroyd.prodlisthpdemo.support.Support.truncImageString;

/**
 * Disk cache implementation.
 * <p>
 * This is based, in part, on the DiskLruCache implementation in the Android source code base.
 *
 * @see <a href="https://android.googlesource.com/platform/libcore/+/jb-mr2-release/luni/src/main/java/libcore/io/DiskLruCache.java">
 *      DiskLruCache Implementation</a>
 */
public final class CacheDiskImage
{
    /**
     * Singleton for caching images on disk.
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
     * <p>
     * Synchronization occurs at a high level, via the get() and add() methods. As a result,
     * accesses of lower level entities (such as getEntity(), icdHm and icdLl) do not have
     * to be separately synchronized.
     */
    static volatile private CacheDiskImage instance;

    /** Base name for image files. */
    static final private String FILENAME_BASE = "image";

    /**
     * Internal key/value mapping for cache storage.
     * This does not have to be synchronized since all accesses of it
     * come from within synchronized methods (get() and add()).
     */
    private final HashMap<String,Entry> icdHm  = new HashMap<>();

    /**
     * First in / first out tracking for deleting cache entries.
     * This does not have to be synchronized since all accesses of it
     * come from within add(), which is already synchronized.
     */
    private final LinkedList<String> icdLl = new LinkedList<>();

    /** Full file name for the cache subdirectory. */
    private final String cacheDirName;

    /** Maximum sizeBitmap of the cache in bytes. */
    private long maxCacheSize = 0;

    /** Current sizeBitmap (bytes used) of the disk cache. */
    private long currentCacheSize = 0;

    /**
     * When we need to make room in the cache, we'll make enough for at least this many
     * additional images the size of the one being added.
     */
    static final private int IMAGE_SIZE_MULTIPLIER = 10;

    /**
     * Constructor.
     * <p>
     * We pass in the subdirectory name so that different apps can provide different
     * cache subdirectories. Synchronization isn't necessary since this is only called
     * from a synchronized block within makeInstance().
     *
     * @param a   standard Activity instance.
     * @param cacheSubdirName  subdirectory name for the cache -- unique to this app/usage.
     */
    private CacheDiskImage(Activity a, String cacheSubdirName, long _maxCacheSize) {
        maxCacheSize = _maxCacheSize;
        trace(String.format(Locale.US, "Setting maximum disk cache size: %d", maxCacheSize));
        cacheDirName = getDiskCacheDirName(a, cacheSubdirName);
        /* File handle for the disk cache subdirectory. */
        File cacheDir = new File(cacheDirName);
        if (cacheDir.exists()) {
            if (Settings.isDiskCacheClear()) {
                trace(String.format("Deleting cache directory: %s", cacheDirName));
                for (File f : cacheDir.listFiles()) {
                    trace(String.format("  Deleting: %s", f.getName()));
                    deleteFile(f);
                }
                deleteFile(cacheDir);
            }
            else {
                trace(String.format("Retaining cache directory: %s", cacheDirName));
                if (!cacheDir.isDirectory()) {
                    throw new CacheDiskImageException(
                            String.format("Disk cache exists but is not a directory: %s",
                                    cacheDirName));
                }
                return;
            }
        }
        trace(String.format("Creating cache directory: %s", cacheDirName));
        if (!cacheDir.mkdirs()) {
            throw new CacheDiskImageException("Could not create disk cache directory.");
        }
    }

    /**
     * Delete a file and throw an exception if the deletion fails.
     *
     * @param f the File instance for the file to be deleted.
     */
    private static void deleteFile(File f) {
        if (!f.delete()) {
            throw new CacheDiskImageException(
                    String.format("Could not delete file: %s", f.getName()));
        }
    }

    /**
     * Standard singleton makeInstance() method.
     * <p>
     * We avoid the double check lock issue by making the "instance" field "volatile".
     *
     * @param a   standard Activity instance.
     * @param cacheDirName  subdirectory name for the cache -- unique to this app/usage.
     * @return  singleton instance.
     */
    public static CacheDiskImage makeInstance(Activity a, String cacheDirName, long maxCacheSize) {
        if (instance != null)
            return instance;

        synchronized(CacheDiskImage.class) {
            if (instance == null)
                instance = new CacheDiskImage(a, cacheDirName, maxCacheSize);
        }
        return instance;
    }

    /**
     * Change the maximum disk cache size.
     *
     * @param _maxCacheSize the maximum cache size.
     */
    public static void setMaxCacheSize(long _maxCacheSize) {
        if (instance == null)
            throw new CacheDiskImageException("null instance");
        instance.maxCacheSize = _maxCacheSize;
        trace(String.format(Locale.US, "Setting maximum disk cache size: %d", instance.maxCacheSize));
    }

    /**
     * Get the image from the disk, if it is present on disk.
     * If not, get it from the network and cache it.
     *
     * @param url url for the bitmap.
     * @return bitmap obtained from the URL.
     */
    public synchronized Bitmap get(String url) {
        if (!Settings.isDiskCacheOn())
            return null;

        Entry entry = getEntry(url);   // this always returns a valid entry.
        boolean stored = entry.isStored();

        trace(String.format("Getting [%s]: %s", stored ? "found" : "not found", url));

        if (!stored)
            return null;

        String filename = entry.longName;
        File f = new File(filename);
        fileCheck(f, url);
        Bitmap bitmap = BitmapFactory.decodeFile(filename);
        if (bitmap == null) {
            throw new CacheDiskImageException("Could not obtain bitmap from factory.");
        }
        return bitmap;
    }

    /**
     * Add an image bitmap to the disk cache.
     *
     * Delete entries for the cache, if/as necessary.
     *
     * @param wrActivity    the current Activity.
     * @param url           the url for the image to be stored.
     * @param bitmap        the bitmap of the image to be stored.
     */
    public synchronized void add(WeakReference<Activity> wrActivity, String url, Bitmap bitmap) {
        if (!Settings.isDiskCacheOn())
            return;

        if (url == null) {
            trace(String.format("Adding: %s", url));
            throw new CacheDiskImageException("null url");
        }

        Entry entry = getEntry(url);   // this always returns a valid entry.
        String filename = entry.longName;
        trace(String.format("Adding [file=%s]: %s", entry.shortName, url));

        if (maxCacheSize == 0) {
            throw new CacheDiskImageException("cache sizeBitmap not initialized.");
        }

        if (entry.isStored()) {
            trace(String.format("Already added this image... returning\n"));
            return;
        }

        entry.setSizeBitmap(bitmap.getByteCount());

        /*
         * Clear cache entries, if necessary.
         *
         * Catch-22: We need to know the size of the compressed bitmap as a file
         * so that we can determine whether or not to free up additional space. However,
         * we won't know the file size until we write it out. Instead, we make the
         * assumption that the compressed file will be smaller than the original bitmap
         * and use the original bitmap's size to determine whether or not to delete
         * some of the cached files.
         */
        prSizes("Initial", entry);
        if (currentCacheSize + entry.getSizeBitmap() > maxCacheSize) {
            while ((currentCacheSize + (entry.getSizeBitmap() * IMAGE_SIZE_MULTIPLIER)) > maxCacheSize) {
                if (icdLl.isEmpty()) {
                    break;
                }

                // Remove entry from internal data structures.
                String lastImage = icdLl.removeLast();
                if (lastImage == null) {
                    throw new CacheDiskImageException("null key when removing entries.");
                }
                Entry lastEntry = icdHm.get(lastImage);

                // Delete the bitmap file.
                String msg = String.format("Removing: File=%s Url=%s",
                        lastEntry.shortName, truncImageString(lastEntry.url));
                trace(msg);

                File le = new File(lastEntry.longName);
                long lastFileSize = le.length();
                prSizes("Pre-removal", lastEntry);
                fileCheck(le, lastEntry.url);
                deleteFile(le);
                lastEntry.setStored(false);
                currentCacheSize -= lastFileSize;
                prSizes("Post-removal", lastEntry);
            }
        }

        try (FileOutputStream fos = new FileOutputStream(filename)) {
            /*
             * PNG is the preferred format; that will also cause the second parameter, quality,
             * to be ignored.
             */
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos)) {
                File f = new File(entry.longName);
                entry.setSizeFile(f.length());
                currentCacheSize += entry.getSizeFile();
                icdLl.add(entry.url);
                entry.setStored(true);
                prSizes("Final", entry);
            }
            else {
                String msg = String.format(
                        "CacheDiskImage - file could not be written out: %s", filename);
                Toaster.display(wrActivity, msg);
                Support.loge(msg);
            }
        }
        catch (IOException ioe) {
            String msg = String.format("CacheDiskImage - IO Exception: %s", filename);
            Toaster.display(wrActivity, msg);
            Support.loge(msg);
        }
    }

    /**
     * Log detailed disk caching information.
     *
     * @param tag   unique tag to represent the caller.
     * @param entry the disk cache entry to be logged.
     */
    private void prSizes(String tag, Entry entry) {
        if (Settings.isAppTraceDetails()) {
            String msg = String.format(Locale.US,
                    "  DETAILS: %s: Cur:Max=%d:%d [File=%s  Url=%s] [File=%d, Bitmap=%d]",
                    tag, currentCacheSize, maxCacheSize,
                    entry.shortName, truncImageString(entry.url),
                    entry.getSizeFile(), entry.getSizeBitmap());
            trace(msg);
        }
    }

    /**
     * Get/create a unique subdirectory in the app's standard cache directory.
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
        String cacheDirName = cachePath + File.separator + cacheSubdirName;
        trace("Cache directory name: " + cacheDirName);
        return cacheDirName;
    }

    /**
     * Check to ensure the specified File object exists and is a normal file.
     *
     * @param f     the File object of interest (it is for a bitmap).
     * @param url   the url of the image associated with the bitmap.
     *              It serves as the unique id for the cached image bitmap.
     */
    private void fileCheck(File f, String url) {
        if (!f.exists()) {
            throw new CacheDiskImageException(
                    String.format("Missing [file=%s]: %s", f.getName(), url));
        }
        if (!f.isFile()) {
            throw new CacheDiskImageException(
                    String.format("File is not a normal file [file=%s]: %s", f.getName(), url));
        }
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

    /**
     * Get the Entry for the specified URL.
     * Create the Entry, if necessary.
     * This does not have to be synchronized since all calls to it
     * come from synchronized methods (get() and add()).
     *
     * @param url   url of the image to be loaded.
     * @return  Entry representing the image to be loaded.
     */
    private synchronized Entry getEntry(String url) {
        Entry entry = icdHm.get(url);
        if (entry == null) {
            entry = new Entry(url);
        }
        return entry;
    }

    /**
     * Tracing method specific to the disk cache.
     *
     * @param msg message to be logged.
     */
    private static void trace(String msg) {
        Support.trc(Settings.isDiskCacheTrace(), "Cache Disk", msg);
    }

    /**
     * Counter value provides last part of filename.
     * Can create a few billion unique filenames.
     * Must be outside of the Entry definition, since it is static and, as an inner
     * class, Entry can't be.
     */
    private int entryCounter = 0;

    /**
     * The Entry class provides a wrapper containing metadata for each image that might
     * be stored in the disk cache. Once an entry is created for a given image (as identified
     * by the image's url), it exists indefinitely.
     * <p>
     * All entries are stored in icdHm.
     * However, entries are only stored in icdLl when their image is
     * stored in the file system.
     * <p>
     * Each image is uniquely identified by its url. In addition, a unique long "id"
     * is generated for each image (there is a 1:1 mapping between urls and ids) and
     * used to create the cache filename.
     * <p>
     * The url and id are stored in the Entry as identifiers. The short and long names
     * are stored for performance reasons, as
     */
    private class Entry
    {
        final private String url;       // unique identifier for the image.
        final private long id;          // unique id for the image (usable in its filename).
        final private String shortName; // base name for the image file (including its id).
        final private String longName;  // full name for the image file.
        private long sizeBitmap;        // size of the bitmap in bytes.
        private long sizeFile;          // size of the image file on disk in bytes.
        private boolean stored;         // true iff the image has been stored in the file system.

        /**
         * Constructor.
         *
         * @param _url  Image url. Serves as the unique id for the image's bitmap in the disk cache.
         */
        private Entry(String _url) {
            url   = _url;
            id    = entryCounter++;
            shortName = String.format(Locale.US, "%s-%d", FILENAME_BASE, id);
            longName = cacheDirName + File.separator + shortName;
            icdHm.put(url, this);
        }

        /*
         * Getters and setters.
         */
        private long getSizeBitmap() { return sizeBitmap; }
        private void setSizeBitmap(long _sizeBitmap) { sizeBitmap = _sizeBitmap; }

        private long getSizeFile() { return sizeFile; }
        private void setSizeFile(long _sizeFile) { sizeFile = _sizeFile; }

        private boolean isStored() { return stored; }
        private void setStored(boolean _stored) { stored = _stored; }
    }
}

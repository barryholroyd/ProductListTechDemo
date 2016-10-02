package com.barryholroyd.productsdemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

import static com.barryholroyd.productsdemo.Configure.*;
import static com.barryholroyd.productsdemo.Configure.App.USE_THREADS;
import static com.barryholroyd.productsdemo.Configure.DiskCache.DC_CACHE_DIR;
import static com.barryholroyd.productsdemo.Configure.DiskCache.DC_SIZE_BYTES;
import static com.barryholroyd.productsdemo.Configure.MemoryCache.MC_PERCENT;
import static com.barryholroyd.productsdemo.Configure.MemoryCache.MC_SIZE_BYTES;
import static com.barryholroyd.productsdemo.Configure.MemoryCache.MC_SIZE_PERCENT;

/**
 * Recycler adapter to display the list of products.
 * <p>
 * For demonstration purposes I've implemented both Threads-based and an AsyncTask-based
 * approaches for loading images.
 *
 * @author Barry Holroyd
 */
public class ProductListRecyclerAdapter
	extends RecyclerView.Adapter<ProductListRecyclerAdapter.ProductListViewHolder>
{
    /** "No image" string constant for memory cache. */
    static private final String NO_IMAGE = "NO IMAGE";

	/** Standard Activity instance. */
	Activity a;

    /** In-memory caching instance. */
    private ImageCacheMemory cacheMemory;

    ProductListRecyclerAdapter(Activity _a) {
        a = _a;
        cacheMemory = MC_PERCENT
				? ImageCacheMemory.createWithPercent(MC_SIZE_PERCENT)
				: ImageCacheMemory.createWithBytes(MC_SIZE_BYTES);

    }

	/**
	 * Enum to communicate the type of the row that a given ViewHolder is initialized
	 * for. The View itself is the same for both the header and each item; however,
	 * the header does not have an OnClickListener callback assigned to it.
	 */
	private enum ViewType {
		/** The row is the header. */
		HEADER,
		/** The row is a standard product row. */
		ITEM
	};

	/**
	 * Array of products. Each cell contains information about the specific product.
	 * This is filled in, in batches, from JSON pulled from the cloud.
	 */
	private ProductInfoArrayList pial = new ProductInfoArrayList();

	/**
	 * Mapping table of ProductId to ProductInfo, for quick lookup.
	 */
	private HashMap<String,ProductInfo> pihm = new HashMap<>();

	/**
	 * Getter for the backing array list. Needed for bundling/unbundling across
	 * device configuration changes.
	 *
	 * @return ArrayList backing storage for the adapter.
	 */
	ProductInfoArrayList getProductInfoArrayList() { return pial; }

	/**
	 * Create a ViewHolder to contain a View for each row, inflated from an XML layout file.
	 *
	 * @param vg the ViewGroup into which the new View will be added after
	 *              it is bound to an adapter position
	 * @param viewType the viewType of the new View. In this implementation, it is
	 *                 actually a value from the {@link ProductListRecyclerAdapter.ViewType} enum.
	 * @return a new ViewHolder that holds a View of the given view type new ViewHolder.
	 */
	@Override
	public ProductListViewHolder onCreateViewHolder(ViewGroup vg, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(vg.getContext());
		View v = inflater.inflate(R.layout.product_row, vg, false);
		return new ProductListViewHolder(v, ViewType.values()[viewType]);
	}

	/**
	 * Fill in the ViewHolder passed in with the data at "position" in the array.
	 *
	 * This is done by calling a method on the ViewHolder itself, so that that
	 * data can remain private to the ViewHolder.
	 *
	 * @param viewHolder ViewHolder which should be updated to represent the contents of
	 *                      the item at the given position in the data set.
     * @param position the position of the item within the adapter's data set. Position 0
	 *                 represents the header and so isn't actually in the data set; Position
	 *                 1 represents the first item in the actual data set, so we subtract 1
	 *                 from any position value greater than 0.
	 */
	@Override
	public void onBindViewHolder(ProductListViewHolder viewHolder, int position) {
		if (position >= (getItemCount())) {
			throw new IndexOutOfBoundsException(
				String.format("ProductInfo has %d products; product %d requested.",
					pial.size(), position));
		}
		if (position == 0) {
			viewHolder.bindHeader();
		}
		else {
			viewHolder.bindData(pial.get(position - 1));
		}
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0)
			return ViewType.HEADER.ordinal();
		else
			return ViewType.ITEM.ordinal();
	}

	/**
	 * Return the number of items in the backing array.
	 *
	 * @return the number of items in the backing array (plus 1 for the header).
	 */
	@Override
	public int getItemCount() {
		return pial.size() + 1; // account for the header
	}

	/**
	 * Update the array of products and redisplay the product list.
	 *
	 * @param pialNew the array of new products to add to the data set.
	 */
	void updateData(ProductInfoArrayList pialNew) {
		pial.addAll(pialNew);
		for (ProductInfo pi : pialNew) {
			pihm.put(pi.id, pi);
		}
		notifyDataSetChanged();
	}

	/**
	 * Tracing method for app overall.
	 *
	 * @param msg message to be logged.
	 */
	static private void trace(String msg) {
		Support.trc(Configure.App.APP_TRACE, "App", msg);
	}

	/**
	 * ViewHolder class specific to ProductListRecyclerAdapter.
	 */
	protected class ProductListViewHolder extends RecyclerView.ViewHolder
	{
		/*
		 * Assuming the the BitmapFactory default image format of ARGB_8888 (4 bytes):
		 * Image (bitmap) size: 100 * 100 * 4 = 40,000 bytes.
		 */

		/** Max. number of height pixels in product image. */
		final int IMAGE_HSIZE = 100;

		/** Max. number of width pixels in product image. */
		final int IMAGE_WSIZE = 100;

		/**
		 * The "tvId" field contains the identifier for the product. It is provided
		 * within the is hidden ("gone") in the text view.
		 */
		private final TextView tvId;

		// DEL:
		private final TextView tvImageName;

		/** The product tvName. */
		private final TextView tvName;

		/** A short description of the product. */
		private final TextView tvShortDescription;

		/** Product image view. */
		private final ImageView ivProductImage;

		/**
         * This stores the most current requested url for the ViewHolder instance.
         *
         * @see #isSameUrlString(String, String, String)
         */
		private String currentUrl;

		/**
		 * A single instance of OnClickListener that can be used for all rows
		 * in the displayed product list. We could use a lambda expression instead
		 * of creating a whole new class, but Android's support of Java 8
		 * isn't complete enough yet for it to be worthwhile (even with Jack,
		 * there are trade offs).
		 */
		private final OnClickListenerRow onClickListenerRow = new OnClickListenerRow();

		/**
		 * Constructor for the ViewHolder. Save references to the relevant fields
		 * within the View.
		 *
		 * @param itemView the View (row) to be managed by this ViewHolder.
		 * @param viewType the type of the View for the row. The View is
		 *                 actually the same for both ViewTypes, but HEADER
		 *                 doesn't have an OnClickListener callback.
		 */
		ProductListViewHolder(View itemView, ViewType viewType) {
			super(itemView);
			tvId = (TextView) itemView.findViewById(R.id.id);
			tvImageName = (TextView) itemView.findViewById(R.id.image_name);
			tvName = (TextView) itemView.findViewById(R.id.name);
			tvShortDescription = (TextView) itemView.findViewById(R.id.short_description);
            ivProductImage = (ImageView) itemView.findViewById(R.id.pd_product_image);
			if (viewType == ViewType.ITEM) {
				itemView.setOnClickListener(onClickListenerRow);
			}
		}

		/**
		 * Binds column names to the header row.
		 */
		protected void bindHeader() {
			formatHeaderField(tvName, "Name");
			formatHeaderField(tvShortDescription, "Description");
		}

		/**
		 * Utility method to format header fields.
		 */
		private void formatHeaderField(TextView tv, String label) {
			tv.setText(label);

            // Center the text view i the frame layout.
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) tv.getLayoutParams();
			lp.gravity = Gravity.CENTER;
			tv.setLayoutParams(lp);

            // Center the text within the text view.
            tv.setGravity(Gravity.CENTER);

			tv.setTextSize(30);
			tv.setTypeface(Typeface.create((String) null, Typeface.BOLD_ITALIC));
		}

		/**
		 * Binds actual data passed in by the adapter. Called directly by the
		 * adapter's onBindViewHolder() method. This exists so that the data
		 * can be kept privately within this ViewHolder.
		 *
		 * @param pi the product info for a specific product,
		 *                 read in as JSON from the cloud.
		 */
		protected void bindData(ProductInfo pi) {
			tvId.setText(pi.id);
			tvImageName.setText(Support.truncImageString(pi.imageUrl));
			tvName.setText(pi.name);
			tvShortDescription.setText(pi.shortDescription);
			loadImage(a, ivProductImage, pi.imageUrl);
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
		private void loadImage(Activity a, ImageView iv, String url) {
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
			if (USE_THREADS) (new LiThread(a, iv, url)).start();
			else new LiAsyncTask(url).execute();
		}

		private class LiThread extends Thread
		{
			private Activity a;
			private ImageView iv;
			private String url;
			private Bitmap bitmap;
			private ImageCacheDisk imageCacheDisk;

			LiThread(Activity _a, ImageView _iv, String _url) {
				a = _a;
				iv= _iv;
				url = _url;
				imageCacheDisk = ImageCacheDisk.getInstance(a, DC_CACHE_DIR, DC_SIZE_BYTES);
			}

			@Override
			public void run() {
                if (url == null) {
					trace(String.format("No image provided -- loading default image."));
                    setImageView(iv, Support.getNoImageBitmap(a));
                    return;
                }

                // Try the disk cache.
                if (!isSameUrlString("Pre-disk cache", url, currentUrl))
                    url = currentUrl;
				bitmap = imageCacheDisk.get(url);
				if (bitmap != null) {
					cacheMemory.add(url, bitmap);
					setImageView(iv, bitmap);
					return;
				}

                if (!isSameUrlString("Pre-network", url, currentUrl)) {
                    url = currentUrl;
				}

				try {
                	bitmap = NetworkSupport.getImageFromNetwork(url, IMAGE_HSIZE, IMAGE_WSIZE);
				}
				catch (NetworkSupportException nse) {
					String msg = String.format(String.format(
							"NetworkSupportException: %s", nse.getMessage()));
					Support.loge(msg);
                    Toaster.display(a, msg);
					return;
				}

				if (!isSameUrlString("Post-network", url, currentUrl)) {
                    /*
                     * The image request changed at the last instant. Give up and let the
                     * later thread handling the newer image request get it loaded. In rare
                     * cases, the default image may stayed displayed. I believe this happens if
                     * this thread ends up executing after the "other" thread. Pragmatically,
                     * this isn't a problem with the memory and disk caches in place.
                     * NTH: display the proper image if the other thread has already run.
                     */
                    String oldUrl = Support.truncImageString(url);
                    String newUrl = Support.truncImageString(currentUrl);
                    trace(String.format("Loading default image instead of %s.", newUrl));
                    setImageView(iv, Support.getNoImageBitmap(a));
                    return;
                }
                if (bitmap != null) {
                    cacheMemory.add(url, bitmap);
					imageCacheDisk.add(a, url, bitmap);
					setImageView(iv, bitmap);
					return;
				}
				Support.loge(String.format(
						"ImageLoaderThreads() - Could not load image from %s\n",
						url));
			}

		}

		private class LiAsyncTask extends AsyncTask<String, Void, Bitmap>
		{
			String url;

			LiAsyncTask(String _url) { url = _url; }

			protected Bitmap doInBackground(String... args) {
				return null; // TBD: placeholder
			}
			void postExecute(Bitmap bitmap) {
				// TBD: check currentUrl.
				// TBD: iv = ...;
			}
		}
        // DEL:
//class AsyncTaskNetworkLoader extends AsyncTask<String, Void, Bitmap> {
//    Activity a;
//    ImageView iv;
//
//    AsyncTaskNetworkLoader(Activity _a, ImageView _iv) {
//        a = _a;
//        iv  = _iv;
//    }
//
//    @Override
//    protected Bitmap doInBackground(String ... params) {
//        return NetworkSupport.getImageFromNetwork(a, params[0], 100, 100);
//    }
//
//    @Override
//    protected void onPostExecute(Bitmap bitmap) {
//        if (bitmap != null) {
//            iv.setImageBitmap(bitmap);
//        }
//    }
//}



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
         */
        private boolean isSameUrlString(String label, String url, String currentUrl) {
            if (!url.equals(currentUrl)) {
                String oldUrl = Support.truncImageString(url);
                String newUrl = Support.truncImageString(currentUrl);
                trace(String.format(
                        "Image request has changed [%s]: old=%s new=%s.",
                        label, oldUrl, newUrl));
                return false;
            }
            else
                return true;
        }

        /** Set the ImageView on the main thread. */
        private void setImageView(final ImageView iv, final Bitmap bitmap) {
            a.runOnUiThread(new Runnable() {
                public void run() { iv.setImageBitmap(bitmap); }
            });
        }

		/**
		 * Handle clicks on rows within the product list.
		 */
		private class OnClickListenerRow implements View.OnClickListener
		{
			/**
			 * Clicking on a row will call this method to start up an activity
			 * to display the selected product's information.
			 *
			 * @param v The View for the row to be displayed.
			 */
			@Override
			public void onClick(View v) {
				// Create parcel of the current ProductInfo and pass that to the activity.
				TextView tvId = (TextView) v.findViewById(R.id.id);
				String productId = (String) tvId.getText();

				Intent intent = new Intent(a, ActivityProductInfo.class);
				intent.putExtra(Support.getKeyProductInfo(a), pihm.get(productId));
				a.startActivity(intent);
			}
		}
	}
}

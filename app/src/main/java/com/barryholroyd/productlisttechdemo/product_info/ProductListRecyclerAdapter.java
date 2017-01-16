package com.barryholroyd.productlisttechdemo.product_info;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.barryholroyd.productlisttechdemo.ActivityProductInfo;
import com.barryholroyd.productlisttechdemo.ActivityProductList;
import com.barryholroyd.productlisttechdemo.R;
import com.barryholroyd.productlisttechdemo.config.Settings;
import com.barryholroyd.productlisttechdemo.support.Support;

import java.util.HashMap;
import java.util.Locale;

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
	/** Standard Activity instance. */
	private final Activity a;

    public ProductListRecyclerAdapter(Activity _a) {
        a = _a;
        ActivityProductList.trace(String.format("Approach for loading images in the background: %s.",
				Settings.isAppUseThreads() ? "Threads" : "AsyncTask"));
        ActivityProductList.trace(String.format("Memory caching: %s.", Settings.isMemoryCacheOn() ? "ON" : "OFF"));
        ActivityProductList.trace(String.format("Disk caching: %s.",   Settings.isDiskCacheOn() ? "ON" : "OFF"));
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
	}

	/**
	 * Array of products. Each cell contains information about the specific product.
	 * This is filled in, in batches, from JSON pulled from the cloud.
	 */
	private final ProductInfoArrayList pial = new ProductInfoArrayList();

	/**
	 * Mapping table of ProductId to ProductInfo, for quick lookup.
	 */
	private final HashMap<Integer,ProductInfo> pihm = new HashMap<>();

	/**
	 * Getter for the backing array list. Needed for bundling/unbundling across
	 * device configuration changes.
	 *
	 * @return ArrayList backing storage for the adapter.
	 */
	public ProductInfoArrayList getProductInfoArrayList() { return pial; }

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
				String.format(Locale.US, "ProductInfo has %d products; product %d requested.",
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
	public void updateData(ProductInfoArrayList pialNew) {
		pial.addAll(pialNew);
		for (ProductInfo pi : pialNew) {
			pihm.put(pi.getId(), pi);
		}
		notifyDataSetChanged();
	}

    /**
	 * ViewHolder class specific to ProductListRecyclerAdapter.
	 */
	protected class ProductListViewHolder extends RecyclerView.ViewHolder
	{
		/**
		 * The "tvId" field contains the identifier for the product. It is provided
		 * within the is hidden ("gone") in the text view.
		 */
		private final TextView tvId;

		/** The product tvName. */
		private final TextView tvName;

		/** A short description of the product. */
		private final TextView tvShortDescription;

		/** Product image view. */
		private final ImageView ivProductImage;

		/** Object to load and display a product image. */
		private final ImageLoader imageLoader;

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
			tvName = (TextView) itemView.findViewById(R.id.name);
			tvShortDescription = (TextView) itemView.findViewById(R.id.short_description);
            ivProductImage = (ImageView) itemView.findViewById(R.id.pd_product_image);
            imageLoader = new ImageLoader(a);
			if (viewType == ViewType.ITEM) {
				itemView.setOnClickListener(onClickListenerRow);
			}
		}

		/**
		 * Binds column names to the header row.
		 */
		void bindHeader() {
            ivProductImage.setImageBitmap(ImageLoader.getBlankImageBitmap());
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
		@SuppressLint("SetTextI18n")
		void bindData(ProductInfo pi) {
			tvId.setText(String.format(Locale.US, "%d", pi.getId()));

            // Optionally display image url, for debugging purposes.
            String name = Settings.isAppDisplayUrl()
                    ? String.format("[%s]\n%s",
					  Support.truncImageString(pi.getImageUrl()), pi.getName())
                    : pi.getName();
            tvName.setText(name);

			tvShortDescription.setText(pi.getShortDescription());
			imageLoader.load(ivProductImage, pi.getImageUrl());
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
				Integer itemId = Integer.valueOf((String) tvId.getText());

				Intent intent = new Intent(a, ActivityProductInfo.class);
				intent.putExtra(Support.getKeyProductInfo(a), pihm.get(itemId));
				a.startActivity(intent);
			}
		}
	}
}

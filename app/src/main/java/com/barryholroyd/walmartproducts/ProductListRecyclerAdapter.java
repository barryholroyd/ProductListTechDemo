package com.barryholroyd.walmartproducts;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;

/**
 * Recycler adapter to display the list of products.
 *
 * @author Barry Holroyd
 */
public class ProductListRecyclerAdapter
	extends RecyclerView.Adapter<ProductListRecyclerAdapter.ProductListViewHolder>
{
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
	 * ViewHolder class specific to ProductListRecyclerAdapter.
	 */
	protected class ProductListViewHolder extends RecyclerView.ViewHolder
	{
		/**
		 * The "id" field contains the identifier for the product. It is provided
		 * within the is hidden ("gone") in the text view.
		 */
		private final TextView id;

		/** The product name. */
		private final TextView name;

		/** A short description of the product. */
		private final TextView  shortDescription;

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
			id = (TextView) itemView.findViewById(R.id.id);
			name = (TextView) itemView.findViewById(R.id.name);
			shortDescription = (TextView) itemView.findViewById(R.id.short_description);
			if (viewType == ViewType.ITEM) {
				itemView.setOnClickListener(onClickListenerRow);
			}
		}

		/**
		 * Binds column names to the header row.
		 */
		protected void bindHeader() {
			name.setText("Name");
			name.setGravity(Gravity.CENTER_HORIZONTAL);
			name.setTextSize(20);
			shortDescription.setGravity(Gravity.CENTER_HORIZONTAL);
			shortDescription.setText("Description");
			shortDescription.setTextSize(20);
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
			id.setText(pi.id);
			name.setText(pi.name);
			shortDescription.setText(pi.shortDescription);
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

				Activity a = Support.getActivity();
				Intent intent = new Intent(a, ActivityProductInfo.class);
				intent.putExtra(Support.getKeyProductInfo(), pihm.get(productId));
				a.startActivity(intent);
			}

		}
	}
}




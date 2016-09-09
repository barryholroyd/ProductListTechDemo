package com.barryholroyd.walmartproducts;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Recycler adapter to display the list of products.
 *
 * @author Barry Holroyd
 */
public class ProductListRecyclerAdapter
	extends RecyclerView.Adapter<ProductListRecyclerAdapter.ProductListViewHolder>
{
	/**
	 * Array of products. Each cell contains information about the specific product.
	 * This is filled in, in batches, from JSON pulled from the cloud.
	 */
	private ProductInfoArrayList pial = new ProductInfoArrayList();

	/**
	 * Create a ViewHolder to contain a View for each row, inflated from an XML layout file.
	 *
	 * @param vg the ViewGroup into which the new View will be added after
	 *              it is bound to an adapter position
	 * @param viewType the viewType of the new View.
	 *                    See RecyclerView.Adapter.getItemViewType().
	 * @return a new ViewHolder that holds a View of the given view type new ViewHolder.
	 */
	@Override
	public ProductListViewHolder onCreateViewHolder(ViewGroup vg, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(vg.getContext());
		View v = inflater.inflate(R.layout.product_row, vg, false);
		return new ProductListViewHolder(v);
	}

	/**
	 * Fill in the ViewHolder passed in with the data at "position" in the array.
	 *
	 * This is done by calling a method on the ViewHolder itself, so that that
	 * data can remain private to the ViewHolder.
	 *
	 * @param viewHolder ViewHolder which should be updated to represent the contents of
	 *                      the item at the given position in the data set.
     * @param position the position of the item within the adapter's data set.
	 */
	@Override
	public void onBindViewHolder(ProductListViewHolder viewHolder, int position) {
		if (position >= pial.size()) {
			throw new IndexOutOfBoundsException(
				String.format("ProductInfo has %d products; product %d requested.",
					pial.size(), position));
		}
		viewHolder.bindData(pial.get(position));
	}

	/**
	 * Return the number of items in the backing array.
	 *
	 * @return the number of items in the backing array.
	 */
	@Override
	public int getItemCount() {
		return pial.size();
	}

	/**
	 * Update the array of products and redisplay the product list.
	 *
	 * @param pialNew the array of new products to add to the data set.
	 */
	void updateData(ProductInfoArrayList pialNew) {
		pial.addAll(pialNew);
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
		/**
		 * The product name.
		 */
		private final TextView name;
		/**
		 * A short description of the product.
		 */
		private final TextView  shortDescription;

		/**
		 * A single instance of OnClickListener that can be used for all rows
		 * in the displayed product list. We could use a lambda expression instead
		 * of creating a
		 */
		private final OnClickListenerRow onClickListenerRow = new OnClickListenerRow();

		/**
		 * Constructor for the ViewHolder. Save references to the relevant fields
		 * within the View.
		 *
		 * @param itemView the View (row) to be managed by this ViewHolder.
		 */
		ProductListViewHolder(View itemView) {
			super(itemView);
			id = (TextView) itemView.findViewById(R.id.id);
			name = (TextView) itemView.findViewById(R.id.name);
			shortDescription = (TextView) itemView.findViewById(R.id.short_description);
			itemView.setOnClickListener(onClickListenerRow);
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
				TextView tvId = (TextView) v.findViewById(R.id.id);
				String id = (String) tvId.getText();
				Activity a = Support.getActivity();
				Intent intent = new Intent(a, ActivityProductInfo.class);
				intent.putExtra(Support.getKeyId(), id);
				a.startActivity(intent);
			}
		}
	}
}




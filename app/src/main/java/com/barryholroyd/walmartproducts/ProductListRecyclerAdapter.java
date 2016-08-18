package com.barryholroyd.walmartproducts;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Barry on 8/17/2016.
 */
public class ProductListRecyclerAdapter extends RecyclerView.Adapter<ProductListViewHolder>
{
	private final RecyclerView mRecyclerView;
	private final LinearLayoutManager llm;
	private ProductInfoArrayList pial = new ProductInfoArrayList();

	ProductListRecyclerAdapter(RecyclerView _mRecyclerView, LinearLayoutManager _llm) {
		mRecyclerView = _mRecyclerView;
		llm = _llm;
	}

	@Override
	public ProductListViewHolder onCreateViewHolder(ViewGroup vg, int position) {
		LayoutInflater inflater = LayoutInflater.from(vg.getContext());
		View v = inflater.inflate(R.layout.product_row, vg, false);
		return new ProductListViewHolder(v);
	}

	/**
	 * Fill in the ViewHolder passed in with the data at "position" in the array.
	 *
	 * This is done by calling a method on the ViewHolder itself, so that that
	 * data can remain private to the ViewHolder.
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
	 */
	@Override
	public int getItemCount() {
		return pial.size();
	}

	/**
	 * Update the data set.
	 */
	void updateData(ProductInfoArrayList pialNew) {
		pial.addAll(pialNew);
		notifyDataSetChanged();
	}
}

class ProductListViewHolder extends RecyclerView.ViewHolder
{
	private final TextView id;
	private final TextView name;
	private final TextView  shortDescription;
	private final OnClickRow onClickRow;

	ProductListViewHolder(View itemView) {
		super(itemView);
		id = (TextView) itemView.findViewById(R.id.id);
		name = (TextView) itemView.findViewById(R.id.name);
		shortDescription = (TextView) itemView.findViewById(R.id.short_description);
		onClickRow = new OnClickRow();
	}

	/**
	 * Binds actual data passed in by the adapter.
	 */
	void bindData(ProductInfo pi) {
		id.setText(pi.id);
		name.setText(pi.name);
		shortDescription.setText(pi.shortDescription);
	}
}

class OnClickRow implements View.OnClickListener
{
	@Override
	public void onClick(View v) {
		TextView tvId = (TextView) v.findViewById(R.id.id);
		String id = (String) tvId.getText();
		Activity a = Support.getActivity();
		Intent intent = new Intent(a, ActivityProductList.class);
		intent.putExtra(Support.getKeyId(), id);
		a.startActivity(intent);
	}
}

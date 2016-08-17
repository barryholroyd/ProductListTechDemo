package com.barryholroyd.walmartproducts;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class ActivityProductList extends AppCompatActivity
{
	private static RecyclerView mRecyclerView = null;
	static private Activity a = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		a = this;
		setContentView(R.layout.productlist);
		init();
	}

	private void init() {
		mRecyclerView = (RecyclerView) findViewById(R.id.list);
		LinearLayoutManager llm = new LinearLayoutManager(this);
		mRecyclerView.setLayoutManager(llm);
		mRecyclerView.setAdapter(new ProductListRecyclerAdapter(mRecyclerView, llm));
	}

	static Activity getActivity() {
		if (a == null)
			throw new IllegalStateException("getActivity() called before activity initialized.");
		return a;
	}

	/**
	 * TBD: delete when no longer needed.
	 *
	 * @param view
	 */
	public void displayProductInfo(View view) {
		startActivity(new Intent(this, ActivityProductInfo.class));
	}
}

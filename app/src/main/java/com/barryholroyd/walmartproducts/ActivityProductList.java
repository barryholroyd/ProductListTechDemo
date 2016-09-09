package com.barryholroyd.walmartproducts;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * List the Walmart products.
 */
public class ActivityProductList extends AppCompatActivity
{
	/**
	 * Logger tag for this application.
	 */
	static final public String LOGTAG = "WalmartProduct";
	/**
	 * RecyclerView used to display the list of products pulled from the cloud.
	 */
	static RecyclerView recyclerView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new Support(this);
		setContentView(R.layout.productlist);
		init();
		GetProducts.instance.getNextBatch();
	}

	private void init() {
		recyclerView = (RecyclerView) findViewById(R.id.list);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(new ProductListRecyclerAdapter());
	}

	/**
	 * Temporary callback for button clicks in the Product List.
	 * TBD: delete when no longer needed.
	 *
	 * @param view The button clicked on.
	 */
	public void buttonTmp(View view) {
		switch (view.getId()) {
			case R.id.button_productinfo:
				startActivity(new Intent(this, ActivityProductInfo.class));
				break;
			case R.id.button_productlist:
				GetProducts.instance.getNextBatch();
				break;
		}
	}
}

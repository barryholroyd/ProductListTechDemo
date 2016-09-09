package com.barryholroyd.walmartproducts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/*
 * Display the information about a specific product.
 */
public class ActivityProductInfo extends AppCompatActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new Support(this);
		setContentView(R.layout.productinfo);

		Intent intent = getIntent();
		String productId = intent.getStringExtra(Support.getKeyId());
	}

	/**
	 * Callback method to start the activity which displays the full list of products.
	 *
	 * @param view the button which was pressed to call this callback.
	 */
	public void displayProductList(View view) {
		startActivity(new Intent(this, ActivityProductList.class));
	}
}

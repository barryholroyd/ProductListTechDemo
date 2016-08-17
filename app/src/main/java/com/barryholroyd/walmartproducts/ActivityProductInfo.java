package com.barryholroyd.walmartproducts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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
	 * TBD: delete when no longer needed.
	 *
	 * @param view
	 */
	public void displayProductList(View view) {
		startActivity(new Intent(this, ActivityProductList.class));
	}
}

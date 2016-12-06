package com.barryholroyd.productsdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.barryholroyd.productsdemo.product_info.ImageLoader;
import com.barryholroyd.productsdemo.product_info.ProductInfo;
import com.barryholroyd.productsdemo.support.ActivityPrintStates;
import com.barryholroyd.productsdemo.support.Support;

/*
 * Display the information about a specific product.
 *
 * NTH: Product Info screen's table should have the same border coloring
 * as the Product List screen.
*/
public class ActivityProductInfo extends ActivityPrintStates
{
	ImageLoader imageLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.productinfo);

		Toolbar myToolbar =(Toolbar) findViewById(R.id.appbar);
		setSupportActionBar(myToolbar);

		imageLoader = new ImageLoader(this);

		Intent intent = getIntent();
		ProductInfo productInfo = intent.getParcelableExtra(Support.getKeyProductInfo(this));

		setFields(productInfo);
	}

	/**
	 * Create the standard options menu for the app bar.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_appbar, menu);
		return true;
	}

	private void setFields(ProductInfo pi) {
		TextView  tvName               = (TextView)  findViewById(R.id.name);
		TextView  tvPrice              = (TextView)  findViewById(R.id.price);
		TextView  tvRating             = (TextView)  findViewById(R.id.rating);
		TextView  tvReviewCount        = (TextView)  findViewById(R.id.review_count);
		TextView  tvInStock            = (TextView)  findViewById(R.id.in_stock);
		ImageView tvProductImage       = (ImageView) findViewById(R.id.product_image);
		TextView  tvProductDescription = (TextView)  findViewById(R.id.product_description);

		tvName.setText(pi.getName());
		tvPrice.setText("$"+Double.toString(pi.getPrice()));
		tvRating.setText(pi.getReviewRating());
		tvReviewCount.setText(Integer.toString(pi.getNumReviews()));
		tvInStock.setText(pi.getInStock());
		imageLoader.load(tvProductImage, pi.getImageUrl());
		tvProductDescription.setText(pi.getLongDescription());
	}

	/**
	 * Callback method to start the activity which displays the full list of products.
	 *
	 * @param view the button which was pressed to call this callback.
	 */
	public void displayProductList(View view) {
		finish();
	}
}

package com.barryholroyd.productsdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/*
 * Display the information about a specific product.
 */
public class ActivityProductInfo extends AppCompatActivity
{
	ImageLoader imageLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.productinfo);

		imageLoader = new ImageLoader(this);

		Intent intent = getIntent();
		ProductInfo productInfo = intent.getParcelableExtra(Support.getKeyProductInfo(this));

		setFields(productInfo);
	}

	private void setFields(ProductInfo pi) {
		TextView  tvName               = (TextView)  findViewById(R.id.name);
		TextView  tvPrice              = (TextView)  findViewById(R.id.price);
		TextView  tvRating             = (TextView)  findViewById(R.id.rating);
		TextView  tvReviewCount        = (TextView)  findViewById(R.id.review_count);
		TextView  tvInStock            = (TextView)  findViewById(R.id.in_stock);
		ImageView tvProductImage       = (ImageView) findViewById(R.id.product_image);
		TextView  tvProductDescription = (TextView)  findViewById(R.id.product_description);

		tvName.setText(pi.name);
		tvPrice.setText(pi.price);
		tvRating.setText(Double.toString(pi.reviewRating));
		tvReviewCount.setText(Integer.toString(pi.reviewCount));
		tvInStock.setText(Boolean.toString(pi.inStock));
		imageLoader.load(tvProductImage, pi.imageUrl);
		tvProductDescription.setText(pi.longDescription);
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

package com.barryholroyd.prodlisthpdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.barryholroyd.prodlisthpdemo.product_info.ImageLoader;
import com.barryholroyd.prodlisthpdemo.product_info.ProductInfo;
import com.barryholroyd.prodlisthpdemo.support.ActivityPrintStates;
import com.barryholroyd.prodlisthpdemo.support.Support;

import java.util.Locale;

/**
 * Display information about a specific product.
 */
public class ActivityProductInfo extends ActivityPrintStates
{
	/** Object to load and display a product image. */
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

	/** Create the standard options menu for the app bar. */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_appbar, menu);
		return true;
	}

	/**
	 * Display the fields for the Product Info page.
	 *
	 * @param pi object containing all the product-specific information.
	 */
	private void setFields(ProductInfo pi) {
		TextView  tvName               = (TextView)  findViewById(R.id.name);
		TextView  tvPrice              = (TextView)  findViewById(R.id.price);
		TextView  tvReviewRating       = (TextView)  findViewById(R.id.review_rating);
		TextView  tvReviewCount        = (TextView)  findViewById(R.id.review_count);
		TextView  tvInStock            = (TextView)  findViewById(R.id.in_stock);
		ImageView tvProductImage       = (ImageView) findViewById(R.id.product_image);
		TextView  tvProductDescription = (TextView)  findViewById(R.id.product_description);

		tvName.setText(pi.getName());
		tvPrice.setText(String.format(Locale.US, "$%.2f", pi.getPrice()));
        setReviewRating(tvReviewRating, pi.getReviewRating(), pi.getName());
		tvReviewCount.setText(String.format(Locale.US, "%d", pi.getNumReviews()));
		tvInStock.setText(pi.getInStock());
		imageLoader.load(tvProductImage, pi.getImageUrl());
		tvProductDescription.setText(pi.getLongDescription());
	}

    private void setReviewRating(TextView tvReviewRating, String reviewRating, String name) {
        String s = (reviewRating == null)
                ? "--"
                : String.format(Locale.US, "%.2f", Float.valueOf(reviewRating));
        tvReviewRating.setText(s);
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

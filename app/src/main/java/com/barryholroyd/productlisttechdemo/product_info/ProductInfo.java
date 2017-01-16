package com.barryholroyd.productlisttechdemo.product_info;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Product information for a specific product.
 *
 * @author Barry Holroyd
 */
public class ProductInfo implements Parcelable
{
	private int		id;
	private String  name;
	private String  shortDescription;
	private String  longDescription;
	private double  price;
	private String	imageUrl;
	private String  reviewRating;
	private int		numReviews;
	private String	inStock;

	/*
	 * Setters.
	 */
	public void setId(int id) {
		this.id = id;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public void setInStock(String inStock) {
		this.inStock = inStock;
	}

	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public void setNumReviews(int numReviews) {
		this.numReviews = numReviews;
	}

	public void setReviewRating(String reviewRating) {
		this.reviewRating = reviewRating;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	/*
	 * Getters.
	 */
	public int getId() {
		return id;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getInStock() {
		return inStock;
	}

	public String getLongDescription() {
		return longDescription;
	}

	public String getName() {
		return name;
	}

	public double getPrice() {
		return price;
	}

	public int getNumReviews() {
		return numReviews;
	}

	public String getReviewRating() {
		return reviewRating;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	ProductInfo() {}

	/** Parcelable constructor. */
	private ProductInfo(Parcel in) {
		id = in.readInt();
		name = in.readString();
		shortDescription = in.readString();
		longDescription = in.readString();
		price = in.readDouble();
		imageUrl = in.readString();
		reviewRating = in.readString();
		numReviews = in.readInt();
		inStock = in.readString();
	}

	/** Parcelable describeContents(). */
	public int describeContents() {
		return 0;
	}

	/** Parcelable writeToParcel(). */
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(id);
		out.writeString(name);
		out.writeString(shortDescription);
		out.writeString(longDescription);
		out.writeDouble(price);
		out.writeString(imageUrl);
		out.writeString(reviewRating);
		out.writeInt(numReviews);
		out.writeString(inStock);
	}

	/**
	 * Standard (required) inner class which can be called to create a new instance
	 * of ProductInfo from a parcel, as well as an empty array of ProductInfo (which
	 * can then be filled in with ProductInfo instances).
	 */
	public static final Parcelable.Creator<ProductInfo> CREATOR =
		new Parcelable.Creator<ProductInfo>() {
			public ProductInfo createFromParcel(Parcel in) {
				return new ProductInfo(in);
			}
			public ProductInfo[] newArray(int size) {
				return new ProductInfo[size];
			}
		};
}

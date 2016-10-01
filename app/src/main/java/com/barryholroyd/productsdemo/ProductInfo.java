package com.barryholroyd.productsdemo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Product information for a specific product.
 *
 * @author Barry Holroyd
 */
public class ProductInfo implements Parcelable
{
	String  id;
	String  name;
	String  shortDescription;
	String  longDescription;
	String  price;
	String  imageUrl;
	double  reviewRating;
	int     reviewCount;
	boolean inStock;

	ProductInfo() {}

	// TBD: display image.

	private ProductInfo(Parcel in) {
		id = in.readString();
		name = in.readString();
		shortDescription = in.readString();
		longDescription = in.readString();
		price = in.readString();
		imageUrl = in.readString();
		reviewRating = in.readDouble();
		reviewCount = in.readInt();
		inStock = (in.readInt() == 1);
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeString(id);
		out.writeString(name);
		out.writeString(shortDescription);
		out.writeString(longDescription);
		out.writeString(price);
		out.writeString(imageUrl);
		out.writeDouble(reviewRating);
		out.writeInt(reviewCount);
		out.writeInt(inStock ? 1 : 0);
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

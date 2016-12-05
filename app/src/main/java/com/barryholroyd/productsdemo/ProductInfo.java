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
	int		id;
	String  name;
	String  shortDescription;
	String  longDescription;
	double  price;
	String	imageUrl;
	String  reviewRating;
	int     reviewCount;
	String	inStock;

	ProductInfo() {}

	private ProductInfo(Parcel in) {
		id = in.readInt();
		name = in.readString();
		shortDescription = in.readString();
		longDescription = in.readString();
		price = in.readDouble();
		imageUrl = in.readString();
		reviewRating = in.readString();
		reviewCount = in.readInt();
		inStock = in.readString();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(id);
		out.writeString(name);
		out.writeString(shortDescription);
		out.writeString(longDescription);
		out.writeDouble(price);
		out.writeString(imageUrl);
		out.writeString(reviewRating);
		out.writeInt(reviewCount);
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

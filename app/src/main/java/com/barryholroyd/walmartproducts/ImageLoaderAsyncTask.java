package com.barryholroyd.walmartproducts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * TBD:
 */

public class ImageLoaderAsyncTask
{
    void load(Context ctx, final ImageView iv, final String imageUrlStr) {
        AsyncTaskNetworkLoader atnl = new AsyncTaskNetworkLoader(ctx, iv);
        atnl.execute(imageUrlStr);
    }
}

class AsyncTaskNetworkLoader extends AsyncTask<String, Void, Bitmap> {
    Context ctx;
    ImageView iv;

    AsyncTaskNetworkLoader(Context _ctx, ImageView _iv) {
        ctx = _ctx;
        iv  = _iv;
    }

    @Override
    protected Bitmap doInBackground(String ... params) {
        return getBitmap(ctx, iv, params[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        iv.setImageBitmap(bitmap);
    }

    static private Bitmap getBitmap(Context ctx, ImageView iv, String urlStr) {
        return getImageFromNetwork(ctx, urlStr);
    }

    static private Bitmap getImageFromNetwork(Context ctx, String urlStr) {
        InputStream is = NetworkSupport.getInputStreamFromUrl(ctx, urlStr);
        return BitmapFactory.decodeStream(is);
    }
}

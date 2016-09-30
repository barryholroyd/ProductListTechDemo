// DEL: delete this file?

//package com.barryholroyd.walmartproducts;
//
//import android.app.Activity;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.AsyncTask;
//import android.widget.ImageView;
//
//import java.io.InputStream;
//
///**
// * TBD:
// */
//
//public class ImageLoaderAsyncTask
//{
//    void load(Activity a, final ImageView iv, final String imageUrlStr) {
//        AsyncTaskNetworkLoader atnl = new AsyncTaskNetworkLoader(a, iv);
//        atnl.execute(imageUrlStr);
//    }
//}
//
//class AsyncTaskNetworkLoader extends AsyncTask<String, Void, Bitmap> {
//    Activity a;
//    ImageView iv;
//
//    AsyncTaskNetworkLoader(Activity _a, ImageView _iv) {
//        a = _a;
//        iv  = _iv;
//    }
//
//    @Override
//    protected Bitmap doInBackground(String ... params) {
//        return NetworkSupport.getImageFromNetwork(a, params[0], 100, 100);
//    }
//
//    @Override
//    protected void onPostExecute(Bitmap bitmap) {
//        if (bitmap != null) {
//            iv.setImageBitmap(bitmap);
//        }
//    }
//}

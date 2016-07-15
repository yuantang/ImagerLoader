package com.coder.tom.imagerloader.imageloader;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by tangyuan on 2016/7/11.
 */
public class LoaderResult {
    public ImageView imageView;
    public  String uri;
    public Bitmap bitmap;

    public LoaderResult(ImageView imageView, String uri, Bitmap bitmap) {
        this.imageView = imageView;
        this.uri = uri;
        this.bitmap = bitmap;
    }
}

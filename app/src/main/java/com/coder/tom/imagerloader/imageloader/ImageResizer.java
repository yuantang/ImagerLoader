package com.coder.tom.imagerloader.imageloader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.InputStream;

/**
 * Created by tangyuan on 2016/7/11.
 */
public class ImageResizer {
    private static final String TAG ="ImageResizer" ;

    public ImageResizer() {
    }
    public Bitmap decodeSampleBitmapFromResource(Resources res,int resId,int reqWidth,int reqHeight){
        final BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeResource(res,resId,options);
        options.inSampleSize=calculatInSampleSize(options,reqWidth,reqHeight);
        options.inJustDecodeBounds=false;
        return  BitmapFactory.decodeResource(res,resId,options);
    }
    public Bitmap decodeSampleBitmapFromStream(InputStream inputStream, int reqWidth, int reqHeight){
        final BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeStream(inputStream);
        options.inSampleSize=calculatInSampleSize(options,reqWidth,reqHeight);
        options.inJustDecodeBounds=false;
        return  BitmapFactory.decodeStream(inputStream);
    }
    public Bitmap decodeSampleBitmapFromFileDescriptor(FileDescriptor  fd, int reqWidth, int reqHeight){
        final BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFileDescriptor(fd,null,options);
        options.inSampleSize=calculatInSampleSize(options,reqWidth,reqHeight);
        options.inJustDecodeBounds=false;
        return  BitmapFactory.decodeFileDescriptor(fd,null,options);
    }

    private int calculatInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        if (reqWidth==0||reqHeight==0){
            return 1;
        }
        final int width=options.outWidth;
        final int height=options.outHeight;
        int inSimpleSize=1;
        if (height>reqHeight||width>reqWidth){
            final int halfHeight=height/2;
            final int halfWidht=width/2;
            while ((halfHeight/inSimpleSize)>=reqHeight
                    &&(halfWidht/inSimpleSize)>=reqWidth){
                inSimpleSize*=2;
            }
        }
        Log.d(TAG,"inSimpleSize:"+inSimpleSize);
        return inSimpleSize;
    }
}

package com.coder.tom.imagerloader;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.coder.tom.imagerloader.imageloader.ImagerLoader;

/**
 * Created by tangyuan on 2016/7/14.
 */
public class ImageAdapter extends BaseAdapter {
    String TAG="ImageAdapter";
    private Context mContext;
    private String[] urls;
    private boolean isGridViewIdle;
    private ImagerLoader mImagerLoader;
    private int reqWidth;
    private  int reqHeight;
    public ImageAdapter(Context context, String[] urls,boolean isGridViewIdle,ImagerLoader mImagerLoader,int reqWidth,int reqHeight) {
        this.mContext=context;
        this.urls=urls;
        this.isGridViewIdle=isGridViewIdle;
        this.mImagerLoader=mImagerLoader;
        this.reqWidth=reqWidth;
        this.reqHeight=reqHeight;
    }
    @Override

    public int getCount() {
        return urls.length;
    }

    @Override
    public Object getItem(int i) {
        return urls[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView==null){
            viewHolder=new ViewHolder();
            convertView= LayoutInflater.from(mContext).inflate(R.layout.layout_item,parent,false);
            viewHolder.imageView= (ImageView) convertView.findViewById(R.id.item_img);
            convertView.setTag(viewHolder);
        }else {
            viewHolder= (ViewHolder) convertView.getTag();
        }
        String url= urls[i];
        Log.i(TAG,"URL:"+url);
        String tag= (String) viewHolder.imageView.getTag();

        if (!url.equals(tag)){
            viewHolder.imageView.setImageResource(R.mipmap.ic_launcher);
        }
        if (isGridViewIdle){
            viewHolder.imageView.setTag(url);
            mImagerLoader.bindBitmap(url,viewHolder.imageView,reqWidth,reqHeight);
            Log.i(TAG,"reqWidth:"+reqWidth);
        }
        return convertView;
    }

    class  ViewHolder{
        ImageView imageView;
    }
}

package com.coder.tom.imagerloader;

import android.content.Context;

/**
 * Created by tangyuan on 2016/7/15.
 */
public class DisplayUtil {
    public static int dip2px(Context context, float dipValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5f);
    }
    public static int px2dip(Context context, float pxValue){
        final float scale = context.getResources().getDisplayMetrics().density;
         return (int)(pxValue / scale + 0.5f);
    }
}

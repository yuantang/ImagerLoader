package com.coder.tom.imagerloader.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.coder.tom.imagerloader.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tangyuan on 2016/7/11.
 */
public class ImagerLoader {
    private  static  final String TAG="ImagerLoader";

    //handler 接收消息
    private  static  final  int MESSAGE_POST_RESULT=1;
    //CUP核数
    private  static final int CPU_COUNT=Runtime.getRuntime().availableProcessors();
    //线程核心数
    private static  final int CORE_POOL_SZIE=CPU_COUNT+1;
    //最大线程数
    private static final int MAXIMUM_POOL_SIZE=CPU_COUNT*2+1;
    //线程存活时间
    private static final long KEEP_ALIVE=10L;

    private static final int TAG_KEY_URI= R.mipmap.ic_launcher;
    //磁盘缓存大小50m
    private static final long DISK_CACHE_SIZE=1024*10224*50;
    //IO缓存8kb
    private static final int I0_BUFFER_SIZE=1024*8;

    private static final int DISK_CACHE_INDEX=0;

    private boolean mIsDiskLruCacheCreate=false;


    private static final ThreadFactory sThreadFactory=new ThreadFactory() {
        private final AtomicInteger mCount=new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable,"ImagerLoader#"+mCount.getAndIncrement());
        }
    };
    //线程池
    public static final Executor THREAD_POOL_EXECUTOR=new ThreadPoolExecutor(
            CORE_POOL_SZIE,
            MAXIMUM_POOL_SIZE,
            KEEP_ALIVE,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            sThreadFactory);

    //主线程进行图片加载处理
    private Handler mMainHandler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            LoaderResult result= (LoaderResult) msg.obj;
            ImageView imageView=result.imageView;
            String uri= (String) imageView.getTag(TAG_KEY_URI);
            Bitmap bitmap=result.bitmap;
            if (uri.equals(result.uri)){
                imageView.setImageBitmap(bitmap);
            }else {
                Log.i(TAG,"set imge bitmap,but uri has changed,ignored!");
            }
        }
    };
    private Context mContext;
    private ImageResizer mImageResizer=new ImageResizer();
    private LruCache<String,Bitmap> mMemoryCache;
    public DiskLruCache mDiskLruCache;

    public ImagerLoader( Context context) {
        this.mContext = context.getApplicationContext();
        int maxMemory= (int) (Runtime.getRuntime().maxMemory()/1024);
        int cacheSize=maxMemory/8;
        mMemoryCache=new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes()*bitmap.getHeight()/1024;
            }
            @Override
            protected void entryRemoved(boolean evicted, String key,
                                        Bitmap oldValue, Bitmap newValue) {
                Log.i(TAG, "hard cache is full , push to soft cache");
            }
        };
        File diskCacheDir=getDiskCacheDir(mContext,"bitmap");
        if (!diskCacheDir.exists()){
            diskCacheDir.mkdirs();
        }
        if (getUsableSpace(diskCacheDir)>DISK_CACHE_SIZE){
            try {
                mDiskLruCache=DiskLruCache.open(diskCacheDir,1,1,DISK_CACHE_SIZE);
                mIsDiskLruCacheCreate=true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private long getUsableSpace(File diskCacheDir) {
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.GINGERBREAD){
            return diskCacheDir.getUsableSpace();
        }
        final StatFs statfs=new StatFs(diskCacheDir.getPath());
        return statfs.getBlockSize()*statfs.getAvailableBlocks();
    }

    private File getDiskCacheDir(Context mContext, String uniqueName) {
        boolean externalStorageAvailable= Environment.getExternalStorageDirectory().equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        if (externalStorageAvailable){
            cachePath=mContext.getExternalCacheDir().getPath();
        }else {
            cachePath=mContext.getCacheDir().getPath();
        }
        return new File(cachePath+File.separator+uniqueName);
    }

    public  static ImagerLoader build(Context context){
        return new ImagerLoader(context);
    }

    //添加到内存缓存
    private void addBitmapToMemCache(String key,Bitmap bitmap){
        if (getBitmapFromMemCache(key)==null){
            mMemoryCache.put(key,bitmap);
        }
    }
    //取内存缓存
    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    /***
     * imageloader再加图片，没有指定宽高
     * @param uri
     * @param imageView
     */
    public void bindBitmap(final String uri,final ImageView imageView){
        bindBitmap(uri,imageView,0,0);
    }

    /**
     * imageloader再加图片，指定宽高
     * @param uri
     * @param imageView
     * @param reqWidth
     * @param reqHeight
     */
    public void bindBitmap(final String uri, final ImageView imageView, final int reqWidth, final int reqHeight) {
        imageView.setTag(TAG_KEY_URI,uri);
        //缓存取照片
        Bitmap bitmap=loadBitmapFromMemCache(uri);
        if (bitmap!=null){
            Log.i(TAG,"loadBitmapFromMemCache,uri:"+uri);
            imageView.setImageBitmap(bitmap);
            return;
        }
        Runnable loadBitmapTask=new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap= null;
                try {
                    bitmap = loadbitmap(uri,reqWidth,reqHeight);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (bitmap!=null){
                    LoaderResult result=new LoaderResult(imageView, uri,bitmap);
                    mMainHandler.obtainMessage(MESSAGE_POST_RESULT,result).sendToTarget();
                }
            }
        };
        THREAD_POOL_EXECUTOR.execute(loadBitmapTask);
    }

    private Bitmap loadbitmap(String uri, int reqWidth, int reqHeight) throws IOException {
        Bitmap bitmap=loadBitmapFromMemCache(uri);
        if (bitmap!=null){
            Log.i(TAG,"loadBitmapFromMemCache,uri:"+uri);
            return bitmap;
        }
        bitmap=loadBitmapFromDiskCache(uri,reqWidth,reqHeight);
        if (bitmap!=null){
            Log.i(TAG,"loadBitmapFromDiskCache,uri:"+uri);
            return bitmap;
        }
        bitmap=loadBitmapFromHttp(uri,reqWidth,reqHeight);
        Log.i(TAG,"loadBitmapFromHttp,uri:"+uri);
        if (bitmap==null&&!mIsDiskLruCacheCreate){
            Log.i(TAG,"encounter error,DiskLruCache is not created.");
            bitmap=downLoadBitmapFromUrl(uri,reqWidth,reqWidth);
            Log.i(TAG,"downLoadBitmapFromUrl,uri:"+uri);
        }
        return bitmap;
    }

    /**
     * 从缓存获取图片
     * @param url
     * @return
     */
    private Bitmap loadBitmapFromMemCache(String url) {
        final String key=hashKeyFromUrl(url);
        Bitmap bitmap=getBitmapFromMemCache(key);
        return bitmap;
    }


    private String hashKeyFromUrl(String uri) {
        String cacheKey = null;
        try {
            final MessageDigest mDisget= MessageDigest.getInstance("MD5");
            mDisget.update(uri.getBytes());
            cacheKey=byteToHexString(mDisget.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return cacheKey;
    }

    private String byteToHexString(byte[] digest) {
        StringBuilder sb=new StringBuilder();
        for (int i = 0; i <digest.length; i++) {
            String hex=Integer.toHexString(0xFF&digest[i]);
            if (hex.length()==1){
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 通过Url获取图片
     * @param urlString
     * @return
     */
    private Bitmap downLoadBitmapFromUrl(String urlString,int reqWidth, int reqHeight) {
        Bitmap bitmap=null;
        HttpURLConnection urlConnection=null;
        BufferedInputStream in=null;
        try {
            final URL url=new URL(urlString);
            urlConnection= (HttpURLConnection) url.openConnection();
            in=new BufferedInputStream(urlConnection.getInputStream(),I0_BUFFER_SIZE);
//            bitmap= BitmapFactory.decodeStream(in);
            bitmap=mImageResizer.decodeSampleBitmapFromStream(in,reqWidth,reqHeight);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG,"Error in downloadBitmap.");
        }finally {
            if (urlConnection!=null){
                urlConnection.disconnect();
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * 网络获取图片
     * @param url
     * @param reqWidth
     * @param reqHeight
     * @return
     * @throws IOException
     */
    private Bitmap loadBitmapFromHttp(String url, int reqWidth, int reqHeight) throws IOException {
        if (Looper.myLooper()==Looper.getMainLooper()){
            throw new RuntimeException("can not visit network from UI thread.");
        }
        if (mDiskLruCache==null){
            return null;
        }
        String key=hashKeyFromUrl(url);
        DiskLruCache.Editor editor=mDiskLruCache.edit(key);
        if (editor!=null){
            OutputStream  outputStream=editor.newOutputStream(DISK_CACHE_INDEX);
            if (downLoadUrlToStream(url,outputStream)){
                editor.commit();
            }else {
                editor.abort();
            }
            mDiskLruCache.flush();
        }
        return loadBitmapFromDiskCache(url,reqWidth,reqHeight);
    }

    /**
     * 获取图片的输入流
     * @param urlString
     * @param outputStream
     * @return
     */
    private boolean downLoadUrlToStream(String urlString, OutputStream outputStream)  {
        HttpURLConnection urlConnection=null;
        BufferedOutputStream out=null;
        BufferedInputStream in=null;
        try {
            URL  url = new URL(urlString);
            urlConnection= (HttpURLConnection) url.openConnection();
            in=new BufferedInputStream(urlConnection.getInputStream(),I0_BUFFER_SIZE);
            out=new BufferedOutputStream(outputStream,I0_BUFFER_SIZE);
            int b;
            while ((b=in.read())!=-1){
                out.write(b);
            }
            return true;
        }  catch (IOException e) {
            Log.e(TAG,"downloadBitmap failed."+e);
        }finally {
            if (urlConnection!=null){
                urlConnection.disconnect();
            }
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                Log.e(TAG,"downloadBitmap failed close io."+e);
            }
        }
        return false;
    }



    /**
     * 从磁盘缓存区图片
     * @param url
     * @param reqWidth
     * @param reqHeight
     * @return
     * @throws IOException
     */
    private Bitmap loadBitmapFromDiskCache(String url, int reqWidth, int reqHeight) throws IOException {
        if (Looper.myLooper()==Looper.getMainLooper()){
            throw new RuntimeException("can not visit network from UI thread.");
        }
        if (mDiskLruCache==null){
            return null;
        }
        Bitmap bitmap=null;
        String key=hashKeyFromUrl(url);
        DiskLruCache.Snapshot snapshot=mDiskLruCache.get(key);
        if (snapshot!=null){
            FileInputStream fileInputStream= (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor fileDescriptor=fileInputStream.getFD();
            bitmap=mImageResizer.decodeSampleBitmapFromFileDescriptor(fileDescriptor,reqWidth,reqHeight);
            if (bitmap!=null){
                addBitmapToMemCache(key,bitmap);
            }
        }
        return  bitmap;
    }



}

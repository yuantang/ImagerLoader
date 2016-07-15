package com.coder.tom.imagerloader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.AbsListView;
import android.widget.GridView;

import com.coder.tom.imagerloader.imageloader.ImagerLoader;

public class MainActivity extends AppCompatActivity {
    private GridView gridView;
    private ImageAdapter adapter;
    private boolean isGridViewIdle=true;
    private ImagerLoader mImagerLoader;
    private String[] urls={
            "http://img15.3lian.com/2015/f2/50/d/70.jpg",
            "http://img5.imgtn.bdimg.com/it/u=2074666895,1663510338&fm=21&gp=0.jpg",
            "http://img4.imgtn.bdimg.com/it/u=2348827052,3777058386&fm=21&gp=0.jpg",
            "http://www.bz55.com/uploads/allimg/140327/137-14032G45448.jpg",
            "http://d.3987.com/meinv_140911/002.jpg",
            "http://pic.58pic.com/58pic/13/07/10/88P58PICvhf_1024.jpg",
            "http://img15.3lian.com/2015/f2/50/d/70.jpg",
            "http://img5.imgtn.bdimg.com/it/u=2074666895,1663510338&fm=21&gp=0.jpg",
            "http://img4.imgtn.bdimg.com/it/u=2348827052,3777058386&fm=21&gp=0.jpg",
            "http://www.bz55.com/uploads/allimg/140327/137-14032G45448.jpg",
            "http://d.3987.com/meinv_140911/002.jpg",
            "http://pic.58pic.com/58pic/13/07/10/88P58PICvhf_1024.jpg",
            "http://img15.3lian.com/2015/f2/50/d/70.jpg",
            "http://img5.imgtn.bdimg.com/it/u=2074666895,1663510338&fm=21&gp=0.jpg",
            "http://img4.imgtn.bdimg.com/it/u=2348827052,3777058386&fm=21&gp=0.jpg",
            "http://www.bz55.com/uploads/allimg/140327/137-14032G45448.jpg",
            "http://d.3987.com/meinv_140911/002.jpg",
            "http://pic.58pic.com/58pic/13/07/10/88P58PICvhf_1024.jpg",
            "http://d.3987.com/meinv_140911/002.jpg",
            "http://pic.58pic.com/58pic/13/07/10/88P58PICvhf_1024.jpg",
            "http://img15.3lian.com/2015/f2/50/d/70.jpg",
            "http://img5.imgtn.bdimg.com/it/u=2074666895,1663510338&fm=21&gp=0.jpg",
            "http://img4.imgtn.bdimg.com/it/u=2348827052,3777058386&fm=21&gp=0.jpg",
            "http://www.bz55.com/uploads/allimg/140327/137-14032G45448.jpg",
            "http://d.3987.com/meinv_140911/002.jpg",
            "http://pic.58pic.com/58pic/13/07/10/88P58PICvhf_1024.jpg",
            "http://img15.3lian.com/2015/f2/50/d/70.jpg",
            "http://img5.imgtn.bdimg.com/it/u=2074666895,1663510338&fm=21&gp=0.jpg",
            "http://img4.imgtn.bdimg.com/it/u=2348827052,3777058386&fm=21&gp=0.jpg",
            "http://www.bz55.com/uploads/allimg/140327/137-14032G45448.jpg",
            "http://d.3987.com/meinv_140911/002.jpg",
            "http://pic.58pic.com/58pic/13/07/10/88P58PICvhf_1024.jpg",
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridView= (GridView) findViewById(R.id.gridview);
        mImagerLoader=ImagerLoader.build(this);
        adapter=new ImageAdapter(this,urls,isGridViewIdle,mImagerLoader,100,100);
        gridView.setAdapter(adapter);
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                //当Gridview 没有滑动时加载图片
                if (i== AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
                    isGridViewIdle=true;
                    adapter.notifyDataSetChanged();
                }else {
                    isGridViewIdle=false;
                }
            }
            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });
    }
}

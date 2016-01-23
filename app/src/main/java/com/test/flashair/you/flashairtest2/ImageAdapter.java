package com.test.flashair.you.flashairtest2;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by you on 2016/01/15.
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;

    private ArrayList<Bitmap> mBitmapList;

    public ImageAdapter(Context c) {
        mContext = c;
        mBitmapList = new ArrayList<>();
    }

    public int getCount() {
        return mBitmapList.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public void addBitmap(Bitmap bm) {
        mBitmapList.add(bm);

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setImageBitmap(mBitmapList.get(position));
        return imageView;
    }
}

package com.test.flashair.you.flashairtest2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by you on 2016/03/13.
 */
public class ThumbnailListAdapter extends SimpleExpandableListAdapter {
    private Context mContext;

    public ThumbnailListAdapter(
            Context context,
            List<? extends Map<String, ?>> groupData, int groupLayout,
            String[] groupFrom, int[] groupTo,
            List<? extends List<? extends Map<String, ?>>> childData,
            int childLayout, String[] childFrom, int[] childTo
    ){
        super(context, groupData, groupLayout, groupFrom, groupTo, childData, childLayout, childFrom, childTo);
        mContext = context;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        TextView view = (TextView)super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
        HashMap<String, String> s = (HashMap<String, String>)getChild(groupPosition, childPosition);
        File file = new File(mContext.getCacheDir(), s.get(ExpandableDateViewActivity.KEY2));
        try {
            InputStream is = new FileInputStream(file);
            Bitmap thumbnail = BitmapFactory.decodeStream(is);
            Drawable drawable = new BitmapDrawable(mContext.getResources(), thumbnail);
            drawable.setBounds(0,0,thumbnail.getWidth(),thumbnail.getHeight());
            view.setCompoundDrawables(drawable, null, null, null);
        }catch (IOException e){
            Log.e("ERR", e.toString());
        }
        return view;
    }
}

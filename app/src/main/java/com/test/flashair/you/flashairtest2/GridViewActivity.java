package com.test.flashair.you.flashairtest2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by you on 2016/01/15.
 */
public class GridViewActivity extends Activity {
    String flashAirName;
    String directoryName;

    ImageAdapter mImageAdapter;
    TreeMap<String, FileItem> mImageItems;
    ArrayList<String> mFileNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);
        final GridView gridView = (GridView) findViewById(R.id.gridview);

        mImageAdapter = new ImageAdapter(this);
        mFileNames = new ArrayList<>();

        Bundle extrasData = getIntent().getExtras();
        flashAirName = extrasData.getString("FlashAirName");
        directoryName = extrasData.getString("DirectoryName");
        Serializable s = extrasData.getSerializable("ImageItems");

        HashMap<String, FileItem> hashMap = (HashMap<String, FileItem>) (s);
        mImageItems = FileItem.createMap();
        for (String filename : hashMap.keySet()) {
            if (FileItem.isJpeg(filename)) {
                mImageItems.put(filename, hashMap.get(filename));
            }
        }

        setThumbnails(gridView);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String filename = mFileNames.get(position);
                if (FileItem.isJpeg(filename)) {
                    Intent viewImageIntent = new Intent(getApplicationContext(), ImageViewActivity.class);
                    viewImageIntent.putExtra("FlashAirName", flashAirName);
                    viewImageIntent.putExtra("DirectoryName", directoryName);
                    viewImageIntent.putExtra("ImageItem", mImageItems.get(filename));
                    GridViewActivity.this.startActivity(viewImageIntent);
                }
            }
        });
    }

    private void setThumbnails(GridView gridView) {
        File cachePath = getCacheDir();
        int maxWidth = 0;
        for (FileItem item : mImageItems.values()) {
            String filename = item.filename;
            File file = new File(cachePath, filename);
            try {
                InputStream is = new FileInputStream(file);
                Bitmap thumbnail = BitmapFactory.decodeStream(is);
                double thumbnailScale = 2.3;
                thumbnail = Bitmap.createScaledBitmap(thumbnail, (int)(thumbnail.getWidth() * thumbnailScale), (int)(thumbnail.getHeight() * thumbnailScale), false);
                if (thumbnail != null) {
                    maxWidth = Math.max(thumbnail.getWidth(), maxWidth);
                    mImageAdapter.addBitmap(thumbnail);
                    mFileNames.add(filename);
                }
            } catch (IOException e) {
                Log.i("IOException", e.toString());
            }
        }

        Display d = getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);
        int columns = displayMetrics.widthPixels / ((maxWidth) + gridView.getVerticalSpacing());
        gridView.setNumColumns(columns);
        gridView.setAdapter(mImageAdapter);
    }
}

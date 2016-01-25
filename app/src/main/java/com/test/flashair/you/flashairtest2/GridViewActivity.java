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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by you on 2016/01/15.
 */
public class GridViewActivity extends Activity {
    ImageAdapter imageAdapter;
    String flashAirName;
    String directoryName;
    TreeMap<String, FileItem> imageItems;
    ArrayList<String> fileNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);
        final GridView gridView = (GridView) findViewById(R.id.gridview);

        imageAdapter = new ImageAdapter(this);
        fileNames = new ArrayList<>();

        Bundle extrasData = getIntent().getExtras();
        String[] filenames = extrasData.getStringArray("filenames");
        flashAirName = extrasData.getString("FlashAirName");
        directoryName = extrasData.getString("DirectoryName");
        Serializable s = extrasData.getSerializable("ImageItems");
        HashMap<String, FileItem> hashMap = (HashMap<String, FileItem>) (s);
        imageItems = FileItem.createMap();
        for (String filename : hashMap.keySet()) {
            imageItems.put(filename, hashMap.get(filename));
        }

        setThumbnails(gridView, filenames);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String filename = fileNames.get(position);

                Log.i("GridView item clicked.", "position = " + position + ". id = " + id + ".filename = " + filename);
                if (FileItem.isJpeg(filename)) {
                    Intent viewImageIntent = new Intent(getApplicationContext(), ImageViewActivity.class);
                    viewImageIntent.putExtra("flashAirName", flashAirName);
                    viewImageIntent.putExtra("downloadFile", filename);
                    viewImageIntent.putExtra("directoryName", directoryName);
                    viewImageIntent.putExtra("ImageItem", imageItems.get(filename));
                    GridViewActivity.this.startActivity(viewImageIntent);
                }
            }
        });
    }

    private void setThumbnails(GridView gridView, String[] filenames) {
        File cachePath = getCacheDir();
        int maxWidth = 0;
        for (FileItem item : imageItems.values()) {
            String filename = item.filename;
            File file = new File(cachePath, filename);
            try {
                InputStream is = new FileInputStream(file);
                Bitmap thumbnail = BitmapFactory.decodeStream(is);
                thumbnail = Bitmap.createScaledBitmap(thumbnail, thumbnail.getWidth() * 3, thumbnail.getHeight() * 3, false);
                if (thumbnail != null) {
                    maxWidth = Math.max(thumbnail.getWidth(), maxWidth);
                    imageAdapter.addBitmap(thumbnail);
                    fileNames.add(filename);
                }
            } catch (IOException e) {
                Log.i("IOException", e.toString());
            }
        }

        Display d = getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);
        int columns = displayMetrics.widthPixels / ((int) (maxWidth) + gridView.getVerticalSpacing());
        gridView.setNumColumns(columns);
        gridView.setAdapter(imageAdapter);
    }
}

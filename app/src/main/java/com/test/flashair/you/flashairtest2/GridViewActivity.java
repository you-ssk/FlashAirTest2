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
import java.util.ArrayList;

/**
 * Created by you on 2016/01/15.
 */
public class GridViewActivity extends Activity {
    ImageAdapter imageAdapter;
    ArrayList<String> fileNames;
    String flashAirName;
    String directoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);
        Display d = getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);
        GridView gridView = (GridView) findViewById(R.id.gridview);

        imageAdapter = new ImageAdapter(this);
        fileNames = new ArrayList<>();

        Bundle extrasData = getIntent().getExtras();
        String[] filenames = extrasData.getStringArray("filenames");
        flashAirName = extrasData.getString("FlashAirName");
        directoryName = extrasData.getString("DirectoryName");

        File cachePath = getCacheDir();

        int maxWidth = 0;
        for (String filename : filenames) {
            File file = new File(cachePath, filename);
            try {
                InputStream is = new FileInputStream(file);
                Bitmap thumbnail = BitmapFactory.decodeStream(is);
                if (thumbnail != null) {
                    maxWidth = Math.max(thumbnail.getWidth(), maxWidth);
                    imageAdapter.addBitmap(thumbnail);
                    fileNames.add(filename);
                }
            } catch (IOException e) {
                Log.i("IOException", e.toString());
            }
        }
        int colmuns = displayMetrics.widthPixels / (maxWidth + gridView.getVerticalSpacing());
        gridView.setNumColumns(colmuns);
        gridView.setAdapter(imageAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String filename = fileNames.get(position);
                Log.i("GridView item clicked.", "position = " + position + ". id = " + id + ".filename = " + filename );
                if (FileItem.isJpeg(filename.toString())) {
                    Intent viewImageIntent = new Intent(getApplicationContext(), ImageViewActivity.class);
                    viewImageIntent.putExtra("flashAirName", flashAirName);
                    viewImageIntent.putExtra("downloadFile", filename);
                    viewImageIntent.putExtra("directoryName", directoryName);
                    GridViewActivity.this.startActivity(viewImageIntent);
                }
            }
        });
    }

}

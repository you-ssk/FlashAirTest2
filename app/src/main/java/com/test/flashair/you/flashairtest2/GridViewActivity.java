package com.test.flashair.you.flashairtest2;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.GridView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by you on 2016/01/15.
 */
public class GridViewActivity extends Activity {
    ImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);
        Display d = getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);
        GridView gridView = (GridView) findViewById(R.id.gridview);

        imageAdapter = new ImageAdapter(this);

        Bundle extrasData = getIntent().getExtras();
        String[] filenames = extrasData.getStringArray("filenames");
        File cachePath = getCacheDir();

        int maxWidth = 0;
        for (String filename: filenames){
            File file = new File(cachePath, filename);
            try {
                InputStream is = new FileInputStream(file);
                Bitmap thumbnail = BitmapFactory.decodeStream(is);
                if (thumbnail != null) {
                    maxWidth = Math.max(thumbnail.getWidth(), maxWidth);
                    imageAdapter.addBitmap(thumbnail);
                }
            } catch (IOException e){
                Log.i("IOException", e.toString());
            }
        }
        int colmuns = displayMetrics.widthPixels / (maxWidth + gridView.getVerticalSpacing());
        gridView.setNumColumns(colmuns);


        gridView.setAdapter(imageAdapter);
    }
}

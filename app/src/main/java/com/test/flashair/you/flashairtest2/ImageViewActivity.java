package com.test.flashair.you.flashairtest2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by you on 2016/01/09.
 */
public class ImageViewActivity extends Activity {
    ImageView imageView;
    Button backButton;
    Button saveButton;
    String filename;
    Bitmap downloadBitmap;

    String flashAirName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        getIntent();
        imageView = (ImageView) findViewById(R.id.imageView1);
        backButton = (Button) findViewById(R.id.button2);
        backButton.getBackground().setColorFilter(Color.rgb(65, 183, 216), PorterDuff.Mode.SRC_IN);
        saveButton = (Button) findViewById(R.id.button3);
        saveButton.getBackground().setColorFilter(Color.rgb(216, 183, 65), PorterDuff.Mode.SRC_IN);

        Bundle extrasData = getIntent().getExtras();
        flashAirName = extrasData.getString("flashAirName");
        String fileName = extrasData.getString("downloadFile");
        String directory = extrasData.getString("directoryName");
        File cacheDir = getCacheDir();
        File file = new File(cacheDir, fileName);
        try {
            InputStream is = new FileInputStream(file);
            Bitmap thumbnail = BitmapFactory.decodeStream(is);
            viewImage(thumbnail);
        } catch (IOException e) {
            Log.i("IOException", e.toString());
        }
        downloadFile(fileName, directory);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.image_view, menu);
        return true;
    }

    void downloadFile(final String downloadFile, String directory) {
        final ProgressDialog waitDialog;
        waitDialog = new ProgressDialog(this);
        waitDialog.setMessage("Now downloading...");
        waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        waitDialog.show();
        new AsyncTask<String, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... params) {
                String fileName = params[0];
                return FlashAirRequest.getBitmap(fileName);
            }

            @Override
            protected void onPostExecute(Bitmap resultBitmap) {
                waitDialog.dismiss();
                viewImage(resultBitmap);
                filename = downloadFile;
                downloadBitmap = resultBitmap;
            }
        }.execute("http://" + flashAirName + "/" + directory + "/" + downloadFile.toString());
    }

    void viewImage(Bitmap imageBitmap) {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageViewActivity.this.finish();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage(downloadBitmap, filename);
            }
        });

        if (imageBitmap == null) {
            imageView.setImageResource(R.drawable.ic_launcher);
        } else {
            int w = imageBitmap.getWidth();
            int h = imageBitmap.getHeight();
            double wScale = w / 2048.0;
            double hScale = h / 2048.0;
            double scale = Math.max(1.0, Math.max(wScale, hScale));
            Bitmap scaled = Bitmap.createScaledBitmap(imageBitmap, (int) (w / scale), (int) (h / scale), false);

            imageView.setImageBitmap(scaled);
        }
    }

    void saveImage(Bitmap imageBitmap, String filename) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(path, "flashair" + "/" + filename);
        try {
            path.mkdirs();
            OutputStream os = new FileOutputStream(file);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (IOException e) {
            Log.e("ERROR:", e.toString());
        }

        ContentValues values = new ContentValues();
        ContentResolver cr = getContentResolver();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put("_data", file.toString());
        cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}

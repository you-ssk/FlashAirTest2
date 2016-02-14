package com.test.flashair.you.flashairtest2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by you on 2016/01/09.
 */
public class ImageViewActivity extends Activity {
    String flashAirName;
    ImageView imageView;
    Button saveButton;
    Button downButton;

    String filename;
    String directory;
    FileItem item;
    byte[] downloadBitmapByteArray;

    double zoomFactors[] = {1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 2.0};
    int zoomIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        imageView = (ImageView) findViewById(R.id.imageView1);

        Bundle extrasData = getIntent().getExtras();
        flashAirName = extrasData.getString("flashAirName");
        filename = extrasData.getString("downloadFile");
        directory = extrasData.getString("directoryName");
        item = (FileItem) extrasData.getSerializable("ImageItem");

        setupButton();
        File cacheDir = getCacheDir();
        File file = new File(cacheDir, filename);
        viewThumbnail(file);

        imageView.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view){
                if (downloadBitmapByteArray == null)
                    return;

                zoomIndex += 1;
                double zoomFactor = zoomFactors[zoomIndex % zoomFactors.length];
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(downloadBitmapByteArray, 0, downloadBitmapByteArray.length, opts);
                Size orgSize = new Size(opts.outWidth, opts.outHeight);
                Point pos = new Point(orgSize.getWidth() / 2, orgSize.getHeight() / 2);
                Bitmap bitmap = getBitmap(downloadBitmapByteArray, pos, zoomFactor);
                imageView.setImageBitmap(bitmap);
            }
        });
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
        waitDialog.setMax(item.size);
        waitDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        waitDialog.show();
        String command = "http://" + flashAirName + "/" + directory + "/" + downloadFile;
        new ImageDownloader(waitDialog, listener).execute(command);
    }

    void setupButton() {
        saveButton = (Button) findViewById(R.id.button3);
        saveButton.getBackground().setColorFilter(Color.rgb(216, 183, 65), PorterDuff.Mode.SRC_IN);
        downButton = (Button) findViewById(R.id.button4);
        downButton.getBackground().setColorFilter(Color.rgb(65, 216, 183), PorterDuff.Mode.SRC_IN);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage(downloadBitmapByteArray, filename);
            }
        });

        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadFile(filename, directory);
            }
        });
    }

    void viewThumbnail(File file) {
        imageView.setImageBitmap(BitmapFactory.decodeFile(file.toString()));
    }

    void viewImage(final byte[] bitmapByteArray) {
        if (bitmapByteArray == null) {
            imageView.setImageResource(R.drawable.ic_launcher);
        } else {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 4;
            Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length, opts);
            imageView.setImageBitmap(bitmap);
            downloadBitmapByteArray = bitmapByteArray;
        }
    }

    void viewImageRegion(final byte[] bitmapByteArray) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length, opts);
        Size orgSize = new Size(opts.outWidth, opts.outHeight);
        Point pos = new Point(orgSize.getWidth() / 2, orgSize.getHeight() / 2);
        Bitmap bitmap = getBitmap(bitmapByteArray, pos, 1.0);
        imageView.setImageBitmap(bitmap);
        downloadBitmapByteArray = bitmapByteArray;
    }

    Bitmap getBitmap(final byte[] bitmapByteArray, Point pos, double zoomFactor) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length, opts);

        Rect rect;
        BitmapFactory.Options regionOpts = new BitmapFactory.Options();
        {
            int preferredWidth = (int) (opts.outWidth / zoomFactor);
            int preferredHeight = (int) (opts.outHeight / zoomFactor);
            rect = new Rect(pos.x - preferredWidth / 2, pos.y - preferredHeight / 2, preferredWidth, preferredHeight);
            regionOpts.inSampleSize = Math.max(preferredWidth / imageView.getWidth(), preferredHeight / imageView.getHeight());
        }
        try {
            BitmapRegionDecoder regionDecoder = BitmapRegionDecoder.newInstance(bitmapByteArray, 0, bitmapByteArray.length, true);
            return regionDecoder.decodeRegion(rect, regionOpts);
        } catch (IOException e) {
            Log.e("IOException", e.toString());
        }
        return null;
    }

    void saveImage(byte[] bitmapByteArray, String filename) {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/flashair";
        File dir = new File(path);
        if (!dir.exists()) {
            boolean r = dir.mkdirs();
        }
        File file = new File(path, filename);
        try {
            OutputStream os = new FileOutputStream(file);
            os.write(bitmapByteArray);
            os.close();
            updateContent(file.toString());
        } catch (IOException e) {
            Log.e("ERROR:", e.toString());
        }
    }

    void updateContent(String filename) {
        ContentValues values = new ContentValues();
        ContentResolver cr = getContentResolver();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put("_data", filename.toString());
        cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }


    private ImageDownloader.ImageDownloadCompleted listener = new ImageDownloader.ImageDownloadCompleted() {
        public void onCompleted(byte[] byteArray) {
            //viewImage(byteArray);
            viewImageRegion(byteArray);
        }
    };
}

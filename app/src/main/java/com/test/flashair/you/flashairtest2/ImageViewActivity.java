package com.test.flashair.you.flashairtest2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.util.Size;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.OverScroller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by you on 2016/01/09.
 */
public class ImageViewActivity extends Activity {
    ImageView imageView;
    Button saveButton;
    Button downButton;

    String flashAirName;
    String filename;
    String directory;
    FileItem item;
    byte[] downloadBitmapByteArray;
    Size bitmap_size;
    double initialScale = 1.0;

    private GestureDetectorCompat mDetector;
    private ScaleGestureDetector mScaleDetector;
    private OverScroller mScroller;
    private final Handler mHandler = new Handler();

    private ImageViewActivity self = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        imageView = (ImageView) findViewById(R.id.imageView1);
        imageView.setScaleType(ImageView.ScaleType.MATRIX);

        Bundle extrasData = getIntent().getExtras();
        flashAirName = extrasData.getString("flashAirName");
        filename = extrasData.getString("downloadFile");
        directory = extrasData.getString("directoryName");
        item = (FileItem) extrasData.getSerializable("ImageItem");

        setupButton();
        File cacheDir = getCacheDir();
        File file = new File(cacheDir, filename);
        Bitmap bitmap = BitmapFactory.decodeFile(file.toString());
        bitmap_size = new Size(bitmap.getWidth(), bitmap.getHeight());
        imageView.setImageBitmap(bitmap);

        mDetector = new GestureDetectorCompat(this, new MyGestureListener());
        mScaleDetector = new ScaleGestureDetector(this, new MyScaleGestureDetector());
        mScroller = new OverScroller(getApplicationContext());
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus)
            return;
        Reset();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.image_view, menu);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean retVal = mScaleDetector.onTouchEvent(event);
        retVal = mDetector.onTouchEvent(event) || retVal;
        return retVal || super.onTouchEvent(event);
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

    void viewImage(final byte[] bitmapByteArray) {
        if (bitmapByteArray == null) {
            imageView.setImageResource(R.drawable.ic_launcher);
        } else {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 4;
            Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length, opts);
            bitmap_size = new Size(bitmap.getWidth(), bitmap.getHeight());
            imageView.setImageBitmap(bitmap);
            downloadBitmapByteArray = bitmapByteArray;
            Reset();
        }
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
            downloadBitmapByteArray = byteArray;
            viewImage(byteArray);
        }
    };

    private void ZoomTo(float scale, float x, float y) {
        Matrix matrix = new Matrix(imageView.getImageMatrix());
        matrix.postScale(scale, scale, x, y);
        float[] values = new float[9];
        matrix.getValues(values);
        if (values[Matrix.MSCALE_X] < initialScale || values[Matrix.MSCALE_X] > initialScale * 5)
            return;
        imageView.setImageMatrix(matrix);
        imageView.invalidate();
    }

    private void Scroll(float x, float y) {
        Matrix matrix = new Matrix(imageView.getImageMatrix());
        matrix.postTranslate(x, y);
        imageView.setImageMatrix(matrix);
        imageView.invalidate();
    }

    private void Fling(float velocityX, float velocityY){
        mScroller.forceFinished(true);
        Matrix matrix = imageView.getImageMatrix();
        float[] values = new float[9];
        matrix.getValues(values);
        int startX = (int) values[Matrix.MTRANS_X];
        int startY = (int) values[Matrix.MTRANS_Y];
        mScroller.fling(startX, startY, (int) velocityX, (int) velocityY,
                (int) -Float.MAX_VALUE, (int) Float.MAX_VALUE,
                (int) -Float.MAX_VALUE, (int) Float.MAX_VALUE);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mScroller.computeScrollOffset();
                Matrix matrix = imageView.getImageMatrix();
                float[] values = new float[9];
                matrix.getValues(values);
                values[Matrix.MTRANS_X] = mScroller.getCurrX();
                values[Matrix.MTRANS_Y] = mScroller.getCurrY();
                matrix.setValues(values);
                imageView.setImageMatrix(matrix);
                imageView.invalidate();
                if (!mScroller.isFinished())
                    mHandler.postDelayed(this, 10);
            }
        });
    }

    private void Reset() {
        Matrix matrix = new Matrix();
        RectF src = new RectF(0, 0, bitmap_size.getWidth(), bitmap_size.getHeight());
        RectF dst = new RectF(0, 0, imageView.getWidth(), imageView.getHeight());
        matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);
        imageView.setImageMatrix(matrix);
        float[] values = new float[9];
        matrix.getValues(values);
        initialScale = values[Matrix.MSCALE_X];
        imageView.invalidate();
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDown(MotionEvent event) {
            //Log.d(DEBUG_TAG, "onDown: " + event.toString());
            mScroller.forceFinished(true);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //Log.d(DEBUG_TAG, "onFling: " + e1.toString() + e2.toString() + "\nvelocity = " + velocityX + "," + velocityY);
            Fling(velocityX, velocityY);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            self.Scroll(-distanceX, -distanceY);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            self.ZoomTo(1.3f, e.getX(), e.getY());
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            self.Reset();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            //Log.d(DEBUG_TAG, "onLongPress: " + e.toString());
        }
    }

    private class MyScaleGestureDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private static final String DEBUG_TAG = "ScaleGestureDetector";

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            ZoomTo(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
            return true;
        }
    }
}

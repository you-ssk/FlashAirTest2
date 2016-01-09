package com.test.flashair.you.flashairtest2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created by you on 2016/01/09.
 */
public class ImageViewActivity extends Activity {
    ImageView imageView;
    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        getIntent();
        imageView = (ImageView)findViewById(R.id.imageView1);
        backButton = (Button)findViewById(R.id.button2);
        backButton.getBackground().setColorFilter(
                Color.rgb(65, 183, 216), PorterDuff.Mode.SRC_IN
        );
        Bundle extrasData = getIntent().getExtras();
        String fileName = extrasData.getString("downloadFile");
        String directory = extrasData.getString("directoryName");
        downloadFile(fileName, directory);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //getMenuInflater().inflate(R.menu.image_view, menu);
        return true;
    }

    void downloadFile(String downloadFile, String directory){
        Log.i("downloadFile", downloadFile + ":" + directory);
        final ProgressDialog waitDialog;
        waitDialog = new ProgressDialog(this);
        waitDialog.setMessage("Now downloading...");
        waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        waitDialog.show();
        new AsyncTask<String, Void, Bitmap>(){
            @Override
            protected Bitmap doInBackground(String... params){
                String fileName = params[0];
                return FlashAirRequest.getBitmap(fileName);
            }
            @Override
            protected void onPostExecute(Bitmap resultBitmap) {
                waitDialog.dismiss();
                viewImage(resultBitmap);
            }
        }.execute("http://192.168.0.11/" + directory + "/" + downloadFile.toString());
    }

    void viewImage(Bitmap imageBitmap){
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageViewActivity.this.finish();
            }
        });

        if (imageBitmap == null){
            imageView.setImageResource(R.drawable.ic_launcher);
        } else {
            imageView.setImageBitmap(imageBitmap);
        }
    }
}

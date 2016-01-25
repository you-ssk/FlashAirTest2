package com.test.flashair.you.flashairtest2;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by you on 2016/01/25.
 */
public class ImageDownloader extends AsyncTask<String, Integer, byte[]> {
    public interface ImageDownloadCompleted {
        void onCompleted(byte[] byteArray);
    }
    ProgressDialog progress;
    ImageDownloadCompleted listener;

    public ImageDownloader(ProgressDialog _progress, ImageDownloadCompleted _listener){
        progress = _progress;
        listener = _listener;
    }
    @Override
    protected byte[] doInBackground(String... params) {
        String command = params[0];
        byte[] byteArray = null;
        try {
            URL url = new URL(command);
            HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();
            urlCon.connect();
            InputStream inputStream = urlCon.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] byteChunk = new byte[2048];
            int bytesRead = 0;
            while ((bytesRead = inputStream.read(byteChunk)) != -1) {
                byteArrayOutputStream.write(byteChunk, 0, bytesRead);
                publishProgress(byteArrayOutputStream.size());
            }
            inputStream.close();
            byteArray = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
        } catch (MalformedURLException e) {
            Log.e("", "ERROR: " + e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("", "ERROR: " + e.toString());
            e.printStackTrace();
        }
        return byteArray;
    }

    @Override
    protected void onPostExecute(byte[] byteArray) {
        progress.dismiss();
        listener.onCompleted(byteArray);
    }

    protected void onProgressUpdate(Integer... _progress){
        progress.setProgress(_progress[0]);
    }
}

package com.test.flashair.you.flashairtest2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by you on 2016/01/04.
 */
public class FlashAirRequest {
    static public String getString(String command){
        String result = "";
        try{
            URL url = new URL(command);

            //URLConnection urlCon = url.openConnection();
            HttpURLConnection urlCon = (HttpURLConnection)url.openConnection();
            urlCon.connect();
            InputStream inputStream = urlCon.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuffer strbuf = new StringBuffer();
            String str;
            while ((str = bufferedReader.readLine()) != null){
                if (strbuf.toString() != "")
                    strbuf.append("\n");
                strbuf.append(str);
            }
            result = strbuf.toString();
        } catch (MalformedURLException e){
            Log.e("ERROR", "ERROR: " + e.toString());
            e.printStackTrace();
            result = e.toString();
        } catch (IOException e){
            Log.e("ERROR", "ERROR: " + e.toString());
            e.printStackTrace();
            result = e.toString();
        }
        return result;
    }

    static public Bitmap getBitmap(String command){
        Bitmap resultBitmap = null;
        try{
            URL url = new URL(command);
            //URLConnection urlCon = url.openConnection();
            HttpURLConnection urlCon = (HttpURLConnection)url.openConnection();
            urlCon.connect();
            InputStream inputStream = urlCon.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] byteChunk = new byte[2048];
            int bytesRead = 0;
            while ( (bytesRead = inputStream.read(byteChunk)) != -1){
                byteArrayOutputStream.write(byteChunk, 0, bytesRead);
            }
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            BitmapFactory.Options bfOptions = new BitmapFactory.Options();
            resultBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, bfOptions);
            byteArrayOutputStream.close();
            inputStream.close();
        } catch (MalformedURLException e){
            Log.e("","ERROR: " + e.toString());
            e.printStackTrace();
        } catch (IOException e){
            Log.e("","ERROR: " + e.toString());
            e.printStackTrace();
        }
        return resultBitmap;
    }
}

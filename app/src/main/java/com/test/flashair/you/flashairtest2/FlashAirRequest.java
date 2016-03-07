package com.test.flashair.you.flashairtest2;

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
    static public String getFileCount(String name, String dir) {
        return FlashAirRequest.getString("http://" + name + "/command.cgi?op=101&DIR=" + dir);
    }

    static public String getFileList(String name, String dir) {
        return FlashAirRequest.getString("http://" + name + "/command.cgi?op=100&DIR=" + dir);
    }

    static public  byte[] getThumbnail(String flashAirName, String file){
        return getBitmapByteArray("http://" + flashAirName + "/thumbnail.cgi?" + file);
    }
    static public String getString(String command) {
        String result;
        try {
            URL url = new URL(command);
            URLConnection urlCon = url.openConnection();
            urlCon.connect();
            InputStream inputStream = urlCon.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder strBuf = new StringBuilder();
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                if (! strBuf.toString().equals(""))
                    strBuf.append("\n");
                strBuf.append(str);
            }
            result = strBuf.toString();
        } catch (MalformedURLException e) {
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

    static public byte[] getBitmapByteArray(String command) {
        byte[] byteArray = null;
        try {
            URL url = new URL(command);
            HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();
            urlCon.connect();
            InputStream inputStream = urlCon.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] byteChunk = new byte[2048];
            int bytesRead;
            while ((bytesRead = inputStream.read(byteChunk)) != -1) {
                byteArrayOutputStream.write(byteChunk, 0, bytesRead);
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
}

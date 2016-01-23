package com.test.flashair.you.flashairtest2;

import android.graphics.drawable.BitmapDrawable;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by you on 2016/01/17.
 */
public class FileItem {
    String directory;
    String filename;
    int size;
    int attribute;
    GregorianCalendar date;
    BitmapDrawable thumbnail;

    FileItem(String[] data){
        parse(data);
    }
    void parse(String[] data){
        directory = data[0];
        filename =  data[1];
        if (! filename.contains(".")){
            filename += "/";
        }
        size = Integer.parseInt(data[2]);
        attribute = Integer.parseInt(data[3]);
        int idate = Integer.parseInt(data[4]);

        int year = (idate & 0b1111_1110_0000_0000) >> 9 + 1980;
        int month = (idate & 0b0000_0001_1110_0000) >> 5;
        int day = (idate & 0b0000_0000_0001_1111);

        int itime = Integer.parseInt(data[5]);
        int hour = (itime & 0b1111_1000_0000_0000) >> 11;
        int min = (itime & 0b0000_0111_1110_0000) >> 5;
        int sec = (itime & 0b0000_0000_0001_1111) * 2;
        date = new GregorianCalendar(year,month,day,hour,min,sec);
    }

    static public boolean isJpeg(String filename){
        return filename.toLowerCase(Locale.getDefault()).endsWith(".jpg");
    }
    static public boolean isRaw(String filename){
        return filename.toLowerCase(Locale.getDefault()).endsWith(".nef");
    }

}

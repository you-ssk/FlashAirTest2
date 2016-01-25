package com.test.flashair.you.flashairtest2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by you on 2016/01/17.
 */
public class FileItem implements Serializable {
    String directory;
    String filename;
    int size;
    int attribute;
    GregorianCalendar date;

    FileItem(String[] data) {
        parse(data);
    }

    void parse(String[] data) {
        directory = data[0];
        filename = data[1];
        if (!filename.contains(".")) {
            filename += "/";
        }
        size = Integer.parseInt(data[2]);
        attribute = Integer.parseInt(data[3]);
        int iDate = Integer.parseInt(data[4]);

        int year = (iDate & 0b1111_1110_0000_0000) >> 9 + 1980;
        int month = (iDate & 0b0000_0001_1110_0000) >> 5;
        int day = (iDate & 0b0000_0000_0001_1111);

        int iTime = Integer.parseInt(data[5]);
        int hour = (iTime & 0b1111_1000_0000_0000) >> 11;
        int min = (iTime & 0b0000_0111_1110_0000) >> 5;
        int sec = (iTime & 0b0000_0000_0001_1111) * 2;
        date = new GregorianCalendar(year, month, day, hour, min, sec);
    }

    static public boolean isJpeg(String filename) {
        return filename.toLowerCase(Locale.getDefault()).endsWith(".jpg");
    }

    static public boolean isRaw(String filename) {
        return filename.toLowerCase(Locale.getDefault()).endsWith(".nef");
    }

    static public void parse(TreeMap<String, FileItem> imageItems, String input) {
        imageItems.clear();
        String[] allFiles = input.split("([,\n])");
        for (int i = 1; i < allFiles.length; i = i + 6) {
            String[] elem = Arrays.copyOfRange(allFiles, i, i + 6);
            FileItem item = new FileItem(elem);
            imageItems.put(item.filename, item);
        }
    }

    static public TreeMap<String, FileItem> createMap(){
        Comparator<String> fileItemComparator = new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s2.compareTo(s1);
            }
        };
        return new TreeMap<>(fileItemComparator);
    }
}

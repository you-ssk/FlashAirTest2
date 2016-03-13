package com.test.flashair.you.flashairtest2;

import java.util.Comparator;

/**
 * Created by you on 2016/03/13.
 */

public class FileItemComparator implements Comparator<FileItem> {
    @Override
    public int compare(FileItem f1, FileItem f2){
        return f1.date.compareTo(f2.date);
    }
}

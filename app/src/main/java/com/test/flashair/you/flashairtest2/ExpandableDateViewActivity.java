package com.test.flashair.you.flashairtest2;

import android.app.Activity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by you on 2016/03/13.
 */
public class ExpandableDateViewActivity extends Activity {

    private final String KEY1 = "TITLE";
    private final String KEY2 = "CHILD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expand_date_view);

        Bundle extrasData = getIntent().getExtras();
        Serializable s = extrasData.getSerializable("ImageItems");
        HashMap<String, FileItem> hashMap = (HashMap<String, FileItem>) (s);

        HashMap<String, List<FileItem>> items = new HashMap<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Map.Entry<String, FileItem> item : hashMap.entrySet()) {
            if (! FileItem.isJpeg(item.getValue().filename)){
                continue;
            }
            Calendar date = item.getValue().date;
            String key = sdf.format(date.getTime());
            if (items.get(key) == null) {
                items.put(key, new ArrayList<FileItem>());
            }
            items.get(key).add(item.getValue());
        }

        List<String> dates = new ArrayList<>(items.keySet());
        Collections.sort(dates);
        Collections.reverse(dates);

        List<Map<String, String>> parentList = new ArrayList<>();
        List<List<Map<String, String>>> allChildList = new ArrayList<>();

        for (String i : dates) {
            Map<String, String> parentData = new HashMap<>();
            parentData.put(KEY1, i);
            parentList.add(parentData);
        }

        for (String i : dates) {
            List<Map<String, String>> childList = new ArrayList<>();
            List<FileItem> c = items.get(i);
            Collections.sort(c, new FileItemComparator());
            Collections.reverse(c);
            for (FileItem item : c ){
                Map<String, String> childData = new HashMap<>();
                childData.put(KEY2, item.filename);
                childList.add(childData);
            }
            allChildList.add(childList);
        }

        SimpleExpandableListAdapter adapter =
                new SimpleExpandableListAdapter(
                        this,
                        parentList,
                        android.R.layout.simple_expandable_list_item_1,
                        new String[]{KEY1},
                        new int[]{android.R.id.text1, android.R.id.text2},
                        allChildList,
                        android.R.layout.simple_expandable_list_item_1,
                        new String[]{KEY2},
                        new int[]{android.R.id.text1, android.R.id.text2}
                );
        ExpandableListView elv = (ExpandableListView) findViewById(R.id.expandableListView);
        elv.setAdapter(adapter);

        elv.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                ExpandableListAdapter adapter = parent.getExpandableListAdapter();
                Map<String, String> item = (Map<String, String>) adapter.getGroup(groupPosition);
                Toast.makeText(getApplicationContext(), "Pressed." + item.get(KEY1), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                ExpandableListAdapter adapter = parent.getExpandableListAdapter();
                Map<String, String> item = (Map<String, String>) adapter.getChild(groupPosition, childPosition);
                Toast.makeText(getApplicationContext(), "Child Pressed. " + item.get(KEY2), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }
}

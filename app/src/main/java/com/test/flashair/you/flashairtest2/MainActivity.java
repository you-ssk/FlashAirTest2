package com.test.flashair.you.flashairtest2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ListView listView;
    Button backButton;
    TextView currentDirText;
    TextView numFilesText;

    //String flashairName = "192.168.0.11";
    //String flashairName = "flashair";
    String flashairName = "192.168.43.123";

    String rootDir = "DCIM/101NCD90";
    String directoryName = rootDir;

    SimpleAdapter listAdapter;
    int checkInterval = 5000;
    Handler updateHandler;
    boolean viewingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] perms = {
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_EXTERNAK_STORAGE",
                "android.permission.INTERNET",
                "android.permission.ACCESS_NETWORK_STATE",
                "android.permission.ACCESS_WIFI_STATE"
        };
        int permsRequestCode = 200;
        requestPermissions(perms, permsRequestCode);
        setContentView(R.layout.activity_main);
        viewingList = true;
        try {
            backButton = (Button) findViewById(R.id.button1);
            backButton.getBackground().setColorFilter(Color.rgb(65, 138, 216), PorterDuff.Mode.SRC_IN);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (directoryName.equals(rootDir)) {
                        listRootDirectory();
                    } else {
                        int index = directoryName.lastIndexOf("/");
                        directoryName = directoryName.substring(0, index);
                        listDirectory(directoryName);
                    }
                }
            });
            backButton.setEnabled(false);
            listRootDirectory();
        } catch (Exception e){
            Log.e("ERROR", "ERROR in CODE:" + e.toString());
            e.printStackTrace();
        }
        updateHandler = new Handler();
        startUpdate();
/*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        viewingList = hasFocus;
    }


    public boolean checkIfListView(){
        return viewingList;
    }

    public Runnable statusChecker = new Runnable() {
        @Override
        public void run() {
            if (checkIfListView()){
                new AsyncTask<String, Void, String>(){
                    @Override
                    protected String doInBackground(String... params){
                        return FlashAirRequest.getString(params[0]);
                    }

                    @Override
                    protected void onPostExecute(String status){
                        if (status.equals("1")){
                            listDirectory(directoryName);
                        }
                    }
                } .execute("http://" + flashairName + "/command.cgi?op=102");
            }
            updateHandler.postDelayed(statusChecker, checkInterval);
        }
    };

    public void startUpdate(){
        statusChecker.run();
    }

    public void stopUpdate(){
        updateHandler.removeCallbacks(statusChecker);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        Object item = l.getItemAtPosition(position);
        if (item instanceof Map<?,?>){
            Map<String, Object> mapItem = (Map<String, Object>)item;
            Object downloadFile = mapItem.get("fname");
            if (downloadFile.toString().endsWith("/")){
                String dirName = downloadFile.toString().substring(0, downloadFile.toString().length()-1);
                directoryName = directoryName + "/" + dirName;
                listDirectory(directoryName);
            } else if ( downloadFile.toString().toLowerCase((Locale.getDefault())).endsWith(".jpg")) {
                Intent viewImageIntent = new Intent(this, ImageViewActivity.class);
                viewImageIntent.putExtra("downloadFile", downloadFile.toString());
                viewImageIntent.putExtra("directoryName", directoryName);
                if (mapItem.containsKey("bmp"))
                    viewImageIntent.putExtra("thumbnail", (Parcelable)mapItem.get("bmp"));
                MainActivity.this.startActivity(viewImageIntent);
            }
        }
    }

    public void listRootDirectory() {
        directoryName = rootDir;
        listDirectory(directoryName);
    }

    public void listDirectory(String dir) {
        if (dir.equals(rootDir)) {
            backButton.setEnabled(false);
        } else {
            backButton.setEnabled(true);
        }
        currentDirText = (TextView) findViewById(R.id.textView1);
        currentDirText.setText(dir + "/");
        dir = "/" + dir;
        numFilesText = (TextView) findViewById(R.id.textView2);

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String dir = params[0];
                return FlashAirRequest.getString("http://" + flashairName + "/command.cgi?op=101&DIR=" + dir);
            }

            @Override
            protected void onPostExecute(String fileCount) {
                numFilesText.setText("Items Found: " + fileCount);
            }
        }.execute(dir);

        new AsyncTask<String, Void, ListAdapter>() {
            @Override
            protected ListAdapter doInBackground(String... params) {
                String dir = params[0];
                ArrayList <String> fileNames = new ArrayList<String>();
                String files = FlashAirRequest.getString("http://" + flashairName + "/command.cgi?op=100&DIR=" + dir);
                String[] allFiles = files.split("([,\n])");
                for (int i = 2; i < allFiles.length; i=i+6){
                    if (allFiles[i].contains(".")) {
                        fileNames.add(allFiles[i]);
                    } else {
                        fileNames.add(allFiles[i] + "/");
                    }
                }

                ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
                for (int i = 0; i < fileNames.size(); i++){
                    String fileName = "http://" + flashairName + "/thumbnail.cgi?" + directoryName + "/" + fileNames.get(i);
                    Map<String, Object> entry = new HashMap<String, Object>();
                    BitmapDrawable drawIcon = null;
                    if ( (fileName.toLowerCase(Locale.getDefault()).endsWith((".jpg")))){
                        Bitmap thumbnail = FlashAirRequest.getBitmap(fileName);
                        entry.put("bmp", thumbnail);
                        drawIcon = new BitmapDrawable(getResources(), thumbnail);
                    }
                    if (drawIcon == null){
                        entry.put("thmb", R.drawable.ic_launcher);
                    } else {
                        entry.put("thmb", drawIcon);
                    }
                    entry.put("fname", fileNames.get(i));
                    data.add(entry);
                }
                listAdapter = new SimpleAdapter(
                        MainActivity.this,
                        data,
                        android.R.layout.activity_list_item,
                        new String[]{"thmb", "fname"},
                        new int[]{android.R.id.icon, android.R.id.text1});
                listAdapter.setViewBinder(new CustomViewBinder());
                return listAdapter;
            }
            @Override
            protected void onPostExecute(ListAdapter listAdapter) {
                listView = (ListView)findViewById(R.id.listView1);
                ColorDrawable divcolor = new ColorDrawable(Color.rgb(17,19,58));
                listView.setDivider(divcolor);
                listView.setDividerHeight(1);
                listView.setAdapter(listAdapter);
                listView.setOnItemClickListener(MainActivity.this);
            }
        }.execute(dir);
    }

    class CustomViewBinder implements SimpleAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            if ((view instanceof ImageView) && (data instanceof Drawable)) {
                ImageView imageView = (ImageView) view;
                BitmapDrawable thumbnail = (BitmapDrawable) data;
                imageView.setImageDrawable(thumbnail);
                return true;
            }
            return false;
        }
    }
}

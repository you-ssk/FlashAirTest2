package com.test.flashair.you.flashairtest2;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity
        extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    ListView mListView;
    SimpleAdapter mListAdapter;
    Button mGridButton;

    static String defaultFlashAirName = "192.168.0.255";
    static String defaultRootDir = "DCIM/101NCD90";
    String mFlashAirName = defaultFlashAirName;
    String mRootDir = defaultRootDir;
    String mDirectoryName = mRootDir;

    int checkInterval = 5000;
    Handler mUpdateHandler;
    boolean mViewingList;
    SharedPreferences.OnSharedPreferenceChangeListener mPrefListener;
    TreeMap<String, FileItem> mImageItems;

    private static final String PREF_KEY_FLASH_AIR_NAME = "FlashAirName";
    private static final String PREF_KEY_ROOT_DIR = "RootDir";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] perms = {
                //"android.permission.INTERNET",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_EXTERNAK_STORAGE",
                //"android.permission.ACCESS_NETWORK_STATE",
                //"android.permission.ACCESS_WIFI_STATE"
        };
        int permsRequestCode = 200;
        requestPermissions(perms, permsRequestCode);

        setContentView(R.layout.activity_main);
        Toolbar main_toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(main_toolbar);
        setupPrefListener();
        readPref();
        setupGridButton();

        mViewingList = true;
        mImageItems = FileItem.createMap();
        mUpdateHandler = new Handler();

        listRootDirectory();
        startUpdate();
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
        mViewingList = hasFocus;
    }

    public boolean checkIfListView() {
        return mViewingList;
    }

    public Runnable statusChecker = new Runnable() {
        @Override
        public void run() {
            if (checkIfListView()) {
                new AsyncTask<String, Void, String>() {
                    @Override
                    protected String doInBackground(String... params) {
                        return FlashAirRequest.getString(params[0]);
                    }

                    @Override
                    protected void onPostExecute(String status) {
                        if (status.equals("1")) {
                            listDirectory(mDirectoryName);
                        }
                    }
                }.execute("http://" + mFlashAirName + "/command.cgi?op=102");
            }
            mUpdateHandler.postDelayed(statusChecker, checkInterval);
        }
    };

    public void startUpdate() {
        statusChecker.run();
    }

    public void stopUpdate() {
        mUpdateHandler.removeCallbacks(statusChecker);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            final View dialogView = getLayoutInflater().inflate(R.layout.address_dialog, null);
            final EditText address = (EditText) dialogView.findViewById(R.id.editAddress);
            final EditText root = (EditText) dialogView.findViewById(R.id.rootDir);
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            address.setText(prefs.getString(PREF_KEY_FLASH_AIR_NAME, ""));
            root.setText(prefs.getString(PREF_KEY_ROOT_DIR, ""));

            new AlertDialog.Builder(this)
                    .setTitle("Setting")
                    .setView(dialogView)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String a = address.getText().toString();
                            String r = root.getText().toString();
                            prefs.edit()
                                    .putString(PREF_KEY_FLASH_AIR_NAME, a)
                                    .putString(PREF_KEY_ROOT_DIR, r)
                                    .apply();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        Object item = l.getItemAtPosition(position);
        if (item instanceof Map<?, ?>) {
            Map<String, Object> mapItem = (Map<String, Object>) item;
            String filenName = mapItem.get("fname").toString();
            if (filenName.endsWith("/")) {
                String dirName = filenName.substring(0, filenName.length() - 1);
                mDirectoryName = mDirectoryName + "/" + dirName;
                listDirectory(mDirectoryName);
            } else if (FileItem.isJpeg(filenName)) {
                Intent viewImageIntent = new Intent(this, ImageViewActivity.class);
                viewImageIntent.putExtra("FlashAirName", mFlashAirName);
                viewImageIntent.putExtra("DirectoryName", mDirectoryName);
                viewImageIntent.putExtra("ImageItem", mImageItems.get(filenName));
                MainActivity.this.startActivity(viewImageIntent);
            }
        }
    }

    public void listRootDirectory() {
        mDirectoryName = mRootDir;
        listDirectory(mDirectoryName);
    }

    public void listDirectory(String dir) {
        ((TextView) findViewById(R.id.textView1)).setText(dir + "/");
        dir = "/" + dir;

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                return FlashAirRequest.getFileCount(mFlashAirName, params[0]);
            }

            @Override
            protected void onPostExecute(String fileCount) {
                ((TextView) findViewById(R.id.textView2)).setText("Items Found: " + fileCount);
            }
        }.execute(dir);

        new AsyncTask<String, Void, ListAdapter>() {
            @Override
            protected ListAdapter doInBackground(String... params) {
                String files = FlashAirRequest.getFileList(mFlashAirName, params[0]);
                FileItem.parse(mImageItems, files);
                ArrayList<Map<String, Object>> data = new ArrayList<>();
                for (FileItem item : mImageItems.values()) {
                    String filename = item.filename;
                    if (!FileItem.isJpeg(filename)) {
                        continue;
                    }
                    {
                        updateCachedThumbnail(item);
                        Map<String, Object> entry = new HashMap<>();
                        entry.put("filename", filename);
                        entry.put("fname", filename);
                        data.add(entry);
                    }
                }

                mListAdapter = new SimpleAdapter(
                        MainActivity.this,
                        data,
                        android.R.layout.activity_list_item,
                        new String[]{"filename", "fname"},
                        new int[]{android.R.id.icon, android.R.id.text1});
                mListAdapter.setViewBinder(new CustomViewBinder());
                return mListAdapter;
            }

            @Override
            protected void onPostExecute(ListAdapter listAdapter) {
                ((TextView) findViewById(R.id.textView2)).setText("Items Found(jpeg): " + listAdapter.getCount());
                mListView = (ListView) findViewById(R.id.listView1);
                mListView.setDivider(new ColorDrawable(Color.rgb(17, 19, 58)));
                mListView.setDividerHeight(1);
                mListView.setAdapter(listAdapter);
                mListView.setOnItemClickListener(MainActivity.this);
            }
        }.execute(dir);
    }

    private void setupPrefListener() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(PREF_KEY_ROOT_DIR)) {
                    mRootDir = sharedPreferences.getString(key, defaultRootDir);
                    listRootDirectory();
                } else if (key.equals(PREF_KEY_FLASH_AIR_NAME)) {
                    mFlashAirName = sharedPreferences.getString(key, defaultFlashAirName);
                    listRootDirectory();
                }
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(mPrefListener);
    }

    private void readPref() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mFlashAirName = prefs.getString(PREF_KEY_FLASH_AIR_NAME, defaultFlashAirName);
        mRootDir = prefs.getString(PREF_KEY_ROOT_DIR, defaultRootDir);
        if (!Patterns.IP_ADDRESS.matcher(mFlashAirName).matches()) {
            mFlashAirName = defaultFlashAirName;
        }
        Log.i("FlashAirName = ", mFlashAirName);
        Log.i("RootDir = ", mRootDir);
    }

    private void setupGridButton() {
        mGridButton = (Button) findViewById(R.id.button_grid);
        mGridButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gridViewIntent = new Intent(MainActivity.this, GridViewActivity.class);

                SimpleAdapter adapter = MainActivity.this.mListAdapter;
                if (adapter != null) {
                    gridViewIntent.putExtra("FlashAirName", mFlashAirName);
                    gridViewIntent.putExtra("DirectoryName", mDirectoryName);
                    gridViewIntent.putExtra("ImageItems", mImageItems);
                }
                MainActivity.this.startActivity(gridViewIntent);
            }
        });
    }

    private void updateCachedThumbnail(FileItem item){
        String filename = item.filename;
        File cachedFile = new File(getCacheDir(), filename);
        boolean isNeedUpdate = false;
        if (! cachedFile.exists()){
            isNeedUpdate = true;
        } else {
            long d1 = cachedFile.lastModified();
            long d2 = item.date.getTimeInMillis();
            if (d1 < d2){
                isNeedUpdate = true;
            }
        }
        if (isNeedUpdate){
            String filePath = mDirectoryName + "/" + filename;
            byte[] thumbnail = FlashAirRequest.getThumbnail(mFlashAirName, filePath);
            if (thumbnail != null) {
                try {
                    OutputStream os = new FileOutputStream(cachedFile.getPath());
                    os.write(thumbnail);
                    os.close();
                } catch (IOException e) {
                    Log.i("Exception", e.toString());
                }
            }

        }
    }

    class CustomViewBinder implements SimpleAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            if (view instanceof ImageView) {
                if (view.getId() == android.R.id.icon) {
                    ImageView imageView = (ImageView) view;
                    String filename = (String) data;
                    if (FileItem.isJpeg(filename)) {
                        try {
                            File file = new File(getCacheDir(), filename);
                            InputStream is = new FileInputStream(file);
                            Bitmap thumbnail = BitmapFactory.decodeStream(is);
                            if (thumbnail != null) {
                                imageView.setImageBitmap(thumbnail);
                            } else {
                                imageView.setImageResource(R.drawable.ic_launcher);
                            }
                        } catch (IOException e) {
                            Log.i("IOException", e.toString());
                            imageView.setImageResource(R.drawable.ic_launcher);
                        }
                    }
                    return true;
                }
            }
            return false;
        }
    }
}

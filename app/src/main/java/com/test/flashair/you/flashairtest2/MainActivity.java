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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity
        extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    ListView listView;
    Button gridButton;
    TextView currentDirText;
    TextView numFilesText;

    String flashAirName;
    String defaultFlashAirName = "192.168.0.255";
    String rootDir = "DCIM/101NCD90";
    String defaultRootDir = "DCIM/101NCD90";

    String directoryName = rootDir;
    SimpleAdapter listAdapter;

    int checkInterval = 5000;
    Handler updateHandler;
    boolean viewingList;
    SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    TreeMap<String, FileItem> imageItems;

    private static final String PREF_KEY_FLASH_AIR_NAME = "FlashAirName";
    private static final String PREF_KEY_ROOT_DIR = "RootDir";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] perms = {
                "android.permission.INTERNET",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_EXTERNAK_STORAGE",
                //"android.permission.ACCESS_NETWORK_STATE",
                //"android.permission.ACCESS_WIFI_STATE"
        };
        int permsRequestCode = 200;
        //requestPermissions(perms, permsRequestCode);
        setContentView(R.layout.activity_main);
        Toolbar main_toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(main_toolbar);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key == PREF_KEY_ROOT_DIR) {
                    String changed = sharedPreferences.getString(key, defaultRootDir);
                    rootDir = changed;
                    listRootDirectory();
                } else if (key == PREF_KEY_FLASH_AIR_NAME) {
                    String changed = sharedPreferences.getString(key, defaultFlashAirName);
                    flashAirName = changed;
                    listRootDirectory();
                }
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(prefListener);
        flashAirName = prefs.getString(PREF_KEY_FLASH_AIR_NAME, defaultFlashAirName);
        rootDir = prefs.getString(PREF_KEY_ROOT_DIR, defaultRootDir);

        Log.i("FlashAirName = ", flashAirName);
        Log.i("RootDir = ", rootDir);

        viewingList = true;
        imageItems = FileItem.createMap();

        boolean b = Patterns.IP_ADDRESS.matcher(flashAirName).matches();

        try {
            gridButton = (Button) findViewById(R.id.button_grid);
            gridButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("onClick", "GridButton");
                    Intent gridViewIntent = new Intent(MainActivity.this, GridViewActivity.class);

                    SimpleAdapter adapter = MainActivity.this.listAdapter;
                    if (adapter != null) {
                        ArrayList<String> filenames = new ArrayList<>();
                        for (int i = 0; i < adapter.getCount(); i++) {
                            Map<String, Object> item = (Map<String, Object>) adapter.getItem(i);
                            String filename = item.get("fname").toString();
                            if (FileItem.isJpeg(filename)) {
                                filenames.add(filename);
                            }
                        }
                        String[] fnames = filenames.toArray(new String[filenames.size()]);
                        gridViewIntent.putExtra("filenames", fnames);
                        gridViewIntent.putExtra("FlashAirName", flashAirName);
                        gridViewIntent.putExtra("DirectoryName", directoryName);
                        gridViewIntent.putExtra("ImageItems", imageItems);
                    }
                    MainActivity.this.startActivity(gridViewIntent);
                }
            });
            listRootDirectory();
        } catch (Exception e) {
            Log.e("ERROR", "ERROR in CODE:" + e.toString());
            e.printStackTrace();
        }
        updateHandler = new Handler();

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
        viewingList = hasFocus;
    }

    public boolean checkIfListView() {
        return viewingList;
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
                            listDirectory(directoryName);
                        }
                    }
                }.execute("http://" + flashAirName + "/command.cgi?op=102");
            }
            updateHandler.postDelayed(statusChecker, checkInterval);
        }
    };

    public void startUpdate() {
        statusChecker.run();
    }

    public void stopUpdate() {
        updateHandler.removeCallbacks(statusChecker);
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
            Object downloadFile = mapItem.get("fname");
            if (downloadFile.toString().endsWith("/")) {
                String dirName = downloadFile.toString().substring(0, downloadFile.toString().length() - 1);
                directoryName = directoryName + "/" + dirName;
                listDirectory(directoryName);
            } else if (FileItem.isJpeg(downloadFile.toString())) {
                Intent viewImageIntent = new Intent(this, ImageViewActivity.class);
                viewImageIntent.putExtra("flashAirName", flashAirName);
                viewImageIntent.putExtra("downloadFile", downloadFile.toString());
                viewImageIntent.putExtra("directoryName", directoryName);
                viewImageIntent.putExtra("ImageItem", imageItems.get(downloadFile.toString()));
                MainActivity.this.startActivity(viewImageIntent);
            }
        }
    }

    public void listRootDirectory() {
        directoryName = rootDir;
        listDirectory(directoryName);
    }

    public void listDirectory(String dir) {
        currentDirText = (TextView) findViewById(R.id.textView1);
        currentDirText.setText(dir + "/");
        dir = "/" + dir;
        numFilesText = (TextView) findViewById(R.id.textView2);

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String dir = params[0];
                return FlashAirRequest.getString("http://" + flashAirName + "/command.cgi?op=101&DIR=" + dir);
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
                String files = FlashAirRequest.getString("http://" + flashAirName + "/command.cgi?op=100&DIR=" + dir);
                FileItem.parse(imageItems, files);

                ArrayList<Map<String, Object>> data = new ArrayList<>();

                for (FileItem item : imageItems.values()) {
                    String filename = item.filename;
                    if (FileItem.isRaw(filename)) {
                        continue;
                    }
                    String command = "http://" + flashAirName + "/thumbnail.cgi?" + directoryName + "/" + filename;
                    Map<String, Object> entry = new HashMap<>();

                    if (FileItem.isJpeg(filename)) {
                        byte[] thumbnail = FlashAirRequest.getBitmapByteArray(command);
                        if (thumbnail != null) {
                            File file = new File(getCacheDir(), filename);
                            try {
                                OutputStream os = new FileOutputStream(file);
                                os.write(thumbnail);
                                os.close();
                            } catch (IOException e) {
                                Log.i("Exception", e.toString());
                            }
                        }
                    }
                    entry.put("filename", filename);
                    entry.put("fname", filename);
                    data.add(entry);
                }
                listAdapter = new SimpleAdapter(
                        MainActivity.this,
                        data,
                        android.R.layout.activity_list_item,
                        new String[]{"filename", "fname"},
                        new int[]{android.R.id.icon, android.R.id.text1});
                listAdapter.setViewBinder(new CustomViewBinder());
                return listAdapter;
            }

            @Override
            protected void onPostExecute(ListAdapter listAdapter) {
                listView = (ListView) findViewById(R.id.listView1);
                ColorDrawable divcolor = new ColorDrawable(Color.rgb(17, 19, 58));
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

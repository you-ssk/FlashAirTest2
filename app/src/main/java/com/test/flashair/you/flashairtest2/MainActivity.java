package com.test.flashair.you.flashairtest2;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ListView listView;
    Button backButton;
    TextView currentDirText;
    TextView numFilesText;

    String rootDir = "DCIM/101NCD90";
    String directoryName = rootDir;

    ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        Object downloadFile = l.getItemAtPosition(position);
        Log.e("SASAKI", downloadFile.toString());
        if (downloadFile.toString().endsWith("/")){

        } else if (downloadFile.toString().toLowerCase(Locale.getDefault()).endsWith(".jpg")) {
            Intent viewImageIntent = new Intent(this, ImageViewActivity.class);
            viewImageIntent.putExtra("downloadFile", downloadFile.toString());
            viewImageIntent.putExtra("directoryName", directoryName);
            MainActivity.this.startActivity(viewImageIntent);
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
                Log.i("INFO:", dir);
                String fileCount = FlashAirRequest.getString("http://192.168.0.11/command.cgi?op=101&DIR=" + dir);
                return fileCount;
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
                String files = FlashAirRequest.getString("http://192.168.0.11/command.cgi?op=100&DIR=" + dir);
                String[] allFiles = files.split("([,\n])");
                for (int i = 2; i < allFiles.length; i=i+6){
                    if (allFiles[i].contains(".")) {
                        fileNames.add(allFiles[i]);
                    } else {
                        fileNames.add(allFiles[i] + "/");
                    }
                }
                listAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, fileNames);
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
}

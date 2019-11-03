package edu.wmdd.lab8;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getName();
    private RentalDBHelper helper = null;
    private SQLiteDatabase db = null;

    private ListView urlsListView;
    static ArrayList<UrlItem> itemsArrayList = new ArrayList<UrlItem>();
    private UrlListAdapter urlListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the database, potentially creating it
        helper = new RentalDBHelper(this);
        db = helper.getReadableDatabase();

        // Only populate the db if it is empty
        Cursor c = db.rawQuery("SELECT count(*) FROM issues", null);
        c.moveToFirst();
        if (c.getInt(0) == 0) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    // We have to init the data in a separate thread because of networking
                    helper.initData();

                    // We are now ready to initialize the view on the UI thread
                    runOnUiThread(() -> {
                        initView();
                    });
                }
            };
            t.start();
        } else {
            // We are already inside the UI thread
            initView();
        }
        c.close();
    }

    private void initView() {
        urlsListView = findViewById(R.id.urlsListView);
        urlsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3)
            {
                UrlItem item = (UrlItem) adapter.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("url", item.url);

                Log.d(TAG, item.url);
                startActivity(intent);
            }
        });
        //urlListAdapter = new UrlListAdapter(this, itemsArrayList);
        //urlsListView.setAdapter(urlListAdapter);

        Spinner spinner = findViewById(R.id.spinnerTextView);
        ArrayList<String> areas = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT DISTINCT(area) FROM issues", null);
        while (cursor.moveToNext()) {
            String area = cursor.getString(0);
            areas.add(area);
        }
        cursor.close();
        ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, areas);

        spinner.setAdapter(areaAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedArea = ((TextView) view).getText().toString();
                Cursor cursor1 = db.rawQuery("SELECT operator, businessURL FROM issues WHERE area = ?", new String[]{selectedArea});
                //ArrayList<String> operators = new ArrayList<>();

                itemsArrayList.clear();
                while (cursor1.moveToNext()) {
                    String operator = cursor1.getString(0);
                    String businessURL = cursor1.getString(1);
                    //operators.add(operator);

                    UrlItem item = new UrlItem();
                    item.url = businessURL.replace("http", "https");
                    item.operator = operator;
                    itemsArrayList.add(item);
                }

                urlListAdapter = new UrlListAdapter(MainActivity.this, itemsArrayList);
                urlsListView.setAdapter(urlListAdapter);

                //TextView textView = findViewById(R.id.textView);
                //textView.setText(operators.stream().reduce("", (s, s2) -> s + "\n" + s2));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

}

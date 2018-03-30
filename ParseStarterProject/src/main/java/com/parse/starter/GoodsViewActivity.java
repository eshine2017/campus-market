package com.parse.starter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoodsViewActivity extends AppCompatActivity {

    ListView goodsListView;
    boolean isRequestSelected;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.menu_goods_view, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // init sale activity                  n
        if (item.getItemId() == R.id.saleItem) {
            Log.i("Info", "add a new sale item");
            Intent intent = new Intent(getApplicationContext(), EditGoodsInfoActivity.class);
            intent.putExtra("saleOrRequest", "sale");
            startActivity(intent);
        }

        // init request activity
        else if (item.getItemId() == R.id.requestItem) {
            Log.i("Info", "request some item");
            Intent intent = new Intent(getApplicationContext(), EditGoodsInfoActivity.class);
            intent.putExtra("saleOrRequest", "request");
            startActivity(intent);
        }

        // view my sale or request
        else if (item.getItemId() == R.id.viewMySaleRequest) {
            Log.i("Info", "view my sale or request");
            Intent intent = new Intent(getApplicationContext(), MySaleRequestActivity.class);
            startActivity(intent);
        }

        // view my chat history
        else if (item.getItemId() == R.id.viewChat) {
            Log.i("Info", "view my chat history");
            Intent intent = new Intent(getApplicationContext(), ChatHistoryActivity.class);
            startActivity(intent);
        }

        // logout
        else if (item.getItemId() == R.id.logout) {
            ParseUser.logOut();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /* update the listing items */
    private void updateGoodsList() {

        ParseQuery<ParseObject> query; // query for Sale or Request
        if (!isRequestSelected) query = ParseQuery.getQuery("Sale");
        else query = ParseQuery.getQuery("Request");

        //query.whereContainedIn("username", ParseUser.getCurrentUser().getList("isFollowing"));
        query.orderByDescending("createdAt");
        query.setLimit(20);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {

                    // add item information to HashMap
                    final List<Map<String, String>> goodsInfo = new ArrayList<>();
                    for (ParseObject item : objects) {
                        Map<String, String> itemInfo = new HashMap<>();
                        itemInfo.put("title", item.getString("title") + " -- $" + item.getString("price"));
                        String description = item.getString("description");
                        if (description.length() > 80) description =
                                description.substring(0, description.indexOf(' ', 80)) + "...";
                        itemInfo.put("description", description);
                        itemInfo.put("objectId", item.getObjectId());
                        goodsInfo.add(itemInfo);
                    }

                    // set array adapter
                    SimpleAdapter simpleAdapter = new SimpleAdapter(GoodsViewActivity.this,
                            goodsInfo, android.R.layout.simple_list_item_2,
                            new String[] {"title", "description"},
                            new int[] {android.R.id.text1, android.R.id.text2});
                    goodsListView.setAdapter(simpleAdapter);

                    // set listener for detailed information page
                    goodsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                            Toast.makeText(GoodsViewActivity.this, goodsInfo.get(i).get("objectId"), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), GoodsDetailActivity.class);
                            intent.putExtra("objectId", goodsInfo.get(i).get("objectId"));
                            if (!isRequestSelected) intent.putExtra("saleOrRequest", "sale");
                            else intent.putExtra("saleOrRequest", "request");
                            startActivity(intent);
                        }
                    });
                }
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods_view);

        setTitle("View the Market");

        // add some sample items
//        ParseObject saleItem = new ParseObject("Sale");
//        saleItem.put("title", "Iphone X 256G silver");
//        saleItem.put("description", "The phone is in perfect condition, just $800");
//        saleItem.saveInBackground();
//
//        ParseObject requestItem = new ParseObject("Request");
//        requestItem.put("title", "PS4 1T");
//        requestItem.put("description", "I need a well performed PS4. White is prefer and around $300");
//        requestItem.saveInBackground();

        // set up switch to show sale item or request item
        Switch mySwitch = (Switch) findViewById(R.id.saleRequestSwitch);
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isRequestSelected = b;
                updateGoodsList();
            }
        });

        // init goods list
        goodsListView = (ListView) findViewById(R.id.goodsListView);
        isRequestSelected = false;
        updateGoodsList();
    }
}

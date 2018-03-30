package com.parse.starter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySaleRequestActivity extends AppCompatActivity {

    ListView goodsListView;
    boolean isRequestSelected;

    /* update the listing items */
    private void updateGoodsList() {

        ParseQuery<ParseObject> query; // query for Sale or Request
        if (!isRequestSelected) query = ParseQuery.getQuery("Sale");
        else query = ParseQuery.getQuery("Request");

        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
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
                    SimpleAdapter simpleAdapter = new SimpleAdapter(MySaleRequestActivity.this,
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

        setTitle("My Sale/Request");

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

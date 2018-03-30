package com.parse.starter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ChatHistoryActivity extends AppCompatActivity {

    ArrayList<String> users = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        setTitle("My Chat History");

        ListView userListView = (ListView) findViewById(R.id.chatUsersListView);

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);

                intent.putExtra("username", users.get(i));

                startActivity(intent);

            }
        });

        users.clear();

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, users);

        userListView.setAdapter(arrayAdapter);

        // search for both sender and recipient
        ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Message");
        query1.whereEqualTo("sender", ParseUser.getCurrentUser().getUsername());

        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Message");
        query2.whereEqualTo("recipient", ParseUser.getCurrentUser().getUsername());

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(query1);
        queries.add(query2);
        ParseQuery<ParseObject> query = ParseQuery.or(queries);
        query.orderByDescending("createdAt");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject object : objects) {
                        String username = object.getString("sender");
                        if (username.equals(ParseUser.getCurrentUser().getUsername()))
                            username = object.getString("recipient");
                        if (!users.contains(username)) users.add(username);
                    }

                    arrayAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}

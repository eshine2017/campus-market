package com.parse.starter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class GoodsDetailActivity extends AppCompatActivity {

    String username;

    // start a chat activity with saler or requester
    public void makeChat(View view) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    // view the selected location
    public void viewLocation(View view) {
        final Intent intent = getIntent();
        ParseQuery<ParseObject> query;
        if (intent.getStringExtra("saleOrRequest").equals("sale")) {
            Log.i("Info", "view a sale item");
            query = ParseQuery.getQuery("Sale");
        } else {
            Log.i("Info", "view a request item");
            query = ParseQuery.getQuery("Request");
        }

        query.whereEqualTo("objectId", intent.getStringExtra("objectId"));
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() != 0) {
                        ParseObject item = objects.get(0);
                        String address = item.getString("address");
                        ParseGeoPoint geoPoint = item.getParseGeoPoint("location");
                        Location location = new Location(LocationManager.GPS_PROVIDER);
                        location.setLatitude(geoPoint.getLatitude());
                        location.setLongitude(geoPoint.getLongitude());

                        // view location in new activity
                        Intent intent1 = new Intent(getApplicationContext(), ViewLocationActivity.class);
                        intent1.putExtra("address", address);
                        intent1.putExtra("location", location);
                        startActivity(intent1);
                    }
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods_detail);

        // first download item info from Parse server
        final Intent intent = getIntent();
        ParseQuery<ParseObject> query;
        if (intent.getStringExtra("saleOrRequest").equals("sale")) {
            Log.i("Info", "view a sale item");
            query = ParseQuery.getQuery("Sale");
        } else {
            Log.i("Info", "view a request item");
            query = ParseQuery.getQuery("Request");
        }

        query.whereEqualTo("objectId", intent.getStringExtra("objectId"));
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() != 0) {
                        ParseObject item = objects.get(0);
                        TextView titleTextView = findViewById(R.id.titleTextView);
                        titleTextView.setText(item.getString("title"));
                        setTitle(item.getString("title"));

                        TextView priceTextView = findViewById(R.id.priceTextView);
                        priceTextView.setText("$" + item.getString("price"));

                        TextView descriptionTextView = findViewById(R.id.descriptionTextView);
                        descriptionTextView.setText(item.getString("description"));

                        ParseFile file = (ParseFile) item.get("picture");
                        file.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] data, ParseException e) {
                                if (e == null && data != null) {
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                    ImageView imageView = findViewById(R.id.imageView);
                                    imageView.setImageBitmap(bitmap);
                                }
                            }
                        });

                        if (item.getString("username").equals(ParseUser.getCurrentUser().getUsername())) {
                            Button chatEditButton = findViewById(R.id.chatEditButton);
                            chatEditButton.setVisibility(View.INVISIBLE);
                        }

                        username = item.getString("username");
                    }
                } else {
                    Log.i("Warning", "cannot find this item");
                }
            }
        });
    }
}

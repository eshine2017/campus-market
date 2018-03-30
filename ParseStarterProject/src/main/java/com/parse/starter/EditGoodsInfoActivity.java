package com.parse.starter;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditGoodsInfoActivity extends AppCompatActivity {

    String saleOrRequest;

    Bitmap bitmap;
    String address;
    Location location;

    // select a preferred trading location from google map api
    public void setLocation(View view) {
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivityForResult(intent, 2);
    }

    // select a picture from gallery
    public void uploadPicture(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                getPhoto();
            }

        } else {
            getPhoto();
        }
    }

    public void getPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getPhoto();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();

            try {

                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                ImageView itemImageView = (ImageView) findViewById(R.id.imageView);
                itemImageView.setImageBitmap(bitmap);

                // reset image height to 100 dp
                float scale = getResources().getDisplayMetrics().density;
                itemImageView.getLayoutParams().height = (int) (100*scale);
                itemImageView.setVisibility(View.VISIBLE);
                Log.i("Info", "got the picture");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            address = data.getStringExtra("address");
            Log.i("Info", address);
            location = data.getParcelableExtra("location");
            Log.i("Info", location.toString());
            ImageView locationImageView = (ImageView) findViewById(R.id.locationImageView);
            locationImageView.setVisibility(View.VISIBLE);
        }
    }

    // upload new sale or request to parse server
    public void submitGoodsInfo(View view) {

        EditText titleEditText = (EditText) findViewById(R.id.titleEditText);
        String title = titleEditText.getText().toString();
        if (title.equals("")) {
            alertIncompleteInfo("No title inputted");
            return;
        }

        EditText priceEditText = (EditText) findViewById(R.id.priceEditText);
        String price = priceEditText.getText().toString();
        if (price.equals("")) {
            alertIncompleteInfo("No price inputted");
            return;
        }

        EditText descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        String description = descriptionEditText.getText().toString();
        if (description.equals("")) {
            alertIncompleteInfo("No description inputted");
            return;
        }

        if (bitmap == null) {
            alertIncompleteInfo("No picture uploaded");
            return;
        }

        if (address == null) {
            alertIncompleteInfo("No location selected");
            return;
        }

        // send item info to parse server
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        ParseFile file = new ParseFile("image.png", byteArray);

        ParseObject object;
        if (saleOrRequest.equals("sale")) {
            object = new ParseObject("Sale");
        } else {
            object = new ParseObject("Request");
        }
        object.put("username", ParseUser.getCurrentUser().getUsername());
        object.put("title", title);
        object.put("price", price);
        object.put("description", description);
        object.put("picture", file);
        object.put("address", address);
        ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        object.put("location", geoPoint);

        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(EditGoodsInfoActivity.this, "Submit successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), GoodsViewActivity.class);
                    startActivity(intent);
                }
                else
                    Toast.makeText(EditGoodsInfoActivity.this, "Submit failed, please try again later.",
                            Toast.LENGTH_SHORT).show();
            }
        });

        //Log.i("Info", "new sale/request submitted");
    }

    // alert user if information is not complete
    private void alertIncompleteInfo(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(message);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    // go back to goods view if canceled
    public void cancelEditGoodsInfo(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_goods_info);

        Intent intent = getIntent();
        saleOrRequest = intent.getStringExtra("saleOrRequest");
        if (saleOrRequest.equals("sale")) {
            setTitle("Start a Sale");
        } else {
            setTitle("Start a Request");
        }
    }
}

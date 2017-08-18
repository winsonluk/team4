package com.example.split;

import android.content.Intent;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.join_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), JoinActivity.class));
            }
        });

        findViewById(R.id.create_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            try {
                JSONArray arr = new JSONArray();
                JSONObject obj = new JSONObject();
                obj.put("id", 1);
                obj.put("name", "1/2 Dozen Assorted");
                obj.put("cost", 8.69);
                arr.put(obj);

                JSONObject obj1 = new JSONObject();
                obj1.put("id", 2);
                obj1.put("name", "Chocolate w/Sprinkles");
                obj1.put("cost", 4.77);
                arr.put(obj1);

                JSONObject obj2 = new JSONObject();
                obj2.put("id", 3);
                obj2.put("name", "Whole Milk");
                obj2.put("cost", 1.99);
                arr.put(obj2);

                JSONObject obj3 = new JSONObject();
                obj3.put("id", 4);
                obj3.put("name", "Chocolate Milk");
                obj3.put("cost", 1.99);
                arr.put(obj3);


                //Snackbar.make().show();
                new PostReceiptDataTask().execute(arr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void onTransactionCreated(int id) {
        Intent intent = new Intent(this, ClaimActivity.class);
        intent.putExtra("TRANSACTION_ID", id);
        startActivity(intent);
    }

    private class PostReceiptDataTask extends AsyncTask<JSONArray, Void, Integer>
    {
        @Override
        protected Integer doInBackground(JSONArray... params) {
            try {
                URL url = new URL("http://notlessthan.herokuapp.com/transaction");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.connect();

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(params[0].toString());
                writer.close();
                os.close();

                InputStream is = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                StringBuilder builder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();

                JSONObject response = new JSONObject(builder.toString());
                int transactionId = response.getInt("transactionId");
                return transactionId;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer id) {
            onTransactionCreated(id);
            new PostNewPersonTask().execute(id);
        }
    }

    private class PostNewPersonTask extends AsyncTask<Integer, Void, Integer>
    {
        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                URL url = new URL("http://notlessthan.herokuapp.com/" + params[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.connect();

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write("id=abc123&name=Elan Hamburger");
                writer.close();
                os.close();

                InputStream is = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                StringBuilder builder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();

                return params[0];
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0;
        }
    }
}

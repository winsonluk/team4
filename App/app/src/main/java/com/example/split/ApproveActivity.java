package com.example.split;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;

public class ApproveActivity extends AppCompatActivity {

    int transactionId;
    ArrayList<Item> itemList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve);

        itemList = new ArrayList<Item>();

        transactionId = getIntent().getIntExtra("TRANSACTION_ID", 0);
        findViewById(R.id.approve_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ApproveTask().execute();
            }
        });

        new GetReceiptDataTask().execute();
    }

    private void onApproved() {
        Button button = (Button) findViewById(R.id.approve_button);
        button.setText("APPROVED!");

        startActivity(new Intent(this, MainActivity.class));
    }

    private void onReceiptGet(double total) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        TextView tv = (TextView) findViewById(R.id.payment_textview);
        tv.setText(formatter.format(total));
    }

    private class GetReceiptDataTask extends AsyncTask<Void, Void, Double> {
        @Override
        protected Double doInBackground(Void... params) {
            try {
                URL url = new URL("http://notlessthan.herokuapp.com/" + transactionId);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream is = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                StringBuilder builder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();
                JSONObject response = new JSONObject(builder.toString());
                double total = 0.0;
                for (int i = 0; i < response.getJSONArray("items").length(); i++) {
                    if (response.getJSONArray("items").getJSONObject(i).getString("payer").equals("abc123")) {
                        total += response.getJSONArray("items").getJSONObject(i).getDouble("cost");
                    }
                }

                return total;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0.0;
        }
        @Override
        protected void onPostExecute(Double total) {
            onReceiptGet(total);
        }
    }

    private class ApproveTask extends AsyncTask<Integer, Void, Integer>
    {
        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                URL url = new URL("http://notlessthan.herokuapp.com/" + params[0] + "/approve");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.connect();

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write("id=abc123");
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

        @Override
        protected void onPostExecute(Integer id) {
            onApproved();
        }
    }

}

package com.example.split;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

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
import java.text.NumberFormat;
import java.util.ArrayList;

public class ClaimActivity extends AppCompatActivity {

    MyCustomAdapter dataAdapter;
    private ArrayList<Item> itemList;
    CountDownTimer timer;
    int transactionId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim);

        itemList = new ArrayList<>();
        dataAdapter = new MyCustomAdapter(this, R.layout.item_layout, itemList);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(dataAdapter);

        transactionId = getIntent().getIntExtra("TRANSACTION_ID", 0);
        setTitle("Transaction #" + transactionId);
        findViewById(R.id.submit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SubmitTask().execute(transactionId);
                Button b = (Button) view;
                b.setText("WAITING FOR SUBMISSIONS...");
            }
        });

        new GetReceiptDataTask().execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (timer != null) {
            timer.cancel();
        }
    }

    private void onReceiptGet(int id) {
        if (id == 0) {
            dataAdapter.notifyDataSetChanged();
            timer = new CountDownTimer(5000, 5000) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    new GetReceiptDataTask().execute();
                }
            }.start();
        } else if (id == 1) {
            Intent intent = new Intent(this, ApproveActivity.class);
            intent.putExtra("TRANSACTION_ID", transactionId);
            startActivity(intent);
        }
    }

    private class GetReceiptDataTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
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
                itemList.clear();
                JSONObject response = new JSONObject(builder.toString());
                if (response.getBoolean("submitted")) {
                    return 1;
                } else {
                    NumberFormat formatter = NumberFormat.getCurrencyInstance();
                    for (int i = 0; i < response.getJSONArray("items").length(); i++) {
                        Item item = new Item(
                                formatter.format(Double.parseDouble(response.getJSONArray("items").getJSONObject(i).getString("cost"))),
                                response.getJSONArray("items").getJSONObject(i).getString("name"),
                                response.getJSONArray("items").getJSONObject(i).getInt("id"),
                                !response.getJSONArray("items").getJSONObject(i).isNull("payer"));
                        itemList.add(item);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }
        @Override
        protected void onPostExecute(Integer id) {
            onReceiptGet(id);
        }
    }

    private class PostClaimTask extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                URL url = new URL("http://notlessthan.herokuapp.com/" + transactionId + "/claim");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.connect();
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                String paramString = "id=" + params[0] + "&personId=" + "abc123";
                writer.write(paramString);
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
                return 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    private class SubmitTask extends AsyncTask<Integer, Void, Integer>
    {
        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                URL url = new URL("http://notlessthan.herokuapp.com/" + params[0] + "/submit");
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
    }

    private class MyCustomAdapter extends ArrayAdapter<Item> {

        public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<Item> itemList) {
            super(context, textViewResourceId, itemList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Item item = itemList.get(position);

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.item_layout, null);
            }

            CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkBox);
            cb.setTag(item);

            cb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    Item item = (Item) cb.getTag();
                    item.paid = cb.isChecked();
                    new PostClaimTask().execute(item.id);
                }
            });

            cb.setText(item.name);
            cb.setChecked(item.paid);

            TextView tv = (TextView) convertView.findViewById(R.id.name);
            tv.setText(" (" +  item.cost + ")");

            return convertView;
        }
    }
}

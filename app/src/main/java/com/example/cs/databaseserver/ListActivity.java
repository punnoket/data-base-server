package com.example.cs.databaseserver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.StrictMode;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class ListActivity extends AppCompatActivity implements OnItemClickListener {
    private DecimalFormat formatter;
    private String url;
    private ListView lisView;
    private ArrayList<HashMap<String, String>> dataHashMaps;
    private AlertDialog.Builder viewDetail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        lisView = (ListView) findViewById(R.id.listView);
        url = "http://www.lstpch.com/android/getData.php";
        formatter = new DecimalFormat("#,###.##");
        viewDetail = new AlertDialog.Builder(this);
        dataHashMaps = new ArrayList<HashMap<String, String>>();
        getData();
    }

    private void getData() {
        try {
            JSONArray data = new JSONArray(getJSONUrl(url));
            HashMap<String, String> map;
            for (int i = 0; i < data.length(); i++) {
                JSONObject c = data.getJSONObject(i);
                map = new HashMap<String, String>();
                map.put("trans_id", c.getString("trans_id"));
                map.put("name", c.getString("name"));
                map.put("msg", c.getString("msg"));
                map.put("amt", c.getString("amt"));
                map.put("note", c.getString("note"));
                dataHashMaps.add(map);
            }

            SimpleAdapter sAdap;
            sAdap = new SimpleAdapter(ListActivity.this, dataHashMaps, R.layout.activity_column,
                    new String[]{"trans_id", "name", "msg", "amt", "note"}, new int[]{R.id.col_trans_id, R.id.col_name, R.id.col_msg, R.id.col_amt, R.id.col_note});
            lisView.setAdapter(sAdap);
            lisView.setOnItemClickListener(this);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getJSONUrl(String url) {
        StringBuilder str = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) { // Download OK
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content, "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    str.append(line);
                }
            } else {
                Log.e("Log", "Failed to download result..");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str.toString();
    }

    private void showDialog(HashMap<String, String> dataHashMaps) {
        String sTransID = dataHashMaps.get("trans_id");
        String sName = dataHashMaps.get("name");
        String sMsg = dataHashMaps.get("msg");
        String sAmt = dataHashMaps.get("amt");
        String sNote = dataHashMaps.get("note");
        viewDetail.setIcon(android.R.drawable.btn_star_big_on);
        viewDetail.setTitle("รายละเอียด");
        viewDetail.setMessage("เลขที่รายการ : " + sTransID + "\n"
                + "ชื่อ : " + sName + "\n" + "รายการ : " + sMsg + "\n"
                + "จำนวนเงิน : " + formatter.format(Double.parseDouble(sAmt)) + "\n"
                + "หมายเหตุ : " + sNote);
        viewDetail.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.dismiss();
                    }
                });
        viewDetail.show();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        showDialog(dataHashMaps.get(i));
    }
}

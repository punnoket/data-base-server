package com.example.cs.databaseserver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private EditText nameEditText;
    private EditText messageEditText;
    private EditText amountEditText;
    private AlertDialog.Builder dialog;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        setUI();
        setDialog();

        final ImageButton btn = (ImageButton) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (SaveData()) {
                    if (v != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        final EditText txtMsg = (EditText) findViewById(R.id.msg);
                        txtMsg.requestFocus();
                    }
                }
            }
        });
    }

    private void setUI() {
        nameEditText = (EditText) findViewById(R.id.name);
        messageEditText = (EditText) findViewById(R.id.msg);
        amountEditText = (EditText) findViewById(R.id.amt);

    }

    private void setDialog() {
        dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.err_title);
        dialog.setIcon(android.R.drawable.btn_star_big_on);
        dialog.setPositiveButton("Close", null);
    }

    public boolean SaveData() {
        List<NameValuePair> params = new ArrayList<>();
        String url = "http://www.lstpch.com/android/saveData.php";

        params.add(new BasicNameValuePair("sName", nameEditText.getText().toString()));
        params.add(new BasicNameValuePair("sMsg", messageEditText.getText().toString()));
        params.add(new BasicNameValuePair("sAmt", amountEditText.getText().toString()));


        if (nameEditText.getText().length() == 0) {
            dialog.setMessage(R.string.input_name);
            dialog.show();
            nameEditText.requestFocus();
            return false;
        }

        String resultServer = getHttpPost(url, params);
        String strStatusID = "0";
        String strError = "Unknow Status!";
        try {
            JSONObject responseJson = new JSONObject(resultServer);
            strStatusID = responseJson.getString("StatusID");
            strError = responseJson.getString("Error");
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Submission Error!", Toast.LENGTH_LONG).show();
        }

        if (strStatusID.equals("0")) {
            dialog.setMessage(strError);
            dialog.show();
            return false;
        } else {
            dialog.setTitle(R.string.submit_title);
            dialog.setMessage(R.string.submit_result);
            dialog.show();
            nameEditText.setText("");
            messageEditText.setText("");
            amountEditText.setText("");
            return true;
        }
    }

    public String getHttpPost(String url, List<NameValuePair> params) {
        StringBuilder str = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse response = client.execute(httpPost);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) { // Status OK
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_record:
                startActivity(new Intent(this, ListActivity.class));
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

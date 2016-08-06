package com.sandarumk.wikispeak;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button findButton = (Button) findViewById(R.id.find_button);
        EditText findEditText = (EditText) findViewById(R.id.find_edit_text);
        final String findString = findEditText.getText().toString();


        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vw) {
                Toast.makeText(getBaseContext(), "test" + findString, Toast.LENGTH_LONG).show();
                TextView textview = (TextView) findViewById(R.id.text_view);
                textview.setText(findString);
                new QueryWikipedia().execute(findString);
            }
        });
    }




    public class QueryWikipedia extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection httpURLConnection = null;
            StringBuffer stringBuffer = new StringBuffer("");
            String responseJSONString = null;
            BufferedReader bufferedReader = null;

            try {

                final String BASE_URL = "http://en.wikipedia.org/w/api.php?";
                final String QUERY_ACTION = "action";
                final String QUERY_FORMAT = "format";
                final String QUERY_PROP = "prop";
                final String QUERY_SECTION = "section";
                final String QUERY_PAGE = "page";
                final String QUERY_CALLBACK = "callback";

                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendQueryParameter(QUERY_ACTION, "parse")
                        .appendQueryParameter(QUERY_FORMAT, "json")
                        .appendQueryParameter(QUERY_PROP, "text")
                        .appendQueryParameter(QUERY_SECTION, "0")
                        .appendQueryParameter(QUERY_PAGE, "India")
                        .appendQueryParameter(QUERY_CALLBACK, "?").build();

                URL url;
                url = new URL(buildUri.toString());
                Log.d("WIKISPEAK",buildUri.toString());
                //final String BASE_URL = "http://en.wikipedia.org/w/api.php?action=parse&format=json&prop=text&section=0&page="+myString+"&callback=?"


                // create the HTTP connection and connect
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                // input stream from the connection should be assigned to the forcast Json string
                InputStream inputStream = httpURLConnection.getInputStream();
                if (inputStream == null) {
                    responseJSONString = null;
                }

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));


                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line + "\n");
                }

                if (stringBuffer.length() == 0) {
                    responseJSONString = null;
                }

                responseJSONString = stringBuffer.toString();
                Log.d("WIKITASK", responseJSONString);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException protocolex) {
                protocolex.printStackTrace();
                Log.e("WIKITASK", "onCreateView: Protocol Exception");
            } catch (IOException ioex) {
                ioex.printStackTrace();
                Log.e("WIKITASK", "onCreateView: IO Exception");
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException ioex) {
                        ioex.printStackTrace();
                        Log.e("WIKITASK", "onCreateView: io Exception");
                    }
                }
            }

                return responseJSONString;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result !=null){
                Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();

            }

        }
    }


}

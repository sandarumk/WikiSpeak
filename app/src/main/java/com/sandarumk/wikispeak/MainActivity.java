package com.sandarumk.wikispeak;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {


    private static final String TAG = "WIKITASK";

    private WebView webViewContent;
    private TextToSpeech tts;
    private boolean ttsInitialized;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button findButton = (Button) findViewById(R.id.find_button);
        EditText findEditText = (EditText) findViewById(R.id.find_edit_text);

        webViewContent = (WebView)findViewById(R.id.text_content);

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

        try {
            Intent checkIntent = new Intent();
            checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(checkIntent, 123);
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),"No application found for Text to speech",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && !ttsInitialized)
        {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
            {
                // success, create the TTS instance
                tts = new TextToSpeech(this, (TextToSpeech.OnInitListener) this);
                if (tts!=null)
                    ttsInitialized = true;
            }
            else
            {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                try {
                    startActivity(installIntent);
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(),"Unable to find Text To Speech Application",Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
//                    leftToRead = speakFull(res);
            int result = tts.setLanguage(Locale.UK);
            Log.d(TAG,"TTS init success");
//            ttsInitialized = true;
        }else{
            Log.d(TAG,"TTS init failed");
        }
        return;
    }

    public class QueryWikipedia extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection httpURLConnection = null;
            StringBuffer stringBuffer = new StringBuffer("");
            String content = null;
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

                String url = buildUri.toString();
                Log.d(TAG, "url =" + url);
                String data = getData(url);
                Log.d(TAG, "loaded data =" + data);


                //parse json
                if(data != null && data.length() > 0 ) {
                    data = data.replace("/**/(","");//remove initical characters
                    data = data.replace("\"//","https://");
                    JSONObject mainObject = new JSONObject(data);
                    content = mainObject.getJSONObject("parse").getJSONObject("text").getString("*");
                    Log.d(TAG, "wiki content =" + content);
                }

            } catch (IOException ioex) {
                ioex.printStackTrace();
                Log.e("WIKITASK", "onCreateView: IO Exception",ioex);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "JSON parsing error",e);
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

                return content;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result !=null){
//                Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();
                if(webViewContent != null){
                    webViewContent.loadData(result, "text/html", "utf-8");
                    if(ttsInitialized) {
                        try {
                            tts.setLanguage(Locale.US);
                            Document doc = Jsoup.parse(result);
                            Elements ps = doc.select("p");

                            String ttsText = "test";
                            if(ps.size() > 0){
                                ttsText = ps.get(0).text();
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                 tts.speak(ttsText, TextToSpeech.QUEUE_FLUSH, null, result.toString());
                            } else {
                                tts.speak(ttsText, TextToSpeech.QUEUE_FLUSH, null);
                            }
                        }catch (Exception e){
                            Log.e(TAG,"Unable to speech", e);
                        }
                    }
                }
            }

        }
    }

    OkHttpClient client = new OkHttpClient();


    public  String getData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }


}

package stocks.softified.com.stockalerts;

import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;

import stocks.softified.com.stockalerts.utils.AsyncResponse;
import stocks.softified.com.stockalerts.utils.Utils;

public class MainActivity extends AppCompatActivity{


    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT = 20000;
    public static final int READ_TIMEOUT = 25000;
    private SimpleCursorAdapter myAdapter;

    SearchView searchView = null;
    private String[] strArrData = {""};
    private String searchQuery = "";

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String[] fromArr = new String[] {"symbol"};
        final int[] to = new int[] {android.R.id.text1};

        // setup SimpleCursorAdapter
        myAdapter = new SimpleCursorAdapter(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, null, fromArr, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // adds item to action bar
        getMenuInflater().inflate(R.menu.search_main, menu);

        // Get Search item from action bar and Get Search service
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
            searchView.setIconified(false);
            searchView.setSuggestionsAdapter(myAdapter);
            // Getting selected (clicked) item suggestion
            searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                @Override
                public boolean onSuggestionClick(int position) {
                    // Add clicked text to search box
                    CursorAdapter ca = searchView.getSuggestionsAdapter();
                    Cursor cursor = ca.getCursor();
                    cursor.moveToPosition(position);
                    searchQuery = cursor.getString(cursor.getColumnIndex("symbol"));
                    searchQuery = searchQuery.substring(0, searchQuery.indexOf("("));
                    searchView.setQuery(searchQuery,false);

                    return true;
                }

                @Override
                public boolean onSuggestionSelect(int position) {
                    return true;
                }
            });
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return onQueryTextChange(s);
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    searchQuery = s;
                    if(searchQuery.length()>0) {
                        handler.removeCallbacksAndMessages(null);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Fetch data from mysql table using AsyncTask

                                new AsyncFetch().execute();

                            }
                        }, 350);
                    }

                    return false;
                }
            });
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }


    // Every time when you press search button on keypad an Activity is recreated which in turn calls this function
    @Override
    protected void onNewIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (searchView != null) {
                searchView.clearFocus();
            }

            // User entered text and pressed search button. Perform task ex: fetching data from database and display

        }
    }

    // Create class AsyncFetch
    private class AsyncFetch extends AsyncTask<String, String, String> {
        public AsyncResponse delegate = null;
//        ProgressDialog pdLoading = new ProgressDialog(MainActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            pdLoading.setMessage("\tLoading...");
//            pdLoading.setCancelable(false);
//            pdLoading.show();

        }

        @Override
        protected String doInBackground(String... params) {
            searchQuery = searchQuery.toUpperCase();

            String symbolsToSearch = Utils.getPermutations(searchQuery);
            if(symbolsToSearch!=null) {
                try {
                    // Enter URL address where your php file resides or your JSON file address
                    url = new URL("https://query.yahooapis.com/v1/public/yql?q=select%20symbol,Name%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%22" + symbolsToSearch + "%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=");
                    //TODO: this url works from browser. maybe because of headers and cookies.
                    //url = new URL("https://finance.yahoo.com/_finance_doubledown/api/resource/searchassist;gossipConfig=%7B%22url%22:%7B%22host%22:%22s.yimg.com%22,%22path%22:%22/xb/v6/finance/autocomplete%22,%22query%22:%7B%22appid%22:%22yahoo.com%22,%22nresults%22:10,%22output%22:%22yjsonp%22,%22region%22:%22US%22,%22lang%22:%22en-US%22%7D,%22protocol%22:%22https%22%7D,%22isJSONP%22:true,%22queryKey%22:%22query%22,%22resultAccessor%22:%22ResultSet.Result%22,%22suggestionTitleAccessor%22:%22symbol%22,%22suggestionMeta%22:[%22symbol%22,%22name%22,%22exch%22,%22type%22,%22exchDisp%22,%22typeDisp%22]%7D;searchTerm="+searchQuery);
                    //http://stockcharts.com/j-ci/ci?suggest=AMZN&limit=10&exchanges=any&_=1483097805063
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return null;
                }
                try {

                    // Setup HttpURLConnection class to send and receive data from php and mysql
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(READ_TIMEOUT);
                    conn.setConnectTimeout(CONNECTION_TIMEOUT);
                    conn.setRequestMethod("GET");

                    // setDoOutput to true as we receive data
                    conn.setDoOutput(true);
                    conn.connect();

                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return null;
                }

                try {

                    int response_code = conn.getResponseCode();

                    // Check if successful connection made
                    if (response_code == HttpURLConnection.HTTP_OK) {

                        // Read data sent from server
                        InputStream input = conn.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                        StringBuilder result = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }

                        // Pass data to onPostExecute method
                        return (result.toString());

                    } else {
                        return null;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    return e.toString();
                } finally {
                    conn.disconnect();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread
            Map<String, String> dataList = new HashMap();
//            pdLoading.dismiss();

            if(result == null) {

                // Do some action if no data from database

            }else{

                try {
                    /*
                    Log.i("RESULTS --> ",result);
                    JSONObject responseObj = new JSONObject(result);
                    JSONArray itemsArray = responseObj.getJSONArray("items");
                    for(int i=0; i<itemsArray.length(); i++){
                        JSONObject itemObj = itemsArray.getJSONObject(i);
                        if(itemObj.getString("type").equalsIgnoreCase("S") || itemObj.getString("type").equalsIgnoreCase("M")){
                            dataList.put(itemObj.getString("symbol"), itemObj.getString("name"));
                        }
                    }
                    */

                        try {
                            JSONObject responseObj = new JSONObject(result);
                            JSONObject queryObj = responseObj.getJSONObject("query");
                            JSONObject resultsObj = queryObj.getJSONObject("results");
                            JSONArray quoteArr = resultsObj.getJSONArray("quote");

                            for (int i = 0; i < quoteArr.length(); i++) {
                                JSONObject quoteObj = quoteArr.getJSONObject(i);
                                Log.i("Response: ", quoteObj.getString("symbol") + " - " + quoteObj.getString("Name"));
                                if (quoteObj.getString("Name") != null && !quoteObj.getString("Name").equalsIgnoreCase("null") && !quoteObj.getString("Name").equalsIgnoreCase(""))
                                    dataList.put(quoteObj.getString("symbol"), quoteObj.getString("Name"));
                            }
                        } catch (JSONException e) {
                            JSONObject responseObj = new JSONObject(result);
                            JSONObject queryObj = responseObj.getJSONObject("query");

                            JSONObject resultsObj = queryObj.getJSONObject("results");
                            JSONObject quoteObj = resultsObj.getJSONObject("quote");

                            Log.i("Response: ", quoteObj.getString("symbol") + " - " + quoteObj.getString("Name"));
                            if (quoteObj.getString("Name") != null && !quoteObj.getString("Name").equalsIgnoreCase("null") && !quoteObj.getString("Name").equalsIgnoreCase(""))
                                dataList.put(quoteObj.getString("symbol"), quoteObj.getString("Name"));
                        }

                        // Utils.logArray(strArrData);
                        // Filter data
                        final MatrixCursor mc = new MatrixCursor(new String[]{ BaseColumns._ID, "symbol" });
                        int i=1;
                        for(String symbol: dataList.keySet()){
                            if(symbol.equalsIgnoreCase(searchQuery)){
                                mc.addRow(new Object[] {0, symbol+"("+dataList.get(symbol)+")"});
                                break;
                            }
                        }
                        for(String symbol: dataList.keySet()){
                            if(!symbol.equalsIgnoreCase(searchQuery)){
                                mc.addRow(new Object[] {i, symbol+"("+dataList.get(symbol)+")"});
                            }
                            i++;
                        }
                        myAdapter.changeCursor(mc);
                        myAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    // You to understand what actually error is and handle it appropriately
                    e.printStackTrace();
                }

            }

        }



    }

}

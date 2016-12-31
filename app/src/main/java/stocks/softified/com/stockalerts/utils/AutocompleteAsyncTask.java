package stocks.softified.com.stockalerts.utils;

import android.content.Context;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by waqas on 12/31/16.
 */

public class AutocompleteAsyncTask extends AsyncTask<String, String, String> {

    private Context context;
    private String searchQuery;
    private SimpleCursorAdapter myAdapter;

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT = 20000;
    public static final int READ_TIMEOUT = 25000;

    public AsyncResponse delegate = null;

    HttpURLConnection conn;
    URL url = null;


    public AutocompleteAsyncTask(Context context, String searchQuery, SimpleCursorAdapter myAdapter){
        this.context = context;
        this.searchQuery = searchQuery;
        this.myAdapter = myAdapter;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        searchQuery = searchQuery.toUpperCase();
        //String symbolsToSearch = Utils.getPermutations(searchQuery);
        if(searchQuery!=null) {
            try {
                // Enter URL address where your php file resides or your JSON file address
                //url = new URL("https://query.yahooapis.com/v1/public/yql?q=select%20symbol,Name%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%22" + symbolsToSearch + "%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=");
                //TODO: this url works from browser. maybe because of headers and cookies.
                //url = new URL("https://finance.yahoo.com/_finance_doubledown/api/resource/searchassist;gossipConfig=%7B%22url%22:%7B%22host%22:%22s.yimg.com%22,%22path%22:%22/xb/v6/finance/autocomplete%22,%22query%22:%7B%22appid%22:%22yahoo.com%22,%22nresults%22:10,%22output%22:%22yjsonp%22,%22region%22:%22US%22,%22lang%22:%22en-US%22%7D,%22protocol%22:%22https%22%7D,%22isJSONP%22:true,%22queryKey%22:%22query%22,%22resultAccessor%22:%22ResultSet.Result%22,%22suggestionTitleAccessor%22:%22symbol%22,%22suggestionMeta%22:[%22symbol%22,%22name%22,%22exch%22,%22type%22,%22exchDisp%22,%22typeDisp%22]%7D;searchTerm="+searchQuery);
                url = new URL("http://stockcharts.com/j-ci/ci?suggest="+searchQuery+"&limit=10&exchanges=any&_=1483097805063");

                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");

                conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                conn.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
                conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Connection", "keep-alive");
                conn.setRequestProperty("Host", "stockcharts.com");
                conn.setRequestProperty("Pragma", "no-cache");
                conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36");


                // setDoOutput to true as we receive data
                conn.setDoOutput(true);
                conn.connect();

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
                return null;
            } finally {
                conn.disconnect();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        Map<String, String> dataList = new HashMap();
//            pdLoading.dismiss();

        if(result == null) {

            // Do some action if no data from database

        }else{

            try {

                Log.i("RESULTS --> ",result);
                JSONObject responseObj = new JSONObject(result);
                JSONArray itemsArray = responseObj.getJSONArray("companies");
                for(int i=0; i<itemsArray.length(); i++){
                    JSONObject itemObj = itemsArray.getJSONObject(i);
                    dataList.put(itemObj.getString("symbol"), itemObj.getString("name"));
                }

                /*

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
                    */
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

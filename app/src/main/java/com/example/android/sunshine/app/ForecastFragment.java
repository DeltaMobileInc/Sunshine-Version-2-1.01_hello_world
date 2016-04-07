package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ForecastFragment extends Fragment {

    ArrayAdapter<String> mforecast;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onStart() {
        super.onStart();

        updateweather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String[] forecastarray = {"today sunny 88/33", "tomorrow sunny 88/33", "wed sunny 88/33", "thurs sunny 88/33", "fri sunny 88/33", "sat sunny 88/33", "sun sunny 88/33"};

        List<String> weekforecast =  new ArrayList<String>(
                Arrays.asList(forecastarray)
        );

        //Setting the adapter for list
         mforecast = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                 weekforecast);


        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView mlistview = (ListView) rootView.findViewById(R.id.listView_forecast);
        mlistview.setAdapter(mforecast);

        mlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Toast.makeText(view.getContext(),"listiemclick" + parent.getItemAtPosition(position).toString(),Toast.LENGTH_SHORT).show();


                Intent intent = new Intent(getActivity().getApplicationContext(), DetailActivity.class);
                intent.putExtra("FORECAST", parent.getItemAtPosition(position).toString());
                startActivity(intent);
            }
        });


        return rootView;
    }
    /**update Weather function
     *
     */
    private void updateweather(){

        SharedPreferences locationSettings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String zip = locationSettings.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_defaultvalue));
        Toast.makeText(getActivity().getApplicationContext(),"prefvalue  " + zip,Toast.LENGTH_LONG).show();
        new FetchWeatherTask().execute(zip);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.forcast_fragment_menu, menu);

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateweather();
            return true;
        }


        return super.onOptionsItemSelected(item);


    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();


        /**
         * get HIGH/LOW Temp
         *
         * @param max
         * @param min
         * @return
         */
        private String getMaxMin(Double max, Double min) {


            SharedPreferences unitref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitType = unitref.getString(getString(R.string.pref_unit_key), getString(R.string.pref_unit_dialgoue_default_value));

            if(unitType.equals(getString(R.string.pref_unit_imperial)))
            {
                max = (max*1.8) +32;
                min = (min*1.8) +32;
            }
            else if(unitType.equals(getString(R.string.pref_unit_metric))){
                Log.d(LOG_TAG,"Unit Type Not Found : " +unitType );
            }

            long high = Math.round(max);
            long low = Math.round(min);

            return high + "/" + low;
        }

        /**getReadableDateString
         *
         * @param time
         * @return
         */
        private String getReadableDateString(long time) {
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Method to get 7 days weather data from the json string
         *
         * @param jsonString
         * @param numDays
         * @return
         */


        private String[] getWeatherDataFromJSON(String jsonString, int numDays) {

            String day;
            String description;
            Double max, min;
            String highAndLow;
            String[] results = new String[numDays];
            final String JSON_LIST_ARRAY_NAME = "list";
            final String JSON_TEMP_OBJECT_NAME = "temp";
            final String JSON_WEATHER_ARRAY_NAME = "weather";
            final String JSON_TEMP_MAX = "max";
            final String JSON_TEMP_MIN = "min";
            final String JSON_TEMP_DESCRIPTION = "description";

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();


            JSONObject jsonrootObject = null;
            try {
                jsonrootObject = new JSONObject(jsonString);
                JSONArray json_list_Array = jsonrootObject.optJSONArray(JSON_LIST_ARRAY_NAME);

                for (int i = 0; i < json_list_Array.length(); i++) {

                    long dateTime;
                    // Cheating to convert this to UTC time, which is what we want anyhow
                    dateTime = dayTime.setJulianDay(julianStartDay + i);
                    day = getReadableDateString(dateTime);

                    JSONObject eachJSONObject = json_list_Array.getJSONObject(i);
                    max = eachJSONObject.getJSONObject(JSON_TEMP_OBJECT_NAME).optDouble(JSON_TEMP_MAX);
                    min = eachJSONObject.getJSONObject(JSON_TEMP_OBJECT_NAME).optDouble(JSON_TEMP_MIN);
                    highAndLow = getMaxMin(max, min);
                    JSONArray weatherArray = eachJSONObject.getJSONArray(JSON_WEATHER_ARRAY_NAME);
                    description = weatherArray.getJSONObject(0).getString(JSON_TEMP_DESCRIPTION);
                    results[i] =day+" - "+description+" - "+ highAndLow;
                    Log.v(LOG_TAG, "results" + results[i]);
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, "FROM getweather function" + e.getMessage(), e);
                e.printStackTrace();
            }

            return results;
        }


        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            Log.e(LOG_TAG, "From the downloadtask");
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            int no_days = 7;
            String format = "JSON";
            String units = "metric";
            String app_id = "e3226491370c38dad336475b417a29eb";
            final String baseUri = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "7";
            final String ID_PARAM = "APPID";
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {


                Uri builtUri = Uri.parse(baseUri).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(no_days))
                        .appendQueryParameter(ID_PARAM, app_id)
                        .build();

                // String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?q="+params[0]+"&mode=json&units=metric&cnt=7&APPID=e3226491370c38dad336475b417a29eb";
                // String apiKey = "&APPID=" + BuildConfig.OPEN_WEATHER_MAP_API_KEY;

                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "FORMATTED URL IS " + url);
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();


                Log.e(LOG_TAG, "JSON STRING IS " + forecastJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }


            return getWeatherDataFromJSON(forecastJsonStr, no_days);
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);

            if (strings != null){

                mforecast.clear();
                mforecast.addAll(strings);

            }


        }
    }


}



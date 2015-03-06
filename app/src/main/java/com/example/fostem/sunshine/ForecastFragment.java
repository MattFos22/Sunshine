package com.example.fostem.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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

import org.apache.http.protocol.HTTP;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by fostem on 13/02/2015.
 */
/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> adapter;
    private SharedPreferences _prefs;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        _prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        int id = item.getItemId();
        if(id == R.id.action_refresh)
        {
            String location = _prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
            String metric = _prefs.getString(getString(R.string.pref_unit_key), getString(R.string.pref_unit_default));
            WeatherHelper weatherHelper = new WeatherHelper();
            weatherHelper.execute(location, metric);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        _prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        String location = _prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        String metric = _prefs.getString(getString(R.string.pref_unit_key), getString(R.string.pref_unit_default));
        WeatherHelper weatherHelper = new WeatherHelper();
        weatherHelper.execute(location, metric);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        WeatherHelper weatherHelper = new WeatherHelper();

//        String[] arr = new String[]{"Today - Sunny - 88/63",
//                "Tomorrow - Foggy - 70/46",
//                "Weds - Cloudy - 72/63",
//                "Thurs - Rainy - 64/51",
//                "Fri - Foggy - 70/46",
//                "Sat - Sunny - 76/68",
//                "Sun - Chilled - 45/98",
//                "Mon - Hothothot - 99/99"};
//
//
        ArrayList<String> list = new ArrayList<String>();

        adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                list);

        ListView listView = (ListView) rootView.findViewById(
                R.id.Listview_forecast);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Context context = getActivity().getApplicationContext();
                CharSequence text = adapter.getItem(position);

                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                Intent sendIntent = new Intent(getActivity(), DetailActivity.class);
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                sendIntent.setType(HTTP.PLAIN_TEXT_TYPE);

                if(sendIntent.resolveActivity(getActivity().getPackageManager()) != null)
                {
                    startActivity(sendIntent);
                }
            }
        });


        return rootView;
    }


    public class WeatherHelper extends AsyncTask<String, Void, String[]> {

        private final String Log_TAG = WeatherHelper.class.getSimpleName();

        @Override
        protected void onPostExecute(String[] result) {
            adapter.clear();
            if(result != null) {
                for (String forecastStr : result) {
                    adapter.add(forecastStr);
                }
            }
        }

        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
// so that they can be closed in the finally block.

            if(params.length == 0)
                return null;

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            String format = "json";
            String units = params[1];
            int numDays = 7;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .build();


                URL url = new URL(builtUri.toString());

                Log.v(Log_TAG, "Built URI " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
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
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();

                Log.v(Log_TAG, "Forecast Json: " +forecastJsonStr);
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }

            WeatherDataParser parser = new WeatherDataParser();

            try {
                return parser.getWeatherDataFromJson(forecastJsonStr, numDays);
            } catch (JSONException e) {
                Log.e(Log_TAG, "JsonException Occurred in string: " + forecastJsonStr);
            }
            return null;
        }
    }
}



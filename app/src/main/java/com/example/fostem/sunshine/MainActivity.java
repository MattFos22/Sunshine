package com.example.fostem.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.example.fostem.sunshine.ForecastFragment;
import com.example.fostem.sunshine.SettingsActivity;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            SettingsActivity.Launch(this);
            return true;
        } else if (id == R.id.action_showlocation) {
            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String postalCode = SP.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
            showMap(postalCode);
        }

        return super.onOptionsItemSelected(item);
    }

    public void showMap(String geoLocation) {
        String prefix = "geo:";
        String encodedQuery = Uri.encode(geoLocation);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(prefix + encodedQuery));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}


package com.example.siddharth.popmovieapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
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


public class MainActivity extends ActionBarActivity {
    private Movie[] movieList;
    private GridView gridview;
    private String v_Preference;



    public boolean isConnectedToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }

        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(getApplication(), R.xml.pref_general, true);

        if (savedInstanceState == null || !savedInstanceState.containsKey("Movies")) {
            updateMovie();
            setContentView(R.layout.activity_main);
            gridview = (GridView) findViewById(R.id.gridview);
        } else {
            movieList = (Movie[]) savedInstanceState.getParcelableArray("Movies");
            String[] urlArray = new String[movieList.length];
            for (int i = 0; i < movieList.length; i++) {
                urlArray[i] = movieList[i].getPoster_path();
            }
            setContentView(R.layout.activity_main);
            gridview = (GridView) findViewById(R.id.gridview);
            ImageAdapter adapter = new ImageAdapter(MainActivity.this, urlArray);
            gridview.setAdapter(adapter);
        }

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Movie movieToPass = movieList[position];

                Intent intent = new Intent(getApplication(), MovieDescription.class);
                Bundle b = new Bundle();
                b.putParcelable("MOVIE", movieToPass);
                intent.putExtras(b);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateMovie();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle saving_State) {
        saving_State.putParcelableArray("Movies", movieList);
        saving_State.putString("Preference", v_Preference);
        super.onSaveInstanceState(saving_State);
    }

    private void updateMovie() {
        //final String LOG_TAG = FetchMovieTask.class.getSimpleName();
        //Log.e(LOG_TAG,"updateMovie() Called");
        if(isConnectedToInternet()==true) {
            FetchMovieTask updateMovies = new FetchMovieTask();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            v_Preference = sharedPref.getString(getString(R.string.pref_order), getString(R.string.pref_syncConnection_default));

            updateMovies.execute(v_Preference);
        }
        else
        {
            Toast toast = Toast.makeText(getApplicationContext(), "Device Not Connected to the Internet.",
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM|Gravity.CENTER, 0, 10);
            toast.show();
        }
    }

    public class FetchMovieTask extends AsyncTask<String, Void, Movie[]> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        private Movie[] getMovieDataFromJson(String movieJsonStr) throws JSONException {

            JSONObject jData = new JSONObject(movieJsonStr);
            JSONArray jArray = jData.getJSONArray("results");
            Movie[] movieArray = new Movie[jArray.length()];

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jActualMovie = jArray.getJSONObject(i);

                String id = jActualMovie.getString("id");
                String title = jActualMovie.getString("title");
                String posterPath = "";
                String backdropPath = "";

                if (jActualMovie.getString("poster_path") == "null")
                {
                    posterPath = "empty";
                }  else
                {
                    posterPath = getString(R.string.base_poster_path) + jActualMovie.getString("poster_path");
                }

                if (jActualMovie.getString("backdrop_path") == "null"){
                    backdropPath = "empty";
                }  else {
                    backdropPath = getString(R.string.base_backdrop_path) + jActualMovie.getString("backdrop_path");
                }

                String release = jActualMovie.getString("release_date");

                String popularity = jActualMovie.getString("popularity");

                String overview = jActualMovie.getString("overview");

                String vote_average = jActualMovie.getString("vote_average");

                Movie m = new Movie(id, title, popularity, overview, posterPath,
                        vote_average, release, backdropPath);
                movieArray[i] = m;

            }

            if (movieArray == null) {
                return null;
            } else {
                return movieArray;
            }
        }

        @Override
        protected Movie[] doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieJsonStr = null;


            try {
                Uri buildUri = Uri.parse(getString(R.string.BASE_URL_MOVIE)).buildUpon()
                        .appendQueryParameter(getString(R.string.query_param), params[0])
                        .appendQueryParameter(getString(R.string.api_param), getString(R.string.api_key))
                        .build();

                URL url = new URL(buildUri.toString());


                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null)
                {
                    movieJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));


                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    movieJsonStr = null;
                }
                movieJsonStr = buffer.toString();


            } catch (IOException e) {
                Log.e(LOG_TAG,"NO INTERNET");
                movieJsonStr = null;
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
            if(movieJsonStr != null) {
                try {
                    Movie[] j = getMovieDataFromJson(movieJsonStr);
                    return j;

                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
                Movie[] j = new Movie[0];
                try {
                    j = getMovieDataFromJson(movieJsonStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return j;
            }
            else
            {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Movie[] result) {
            String[] urlArray = new String[result.length];
            if (result != null) {
                movieList = result;
                for (int i = 0; i < movieList.length; i++) {
                    urlArray[i] = movieList[i].getPoster_path();
                }
                GridView grid = (GridView) findViewById(R.id.gridview);
                ImageAdapter adapter = new ImageAdapter(MainActivity.this, urlArray);
                grid.setAdapter(adapter);

            }
        }
    }
}

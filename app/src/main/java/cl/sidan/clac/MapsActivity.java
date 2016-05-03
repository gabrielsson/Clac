package cl.sidan.clac;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.NavUtils;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

import cl.sidan.clac.other.ThemePicker;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private static final String APP_SHARED_PREFS = "cl.sidan";
    private SharedPreferences preferences = null;

    private GoogleMap mMap;
    private LatLng PROPELLERN = new LatLng(57.689455, 11.976570);

    private Map<Marker, Integer> beerMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get common preferences
        preferences = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);

        // Set saved theme
        int themePos = preferences.getInt("theme", 0);
        this.setTheme(ThemePicker.getTheme(themePos));

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if ( null != actionBar ) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Bundle extras = getIntent().getExtras();
        float[] lats = {},
                lngs = {};
        String[] titles = {},
                snippets = {};
        int[] beers = {};

        if (null != extras) {
            lats = extras.getFloatArray("Latitudes");
            lngs = extras.getFloatArray("Longitudes");
            titles = extras.getStringArray("Titles");
            snippets = extras.getStringArray("Snippets");
            beers = extras.getIntArray("Beers");
        }
        googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        LatLng ll = null;
        for( int i = 0; i < titles.length; i++ ) {
            ll = new LatLng(lats[i], lngs[i]);

            MarkerOptions mark = new MarkerOptions()
                    .position(ll)
                    .title(titles[i])
                    .snippet(snippets[i]);

            // Other color if beer has been drunk.
            // The argument to defaultMarker() takes a float between 0-360.0,
            // check BitmapDescriptorFactory.HUE_*.
            // It is possible to create a BitmapDescriptor from drawables etc.
            BitmapDescriptor icon;
            if (0 < beers[i]) {
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            } else {
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            }
            mark.icon(icon);

            // Add a marker and set the number of beers drunk
            Marker marker = mMap.addMarker(mark);
            beerMap.put(marker, beers[i]);
        }

        /* Add propellern */
        BitmapDrawable bd = (BitmapDrawable) getResources().getDrawable(R.drawable.proppeller);
        Bitmap b = bd.getBitmap();
        Bitmap bhalfsize = Bitmap.createScaledBitmap(b, b.getWidth()/2, b.getHeight()/2, false);
        MarkerOptions propellern = new MarkerOptions()
                .position(PROPELLERN)
                .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize));
        mMap.addMarker(propellern);

        /* See zoom */
        if ( null == ll || 1 < titles.length ) {
            // Zoom from far 2.0 (zoomed out) to close 21.0 (zoomed in)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PROPELLERN, 12.2f));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 12.2f));
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View mWindow;

        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // Don't show an InfoWindow for proppellern.
            if (marker.getPosition().equals(PROPELLERN) && null == marker.getTitle()) {
                return null;
            }

            ImageView pic = (ImageView) mWindow.findViewById(R.id.badge);
            pic.setImageResource(R.drawable.nopo);

            String title = marker.getTitle();
            TextView titleUi = (TextView) mWindow.findViewById(R.id.title);
            titleUi.setText(title);
            titleUi.setTextColor(getResources().getColor(R.color.red));

            String snippet = marker.getSnippet();
            TextView snippetUi = (TextView) mWindow.findViewById(R.id.snippet);
            snippetUi.setText(snippet);

            TextView numBeers = (TextView) mWindow.findViewById(R.id.numBeers);
            String beerText = "";
            int beers = beerMap.get(marker);
            if (0 < beers) {
                beerText = beerMap.get(marker) + " " + (beers == 1 ? "beer" : "beers") + " reported.";
            }
            numBeers.setText(beerText);

            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }
}

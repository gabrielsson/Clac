package cl.sidan.clac;

import android.support.v4.app.NavUtils;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        //getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng gbg = new LatLng(57.7072326, 11.9670171);

        Bundle extras = getIntent().getExtras();
        float[] lats = {},
                lngs = {};
        String[] titles = {},
                snippets = {};

        if (null != extras) {
            lats = extras.getFloatArray("Latitudes");
            lngs = extras.getFloatArray("Longitudes");
            titles = extras.getStringArray("Titles");
            snippets = extras.getStringArray("Snippets");
        }

        for( int i = 0; i < titles.length; i++ ) {
            LatLng ll = new LatLng(lats[i], lngs[i]);
            MarkerOptions mark = new MarkerOptions().position(ll).title(titles[i]).snippet(snippets[i]);

            // Add a marker
            mMap.addMarker(mark);
        }

        // Zoom from far 2.0 (zoomed out) to close 21.0 (zoomed in)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gbg, 12.2f));
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
}

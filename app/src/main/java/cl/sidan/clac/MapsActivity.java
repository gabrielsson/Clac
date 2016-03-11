package cl.sidan.clac;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng gbg = new LatLng(57.7072326, 11.9670171);

        Bundle extras = getIntent().getExtras();
        float[] lats = {},
                lngs = {};
        String[] labels = {};

        if (null != extras) {
            lats = extras.getFloatArray("Latitudes");
            lngs = extras.getFloatArray("Longitudes");
            labels = extras.getStringArray("Labels");
        }

        for( int i = 0; i < labels.length; i++ ) {
            LatLng ll = new LatLng(lats[i], lngs[i]);

            // Add a marker
            mMap.addMarker(new MarkerOptions().position(ll).title(labels[i]));
        }

        // Zoom from far 2.0 (zoomed out) to close 21.0 (zoomed in)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gbg, 13.0f));
    }
}

package cl.sidan.clac.listeners;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ListenerLocation implements LocationListener
{
    private static final int LOCATION_UPDATE_TIME = 1000 * 60 * 2;
    private Location lastKnownLocation = null;
    private LocationManager locationManager = null;

    private Context context = null;

    public ListenerLocation(Context context, Location lastKnownLocation) {
        this.context = context;
        // Experimental, get lastKnownLocation from activity if the location listener has been recreated.
        this.lastKnownLocation = lastKnownLocation;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        long minTime = 1000 * 60 * 10;
        float minDistance = 50;

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, this);
        } catch ( IllegalArgumentException e ) {
            Log.e("Location", "Network provider does not exist in emulator.");
        }

        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, minTime, minDistance, this);

        /* USED FOR SIDE-EFFECTS: updates lastKnownPosition */
        Log.d("Location", "LastKnownLocation " + getLocation());
    }

    @Override
    public void onLocationChanged(Location loc)
    {
        if( isBetterLocation(loc, lastKnownLocation) ) {
            lastKnownLocation = loc;
        }
    }

    @Override
    public void onProviderDisabled(String provider)
    {

        Toast.makeText(context,
                provider + " location provider disabled",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider)
    {
        Toast.makeText(context,
                provider + " location provider enabled",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    public static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > LOCATION_UPDATE_TIME;
        boolean isSignificantlyOlder = timeDelta < -LOCATION_UPDATE_TIME;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public Location getLocation() {
        Location newLocationPassive = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if ( newLocationPassive != null && isBetterLocation(newLocationPassive, lastKnownLocation) ) {
            lastKnownLocation = newLocationPassive;
        }

        // Could need to add a try/catch here, because network provider does not exist in emulator.
        Location newLocationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if ( newLocationNetwork != null && isBetterLocation(newLocationNetwork, lastKnownLocation) ) {
            lastKnownLocation = newLocationNetwork;
        }

        Location newLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if( newLocationGPS != null && isBetterLocation(newLocationGPS, lastKnownLocation) ) {
            lastKnownLocation = newLocationGPS;
        }

        return lastKnownLocation;
    }

    public final void stopLocationUpdates() {
        locationManager.removeUpdates(this);
    }
}
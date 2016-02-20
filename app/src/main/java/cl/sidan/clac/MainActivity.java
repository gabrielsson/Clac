package cl.sidan.clac;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Locale;

import cl.sidan.clac.access.impl.JSONParserSidanAccess;
import cl.sidan.clac.access.interfaces.Entry;
import cl.sidan.clac.access.interfaces.SidanAccess;
import cl.sidan.clac.access.interfaces.User;
import cl.sidan.clac.fragments.FragmentArr;
import cl.sidan.clac.fragments.FragmentReadEntries;
import cl.sidan.clac.fragments.FragmentSettings;
import cl.sidan.clac.fragments.FragmentWrite;
import cl.sidan.clac.fragments.MyExceptionHandler;
import cl.sidan.clac.fragments.MyFragmentPagerAdapter;
import cl.sidan.clac.fragments.MyLocationListener;
import cl.sidan.clac.fragments.RequestEntry;
import cl.sidan.clac.interfaces.ScrollListener;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String APP_SHARED_PREFS = "cl.sidan";

    private SharedPreferences preferences = null;
    private boolean isUserLoggedIn;
    private static String nummer = "";
    private static String password = "";

    private ArrayList<User> kumpaner = new ArrayList<>();
    private ArrayList<Integer> selectedItems = new ArrayList<>();

    private MyLocationListener locationListener = null;
    private Location lastKnownLocation = null;

    private ArrayList<Entry> notSentList = new ArrayList<>();
    private DrawerLayout drawer;

    @Override
    protected void onResume() {
        preferences = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
        isUserLoggedIn = preferences.getBoolean("userLoggedIn", false);
        if (!isUserLoggedIn) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        super.onResume();
    }

    @Override
    protected void onRestart() {
        preferences = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
        isUserLoggedIn = preferences.getBoolean("userLoggedInState", false);
        if (!isUserLoggedIn) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        super.onRestart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get common preferences
        preferences = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
        isUserLoggedIn = preferences.getBoolean("userLoggedIn", false);
        nummer = preferences.getString("username", null);
        password = preferences.getString("password", null);

        // Start login activity if needed
        if (!isUserLoggedIn) {
            Log.d("XXX_SWO","User not logged in. Starting LoginActivity");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            // Report exceptions via mail
            Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));

            // Standard view consist of a FragmentReadEntries, together with a Actionbar menu,
            // and a FloatingActionButton
            setContentView(R.layout.activity_main);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            ListView listView = (ListView) findViewById(R.id.entries);

            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.right_drawer, FragmentWrite.newInstance())
                            .commit();
                    drawer.openDrawer(GravityCompat.END);
                }
            });

            final ScrollListener scrl = new ScrollListener
                    .Builder()
                    .footer(fab)
                    .minFooterTranslation(getResources().getDimensionPixelSize(R.dimen.fab_height))
                    .actionbar(getSupportActionBar())
                    .isSnappable(true)
                    .build();
            listView.setOnScrollListener(scrl);

            // FragmentWrite is the default fragmentp
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.right_drawer, FragmentWrite.newInstance())
                    .commit();
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            super.onBackPressed();
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
        switch (item.getItemId()) {
            case R.id.action_settings:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.right_drawer, FragmentSettings.newInstance())
                        .commit();
                drawer.openDrawer(GravityCompat.END);
                return true;
            case R.id.action_logout:
                logOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment = null;
        switch (item.getItemId()) {
            case R.id.nav_camera:
                fragment = FragmentWrite.newInstance();
                break;

            case R.id.nav_gallery:
                fragment = FragmentArr.newInstance();
                break;
            case R.id.nav_slideshow:
                break;
            case R.id.nav_manage:
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_send:
                break;
            default:
                // Kill
                throw new RuntimeException("Some functionality is obviously not implemented yet.");
        }

        if(fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.right_drawer, fragment)
                    .commit();
            item.setChecked(true);
            drawer.openDrawer(GravityCompat.END);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logOut() {
        preferences.edit().clear().apply();

        // Broadcast logout to other activities
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.package.ACTION_LOGOUT");
        sendBroadcast(broadcastIntent);
        finish();
    }

    public boolean checkAndUpdateTime() {
        /* Save date to see if information is up-to-date */
        GregorianCalendar cal = new GregorianCalendar();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        cal.add(GregorianCalendar.HOUR, 5);
        String dateNow = formatter.format(cal.getTime());
        String dateOld = preferences.getString("lastTimeReportedKumpaner", "");
        Log.d("Kumpaner", "Uppdatera tid: Now=" + dateNow + ", was=" + dateOld);
        if( dateNow.compareTo(dateOld) >= 0 ){
            Log.d("Kumpaner", "Fortfarande inom 5 timmar. Sparar nuvarande tid.");
            preferences.edit().putString("lastTimeReportedKumpaner", dateNow).apply();
            return true;
        } else {
            Log.d("Kumpaner", "Tiden har gått ut, rensar kumpan listan.");
            selectedItems.clear();
            return false;
        }
    }

    public void createEntryAndSend(Entry entry) {
        if( checkAndUpdateTime() ) {
            Log.d("Kumpaner", "Creating kumpaner.");
            ((RequestEntry) entry).setKumpaner(kumpaner);
        } else {
            Log.d("Kumpaner", "No kumpaner found.");
        }
        new CreateEntryAsync().execute(entry);
    }

    public final void notifyLocationChange() {
        if( preferences.getBoolean("positionSetting", true) && locationListener == null ) {
            locationListener = new MyLocationListener(this, lastKnownLocation);
            Log.d("Location", "New Location listener created.");
        } else if(locationListener != null) {
            locationListener.stopLocationUpdates();
            locationListener = null;
            Log.d("Location", "Stopped and removed location listener.");
        }
    }

    public final void notifyGCMChange() {
        if( preferences.getBoolean("notifications", true) ) {
            // GCMUtil.unregister(this);
        } else {
            // GCMUtil.register(this);
        }
    }

    /**************************************************************************
     * Getters/is-functions
     *************************************************************************/

    public static SidanAccess sidanAccess() {
        return LazyHolder.INSTANCE;
    }

    public final SharedPreferences getPrefs() {
        return preferences;
    }

    public final boolean isConnected() {
        return true; // ConnectionUtil.isNetworkConnected(this);
    }

    public boolean isLoggedIn() {
        return !(nummer == null ||
                nummer.isEmpty() ||
                "#".equals(nummer)) &&
                sidanAccess().authenticateUser();
    }

    public final Location getLocation() {
        if( locationListener != null ) {
            lastKnownLocation = locationListener.getLocation();
            return lastKnownLocation;
        }
        return null;
    }

    /**************************************************************************
     * CLASSES
     *************************************************************************/

    private static class LazyHolder {
        private static SidanAccess INSTANCE = new JSONParserSidanAccess(nummer, password);
    }

    public final class CreateEntryAsync extends AsyncTask<Entry, Entry, Boolean> {
        @Override
        protected Boolean doInBackground(Entry... entries) {
            Collections.addAll(notSentList, entries);

            Entry e;
            for(int i = 0; i < notSentList.size(); i++) {
                e = notSentList.get(i);
                if( onCreateEntry(e) ) {
                    notSentList.remove(i);
                }
            }
            return notSentList.isEmpty();
        }


        public final boolean onCreateEntry(Entry entry) {
            String host = null;
            try {
                host = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            boolean isSuccess = sidanAccess().createEntry(entry.getMessage(), entry.getLatitude(), entry.getLongitude(),
                    entry.getEnheter(), entry.getStatus(), host, entry.getSecret(), entry.getImage(),
                    entry.getFileName(), entry.getKumpaner());

            if( isSuccess ) {
                Log.d("WriteEntry", "Successfully created entry, now notifying GCM users...");
                // GCMUtil.notifyGCM(getApplicationContext(), nummer, entry.getMessage());
            }

            return isSuccess;
        }

        @Override
        protected void onPostExecute(Boolean retur) {
            Log.e("WriteEntry", "Could not create some Entries.");
        }
    }
}

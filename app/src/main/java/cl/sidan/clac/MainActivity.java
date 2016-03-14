package cl.sidan.clac;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.ListView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import cl.sidan.clac.access.impl.JSONParserSidanAccess;
import cl.sidan.clac.access.interfaces.Entry;
import cl.sidan.clac.access.interfaces.SidanAccess;
import cl.sidan.clac.fragments.FragmentChangePassword;
import cl.sidan.clac.fragments.FragmentArr;
import cl.sidan.clac.fragments.FragmentMembers;
import cl.sidan.clac.fragments.FragmentReadEntries;
import cl.sidan.clac.fragments.FragmentSettings;
import cl.sidan.clac.fragments.FragmentStats;
import cl.sidan.clac.fragments.FragmentWrite;
import cl.sidan.clac.listeners.ListenerLocation;
import cl.sidan.clac.listeners.ListenerScroller;
import cl.sidan.clac.other.MyExceptionHandler;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String APP_SHARED_PREFS = "cl.sidan";

    private SharedPreferences preferences = null;
    private boolean isUserLoggedIn;
    public static String number = "";

    private ListenerLocation locationListener = null;
    private Location lastKnownLocation = null;

    private DrawerLayout drawer;

    private HashMap<String, Fragment> fragments = new HashMap<>();
    private FragmentReadEntries fragmentReadEntries;

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
        number = preferences.getString("username", null);
        String password = preferences.getString("password", null);

        // Start login activity if needed
        if (!isUserLoggedIn) {
            Log.d("XXX_SWO","User not logged in. Starting LoginActivity.");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            LazyHolder.INSTANCE = new JSONParserSidanAccess(number, password);

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
                    getReusedFragment(new FragmentWrite());
                }
            });


            final ListenerScroller scrl = new ListenerScroller
                    .Builder()
                    .footer(fab)
                    .minFooterTranslation(getResources().getDimensionPixelSize(R.dimen.fab_height))
                    .actionbar(getSupportActionBar())
                    .isSnappable(true)
                    .build();
            listView.setOnScrollListener(scrl);

            getReusedFragment(new FragmentWrite());
            drawer.closeDrawer(GravityCompat.END);
        }
    }


    @Override
    public void onBackPressed() {
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
                getReusedFragment(new FragmentSettings());
                return true;
            case R.id.action_logout:
                logOut();
                return true;
            case R.id.action_change_password:
                getReusedFragment(new FragmentChangePassword());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_read_entries:
                //Do nothing, drawers will be closed since no new fragment is loaded.
                break;
            case R.id.nav_write_entry:
                getReusedFragment(new FragmentWrite());
                break;
            case R.id.nav_arr:
                getReusedFragment(new FragmentArr());
                break;
            case R.id.nav_members:
                getReusedFragment(new FragmentMembers());
                break;
            case R.id.nav_stats:
                getReusedFragment(new FragmentStats());
                break;
            case R.id.nav_map:
                ArrayList<Entry> entries = fragmentReadEntries.returnEntries();
                HashMap<String, Entry> hm = new HashMap<>();

                for ( Entry e : entries ) {
                    if ( // !hm.containsKey(e.getSignature()) && // not registered yet
                            !e.getLongitude().equals(BigDecimal.ZERO) ) { // has a position
                        hm.put(e.getSignature(), e);
                    }
                }

                int i = 0, size = hm.size();
                float[] lats = new float[size],
                        lngs = new float[size];
                String[] titles = new String[size],
                        snippets = new String[size];

                Log.d("MapIntent", "Will plot " + size + " entries in the map.");

                for (Entry e : hm.values()) {
                    lats[i] = e.getLatitude().floatValue();
                    lngs[i] = e.getLongitude().floatValue();
                    titles[i] = e.getSignature() + " kl. " + e.getTime();
                    snippets[i] = "";

                    if ( !e.getMessage().isEmpty() ) {
                        snippets[i] += e.getMessage() + "  /" + e.getSignature();
                    }
                    if (0 < e.getEnheter()) {
                        snippets[i] += e.getEnheter() + " enheter rapporterade av " + e.getSignature();
                    }
                    i++;
                }

                Intent mapIntent = new Intent(this, MapsActivity.class);
                mapIntent.putExtra("Latitudes", lats);
                mapIntent.putExtra("Longitudes", lngs);
                mapIntent.putExtra("Titles", titles);
                mapIntent.putExtra("Snippets", snippets);

                startActivity(mapIntent);
                break;

            case R.id.nav_settings:
                getReusedFragment(new FragmentSettings());
                break;
/*
            case R.id.nav_tattarbilder:
                break;
            case R.id.nav_ninja:
                break;
*/
            default:
                // Kill
                throw new RuntimeException("Some functionality is obviously not implemented yet.");
        }

        item.setChecked(true);

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void registerReadEntriesFragment(FragmentReadEntries fragmentReadEntries) {
        this.fragmentReadEntries = fragmentReadEntries;
    }

    public Fragment getReusedFragment(Fragment instanceFragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if ( null == fragments.get(instanceFragment.getClass().getCanonicalName()) ) {
            fragments.put(instanceFragment.getClass().getCanonicalName(), instanceFragment);
            ft.add(R.id.right_drawer, instanceFragment, instanceFragment.getClass().getCanonicalName());
        }

        for( Fragment fragment : fragments.values() ) {
            ft.hide(fragment);
        }

        Fragment reusedFragment = fragments.get(instanceFragment.getClass().getCanonicalName());
        ft.show(reusedFragment);

        ft.commit();
        drawer.openDrawer(GravityCompat.END);
        return reusedFragment;
    }


    public void logOut() {
        preferences.edit().clear().apply();

        // Broadcast logout to other activities

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

    }


    public final void notifyLocationChange() {
        if( preferences.getBoolean("positionSetting", true) && locationListener == null ) {
            locationListener = new ListenerLocation(this, lastKnownLocation);
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
        private static SidanAccess INSTANCE;//= new JSONParserSidanAccess(number, password);
    }
}

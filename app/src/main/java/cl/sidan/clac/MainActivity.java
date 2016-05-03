package cl.sidan.clac;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import cl.sidan.clac.access.impl.JSONParserSidanAccess;
import cl.sidan.clac.access.interfaces.Entry;
import cl.sidan.clac.access.interfaces.SidanAccess;
import cl.sidan.clac.access.interfaces.UpdateInfo;
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
import cl.sidan.clac.other.ThemePicker;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String APP_SHARED_PREFS = "cl.sidan";
    private long SECONDS_BETWEEN_UPDATE_CHECKS = 60*60*24*5; // 5 days

    private SharedPreferences preferences = null;
    private boolean isUserLoggedIn;
    public static String number = "";

    private ListenerLocation locationListener = null;
    private Location lastKnownLocation = null;

    private DrawerLayout drawer;

    private HashMap<String, Fragment> fragments = new HashMap<>();
    private FragmentReadEntries fragmentReadEntries;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get common preferences
        preferences = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
        isUserLoggedIn = preferences.getBoolean("userLoggedIn", false);
        number = preferences.getString("username", null);
        String password = preferences.getString("password", null);

        // Set saved theme
        int themePos = preferences.getInt("theme", 0);
        this.setTheme(ThemePicker.getTheme(themePos));

        // Start login activity if needed
        if (!isUserLoggedIn) {
            Log.d("MainActivity", "User not logged in. Starting LoginActivity.");
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

            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);

            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            if ( null == savedInstanceState ) { // only on first create!
                getReusedFragment(new FragmentWrite());
                drawer.closeDrawer(GravityCompat.END);

                // Check if no view has focus:
                View view = this.getCurrentFocus();
                if (view != null) {
                    // Hide the soft-keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getReusedFragment(new FragmentWrite());

                    // Show the soft-keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            });

            ListView listView = (ListView) findViewById(R.id.entries);
            final ListenerScroller scrl = new ListenerScroller
                    .Builder()
                    .footer(fab)
                    .minFooterTranslation(getResources().getDimensionPixelSize(R.dimen.fab_height))
                    .actionbar(getSupportActionBar())
                    .isSnappable(true)
                    .fragment(fragmentReadEntries)
                    .build();
            listView.setOnScrollListener(scrl);

            TextView nummerView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nummerView);
            nummerView.setText(number);
        }
    }

    /**
     * See https://developer.android.com/training/basics/activity-lifecycle/stopping.html
     * for transitions between the states;
     * created -> started -> resumed -> paused -> stopped -> destroyed
     *               ^                              |
     *               |______________________________|
     */
    @Override
    protected void onStart() {
        checkForUpdates();

        preferences = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
        isUserLoggedIn = preferences.getBoolean("userLoggedIn", false);

        if (!isUserLoggedIn) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        notifyLocationChange(); // Add a location listener

        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            // Hide the soft-keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        super.onStart();
    }

    @Override
    public void onBackPressed() {
        if ( !closeDrawers() ) {
            /*
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            */
            super.onBackPressed();
        }
    }

    public boolean closeDrawers() {
        if (drawer.isDrawerOpen(GravityCompat.START) || drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.START);
            drawer.closeDrawer(GravityCompat.END);
            return true;
        }
        return false;
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

                // Show the soft-keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
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

                long TWENTYFOUR_HOURS = 60*60*24*1000,
                        ten_minutes = 60*10*1000, // Because the server time is incorrect with
                                                  // about ~7 minutes, we also add 10 minutes
                        unixYesterday = System.currentTimeMillis() - TWENTYFOUR_HOURS + ten_minutes;
                Date yesterday = new Date(unixYesterday);

                for ( Entry e : entries ) {
                    if ( // !hm.containsKey(e.getSignature()) && // Not registered yet
                            !e.getLongitude().equals(BigDecimal.ZERO) && // Have a position
                            e.getDateTime().after(yesterday) // Happened in the last day
                            ) {
                        // Only add the latest entry (per signature)
                        Entry latest = hm.get(e.getSignature());
                        if (null == latest || e.getDateTime().after(latest.getDateTime())) {
                            hm.put(e.getSignature(), e);
                        }
                    }
                }

                int i = 0, size = hm.size();
                float[] lats = new float[size],
                        lngs = new float[size];
                String[] titles = new String[size],
                        snippets = new String[size];
                int[] beers = new int[size];

                Log.d("MapIntent", "Will plot " + size + " entries in the map.");

                for (Entry e : hm.values()) {
                    lats[i] = e.getLatitude().floatValue();
                    lngs[i] = e.getLongitude().floatValue();

                    // if the post was before midnight, post "yesterday"
                    String wasYesterday = "";
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date dateWithoutTime = sdf.parse(sdf.format(new Date()));
                        wasYesterday = (e.getDateTime().before(dateWithoutTime) ? " yesterday" : "");
                    } catch (ParseException err) {
                        Log.e("MapIntent", "Cannot parse date for 'yesterday': " + err.getMessage());
                    }

                    titles[i] = e.getSignature() + " kl. " + e.getTime() + wasYesterday + timeSinceEventText(e.getDateTime());
                    beers[i] = e.getEnheter();
                    snippets[i] = "";

                    if ( !e.getMessage().isEmpty() ) {
                        snippets[i] += e.getMessage() + "  /" + e.getSignature();
                    }
                    i++;
                }

                Intent mapIntent = new Intent(this, MapsActivity.class);
                mapIntent.putExtra("Latitudes", lats);
                mapIntent.putExtra("Longitudes", lngs);
                mapIntent.putExtra("Titles", titles);
                mapIntent.putExtra("Snippets", snippets);
                mapIntent.putExtra("Beers", beers);

                startActivity(mapIntent);
                break;

            case R.id.nav_settings:
                getReusedFragment(new FragmentSettings());
                break;

            case R.id.nav_search:
                getReusedFragment(new FragmentSearch());
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
    public FragmentReadEntries getReadEntriesFragment() {
        return this.fragmentReadEntries;
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
        } else if( !preferences.getBoolean("positionSetting", false) && locationListener != null) {
            locationListener.stopLocationUpdates();
            locationListener = null;
            Log.d("Location", "Stopped and removed location listener.");
        }
    }

    public void checkForUpdates() {
        Long unixTime = System.currentTimeMillis() / 1000, // Unix Epoch Seconds
                lastUpdateCheck = preferences.getLong("LastUpdateCheck", 0),
                nextTimeToPerformCheck = unixTime - SECONDS_BETWEEN_UPDATE_CHECKS;
        if ( lastUpdateCheck < nextTimeToPerformCheck ) {
            // Asynctask
            Log.d("XXX_SWO", "Time to check for updates, last check was " + lastUpdateCheck);
            preferences.edit().putLong("LastUpdateCheck", unixTime).apply();
            new CheckForNewVersionAsync().execute();
        }
    }

    /**************************************************************************
     * Getters/is-functions
     *************************************************************************/

    public static SidanAccess sidanAccess() {
        return LazyHolder.INSTANCE;
    }

    public String whoAmI() {
        return number;
    }

    public final SharedPreferences getPrefs() {
        return preferences;
    }

    public final boolean isConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public final Location getLocation() {
        if( locationListener != null ) {
            lastKnownLocation = locationListener.getLocation();
            return lastKnownLocation;
        }
        return null;
    }

    public String timeSinceEventText(Date event) {
        Calendar c = Calendar.getInstance();
        c.setTime(event);
        long millis = c.getTimeInMillis(),
                secondsSincePost = (System.currentTimeMillis() - millis) / 1000;
        String timeSincePost;
        if ( secondsSincePost < 60*60 ) {
            timeSincePost = " (" + Math.round(secondsSincePost/60) + " minutes ago)";
        } else {
            timeSincePost = " (" + Math.round(secondsSincePost/(60*60)) + " hours ago)";
        }
        return timeSincePost;
    }

    /**************************************************************************
     * CLASSES
     *************************************************************************/

    private static class LazyHolder {
        private static SidanAccess INSTANCE;//= new JSONParserSidanAccess(number, password);
    }

    private final class CheckForNewVersionAsync extends AsyncTask<Void, Void, UpdateInfo> {
        @Override
        protected UpdateInfo doInBackground(Void... voids) {
            return sidanAccess().checkForUpdates();
        }

        @Override
        protected void onPostExecute(UpdateInfo info) {
            String currentVersion = BuildConfig.VERSION_NAME;
            Log.d("XXX_SWO", "The current version is " + currentVersion + " and latest is " + info.getLatestVersion());
            if ( !currentVersion.equals(info.getLatestVersion()) ) {
                Log.d("XXX_SWO", "Found new version, starting activity!");

                Intent newVersionIntent = new Intent("cl.sidan.clac.UPDATE");
                // newVersionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
                newVersionIntent.putExtra("UpgradeUrl", info.getURL());
                newVersionIntent.putExtra("UpgradeNews", info.getNews());
                startActivity(newVersionIntent);
            } else {
                Log.d("XXX_SWO", "No need for an Version Activity.");
            }
        }
    }
}

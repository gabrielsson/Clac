package cl.sidan.clac.access.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.sileria.android.Kit;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;

import cl.sidan.access.impl.JSONParserSidanAccess;
import cl.sidan.access.interfaces.Entry;
import cl.sidan.access.interfaces.SidanAccess;
import cl.sidan.access.interfaces.User;
import cl.sidan.util.ConnectionUtil;
import cl.sidan.util.GCMUtil;

// http://aniqroid.sileria.com/doc/api/


public class MainActivity extends ActionBarActivity {
    private SharedPreferences preferences = null;
    private static String nummer = "";
    private static String password = "";

    private ArrayList<User> kumpaner = new ArrayList<User>();
    private ArrayList<Integer> selectedItems = new ArrayList<Integer>();

    private ViewPager mViewPager = null;

    private MyLocationListener locationListener = null;
    private Location lastKnownLocation = null;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));

        preferences = getSharedPreferences("cl.sidan", 0);
        nummer = preferences.getString("nummer", null);
        password = preferences.getString("password", null);

        if( preferences.getBoolean("positionSetting", true) ) {
            notifyLocationChange();
        }

        if (savedInstanceState == null) {
            /* Create Login fragment and check if login is successful. */
            setContentView(R.layout.activity_main);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, FragmentLogin.newInstance(), "tag_login")
                    .commit();
        }
    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        /* Kolla att man är inloggad innan vi genererar menyn */
        if( loggedIn() && getFragmentManager().findFragmentByTag("tag_login") == null) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }
        return false;
    }

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.logout:
                preferences.edit().clear().apply();
                new MainActivity();
                break;

            case R.id.exit_app:
                System.exit(0);
                break;

            case R.id.report_beer:
                showRapporteraEnheterPopUp();
                break;

            case R.id.kumpaner:
                showRapporteraKumpanerPopUp();
                break;

            default:
                Log.d("XXX_SWO", "Button does not exist!");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public final void onDestroy() {
        super.onDestroy();
        Kit.destroy();
    }

    public static boolean loggedIn() {
        return !(nummer == null || nummer.isEmpty() || "#".equals(nummer));
    }

    public final boolean isConnected() {
        return ConnectionUtil.isNetworkConnected(this);
    }

    /* Hade varit gött att flytta denna till FragmentLogin
     * så att MainActivity är så liten som möjligt.
     */
    public final void removeLogin() {
        nummer = preferences.getString("nummer", null);
        password = preferences.getString("password", null);

        if( loggedIn() ) {
            setContentView(R.layout.main);

            // http://aniqroid.sileria.com/doc/api/
            Kit.init(getApplicationContext());

            mViewPager = (ViewPager) findViewById(R.id.viewpager);
            MyFragmentPagerAdapter mMyFragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
            mViewPager.setAdapter(mMyFragmentPagerAdapter);
            mViewPager.setCurrentItem(1);
            mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    // XXX_SWO: If we have an ActionBar, we can set which item is selected here
                    // bar.setSelectedNavigationItem(position)

                    Fragment fragment = ((MyFragmentPagerAdapter) mViewPager.getAdapter()).getFragment(position);
                    if( fragment != null) {
                        fragment.onResume();
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) { }
            });
        }
    }

    private void showRapporteraKumpanerPopUp() {
        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
        helpBuilder.setTitle("Rapportera Kumpaner");
        helpBuilder.setIcon(R.drawable.nopo);

        /* Fixa om detta så att man får det från en databas. */
        final CharSequence[] kumpans = new CharSequence[85];
        for( int i = 0; i < kumpans.length; i++) {
            int nummer = i+1;
            if( nummer == 70 )
                kumpans[i] = "Redan färjat";
            else if( "#13,".contains("#"+nummer+",") )
                kumpans[i] = "Orättvist förlorarnummer";
            else if( "#1, #5, #6, #21, #69,".contains("#"+nummer+",") )
                kumpans[i] = "Nej, vinnarnummer";
            else
                kumpans[i] = "#" + nummer;
        }
        Collections.reverse(Arrays.asList(kumpans));

        checkAndUpdateTime();

        boolean[] checkedItems = new boolean[kumpans.length];
        for( int i = 0; i < checkedItems.length; i++ ) {
            checkedItems[i] = false;
        }
        for( int i : selectedItems ) {
            checkedItems[i] = true;
        }

        helpBuilder.setMultiChoiceItems(kumpans, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                if (b) {
                    // If the user checked the item, add it to the selected items
                    // write your code when user checked the checkbox
                    selectedItems.add(i);
                } else if (selectedItems.contains(i)) {
                    // Else, if the item is already in the array, remove it
                    // write your code when user Uchecked the checkbox
                    selectedItems.remove(Integer.valueOf(i));
                }
            }
        });

        helpBuilder.setNegativeButton("Ångra",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                    }
                });
        helpBuilder.setPositiveButton("Sup!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                kumpaner.clear();
                for (int j = 0; j < selectedItems.size(); j++) {
                    String signatur = kumpans[selectedItems.get(j)].toString();
                    RequestUser user = new RequestUser(signatur);
                    kumpaner.add(user);
                }
            }
        });

        // Remember, create doesn't show the dialog
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.show();
    }

    private void showRapporteraEnheterPopUp() {
        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
        helpBuilder.setTitle("Rapportera enheter");
        helpBuilder.setIcon(R.drawable.olsug_32);
        helpBuilder.setItems(R.array.antalEnheter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item

                Log.d("RapporteraEnheter", "Antal enheter " + which);

                RequestEntry entry = new RequestEntry();
                entry.setEnheter(which + 1);

                Location myLocation = getLocation();
                if (myLocation != null) {
                    entry.setLatitude(BigDecimal.valueOf(myLocation.getLatitude()));
                    entry.setLongitude(BigDecimal.valueOf(myLocation.getLongitude()));
                }

                if (checkAndUpdateTime()) {
                    entry.setKumpaner(kumpaner);
                }
                new CreateEntryAsync().execute(entry);
            }
        });

        helpBuilder.setNegativeButton("Ångra",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                    }
                });

        // Remember, create doesn't show the dialog
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.show();
    }

    public boolean checkAndUpdateTime() {
        /* Save date to see if information is up-to-date */
        GregorianCalendar cal = new GregorianCalendar();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
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

    /* Set the current view given a FragmentOrder item */
    public final void setCurrentItem(int order) {
        mViewPager.setCurrentItem(order);
    }

    public final Location getLocation() {
        if( locationListener != null ) {
            lastKnownLocation = locationListener.getLocation();
            return lastKnownLocation;
        }
        return null;
    }

    private static class LazyHolder {
        private static SidanAccess INSTANCE = new JSONParserSidanAccess(nummer, password);
    }
    public static SidanAccess sidanAccess() {
        return LazyHolder.INSTANCE;
    }
    public final SharedPreferences getPrefs() {
        return preferences;
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
            GCMUtil.notifyGCM(getApplicationContext(), nummer, entry.getMessage());
        }

        return isSuccess;
    }

    ArrayList<Entry> notSentList = new ArrayList<Entry>();
    public final class CreateEntryAsync extends AsyncTask<Entry, Entry, Boolean> {
        @Override
        protected Boolean doInBackground(Entry... entries) {
            for(Entry e : entries) {
                notSentList.add(e);
            }
            Entry e = null;
            for(int i = 0; i < notSentList.size(); i++) {
                e = notSentList.get(i);
                if( onCreateEntry(e) ) {
                    notSentList.remove(i);
                }
            }
            return notSentList.isEmpty();
        }

        @Override
        protected void onPostExecute(Boolean retur) {
            Log.e("WriteEntry", "Could not create some Entries.");
        }
    }

    public final void notifyGCMChange() {
        if( preferences.getBoolean("notifications", true) ) {
            GCMUtil.unregister(this);
        } else {
            GCMUtil.register(this);
        }
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
}

package cl.sidan.clac.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import cl.sidan.clac.MainActivity;
import cl.sidan.clac.MapsActivity;
import cl.sidan.clac.R;
import cl.sidan.clac.access.interfaces.Entry;
import cl.sidan.clac.adapters.AdapterEntries;
import cl.sidan.clac.interfaces.ScrollingFragment;

public class FragmentReadEntries extends Fragment implements ScrollingFragment {
    private final String TAG = getClass().getCanonicalName();
    private ArrayList<Entry> entries = new ArrayList<>();
    private AdapterEntries entriesAdapter = null;
    private SwipeRefreshLayout entriesContainer = null;
    private View rootView;
    private int scrollPosition = 0, top = 0;
    private SharedPreferences preferences;
    private ListView listView;

    boolean isLoading = false;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        setMenuVisibility(true);

        preferences = ((MainActivity) getActivity()).getPrefs();

        if( null != savedInstanceState ) {
            scrollPosition = savedInstanceState.getInt("scrollPosition", 0);
            top = savedInstanceState.getInt("top", 0);
        }
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
                                   Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_readentries, container, false);

        float fontsize = preferences.getFloat("font_size", 15);

        entriesAdapter = new AdapterEntries(inflater.getContext(), R.layout.entry, entries, fontsize);
        entriesAdapter.setNotifyOnChange(true);

        entriesContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.entriesContainer);
        entriesContainer.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        top = 0;
                        scrollPosition = 0;
                        new ReadEntriesAsync().execute(0);
                    }
                }
        );
        entriesContainer.setRefreshing(true);
        new ReadEntriesAsync().execute(0);

        listView = (ListView) rootView.findViewById(R.id.entries);
        listView.setAdapter(entriesAdapter);
        registerForContextMenu(listView);

        ((MainActivity) getActivity()).registerReadEntriesFragment(this);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        // Save scroll position
        ListView listView = (ListView) rootView.findViewById(R.id.entries);
        scrollPosition = listView.getFirstVisiblePosition();
        top = getTop();

        state.putInt("scrollPosition", scrollPosition);
        state.putInt("top", top);

        super.onSaveInstanceState(state);
    }

    @Override
    public final void onCreateContextMenu(ContextMenu menu, View v,
                                          ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_entry, menu);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Entry e = entriesAdapter.getItem(info.position);

        String nummer = preferences.getString("number", "");

        if (e != null) {
            MenuItem likeItem = menu.findItem(R.id.like_entry);
            likeItem.setEnabled(!nummer.equals(e.getSignature()));

            MenuItem viewPosItem = menu.findItem(R.id.view_position);
            viewPosItem.setEnabled(0 != e.getLatitude().compareTo(BigDecimal.ZERO) && 0 != e.getLongitude().compareTo(BigDecimal.ZERO));
        }

        MenuItem editItem = menu.findItem(R.id.edit_entry);
        editItem.setEnabled(false);
    }

    public ArrayList<Entry> returnEntries() {
        return entries;
    }

    Entry lastClicked = null;
    @Override
    public final boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Intent intent;
        Uri data;
        Entry e;

        switch (item.getItemId()) {
            case R.id.view_position:
                e = entriesAdapter.getItem(info.position);

                String title,
                        snippet = "",
                        timeSincePost = ((MainActivity) getActivity()).timeSinceEventText(e.getDateTime());
                title = e.getSignature() + " kl. " + e.getTime() + timeSincePost;

                if ( !e.getMessage().isEmpty() ) {
                    snippet += e.getMessage() + "  /" + e.getSignature();
                }
                if (0 < e.getEnheter()) {
                    snippet += e.getEnheter() + " enheter rapporterade av " + e.getSignature();
                }

                Intent mapIntent = new Intent(getActivity(), MapsActivity.class);

                float[] lats = { e.getLatitude().floatValue() },
                        lngs = { e.getLongitude().floatValue() };
                String[] titles = { title },
                        snippets = { snippet };

                mapIntent.putExtra("Latitudes", lats);
                mapIntent.putExtra("Longitudes", lngs);
                mapIntent.putExtra("Titles", titles);
                mapIntent.putExtra("Snippets", snippets);

                startActivity(mapIntent);

                return true;

            case R.id.edit_entry:
                Log.d("Context menu", "Edit entry " + info.id);
                return true;

            case R.id.like_entry:
                e = entriesAdapter.getItem(info.position);
                new CreateLikeAsync().execute(e != null ? e.getId() : null);
                Log.d("Context menu", "Like entry " + info.id);
                return true;

            case R.id.reply_entry_via:
                /**
                 * Jag tror att entriesAdaptern försvinner då det är en submeny. DVS, att man
                 * klickar på ett menuitem istället för ett entry när man får upp replyVia menyn.
                 * Därför sparar vi entryt utanför och kopierar tillbaka det med e = lastClicked.
                 **/
                lastClicked = entriesAdapter.getItem(info.position);
                return true;

            case R.id.reply_entry_hemlis:
            case R.id.reply_entry:

                View writeView = ((MainActivity) getActivity()).getReusedFragment(new FragmentWrite()).getView();

                boolean hemlis = false;
                /* Om det är hemlis eller inte */
                if (item.getItemId() == R.id.reply_entry) {
                    e = entriesAdapter.getItem(info.position);
                } else {
                    e = lastClicked;
                    hemlis = true;
                }

                String signed = e != null ? e.getSignature() : null;
                if (signed != null && !signed.isEmpty()) {
                    signed += ": ";
                } else {
                    signed = "";
                }

                EditText writeText = (EditText) writeView.findViewById(R.id.write_entry_text);
                writeText.setText(signed);
                writeText.requestFocus();
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                ((CheckBox) writeView.findViewById(R.id.write_entry_secret)).setChecked(hemlis);

                return true;

            case R.id.reply_entry_sms:
                // e = lastClicked;

                intent = new Intent("android.intent.action.VIEW");
                /* LÄGG TILL NUMMER NEDAN */
                data = Uri.parse("sms:1");
                intent.setData(data);
                startActivity(intent);
                return true;

            case R.id.reply_entry_email:
                e = lastClicked;

                String signature = e.getSignature();
                String[] emails = new String[1];
                if (signature.startsWith("#") && signature.length() < 4) {
                    try {
                        /* Used for side effects */
                        //noinspection ResultOfMethodCallIgnored
                        Integer.parseInt(signature.substring(1));
                        emails[0] = signature.substring(1) + "@chalmerslosers.com";
                    } catch (NumberFormatException nfe) {
                        Log.d("REPLY", "Could not parse signature. " + nfe);
                    }
                }

                intent = new Intent(Intent.ACTION_SEND);
                /* BORDE GÅ ATT ÄNDRA TILL text/html ELLER NÅTT */
                intent.setType("text/plain");

                String message = "\n\n______________________________________\n" + e.toString();
                intent.putExtra(Intent.EXTRA_EMAIL, emails);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Svar från Sidan");
                intent.putExtra(Intent.EXTRA_TEXT, message);

                startActivity(Intent.createChooser(intent, "Skicka mail.."));
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    public final class CreateLikeAsync extends AsyncTask<Integer, Entry, Void> {
        @Override
        protected Void doInBackground(Integer... integers) {
            String host = null;
            try {
                host = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            String nummer = ((MainActivity) getActivity()).getPrefs().getString("number", null);

            for(Integer i : integers) {
                ((MainActivity) getActivity()).sidanAccess().createLike(i, nummer, host);
                Log.d("Debug", "Liked " + i);
            }

            return null;
        }
    }

    public final class ReadEntriesAsync extends AsyncTask<Integer, Void, List<Entry>> {
        @Override
        protected List<Entry> doInBackground(Integer... integers) {
            int skip = integers[0] > 0 ? integers[0] : 0,
                    take = 50;
            isLoading = true;
            return ((MainActivity) getActivity()).sidanAccess().readEntries(skip, take);
        }

        @Override
        protected void onPostExecute(List<Entry> response) {
            if( response.isEmpty() ) {
                Log.d(TAG, "Response was empty from server, will not clear entries.");
                Toast.makeText(rootView.getContext(),
                        "Kunde inte hämta inlägg från servern.",
                        Toast.LENGTH_SHORT).show();
                entriesContainer.setRefreshing(false);
            } else {
                // Remove posts from ignored members
                HashSet<String> ignoredMembers = (HashSet<String>) preferences
                        .getStringSet("ignoredMembers", new HashSet<String>());
                for( int i = response.size() - 1; i >= 0; i-- ) {
                    Entry e = response.get(i);
                    if ( ignoredMembers.contains(e.getSignature()) ) {
                        response.remove(e);
                        Log.d(TAG, "Removed entry " + e.getMessage() + " from " + e.getSignature());
                    }
                }

                // Add both old and new entries, sort on Id
                TreeMap<Integer, Entry> newEntries = new TreeMap<>(new Comparator<Integer>() {
                    @Override
                    public int compare(Integer lhs, Integer rhs) {
                        if (lhs < rhs) {
                            return 1;
                        } else if (rhs > lhs) {
                            return -1;
                        }
                        return 0;
                    }
                });

                for (Entry e : entries) {
                    newEntries.put(e.getId(), e);
                }
                for (Entry e : response) {
                    newEntries.put(e.getId(), e);
                }
                Log.d(TAG, "Entries from server: " + response.size() + ", total entries: " + newEntries.size());

                entries.clear();
                entries.addAll(newEntries.values());
                entriesAdapter.notifyDataSetChanged();
                entriesContainer.setRefreshing(false);
                isLoading = false;

                // Scroll to position
                listView.setSelectionFromTop(scrollPosition, top);
            }
        }
    }

    public int getTop() {
        View v = listView.getChildAt(0);
        if (null != v) {
            return (v.getTop() - listView.getPaddingTop());
        }
        return 0;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void readMoreEntries() {
        scrollPosition = listView.getFirstVisiblePosition();
        top = getTop();
        entriesContainer.setRefreshing(true);
        new ReadEntriesAsync().execute(entries.size());
    }
}

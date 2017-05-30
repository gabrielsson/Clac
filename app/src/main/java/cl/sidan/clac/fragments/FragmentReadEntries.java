package cl.sidan.clac.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import cl.sidan.clac.MainActivity;
import cl.sidan.clac.MapsActivity;
import cl.sidan.clac.R;
import cl.sidan.clac.access.interfaces.Entry;
import cl.sidan.clac.adapters.AdapterBeer;
import cl.sidan.clac.adapters.AdapterEntries;
import cl.sidan.clac.interfaces.ScrollingFragment;
import cl.sidan.clac.objects.RequestEntry;

public class FragmentReadEntries extends Fragment implements ScrollingFragment {
    private final String TAG = getClass().getCanonicalName();

    private static final String CONVERSATION_REGEX = "^.+:.*";
    private static final String CONVERSATION_SPLIT = ":";

    private ArrayList<Entry> entries = new ArrayList<>();
    private AdapterEntries entriesAdapter = null;
    private SwipeRefreshLayout entriesContainer = null;
    private View rootView;
    private int scrollPosition = 0, top = 0;
    private SharedPreferences preferences;
    private ListView listView;
    private EntriesListType entriesListType = EntriesListType.READ;
    boolean isLoading = false;
    private String searchString;

    private enum EntriesListType {
        SEARCH, READ
    }

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
                        entries.clear();
                        top = 0;
                        scrollPosition = 0;
                        entriesListType = EntriesListType.READ;
                        new ReadEntriesAsync().execute(0);
                        ((MainActivity) getActivity()).clearNotifications();
                    }
                }
        );

        entriesContainer.setRefreshing(true);
        entriesListType = EntriesListType.READ;
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

        // What does this do?! Please comment!
        View v = getView();
        if (null != v) {
            v.setFocusableInTouchMode(true);
            v.requestFocus();
            v.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK
                            && entriesAdapter.isFilteredResults()) {
                        entriesAdapter.getFilter().filter("");
                        return true;
                    }
                    return false;
                }
            });
        }

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

        String nummer = preferences.getString("username", "");

        if (e != null) {
            MenuItem likeItem = menu.findItem(R.id.like_entry);
            likeItem.setEnabled(!nummer.equals(e.getSignature()));

            boolean conversationEntry = e.getMessage().matches(CONVERSATION_REGEX);
            MenuItem conversationItem = menu.findItem(R.id.filter_conversation);
            conversationItem.setEnabled(conversationEntry);

            MenuItem viewPosItem = menu.findItem(R.id.view_position);
            viewPosItem.setEnabled(0 != e.getLatitude().compareTo(BigDecimal.ZERO) && 0 != e.getLongitude().compareTo(BigDecimal.ZERO));

            if(!e.getSignature().equals(nummer) || info.position >= 10) {
                MenuItem editItem = menu.findItem(R.id.edit_entry);
                editItem.setEnabled(false);
            }
        }
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

                Intent mapIntent = new Intent(getActivity(), MapsActivity.class);

                float[] lats = { e.getLatitude().floatValue() },
                        lngs = { e.getLongitude().floatValue() };
                String[] titles = { title },
                        snippets = { snippet };
                int[] beers = { e.getEnheter() };

                mapIntent.putExtra("Latitudes", lats);
                mapIntent.putExtra("Longitudes", lngs);
                mapIntent.putExtra("Titles", titles);
                mapIntent.putExtra("Snippets", snippets);
                mapIntent.putExtra("Beers", beers);

                startActivity(mapIntent);

                return true;

            case R.id.edit_entry:
                Log.d("Context menu", "Edit entry " + info.id);
                e = entriesAdapter.getItem(info.position);
                String number = ((MainActivity) getActivity()).whoAmI();
                /**
                 *  Kan bara ändra på sina egna inlägg och om de är en av de 10 senaste.
                 */
                if (e != null && e.getSignature().equals(number) && info.position < 10) {
                    Log.d("Change Entry", "Creating popup for entry " + e.getId());
                    showEditEntryPopup(e);
                }

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

                if (null != writeView) {
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
                }
                return false;

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

            case R.id.filter_conversation:
                e = entriesAdapter.getItem(info.position);

                if( !e.getMessage().matches(CONVERSATION_REGEX) ) return false;

                String[] messageSplit = e.getMessage().split(CONVERSATION_SPLIT);
                if( messageSplit.length < 2 ){
                    Toast.makeText(rootView.getContext(), "Unable to find conversation start.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                String targetSig = messageSplit[0];
                String writerSig = e.getSignature();
                
                Toast.makeText(rootView.getContext(), this.getString(R.string.filter_conversation_toast)+writerSig+" & "+targetSig, Toast.LENGTH_SHORT).show();
                entriesAdapter.getFilter().filter(writerSig+":"+targetSig);

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private ArrayList<String> selectedKumpaner = new ArrayList<>();
    private void showEditEntryPopup(final Entry e) {
        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(rootView.getContext());
        helpBuilder.setTitle("Uppdatera Meddelande");
        // helpBuilder.setIcon(R.drawable.olsug_32);

        LayoutInflater factory = LayoutInflater.from(rootView.getContext());
        final View textEntryView = factory.inflate(R.layout.popup_change_msg, null);
        helpBuilder.setView(textEntryView);

        final EditText msgText = (EditText) textEntryView.findViewById(R.id.write_entry_text);
        final Spinner msgBeers = (Spinner) textEntryView.findViewById(R.id.number_of_beers);
        final CheckBox msgSecret = (CheckBox) textEntryView.findViewById(R.id.write_entry_secret);
        final TextView kumpanText = (TextView) textEntryView.findViewById(R.id.kumpaner);

        msgText.setText(e.getMessage());
        String[] antalEnheter = getResources().getStringArray(R.array.antalEnheterInklNoll),
                namnEnheter = getResources().getStringArray(R.array.namnEnheterInklNoll);
        AdapterBeer beerAdapter = new AdapterBeer(factory, getActivity(), R.layout.beer_spinner_item, antalEnheter, namnEnheter);
        msgBeers.setAdapter(beerAdapter);
        msgSecret.setChecked(e.getSecret());
        /**
         * För att ändra kumpanerna kommer vi att vilja lyfta ut viss funktionalitet från
         * FragmentWrite och lägga det i MainActivity.
         * Exempelvis showRapporteraKumpanerPopUp och de arrayer/funktioner som håller koll på
         * vilka kumpaner man har för tillfället.
         */
        kumpanText.setText(TextUtils.join(",", e.getKumpaner()));

        helpBuilder.setNegativeButton("Ångra",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                    }
                });
        helpBuilder.setPositiveButton("Uppdatera",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String message = msgText.getText().toString();
                        int beers = msgBeers.getSelectedItemPosition();
                        boolean secret = msgSecret.isChecked();

                        RequestEntry updatedEntry = new RequestEntry();
                        updatedEntry.setId(e.getId());
                        updatedEntry.setMessage(message);
                        updatedEntry.setEnheter(beers);
                        updatedEntry.setSecret(secret);
                        updatedEntry.setKumpaner(e.getKumpaner());

                        new UpdateEntryAsync().execute(updatedEntry);
                    }
                });

        // Remember, create doesn't show the dialog
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.show();
    }

    public void searchEntries(String searchString) {
        entries.clear();
        entriesListType = EntriesListType.SEARCH;
        this.searchString = searchString;
        top = 0;
        scrollPosition = 0;
        new ReadEntriesAsync().execute(0);
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

            for(Integer i : integers) {
                ((MainActivity) getActivity()).sidanAccess().createLike(i, host);
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

            if (entriesListType == EntriesListType.READ) {
                Log.d(getClass().getCanonicalName(), "Reading new entries");
                return ((MainActivity) getActivity()).sidanAccess().readEntries(skip, take);
            } else {
                Log.d(getClass().getCanonicalName(), "Searching for new entries");
                return ((MainActivity) getActivity()).sidanAccess().searchEntries(searchString, skip, take);
            }
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

                HashMap<Integer, Entry> allEntries = new HashMap<>();
                for (Entry e : entries) {
                    allEntries.put(e.getId(), e);
                }
                for (Entry e : response) {
                    allEntries.put(e.getId(), e);
                }
                ArrayList<Entry> tempEntries = new ArrayList<>(allEntries.values());
                Collections.sort(tempEntries);

                Log.d(TAG, "Entries from server: " + response.size() + ", total entries: " + tempEntries.size());

                populateEntries(tempEntries);
                entriesContainer.setRefreshing(false);
                isLoading = false;

                // Scroll to position
                listView.setSelectionFromTop(scrollPosition, top);
            }
        }
    }

    private void populateEntries(List<Entry> tempEntries) {
        entries.clear();
        entries.addAll(tempEntries);
        entriesAdapter.notifyDataSetChanged();
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

    public final class UpdateEntryAsync extends AsyncTask<Entry, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Entry... entries) {
            Entry entry = entries[0];

            return ((MainActivity) getActivity()).sidanAccess().updateEntry(
                    entry.getId(), entry.getMessage(), entry.getEnheter(), entry.getSecret(),
                    entry.getKumpaner());
        }

        protected void onPostExecute(boolean success) {
            if (success) {
                Toast.makeText(rootView.getContext(),
                        "Meddelande uppdaterat!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(rootView.getContext(),
                        "Något gick snett. Försök senare!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

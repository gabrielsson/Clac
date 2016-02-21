package cl.sidan.clac.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import cl.sidan.clac.MainActivity;
import cl.sidan.clac.R;
import cl.sidan.clac.access.interfaces.Entry;

public class FragmentReadEntries extends Fragment {
    private final String TAG = getClass().getCanonicalName();
    private List<Entry> entries = new ArrayList<>();
    private AdapterEntries entriesAdapter = null;
    private SwipeRefreshLayout entriesContainer = null;
    private static FragmentReadEntries readEntriesFragment;
    private View rootView;

    public static FragmentReadEntries newInstance() {
        if( null == readEntriesFragment ) {
            readEntriesFragment = new FragmentReadEntries();
        }
        return readEntriesFragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setMenuVisibility(true);
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
                                   Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_readentries, container, false);

        float fontsize = ((MainActivity) getActivity()).getPrefs().getFloat("font_size", 15);

        if(entriesAdapter == null) {
            entriesAdapter = new AdapterEntries(inflater.getContext(), R.layout.entry, entries, fontsize);
            entriesAdapter.setNotifyOnChange(true);
        }

        entriesContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.entriesContainer);
        entriesContainer.setRefreshing(true);

        entriesContainer.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new ReadEntriesAsync().execute();
                    }
                }
        );

        ListView listView = (ListView) rootView.findViewById(R.id.entries);
        listView.setAdapter(entriesAdapter);

        registerForContextMenu(listView);

        new ReadEntriesAsync().execute();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        float font_size = ((MainActivity) getActivity()).getPrefs().getFloat("font_size", 15);
        if (entriesAdapter == null) {
            entriesAdapter = new AdapterEntries(rootView.getContext(), R.layout.entry, entries, font_size);
            entriesAdapter.setNotifyOnChange(true);
        }

        ListView listView = (ListView) rootView.findViewById(R.id.entries);
        listView.setAdapter(entriesAdapter);
    }

    @Override
    public final void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public final void onCreateContextMenu(ContextMenu menu, View v,
                                          ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_entry, menu);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Entry e = entriesAdapter.getItem(info.position);

        MenuItem likeItem = menu.findItem(R.id.like_entry);
        String nummer = ((MainActivity) getActivity()).getPrefs().getString("number", "");
        if (e != null) {
            likeItem.setEnabled(!nummer.equals(e.getSignature()));
        }
        MenuItem editItem = menu.findItem(R.id.edit_entry);
        editItem.setEnabled(false);
    }

    Entry lastClicked = null;
    @Override
    public final boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Intent intent;
        Uri data;
        Entry e;

        switch (item.getItemId()) {
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

                ((EditText) getActivity().findViewById(R.id.write_entry_text)).setText(signed);
                ((CheckBox) getActivity().findViewById(R.id.write_entry_secret)).setChecked(hemlis);

                //((MainActivity) getActivity()).setCurrentItem(MyFragmentPagerAdapter.FragmentOrder.writeentry);

                return true;

            case R.id.reply_entry_sms:
                // e = lastClicked;

                intent = new Intent("android.intent.action.VIEW");
                /* LÄGG TILL NUMMER NEDAN */
                data = Uri.parse("sms:0737351374");
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

    public final void populateEntries() {
        Log.d(TAG, "Entered populateEntries()");
        List<Entry> response = ((MainActivity) getActivity()).sidanAccess().readEntries(0, 50);

        if( response.isEmpty() ) {
            Log.d(TAG, "Response was empty from server, will not clear entries.");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(rootView.getContext(),
                            "Kunde inte hämta inlägg från servern.",
                            Toast.LENGTH_SHORT).show();
                }
            } );
        } else {
            Log.d(TAG, "Entries from server: " + response.size());
            entries.clear();
            entries.addAll(response);
        }

        /* Möjlig Nullpekare här. Om både denna kör och versionsasync eller något. */
        if( getActivity() != null ) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    entriesAdapter.notifyDataSetInvalidated();
                    entriesContainer.setRefreshing(false);
                }
            });
        }
        Log.d(TAG, "Entries size " + entries.size());
    }


    private void onCreateLike(Integer i) {
        String host = null;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String nummer = ((MainActivity) getActivity()).getPrefs().getString("number", null);
        ((MainActivity) getActivity()).sidanAccess().createLike(i, nummer, host);
        Log.d("Debug", "Liked " + i);
    }

    public final class CreateLikeAsync extends AsyncTask<Integer, Entry, Void> {
        @Override
        protected Void doInBackground(Integer... integers) {
            for(Integer i : integers) {
                onCreateLike(i);
            }
            return null;
        }
    }

    public final class ReadEntriesAsync extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            populateEntries();
            return null;
        }
    }
}

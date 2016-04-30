package cl.sidan.clac;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.List;

import cl.sidan.clac.access.interfaces.Entry;
import cl.sidan.clac.fragments.FragmentReadEntries;

public class FragmentSearch extends Fragment {
    private View rootView;

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search, container, false);

        Button searchButt = (Button) rootView.findViewById(R.id.search_button);
        searchButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText searchField = (EditText) rootView.findViewById(R.id.search_string);
                String searchString = searchField.getText().toString();

                new SearchEntriesAsync().execute(searchString);
            }
        });

        return rootView;
    }

    public final class SearchEntriesAsync extends AsyncTask<String, Void, List<Entry>> {

        @Override
        protected List<Entry> doInBackground(String... params) {
            Log.d(getClass().getCanonicalName(), "Searching for " + params[0]);
            return ((MainActivity) getActivity()).sidanAccess().searchEntries(params[0], 0, 50);
        }

        @Override
        protected void onPostExecute(List<Entry> entries) {
            Log.d(getClass().getCanonicalName(), "Found " + entries.size() + " entries");
            FragmentReadEntries readEntriesFragment = ((MainActivity) getActivity()).getReadEntriesFragment();
            readEntriesFragment.setEntries(entries);
        }
    }
}

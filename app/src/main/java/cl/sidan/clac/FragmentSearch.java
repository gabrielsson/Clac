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
                FragmentReadEntries readEntriesFragment = ((MainActivity) getActivity()).getReadEntriesFragment();
                readEntriesFragment.searchEntries(searchString);

                ((MainActivity) getActivity()).closeDrawers();
            }
        });

        return rootView;
    }
}

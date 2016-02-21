package cl.sidan.clac.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cl.sidan.clac.MainActivity;
import cl.sidan.clac.R;
import cl.sidan.clac.access.interfaces.Arr;
import cl.sidan.clac.access.interfaces.User;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the { OnListFragmentInteractionListener}
 * interface.
 */
public class FragmentMembers extends Fragment {

    private AdapterMembers memberAdapter = null;
    private List<User> members = new ArrayList<>();

    private Context context;
    private View rootView;

    private static FragmentMembers membersFragment;

    public static FragmentMembers newInstance() {
        if( null == membersFragment ) {
            membersFragment = new FragmentMembers();
        }
        return membersFragment;
    }


    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_member_list, container, false);
        context = rootView.getContext();

        final SharedPreferences preferences = ((MainActivity) getActivity()).getPrefs();
        float font_size = preferences.getFloat("font_size", 15);

        if(memberAdapter == null) {
            memberAdapter = new AdapterMembers(inflater.getContext(), R.layout.fragment_member, members, font_size);
            memberAdapter.setNotifyOnChange(true);
        }

        ListView memberList = (ListView) rootView.findViewById(R.id.memberList);
        memberList.setAdapter(memberAdapter);
        registerForContextMenu(memberList);

        new ReadMembersAsync().execute();

        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        SharedPreferences preferences = ((MainActivity) getActivity()).getPrefs();
        float font_size = preferences.getFloat("font_size", 15);

        if(memberAdapter == null) {
            memberAdapter = new AdapterMembers(rootView.getContext(), R.layout.fragment_member, members, font_size);
            memberAdapter.setNotifyOnChange(true);
        }

        ListView listView = (ListView) rootView.findViewById(R.id.memberList);
        listView.setAdapter(memberAdapter);
    }

    public final class ReadMembersAsync extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            members.clear();
            members.addAll(((MainActivity) getActivity()).sidanAccess().readMembers());
            if(getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        memberAdapter.notifyDataSetInvalidated();
                    }
                });
            }
            return null;
        }

    }

}

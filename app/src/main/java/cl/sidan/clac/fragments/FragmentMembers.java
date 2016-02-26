package cl.sidan.clac.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cl.sidan.clac.MainActivity;
import cl.sidan.clac.R;
import cl.sidan.clac.access.interfaces.User;
import cl.sidan.clac.adapters.AdapterMembers;

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

        float font_size = ((MainActivity) getActivity()).getPrefs().getFloat("font_size", 15);

        if(memberAdapter == null) {
            memberAdapter = new AdapterMembers(inflater.getContext(), R.layout.adapter_member_item, members, font_size);
            memberAdapter.setNotifyOnChange(true);
        }

        ListView memberList = (ListView) rootView.findViewById(R.id.memberList);
        memberList.setAdapter(memberAdapter);
        registerForContextMenu(memberList);

        registerForContextMenu(memberList);

        new ReadMembersAsync().execute();

        return rootView;
    }

    @Override
    public final void onCreateContextMenu(ContextMenu menu, View v,
                                          ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_members, menu);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        User user = memberAdapter.getItem(info.position);

        MenuItem callItem = menu.findItem(R.id.member_call);
        MenuItem smsItem = menu.findItem(R.id.member_sms);
        MenuItem mailItem = menu.findItem(R.id.member_mail);

        if (user != null) {
           callItem.setEnabled(true);
           smsItem.setEnabled(true);
           mailItem.setEnabled(true);
        }
        else{
            smsItem.setEnabled(false);
            callItem.setEnabled(false);
            mailItem.setEnabled(false);
        }

    }

    @Override
    public final boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Intent intent;
        User user = memberAdapter.getItem(info.position);

        switch (item.getItemId()) {
            case R.id.member_call:
                intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + user.getPhone()));
                startActivity(intent);
                return true;

            case R.id.member_sms:
                intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:" + user.getPhone()));
                startActivity(intent);
                return true;

            case R.id.member_mail:
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        SharedPreferences preferences = ((MainActivity) getActivity()).getPrefs();
        float font_size = preferences.getFloat("font_size", 15);

        if(memberAdapter == null) {
            memberAdapter = new AdapterMembers(rootView.getContext(), R.layout.adapter_member_item, members, font_size);
            memberAdapter.setNotifyOnChange(true);
        }

        ListView listView = (ListView) rootView.findViewById(R.id.memberList);
        listView.setAdapter(memberAdapter);
    }

    public final class ReadMembersAsync extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            members.clear();
            /* We want to show all numbers in the memberlist, thats why we set onlyValidUsers
             * to false below.
             */
            members.addAll(((MainActivity) getActivity()).sidanAccess().readMembers(false));
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

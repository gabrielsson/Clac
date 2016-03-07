package cl.sidan.clac.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;
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

    private View rootView;

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_member_list, container, false);

        if(memberAdapter == null) {
            float font_size = ((MainActivity) getActivity()).getPrefs().getFloat("font_size", 15);
            SharedPreferences preferences = ((MainActivity) getActivity()).getPrefs();
            HashSet<String> ignoredMembers = (HashSet<String>) preferences.getStringSet("ignoredMembers", new HashSet<String>());

            memberAdapter = new AdapterMembers(inflater.getContext(), R.layout.adapter_member_item, members, font_size);
            memberAdapter.setIgnoredMembers(ignoredMembers);
            memberAdapter.setNotifyOnChange(true);
        }

        ListView memberList = (ListView) rootView.findViewById(R.id.memberList);
        memberList.setAdapter(memberAdapter);
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

        MenuItem ignoreItem = menu.findItem(R.id.member_ignore);
        MenuItem callItem = menu.findItem(R.id.member_call);
        MenuItem smsItem = menu.findItem(R.id.member_sms);
        MenuItem mailItem = menu.findItem(R.id.member_mail);

        SharedPreferences preferences = ((MainActivity) getActivity()).getPrefs();
        HashSet<String> ignoredMembers = (HashSet<String>) preferences.getStringSet("ignoredMembers", new HashSet<String>());
        if ( ignoredMembers.contains(user.getSignature()) ) {
            ignoreItem.setTitle("Ignorerad");
        }

        ignoreItem.setEnabled(!user.isValid());

        if ( null == user.getPhone() || user.getPhone().isEmpty() ) {
            callItem.setEnabled(false);
            smsItem.setEnabled(false);
        }
        if ( null == user.getEmail() || user.getEmail().isEmpty() ) {
            mailItem.setEnabled(false);
        }
    }

    @Override
    public final boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Intent intent;
        User user = memberAdapter.getItem(info.position);

        switch (item.getItemId()) {
            case R.id.member_ignore:
                // Ignorera mera.
                SharedPreferences preferences = ((MainActivity) getActivity()).getPrefs();
                HashSet<String> ignoredMembers = (HashSet<String>) preferences.getStringSet("ignoredMembers", new HashSet<String>());
                if ( ignoredMembers.contains(user.getSignature()) ) {
                    // unignore
                    Log.d("XXX_SWO", "The user is already ignored. Unignoring!");
                    ignoredMembers.remove(user.getSignature());
                } else {
                    // ignore
                    Log.d("XXX_SWO", "The user is not yet ignored. Ignoring " + user.getSignature() + "!");
                    ignoredMembers.add(user.getSignature());
                }
                preferences.edit().putStringSet("ignoredMembers", ignoredMembers).apply();
                memberAdapter.setIgnoredMembers(ignoredMembers);
                memberAdapter.notifyDataSetInvalidated();

                return true;

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
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ user.getEmail() });
                intent.putExtra(Intent.EXTRA_TEXT, "\n\n\nMvh\n" + ((MainActivity) getActivity()).getPrefs().getString("username", "#"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "");

                startActivity(intent);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        /* XXX_SWO: I don't think any of this happends/need to happen */
        if(memberAdapter == null) {
            SharedPreferences preferences = ((MainActivity) getActivity()).getPrefs();
            float font_size = preferences.getFloat("font_size", 15);

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

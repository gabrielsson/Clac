package cl.sidan.clac.fragments;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import cl.sidan.clac.MainActivity;
import cl.sidan.clac.R;
//import cl.sidan.clac.interfaces.ConnectionUtil;
//import cl.sidan.util.GCMUtil;

public class FragmentLogin extends Fragment {

    public static FragmentLogin newInstance() {
        return new FragmentLogin();
    }

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        setMenuVisibility(false);
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        Button login = (Button) rootView.findViewById(R.id.login);

        final SharedPreferences preferences = ((MainActivity) getActivity()).getPrefs();
        ((EditText) rootView.findViewById(R.id.nummer)).setText(preferences.getString("nummer", "#"));
        ((EditText) rootView.findViewById(R.id.password)).setText(preferences.getString("password", ""));

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("XXX_SWO", "Clicked button.");
                String nummer = ((EditText) rootView.findViewById(R.id.nummer)).getText().toString();
                String password = ((EditText) rootView.findViewById(R.id.password)).getText().toString();
                Log.d("XXX_SWO", "nummer from activity = " + nummer);
                preferences.edit().putString("nummer", nummer).apply();
                preferences.edit().putString("password", password).apply();

                /* Vi behöver någon form av validering här. */

                    getActivity().recreate();
                //checkForGCMAndTryToRetrieveIfMissing();

                //((MainActivity) getActivity()).removeLogin();
            }
        });

        TextView offline = (TextView) getActivity().findViewById(R.id.offline_info);
        Boolean isConnected = true; // ConnectionUtil.isNetworkConnected(getActivity());
        if (offline != null) {
            if (isConnected) {
                offline.setVisibility(View.INVISIBLE);
            } else {
                offline.setVisibility(View.VISIBLE);
            }
        } else if (!isConnected) {
            Log.e("Connection", "Not connected!");
        }

        setHasOptionsMenu(false);
        setMenuVisibility(false);

        if( isConnected && ((MainActivity) getActivity()).loggedIn() ) {
            setHasOptionsMenu(true);
            setMenuVisibility(true);
            ((MainActivity) getActivity()).removeLogin();
        }

        return rootView;
    }


    @SuppressWarnings("unchecked")
    private void checkForGCMAndTryToRetrieveIfMissing() {
        // if( !GCMUtil.isRegistered(getActivity().getApplicationContext()) ){
            new AsyncTask(){
                @Override
                protected Object doInBackground(Object[] objects) {
                    Log.d("XXX_SWO", "Checks GCM or retrieve.");
                    String androidId = Settings.Secure.getString(getActivity().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                    String regId = ((MainActivity) getActivity()).sidanAccess().getGCMRegIdFromDeviceId(androidId);
                    if( regId != null ) {
                        SharedPreferences.Editor editor = ((MainActivity) getActivity()).getPrefs().edit();
                        // editor.putString(GCMUtil.PREFS_REG_ID_KEY, regId).apply();
                    }
                    return null;
                }
            }.execute(null,null,null);
        // }
    }

}

package cl.sidan.clac.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cl.sidan.clac.MainActivity;
import cl.sidan.clac.R;
import cl.sidan.clac.access.interfaces.User;

public class FragmentChangePassword extends Fragment {

    private List<User> userList;
    private ArrayList<String> userNameList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter = null;

    public FragmentChangePassword() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ReadMembersAsync().execute();

    }

    @Override
    public void onResume() {
        super.onResume();
        new ReadMembersAsync().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        final Spinner numberSpinner = (Spinner) view.findViewById(R.id.change_username);
        final EditText adminPassword = (EditText) view.findViewById(R.id.admin_password);
        numberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String userName = (String) numberSpinner.getSelectedItem();
                if ( userName.equals( ((MainActivity) getActivity()).whoAmI() ) ) {
                    adminPassword.setVisibility(View.GONE);
                } else {
                    adminPassword.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (spinnerAdapter == null) {
            userNameList.clear();
            userNameList.add( ((MainActivity) getActivity()).whoAmI() );
            spinnerAdapter = new ArrayAdapter<>(
                    getActivity(), android.R.layout.simple_spinner_item, userNameList);
            numberSpinner.setAdapter(spinnerAdapter);
        }

        Button changePasswordButton = (Button) view.findViewById(R.id.change_password_button);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ChangePasswordAsync().execute();
            }
        });
        return view;
    }

    private void populateUserNames() {
        // run in background
        userList = ((MainActivity) getActivity()).sidanAccess().readMembers(true);

        userNameList.clear();
        for (User u : userList) {
            userNameList.add(u.getSignature());
        }

        // run on ui thread

        /* Möjlig Nullpekare här. Om både denna kör och någon annan async-task körs
         * eller något. Lite oklart varför. */
        if ( getActivity() != null ) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    spinnerAdapter.notifyDataSetChanged();

                    Log.d("XXX_SWO", "WhoAmI Position " +  spinnerAdapter.getPosition(((MainActivity) getActivity()).whoAmI()));
                    Spinner numberSpinner = (Spinner) getActivity().findViewById(R.id.change_username);
                    numberSpinner.setSelection(spinnerAdapter.getPosition(((MainActivity) getActivity()).whoAmI()));
                }
            });
        }
    }

    public void onChangePassword() {
        Spinner spinner = (Spinner) getActivity().findViewById(R.id.change_username);
        String userName = (String) spinner.getSelectedItem();
        EditText passWordEditText = (EditText) getActivity().findViewById(R.id.change_password);
        EditText adminEditText = (EditText) getActivity().findViewById(R.id.admin_password);
        String adminPassword = adminEditText.getText().toString();
        String password = passWordEditText.getText().toString();

        ((MainActivity) getActivity()).sidanAccess().updatePassword(userName, password, adminPassword);

        if ( userName.equals( ((MainActivity) getActivity()).whoAmI() ) ) {
            ((MainActivity) getActivity()).logOut();
        }

        passWordEditText.setText("");
        adminEditText.setText("");

        Toast.makeText(getContext(), "Lösenord uppdaterat!", Toast.LENGTH_SHORT).show();
    }

    public final class ReadMembersAsync extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            populateUserNames();
            return null;
        }
    }

    public final class ChangePasswordAsync extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            onChangePassword();
            return true;
        }
    }
}

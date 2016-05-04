package cl.sidan.clac.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
    private ArrayList<String> userNameList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter = null;
    private View rootView;

    @Override
    public void onResume() {
        super.onResume();
        new ReadMembersAsync().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_change_password, container, false);

        final Spinner numberSpinner = (Spinner) rootView.findViewById(R.id.change_username);
        final EditText adminPassword = (EditText) rootView.findViewById(R.id.admin_password);
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

        Button changePasswordButton = (Button) rootView.findViewById(R.id.change_password_button);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner spinner = (Spinner) rootView.findViewById(R.id.change_username);
                String userName = (String) spinner.getSelectedItem();
                EditText passWordEditText = (EditText) rootView.findViewById(R.id.change_password);
                EditText adminEditText = (EditText) rootView.findViewById(R.id.admin_password);
                String adminPassword = adminEditText.getText().toString();
                String password = passWordEditText.getText().toString();

                Toast.makeText(getContext(), "Uppdaterar lösenord.", Toast.LENGTH_SHORT).show();
                new ChangePasswordAsync().execute(new UserPassword(userName, password, adminPassword));
            }
        });

        return rootView;
    }

    public final class ReadMembersAsync extends AsyncTask<String, Void, List<User>> {
        @Override
        protected List<User> doInBackground(String... strings) {
            return ((MainActivity) getActivity()).sidanAccess().readMembers(true);
        }
        @Override
        protected void onPostExecute(List<User> userList) {
            if (!userList.isEmpty()) {
                userNameList.clear();
                for (User u : userList) {
                    userNameList.add(u.getSignature());
                }
                spinnerAdapter.notifyDataSetChanged();

                String whoAmI = ((MainActivity) getActivity()).whoAmI();
                int whoAmIPos = spinnerAdapter.getPosition(whoAmI);
                Spinner numberSpinner = (Spinner) rootView.findViewById(R.id.change_username);
                numberSpinner.setSelection(whoAmIPos);
            } else {
                Toast.makeText(getActivity(), "Kunde inte hämta andra medlemmar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public final class ChangePasswordAsync extends AsyncTask<UserPassword, Void, Boolean> {
        @Override
        protected Boolean doInBackground(UserPassword... params) {
            Boolean b = ((MainActivity) getActivity()).sidanAccess().updatePassword(
                    params[0].username, params[0].password, params[0].adminPassword);

            if ( params[0].username.equals( ((MainActivity) getActivity()).whoAmI() ) ) {
                ((MainActivity) getActivity()).logOut();
            }

            return b;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            if (b) {
                EditText passWordEditText = (EditText) rootView.findViewById(R.id.change_password);
                EditText adminEditText = (EditText) rootView.findViewById(R.id.admin_password);

                passWordEditText.setText("");
                adminEditText.setText("");

                Toast.makeText(getContext(), "Lösenord uppdaterat!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Misslyckades med lösenordsbytet. Testa senare.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UserPassword {
        String username, password, adminPassword;
        UserPassword(String username, String password, String adminPassword) {
            this.username = username;
            this.password = password;
            this.adminPassword = adminPassword;
        }
    }
}

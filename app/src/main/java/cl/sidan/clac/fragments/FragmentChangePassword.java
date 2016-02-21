package cl.sidan.clac.fragments;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import cl.sidan.clac.MainActivity;
import cl.sidan.clac.R;
import cl.sidan.clac.access.interfaces.User;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentChangePassword.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentChangePassword#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentChangePassword extends Fragment {
    // TODO: Rename parameter arguments, choose names that match

    private List<User> userList;

    private OnFragmentInteractionListener mListener;

    public FragmentChangePassword() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentChangePassword.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentChangePassword newInstance(String param1, String param2) {
        FragmentChangePassword fragment = new FragmentChangePassword();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new ReadMembersAsync().execute();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void populateUserNames() {
        //run in background
        userList = ((MainActivity) getActivity()).sidanAccess().readMembers(true);

        //run on ui thread
          /* Möjlig Nullpekare här. Om både denna kör och versionsasync eller något. */
        if( getActivity() != null ) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Spinner spinner = (Spinner) getActivity().findViewById(R.id.change_username);

                    List<String> userNameList = new ArrayList<String>();
                    for (User u: userList) {
                        userNameList.add(u.getSignature());
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, userNameList);

                    spinner.setAdapter(spinnerAdapter);
                    spinner.setSelection(spinnerAdapter.getPosition(((MainActivity)getActivity()).number));
                }
            });
        }




    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);
        // Inflate the layout for this fragment
        Button changePasswordButton = (Button) view.findViewById(R.id.change_password_button);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ChangePasswordAsync().execute();
            }
        });
        return view;
    }


    public void onChangePassword() {
        Spinner spinner = (Spinner)getActivity().findViewById(R.id.change_username);
        String userName = (String) spinner.getSelectedItem();
        EditText passWordEditText = (EditText) getActivity().findViewById(R.id.change_password);
        EditText adminEditText = (EditText) getActivity().findViewById(R.id.admin_password);
        String adminPassword = adminEditText.getText().toString();
        String password = passWordEditText.getText().toString();

        ((MainActivity)getActivity()).sidanAccess().updatePassword(userName, password, adminPassword);

        if(userName.equals(((MainActivity)getActivity()).number)) {
            ((MainActivity) getActivity()).logOut();
        }
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
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

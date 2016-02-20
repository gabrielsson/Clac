package cl.sidan.clac.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cl.sidan.clac.MainActivity;
import cl.sidan.clac.R;
import cl.sidan.clac.access.interfaces.User;

/**
 * Created by Christofer on 2016-02-20.
 */
public class FragmentMembers extends Fragment {

    private List<User> members = new ArrayList<>();
    //((MainActivity) getActivity()).sidanAccess().readMembers()

    public FragmentMembers() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_members, container, false);
    }
}

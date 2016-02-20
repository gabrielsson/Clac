package cl.sidan.clac.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cl.sidan.clac.R;

/**
 * Created by Christofer on 2016-02-20.
 */
public class FragmentMembers extends Fragment {

    public FragmentMembers() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_members, container, false);
    }
}

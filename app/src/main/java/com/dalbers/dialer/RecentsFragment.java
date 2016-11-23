package com.dalbers.dialer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.dalbers.dialer.DataHandling.Recent;

import java.util.ArrayList;

public class RecentsFragment extends Fragment {
    private String mParam1;
    private String mParam2;
    private RecyclerView recentsList;
    private LinearLayoutManager layoutManager;
    private RecentsAdapter adapter;
    private ArrayList<Recent> recents;
    public RecentsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            recents = getArguments().getParcelableArrayList("recents");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recents, container, false);
        recentsList = (RecyclerView) view.findViewById(R.id.recents_list);

        recentsList.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getContext());
        recentsList.setLayoutManager(layoutManager);

        if(recents != null && recents.size() > 0) {
            adapter = new RecentsAdapter(recents, true, getContext());
            recentsList.setAdapter(adapter);
        }

        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }
}

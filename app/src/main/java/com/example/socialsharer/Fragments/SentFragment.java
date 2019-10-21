package com.example.socialsharer.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.socialsharer.R;

/**
 *
 */
public class SentFragment extends ContactsFragment {
    private static final String TAG = "SentRequestFragment";

    public SentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setState(1);
        setState2(2);
        setInfo1("Waiting");
        setInfo2("Rejected");
        setLayoutId(R.layout.fragment_sent_requests);
        setEmptyId(R.id.sent_requests_empty);
        setListViewId(R.id.sent_requests_list);
        setSearchId(R.id.search_sent_requests);
        setFragmentState("sent_request_fragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }
}

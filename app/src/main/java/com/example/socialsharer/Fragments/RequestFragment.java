package com.example.socialsharer.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.socialsharer.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import io.opencensus.metrics.LongGauge;

/**
 * This class extends ContactsFragment, all setting are set up in that fragments
 * Only need to change parameters.
 * This class displays received requests
 */
public class RequestFragment extends ContactsFragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "RequestFragment";

    public RequestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set parameters used in received request fragment
        setState(4);
        setLayoutId(R.layout.fragment_requests);
        setEmptyId(R.id.requests_empty);
        setListViewId(R.id.requests_list);
        setSearchId(R.id.search_requests);
        setFragmentState("request_fragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }
}

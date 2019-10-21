package com.example.socialsharer.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.socialsharer.CommonFunctions;
import com.example.socialsharer.ContactProfileActivity;
import com.example.socialsharer.R;
import com.example.socialsharer.data.Contact;
import com.example.socialsharer.data.ContactAdapter;
import com.example.socialsharer.data.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *  Used for display contacts
 */
public class ContactsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "ContactsFragment";

    private int state = 3;
    private int state2 = -1;
    private String local_path;
    private String userEmail;
    private long userNumber;
    private ArrayList<Contact> contactList;
    private ListView listView;
    private ContactAdapter contactAdapter;
    private ArrayList<String> contacts;
    private StorageReference storageRef;
    private FirebaseFirestore db;
    private SearchView search;
    private ArrayList<User> contactUserList = new ArrayList<>();
    private int emptyId = R.id.empty;
    private int listViewId = R.id.contact_list;
    private int layoutId = R.layout.fragment_contacts;
    private int searchId = R.id.search_contacts;
    private String fragmentState = "contact_fragment";
    private boolean firstOpen = true;
    private ProgressDialog dialog;
    private String info1;
    private String info2;

    public ContactsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContactsFragment.
     */
    private static ContactsFragment newInstance(String param1, String param2) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            userEmail = bundle.getString("email");
            userNumber = bundle.getLong("userNumber");
        }
        storageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(layoutId, container, false);

        listView = view.findViewById(listViewId);
        search = view.findViewById(searchId);

        search.setVisibility(View.INVISIBLE);

        contactList = new ArrayList<>();
        listView.setEmptyView(view.findViewById(emptyId));
        contactAdapter = new ContactAdapter(getContext(),contactList);
        listView.setAdapter(contactAdapter);

        listView.setVisibility(View.INVISIBLE);

        updateList(state, state2, info1, info2);

        String display;
        if (fragmentState.equals("contact_fragment")){
            display = "contacts";
        } else if (fragmentState.equals("request_fragment")){
            display = "requests";
        } else {
            display = "previous sent requests";
        }

        dialog = ProgressDialog.show(getContext(), "",
                "Loading " + display + ". Please wait...", true);

        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View view,
                                            int position, long id) {
                        Intent intent = new Intent(getActivity(), ContactProfileActivity.class);
                        intent.putExtra("state", fragmentState);
                        Log.i(TAG, userEmail);
                        User clickedUser = contactUserList.get(position);
                        intent.putExtra("contact", clickedUser);
                        intent.putExtra("userEmail", userEmail);
                        startActivity(intent);
                    }
                });
        return view;
    }

    private void downloadImage(final String userEmail, final String info) {
        final String path = userEmail + "/Photo";
        StorageReference imageRef = storageRef.child(path);
        try {
            final File localFile = File.createTempFile("images", "jpg");
            imageRef.getFile(localFile).addOnSuccessListener(
                    new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Log.i(TAG, "User image Downloaded");
                            local_path = localFile.getAbsolutePath();
                            addUserToList(userEmail, local_path, info);
                            dismissDialog();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    dismissDialog();
                    addUserToList(userEmail, null, info);
                    Log.i(TAG, "Fail to download user image, using default");
                }
            });
        } catch (Exception e) {
            dismissDialog();
            Toast.makeText(getActivity(), "Fail to load contacts information," +
                    " check your internet connection.", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Fail to create temp file");
        }
    }

    private void addUserToList(final String email, final String path, final String info){
        final DocumentReference docRef = db.collection("users")
                .document(email);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String displayName = email;
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        User user = CommonFunctions.createUser(document, path);
                        contactUserList.add(user);
                        Log.i(TAG, "Adding user: " + user.getEmail());
                        Log.i(TAG, "Users nick name: " + user.getNickName());
                        Log.i(TAG, "Current contact list size: " + contactUserList.size());
                        String username = user.getNickName();
                        if(username != null) {
                            displayName = username;
                        }
                    }
                } else {
                    Log.i(TAG, "Connect to fire base failed, check internet connection");
                }
                String displayName_withInfo = displayName;
                if (info != null){
                    displayName_withInfo += " (" + info + ")";
                }
                if (path == null) {
                    contactList.add(new Contact(displayName_withInfo, R.drawable.unknown));
                } else {
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                            bitmap, 48, 48, false);
                    contactList.add(new Contact(displayName_withInfo, scaledBitmap));
                }
                search.setVisibility(View.VISIBLE);
                contactAdapter.notifyDataSetChanged();
            }
        });
    }

    public void updateList(long state1, long state2, String info1, String info2){
        if (state2 != -1){
            getList(state1, info1);
            getList(state2, info2);
        } else {
            getList(state1, null);
        }
    }

    public void getList(final long state, final String info){
        final DocumentReference documentRef = db.collection("request")
                .document(userEmail);

        documentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    int contact_number = 0;
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        Map<String, Object> requests;
                        requests = document.getData();
                        if (requests.size() != 0) {
                            // Current user has potential requests/contacts
                            Set users = requests.keySet();
                            for(Object user: users){
                                if((long) requests.get(user) == state){
                                    downloadImage((String) user, info);
                                    contact_number += 1;
                                }
                            }
                            if (contact_number == 0){
                                // No requests/contacts
                                dismissDialog();
                            }
                        } else {
                            // Current user has no requests/contacts,
                            // but request document already exist
                            dismissDialog();
                        }
                    } else {
                        // Current user has no requests/contacts
                        dismissDialog();
                    }
                } else {
                    if(isAdded()){
                        // Internet fail, can not reach fire base
                        dismissDialog();
                        Toast.makeText(getActivity(), "Fail to load contacts information," +
                                " check your internet connection.", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Fail to get user friends, check internet connection");
                    }
                }
            }
        });
    }

    private void dismissDialog(){
        if(dialog != null) {
            dialog.dismiss();
        }
        if(listView.getVisibility()!= View.INVISIBLE) {
            listView.setVisibility(View.VISIBLE);
        }
        contactAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(firstOpen){
            firstOpen = false;
        } else {
            contactList.clear();
            contactUserList.clear();
            contactAdapter.notifyDataSetChanged();
            updateList(state, state2, info1, info2);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    void setState(int state){
        this.state = state;
    }

    void setState2(int state){
        this.state2 = state;
    }

    void setEmptyId(int id){
        this.emptyId = id;
    }

    void setListViewId(int id){
        this.listViewId = id;
    }

    void setLayoutId(int id){
        this.layoutId = id;
    }

    void setSearchId(int id){
        this.searchId = id;
    }

    void setFragmentState(String state){
        this.fragmentState = state;
    }

    void setInfo1(String info1){
        this.info1 = info1;
    }

    void setInfo2(String info2){
        this.info2 = info2;
    }
}

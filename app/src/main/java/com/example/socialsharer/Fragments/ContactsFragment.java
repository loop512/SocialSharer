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
import java.util.Map;
import java.util.Set;

/**
 *  This is the class used for display contacts of login user.
 */
public class ContactsFragment extends Fragment {

    private static final String TAG = "ContactsFragment";

    // state, state2 are used to query data base, and can be reset by calling set functions
    // detailed state document is in the bottom main Activity functions:
    // listener, compare, compare toast
    private int state = 3;
    private int state2 = -1;
    // Store user image's local path, user email, the current contact list
    private String local_path;
    private String userEmail;
    private ArrayList<Contact> contactList;
    private ListView listView;
    private ContactAdapter contactAdapter;
    // Database objects used for querying the database
    private StorageReference storageRef;
    private FirebaseFirestore db;
    // A search view on top of the screen
    private SearchView search;
    // Stores detailed contacts information which are User objects
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
     * Connect to the database and get current user's email from main activity
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            userEmail = bundle.getString("email");
        }
        storageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(layoutId, container, false);

        // A search bar on top of the screen and a dynamic list view.
        listView = view.findViewById(listViewId);
        search = view.findViewById(searchId);

        // First set to invisible while querying data base in case there are no contacts
        search.setVisibility(View.INVISIBLE);

        // Set up dynamic list adapter
        contactList = new ArrayList<>();
        listView.setEmptyView(view.findViewById(emptyId));
        contactAdapter = new ContactAdapter(getContext(),contactList);
        listView.setAdapter(contactAdapter);

        // First set to invisible until query finish or fail
        listView.setVisibility(View.INVISIBLE);

        // This function queries database
        updateList(state, state2, info1, info2);

        // Controls whether displaying contacts, received requests or previous sent requests.
        String display;
        if (fragmentState.equals("contact_fragment")){
            display = "contacts";
        } else if (fragmentState.equals("request_fragment")){
            display = "requests";
        } else {
            display = "previous sent requests";
        }

        // A progress dialog, dismiss when the query finish or failed.
        dialog = ProgressDialog.show(getContext(), "",
                "Loading " + display + ". Please wait...", true);

        // Set on click listener
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View view,
                                            int position, long id) {
                        // Start next activity to show detailed information,
                        // and pass necessary information to that activity
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

    /**
     * This method downloads given user's image from the database,
     * and put necessary information (Only for sent request) together
     * in the contactUserList for later display usage.
     * Using default image if that user haven't upload his image
     * @param userEmail the user's email used for downloading that user's image
     * @param info Only used for sent request, display whether the request is waiting or rejected.
     */
    private void downloadImage(final String userEmail, final String info) {
        // Path in database which stores the photo
        final String path = userEmail + "/Photo";
        StorageReference imageRef = storageRef.child(path);
        try {
            // Create a temp file for storing the image
            final File localFile = File.createTempFile("images", "jpg");
            imageRef.getFile(localFile).addOnSuccessListener(
                    new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // User image successfully downloaded,
                            // get the path and stores in User object
                            Log.i(TAG, "User image Downloaded");
                            local_path = localFile.getAbsolutePath();
                            addUserToList(userEmail, local_path, info);
                            dismissDialog();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Fail to download user image, using default image
                    dismissDialog();
                    addUserToList(userEmail, null, info);
                    Log.i(TAG, "Fail to download user image, using default");
                }
            });
        } catch (Exception e) {
            // Connection error, need user to check permission or internet connection.
            // Usually won't happened, permission already asked.
            dismissDialog();
            Toast.makeText(getActivity(), "Fail to load contacts information," +
                    " check your internet connection.", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Fail to create temp file");
        }
    }

    /**
     * This function can aad user to list,
     * with their email the function queries database
     * and then construct the object of User class
     * @param email the target user's email
     * @param path target user's image stored path
     * @param info extra information to display (only used for sent requests)
     */
    private void addUserToList(final String email, final String path, final String info){
        // Create database reference
        final DocumentReference docRef = db.collection("users")
                .document(email);
        //Query data base
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String displayName = email;
                if (task.isSuccessful()){
                    // Successfully connected to the database
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        // Current query user has information documents
                        User user = CommonFunctions.createUser(document, path);
                        contactUserList.add(user);
                        Log.i(TAG, "Adding user: " + user.getEmail());
                        Log.i(TAG, "Users nick name: " + user.getNickName());
                        Log.i(TAG, "Current contact list size: " + contactUserList.size());
                        String username = user.getNickName();
                        if(username != null) {
                            // If user doesn't provide a nick name, using email instead
                            displayName = username;
                        }
                    }
                } else {
                    // Fail on connect to data base
                    Log.i(TAG, "Connect to fire base failed, check internet connection");
                }
                String displayName_withInfo = displayName;
                if (info != null){
                    // Only used to display extra information for sent request
                    displayName_withInfo += " (" + info + ")";
                }
                if (path == null) {
                    // This user's image is not provided and using default
                    contactList.add(new Contact(displayName_withInfo, R.drawable.unknown));
                } else {
                    // Using the downloaded image provided by that user
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                            bitmap, 48, 48, false);
                    contactList.add(new Contact(displayName_withInfo, scaledBitmap));
                }
                // Set search visible since contacts/request are available
                search.setVisibility(View.VISIBLE);
                contactAdapter.notifyDataSetChanged();
            }
        });
    }

    public void updateList(long state1, long state2, String info1, String info2){
        if (state2 != -1){
            // This fragment is sent request fragment, need to check two state on the database
            // state1 -> sent request by current user but not respond yet
            // state2 -> sent request by current user and rejected by received user
            getList(state1, info1);
            getList(state2, info2);
        } else {
            // This fragment is received request or contact,
            // only need to retrieve received request or contact information
            getList(state1, null);
        }
    }

    /**
     * This function retrieve all the contacts/request information of current login user
     * @param state query state, detailed state introduction is in main activity
     * @param info extra info to display whether a request is accepted or rejected
     */
    public void getList(final long state, final String info){
        // Set database reference
        final DocumentReference documentRef = db.collection("request")
                .document(userEmail);

        documentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    // Connected to fire base
                    int contact_number = 0;
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        // Current user's contacts/requests document exist
                        Map<String, Object> requests;
                        requests = document.getData();
                        if (requests.size() != 0) {
                            // Current user has potential requests/contacts
                            Set users = requests.keySet();
                            for(Object user: users){
                                // Retrieve all the information
                                // for contacts/requests in the document
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

    /**
     * This function dismiss the process dialog and display the list view
     */
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
            // Clear all the previous retrieved information and re-recommend users
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

    /**************************************************************************************
     * Following parts are functions used for setting up states and usefull
     * parameters.
     * state1, state2 -> Parameters used for querying database
     * emptyId -> Used for controlling displaying information in empty list view
     * listViewId -> Used for controlling which list view to display
     * searchId -> Used for controlling which search bar(different default hint) to display
     * fragmentState -> Determine whether this fragment should display requests or contacts
     * info1, info2 -> Information displayed on each row when request are waiting or rejected
     **************************************************************************************/

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

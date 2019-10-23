package com.example.socialsharer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.socialsharer.Fragments.ContactsFragment;
import com.example.socialsharer.Fragments.MapShareFragment;
import com.example.socialsharer.Fragments.ProfileFragment;
import com.example.socialsharer.Fragments.QRShareFragment;
import com.example.socialsharer.Fragments.RequestFragment;
import com.example.socialsharer.Fragments.SentFragment;
import com.example.socialsharer.Fragments.SettingsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.Map;
import java.util.Set;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * The main activity after login, shows the fragment list
 * Also listen to database change to notify current user
 * about new requests and contacts
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    public static int previousItemId = 0;
    public static int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private static final String TAG = "MainActivity";
    private static final String PHOTO = "Photo";

    public DrawerLayout drawer;
    private String userEmail;
    private long userNumber;
    private FirebaseFirestore db;
    private CircleImageView profileImage;
    private TextView profileName;
    private TextView profileEmail;
    private Map<String, Object> currentRequest;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissions();
        db = FirebaseFirestore.getInstance();

        // Authenticate current user
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference(mAuth.getCurrentUser().getEmail());

        // Load necessary information
        SharedPreferences shared = getSharedPreferences("SharedInformation", MODE_PRIVATE);
        userNumber = shared.getLong("userNumber", 0);
        userEmail = mAuth.getCurrentUser().getEmail();

        Log.i(TAG, "Register email: " + userEmail + " userNumber: " + userNumber);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setCheckedItem(R.id.nav_profile);
        Fragment fragment = new ProfileFragment();
//        fragment.setArguments(bundle);
        setTitle(R.string.menu_profile);
        loadFragment(fragment, "profile");
        navigationView.setNavigationItemSelectedListener(this);

        profileImage = navigationView.getHeaderView(0).findViewById(R.id.drawer_profile_image);
        profileName = navigationView.getHeaderView(0).findViewById(R.id.drawer_profile_name);
        profileEmail = navigationView.getHeaderView(0).findViewById(R.id.drawer_profile_email);
        getBasicProfile();

        // Set up database listener
        listener();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        if (previousItemId == id) {
            drawer.closeDrawer(GravityCompat.START);
            return false;
        }

        // Put information that need to pass to each fragments
        Bundle bundle = new Bundle();
        bundle.putString("email", userEmail);
        bundle.putLong("userNumber", userNumber);

        String tag;
        if (id == R.id.nav_profile) {
            fragment = new ProfileFragment();
            tag = "profile";
        } else if (id == R.id.nav_qrshare) {
            fragment = new QRShareFragment();
            tag = "qrshare";
        } else if (id == R.id.nav_mapshare) {
            fragment = new MapShareFragment();
            tag = "mapshare";
        } else if (id == R.id.nav_contacts) {
            fragment = new ContactsFragment();
            tag = "contacts";
        } else if (id == R.id.nav_settings) {
            fragment = new SettingsFragment();
            tag = "settings";
        } else if (id == R.id.nav_requests){
            fragment = new RequestFragment();
            tag = "requests";
        } else if (id == R.id.nav_sent_requests){
            fragment = new SentFragment();
            tag = "sent_requests";
        }
        else {
            fragment = new ProfileFragment();
            tag = "profile";
        }
        fragment.setArguments(bundle);
        previousItemId = item.getItemId();
        drawer.closeDrawer(GravityCompat.START);
        loadFragment(fragment, tag);

        item.setChecked(true);
        setTitle(item.getTitle());
        return true;
    }

    private void loadFragment(Fragment fragment, String tag) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment, tag);
        if(!tag.equals("mapshare")) {
            // Put fragments in to stack
            fragmentTransaction.addToBackStack(tag);
        }
        fragmentTransaction.commit();
    }

    public void openMapActivity(){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    /**
     * this method request to permission asked.
     */
    private void requestPermissions() {
        // Check whether should ask for permission
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
        boolean shouldProvideRationale_storage =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);

        String[] permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
        };

        if (shouldProvideRationale && shouldProvideRationale_storage) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
        } else {
            Log.i(TAG, "Requesting permission");
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(this,
                    permissions,
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void getBasicProfile(){
        storageRef.child(PHOTO).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d("URI", uri.toString());
                Glide.with(MainActivity.this).load(uri).into(profileImage);
                Log.i(TAG, "Main start");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Load Image Exception", e.toString());
                profileImage.setImageResource(R.drawable.unknown);
            }
        });
        profileName.setText(mAuth.getCurrentUser().getDisplayName());
        profileEmail.setText(mAuth.getCurrentUser().getEmail());
    }

    /**
     * This function listen to the database for real time changes
     * and inform the login user about new received/rejected request
     * or new accepted/deleted contacts
     */
    private void listener(){
        final DocumentReference requestDocRef =
                db.collection("request").document(userEmail);
        requestDocRef.addSnapshotListener(MetadataChanges.INCLUDE,
                new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Log.i(TAG, "Fail to listen to request," +
                            " still can manually receive request");
                }

                // Get all data from database
                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                // If data changed compare changes in the data
                if (snapshot != null && snapshot.exists()){
                    Map<String, Object> tempRequest = snapshot.getData();
                    compare(tempRequest);
                    Log.d(TAG, source + " data: " + snapshot.getData());
                } else {
                    Log.d(TAG, source + " data: null");
                }
            }
        });
    }

    /**
     * Comparing the data changes and decide what to toast.
     * @param temp A temp map object that stores all the data retrieved from database after change
     */
    void compare(Map<String, Object> temp){
        if (currentRequest == null){
            // initialize, record current state!
            currentRequest = temp;
        } else {
            // state changed!
            Set users = temp.keySet();
            for(Object user:users){
                if (!currentRequest.containsKey(user)
                        || currentRequest.get(user) != temp.get(user)){
                    // update found, compare and toast
                    long state = (long) temp.get(user);
                    final int toast_state;
                    final String default_toast;
                    Log.i(TAG, "Server new state: " + state);
                    if(state == 2){
                        if(currentRequest.containsKey(user)){
                            long current_state = (long)currentRequest.get(user);
                            Log.i(TAG, Long.toString(current_state));
                            if (current_state == 1){
                                // request rejected by that user
                                toast_state = 2;
                                default_toast = "You have an rejected request," +
                                        " check your sent requests";
                                Log.i(TAG, "new rejection");
                            } else {
                                // removed by that user
                                toast_state = 1;
                                default_toast =
                                        "You have been deleted by a user, check your contacts.";
                                Log.i(TAG, "new deletion");
                            }
                        } else {
                            // removed by that user
                            toast_state = 1;
                            default_toast =
                                    "You have been deleted by a user, check your contacts.";
                            Log.i(TAG, "new deletion");
                        }
                    } else if (state == 3){
                        // request confirmed by that user
                        toast_state = 3;
                        default_toast = "You have a new confirmed request," +
                                " check your contacts.";
                    } else if (state == 4){
                        // received request from that user
                        toast_state = 4;
                        default_toast = "You received a new friend request," +
                                "check your received request.";
                    } else {
                        // No need to process, ignore changes
                        toast_state = -1;
                        default_toast = null;
                    }
                    compareToast(this, user, toast_state, default_toast);
                }
            }
            currentRequest = temp;
        }
    }

    /**
     * This function toast different messages based on comparison
     * @param activity Activity to toast
     * @param user The user who trigger this event
     * @param toast_state Decide to toast or not
     * @param default_toast The default information
     */
    private void compareToast(final Context activity, Object user,
                              final int toast_state, final String default_toast){
        if (toast_state != -1){
            // need to toast, but first check trigger user's database to get his nickname
            String email = (String) user;
            DocumentReference docRef = db.collection("users")
                    .document(email);
            docRef.get().addOnCompleteListener(
                    new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                // Connected to database
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()){
                                    // User exist
                                    if (document.get("nickName") != null){
                                        String username = (String) document.get("nickName");
                                        String toast;
                                        if (toast_state == 1){
                                            // been deleted by that user
                                            toast = "You have been deleted by " + username +
                                                    ", check your contacts.";
                                        } else if (toast_state == 2){
                                            // been rejected by that user
                                            toast = "Your request have been rejected by "
                                                    + username + ", check your sent requests";
                                        } else if (toast_state == 3) {
                                            // been confirmed by that user
                                            toast = "Your request have been confirmed by "
                                                    + username + ", check your sent requests";
                                        } else {
                                            // receive request from that user
                                            toast = "You received a new friend request from "
                                                    + username + ", check your received request.";
                                        }
                                        // toast user to inform the request state is changed
                                        Toast.makeText(activity,
                                                toast, Toast.LENGTH_SHORT).show();
                                    } else {
                                        // User has no nick name, using default
                                        Toast.makeText(activity,
                                                default_toast, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // Document not exist, use default, this usually won't happened.
                                    Toast.makeText(activity,
                                            default_toast, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Fail connect to fire base store database, use default toast
                                Toast.makeText(activity,
                                        default_toast, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}



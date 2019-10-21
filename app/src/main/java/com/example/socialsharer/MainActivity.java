package com.example.socialsharer;

import android.Manifest;
import android.app.Activity;
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
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.socialsharer.Fragments.CircleImage;
import com.example.socialsharer.Fragments.ContactsFragment;
import com.example.socialsharer.Fragments.MapShareFragment;
import com.example.socialsharer.Fragments.ProfileFragment;
import com.example.socialsharer.Fragments.QRShareFragment;
import com.example.socialsharer.Fragments.RequestFragment;
import com.example.socialsharer.Fragments.SettingsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
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

import de.hdodenhof.circleimageview.CircleImageView;

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

    private FirebaseAuth mAuth;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissions();
        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference(mAuth.getCurrentUser().getEmail());

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
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        String[] permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
        };

        if (shouldProvideRationale) {
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
}



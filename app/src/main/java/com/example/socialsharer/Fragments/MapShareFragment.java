package com.example.socialsharer.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.socialsharer.R;
import com.example.socialsharer.data.User;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Random;

public class MapShareFragment extends Fragment implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback, LocationListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "MapShareFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    MapView mMapView;
    private GoogleMap googleMap;
    private Double latitude;
    private Double longitude;
    private LocationManager locationManager;
    private FirebaseFirestore db;
    private Location location;
    private Handler timeHandler;
    private Handler recommendHandler;
    private Runnable timeRunnable;
    private Runnable recommendRunnable;
    private Boolean permission = false;
    private ArrayList<User> recomendUserList = new ArrayList();
    private String userEmail;
    private long userNumber;
    private ArrayList<Integer> selectedIndex = new ArrayList();
    private int targeNumber = 3;
    private String nickName;
        private CircleImage imageHandler = new CircleImage();
    private boolean firstTime = true;
    private StorageReference storageRef;
    public boolean imageDownload = false;
    private HashMap<String, String> fileLists = new HashMap<String, String>();

    public MapShareFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapShareFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapShareFragment newInstance(String param1, String param2) {
        MapShareFragment fragment = new MapShareFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        db = FirebaseFirestore.getInstance();

        Bundle bundle = getArguments();
        userEmail = bundle.getString("email");
        userNumber = bundle.getLong("userNumber");
        try{
            nickName = bundle.getString("nickName");
        } catch (Error error){
            nickName = null;
        }
        Log.i(TAG, "Register email: " + userEmail + " userNumber: " + userNumber);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map_share, container,
                false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately
        mMapView.getMapAsync(this);

        return v;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            permission = true;
            Log.i(TAG, "Initiate, user permission is granted");

            // Attach basic functions and buttons to google map
            googleMap.setMyLocationEnabled(true);
            googleMap.setOnMyLocationButtonClickListener(this);
            googleMap.setOnMyLocationClickListener(this);

            // Initiate the location manager, update user's current location and setup the timer.
            locationManager = (LocationManager) getActivity()
                    .getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    100,100,this);

            timeHandler = new Handler();
            timeUpdate();
            timeHandler.postDelayed(timeRunnable, 300000);

            Log.i(TAG, "Initiate success");

            randomSelect();
            for (int index: selectedIndex){
                getDocument(index);
                Log.i(TAG, "User: " + index);
            }

            recommendHandler = new Handler();
            recommendUser();
            recommendHandler.postDelayed(recommendRunnable, 1000);

        } else {
            Log.w(TAG, "User permission not granted");
            Toast.makeText(getActivity(),
                    "Location permission not granted, can not use \"Map Share\" function",
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        String messageToShow;
        String message = "!\nGo to find some new friends :)";
        if (nickName == null){
            messageToShow = "This is yourself" + message;
        } else {
            messageToShow = "Hey " + nickName + message;
        }
        Toast.makeText(getActivity(), messageToShow, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(firstTime){
            this.longitude = location.getLongitude();
            this.latitude = location.getLatitude();

            updateLocation(userEmail);
            // Move camera to current location when open the app
            LatLng latLng = new LatLng(this.latitude, this.longitude);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 8);
            googleMap.animateCamera(cameraUpdate);
            firstTime = false;

        }
        this.longitude = location.getLongitude();
        this.latitude = location.getLatitude();
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    // Calculate the distance between two points with give latitudes and longitudes
    public float calculateDistance(Double latitudeA, Double longitudeA,
                                    Double latitudeB, Double longitudeB){
        float[] results = new float[1];
        Location.distanceBetween(latitudeA, longitudeA,
                latitudeB, longitudeB,
                results);
        return results[0];
    }

    @Override
    public void onStop() {
        // Called when fragment is stop
        super.onStop();
        if(timeRunnable != null){
            timeHandler.removeCallbacks(timeRunnable);
            Log.i(TAG, "Stop of fragment, timer is removed");
        }
        if (permission){
            updateLocation(userEmail);
        }
    }

    @Override
    public void onPause() {
        // Called when app is switched or fragment is change
        super.onPause();
        if(timeRunnable != null){
            timeHandler.removeCallbacks(timeRunnable);
            Log.i(TAG, "Pause of fragment, timer is removed");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (permission){
            timeHandler.postDelayed(timeRunnable, 300000);
            Log.i(TAG, "Resume of fragment, timer is added");
        } else {}
    }

    public void updateLocation(String email){
        Map<String, Object> data = new HashMap<>();

        data.put("latitude", this.latitude);
        data.put("longitude", this.longitude);

        db.collection("users").document(email)
                .set(data, SetOptions.merge());
        Log.i(TAG, "Location updated successfully: longitude:" + this.longitude.toString()
            + " latitude" + this.latitude);
    }

    public void timeUpdate(){
        Log.i(TAG, "Timer added");
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                updateLocation(userEmail);
                Log.i(TAG, "Times up, update location");
                timeHandler.postDelayed(timeRunnable, 300000);
            }
        };
    }

    public void downloadImages(){
        storageRef = FirebaseStorage.getInstance().getReference();
        for(User user: recomendUserList) {
            String path = user.getEmail() + "/Photo";
            StorageReference imageRef = storageRef.child(path);
            final Double longitude = user.getLongitude();
            final Double latitude = user.getLatitude();
            final float opacity = (float) 0.75;
            final String name = user.getNickName();
            final String introduction = user.getIntroduction();
            final LatLng userLocation = new LatLng(latitude, longitude);

            try{
                final File localFile = File.createTempFile("images", "jpg");
                imageRef.getFile(localFile).addOnSuccessListener(
                        new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Log.i(TAG, "User image Downloaded");
                                String path = localFile.getAbsolutePath();
                                fileLists.put(name, path);

                                Bitmap bitmap = BitmapFactory.decodeFile(path);
                                addMarker(bitmap, opacity, userLocation, name, introduction);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Bitmap bitmap = BitmapFactory.decodeResource(
                                getResources(), R.drawable.unknown);
                        addMarker(bitmap, opacity, userLocation, name, introduction);
                        Log.i(TAG, "Fail to download user image, using default");
                    }
                });
            } catch (Exception e){
                Log.i(TAG, "Fail to create temp file");
            }
        }
    }

    public void addMarker(Bitmap bitmap, float opacity, LatLng userLocation,
                          String name, String introduction){
        Bitmap smallBitMap = scaleBitmap(bitmap, 170, false);
        Bitmap handledBitmap = imageHandler.transform(smallBitMap);
        BitmapDescriptor bitmapDescriptor =
                BitmapDescriptorFactory.fromBitmap(handledBitmap);
        MarkerOptions marker = new MarkerOptions()
                .alpha(opacity)
                .position(userLocation)
                .title(name)
                .icon(bitmapDescriptor);
        if (introduction != null) {
            marker.snippet(introduction);
        }
        googleMap.addMarker(marker);
    }

    public void recommendUser(){
        storageRef = FirebaseStorage.getInstance().getReference();

        Log.i(TAG, "Check whether recommend users are loaded");
        recommendRunnable = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Recommend list size " + recomendUserList.size());
                if(recomendUserList.size() >= targeNumber){
                    downloadImages();
                    recommendHandler.removeCallbacks(recommendRunnable);
                } else {
                    // Wait for another 1 sec
                    randomSelect();
                    for (int index: selectedIndex){
                        getDocument(index);
                        Log.i(TAG, "User: " + index);
                    }
                    Log.i(TAG, "Still fetching data from server.");
                    recommendHandler.postDelayed(recommendRunnable, 1000);
                }
            }
        };
    }

    public void randomSelect(){
        int maxIndex = new Long(userNumber).intValue();
        ArrayList selectedList = new ArrayList();
        Random generater = new Random();
        int length = selectedList.size();
        for (int currentNum = 0; currentNum < targeNumber; currentNum ++){
            int nextInt = generater.nextInt(maxIndex);
            while (selectedList.contains(nextInt)) {
                nextInt = generater.nextInt(maxIndex);
            }
            selectedList.add(nextInt);
        }
        selectedIndex = selectedList;
    }

    public void getDocument(int index){
        Log.i(TAG, "fetch index: " + index);
        db.collection("users")
                .whereEqualTo("index", index)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String nickName = null;
                                String introduction = null;
                                Double latitude = null;
                                Double longitude = null;
                                String email = null;
                                if (document.get("nickName") != null){
                                    nickName = (String) document.get("nickName");
                                }
                                if (document.get("latitude") != null){
                                    latitude = (Double) document.get("latitude");
                                }
                                if (document.get("longitude") != null){
                                    longitude = (Double) document.get("longitude");
                                }
                                if (document.get("introduction") != null){
                                    introduction = (String) document.get("introduction");
                                }
                                email = document.getId();
                                User newRecommendUser =
                                        new User(email,
                                                nickName, introduction, latitude, longitude);
                                if (email.equals(userEmail)||
                                        latitude == null || longitude == null){
                                    targeNumber = targeNumber - 1;
                                }
                                else {
                                    Log.i(TAG, "add user: " + newRecommendUser.getNickName());
                                    if(!containUser(nickName)){
                                        recomendUserList.add(newRecommendUser);
                                        Log.i(TAG, "Current size: " + recomendUserList.size());
                                    }
                                    if(recomendUserList.size() == targeNumber){
                                        break;
                                    }
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private boolean containUser(String nickName){
        for(User user: recomendUserList){
            if (user.getNickName().equals((nickName))){
                return true;
            }
        }
        return false;
    }

    // Scale a bit map image
    private Bitmap scaleBitmap(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        realImage = Bitmap.createScaledBitmap(realImage, 300, 300, false);
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }
}

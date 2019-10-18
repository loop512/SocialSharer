package com.example.socialsharer.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.socialsharer.EditProfileActivity;
import com.example.socialsharer.EditSocialsActivity;
import com.example.socialsharer.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import io.grpc.Context;

import static android.app.Activity.RESULT_OK;



public class ProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ////////////////////////////////////////////////////////////////////////////////////////
    private static final String TAG = "ProfileFragment";
    private static final String HOME_ADDRESS = "Home Address";
    private static final String DOB = "Data Of Birth";
    private static final String CONTACT_NUMBER = "Contact Number";
    private static final String OCCUPATION = "Occupation";
    private static final String PHOTO = "Photo";
    private static final int PHOTO_SELECTION_REQUEST = 1;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String email = auth.getCurrentUser().getEmail().toString();
    private String fullName = auth.getCurrentUser().getDisplayName().toString();
    private DocumentReference profileRef = db.collection("users").document(email);
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference(email);
    private ListenerRegistration profileListener;

    private TextView profileName, profileJob, profileAddress, profileNumber,profileEmail;
    private ImageView editDetail;
    private ImageView editPhoto;
    private ImageView editSocials;
    private CircleImageView profileImage;
    private long userNumber;
    //////////////////////////////////////////////////////////////////////////////////////////

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        userNumber = bundle.getLong("userNumber");

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate (R.layout.fragment_profile, container, false);
//        ImageView profileImage = (ImageView)view.findViewById(R.id.profile_image);

        profileAddress = view.findViewById(R.id.profile_address);
        profileEmail = view.findViewById(R.id.profile_email);
        profileJob =  view.findViewById(R.id.profile_profession);
        profileName = view.findViewById(R.id.profile_name);
        profileNumber = view.findViewById(R.id.profile_number);
        editDetail = view.findViewById(R.id.edit_details);
        editPhoto = view.findViewById(R.id.edit_image);
        editSocials = view.findViewById(R.id.edit_socials);
        profileImage = view.findViewById(R.id.profile_image);
//        profileImage.setImageResource(R.drawable.unknown);

        editDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),EditProfileActivity.class);
                intent.putExtra("userNumber", userNumber);
                startActivity(intent);
            }
        });

        editSocials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditSocialsActivity.class);
                intent.putExtra("userNumber", userNumber);
                startActivity(intent);
            }
        });

        editPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.putExtra("userNumber", userNumber);
                startActivityForResult(intent, PHOTO_SELECTION_REQUEST);
            }
        });

        storageRef.child(PHOTO).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d("URI", uri.toString());
                Glide.with(ProfileFragment.this).load(uri).into(profileImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Load Image Exception", e.toString());
                profileImage.setImageResource(R.drawable.unknown);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PHOTO_SELECTION_REQUEST && resultCode == RESULT_OK){
            final Uri uri = data.getData();


            final StorageReference photoPath = storageRef.child(PHOTO);
            photoPath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getActivity(), "Successfully uploaded", Toast.LENGTH_SHORT).show();
                    Glide.with(ProfileFragment.this).load(uri).into(profileImage);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), "Upload Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        profileListener = profileRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Log.d(TAG, e.toString());
                    return;
                }

                if(documentSnapshot.exists()){
                    String address = documentSnapshot.getString(HOME_ADDRESS);
                    String job = documentSnapshot.getString(OCCUPATION);
                    String number = documentSnapshot.getString(CONTACT_NUMBER);

                    profileAddress.setText(address);
                    profileEmail.setText(email);
                    profileJob.setText(job);
                    profileName.setText(fullName);
                    profileNumber.setText(number);
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        profileListener.remove();
    }
}

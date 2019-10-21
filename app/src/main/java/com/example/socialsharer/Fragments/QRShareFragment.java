package com.example.socialsharer.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.socialsharer.QrScannerActivity;
import com.example.socialsharer.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.DexterBuilder;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class QRShareFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private ImageView qrImage;
    private Button saveQR, scanQR;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String email = auth.getCurrentUser().getEmail().toString();
    private StorageReference sRef = FirebaseStorage.getInstance().getReference(email);
    private static final String QR_CODE = "QRCODE";
    OutputStream  outputStream;
    private static final String TAG = "QRShareFragment";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public QRShareFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment QRShareFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static QRShareFragment newInstance(String param1, String param2) {
        QRShareFragment fragment = new QRShareFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate (R.layout.fragment_qrshare, container, false);

        qrImage = view.findViewById(R.id.qrCode);
        saveQR = view.findViewById(R.id.export_qr_button);
        scanQR = view.findViewById(R.id.scan_qr_button);
        try {
            if(email != null){
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                BitMatrix bitMatrix = multiFormatWriter.encode(email, BarcodeFormat.QR_CODE,500,500);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                final Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                    qrImage.setImageBitmap(bitmap);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                StorageReference qrStorage = sRef.child(QR_CODE);
                UploadTask uploadTask = qrStorage.putBytes(data);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "QR Code uploaded failed", Toast.LENGTH_SHORT).show();
                    }
                });

                saveQR.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        File path = Environment.getExternalStorageDirectory();
                        File dir = new File(path+"/QrCode/");
                        dir.mkdirs();
                        File file = new File(dir, "User_QR_Code.jpg");
                        try {
                            outputStream = new FileOutputStream(file);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                        Toast.makeText(getActivity(), "QR Code saved to your photos.", Toast.LENGTH_SHORT).show();
                        try {
                            outputStream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                });
            }
        } catch (WriterException e){
            e.printStackTrace();
        }

        scanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startNext = new Intent(getActivity(), QrScannerActivity.class);
                startActivity(startNext);
            }
        });

        return view;

    }

}

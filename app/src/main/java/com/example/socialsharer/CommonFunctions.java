package com.example.socialsharer;

import android.app.Activity;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;


public class CommonFunctions {

    public static void sendRequest(final Activity activity, final String current_email,
                            final String email, final String nickname,
                            final String TAG){
        // document reference for friend
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference documentRef = db.collection("request")
                .document(current_email);

        documentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                FieldPath field = FieldPath.of(email);
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        Log.i(TAG, "target user email: " + email);
                        if (document.get(field) == null){
                            // haven't send request to such user
                            documentRef.update(field, 1);
                            Toast.makeText(activity,
                                    "Successfully sent friend request to "
                                            + nickname + ".", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "document exist, never sent request to this user");
                        } else {
                            // Already sent request to this user, check request state
                            long requestState = (long) document.get(field );
                            Log.i(TAG, "document exist, request state: " + requestState);
                            if(requestState == 1){
                                Toast.makeText(activity, "Already to send request to "
                                                + nickname + ", still wait for confirmation."
                                        , Toast.LENGTH_SHORT).show();
                            } else if (requestState == 3){
                                // Already friend, usually requestState won't be 3 here, but in case
                                // of that happened we have following code to handle that situation.
                                Toast.makeText(activity, "User " + nickname
                                                + " is already your friend!"
                                        , Toast.LENGTH_SHORT).show();
                            } else if (requestState == 2){
                                // Request want rejected, resent the request.
                                documentRef.update(field, 1);
                                Toast.makeText(activity,
                                        "Successfully sent friend request to "
                                                + nickname + ".", Toast.LENGTH_SHORT).show();
                            }
                            // received request, don't do anything
                        }
                    } else {
                        // Haven't sent request to anyone, create document and send request
                        documentRef.update(field, 1);
                        Toast.makeText(activity, "Successfully sent friend request to "
                                + nickname + ".", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "document not exist, never sent request to any user");
                    }
                } else {
                    // Fail to download document from file base
                    Toast.makeText(activity, "Fail to send request to " + nickname
                            + ", check your internet connection.", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Get sent request task fail, check internet connection");
                }
            }
        });
    }
}

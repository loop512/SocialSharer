package com.example.socialsharer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.example.socialsharer.data.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides common functions that can be used on all the classed
 */
public class CommonFunctions {

    /**
     * This function allows sending request to a target user
     * @param activity The sending request's trigger activity
     * @param current_email Email of the user who send request
     * @param email Email of the user who receives request
     * @param nickname Target user's nickname
     * @param TAG Activity tag
     */
    public static void sendRequest(final Context activity, final String current_email,
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
                // Prepare the feedback
                String feedbackSuccess = "Successfully sent friend request to "
                        + nickname + ".";
                String feedbackInternet = "Fail to send request to " + nickname
                        + ", check your internet connection.";

                // Prepare update objects used for database updates
                Map<String, Object> request = new HashMap<>();
                request.put(email, 1);
                Map<String, Object> receive = new HashMap<>();
                receive.put(current_email, 4);

                if(task.isSuccessful()){
                    // Successfully connect to database
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        Log.i(TAG, "target user email: " + email);
                        if (document.get(field) == null){
                            // haven't send request to such user
                            documentRef.set(request, SetOptions.merge());
                            setRequestState(activity, email, receive,
                                    feedbackSuccess, feedbackInternet,
                                    true, TAG, null, null);
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
                            } else if (requestState == 2 || requestState == 5){
                                // Request was rejected, resent the request.
                                documentRef.set(request, SetOptions.merge());
                                setRequestState(activity, email, receive,
                                        feedbackSuccess, feedbackInternet, true, TAG,
                                        null, null);
                            } else if (requestState == 4){
                                // also receive request from that user, directly add friend
                                feedbackSuccess = "User " + nickname
                                        + " also sent a request to you,"
                                        + " successfully add as your contact.";
                                request.put(email, 3);
                                receive.put(current_email, 3);
                                documentRef.set(request, SetOptions.merge());
                                setRequestState(activity, email, receive,
                                        feedbackSuccess, feedbackInternet, true, TAG,
                                        null, null);
                            } else {
                                // Impossible to reach here, but leave for future development
                                Log.i(TAG, "Unknown state, resent request");
                                documentRef.set(request, SetOptions.merge());
                                setRequestState(activity, email, receive,
                                        feedbackSuccess, feedbackInternet, true, TAG,
                                        null, null);
                            }
                        }
                    } else {
                        // Haven't sent request to anyone, create document and send request
                        documentRef.set(request, SetOptions.merge());
                        setRequestState(activity, email, receive,
                                feedbackSuccess, feedbackInternet, true, TAG,
                                null, null);
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

    /**
     * This function set request states in user's database documents,
     * used for send/reject/delete requests/contacts
     * @param activity The trigger activity
     * @param email Target user's email
     * @param update Update object used for database update
     * @param feedbackSuccess Success feedback
     * @param feedbackInternet Failure feedback
     * @param toast Toast information
     * @param TAG Activity tag
     * @param btn Button trigger this event
     * @param btnChange Changed text on button if success
     */
    public static void setRequestState(final Context activity, final String email,
                                       final Map update, final String feedbackSuccess,
                                       final String feedbackInternet, final boolean toast,
                                       final String TAG, final Button btn, final String btnChange){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference documentRef = db.collection("request")
                .document(email);
        documentRef.set(update, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    // successful update, change button if exist
                    if (toast){
                        if(btn != null){
                            btn.setClickable(false);
                            btn.setText(btnChange);
                        }
                        Toast.makeText(activity, feedbackSuccess, Toast.LENGTH_SHORT).show();
                    }
                    Log.i(TAG, "Update request successful");
                } else {
                    // fail connect to fire base
                    if (toast){
                        Toast.makeText(activity, feedbackInternet, Toast.LENGTH_SHORT).show();
                    }
                    Log.i(TAG, "Fail to update request," +
                            " check permission or internet connection");
                }
            }
        });
    }

    /**
     * This function retrieve a user's all information with given document snapshot
     * @param document A documentSnapshot object of target user
     * @param path User's image store path
     * @return
     */
    public static User createUser(DocumentSnapshot document, String path){
        String nickName, introduction, email, occupation, imagePath = path,
                contactNumber, facebook, twitter,
                instagram = null, wechat = null, linkedin = null;

        Double latitude = null;
        Double longitude = null;

        // load last known location
        if (document.get("latitude") != null){
            latitude = (Double) document.get("latitude");
        }
        if (document.get("longitude") != null){
            longitude = (Double) document.get("longitude");
        }

        // Load user information
        nickName = convertString(document.get("nickName"));
        introduction = convertString(document.get("Introduction"));
        occupation = convertString(document.get("Occupation"));
        contactNumber = convertString(document.get("Contact Number"));
        email = document.getId();
        facebook = convertString(document.get("Facebook"));
        twitter = convertString(document.get("Twitter"));
        instagram = convertString(document.get("Instagram"));
        wechat = convertString(document.get("Wechat"));
        linkedin = convertString(document.get("Linkedin"));

        // Create User object to store all the information
        User user = new User(email, nickName, introduction, latitude, longitude, occupation,
                        imagePath, contactNumber, facebook, twitter, instagram, wechat, linkedin);
        return user;
    }

    private static String convertString(Object object){
        if (object == null){
            return null;
        } else {
            return (String) object;
        }
    }

    /**
     * Copy the clicked social media information to clipboard
     * @param context The trigger activity or fragment
     * @param text Text to copy
     */
    public static void setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard
                = (android.content.ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip =
                android.content.ClipData.newPlainText("Copied", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Information successfully " +
                "copied  to clipboard", Toast.LENGTH_SHORT).show();
    }

    /**
     * This function raise an alert before sending request
     * which ask for double confirm on sending a request to target user
     * @param context Trigger activity or fragment
     * @param currentUserEmail Email of user who send request
     * @param targetEmail Email of user who receive request
     * @param targetName Nickname of user who receive request
     * @param TAG Activity or fragment Tag
     */
    public static void sendRequestAlert(final Context context, final String currentUserEmail,
                                        final String targetEmail, final String targetName,
                                        final String TAG){
        // Show a dialog to double check user's intention
        new AlertDialog.Builder(context)
                .setTitle("Send friend request to " + targetName)
                .setMessage("Are you sure you want to sent a friend request to "
                        + targetName + "?")

                // User click on yes, check and send request
                .setPositiveButton("Send",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                                sendRequest(context, currentUserEmail,
                                                targetEmail, targetName, TAG);
                                //sendRequest(userEmail, email, title);
                            }
                        })
                // User click on no
                .setNegativeButton("Cancel", null)
                .show();
    }
}

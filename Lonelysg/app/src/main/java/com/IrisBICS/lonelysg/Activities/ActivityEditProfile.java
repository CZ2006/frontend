package com.IrisBICS.lonelysg.Activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.IrisBICS.lonelysg.AppController;

import com.IrisBICS.lonelysg.FirebaseAuthHelper;

import com.IrisBICS.lonelysg.Models.User;

import com.IrisBICS.lonelysg.R;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityEditProfile extends Activity {

    private CircleImageView editProfilePic;
    private Uri imageUri;
    private String downloadProfileUrlString;
    private Uri downloadProfileUri;
    private EditText editName;
    private EditText editAge;
    private EditText editOccupation;
    private EditText editInterest;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private static final int PICK_IMAGE = 1;
    private String userID = mAuth.getCurrentUser().getUid();
    private Task<Uri> downloadUrl;
    private String userProfilePicUri;
    private User user = new User();
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    String currentUserID = FirebaseAuthHelper.getCurrentUserID();

    // For dropdown box
    Spinner dropdownbox;
    String categories[] = {"Choose your Gender", "Male", "Female", "Non-binary", "Transgender", "Intersex", "Gender Non-Conforming", "Others"};
    ArrayAdapter<String> arrayAdapter;

    Button confirmButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editprofile);

        editProfilePic = findViewById(R.id.editProfilePic);
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
//        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Uploads");

        editName = findViewById(R.id.editProfileName);
        editAge = findViewById(R.id.editProfileAge);
        editOccupation = findViewById(R.id.editProfileOccupation);
        editInterest = findViewById(R.id.editProfileInterests);

        setProfileHint(userID);

        // For dropdown box (category selection)
        dropdownbox = (Spinner) findViewById(R.id.genderCategoryDropBox);
        arrayAdapter = new ArrayAdapter<String>(ActivityEditProfile.this, android.R.layout.simple_list_item_1, categories);
        dropdownbox.setAdapter(arrayAdapter);

        confirmButton = (Button)findViewById(R.id.editProfileConfirmButton);
        cancelButton = (Button)findViewById(R.id.editProfileCancelButton);

        editProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageChooser();
            }
        });

        // Pressing confirm button
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUri != null) {
                    updateProfileWithPic();
                }
                else {updateProfileWithoutPic();}
                Intent i = new Intent (ActivityEditProfile.this, ActivityNavigationBar.class);
                startActivity(i);
            }
        });

        // Pressing cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ActivityEditProfile.this, "Process Cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateProfileWithPic() {
        final StorageReference fileRef = mStorageRef.child(userID+ "." + getFileExtension(imageUri));

        UploadTask uploadTask = fileRef.putFile(imageUri);
        downloadUrl = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return fileRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    downloadProfileUri = task.getResult();
                    try {
                        JSONObject jsonBody = new JSONObject();

                        if (editName.getText().toString().matches("")) {
                            jsonBody.put("username", editName.getHint());
                        }
                        else{jsonBody.put("username", editName.getText());}

                        if (!dropdownbox.getSelectedItem().toString().matches("Choose your Gender")) {
                            jsonBody.put("gender", dropdownbox.getSelectedItem().toString());
                        }
                        if (editAge.getText().toString().matches("")) {
                            jsonBody.put("age", editAge.getHint());
                        }
                        else{jsonBody.put("age", editAge.getText());}
                        if (editOccupation.getText().toString().matches("")) {
                            jsonBody.put("occupation", editOccupation.getHint());
                        }
                        else{jsonBody.put("occupation", editOccupation.getText());}
                        if (editInterest.getText().toString().matches("")) {
                            jsonBody.put("interests", editInterest.getHint());
                        }
                        else{jsonBody.put("interests", editInterest.getText());}
                        jsonBody.put("image",downloadProfileUri.toString());
                        String URL = "https://us-central1-lonely-4a186.cloudfunctions.net/app/XQ/updateUser/"+userID;

                        JsonObjectRequest updateUserRequest = new JsonObjectRequest(Request.Method.PUT, URL, jsonBody,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("Volley", error.toString());
                            }
                        });
                        AppController.getInstance(ActivityEditProfile.this).addToRequestQueue(updateUserRequest);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void updateProfileWithoutPic(){
        try {
            JSONObject jsonBody = new JSONObject();

            if (editName.getText().toString().matches("")) {
                jsonBody.put("username", editName.getHint());
            }
            else{jsonBody.put("username", editName.getText());}

            if (!dropdownbox.getSelectedItem().toString().matches("Choose your Gender")) {
                jsonBody.put("gender", dropdownbox.getSelectedItem().toString());
            }
            if (editAge.getText().toString().matches("")) {
                jsonBody.put("age", editAge.getHint());
            }
            else{jsonBody.put("age", editAge.getText());}
            if (editOccupation.getText().toString().matches("")) {
                jsonBody.put("occupation", editOccupation.getHint());
            }
            else{jsonBody.put("occupation", editOccupation.getText());}
            if (editInterest.getText().toString().matches("")) {
                jsonBody.put("interests", editInterest.getHint());
            }
            else{jsonBody.put("interests", editInterest.getText());}
            String URL = "https://us-central1-lonely-4a186.cloudfunctions.net/app/XQ/updateUser/"+userID;

            JsonObjectRequest updateUserRequest = new JsonObjectRequest(Request.Method.PUT, URL, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Volley", error.toString());
                }
            });
            AppController.getInstance(ActivityEditProfile.this).addToRequestQueue(updateUserRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setProfileHint(String userID) {
        String URL = "https://us-central1-lonely-4a186.cloudfunctions.net/app/XQ/getUser/"+userID;
        JsonObjectRequest setProfileHintRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            user.setUsername(response.getString("username"));
                            user.setGender(response.getString("gender"));
                            user.setAge(response.getString("age"));
                            user.setOccupation(response.getString("occupation"));
                            user.setInterests(response.getString("interests"));
                            String imageUrl = response.getString("image");
                            user.setProfilePic(Uri.parse(imageUrl));
                            editName.setHint(user.getUsername());
                            editAge.setHint(user.getAge());
                            editOccupation.setHint(user.getOccupation());
                            editInterest.setHint(user.getInterests());
                            if (user.getProfilePic()!=null) {
                                Picasso.get().load(user.getProfilePic()).into(editProfilePic);
                            }
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley", error.toString());
                    }
                });
        AppController.getInstance(this).addToRequestQueue(setProfileHintRequest);
    };

    private void openImageChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(editProfilePic);
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}
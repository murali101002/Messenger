package android.com.messenger;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.messages.internal.Update;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class Update_View_Profile extends AppCompatActivity {
    EditText fname, lname;
    Button update, cancel, back;
    ImageView dp, viewDp;
    LinearLayout updateProfile, viewProfile;
    TextView fnameView, lnameView;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = null;
    private FirebaseAuth.AuthStateListener mAuthListener;
    User userProfile = null;
    String receivedKey = null;
    Uri imageUri = null;
    DatabaseReference fbDatabase = FirebaseDatabase.getInstance().getReference();
    public static final int resultCode = 9000;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference imgRef = null;
    StorageReference storageRef = storage.getReferenceFromUrl("gs://messenger-c5fb3.appspot.com");
    UploadTask uploadTask = null;
    UploadTask.TaskSnapshot snapshot = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        fname = (EditText) findViewById(R.id.fname_update);
        fnameView = (TextView) findViewById(R.id.fname_view);
        lnameView = (TextView) findViewById(R.id.lname_view);
        back = (Button) findViewById(R.id.btn_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMessengerScreen();
            }
        });
        updateProfile = (LinearLayout) findViewById(R.id.updateProfile);
        viewProfile = (LinearLayout) findViewById(R.id.viewProfile);
        viewDp = (ImageView) findViewById(R.id.dp_view);
        lname = (EditText) findViewById(R.id.lname_update);
        update = (Button) findViewById(R.id.btn_update);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fname.getText().toString() == null || lname.getText().toString() == null) {
                    Toast.makeText(getApplicationContext(), "Enter valid values", Toast.LENGTH_SHORT).show();
                } else {
                    User updateUser = new User();
                    updateUser.setfName(fname.getText().toString());
                    updateUser.setlName(lname.getText().toString());
                    if(snapshot != null){
                        updateUser.setImageUrl(snapshot.getDownloadUrl().toString());
                        fbDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("imageUrl").setValue(updateUser.getImageUrl());
                    }
                    fbDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("fName").setValue(updateUser.getfName());
                    fbDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("lName").setValue(updateUser.getlName());
                    Toast.makeText(getApplicationContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    backToMessengerScreen();
                }
            }
        });
        cancel = (Button) findViewById(R.id.cancel_update);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMessengerScreen();
            }
        });
        dp = (ImageView) findViewById(R.id.dp_update);
        dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select picture"), resultCode);
            }
        });
        user = mAuth.getCurrentUser();
//        receivedKey = getIntent().getExtras().getString(ProfilesArrayAdapter.USERKEY);
        receivedKey = null;
        if (receivedKey != null) {
            viewProfile.setVisibility(View.VISIBLE);
            updateProfile.setVisibility(View.GONE);
            fbDatabase.child("Users").child(receivedKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    fnameView.setText(dataSnapshot.child("fName").getValue().toString());
                    lnameView.setText(dataSnapshot.child("lName").getValue().toString());
                    Picasso.with(getApplicationContext()).load(Uri.parse(dataSnapshot.child("imageUrl").getValue().toString())).into(viewDp);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            viewProfile.setVisibility(View.GONE);
            updateProfile.setVisibility(View.VISIBLE);
            fbDatabase.child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    fname.setText(dataSnapshot.child("fName").getValue().toString());
                    lname.setText(dataSnapshot.child("lName").getValue().toString());
                    Picasso.with(getApplicationContext()).load(Uri.parse(dataSnapshot.child("imageUrl").getValue().toString())).into(dp);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        userProfile = new User();

    }

    private void backToMessengerScreen() {
        Intent backIntent = new Intent(Update_View_Profile.this, Messenger.class);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(backIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (this.resultCode == requestCode && resultCode == RESULT_OK) {
            imageUri = data.getData();
            imgRef = storageRef.child("images/"+imageUri.getLastPathSegment());
            uploadTask = imgRef.putFile(imageUri);
            uploadTask.addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Update_View_Profile.this, "Error uploading image", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    snapshot = taskSnapshot;
                }
            });
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            } catch (IOException e) {
                e.printStackTrace();
            }
            dp.setImageBitmap(bitmap);
        }
    }

    public void CancelUpdate(View view) {
        Intent backToProfiles = new Intent(this, Messenger.class);
        backToProfiles.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        backToProfiles.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(backToProfiles);
    }
}

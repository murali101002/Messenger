package android.com.messenger;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.app.ProgressDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class SignUp extends AppCompatActivity {
    EditText email, pwd, fname, lname, repPwd;
    FirebaseAuth mAuth;
    Uri imageUri = null;
    ImageView displayPic;
    RadioGroup radioGroup;
    RadioButton rbMale, rbFemale;
    DatabaseReference fbDatabase = null;
    Button signUp;
    UploadTask uploadTask = null;
    public static final int resultCode = 1000;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference imgRef = null;
    StorageReference storageRef = storage.getReferenceFromUrl("gs://messenger-c5fb3.appspot.com");
    UploadTask.TaskSnapshot snapshot = null;
    String gender = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (this.resultCode == requestCode && resultCode == RESULT_OK) {
            imageUri = data.getData();
            imgRef = storageRef.child("images/" + imageUri.getLastPathSegment());
            uploadTask = imgRef.putFile(imageUri);
            uploadTask.addOnCompleteListener(this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(SignUp.this, "Error uploading image", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
            displayPic.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        email = (EditText) findViewById(R.id.emailSIgnUP);
        pwd = (EditText) findViewById(R.id.pwd_signup);
        //repPwd = (EditText) findViewById(R.id.repPwd_signup);
        fname = (EditText) findViewById(R.id.fname);
        lname = (EditText) findViewById(R.id.lname);
        displayPic = (ImageView) findViewById(R.id.dp);
        rbMale = (RadioButton) findViewById(R.id.male);
        rbFemale = (RadioButton) findViewById(R.id.female);
        radioGroup = (RadioGroup) findViewById(R.id.rGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.male) {
                    gender = "male";
                } else if (checkedId == R.id.female) {
                    gender = "female";
                }
            }
        });
        displayPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();startActivityForResult(Intent.createChooser(intent, "Select picture"), resultCode);
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

            }
        });
        //signUp = (Button) findViewById(R.id.cancel);
        mAuth = FirebaseAuth.getInstance();
        fbDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void CancelSignUp(View view) {
        Intent login = new Intent(this, MainActivity.class);
        login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(login);
    }

    public void SignUp(View view) {
        if (email.getText().toString().length() < 1 || pwd.getText().toString().length() < 6 || fname.getText().toString().length() < 1) {
            Toast.makeText(getApplicationContext(), "Enter valid values", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.createUserWithEmailAndPassword(email.getText().toString(), pwd.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
                                builder.setMessage(task.getException().getMessage())
                                        .setTitle("Email exists")
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                                Log.d("TAG", "Login biscuit");
                            } else {
                                Log.d("Task", String.valueOf(task.getResult().getUser().getUid()));
                                Toast.makeText(SignUp.this, "Succesfully Signed In", Toast.LENGTH_SHORT).show();
                                Intent signInActivity = new Intent(SignUp.this, MainActivity.class);
                                User user = new User();
                                user.setfName(fname.getText().toString());
                                user.setlName(lname.getText().toString());
                                user.setGender(gender);
                                if (snapshot != null) {
                                    user.setImageUrl(snapshot.getDownloadUrl().toString());
                                } else {
                                    user.setImageUrl("");
                                }
                                user.setEmail(email.getText().toString());
                                fbDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).setValue(user);
                                signInActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                signInActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(signInActivity);
                            }
                        }
                    });
        }
    }
}

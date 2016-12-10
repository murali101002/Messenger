package android.com.messenger;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //    ImageView signInButton2, fbSignUp;
    ProgressDialog progressDialog = null;
    GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 900;
    SignInButton signInButton = null;
    EditText email, pwd;
    Button login, signUp;
    LoginButton loginButton;
    private static final String TAG = "FacebookLogin";
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = null;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private CallbackManager mCallbackManager;
    DatabaseReference fdDatabase = FirebaseDatabase.getInstance().getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(getResources().getDrawable(R.mipmap.ic_launcher));
        AppEventsLogger.activateApp(this);
        mCallbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.loginButton);
        loginButton.setReadPermissions(Arrays.asList("email"));
        getSessionUser();
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("FB", "Success");
                handleFacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.d("FB", "Fail");
            }
        });



        //mAuth.signOut();
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Authenticating...");
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        email = (EditText) findViewById(R.id.emailLogin);
        pwd = (EditText) findViewById(R.id.pwdLogin);
        login = (Button) findViewById(R.id.login);
        signUp = (Button) findViewById(R.id.signUp);
        login.setOnClickListener(this);
        signUp.setOnClickListener(this);
        FirebaseApp.initializeApp(this);
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        //fbSignUp = (ImageView) findViewById(R.id.fb);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseUser googleUser = mAuth.getCurrentUser();
                if (googleUser == null) {
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    startActivityForResult(signInIntent, RC_SIGN_IN);

                }

            }
        });


    }

    private void getSessionUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(MainActivity.this, Messenger.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        mAuth.removeAuthStateListener(mAuthListener);
    }

    private void handleFacebookAccessToken(final AccessToken accessToken) {
        progressDialog.show();
        AuthCredential fbCredentials = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(fbCredentials).addOnCompleteListener(this,new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Facebook Authentication Failed", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Facebook Authentication Success", Toast.LENGTH_LONG).show();
                    final User fbUser = new User();
                    Profile fbProfile = Profile.getCurrentProfile();
                    fbUser.setImageUrl(fbProfile.getProfilePictureUri(20, 20).toString());
                    fbUser.setfName(fbProfile.getFirstName());
                    fbUser.setlName(fbProfile.getLastName());
                    fdDatabase.child("Users").child(task.getResult().getUser().getUid()).setValue(fbUser);
                    progressDialog.hide();
                    getSessionUser();
                    GraphRequest graphRequest = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            Log.v("LoginActivity Response ", response.toString());
                            try {
                                //fbUser.setfName(object.getString("name"));
                                fbUser.setEmail(object.getString("email"));
                                Profile fbProfile = Profile.getCurrentProfile();
                                fbUser.setImageUrl(fbProfile.getProfilePictureUri(20, 20).toString());
                                fbUser.setfName(fbProfile.getFirstName());
                                fbUser.setlName(fbProfile.getLastName());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        progressDialog.show();
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount acct = result.getSignInAccount();

                postGoogleAccntDataToFirebase(acct);
                progressDialog.hide();
                getSessionUser();

            }
        }
    }

    private void postGoogleAccntDataToFirebase(final GoogleSignInAccount acct) {
        AuthCredential credentials = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        FirebaseAuth.getInstance().signInWithCredential(credentials).addOnCompleteListener(this,new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    User user = new User();
                    user.setEmail(acct.getEmail());
                    user.setfName(acct.getGivenName());
                    user.setlName(acct.getFamilyName());
                    user.setImageUrl(String.valueOf(acct.getPhotoUrl()));
                    Date date = new Date();
                    SimpleDateFormat sd = new SimpleDateFormat("MM/dd/yy");
                    String newDate = sd.format(date);
                    user.setTime(newDate);
                    fdDatabase.child("Users").child(task.getResult().getUser().getUid()).setValue(user);
                    Toast.makeText(MainActivity.this, "SignIn Successful", Toast.LENGTH_SHORT).show();
                    getSessionUser();
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                //progressDialog.show();
                mAuth.signInWithEmailAndPassword(email.getText().toString(), pwd.getText().toString())
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(MainActivity.this, Messenger.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setMessage(task.getException().getMessage())
                                            .setTitle("No data is find with this user")
                                            .setPositiveButton(android.R.string.ok, null);
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                    Log.d("TAG", "Login biscuit");
                                }
                            }
                        });
                //progressDialog.hide();
                break;
            case R.id.signUp:
                Intent createIntent = new Intent(this, SignUp.class);
                startActivity(createIntent);
                break;
        }
    }


}

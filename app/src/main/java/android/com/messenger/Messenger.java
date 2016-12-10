package android.com.messenger;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

public class Messenger extends AppCompatActivity {
    TextView name, unreadTxts;
    ImageView editProfile, logout;
    ListView allProfiles;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = null;
    private FirebaseAuth.AuthStateListener mAuthListener;
    String fname, lname, imageUri;
    DataSnapshot userProfile = null;
    DatabaseReference fbDatabase = FirebaseDatabase.getInstance().getReference();
    public static ArrayList<User> profilesList = null;
    public static ArrayList<String> userKeys = null;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference imgRef = null;
    public static int unreadCount = 0;
    Texts chatMsg = null;
    User curUser = null;
    String key = null;
    StorageReference storageRef = storage.getReferenceFromUrl("gs://messenger-c5fb3.appspot.com");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        editProfile = (ImageView) findViewById(R.id.profile);
        logout = (ImageView) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(Messenger.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                Toast.makeText(Messenger.this, "You are successfully Logged out", Toast.LENGTH_SHORT).show();
                startActivity(intent);

            }
        });
        allProfiles = (ListView) findViewById(R.id.profilesList);
        allProfiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                profilesList.get(position).setUnreadCnt(0);
                CallCustomAdater(profilesList, userKeys, 0);
                //Set read status to yes
                fbDatabase.child("Texts").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot chat:dataSnapshot.getChildren()){
                            Texts texts = chat.getValue(Texts.class);
                            if(texts.getSenderKey().equals(userKeys.get(position)) &&
                                    texts.getReceiverKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                //texts.setRead("yes");
                                fbDatabase.child("Texts").child(chat.getKey()).child("read").setValue("yes");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                Intent chat = new Intent(Messenger.this, Chats.class);
                chat.putExtra("KEY", userKeys.get(position));
                startActivity(chat);
            }
        });
        name = (TextView) findViewById(R.id.label);
        user = mAuth.getCurrentUser();
        //Get All Profiles from FireBase
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewProfile = new Intent(getApplicationContext(), Update_View_Profile.class);
                startActivity(viewProfile);
            }
        });
        fbDatabase.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                profilesList = new ArrayList<User>();
                userKeys = new ArrayList<String>();
                for (final DataSnapshot profile : dataSnapshot.getChildren()) {
                    curUser = profile.getValue(User.class);
                    if (curUser != null && (!profile.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))) {
                        key = profile.getKey();
                        userKeys.add(key);
//                        curUser.setUnreadCnt(unreadCount);
                        profilesList.add(curUser);
                        //===========================================

                        fbDatabase.child("Texts").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                User tempUser = profile.getValue(User.class);
                                String combinedKey = profile.getKey()+" "+FirebaseAuth.getInstance().getCurrentUser().getUid();
                                unreadCount = 0;
                                for(DataSnapshot text:dataSnapshot.getChildren()){
                                    chatMsg = text.getValue(Texts.class);
                                    if((combinedKey.contains(chatMsg.getReceiverKey()) && combinedKey.contains(chatMsg.getSenderKey()))
                                            && (chatMsg.getReceiverKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))){
                                        if(chatMsg.getRead().equalsIgnoreCase("no")){
                                            unreadCount++;
//                                            tempUser = profile.getValue(User.class);
                                        }
                                    }
                                }
                                User tempUser = profile.getValue(User.class);
                                for(User userProf:profilesList){
                                    if(userProf.getImageUrl().equals(tempUser.getImageUrl()) &&
                                    userProf.getfName().equals(tempUser.getfName()) &&
                                    userProf.getlName().equals(tempUser.getlName())){
                                        profilesList.get(profilesList.indexOf(userProf)).setUnreadCnt(unreadCount);
                                        break;
                                    }
                                }
//                                profilesList.get(profilesList.indexOf(tempUser)).setUnreadCnt(unreadCount);
                                CallCustomAdater(profilesList, userKeys, unreadCount);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        //===========================================
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Get Current Profile Info
        fbDatabase.child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                fname = dataSnapshot.child("fName").getValue().toString();
                lname = dataSnapshot.child("lName").getValue().toString();
                imageUri = dataSnapshot.child("imageUrl").getValue().toString();
                name.setText(fname + " " + lname);
                Picasso.with(getApplicationContext()).load(imageUri).into(editProfile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void CallCustomAdater(ArrayList<User> profilesList, ArrayList<String> userKeys, int unreadCount) {
        if (profilesList.size() > 0 && (profilesList != null)) {
            ProfilesArrayAdapter adapter = new ProfilesArrayAdapter(getApplicationContext(),
                    R.layout.profiles_list_layout, profilesList, userKeys, unreadCount);
            allProfiles.setAdapter(adapter);
        }
    }

}

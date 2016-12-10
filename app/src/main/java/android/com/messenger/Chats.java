package android.com.messenger;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Chats extends AppCompatActivity implements View.OnClickListener {
    TextView label, nothingToShow;
    EditText typeText;
    ImageView profileDp, sendText, addPic;
    ListView chatsListView;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference fbDatabase = FirebaseDatabase.getInstance().getReference();
    FirebaseUser fbUser = mAuth.getCurrentUser();
    String receivedKey = null;
    Texts chat = null;
    public static ArrayList<Texts> chats = null;
    Uri imageUri = null;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference imgRef = null;
    StorageReference storageRef = storage.getReferenceFromUrl("gs://messenger-c5fb3.appspot.com");
    UploadTask uploadTask = null;
    UploadTask.TaskSnapshot snapshot = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        typeText = (EditText) findViewById(R.id.typeMsg);
        label = (TextView) findViewById(R.id.label_chats);
        nothingToShow = (TextView) findViewById(R.id.nothingToShow);
        profileDp = (ImageView) findViewById(R.id.img_chats);
        sendText = (ImageView) findViewById(R.id.send);
        addPic = (ImageView) findViewById(R.id.addPic);
        sendText.setOnClickListener(this);
        addPic.setOnClickListener(this);
        chatsListView = (ListView) findViewById(R.id.chatsList);
        chatsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Texts currentUserText = chats.get(position);
//                if(currentUserText.getTextObjKey().equals(FirebaseAuth.getInstance().getCurrentUser())){
                    fbDatabase.child("Texts").child(chats.get(position).getTextObjKey()).removeValue()
                            .addOnCompleteListener(Chats.this,new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Error while deleting the text!!!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Text deleted", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
//                }else{
//                    Toast.makeText(getApplicationContext(), "You can delete only your texts", Toast.LENGTH_SHORT).show();
//                }
                return false;
            }
        });
        receivedKey = getIntent().getExtras().getString("KEY");
        fbDatabase.child("Users").child(receivedKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                label.setText(dataSnapshot.child("fName").getValue().toString() + " " + dataSnapshot.child("lName").getValue().toString());
                Picasso.with(getApplicationContext()).load(dataSnapshot.child("imageUrl").getValue().toString()).into(profileDp);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        fbDatabase.child("Texts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chats = new ArrayList<>();
                for (DataSnapshot textObj : dataSnapshot.getChildren()) {
                    Texts chat = textObj.getValue(Texts.class);
                    if (chat != null) {
                        String combinedKey = receivedKey+" "+fbUser.getUid();
                        if (combinedKey.contains(chat.getReceiverKey()) && combinedKey.contains(chat.getSenderKey())) {
                            chat.setTextObjKey(textObj.getKey());
                            chats.add(chat);
                        }
                    }
                }
                displayConversations(chats);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void displayConversations(ArrayList<Texts> chatList) {
        if (chatList.size() > 0 && chatList != null) {
            nothingToShow.setVisibility(View.GONE);
            chatsListView.setVisibility(View.VISIBLE);
            ChatArrayAdapter adapter = new ChatArrayAdapter(getApplicationContext(), R.layout.texts_layout, chatList);
            chatsListView.setAdapter(adapter);
        } else {
            chatsListView.setVisibility(View.GONE);
            nothingToShow.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:

                if (typeText.getText().toString().trim().length() < 1 && (imageUri == null)) {
                    Toast.makeText(getApplicationContext(), "Enter Message", Toast.LENGTH_SHORT).show();
                } else {
                    chat = new Texts();
                    chat.setRead("no");
                    if (typeText.getText().toString() == null) {
                        chat.setText("");
                    } else {
                        chat.setText(typeText.getText().toString());
                    }
                    typeText.setText("");
                    chat.setReceiverKey(receivedKey);
                    chat.setSenderKey(fbUser.getUid());
                    if (snapshot!=null) {
                        chat.setImageUrl(snapshot.getDownloadUrl().toString());
                    } else {
                        chat.setImageUrl("");
                    }

                    Date sentTime = new Date();
                    DateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a");
                    chat.setTime(dateFormatter.format(sentTime));
                    fbDatabase.child("Texts").push().setValue(chat);
                    addPic.setImageResource(R.drawable.gallery);
                }

                break;
            case R.id.addPic:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select picture"), SignUp.resultCode);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (SignUp.resultCode == requestCode && resultCode == RESULT_OK) {
            imageUri = data.getData();
            imgRef = storageRef.child("images/"+imageUri.getLastPathSegment());
            uploadTask = imgRef.putFile(imageUri);
            uploadTask.addOnCompleteListener(this,new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(!task.isSuccessful()){
                        Log.d("Task",task.toString());
                    }
                }
            }).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    snapshot = taskSnapshot;
                    //chat.setImageUrl(taskSnapshot.getDownloadUrl().toString());
                }
            });
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            } catch (IOException e) {
                e.printStackTrace();
            }
            addPic.setImageBitmap(bitmap);

        }
    }
}

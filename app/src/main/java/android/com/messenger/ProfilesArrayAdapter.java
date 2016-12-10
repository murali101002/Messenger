package android.com.messenger;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by murali101002 on 11/17/2016.
 */
public class ProfilesArrayAdapter extends ArrayAdapter<User> {
    int resourceFile;
    Context profileContext;
    List<User> profiles;
    ArrayList<String> keysList = null;
    int unreadCnt = 0;
    User profile = null;
    View currentView = null;
    DatabaseReference fbDatabase = FirebaseDatabase.getInstance().getReference();
    public static final String USERKEY = "userKey";

    public ProfilesArrayAdapter(Context context, int resource, List<User> objects, ArrayList<String> keysList, int unreadCount) {
        super(context, resource, objects);
        this.resourceFile = resource;
        this.profileContext = context;
        this.profiles = objects;
        this.keysList = keysList;
        this.unreadCnt = unreadCount;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) profileContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(resourceFile, parent, false);
        }
        currentView = convertView;
        convertView.setId(position);
        profile = profiles.get(position);
        TextView count = (TextView) convertView.findViewById(R.id.unread);
//        count.setVisibility(View.INVISIBLE);
        ImageView dp = (ImageView) convertView.findViewById(R.id.profImage);
        TextView displayName = (TextView) convertView.findViewById(R.id.profileName);

        displayName.setText(profile.getfName() + " " + profile.getlName());
        Uri imageUri = Uri.parse(profile.getImageUrl());
        Picasso.with(convertView.getContext()).load(imageUri).into(dp);
        if(profile.getUnreadCnt()>0){
            count.setVisibility(View.VISIBLE);
            count.setText(String.valueOf(profile.getUnreadCnt()));
        }

        return convertView;
    }
}

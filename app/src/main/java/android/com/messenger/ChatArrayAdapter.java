package android.com.messenger;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

/**
 * Created by murali101002 on 11/18/2016.
 */
public class ChatArrayAdapter extends ArrayAdapter<Texts> {
    int mRes;
    List<Texts> chatList;
    Context mContext;
    Texts chat = null;

    public ChatArrayAdapter(Context context, int resource, List<Texts> objects) {
        super(context, resource, objects);
        this.mRes = resource;
        this.chatList = objects;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(mRes, parent, false);
        }
        chat = chatList.get(position);

        TextView text = (TextView) convertView.findViewById(R.id.reply);
        RelativeLayout relativeLayout = (RelativeLayout) convertView.findViewById(R.id.textRelative);
        TextView time = (TextView) convertView.findViewById(R.id.lbl_time);
        ImageView galleryPic = (ImageView) convertView.findViewById(R.id.imageView);
        galleryPic.setVisibility(View.GONE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 1.0f;

        RelativeLayout.LayoutParams relParams = new RelativeLayout.LayoutParams(200,200);


        text.setText(chat.getText());
        time.setText(chat.getTime());

        //pretty time
        PrettyTime prettyTime = new PrettyTime();
        String datetime = prettyTime.format(new Date(chat.getTime()));
        time.setText(datetime);

        if (chat.getSenderKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            time.setGravity(Gravity.RIGHT);
            text.setGravity(Gravity.RIGHT);
            params.gravity = Gravity.RIGHT;
            relParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//            convertView.setBackgroundColor(Color.rgb(255, 255, 153));
            convertView.setBackground(convertView.getContext().getResources().getDrawable(R.drawable.seperator_line));
            relativeLayout.setBackgroundColor(Color.rgb(255, 255, 153));
        } else {
            text.setGravity(Gravity.LEFT);
            time.setGravity(Gravity.LEFT);
            params.gravity = Gravity.LEFT;
            relParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//            convertView.setBackgroundColor(Color.rgb(153, 204, 255));
            convertView.setBackground(convertView.getContext().getResources().getDrawable(R.drawable.seperator_line));
            relativeLayout.setBackgroundColor(Color.rgb(153, 204, 255));
        }

        if (chat.getImageUrl() != null && chat.getImageUrl().length() > 1) {
            galleryPic.setVisibility(View.VISIBLE);
            Picasso.with(convertView.getContext()).load(chat.getImageUrl()).into(galleryPic);
        }
        galleryPic.setLayoutParams(relParams);
        return convertView;
    }
}

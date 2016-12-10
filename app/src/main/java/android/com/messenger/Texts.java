package android.com.messenger;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by murali101002 on 11/18/2016.
 */
public class Texts implements Comparable<Texts> {
    String text;
    String time;
    String senderKey;
    String receiverKey;
    String textObjKey;

    public String getTextObjKey() {
        return textObjKey;
    }

    public void setTextObjKey(String textObjKey) {
        this.textObjKey = textObjKey;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    String imageUrl;

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }

    public String getReceiverKey() {
        return receiverKey;
    }

    public void setReceiverKey(String receiverKey) {
        this.receiverKey = receiverKey;
    }

    public String getSenderKey() {
        return senderKey;
    }

    public void setSenderKey(String senderKey) {
        this.senderKey = senderKey;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    String read;

    @Override
    public int compareTo(Texts another) {
        Date d1 = new Date(this.getTime());
        Date d2 = new Date(another.getTime());
        if(d1.compareTo(d2)>0){
            return 1;
        }else if(d1.compareTo(d2)<0){
            return -1;
        }
        return 0;
    }
}

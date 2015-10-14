package com.example.denis.firebasechat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by denis on 09.10.15.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    public Context context;

    private Firebase mFirebaseUsersRef;

    private String currentUserId = "";

    private List<Message> messages = new ArrayList<>();
    private Map<String, User> users = new HashMap<>();
    private MessageAddedCallback messageAddedCallback;

    public ChatAdapter(Context context) {
        this.context = context;
        mFirebaseUsersRef = new Firebase(FBConstants.FIREBASE_URL).child(FBConstants.FIREBASE_USERS);
    }

    public void setCurrentUserId(String uid) {
        currentUserId = uid;
    }

    public void addMessage(final Message msg) {
        String uid = msg.uid;
        if (users.get(uid) == null) {
            mFirebaseUsersRef.child(msg.uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    users.put(msg.uid, dataSnapshot.getValue(User.class));
                    messages.add(msg);
                    notifyItemInserted(messages.size());
                    notifyMessageAdded();
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
        } else {
            messages.add(msg);
            notifyItemInserted(messages.size());
            notifyMessageAdded();
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvAuthor, tvTime;
        ImageView ivAvatar;

        public MessageViewHolder(View itemView) {
            super(itemView);
            tvAuthor = (TextView) itemView.findViewById(R.id.tvUserName);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
            tvMessage = (TextView) itemView.findViewById(R.id.tvMessageText);
            ivAvatar = (ImageView) itemView.findViewById(R.id.ivAvatar);
        }
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message msg = messages.get(position);
        holder.tvMessage.setText(msg.message);
        User user = getUserById(msg.uid);
        if (user.name == null || user.name.isEmpty())
            holder.tvAuthor.setText(user.email);
        else
            holder.tvAuthor.setText(user.name);
        if (msg.uid.equals(currentUserId))
            holder.tvAuthor.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        else
            holder.tvAuthor.setTextColor(context.getResources().getColor(R.color.colorAccent));

        Picasso.with(context).load(user.avatarPath).into(holder.ivAvatar);
        Date date = new Date(msg.timeStamp);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE dd/MM/yy HH:mm:ss");
        holder.tvTime.setText(sdf.format(date));
    }

    private User getUserById(String uid) {
        return users.get(uid);
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessageAddedCallback(MessageAddedCallback messageAddedCallback) {
        this.messageAddedCallback = messageAddedCallback;
    }

    private void notifyMessageAdded() {
        if (messageAddedCallback != null)
            messageAddedCallback.messageAdded();
    }

    public interface MessageAddedCallback {
        void messageAdded();
    }
}

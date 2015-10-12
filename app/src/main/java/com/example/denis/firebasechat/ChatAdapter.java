package com.example.denis.firebasechat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by denis on 09.10.15.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    public Context context;

    private List<Message> messages = new ArrayList<>();
    private Map<String, User> users = new HashMap<>();

    public ChatAdapter(Context context) {
        this.context = context;
    }

    public void addMessage(Message msg) {
        messages.add(msg);
        notifyItemInserted(messages.size());
    }

    public void setUsers(Map<String, User> users) {
        this.users = users;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvAuthor;
        ImageView ivAvatar;

        public MessageViewHolder(View itemView) {
            super(itemView);
            tvAuthor = (TextView) itemView.findViewById(R.id.tvUserName);
            tvMessage = (TextView) itemView.findViewById(R.id.tvMessageText);
            ivAvatar = (ImageView) itemView.findViewById(R.id.ivAvatar);
        }
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message msg = messages.get(position);
        holder.tvMessage.setText(msg.message);
        User user = getUserById(msg.uid);
        holder.tvAuthor.setText(user.name);
        Picasso.with(context).load(user.avatarPath).into(holder.ivAvatar);
    }

    private User getUserById(String uid) {
        return users.get(uid);
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, null, false));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}

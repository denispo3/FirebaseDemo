package com.example.denis.firebasechat;


import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

public class ChatFragment extends Fragment {

    private static final String UID_KEY = "user_key";

    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnSend;

    public static ChatFragment newInstance(String uid) {
        Bundle args = new Bundle();
        args.putString(UID_KEY, uid);
        ChatFragment chatFragment = new ChatFragment();
        chatFragment.setArguments(args);
        return chatFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvChat = (RecyclerView) view.findViewById(R.id.rvChat);
        etMessage = (EditText) view.findViewById(R.id.etMessage);
        btnSend = (ImageButton) view.findViewById(R.id.btnSendMessage);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListeners();
        initRecyclerView();
    }

    private void initRecyclerView() {
        rvChat.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvChat.setItemAnimator(new DefaultItemAnimator());
        rvChat.setAdapter(new ChatAdapter());
    }

    private void setListeners() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {

    }
}

package com.example.denis.firebasechat;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;

public class ChatFragment extends Fragment {

    private static final String LOG_TAG = "ChatFragment";
    private static final String UID_KEY = "user_key";

    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvOfflineMode;

    private Firebase mFirebaseMessagesRef, mFirebaseConnectionRef;
    private Query mFirebaseMessagesQuery;

    private ChatAdapter mChatAdapter;

    private String uid = "";

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
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvChat = (RecyclerView) view.findViewById(R.id.rvChat);
        etMessage = (EditText) view.findViewById(R.id.etMessage);
        btnSend = (ImageButton) view.findViewById(R.id.btnSendMessage);
        tvOfflineMode = (TextView) view.findViewById(R.id.tvOfflineMode);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Init path to messages
        mFirebaseMessagesRef = new Firebase(FBConstants.FIREBASE_URL).child(FBConstants.FIREBASE_MESSAGES);
        mFirebaseMessagesQuery = mFirebaseMessagesRef.limitToLast(FBConstants.FIREBASE_QUERY_LIMIT);
        //Init path to connection state
        mFirebaseConnectionRef = new Firebase(FBConstants.FIREBASE_URL).child(FBConstants.FIREBASE_CONNECTION_PATH);

        // Get user id
        Bundle args = getArguments();
        if (args != null && args.size() > 0) {
            uid = args.getString(UID_KEY);
        }

        initRecyclerView();
        setListeners();
        initMessageInput();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuLogout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        startFirebaseTracking();
        startOfflineModeTracking();
    }

    private void initMessageInput() {
        etMessage.requestFocus();
        etMessage.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)
                    sendMessage();
                return false;
            }
        });
    }

    private void startOfflineModeTracking() {
        mFirebaseConnectionRef.addValueEventListener(firebaseConnectionListener);
    }

    // Listener to monitor connection state
    private ValueEventListener firebaseConnectionListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            boolean connected = snapshot.getValue(Boolean.class);
            if (connected) {
                tvOfflineMode.setVisibility(View.GONE);
            } else {
                tvOfflineMode.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onCancelled(FirebaseError error) {
        }
    };

    private void startFirebaseTracking() {
        mChatAdapter.setCurrentUserId(uid);
        mFirebaseMessagesQuery.addChildEventListener(messagesChildEventListener);
    }

    private ChildEventListener messagesChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Message msg = dataSnapshot.getValue(Message.class);
            mChatAdapter.addMessage(msg);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            logout();
        }
    };

    private void initRecyclerView() {
        rvChat.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvChat.setItemAnimator(new DefaultItemAnimator());
        mChatAdapter = new ChatAdapter(getActivity().getApplicationContext());
        rvChat.setAdapter(mChatAdapter);
    }

    private void setListeners() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        mChatAdapter.setMessageAddedCallback(new ChatAdapter.MessageAddedCallback() {
            @Override
            public void messageAdded() {
                rvChat.getLayoutManager().scrollToPosition(mChatAdapter.getItemCount() - 1);
            }
        });
    }

    private void sendMessage() {
        String msgText = etMessage.getText().toString();
        if (!msgText.isEmpty()) {
            etMessage.setText("");
            Message msg = new Message(uid, msgText);
            mFirebaseMessagesRef.push().setValue(msg.getObjectMapping());
        }
    }

    private void logout() {
        LoginManager.getInstance().logOut();
        mFirebaseMessagesRef.unauth();
        SharedPrefUtils.clearToken(getActivity());
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new LoginFragment()).commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        mFirebaseMessagesQuery.removeEventListener(messagesChildEventListener);
        mFirebaseConnectionRef.removeEventListener(firebaseConnectionListener);
    }
}

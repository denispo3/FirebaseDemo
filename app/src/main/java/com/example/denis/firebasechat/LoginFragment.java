package com.example.denis.firebasechat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Map;


public class LoginFragment extends Fragment {

    private EditText etName, etEmail, etPassword;
    private RelativeLayout rlRootContainer;

    private Firebase mFirebaseRootRef;

    private static final String LOG_TAG = "LoginFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = (EditText) view.findViewById(R.id.etName);
        etEmail = (EditText) view.findViewById(R.id.etEmail);
        etPassword = (EditText) view.findViewById(R.id.etPassword);
        rlRootContainer = (RelativeLayout) view.findViewById(R.id.rlRootContainer);

        view.findViewById(R.id.btnGoChatting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }
        });

        mFirebaseRootRef = new Firebase(Constants.FIREBASE_URL);

        tryAutoLogin();
    }

    private void tryAutoLogin() {
        mFirebaseRootRef.authWithCustomToken(getToken(), new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                runChatFragment(authData.getUid());
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
            }
        });
    }

    public void createUser() {
        final String email = etEmail.getText().toString();
        final String password = etPassword.getText().toString();

        mFirebaseRootRef.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> stringObjectMap) {
                authUser(email, password, true);
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                if (firebaseError.getCode() == FirebaseError.EMAIL_TAKEN) {
                    authUser(email, password, !etName.getText().toString().isEmpty());
                } else {
                    Log.d(LOG_TAG, "" + firebaseError.getMessage());
                    Snackbar.make(rlRootContainer, firebaseError.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void authUser(final String email, String password, final boolean updateUser) {
        mFirebaseRootRef.authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(final AuthData authData) {
                saveToken(authData.getToken());
                Log.d(LOG_TAG, "auth: " + authData.toString());
                if (updateUser) {
                    User user = new User();
                    user.email = email;
                    user.name = etName.getText().toString();
                    user.avatarPath = (String) authData.getProviderData().get("profileImageURL");
                    mFirebaseRootRef.child(Constants.FIREBASE_USERS).child(authData.getUid()).setValue(user, new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            runChatFragment(authData.getUid());
                        }
                    });
                } else {
                    runChatFragment(authData.getUid());
                }
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Snackbar.make(rlRootContainer, firebaseError.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void runChatFragment(String uid) {
        getFragmentManager().beginTransaction().replace(R.id.flContainer, ChatFragment.newInstance(uid)).commit();
    }

    private void saveToken(String token) {
        SharedPreferences sp = getActivity().getPreferences(Context.MODE_PRIVATE);
        sp.edit().putString(Constants.SHARED_PREFS_USER_TOKEN_KEY, token).apply();
    }

    private String getToken() {
        SharedPreferences sp = getActivity().getPreferences(Context.MODE_PRIVATE);
        return sp.getString(Constants.SHARED_PREFS_USER_TOKEN_KEY, "");
    }
}

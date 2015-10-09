package com.example.denis.firebasechat;

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


public class MainFragment extends Fragment {

    private EditText etName, etEmail, etPassword;
    private RelativeLayout rlRootContainer;

    private Firebase mFirebaseRef;

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
    }

    public void createUser() {
        mFirebaseRef = new Firebase("https://burning-fire-3180.firebaseio.com/");

        mFirebaseRef.createUser(etEmail.getText().toString(), etPassword.getText().toString(), new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> stringObjectMap) {
                authUser();
                //Log.d("MainFragment", "" + stringObjectMap.get("uid"));
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                if (firebaseError.getCode() == FirebaseError.EMAIL_TAKEN) {
                    authUser();
                } else {
                    Log.d("MainFragment", "" + firebaseError.getMessage());
                    Snackbar.make(rlRootContainer, firebaseError.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void authUser() {
        mFirebaseRef.authWithPassword(etEmail.getText().toString(), etPassword.getText().toString(), new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Log.d("MainFragment", "auth: " + authData.toString());
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Snackbar.make(rlRootContainer, firebaseError.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }
}

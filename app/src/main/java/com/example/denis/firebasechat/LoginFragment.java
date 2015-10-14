package com.example.denis.firebasechat;

import android.app.DialogFragment;
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

        mFirebaseRootRef = new Firebase(FBConstants.FIREBASE_URL);

        tryAutoLogin();
    }

    private void showLoadingDialog() {
        LoadingDialog loadingDialog = new LoadingDialog();
        loadingDialog.show(getFragmentManager(), LoginFragment.class.getSimpleName());
    }

    private void hideLoadingDialog() {
        DialogFragment loadingDialog = (DialogFragment) getFragmentManager().findFragmentByTag(LoginFragment.class.getSimpleName());
        if (loadingDialog != null)
            loadingDialog.dismissAllowingStateLoss();
    }

    private void tryAutoLogin() {
        String token = SharedPrefUtils.getToken(getActivity());
        if (token != null && !token.isEmpty()) {
            showLoadingDialog();
            mFirebaseRootRef.authWithCustomToken(token, new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    hideLoadingDialog();
                    SharedPrefUtils.saveToken(getActivity(), authData.getToken());
                    runChatFragment(authData.getUid());
                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    hideLoadingDialog();
                }
            });
        }
    }

    public void createUser() {
        showLoadingDialog();
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
                    hideLoadingDialog();
                    //Log.d(LOG_TAG, "" + firebaseError.getMessage());
                    Snackbar.make(rlRootContainer, firebaseError.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void authUser(final String email, String password, final boolean updateUser) {
        mFirebaseRootRef.authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(final AuthData authData) {
                hideLoadingDialog();
                SharedPrefUtils.saveToken(getActivity(), authData.getToken());
                //Log.d(LOG_TAG, "auth: " + authData.toString());
                if (updateUser) {
                    User user = new User();
                    user.email = email;
                    user.name = etName.getText().toString();
                    user.avatarPath = (String) authData.getProviderData().get("profileImageURL");
                    mFirebaseRootRef.child(FBConstants.FIREBASE_USERS).child(authData.getUid()).setValue(user, new Firebase.CompletionListener() {
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
                hideLoadingDialog();
                Snackbar.make(rlRootContainer, firebaseError.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void runChatFragment(String uid) {
        getFragmentManager().beginTransaction().replace(R.id.flContainer, ChatFragment.newInstance(uid)).commit();
    }

}

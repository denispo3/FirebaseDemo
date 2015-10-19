package com.example.denis.firebasechat;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Map;


public class LoginFragment extends Fragment implements View.OnClickListener {

    private EditText etName, etEmail, etPassword;
    private CoordinatorLayout rlRootContainer;
    private TextView tvResetPassword;
    private Button btnGoChatting;

    private LoginButton btnFacebookLogin;
    private CallbackManager callbackManager;

    private Firebase mFirebaseRootRef;

    private static final String LOG_TAG = "LoginFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //FacebookUtil.facebookHashKey(getActivity());
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = (EditText) view.findViewById(R.id.etName);
        etEmail = (EditText) view.findViewById(R.id.etEmail);
        etPassword = (EditText) view.findViewById(R.id.etPassword);
        rlRootContainer = (CoordinatorLayout) view.findViewById(R.id.rlRootContainer);
        tvResetPassword = (TextView) view.findViewById(R.id.tvForgotPassword);
        btnGoChatting = (Button) view.findViewById(R.id.btnGoChatting);
        btnFacebookLogin = (LoginButton) view.findViewById(R.id.btnFacebookLogin);

        setListeners();
        configureFacebook();

        mFirebaseRootRef = new Firebase(FBConstants.FIREBASE_URL);
        tryToAutoLogin();
    }

    private void configureFacebook() {
        btnFacebookLogin.setReadPermissions("email");
        btnFacebookLogin.setFragment(this);
        btnFacebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                showLoadingDialog();
                mFirebaseRootRef.authWithOAuthToken("facebook", AccessToken.getCurrentAccessToken().getToken(), new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        hideLoadingDialog();
                        //Log.d(LOG_TAG, "fb: " + authData.toString());
                        User user = new User();
                        user.email = authData.getProviderData().get("email").toString();
                        user.name = authData.getProviderData().get("displayName").toString();
                        user.avatarPath = (String) authData.getProviderData().get("profileImageURL");
                        processAuthData(authData, user, true);
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        hideLoadingDialog();
                        Snackbar.make(rlRootContainer, firebaseError.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
                Log.d(LOG_TAG, AccessToken.getCurrentAccessToken().getToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }
        });
    }

    private void tryToAutoLogin() {
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
                User user = new User();
                user.email = etEmail.getText().toString();
                user.name = etName.getText().toString();
                user.avatarPath = (String) authData.getProviderData().get("profileImageURL");
                processAuthData(authData, user, updateUser);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                hideLoadingDialog();
                Snackbar.make(rlRootContainer, firebaseError.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void processAuthData(final AuthData authData, User user, boolean updateUser) {
        hideLoadingDialog();
        SharedPrefUtils.saveToken(getActivity(), authData.getToken());
        //Log.d(LOG_TAG, "auth: " + authData.toString());
        if (updateUser) {
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

    private void runChatFragment(String uid) {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, ChatFragment.newInstance(uid)).commit();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnGoChatting:
                createUser();
                break;
            case R.id.tvForgotPassword:
                resetPassword();
                break;
        }
    }

    private void resetPassword() {
        String email = etEmail.getText().toString();
        if (!email.isEmpty()) {
            showLoadingDialog();
            mFirebaseRootRef.resetPassword(email, new Firebase.ResultHandler() {
                @Override
                public void onSuccess() {
                    hideLoadingDialog();
                    Snackbar.make(rlRootContainer, "You will receive new password on email", Snackbar.LENGTH_LONG).show();
                }

                @Override
                public void onError(FirebaseError firebaseError) {
                    hideLoadingDialog();
                    Snackbar.make(rlRootContainer, firebaseError.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void setListeners() {
        btnGoChatting.setOnClickListener(this);
        tvResetPassword.setOnClickListener(this);
    }

    private void showLoadingDialog() {
        LoadingDialog loadingDialog = new LoadingDialog();
        loadingDialog.show(getActivity().getFragmentManager(), LoginFragment.class.getSimpleName());
    }

    private void hideLoadingDialog() {
        DialogFragment loadingDialog = (DialogFragment) getActivity().getFragmentManager().findFragmentByTag(LoginFragment.class.getSimpleName());
        if (loadingDialog != null)
            loadingDialog.dismissAllowingStateLoss();
    }

}

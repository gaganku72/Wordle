package com.zuescoder69.wordle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.zuescoder69.wordle.databinding.FragmentLoginBinding;
import com.zuescoder69.wordle.userData.SessionManager;
import com.zuescoder69.wordle.utils.CommonValues;

import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends BaseFragment {
    private FragmentLoginBinding binding;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private NavController navCo;
    private Animation scaleUp, scaleDown;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scaleUp = AnimationUtils.loadAnimation(getContext(), R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(getContext(), R.anim.scale_down);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.mainFragment);
        navCo = navHostFragment.getNavController();
        startGoogleLogin();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void startGoogleLogin() {
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);
        binding.loginBtn.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.loginBtn.startAnimation(scaleUp);
                resultLauncher.launch(new Intent(mGoogleSignInClient.getSignInIntent()));
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.loginBtn.startAnimation(scaleDown);
            }
            return true;
        });
    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {

            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent intent = result.getData();
                try {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
                    handleSignInResult(task);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "3", Toast.LENGTH_SHORT).show();
                    Log.i("LoginFragment", e.getMessage());
                }

            }
        }
    });

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount acc = completedTask.getResult(ApiException.class);
            Log.d("LoginFragment", "Login Success");
            FirebaseGoogleAuth(acc);
        } catch (ApiException e) {
            Log.e("LoginFragment", "Exception: " + Log.getStackTraceString(e));
            showToast("Login Failed");
            FirebaseGoogleAuth(null);
        }
    }

    private void FirebaseGoogleAuth(GoogleSignInAccount acct) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(getActivity(), task -> {
                    try {
                        if (task.isSuccessful()) {
                            boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                            String userfirebaseid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            String userEmailID = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                            String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                            int firstSpace = userName.indexOf(" "); // detect the first space character
                            String firstName = userName.substring(0, firstSpace);

                            /**
                             * Adding user detals in the Shared Preferences for the user session
                             */
                            SessionManager sessionManager = new SessionManager(getContext());
                            sessionManager.createLoginSession(userEmailID, userfirebaseid, userName, firstName);
                            if (isNew) {
                                databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("User Details").child(userfirebaseid);
                                Map setValues = new HashMap();
                                setValues.put("eMail ID", userEmailID);
                                setValues.put("Name", userName);
                                setValues.put("NewUser", "Yes");
                                databaseReference.setValue(setValues);

                            }
                            new Handler().postDelayed(() -> {
                                showToast("Welcome " + userName);
                                navCo.navigate(R.id.action_loginFragment_to_menu_fragment);
                            }, 2000);

                        } else {
                            Log.d("LoginFragment", "UnSuccessfull");
                        }
                    } catch (Exception e) {
                        Log.e("LoginFragment", "Exception: " + Log.getStackTraceString(e));
                    }


                });
    }

    @Override
    public void onStart() {
        super.onStart();
        /**
         * Opening the notes list fragment is the user is logged in.
         */
        if (mAuth.getCurrentUser() != null) {
            navCo.navigate(R.id.action_loginFragment_to_menu_fragment);
        }
    }

    private void showToast(String msg) {
        showToast(msg, getContext(), getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        CommonValues.currentFragment = CommonValues.loginFragment;
    }
}
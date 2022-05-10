package com.zuescoder69.wordle;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RadioGroup;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.zuescoder69.wordle.databinding.FragmentMenuBinding;
import com.zuescoder69.wordle.databinding.FragmentSettingsBinding;
import com.zuescoder69.wordle.userData.SessionManager;
import com.zuescoder69.wordle.utils.CommonValues;

public class SettingsFragment extends BaseFragment {
    private FragmentSettingsBinding binding;
    private boolean themeDark, vibration;
    private SessionManager sessionManager;
    private Animation scaleUp, scaleDown;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scaleUp = AnimationUtils.loadAnimation(getContext(), R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(getContext(), R.anim.scale_down);
        sessionManager = new SessionManager(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);
        setUpUI();
        setUpOnClicks();
    }

    private void setUpUI() {
//        binding.themeGroup.setVisibility(View.GONE);
//        binding.themeTxt.setVisibility(View.GONE);
        themeDark = sessionManager.getBooleanKey(CommonValues.THEME_DARK);
        vibration = sessionManager.getBooleanKey(CommonValues.VIBRATION);

        if (themeDark) {
            binding.themeLight.setChecked(true);
        } else {
            binding.themeDark.setChecked(true);
        }

        if (vibration) {
            binding.vibrationOn.setChecked(true);
        } else {
            binding.vibrationOff.setChecked(true);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpOnClicks() {
        binding.themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.themeDark.getId()) {
                sessionManager.addBooleanKey(CommonValues.THEME_DARK, false);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                sessionManager.addBooleanKey(CommonValues.THEME_DARK, true);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });

        binding.vibrationGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.vibrationOn.getId()) {
                sessionManager.addBooleanKey(CommonValues.VIBRATION, true);
            } else {
                sessionManager.addBooleanKey(CommonValues.VIBRATION, false);
            }
        });

        binding.logoutBtn.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.logoutBtn.startAnimation(scaleUp);
                sessionManager.clearSession();
                mGoogleSignInClient.signOut();
                mAuth.signOut();
                Navigation.findNavController(getView()).navigate(R.id.action_settingsFragment_to_loginFragment);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.logoutBtn.startAnimation(scaleDown);
            }
            return true;
        });
    }
}
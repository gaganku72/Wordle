package com.zuescoder69.wordle;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.zuescoder69.wordle.databinding.FragmentMenuBinding;
import com.zuescoder69.wordle.databinding.FragmentSettingsBinding;
import com.zuescoder69.wordle.userData.SessionManager;
import com.zuescoder69.wordle.utils.CommonValues;

public class SettingsFragment extends BaseFragment {
    private FragmentSettingsBinding binding;
    private boolean themeDark, vibration;
    private SessionManager sessionManager;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        setUpUI();
        setUpOnClicks();
    }

    private void setUpUI() {
        binding.themeGroup.setVisibility(View.GONE);
        binding.themeTxt.setVisibility(View.GONE);
        themeDark = sessionManager.getBooleanKey(CommonValues.THEME_DARK);
        vibration = sessionManager.getBooleanKey(CommonValues.VIBRATION);

        if (themeDark) {
            binding.themeDark.setChecked(true);
        } else {
            binding.themeLight.setChecked(true);
        }

        if (vibration) {
            binding.vibrationOn.setChecked(true);
        } else {
            binding.vibrationOff.setChecked(true);
        }
    }

    private void setUpOnClicks() {
        binding.themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.themeDark.getId()) {
                sessionManager.addBooleanKey(CommonValues.THEME_DARK, true);
                changeTheme();
            } else {
                sessionManager.addBooleanKey(CommonValues.THEME_DARK, false);
            }
        });

        binding.vibrationGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.vibrationOn.getId()) {
                sessionManager.addBooleanKey(CommonValues.VIBRATION, true);
            } else {
                sessionManager.addBooleanKey(CommonValues.VIBRATION, false);
            }
        });
    }

    private void changeTheme() {

    }
}
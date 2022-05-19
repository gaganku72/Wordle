package com.zuescoder69.wordle;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.zuescoder69.wordle.databinding.FragmentOnBoardingBinding;
import com.zuescoder69.wordle.userData.SessionManager;
import com.zuescoder69.wordle.utils.CommonValues;

public class OnBoardingFragment extends BaseFragment {
    private FragmentOnBoardingBinding binding;
    private Animation scaleUp, scaleDown;

    public OnBoardingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scaleUp = AnimationUtils.loadAnimation(getContext(), R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(getContext(), R.anim.scale_down);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentOnBoardingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpOnClick();
        setTheme();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpOnClick() {
        binding.playGameBtn.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.playGameBtn.startAnimation(scaleUp);
                Navigation.findNavController(getView()).navigate(R.id.action_onBoardingFragment_to_menu_fragment);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.playGameBtn.startAnimation(scaleDown);
            }
            return true;
        });
    }

    private void setTheme() {
        SessionManager sessionManager = new SessionManager(getContext());
        boolean isThemeBlack = sessionManager.getBooleanKey(CommonValues.THEME_DARK);
        if (isThemeBlack) {
            binding.row12.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row13.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row14.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row15.setTextColor(getContext().getColor(R.color.no_bg_txt));

            binding.row21.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row23.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row24.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row25.setTextColor(getContext().getColor(R.color.no_bg_txt));

            binding.row31.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row32.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row33.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row35.setTextColor(getContext().getColor(R.color.no_bg_txt));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        CommonValues.currentFragment = CommonValues.onBoardingFragment;
    }
}
package com.zuescoder69.wordle;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.zuescoder69.wordle.databinding.FragmentMenuFragmentBinding;
import com.zuescoder69.wordle.utils.CommonValues;

import java.util.Map;

public class menu_fragment extends BaseFragment {
    private FragmentMenuFragmentBinding binding;
    private Animation scaleUp, scaleDown;
    private boolean isForceUpdate = false;

    public menu_fragment() {
        // Required empty public constructor
    }

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
        binding = FragmentMenuFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupOnClicks();
        getInitialData();
    }

    private void getInitialData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("AppData");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {};
                    Map<String, Object> map = dataSnapshot.getValue(genericTypeIndicator);
                    CommonValues.versionCodeFirebase = (String) map.get("VersionCode");
                    CommonValues.comeTomorrowMsg = (String) map.get("ComeTomorrowMsg");
                    String toShowAd = (String) map.get("ShowAd");
                    if (toShowAd.equalsIgnoreCase("true")) {
                        CommonValues.isShowAd = true;
                    } else {
                        CommonValues.isShowAd = false;
                    }

                    if(CommonValues.versionCode.equalsIgnoreCase(CommonValues.versionCodeFirebase)) {
                        isForceUpdate = false;
                    } else {
                        isForceUpdate = true;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupOnClicks() {
        binding.classicBtn.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.classicBtn.startAnimation(scaleUp);
                if (!isForceUpdate) {
                    Bundle bundle = new Bundle();
                    bundle.putString("gameMode", "classic");
                    Navigation.findNavController(getView()).navigate(R.id.action_menu_fragment_to_gameFragment, bundle);
                } else {
                    showToast("App Update REQUIRED");
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.classicBtn.startAnimation(scaleDown);
            }
            return true;
        });

        binding.dailyBtn.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.dailyBtn.startAnimation(scaleUp);
                if (!isForceUpdate) {
                    Bundle bundle = new Bundle();
                    bundle.putString("gameMode", "daily");
                    Navigation.findNavController(getView()).navigate(R.id.action_menu_fragment_to_gameFragment, bundle);
                } else {
                    showToast("App Update REQUIRED");
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.dailyBtn.startAnimation(scaleDown);
            }
            return true;
        });

        binding.statsBtn.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.dailyBtn.startAnimation(scaleUp);
                if (!isForceUpdate) {
                    Navigation.findNavController(getView()).navigate(R.id.action_menu_fragment_to_statsFragment);
                } else {
                    showToast("App Update REQUIRED");
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.dailyBtn.startAnimation(scaleDown);
            }
            return true;
        });
    }

    private void showToast(String msg) {
        showToast(msg, getContext(), getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        CommonValues.currentFragment = CommonValues.menuFragment;
    }
}
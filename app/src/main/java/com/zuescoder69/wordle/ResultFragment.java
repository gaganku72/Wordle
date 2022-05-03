package com.zuescoder69.wordle;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.zuescoder69.wordle.databinding.FragmentResultBinding;
import com.zuescoder69.wordle.utils.CommonValues;

public class ResultFragment extends Fragment {
    private FragmentResultBinding binding;
    private String answer = "";
    private Animation scaleUp, scaleDown;

    public ResultFragment() {
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
        binding = FragmentResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpOnClick();
        CommonValues.currentFragment = CommonValues.resultFragment;
        String winner = getArguments().getString("winnerName");
        if (getArguments().containsKey("answer")) {
            answer = getArguments().getString("answer");
        }
        binding.win.setVisibility(View.INVISIBLE);
        binding.lost.setVisibility(View.INVISIBLE);
        binding.answerBtn.setVisibility(View.GONE);
        binding.answerTv.setVisibility(View.GONE);
        if (winner.equalsIgnoreCase("lost")) {
            binding.winnerName.setText("You both lost");
            binding.lost.setVisibility(View.VISIBLE);
            binding.win.setVisibility(View.INVISIBLE);
            loadRewardedAd();
        } else {
            binding.winnerName.setText(winner);
            binding.lost.setVisibility(View.INVISIBLE);
            binding.win.setVisibility(View.VISIBLE);

            Handler handler1 = new Handler();
            handler1.postDelayed(() -> {
                if (CommonValues.currentFragment.equalsIgnoreCase(CommonValues.gameFragment)) {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    Navigation.findNavController(getView()).navigate(R.id.action_resultFragment_to_menu_fragment);
                }
            }, 5000);
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("Rooms").child(CommonValues.roomDate).child(CommonValues.roomId);
        databaseReference.removeValue();
    }

    private void loadRewardedAd() {
        if (CommonValues.mRewardedAd == null && CommonValues.isShowAd) {
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(getActivity(), CommonValues.rewardAdId,
                    adRequest, new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error.
                            CommonValues.mRewardedAd = null;
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            CommonValues.mRewardedAd = rewardedAd;
                            binding.answerBtn.setVisibility(View.VISIBLE);
                        }
                    });
        } else if (CommonValues.mRewardedAd != null && CommonValues.isShowAd) {
            binding.answerBtn.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpOnClick() {
        binding.answerBtn.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.answerBtn.startAnimation(scaleUp);
                if (CommonValues.mRewardedAd != null) {
                    CommonValues.mRewardedAd.show(getActivity(), rewardItem -> {
                        CommonValues.mRewardedAd = null;
                        loadRewardedAd();
                        binding.answerTv.setVisibility(View.VISIBLE);
                        binding.answerTv.setText("Worlde is - " + answer);
                    });
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.answerBtn.startAnimation(scaleDown);
            }
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
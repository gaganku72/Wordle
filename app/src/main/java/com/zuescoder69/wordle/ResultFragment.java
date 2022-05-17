package com.zuescoder69.wordle;

import android.annotation.SuppressLint;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.zuescoder69.wordle.databinding.FragmentResultBinding;
import com.zuescoder69.wordle.params.Params;
import com.zuescoder69.wordle.userData.SessionManager;
import com.zuescoder69.wordle.utils.CommonValues;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class ResultFragment extends Fragment {
    private FragmentResultBinding binding;
    private String answer = "";
    private Animation scaleUp, scaleDown;
    private boolean isWinner = false;
    private final String wordInDB = "Word";
    private final String classic = Params.CLASSIC_GAME_MODE;
    private String userId = "", winnerId, roomId, wordsCount, wordId, currentDate;
    private String userId1 = "", userId2 = "";
    private SessionManager sessionManager;
    private DatabaseReference databaseReference;

    public ResultFragment() {
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userId = sessionManager.getStringKey(Params.KEY_USER_ID);
        binding.resultLayout.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);
        getCurrentDate();
        setUpOnClick();
        CommonValues.currentFragment = CommonValues.resultFragment;
        String winner = getArguments().getString("winnerName");
        winnerId = getArguments().getString("winnerId");
        roomId = getArguments().getString("roomId");
        userId1 = getArguments().getString("userId1");
        userId2 = getArguments().getString("userId2");
        setUpRestartGame();
        if (getArguments().containsKey("answer")) {
            answer = getArguments().getString("answer");
        }
        binding.win.setVisibility(View.INVISIBLE);
        binding.lost.setVisibility(View.INVISIBLE);
        binding.restartGameBtn.setVisibility(View.VISIBLE);
        binding.answerBtn.setVisibility(View.GONE);
        binding.answerTv.setVisibility(View.GONE);
        if (winner.equalsIgnoreCase("lost")) {
            binding.winnerName.setText("You both lost");
            binding.lost.setVisibility(View.VISIBLE);
            binding.win.setVisibility(View.INVISIBLE);
            loadRewardedAd();
        } else {
            if (userId.equalsIgnoreCase(winnerId)) {
                isWinner = true;
                binding.winnerName.setText(winner);
                binding.lost.setVisibility(View.INVISIBLE);
                binding.win.setVisibility(View.VISIBLE);
            } else {
                isWinner = false;
                loadRewardedAd();
                binding.winnerName.setText(winner);
                binding.lost.setVisibility(View.INVISIBLE);
                binding.win.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setUpRestartGame() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("Rooms").child(CommonValues.roomDate).child(roomId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {
                    };
                    Map<String, Object> map = dataSnapshot.getValue(genericTypeIndicator);
                    String userRestartStatus1 = (String) map.get("UserRestartStatus1");
                    String userRestartStatus2 = (String) map.get("UserRestartStatus2");
                    binding.resultLayout.setVisibility(View.VISIBLE);
                    binding.progress.setVisibility(View.GONE);

                    if (userRestartStatus1.equalsIgnoreCase("Yes")) {
                        if (userId.equalsIgnoreCase(userId1)) {
                            binding.restartStatus.setVisibility(View.VISIBLE);
                            binding.restartStatus.setText("Re-Match game request sent!");
                        } else {
                            binding.restartStatus.setVisibility(View.VISIBLE);
                            binding.restartStatus.setText("Opponent wants a Re-Match");
                        }
                    } else if (userRestartStatus2.equalsIgnoreCase("Yes")) {
                        if (userId.equalsIgnoreCase(userId2)) {
                            binding.restartStatus.setVisibility(View.VISIBLE);
                            binding.restartStatus.setText("Re-Match game request sent!");
                        } else {
                            binding.restartStatus.setVisibility(View.VISIBLE);
                            binding.restartStatus.setText("Opponent wants a Re-Match");
                        }
                    }
                    if (userRestartStatus1.equalsIgnoreCase("Yes") && userRestartStatus2.equalsIgnoreCase("Yes")) {
                        restartGame();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void restartGame() {
        if (CommonValues.currentFragment.equalsIgnoreCase(CommonValues.resultFragment)) {
            getAnswer();
        }
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
                        binding.answerTv.setText("Wordly is - " + answer);
                    });
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.answerBtn.startAnimation(scaleDown);
            }
            return true;
        });

        binding.restartGameBtn.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.restartGameBtn.startAnimation(scaleUp);
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("Rooms").child(CommonValues.roomDate).child(roomId);
                Map setValues = new HashMap();
                if (userId.equalsIgnoreCase(userId1)) {
                    setValues.put("UserRestartStatus1", "Yes");
                }
                else if (userId.equalsIgnoreCase(userId2)) {
                    setValues.put("UserRestartStatus2", "Yes");
                }
                databaseReference.updateChildren(setValues);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.restartGameBtn.startAnimation(scaleDown);
            }
            return true;
        });

        binding.homeBtn.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.homeBtn.startAnimation(scaleUp);
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("Rooms").child(CommonValues.roomDate).child(CommonValues.roomId);
                databaseReference.removeValue();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.homeBtn.startAnimation(scaleDown);
            }
            return true;
        });
    }

    private void getAnswer() {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("AppData");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("WordsCount")) {
                        GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {
                        };
                        Map<String, Object> map = snapshot.getValue(genericTypeIndicator);
                        wordsCount = (String) map.get("WordsCount");
                        wordId = getRandomNumber();
                        databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("User Details").child(userId);
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("User Details").child(userId).child(classic).child(currentDate);
                                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                if (!dataSnapshot.hasChild(wordInDB + wordId)) {
                                                    databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("Words");
                                                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.exists()) {
                                                                GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {
                                                                };
                                                                Map<String, Object> map = dataSnapshot.getValue(genericTypeIndicator);
                                                                if (dataSnapshot.hasChild(wordInDB + wordId)) {
                                                                    answer = (String) map.get(wordInDB + wordId);
                                                                    answer = answer.toUpperCase();
                                                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("Rooms").child(CommonValues.roomDate).child(roomId);
                                                                    Map setValues = new HashMap();
                                                                    setValues.put("Answer", answer);
                                                                    setValues.put("WinnerId", "");
                                                                    setValues.put("WordId", wordId);
                                                                    setValues.put("WinnerName", "");
                                                                    setValues.put("Lobby Status", "In-Game");
                                                                    setValues.put("UserStatus1", "yes");
                                                                    setValues.put("UserStatus2", "yes");
                                                                    setValues.put("UserRestartStatus1", "No");
                                                                    setValues.put("UserRestartStatus2", "No");
                                                                    databaseReference.updateChildren(setValues);
                                                                    Handler handler = new Handler();
                                                                    handler.postDelayed(() -> {
                                                                        Bundle bundle = new Bundle();
                                                                        bundle.putString("gameMode", Params.MULTI_GAME_MODE);
                                                                        if (getView() != null) {
                                                                            Navigation.findNavController(getView()).navigate(R.id.action_resultFragment_to_gameFragment, bundle);
                                                                        }
                                                                    },1000);
//                                                                    RoomFragmentDirections.ActionRoomFragmentToLobbyFragment action = RoomFragmentDirections.actionRoomFragmentToLobbyFragment(newRoomId);
//                                                                    Navigation.findNavController(getView()).navigate(action);
//                                                                    binding.progressBar.setVisibility(View.GONE);
                                                                } else {
                                                                    getAnswer();
                                                                }

                                                                Log.d("DEMON", "onDataChange: Answer-)" + answer);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });
                                                } else {
                                                    getAnswer();
                                                }
                                            } else {
                                                Map setValues = new HashMap();
                                                setValues.put(wordInDB + wordId, "done");
                                                databaseReference.updateChildren(setValues);

                                                databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("Words");
                                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()) {
                                                            GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {
                                                            };
                                                            Map<String, Object> map = dataSnapshot.getValue(genericTypeIndicator);
                                                            if (dataSnapshot.hasChild(wordInDB + wordId)) {
                                                                answer = (String) map.get(wordInDB + wordId);
                                                            } else {
                                                                getAnswer();
                                                            }
                                                            answer = answer.toUpperCase();
                                                            Log.d("DEMON", "onDataChange: Answer-)" + answer);
//                                                                showToast(answer);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getRandomNumber() {
        Random rand = new Random();
        int randomNum = rand.nextInt(Integer.parseInt(wordsCount));
        randomNum = randomNum + 1;
        return String.valueOf(randomNum);
    }

    private void getCurrentDate() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        currentDate = df.format(c);
        Log.d("DEMON", "getCurrentDate: date-)" + currentDate);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
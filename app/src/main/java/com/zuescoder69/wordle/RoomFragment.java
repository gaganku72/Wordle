package com.zuescoder69.wordle;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.zuescoder69.wordle.databinding.FragmentRoomBinding;
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

public class RoomFragment extends BaseFragment {
    private FragmentRoomBinding binding;
    private final String wordInDB = "Word";
    private final String classic = Params.CLASSIC_GAME_MODE;
    private Animation scaleUp, scaleDown;
    private String newRoomId, wordsCount, wordId, answer, userId, currentDate, userName;
    private DatabaseReference databaseReference;

    public RoomFragment() {
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
        binding = FragmentRoomBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CommonValues.currentFragment = CommonValues.roomFragment;
        binding.progressBar.setVisibility(View.VISIBLE);
        getCurrentDate();
        getData();
        setUpOnClicks();
    }

    private void getData() {
        SessionManager sessionManager = new SessionManager(getContext());
        userName = sessionManager.getStringKey(Params.KEY_USER_NAME);
        userId = sessionManager.getStringKey(Params.KEY_USER_ID);
        binding.progressBar.setVisibility(View.GONE);
        binding.createRoomBtn.setVisibility(View.VISIBLE);
        binding.joinRoomBtn.setVisibility(View.VISIBLE);

        if (getArguments() != null) {
            binding.roomId.setText(getArguments().getString("rooomId"));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpOnClicks() {
        binding.createRoomBtn.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.createRoomBtn.startAnimation(scaleUp);
                binding.progressBar.setVisibility(View.VISIBLE);
                showToast("Creating room", getContext(), getActivity());
                createRoom();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.createRoomBtn.startAnimation(scaleDown);
            }
            return true;
        });

        binding.joinRoomBtn.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.joinRoomBtn.startAnimation(scaleUp);
                joinRoom();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.joinRoomBtn.startAnimation(scaleDown);
            }
            return true;
        });
    }

    private void joinRoom() {
        String rooomIdLocal = binding.roomId.getText().toString().trim();
        if (!TextUtils.isEmpty(rooomIdLocal)) {
            String roomDateLocal = rooomIdLocal.substring(0, 2);
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("Rooms").child(roomDateLocal).child(rooomIdLocal);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {
                        };
                        Map<String, Object> map = dataSnapshot.getValue(genericTypeIndicator);
                        String userId1 = (String) map.get("UserId1");
                        String userId2 = (String) map.get("UserId2");
                        String lobbyStatus = (String) map.get("Lobby Status");
                        if (lobbyStatus.equalsIgnoreCase("In-Game")) {
                            showToast("Wrong room ID", getContext(), getActivity());
                        } else {
                            if (userId.equalsIgnoreCase(userId1)) {
                                RoomFragmentDirections.ActionRoomFragmentToLobbyFragment action = RoomFragmentDirections.actionRoomFragmentToLobbyFragment(rooomIdLocal);
                                Navigation.findNavController(getView()).navigate(action);
                            } else {
                                databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("Rooms").child(roomDateLocal).child(rooomIdLocal);
                                Map setValues = new HashMap();
                                setValues.put("Max Limit", "2");
                                setValues.put("Current Limit", "2");
                                setValues.put("Lobby Status", "Lobby");
                                setValues.put("UserName2", userName);
                                setValues.put("UserId2", userId);
                                CommonValues.roomIds.add(rooomIdLocal);
                                databaseReference.updateChildren(setValues);
                                RoomFragmentDirections.ActionRoomFragmentToLobbyFragment action = RoomFragmentDirections.actionRoomFragmentToLobbyFragment(rooomIdLocal);
                                Navigation.findNavController(getView()).navigate(action);
                            }
                        }
                    } else {
                        showToast("Wrong room ID", getContext(), getActivity());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            showToast("Room ID Required", getContext(), getActivity());
        }
    }

    private void createRoom() {
        long max = 200;
        long min = 20;
        long range = max - min + 1;
        long rand = (long) (Math.random() * range) + min;
        long temp = rand;
        rand = (long) (Math.random() * range) + min;
        temp = rand + temp;
        rand = (long) (Math.random() * range) + min;
        temp = rand + temp;
        rand = (long) (Math.random() * range) + min;
        temp = rand + temp;
        rand = (long) (Math.random() * range) + min;
        temp = rand + temp;
        newRoomId = Long.toString(temp);
        newRoomId = CommonValues.roomDate + newRoomId;
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("Rooms").child(CommonValues.roomDate).child(newRoomId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    createRoom();
                } else {
                    getAnswer();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
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
                                                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("Rooms").child(CommonValues.roomDate).child(newRoomId);
                                                                    Map setValues = new HashMap();
                                                                    setValues.put("Max Limit", "2");
                                                                    setValues.put("Current Limit", "1");
                                                                    setValues.put("Lobby Status", "Open");
                                                                    setValues.put("UserName1", userName);
                                                                    setValues.put("UserName2", "");
                                                                    setValues.put("UserStatus1", "yes");
                                                                    setValues.put("UserStatus2", "yes");
                                                                    setValues.put("UserId1", userId);
                                                                    setValues.put("UserId2", "");
                                                                    setValues.put("Answer", answer);
                                                                    setValues.put("RoomId", newRoomId);
                                                                    setValues.put("WinnerId", "");
                                                                    setValues.put("WordId", wordId);
                                                                    setValues.put("WinnerName", "");
                                                                    setValues.put("UserRestartStatus1", "No");
                                                                    setValues.put("UserRestartStatus2", "No");
                                                                    databaseReference.setValue(setValues);
                                                                    CommonValues.roomIds.add(newRoomId);
                                                                    RoomFragmentDirections.ActionRoomFragmentToLobbyFragment action = RoomFragmentDirections.actionRoomFragmentToLobbyFragment(newRoomId);
                                                                    Navigation.findNavController(getView()).navigate(action);
                                                                    binding.progressBar.setVisibility(View.GONE);
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
        CommonValues.currentFragment = CommonValues.roomFragment;
    }
}
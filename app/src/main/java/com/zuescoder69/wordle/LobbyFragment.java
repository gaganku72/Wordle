package com.zuescoder69.wordle;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
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

import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.zuescoder69.wordle.databinding.FragmentLobbyBinding;
import com.zuescoder69.wordle.params.FirebaseParams;
import com.zuescoder69.wordle.params.Params;
import com.zuescoder69.wordle.userData.SessionManager;
import com.zuescoder69.wordle.utils.CommonValues;

import java.util.HashMap;
import java.util.Map;

public class LobbyFragment extends BaseFragment {
    private FragmentLobbyBinding binding;
    private Animation scaleUp, scaleDown;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceRealTime;
    private ValueEventListener valueEventListener;
    private String roomId = "", userName1 = "", userName2 = "", userId1 = "", userId2 = "", answer = "", lobbyStatus = "", userIdLocal = "";
    private FirebaseAnalytics mFirebaseAnalytics;

    public LobbyFragment() {
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
        binding = FragmentLobbyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CommonValues.currentFragment = CommonValues.lobbyFragment;
        binding.progress.setVisibility(View.VISIBLE);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        binding.lobby.setVisibility(View.GONE);
        setUpOnClicks();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpOnClicks() {
        binding.startGameBtn.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.startGameBtn.startAnimation(scaleUp);
                if (TextUtils.isEmpty(userId2)) {
                    showToast("So Alone In Life? Play Unlimited instead");
                } else {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("Rooms").child(CommonValues.roomDate).child(roomId);
                    Map setValues = new HashMap();
                    setValues.put("Max Limit", "2");
                    setValues.put("Current Limit", "2");
                    setValues.put("Lobby Status", "In-Game");
                    databaseReference.updateChildren(setValues);
                    Bundle bundle = new Bundle();
                    bundle.putString("gameMode", Params.MULTI_GAME_MODE);
//                    Navigation.findNavController(getView()).navigate(R.id.action_lobbyFragment_to_gameFragment, bundle);
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.startGameBtn.startAnimation(scaleDown);
            }
            return true;
        });

        binding.roomIdLyt.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.roomIdLyt.startAnimation(scaleUp);
                createLink();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.roomIdLyt.startAnimation(scaleDown);
            }
            return true;
        });
    }

    private void createLink() {
        String shareLink = "https://wordl.page.link/?" +
                "link=https://play.google.com/store/apps/details?id=com.zuescoder69.wordle/roomId-" + roomId +
                "&apn=" + getContext().getPackageName() +
                "&st=" + "Wordly" +
                "&sd=" + "Join the room and let's play." +
                "&si=" + "https://firebasestorage.googleapis.com/v0/b/name-place-animal-thing-b8644.appspot.com/o/icon.png?alt=media&token=85ac5cda-7e13-439b-9106-713430e0d5c3";

        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(Uri.parse(shareLink))
                .buildShortDynamicLink()
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Short link created
                        Log.d("123DEMON123", "createLink: success");
                        Uri shortLink = task.getResult().getShortLink();

                        try {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            String shareMessage = "Hey join the room using this link:\n" + shortLink.toString();
                            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                            shareIntent.setType("text/plain");
                            startActivity(Intent.createChooser(shareIntent, "choose one"));
                        } catch (Exception e) {
                            Log.e("123DEMON123", e.getMessage());
                        }
                    } else {
                        try {
                            Log.d("123DEMON123", "createLink: failed");
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Worlde");
                            String shareMessage = "Hey the room ID is:\n" + roomId;
                            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                            startActivity(Intent.createChooser(shareIntent, "choose one"));
                        } catch (Exception e) {
                            Log.e("123DEMON123", e.getMessage());
                        }
                    }
                });
    }

    private void getLobbyData() {
        roomId = LobbyFragmentArgs.fromBundle(getArguments()).getRoomId();
        CommonValues.roomId = roomId;
        SessionManager sessionManager = new SessionManager(getContext());
        userIdLocal = sessionManager.getStringKey(Params.KEY_USER_ID);
        databaseReferenceRealTime = FirebaseDatabase.getInstance().getReference().child("Wordle").child("Rooms").child(CommonValues.roomDate).child(roomId);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && CommonValues.currentFragment.equalsIgnoreCase(CommonValues.lobbyFragment)) {
                    GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {
                    };
                    Map<String, Object> map = dataSnapshot.getValue(genericTypeIndicator);
                    userName1 = (String) map.get("UserName1");
                    userId1 = (String) map.get("UserId1");
                    answer = (String) map.get("Answer");
                    userName2 = (String) map.get("UserName2");
                    userId2 = (String) map.get("UserId2");
                    lobbyStatus = (String) map.get("Lobby Status");
                    setVisibility();
                    checkLobbyStatus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseReferenceRealTime.addValueEventListener(valueEventListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (valueEventListener != null) {
            databaseReferenceRealTime.removeEventListener(valueEventListener);
        }
    }

    private void checkLobbyStatus() {
        if (CommonValues.currentFragment.equalsIgnoreCase(CommonValues.lobbyFragment)) {
            if (lobbyStatus.equalsIgnoreCase("In-Game")) {
                if (CommonValues.currentFragment.equalsIgnoreCase(CommonValues.lobbyFragment)) {
                    Bundle bundle = new Bundle();
                    bundle.putString("gameMode", Params.MULTI_GAME_MODE);
                    if (getView() != null) {
                        mFirebaseAnalytics.logEvent(FirebaseParams.ROOM_STARTED, null);
                        CommonValues.currentFragment = CommonValues.gameFragment;
                        Navigation.findNavController(getView()).navigate(R.id.action_lobbyFragment_to_gameFragment, bundle);
                    }
                }
            }
        }
    }

    private void setVisibility() {
        binding.player1.setText("Player 1 - " + userName1);
        binding.roomId.setText("Room ID - " + roomId);
        if (!TextUtils.isEmpty(userName2)) {
            binding.player2.setText("Player 2 - " + userName2);
            binding.player2.setVisibility(View.VISIBLE);
            binding.status.setVisibility(View.GONE);
        } else {
            binding.player2.setVisibility(View.GONE);
            binding.status.setText("Waiting for the 2nd player");
        }

        if (userIdLocal.equalsIgnoreCase(userId1)) {
            binding.startGameBtn.setVisibility(View.VISIBLE);
        } else {
            binding.startGameBtn.setVisibility(View.GONE);
            binding.status.setText("Player 1 will start the Game");
            binding.status.setVisibility(View.VISIBLE);
        }
        binding.progress.setVisibility(View.GONE);
        binding.lobby.setVisibility(View.VISIBLE);
    }

    private void showToast(String msg) {
        showToast(msg, getContext(), getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        CommonValues.currentFragment = CommonValues.lobbyFragment;
        getLobbyData();
    }
}
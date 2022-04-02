package com.zuescoder69.wordle.stats;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.zuescoder69.wordle.databinding.FragmentStatsBinding;
import com.zuescoder69.wordle.params.Params;
import com.zuescoder69.wordle.userData.SessionManager;

import java.util.Map;

public class StatsFragment extends Fragment {
    private FragmentStatsBinding binding;
    private int totalPlayed = 0;
    private int row1 = 0;
    private int row2 = 0;
    private int row3 = 0;
    private int row4 = 0;
    private int row5 = 0;
    private int row6 = 0;

    public StatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDataFromFirebase();
    }

    private void getDataFromFirebase() {
        SessionManager sessionManager = new SessionManager(getContext());
        String userId = sessionManager.getStringKey(Params.KEY_USER_ID);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("User Details").child(userId).child("GameData").child(Params.CLASSIC_GAME_MODE);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {
                    };
                    Map<String, Object> map = dataSnapshot.getValue(genericTypeIndicator);
                    row1 = Integer.parseInt((String) map.getOrDefault("row1", "0"));
                    row2 = Integer.parseInt((String) map.getOrDefault("row2", "0"));
                    row3 = Integer.parseInt((String) map.getOrDefault("row3", "0"));
                    row4 = Integer.parseInt((String) map.getOrDefault("row4", "0"));
                    row5 = Integer.parseInt((String) map.getOrDefault("row5", "0"));
                    row6 = Integer.parseInt((String) map.getOrDefault("row6", "0"));
                    totalPlayed = Integer.parseInt((String) map.getOrDefault("totalPlayed", "0"));

                    binding.score1.setText("" + row1);
                    binding.score2.setText("" + row2);
                    binding.score3.setText("" + row3);
                    binding.score4.setText("" + row4);
                    binding.score5.setText("" + row5);
                    binding.score6.setText("" + row6);

                    totalPlayed = totalPlayed * 100;
                    row1 = row1 * 100;
                    row2 = row2 * 100;
                    row3 = row2 * 100;
                    row4 = row2 * 100;
                    row5 = row2 * 100;
                    row6 = row2 * 100;
                    Log.d("DEMON", "onDataChange: Row 1 " + row1);
                    Log.d("DEMON", "onDataChange: Row 2 " + row2);
                    Log.d("DEMON", "onDataChange: Row 3 " + row3);
                    Log.d("DEMON", "onDataChange: Row 4 " + row4);
                    Log.d("DEMON", "onDataChange: Row 5 " + row5);
                    Log.d("DEMON", "onDataChange: Row 6 " + row6);
                    Thread thread = new Thread(() -> setProgress());
                    Handler handler = new Handler();
                    handler.postDelayed(() -> thread.start(), 100);



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setProgress() {
        binding.progressRow1.setMax(totalPlayed);
        binding.progressRow2.setMax(totalPlayed);
        binding.progressRow3.setMax(totalPlayed);
        binding.progressRow4.setMax(totalPlayed);
        binding.progressRow5.setMax(totalPlayed);
        binding.progressRow6.setMax(totalPlayed);

        binding.progressRow1.setProgress(0);
        binding.progressRow2.setProgress(0);
        binding.progressRow3.setProgress(0);
        binding.progressRow4.setProgress(0);
        binding.progressRow5.setProgress(0);
        binding.progressRow6.setProgress(0);
        boolean rowStatus1 = true;
        boolean rowStatus2 = true;
        boolean rowStatus3 = true;
        boolean rowStatus4 = true;
        boolean rowStatus5 = true;
        boolean rowStatus6 = true;
        if (row1 == 0)
            rowStatus1 = false;
        if (row2 == 0)
            rowStatus2 = false;
        if (row3 == 0)
            rowStatus3 = false;
        if (row4 == 0)
            rowStatus4 = false;
        if (row5 == 0)
            rowStatus5 = false;
        if (row6 == 0)
            rowStatus6 = false;

        for (int i = 1; i <= totalPlayed; i++) {
            try {
                Thread.sleep(5);

                if (rowStatus1) {
                    if (i <= row1)
                        binding.progressRow1.setProgress(i);
                    else if (row1 == 0)
                        binding.progressRow1.setProgress(0);
                }

                if (rowStatus2) {
                    if (i <= row2)
                        binding.progressRow2.setProgress(i);
                    else if (row2 == 0)
                        binding.progressRow2.setProgress(0);
                }

                if (rowStatus3) {
                    if (i <= row3)
                        binding.progressRow3.setProgress(i);
                    else if (row3 == 0)
                        binding.progressRow3.setProgress(0);
                }

                if (rowStatus4) {
                    if (i <= row4)
                        binding.progressRow4.setProgress(i);
                    else if (row4 == 0)
                        binding.progressRow4.setProgress(0);
                }

                if (rowStatus5) {
                    if (i <= row5)
                        binding.progressRow5.setProgress(i);
                    else if (row5 == 0)
                        binding.progressRow5.setProgress(0);
                }

                if (rowStatus6) {
                    if (i <= row6)
                        binding.progressRow6.setProgress(i);
                    else if (row6 == 0)
                        binding.progressRow6.setProgress(0);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
package com.zuescoder69.wordle;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.zuescoder69.wordle.databinding.FragmentResultBinding;
import com.zuescoder69.wordle.utils.CommonValues;

public class ResultFragment extends Fragment {
    private FragmentResultBinding binding;

    public ResultFragment() {
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
        binding = FragmentResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String winner = getArguments().getString("winnerName");
        binding.win.setVisibility(View.INVISIBLE);
        binding.lost.setVisibility(View.INVISIBLE);
        if (winner.equalsIgnoreCase("lost")) {
            binding.winnerName.setText("You both lost");
            binding.lost.setVisibility(View.VISIBLE);
            binding.win.setVisibility(View.INVISIBLE);
        } else {
            binding.winnerName.setText(winner);
            binding.lost.setVisibility(View.INVISIBLE);
            binding.win.setVisibility(View.VISIBLE);
        }

        Handler handler1 = new Handler();
        handler1.postDelayed(() -> {
            if (CommonValues.currentFragment.equalsIgnoreCase(CommonValues.gameFragment)) {
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Navigation.findNavController(getView()).navigate(R.id.action_resultFragment_to_menu_fragment);
            }
        }, 5000);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("Rooms").child(CommonValues.roomDate).child(CommonValues.roomId);
        databaseReference.removeValue();
    }
}
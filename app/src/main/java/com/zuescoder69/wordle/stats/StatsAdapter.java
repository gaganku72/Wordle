package com.zuescoder69.wordle.stats;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.zuescoder69.wordle.MenuFragment;

/**
 * Created by Gagan Kumar on 22/03/22.
 */
public class StatsAdapter extends FragmentStateAdapter {
    public StatsAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new DailyStatsFragment();
            case 2:
                return new ClassicStatsFragment();
        }
        return new MenuFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}

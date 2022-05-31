package com.zuescoder69.wordle.app;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.zuescoder69.wordle.R;

/**
 * Created by Gagan Kumar on 30/05/22.
 */
public abstract class MvpBaseFragment extends Fragment {

    public void showToastMsg(String msg) {
        try {
            if (getContext() != null && getActivity() != null) {
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.creating_toast, getActivity().findViewById(R.id.toast_layout));
                Toast creatingRoomToast;
                TextView toastContent = layout.findViewById(R.id.contentTV);
                creatingRoomToast = new Toast(getContext());
                creatingRoomToast.setGravity(Gravity.BOTTOM, 0, 0);
                creatingRoomToast.setDuration(Toast.LENGTH_SHORT);
                creatingRoomToast.setView(layout);
                toastContent.setText(msg);
                creatingRoomToast.show();
            }
        }catch (Exception e){
            Log.e("BaseFragment", e.getMessage());
        }
    }

    protected abstract void initializePresenter();

    public void showToastOnHeight(String msg) {
        try {
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast_on_height, getActivity().findViewById(R.id.toast_layout));
            Toast creatingRoomToast;
            TextView toastContent = layout.findViewById(R.id.contentTV);
            creatingRoomToast = new Toast(getContext());
            creatingRoomToast.setGravity(Gravity.BOTTOM, 0, 0);
            creatingRoomToast.setDuration(Toast.LENGTH_SHORT);
            creatingRoomToast.setView(layout);
            toastContent.setText(msg);
            creatingRoomToast.show();
        }catch (Exception e){
            Log.e("BaseFragment", e.getMessage());
        }
    }
}

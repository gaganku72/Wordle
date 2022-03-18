package com.zuescoder69.wordle;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

/**
 * Created by Gagan Kumar on 11/01/22.
 */
public abstract class BaseFragment extends Fragment {

    public void showToast(String msg, Context context, Activity activity) {
        try {
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.creating_toast, activity.findViewById(R.id.toast_layout));
            Toast creatingRoomToast;
            TextView toastContent = layout.findViewById(R.id.contentTV);
            creatingRoomToast = new Toast(context);
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

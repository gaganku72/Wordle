package com.zuescoder69.wordle;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.zuescoder69.wordle.utils.CommonValues;

public class MainActivity extends AppCompatActivity {
    private Boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        NavController navController = Navigation.findNavController(this, R.id.mainFragment);

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, pendingDynamicLinkData -> {
                    // Get deep link from result (may be null if no link is found)
                    Uri deepLink;
                    if (pendingDynamicLinkData != null) {
                        deepLink = pendingDynamicLinkData.getLink();
                        if (deepLink != null) {
                            String joinLink = deepLink.toString();
                            try {
                                String roomId = joinLink.substring(joinLink.lastIndexOf("-") + 1);
                                Bundle bundle = new Bundle();
                                bundle.putString("rooomId", roomId);
                                navController.navigate(R.id.roomFragment, bundle);
                            } catch (Exception e) {
                                Log.e("DEMON", "onSuccess: " + e.getMessage());
                            }
                        }
                    }


                    // Handle the deep link. For example, open the linked
                    // content, or apply promotional credit to the user's
                    // account.
                    // ...

                    // ...
                })
                .addOnFailureListener(this, e -> Log.w("DEMON", "getDynamicLink:onFailure", e));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("DEMON", "onPause()");
        CommonValues.currentFragment = "none";
    }

    public void showToast(String msg) {
        try {
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.creating_toast, this.findViewById(R.id.toast_layout));
            Toast creatingRoomToast;
            TextView toastContent = layout.findViewById(R.id.contentTV);
            creatingRoomToast = new Toast(this);
            creatingRoomToast.setGravity(Gravity.BOTTOM, 0, 0);
            creatingRoomToast.setDuration(Toast.LENGTH_SHORT);
            creatingRoomToast.setView(layout);
            toastContent.setText(msg);
            creatingRoomToast.show();
        } catch (Exception e) {
            Log.e("BaseFragment", e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        if (CommonValues.currentFragment.equals(CommonValues.menuFragment) || CommonValues.currentFragment.equals(CommonValues.gameFragment) || CommonValues.currentFragment.equals(CommonValues.lobbyFragment)) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            doubleBackToExitPressedOnce = true;
            showToast("Press BACK again to exit");

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        } else {
            super.onBackPressed();
        }
    }
}
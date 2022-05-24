package com.zuescoder69.wordle;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.zuescoder69.wordle.notification.MyBroadcastReceiver;
import com.zuescoder69.wordle.utils.CommonValues;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        NavController navController = Navigation.findNavController(this, R.id.mainFragment);

        FirebaseMessaging.getInstance().subscribeToTopic("all")
                .addOnCompleteListener(task -> {
                    String msg = "success";
                    if (!task.isSuccessful()) {
                        msg = "failed";
                    }
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                });

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("DEMON", "Refreshed token: " + refreshedToken);
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, pendingDynamicLinkData -> {
                    // Get deep link from result (may be null if no link is found)
                    Uri deepLink;
                    if (pendingDynamicLinkData != null) {
                        deepLink = pendingDynamicLinkData.getLink();
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

    @Override
    protected void onDestroy() {
        Log.e("Custom", "onDestroy: ");
        Intent broadcastIntent = new Intent(this, MyBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);
        super.onDestroy();
    }
}
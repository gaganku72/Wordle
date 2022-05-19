package com.zuescoder69.wordle;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.zuescoder69.wordle.utils.CommonValues;

public class MainActivity extends AppCompatActivity {

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
                        String joinLink=deepLink.toString();
                        try
                        {
                            String roomId=joinLink.substring(joinLink.lastIndexOf("-")+1);
                            Bundle bundle = new Bundle();
                            bundle.putString("rooomId", roomId);
                            navController.navigate(R.id.roomFragment, bundle);
                        }
                        catch (Exception e)
                        {
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
}
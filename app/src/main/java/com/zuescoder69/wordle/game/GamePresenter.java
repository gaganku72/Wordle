package com.zuescoder69.wordle.game;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.zuescoder69.wordle.R;
import com.zuescoder69.wordle.models.RowModel;
import com.zuescoder69.wordle.params.Params;
import com.zuescoder69.wordle.userData.DbHandler;
import com.zuescoder69.wordle.userData.SessionManager;
import com.zuescoder69.wordle.utils.CommonValues;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Created by Gagan Kumar on 30/05/22.
 */
public class GamePresenter implements Game.Presenter {
    private Game.View view;
    private final Context context;
    private final Activity activity;

    private final String TAG = "DEMON";
    private final String wordInDB = "Word";
    private final String classic = Params.CLASSIC_GAME_MODE;
    private final String daily = Params.DAILY_GAME_MODE;
    private final String multi = Params.MULTI_GAME_MODE;

    private String wordsCount;
    private String answer;
    private String currentWord;
    private String gameMode;
    private String userId;
    private String wordId;
    private String roomId;
    private String lobbyStatus = "";
    private String winnerId = "";
    private String winnerName = "";
    private String userStatus1 = "";
    private String userStatus2 = "";
    private String userId1 = "";
    private String userId2 = "";

    private DbHandler dbHandler;
    private SessionManager sessionManager;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceRealTime;
    private ValueEventListener valueEventListener;
    private InterstitialAd mInterstitialAd;

    private ArrayList<RowModel> rowsList;

    private boolean isEnterEnabled = true;
    private boolean gameLost = false;
    private boolean isAdFree = false;
    private ArrayList<Boolean> correctCol;
    private ArrayList<String> correctColLetters;

    public GamePresenter(Context context) {
        this.context = context;
        activity = (Activity) context;
    }

    @Override
    public void start(Bundle arguments) {
        if (arguments.containsKey("gameMode")) {
            gameMode = arguments.getString("gameMode");
        }
        varInit();
        CommonValues.currentFragment = CommonValues.gameFragment;
        isAdFree = CommonValues.isAdFree;
        userId = sessionManager.getStringKey(Params.KEY_USER_ID);
        setTheme();

        if (view != null) {
            view.setInitialUI();
        }
        getGameData();
    }

    private void getGameData() {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            getCurrentDate();
            if (!gameMode.equalsIgnoreCase(multi)) {
                getPreviousGameData();
            } else {
                getMultiplayerGameData();
            }
            if (view != null) {
                view.setupOnClicks();
            }
            getAppData();
        }, 300);
    }

    private void getAppData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("AppData");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {
                    };
                    Map<String, Object> map = dataSnapshot.getValue(genericTypeIndicator);
                    String toShowAd = (String) map.get("ShowAd");
                    String toShowBannerAd = (String) map.get("ShowBannerAd");
                    ArrayList<String> userId = new ArrayList<>();
                    for (int i = 1; i < 11; i++) {
                        if (map.containsKey("UserId" + i)) {
                            userId.add((String) map.get("UserId" + i));
                        }
                    }
                    if (toShowAd.equalsIgnoreCase("true")) {
                        String currentUserId = sessionManager.getStringKey(Params.KEY_USER_ID);
                        if (userId.contains(currentUserId)) {
                            CommonValues.isShowAd = false;
                            CommonValues.isAdFree = true;
                            CommonValues.isUserPremium = true;
                            if (view != null) {
                                view.setPremiumUI(gameLost);
                            }
                        } else {
                            CommonValues.isShowAd = true;
                            loadAd();
                            if (toShowBannerAd.equalsIgnoreCase("true")) {
                                loadBannerAd();
                            }
                            if (!gameMode.equalsIgnoreCase(multi)) {
                                loadRewardedAd();
                            }
                        }
                    } else {
                        CommonValues.isShowAd = false;
                        if (view != null) {
                            view.dismissProgress();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void loadRewardedAd() {
        if (!gameMode.equalsIgnoreCase(multi) && activity != null) {
            if (CommonValues.mRewardedAd == null && CommonValues.isShowAd) {
                AdRequest adRequest = new AdRequest.Builder().build();
                RewardedAd.load(activity, CommonValues.rewardAdId,
                        adRequest, new RewardedAdLoadCallback() {
                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                // Handle the error.
                                CommonValues.mRewardedAd = null;
                                if (view != null) {
                                    view.dismissProgress();
                                }
                            }

                            @Override
                            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                                CommonValues.mRewardedAd = rewardedAd;
                                setRewardedCallbacks();
                                if (view != null) {
                                    view.setPremiumUI(gameLost);
                                }
                            }
                        });
            } else if (CommonValues.mRewardedAd != null && CommonValues.isShowAd) {
                setRewardedCallbacks();
                if (view != null) {
                    view.setPremiumUI(gameLost);
                }
            }
        }
    }

    private void setRewardedCallbacks() {
        if (CommonValues.mRewardedAd != null) {
            CommonValues.mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    CommonValues.mRewardedAd = null;
                    loadRewardedAd();
                    if (view != null) {
                        view.setHelpBtnVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    CommonValues.mRewardedAd = null;
                    loadRewardedAd();
                    if (view != null) {
                        view.setHelpBtnVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                    CommonValues.mRewardedAd = null;
                    loadRewardedAd();
                    if (view != null) {
                        view.setHelpBtnVisibility(View.INVISIBLE);
                    }
                }
            });
        }
    }

    private void loadBannerAd() {
        if (context != null) {
            AdView adView = new AdView(context);
            adView.setAdUnitId(CommonValues.bannerAdId);
            if (view != null) {
                view.addBannerAdInAdView(adView);
            }
            AdRequest adRequest = new AdRequest.Builder().build();
            AdSize adSize = getAdSize();
            if (adSize != null) {
                adView.setAdSize(adSize);
            }
            adView.loadAd(adRequest);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    if (view != null) {
                        view.setBannerAdVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private AdSize getAdSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        if (activity != null && context != null) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);

            float widthPixels = outMetrics.widthPixels;
            float density = outMetrics.density;

            int adWidth = (int) (widthPixels / density);

            // Step 3 - Get adaptive ad size and return for setting on the ad view.
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
        }
        return null;
    }

    private void loadAd() {
        if (context != null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            InterstitialAd.load(context, CommonValues.interVideoId, adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            mInterstitialAd = interstitialAd;
                            if (view != null) {
                                view.dismissProgress();
                            }
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.i(TAG, loadAdError.getMessage());
                            mInterstitialAd = null;
                            if (view != null) {
                                view.dismissProgress();
                            }
                        }
                    });
        }
    }

    private void getMultiplayerGameData() {
        roomId = CommonValues.roomId;
        databaseReferenceRealTime = FirebaseDatabase.getInstance().getReference().child("Wordle").child("Rooms").child(CommonValues.roomDate).child(roomId);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && CommonValues.currentFragment.equalsIgnoreCase(CommonValues.gameFragment)) {
                    GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {
                    };
                    Map<String, Object> map = dataSnapshot.getValue(genericTypeIndicator);
                    answer = (String) map.get("Answer");
                    lobbyStatus = (String) map.get("Lobby Status");
                    wordId = (String) map.get("WordId");
                    winnerId = (String) map.get("WinnerId");
                    winnerName = (String) map.get("WinnerName");
                    userStatus1 = (String) map.get("UserStatus1");
                    userStatus2 = (String) map.get("UserStatus2");
                    userId1 = (String) map.get("UserId1");
                    userId2 = (String) map.get("UserId2");
                    checkLobbyStatus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseReferenceRealTime.addValueEventListener(valueEventListener);
    }

    private void checkLobbyStatus() {
        if (CommonValues.currentFragment.equalsIgnoreCase(CommonValues.gameFragment)) {
            if (lobbyStatus.equalsIgnoreCase("Result")) {
                databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("User Details").child(userId).child(classic).child(CommonValues.currentDate);
                Map setValues = new HashMap();
                setValues.put(wordInDB + wordId, "done");
                databaseReference.updateChildren(setValues);

                if (!TextUtils.isEmpty(winnerId)) {
                    if (userId.equalsIgnoreCase(winnerId)) {
                        if (view != null) {
                            view.setVictoryVisibility(View.VISIBLE);
                        }
                        sessionManager.addBooleanKey(Params.IS_GAME_WON, true);
                    } else {
                        if (view != null) {
                            view.setLoseVisibility(View.VISIBLE);
                        }
                    }
                }

                Handler handler1 = new Handler();
                handler1.postDelayed(() -> {
                    if (CommonValues.currentFragment.equalsIgnoreCase(CommonValues.gameFragment)) {
                        if (view != null) {
                            view.clearFlags();
                        }
                        Bundle bundle = new Bundle();
                        bundle.putString("winnerName", winnerName);
                        bundle.putString("answer", answer);
                        bundle.putString("roomId", roomId);
                        bundle.putString("winnerId", winnerId);
                        bundle.putString("userId1", userId1);
                        bundle.putString("userId2", userId2);
                        if (view != null) {
                            view.moveToFragment(R.id.action_gameFragment_to_resultFragment, bundle);
                        }
                    }
                }, 5000);
            } else {
                if (userStatus1.equalsIgnoreCase("no") && userStatus2.equalsIgnoreCase("no")) {
                    Handler handler1 = new Handler();
                    handler1.postDelayed(() -> {
                        if (CommonValues.currentFragment.equalsIgnoreCase(CommonValues.gameFragment)) {
                            if (view != null) {
                                view.clearFlags();
                            }
                            Bundle bundle = new Bundle();
                            bundle.putString("winnerName", "lost");
                            bundle.putString("answer", answer);
                            bundle.putString("roomId", roomId);
                            bundle.putString("winnerId", winnerId);
                            bundle.putString("userId1", userId1);
                            bundle.putString("userId2", userId2);
                            if (view != null) {
                                view.moveToFragment(R.id.action_gameFragment_to_resultFragment, bundle);
                            }
                        }
                    }, 5000);
                }
            }
        }
    }

    private void getPreviousGameData() {
        boolean isPreviousGame;
        if (gameMode.equalsIgnoreCase(classic)) {
            isPreviousGame = sessionManager.getBooleanKey(Params.KEY_IS_PREVIOUS_CLASSIC_GAME);
        } else {
            isPreviousGame = sessionManager.getBooleanKey(Params.KEY_IS_PREVIOUS_DAILY_GAME);
        }

        if (isPreviousGame) {
            int lastRow;
            if (gameMode.equalsIgnoreCase(classic)) {
                answer = sessionManager.getStringKey(Params.KEY_LAST_CLASSIC_ANSWER);
                String lastRowString = sessionManager.getStringKey(Params.KEY_LAST_CLASSIC_ROW);
                lastRow = Integer.parseInt(lastRowString);
            } else {
                answer = sessionManager.getStringKey(Params.KEY_LAST_DAILY_ANSWER);
                Log.d(TAG, "getPreviousGameData Answer: " + answer);
                String lastDate = sessionManager.getStringKey(Params.KEY_LAST_DAILY_DATE);
                if (!lastDate.equalsIgnoreCase(CommonValues.currentDate)) {
                    sessionManager.addBooleanKey(Params.KEY_IS_PREVIOUS_DAILY_GAME, false);
                    getAnswer();
                    dbHandler.dropTable(gameMode);
                    return;
                }
                String lastRowString = sessionManager.getStringKey(Params.KEY_LAST_DAILY_ROW);
                lastRow = Integer.parseInt(lastRowString);
            }
            Log.d(TAG, "getPreviousGameData: " + answer);
            for (int i = 1; i <= lastRow; i++) {
                Cursor cursor = dbHandler.readRowFromDB(i, gameMode);
                while (cursor.moveToNext()) {
                    rowsList.add(new RowModel(cursor.getString(0)
                            , cursor.getString(1), cursor.getString(2)
                            , cursor.getString(3), cursor.getString(4)
                            , cursor.getString(5)));
                }
                if (view != null) {
                    view.setDataOfLastGameInViews(rowsList);
                }
            }
        } else {
            getAnswer();
        }
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
                                    if (gameMode.equalsIgnoreCase(classic)) {
                                        databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("User Details").child(userId).child(classic).child(CommonValues.currentDate);
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
                                                                    } else {
                                                                        getAnswer();
                                                                    }
                                                                    answer = answer.toUpperCase();
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
                                    } else if (gameMode.equalsIgnoreCase(daily)) {
                                        databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("User Details").child(userId).child(daily).child(CommonValues.currentDate);
                                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (!dataSnapshot.exists()) {
                                                    databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("DailyWords");
                                                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.exists()) {
                                                                GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {
                                                                };
                                                                Map<String, Object> map = dataSnapshot.getValue(genericTypeIndicator);
                                                                answer = (String) map.get(CommonValues.currentDate);
                                                                answer = answer.toUpperCase();
                                                                Log.d("DEMON", "onDataChange: Answer-)" + answer);
//                                                                showToast(answer);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });
                                                } else {
                                                    if (view != null) {
                                                        view.comeTomorrow();
                                                    }
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
        CommonValues.currentDate = df.format(c);
        Log.d("DEMON", "getCurrentDate: date-)" + CommonValues.currentDate);
    }

    private void setTheme() {
        boolean isThemeBlack = sessionManager.getBooleanKey(CommonValues.THEME_DARK);
        if (view != null) {
            view.setTheme(isThemeBlack);
            boolean vibration = sessionManager.getBooleanKey(CommonValues.VIBRATION);
            view.setVibration(vibration);
        }
    }

    private void varInit() {
        sessionManager = new SessionManager(context);
        dbHandler = new DbHandler(context);
        rowsList = new ArrayList<>();
        initCorrectColList();
    }

    private void initCorrectColList() {
        correctCol = new ArrayList<>();
        correctColLetters = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            correctCol.add(i, false);
            correctColLetters.add(i, "");
        }
    }

    @Override
    public void onEnterClick() {
        if (mInterstitialAd != null && !isAdFree && activity != null) {
            mInterstitialAd.show(activity);
            loadAd();
            loadRewardedAd();
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    // Called when fullscreen content is dismissed.
                    submitWord();
                    Log.d("TAG", "The ad was dismissed.");
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    // Called when fullscreen content failed to show.
                    Log.d("TAG", "The ad failed to show.");
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    // Called when fullscreen content is shown.
                    // Make sure to set your reference to null so you don't
                    // show it a second time.
                    Log.d("TAG", "The ad was shown.");
                }
            });
        } else {
            submitWord();
        }
    }

    public void submitWord() {
        isEnterEnabled = false;
        ArrayList<String> list = new ArrayList<>();
        if (view != null) {
            currentWord = view.getWord();
        } else {
            return;
        }
        boolean isWordCorrect = sessionManager.isWordCorrect(currentWord);
        if (isWordCorrect) {
            for (int i = 0; i < currentWord.length(); i++) {
                char letter = currentWord.charAt(i);
                String s = letter + "";
                list.add(s);
            }
            wordleLogic(list, true);
            isEnterEnabled = true;
        } else {
            if (view != null) {
                view.noWordAnimation();
            }
        }
    }

    @Override
    public void setDataInDB(int row) {
        if (!gameMode.equalsIgnoreCase(multi)) {
            if (gameMode.equalsIgnoreCase(classic)) {
                sessionManager.addBooleanKey(Params.KEY_IS_PREVIOUS_CLASSIC_GAME, true);
            } else {
                sessionManager.addBooleanKey(Params.KEY_IS_PREVIOUS_DAILY_GAME, true);
            }
            String letter1, letter2, letter3, letter4, letter5;
            letter1 = currentWord.charAt(0) + "";
            letter2 = currentWord.charAt(1) + "";
            letter3 = currentWord.charAt(2) + "";
            letter4 = currentWord.charAt(3) + "";
            letter5 = currentWord.charAt(4) + "";

            if (row < 6) {
                if (gameMode.equalsIgnoreCase(classic)) {
                    sessionManager.addStringKey(Params.KEY_LAST_CLASSIC_ROW, row+"");
                } else {
                    sessionManager.addStringKey(Params.KEY_LAST_DAILY_ROW, row+"");
                }
                dbHandler.addRow(row, letter1, letter2, letter3, letter4, letter5, gameMode);
            } else if (row == 6) {
                if (gameMode.equalsIgnoreCase(classic)) {
                    sessionManager.addStringKey(Params.KEY_LAST_CLASSIC_ROW, "6");
                    sessionManager.addBooleanKey(Params.KEY_IS_PREVIOUS_CLASSIC_GAME, false);
                } else if (gameMode.equalsIgnoreCase(daily)) {
                    sessionManager.addStringKey(Params.KEY_LAST_DAILY_ROW, "6");
                    sessionManager.addBooleanKey(Params.KEY_IS_PREVIOUS_DAILY_GAME, false);
                }

                dbHandler.dropTable(gameMode);
            }
            if (gameMode.equalsIgnoreCase(classic)) {
                sessionManager.addStringKey(Params.KEY_LAST_CLASSIC_ANSWER, answer);
            } else if (gameMode.equalsIgnoreCase(daily)) {
                sessionManager.addStringKey(Params.KEY_LAST_DAILY_ANSWER, answer);
                sessionManager.addStringKey(Params.KEY_LAST_DAILY_DATE, CommonValues.currentDate);
            }

            sessionManager.addStringKey(Params.KEY_LAST_GAME_MODE, gameMode);
        }
    }

    @Override
    public void wordleLogic(ArrayList<String> lettersList, boolean isSubmitWord) {
        if (view != null) {
            view.setFlags();
        }
        int time = 1100;

        for (int i = lettersList.size() - 1; i >= 0; i--) {
            int count = 0;
            int nums = 0;
            ArrayList<Integer> indexes = new ArrayList<>();
            ArrayList<Integer> actualIndexes = new ArrayList<>();
            for (int j = lettersList.size() - 1; j >= 0; j--) {
                if (answer.length() > j) {
                    if (lettersList.get(i).equalsIgnoreCase(answer.charAt(j) + "")) {
                        count++;
                        actualIndexes.add(j);
                    }
                }
            }
            for (int j = lettersList.size() - 1; j >= 0; j--) {
                if (lettersList.get(i).equalsIgnoreCase(lettersList.get(j))) {
                    nums++;
                    indexes.add(j);
                }
            }

            if (nums == 1) {
                nums = 0;
            }
            if (nums > count) {
                int diff = nums - count;
                String letter = lettersList.get(i);
                if (containsAny(indexes, actualIndexes)) {
                    if (diff == 1) {
                        if (!actualIndexes.contains(i)) {
                            lettersList.set(i, "-");
                        }
                    } else {
                        for (int j = lettersList.size() - 1; j >= 0; j--) {
                            if (diff > 1) {
                                if (!actualIndexes.contains(j)) {
                                    if (letter.equalsIgnoreCase(lettersList.get(j))) {
                                        lettersList.set(j, "-");
                                        diff--;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (diff == 1) {
                        lettersList.set(i, "-");
                    } else {
                        for (int j = lettersList.size() - 1; j >= 0; j--) {
                            if (diff > 1) {
                                if (letter.equalsIgnoreCase(lettersList.get(j))) {
                                    lettersList.set(j, "-");
                                    diff--;
                                }
                            }
                        }
                    }
                }

            }
        }

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (answer.contains(lettersList.get(0))) {
                if (answer.length() > 0) {
                    String newLetter = answer.charAt(0) + "";

                    if (lettersList.get(0).equals(newLetter)) {
                        if (view != null) {
                            view.makeAnimation(1, isSubmitWord, CommonValues.CORRECT);
                        }
                        correctCol.set(0, true);
                        correctColLetters.set(0, newLetter);

                    } else {
                        if (view != null) {
                            view.makeAnimation(1, isSubmitWord, CommonValues.HAS);
                        }
                    }
                }
            } else {
                if (view != null) {
                    view.makeAnimation(1, isSubmitWord, CommonValues.WRONG);
                }
            }
        }, time);

        handler.postDelayed(() -> {
            if (answer.contains(lettersList.get(1))) {
                if (answer.length() > 1) {
                    String newLetter = answer.charAt(1) + "";

                    if (lettersList.get(1).equals(newLetter)) {
                        if (view != null) {
                            view.makeAnimation(2, isSubmitWord, CommonValues.CORRECT);
                        }
                        correctCol.set(1, true);
                        correctColLetters.set(1, newLetter);
                    } else {
                        if (view != null) {
                            view.makeAnimation(2, isSubmitWord, CommonValues.HAS);
                        }
                    }
                }
            } else {
                if (view != null) {
                    view.makeAnimation(2, isSubmitWord, CommonValues.WRONG);
                }
            }
        }, time * 2);

        handler.postDelayed(() -> {
            if (answer.contains(lettersList.get(2))) {
                if (answer.length() > 2) {
                    String newLetter = answer.charAt(2) + "";

                    if (lettersList.get(2).equals(newLetter)) {
                        if (view != null) {
                            view.makeAnimation(3, isSubmitWord, CommonValues.CORRECT);
                        }
                        correctCol.set(2, true);
                        correctColLetters.set(2, newLetter);
                    } else {
                        if (view != null) {
                            view.makeAnimation(3, isSubmitWord, CommonValues.HAS);
                        }
                    }
                }
            } else {
                if (view != null) {
                    view.makeAnimation(3, isSubmitWord, CommonValues.WRONG);
                }
            }
        }, time * 3);

        handler.postDelayed(() -> {
            if (answer.contains(lettersList.get(3))) {
                if (answer.length() > 3) {
                    String newLetter = answer.charAt(3) + "";

                    if (lettersList.get(3).equals(newLetter)) {
                        if (view != null) {
                            view.makeAnimation(4, isSubmitWord, CommonValues.CORRECT);
                        }
                        correctCol.set(3, true);
                        correctColLetters.set(3, newLetter);
                    } else {
                        if (view != null) {
                            view.makeAnimation(4, isSubmitWord, CommonValues.HAS);
                        }
                    }
                }
            } else {
                if (view != null) {
                    view.makeAnimation(4, isSubmitWord, CommonValues.WRONG);
                }
            }
        }, time * 4);

        handler.postDelayed(() -> {
            if (answer.contains(lettersList.get(4))) {
                if (answer.length() > 4) {
                    String newLetter = answer.charAt(4) + "";

                    if (lettersList.get(4).equals(newLetter)) {
                        if (view != null) {
                            view.makeAnimation(5, isSubmitWord, CommonValues.CORRECT);
                        }
                        correctCol.set(4, true);
                        correctColLetters.set(4, newLetter);
                    } else {
                        if (view != null) {
                            view.makeAnimation(5, isSubmitWord, CommonValues.HAS);
                        }
                    }
                }
            } else {
                if (view != null) {
                    view.makeAnimation(5, isSubmitWord, CommonValues.WRONG);
                }
            }
            if (view != null) {
                view.setButtonsBackground(lettersList, answer, correctColLetters);
            }
        }, time * 5);
    }

    private <T> boolean containsAny(ArrayList<T> l1, ArrayList<T> l2) {
        for (T elem : l1) {
            if (l2.contains(elem)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void lastRow(String rowLocal) {
        if (currentWord.equalsIgnoreCase(answer)) {
            if (!gameMode.equalsIgnoreCase(multi)) {
                dbHandler.dropTable(gameMode);
                sessionManager.clearGameModeSession(gameMode);
                if (view != null) {
                    view.setVictoryVisibility(View.VISIBLE);
                }
                sessionManager.addBooleanKey(Params.IS_GAME_WON, true);
            }
            if (view != null) {
                view.setFlags();
                view.setGameFragmentEnabled(false);
            }
            if (gameMode.equalsIgnoreCase(daily)) {
                databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("User Details").child(userId).child(daily).child(CommonValues.currentDate);
                Map setValues = new HashMap();
                setValues.put(CommonValues.currentDate, "done");
                databaseReference.updateChildren(setValues);

                Handler handler1 = new Handler();
                handler1.postDelayed(() -> {
                    if (CommonValues.currentFragment.equalsIgnoreCase(CommonValues.gameFragment)) {
                        if (view != null) {
                            view.clearFlags();
                            view.moveToFragment(R.id.action_gameFragment_to_menu_fragment, null);
                        }
                    }
                }, 5000);

            } else if (gameMode.equalsIgnoreCase(classic)) {
                databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("User Details").child(userId).child(classic).child(CommonValues.currentDate);
                Map setValues = new HashMap();
                setValues.put(wordInDB + wordId, "done");
                databaseReference.updateChildren(setValues);

                Handler handler1 = new Handler();
                handler1.postDelayed(() -> {
                    if (CommonValues.currentFragment.equalsIgnoreCase(CommonValues.gameFragment)) {
                        if (view != null) {
                            view.clearFlags();
                            Bundle bundle = new Bundle();
                            bundle.putString("gameMode", classic);
                            view.moveToFragment(R.id.action_gameFragment_self, bundle);
                        }
                    }
                }, 5000);
            } else if (gameMode.equalsIgnoreCase(multi)) {
                databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("User Details").child(userId).child(classic).child(CommonValues.currentDate);
                Map setValues0 = new HashMap();
                setValues0.put(wordInDB + wordId, "done");
                databaseReference.updateChildren(setValues0);

                databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("Rooms").child(CommonValues.roomDate).child(roomId);
                Map setValues = new HashMap();
                setValues.put("Lobby Status", "Result");
                setValues.put("WinnerId", userId);
                setValues.put("WinnerName", sessionManager.getStringKey(Params.KEY_USER_NAME));
                databaseReference.updateChildren(setValues);
            }
        }
    }

    @Override
    public void showHint() {
        if (CommonValues.isUserPremium) {
            for (int i = 0; i < correctCol.size(); i++) {
                if (!correctCol.get(i)) {
                    if (view != null) {
                        view.setHintTvVisibility(View.VISIBLE);
                    }
                    if (answer.length() > i) {
                        String hint = answer.charAt(i) + "";
                        if (view != null) {
                            view.setHintTvText("Word has letter - " + hint.toUpperCase());
                        }
                    }
                    break;
                }
            }
        } else if (CommonValues.mRewardedAd != null && CommonValues.isShowAd && activity != null) {
            CommonValues.mRewardedAd.show(activity, rewardItem -> {
                CommonValues.mRewardedAd = null;
                loadRewardedAd();
                for (int i = 0; i < correctCol.size(); i++) {
                    if (!correctCol.get(i)) {
                        if (view != null) {
                            view.setHintTvVisibility(View.VISIBLE);
                        }
                        if (answer.length() > i) {
                            String hint = answer.charAt(i) + "";
                            if (view != null) {
                                view.setHintTvText("Word has letter - " + hint.toUpperCase());
                            }
                        }
                        break;
                    }
                }
            });
        }
    }

    @Override
    public void setMuliplayerLost() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("Rooms").child(CommonValues.roomDate).child(roomId);
        Map setValues = new HashMap();
        if (userId.equalsIgnoreCase(userId1)) {
            setValues.put("UserStatus1", "no");
        } else if (userId.equalsIgnoreCase(userId2)) {
            setValues.put("UserStatus2", "no");
        }
        databaseReference.updateChildren(setValues);
    }

    @Override
    public void setAnswerInUserFirebase() {
        if (gameMode.equalsIgnoreCase(classic)) {
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("User Details").child(userId).child(classic).child(CommonValues.currentDate);
            Map setValues = new HashMap();
            setValues.put(wordInDB + wordId, "done");
            databaseReference.updateChildren(setValues);
        }
    }

    @Override
    public void restartGame() {
        if (CommonValues.isAdFree) {
            if (view != null) {
                view.removeAllCharFromViews();
            }
        } else if (CommonValues.mRewardedAd != null && CommonValues.isShowAd && activity != null) {
            CommonValues.mRewardedAd.show(activity, rewardItem -> {
                CommonValues.mRewardedAd = null;
                loadRewardedAd();
                if (view != null) {
                    view.removeAllCharFromViews();
                }
            });
        }
    }

    @Override
    public void setGameLost(boolean b) {
        gameLost = b;
    }

    @Override
    public String getAnswerString() {
        return answer;
    }

    @Override
    public String getGameMode() {
        return gameMode;
    }

    @Override
    public void detachListener() {
        if (valueEventListener != null) {
            databaseReferenceRealTime.removeEventListener(valueEventListener);
        }
    }

    @Override
    public boolean isEnterEnabled() {
        return isEnterEnabled;
    }

    @Override
    public void setIsEnterEnabled(boolean b) {
        isEnterEnabled = b;
    }

    @Override
    public void takeView(Game.View view) {
        this.view = view;
    }

    @Override
    public void dropView() {
        view = null;
    }
}

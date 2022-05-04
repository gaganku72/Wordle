package com.zuescoder69.wordle.utils;

import com.google.android.gms.ads.rewarded.RewardedAd;

import java.util.ArrayList;

/**
 * Created by Gagan Kumar on 11/01/22.
 */
public class CommonValues {
    public static String versionCode = "3";
    public static String versionCodeFirebase = "3";
    public static String rewardAdId = "ca-app-pub-3940256099942544/5224354917";
    public static String interNormalId = "ca-app-pub-2645963896743153/9833622836";
    public static String interVideoId = "ca-app-pub-3940256099942544/1033173712";
    public static String currentFragment = "";
    public static boolean isShowAd = false;
    public static boolean isAdFree = false;
    public static String comeTomorrowMsg = "Game played for today";
    public static String roomId = "";
    public static String roomDate = "";
    public static ArrayList<String> roomIds = new ArrayList<>();
    public static RewardedAd mRewardedAd;

    public static final String splashScreenFragment = "splashScreenFragment";
    public static final String menuFragment = "menuFragment";
    public static final String loginFragment = "loginFragment";
    public static final String gameFragment = "gameFragment";
    public static final String lobbyFragment = "lobbyFragment";
    public static final String roomFragment = "roomFragment";
    public static final String resultFragment = "resultFragment";
    public static final String THEME = "theme";
    public static final String THEME_DARK = "themeDark";
    public static final String THEME_LIGHT = "themeLight";
    public static final String VIBRATION = "vibration";
}

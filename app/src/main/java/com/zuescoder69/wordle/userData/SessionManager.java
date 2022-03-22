package com.zuescoder69.wordle.userData;

import android.content.Context;
import android.content.SharedPreferences;

import com.zuescoder69.wordle.params.Params;

import java.util.HashMap;

/**
 * Created by Gagan Kumar on 12/01/22.
 */
public class SessionManager {
    static SharedPreferences userSession;
    SharedPreferences.Editor editor;
    android.content.Context Context;

    public SessionManager(Context context) {
        Context = context;
        userSession = Context.getSharedPreferences("userSession", Context.MODE_PRIVATE);
        editor = userSession.edit();
    }

    public void createLoginSession(String email, String user_id, String userName, String firstName) {
        editor.putString(Params.KEY_EMAIL, email);
        editor.putString(Params.KEY_USER_ID, user_id);
        editor.putString(Params.KEY_USER_NAME, userName);
        editor.putString(Params.KEY_FIRST_NAME, firstName);

        editor.commit();
    }

    public HashMap<String, String> getSessionDetails() {
        HashMap<String, String> userData = new HashMap<String, String>();

        userData.put(Params.KEY_EMAIL, userSession.getString(Params.KEY_EMAIL, null));
        userData.put(Params.KEY_USER_ID, userSession.getString(Params.KEY_USER_ID, null));
        userData.put(Params.KEY_USER_NAME, userSession.getString(Params.KEY_USER_NAME, null));

        return userData;
    }

    public void addStringKey(String key, String data) {
        editor.putString(key, data);
        editor.commit();
    }

    public void addBooleanKey(String key, boolean data) {
        editor.putBoolean(key, data);
        editor.commit();
    }

    public String getStringKey(String key) {
        String result = userSession.getString(key, "");
        return result;
    }

    public boolean getBooleanKey(String key) {
        boolean result = userSession.getBoolean(key, false);
        return result;
    }

    public void clearSession() {
        editor.clear();
        editor.commit();
    }

    public void clearGameModeSession(String gameMode) {
        if (gameMode.equalsIgnoreCase("classic")) {
            addStringKey(Params.KEY_LAST_CLASSIC_ANSWER, "");
            addBooleanKey(Params.KEY_IS_PREVIOUS_CLASSIC_GAME, false);
            addStringKey(Params.KEY_LAST_CLASSIC_ROW, "");
        } else {
            addStringKey(Params.KEY_LAST_DAILY_ANSWER, "");
            addBooleanKey(Params.KEY_IS_PREVIOUS_DAILY_GAME, false);
            addStringKey(Params.KEY_LAST_DAILY_ROW, "");
        }
    }

    public static String getEmail() {
        return (userSession.getString(Params.KEY_EMAIL, ""));
    }
}

package com.zuescoder69.wordle.params;

/**
 * Created by Gagan Kumar on 12/01/22.
 */
public class Params {
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "wordle_db";
    public static final String PREVIOUS_DAILY_GAME_TABLE = "daily";
    public static final String PREVIOUS_CLASSIC_GAME_TABLE = "classic";

    public static final String KEY_EMAIL = "email";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_FIRST_NAME = "first_name";

    //Keys of previous_game table;
    public static final String KEY_ROW = "row";
    public static final String KEY_LETTER1 = "letter1";
    public static final String KEY_LETTER2 = "letter2";
    public static final String KEY_LETTER3 = "letter3";
    public static final String KEY_LETTER4 = "letter4";
    public static final String KEY_LETTER5 = "letter5";

    public static final String KEY_IS_PREVIOUS_CLASSIC_GAME = "isPreviousClassicGame";
    public static final String KEY_IS_PREVIOUS_DAILY_GAME = "isPreviousDailyGame";
    public static final String KEY_LAST_CLASSIC_ROW = "classicLastRow";
    public static final String KEY_LAST_DAILY_ROW = "dailyLastRow";
    public static final String KEY_LAST_CLASSIC_ANSWER = "classicLastAnswer";
    public static final String KEY_LAST_DAILY_ANSWER = "dailyLastAnswer";
    public static final String KEY_LAST_GAME_MODE = "lastGameMode";

    public static final String CLASSIC_GAME_MODE = "classic";
    public static final String DAILY_GAME_MODE = "classic";
}

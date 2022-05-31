package com.zuescoder69.wordle.game;

import android.os.Bundle;

import com.google.android.gms.ads.AdView;
import com.zuescoder69.wordle.app.BasePresenter;
import com.zuescoder69.wordle.app.BaseView;
import com.zuescoder69.wordle.models.RowModel;

import java.util.ArrayList;

/**
 * Created by Gagan Kumar on 30/05/22.
 */
public class Game {
    public interface View extends BaseView<Presenter> {

        void setTheme(boolean isThemeBlack);

        void setInitialUI();

        void comeTomorrow();

        void setVictoryVisibility(int visibility);

        void setLoseVisibility(int visibility);

        void clearFlags();

        void moveToFragment(int actionName, Bundle bundle);

        void setPremiumUI(boolean gameLost);

        void dismissProgress();

        void addBannerAdInAdView(AdView adView);

        void setBannerAdVisibility(int visibility);

        void setHelpBtnVisibility(int visibility);

        void setupOnClicks();

        void setVibration(boolean vibration);

        String getWord();

        void setFlags();

        void makeAnimation(int index, boolean currentWord, String status);

        void setGameFragmentEnabled(boolean enabled);

        void setHintTvVisibility(int visibility);

        void setHintTvText(String s);

        void noWordAnimation();

        void setButtonsBackground(ArrayList<String> list, String answer, ArrayList<String> correctColLetters);

        void setDataOfLastGameInViews(ArrayList<RowModel> rowsList);

        void removeAllCharFromViews();
    }

    public interface Presenter extends BasePresenter<View> {

        void start(Bundle arguments);

        void onEnterClick();

        void setDataInDB(int row);

        void lastRow(String rowLocal);

        void showHint();

        void wordleLogic(ArrayList<String> lettersList, boolean isSubmitWord);

        String getGameMode();

        void setMuliplayerLost();

        void setGameLost(boolean b);
        
        String getAnswerString();

        void loadRewardedAd();

        void setAnswerInUserFirebase();

        void restartGame();

        void detachListener();

        boolean isEnterEnabled();

        void setIsEnterEnabled(boolean b);
    }
}

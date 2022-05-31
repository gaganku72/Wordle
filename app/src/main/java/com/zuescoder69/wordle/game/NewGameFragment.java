package com.zuescoder69.wordle.game;

import static android.content.Context.VIBRATOR_SERVICE;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.google.android.gms.ads.AdView;
import com.zuescoder69.wordle.R;
import com.zuescoder69.wordle.app.MvpBaseFragment;
import com.zuescoder69.wordle.databinding.FragmentNewGameBinding;
import com.zuescoder69.wordle.models.RowModel;
import com.zuescoder69.wordle.params.Params;
import com.zuescoder69.wordle.utils.CommonValues;

import java.util.ArrayList;

public class NewGameFragment extends MvpBaseFragment implements Game.View {

    private FragmentNewGameBinding binding;
    private GamePresenter presenter;
    private Vibrator vibrator;
    private Animation scaleUp, scaleDown;
    private AnimatorSet rotate;
    private long vibrationTime = 80;
    private int row = 1;
    private int current = 1;
    private final String classic = Params.CLASSIC_GAME_MODE;
    private final String multi = Params.MULTI_GAME_MODE;

    public NewGameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new GamePresenter(getContext());
        scaleUp = AnimationUtils.loadAnimation(getContext(), R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(getContext(), R.anim.scale_down);
        rotate = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.rotate);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentNewGameBinding.inflate(inflater, container, false);
        initializePresenter();
        showToastMsg("LoL");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            presenter.start(getArguments());
        }
        if (getContext() != null) {
            vibrator = (Vibrator) getContext().getSystemService(VIBRATOR_SERVICE);
        }
    }

    @Override
    public void setTheme(boolean isThemeBlack) {
        if (isThemeBlack && getContext() != null) {
            binding.row11.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row12.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row13.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row14.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row15.setTextColor(getContext().getColor(R.color.no_bg_txt));

            binding.row21.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row22.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row23.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row24.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row25.setTextColor(getContext().getColor(R.color.no_bg_txt));

            binding.row31.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row32.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row33.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row34.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row35.setTextColor(getContext().getColor(R.color.no_bg_txt));

            binding.row41.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row42.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row43.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row44.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row45.setTextColor(getContext().getColor(R.color.no_bg_txt));

            binding.row51.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row52.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row53.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row54.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row55.setTextColor(getContext().getColor(R.color.no_bg_txt));

            binding.row61.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row62.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row63.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row64.setTextColor(getContext().getColor(R.color.no_bg_txt));
            binding.row65.setTextColor(getContext().getColor(R.color.no_bg_txt));
        }
    }

    @Override
    public void setInitialUI() {
        binding.victory.setVisibility(View.GONE);
        binding.lose.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);
        binding.gameFragment.setVisibility(View.GONE);
        binding.helpBtn.setVisibility(View.INVISIBLE);
        binding.restartGameBtn.setVisibility(View.GONE);
        binding.nextGameBtn.setVisibility(View.GONE);
        binding.seeAnswerBtn.setVisibility(View.GONE);
    }

    @Override
    public void setVictoryVisibility(int visibility) {
        binding.victory.setVisibility(visibility);
    }

    @Override
    public void setLoseVisibility(int visibility) {
        binding.lose.setVisibility(visibility);
    }

    @Override
    public void clearFlags() {
        if (getActivity() != null) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    @Override
    public void moveToFragment(int actionName, Bundle bundle) {
        if (getView() != null) {
            if (bundle != null)
                Navigation.findNavController(getView()).navigate(actionName, bundle);
            else
                Navigation.findNavController(getView()).navigate(actionName);
        }
    }

    @Override
    public void comeTomorrow() {
        showToastMessage(CommonValues.comeTomorrowMsg);
        if (getView() != null) {
            Navigation.findNavController(getView()).navigate(R.id.action_gameFragment_to_menu_fragment);
        }
    }

    @Override
    public void setPremiumUI(boolean gameLost) {
        binding.progress.setVisibility(View.GONE);
        binding.gameFragment.setVisibility(View.VISIBLE);
        if (!gameLost) {
            binding.helpBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void dismissProgress() {
        binding.progress.setVisibility(View.GONE);
        binding.gameFragment.setVisibility(View.VISIBLE);
    }

    @Override
    public void addBannerAdInAdView(AdView adView) {
        binding.frameLayout.addView(adView);
    }

    @Override
    public void setBannerAdVisibility(int visibility) {
        binding.frameLayout.setVisibility(visibility);
    }

    @Override
    public void showToastMessage(String message) {
        if (!TextUtils.isEmpty(message)) {
            showToastMsg(message);
        }
    }

    @Override
    public void setHelpBtnVisibility(int visibility) {
        binding.helpBtn.setVisibility(visibility);
    }

    @Override
    public void setVibration(boolean vibration) {
        if (vibration) {
            vibrationTime = 80;
        } else {
            vibrationTime = 0;
        }
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void setupOnClicks() {
        binding.btnQ.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnQ.startAnimation(scaleUp);
                setCharInView("Q");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnQ.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnW.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnW.startAnimation(scaleUp);
                setCharInView("W");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnW.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnE.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnE.startAnimation(scaleUp);
                setCharInView("E");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnE.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnR.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnR.startAnimation(scaleUp);
                setCharInView("R");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnR.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnT.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnT.startAnimation(scaleUp);
                setCharInView("T");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnT.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnY.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnY.startAnimation(scaleUp);
                setCharInView("Y");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnY.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnU.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnU.startAnimation(scaleUp);
                setCharInView("U");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnU.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnI.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnI.startAnimation(scaleUp);
                setCharInView("I");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnI.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnO.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnO.startAnimation(scaleUp);
                setCharInView("O");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnO.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnP.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnP.startAnimation(scaleUp);
                setCharInView("P");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnP.startAnimation(scaleDown);
            }
            return true;
        });


        binding.btnA.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnA.startAnimation(scaleUp);
                setCharInView("A");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnA.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnS.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnS.startAnimation(scaleUp);
                setCharInView("S");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnS.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnD.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnD.startAnimation(scaleUp);
                setCharInView("D");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnD.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnF.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnF.startAnimation(scaleUp);
                setCharInView("F");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnF.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnG.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnG.startAnimation(scaleUp);
                setCharInView("G");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnG.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnH.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnH.startAnimation(scaleUp);
                setCharInView("H");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnH.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnJ.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnJ.startAnimation(scaleUp);
                setCharInView("J");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnJ.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnK.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnK.startAnimation(scaleUp);
                setCharInView("K");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnK.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnL.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnL.startAnimation(scaleUp);
                setCharInView("L");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnL.startAnimation(scaleDown);
            }
            return true;
        });


        binding.btnZ.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnZ.startAnimation(scaleUp);
                setCharInView("Z");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnZ.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnX.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnX.startAnimation(scaleUp);
                setCharInView("X");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnX.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnC.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnC.startAnimation(scaleUp);
                setCharInView("C");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnC.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnV.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnV.startAnimation(scaleUp);
                setCharInView("V");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnV.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnB.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnB.startAnimation(scaleUp);
                setCharInView("B");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnB.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnN.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnN.startAnimation(scaleUp);
                setCharInView("N");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnN.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnM.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnM.startAnimation(scaleUp);
                setCharInView("M");
                vibrator.vibrate(vibrationTime);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnM.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnEnter.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.btnEnter.startAnimation(scaleUp);
                vibrator.vibrate(vibrationTime);
                if (presenter.isEnterEnabled()) {
                    if (current == 6) {
                        presenter.onEnterClick();
                    }
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnEnter.startAnimation(scaleDown);
            }
            return true;
        });

        binding.btnCancel.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                vibrator.vibrate(vibrationTime);
                binding.btnCancel.startAnimation(scaleUp);
                removeCharInView();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.btnCancel.startAnimation(scaleDown);
            }
            return true;
        });

        binding.helpBtn.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                binding.helpBtn.startAnimation(scaleUp);
                presenter.showHint();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.helpBtn.startAnimation(scaleDown);
            }
            return true;
        });
    }

    @Override
    public void setFlags() {
        if (getActivity() != null) {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    @Override
    public void setHintTvVisibility(int visibility) {
        binding.hintTv.setVisibility(View.VISIBLE);
    }

    @Override
    public void setHintTvText(String s) {
        binding.hintTv.setText(s);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void showLostGameViews() {
        if (CommonValues.isShowAd || CommonValues.isAdFree) {
            String gameMode = presenter.getGameMode();
            presenter.setGameLost(true);
            if (CommonValues.mRewardedAd != null || CommonValues.isAdFree) {
                binding.helpBtn.setVisibility(View.INVISIBLE);
                binding.nextGameBtn.setVisibility(View.VISIBLE);
                binding.seeAnswerBtn.setVisibility(View.VISIBLE);
                binding.restartGameBtn.setVisibility(View.VISIBLE);

                binding.nextGameBtn.setOnTouchListener((view, motionEvent) -> {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        binding.nextGameBtn.startAnimation(scaleUp);
                        if (gameMode.equalsIgnoreCase(classic)) {
                            Bundle bundle = new Bundle();
                            bundle.putString("gameMode", classic);
                            if (getView() != null) {
                                Navigation.findNavController(getView()).navigate(R.id.action_gameFragment_self, bundle);
                            }
                        } else {
                            if (getView() != null) {
                                Navigation.findNavController(getView()).navigate(R.id.action_gameFragment_to_menu_fragment);
                            }
                        }
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        binding.nextGameBtn.startAnimation(scaleDown);
                    }
                    return true;
                });

                binding.seeAnswerBtn.setOnTouchListener((view, motionEvent) -> {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        binding.seeAnswerBtn.startAnimation(scaleUp);
                        seeAnswer();
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        binding.seeAnswerBtn.startAnimation(scaleDown);
                    }
                    return true;
                });

                binding.restartGameBtn.setOnTouchListener((view, motionEvent) -> {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        binding.restartGameBtn.startAnimation(scaleUp);
                        presenter.restartGame();
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        binding.restartGameBtn.startAnimation(scaleDown);
                    }
                    return true;
                });
            }
        } else {
            Handler handler1 = new Handler();
            handler1.postDelayed(() -> {
                String gameMode = presenter.getGameMode();
                if (gameMode.equalsIgnoreCase(classic)) {
                    Bundle bundle = new Bundle();
                    bundle.putString("gameMode", classic);
                    if (getView() != null) {
                        Navigation.findNavController(getView()).navigate(R.id.action_gameFragment_self, bundle);
                    }
                } else {
                    if (getView() != null) {
                        Navigation.findNavController(getView()).navigate(R.id.action_gameFragment_to_menu_fragment);
                    }
                }
            }, 5000);
        }
    }

    private void seeAnswer() {
        if (CommonValues.isAdFree) {
            binding.hintTv.setVisibility(View.VISIBLE);
            binding.hintTv.setText("Wordly is - " + presenter.getAnswerString());
            binding.restartGameBtn.setVisibility(View.GONE);
            binding.seeAnswerBtn.setVisibility(View.GONE);
        } else if (CommonValues.mRewardedAd != null && CommonValues.isShowAd && getActivity() != null) {
            CommonValues.mRewardedAd.show(getActivity(), rewardItem -> {
                CommonValues.mRewardedAd = null;
                presenter.loadRewardedAd();
                binding.hintTv.setVisibility(View.VISIBLE);
                binding.hintTv.setText("Wordly is - " + presenter.getAnswerString());
                binding.seeAnswerBtn.setVisibility(View.GONE);
                binding.restartGameBtn.setVisibility(View.GONE);
            });
        }

        presenter.setAnswerInUserFirebase();
    }

    @Override
    public void removeAllCharFromViews() {
        current = 1;
        row = 1;

        binding.row11.setText("");
        binding.row12.setText("");
        binding.row13.setText("");
        binding.row14.setText("");
        binding.row15.setText("");

        binding.row21.setText("");
        binding.row22.setText("");
        binding.row23.setText("");
        binding.row24.setText("");
        binding.row25.setText("");

        binding.row31.setText("");
        binding.row32.setText("");
        binding.row33.setText("");
        binding.row34.setText("");
        binding.row35.setText("");

        binding.row41.setText("");
        binding.row42.setText("");
        binding.row43.setText("");
        binding.row44.setText("");
        binding.row45.setText("");

        binding.row51.setText("");
        binding.row52.setText("");
        binding.row53.setText("");
        binding.row54.setText("");
        binding.row55.setText("");

        binding.row61.setText("");
        binding.row62.setText("");
        binding.row63.setText("");
        binding.row64.setText("");
        binding.row65.setText("");

        binding.row11.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row12.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row13.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row14.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row15.setBackgroundResource(R.drawable.alphabets_bg);

        binding.row21.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row22.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row23.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row24.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row25.setBackgroundResource(R.drawable.alphabets_bg);

        binding.row31.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row32.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row33.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row34.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row35.setBackgroundResource(R.drawable.alphabets_bg);

        binding.row41.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row42.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row43.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row44.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row45.setBackgroundResource(R.drawable.alphabets_bg);

        binding.row51.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row52.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row53.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row54.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row55.setBackgroundResource(R.drawable.alphabets_bg);

        binding.row61.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row62.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row63.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row64.setBackgroundResource(R.drawable.alphabets_bg);
        binding.row65.setBackgroundResource(R.drawable.alphabets_bg);

        presenter.setGameLost(false);
        binding.lose.setVisibility(View.GONE);
        binding.restartGameBtn.setVisibility(View.GONE);
        binding.nextGameBtn.setVisibility(View.GONE);
        binding.seeAnswerBtn.setVisibility(View.GONE);
    }

    private void makeRotateAnim(TextView textView, String status, int index) {
        rotate.setTarget(textView);
        rotate.start();

        if (status.equalsIgnoreCase(CommonValues.CORRECT)) {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (index == 5) {
                    presenter.lastRow("row" + row);
                    clearFlags();
                }
                textView.setBackgroundResource(R.drawable.alphabets_correct_bg);
                setBoxColor(textView);
            }, 450);
        } else if (status.equalsIgnoreCase(CommonValues.HAS)) {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (index == 5 && row == 6) {
                    String gameMode = presenter.getGameMode();
                    if (gameMode.equalsIgnoreCase(multi)) {
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        binding.lose.setVisibility(View.VISIBLE);
                        textView.setBackgroundResource(R.drawable.alphabets_has_bg);
                        setBoxColor(textView);
                        Handler handler2 = new Handler();
                        handler2.postDelayed(() -> presenter.setMuliplayerLost(), 500);
                    } else {
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        binding.lose.setVisibility(View.VISIBLE);
                        textView.setBackgroundResource(R.drawable.alphabets_has_bg);
                        setBoxColor(textView);
                        showLostGameViews();
                    }
                } else {
                    textView.setBackgroundResource(R.drawable.alphabets_has_bg);
                    setBoxColor(textView);
                }
            }, 450);
        } else if (status.equalsIgnoreCase(CommonValues.WRONG)) {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (index == 5 && row == 6) {
                    String gameMode = presenter.getGameMode();
                    if (gameMode.equalsIgnoreCase(multi)) {
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        binding.lose.setVisibility(View.VISIBLE);
                        textView.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                        setBoxColor(textView);
                        Handler handler2 = new Handler();
                        handler2.postDelayed(() -> presenter.setMuliplayerLost(), 500);
                    } else {
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        binding.lose.setVisibility(View.VISIBLE);
                        textView.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                        setBoxColor(textView);
                        showLostGameViews();
                    }
                } else {
                    textView.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    setBoxColor(textView);
                }
            }, 450);
        }

        if (index == 5) {
            clearFlags();
        }
    }

    private void setBoxColor(TextView textView) {
        textView.setTextColor(getContext().getColor(R.color.white));
    }

    @Override
    public void setGameFragmentEnabled(boolean enabled) {
        binding.gameFragment.setEnabled(enabled);
    }

    @Override
    public void makeAnimation(int index, boolean isSubmitWord, String status) {
        if (row == 1) {
            if (index == 1) {
                makeRotateAnim(binding.row11, status, index);
            } else if (index == 2) {
                makeRotateAnim(binding.row12, status, index);
            } else if (index == 3) {
                makeRotateAnim(binding.row13, status, index);
            } else if (index == 4) {
                makeRotateAnim(binding.row14, status, index);
            } else if (index == 5) {
                makeRotateAnim(binding.row15, status, index);
                if (isSubmitWord) {
                    presenter.setDataInDB(1);
                }
            }
        } else if (row == 2) {
            if (index == 1) {
                makeRotateAnim(binding.row21, status, index);
            } else if (index == 2) {
                makeRotateAnim(binding.row22, status, index);
            } else if (index == 3) {
                makeRotateAnim(binding.row23, status, index);
            } else if (index == 4) {
                makeRotateAnim(binding.row24, status, index);
            } else if (index == 5) {
                makeRotateAnim(binding.row25, status, index);
                if (isSubmitWord) {
                    presenter.setDataInDB(2);
                }
            }
        } else if (row == 3) {
            if (index == 1) {
                makeRotateAnim(binding.row31, status, index);
            } else if (index == 2) {
                makeRotateAnim(binding.row32, status, index);
            } else if (index == 3) {
                makeRotateAnim(binding.row33, status, index);
            } else if (index == 4) {
                makeRotateAnim(binding.row34, status, index);
            } else if (index == 5) {
                makeRotateAnim(binding.row35, status, index);
                if (isSubmitWord) {
                    presenter.setDataInDB(3);
                }
            }
        } else if (row == 4) {
            if (index == 1) {
                makeRotateAnim(binding.row41, status, index);
            } else if (index == 2) {
                makeRotateAnim(binding.row42, status, index);
            } else if (index == 3) {
                makeRotateAnim(binding.row43, status, index);
            } else if (index == 4) {
                makeRotateAnim(binding.row44, status, index);
            } else if (index == 5) {
                makeRotateAnim(binding.row45, status, index);
                if (isSubmitWord) {
                    presenter.setDataInDB(4);
                }
            }
        } else if (row == 5) {
            if (index == 1) {
                makeRotateAnim(binding.row51, status, index);
            } else if (index == 2) {
                makeRotateAnim(binding.row52, status, index);
            } else if (index == 3) {
                makeRotateAnim(binding.row53, status, index);
            } else if (index == 4) {
                makeRotateAnim(binding.row54, status, index);
            } else if (index == 5) {
                makeRotateAnim(binding.row55, status, index);
                if (isSubmitWord) {
                    presenter.setDataInDB(5);
                }
            }
        } else if (row == 6) {
            if (index == 1) {
                makeRotateAnim(binding.row61, status, index);
            } else if (index == 2) {
                makeRotateAnim(binding.row62, status, index);
            } else if (index == 3) {
                makeRotateAnim(binding.row63, status, index);
            } else if (index == 4) {
                makeRotateAnim(binding.row64, status, index);
            } else if (index == 5) {
                makeRotateAnim(binding.row65, status, index);
                if (isSubmitWord) {
                    presenter.setDataInDB(6);
                }
            }
        }
    }

    @Override
    public void noWordAnimation() {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            ObjectAnimator animation1 = new ObjectAnimator();
            ObjectAnimator animation2 = new ObjectAnimator();
            ObjectAnimator animation3 = new ObjectAnimator();
            ObjectAnimator animation4 = new ObjectAnimator();
            ObjectAnimator animation5 = new ObjectAnimator();
            if (row == 1) {
                animation1 = ObjectAnimator.ofFloat(binding.row11, "translationX", 100f);
                animation2 = ObjectAnimator.ofFloat(binding.row12, "translationX", 100f);
                animation3 = ObjectAnimator.ofFloat(binding.row13, "translationX", 100f);
                animation4 = ObjectAnimator.ofFloat(binding.row14, "translationX", 100f);
                animation5 = ObjectAnimator.ofFloat(binding.row15, "translationX", 100f);
            } else if (row == 2) {
                animation1 = ObjectAnimator.ofFloat(binding.row21, "translationX", 100f);
                animation2 = ObjectAnimator.ofFloat(binding.row22, "translationX", 100f);
                animation3 = ObjectAnimator.ofFloat(binding.row23, "translationX", 100f);
                animation4 = ObjectAnimator.ofFloat(binding.row24, "translationX", 100f);
                animation5 = ObjectAnimator.ofFloat(binding.row25, "translationX", 100f);
            } else if (row == 3) {
                animation1 = ObjectAnimator.ofFloat(binding.row31, "translationX", 100f);
                animation2 = ObjectAnimator.ofFloat(binding.row32, "translationX", 100f);
                animation3 = ObjectAnimator.ofFloat(binding.row33, "translationX", 100f);
                animation4 = ObjectAnimator.ofFloat(binding.row34, "translationX", 100f);
                animation5 = ObjectAnimator.ofFloat(binding.row35, "translationX", 100f);
            } else if (row == 4) {
                animation1 = ObjectAnimator.ofFloat(binding.row41, "translationX", 100f);
                animation2 = ObjectAnimator.ofFloat(binding.row42, "translationX", 100f);
                animation3 = ObjectAnimator.ofFloat(binding.row43, "translationX", 100f);
                animation4 = ObjectAnimator.ofFloat(binding.row44, "translationX", 100f);
                animation5 = ObjectAnimator.ofFloat(binding.row45, "translationX", 100f);
            } else if (row == 5) {
                animation1 = ObjectAnimator.ofFloat(binding.row51, "translationX", 100f);
                animation2 = ObjectAnimator.ofFloat(binding.row52, "translationX", 100f);
                animation3 = ObjectAnimator.ofFloat(binding.row53, "translationX", 100f);
                animation4 = ObjectAnimator.ofFloat(binding.row54, "translationX", 100f);
                animation5 = ObjectAnimator.ofFloat(binding.row55, "translationX", 100f);
            } else if (row == 6) {
                animation1 = ObjectAnimator.ofFloat(binding.row61, "translationX", 100f);
                animation2 = ObjectAnimator.ofFloat(binding.row62, "translationX", 100f);
                animation3 = ObjectAnimator.ofFloat(binding.row63, "translationX", 100f);
                animation4 = ObjectAnimator.ofFloat(binding.row64, "translationX", 100f);
                animation5 = ObjectAnimator.ofFloat(binding.row65, "translationX", 100f);
            }
            animation1.setDuration(100);
            animation1.start();
            animation2.setDuration(100);
            animation2.start();
            animation3.setDuration(100);
            animation3.start();
            animation4.setDuration(100);
            animation4.start();
            animation5.setDuration(100);
            animation5.start();
        }, 100);

        handler.postDelayed(() -> {
            ObjectAnimator animation1 = new ObjectAnimator();
            ObjectAnimator animation2 = new ObjectAnimator();
            ObjectAnimator animation3 = new ObjectAnimator();
            ObjectAnimator animation4 = new ObjectAnimator();
            ObjectAnimator animation5 = new ObjectAnimator();
            if (row == 1) {
                animation1 = ObjectAnimator.ofFloat(binding.row11, "translationX", -100f);
                animation2 = ObjectAnimator.ofFloat(binding.row12, "translationX", -100f);
                animation3 = ObjectAnimator.ofFloat(binding.row13, "translationX", -100f);
                animation4 = ObjectAnimator.ofFloat(binding.row14, "translationX", -100f);
                animation5 = ObjectAnimator.ofFloat(binding.row15, "translationX", -100f);
            } else if (row == 2) {
                animation1 = ObjectAnimator.ofFloat(binding.row21, "translationX", -100f);
                animation2 = ObjectAnimator.ofFloat(binding.row22, "translationX", -100f);
                animation3 = ObjectAnimator.ofFloat(binding.row23, "translationX", -100f);
                animation4 = ObjectAnimator.ofFloat(binding.row24, "translationX", -100f);
                animation5 = ObjectAnimator.ofFloat(binding.row25, "translationX", -100f);
            } else if (row == 3) {
                animation1 = ObjectAnimator.ofFloat(binding.row31, "translationX", -100f);
                animation2 = ObjectAnimator.ofFloat(binding.row32, "translationX", -100f);
                animation3 = ObjectAnimator.ofFloat(binding.row33, "translationX", -100f);
                animation4 = ObjectAnimator.ofFloat(binding.row34, "translationX", -100f);
                animation5 = ObjectAnimator.ofFloat(binding.row35, "translationX", -100f);
            } else if (row == 4) {
                animation1 = ObjectAnimator.ofFloat(binding.row41, "translationX", -100f);
                animation2 = ObjectAnimator.ofFloat(binding.row42, "translationX", -100f);
                animation3 = ObjectAnimator.ofFloat(binding.row43, "translationX", -100f);
                animation4 = ObjectAnimator.ofFloat(binding.row44, "translationX", -100f);
                animation5 = ObjectAnimator.ofFloat(binding.row45, "translationX", -100f);
            } else if (row == 5) {
                animation1 = ObjectAnimator.ofFloat(binding.row51, "translationX", -100f);
                animation2 = ObjectAnimator.ofFloat(binding.row52, "translationX", -100f);
                animation3 = ObjectAnimator.ofFloat(binding.row53, "translationX", -100f);
                animation4 = ObjectAnimator.ofFloat(binding.row54, "translationX", -100f);
                animation5 = ObjectAnimator.ofFloat(binding.row55, "translationX", -100f);
            } else if (row == 6) {
                animation1 = ObjectAnimator.ofFloat(binding.row61, "translationX", -100f);
                animation2 = ObjectAnimator.ofFloat(binding.row62, "translationX", -100f);
                animation3 = ObjectAnimator.ofFloat(binding.row63, "translationX", -100f);
                animation4 = ObjectAnimator.ofFloat(binding.row64, "translationX", -100f);
                animation5 = ObjectAnimator.ofFloat(binding.row65, "translationX", -100f);
            }
            animation1.setDuration(100);
            animation1.start();
            animation2.setDuration(100);
            animation2.start();
            animation3.setDuration(100);
            animation3.start();
            animation4.setDuration(100);
            animation4.start();
            animation5.setDuration(100);
            animation5.start();
        }, 200);

        handler.postDelayed(() -> {
            ObjectAnimator animation1 = new ObjectAnimator();
            ObjectAnimator animation2 = new ObjectAnimator();
            ObjectAnimator animation3 = new ObjectAnimator();
            ObjectAnimator animation4 = new ObjectAnimator();
            ObjectAnimator animation5 = new ObjectAnimator();
            if (row == 1) {
                animation1 = ObjectAnimator.ofFloat(binding.row11, "translationX", 0f);
                animation2 = ObjectAnimator.ofFloat(binding.row12, "translationX", 0f);
                animation3 = ObjectAnimator.ofFloat(binding.row13, "translationX", 0f);
                animation4 = ObjectAnimator.ofFloat(binding.row14, "translationX", 0f);
                animation5 = ObjectAnimator.ofFloat(binding.row15, "translationX", 0f);
            } else if (row == 2) {
                animation1 = ObjectAnimator.ofFloat(binding.row21, "translationX", 0f);
                animation2 = ObjectAnimator.ofFloat(binding.row22, "translationX", 0f);
                animation3 = ObjectAnimator.ofFloat(binding.row23, "translationX", 0f);
                animation4 = ObjectAnimator.ofFloat(binding.row24, "translationX", 0f);
                animation5 = ObjectAnimator.ofFloat(binding.row25, "translationX", 0f);
            } else if (row == 3) {
                animation1 = ObjectAnimator.ofFloat(binding.row31, "translationX", 0f);
                animation2 = ObjectAnimator.ofFloat(binding.row32, "translationX", 0f);
                animation3 = ObjectAnimator.ofFloat(binding.row33, "translationX", 0f);
                animation4 = ObjectAnimator.ofFloat(binding.row34, "translationX", 0f);
                animation5 = ObjectAnimator.ofFloat(binding.row35, "translationX", 0f);
            } else if (row == 4) {
                animation1 = ObjectAnimator.ofFloat(binding.row41, "translationX", 0f);
                animation2 = ObjectAnimator.ofFloat(binding.row42, "translationX", 0f);
                animation3 = ObjectAnimator.ofFloat(binding.row43, "translationX", 0f);
                animation4 = ObjectAnimator.ofFloat(binding.row44, "translationX", 0f);
                animation5 = ObjectAnimator.ofFloat(binding.row45, "translationX", 0f);
            } else if (row == 5) {
                animation1 = ObjectAnimator.ofFloat(binding.row51, "translationX", 0f);
                animation2 = ObjectAnimator.ofFloat(binding.row52, "translationX", 0f);
                animation3 = ObjectAnimator.ofFloat(binding.row53, "translationX", 0f);
                animation4 = ObjectAnimator.ofFloat(binding.row54, "translationX", 0f);
                animation5 = ObjectAnimator.ofFloat(binding.row55, "translationX", 0f);
            } else if (row == 6) {
                animation1 = ObjectAnimator.ofFloat(binding.row61, "translationX", 0f);
                animation2 = ObjectAnimator.ofFloat(binding.row62, "translationX", 0f);
                animation3 = ObjectAnimator.ofFloat(binding.row63, "translationX", 0f);
                animation4 = ObjectAnimator.ofFloat(binding.row64, "translationX", 0f);
                animation5 = ObjectAnimator.ofFloat(binding.row65, "translationX", 0f);
            }
            animation1.setDuration(100);
            animation1.start();
            animation2.setDuration(100);
            animation2.start();
            animation3.setDuration(100);
            animation3.start();
            animation4.setDuration(100);
            animation4.start();
            animation5.setDuration(100);
            animation5.start();
            presenter.setIsEnterEnabled(true);
        }, 300);
        vibrator.vibrate(300);
        showToastOnHeight("Not in word list");
    }

    @Override
    public void setButtonsBackground(ArrayList<String> list, String answer, ArrayList<String> correctColLetters) {
        for (int i = 0; i < list.size(); i++) {
            String answerChar = "";
            if (answer.length() > i) {
                answerChar = answer.charAt(i) + "";
            } else {
                return;
            }
            if (list.get(i).equalsIgnoreCase("Q")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnQ.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnQ.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnQ.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("W")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnW.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnW.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnW.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("E")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnE.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnE.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnE.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("R")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnR.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnR.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnR.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("T")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnT.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnT.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnT.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("Y")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnY.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnY.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnY.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("U")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnU.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnU.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnU.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("I")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnI.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnI.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnI.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("O")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnO.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnO.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnO.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("P")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnP.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnP.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnP.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("A")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnA.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnA.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnA.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("S")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnS.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnS.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnS.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("D")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnD.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnD.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnD.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("F")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnF.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnF.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnF.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("G")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnG.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnG.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnG.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("H")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnH.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnH.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnH.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("J")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnJ.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnJ.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnJ.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("K")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnK.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnK.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnK.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("L")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnL.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnL.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnL.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("Z")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnZ.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnZ.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnZ.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("X")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnX.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnX.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnX.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("C")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnC.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnC.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnC.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("V")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnV.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnV.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnV.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("B")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnB.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnB.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnB.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("N")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnN.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnN.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnN.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("M")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnM.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnM.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else if (!answer.contains(list.get(i)) && !correctColLetters.contains(list.get(i))) {
                    binding.btnM.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            }
        }
        row++;
        current = 1;
    }

    @Override
    public void setDataOfLastGameInViews(ArrayList<RowModel> rowsList) {
        for (int i = 0; i < rowsList.size(); i++) {
            if (i == 0) {
                row = Integer.parseInt(rowsList.get(i).getRow());
                setCharInView(rowsList.get(i).getLetter1());
                setCharInView(rowsList.get(i).getLetter2());
                setCharInView(rowsList.get(i).getLetter3());
                setCharInView(rowsList.get(i).getLetter4());
                setCharInView(rowsList.get(i).getLetter5());
                ArrayList<String> list = new ArrayList<>();
                list.add(rowsList.get(i).getLetter1());
                list.add(rowsList.get(i).getLetter2());
                list.add(rowsList.get(i).getLetter3());
                list.add(rowsList.get(i).getLetter4());
                list.add(rowsList.get(i).getLetter5());
                presenter.wordleLogic(list, false);
            } else {
                int time = 1100 * 5;
                Handler handler = new Handler();
                int finalI = i;
                handler.postDelayed(() -> {
                    row = Integer.parseInt(rowsList.get(finalI).getRow());
                    setCharInView(rowsList.get(finalI).getLetter1());
                    setCharInView(rowsList.get(finalI).getLetter2());
                    setCharInView(rowsList.get(finalI).getLetter3());
                    setCharInView(rowsList.get(finalI).getLetter4());
                    setCharInView(rowsList.get(finalI).getLetter5());
                    ArrayList<String> list = new ArrayList<>();
                    list.add(rowsList.get(finalI).getLetter1());
                    list.add(rowsList.get(finalI).getLetter2());
                    list.add(rowsList.get(finalI).getLetter3());
                    list.add(rowsList.get(finalI).getLetter4());
                    list.add(rowsList.get(finalI).getLetter5());
                    presenter.wordleLogic(list, false);
                }, time);
            }
        }
    }

    private void setCharInView(String alphabet) {
        if (row == 1) {
            if (current == 1) {
                binding.row11.setText(alphabet);
                setCharInViewAnimation(binding.row11);
            } else if (current == 2) {
                binding.row12.setText(alphabet);
                setCharInViewAnimation(binding.row12);
            } else if (current == 3) {
                binding.row13.setText(alphabet);
                setCharInViewAnimation(binding.row13);
            } else if (current == 4) {
                binding.row14.setText(alphabet);
                setCharInViewAnimation(binding.row14);
            } else if (current == 5) {
                binding.row15.setText(alphabet);
                setCharInViewAnimation(binding.row15);
            } else {
                return;
            }
            current++;
        } else if (row == 2) {
            if (current == 1) {
                binding.row21.setText(alphabet);
                setCharInViewAnimation(binding.row21);
            } else if (current == 2) {
                binding.row22.setText(alphabet);
                setCharInViewAnimation(binding.row22);
            } else if (current == 3) {
                binding.row23.setText(alphabet);
                setCharInViewAnimation(binding.row23);
            } else if (current == 4) {
                binding.row24.setText(alphabet);
                setCharInViewAnimation(binding.row24);
            } else if (current == 5) {
                binding.row25.setText(alphabet);
                setCharInViewAnimation(binding.row25);
            } else {
                return;
            }
            current++;
        } else if (row == 3) {
            if (current == 1) {
                binding.row31.setText(alphabet);
                setCharInViewAnimation(binding.row31);
            } else if (current == 2) {
                binding.row32.setText(alphabet);
                setCharInViewAnimation(binding.row32);
            } else if (current == 3) {
                binding.row33.setText(alphabet);
                setCharInViewAnimation(binding.row33);
            } else if (current == 4) {
                binding.row34.setText(alphabet);
                setCharInViewAnimation(binding.row34);
            } else if (current == 5) {
                binding.row35.setText(alphabet);
                setCharInViewAnimation(binding.row35);
            } else {
                return;
            }
            current++;
        } else if (row == 4) {
            if (current == 1) {
                binding.row41.setText(alphabet);
                setCharInViewAnimation(binding.row41);
            } else if (current == 2) {
                binding.row42.setText(alphabet);
                setCharInViewAnimation(binding.row42);
            } else if (current == 3) {
                binding.row43.setText(alphabet);
                setCharInViewAnimation(binding.row43);
            } else if (current == 4) {
                binding.row44.setText(alphabet);
                setCharInViewAnimation(binding.row44);
            } else if (current == 5) {
                binding.row45.setText(alphabet);
                setCharInViewAnimation(binding.row45);
            } else {
                return;
            }
            current++;
        } else if (row == 5) {
            if (current == 1) {
                binding.row51.setText(alphabet);
                setCharInViewAnimation(binding.row51);
            } else if (current == 2) {
                binding.row52.setText(alphabet);
                setCharInViewAnimation(binding.row52);
            } else if (current == 3) {
                binding.row53.setText(alphabet);
                setCharInViewAnimation(binding.row53);
            } else if (current == 4) {
                binding.row54.setText(alphabet);
                setCharInViewAnimation(binding.row54);
            } else if (current == 5) {
                binding.row55.setText(alphabet);
                setCharInViewAnimation(binding.row55);
            } else {
                return;
            }
            current++;
        } else if (row == 6) {
            if (current == 1) {
                binding.row61.setText(alphabet);
                setCharInViewAnimation(binding.row61);
            } else if (current == 2) {
                binding.row62.setText(alphabet);
                setCharInViewAnimation(binding.row62);
            } else if (current == 3) {
                binding.row63.setText(alphabet);
                setCharInViewAnimation(binding.row63);
            } else if (current == 4) {
                binding.row64.setText(alphabet);
                setCharInViewAnimation(binding.row64);
            } else if (current == 5) {
                binding.row65.setText(alphabet);
                setCharInViewAnimation(binding.row65);
            } else {
                return;
            }
            current++;
        }
    }

    private void removeCharInView() {
        if (current <= 6 && current > 1) {
            current--;
        } else {
            return;
        }
        if (row == 1) {
            if (current == 1) {
                binding.row11.setText("");
                current = 1;
                return;
            } else if (current == 2) {
                binding.row12.setText("");
            } else if (current == 3) {
                binding.row13.setText("");
            } else if (current == 4) {
                binding.row14.setText("");
            } else if (current == 5) {
                binding.row15.setText("");
            } else {
                return;
            }
        } else if (row == 2) {
            if (current == 1) {
                binding.row21.setText("");
                current = 1;
                return;
            } else if (current == 2) {
                binding.row22.setText("");
            } else if (current == 3) {
                binding.row23.setText("");
            } else if (current == 4) {
                binding.row24.setText("");
            } else if (current == 5) {
                binding.row25.setText("");
            } else {
                return;
            }
        } else if (row == 3) {
            if (current == 1) {
                binding.row31.setText("");
            } else if (current == 2) {
                binding.row32.setText("");
            } else if (current == 3) {
                binding.row33.setText("");
            } else if (current == 4) {
                binding.row34.setText("");
            } else if (current == 5) {
                binding.row35.setText("");
            } else {
                return;
            }
        } else if (row == 4) {
            if (current == 1) {
                binding.row41.setText("");
            } else if (current == 2) {
                binding.row42.setText("");
            } else if (current == 3) {
                binding.row43.setText("");
            } else if (current == 4) {
                binding.row44.setText("");
            } else if (current == 5) {
                binding.row45.setText("");
            } else {
                return;
            }
        } else if (row == 5) {
            if (current == 1) {
                binding.row51.setText("");
            } else if (current == 2) {
                binding.row52.setText("");
            } else if (current == 3) {
                binding.row53.setText("");
            } else if (current == 4) {
                binding.row54.setText("");
            } else if (current == 5) {
                binding.row55.setText("");
            } else {
                return;
            }
        } else if (row == 6) {
            if (current == 1) {
                binding.row61.setText("");
            } else if (current == 2) {
                binding.row62.setText("");
            } else if (current == 3) {
                binding.row63.setText("");
            } else if (current == 4) {
                binding.row64.setText("");
            } else if (current == 5) {
                binding.row65.setText("");
            } else {
                return;
            }
        }
    }

    @Override
    public String getWord() {
        String word = "";
        if (row == 1) {
            word = binding.row11.getText().toString() + binding.row12.getText().toString() +
                    binding.row13.getText().toString() + binding.row14.getText().toString() +
                    binding.row15.getText().toString();
        } else if (row == 2) {
            word = binding.row21.getText().toString() + binding.row22.getText().toString() +
                    binding.row23.getText().toString() + binding.row24.getText().toString() +
                    binding.row25.getText().toString();
        } else if (row == 3) {
            word = binding.row31.getText().toString() + binding.row32.getText().toString() +
                    binding.row33.getText().toString() + binding.row34.getText().toString() +
                    binding.row35.getText().toString();
        } else if (row == 4) {
            word = binding.row41.getText().toString() + binding.row42.getText().toString() +
                    binding.row43.getText().toString() + binding.row44.getText().toString() +
                    binding.row45.getText().toString();
        } else if (row == 5) {
            word = binding.row51.getText().toString() + binding.row52.getText().toString() +
                    binding.row53.getText().toString() + binding.row54.getText().toString() +
                    binding.row55.getText().toString();
        } else if (row == 6) {
            word = binding.row61.getText().toString() + binding.row62.getText().toString() +
                    binding.row63.getText().toString() + binding.row64.getText().toString() +
                    binding.row65.getText().toString();
        }

        return word;
    }

    private void setCharInViewAnimation(TextView textView) {
        textView.startAnimation(scaleUp);
        Handler handler = new Handler();
        handler.postDelayed(() -> textView.startAnimation(scaleDown), 120);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.dropView();
        presenter.detachListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        CommonValues.currentFragment = CommonValues.gameFragment;
    }

    @Override
    protected void initializePresenter() {
        presenter.takeView(this);
    }
}
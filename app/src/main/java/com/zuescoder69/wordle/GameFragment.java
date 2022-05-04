package com.zuescoder69.wordle;

import static android.content.Context.VIBRATOR_SERVICE;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
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
import com.zuescoder69.wordle.databinding.FragmentGameBinding;
import com.zuescoder69.wordle.models.RowModel;
import com.zuescoder69.wordle.params.Params;
import com.zuescoder69.wordle.userData.DbHandler;
import com.zuescoder69.wordle.userData.SessionManager;
import com.zuescoder69.wordle.utils.CommonValues;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class GameFragment extends BaseFragment {

    private final String TAG = "DEMON";
    private final String wordInDB = "Word";
    private final String classic = Params.CLASSIC_GAME_MODE;
    private final String daily = Params.DAILY_GAME_MODE;
    private final String multi = Params.MULTI_GAME_MODE;
    private long vibrationTime = 80;

    private FragmentGameBinding binding;

    private int row = 1;
    private int current = 1;

    private String wordsCount;
    private String answer;
    private String currentWord;
    private String gameMode;
    private String currentDate;
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

    private Animation scaleUp, scaleDown;
    private DbHandler dbHandler;
    private SessionManager sessionManager;
    private DatabaseReference databaseReference;
    private Vibrator vibrator;
    private InterstitialAd mInterstitialAd;

    private ArrayList<RowModel> rowsList;

    private boolean isEnterEnabled = true, gameLost = false, isAdFree = false;
    private final ArrayList<Boolean> correctCol = new ArrayList<>();

    public GameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scaleUp = AnimationUtils.loadAnimation(getContext(), R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(getContext(), R.anim.scale_down);
        dbHandler = new DbHandler(getContext());
        rowsList = new ArrayList<>();
        Bundle bundle = getArguments();
        gameMode = bundle.getString("gameMode");
        vibrator = (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentGameBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(getContext());
        initCorrectCol();
        setVibration();
        CommonValues.currentFragment = CommonValues.gameFragment;
        isAdFree = CommonValues.isAdFree;
        binding.victory.setVisibility(View.GONE);
        binding.lose.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);
        binding.gameFragment.setVisibility(View.GONE);
        binding.helpBtn.setVisibility(View.INVISIBLE);
        binding.restartGameBtn.setVisibility(View.GONE);
        binding.nextGameBtn.setVisibility(View.GONE);
        binding.seeAnswerBtn.setVisibility(View.GONE);
        userId = sessionManager.getStringKey(Params.KEY_USER_ID);
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (!gameMode.equalsIgnoreCase(multi)) {
                getPreviousGameData();
            } else {
                getMultiplayerGameData();
            }
            getCurrentDate();
            setupOnClicks();
            getAppData();
        }, 300);
    }

    private void setVibration() {
        boolean vibration = sessionManager.getBooleanKey(CommonValues.VIBRATION);
        if (vibration) {
            vibrationTime = 80;
        } else {
            vibrationTime = 0;
        }
    }

    private void initCorrectCol() {
        for (int i = 0; i < 5; i++) {
            correctCol.add(i, false);
        }
    }

    private void getMultiplayerGameData() {
        roomId = CommonValues.roomId;
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("Rooms").child(CommonValues.roomDate).child(roomId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
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
        });
    }

    private void checkLobbyStatus() {
        if (lobbyStatus.equalsIgnoreCase("Result")) {
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("User Details").child(userId).child(classic).child(currentDate);
            Map setValues = new HashMap();
            setValues.put(wordInDB + wordId, "done");
            databaseReference.updateChildren(setValues);

            if (!TextUtils.isEmpty(winnerId)) {
                if (userId.equalsIgnoreCase(winnerId)) {
                    binding.victory.setVisibility(View.VISIBLE);
                } else {
                    binding.lose.setVisibility(View.VISIBLE);
                }
            }

            Handler handler1 = new Handler();
            handler1.postDelayed(() -> {
                if (CommonValues.currentFragment.equalsIgnoreCase(CommonValues.gameFragment)) {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    Bundle bundle = new Bundle();
                    bundle.putString("winnerName", winnerName);
                    if (getView() != null) {
                        Navigation.findNavController(getView()).navigate(R.id.action_gameFragment_to_resultFragment, bundle);
                    }
                }
            }, 5000);
        } else {
            if (userStatus1.equalsIgnoreCase("no") && userStatus2.equalsIgnoreCase("no")) {
                Handler handler1 = new Handler();
                handler1.postDelayed(() -> {
                    if (CommonValues.currentFragment.equalsIgnoreCase(CommonValues.gameFragment)) {
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        Bundle bundle = new Bundle();
                        bundle.putString("winnerName", "lost");
                        bundle.putString("answer", answer);
                        if (getView() != null) {
                            Navigation.findNavController(getView()).navigate(R.id.action_gameFragment_to_resultFragment, bundle);
                        }
                    }
                }, 5000);
            }
        }
    }

    private void setMuliplayerLost() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("Rooms").child(CommonValues.roomDate).child(roomId);
        Map setValues = new HashMap();
        if (userId.equalsIgnoreCase(userId1)) {
            setValues.put("UserStatus1", "no");
        } else if (userId.equalsIgnoreCase(userId2)) {
            setValues.put("UserStatus2", "no");
        }
        databaseReference.updateChildren(setValues);
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
                    if (toShowAd.equalsIgnoreCase("true")) {
                        CommonValues.isShowAd = true;
                        loadAd();
                        loadRewardedAd();
                        setRewardedCallbacks();
                    } else {
                        CommonValues.isShowAd = false;
                        binding.progress.setVisibility(View.GONE);
                        binding.gameFragment.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(getContext(), CommonValues.interVideoId, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        binding.progress.setVisibility(View.GONE);
                        binding.gameFragment.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.i(TAG, loadAdError.getMessage());
                        mInterstitialAd = null;
                        binding.progress.setVisibility(View.GONE);
                        binding.gameFragment.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void loadRewardedAd() {
        if (!gameMode.equalsIgnoreCase(multi)) {
            if (CommonValues.mRewardedAd == null && CommonValues.isShowAd) {
                AdRequest adRequest = new AdRequest.Builder().build();
                RewardedAd.load(getActivity(), CommonValues.rewardAdId,
                        adRequest, new RewardedAdLoadCallback() {
                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                // Handle the error.
                                CommonValues.mRewardedAd = null;
                                binding.progressBar.setVisibility(View.GONE);
                                binding.gameFragment.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                                CommonValues.mRewardedAd = rewardedAd;
                                binding.progressBar.setVisibility(View.GONE);
                                binding.gameFragment.setVisibility(View.VISIBLE);
                                if (!gameLost) {
                                    binding.helpBtn.setVisibility(View.VISIBLE);
                                }
                            }
                        });
            } else if (CommonValues.mRewardedAd != null && CommonValues.isShowAd) {
                binding.progressBar.setVisibility(View.GONE);
                binding.gameFragment.setVisibility(View.VISIBLE);
                if (!gameLost) {
                    binding.helpBtn.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void setRewardedCallbacks() {
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
                binding.helpBtn.setVisibility(View.INVISIBLE);
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
                binding.helpBtn.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                CommonValues.mRewardedAd = null;
                loadRewardedAd();
                binding.helpBtn.setVisibility(View.INVISIBLE);
            }
        });
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
                setDataOfLastGameInViews();
            }
        } else {
            getAnswer();
        }
    }

    private void setDataOfLastGameInViews() {
        for (int i = 0; i < rowsList.size(); i++) {
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
            wordleLogicForPreviousGame(list);
        }
    }

    private void getCurrentDate() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        currentDate = df.format(c);
        Log.d("DEMON", "getCurrentDate: date-)" + currentDate);
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
                                        databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("User Details").child(userId).child(classic).child(currentDate);
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
//                                                                    showToast(answer);
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
//                                                                showToast(answer);
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
                                        databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("User Details").child(userId).child(daily).child(currentDate);
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
                                                                answer = (String) map.get(currentDate);
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
                                                    showToast(CommonValues.comeTomorrowMsg);
                                                    if (getView() != null) {
                                                        Navigation.findNavController(getView()).navigate(R.id.action_gameFragment_to_menu_fragment);
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

    private void addItemsInFirebase() {
        String words = "aback,abase,abate,abbey,abbot,abhor,abide,abled,abode,abort,about,above,abuse,abyss,acorn,acrid,actor,acute,adage,adapt,adept,admin,admit,adobe,adopt,adore,adorn,adult,affix,afire,afoot,afoul,after,again,agape,agate,agent,agile,aging,aglow,agony,agora,agree,ahead,aider,aisle,alarm,album,alert,algae,alibi,alien,align,alike,alive,allay,alley,allot,allow,alloy,aloft,alone,along,aloof,aloud,alpha,altar,alter,amass,amaze,amber,amble,amend,amiss,amity,among,ample,amply,amuse,angel,anger,angle,angry,angst,anime,ankle,annex,annoy,annul,anode,antic,anvil,aorta,apart,aphid,aping,apnea,apple,apply,apron,aptly,arbor,ardor,arena,argue,arise,armor,aroma,arose,array,arrow,arson,artsy,ascot,ashen,aside,askew,assay,asset,atoll,atone,attic,audio,audit,augur,aunty,avail,avert,avian,avoid,await,awake,award,aware,awash,awful,awoke,axial,axiom,axion,azure,bacon,badge,badly,bagel,baggy,baker,baler,balmy,banal,banjo,barge,baron,basal,basic,basil,basin,basis,baste,batch,bathe,baton,batty,bawdy,bayou,beach,beady,beard,beast,beech,beefy,befit,began,begat,beget,begin,begun,being,belch,belie,belle,belly,below,bench,beret,berry,berth,beset,betel,bevel,bezel,bible,bicep,biddy,bigot,bilge,billy,binge,bingo,biome,birch,birth,bison,bitty,black,blade,blame,bland,blank,blare,blast,blaze,bleak,bleat,bleed,bleep,blend,bless,blimp,blind,blink,bliss,blitz,bloat,block,bloke,blond,blood,bloom,blown,bluer,bluff,blunt,blurb,blurt,blush,board,boast,bobby,boney,bongo,bonus,booby,boost,booth,booty,booze,boozy,borax,borne,bosom,bossy,botch,bough,boule,bound,bowel,boxer,brace,braid,brain,brake,brand,brash,brass,brave,bravo,brawl,brawn,bread,break,breed,briar,bribe,brick,bride,brief,brine,bring,brink,briny,brisk,broad,broil,broke,brood,brook,broom,broth,brown,brunt,brush,brute,buddy,budge,buggy,bugle,build,built,bulge,bulky,bully,bunch,bunny,burly,burnt,burst,bused,bushy,butch,butte,buxom,buyer,bylaw,cabal,cabby,cabin,cable,cacao,cache,cacti,caddy,cadet,cagey,cairn,camel,cameo,canal,candy,canny,canoe,canon,caper,caput,carat,cargo,carol,carry,carve,caste,catch,cater,catty,caulk,cause,cavil,cease,cedar,cello,chafe,chaff,chain,chair,chalk,champ,chant,chaos,chard,charm,chart,chase,chasm,cheap,cheat,check,cheek,cheer,chess,chest,chick,chide,chief,child,chili,chill,chime,china,chirp,chock,choir,choke,chord,chore,chose,chuck,chump,chunk,churn,chute,cider,cigar,cinch,circa,civic,civil,clack,claim,clamp,clang,clank,clash,clasp,class,clean,clear,cleat,cleft,clerk,click,cliff,climb,cling,clink,cloak,clock,clone,close,cloth,cloud,clout,clove,clown,cluck,clued,clump,clung,coach,coast,cobra,cocoa,colon,color,comet,comfy,comic,comma,conch,condo,conic,copse,coral,corer,corny,couch,cough,could,count,coupe,court,coven,cover,covet,covey,cower,coyly,crack,craft,cramp,crane,crank,crash,crass,crate,crave,crawl,craze,crazy,creak,cream,credo,creed,creek,creep,creme,crepe,crept,cress,crest,crick,cried,crier,crime,crimp,crisp,croak,crock,crone,crony,crook,cross,croup,crowd,crown,crude,cruel,crumb,crump,crush,crust,crypt,cubic,cumin,curio,curly,curry,curse,curve,curvy,cutie,cyber,cycle,cynic,daddy,daily,dairy,daisy,dally,dance,dandy,datum,daunt,dealt,death,debar,debit,debug,debut,decal,decay,decor,decoy,decry,defer,deign,deity,delay,delta,delve,demon,demur,denim,dense,depot,depth,derby,deter,detox,deuce,devil,diary,dicey,digit,dilly,dimly,diner,dingo,dingy,diode,dirge,dirty,disco,ditch,ditto,ditty,diver,dizzy,dodge,dodgy,dogma,doing,dolly,donor,donut,dopey,doubt,dough,dowdy,dowel,downy,dowry,dozen,draft,drain,drake,drama,drank,drape,drawl,drawn,dread,dream,dress,dried,drier,drift,drill,drink,drive,droit,droll,drone,drool,droop,dross,drove,drown,druid,drunk,dryer,dryly,duchy,dully,dummy,dumpy,dunce,dusky,dusty,dutch,duvet,dwarf,dwell,dwelt,dying,eager,eagle,early,earth,easel,eaten,eater,ebony,eclat,edict,edify,eerie,egret,eight,eject,eking,elate,elbow,elder,elect,elegy,elfin,elide,elite,elope,elude,email,embed,ember,emcee,empty,enact,endow,enema,enemy,enjoy,ennui,ensue,enter,entry,envoy,epoch,epoxy,equal,equip,erase,erect,erode,error,erupt,essay,ester,ether,ethic,ethos,etude,evade,event,every,evict,evoke,exact,exalt,excel,exert,exile,exist,expel,extol,extra,exult,eying,fable,facet,faint,fairy,faith,false,fancy,fanny,farce,fatal,fatty,fault,fauna,favor,feast,fecal,feign,fella,felon,femme,femur,fence,feral,ferry,fetal,fetch,fetid,fetus,fever,fewer,fiber,fibre,ficus,field,fiend,fiery,fifth,fifty,fight,filer,filet,filly,filmy,filth,final,finch,finer,first,fishy,fixer,fizzy,fjord,flack,flail,flair,flake,flaky,flame,flank,flare,flash,flask,fleck,fleet,flesh,flick,flier,fling,flint,flirt,float,flock,flood,floor,flora,floss,flour,flout,flown,fluff,fluid,fluke,flume,flung,flunk,flush,flute,flyer,foamy,focal,focus,foggy,foist,folio,folly,foray,force,forge,forgo,forte,forth,forty,forum,found,foyer,frail,frame,frank,fraud,freak,freed,freer,fresh,friar,fried,frill,frisk,fritz,frock,frond,front,frost,froth,frown,froze,fruit,fudge,fugue,fully,fungi,funky,funny,furor,furry,fussy,fuzzy,gaffe,gaily,gamer,gamma,gamut,gassy,gaudy,gauge,gaunt,gauze,gavel,gawky,gayer,gayly,gazer,gecko,geeky,geese,genie,genre,ghost,ghoul,giant,giddy,gipsy,girly,girth,given,giver,glade,gland,glare,glass,glaze,gleam,glean,glide,glint,gloat,globe,gloom,glory,gloss,glove,glyph,gnash,gnome,godly,going,golem,golly,gonad,goner,goody,gooey,goofy,goose,gorge,gouge,gourd,grace,grade,graft,grail,grain,grand,grant,grape,graph,grasp,grass,grate,grave,gravy,graze,great,greed,green,greet,grief,grill,grime,grimy,grind,gripe,groan,groin,groom,grope,gross,group,grout,grove,growl,grown,gruel,gruff,grunt,guard,guava,guess,guest,guide,guild,guile,guilt,guise,gulch,gully,gumbo,gummy,guppy,gusto,gusty,gypsy,habit,hairy,halve,handy,happy,hardy,harem,harpy,harry,harsh,haste,hasty,hatch,hater,haunt,haute,haven,havoc,hazel,heady,heard,heart,heath,heave,heavy,hedge,hefty,heist,helix,hello,hence,heron,hilly,hinge,hippo,hippy,hitch,hoard,hobby,hoist,holly,homer,honey,honor,horde,horny,horse,hotel,hotly,hound,house,hovel,hover,howdy,human,humid,humor,humph,humus,hunch,hunky,hurry,husky,hussy,hutch,hydro,hyena,hymen,hyper,icily,icing,ideal,idiom,idiot,idler,idyll,igloo,iliac,image,imbue,impel,imply,inane,inbox,incur,index,inept,inert,infer,ingot,inlay,inlet,inner,input,inter,intro,ionic,irate,irony,islet,issue,itchy,ivory,jaunt,jazzy,jelly,jerky,jetty,jewel,jiffy,joint,joist,joker,jolly,joust,judge,juice,juicy,jumbo,jumpy,junta,junto,juror,kappa,karma,kayak,kebab,khaki,kinky,kiosk,kitty,knack,knave,knead,kneed,kneel,knelt,knife,knock,knoll,known,koala,krill,label,labor,laden,ladle,lager,lance,lanky,lapel,lapse,large,larva,lasso,latch,later,lathe,latte,laugh,layer,leach,leafy,leaky,leant,leapt,learn,lease,leash,least,leave,ledge,leech,leery,lefty,legal,leggy,lemon,lemur,leper,level,lever,libel,liege,light,liken,lilac,limbo,limit,linen,liner,lingo,lipid,lithe,liver,livid,llama,loamy,loath,lobby,local,locus,lodge,lofty,logic,login,loopy,loose,lorry,loser,louse,lousy,lover,lower,lowly,loyal,lucid,lucky,lumen,lumpy,lunar,lunch,lunge,lupus,lurch,lurid,lusty,lying,lymph,lynch,lyric,macaw,macho,macro,madam,madly,mafia,magic,magma,maize,major,maker,mambo,mamma,mammy,manga,mange,mango,mangy,mania,manic,manly,manor,maple,march,marry,marsh,mason,masse,match,matey,mauve,maxim,maybe,mayor,mealy,meant,meaty,mecca,medal,media,medic,melee,melon,mercy,merge,merit,merry,metal,meter,metro,micro,midge,midst,might,milky,mimic,mince,miner,minim,minor,minty,minus,mirth,miser,missy,mocha,modal,model,modem,mogul,moist,molar,moldy,money,month,moody,moose,moral,moron,morph,mossy,motel,motif,motor,motto,moult,mound,mount,mourn,mouse,mouth,mover,movie,mower,mucky,mucus,muddy,mulch,mummy,munch,mural,murky,mushy,music,musky,musty,myrrh,nadir,naive,nanny,nasal,nasty,natal,naval,navel,needy,neigh,nerdy,nerve,never,newer,newly,nicer,niche,niece,night,ninja,ninny,ninth,noble,nobly,noise,noisy,nomad,noose,north,nosey,notch,novel,nudge,nurse,nutty,nylon,nymph,oaken,obese,occur,ocean,octal,octet,odder,oddly,offal,offer,often,olden,older,olive,ombre,omega,onion,onset,opera,opine,opium,optic,orbit,order,organ,other,otter,ought,ounce,outdo,outer,outgo,ovary,ovate,overt,ovine,ovoid,owing,owner,oxide,ozone,paddy,pagan,paint,paler,palsy,panel,panic,pansy,papal,paper,parer,parka,parry,parse,party,pasta,paste,pasty,patch,patio,patsy,patty,pause,payee,payer,peace,peach,pearl,pecan,pedal,penal,pence,penne,penny,perch,peril,perky,pesky,pesto,petal,petty,phase,phone,phony,photo,piano,picky,piece,piety,piggy,pilot,pinch,piney,pinky,pinto,piper,pique,pitch,pithy,pivot,pixel,pixie,pizza,place,plaid,plain,plait,plane,plank,plant,plate,plaza,plead,pleat,plied,plier,pluck,plumb,plume,plump,plunk,plush,poesy,point,poise,poker,polar,polka,polyp,pooch,poppy,porch,poser,posit,posse,pouch,pound,pouty,power,prank,prawn,preen,press,price,prick,pride,pried,prime,primo,print,prior,prism,privy,prize,probe,prone,prong,proof,prose,proud,prove,prowl,proxy,prude,prune,psalm,pubic,pudgy,puffy,pulpy,pulse,punch,pupal,pupil,puppy,puree,purer,purge,purse,pushy,putty,pygmy,quack,quail,quake,qualm,quark,quart,quash,quasi,queen,queer,quell,query,quest,queue,quick,quiet,quill,quilt,quirk,quite,quota,quote,quoth,rabbi,rabid,racer,radar,radii,radio,rainy,raise,rajah,rally,ralph,ramen,ranch,randy,range,rapid,rarer,raspy,ratio,ratty,raven,rayon,razor,reach,react,ready,realm,rearm,rebar,rebel,rebus,rebut,recap,recur,recut,reedy,refer,refit,regal,rehab,reign,relax,relay,relic,remit,renal,renew,repay,repel,reply,rerun,reset,resin,retch,retro,retry,reuse,revel,revue,rhino,rhyme,rider,ridge,rifle,right,rigid,rigor,rinse,ripen,riper,risen,riser,risky,rival,river,rivet,roach,roast,robin,robot,rocky,rodeo,roger,rogue,roomy,roost,rotor,rouge,rough,round,rouse,route,rover,rowdy,rower,royal,ruddy,ruder,rugby,ruler,rumba,rumor,rupee,rural,rusty,sadly,safer,saint,salad,sally,salon,salsa,salty,salve,salvo,sandy,saner,sappy,sassy,satin,satyr,sauce,saucy,sauna,saute,savor,savoy,savvy,scald,scale,scalp,scaly,scamp,scant,scare,scarf,scary,scene,scent,scion,scoff,scold,scone,scoop,scope,score,scorn,scour,scout,scowl,scram,scrap,scree,screw,scrub,scrum,scuba,sedan,seedy,segue,seize,semen,sense,sepia,serif,serum,serve,setup,seven,sever,sewer,shack,shade,shady,shaft,shake,shaky,shale,shall,shalt,shame,shank,shape,shard,share,shark,sharp,shave,shawl,shear,sheen,sheep,sheer,sheet,sheik,shelf,shell,shied,shift,shine,shiny,shire,shirk,shirt,shoal,shock,shone,shook,shoot,shore,shorn,short,shout,shove,shown,showy,shrew,shrub,shrug,shuck,shunt,shush,shyly,siege,sieve,sight,sigma,silky,silly,since,sinew,singe,siren,sissy,sixth,sixty,skate,skier,skiff,skill,skimp,skirt,skulk,skull,skunk,slack,slain,slang,slant,slash,slate,slave,sleek,sleep,sleet,slept,slice,slick,slide,slime,slimy,sling,slink,sloop,slope,slosh,sloth,slump,slung,slunk,slurp,slush,slyly,smack,small,smart,smash,smear,smell,smelt,smile,smirk,smite,smith,smock,smoke,smoky,smote,snack,snail,snake,snaky,snare,snarl,sneak,sneer,snide,sniff,snipe,snoop,snore,snort,snout,snowy,snuck,snuff,soapy,sober,soggy,solar,solid,solve,sonar,sonic,sooth,sooty,sorry,sound,south,sower,space,spade,spank,spare,spark,spasm,spawn,speak,spear,speck,speed,spell,spelt,spend,spent,sperm,spice,spicy,spied,spiel,spike,spiky,spill,spilt,spine,spiny,spire,spite,splat,split,spoil,spoke,spoof,spook,spool,spoon,spore,sport,spout,spray,spree,sprig,spunk,spurn,spurt,squad,squat,squib,stack,staff,stage,staid,stain,stair,stake,stale,stalk,stall,stamp,stand,stank,stare,stark,start,stash,state,stave,stead,steak,steal,steam,steed,steel,steep,steer,stein,stern,stick,stiff,still,stilt,sting,stink,stint,stock,stoic,stoke,stole,stomp,stone,stony,stood,stool,stoop,store,stork,storm,story,stout,stove,strap,straw,stray,strip,strut,stuck,study,stuff,stump,stung,stunk,stunt,style,suave,sugar,suing,suite,sulky,sully,sumac,sunny,super,surer,surge,surly,sushi,swami,swamp,swarm,swash,swath,swear,sweat,sweep,sweet,swell,swept,swift,swill,swine,swing,swirl,swish,swoon,swoop,sword,swore,sworn,swung,synod,syrup,tabby,table,taboo,tacit,tacky,taffy,taint,taken,taker,tally,talon,tamer,tango,tangy,taper,tapir,tardy,tarot,taste,tasty,tatty,taunt,tawny,teach,teary,tease,teddy,teeth,tempo,tenet,tenor,tense,tenth,tepee,tepid,terra,terse,testy,thank,theft,their,theme,there,these,theta,thick,thief,thigh,thing,think,third,thong,thorn,those,three,threw,throb,throw,thrum,thumb,thump,thyme,tiara,tibia,tidal,tiger,tight,tilde,timer,timid,tipsy,titan,tithe,title,toast,today,toddy,token,tonal,tonga,tonic,tooth,topaz,topic,torch,torso,torus,total,totem,touch,tough,towel,tower,toxic,toxin,trace,track,tract,trade,trail,train,trait,tramp,trash,trawl,tread,treat,trend,triad,trial,tribe,trice,trick,tried,tripe,trite,troll,troop,trope,trout,trove,truce,truck,truer,truly,trump,trunk,truss,trust,truth,tryst,tubal,tuber,tulip,tulle,tumor,tunic,turbo,tutor,twang,tweak,tweed,tweet,twice,twine,twirl,twist,twixt,tying,udder,ulcer,ultra,umbra,uncle,uncut,under,undid,undue,unfed,unfit,unify,union,unite,unity,unlit,unmet,unset,untie,until,unwed,unzip,upper,upset,urban,urine,usage,usher,using,usual,usurp,utile,utter,vague,valet,valid,valor,value,valve,vapid,vapor,vault,vaunt,vegan,venom,venue,verge,verse,verso,verve,vicar,video,vigil,vigor,villa,vinyl,viola,viper,viral,virus,visit,visor,vista,vital,vivid,vixen,vocal,vodka,vogue,voice,voila,vomit,voter,vouch,vowel,vying,wacky,wafer,wager,wagon,waist,waive,waltz,warty,waste,watch,water,waver,waxen,weary,weave,wedge,weedy,weigh,weird,welch,welsh,wench,whack,whale,wharf,wheat,wheel,whelp,where,which,whiff,while,whine,whiny,whirl,whisk,white,whole,whoop,whose,widen,wider,widow,width,wield,wight,willy,wimpy,wince,winch,windy,wiser,wispy,witch,witty,woken,woman,women,woody,wooer,wooly,woozy,wordy,world,worry,worse,worst,worth,would,wound,woven,wrack,wrath,wreak,wreck,wrest,wring,wrist,write,wrong,wrote,wrung,wryly,yacht,yearn,yeast,yield,young,youth,zebra,zesty,zonal";
        String guessWords = "aahed,aalii,aargh,aarti,abaca,abaci,abacs,abaft,abaka,abamp,aband,abash,abask,abaya,abbas,abbed,abbes,abcee,abeam,abear,abele,abers,abets,abies,abler,ables,ablet,ablow,abmho,abohm,aboil,aboma,aboon,abord,abore,abram,abray,abrim,abrin,abris,absey,absit,abuna,abune,abuts,abuzz,abyes,abysm,acais,acari,accas,accoy,acerb,acers,aceta,achar,ached,aches,achoo,acids,acidy,acing,acini,ackee,acker,acmes,acmic,acned,acnes,acock,acold,acred,acres,acros,acted,actin,acton,acyls,adaws,adays,adbot,addax,added,adder,addio,addle,adeem,adhan,adieu,adios,adits,adman,admen,admix,adobo,adown,adoze,adrad,adred,adsum,aduki,adunc,adust,advew,adyta,adzed,adzes,aecia,aedes,aegis,aeons,aerie,aeros,aesir,afald,afara,afars,afear,aflaj,afore,afrit,afros,agama,agami,agars,agast,agave,agaze,agene,agers,agger,aggie,aggri,aggro,aggry,aghas,agila,agios,agism,agist,agita,aglee,aglet,agley,agloo,aglus,agmas,agoge,agone,agons,agood,agria,agrin,agros,agued,agues,aguna,aguti,aheap,ahent,ahigh,ahind,ahing,ahint,ahold,ahull,ahuru,aidas,aided,aides,aidoi,aidos,aiery,aigas,aight,ailed,aimed,aimer,ainee,ainga,aioli,aired,airer,airns,airth,airts,aitch,aitus,aiver,aiyee,aizle,ajies,ajiva,ajuga,ajwan,akees,akela,akene,aking,akita,akkas,alaap,alack,alamo,aland,alane,alang,alans,alant,alapa,alaps,alary,alate,alays,albas,albee,alcid,alcos,aldea,alder,aldol,aleck,alecs,alefs,aleft,aleph,alews,aleye,alfas,algal,algas,algid,algin,algor,algum,alias,alifs,aline,alist,aliya,alkie,alkos,alkyd,alkyl,allee,allel,allis,allod,allyl,almah,almas,almeh,almes,almud,almug,alods,aloed,aloes,aloha,aloin,aloos,alowe,altho,altos,alula,alums,alure,alvar,alway,amahs,amain,amate,amaut,amban,ambit,ambos,ambry,ameba,ameer,amene,amens,ament,amias,amice,amici,amide,amido,amids,amies,amiga,amigo,amine,amino,amins,amirs,amlas,amman,ammon,ammos,amnia,amnic,amnio,amoks,amole,amort,amour,amove,amowt,amped,ampul,amrit,amuck,amyls,anana,anata,ancho,ancle,ancon,andro,anear,anele,anent,angas,anglo,anigh,anile,anils,anima,animi,anion,anise,anker,ankhs,ankus,anlas,annal,annas,annat,anoas,anole,anomy,ansae,antae,antar,antas,anted,antes,antis,antra,antre,antsy,anura,anyon,apace,apage,apaid,apayd,apays,apeak,apeek,apers,apert,apery,apgar,aphis,apian,apiol,apish,apism,apode,apods,apoop,aport,appal,appay,appel,appro,appui,appuy,apres,apses,apsis,apsos,apted,apter,aquae,aquas,araba,araks,arame,arars,arbas,arced,archi,arcos,arcus,ardeb,ardri,aread,areae,areal,arear,areas,areca,aredd,arede,arefy,areic,arene,arepa,arere,arete,arets,arett,argal,argan,argil,argle,argol,argon,argot,argus,arhat,arias,ariel,ariki,arils,ariot,arish,arked,arled,arles,armed,armer,armet,armil,arnas,arnut,aroba,aroha,aroid,arpas,arpen,arrah,arras,arret,arris,arroz,arsed,arses,arsey,arsis,artal,artel,artic,artis,aruhe,arums,arval,arvee,arvos,aryls,asana,ascon,ascus,asdic,ashed,ashes,ashet,asked,asker,askoi,askos,aspen,asper,aspic,aspie,aspis,aspro,assai,assam,asses,assez,assot,aster,astir,astun,asura,asway,aswim,asyla,ataps,ataxy,atigi,atilt,atimy,atlas,atman,atmas,atmos,atocs,atoke,atoks,atoms,atomy,atony,atopy,atria,atrip,attap,attar,atuas,audad,auger,aught,aulas,aulic,auloi,aulos,aumil,aunes,aunts,aurae,aural,aurar,auras,aurei,aures,auric,auris,aurum,autos,auxin,avale,avant,avast,avels,avens,avers,avgas,avine,avion,avise,aviso,avize,avows,avyze,awarn,awato,awave,aways,awdls,aweel,aweto,awing,awmry,awned,awner,awols,awork,axels,axile,axils,axing,axite,axled,axles,axman,axmen,axoid,axone,axons,ayahs,ayaya,ayelp,aygre,ayins,ayont,ayres,ayrie,azans,azide,azido,azine,azlon,azoic,azole,azons,azote,azoth,azuki,azurn,azury,azygy,azyme,azyms,baaed,baals,babas,babel,babes,babka,baboo,babul,babus,bacca,bacco,baccy,bacha,bachs,backs,baddy,baels,baffs,baffy,bafts,baghs,bagie,bahts,bahus,bahut,bails,bairn,baisa,baith,baits,baiza,baize,bajan,bajra,bajri,bajus,baked,baken,bakes,bakra,balas,balds,baldy,baled,bales,balks,balky,balls,bally,balms,baloo,balsa,balti,balun,balus,bambi,banak,banco,bancs,banda,bandh,bands,bandy,baned,banes,bangs,bania,banks,banns,bants,bantu,banty,banya,bapus,barbe,barbs,barby,barca,barde,bardo,bards,bardy,bared,barer,bares,barfi,barfs,baric,barks,barky,barms,barmy,barns,barny,barps,barra,barre,barro,barry,barye,basan,based,basen,baser,bases,basho,basij,basks,bason,basse,bassi,basso,bassy,basta,basti,basto,basts,bated,bates,baths,batik,batta,batts,battu,bauds,bauks,baulk,baurs,bavin,bawds,bawks,bawls,bawns,bawrs,bawty,bayed,bayer,bayes,bayle,bayts,bazar,bazoo,beads,beaks,beaky,beals,beams,beamy,beano,beans,beany,beare,bears,beath,beats,beaty,beaus,beaut,beaux,bebop,becap,becke,becks,bedad,bedel,bedes,bedew,bedim,bedye,beedi,beefs,beeps,beers,beery,beets,befog,begad,begar,begem,begot,begum,beige,beigy,beins,bekah,belah,belar,belay,belee,belga,bells,belon,belts,bemad,bemas,bemix,bemud,bends,bendy,benes,benet,benga,benis,benne,benni,benny,bento,bents,benty,bepat,beray,beres,bergs,berko,berks,berme,berms,berob,beryl,besat,besaw,besee,beses,besit,besom,besot,besti,bests,betas,beted,betes,beths,betid,beton,betta,betty,bever,bevor,bevue,bevvy,bewet,bewig,bezes,bezil,bezzy,bhais,bhaji,bhang,bhats,bhels,bhoot,bhuna,bhuts,biach,biali,bialy,bibbs,bibes,biccy,bices,bided,bider,bides,bidet,bidis,bidon,bield,biers,biffo,biffs,biffy,bifid,bigae,biggs,biggy,bigha,bight,bigly,bigos,bijou,biked,biker,bikes,bikie,bilbo,bilby,biled,biles,bilgy,bilks,bills,bimah,bimas,bimbo,binal,bindi,binds,biner,bines,bings,bingy,binit,binks,bints,biogs,biont,biota,biped,bipod,birds,birks,birle,birls,biros,birrs,birse,birsy,bises,bisks,bisom,bitch,biter,bites,bitos,bitou,bitsy,bitte,bitts,bivia,bivvy,bizes,bizzo,bizzy,blabs,blads,blady,blaer,blaes,blaff,blags,blahs,blain,blams,blart,blase,blash,blate,blats,blatt,blaud,blawn,blaws,blays,blear,blebs,blech,blees,blent,blert,blest,blets,bleys,blimy,bling,blini,blins,bliny,blips,blist,blite,blits,blive,blobs,blocs,blogs,blook,bloop,blore,blots,blows,blowy,blubs,blude,bluds,bludy,blued,blues,bluet,bluey,bluid,blume,blunk,blurs,blype,boabs,boaks,boars,boart,boats,bobac,bobak,bobas,bobol,bobos,bocca,bocce,bocci,boche,bocks,boded,bodes,bodge,bodhi,bodle,boeps,boets,boeuf,boffo,boffs,bogan,bogey,boggy,bogie,bogle,bogue,bogus,bohea,bohos,boils,boing,boink,boite,boked,bokeh,bokes,bokos,bolar,bolas,bolds,boles,bolix,bolls,bolos,bolts,bolus,bomas,bombe,bombo,bombs,bonce,bonds,boned,boner,bones,bongs,bonie,bonks,bonne,bonny,bonza,bonze,booai,booay,boobs,boody,booed,boofy,boogy,boohs,books,booky,bools,booms,boomy,boong,boons,boord,boors,boose,boots,boppy,borak,boral,boras,borde,bords,bored,boree,borel,borer,bores,borgo,boric,borks,borms,borna,boron,borts,borty,bortz,bosie,bosks,bosky,boson,bosun,botas,botel,botes,bothy,botte,botts,botty,bouge,bouks,boult,bouns,bourd,bourg,bourn,bouse,bousy,bouts,bovid,bowat,bowed,bower,bowes,bowet,bowie,bowls,bowne,bowrs,bowse,boxed,boxen,boxes,boxla,boxty,boyar,boyau,boyed,boyfs,boygs,boyla,boyos,boysy,bozos,braai,brach,brack,bract,brads,braes,brags,brail,braks,braky,brame,brane,brank,brans,brant,brast,brats,brava,bravi,braws,braxy,brays,braza,braze,bream,brede,breds,breem,breer,brees,breid,breis,breme,brens,brent,brere,brers,breve,brews,breys,brier,bries,brigs,briki,briks,brill,brims,brins,brios,brise,briss,brith,brits,britt,brize,broch,brock,brods,brogh,brogs,brome,bromo,bronc,brond,brool,broos,brose,brosy,brows,brugh,bruin,bruit,brule,brume,brung,brusk,brust,bruts,buats,buaze,bubal,bubas,bubba,bubbe,bubby,bubus,buchu,bucko,bucks,bucku,budas,budis,budos,buffa,buffe,buffi,buffo,buffs,buffy,bufos,bufty,buhls,buhrs,buiks,buist,bukes,bulbs,bulgy,bulks,bulla,bulls,bulse,bumbo,bumfs,bumph,bumps,bumpy,bunas,bunce,bunco,bunde,bundh,bunds,bundt,bundu,bundy,bungs,bungy,bunia,bunje,bunjy,bunko,bunks,bunns,bunts,bunty,bunya,buoys,buppy,buran,buras,burbs,burds,buret,burfi,burgh,burgs,burin,burka,burke,burks,burls,burns,buroo,burps,burqa,burro,burrs,burry,bursa,burse,busby,buses,busks,busky,bussu,busti,busts,busty,buteo,butes,butle,butoh,butts,butty,butut,butyl,buzzy,bwana,bwazi,byded,bydes,byked,bykes,byres,byrls,byssi,bytes,byway,caaed,cabas,caber,cabob,caboc,cabre,cacas,cacks,cacky,cadee,cades,cadge,cadgy,cadie,cadis,cadre,caeca,caese,cafes,caffs,caged,cager,cages,cagot,cahow,caids,cains,caird,cajon,cajun,caked,cakes,cakey,calfs,calid,calif,calix,calks,calla,calls,calms,calmy,calos,calpa,calps,calve,calyx,caman,camas,cames,camis,camos,campi,campo,camps,campy,camus,caned,caneh,caner,canes,cangs,canid,canna,canns,canso,canst,canto,cants,canty,capas,caped,capes,capex,caphs,capiz,caple,capon,capos,capot,capri,capul,carap,carbo,carbs,carby,cardi,cards,cardy,cared,carer,cares,caret,carex,carks,carle,carls,carns,carny,carob,carom,caron,carpi,carps,carrs,carse,carta,carte,carts,carvy,casas,casco,cased,cases,casks,casky,casts,casus,cates,cauda,cauks,cauld,cauls,caums,caups,cauri,causa,cavas,caved,cavel,caver,caves,cavie,cawed,cawks,caxon,ceaze,cebid,cecal,cecum,ceded,ceder,cedes,cedis,ceiba,ceili,ceils,celeb,cella,celli,cells,celom,celts,cense,cento,cents,centu,ceorl,cepes,cerci,cered,ceres,cerge,ceria,ceric,cerne,ceroc,ceros,certs,certy,cesse,cesta,cesti,cetes,cetyl,cezve,chace,chack,chaco,chado,chads,chaft,chais,chals,chams,chana,chang,chank,chape,chaps,chapt,chara,chare,chark,charr,chars,chary,chats,chave,chavs,chawk,chaws,chaya,chays,cheep,chefs,cheka,chela,chelp,chemo,chems,chere,chert,cheth,chevy,chews,chewy,chiao,chias,chibs,chica,chich,chico,chics,chiel,chiks,chile,chimb,chimo,chimp,chine,ching,chink,chino,chins,chips,chirk,chirl,chirm,chiro,chirr,chirt,chiru,chits,chive,chivs,chivy,chizz,choco,chocs,chode,chogs,choil,choko,choky,chola,choli,cholo,chomp,chons,choof,chook,choom,choon,chops,chota,chott,chout,choux,chowk,chows,chubs,chufa,chuff,chugs,chums,churl,churr,chuse,chuts,chyle,chyme,chynd,cibol,cided,cides,ciels,ciggy,cilia,cills,cimar,cimex,cinct,cines,cinqs,cions,cippi,circs,cires,cirls,cirri,cisco,cissy,cists,cital,cited,citer,cites,cives,civet,civie,civvy,clach,clade,clads,claes,clags,clame,clams,clans,claps,clapt,claro,clart,clary,clast,clats,claut,clave,clavi,claws,clays,cleck,cleek,cleep,clefs,clegs,cleik,clems,clepe,clept,cleve,clews,clied,clies,clift,clime,cline,clint,clipe,clips,clipt,clits,cloam,clods,cloff,clogs,cloke,clomb,clomp,clonk,clons,cloop,cloot,clops,clote,clots,clour,clous,clows,cloye,cloys,cloze,clubs,clues,cluey,clunk,clype,cnida,coact,coady,coala,coals,coaly,coapt,coarb,coate,coati,coats,cobbs,cobby,cobia,coble,cobza,cocas,cocci,cocco,cocks,cocky,cocos,codas,codec,coded,coden,coder,codes,codex,codon,coeds,coffs,cogie,cogon,cogue,cohab,cohen,cohoe,cohog,cohos,coifs,coign,coils,coins,coirs,coits,coked,cokes,colas,colby,colds,coled,coles,coley,colic,colin,colls,colly,colog,colts,colza,comae,comal,comas,combe,combi,combo,combs,comby,comer,comes,comix,commo,comms,commy,compo,comps,compt,comte,comus,coned,cones,coney,confs,conga,conge,congo,conia,conin,conks,conky,conne,conns,conte,conto,conus,convo,cooch,cooed,cooee,cooer,cooey,coofs,cooks,cooky,cools,cooly,coomb,cooms,coomy,coons,coops,coopt,coost,coots,cooze,copal,copay,coped,copen,coper,copes,coppy,copra,copsy,coqui,coram,corbe,corby,cords,cored,cores,corey,corgi,coria,corks,corky,corms,corni,corno,corns,cornu,corps,corse,corso,cosec,cosed,coses,coset,cosey,cosie,costa,coste,costs,cotan,coted,cotes,coths,cotta,cotts,coude,coups,courb,courd,coure,cours,couta,couth,coved,coves,covin,cowal,cowan,cowed,cowks,cowls,cowps,cowry,coxae,coxal,coxed,coxes,coxib,coyau,coyed,coyer,coypu,cozed,cozen,cozes,cozey,cozie,craal,crabs,crags,craic,craig,crake,crame,crams,crans,crape,craps,crapy,crare,craws,crays,creds,creel,crees,crems,crena,creps,crepy,crewe,crews,crias,cribs,cries,crims,crine,crios,cripe,crips,crise,crith,crits,croci,crocs,croft,crogs,cromb,crome,cronk,crons,crool,croon,crops,crore,crost,crout,crows,croze,cruck,crudo,cruds,crudy,crues,cruet,cruft,crunk,cruor,crura,cruse,crusy,cruve,crwth,cryer,ctene,cubby,cubeb,cubed,cuber,cubes,cubit,cuddy,cuffo,cuffs,cuifs,cuing,cuish,cuits,cukes,culch,culet,culex,culls,cully,culms,culpa,culti,cults,culty,cumec,cundy,cunei,cunit,cunts,cupel,cupid,cuppa,cuppy,curat,curbs,curch,curds,curdy,cured,curer,cures,curet,curfs,curia,curie,curli,curls,curns,curny,currs,cursi,curst,cusec,cushy,cusks,cusps,cuspy,cusso,cusum,cutch,cuter,cutes,cutey,cutin,cutis,cutto,cutty,cutup,cuvee,cuzes,cwtch,cyano,cyans,cycad,cycas,cyclo,cyder,cylix,cymae,cymar,cymas,cymes,cymol,cysts,cytes,cyton,czars,daals,dabba,daces,dacha,dacks,dadah,dadas,dados,daffs,daffy,dagga,daggy,dagos,dahls,daiko,daine,daint,daker,daled,dales,dalis,dalle,dalts,daman,damar,dames,damme,damns,damps,dampy,dancy,dangs,danio,danks,danny,dants,daraf,darbs,darcy,dared,darer,dares,darga,dargs,daric,daris,darks,darky,darns,darre,darts,darzi,dashi,dashy,datal,dated,dater,dates,datos,datto,daube,daubs,dauby,dauds,dault,daurs,dauts,daven,davit,dawah,dawds,dawed,dawen,dawks,dawns,dawts,dayan,daych,daynt,dazed,dazer,dazes,deads,deair,deals,deans,deare,dearn,dears,deary,deash,deave,deaws,deawy,debag,debby,debel,debes,debts,debud,debur,debus,debye,decad,decaf,decan,decko,decks,decos,dedal,deeds,deedy,deely,deems,deens,deeps,deere,deers,deets,deeve,deevs,defat,deffo,defis,defog,degas,degum,degus,deice,deids,deify,deils,deism,deist,deked,dekes,dekko,deled,deles,delfs,delft,delis,dells,delly,delos,delph,delts,deman,demes,demic,demit,demob,demoi,demos,dempt,denar,denay,dench,denes,denet,denis,dents,deoxy,derat,deray,dered,deres,derig,derma,derms,derns,derny,deros,derro,derry,derth,dervs,desex,deshi,desis,desks,desse,devas,devel,devis,devon,devos,devot,dewan,dewar,dewax,dewed,dexes,dexie,dhaba,dhaks,dhals,dhikr,dhobi,dhole,dholl,dhols,dhoti,dhows,dhuti,diact,dials,diane,diazo,dibbs,diced,dicer,dices,dicht,dicks,dicky,dicot,dicta,dicts,dicty,diddy,didie,didos,didst,diebs,diels,diene,diets,diffs,dight,dikas,diked,diker,dikes,dikey,dildo,dilli,dills,dimbo,dimer,dimes,dimps,dinar,dined,dines,dinge,dings,dinic,dinks,dinky,dinna,dinos,dints,diols,diota,dippy,dipso,diram,direr,dirke,dirks,dirls,dirts,disas,disci,discs,dishy,disks,disme,dital,ditas,dited,dites,ditsy,ditts,ditzy,divan,divas,dived,dives,divis,divna,divos,divot,divvy,diwan,dixie,dixit,diyas,dizen,djinn,djins,doabs,doats,dobby,dobes,dobie,dobla,dobra,dobro,docht,docks,docos,docus,doddy,dodos,doeks,doers,doest,doeth,doffs,dogan,doges,dogey,doggo,doggy,dogie,dohyo,doilt,doily,doits,dojos,dolce,dolci,doled,doles,dolia,dolls,dolma,dolor,dolos,dolts,domal,domed,domes,domic,donah,donas,donee,doner,donga,dongs,donko,donna,donne,donny,donsy,doobs,dooce,doody,dooks,doole,dools,dooly,dooms,doomy,doona,doorn,doors,doozy,dopas,doped,doper,dopes,dorad,dorba,dorbs,doree,dores,doric,doris,dorks,dorky,dorms,dormy,dorps,dorrs,dorsa,dorse,dorts,dorty,dosai,dosas,dosed,doseh,doser,doses,dosha,dotal,doted,doter,dotes,dotty,douar,douce,doucs,douks,doula,douma,doums,doups,doura,douse,douts,doved,doven,dover,doves,dovie,dowar,dowds,dowed,dower,dowie,dowle,dowls,dowly,downa,downs,dowps,dowse,dowts,doxed,doxes,doxie,doyen,doyly,dozed,dozer,dozes,drabs,drack,draco,draff,drags,drail,drams,drant,draps,drats,drave,draws,drays,drear,dreck,dreed,dreer,drees,dregs,dreks,drent,drere,drest,dreys,dribs,drice,dries,drily,drips,dript,droid,droil,droke,drole,drome,drony,droob,droog,drook,drops,dropt,drouk,drows,drubs,drugs,drums,drupe,druse,drusy,druxy,dryad,dryas,dsobo,dsomo,duads,duals,duans,duars,dubbo,ducal,ducat,duces,ducks,ducky,ducts,duddy,duded,dudes,duels,duets,duett,duffs,dufus,duing,duits,dukas,duked,dukes,dukka,dulce,dules,dulia,dulls,dulse,dumas,dumbo,dumbs,dumka,dumky,dumps,dunam,dunch,dunes,dungs,dungy,dunks,dunno,dunny,dunsh,dunts,duomi,duomo,duped,duper,dupes,duple,duply,duppy,dural,duras,dured,dures,durgy,durns,duroc,duros,duroy,durra,durrs,durry,durst,durum,durzi,dusks,dusts,duxes,dwaal,dwale,dwalm,dwams,dwang,dwaum,dweeb,dwile,dwine,dyads,dyers,dyked,dykes,dykey,dykon,dynel,dynes,dzhos,eagre,ealed,eales,eaned,eards,eared,earls,earns,earnt,earst,eased,easer,eases,easle,easts,eathe,eaved,eaves,ebbed,ebbet,ebons,ebook,ecads,eched,eches,echos,ecrus,edema,edged,edger,edges,edile,edits,educe,educt,eejit,eensy,eeven,eevns,effed,egads,egers,egest,eggar,egged,egger,egmas,ehing,eider,eidos,eigne,eiked,eikon,eilds,eisel,ejido,ekkas,elain,eland,elans,elchi,eldin,elemi,elfed,eliad,elint,elmen,eloge,elogy,eloin,elops,elpee,elsin,elute,elvan,elven,elver,elves,emacs,embar,embay,embog,embow,embox,embus,emeer,emend,emerg,emery,emeus,emics,emirs,emits,emmas,emmer,emmet,emmew,emmys,emoji,emong,emote,emove,empts,emule,emure,emyde,emyds,enarm,enate,ended,ender,endew,endue,enews,enfix,eniac,enlit,enmew,ennog,enoki,enols,enorm,enows,enrol,ensew,ensky,entia,enure,enurn,envoi,enzym,eorls,eosin,epact,epees,ephah,ephas,ephod,ephor,epics,epode,epopt,epris,eques,equid,erbia,erevs,ergon,ergos,ergot,erhus,erica,erick,erics,ering,erned,ernes,erose,erred,erses,eruct,erugo,eruvs,erven,ervil,escar,escot,esile,eskar,esker,esnes,esses,estoc,estop,estro,etage,etape,etats,etens,ethal,ethne,ethyl,etics,etnas,ettin,ettle,etuis,etwee,etyma,eughs,euked,eupad,euros,eusol,evens,evert,evets,evhoe,evils,evite,evohe,ewers,ewest,ewhow,ewked,exams,exeat,execs,exeem,exeme,exfil,exies,exine,exing,exits,exode,exome,exons,expat,expos,exude,exuls,exurb,eyass,eyers,eyots,eyras,eyres,eyrie,eyrir,ezine,fabby,faced,facer,faces,facia,facta,facts,faddy,faded,fader,fades,fadge,fados,faena,faery,faffs,faffy,faggy,fagin,fagot,faiks,fails,faine,fains,fairs,faked,faker,fakes,fakey,fakie,fakir,falaj,falls,famed,fames,fanal,fands,fanes,fanga,fango,fangs,fanks,fanon,fanos,fanum,faqir,farad,farci,farcy,fards,fared,farer,fares,farle,farls,farms,faros,farro,farse,farts,fasci,fasti,fasts,fated,fates,fatly,fatso,fatwa,faugh,fauld,fauns,faurd,fauts,fauve,favas,favel,faver,faves,favus,fawns,fawny,faxed,faxes,fayed,fayer,fayne,fayre,fazed,fazes,feals,feare,fears,feart,fease,feats,feaze,feces,fecht,fecit,fecks,fedex,feebs,feeds,feels,feens,feers,feese,feeze,fehme,feint,feist,felch,felid,fells,felly,felts,felty,femal,femes,femmy,fends,fendy,fenis,fenks,fenny,fents,feods,feoff,ferer,feres,feria,ferly,fermi,ferms,ferns,ferny,fesse,festa,fests,festy,fetas,feted,fetes,fetor,fetta,fetts,fetwa,feuar,feuds,feued,feyed,feyer,feyly,fezes,fezzy,fiars,fiats,fibro,fices,fiche,fichu,ficin,ficos,fides,fidge,fidos,fiefs,fient,fiere,fiers,fiest,fifed,fifer,fifes,fifis,figgy,figos,fiked,fikes,filar,filch,filed,files,filii,filks,fille,fillo,fills,filmi,films,filos,filum,finca,finds,fined,fines,finis,finks,finny,finos,fiord,fiqhs,fique,fired,firer,fires,firie,firks,firms,firns,firry,firth,fiscs,fisks,fists,fisty,fitch,fitly,fitna,fitte,fitts,fiver,fives,fixed,fixes,fixit,fjeld,flabs,flaff,flags,flaks,flamm,flams,flamy,flane,flans,flaps,flary,flats,flava,flawn,flaws,flawy,flaxy,flays,fleam,fleas,fleek,fleer,flees,flegs,fleme,fleur,flews,flexi,flexo,fleys,flics,flied,flies,flimp,flims,flips,flirs,flisk,flite,flits,flitt,flobs,flocs,floes,flogs,flong,flops,flors,flory,flosh,flota,flote,flows,flubs,flued,flues,fluey,fluky,flump,fluor,flurr,fluty,fluyt,flyby,flype,flyte,foals,foams,foehn,fogey,fogie,fogle,fogou,fohns,foids,foils,foins,folds,foley,folia,folic,folie,folks,folky,fomes,fonda,fonds,fondu,fones,fonly,fonts,foods,foody,fools,foots,footy,foram,forbs,forby,fordo,fords,forel,fores,forex,forks,forky,forme,forms,forts,forza,forze,fossa,fosse,fouat,fouds,fouer,fouet,foule,fouls,fount,fours,fouth,fovea,fowls,fowth,foxed,foxes,foxie,foyle,foyne,frabs,frack,fract,frags,fraim,franc,frape,fraps,frass,frate,frati,frats,fraus,frays,frees,freet,freit,fremd,frena,freon,frere,frets,fribs,frier,fries,frigs,frise,frist,frith,frits,fritt,frize,frizz,froes,frogs,frons,frore,frorn,frory,frosh,frows,frowy,frugs,frump,frush,frust,fryer,fubar,fubby,fubsy,fucks,fucus,fuddy,fudgy,fuels,fuero,fuffs,fuffy,fugal,fuggy,fugie,fugio,fugle,fugly,fugus,fujis,fulls,fumed,fumer,fumes,fumet,fundi,funds,fundy,fungo,fungs,funks,fural,furan,furca,furls,furol,furrs,furth,furze,furzy,fused,fusee,fusel,fuses,fusil,fusks,fusts,fusty,futon,fuzed,fuzee,fuzes,fuzil,fyces,fyked,fykes,fyles,fyrds,fytte,gabba,gabby,gable,gaddi,gades,gadge,gadid,gadis,gadje,gadjo,gadso,gaffs,gaged,gager,gages,gaids,gains,gairs,gaita,gaits,gaitt,gajos,galah,galas,galax,galea,galed,gales,galls,gally,galop,galut,galvo,gamas,gamay,gamba,gambe,gambo,gambs,gamed,games,gamey,gamic,gamin,gamme,gammy,gamps,ganch,gandy,ganef,ganev,gangs,ganja,ganof,gants,gaols,gaped,gaper,gapes,gapos,gappy,garbe,garbo,garbs,garda,gares,garis,garms,garni,garre,garth,garum,gases,gasps,gaspy,gasts,gatch,gated,gater,gates,gaths,gator,gauch,gaucy,gauds,gauje,gault,gaums,gaumy,gaups,gaurs,gauss,gauzy,gavot,gawcy,gawds,gawks,gawps,gawsy,gayal,gazal,gazar,gazed,gazes,gazon,gazoo,geals,geans,geare,gears,geats,gebur,gecks,geeks,geeps,geest,geist,geits,gelds,gelee,gelid,gelly,gelts,gemel,gemma,gemmy,gemot,genal,genas,genes,genet,genic,genii,genip,genny,genoa,genom,genro,gents,genty,genua,genus,geode,geoid,gerah,gerbe,geres,gerle,germs,germy,gerne,gesse,gesso,geste,gests,getas,getup,geums,geyan,geyer,ghast,ghats,ghaut,ghazi,ghees,ghest,ghyll,gibed,gibel,giber,gibes,gibli,gibus,gifts,gigas,gighe,gigot,gigue,gilas,gilds,gilet,gills,gilly,gilpy,gilts,gimel,gimme,gimps,gimpy,ginch,ginge,gings,ginks,ginny,ginzo,gipon,gippo,gippy,girds,girls,girns,giron,giros,girrs,girsh,girts,gismo,gisms,gists,gitch,gites,giust,gived,gives,gizmo,glace,glads,glady,glaik,glair,glams,glans,glary,glaum,glaur,glazy,gleba,glebe,gleby,glede,gleds,gleed,gleek,glees,gleet,gleis,glens,glent,gleys,glial,glias,glibs,gliff,glift,glike,glime,glims,glisk,glits,glitz,gloam,globi,globs,globy,glode,glogg,gloms,gloop,glops,glost,glout,glows,gloze,glued,gluer,glues,gluey,glugs,glume,glums,gluon,glute,gluts,gnarl,gnarr,gnars,gnats,gnawn,gnaws,gnows,goads,goafs,goals,goary,goats,goaty,goban,gobar,gobbi,gobbo,gobby,gobis,gobos,godet,godso,goels,goers,goest,goeth,goety,gofer,goffs,gogga,gogos,goier,gojis,golds,goldy,goles,golfs,golpe,golps,gombo,gomer,gompa,gonch,gonef,gongs,gonia,gonif,gonks,gonna,gonof,gonys,gonzo,gooby,goods,goofs,googs,gooks,gooky,goold,gools,gooly,goons,goony,goops,goopy,goors,goory,goosy,gopak,gopik,goral,goras,gored,gores,goris,gorms,gormy,gorps,gorse,gorsy,gosht,gosse,gotch,goths,gothy,gotta,gouch,gouks,goura,gouts,gouty,gowan,gowds,gowfs,gowks,gowls,gowns,goxes,goyim,goyle,graal,grabs,grads,graff,graip,grama,grame,gramp,grams,grana,grans,grapy,gravs,grays,grebe,grebo,grece,greek,grees,grege,grego,grein,grens,grese,greve,grews,greys,grice,gride,grids,griff,grift,grigs,grike,grins,griot,grips,gript,gripy,grise,grist,grisy,grith,grits,grize,groat,grody,grogs,groks,groma,grone,groof,grosz,grots,grouf,grovy,grows,grrls,grrrl,grubs,grued,grues,grufe,grume,grump,grund,gryce,gryde,gryke,grype,grypt,guaco,guana,guano,guans,guars,gucks,gucky,gudes,guffs,gugas,guids,guimp,guiro,gulag,gular,gulas,gules,gulet,gulfs,gulfy,gulls,gulph,gulps,gulpy,gumma,gummi,gumps,gundy,gunge,gungy,gunks,gunky,gunny,guqin,gurdy,gurge,gurls,gurly,gurns,gurry,gursh,gurus,gushy,gusla,gusle,gusli,gussy,gusts,gutsy,gutta,gutty,guyed,guyle,guyot,guyse,gwine,gyals,gyans,gybed,gybes,gyeld,gymps,gynae,gynie,gynny,gynos,gyoza,gypos,gyppo,gyppy,gyral,gyred,gyres,gyron,gyros,gyrus,gytes,gyved,gyves,haafs,haars,hable,habus,hacek,hacks,hadal,haded,hades,hadji,hadst,haems,haets,haffs,hafiz,hafts,haggs,hahas,haick,haika,haiks,haiku,hails,haily,hains,haint,hairs,haith,hajes,hajis,hajji,hakam,hakas,hakea,hakes,hakim,hakus,halal,haled,haler,hales,halfa,halfs,halid,hallo,halls,halma,halms,halon,halos,halse,halts,halva,halwa,hamal,hamba,hamed,hames,hammy,hamza,hanap,hance,hanch,hands,hangi,hangs,hanks,hanky,hansa,hanse,hants,haole,haoma,hapax,haply,happi,hapus,haram,hards,hared,hares,harim,harks,harls,harms,harns,haros,harps,harts,hashy,hasks,hasps,hasta,hated,hates,hatha,hauds,haufs,haugh,hauld,haulm,hauls,hault,hauns,hause,haver,haves,hawed,hawks,hawms,hawse,hayed,hayer,hayey,hayle,hazan,hazed,hazer,hazes,heads,heald,heals,heame,heaps,heapy,heare,hears,heast,heats,heben,hebes,hecht,hecks,heder,hedgy,heeds,heedy,heels,heeze,hefte,hefts,heids,heigh,heils,heirs,hejab,hejra,heled,heles,helio,hells,helms,helos,helot,helps,helve,hemal,hemes,hemic,hemin,hemps,hempy,hench,hends,henge,henna,henny,henry,hents,hepar,herbs,herby,herds,heres,herls,herma,herms,herns,heros,herry,herse,hertz,herye,hesps,hests,hetes,heths,heuch,heugh,hevea,hewed,hewer,hewgh,hexad,hexed,hexer,hexes,hexyl,heyed,hiant,hicks,hided,hider,hides,hiems,highs,hight,hijab,hijra,hiked,hiker,hikes,hikoi,hilar,hilch,hillo,hills,hilts,hilum,hilus,himbo,hinau,hinds,hings,hinky,hinny,hints,hiois,hiply,hired,hiree,hirer,hires,hissy,hists,hithe,hived,hiver,hives,hizen,hoaed,hoagy,hoars,hoary,hoast,hobos,hocks,hocus,hodad,hodja,hoers,hogan,hogen,hoggs,hoghs,hohed,hoick,hoied,hoiks,hoing,hoise,hokas,hoked,hokes,hokey,hokis,hokku,hokum,holds,holed,holes,holey,holks,holla,hollo,holme,holms,holon,holos,holts,homas,homed,homes,homey,homie,homme,homos,honan,honda,honds,honed,honer,hones,hongi,hongs,honks,honky,hooch,hoods,hoody,hooey,hoofs,hooka,hooks,hooky,hooly,hoons,hoops,hoord,hoors,hoosh,hoots,hooty,hoove,hopak,hoped,hoper,hopes,hoppy,horah,horal,horas,horis,horks,horme,horns,horst,horsy,hosed,hosel,hosen,hoser,hoses,hosey,hosta,hosts,hotch,hoten,hotty,houff,houfs,hough,houri,hours,houts,hovea,hoved,hoven,hoves,howbe,howes,howff,howfs,howks,howls,howre,howso,hoxed,hoxes,hoyas,hoyed,hoyle,hubby,hucks,hudna,hudud,huers,huffs,huffy,huger,huggy,huhus,huias,hulas,hules,hulks,hulky,hullo,hulls,hully,humas,humfs,humic,humps,humpy,hunks,hunts,hurds,hurls,hurly,hurra,hurst,hurts,hushy,husks,husos,hutia,huzza,huzzy,hwyls,hydra,hyens,hygge,hying,hykes,hylas,hyleg,hyles,hylic,hymns,hynde,hyoid,hyped,hypes,hypha,hyphy,hypos,hyrax,hyson,hythe,iambi,iambs,ibrik,icers,iched,iches,ichor,icier,icker,ickle,icons,ictal,ictic,ictus,idant,ideas,idees,ident,idled,idles,idola,idols,idyls,iftar,igapo,igged,iglus,ihram,ikans,ikats,ikons,ileac,ileal,ileum,ileus,iliad,ilial,ilium,iller,illth,imago,imams,imari,imaum,imbar,imbed,imide,imido,imids,imine,imino,immew,immit,immix,imped,impis,impot,impro,imshi,imshy,inapt,inarm,inbye,incel,incle,incog,incus,incut,indew,india,indie,indol,indow,indri,indue,inerm,infix,infos,infra,ingan,ingle,inion,inked,inker,inkle,inned,innit,inorb,inrun,inset,inspo,intel,intil,intis,intra,inula,inure,inurn,inust,invar,inwit,iodic,iodid,iodin,iotas,ippon,irade,irids,iring,irked,iroko,irone,irons,isbas,ishes,isled,isles,isnae,issei,istle,items,ither,ivied,ivies,ixias,ixnay,ixora,ixtle,izard,izars,izzat,jaaps,jabot,jacal,jacks,jacky,jaded,jades,jafas,jaffa,jagas,jager,jaggs,jaggy,jagir,jagra,jails,jaker,jakes,jakey,jalap,jalop,jambe,jambo,jambs,jambu,james,jammy,jamon,janes,janns,janny,janty,japan,japed,japer,japes,jarks,jarls,jarps,jarta,jarul,jasey,jaspe,jasps,jatos,jauks,jaups,javas,javel,jawan,jawed,jaxie,jeans,jeats,jebel,jedis,jeels,jeely,jeeps,jeers,jeeze,jefes,jeffs,jehad,jehus,jelab,jello,jells,jembe,jemmy,jenny,jeons,jerid,jerks,jerry,jesse,jests,jesus,jetes,jeton,jeune,jewed,jewie,jhala,jiaos,jibba,jibbs,jibed,jiber,jibes,jiffs,jiggy,jigot,jihad,jills,jilts,jimmy,jimpy,jingo,jinks,jinne,jinni,jinns,jirds,jirga,jirre,jisms,jived,jiver,jives,jivey,jnana,jobed,jobes,jocko,jocks,jocky,jocos,jodel,joeys,johns,joins,joked,jokes,jokey,jokol,joled,joles,jolls,jolts,jolty,jomon,jomos,jones,jongs,jonty,jooks,joram,jorum,jotas,jotty,jotun,joual,jougs,jouks,joule,jours,jowar,jowed,jowls,jowly,joyed,jubas,jubes,jucos,judas,judgy,judos,jugal,jugum,jujus,juked,jukes,jukus,julep,jumar,jumby,jumps,junco,junks,junky,jupes,jupon,jural,jurat,jurel,jures,justs,jutes,jutty,juves,juvie,kaama,kabab,kabar,kabob,kacha,kacks,kadai,kades,kadis,kafir,kagos,kagus,kahal,kaiak,kaids,kaies,kaifs,kaika,kaiks,kails,kaims,kaing,kains,kakas,kakis,kalam,kales,kalif,kalis,kalpa,kamas,kames,kamik,kamis,kamme,kanae,kanas,kandy,kaneh,kanes,kanga,kangs,kanji,kants,kanzu,kaons,kapas,kaphs,kapok,kapow,kapus,kaput,karas,karat,karks,karns,karoo,karos,karri,karst,karsy,karts,karzy,kasha,kasme,katal,katas,katis,katti,kaugh,kauri,kauru,kaury,kaval,kavas,kawas,kawau,kawed,kayle,kayos,kazis,kazoo,kbars,kebar,kebob,kecks,kedge,kedgy,keech,keefs,keeks,keels,keema,keeno,keens,keeps,keets,keeve,kefir,kehua,keirs,kelep,kelim,kells,kelly,kelps,kelpy,kelts,kelty,kembo,kembs,kemps,kempt,kempy,kenaf,kench,kendo,kenos,kente,kents,kepis,kerbs,kerel,kerfs,kerky,kerma,kerne,kerns,keros,kerry,kerve,kesar,kests,ketas,ketch,ketes,ketol,kevel,kevil,kexes,keyed,keyer,khadi,khafs,khans,khaph,khats,khaya,khazi,kheda,kheth,khets,khoja,khors,khoum,khuds,kiaat,kiack,kiang,kibbe,kibbi,kibei,kibes,kibla,kicks,kicky,kiddo,kiddy,kidel,kidge,kiefs,kiers,kieve,kievs,kight,kikes,kikoi,kiley,kilim,kills,kilns,kilos,kilps,kilts,kilty,kimbo,kinas,kinda,kinds,kindy,kines,kings,kinin,kinks,kinos,kiore,kipes,kippa,kipps,kirby,kirks,kirns,kirri,kisan,kissy,kists,kited,kiter,kites,kithe,kiths,kitul,kivas,kiwis,klang,klaps,klett,klick,klieg,kliks,klong,kloof,kluge,klutz,knags,knaps,knarl,knars,knaur,knawe,knees,knell,knish,knits,knive,knobs,knops,knosp,knots,knout,knowe,knows,knubs,knurl,knurr,knurs,knuts,koans,koaps,koban,kobos,koels,koffs,kofta,kogal,kohas,kohen,kohls,koine,kojis,kokam,kokas,koker,kokra,kokum,kolas,kolos,kombu,konbu,kondo,konks,kooks,kooky,koori,kopek,kophs,kopje,koppa,korai,koras,korat,kores,korma,koros,korun,korus,koses,kotch,kotos,kotow,koura,kraal,krabs,kraft,krais,krait,krang,krans,kranz,kraut,krays,kreep,kreng,krewe,krona,krone,kroon,krubi,krunk,ksars,kubie,kudos,kudus,kudzu,kufis,kugel,kuias,kukri,kukus,kulak,kulan,kulas,kulfi,kumis,kumys,kuris,kurre,kurta,kurus,kusso,kutas,kutch,kutis,kutus,kuzus,kvass,kvell,kwela,kyack,kyaks,kyang,kyars,kyats,kybos,kydst,kyles,kylie,kylin,kylix,kyloe,kynde,kynds,kypes,kyrie,kytes,kythe,laari,labda,labia,labis,labra,laced,lacer,laces,lacet,lacey,lacks,laddy,laded,lader,lades,laers,laevo,lagan,lahal,lahar,laich,laics,laids,laigh,laika,laiks,laird,lairs,lairy,laith,laity,laked,laker,lakes,lakhs,lakin,laksa,laldy,lalls,lamas,lambs,lamby,lamed,lamer,lames,lamia,lammy,lamps,lanai,lanas,lanch,lande,lands,lanes,lanks,lants,lapin,lapis,lapje,larch,lards,lardy,laree,lares,largo,laris,larks,larky,larns,larnt,larum,lased,laser,lases,lassi,lassu,lassy,lasts,latah,lated,laten,latex,lathi,laths,lathy,latke,latus,lauan,lauch,lauds,laufs,laund,laura,laval,lavas,laved,laver,laves,lavra,lavvy,lawed,lawer,lawin,lawks,lawns,lawny,laxed,laxer,laxes,laxly,layed,layin,layup,lazar,lazed,lazes,lazos,lazzi,lazzo,leads,leady,leafs,leaks,leams,leans,leany,leaps,leare,lears,leary,leats,leavy,leaze,leben,leccy,ledes,ledgy,ledum,leear,leeks,leeps,leers,leese,leets,leeze,lefte,lefts,leger,leges,legge,leggo,legit,lehrs,lehua,leirs,leish,leman,lemed,lemel,lemes,lemma,lemme,lends,lenes,lengs,lenis,lenos,lense,lenti,lento,leone,lepid,lepra,lepta,lered,leres,lerps,lesbo,leses,lests,letch,lethe,letup,leuch,leuco,leuds,leugh,levas,levee,leves,levin,levis,lewis,lexes,lexis,lezes,lezza,lezzy,liana,liane,liang,liard,liars,liart,liber,libra,libri,lichi,licht,licit,licks,lidar,lidos,liefs,liens,liers,lieus,lieve,lifer,lifes,lifts,ligan,liger,ligge,ligne,liked,liker,likes,likin,lills,lilos,lilts,liman,limas,limax,limba,limbi,limbs,limby,limed,limen,limes,limey,limma,limns,limos,limpa,limps,linac,linch,linds,lindy,lined,lines,liney,linga,lings,lingy,linin,links,linky,linns,linny,linos,lints,linty,linum,linux,lions,lipas,lipes,lipin,lipos,lippy,liras,lirks,lirot,lisks,lisle,lisps,lists,litai,litas,lited,liter,lites,litho,liths,litre,lived,liven,lives,livor,livre,llano,loach,loads,loafs,loams,loans,loast,loave,lobar,lobed,lobes,lobos,lobus,loche,lochs,locie,locis,locks,locos,locum,loden,lodes,loess,lofts,logan,loges,loggy,logia,logie,logoi,logon,logos,lohan,loids,loins,loipe,loirs,lokes,lolls,lolly,lolog,lomas,lomed,lomes,loner,longa,longe,longs,looby,looed,looey,loofa,loofs,looie,looks,looky,looms,loons,loony,loops,loord,loots,loped,loper,lopes,loppy,loral,loran,lords,lordy,lorel,lores,loric,loris,losed,losel,losen,loses,lossy,lotah,lotas,lotes,lotic,lotos,lotsa,lotta,lotte,lotto,lotus,loued,lough,louie,louis,louma,lound,louns,loupe,loups,loure,lours,loury,louts,lovat,loved,loves,lovey,lovie,lowan,lowed,lowes,lownd,lowne,lowns,lowps,lowry,lowse,lowts,loxed,loxes,lozen,luach,luaus,lubed,lubes,lubra,luces,lucks,lucre,ludes,ludic,ludos,luffa,luffs,luged,luger,luges,lulls,lulus,lumas,lumbi,lumme,lummy,lumps,lunas,lunes,lunet,lungi,lungs,lunks,lunts,lupin,lured,lurer,lures,lurex,lurgi,lurgy,lurks,lurry,lurve,luser,lushy,lusks,lusts,lusus,lutea,luted,luter,lutes,luvvy,luxed,luxer,luxes,lweis,lyams,lyard,lyart,lyase,lycea,lycee,lycra,lymes,lynes,lyres,lysed,lyses,lysin,lysis,lysol,lyssa,lyted,lytes,lythe,lytic,lytta,maaed,maare,maars,mabes,macas,maced,macer,maces,mache,machi,machs,macks,macle,macon,madge,madid,madre,maerl,mafic,mages,maggs,magot,magus,mahoe,mahua,mahwa,maids,maiko,maiks,maile,maill,mails,maims,mains,maire,mairs,maise,maist,makar,makes,makis,makos,malam,malar,malas,malax,males,malic,malik,malis,malls,malms,malmy,malts,malty,malus,malva,malwa,mamas,mamba,mamee,mamey,mamie,manas,manat,mandi,maneb,maned,maneh,manes,manet,mangs,manis,manky,manna,manos,manse,manta,manto,manty,manul,manus,mapau,maqui,marae,marah,maras,marcs,mardy,mares,marge,margs,maria,marid,marka,marks,marle,marls,marly,marms,maron,maror,marra,marri,marse,marts,marvy,masas,mased,maser,mases,mashy,masks,massa,massy,masts,masty,masus,matai,mated,mater,mates,maths,matin,matlo,matte,matts,matza,matzo,mauby,mauds,mauls,maund,mauri,mausy,mauts,mauzy,maven,mavie,mavin,mavis,mawed,mawks,mawky,mawns,mawrs,maxed,maxes,maxis,mayan,mayas,mayed,mayos,mayst,mazed,mazer,mazes,mazey,mazut,mbira,meads,meals,meane,means,meany,meare,mease,meath,meats,mebos,mechs,mecks,medii,medle,meeds,meers,meets,meffs,meins,meint,meiny,meith,mekka,melas,melba,melds,melic,melik,mells,melts,melty,memes,memos,menad,mends,mened,menes,menge,mengs,mensa,mense,mensh,menta,mento,menus,meous,meows,merch,mercs,merde,mered,merel,merer,meres,meril,meris,merks,merle,merls,merse,mesal,mesas,mesel,meses,meshy,mesic,mesne,meson,messy,mesto,meted,metes,metho,meths,metic,metif,metis,metol,metre,meuse,meved,meves,mewed,mewls,meynt,mezes,mezze,mezzo,mhorr,miaou,miaow,miasm,miaul,micas,miche,micht,micks,micky,micos,micra,middy,midgy,midis,miens,mieve,miffs,miffy,mifty,miggs,mihas,mihis,miked,mikes,mikra,mikva,milch,milds,miler,miles,milfs,milia,milko,milks,mille,mills,milor,milos,milpa,milts,milty,miltz,mimed,mimeo,mimer,mimes,mimsy,minae,minar,minas,mincy,minds,mined,mines,minge,mings,mingy,minis,minke,minks,minny,minos,mints,mired,mires,mirex,mirid,mirin,mirks,mirky,mirly,miros,mirvs,mirza,misch,misdo,mises,misgo,misos,missa,mists,misty,mitch,miter,mites,mitis,mitre,mitts,mixed,mixen,mixer,mixes,mixte,mixup,mizen,mizzy,mneme,moans,moats,mobby,mobes,mobey,mobie,moble,mochi,mochs,mochy,mocks,moder,modes,modge,modii,modus,moers,mofos,moggy,mohel,mohos,mohrs,mohua,mohur,moile,moils,moira,moire,moits,mojos,mokes,mokis,mokos,molal,molas,molds,moled,moles,molla,molls,molly,molto,molts,molys,momes,momma,mommy,momus,monad,monal,monas,monde,mondo,moner,mongo,mongs,monic,monie,monks,monos,monte,monty,moobs,mooch,moods,mooed,mooks,moola,mooli,mools,mooly,moong,moons,moony,moops,moors,moory,moots,moove,moped,moper,mopes,mopey,moppy,mopsy,mopus,morae,moras,morat,moray,morel,mores,moria,morne,morns,morra,morro,morse,morts,mosed,moses,mosey,mosks,mosso,moste,mosts,moted,moten,motes,motet,motey,moths,mothy,motis,motte,motts,motty,motus,motza,mouch,moues,mould,mouls,moups,moust,mousy,moved,moves,mowas,mowed,mowra,moxas,moxie,moyas,moyle,moyls,mozed,mozes,mozos,mpret,mucho,mucic,mucid,mucin,mucks,mucor,mucro,mudge,mudir,mudra,muffs,mufti,mugga,muggs,muggy,muhly,muids,muils,muirs,muist,mujik,mulct,muled,mules,muley,mulga,mulie,mulla,mulls,mulse,mulsh,mumms,mumps,mumsy,mumus,munga,munge,mungo,mungs,munis,munts,muntu,muons,muras,mured,mures,murex,murid,murks,murls,murly,murra,murre,murri,murrs,murry,murti,murva,musar,musca,mused,muser,muses,muset,musha,musit,musks,musos,musse,mussy,musth,musts,mutch,muted,muter,mutes,mutha,mutis,muton,mutts,muxed,muxes,muzak,muzzy,mvule,myall,mylar,mynah,mynas,myoid,myoma,myope,myops,myopy,mysid,mythi,myths,mythy,myxos,mzees,naams,naans,nabes,nabis,nabks,nabla,nabob,nache,nacho,nacre,nadas,naeve,naevi,naffs,nagas,naggy,nagor,nahal,naiad,naifs,naiks,nails,naira,nairu,naked,naker,nakfa,nalas,naled,nalla,named,namer,names,namma,namus,nanas,nance,nancy,nandu,nanna,nanos,nanua,napas,naped,napes,napoo,nappa,nappe,nappy,naras,narco,narcs,nards,nares,naric,naris,narks,narky,narre,nashi,natch,nates,natis,natty,nauch,naunt,navar,naves,navew,navvy,nawab,nazes,nazir,nazis,nduja,neafe,neals,neaps,nears,neath,neats,nebek,nebel,necks,neddy,needs,neeld,neele,neemb,neems,neeps,neese,neeze,negro,negus,neifs,neist,neive,nelis,nelly,nemas,nemns,nempt,nenes,neons,neper,nepit,neral,nerds,nerka,nerks,nerol,nerts,nertz,nervy,nests,netes,netop,netts,netty,neuks,neume,neums,nevel,neves,nevus,newbs,newed,newel,newie,newsy,newts,nexts,nexus,ngaio,ngana,ngati,ngoma,ngwee,nicad,nicht,nicks,nicol,nidal,nided,nides,nidor,nidus,niefs,nieve,nifes,niffs,niffy,nifty,niger,nighs,nihil,nikab,nikah,nikau,nills,nimbi,nimbs,nimps,niner,nines,ninon,nipas,nippy,niqab,nirls,nirly,nisei,nisse,nisus,niter,nites,nitid,niton,nitre,nitro,nitry,nitty,nival,nixed,nixer,nixes,nixie,nizam,nkosi,noahs,nobby,nocks,nodal,noddy,nodes,nodus,noels,noggs,nohow,noils,noily,noint,noirs,noles,nolls,nolos,nomas,nomen,nomes,nomic,nomoi,nomos,nonas,nonce,nones,nonet,nongs,nonis,nonny,nonyl,noobs,nooit,nooks,nooky,noons,noops,nopal,noria,noris,norks,norma,norms,nosed,noser,noses,notal,noted,noter,notes,notum,nould,noule,nouls,nouns,nouny,noups,novae,novas,novum,noway,nowed,nowls,nowts,nowty,noxal,noxes,noyau,noyed,noyes,nubby,nubia,nucha,nuddy,nuder,nudes,nudie,nudzh,nuffs,nugae,nuked,nukes,nulla,nulls,numbs,numen,nummy,nunny,nurds,nurdy,nurls,nurrs,nutso,nutsy,nyaff,nyala,nying,nyssa,oaked,oaker,oakum,oared,oases,oasis,oasts,oaten,oater,oaths,oaves,obang,obeah,obeli,obeys,obias,obied,obiit,obits,objet,oboes,obole,oboli,obols,occam,ocher,oches,ochre,ochry,ocker,ocrea,octad,octan,octas,octyl,oculi,odahs,odals,odeon,odeum,odism,odist,odium,odors,odour,odyle,odyls,ofays,offed,offie,oflag,ofter,ogams,ogeed,ogees,oggin,ogham,ogive,ogled,ogler,ogles,ogmic,ogres,ohias,ohing,ohmic,ohone,oidia,oiled,oiler,oinks,oints,ojime,okapi,okays,okehs,okras,oktas,oldie,oleic,olein,olent,oleos,oleum,olios,ollas,ollav,oller,ollie,ology,olpae,olpes,omasa,omber,ombus,omens,omers,omits,omlah,omovs,omrah,oncer,onces,oncet,oncus,onely,oners,onery,onium,onkus,onlay,onned,ontic,oobit,oohed,oomph,oonts,ooped,oorie,ooses,ootid,oozed,oozes,opahs,opals,opens,opepe,oping,oppos,opsin,opted,opter,orach,oracy,orals,orang,orant,orate,orbed,orcas,orcin,ordos,oread,orfes,orgia,orgic,orgue,oribi,oriel,orixa,orles,orlon,orlop,ormer,ornis,orpin,orris,ortho,orval,orzos,oscar,oshac,osier,osmic,osmol,ossia,ostia,otaku,otary,ottar,ottos,oubit,oucht,ouens,ouija,oulks,oumas,oundy,oupas,ouped,ouphe,ouphs,ourie,ousel,ousts,outby,outed,outre,outro,outta,ouzel,ouzos,ovals,ovels,ovens,overs,ovist,ovoli,ovolo,ovule,owche,owies,owled,owler,owlet,owned,owres,owrie,owsen,oxbow,oxers,oxeye,oxids,oxies,oxime,oxims,oxlip,oxter,oyers,ozeki,ozzie,paals,paans,pacas,paced,pacer,paces,pacey,pacha,packs,pacos,pacta,pacts,padis,padle,padma,padre,padri,paean,paedo,paeon,paged,pager,pages,pagle,pagod,pagri,paiks,pails,pains,paire,pairs,paisa,paise,pakka,palas,palay,palea,paled,pales,palet,palis,palki,palla,palls,pally,palms,palmy,palpi,palps,palsa,pampa,panax,pance,panda,pands,pandy,paned,panes,panga,pangs,panim,panko,panne,panni,panto,pants,panty,paoli,paolo,papas,papaw,papes,pappi,pappy,parae,paras,parch,pardi,pards,pardy,pared,paren,pareo,pares,pareu,parev,parge,pargo,paris,parki,parks,parky,parle,parly,parma,parol,parps,parra,parrs,parti,parts,parve,parvo,paseo,pases,pasha,pashm,paska,paspy,passe,pasts,pated,paten,pater,pates,paths,patin,patka,patly,patte,patus,pauas,pauls,pavan,paved,paven,paver,paves,pavid,pavin,pavis,pawas,pawaw,pawed,pawer,pawks,pawky,pawls,pawns,paxes,payed,payor,paysd,peage,peags,peaks,peaky,peals,peans,peare,pears,peart,pease,peats,peaty,peavy,peaze,pebas,pechs,pecke,pecks,pecky,pedes,pedis,pedro,peece,peeks,peels,peens,peeoy,peepe,peeps,peers,peery,peeve,peggy,peghs,peins,peise,peize,pekan,pekes,pekin,pekoe,pelas,pelau,peles,pelfs,pells,pelma,pelon,pelta,pelts,pends,pendu,pened,penes,pengo,penie,penis,penks,penna,penni,pents,peons,peony,pepla,pepos,peppy,pepsi,perai,perce,percs,perdu,perdy,perea,peres,peris,perks,perms,perns,perog,perps,perry,perse,perst,perts,perve,pervo,pervs,pervy,pesos,pests,pesty,petar,peter,petit,petre,petri,petti,petto,pewee,pewit,peyse,phage,phang,phare,pharm,pheer,phene,pheon,phese,phial,phish,phizz,phlox,phoca,phono,phons,phots,phpht,phuts,phyla,phyle,piani,pians,pibal,pical,picas,piccy,picks,picot,picra,picul,piend,piers,piert,pieta,piets,piezo,pight,pigmy,piing,pikas,pikau,piked,piker,pikes,pikey,pikis,pikul,pilae,pilaf,pilao,pilar,pilau,pilaw,pilch,pilea,piled,pilei,piler,piles,pilis,pills,pilow,pilum,pilus,pimas,pimps,pinas,pined,pines,pingo,pings,pinko,pinks,pinna,pinny,pinon,pinot,pinta,pints,pinup,pions,piony,pious,pioye,pioys,pipal,pipas,piped,pipes,pipet,pipis,pipit,pippy,pipul,pirai,pirls,pirns,pirog,pisco,pises,pisky,pisos,pissy,piste,pitas,piths,piton,pitot,pitta,piums,pixes,pized,pizes,plaas,plack,plage,plans,plaps,plash,plasm,plast,plats,platt,platy,playa,plays,pleas,plebe,plebs,plena,pleon,plesh,plews,plica,plies,plims,pling,plink,ploat,plods,plong,plonk,plook,plops,plots,plotz,plouk,plows,ploye,ploys,plues,pluff,plugs,plums,plumy,pluot,pluto,plyer,poach,poaka,poake,poboy,pocks,pocky,podal,poddy,podex,podge,podgy,podia,poems,poeps,poets,pogey,pogge,pogos,pohed,poilu,poind,pokal,poked,pokes,pokey,pokie,poled,poler,poles,poley,polio,polis,polje,polks,polls,polly,polos,polts,polys,pombe,pomes,pommy,pomos,pomps,ponce,poncy,ponds,pones,poney,ponga,pongo,pongs,pongy,ponks,ponts,ponty,ponzu,poods,pooed,poofs,poofy,poohs,pooja,pooka,pooks,pools,poons,poops,poopy,poori,poort,poots,poove,poovy,popes,poppa,popsy,porae,poral,pored,porer,pores,porge,porgy,porin,porks,porky,porno,porns,porny,porta,ports,porty,posed,poses,posey,posho,posts,potae,potch,poted,potes,potin,potoo,potsy,potto,potts,potty,pouff,poufs,pouke,pouks,poule,poulp,poult,poupe,poupt,pours,pouts,powan,powin,pownd,powns,powny,powre,poxed,poxes,poynt,poyou,poyse,pozzy,praam,prads,prahu,prams,prana,prang,praos,prase,prate,prats,pratt,praty,praus,prays,predy,preed,prees,preif,prems,premy,prent,preon,preop,preps,presa,prese,prest,preve,prexy,preys,prial,pricy,prief,prier,pries,prigs,prill,prima,primi,primp,prims,primy,prink,prion,prise,priss,proas,probs,prods,proem,profs,progs,proin,proke,prole,proll,promo,proms,pronk,props,prore,proso,pross,prost,prosy,proto,proul,prows,proyn,prunt,pruta,pryer,pryse,pseud,pshaw,psion,psoae,psoai,psoas,psora,psych,psyop,pubco,pubes,pubis,pucan,pucer,puces,pucka,pucks,puddy,pudge,pudic,pudor,pudsy,pudus,puers,puffa,puffs,puggy,pugil,puhas,pujah,pujas,pukas,puked,puker,pukes,pukey,pukka,pukus,pulao,pulas,puled,puler,pules,pulik,pulis,pulka,pulks,pulli,pulls,pully,pulmo,pulps,pulus,pumas,pumie,pumps,punas,punce,punga,pungs,punji,punka,punks,punky,punny,punto,punts,punty,pupae,pupas,pupus,purda,pured,pures,purin,puris,purls,purpy,purrs,pursy,purty,puses,pusle,pussy,putid,puton,putti,putto,putts,puzel,pwned,pyats,pyets,pygal,pyins,pylon,pyned,pynes,pyoid,pyots,pyral,pyran,pyres,pyrex,pyric,pyros,pyxed,pyxes,pyxie,pyxis,pzazz,qadis,qaids,qajaq,qanat,qapik,qibla,qophs,qorma,quads,quaff,quags,quair,quais,quaky,quale,quant,quare,quass,quate,quats,quayd,quays,qubit,quean,queme,quena,quern,queyn,queys,quich,quids,quiff,quims,quina,quine,quino,quins,quint,quipo,quips,quipu,quire,quirt,quist,quits,quoad,quods,quoif,quoin,quoit,quoll,quonk,quops,qursh,quyte,rabat,rabic,rabis,raced,races,rache,racks,racon,radge,radix,radon,raffs,rafts,ragas,ragde,raged,ragee,rager,rages,ragga,raggs,raggy,ragis,ragus,rahed,rahui,raias,raids,raiks,raile,rails,raine,rains,raird,raita,raits,rajas,rajes,raked,rakee,raker,rakes,rakia,rakis,rakus,rales,ramal,ramee,ramet,ramie,ramin,ramis,rammy,ramps,ramus,ranas,rance,rands,ranee,ranga,rangi,rangs,rangy,ranid,ranis,ranke,ranks,rants,raped,raper,rapes,raphe,rappe,rared,raree,rares,rarks,rased,raser,rases,rasps,rasse,rasta,ratal,ratan,ratas,ratch,rated,ratel,rater,rates,ratha,rathe,raths,ratoo,ratos,ratus,rauns,raupo,raved,ravel,raver,raves,ravey,ravin,rawer,rawin,rawly,rawns,raxed,raxes,rayah,rayas,rayed,rayle,rayne,razed,razee,razer,razes,razoo,readd,reads,reais,reaks,realo,reals,reame,reams,reamy,reans,reaps,rears,reast,reata,reate,reave,rebbe,rebec,rebid,rebit,rebop,rebuy,recal,recce,recco,reccy,recit,recks,recon,recta,recti,recto,redan,redds,reddy,reded,redes,redia,redid,redip,redly,redon,redos,redox,redry,redub,redux,redye,reech,reede,reeds,reefs,reefy,reeks,reeky,reels,reens,reest,reeve,refed,refel,reffo,refis,refix,refly,refry,regar,reges,reggo,regie,regma,regna,regos,regur,rehem,reifs,reify,reiki,reiks,reink,reins,reird,reist,reive,rejig,rejon,reked,rekes,rekey,relet,relie,relit,rello,reman,remap,remen,remet,remex,remix,renay,rends,reney,renga,renig,renin,renne,renos,rente,rents,reoil,reorg,repeg,repin,repla,repos,repot,repps,repro,reran,rerig,resat,resaw,resay,resee,reses,resew,resid,resit,resod,resow,resto,rests,resty,resus,retag,retax,retem,retia,retie,retox,revet,revie,rewan,rewax,rewed,rewet,rewin,rewon,rewth,rexes,rezes,rheas,rheme,rheum,rhies,rhime,rhine,rhody,rhomb,rhone,rhumb,rhyne,rhyta,riads,rials,riant,riata,ribas,ribby,ribes,riced,ricer,rices,ricey,richt,ricin,ricks,rides,ridgy,ridic,riels,riems,rieve,rifer,riffs,rifte,rifts,rifty,riggs,rigol,riled,riles,riley,rille,rills,rimae,rimed,rimer,rimes,rimus,rinds,rindy,rines,rings,rinks,rioja,riots,riped,ripes,ripps,rises,rishi,risks,risps,risus,rites,ritts,ritzy,rivas,rived,rivel,riven,rives,riyal,rizas,roads,roams,roans,roars,roary,roate,robed,robes,roble,rocks,roded,rodes,roguy,rohes,roids,roils,roily,roins,roist,rojak,rojis,roked,roker,rokes,rolag,roles,rolfs,rolls,romal,roman,romeo,romps,ronde,rondo,roneo,rones,ronin,ronne,ronte,ronts,roods,roofs,roofy,rooks,rooky,rooms,roons,roops,roopy,roosa,roose,roots,rooty,roped,roper,ropes,ropey,roque,roral,rores,roric,rorid,rorie,rorts,rorty,rosed,roses,roset,roshi,rosin,rosit,rosti,rosts,rotal,rotan,rotas,rotch,roted,rotes,rotis,rotls,roton,rotos,rotte,rouen,roues,roule,rouls,roums,roups,roupy,roust,routh,routs,roved,roven,roves,rowan,rowed,rowel,rowen,rowie,rowme,rownd,rowth,rowts,royne,royst,rozet,rozit,ruana,rubai,rubby,rubel,rubes,rubin,ruble,rubli,rubus,ruche,rucks,rudas,rudds,rudes,rudie,rudis,rueda,ruers,ruffe,ruffs,rugae,rugal,ruggy,ruing,ruins,rukhs,ruled,rules,rumal,rumbo,rumen,rumes,rumly,rummy,rumpo,rumps,rumpy,runch,runds,runed,runes,rungs,runic,runny,runts,runty,rupia,rurps,rurus,rusas,ruses,rushy,rusks,rusma,russe,rusts,ruths,rutin,rutty,ryals,rybat,ryked,rykes,rymme,rynds,ryots,ryper,saags,sabal,sabed,saber,sabes,sabha,sabin,sabir,sable,sabot,sabra,sabre,sacks,sacra,saddo,sades,sadhe,sadhu,sadis,sados,sadza,safed,safes,sagas,sager,sages,saggy,sagos,sagum,saheb,sahib,saice,saick,saics,saids,saiga,sails,saims,saine,sains,sairs,saist,saith,sajou,sakai,saker,sakes,sakia,sakis,sakti,salal,salat,salep,sales,salet,salic,salix,salle,salmi,salol,salop,salpa,salps,salse,salto,salts,salue,salut,saman,samas,samba,sambo,samek,samel,samen,sames,samey,samfu,sammy,sampi,samps,sands,saned,sanes,sanga,sangh,sango,sangs,sanko,sansa,santo,sants,saola,sapan,sapid,sapor,saran,sards,sared,saree,sarge,sargo,sarin,saris,sarks,sarky,sarod,saros,sarus,saser,sasin,sasse,satai,satay,sated,satem,sates,satis,sauba,sauch,saugh,sauls,sault,saunt,saury,sauts,saved,saver,saves,savey,savin,sawah,sawed,sawer,saxes,sayed,sayer,sayid,sayne,sayon,sayst,sazes,scabs,scads,scaff,scags,scail,scala,scall,scams,scand,scans,scapa,scape,scapi,scarp,scars,scart,scath,scats,scatt,scaud,scaup,scaur,scaws,sceat,scena,scend,schav,schmo,schul,schwa,sclim,scody,scogs,scoog,scoot,scopa,scops,scots,scoug,scoup,scowp,scows,scrab,scrae,scrag,scran,scrat,scraw,scray,scrim,scrip,scrob,scrod,scrog,scrow,scudi,scudo,scuds,scuff,scuft,scugs,sculk,scull,sculp,sculs,scums,scups,scurf,scurs,scuse,scuta,scute,scuts,scuzz,scyes,sdayn,sdein,seals,seame,seams,seamy,seans,seare,sears,sease,seats,seaze,sebum,secco,sechs,sects,seder,sedes,sedge,sedgy,sedum,seeds,seeks,seeld,seels,seely,seems,seeps,seepy,seers,sefer,segar,segni,segno,segol,segos,sehri,seifs,seils,seine,seirs,seise,seism,seity,seiza,sekos,sekts,selah,seles,selfs,sella,selle,sells,selva,semee,semes,semie,semis,senas,sends,senes,sengi,senna,senor,sensa,sensi,sente,senti,sents,senvy,senza,sepad,sepal,sepic,sepoy,septa,septs,serac,serai,seral,sered,serer,seres,serfs,serge,seric,serin,serks,seron,serow,serra,serre,serrs,serry,servo,sesey,sessa,setae,setal,seton,setts,sewan,sewar,sewed,sewel,sewen,sewin,sexed,sexer,sexes,sexto,sexts,seyen,shads,shags,shahs,shako,shakt,shalm,shaly,shama,shams,shand,shans,shaps,sharn,shash,shaul,shawm,shawn,shaws,shaya,shays,shchi,sheaf,sheal,sheas,sheds,sheel,shend,shent,sheol,sherd,shere,shero,shets,sheva,shewn,shews,shiai,shiel,shier,shies,shill,shily,shims,shins,ships,shirr,shirs,shish,shiso,shist,shite,shits,shiur,shiva,shive,shivs,shlep,shlub,shmek,shmoe,shoat,shoed,shoer,shoes,shogi,shogs,shoji,shojo,shola,shool,shoon,shoos,shope,shops,shorl,shote,shots,shott,showd,shows,shoyu,shred,shris,shrow,shtik,shtum,shtup,shule,shuln,shuls,shuns,shura,shute,shuts,shwas,shyer,sials,sibbs,sibyl,sices,sicht,sicko,sicks,sicky,sidas,sided,sider,sides,sidha,sidhe,sidle,sield,siens,sient,sieth,sieur,sifts,sighs,sigil,sigla,signa,signs,sijos,sikas,siker,sikes,silds,siled,silen,siler,siles,silex,silks,sills,silos,silts,silty,silva,simar,simas,simba,simis,simps,simul,sinds,sined,sines,sings,sinhs,sinks,sinky,sinus,siped,sipes,sippy,sired,siree,sires,sirih,siris,siroc,sirra,sirup,sisal,sises,sista,sists,sitar,sited,sites,sithe,sitka,situp,situs,siver,sixer,sixes,sixmo,sixte,sizar,sized,sizel,sizer,sizes,skags,skail,skald,skank,skart,skats,skatt,skaws,skean,skear,skeds,skeed,skeef,skeen,skeer,skees,skeet,skegg,skegs,skein,skelf,skell,skelm,skelp,skene,skens,skeos,skeps,skers,skets,skews,skids,skied,skies,skiey,skimo,skims,skink,skins,skint,skios,skips,skirl,skirr,skite,skits,skive,skivy,sklim,skoal,skody,skoff,skogs,skols,skool,skort,skosh,skran,skrik,skuas,skugs,skyed,skyer,skyey,skyfs,skyre,skyrs,skyte,slabs,slade,slaes,slags,slaid,slake,slams,slane,slank,slaps,slart,slats,slaty,slaws,slays,slebs,sleds,sleer,slews,sleys,slier,slily,slims,slipe,slips,slipt,slish,slits,slive,sloan,slobs,sloes,slogs,sloid,slojd,slomo,sloom,sloot,slops,slopy,slorm,slots,slove,slows,sloyd,slubb,slubs,slued,slues,sluff,slugs,sluit,slums,slurb,slurs,sluse,sluts,slyer,slype,smaak,smaik,smalm,smalt,smarm,smaze,smeek,smees,smeik,smeke,smerk,smews,smirr,smirs,smits,smogs,smoko,smolt,smoor,smoot,smore,smorg,smout,smowt,smugs,smurs,smush,smuts,snabs,snafu,snags,snaps,snarf,snark,snars,snary,snash,snath,snaws,snead,sneap,snebs,sneck,sneds,sneed,snees,snell,snibs,snick,snies,snift,snigs,snips,snipy,snirt,snits,snobs,snods,snoek,snoep,snogs,snoke,snood,snook,snool,snoot,snots,snowk,snows,snubs,snugs,snush,snyes,soaks,soaps,soare,soars,soave,sobas,socas,soces,socko,socks,socle,sodas,soddy,sodic,sodom,sofar,sofas,softa,softs,softy,soger,sohur,soils,soily,sojas,sojus,sokah,soken,sokes,sokol,solah,solan,solas,solde,soldi,soldo,solds,soled,solei,soler,soles,solon,solos,solum,solus,soman,somas,sonce,sonde,sones,songs,sonly,sonne,sonny,sonse,sonsy,sooey,sooks,sooky,soole,sools,sooms,soops,soote,soots,sophs,sophy,sopor,soppy,sopra,soral,soras,sorbo,sorbs,sorda,sordo,sords,sored,soree,sorel,sorer,sores,sorex,sorgo,sorns,sorra,sorta,sorts,sorus,soths,sotol,souce,souct,sough,souks,souls,soums,soups,soupy,sours,souse,souts,sowar,sowce,sowed,sowff,sowfs,sowle,sowls,sowms,sownd,sowne,sowps,sowse,sowth,soyas,soyle,soyuz,sozin,spacy,spado,spaed,spaer,spaes,spags,spahi,spail,spain,spait,spake,spald,spale,spall,spalt,spams,spane,spang,spans,spard,spars,spart,spate,spats,spaul,spawl,spaws,spayd,spays,spaza,spazz,speal,spean,speat,specs,spect,speel,speer,speil,speir,speks,speld,spelk,speos,spets,speug,spews,spewy,spial,spica,spick,spics,spide,spier,spies,spiff,spifs,spiks,spile,spims,spina,spink,spins,spirt,spiry,spits,spitz,spivs,splay,splog,spode,spods,spoom,spoor,spoot,spork,sposh,spots,sprad,sprag,sprat,spred,sprew,sprit,sprod,sprog,sprue,sprug,spuds,spued,spuer,spues,spugs,spule,spume,spumy,spurs,sputa,spyal,spyre,squab,squaw,squeg,squid,squit,squiz,stabs,stade,stags,stagy,staig,stane,stang,staph,staps,starn,starr,stars,stats,staun,staws,stays,stean,stear,stedd,stede,steds,steek,steem,steen,steil,stela,stele,stell,steme,stems,stend,steno,stens,stent,steps,stept,stere,stets,stews,stewy,steys,stich,stied,sties,stilb,stile,stime,stims,stimy,stipa,stipe,stire,stirk,stirp,stirs,stive,stivy,stoae,stoai,stoas,stoat,stobs,stoep,stogy,stoit,stoln,stoma,stond,stong,stonk,stonn,stook,stoor,stope,stops,stopt,stoss,stots,stott,stoun,stoup,stour,stown,stowp,stows,strad,strae,strag,strak,strep,strew,stria,strig,strim,strop,strow,stroy,strum,stubs,stude,studs,stull,stulm,stumm,stums,stuns,stupa,stupe,sture,sturt,styed,styes,styli,stylo,styme,stymy,styre,styte,subah,subas,subby,suber,subha,succi,sucks,sucky,sucre,sudds,sudor,sudsy,suede,suent,suers,suete,suets,suety,sugan,sughs,sugos,suhur,suids,suint,suits,sujee,sukhs,sukuk,sulci,sulfa,sulfo,sulks,sulph,sulus,sumis,summa,sumos,sumph,sumps,sunis,sunks,sunna,sunns,sunup,supes,supra,surah,sural,suras,surat,surds,sured,sures,surfs,surfy,surgy,surra,sused,suses,susus,sutor,sutra,sutta,swabs,swack,swads,swage,swags,swail,swain,swale,swaly,swamy,swang,swank,swans,swaps,swapt,sward,sware,swarf,swart,swats,swayl,sways,sweal,swede,sweed,sweel,sweer,swees,sweir,swelt,swerf,sweys,swies,swigs,swile,swims,swink,swipe,swire,swiss,swith,swits,swive,swizz,swobs,swole,swoln,swops,swopt,swots,swoun,sybbe,sybil,syboe,sybow,sycee,syces,sycon,syens,syker,sykes,sylis,sylph,sylva,symar,synch,syncs,synds,syned,synes,synth,syped,sypes,syphs,syrah,syren,sysop,sythe,syver,taals,taata,taber,tabes,tabid,tabis,tabla,tabor,tabun,tabus,tacan,taces,tacet,tache,tacho,tachs,tacks,tacos,tacts,taels,tafia,taggy,tagma,tahas,tahrs,taiga,taigs,taiko,tails,tains,taira,taish,taits,tajes,takas,takes,takhi,takin,takis,takky,talak,talaq,talar,talas,talcs,talcy,talea,taler,tales,talks,talky,talls,talma,talpa,taluk,talus,tamal,tamed,tames,tamin,tamis,tammy,tamps,tanas,tanga,tangi,tangs,tanhs,tanka,tanks,tanky,tanna,tansy,tanti,tanto,tanty,tapas,taped,tapen,tapes,tapet,tapis,tappa,tapus,taras,tardo,tared,tares,targa,targe,tarns,taroc,tarok,taros,tarps,tarre,tarry,tarsi,tarts,tarty,tasar,tased,taser,tases,tasks,tassa,tasse,tasso,tatar,tater,tates,taths,tatie,tatou,tatts,tatus,taube,tauld,tauon,taupe,tauts,tavah,tavas,taver,tawai,tawas,tawed,tawer,tawie,tawse,tawts,taxed,taxer,taxes,taxis,taxol,taxon,taxor,taxus,tayra,tazza,tazze,teade,teads,teaed,teaks,teals,teams,tears,teats,teaze,techs,techy,tecta,teels,teems,teend,teene,teens,teeny,teers,teffs,teggs,tegua,tegus,tehrs,teiid,teils,teind,teins,telae,telco,teles,telex,telia,telic,tells,telly,teloi,telos,temed,temes,tempi,temps,tempt,temse,tench,tends,tendu,tenes,tenge,tenia,tenne,tenno,tenny,tenon,tents,tenty,tenue,tepal,tepas,tepoy,terai,teras,terce,terek,teres,terfe,terfs,terga,terms,terne,terns,terry,terts,tesla,testa,teste,tests,tetes,teths,tetra,tetri,teuch,teugh,tewed,tewel,tewit,texas,texes,texts,thack,thagi,thaim,thale,thali,thana,thane,thang,thans,thanx,tharm,thars,thaws,thawy,thebe,theca,theed,theek,thees,thegn,theic,thein,thelf,thema,thens,theow,therm,thesp,thete,thews,thewy,thigs,thilk,thill,thine,thins,thiol,thirl,thoft,thole,tholi,thoro,thorp,thous,thowl,thrae,thraw,thrid,thrip,throe,thuds,thugs,thuja,thunk,thurl,thuya,thymi,thymy,tians,tiars,tical,ticca,ticed,tices,tichy,ticks,ticky,tiddy,tided,tides,tiers,tiffs,tifos,tifts,tiges,tigon,tikas,tikes,tikis,tikka,tilak,tiled,tiler,tiles,tills,tilly,tilth,tilts,timbo,timed,times,timon,timps,tinas,tinct,tinds,tinea,tined,tines,tinge,tings,tinks,tinny,tints,tinty,tipis,tippy,tired,tires,tirls,tiros,tirrs,titch,titer,titis,titre,titty,titup,tiyin,tiyns,tizes,tizzy,toads,toady,toaze,tocks,tocky,tocos,todde,toeas,toffs,toffy,tofts,tofus,togae,togas,toged,toges,togue,tohos,toile,toils,toing,toise,toits,tokay,toked,toker,tokes,tokos,tolan,tolar,tolas,toled,toles,tolls,tolly,tolts,tolus,tolyl,toman,tombs,tomes,tomia,tommy,tomos,tondi,tondo,toned,toner,tones,toney,tongs,tonka,tonks,tonne,tonus,tools,tooms,toons,toots,toped,topee,topek,toper,topes,tophe,tophi,tophs,topis,topoi,topos,toppy,toque,torah,toran,toras,torcs,tores,toric,torii,toros,torot,torrs,torse,torsi,torsk,torta,torte,torts,tosas,tosed,toses,toshy,tossy,toted,toter,totes,totty,touks,touns,tours,touse,tousy,touts,touze,touzy,towed,towie,towns,towny,towse,towsy,towts,towze,towzy,toyed,toyer,toyon,toyos,tozed,tozes,tozie,trabs,trads,tragi,traik,trams,trank,tranq,trans,trant,trape,traps,trapt,trass,trats,tratt,trave,trayf,trays,treck,treed,treen,trees,trefa,treif,treks,trema,trems,tress,trest,trets,trews,treyf,treys,triac,tride,trier,tries,triff,trigo,trigs,trike,trild,trill,trims,trine,trins,triol,trior,trios,trips,tripy,trist,troad,troak,troat,trock,trode,trods,trogs,trois,troke,tromp,trona,tronc,trone,tronk,trons,trooz,troth,trots,trows,troys,trued,trues,trugo,trugs,trull,tryer,tryke,tryma,tryps,tsade,tsadi,tsars,tsked,tsuba,tsubo,tuans,tuart,tuath,tubae,tubar,tubas,tubby,tubed,tubes,tucks,tufas,tuffe,tuffs,tufts,tufty,tugra,tuile,tuina,tuism,tuktu,tules,tulpa,tulsi,tumid,tummy,tumps,tumpy,tunas,tunds,tuned,tuner,tunes,tungs,tunny,tupek,tupik,tuple,tuque,turds,turfs,turfy,turks,turme,turms,turns,turnt,turps,turrs,tushy,tusks,tusky,tutee,tutti,tutty,tutus,tuxes,tuyer,twaes,twain,twals,twank,twats,tways,tweel,tween,tweep,tweer,twerk,twerp,twier,twigs,twill,twilt,twink,twins,twiny,twire,twirp,twite,twits,twoer,twyer,tyees,tyers,tyiyn,tykes,tyler,tymps,tynde,tyned,tynes,typal,typed,types,typey,typic,typos,typps,typto,tyran,tyred,tyres,tyros,tythe,tzars,udals,udons,ugali,ugged,uhlan,uhuru,ukase,ulama,ulans,ulema,ulmin,ulnad,ulnae,ulnar,ulnas,ulpan,ulvas,ulyie,ulzie,umami,umbel,umber,umble,umbos,umbre,umiac,umiak,umiaq,ummah,ummas,ummed,umped,umphs,umpie,umpty,umrah,umras,unais,unapt,unarm,unary,unaus,unbag,unban,unbar,unbed,unbid,unbox,uncap,unces,uncia,uncos,uncoy,uncus,undam,undee,undos,undug,uneth,unfix,ungag,unget,ungod,ungot,ungum,unhat,unhip,unica,units,unjam,unked,unket,unkid,unlaw,unlay,unled,unlet,unlid,unman,unmew,unmix,unpay,unpeg,unpen,unpin,unred,unrid,unrig,unrip,unsaw,unsay,unsee,unsew,unsex,unsod,untax,untin,unwet,unwit,unwon,upbow,upbye,updos,updry,upend,upjet,uplay,upled,uplit,upped,upran,uprun,upsee,upsey,uptak,upter,uptie,uraei,urali,uraos,urare,urari,urase,urate,urbex,urbia,urdee,ureal,ureas,uredo,ureic,urena,urent,urged,urger,urges,urial,urite,urman,urnal,urned,urped,ursae,ursid,urson,urubu,urvas,users,usnea,usque,usure,usury,uteri,uveal,uveas,uvula,vacua,vaded,vades,vagal,vagus,vails,vaire,vairs,vairy,vakas,vakil,vales,valis,valse,vamps,vampy,vanda,vaned,vanes,vangs,vants,vaped,vaper,vapes,varan,varas,vardy,varec,vares,varia,varix,varna,varus,varve,vasal,vases,vasts,vasty,vatic,vatus,vauch,vaute,vauts,vawte,vaxes,veale,veals,vealy,veena,veeps,veers,veery,vegas,veges,vegie,vegos,vehme,veils,veily,veins,veiny,velar,velds,veldt,veles,vells,velum,venae,venal,vends,vendu,veney,venge,venin,vents,venus,verbs,verra,verry,verst,verts,vertu,vespa,vesta,vests,vetch,vexed,vexer,vexes,vexil,vezir,vials,viand,vibes,vibex,vibey,viced,vices,vichy,viers,views,viewy,vifda,viffs,vigas,vigia,vilde,viler,villi,vills,vimen,vinal,vinas,vinca,vined,viner,vines,vinew,vinic,vinos,vints,viold,viols,vired,vireo,vires,virga,virge,virid,virls,virtu,visas,vised,vises,visie,visne,vison,visto,vitae,vitas,vitex,vitro,vitta,vivas,vivat,vivda,viver,vives,vizir,vizor,vleis,vlies,vlogs,voars,vocab,voces,voddy,vodou,vodun,voema,vogie,voids,voile,voips,volae,volar,voled,voles,volet,volks,volta,volte,volti,volts,volva,volve,vomer,voted,votes,vouge,voulu,vowed,vower,voxel,vozhd,vraic,vrils,vroom,vrous,vrouw,vrows,vuggs,vuggy,vughs,vughy,vulgo,vulns,vulva,vutty,waacs,wacke,wacko,wacks,wadds,waddy,waded,wader,wades,wadge,wadis,wadts,waffs,wafts,waged,wages,wagga,wagyu,wahoo,waide,waifs,waift,wails,wains,wairs,waite,waits,wakas,waked,waken,waker,wakes,wakfs,waldo,walds,waled,waler,wales,walie,walis,walks,walla,walls,wally,walty,wamed,wames,wamus,wands,waned,wanes,waney,wangs,wanks,wanky,wanle,wanly,wanna,wants,wanty,wanze,waqfs,warbs,warby,wards,wared,wares,warez,warks,warms,warns,warps,warre,warst,warts,wases,washy,wasms,wasps,waspy,wasts,watap,watts,wauff,waugh,wauks,waulk,wauls,waurs,waved,waves,wavey,wawas,wawes,wawls,waxed,waxer,waxes,wayed,wazir,wazoo,weald,weals,weamb,weans,wears,webby,weber,wecht,wedel,wedgy,weeds,weeke,weeks,weels,weems,weens,weeny,weeps,weepy,weest,weete,weets,wefte,wefts,weids,weils,weirs,weise,weize,wekas,welds,welke,welks,welkt,wells,welly,welts,wembs,wends,wenge,wenny,wents,weros,wersh,wests,wetas,wetly,wexed,wexes,whamo,whams,whang,whaps,whare,whata,whats,whaup,whaur,wheal,whear,wheen,wheep,wheft,whelk,whelm,whens,whets,whews,wheys,whids,whift,whigs,whilk,whims,whins,whios,whips,whipt,whirr,whirs,whish,whiss,whist,whits,whity,whizz,whomp,whoof,whoot,whops,whore,whorl,whort,whoso,whows,whump,whups,whyda,wicca,wicks,wicky,widdy,wides,wiels,wifed,wifes,wifey,wifie,wifty,wigan,wigga,wiggy,wikis,wilco,wilds,wiled,wiles,wilga,wilis,wilja,wills,wilts,wimps,winds,wined,wines,winey,winge,wings,wingy,winks,winna,winns,winos,winze,wiped,wiper,wipes,wired,wirer,wires,wirra,wised,wises,wisha,wisht,wisps,wists,witan,wited,wites,withe,withs,withy,wived,wiver,wives,wizen,wizes,woads,woald,wocks,wodge,woful,wojus,woker,wokka,wolds,wolfs,wolly,wolve,wombs,womby,womyn,wonga,wongi,wonks,wonky,wonts,woods,wooed,woofs,woofy,woold,wools,woons,woops,woopy,woose,woosh,wootz,words,works,worms,wormy,worts,wowed,wowee,woxen,wrang,wraps,wrapt,wrast,wrate,wrawl,wrens,wrick,wried,wrier,wries,writs,wroke,wroot,wroth,wryer,wuddy,wudus,wulls,wurst,wuses,wushu,wussy,wuxia,wyled,wyles,wynds,wynns,wyted,wytes,xebec,xenia,xenic,xenon,xeric,xerox,xerus,xoana,xrays,xylan,xylem,xylic,xylol,xylyl,xysti,xysts,yaars,yabas,yabba,yabby,yacca,yacka,yacks,yaffs,yager,yages,yagis,yahoo,yaird,yakka,yakow,yales,yamen,yampy,yamun,yangs,yanks,yapok,yapon,yapps,yappy,yarak,yarco,yards,yarer,yarfa,yarks,yarns,yarrs,yarta,yarto,yates,yauds,yauld,yaups,yawed,yawey,yawls,yawns,yawny,yawps,ybore,yclad,ycled,ycond,ydrad,ydred,yeads,yeahs,yealm,yeans,yeard,years,yecch,yechs,yechy,yedes,yeeds,yeesh,yeggs,yelks,yells,yelms,yelps,yelts,yenta,yente,yerba,yerds,yerks,yeses,yesks,yests,yesty,yetis,yetts,yeuks,yeuky,yeven,yeves,yewen,yexed,yexes,yfere,yiked,yikes,yills,yince,yipes,yippy,yirds,yirks,yirrs,yirth,yites,yitie,ylems,ylike,ylkes,ymolt,ympes,yobbo,yobby,yocks,yodel,yodhs,yodle,yogas,yogee,yoghs,yogic,yogin,yogis,yoick,yojan,yoked,yokel,yoker,yokes,yokul,yolks,yolky,yomim,yomps,yonic,yonis,yonks,yoofs,yoops,yores,yorks,yorps,youks,yourn,yours,yourt,youse,yowed,yowes,yowie,yowls,yowza,yrapt,yrent,yrivd,yrneh,ysame,ytost,yuans,yucas,yucca,yucch,yucko,yucks,yucky,yufts,yugas,yuked,yukes,yukky,yukos,yulan,yules,yummo,yummy,yumps,yupon,yuppy,yurta,yurts,yuzus,zabra,zacks,zaida,zaidy,zaire,zakat,zaman,zambo,zamia,zanja,zante,zanza,zanze,zappy,zarfs,zaris,zatis,zaxes,zayin,zazen,zeals,zebec,zebub,zebus,zedas,zeins,zendo,zerda,zerks,zeros,zests,zetas,zexes,zezes,zhomo,zibet,ziffs,zigan,zilas,zilch,zilla,zills,zimbi,zimbs,zinco,zincs,zincy,zineb,zines,zings,zingy,zinke,zinky,zippo,zippy,ziram,zitis,zizel,zizit,zlote,zloty,zoaea,zobos,zobus,zocco,zoeae,zoeal,zoeas,zoism,zoist,zombi,zonae,zonda,zoned,zoner,zones,zonks,zooea,zooey,zooid,zooks,zooms,zoons,zooty,zoppa,zoppo,zoril,zoris,zorro,zouks,zowee,zowie,zulus,zupan,zupas,zuppa,zurfs,zuzim,zygal,zygon,zymes,zymic";
        guessWords = guessWords + words;
        String tempWords = "aback,abase,abate,abbey,abbot,abhor,abide";
        List<String> wordsList = Arrays.asList(words.split(",", -1));
        List<String> guessList = Arrays.asList(guessWords.split(",", -1));

//        databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("Words");

        //TODO for all words
//        Map setValues = new HashMap();
//        for (int i = 0; i < wordsList.size(); i++) {
//            int value = i + 1;
//            setValues.put(wordInDB + value, wordsList.get(i).toUpperCase().trim());
//        }
//        databaseReference.updateChildren(setValues);

        //TODO for all guess words
//        databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("GuessWords");
//        Map setValues1 = new HashMap();
//        for (int i = 0; i < guessList.size(); i++) {
//            setValues1.put(guessList.get(i).trim(), guessList.get(i).toUpperCase().trim());
//        }
//        databaseReference.updateChildren(setValues1);

        //TODO for all daily words
//        databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("DailyWords");
//
//        Date c = Calendar.getInstance().getTime();
//        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
//        String formattedDate = "16-Mar-2022";
//
//        Map setValues2 = new HashMap();
//        int temp = 50;
//        for (int i = 0; i < wordsList.size(); i++) {
//            Calendar calendar = Calendar.getInstance();
//            try {
//                calendar.setTime(df.parse(formattedDate));
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            calendar.add(Calendar.DATE, i + 1);  // number of days to add
//            temp = temp + 1;
//            String tempDate = df.format(calendar.getTime());
//            setValues2.put(tempDate, wordsList.get(i).toUpperCase().trim());
//        }
//        databaseReference.updateChildren(setValues2);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupOnClicks() {
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
                if (isEnterEnabled) {
                    if (current == 6) {
                        if (row < 7) {
                            if (mInterstitialAd != null && !isAdFree) {
                                mInterstitialAd.show(getActivity());
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
                showHint();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                binding.helpBtn.startAnimation(scaleDown);
            }
            return true;
        });
    }

    private void showHint() {
        if (CommonValues.mRewardedAd != null) {
            CommonValues.mRewardedAd.show(getActivity(), rewardItem -> {
//                CommonValues.mRewardedAd = null;
//                loadRewardedAd();
                for (int i = 0; i < correctCol.size(); i++) {
                    if (!correctCol.get(i)) {
                        binding.hintTv.setVisibility(View.VISIBLE);
                        String hint = answer.charAt(i) + "";
                        binding.hintTv.setText("Word has letter - " + hint.toUpperCase());
                        break;
                    }
                }
            });
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void showLostGameViews() {
        if (CommonValues.isShowAd) {
            gameLost = true;
            if (CommonValues.mRewardedAd != null) {
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
                        restartGame();
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        binding.restartGameBtn.startAnimation(scaleDown);
                    }
                    return true;
                });
            }
        } else {
            Handler handler1 = new Handler();
            handler1.postDelayed(() -> {
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

    private void restartGame() {
        if (CommonValues.mRewardedAd != null) {
            CommonValues.mRewardedAd.show(getActivity(), rewardItem -> {
//                CommonValues.mRewardedAd = null;
//                loadRewardedAd();
                removeAllCharFromViews();
            });
        }
    }

    private void removeAllCharFromViews() {
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

        gameLost = false;
        binding.lose.setVisibility(View.GONE);
        binding.restartGameBtn.setVisibility(View.GONE);
        binding.nextGameBtn.setVisibility(View.GONE);
        binding.seeAnswerBtn.setVisibility(View.GONE);
    }

    private void seeAnswer() {
        if (CommonValues.mRewardedAd != null) {
            CommonValues.mRewardedAd.show(getActivity(), rewardItem -> {
//                CommonValues.mRewardedAd = null;
//                loadRewardedAd();
                binding.hintTv.setVisibility(View.VISIBLE);
                binding.hintTv.setText("Wordle is - " + answer);
                binding.restartGameBtn.setVisibility(View.GONE);
            });
        }
    }

    private void submitWord() {
        isEnterEnabled = false;
        ArrayList<String> list = new ArrayList<>();
        currentWord = getWord();
        boolean isWordCorrect = sessionManager.isWordCorrect(currentWord);
        if (isWordCorrect) {
            for (int i = 0; i < currentWord.length(); i++) {
                char letter = currentWord.charAt(i);
                String s = letter + "";
                list.add(s);
            }
            wordleLogic(list);
            isEnterEnabled = true;
        }
//        else {
//            noWordAnimation();
//        }
    }

//    private void noWordAnimation() {
//        Handler handler = new Handler();
//        handler.postDelayed(() -> {
//            ObjectAnimator animation = new ObjectAnimator();
//            if (row == 1) {
//                animation = ObjectAnimator.ofFloat(binding.row1, "translationX", 100f);
//            } else if (row == 2) {
//                animation = ObjectAnimator.ofFloat(binding.row2, "translationX", 100f);
//            } else if (row == 3) {
//                animation = ObjectAnimator.ofFloat(binding.row3, "translationX", 100f);
//            } else if (row == 4) {
//                animation = ObjectAnimator.ofFloat(binding.row4, "translationX", 100f);
//            } else if (row == 5) {
//                animation = ObjectAnimator.ofFloat(binding.row5, "translationX", 100f);
//            } else if (row == 6) {
//                animation = ObjectAnimator.ofFloat(binding.row6, "translationX", 100f);
//            }
//            animation.setDuration(100);
//            animation.start();
//        }, 100);
//
//        handler.postDelayed(() -> {
//            ObjectAnimator animation = new ObjectAnimator();
//            if (row == 1) {
//                animation = ObjectAnimator.ofFloat(binding.row1, "translationX", -100f);
//            } else if (row == 2) {
//                animation = ObjectAnimator.ofFloat(binding.row2, "translationX", -100f);
//            } else if (row == 3) {
//                animation = ObjectAnimator.ofFloat(binding.row3, "translationX", -100f);
//            } else if (row == 4) {
//                animation = ObjectAnimator.ofFloat(binding.row4, "translationX", -100f);
//            } else if (row == 5) {
//                animation = ObjectAnimator.ofFloat(binding.row5, "translationX", -100f);
//            } else if (row == 6) {
//                animation = ObjectAnimator.ofFloat(binding.row6, "translationX", -100f);
//            }
//            animation.setDuration(100);
//            animation.start();
//        }, 200);
//
//        handler.postDelayed(() -> {
//            ObjectAnimator animation = new ObjectAnimator();
//            if (row == 1) {
//                animation = ObjectAnimator.ofFloat(binding.row1, "translationX", 0f);
//            } else if (row == 2) {
//                animation = ObjectAnimator.ofFloat(binding.row2, "translationX", 0f);
//            } else if (row == 3) {
//                animation = ObjectAnimator.ofFloat(binding.row3, "translationX", 0f);
//            } else if (row == 4) {
//                animation = ObjectAnimator.ofFloat(binding.row4, "translationX", 0f);
//            } else if (row == 5) {
//                animation = ObjectAnimator.ofFloat(binding.row5, "translationX", 0f);
//            } else if (row == 6) {
//                animation = ObjectAnimator.ofFloat(binding.row6, "translationX", 0f);
//            }
//            animation.setDuration(100);
//            animation.start();
//            isEnterEnabled = true;
//        }, 300);
//        vibrator.vibrate(300);
//        showToast("Not in word list");
//    }

    private <T> boolean containsAny(ArrayList<T> l1, ArrayList<T> l2) {
        for (T elem : l1) {
            if (l2.contains(elem)) {
                return true;
            }
        }
        return false;
    }

    private void wordleLogicForPreviousGame(ArrayList<String> lettersList) {
        binding.gameFragment.setEnabled(false);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        for (int i = lettersList.size() - 1; i >= 0; i--) {
            int count = 0;
            int nums = 0;
            ArrayList<Integer> indexes = new ArrayList<>();
            ArrayList<Integer> actualIndexes = new ArrayList<>();
            for (int j = lettersList.size() - 1; j >= 0; j--) {
                if (lettersList.get(i).equalsIgnoreCase(answer.charAt(j) + "")) {
                    count++;
                    actualIndexes.add(j);
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
        if (answer.contains(lettersList.get(0))) {
            String newLetter = answer.charAt(0) + "";

            if (lettersList.get(0).equals(newLetter)) {
                makeAnimation(1);
                correctCol.set(0, true);
            } else {
                makeHasAnimation(1);
            }
        } else {
            makeWrongAnimation(1);
        }

        if (answer.contains(lettersList.get(1))) {
            String newLetter = answer.charAt(1) + "";

            if (lettersList.get(1).equals(newLetter)) {
                makeAnimation(2);
                correctCol.set(1, true);
            } else {
                makeHasAnimation(2);
            }
        } else {
            makeWrongAnimation(2);
        }

        if (answer.contains(lettersList.get(2))) {
            String newLetter = answer.charAt(2) + "";

            if (lettersList.get(2).equals(newLetter)) {
                makeAnimation(3);
                correctCol.set(2, true);
            } else {
                makeHasAnimation(3);
            }
        } else {
            makeWrongAnimation(3);
        }

        if (answer.contains(lettersList.get(3))) {
            String newLetter = answer.charAt(3) + "";

            if (lettersList.get(3).equals(newLetter)) {
                makeAnimation(4);
                correctCol.set(3, true);
            } else {
                makeHasAnimation(4);
            }
        } else {
            makeWrongAnimation(4);
        }

        if (answer.contains(lettersList.get(4))) {
            String newLetter = answer.charAt(4) + "";

            if (lettersList.get(4).equals(newLetter)) {
                makeAnimation(5);
                correctCol.set(4, true);
            } else {
                makeHasAnimation(5);
            }
        } else {
            makeWrongAnimation(5);
        }
        setButtonsBackground(lettersList);
    }

    private void wordleLogic(ArrayList<String> lettersList) {
        binding.gameFragment.setEnabled(false);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        int time = 260;
//        for (int i = 0; i < list.size(); i++) {
//            int count = 0;
//            int nums = 0;
//            for (int j = 0; j < list.size(); j++) {
//                if (list.get(i).equalsIgnoreCase(answer.charAt(j)+"")) {
//                    count++;
//                }
//            }
//            for (int j = 0; j < list.size(); j++) {
//                if (list.get(i).equalsIgnoreCase(list.get(j))) {
//                    nums++;
//                }
//            }
//
//            if (nums == 1) {
//                nums = 0;
//            }
//            if (nums > count) {
//                int diff = nums - count;
//                String letter = list.get(i);
//                if(diff == 1) {
//                    list.set(i, "-");
//                } else {
//                    for (int j = 0; j < list.size(); j++) {
//                        if (diff > 1) {
//                            if(letter.equalsIgnoreCase(list.get(j))) {
//                                list.set(j, "-");
//                                diff--;
//                            }
//                        }
//                    }
//                }
//            }
//        }
        for (int i = lettersList.size() - 1; i >= 0; i--) {
            int count = 0;
            int nums = 0;
            ArrayList<Integer> indexes = new ArrayList<>();
            ArrayList<Integer> actualIndexes = new ArrayList<>();
            for (int j = lettersList.size() - 1; j >= 0; j--) {
                if (lettersList.get(i).equalsIgnoreCase(answer.charAt(j) + "")) {
                    count++;
                    actualIndexes.add(j);
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
                String newLetter = answer.charAt(0) + "";

                if (lettersList.get(0).equals(newLetter)) {
                    makeAnimation(1);
                    correctCol.set(0, true);
                } else {
                    makeHasAnimation(1);
                }
            } else {
                makeWrongAnimation(1);
            }
        }, time);

        handler.postDelayed(() -> {
            if (answer.contains(lettersList.get(1))) {
                String newLetter = answer.charAt(1) + "";

                if (lettersList.get(1).equals(newLetter)) {
                    makeAnimation(2);
                    correctCol.set(1, true);
                } else {
                    makeHasAnimation(2);
                }
            } else {
                makeWrongAnimation(2);
            }
        }, time * 2);

        handler.postDelayed(() -> {
            if (answer.contains(lettersList.get(2))) {
                String newLetter = answer.charAt(2) + "";

                if (lettersList.get(2).equals(newLetter)) {
                    makeAnimation(3);
                    correctCol.set(2, true);
                } else {
                    makeHasAnimation(3);
                }
            } else {
                makeWrongAnimation(3);
            }
        }, time * 3);

        handler.postDelayed(() -> {
            if (answer.contains(lettersList.get(3))) {
                String newLetter = answer.charAt(3) + "";

                if (lettersList.get(3).equals(newLetter)) {
                    makeAnimation(4);
                    correctCol.set(3, true);
                } else {
                    makeHasAnimation(4);
                }
            } else {
                makeWrongAnimation(4);
            }
        }, time * 4);

        handler.postDelayed(() -> {
            if (answer.contains(lettersList.get(4))) {
                String newLetter = answer.charAt(4) + "";

                if (lettersList.get(4).equals(newLetter)) {
                    makeAnimation(5);
                    correctCol.set(4, true);
                } else {
                    makeHasAnimation(5);
                }
            } else {
                makeWrongAnimation(5);
            }
            setButtonsBackground(lettersList);
        }, time * 5);
    }

    private void setButtonsBackground(ArrayList<String> list) {
        for (int i = 0; i < list.size(); i++) {
            String answerChar = answer.charAt(i) + "";
            if (list.get(i).equalsIgnoreCase("Q")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnQ.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnQ.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnQ.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("W")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnW.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnW.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnW.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("E")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnE.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnE.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnE.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("R")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnR.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnR.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnR.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("T")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnT.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnT.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnT.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("Y")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnY.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnY.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnY.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("U")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnU.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnU.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnU.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("I")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnI.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnI.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnI.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("O")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnO.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnO.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnO.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("P")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnP.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnP.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnP.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("A")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnA.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnA.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnA.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("S")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnS.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnS.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnS.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("D")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnD.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnD.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnD.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("F")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnF.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnF.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnF.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("G")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnG.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnG.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnG.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("H")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnH.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnH.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnH.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("J")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnJ.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnJ.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnJ.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("K")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnK.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnK.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnK.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("L")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnL.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnL.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnL.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("Z")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnZ.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnZ.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnZ.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("X")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnX.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnX.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnX.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("C")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnC.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnC.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnC.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("V")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnV.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnV.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnV.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("B")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnB.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnB.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnB.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("N")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnN.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnN.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnN.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            } else if (list.get(i).equalsIgnoreCase("M")) {
                if (list.get(i).equalsIgnoreCase(answerChar)) {
                    binding.btnM.setBackgroundResource(R.drawable.keyboard_correct_bg);
                } else if (answer.contains(list.get(i))) {
                    binding.btnM.setBackgroundResource(R.drawable.keyboard_has_bg);
                } else {
                    binding.btnM.setBackgroundResource(R.drawable.keyboard_wrong_bg);
                }
            }
        }
        row++;
        current = 1;
    }

    private void makeWrongAnimation(int index) {
        if (row == 1) {
            if (index == 1) {
                binding.row11.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row11.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row11.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 2) {
                binding.row12.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row12.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row12.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 3) {
                binding.row13.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row13.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row13.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 4) {
                binding.row14.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row14.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row14.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 5) {
                binding.row15.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    binding.row15.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row15.animate().alpha(1f).setDuration(250);
                }, 250);
                setDataInDB(1);
            }
        } else if (row == 2) {
            if (index == 1) {
                binding.row21.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row21.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row21.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 2) {
                binding.row22.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row22.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row22.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 3) {
                binding.row23.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row23.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row23.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 4) {
                binding.row24.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row24.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row24.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 5) {
                binding.row25.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    binding.row25.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row25.animate().alpha(1f).setDuration(250);
                }, 250);
                setDataInDB(2);
            }
        } else if (row == 3) {
            if (index == 1) {
                binding.row31.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row31.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row31.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 2) {
                binding.row32.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row32.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row32.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 3) {
                binding.row33.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row33.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row33.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 4) {
                binding.row34.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row34.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row34.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 5) {
                binding.row35.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


                    binding.row35.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row35.animate().alpha(1f).setDuration(250);
                }, 250);
                setDataInDB(3);
            }
        } else if (row == 4) {
            if (index == 1) {
                binding.row41.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row41.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row41.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 2) {
                binding.row42.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row42.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row42.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 3) {
                binding.row43.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row43.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row43.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 4) {
                binding.row44.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row44.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row44.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 5) {
                binding.row45.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


                    binding.row45.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row45.animate().alpha(1f).setDuration(250);
                }, 250);
                setDataInDB(4);
            }
        } else if (row == 5) {
            if (index == 1) {
                binding.row51.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row51.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row51.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 2) {
                binding.row52.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row52.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row52.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 3) {
                binding.row53.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row53.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row53.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 4) {
                binding.row54.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row54.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row54.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 5) {
                binding.row55.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    binding.row55.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row55.animate().alpha(1f).setDuration(250);
                }, 250);
                setDataInDB(5);
            }
        } else if (row == 6) {
            if (index == 1) {
                binding.row61.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row61.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row61.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 2) {
                binding.row62.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row62.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row62.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 3) {
                binding.row63.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row63.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row63.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 4) {
                binding.row64.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row64.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                    binding.row64.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 5) {
                binding.row65.animate().alpha(0f).setDuration(250);
                if (gameMode.equalsIgnoreCase(multi)) {
                    Handler handler = new Handler();
                    handler.postDelayed(() -> {
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        binding.lose.setVisibility(View.VISIBLE);
                        binding.row65.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                        binding.row65.animate().alpha(1f).setDuration(250);
                        Handler handler1 = new Handler();
                        handler1.postDelayed(() -> setMuliplayerLost(), 500);
                    }, 250);
                } else {
                    Handler handler = new Handler();
                    handler.postDelayed(() -> {
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        binding.lose.setVisibility(View.VISIBLE);
                        binding.row65.setBackgroundResource(R.drawable.alphabets_wrong_bg);
                        binding.row65.animate().alpha(1f).setDuration(250);
                        showLostGameViews();
                    }, 250);
                }
                setDataInDB(6);
            }
        }
    }

    private void makeHasAnimation(int index) {
        if (row == 1) {
            if (index == 1) {
                binding.row11.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row11.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row11.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 2) {
                binding.row12.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row12.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row12.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 3) {
                binding.row13.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row13.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row13.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 4) {
                binding.row14.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row14.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row14.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 5) {
                binding.row15.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


                    binding.row15.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row15.animate().alpha(1f).setDuration(250);
                }, 250);
                setDataInDB(1);
            }
        } else if (row == 2) {
            if (index == 1) {
                binding.row21.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row21.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row21.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 2) {
                binding.row22.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row22.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row22.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 3) {
                binding.row23.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row23.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row23.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 4) {
                binding.row24.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row24.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row24.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 5) {
                binding.row25.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


                    binding.row25.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row25.animate().alpha(1f).setDuration(250);
                }, 250);
                setDataInDB(2);
            }
        } else if (row == 3) {
            if (index == 1) {
                binding.row31.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row31.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row31.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 2) {
                binding.row32.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row32.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row32.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 3) {
                binding.row33.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row33.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row33.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 4) {
                binding.row34.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row34.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row34.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 5) {
                binding.row35.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


                    binding.row35.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row35.animate().alpha(1f).setDuration(250);
                }, 250);
                setDataInDB(3);
            }
        } else if (row == 4) {
            if (index == 1) {
                binding.row41.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row41.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row41.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 2) {
                binding.row42.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row42.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row42.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 3) {
                binding.row43.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row43.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row43.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 4) {
                binding.row44.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row44.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row44.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 5) {
                binding.row45.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


                    binding.row45.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row45.animate().alpha(1f).setDuration(250);
                }, 250);
                setDataInDB(4);
            }
        } else if (row == 5) {
            if (index == 1) {
                binding.row51.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row51.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row51.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 2) {
                binding.row52.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row52.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row52.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 3) {
                binding.row53.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row53.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row53.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 4) {
                binding.row54.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row54.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row54.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 5) {
                binding.row55.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


                    binding.row55.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row55.animate().alpha(1f).setDuration(250);
                }, 250);
                setDataInDB(5);
            }
        } else if (row == 6) {
            if (index == 1) {
                binding.row61.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row61.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row61.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 2) {
                binding.row62.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row62.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row62.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 3) {
                binding.row63.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row63.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row63.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 4) {
                binding.row64.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row64.setBackgroundResource(R.drawable.alphabets_has_bg);
                    binding.row64.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 5) {
                binding.row65.animate().alpha(0f).setDuration(250);
                if (gameMode.equalsIgnoreCase(multi)) {
                    Handler handler = new Handler();
                    handler.postDelayed(() -> {
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        binding.row65.setBackgroundResource(R.drawable.alphabets_has_bg);
                        binding.row65.animate().alpha(1f).setDuration(250);
                        binding.lose.setVisibility(View.VISIBLE);
                        Handler handler1 = new Handler();
                        handler1.postDelayed(() -> setMuliplayerLost(), 500);
                    }, 250);
                } else {
                    Handler handler = new Handler();
                    handler.postDelayed(() -> {
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        binding.lose.setVisibility(View.VISIBLE);
                        binding.row65.setBackgroundResource(R.drawable.alphabets_has_bg);
                        binding.row65.animate().alpha(1f).setDuration(250);
                        showLostGameViews();
                    }, 250);
                }
                setDataInDB(6);
            }
        }
    }

    private String getWord() {
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

    private void makeAnimation(int index) {
        if (currentWord == null) {
            currentWord = getWord();
        }
        if (row == 1) {
            if (index == 1) {
                binding.row11.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row11.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row11.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 2) {
                binding.row12.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row12.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row12.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 3) {
                binding.row13.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row13.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row13.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 4) {
                binding.row14.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row14.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row14.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 5) {
                binding.row15.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    index5("row1");
                    binding.row15.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row15.animate().alpha(1f).setDuration(250);
                }, 250);
                setDataInDB(1);
            }
        } else if (row == 2) {
            if (index == 1) {
                binding.row21.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row21.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row21.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 2) {
                binding.row22.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row22.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row22.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 3) {
                binding.row23.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row23.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row23.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 4) {
                binding.row24.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row24.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row24.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 5) {
                binding.row25.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    index5("row2");
                    binding.row25.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row25.animate().alpha(1f).setDuration(250);
                }, 250);
                setDataInDB(2);
            }
        } else if (row == 3) {
            if (index == 1) {
                binding.row31.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row31.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row31.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 2) {
                binding.row32.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row32.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row32.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 3) {
                binding.row33.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row33.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row33.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 4) {
                binding.row34.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row34.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row34.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 5) {
                binding.row35.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    index5("row3");
                    binding.row35.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row35.animate().alpha(1f).setDuration(250);
                }, 250);
                setDataInDB(3);
            }
        } else if (row == 4) {
            if (index == 1) {
                binding.row41.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row41.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row41.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 2) {
                binding.row42.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row42.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row42.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 3) {
                binding.row43.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row43.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row43.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 4) {
                binding.row44.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row44.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row44.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 5) {
                binding.row45.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    index5("row4");
                    binding.row45.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row45.animate().alpha(1f).setDuration(250);
                }, 250);
                setDataInDB(4);
            }
        } else if (row == 5) {
            if (index == 1) {
                binding.row51.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row51.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row51.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 2) {
                binding.row52.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row52.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row52.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 3) {
                binding.row53.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row53.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row53.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 4) {
                binding.row54.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row54.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row54.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 5) {
                binding.row55.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    index5("row5");
                    binding.row55.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row55.animate().alpha(1f).setDuration(250);
                }, 250);
                setDataInDB(5);
            }
        } else if (row == 6) {
            if (index == 1) {
                binding.row61.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row61.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row61.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 2) {
                binding.row62.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row62.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row62.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 3) {
                binding.row63.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row63.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row63.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 4) {
                binding.row64.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    binding.row64.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row64.animate().alpha(1f).setDuration(250);
                }, 250);
            } else if (index == 5) {
                binding.row65.animate().alpha(0f).setDuration(250);
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    index5("row6");
                    binding.row65.setBackgroundResource(R.drawable.alphabets_correct_bg);
                    binding.row65.animate().alpha(1f).setDuration(250);
                }, 250);
                setDataInDB(6);
            }
        }
    }

    private void index5(String rowLocal) {
        if (currentWord.equalsIgnoreCase(answer)) {
            if (!gameMode.equalsIgnoreCase(multi)) {
                dbHandler.dropTable(gameMode);
                sessionManager.clearGameModeSession(gameMode);
                binding.victory.setVisibility(View.VISIBLE);
            }
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            binding.gameFragment.setEnabled(false);
            if (gameMode.equalsIgnoreCase(daily)) {
                databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("User Details").child(userId).child(daily).child(currentDate);
                Map setValues = new HashMap();
                setValues.put(currentDate, "done");
                databaseReference.updateChildren(setValues);

                databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("User Details").child(userId).child("GameData").child(daily);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {
                            };
                            Map<String, Object> map = dataSnapshot.getValue(genericTypeIndicator);
                            int score = 0;
                            int totalPlayed = 0;
//                            int currentStreak = 0;
//                            int maxStreak = 0;

                            if (map.containsKey(rowLocal)) {
                                score = Integer.parseInt((String) map.get(rowLocal));
                            }
                            if (map.containsKey("totalPlayed")) {
                                totalPlayed = Integer.parseInt((String) map.get("totalPlayed"));
                            }
//                            if (map.containsKey("currentStreak")) {
//                                currentStreak = Integer.parseInt((String) map.get("currentStreak"));
//                            }
//                            if (map.containsKey("maxStreak")) {
//                                maxStreak = Integer.parseInt((String) map.get("maxStreak"));
//                            }

                            Map setValues1 = new HashMap();
                            setValues1.put(rowLocal, score + 1 + "");
                            setValues1.put("totalPlayed", totalPlayed + 1 + "");
//                            setValues1.put("currentStreak", currentStreak + 1 + "");
//                            setValues1.put("maxStreak", maxStreak + 1 + "");
                            databaseReference.updateChildren(setValues1);
                        } else {
                            Map setValues1 = new HashMap();
                            setValues1.put(rowLocal, "1");
                            setValues1.put("totalPlayed", "1");
//                            setValues1.put("currentStreak", "1");
//                            setValues1.put("maxStreak", "1");
                            databaseReference.updateChildren(setValues1);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                Handler handler1 = new Handler();
                handler1.postDelayed(() -> {
                    if (CommonValues.currentFragment.equalsIgnoreCase(CommonValues.gameFragment)) {
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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
                    }
                }, 5000);

            } else if (gameMode.equalsIgnoreCase(classic)) {
                databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("User Details").child(userId).child(classic).child(currentDate);
                Map setValues = new HashMap();
                setValues.put(wordInDB + wordId, "done");
                databaseReference.updateChildren(setValues);

                databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("User Details").child(userId).child("GameData").child(classic);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {
                            };
                            Map<String, Object> map = dataSnapshot.getValue(genericTypeIndicator);
                            int score = 0;
                            int totalPlayed = 0;
//                            int currentStreak = 0;
//                            int maxStreak = 0;

                            if (map.containsKey(rowLocal)) {
                                score = Integer.parseInt((String) map.get(rowLocal));
                            }
                            if (map.containsKey("totalPlayed")) {
                                totalPlayed = Integer.parseInt((String) map.get("totalPlayed"));
                            }
//                            if (map.containsKey("currentStreak")) {
//                                currentStreak = Integer.parseInt((String) map.get("currentStreak"));
//                            }
//                            if (map.containsKey("maxStreak")) {
//                                maxStreak = Integer.parseInt((String) map.get("maxStreak"));
//                            }
                            Map setValues1 = new HashMap();
                            setValues1.put(rowLocal, score + 1 + "");
                            setValues1.put("totalPlayed", totalPlayed + 1 + "");
//                            setValues1.put("currentStreak", currentStreak + 1 + "");
//                            setValues1.put("maxStreak", maxStreak + 1 + "");
                            databaseReference.updateChildren(setValues1);
                        } else {
                            Map setValues1 = new HashMap();
                            setValues1.put(rowLocal, "1");
                            setValues1.put("totalPlayed", "1");
//                            setValues1.put("currentStreak", "1");
//                            setValues1.put("maxStreak", "1");
                            databaseReference.updateChildren(setValues1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                Handler handler1 = new Handler();
                handler1.postDelayed(() -> {
                    if (CommonValues.currentFragment.equalsIgnoreCase(CommonValues.gameFragment)) {
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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
                    }
                }, 5000);
            } else if (gameMode.equalsIgnoreCase(multi)) {
                databaseReference = FirebaseDatabase.getInstance().getReference().child("Wordle").child("Rooms").child(CommonValues.roomDate).child(roomId);
                Map setValues = new HashMap();
                setValues.put("Lobby Status", "Result");
                setValues.put("WinnerId", userId);
                setValues.put("WinnerName", sessionManager.getStringKey(Params.KEY_USER_NAME));
                databaseReference.updateChildren(setValues);
            }
        }
    }

    private void setDataInDB(Integer row) {
        if (!gameMode.equalsIgnoreCase(multi)) {
            if (gameMode.equalsIgnoreCase(classic)) {
                sessionManager.addBooleanKey(Params.KEY_IS_PREVIOUS_CLASSIC_GAME, true);
            } else {
                sessionManager.addBooleanKey(Params.KEY_IS_PREVIOUS_DAILY_GAME, true);
            }
            String letter1 = "", letter2 = "", letter3 = "", letter4 = "", letter5 = "";

            if (row == 1) {
                letter1 = binding.row11.getText().toString().toUpperCase().trim();
                letter2 = binding.row12.getText().toString().toUpperCase().trim();
                letter3 = binding.row13.getText().toString().toUpperCase().trim();
                letter4 = binding.row14.getText().toString().toUpperCase().trim();
                letter5 = binding.row15.getText().toString().toUpperCase().trim();
                if (gameMode.equalsIgnoreCase(classic)) {
                    sessionManager.addStringKey(Params.KEY_LAST_CLASSIC_ROW, "1");
                } else {
                    sessionManager.addStringKey(Params.KEY_LAST_DAILY_ROW, "1");
                }
            } else if (row == 2) {
                letter1 = binding.row21.getText().toString().toUpperCase().trim();
                letter2 = binding.row22.getText().toString().toUpperCase().trim();
                letter3 = binding.row23.getText().toString().toUpperCase().trim();
                letter4 = binding.row24.getText().toString().toUpperCase().trim();
                letter5 = binding.row25.getText().toString().toUpperCase().trim();
                if (gameMode.equalsIgnoreCase(classic)) {
                    sessionManager.addStringKey(Params.KEY_LAST_CLASSIC_ROW, "2");
                } else {
                    sessionManager.addStringKey(Params.KEY_LAST_DAILY_ROW, "2");
                }
            } else if (row == 3) {
                letter1 = binding.row31.getText().toString().toUpperCase().trim();
                letter2 = binding.row32.getText().toString().toUpperCase().trim();
                letter3 = binding.row33.getText().toString().toUpperCase().trim();
                letter4 = binding.row34.getText().toString().toUpperCase().trim();
                letter5 = binding.row35.getText().toString().toUpperCase().trim();
                if (gameMode.equalsIgnoreCase(classic)) {
                    sessionManager.addStringKey(Params.KEY_LAST_CLASSIC_ROW, "3");
                } else {
                    sessionManager.addStringKey(Params.KEY_LAST_DAILY_ROW, "3");
                }
            } else if (row == 4) {
                letter1 = binding.row41.getText().toString().toUpperCase().trim();
                letter2 = binding.row42.getText().toString().toUpperCase().trim();
                letter3 = binding.row43.getText().toString().toUpperCase().trim();
                letter4 = binding.row44.getText().toString().toUpperCase().trim();
                letter5 = binding.row45.getText().toString().toUpperCase().trim();
                if (gameMode.equalsIgnoreCase(classic)) {
                    sessionManager.addStringKey(Params.KEY_LAST_CLASSIC_ROW, "4");
                } else {
                    sessionManager.addStringKey(Params.KEY_LAST_DAILY_ROW, "4");
                }
            } else if (row == 5) {
                letter1 = binding.row51.getText().toString().toUpperCase().trim();
                letter2 = binding.row52.getText().toString().toUpperCase().trim();
                letter3 = binding.row53.getText().toString().toUpperCase().trim();
                letter4 = binding.row54.getText().toString().toUpperCase().trim();
                letter5 = binding.row55.getText().toString().toUpperCase().trim();
                if (gameMode.equalsIgnoreCase(classic)) {
                    sessionManager.addStringKey(Params.KEY_LAST_CLASSIC_ROW, "5");
                } else {
                    sessionManager.addStringKey(Params.KEY_LAST_DAILY_ROW, "5");
                }
            } else if (row == 6) {
                letter1 = binding.row61.getText().toString().toUpperCase().trim();
                letter2 = binding.row62.getText().toString().toUpperCase().trim();
                letter3 = binding.row63.getText().toString().toUpperCase().trim();
                letter4 = binding.row64.getText().toString().toUpperCase().trim();
                letter5 = binding.row65.getText().toString().toUpperCase().trim();

                if (gameMode.equalsIgnoreCase(classic)) {
                    sessionManager.addStringKey(Params.KEY_LAST_CLASSIC_ROW, "6");
                    sessionManager.addBooleanKey(Params.KEY_IS_PREVIOUS_CLASSIC_GAME, false);
                } else {
                    sessionManager.addStringKey(Params.KEY_LAST_DAILY_ROW, "6");
                    sessionManager.addBooleanKey(Params.KEY_IS_PREVIOUS_DAILY_GAME, false);
                }
                dbHandler.dropTable(gameMode);
            }
            if (gameMode.equalsIgnoreCase(classic)) {
                sessionManager.addStringKey(Params.KEY_LAST_CLASSIC_ANSWER, answer);
            } else {
                sessionManager.addStringKey(Params.KEY_LAST_DAILY_ANSWER, answer);
            }

            sessionManager.addStringKey(Params.KEY_LAST_GAME_MODE, gameMode);

            dbHandler.addRow(row, letter1, letter2, letter3, letter4, letter5, gameMode);
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

    private void setCharInView(String alphabet) {
        if (row == 1) {
            if (current == 1) {
                binding.row11.setText(alphabet);
            } else if (current == 2) {
                binding.row12.setText(alphabet);
            } else if (current == 3) {
                binding.row13.setText(alphabet);
            } else if (current == 4) {
                binding.row14.setText(alphabet);
            } else if (current == 5) {
                binding.row15.setText(alphabet);
            } else {
                return;
            }
            current++;
        } else if (row == 2) {
            if (current == 1) {
                binding.row21.setText(alphabet);
            } else if (current == 2) {
                binding.row22.setText(alphabet);
            } else if (current == 3) {
                binding.row23.setText(alphabet);
            } else if (current == 4) {
                binding.row24.setText(alphabet);
            } else if (current == 5) {
                binding.row25.setText(alphabet);
            } else {
                return;
            }
            current++;
        } else if (row == 3) {
            if (current == 1) {
                binding.row31.setText(alphabet);
            } else if (current == 2) {
                binding.row32.setText(alphabet);
            } else if (current == 3) {
                binding.row33.setText(alphabet);
            } else if (current == 4) {
                binding.row34.setText(alphabet);
            } else if (current == 5) {
                binding.row35.setText(alphabet);
            } else {
                return;
            }
            current++;
        } else if (row == 4) {
            if (current == 1) {
                binding.row41.setText(alphabet);
            } else if (current == 2) {
                binding.row42.setText(alphabet);
            } else if (current == 3) {
                binding.row43.setText(alphabet);
            } else if (current == 4) {
                binding.row44.setText(alphabet);
            } else if (current == 5) {
                binding.row45.setText(alphabet);
            } else {
                return;
            }
            current++;
        } else if (row == 5) {
            if (current == 1) {
                binding.row51.setText(alphabet);
            } else if (current == 2) {
                binding.row52.setText(alphabet);
            } else if (current == 3) {
                binding.row53.setText(alphabet);
            } else if (current == 4) {
                binding.row54.setText(alphabet);
            } else if (current == 5) {
                binding.row55.setText(alphabet);
            } else {
                return;
            }
            current++;
        } else if (row == 6) {
            if (current == 1) {
                binding.row61.setText(alphabet);
            } else if (current == 2) {
                binding.row62.setText(alphabet);
            } else if (current == 3) {
                binding.row63.setText(alphabet);
            } else if (current == 4) {
                binding.row64.setText(alphabet);
            } else if (current == 5) {
                binding.row65.setText(alphabet);
            } else {
                return;
            }
            current++;
        }
    }

    private void showToast(String msg) {
        showToast(msg, getContext(), getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        CommonValues.currentFragment = CommonValues.gameFragment;
    }
}
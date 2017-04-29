package com.example.aloom.neonshuffleboardv2;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;


import java.util.HashMap;


//Intent is to produce a realistic simulation of a fling on the puck object for the Neon ShuffleBoard game
public class NSBGameActivity extends AppCompatActivity {
    private final String TAG = "PuckAnimatorActivity";
    AnimatorSet animatePuckProperties;
    AnimatorSet auxAnimatePuckProperties;
    AnimatorSet animatePuckArray[] = new AnimatorSet[6];
    ConstraintLayout gameView;
    float flingDistance;
    float downXPOS;
    float downYPOS;
    float upXPOS;
    float upYPOS;
    float xVelocity;
    float yVelocity;
    float dx;
    float dy;
    int puckClock = 0;
    int animationClock = 0; //keeps track of end of animations
    int maxDuration = 35000;
    long cancelDuration = 1000; //This value actually ends the animation after x milliseconds.
    ImageView redPuck1;
    ImageView redPuck2;
    ImageView redPuck3;
    ImageView bluePuck1;
    ImageView bluePuck2;
    ImageView bluePuck3;
    //standard game mode is 15 points
    int maxScore = 15;
    float spawnY, spawnX; //Stuff I added
    boolean isSpawnCheck = false;
    Integer redPlayerScore = 0;
    Integer bluePlayerScore = 0;
    boolean maxScoreReached = false;
    View.OnTouchListener puckListener;
    GestureDetector detector;
    ImageView[] puckCycleList;
    TextSwitcher blueSwitcher;
    TextSwitcher redSwitcher;
    TextView blueScoreText;
    TextView redScoreText;
    Handler myHandler = new Handler(); //Thing I added.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nsbgame);
        redPuck1 = (ImageView) findViewById(R.id.redPuck1);
        redPuck2 = (ImageView) findViewById(R.id.redPuck2);
        redPuck3 = (ImageView) findViewById(R.id.redPuck3);
        bluePuck1 = (ImageView) findViewById(R.id.bluePuck1);
        bluePuck2 = (ImageView) findViewById(R.id.bluePuck2);
        bluePuck3 = (ImageView) findViewById(R.id.bluePuck3);
        puckCycleList = new ImageView[]{redPuck1, redPuck2, redPuck3, bluePuck1, bluePuck2, bluePuck3};
        setupInitialPuckVisibility();
        gameView = (ConstraintLayout) findViewById(R.id.nsbGame);
        detector = new GestureDetector(this, new CustomGestureListener());
        blueSwitcher = (TextSwitcher) findViewById(R.id.blueScoreSwitcher);
        redSwitcher = (TextSwitcher) findViewById(R.id.redScoreSwitcher);
        blueScoreText = (TextView) findViewById(R.id.blueScoreText);
        redScoreText = (TextView) findViewById(R.id.redScoreText);
        blueScoreText.setText("0");
        redScoreText.setText("0");
        blueScoreText.setTextSize(1, 60f);
        redScoreText.setTextSize(1, 60f);
        listenForTouchOnPuck();
        //Round cycle
        redPuck1.setClickable(true);
        playGame();
        // getPuckSpawn(); // Doesn't work
    }


    public void  getPuckSpawn() {
        spawnY = puckCycleList[0].getY();
        spawnX = puckCycleList[0].getX();
        Log.d(TAG, "YOOOOOOOO y pos: " + puckCycleList[0].getY());
        Log.d(TAG, "initial x pos: " + puckCycleList[0].getX());
        isSpawnCheck = true;
    }
    public void calculateDistance() {
        Log.d(TAG, "initial x pos: " + downXPOS);
        Log.d(TAG, "initial y pos: " + downYPOS);
        flingDistance = (float) Math.sqrt((dx * dx) + (dy * dy));
    }

    public void waitTime(int milliseconds) {
        try {
            wait(milliseconds);
        } catch (Exception e) {
            Log.d("Wait Method: ", "Cause: " + e.getCause());
        }
    }

    private void playSound(int soundID) {
        MediaPlayer soundPlayer = MediaPlayer.create(this, soundID);
        soundPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        soundPlayer.start();

        ObjectAnimator animY = ObjectAnimator.ofFloat(bluePuck1, View.Y, bluePuck1.getY());
    }

    //exclude first puck
    public void setupInitialPuckVisibility() {
        redPuck2.setVisibility(View.GONE);
        redPuck3.setVisibility(View.GONE);
        bluePuck1.setVisibility(View.GONE);
        bluePuck2.setVisibility(View.GONE);
        bluePuck3.setVisibility(View.GONE);
    }

    public void listenForTouchOnPuck() {
        puckListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return detector.onTouchEvent(event);
            }
        };
    }

    public synchronized void playGame() {
        if (puckClock == 0) {
            puckCycleList[puckClock].setOnTouchListener(puckListener);
            puckCycleList[puckClock].setClickable(true);
        }
        else {
            puckCycleList[puckClock].setOnTouchListener(puckListener);
            puckCycleList[puckClock].setClickable(true);
            puckCycleList[puckClock - 1].setClickable(false);
        }
    }

    public void resetPuckPositions() {
        //All values were calculated manually

    }

    class CustomGestureListener implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }
        @Override
        public void onShowPress(MotionEvent e) {
        }
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }
        @Override
        public void onLongPress(MotionEvent e) {
        }
        @Override
        public boolean onFling(MotionEvent fingerDown, MotionEvent move, float velocityX, float velocityY) {
            if (isSpawnCheck == false) {
                getPuckSpawn(); }   //I had to call the function here b/c if I put getPuckSpawn() in onCreate the layout for some reason loads after onCreate, and the coordinats are x,y = 0,0. This is a temporary solution, no significant drawbacks but its not fitting to call this function in the fling method.
            downXPOS = fingerDown.getX();
            downYPOS = fingerDown.getY();
            Log.d(TAG, "XPOS finger down:  " + downXPOS);
            Log.d(TAG, "YPOS finger down:  " + downYPOS);
            Log.d(TAG, "puck left x: " + puckCycleList[puckClock].getLeft());
            Log.d(TAG, "puck right x: " + puckCycleList[puckClock].getRight());
            Log.d(TAG, "puck top y: " + puckCycleList[puckClock].getTop());
            Log.d(TAG, "puck bottom y : " + puckCycleList[puckClock].getBottom());
            Log.i(TAG, "____FLING INITIATED____");
            upXPOS = move.getX();
            upYPOS = move.getY();
            dx = (upXPOS - downXPOS);
            dy = (upYPOS - downYPOS);
            xVelocity = (float) (velocityX * .00035);
            yVelocity = (float) (velocityY * .00035);
            Log.d(TAG, "End X POS:  " + upXPOS);
            Log.d(TAG, "End Y POS:  " + upYPOS);
            Log.d(TAG, "Velocity X:  " + velocityX);
            Log.d(TAG, "Velocity Y:  " + velocityY);
            calculateDistance();
            animatePuck();
            Log.d(TAG, "------------------BEFORE DEANIM.----------------!!!");
            deanimatePuck();
            Log.d(TAG, "-----------------AFTER DEANIM.---------------!!");
            puckClock++;

            if (puckClock < 5) { //If pucks are still on the table
                puckCycleList[puckClock].setVisibility(View.VISIBLE);
                playGame();
            } else { // Else we just flung the last puck.
                endRound();
            }
            Log.d(TAG, "Puck clock Value " + puckClock);

            return true;
        }

        Runnable resetPucksRunnable = new Runnable() {
            @Override
            public void run() {
                for (int i =0; i<6; i++) {
                    final ObjectAnimator animY = ObjectAnimator.ofFloat(puckCycleList[i], View.Y, spawnY);
                    final ObjectAnimator animX = ObjectAnimator.ofFloat(puckCycleList[i], View.X, spawnX);
                    animY.setDuration(1);
                    animX.setDuration(1);
                    auxAnimatePuckProperties = new AnimatorSet();
                    auxAnimatePuckProperties.setInterpolator(new DecelerateInterpolator(17));
                    auxAnimatePuckProperties.setStartDelay(0);
                    auxAnimatePuckProperties.playTogether(animY, animX);
                    auxAnimatePuckProperties.setupStartValues();
                    animatePuckArray[i] = auxAnimatePuckProperties;
                    animatePuckArray[i].start();
                    }
                setupInitialPuckVisibility();
                puckCycleList[0].setClickable(true);
                puckClock = 0;
                animationClock = 0;

            }
        } ;

        public void endRound() {
            puckCycleList[puckClock - 1].setClickable(false);
            myHandler.postDelayed(resetPucksRunnable, cancelDuration );
        }

        public void animatePuck() {
            int SCREEN_BOTTOM_BOUNDS = gameView.getBottom();
            int SCREEN_TOP_BOUNDS = gameView.getTop();
            int PUCK_TOP = puckCycleList[puckClock].getTop();
            int PUCK_BOTTOM = puckCycleList[puckClock].getBottom();
            puckCycleList[puckClock].setAdjustViewBounds(true);
            int[] puckXYLocation = new int[2];
            puckCycleList[puckClock].getLocationOnScreen(puckXYLocation);
            int puckXPOS = puckXYLocation[0];
            int puckYPOS = puckXYLocation[1];
            int bottomBoundary = SCREEN_BOTTOM_BOUNDS - PUCK_BOTTOM;
            int topBoundary = SCREEN_TOP_BOUNDS + PUCK_TOP;
            Log.d(TAG, "SCREEN_BOTTOM_BOUNDS:  " + SCREEN_BOTTOM_BOUNDS);
            Log.d(TAG, "SCREEN_TOP_BOUNDS:  " + SCREEN_TOP_BOUNDS);
            Log.d(TAG, "PUCK_TOP:  " + PUCK_TOP);
            Log.d(TAG, "PUCK_BOTTOM:  " + PUCK_BOTTOM);
            Log.d(TAG, "puck x pos:  " + puckXPOS);
            Log.d(TAG, "puck y pos:  " + puckYPOS);
            Log.d(TAG, "bottomBoundary:  " + bottomBoundary);
            Log.d(TAG, "topBoundary:  " + topBoundary);
            Log.d(TAG, "initial bottom boundary:  " + bottomBoundary);
            final ObjectAnimator animY = ObjectAnimator.ofFloat(puckCycleList[puckClock], View.TRANSLATION_Y, flingDistance * yVelocity);
            final ObjectAnimator animX = ObjectAnimator.ofFloat(puckCycleList[puckClock], View.TRANSLATION_X, flingDistance * xVelocity);
            animY.setDuration(maxDuration);
            animX.setDuration(maxDuration);
            animatePuckProperties = new AnimatorSet();
            animatePuckProperties.setInterpolator(new DecelerateInterpolator(17));
            animatePuckProperties.setStartDelay(0);
            animatePuckProperties.playTogether(animY, animX);
            animatePuckProperties.setupStartValues();
            animatePuckArray[puckClock] = animatePuckProperties; // If you call animatePuckProperties.cancel() then every puck in motion will be cancled. But now this can individually cancel the puck.

            //puckCycleList[puckClock]
            //animatePuckProperties.cancel();
            animatePuckProperties.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    Log.d(TAG, "Animation has been CANCELED!");
                    //cancleToast();

                }
                //works but needs fine tuning
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    animationClock = animationClock + 1;
                    //makeaToast();
                    Log.d(TAG, "ANIMATION HAS ENDED");
                    Log.d(TAG, "(in cancel) Animation clock is = " +animationClock);
                    Log.d(TAG, "--------------Y POSTION---------------------------------------------");
                    if (maxScoreReached == false) {
                        if (animationClock < 3) {//red pucks
                            if (puckCycleList[animationClock].getY() < 540 && puckCycleList[animationClock].getY() > 440) {
                                Log.d(TAG, "Red puck Y position testing within boundary: Puck :" + animationClock + " Y Pos+ " + puckCycleList[animationClock].getY());
                                Log.d(TAG, "Red one point");
                                redPlayerScore += 1;
                                redSwitcher.setCurrentText(redPlayerScore.toString());
                                //playSound(R.raw.onepoint);

                            } else if (puckCycleList[animationClock].getY() < 440 && puckCycleList[animationClock].getY() > 340) {
                                Log.d(TAG, "Red puck Y position testing within boundary: Puck :" + animationClock + " Y Pos+ " + puckCycleList[animationClock].getY());
                                Log.d(TAG, "Red  two points");
                                redPlayerScore += 2;
                                redSwitcher.setCurrentText(redPlayerScore.toString());
                                //playSound(R.raw.twopoints);


                            } else if (puckCycleList[animationClock].getY() < 340 && puckCycleList[animationClock].getY() > 240) {
                                Log.d(TAG, "Red puck Y position testing within boundary: Puck :" + animationClock + " Y Pos+ " + puckCycleList[animationClock].getY());
                                Log.d(TAG, "Red three points");
                                redPlayerScore += 3;
                                redSwitcher.setCurrentText(redPlayerScore.toString());
                                //playSound(R.raw.threepoints);

                            } else {
                                Log.d(TAG, "Red zero points");
                                Log.d(TAG, "Red puck Y position testing within boundary: Puck :" + animationClock + " Y Pos+ " + puckCycleList[animationClock].getY());
                                //playSound(R.raw.zeropoints);

                            }
                        }

                        if (animationClock >= 3 && animationClock < 6) {//blue pucks
                            if (puckCycleList[animationClock].getY() < 540 && puckCycleList[animationClock].getY() > 440) {
                                Log.d(TAG, "Blue puck Y position testing within boundary: Puck :" + animationClock + " Y Pos+ " + puckCycleList[animationClock].getY());
                                Log.d(TAG, "Blue one point");
                                bluePlayerScore += 1;
                                blueSwitcher.setCurrentText(bluePlayerScore.toString());
                                //playSound(R.raw.onepoint);

                            } else if (puckCycleList[animationClock].getY() < 440 && puckCycleList[animationClock].getY() > 340) {
                                Log.d(TAG, "Blue puck Y position testing within boundary: Puck :" + animationClock + " Y Pos+ " + puckCycleList[animationClock].getY());
                                Log.d(TAG, "Blue two points");
                                bluePlayerScore += 2;
                                blueSwitcher.setCurrentText(bluePlayerScore.toString());
                                //playSound(R.raw.twopoints);

                            } else if (puckCycleList[animationClock].getY() < 340 && puckCycleList[animationClock].getY() > 240) {
                                Log.d(TAG, "Blue puck Y position testing within boundary: Puck :" + animationClock + " Y Pos+ " + puckCycleList[animationClock].getY());
                                Log.d(TAG, "Blue three points");
                                bluePlayerScore += 3;
                                blueSwitcher.setCurrentText(bluePlayerScore.toString());
                                //playSound(R.raw.threepoints);

                            } else {
                                Log.d(TAG, "Blue puck Y position testing within boundary: Puck :" + animationClock + " Y Pos+ " + puckCycleList[animationClock].getY());
                                Log.d(TAG, "Blue zero points");
                                //playSound(R.raw.zeropoints);

                            }
                        }
                    }
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    super.onAnimationRepeat(animation);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    //playSound(R.raw.realisticslide2);
                }

                @Override
                public void onAnimationPause(Animator animation) {
                    super.onAnimationPause(animation);
                }

                @Override
                public void onAnimationResume(Animator animation) {
                    super.onAnimationResume(animation);
                }
            });
            //animatePuckProperties.start();
            animatePuckArray[puckClock].start();

        }
    }


    // This is a super smart way of passing a variable to runnable: ie, it's a super smart way of passing a variable's value at one moment even though it can change value in a few seconds
    // thanks to the internet, stackoverflow runnable-with-a-parameter
    private Runnable createCancelRunnable(final int currentpuckClock){
        Log.d(TAG, "Inside Runnable! !!!");
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "DOUBLE INSIDE Runnable! !!!");
                Log.d(TAG, "currentpuckClock = "+currentpuckClock);
                Log.d(TAG, "ABOUT TO INCREMENT ANIM CLOCK ");
                Log.d(TAG, "Prior to Animation clock increment = " +animationClock);
                animatePuckArray[currentpuckClock].cancel();

            }
        };
        return mRunnable;
    }

    private void deanimatePuck() {
        Log.d(TAG, "DEANIMATE PUCK !!!");
        myHandler.postDelayed(createCancelRunnable(puckClock), cancelDuration);
    }

    private boolean maxScoreReached() {
        if (redPlayerScore < maxScore && bluePlayerScore < maxScore) {
            maxScoreReached = false;
            return false;
        }
        Log.d(TAG, "MAX SCORE REACHED");
        maxScoreReached = true;
        return true;
    }

   
}

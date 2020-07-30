package com.example.aloom.neonshuffleboardv2;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Point;
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
    ObjectAnimator animXArray[] = new ObjectAnimator[6];
    ObjectAnimator animYArray[] = new ObjectAnimator[6];
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
    long cancelDuration = 4500; //This value actually ends the animation after x milliseconds.
    ImageView redPuck1;
    ImageView redPuck2;
    ImageView redPuck3;
    ImageView bluePuck1;
    ImageView bluePuck2;
    ImageView bluePuck3;
    //standard game mode is 15 points
    int maxScore = 15;
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
    int widthOfPuck;
    int heightOfPuck;
    float tableLength;
    float middleOfTable;
    float distanceFromMiddle;
    floatPoint nTopLeftTable, nTopRighttable, nBtmLeftTable, nBtmRightTable; //The corners of the table in x,y coordinates. Used for calculating if the puck goes out of bounds.
    private floatPoint oTopLeftTable, oTopRightTable, oBtmLeftTable, oBtmRightTable; //Orignal_Top_Left_Table coordinates, ect.
    private floatPoint oTable, nTable; //new layout's width, for the user's current phone
    float xSkewVal, ySkewVal;
    float spawnY, spawnX; //Stuff I added
    float puckWidthHalf;
    float puckHeightHalf;
    double angleOfTable;
    boolean cancelnext = false;
    int roundNum = 0;

    float oThreePointZoneBtm, oTwoPointZoneBtm, oOnePointZoneBtm, oTopOfTable; //original.
    float nThreePointZoneBtm, nTwoPointZoneBtm, nOnePointZoneBtm, nTopOfTable;
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
      //  blueScoreText.setTextSize(1, 60f);    // I removed this, and put the values in the .XML
     //   redScoreText.setTextSize(1, 60f);
        listenForTouchOnPuck();
        //Round cycle
        redPuck1.setClickable(true);
        playGame();
        //Orignal values of tables.
        angleOfTable = 1.4339812335; // 1.4339 radians. I did some math. 1.433 = atan(1213/167), used in the cheeky calculation to find if a puck is on or off the table.
        oTable = new floatPoint(750, 1334);
        oTopLeftTable = new floatPoint(193, 43);
        oTopRightTable = new floatPoint(552, 43);
        oBtmLeftTable = new floatPoint(30, 1256);
        oBtmRightTable = new floatPoint(719, 1256);
        oThreePointZoneBtm = 100;
        oTwoPointZoneBtm = 155;
        oOnePointZoneBtm = 217;

    }


    public void calculateDistance() {
        Log.e(TAG, "initial x pos: " + downXPOS);
        Log.e(TAG, "initial y pos: " + downYPOS);
        flingDistance = (float) Math.sqrt((dx * dx) + (dy * dy));
    }

    public void waitTime(int milliseconds) {
        try {
            wait(milliseconds);
        } catch (Exception e) {
            Log.e("Wait Method: ", "Cause: " + e.getCause());
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
    }

    //exclude first puck
    public void setupInitialPuckVisibility() {
        redPuck1.setVisibility(View.VISIBLE); //I added this.
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

    private Runnable invisiblePuckRunnable(final ImageView fallingPuck, final String LR){
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                fallingPuck.setVisibility(View.GONE);
                //Below: I have to rotate the pucks back to their original position, else in the next round they will be sideways.
                if (LR.equals("right")) {
                    ObjectAnimator animRotate = ObjectAnimator.ofFloat(fallingPuck, "rotation", -90f, 0f);
                    animRotate.start();
                }
                else {
                    ObjectAnimator animRotate = ObjectAnimator.ofFloat(fallingPuck, "rotation", -90f, 0f);
                    animRotate.start();
                }
            }
        };
        return mRunnable;
    }

    public void fallingOffTableAnimation(ImageView fallingPuck, String LR ){
        //We could use an AnimatorSet here, but I didn't wanna go nuts on the graphics. I feel like thats semi low priority.
        // I wanted to make it shrink aswell but didn't find anything on the web in a reasonable time.
        int fallDuration = 100;
        ObjectAnimator animRotate;
        if (LR.equals("right")) {
            animRotate = ObjectAnimator.ofFloat(fallingPuck, "rotation", 0f, 90f);
            myHandler.postDelayed(invisiblePuckRunnable(fallingPuck, LR), 1000);
        }
        else{
            animRotate = ObjectAnimator.ofFloat(fallingPuck, "rotation", 0f, -90f);
            myHandler.postDelayed(invisiblePuckRunnable(fallingPuck, LR), 1000);
        }
        animRotate.setDuration(fallDuration);
        animRotate.start();


    }
    public boolean findIfPuckCrossesMinimumX(float animX){
        float currentPuckX = Math.abs( animX) - puckWidthHalf;
        if (distanceFromMiddle < currentPuckX ){
            return true;
        }
        return false;
    }

    private float getPuckY(float yPuck){
        return  spawnY + yPuck; //yPuck is a negative value (puck is going up)
    }
    public boolean findIfPuckCrossesBottomTable(float animY) {
        if( getPuckY(animY) > nBtmLeftTable.y - puckHeightHalf) {
            return true;
        }
        return false;
    }
    public boolean findIfPuckCrossesTopTable(float animY ) {
        if( getPuckY(animY) < nTopOfTable - puckHeightHalf) {
            return true;
        }
        return false;
    }

    public boolean findIfPuckCrossesTable(float animX, float animY){
        float currentPuckY = tableLength + animY;
        double tableEdge = distanceFromMiddle + (currentPuckY / Math.tan(angleOfTable)); // Adjacent = Opposite/Tan(x)
//        Log.e(TAG, "tableEdge  " + tableEdge);
//        Log.e(TAG, "Math.abs(animX) + puckWidthHalf   " + (Math.abs(animX) + puckWidthHalf) );
        if (Math.abs(animX) - puckWidthHalf/2 > tableEdge){
                return true;
            }
        else {
            return false; }
    }

    public void getViewValues() {
        widthOfPuck = redPuck1.getWidth();
        heightOfPuck = redPuck1.getHeight();

        nTable = new floatPoint(gameView.getWidth(), gameView.getHeight());
        middleOfTable = nTable.x / 2;
        xSkewVal = nTable.x / oTable.x;
        ySkewVal = nTable.y / oTable.y;
        puckWidthHalf = redPuck1.getWidth()/2;
        puckHeightHalf = redPuck1.getHeight()/2;
        nTopLeftTable  = new floatPoint( oTopLeftTable.x * xSkewVal,  oTopLeftTable.y * ySkewVal);
        nTopRighttable = new floatPoint( oTopRightTable.x * xSkewVal, oTopRightTable.y * ySkewVal);
        nBtmLeftTable  = new floatPoint( oBtmLeftTable.x * xSkewVal,  oBtmLeftTable.y * ySkewVal);
        nBtmRightTable = new floatPoint( oBtmRightTable.x * xSkewVal, oBtmRightTable.y * ySkewVal);
        distanceFromMiddle = nTopRighttable.x -middleOfTable;
        tableLength = nBtmLeftTable.y - nTopLeftTable.y;

        nThreePointZoneBtm = oThreePointZoneBtm * ySkewVal;
        nTwoPointZoneBtm = oTwoPointZoneBtm * ySkewVal;
        nOnePointZoneBtm = oOnePointZoneBtm * ySkewVal;
        nTopOfTable = nTopLeftTable.y;

        spawnY = puckCycleList[0].getY();
        spawnX = puckCycleList[0].getX();
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
            if (isSpawnCheck == false) { //I had to use this  if statement here  b/c if I wanted info from any View objct, ie if I put getViewValues() in onCreate, the layout for some reason loads after onCreate, and the coordinats are x,y = 0,0 and the size of everything is 0. This is a temporary solution, no significant drawbacks but its not fitting to call these in the fling method.
                getViewValues();
                isSpawnCheck = true;
                }
            downXPOS = fingerDown.getX();
            downYPOS = fingerDown.getY();
    /*        Log.e(TAG, "XPOS finger down:  " + downXPOS);
            Log.e(TAG, "YPOS finger down:  " + downYPOS);
            Log.e(TAG, "puck left x: " + puckCycleList[puckClock].getLeft());
            Log.e(TAG, "puck right x: " + puckCycleList[puckClock].getRight());
            Log.e(TAG, "puck top y: " + puckCycleList[puckClock].getTop());
            Log.e(TAG, "puck bottom y : " + puckCycleList[puckClock].getBottom());
            Log.i(TAG, "____FLING INITIATED____");
  */        upXPOS = move.getX();
            upYPOS = move.getY();
            dx = (upXPOS - downXPOS);
            dy = (upYPOS - downYPOS);
            xVelocity = (float) (velocityX * .00035);
            yVelocity = (float) (velocityY * .00035);
            Log.e(TAG, "End X POS:  " + upXPOS);
            Log.e(TAG, "End Y POS:  " + upYPOS);
            Log.e(TAG, "Velocity X:  " + velocityX);
            Log.e(TAG, "Velocity Y:  " + velocityY);
            calculateDistance();
            animatePuck();
            deanimatePuck(); //Solution for updating scoreboard and other .cancel() events. It's b/c the decelerating function is still decelerating, it just changes like .00001 pixels per millisecond, and! that rate of change gets smaller each millisecond.
            puckClock++;

            if (puckClock <= 5) { //If pucks are still on the table
                puckCycleList[puckClock].setVisibility(View.VISIBLE);
                playGame();
            } else { // Else we just flung the last puck.
                endRound();
            }
            Log.e(TAG, "Puck clock Value " + puckClock);

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
                roundNum = roundNum + 1;
                if (roundNum == 3) { // Reset the scoreboard after 3 rounds
                    roundNum = 0;
                    bluePlayerScore = 0;
                    redPlayerScore = 0;
                    redSwitcher.setCurrentText(redPlayerScore.toString());
                    blueSwitcher.setCurrentText(redPlayerScore.toString());
                    //endRoundAnimation(); //presumablly this exists
                }


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
  /*          Log.e(TAG, "SCREEN_BOTTOM_BOUNDS:  " + SCREEN_BOTTOM_BOUNDS);
            Log.e(TAG, "SCREEN_TOP_BOUNDS:  " + SCREEN_TOP_BOUNDS);
            Log.e(TAG, "PUCK_TOP:  " + PUCK_TOP);
            Log.e(TAG, "PUCK_BOTTOM:  " + PUCK_BOTTOM);
            Log.e(TAG, "puck x pos:  " + puckXPOS);
            Log.e(TAG, "puck y pos:  " + puckYPOS);
            Log.e(TAG, "bottomBoundary:  " + bottomBoundary);
            Log.e(TAG, "topBoundary:  " + topBoundary);
            Log.e(TAG, "initial bottom boundary:  " + bottomBoundary);
 */         final ObjectAnimator animY = ObjectAnimator.ofFloat(puckCycleList[puckClock], View.TRANSLATION_Y, flingDistance * yVelocity);
            final ObjectAnimator animX = ObjectAnimator.ofFloat(puckCycleList[puckClock], View.TRANSLATION_X, flingDistance * xVelocity);
            animY.setDuration(maxDuration);
            animX.setDuration(maxDuration);
            animatePuckProperties = new AnimatorSet();
            animatePuckProperties.setInterpolator(new DecelerateInterpolator(17));
            animatePuckProperties.setStartDelay(0);
            animatePuckProperties.playTogether(animY, animX);
            animatePuckProperties.setupStartValues();
            animatePuckArray[puckClock] = animatePuckProperties; // If you call animatePuckProperties.cancel() then every puck in motion will be cancled. But now this can individually cancel the puck.
            animXArray[puckClock] = animX; //This was an attempt to get more control over the animX/animY object, doesn't work as how you'd think it would. :/
            animYArray[puckClock] = animY;

            animXArray[puckClock].addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) { //The animx.getAnimatedValue() updates the image after it gets the AnimatedValue, so I made it so that the animation draws itself 1 more time before the animation cancels b/c it looks too weird. Yet it still looks a bit odd :(
                    if (cancelnext){
                        if ((float) animX.getAnimatedValue() > 0){
                            fallingOffTableAnimation(puckCycleList[puckClock-1], "right" );
                        }
                        else {
                            fallingOffTableAnimation(puckCycleList[puckClock-1], "left" );
                        }
                        animXArray[puckClock-1].cancel();
                        animYArray[puckClock-1].cancel();
                        cancelnext = false;

                    }
                    else {
                        if (findIfPuckCrossesTopTable( (float) animY.getAnimatedValue()) || findIfPuckCrossesBottomTable( (float) animY.getAnimatedValue())    ){
                            cancelnext = true;
                        }
                //        if ( findIfPuckCrossesMinimumX( (float) animX.getAnimatedValue())  ) {
                            if ( findIfPuckCrossesTable( (float) animX.getAnimatedValue(), (float) animY.getAnimatedValue() )) { //If it Crosses the left or right of table.
                                cancelnext = true;
                            }
                 //       }
                    }
                }
            });
            animatePuckProperties.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                }
                //works but needs fine tuning
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
//                    Log.e(TAG, "ANIMATION HAS ENDED");
//                    Log.e(TAG, "puckCycleList[0].getX " + puckCycleList[animationClock].getX());
//                    Log.e(TAG, "Red puck Y position testing within boundary: Puck :" + animationClock + " Y Pos+ " + puckCycleList[animationClock].getY());
//                    Log.e(TAG, "(in cancel) Animation clock is = " +animationClock);
//                    Log.e(TAG, "--------------Y POSTION---------------------------------------------");
                    if (maxScoreReached == false) { //Max Rounds????????
                        //If puck has NOT crossed the table. Checks if the puck is on the table, (Opposed to pucks that fell off the edge).
                        if ( !findIfPuckCrossesTable((float) animX.getAnimatedValue(), (float) animY.getAnimatedValue())) {
                            if (animationClock < 3) {//red pucks
                                if (puckCycleList[animationClock].getY() + puckHeightHalf > nTwoPointZoneBtm && puckCycleList[animationClock].getY() <= nOnePointZoneBtm) {
//                                    Log.e(TAG, "Red puck Y position testing within boundary: Puck :" + animationClock + " Y Pos+ " + puckCycleList[animationClock].getY());
//                                    Log.e(TAG, "Red one point");
                                    redPlayerScore += 1;
                                    redSwitcher.setCurrentText(redPlayerScore.toString());
                                    playSound(R.raw.onepoint);

                                } else if (puckCycleList[animationClock].getY() + puckHeightHalf > nThreePointZoneBtm && puckCycleList[animationClock].getY() <= nTwoPointZoneBtm) {
//                                    Log.e(TAG, "Red puck Y position testing within boundary: Puck :" + animationClock + " Y Pos+ " + puckCycleList[animationClock].getY());
//                                    Log.e(TAG, "Red  two points");
                                    redPlayerScore += 2;
                                    redSwitcher.setCurrentText(redPlayerScore.toString());
                                    playSound(R.raw.twopoints);


                                } else if (puckCycleList[animationClock].getY() + puckHeightHalf > nTopOfTable && puckCycleList[animationClock].getY() <= nThreePointZoneBtm) {
//                                    Log.e(TAG, "Red puck Y position testing within boundary: Puck :" + animationClock + " Y Pos+ " + puckCycleList[animationClock].getY());
//                                    Log.e(TAG, "Red three points");
                                    redPlayerScore += 3;
                                    redSwitcher.setCurrentText(redPlayerScore.toString());
                                    playSound(R.raw.threepoints);

                                } else {
//                                    Log.e(TAG, "Red zero points");
//                                    Log.e(TAG, "Red puck Y position testing within boundary: Puck :" + animationClock + " Y Pos+ " + puckCycleList[animationClock].getY());
                                    playSound(R.raw.zeropoints);

                                }
                            }

                            if (animationClock >= 3 && animationClock < 6) {//blue pucks
                                if (puckCycleList[animationClock].getY() + puckHeightHalf > nTwoPointZoneBtm && puckCycleList[animationClock].getY() <= nOnePointZoneBtm) {
//                                    Log.e(TAG, "Blue puck Y position testing within boundary: Puck :" + animationClock + " Y Pos+ " + puckCycleList[animationClock].getY());
//                                    Log.e(TAG, "Blue one point");
                                    bluePlayerScore += 1;
                                    blueSwitcher.setCurrentText(bluePlayerScore.toString());
                                    playSound(R.raw.onepoint);

                                } else if (puckCycleList[animationClock].getY() + puckHeightHalf > nThreePointZoneBtm && puckCycleList[animationClock].getY() <= nTwoPointZoneBtm) {
//                                    Log.e(TAG, "Blue puck Y position testing within boundary: Puck :" + animationClock + " Y Pos+ " + puckCycleList[animationClock].getY());
//                                    Log.e(TAG, "Blue two points");
                                    bluePlayerScore += 2;
                                    blueSwitcher.setCurrentText(bluePlayerScore.toString());
                                    playSound(R.raw.twopoints);

                                } else if (puckCycleList[animationClock].getY() + puckHeightHalf > nTopOfTable && puckCycleList[animationClock].getY() <= nThreePointZoneBtm) {
//                                    Log.e(TAG, "Blue puck Y position testing within boundary: Puck :" + animationClock + " Y Pos+ " + puckCycleList[animationClock].getY());
//                                    Log.e(TAG, "Blue three points");
                                    bluePlayerScore += 3;
                                    blueSwitcher.setCurrentText(bluePlayerScore.toString());
                                    playSound(R.raw.threepoints);

                                } else {
//                                    Log.e(TAG, "Blue puck Y position testing within boundary: Puck :" + animationClock + " Y Pos+ " + puckCycleList[animationClock].getY());
//                                    Log.e(TAG, "Blue zero points");
                                    playSound(R.raw.zeropoints);

                                }
                            }
                            animationClock = animationClock + 1;
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
                    playSound(R.raw.realisticslide2);
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
            //animatePuckProperties.start(); //No longer needed
            animatePuckArray[puckClock].start();

        }
    }


    // This is a super smart way of passing a variable to runnable: ie, it's a super smart way of passing a variable's value at one moment even though it can change value in a few seconds
    // thanks to the internet, stackoverflow runnable-with-a-parameter
    private Runnable createCancelRunnable(final int currentpuckClock){

        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                animatePuckArray[currentpuckClock].cancel();
            }
        };
        return mRunnable;
    }

    private void deanimatePuck() {
        myHandler.postDelayed(createCancelRunnable(puckClock), cancelDuration);
    }


    private boolean maxScoreReached() {
        if (redPlayerScore < maxScore && bluePlayerScore < maxScore) {
            maxScoreReached = false;
            return false;
        }
        Log.e(TAG, "MAX SCORE REACHED");
        maxScoreReached = true;
        return true;
    }

}
package com.example.jagdtest;

import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;

public class TestFragment extends Fragment
        implements
            OnCompletionListener,
            SeekBar.OnSeekBarChangeListener {

    public TestFragment() {
    }

    public static TestFragment newInstance(boolean isExam) {
        TestFragment f = new TestFragment();
        Bundle args = new Bundle();
        args.putBoolean("isExam", isExam);
        f.setArguments(args);
        return f;
    }
    
    private TestDbHelper mDbHelper;
    private Context mContext;
    private LinearLayout qContainer;
    private MediaPlayer mPlayer;
    private SeekBar songProgressBar;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    private ImageButton btnPlay;
    private Button btnCurQ;
    private Question curQ;
    private int qNumber = 0;
    private boolean isExam = false;
    private LinearLayout qScrol;
    private long timerMillis = 0;
    private SparseArray<CurentAnswer> answProgress = new SparseArray<CurentAnswer>();
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();

    public void analyzeAndMoveToCateg(){
        if (!isExam) return;
        Button btnNextCateg1Q = null;
        for (int i = 0; i < qScrol.getChildCount(); i++){
            Button b = (Button) qScrol.getChildAt(i);
            if (b.equals(btnCurQ)) continue;
            if (b.getTag() == null) continue;
            
            if (curQ.getIdCateg() == this.getCategTypeFromView(b)){
                int id = this.getQIdFromView(b);
                if (answProgress.get(id) != null) continue;
                answProgress.put(id, new CurentAnswer(0, 
                        mContext.getResources().getString(
                        R.string.no_answer), false));
                b.setBackgroundColor(this.getColorByCateg(0));
            }
            if (btnNextCateg1Q == null 
                    && curQ.getIdCateg() == this.getCategTypeFromView(b) - 1){
                btnNextCateg1Q = b;
            }
        }
        if (btnNextCateg1Q != null){
            onQNumClick(btnNextCateg1Q);
        } else {
            endTest();
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (getArguments() != null)
        isExam = getArguments().getBoolean("isExam", false);
        View rootView = inflater.inflate(R.layout.fragment_test, container,
                false);
        mContext = getActivity();
        qContainer = (LinearLayout) rootView.findViewById(R.id.qContainer);
        qScrol = (LinearLayout) rootView.findViewById(R.id.qScrol);
        int qId = 0;
        int qCategId = 0;
        if (savedInstanceState != null) {
            timerMillis = savedInstanceState.getLong("timer");
            qId = savedInstanceState.getInt("curQId");
            qCategId = savedInstanceState.getInt("curCateg");
            isExam = savedInstanceState.getBoolean("isExam");
            answProgress = savedInstanceState.getSparseParcelableArray("progress");
        }
        mDbHelper = new TestDbHelper(mContext);
        SQLiteDatabase db = mDbHelper.openDataBase();
        SparseArray<String> categs = mDbHelper.getAllCategs(db);
        
        for (int i = 0; i < categs.size(); i++) {
            int categId = categs.keyAt(i);
            SparseIntArray qnumbs = mDbHelper.getAllNumsByCateg(db, categId);
            qNumber += qnumbs.size();
            for (int j = 0; j < qnumbs.size(); j++) {
                int id = qnumbs.keyAt(j);
                int qNum = qnumbs.get(id);
                Button b = new Button(mContext);
                //if no saved position, make current the first question
                if (qId == 0 && i == 0 && j == 0) {
                    qId = id;
                }
                if (qId == id) btnCurQ = b;
                
                //change button color
                boolean isQuestionsClosed = (isExam && qCategId > categId) 
                        || (answProgress != null && answProgress.get(id) != null);
                // if it has been already answered, color is gray
                int color = getColorByCateg((isQuestionsClosed) ? 0 : categId);
                b.setBackgroundColor(color);
                
                b.setTag(id + "_" + categId);
                b.setText("" + qNum);
                LayoutParams params = new LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
                int marg = (int) mContext.getResources().getDimension(
                        R.dimen.button_margin);
                params.setMargins(marg, marg, marg, marg);
                b.setLayoutParams(params);

                b.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onQNumClick(v);
                    }
                });
                qScrol.addView(b);
            }
        }
        // init first question in the test
        curQ = mDbHelper.getQuestionsById(db, qId);
        btnCurQ.setBackgroundColor(mContext.getResources().getColor(
                R.color.kornblumen));
        qScrol.scrollTo(btnCurQ.getLeft(), 0);
        initViews(curQ);
        if (answProgress != null && answProgress.get(qId) != null)
            this.disableEnableControls(false, qContainer);
        if (timerMillis == 0) timerMillis = mDbHelper.getCategTimeById(curQ.getIdCateg(), db);
        db.close();
        if (isExam && timerMillis != 0 && mContext instanceof TestActivity){
            ((TestActivity) mContext).setNewTimer(timerMillis);
        }
        return rootView;
    }

    public void saveAnswProg(Question q) {
        CurentAnswer ca = null;
        //HashSet<Answer> hs = q.getAnswers();
        // add views for answers
        switch (q.getQuestionType()) {
        // one select
            case 0 :
            case 2 :
            case 4 : {
                RadioGroup rg = (RadioGroup) qContainer
                        .findViewWithTag("radiogroup");
                
                int id = rg.getCheckedRadioButtonId();
                if (id == -1)
                    return;
                RadioButton b = (RadioButton) rg.findViewById(id);
                int points = 0;
                if (b.getTag() != null) {
                    // if user selected YES answer
                    if (b.getTag().equals("1")) {
                        boolean isYesRightAnsw = (q.getQuestionType() == 2)
                                ? true
                                : false;
                        if (isYesRightAnsw) points = q.getPoints();
                        ca = new CurentAnswer(points, "1", isYesRightAnsw);
                        ca.setMaxPoints(q.getPoints());
                        answProgress.put(q.getId(), ca);
                        return;
                    }
                    // if user selected NO answer
                    if (b.getTag().equals("0")) {
                        boolean isNoRightAnsw = (q.getQuestionType() == 4)
                                ? true
                                : false;
                        if (isNoRightAnsw) points = q.getPoints();
                        ca = new CurentAnswer(points, "0", isNoRightAnsw);
                        ca.setMaxPoints(q.getPoints());
                        answProgress.put(q.getId(), ca);
                        return;
                    }
                }
                // if user selected some other one choice answer
                for (Answer answ : q.getAnswers()) {
                    if (b.getText().toString().equals(answ.getAnswer())) {
                        int p = (answ.isAnswer()) ? q.getPoints() : 0;
                        ca = new CurentAnswer(p, answ.getAnswer(),
                                answ.isAnswer());
                        ca.setMaxPoints(q.getPoints());
                        answProgress.put(q.getId(), ca);
                        return;
                    }
                }
                break;
            }
            // multichoice
            case 1 : {
                LinearLayout multiLayout = (LinearLayout) qContainer
                        .findViewWithTag("multichoice");
                for (int i = 0; i < multiLayout.getChildCount(); i++) {
                    CheckBox c = (CheckBox) multiLayout.getChildAt(i);
                    if (!c.isChecked())
                        continue;
                    for (Answer answ : q.getAnswers()) {
                        if (!c.getText().toString().equals(answ.getAnswer()))
                            continue;
                        int point = (answ.isAnswer()) ? q.getPoints() : 0;
                        if (ca == null) {
                            ca = new CurentAnswer(point, answ.getAnswer(),
                                    answ.isAnswer());
                        } else {
                            ca.addChoice(point, answ.getAnswer(),
                                    answ.isAnswer());
                        }
                        break;
                    }
                }
                // if nothing was selected by user, leave this question open
                if (ca == null)
                    return;
                // if all answers selected correct add extra points for this
                // question
                if (ca.getAnswers().size() == q.getNumRightAnswers())
                    ca.addPoints(q.getExtraPoints());
                ca.setMaxPoints(q.getMaxPoints());
                answProgress.put(q.getId(), ca);
                break;
            }
            // Type answer
            case 3 : {
                EditText edt = (EditText) qContainer
                        .findViewWithTag("user_answer");
                if (edt == null)
                    return;
                String userAnswer = edt.getText().toString();
                if (userAnswer.isEmpty())
                    return;
                String correctAnswer = "";
                if (!q.getAnswers().isEmpty()) {
                    correctAnswer = q.getAnswers().iterator().next().getAnswer();
                }
                int gainedPoints = 0;
                boolean isCorrect = false;
                if (userAnswer.equals(correctAnswer)) {
                    gainedPoints = q.getPoints();
                    isCorrect = true;
                }
                ca = new CurentAnswer(gainedPoints, userAnswer, isCorrect);
                ca.setMaxPoints(q.getPoints());
                answProgress.put(q.getId(), ca);
                break;
            }
        }
    }

    private void disableEnableControls(boolean enable, ViewGroup vg){
        for (int i = 0; i < vg.getChildCount(); i++){
           View child = vg.getChildAt(i);
           child.setEnabled(enable);
           if (child instanceof ViewGroup){ 
              disableEnableControls(enable, (ViewGroup)child);
           }
        }
    }
    
    public void setPrevQColor(Button b) {
        boolean isOpenQ = true;
        if (answProgress.get(getQIdFromView(b)) != null)
            isOpenQ = false;
        if (!isOpenQ) {
            b.setBackgroundColor(getColorByCateg(0));
            return;
        }
        int categ = getCategTypeFromView(b);
        int color = getColorByCateg(categ);
        b.setBackgroundColor(color);
    }

    public void onPause() {
        releasePlayer();
        
        /*if (btnCurQ != null && curQ != null) {
            saveAnswProg(curQ);
            if (answProgress.get(curQ.getId()) != null)
                disableEnableControls(false, qContainer);
        }*/
        super.onPause();
    }
    
    public void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        bundle.putSparseParcelableArray("progress", answProgress);
        bundle.putInt("curQId", curQ.getId());
        bundle.putBoolean("isExam", isExam);
        bundle.putInt("curCateg", curQ.getIdCateg());
        bundle.putLong("timer", timerMillis);
    }

    public int getColAnswered(){
        return answProgress.size();
    }
    public int getColQuestions(){
        return this.qNumber;
    }
    
    public void endTest(){
        if (answProgress == null) answProgress = new SparseArray<CurentAnswer>();
        if (btnCurQ != null && curQ != null) {
            saveAnswProg(curQ);
            if (answProgress.get(curQ.getId()) != null)
                disableEnableControls(false, qContainer);
        }
        int totalPoints = 0;
        int totalMaxPoints = 0;
        int wrongAnswers = 0;
        for (int i = 0; i < answProgress.size(); i++){
            int id = answProgress.keyAt(i);
            CurentAnswer ca = answProgress.get(id);
            totalPoints += ca.getPoints();
            totalMaxPoints += ca.getMaxPoints();
            if (!ca.isWrongAnswer()) continue;
            wrongAnswers++;
        }
        
        int progressDone = totalPoints * 100 / 
                ((totalMaxPoints > 0) ? totalMaxPoints : 1);
        String result = 
                "Gained: " + totalPoints + " points from " + totalMaxPoints + 
                ".\n It is: " + progressDone + "%\n Wrong answers: " + 
                wrongAnswers + " from " + answProgress.size() + " answered.";
        Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
    }
    
    public int getCategTypeFromView(Button b) {
        try {
            String tag = (String) b.getTag();
            return Integer.parseInt(tag.substring(tag.indexOf("_") + 1));
        } catch (Exception e) {
            return 0;
        }
    }

    public int getQIdFromView(Button b) {
        try {
            String tag = (String) b.getTag();
            return Integer.parseInt(tag.substring(0, tag.indexOf("_")));
        } catch (Exception e) {
            return 0;
        }
    }

    public void onQNumClick(View v) {
        if (((Button) v).equals(btnCurQ))
            return;
        ColorDrawable cd = (ColorDrawable) ((Button) v).getBackground();
        if (cd.getColor() == mContext.getResources().getColor(R.color.gray)) {
            Toast.makeText(mContext, R.string.closed_question,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // save actions
        if (btnCurQ != null && curQ != null) {
            saveAnswProg(curQ);
            setPrevQColor(btnCurQ);
        }
        // init new open question
        btnCurQ = (Button) v;
        btnCurQ.setBackgroundColor(mContext.getResources().getColor(
                R.color.kornblumen));
        
        //if it is Exam mode save previousCateg value
        int prewCateg = (isExam && curQ != null) 
                    ? curQ.getIdCateg() 
                    : 0;
        
        int qId = getQIdFromView(btnCurQ);
        SQLiteDatabase db = mDbHelper.openDataBase();
        curQ = mDbHelper.getQuestionsById(db, qId);
        
        //if it is Exam mode and categ changed - set new Timer
        if (isExam && prewCateg != 0 && prewCateg < curQ.getIdCateg()){
            if (mContext instanceof TestActivity){
                long millis = mDbHelper.getCategTimeById(curQ.getIdCateg(), db);
                ((TestActivity) mContext).setNewTimer(millis);
            }
        }
        db.close();
        initViews(curQ);
    }
    
    public void saveTimerCurentTime(long millis){
        timerMillis = millis;
    }

    public boolean isAnswerAlreadySelected(int id, String answer) {
        CurentAnswer ca = answProgress.get(id);
        if (ca != null && ca.getAnswers() != null)
            for (String s : ca.getAnswers().keySet()) {
                if (s.equals(answer))
                    return true;
            }
        return false;
    }

    public void releasePlayer(){
        mHandler.removeCallbacks(mUpdateTimeTask);
        if (mPlayer != null) {
            if (mPlayer.isPlaying())
                mPlayer.stop();
            mPlayer.release();
        }
    }
    
    public void initViews(Question q) {
        if (q == null)
            return;
        releasePlayer();
        qContainer.removeAllViews();

        // add question
        TextView question = new TextView(mContext);
        question.setWidth(LayoutParams.MATCH_PARENT);
        question.setWidth(LayoutParams.WRAP_CONTENT);
        question.setText(q.getQuestion());
        question.setPadding(0, 0, 0, 20);
        qContainer.addView(question);

        // add picture if available
        Bitmap bmp = getPic("pics/" + q.getIdCateg() + "_" + q.getIdNumber()
                + ".jpg");
        if (bmp != null) {
            ImageView img = new ImageView(mContext);
            img.setImageBitmap(bmp);
            img.setScaleType(ScaleType.FIT_CENTER);
            LayoutParams p = new LayoutParams(200, 200);
            img.setPadding(0, 0, 0, 20);
            img.setLayoutParams(p);
            qContainer.addView(img);
        }

        // add audio if available
        mPlayer = getMediaPlayer("audio/" + q.getIdCateg() + "_"
                + q.getIdNumber() + ".mp3");
        if (mPlayer != null) {
            addPlayer(qContainer, mContext);
        }

        // add views for answers
        switch (q.getQuestionType()) {
        // one select
            case 0 : {
                RadioGroup rg = new RadioGroup(mContext);
                rg.setTag("radiogroup");
                rg.setOrientation(RadioGroup.VERTICAL);
                LayoutParams params = new LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                rg.setLayoutParams(params);
                for (Answer answ : q.getAnswers()) {
                    RadioButton rb = new RadioButton(mContext);
                    android.widget.RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT);
                    rb.setLayoutParams(pr);
                    rb.setText(answ.getAnswer());
                    rb.setContentDescription(answ.getAnswer());
                    rb.setChecked(isAnswerAlreadySelected(q.getId(),
                            answ.getAnswer()));
                    rg.addView(rb);
                }
                qContainer.addView(rg);
                break;
            }
            // multichoice
            case 1 : {
                LinearLayout multiLayout = new LinearLayout(mContext);
                multiLayout.setTag("multichoice");
                multiLayout.setOrientation(LinearLayout.VERTICAL);
                LayoutParams params = new LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                multiLayout.setLayoutParams(params);
                for (Answer answ : q.getAnswers()) {
                    CheckBox ctv = new CheckBox(mContext);
                    LayoutParams pr = new LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT);
                    ctv.setLayoutParams(pr);
                    ctv.setText(answ.getAnswer());
                    ctv.setChecked(isAnswerAlreadySelected(q.getId(),
                            answ.getAnswer()));
                    ctv.setContentDescription(answ.getAnswer());
                    multiLayout.addView(ctv);
                }
                qContainer.addView(multiLayout);
                break;
            }
            // Yes / No
            case 2 :
            case 4 : {
                RadioGroup rg = new RadioGroup(mContext);
                rg.setTag("radiogroup");
                rg.setOrientation(RadioGroup.HORIZONTAL);
                LayoutParams params = new LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                rg.setLayoutParams(params);
                RadioButton rb_yes = new RadioButton(mContext);
                rb_yes.setText(R.string.yes);
                rb_yes.setTag("1");
                rb_yes.setChecked(isAnswerAlreadySelected(q.getId(), "1"));
                RadioButton rb_no = new RadioButton(mContext);
                rb_no.setText(R.string.no);
                rb_no.setTag("0");
                rb_no.setChecked(isAnswerAlreadySelected(q.getId(), "0"));
                rg.addView(rb_yes);
                rg.addView(rb_no);
                qContainer.addView(rg);
                break;
            }
            // Type answer
            case 3 : {
                EditText edt = new EditText(mContext);
                edt.setLayoutParams(new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                edt.setInputType(InputType.TYPE_CLASS_TEXT);
                edt.setHint(R.string.your_answer);
                edt.setTag("user_answer");

                CurentAnswer ca = answProgress.get(q.getId());
                if (ca != null && ca.getAnswers() != null)
                    for (String s : ca.getAnswers().keySet()) {
                        edt.setText(s);
                        break;
                    }

                qContainer.addView(edt);
                break;
            }
        }

        TextView accept = new TextView(mContext);
        accept.setGravity(Gravity.RIGHT);
        accept.setText(mContext.getResources().getString(R.string.next));
    }

    public void addPlayer(LinearLayout container, Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout view = (LinearLayout) inflater.inflate(
                R.layout.player_bar, null);
        btnPlay = (ImageButton) view.findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer.isPlaying()) {
                    if (mPlayer != null) {
                        mPlayer.pause();
                        btnPlay.setImageResource(R.drawable.btn_play);
                    }
                } else {
                    if (mPlayer != null) {
                        mPlayer.start();
                        btnPlay.setImageResource(R.drawable.btn_pause);
                    }
                }
            }
        });
        songProgressBar = (SeekBar) view.findViewById(R.id.songProgressBar);
        songCurrentDurationLabel = (TextView) view
                .findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) view
                .findViewById(R.id.songTotalDurationLabel);
        // Listeners
        songProgressBar.setOnSeekBarChangeListener(this);
        mPlayer.setOnCompletionListener(this);
        songProgressBar.setProgress(0);
        songProgressBar.setMax(100);
        // Updating progress bar
        updateProgressBar();
        container.addView(view);
    }

    public Bitmap getPic(String name) {
        AssetManager am = mContext.getAssets();
        try {
            InputStream input = am.open(name);
            if (input == null)
                return null;
            // BufferedInputStream bufferedInputStream = new
            // BufferedInputStream(input);
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            Log.e(e.getLocalizedMessage(), e.getMessage());
        }
        return null;
    }

    public MediaPlayer getMediaPlayer(String filename) {
        MediaPlayer m = null;
        try {
            AssetFileDescriptor descriptor = mContext.getAssets().openFd(
                    filename);
            if (descriptor != null) {
                m = new MediaPlayer();
            } else
                return null;
            m.reset();
            m.setDataSource(descriptor.getFileDescriptor(),
                    descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();

            m.prepare();
            m.setVolume(1f, 1f);
            m.setLooping(true);
            return m;
        } catch (Exception e) {
            Log.e(e.getLocalizedMessage(), e.getMessage());
        }
        return null;
    }

    public int getColorByCateg(int categ) {
        int color = 0;
        switch (categ) {
            case 0 :
                color = mContext.getResources().getColor(R.color.gray);
                break;
            case 1 :
                color = mContext.getResources().getColor(R.color.coral_wtk);
                break;
            case 2 :
                color = mContext.getResources().getColor(R.color.orange_hnd);
                break;
            case 3 :
                color = mContext.getResources().getColor(R.color.yellow_wffn);
                break;
            case 4 :
                color = mContext.getResources().getColor(R.color.green_gstz);
                break;
            case 5 :
                color = mContext.getResources().getColor(R.color.blue_brtm);
                break;
            case 6 :
                color = mContext.getResources().getColor(R.color.violet_okol);
                break;
        }
        return color;
    }

    /**
     * Function to convert milliseconds time to Timer Format
     * Hours:Minutes:Seconds
     * */
    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    /**
     * Function to get Progress percentage
     * 
     * @param currentDuration
     * @param totalDuration
     * */
    public int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage = (((double) currentSeconds) / totalSeconds) * 100;

        // return percentage
        return percentage.intValue();
    }

    /**
     * Function to change progress to timer
     * 
     * @param progress
     *            -
     * @param totalDuration
     *            returns current duration in milliseconds
     * */
    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mPlayer.getDuration();
        int currentPosition = progressToTimer(seekBar.getProgress(),
                totalDuration);
        // forward or backward to certain seconds
        mPlayer.seekTo(currentPosition);
        // update timer progress again
        updateProgressBar();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // mp.release();
        // Log.d("!!!", "Media player has completed playing");
        Toast.makeText(mContext, "Playback Completes", Toast.LENGTH_SHORT)
                .show();
        // mPlayer = null;
        /*
         * mPlayer.seekTo(0); long totalDuration = mPlayer.getDuration(); long
         * currentDuration = mPlayer.getCurrentPosition();
         * songTotalDurationLabel
         * .setText(""+milliSecondsToTimer(totalDuration));
         * songCurrentDurationLabel
         * .setText(""+milliSecondsToTimer(currentDuration)); int progress =
         * (int)(getProgressPercentage(currentDuration, totalDuration));
         * songProgressBar.setProgress(progress); btnPlay.performClick();
         */
    }

    /**
     * Update timer on seekbar
     * */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (mPlayer != null) {
                long totalDuration = mPlayer.getDuration();
                long currentDuration = mPlayer.getCurrentPosition();
                songTotalDurationLabel.setText(""
                        + milliSecondsToTimer(totalDuration));
                songCurrentDurationLabel.setText(""
                        + milliSecondsToTimer(currentDuration));
                int progress = (int) (getProgressPercentage(currentDuration,
                        totalDuration));
                songProgressBar.setProgress(progress);
                mHandler.postDelayed(this, 100);
            }
        }
    };
/*
    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }*/
}

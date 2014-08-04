package com.example.jagdtest;

import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class TestActivity extends ActionBarActivity {
    private TestFragment mFragment;
    private CountDownTimer timer;
    private boolean isExam;
    private Context mContext;
    private MenuItem timeLeft; 
    private Menu mMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        if (getIntent().getExtras() != null)
            isExam = getIntent().getExtras().getBoolean("isExam");
        mContext = this;
        
        if (savedInstanceState == null) {
            mFragment = TestFragment.newInstance(isExam);
            getSupportFragmentManager().beginTransaction()
            .add(R.id.container, mFragment).commit();
        } else {
            mFragment = (TestFragment) getSupportFragmentManager().findFragmentById(R.id.container);
            getSupportFragmentManager().beginTransaction()
            .replace(R.id.container, mFragment).commit();
        }
    }
    
    public void setNewTimer(long millis){
        millis = 30000;
        if (mMenu != null){
        timeLeft = mMenu.findItem(R.id.action_timeleft);
        timeLeft.setTitle("");
        }
        if (timer != null )timer.cancel();
        if (isExam && millis != 0) {
            timer = new CountDownTimer(millis, 1000){
                public void onTick(long millisUntilFinished){
                    int minutes = (int) millisUntilFinished / 60000;
                    int seconds = (int) millisUntilFinished % 60000;
                    if (timeLeft != null)
                    timeLeft.setTitle("Category time left is " + minutes + ":" + seconds);
                    if (mContext != null && millisUntilFinished / 1000 < 10)
                    Toast.makeText(mContext, "" + (millisUntilFinished/1000), 
                            Toast.LENGTH_SHORT).show();
                    mFragment.saveTimerCurentTime(millisUntilFinished);
                }
                public void onFinish(){
                    mFragment.analyzeAndMoveToCateg();
                }
            };
            timer.start();
        }
    }
    
    public void stopTimer(){
        if (timer != null) timer.cancel();
    }
    
    public void onPause(){
        super.onPause();
        stopTimer();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.test, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_endtest) {
            if (mFragment.getColAnswered() < mFragment.getColQuestions() - 1){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(getResources().getString(R.string.alert_confirm_title));
                alertDialogBuilder
                    .setMessage(getResources().getString(R.string.end_test))
                    .setCancelable(false)
                    .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            mFragment.endTest();
                        }
                      })
                      .setNegativeButton("No",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            } else mFragment.endTest();
            
            return true;
        }
        if (id == R.id.action_timeleft) {return true;}
        return super.onOptionsItemSelected(item);
    }

}

package com.example.jagdtest;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private TextView btn_study, btn_train, btn_exam, btn_options;
        
        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container,
                    false);
            btn_study = (TextView) rootView.findViewById(R.id.btn_study);
            btn_train = (TextView) rootView.findViewById(R.id.btn_train);
            btn_exam = (TextView) rootView.findViewById(R.id.btn_exam);
            btn_options = (TextView) rootView.findViewById(R.id.btn_options);
            
            btn_study.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    onClickStudy();
                }
            });
            btn_train.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    onClickTraining();
                }
            });
            btn_exam.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    onClickExam();
                }
            });
            btn_options.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    onClickOptions();
                }
            });
            return rootView;
        }
        
        private void onClickStudy(){
            
        }
        private void onClickTraining(){
            Intent intent = new Intent();
            intent.setClass(getActivity(), TestActivity.class);
            startActivity(intent);
        }
        private void onClickExam(){
            Intent intent = new Intent();
            intent.setClass(getActivity(), TestActivity.class);
            intent.putExtra("isExam", true);
            startActivity(intent);
        }
        private void onClickOptions(){
            
        }
    }

}

package com.example.jagdtest;

import java.util.HashMap;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class CurentAnswer implements Parcelable{
    private int points = 0;
    private int maxPoints = 0;
    private HashMap<String, Boolean> answers;
    private boolean isWrong = false;
    
    public CurentAnswer(int points, String answer, boolean answered){
        if(!answered) {
            isWrong = true;
            points = 0;
        }
        if (!isWrong) this.points += points;
        //if (points == 0) isWrong = true; 
        if (answers == null) answers = new HashMap<String, Boolean>();
        answers.put(answer, answered);
    }
    
    public HashMap<String, Boolean> getAnswers(){
        return answers;
    }
    
    public int getPoints(){
        return points;
    }
    
    public void setPoints(int points){
        if(!isWrong //|| points == 0
                ) this.points = points;
        //if (points == 0) {this.isWrong = true;}
    }
    
    public int getMaxPoints(){
        return maxPoints;
    }
    
    public void setMaxPoints(int points){
        this.maxPoints = points;
    }
    
    public boolean isWrongAnswer(){
        return isWrong;
    }
    
    public void addChoice(int points, String answer, boolean answered){
        if(!answered) {
            isWrong = true;
            points = 0;
        } else addPoints(points);
        answers.put(answer, answered);
    }
    
    public void addPoints(int points){
        if(!isWrong) this.points += points;
        /*if(points == 0) {
            this.points = points;
            isWrong = true;
        }*/
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(points);
        dest.writeInt(maxPoints);
        Bundle bundle = new Bundle();
        bundle.putSerializable("map", answers);
        dest.writeBundle(bundle);
        dest.writeValue(Boolean.valueOf(isWrong));
    }
    
    public static final Parcelable.Creator<CurentAnswer> CREATOR = new Parcelable.Creator<CurentAnswer>() {
        public CurentAnswer createFromParcel(Parcel pc) {
        return new CurentAnswer(pc);
        }

        @Override
        public CurentAnswer[] newArray(int size) {
            return new CurentAnswer[size];
        }
        };
        @SuppressWarnings("unchecked")
        public CurentAnswer(Parcel pc){
            points = pc.readInt();
            maxPoints = pc.readInt();
            Bundle b = pc.readBundle();
            answers = (HashMap<String, Boolean>) b.getSerializable("map");
            isWrong = (boolean) pc.readValue(Boolean.class.getClassLoader());
        }
}

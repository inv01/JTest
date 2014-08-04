package com.example.jagdtest;

import java.util.HashSet;

public class Question {
    private int _id;
    private int idNumber;
    private int idCateg;
    private String question;
    private int questionType;
    private int points;
    private int extraPoints;
    private int status;
    private int idExp;
    private String explanation;
    private String category;
    private HashSet<Answer> answers;
    
    public Question(){}
    public Question(int _id, int idNumber, int idCateg, 
            String question, int questionType, int points, 
            int extraPoints, int status, int idExp,
            String explanation, String category){
        this._id = _id;
        this.idCateg = idCateg;
        this.idExp = idExp;
        this.idNumber = idNumber;
        this.question = question;
        this.questionType = questionType;
        this.points = points;
        this.extraPoints = extraPoints;
        this.status = status;
        this.explanation = explanation;
        this.category = category;
    }
    
    public int getMaxPoints(){
        if (questionType == 2 || questionType == 4) return (points);
        if (answers == null) return 0;
        if (answers.size() == 1)  return (points);
        int rightAnswers = 0;
        for (Answer a : answers){
            if(a.isAnswer()) rightAnswers++;
        }
        return (rightAnswers*points + extraPoints);
    }

    // find number of all correct answers
    public int getNumRightAnswers(){
        if (questionType == 2 || questionType == 4) return 1;
        if (answers == null) return 0;
        if (answers.size() == 1)  return 1;
        int i = 0;
        for (Answer a : answers) {
            if (a.isAnswer()) i++;
        }
        return i;
    }
    
    public void setAnswers(HashSet<Answer> hs){
        this.answers = hs;
        if (hs == null) {this.answers = new HashSet<Answer>();}
        if(this.questionType == 2){
            this.answers.add(new Answer(this._id, "0", true));
            this.answers.add(new Answer(this._id, "1", false));
        }
        if(this.questionType == 4){
            this.answers.add(new Answer(this._id, "0", false));
            this.answers.add(new Answer(this._id, "1", true));
        }
    }
    
    public HashSet<Answer> getAnswers(){
        return answers;
    }
    

    public HashSet<Answer> getRightAnswers(){
        HashSet<Answer> hs = new HashSet<Answer>();
        for(Answer a : answers){
            if(a.isAnswer())hs.add(a);
        }
        return hs;
    }
    
    public String getCategName(){
        return category;
    }
    public String getExplanation(){
        return explanation;
    }
    public void setIdNumber(int idNumber){
        this.idNumber = idNumber;
    }
    public void setId(int _id){
        this._id = _id;
    }
    public void setIdCateg(int idCateg){
        this.idCateg = idCateg;
    }
    public void setQuestion(String question){
        this.question = question;
    }
    public void setQuestionType(int questionType){
        this.questionType = questionType;
    }
    public void setPoints(int points){
        this.points = points;
    }
    public void setExtraPoints(int extraPoints){
        this.extraPoints = extraPoints;
    }
    public void setStatus(int status){
        this.status = status;
    }
    public void setIdExp(int idExp){
        this.idExp = idExp;
    }
    public int getId(){
        return this._id;
    }
    public int getIdNumber(){
        return this.idNumber;
    }
    public int getIdCateg(){
        return this.idCateg;
    }
    public int getQuestionType(){
        return this.questionType;
    }
    public int getPoints(){
        return this.points;
    }
    public int getExtraPoints(){
        return this.extraPoints;
    }
    public int getStatus(){
        return this.status;
    }
    public int getIdExp(){
        return this.idExp;
    }
    public String getQuestion(){
        return this.question;
    }
}

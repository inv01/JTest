package com.example.jagdtest;

public class Answer {
private String answer;
private boolean is_answer;
private int id_question;

public Answer(int id_question,String answer,boolean is_answer){
    this.id_question = id_question;
    this.answer = answer;
    this.is_answer = is_answer;
}

public String getAnswer() {
    return answer;
}

public void setAnswer(String answer) {
    this.answer = answer;
}

public boolean isAnswer() {
    return is_answer;
}

public void setIsAnswer(boolean is_answer) {
    this.is_answer = is_answer;
}

public int getIdQ() {
    return id_question;
}

public void setIdQ(int id_question) {
    this.id_question = id_question;
}

}

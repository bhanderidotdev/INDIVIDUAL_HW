package application;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import databasePart1.DatabaseHelper;

/**
 * The Question class represents a question entity in the system.
 * It contains the Attributes question ID, text, author, and associated answers.
 */

public class Question {
    private int id;
    private String text;
    private String author;
    private List<Answer> answers;

    // Constructor to initialize a new Question object.
    public Question(int id, String text, String author) {
        this.id = id;
        this.text = text;
        this.author = author;
        this.answers = new ArrayList<>();
    }
    
    // Question constructor to reconstruct question object from answer id
    public Question(int answerId, DatabaseHelper databaseHelper) {
    	try{
            ResultSet rs = databaseHelper.getQuestionForAnswer(answerId);
            while (rs.next()) {
                this.id = rs.getInt("id");
                this.text = rs.getString("text");
                this.author = rs.getString("author");
            }
        } catch (SQLException ex) {
            System.out.println("invalid question id");
        }
    }
    
    // Updates the question text while ensuring it is valid.
    public void setText(String text) {
        if (text != null && !text.trim().isEmpty() && text.length() <= 500) {
            this.text = text;
        }
    }
    
    // Adds an answer to the question.
    public void addAnswer(Answer answer) {
        if (answer != null) {
            answers.add(answer);
        }
    }
    
    // Removes an answer from the question.
    public void removeAnswer(Answer answer) {
        answers.remove(answer);
    }
    
    public void clearAnswers() {
    	answers.clear();
    }
    
    // toString for search page
    public String toString() {
        return "ID: " + id + " | " + text + " (" + author + ")";
    }
    
    public int getId() { return id; }
    public String getText() { return text; }
    public String getAuthor() { return author; }
    public List<Answer> getAnswers() { return answers; }
    
    public void setId(int id) { this.id = id; }
}
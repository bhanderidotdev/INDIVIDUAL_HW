package application;

/**
 * The Answer class represents an answer entity in the system.
 * It contains details such as answer ID, text, author, and the associated question ID.
 */

public class Answer {
    private int id;
    private String text;
    private String author;
    private int questionId;

    // Constructor to initialize a new Answer object.
    public Answer(int id, String text, String author, int questionId) {
        this.id = id;
        this.text = text;
        this.author = author;
        this.questionId = questionId;
    }
    
    // Updates the answer text while ensuring it is valid.
    public void setText(String text) {
        if (text != null && !text.trim().isEmpty() && text.length() <= 500) {
            this.text = text;
        }
    }
    
    // toString for search page
    public String toString() {
        return "ID: " + id + " | " + text + " (" + author + ")";
    }
    
    public int getId() { return id; }
    public String getText() { return text; }
    public String getAuthor() { return author; }
    public int getQuestionId() { return questionId; }
    
    public void setId(int id) { this.id = id; }
}
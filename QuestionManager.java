package application;

import databasePart1.DatabaseHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The QuestionManager class handles CRUD operations for Question objects,
 */

public class QuestionManager {
    private final DatabaseHelper databaseHelper;

    // Constructor to initialize the QuestionManager.
    public QuestionManager(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    // Saves a new question to the database, preventing duplicates.
    public void saveQuestion(Question question) {
        String checkQuery = "SELECT COUNT(*) FROM questions WHERE text = ? AND author = ?";
        String insertQuery = "INSERT INTO questions (text, author) VALUES (?, ?)";

        try (Connection conn = databaseHelper.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            
            checkStmt.setString(1, question.getText());
            checkStmt.setString(2, question.getAuthor());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Question already exists, don't insert duplicate
            }

            insertStmt.setString(1, question.getText());
            insertStmt.setString(2, question.getAuthor());
            insertStmt.executeUpdate();

            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    question.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Loads all questions from the database.
    public List<Question> getAllQuestions() {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT id, text, author FROM questions";
        try (Connection conn = databaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                questions.add(new Question(rs.getInt("id"), rs.getString("text"), rs.getString("author")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }
    
    private Question selectedMainQuestion = null; // Store the last clicked main question

    private void handleMainQuestionClick(Question question) {
        selectedMainQuestion = question; // Update the selected question
        System.out.println("Main Question Selected: " + question.getText());
    }

    private Question getSelectedMainQuestion() {
        return selectedMainQuestion;
    }
    
    public List<Question> getSubSetQuestionsForMainQuestion(int qID) {
        List<Question> subsetQuestions = new ArrayList<>();
        String query = "SELECT id, text, author FROM subSetQuestions WHERE qID = ?";

        try (Connection conn = databaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, qID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                subsetQuestions.add(new Question(rs.getInt("id"), rs.getString("text"), rs.getString("author")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subsetQuestions;
    }


    // Creates a new question and adds it to the database.
    public Question createQuestion(int id, String text, String author) {
        if (text == null || text.trim().isEmpty() || text.length() > 500) {
            return null; // Invalid question text
        }
        Question question = new Question(id, text, author);
        saveQuestion(question);
        return question;
    }
    
    public void createSubSetQuestion(int qID, String text, String author) {
        String query = "INSERT INTO subSetQuestions (qID, text, author) VALUES (?, ?, ?)";

        try (Connection conn = databaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, qID);
            pstmt.setString(2, text);
            pstmt.setString(3, author);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int newSqID = generatedKeys.getInt(1);
                    System.out.println("SubSetQuestion created successfully with ID: " + newSqID + ", linked to main question: " + qID);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Deletes a question if the user is the author or an admin.
    public boolean deleteQuestion(int questionId, String userName, boolean isAdmin) {
        String checkQuery = "SELECT author FROM questions WHERE id = ?";
        String deleteQuery = "DELETE FROM questions WHERE id = ?";
        try (Connection conn = databaseHelper.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
            checkStmt.setInt(1, questionId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                String author = rs.getString("author");
                if (author.equals(userName) || isAdmin) {
                    deleteStmt.setInt(1, questionId);
                    return deleteStmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Updates an existing question if the user is the author.
    public boolean editQuestion(int questionId, String newText, String userName) {
        String query = "UPDATE questions SET text = ? WHERE id = ? AND author = ?";
        try (Connection conn = databaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newText);
            pstmt.setInt(2, questionId);
            pstmt.setString(3, userName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean editsubSetQuestion(int questionId, String newText, String userName) {
        String query = "UPDATE subSetQuestions SET text = ? WHERE id = ? AND author = ?";
        try (Connection conn = databaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newText);
            pstmt.setInt(2, questionId);
            pstmt.setString(3, userName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
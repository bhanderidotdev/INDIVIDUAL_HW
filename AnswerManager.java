package application;

import databasePart1.DatabaseHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The AnswerManager class handles CRUD operations for Answer objects,
 * ensuring they are persisted in the database.
 */
public class AnswerManager {
    private final DatabaseHelper databaseHelper;

    // Constructor to initialize the AnswerManager.
    public AnswerManager(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * Saves a new answer to the database, preventing duplicates.
     */
    public void saveAnswer(Answer answer) {
        String checkQuery = "SELECT COUNT(*) FROM answers WHERE text = ? AND author = ? AND questionId = ?";
        String insertQuery = "INSERT INTO answers (text, author, questionId) VALUES (?, ?, ?)";
        try (Connection conn = databaseHelper.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            
            checkStmt.setString(1, answer.getText());
            checkStmt.setString(2, answer.getAuthor());
            checkStmt.setInt(3, answer.getQuestionId());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Answer already exists, don't insert duplicate
            }

            insertStmt.setString(1, answer.getText());
            insertStmt.setString(2, answer.getAuthor());
            insertStmt.setInt(3, answer.getQuestionId());
            insertStmt.executeUpdate();

            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    answer.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Saves a new subset answer to the database, preventing duplicates.
     */
    public void saveSubSetAnswer(Answer answer) {
        String checkQuery = "SELECT COUNT(*) FROM subSetAnswers WHERE text = ? AND author = ? AND saID = ?";
        String insertQuery = "INSERT INTO subSetAnswers (text, author, saID) VALUES (?, ?, ?)";
        try (Connection conn = databaseHelper.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            checkStmt.setString(1, answer.getText());
            checkStmt.setString(2, answer.getAuthor());
            checkStmt.setInt(3, answer.getQuestionId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Duplicate subset answer detected, not inserting: " + answer.getText());
                return; // Prevent duplicate insertions
            }

            // Ensure `saID` correctly references subSetQuestions.id
            insertStmt.setString(1, answer.getText());
            insertStmt.setString(2, answer.getAuthor());
            insertStmt.setInt(3, answer.getQuestionId());
            System.out.println("Inserting subset answer for saID: " + answer.getQuestionId());
            insertStmt.executeUpdate();

            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    answer.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads all answers for a specific question from the database.
     * CORRECTION: The query now filters on questionId rather than id.
     */
    public List<Answer> getAnswersForQuestion(int questionId) {
        List<Answer> answers = new ArrayList<>();
        // Corrected query: use questionId in WHERE clause.
        String query = "SELECT id, text, author, questionId FROM answers WHERE questionId = ?";
        try (Connection conn = databaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    answers.add(new Answer(
                        rs.getInt("id"), 
                        rs.getString("text"), 
                        rs.getString("author"), 
                        rs.getInt("questionId")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Fetching main question answers for question ID: " + questionId);
        return answers;
    }
    
    /**
     * Loads all answers for a specific subset question from the database.
     */
    public List<Answer> getSubSetAnswersForQuestion(int subsetQuestionID) {
        List<Answer> answers = new ArrayList<>();
        String query = "SELECT * FROM subSetAnswers WHERE saID = ?";
        try (Connection conn = databaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, subsetQuestionID);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    answers.add(new Answer(
                        rs.getInt("id"),
                        rs.getString("text"),
                        rs.getString("author"),
                        rs.getInt("saID")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Fetching subset answers for question ID: " + subsetQuestionID);
        return answers;
    }

    /**
     * Creates a new answer and adds it to the database.
     * Note: Instead of manually assigning an ID, the database auto-generates it.
     */
    public Answer createAnswer(int id, String text, String author, int questionId) {
        if (text == null || text.trim().isEmpty() || text.length() > 500) {
            return null; // Invalid answer text
        }
        Answer answer = new Answer(id, text, author, questionId);
        saveAnswer(answer);
        return answer;
    }
    
    /**
     * Creates a new subset answer and adds it to the database.
     */
    public Answer createSubSetAnswer(int id, String text, String author, int subsetQuestionID) {
        if (text == null || text.trim().isEmpty() || text.length() > 500) {
            return null; // Invalid answer text
        }
        Answer answer = new Answer(id, text, author, subsetQuestionID);  // Uses saID
        saveSubSetAnswer(answer);
        return answer;
    }

    /**
     * Updates an existing answer if the user is the author.
     */
    public boolean updateAnswer(int answerId, String newText, String userName) {
        String query = "UPDATE answers SET text = ? WHERE id = ? AND author = ?";
        try (Connection conn = databaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newText);
            pstmt.setInt(2, answerId);
            pstmt.setString(3, userName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Updates a subset answer if the user is the author.
     * Corrected table name to "subSetAnswers".
     */
    public boolean updatesubSetAnswer(int answerId, String newText, String userName) {
        String query = "UPDATE subSetAnswers SET text = ? WHERE id = ? AND author = ?";
        try (Connection conn = databaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newText);
            pstmt.setInt(2, answerId);
            pstmt.setString(3, userName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes an answer if the user is the author or an admin.
     */
    public boolean deleteAnswer(int answerId, String userName, boolean isAdmin) {
        String checkQuery = "SELECT author FROM answers WHERE id = ?";
        String deleteQuery = "DELETE FROM answers WHERE id = ?";
        try (Connection conn = databaseHelper.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
            checkStmt.setInt(1, answerId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                String author = rs.getString("author");
                if (author.equals(userName) || isAdmin) {
                    deleteStmt.setInt(1, answerId);
                    return deleteStmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
package databasePart1;

import java.sql.*;
import java.util.UUID;
import application.User;

/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, handling invitation codes,
 * and providing search and update methods.
 */
public class DatabaseHelper {

    // JDBC driver and database URL for H2 database
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:~/FoundationDatabase";

    // Database credentials
    static final String USER = "sa";
    static final String PASS = "";

    private Connection connection = null;
    private Statement statement = null;
    
    /**
     * Connects to the database, loads the JDBC driver, and creates tables if they don't exist.
     */
    public void connectToDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER); // Load the JDBC driver
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();
            // Uncomment the next line to clear the database and start fresh.
             //statement.execute("DROP ALL OBJECTS");
            
            // Optionally, you could delete all tables before recreating them.
            // deleteAllTables();

            createTables();  // Create all necessary tables
            
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
    }

    /**
     * Creates all necessary tables in the database.
     * Note: The "answers" table is designed without a UNIQUE constraint on (userName, questionId),
     * which allows multiple answers for the same question.
     */
    private void createTables() throws SQLException {
        // Invitation Codes Table
        String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
                + "code VARCHAR(10) PRIMARY KEY, "
                + "isUsed BOOLEAN DEFAULT FALSE)";
        statement.execute(invitationCodesTable);

        // Users Table
        String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userName VARCHAR(255) UNIQUE, "
                + "password VARCHAR(255), "
                + "role VARCHAR(20))";
        statement.execute(userTable);
        
        // Main Questions Table
        String questionsTable = "CREATE TABLE IF NOT EXISTS questions ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "text VARCHAR(500), "
                + "author VARCHAR(255))";
        statement.execute(questionsTable);

        // Subset Questions Table (linked to main questions)
        String subSetQuestionsTable = "CREATE TABLE IF NOT EXISTS subSetQuestions ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "qID INT, "  // Foreign key linking to questions(id)
                + "text VARCHAR(500), "
                + "author VARCHAR(255), "
                + "FOREIGN KEY (qID) REFERENCES questions(id) ON DELETE CASCADE)";
        statement.execute(subSetQuestionsTable);
        
        // Answers Table
        // Note: There is NO UNIQUE constraint here on (userName, questionId),
        // which means multiple answers can be saved for the same question.
        String answersTable = "CREATE TABLE IF NOT EXISTS answers ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "text VARCHAR(500), "
                + "author VARCHAR(255), "
                + "questionId INT, "
                + "resolved BOOLEAN DEFAULT FALSE, " 
                + "FOREIGN KEY (questionId) REFERENCES questions(id) ON DELETE CASCADE)";
        statement.execute(answersTable);
        
        // Subset Answers Table (linked to subset questions)
        // Removed any UNIQUE constraint so that multiple answers can be added.
        String subsetAnswersTable = "CREATE TABLE IF NOT EXISTS subSetAnswers ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "author VARCHAR(255) NOT NULL, "
                + "text VARCHAR(255) NOT NULL, "
                + "saID INT, "  // Foreign key linking to subSetQuestions(id)
                + "resolved BOOLEAN DEFAULT FALSE, " 
                // Removed UNIQUE clause to allow multiple answers.
                + "FOREIGN KEY (saID) REFERENCES subSetQuestions(id) ON DELETE CASCADE)";
        statement.execute(subsetAnswersTable);

        System.out.println("Database tables created successfully.");
    }

    /**
     * Checks if the cse360users table is empty.
     */
    public boolean isDatabaseEmpty() throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM cse360users";
        ResultSet resultSet = statement.executeQuery(query);
        if (resultSet.next()) {
            return resultSet.getInt("count") == 0;
        }
        return true;
    }

    /**
     * Registers a new user.
     */
    public void register(User user) throws SQLException {
        String insertUser = "INSERT INTO cse360users (userName, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Updates the password for an existing user.
     */
    public void updateUserPassword(String userName, String newPassword) throws SQLException {
        String query = "UPDATE cse360users SET password = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, userName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Validates a user's login credentials.
     */
    public boolean login(User user) throws SQLException {
        String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    /**
     * Returns the current database connection.
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
        }
        return connection;
    }
    
    /**
     * Checks if a user already exists.
     */
    public boolean doesUserExist(String userName) {
        String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Retrieves the role of a user.
     */
    public String getUserRole(String userName) {
        String query = "SELECT role FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Generates a new invitation code.
     */
    public String generateInvitationCode() {
        String code = UUID.randomUUID().toString().substring(0, 4);
        String query = "INSERT INTO InvitationCodes (code) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return code;
    }
    
    /**
     * Validates an invitation code and marks it as used.
     */
    public boolean validateInvitationCode(String code) {
        String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                markInvitationCodeAsUsed(code);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Marks an invitation code as used.
     */
    private void markInvitationCodeAsUsed(String code) {
        String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ------------------ Search and Retrieval Methods ------------------

    /**
     * Performs a combined search for questions and answers (case-insensitive).
     */
    public ResultSet searchQuestionsAndAnswers(String keyword) throws SQLException {
        String searchQuery = "SELECT q.id AS questionId, q.text AS questionText, q.author AS questionAuthor, " +
                             "a.id AS answerId, a.text AS answerText, a.author AS answerAuthor " +
                             "FROM questions q LEFT JOIN answers a ON q.id = a.questionId " +
                             "WHERE LOWER(q.text) LIKE LOWER(?) OR LOWER(a.text) LIKE LOWER(?)";
        PreparedStatement pstmt = connection.prepareStatement(searchQuery);
        String likeKeyword = "%" + keyword + "%";
        pstmt.setString(1, likeKeyword);
        pstmt.setString(2, likeKeyword);
        return pstmt.executeQuery();
    }
    
    /**
     * Searches only the questions table (case-insensitive).
     */
    public ResultSet searchQuestions(String keyword) throws SQLException {
        String query = "SELECT id, text, author FROM questions WHERE LOWER(text) LIKE LOWER(?)";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setString(1, "%" + keyword + "%");
        return pstmt.executeQuery();
    }
    
    /**
     * Searches only the answers table (case-insensitive).
     */
    public ResultSet searchAnswers(String keyword) throws SQLException {
        String query = "SELECT id, text, author, questionId FROM answers WHERE LOWER(text) LIKE LOWER(?)";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setString(1, "%" + keyword + "%");
        return pstmt.executeQuery();
    }
    
    /**
     * Retrieves all answers for a specific question.
     */
    public ResultSet getAnswersForQuestion(int questionId) throws SQLException {
        String query = "SELECT id, text, author FROM answers WHERE questionId = ?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setInt(1, questionId);
        return pstmt.executeQuery();
    }
    
    /**
     * Retrieves the question associated with a specific answer.
     */
    public ResultSet getQuestionForAnswer(int answerId) throws SQLException {
        String query = "SELECT q.id, q.text, q.author FROM questions q " +
                       "JOIN answers a ON q.id = a.questionId WHERE a.id = ?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setInt(1, answerId);
        return pstmt.executeQuery();
    }

    // ------------------ Utility Methods ------------------

    /**
     * Prints the structure of the 'questions' table.
     */
    public void printTableStructure() {
        try (ResultSet rs = checkTableStructure()) {
            System.out.println("Columns in 'questions' table:");
            while (rs.next()) {
                System.out.println(rs.getString("COLUMN_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Checks the table structure for the 'questions' table.
     */
    public ResultSet checkTableStructure() throws SQLException {
        String query = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'QUESTIONS'";
        PreparedStatement pstmt = connection.prepareStatement(query);
        return pstmt.executeQuery();
    }
    
    /**
     * Deletes all tables (drops all objects) in the database.
     */
    public void deleteAllTables() {
        String query = "DROP ALL OBJECTS";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(query);
            System.out.println("All tables deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
  
    /**
     * Prints all contents of a given table.
     */
    public void printTableContents(String tableName) {
        String query = "SELECT * FROM " + tableName;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n--- Contents of Table: " + tableName + " ---");

            int columnCount = rs.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(rs.getMetaData().getColumnName(i) + "\t");
            }
            System.out.println("\n-------------------------------------------------");

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Example method to update the 'resolved' status in subSetAnswers.
     */
    public void setSubSetResolved(int answerID) throws SQLException {
        System.out.println("AnswerID is: " + answerID);
        boolean currentStatus = false;

        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement("SELECT resolved FROM subSetAnswers WHERE id = ?")) {

            checkStmt.setInt(1, answerID);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    currentStatus = rs.getBoolean("resolved");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        boolean newStatus = !currentStatus;
        try (Connection conn = getConnection();
             PreparedStatement updateStmt = conn.prepareStatement("UPDATE subSetAnswers SET resolved = ? WHERE id = ?")) {
            updateStmt.setBoolean(1, newStatus);
            updateStmt.setInt(2, answerID);
            updateStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Example method to update the 'resolved' status in answers.
     */
    public void setResolved(int answerID) throws SQLException {
        System.out.println("AnswerID is: " + answerID);
        boolean currentStatus = false;

        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement("SELECT resolved FROM answers WHERE id = ?")) {

            checkStmt.setInt(1, answerID);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    currentStatus = rs.getBoolean("resolved");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        boolean newStatus = !currentStatus;
        try (Connection conn = getConnection();
             PreparedStatement updateStmt = conn.prepareStatement("UPDATE answers SET resolved = ? WHERE id = ?")) {
            updateStmt.setBoolean(1, newStatus);
            updateStmt.setInt(2, answerID);
            updateStmt.executeUpdate();
            System.out.println("Resolved status updated to: " + newStatus + " for answer ID: " + answerID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Checks if a specific subSetAnswer is marked as resolved.
     */
    public boolean isSubSetResolved(int answerID) {
        String query = "SELECT resolved FROM subSetAnswers WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, answerID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("resolved");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Checks if a specific answer is marked as resolved.
     */
    public boolean isResolved(int answerID) {
        String query = "SELECT resolved FROM answers WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, answerID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("resolved");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Closes the database connection and statement.
     */
    public void closeConnection() {
        try {
            if (statement != null) statement.close();
        } catch (SQLException se2) {
            se2.printStackTrace();
        }
        try {
            if (connection != null) connection.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
}
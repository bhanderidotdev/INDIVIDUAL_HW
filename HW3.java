package application;

import databasePart1.DatabaseHelper;
import java.sql.Connection;
import java.sql.Statement;

/**
 * <h1>HW3Test</h1>
 * <p>
 * A standalone testing class for verifying core functionalities of a
 * Java-based educational QandA application. This class focuses on creating
 * automated test cases for common actions such as adding, linking, editing,
 * deleting, and verifying questions and answers in the system.
 * </p>
 * 
 * <h2>Features Tested</h2>
 * <ul>
 *   <li>Creating and saving multiple questions</li>
 *   <li>Linking answers to specific questions</li>
 *   <li>Validating answer updates and user access control</li>
 *   <li>Deleting questions and verifying deletion</li>
 *   <li>Basic success-path answer submission</li>
 * </ul>
 *
 * <p>
 * These test cases are part of Homework 3, designed to ensure familiarity with
 * automated testing, exception handling, and JavaDoc creation.
 * </p>
 *
 * <p><b>Copyright:</b> DIVY MUKESHBHAI BHANDERI ©️ 2025</p>
 * 
 * @author DIVY MUKESHBHAI BHANDERI
 * @version 1.3 — HW3 Final Version with Renamed Test Methods
 */
public class HW3 {

    private static QuestionManager questionManager = new QuestionManager(new DatabaseHelper());
    private static AnswerManager answerManager = new AnswerManager(new DatabaseHelper());

    /**
     * Entry point for executing all automated test cases.
     */
    public static void main(String[] args) {
        System.out.println("===== Running HW3 Tests =====");
        clearDatabase();

        testSaveMultipleQuestions();
        testAnswerBelongsToCorrectQuestion();
        testUpdateAnswerWithWrongUser();
        testDeleteQuestion();
        testSubmitValidAnswer();
    }

    /**
     * Clears all data from the database to ensure test isolation.
     */
    private static void clearDatabase() {
        try (Connection conn = new DatabaseHelper().getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("DELETE FROM answers");
            stmt.executeUpdate("DELETE FROM questions");
            stmt.executeUpdate("DELETE FROM subSetAnswers");
            stmt.executeUpdate("DELETE FROM subSetQuestions");

            System.out.println("Database cleared successfully.");

        } catch (Exception e) {
            System.out.println("Something went wrong while clearing the database: " + e.getMessage());
        }
    }

    /**
     * Verifies that multiple questions can be saved and retrieved correctly.
     */
    public static void testSaveMultipleQuestions() {
        Question q1 = new Question(10, "What is polymorphism?", "Alice");
        Question q2 = new Question(11, "Explain abstraction.", "Bob");
        questionManager.saveQuestion(q1);
        questionManager.saveQuestion(q2);

        boolean foundAll = questionManager.getAllQuestions().stream()
                .anyMatch(q -> q.getText().equals("What is polymorphism?")) &&
                questionManager.getAllQuestions().stream()
                .anyMatch(q -> q.getText().equals("Explain abstraction."));

        System.out.println(foundAll ? "testSaveMultipleQuestions: PASSED"
                                    : "testSaveMultipleQuestions: FAILED");
    }

    /**
     * Checks if an answer is correctly associated with the question it belongs to.
     */
    public static void testAnswerBelongsToCorrectQuestion() {
        Question q = new Question(20, "What is encapsulation?", "Charlie");
        questionManager.saveQuestion(q);

        Answer answer = new Answer(20, "Encapsulation is wrapping data and methods together.", "Dave", q.getId());
        answerManager.saveAnswer(answer);

        Answer retrieved = answerManager.getAnswersForQuestion(q.getId()).stream().findFirst().orElse(null);
        boolean correct = retrieved != null && retrieved.getQuestionId() == q.getId();

        System.out.println(correct ? "testAnswerBelongsToCorrectQuestion: PASSED"
                                   : "testAnswerBelongsToCorrectQuestion: FAILED");
    }

    /**
     * Attempts to update an answer using the wrong user; the update should fail.
     */
    public static void testUpdateAnswerWithWrongUser() {
        Question q = new Question(30, "What is inheritance?", "Eve");
        questionManager.saveQuestion(q);

        Answer answer = new Answer(30, "Inheritance allows classes to inherit features.", "Frank", q.getId());
        answerManager.saveAnswer(answer);

        boolean updated = answerManager.updateAnswer(answer.getId(), "Trying unauthorized edit", "Hacker");

        System.out.println(!updated ? "testUpdateAnswerWithWrongUser: PASSED"
                                    : "testUpdateAnswerWithWrongUser: FAILED");
    }

    /**
     * Saves and deletes a question, then checks that it is removed from the database.
     */
    public static void testDeleteQuestion() {
        Question q = new Question(40, "Explain interfaces in Java.", "Grace");
        questionManager.saveQuestion(q);

        boolean deleted = questionManager.deleteQuestion(q.getId(), "Grace", true);

        boolean stillThere = questionManager.getAllQuestions().stream()
                .anyMatch(question -> question.getId() == q.getId());

        System.out.println(deleted && !stillThere ? "testDeleteQuestion: PASSED"
                                                  : "testDeleteQuestion: FAILED");
    }

    /**
     * Submits a valid answer to a question and verifies it is stored correctly.
     */
    public static void testSubmitValidAnswer() {
        Question q = new Question(50, "What is a constructor?", "Henry");
        questionManager.saveQuestion(q);

        Answer answer = new Answer(50, "A constructor initializes a new object.", "Ivy", q.getId());
        answerManager.saveAnswer(answer);

        boolean stored = answerManager.getAnswersForQuestion(q.getId()).stream()
                .anyMatch(a -> a.getText().equals("A constructor initializes a new object."));

        System.out.println(stored ? "testSubmitValidAnswer: PASSED"
                                  : "testSubmitValidAnswer: FAILED");
    }
}

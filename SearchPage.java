package application;

import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;
import databasePart1.DatabaseHelper;

/**
 * The SearchPage class provides a UI for searching questions and answers.
 */
// Removed extends Application 
public class SearchPage{

    private DatabaseHelper databaseHelper;
    private final User user;
    private final Stage primaryStage;

    // --- UI controls for the Questions Tab ---
    private TextField questionSearchField;
    private Button questionSearchButton;
    private ListView<Question> questionListView;
    private ListView<Answer> questionAnswerListView;

    // --- UI controls for the Answers Tab ---
    private TextField answerSearchField;
    private Button answerSearchButton;
    private ListView<Answer> answerListView;
    private Label relatedQuestionLabel;
    
    public SearchPage(User user, DatabaseHelper databaseHelper, Stage primaryStage) {
        this.user = user;
        this.databaseHelper = databaseHelper;
        this.primaryStage = primaryStage;
    }
    
    // Changed from start(Stage primaryStage) to show
    public void show() {
        primaryStage.setTitle("Search Questions and Answers");

        // Initialize the database connection
        databaseHelper = new DatabaseHelper();
        try {
            databaseHelper.connectToDatabase();
        } catch (SQLException ex) {
            showAlert("Error connecting to database: " + ex.getMessage());
        }

        // Create a TabPane with two tabs: Questions and Answers
        TabPane tabPane = new TabPane();

        // Questions Tab
        Tab questionsTab = new Tab("Questions");
        questionsTab.setContent(createQuestionsTab());
        questionsTab.setClosable(false);

        // Answers Tab
        Tab answersTab = new Tab("Answers");
        answersTab.setContent(createAnswersTab());
        answersTab.setClosable(false);

        tabPane.getTabs().addAll(questionsTab, answersTab);

        Scene scene = new Scene(tabPane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Creates the UI for the Questions Tab.
     */
    private VBox createQuestionsTab() {
        questionSearchField = new TextField();
        questionSearchField.setPromptText("Enter keyword to search questions...");
        questionSearchButton = new Button("Search Questions");
        HBox searchBox = new HBox(10, questionSearchField, questionSearchButton);
        searchBox.setPadding(new Insets(10));

        // ListView to display question search results
        questionListView = new ListView<>();
        questionListView.setPrefHeight(250);

        // ListView to display answers for the selected question
        questionAnswerListView = new ListView<>();
        questionAnswerListView.setPrefHeight(150);

        VBox layout = new VBox(10, searchBox, new Label("Questions:"), questionListView,
                new Label("Answers for selected question:"), questionAnswerListView);
        layout.setPadding(new Insets(10));

        // Set search button action
        questionSearchButton.setOnAction(e -> searchQuestions());

        // When a question is selected, load its answers.
        questionListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                loadAnswersForQuestion(newSel.getId());
            }
        });
        // Double click on question goes to AnswerPage
        questionListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Question selectedItem = questionListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    AnswerPage answerPage = new AnswerPage(new AnswerManager(databaseHelper), user, databaseHelper, primaryStage, selectedItem);
                    answerPage.show();
                }
            }
        });
        
        // When a answer from questionListView is selected, go to question
        questionAnswerListView.setOnMouseClicked(event -> {
        	if (event.getClickCount() == 2) {
                Question selectedItem = questionListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    AnswerPage answerPage = new AnswerPage(new AnswerManager(databaseHelper), user, databaseHelper, primaryStage, selectedItem);
                    answerPage.show();
                }
            }
        });

        return layout;
    }

    /**
     * Creates the UI for the Answers Tab.
     */
    private VBox createAnswersTab() {
        answerSearchField = new TextField();
        answerSearchField.setPromptText("Enter keyword to search answers...");
        answerSearchButton = new Button("Search Answers");
        HBox searchBox = new HBox(10, answerSearchField, answerSearchButton);
        searchBox.setPadding(new Insets(10));

        // ListView to display answer search results
        answerListView = new ListView<>();
        answerListView.setPrefHeight(250);

        // Label to show the question related to the selected answer
        relatedQuestionLabel = new Label("Related question will appear here.");

        VBox layout = new VBox(10, searchBox, new Label("Answers:"), answerListView,
                new Label("Question for selected answer:"), relatedQuestionLabel);
        layout.setPadding(new Insets(10));

        // Set search button action
        answerSearchButton.setOnAction(e -> searchAnswers());

        // When an answer is selected, load the related question.
        answerListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                loadQuestionForAnswer(newSel.getId());
            }
        });
        
        answerListView.setOnMouseClicked(event -> {
        	if (event.getClickCount() == 2) {
                Answer selectedItem = answerListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    System.out.println("Double-clicked on: " + selectedItem);
                    // Reconstruct Question from answerId
                    Question q = new Question(selectedItem.getId(), databaseHelper);
                    // Create answerPage and go to it
                    AnswerPage answerPage = new AnswerPage(new AnswerManager(databaseHelper), user, databaseHelper, primaryStage, q);
                    answerPage.show();
                }
            }
        });

        return layout;
    }

    /**
     * Searches questions using the entered keyword and displays results.
     */
    private void searchQuestions() {
        String keyword = questionSearchField.getText().trim();
        ObservableList<Question> items = FXCollections.observableArrayList();
        try (ResultSet rs = databaseHelper.searchQuestions(keyword)){
            //ResultSet rs = databaseHelper.searchQuestions(keyword);
            while (rs.next()) {
                int id = rs.getInt("id");
                String text = rs.getString("text");
                String author = rs.getString("author");
                items.add(new Question(id, text, author));
            }
            questionListView.setItems(items);
        } catch (SQLException ex) {
            showAlert("Error searching questions: " + ex.getMessage());
        }
    }

    /**
     * Loads answers for the selected question and displays them.
     */
    private void loadAnswersForQuestion(int questionId) {
        ObservableList<Answer> items = FXCollections.observableArrayList();
        try {
            ResultSet rs = databaseHelper.getAnswersForQuestion(questionId);
            while (rs.next()) {
                int id = rs.getInt("id");
                String text = rs.getString("text");
                String author = rs.getString("author");
                items.add(new Answer(id, text, author, questionId));
            }
            questionAnswerListView.setItems(items);
        } catch (SQLException ex) {
            showAlert("Error loading answers: " + ex.getMessage());
        }
    }

    /**
     * Searches answers using the entered keyword and displays results.
     */
    private void searchAnswers() {
        String keyword = answerSearchField.getText().trim();
        ObservableList<Answer> items = FXCollections.observableArrayList();
        try{
            ResultSet rs = databaseHelper.searchAnswers(keyword);
            while (rs.next()) {
                int id = rs.getInt("id");
                String text = rs.getString("text");
                String author = rs.getString("author");
                int questionId = rs.getInt("questionId");
                items.add(new Answer(id, text, author, questionId));
            }
            answerListView.setItems(items);
        } catch (SQLException ex) {
            showAlert("Error searching answers: " + ex.getMessage());
        }
    }

    /**
     * Loads and displays the question for the selected answer.
     */
    private void loadQuestionForAnswer(int answerId) {
        try{
            ResultSet rs = databaseHelper.getQuestionForAnswer(answerId);
            if (rs.next()) {
                int id = rs.getInt("id");
                String text = rs.getString("text");
                String author = rs.getString("author");
                relatedQuestionLabel.setText("Question ID: " + id + "\nText: " + text + "\nAuthor: " + author);
            } else {
                relatedQuestionLabel.setText("No question found for this answer.");
            }
        } catch (SQLException ex) {
            showAlert("Error loading related question: " + ex.getMessage());
        }
    }

    /**
     * Displays an alert with the specified message.
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Search Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
}//class body
package application;

import java.util.List;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ClarificationQuestionPage {
    private final QuestionManager questionManager;
    private final User user;
    private final DatabaseHelper databaseHelper;
    private final Stage primaryStage;
    private final int mainQuestionID; // Store the selected main question's ID
    private ListView<HBox> questionList;

    public ClarificationQuestionPage(QuestionManager questionManager, User user, DatabaseHelper databaseHelper, Stage primaryStage, int mainQuestionID) {
        this.questionManager = questionManager;
        this.user = user;
        this.databaseHelper = databaseHelper;
        this.primaryStage = primaryStage;
        this.mainQuestionID = mainQuestionID; // Store the main question ID
    }

    // Displays the question management UI.
    // Allows users to submit, edit, delete, and manage answers for questions.
    public void show() {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        Label titleLabel = new Label("Clarification Questions");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        TextField questionField = new TextField();
        questionField.setPromptText("Enter your clarification question...");
        
        Button submitButton = new Button("Submit Clarification");
        Label messageLabel = new Label();
        
        questionList = new ListView<>();
        refreshQuestionList();

        submitButton.setOnAction(e -> {
            String text = questionField.getText().trim();
            if (!text.isEmpty() && text.length() <= 500) {
                // Dynamically get selected main question ID
                //Question selectedMainQuestion = getSelectedMainQuestion();
                if (mainQuestionID > 0) {
                    //int mainQuestionID = selectedMainQuestion.getId();

                    // Insert subset question linked to main question
                    questionManager.createSubSetQuestion(mainQuestionID, text, user.getUserName());
                    refreshQuestionList();
                    messageLabel.setText("Clarification question added successfully!");
                } else {
                    messageLabel.setText("Please select a main question first.");
                }
                questionField.clear();
            } else {
                messageLabel.setText("Invalid question. Ensure it's not empty and under 500 characters.");
            }
        });

        
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {           
            QuestionPage questionPage = new QuestionPage(questionManager, user, databaseHelper, primaryStage);
            questionPage.show();
        });
        
        layout.getChildren().addAll(titleLabel, questionField, submitButton, messageLabel, questionList, backButton);
        
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Question Management");
    }
    
    private Question selectedMainQuestion = null; // Store the last clicked main question

    private void handleMainQuestionClick(Question question) {
        selectedMainQuestion = question; // Update the selected question
        System.out.println("Main Question Selected: " + question.getText());
    }

    private Question getSelectedMainQuestion() {
        return selectedMainQuestion;
    }

    // Refreshes the list of questions displayed in the UI.
    private void refreshQuestionList() {
        questionList.getItems().clear();

        // Fetch only subset questions linked to the selected main question
        List<Question> questions = questionManager.getSubSetQuestionsForMainQuestion(mainQuestionID);

        for (Question q : questions) {
            HBox row = new HBox(10);
            Label questionLabel = new Label(q.getText());

            // Make question label clickable
            questionLabel.setOnMouseClicked(event -> {
                ClarificationAnswerPage ClarificationAnswerPage = new ClarificationAnswerPage(new AnswerManager(databaseHelper), user, databaseHelper, primaryStage, q);
                ClarificationAnswerPage.show();
            });

            Button editButton = new Button("Edit");
            editButton.setDisable(!q.getAuthor().equals(user.getUserName()));
            editButton.setOnAction(e -> showEditPopup(q));

            Button deleteButton = new Button("Delete");
            boolean isAdmin = "admin".equals(user.getRole());
            deleteButton.setDisable(!q.getAuthor().equals(user.getUserName()) && !isAdmin);
            deleteButton.setOnAction(e -> {
                if (questionManager.deleteQuestion(q.getId(), user.getUserName(), isAdmin)) {
                    refreshQuestionList();
                }
            });

            row.getChildren().addAll(questionLabel, editButton, deleteButton);
            questionList.getItems().add(row);
        }
    }


    // Displays a popup window to edit an existing question.
	// Only allows the original author to make changes.
    private void showEditPopup(Question question) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Edit Question");

        VBox layout = new VBox(10);
        TextField editField = new TextField(question.getText());
        Button saveButton = new Button("Save");

        saveButton.setOnAction(e -> {
            String newText = editField.getText().trim();
            if (!newText.isEmpty() && newText.length() <= 500) {
                if (questionManager.editsubSetQuestion(question.getId(), newText, user.getUserName())) {
                    refreshQuestionList();
                    popupStage.close();
                }
            }
        });

        layout.getChildren().addAll(new Label("Edit your question:"), editField, saveButton);
        Scene scene = new Scene(layout, 300, 200);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }
}
package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.sql.SQLException;
import java.util.List;
import databasePart1.DatabaseHelper;

/**
 * The AnswerPage class allows users to create, view, edit, and delete answers.
 */
public class ClarificationAnswerPage {
    private final AnswerManager answerManager;
    private final User user;
    private final DatabaseHelper databaseHelper;
    private final Stage primaryStage;
    private final Question question;
    private ListView<HBox> answerList;

    // Constructor
    public ClarificationAnswerPage(AnswerManager answerManager, User user, DatabaseHelper databaseHelper, Stage primaryStage, Question question) {
        this.answerManager = answerManager;
        this.user = user;
        this.databaseHelper = databaseHelper;
        this.primaryStage = primaryStage;
        this.question = question; // Question object that does not require full List<Answer>. It will get list of answers from database in refreshAnswerList()
    }

    // Displays the answer management UI.
    // Allows users to submit, edit, and delete answers for a specific question.
    public void show() {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        Label titleLabel = new Label("Manage Answers for: " + question.getText());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        TextField answerField = new TextField();
        answerField.setPromptText("Enter your answer...");
        
        Button submitButton = new Button("Submit Answer");
        Label messageLabel = new Label();
        
        answerList = new ListView<>();
        refreshAnswerList();

        submitButton.setOnAction(e -> {
            String text = answerField.getText().trim();
            if (!text.isEmpty() && text.length() <= 500) {
                Answer newAnswer = answerManager.createSubSetAnswer(answerManager.getSubSetAnswersForQuestion(question.getId()).size() + 1, text, user.getUserName(), question.getId());
                if (newAnswer != null) {
                    answerManager.saveSubSetAnswer(newAnswer);
                    refreshAnswerList();
                    messageLabel.setText("Answer added successfully!");
                }
                answerField.clear();
            } else {
                messageLabel.setText("Invalid answer. Ensure it's not empty and under 500 characters.");
            }
        });
        
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            QuestionPage questionPage = new QuestionPage(new QuestionManager(databaseHelper), user, databaseHelper, primaryStage);
            questionPage.show();
        });
        
        layout.getChildren().addAll(titleLabel, answerField, submitButton, messageLabel, answerList, backButton);
        
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Answer Management");
        primaryStage.toFront();
    }

    // Refreshes the list of answers displayed in the UI.
    private void refreshAnswerList() {
        answerList.getItems().clear();
        System.out.println(question.getId());
        System.out.println(question.getText());
        System.out.println(question.getAnswers());
        List<Answer> answers = answerManager.getSubSetAnswersForQuestion(question.getId());
        for (Answer a : answers) {
            HBox row = new HBox(10);
            Label answerLabel = new Label(a.getText());
            
            Button editButton = new Button("Edit");
            editButton.setDisable(!a.getAuthor().equals(user.getUserName()));
            editButton.setOnAction(e -> showEditPopup(a));
            
            Button deleteButton = new Button("Delete");
            boolean isAdmin = "admin".equals(user.getRole());
            deleteButton.setDisable(!a.getAuthor().equals(user.getUserName()) && !isAdmin);
            deleteButton.setOnAction(e -> {
                if (answerManager.deleteAnswer(a.getId(), user.getUserName(), isAdmin)) {
                    refreshAnswerList();
                }
            });
            
            Label checkmarkLabel = new Label("✔"); //resolved indication
            
            checkmarkLabel.setVisible(databaseHelper.isSubSetResolved(question.getId()));
            
            Button resolveQuestionButton = new Button("Resolved");
            resolveQuestionButton.setOnAction(e -> {
                // Set question resolved functionality	
                    try {
                        //int questionID = question.getId();
                        databaseHelper.setSubSetResolved(a.getId());
                        refreshAnswerList();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
            });
            
            row.getChildren().addAll(answerLabel, editButton, deleteButton, resolveQuestionButton, checkmarkLabel);
            answerList.getItems().add(row);
        }
    }

    // Displays a popup window to edit an existing answer.
    // Only allows the original author to make changes.
    private void showEditPopup(Answer answer) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Edit Answer");

        VBox layout = new VBox(10);
        TextField editField = new TextField(answer.getText());
        Button saveButton = new Button("Save");

        saveButton.setOnAction(e -> {
            String newText = editField.getText().trim();
            if (!newText.isEmpty() && newText.length() <= 500) {
                if (answerManager.updatesubSetAnswer(answer.getId(), newText, user.getUserName())) {
                    refreshAnswerList();
                    popupStage.close();
                }
            }
        });

        layout.getChildren().addAll(new Label("Edit your answer:"), editField, saveButton);
        Scene scene = new Scene(layout, 300, 200);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }
}
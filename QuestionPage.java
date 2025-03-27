package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.util.List;
import databasePart1.DatabaseHelper;

/**
 * The QuestionPage class allows users to create, view, edit, and delete questions.
 */
public class QuestionPage {
    private final QuestionManager questionManager;
    private final User user;
    private final DatabaseHelper databaseHelper;
    private final Stage primaryStage;
    private Question selectedMainQuestion;
    private ListView<HBox> questionList;

    public QuestionPage(QuestionManager questionManager, User user, DatabaseHelper databaseHelper, Stage primaryStage) {
        this.questionManager = questionManager;
        this.user = user;
        this.databaseHelper = databaseHelper;
        this.primaryStage = primaryStage;
    }

    // Displays the question management UI.
    // Allows users to submit, edit, delete, and manage answers for questions.
    public void show() {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        Label titleLabel = new Label("Manage Your Questions");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        TextField questionField = new TextField();
        questionField.setPromptText("Enter your question...");
        
        Button submitButton = new Button("Submit Question");
        Label messageLabel = new Label();
        
        questionList = new ListView<>();
        refreshQuestionList();

        submitButton.setOnAction(e -> {
            String text = questionField.getText().trim();
            if (!text.isEmpty() && text.length() <= 500) {
                Question newQuestion = questionManager.createQuestion(questionManager.getAllQuestions().size() + 1, text, user.getUserName());
                if (newQuestion != null) {
                    questionManager.saveQuestion(newQuestion);
                    refreshQuestionList();
                    messageLabel.setText("Question added successfully!");
                }
                questionField.clear();
            } else {
                messageLabel.setText("Invalid question. Ensure it's not empty and under 500 characters.");
            }
        });
        
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            UserHomePage userHomePage = new UserHomePage(databaseHelper, user);
            userHomePage.show(primaryStage);
        });
        
        layout.getChildren().addAll(titleLabel, questionField, submitButton, messageLabel, questionList, backButton);
        
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Question Management");
    }

    
    // Refreshes the list of questions displayed in the UI.
    private void refreshQuestionList() {
        questionList.getItems().clear();
        List<Question> questions = questionManager.getAllQuestions();
        for (Question q : questions) {
            HBox row = new HBox(10);
            Label questionLabel = new Label(q.getText());
            // Make question label click-able
            questionLabel.setOnMouseClicked(event -> {
            	AnswerPage answerPage = new AnswerPage(new AnswerManager(databaseHelper), user, databaseHelper, primaryStage, q);
                answerPage.show();
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
            
            Button clarificationButton = new Button("Clarification");
            selectedMainQuestion = q;
        	System.out.println("Selected Main Question: " + selectedMainQuestion.getText());
            clarificationButton.setOnAction(e -> {
                if (selectedMainQuestion != null) {
                    int mainQuestionID = selectedMainQuestion.getId(); // Get selected question's ID

                    ClarificationQuestionPage clarificationPage = new ClarificationQuestionPage(
                            new QuestionManager(databaseHelper),
                            user,
                            databaseHelper,
                            primaryStage,
                            mainQuestionID 
                    );
                    clarificationPage.show();
                } else {
                    System.out.println("Please select a main question first.");
                }
            });
            
            /*
            Label checkmarkLabel = new Label("✔"); //resolved indication
            checkmarkLabel.setVisible(false);
            
            Button resolveQuestionButton = new Button("Resolved");
            resolveQuestionButton.setOnAction(e -> {
                // Set question resolved functionality
            	boolean res = true;
            	// Resolved icon
            	if(res) {
            		checkmarkLabel.setVisible(true);
            	}else {
            		checkmarkLabel.setVisible(false);
            	}
            	
            });
            */
            
            row.getChildren().addAll(questionLabel, editButton, deleteButton, clarificationButton);//, resolveQuestionButton, checkmarkLabel);
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
                if (questionManager.editQuestion(question.getId(), newText, user.getUserName())) {
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
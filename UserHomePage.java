package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

/**
 * This page displays a simple welcome message for the user.
 */
public class UserHomePage {
    private final DatabaseHelper databaseHelper;
    private final User user;
    private final QuestionManager questionManager;

    public UserHomePage(DatabaseHelper databaseHelper, User user) {
        this.databaseHelper = databaseHelper;
        this.user = user;
        this.questionManager = new QuestionManager(databaseHelper);
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        // Label to display the welcome message for the user
        Label userLabel = new Label("Hello, " + user.getUserName() + "!");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Button to navigate to Change Password page
        Button changePasswordButton = new Button("Change Password");
        changePasswordButton.setOnAction(e -> {
            ChangePasswordPage changePasswordPage = new ChangePasswordPage(databaseHelper, user);
            changePasswordPage.show(primaryStage);
        });
        
        
        
        // Button to navigate to Question Management page
        Button manageQuestionsButton = new Button("Manage Questions");
        manageQuestionsButton.setOnAction(e -> {
            QuestionPage questionPage = new QuestionPage(questionManager, user, databaseHelper, primaryStage);
            questionPage.show();
        });
        
        // Button to navigate to Search Q&A page
        Button searchButton = new Button("Search Q&A");
        searchButton.setOnAction(e -> {
            SearchPage searchPage = new SearchPage(user, databaseHelper, primaryStage);
            //searchPage.show();
            //Stage searchStage = new Stage();
            searchPage.show();
        });
        
        layout.getChildren().addAll(userLabel, changePasswordButton, manageQuestionsButton, searchButton);
        Scene userScene = new Scene(layout, 800, 400);
        
        // Set the scene to primary stage
        primaryStage.setScene(userScene);
        primaryStage.setTitle("User Page");
    }
}
package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import databasePart1.*;

/**
 * The UserLoginPage class provides a login interface for users to access their accounts.
 * It validates the user's credentials and navigates to the appropriate page upon successful login.
 */
public class UserLoginPage {
    
    private final DatabaseHelper databaseHelper;

    public UserLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        // Input field for the user's username and password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Username");
        userNameField.setMaxWidth(250);
        
        userNameField.setText("student");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        passwordField.setText("Student123!");
        
        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button loginButton = new Button("Login");
        
        loginButton.setOnAction(a -> {
            // Retrieve user inputs
            String userName = userNameField.getText().trim();
            String password = passwordField.getText().trim();

            if (userName.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Username and Password cannot be empty.");
                return;
            }

            // Validate password format using PasswordEvaluator
            String passwordValidation = PasswordEvaluator.evaluatePassword(password);
            if (!passwordValidation.isEmpty()) {
                errorLabel.setText("Invalid password: " + passwordValidation);
                return;
            }

            try {
                User user = new User(userName, password, "");
                WelcomeLoginPage welcomeLoginPage = new WelcomeLoginPage(databaseHelper);
                
                // Retrieve the user's role from the database using username
                String role = databaseHelper.getUserRole(userName);
                
                if (role != null) {
                    user.setRole(role);
                    if (databaseHelper.login(user)) {
                        welcomeLoginPage.show(primaryStage, user);
                    } else {
                        // Display an error if the login fails
                        errorLabel.setText("Error logging in. Please check your credentials.");
                    }
                } else {
                    // Display an error if the account does not exist
                    errorLabel.setText("User account does not exist.");
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
                errorLabel.setText("An error occurred. Please try again later.");
            }
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(userNameField, passwordField, loginButton, errorLabel);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("User Login");
        primaryStage.show();
    }
}
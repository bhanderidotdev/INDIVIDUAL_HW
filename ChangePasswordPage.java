package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import databasePart1.*;

/**
 * The ChangePasswordPage class allows users to update their password.
 * It validates the input, checks the current password, and updates the database if successful.
 */
public class ChangePasswordPage {
    
    private final DatabaseHelper databaseHelper;
    private final User user;

    public ChangePasswordPage(DatabaseHelper databaseHelper, User user) {
        this.databaseHelper = databaseHelper;
        this.user = user;
    }

    public void show(Stage primaryStage) {
        Label instructionLabel = new Label("Change Your Password");
        
        TextField currentPasswordField = new TextField();
        currentPasswordField.setPromptText("Enter Current Password");
        
        TextField newPasswordField = new TextField();
        newPasswordField.setPromptText("Enter New Password");
        
        TextField confirmPasswordField = new TextField();
        confirmPasswordField.setPromptText("Confirm New Password");
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        Button submitButton = new Button("Change Password");
        
        submitButton.setOnAction(e -> {
            String currentPassword = currentPasswordField.getText().trim();
            String newPassword = newPasswordField.getText().trim();
            String confirmPassword = confirmPasswordField.getText().trim();
            
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                errorLabel.setText("All fields must be filled.");
                return;
            }

            // Validate current password
            if (!user.getPassword().equals(currentPassword)) {
                errorLabel.setText("Current password is incorrect.");
                return;
            }

            // Validate new password format
            String passwordValidation = PasswordEvaluator.evaluatePassword(newPassword);
            if (!passwordValidation.isEmpty()) {
                errorLabel.setText("Invalid password: " + passwordValidation);
                return;
            }

            // Check if new password matches confirmation
            if (!newPassword.equals(confirmPassword)) {
                errorLabel.setText("New passwords do not match.");
                return;
            }
            
            // Update password in the database
            try {
                databaseHelper.updateUserPassword(user.getUserName(), newPassword);
                user.setPassword(newPassword);
                errorLabel.setStyle("-fx-text-fill: green;");
                errorLabel.setText("Password successfully changed.");
            } catch (SQLException ex) {
                errorLabel.setText("Error updating password. Please try again.");
                ex.printStackTrace();
            }
        });
        
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(instructionLabel, currentPasswordField, newPasswordField, confirmPasswordField, submitButton, errorLabel);
        
        primaryStage.setScene(new Scene(layout, 400, 300));
        primaryStage.setTitle("Change Password");
        primaryStage.show();
    }
}
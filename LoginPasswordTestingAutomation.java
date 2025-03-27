package application;

import databasePart1.*;

/**
 * <p> Title: LoginPasswordTestingAutomation Class. </p>
 * 
 * <p> Description: A Java demonstration for semi-automated tests for login and password change. </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2022 </p>
 * 
 * @author Lynn Robert Carter
 * @author Evan Hochhalter
 * 
 * @version 1.01  2025-02-07  Expanded test cases for login and password validation.
 */
public class LoginPasswordTestingAutomation {
    
    static int numPassed = 0; // Counter of passed tests
    static int numFailed = 0; // Counter of failed tests
    private static final DatabaseHelper databaseHelper = new DatabaseHelper();

    public static void main(String[] args) {
        try {
            databaseHelper.connectToDatabase();
            
            System.out.println("______________________________________");
            System.out.println("\nTesting Automation for Login and Password Change");

            // Run login tests
            runLoginTests();
            
            // Run password change tests
            runChangePasswordTests();
            
            System.out.println("____________________________________________________________________________");
            System.out.println("\nNumber of tests passed: " + numPassed);
            System.out.println("Number of tests failed: " + numFailed);
        } catch (Exception e) {
            System.err.println("Error during tests: " + e.getMessage());
        } finally {
            databaseHelper.closeConnection();
        }
    }
    
    private static void runLoginTests() {
        System.out.println("\nStarting Login Tests...");
        
        performLoginTest(1, "validUser", "Valid!123", false);
        performLoginTest(2, "invalidUser", "WrongPass", false);
        performLoginTest(3, "", "Valid!123", false); // Empty username
        performLoginTest(4, "tst", "Valid!123", false); // Username too short
         // Valid username
        performLoginTest(5, "9tst", "Valid!123", false); // Starts with number
        performLoginTest(6, "test.", "Valid!123", false); // No follow-up after special char
         // Valid username
        
        performLoginTest(7, "test.!", "Valid!123", false);
        performLoginTest(8, "test!", "Valid!123", false);
        performLoginTest(9, "!test", "Valid!123", false);
        performLoginTest(10, "testtesttesttest.t", "Valid!123", false); // Exceeds character limit
    }
    
    private static void performLoginTest(int testCase, String username, String password, boolean expected) {
        System.out.println("\nTest Case: " + testCase);
        System.out.println("Input - Username: " + username + ", Password: " + password);
        
        boolean loginSuccess = false;
        try {
            User user = new User(username, password, "");
            if (databaseHelper.doesUserExist(username) && databaseHelper.login(user)) {
                loginSuccess = true;
            }
        } catch (Exception e) {
            System.err.println("Login test error: " + e.getMessage());
        }
        
        validateTestResult(loginSuccess, expected);
    }
    
    private static void runChangePasswordTests() {
        System.out.println("\nStarting Change Password Tests...");
        
        
        performPasswordChangeTest(11, "validUser", "WrongPass", "NewValid!456", false);
        performPasswordChangeTest(12, "validUser", "Valid!123", "short", false);
        performPasswordChangeTest(13, "validUser", "Valid!123", "test123!", false); // No uppercase
        performPasswordChangeTest(14, "validUser", "Valid!123", "Test1234", false); // No special char
        performPasswordChangeTest(15, "validUser", "Valid!123", "TEST123!", false); // No lowercase
        performPasswordChangeTest(16, "validUser", "Valid!123", "Test!123456789test", false); // Too many characters
    }
    
    private static void performPasswordChangeTest(int testCase, String username, String currentPass, String newPass, boolean expected) {
        System.out.println("\nTest Case: " + testCase);
        System.out.println("Input - Username: " + username + ", Current Password: " + currentPass + ", New Password: " + newPass);
        
        boolean changeSuccess = false;
        try {
            User user = new User(username, currentPass, "user");
            if (databaseHelper.doesUserExist(username) && databaseHelper.login(user)) {
                String validation = PasswordEvaluator.evaluatePassword(newPass);
                if (validation.isEmpty()) {
                    databaseHelper.updateUserPassword(username, newPass);
                    changeSuccess = true;
                }
            }
        } catch (Exception e) {
            System.err.println("Password change test error: " + e.getMessage());
        }
        
        validateTestResult(changeSuccess, expected);
    }
    
    private static void validateTestResult(boolean result, boolean expected) {
        if (result == expected) {
            System.out.println("*** Success *** Test passed!");
            numPassed++;
        } else {
            System.out.println("*** Failure *** Test failed!");
            numFailed++;
        }
    }
}
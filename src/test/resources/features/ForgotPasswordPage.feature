
@FEATURE_EXECUTION
Feature: Forgot Password Page

  Background: Condition to check before each scenario
    Given the url is launched successfully

  @regression
  Scenario: Validate the cancel functionality - Feature 2, Scenario - 1

    When User clicks on Forgot your password? link
    Then User should be able to navigate to Reset Password page
    And User clicks on Cancel button to go back to Login Page

  @smoke
  Scenario: Validate the Reset Password functionality - Feature 2, Scenario - 2

    When User clicks on Forgot your password? link
    Then User should be able to navigate to Reset Password page
    And User clicks on Reset Password button and provide username as "abc1234"
    And Verify the message "Reset Password link sent successfully"
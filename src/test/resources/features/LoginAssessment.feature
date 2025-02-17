Feature: Login to Sauce Lab


  @LOGIN_SAUCE
  Scenario Outline: Login to Sauce Lab Application
  #Given we are landed on the login screen
    And enter username as "<username>" and password as "<password>" and click on login button
    Then login should be successful

    Examples:
      | username      | password     |
      | standard_user | secret_sauce |
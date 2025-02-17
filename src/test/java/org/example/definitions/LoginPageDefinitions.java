package org.example.definitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.pageObjects.HomePage;
import org.example.pageObjects.LoginPage;
import org.example.utils.TestSetUp;
import org.testng.Assert;

import java.io.IOException;

public class LoginPageDefinitions {

    TestSetUp setUp;
    public LoginPage loginPage;
    public HomePage homePage;


    public LoginPageDefinitions(TestSetUp setUp) throws IOException {
        this.setUp = setUp;
        this.loginPage = setUp.pageObjectManager.getLoginPage();
        this.homePage= setUp.pageObjectManager.getHomePage();
    }

   /* @Given("User is on Home page")
    public void loginTest() throws IOException {
        //setUp.baseTest.WebDriverManager().get("https://opensource-demo.orangehrmlive.com/");

    }*/

    @When("User enters username as {string} and password as {string}")
    public void goToHomePage(String userName, String passWord) throws InterruptedException {

        // login to application
        Assert.assertTrue(loginPage.login(userName, passWord),"Login not successful");
        //Assert.assertTrue(false,"Login not successful");

    }

    @Then("User should be able to login successfully")
    public void verifyLogin() {
        // Verify home page
        Assert.assertTrue(homePage.getHomePageText().contains("Dashboard"));
    }

    @Then("User should be able to see error message {string}")
    public void verifyErrorMessage(String expectedErrorMessage) {

        // Verify home page
        //Assert.assertEquals(loginPage.getErrorMessage(),expectedErrorMessage);
        Assert.fail("Intentionally failed test case");

    }

}
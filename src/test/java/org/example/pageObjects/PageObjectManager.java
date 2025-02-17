package org.example.pageObjects;

import org.openqa.selenium.WebDriver;

public class PageObjectManager {

    public LoginPage loginPage;
    public HomePage homePage;
    public ForgotPasswordPage forgotPasswordPage;

    public LoginPageSauceLab loginSauce;
    public ProductPage productPage;

    public WebDriver driver;

    public PageObjectManager(WebDriver driver) {
        this.driver = driver;
    }

    public LoginPage getLoginPage() {
        loginPage = new LoginPage(driver);
        return loginPage;
    }
    public HomePage getHomePage() {
        homePage = new HomePage(driver);
        return homePage;
    }
    public ForgotPasswordPage getForgotPasswordPage() {
        forgotPasswordPage = new ForgotPasswordPage(driver);
        return forgotPasswordPage;
    }

    public LoginPageSauceLab getLoginPageSauceLab() {
        loginSauce = new LoginPageSauceLab(driver);
        return loginSauce;
    }
    public ProductPage getProductPage() {
        productPage = new ProductPage(driver);
        return productPage;
    }


}



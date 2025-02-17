package org.example.pageObjects;

import org.example.reusablemethods.ReusableCommonMethods;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LoginPageSauceLab {

    public WebDriver driver;

    @FindBy(id = "user-name")
    WebElement userName;
    @FindBy(id = "password")
    WebElement passWord;
    @FindBy(xpath = "//input[@id='login-button']")
    WebElement loginButton;

    public LoginPageSauceLab(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public boolean login(String strUserName, String strPassword) {
        try {
            // Fill user name
            userName.clear();
            userName.sendKeys(strUserName);
            passWord.clear();
            passWord.sendKeys(strPassword);
            loginButton.click();
            return true;
        }catch (Exception e)
        {
            return false;
        }
    }

}
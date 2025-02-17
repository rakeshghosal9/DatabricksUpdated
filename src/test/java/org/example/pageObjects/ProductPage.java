package org.example.pageObjects;

import org.example.reusablemethods.ReusableCommonMethods;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ProductPage {

    public WebDriver driver;

    @FindBy(xpath = "//span[@class='title'][text()='Products']")
    WebElement productTitle;

    public ProductPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }


    //Get Login Page Title
    public boolean validateProductPageAppears() {
        return ReusableCommonMethods.waitForElementToBeVisible(productTitle, driver, 20);
    }
}
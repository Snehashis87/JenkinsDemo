package code;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;

public class GoogleTitleTest {

	@Test
    public void LoginTest() {

        // Launch Chrome Browser
        WebDriver driver = new ChromeDriver();

        // Open Google
        driver.get("https://www.google.com");

        // Get page title
        String title = driver.getTitle();

        // Print title
        System.out.println("Page Title is: " + title);

        // Close browser
        driver.quit();
    }
}
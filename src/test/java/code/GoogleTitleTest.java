package code;

import org.testng.annotations.Test;

public class GoogleTitleTest extends BaseClass{

	@Test
    public void LoginTest() {

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
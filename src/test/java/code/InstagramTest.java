package code;

import org.testng.annotations.Test;

public class InstagramTest extends BaseClass{
	
	@Test
	public void LoginTests() {

        // Open Google
        driver.get("https://www.instagram.com/");

        // Get page title
        String title = driver.getTitle();

        // Print title
        System.out.println("Page Title is: " + title);

        // Close browser
        driver.quit();
	}

}

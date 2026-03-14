package code;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;

public class InstagramTest {
	
	@Test
	public void LoginTests() {
		WebDriver driver = new ChromeDriver();

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

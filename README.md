# JenkinsDemo – Selenium + TestNG + Maven Automation Framework

A cross-browser UI test automation project that demonstrates running Selenium WebDriver tests with TestNG and Maven, designed to be triggered and monitored through a Jenkins CI/CD pipeline.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Tech Stack](#tech-stack)
3. [Project Structure](#project-structure)
4. [Concepts Implemented](#concepts-implemented)
   - [BaseClass – Reusable Setup & Teardown](#baseclass--reusable-setup--teardown)
   - [Test Classes](#test-classes)
   - [Maven Surefire Plugin & TestNG Suite File](#maven-surefire-plugin--testng-suite-file)
5. [Code Walkthrough](#code-walkthrough)
   - [BaseClass.java](#baseclassjava)
   - [GoogleTitleTest.java](#googletitletestjava)
   - [InstagramTest.java](#instagramtestjava)
   - [pom.xml](#pomxml)
6. [Running Tests Sequentially (Current batch.xml)](#running-tests-sequentially-current-batchxml)
7. [Running Tests in Parallel (Updated batch.xml)](#running-tests-in-parallel-updated-batchxml)
8. [pom.xml Changes Required for Parallel Execution](#pomxml-changes-required-for-parallel-execution)
9. [How to Run](#how-to-run)

---

## Project Overview

This project contains automated UI tests written in Java using the **Selenium WebDriver** library and the **TestNG** testing framework. Tests are built and executed via **Apache Maven**. The project is configured so that:

- The same test logic runs against **multiple browsers** (Chrome, Firefox, Edge) by passing a `browser` parameter through the TestNG XML suite file.
- A **BaseClass** provides the shared browser set-up and teardown logic, which every test class inherits.
- The Maven **Surefire Plugin** picks up the TestNG suite XML file (`batch.xml`) and executes it during the `mvn test` phase.
- Jenkins can trigger `mvn test` on every code push to run the full suite in a CI/CD pipeline.

---

## Tech Stack

| Tool / Library | Version | Purpose |
|---|---|---|
| Java | 11+ | Programming language |
| Selenium WebDriver | 4.38.0 | Browser automation |
| TestNG | 7.10.2 | Test framework & parallel execution |
| Apache Maven | 3.x | Build tool & dependency management |
| Maven Surefire Plugin | 3.2.2 | Runs TestNG suite during `mvn test` |
| Maven Compiler Plugin | 3.11.0 | Compiles Java source files |
| Jenkins | Any LTS | CI/CD pipeline (triggers `mvn test`) |
| ChromeDriver / GeckoDriver / EdgeDriver | Matching browser version | WebDriver binaries (Selenium 4 auto-manages) |

---

## Project Structure

```
JenkinsDemo/
├── pom.xml                          # Maven build & dependency configuration
├── batch.xml                        # TestNG suite file (referenced by pom.xml)
└── src/
    └── test/
        └── java/
            └── code/
                ├── BaseClass.java       # Shared WebDriver setup & teardown
                ├── GoogleTitleTest.java # Test: verifies Google page title
                └── InstagramTest.java  # Test: verifies Instagram page title
```

---

## Concepts Implemented

### BaseClass – Reusable Setup & Teardown

**Inheritance / Base Class Pattern**: `BaseClass` holds the `WebDriver` instance and all common lifecycle logic. Every test class **extends** `BaseClass` instead of duplicating browser setup code.

**TestNG Annotations used in BaseClass**:

| Annotation | Scope | What it does |
|---|---|---|
| `@BeforeClass` | Once per class | Launches the browser and navigates to Google before any test in the class runs |
| `@AfterMethod` | After each `@Test` | Calls `driver.quit()` to close the browser and release resources |
| `@Parameters("browser")` | Method parameter | Injects the `browser` value from the TestNG XML suite file at runtime |

**Multi-browser support**: The `setUp` method reads the injected `browser` parameter and instantiates the correct `WebDriver` implementation:

```java
if (browser.equalsIgnoreCase("chrome"))   driver = new ChromeDriver();
if (browser.equalsIgnoreCase("firefox"))  driver = new FirefoxDriver();
if (browser.equalsIgnoreCase("edge"))     driver = new EdgeDriver();
```

This means **no code change** is needed to switch browsers; only the XML suite file needs to be updated.

---

### Test Classes

Both `GoogleTitleTest` and `InstagramTest` follow the same pattern:

- Extend `BaseClass` to inherit `driver`, `setUp()`, and `tearDown()`.
- Contain a single `@Test` method that:
  1. Navigates to a URL.
  2. Retrieves the page title with `driver.getTitle()`.
  3. Prints the title to stdout.
  4. Calls `driver.quit()` to close the browser.

---

### Maven Surefire Plugin & TestNG Suite File

The Surefire Plugin is configured inside `pom.xml` to point at `batch.xml`:

```xml
<configuration>
    <suiteXmlFiles>
        <suiteXmlFile>batch.xml</suiteXmlFile>
    </suiteXmlFiles>
</configuration>
```

When you run `mvn test`, Maven compiles the sources and then the Surefire Plugin delegates test discovery and execution entirely to TestNG using the suite defined in `batch.xml`.

---

## Code Walkthrough

### BaseClass.java

```java
package code;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.*;

public class BaseClass {

    public WebDriver driver;  // Shared driver instance accessible by subclasses

    @Parameters("browser")    // Value injected from TestNG suite XML at runtime
    @BeforeClass              // Runs once before any test method in the class
    public void setUp(String browser) {

        if (browser.equalsIgnoreCase("chrome")) {
            driver = new ChromeDriver();       // Selenium 4 auto-manages ChromeDriver binary
        } else if (browser.equalsIgnoreCase("firefox")) {
            driver = new FirefoxDriver();      // Selenium 4 auto-manages GeckoDriver binary
        } else if (browser.equalsIgnoreCase("edge")) {
            driver = new EdgeDriver();         // Selenium 4 auto-manages EdgeDriver binary
        }

        driver.manage().window().maximize();   // Maximize browser window
        driver.get("https://www.google.com");  // Default starting URL
    }

    @AfterMethod   // Runs after EVERY @Test method to release the browser process
    public void tearDown() {
        driver.quit();   // Closes all windows and ends the WebDriver session
    }
}
```

**Key points**:
- `WebDriver driver` is declared `public` so that subclasses can use it directly.
- Selenium 4 includes the **Selenium Manager** tool which automatically downloads and configures the correct browser driver binary, so you no longer need to manually set a system property like `System.setProperty("webdriver.chrome.driver", "...")`.
- `driver.quit()` in `@AfterMethod` ensures no zombie browser processes remain after each test.

---

### GoogleTitleTest.java

```java
package code;

import org.testng.annotations.Test;

public class GoogleTitleTest extends BaseClass {

    @Test
    public void LoginTest() {
        driver.get("https://www.google.com");   // Navigate to Google
        String title = driver.getTitle();        // Retrieve page title
        System.out.println("Page Title is: " + title);
        driver.quit();
    }
}
```

- Inherits the browser instance from `BaseClass.setUp()`.
- `driver.get(url)` sends the browser to a new URL.
- `driver.getTitle()` returns the current page's `<title>` tag content.

> **Note**: The explicit `driver.quit()` call inside the test method is redundant because `BaseClass.tearDown()` is already annotated with `@AfterMethod` and calls `driver.quit()` after every test. Having both means `tearDown()` will attempt to quit a browser session that the test method already closed, which will throw a `NoSuchSessionException`. The safe fix is to remove the `driver.quit()` line from the test method and let `@AfterMethod` handle cleanup exclusively.

---

### InstagramTest.java

```java
package code;

import org.testng.annotations.Test;

public class InstagramTest extends BaseClass {

    @Test
    public void LoginTests() {
        driver.get("https://www.instagram.com/");   // Navigate to Instagram
        String title = driver.getTitle();             // Retrieve page title
        System.out.println("Page Title is: " + title);
        driver.quit();
    }
}
```

Follows the same structure as `GoogleTitleTest`, but targets Instagram.

> **Note**: Same as `GoogleTitleTest`, the explicit `driver.quit()` inside the test method is redundant alongside `@AfterMethod tearDown()` in `BaseClass`. Remove the in-method `driver.quit()` call to avoid a duplicate-quit error.

---

### pom.xml

```xml
<project ...>
    <modelVersion>4.0.0</modelVersion>
    <groupId>jenkins</groupId>
    <artifactId>jenkins</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <build>
        <pluginManagement>
            <plugins>
                <!-- Compiles Java source files -->
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.11.0</version>
                </plugin>

                <!-- Runs TestNG suite during `mvn test` -->
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <version>3.2.2</version>
                    <configuration>
                        <suiteXmlFiles>
                            <!-- Points to the TestNG suite file in the project root -->
                            <suiteXmlFile>batch.xml</suiteXmlFile>
                        </suiteXmlFiles>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>

    <dependencies>
        <!-- Selenium WebDriver – browser automation -->
        <dependency>
            <groupId>org.seleniumhq.selenium</groupId>
            <artifactId>selenium-java</artifactId>
            <version>4.38.0</version>
            <scope>compile</scope>
        </dependency>

        <!-- TestNG – test framework -->
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>7.10.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

---

## Running Tests Sequentially (Current batch.xml)

Create `batch.xml` at the project root (same level as `pom.xml`) with the following content to run tests **one at a time** on a single browser:

```xml
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="BatchSuite">

    <test name="GoogleTest">
        <parameter name="browser" value="chrome"/>
        <classes>
            <class name="code.GoogleTitleTest"/>
        </classes>
    </test>

    <test name="InstagramTest">
        <parameter name="browser" value="chrome"/>
        <classes>
            <class name="code.InstagramTest"/>
        </classes>
    </test>

</suite>
```

In this configuration, TestNG runs `GoogleTest` first and then `InstagramTest` in sequence, both on Chrome.

---

## Running Tests in Parallel (Updated batch.xml)

To run tests **in parallel across multiple browsers simultaneously**, update `batch.xml` as shown below.

### Option 1 – Parallel across browsers (recommended for cross-browser testing)

```xml
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<!--
    parallel="tests"  → each <test> block runs in its own thread simultaneously
    thread-count="3"  → up to 3 threads run at the same time (one per browser)
-->
<suite name="BatchSuite" parallel="tests" thread-count="3">

    <test name="Chrome Tests">
        <parameter name="browser" value="chrome"/>
        <classes>
            <class name="code.GoogleTitleTest"/>
            <class name="code.InstagramTest"/>
        </classes>
    </test>

    <test name="Firefox Tests">
        <parameter name="browser" value="firefox"/>
        <classes>
            <class name="code.GoogleTitleTest"/>
            <class name="code.InstagramTest"/>
        </classes>
    </test>

    <test name="Edge Tests">
        <parameter name="browser" value="edge"/>
        <classes>
            <class name="code.GoogleTitleTest"/>
            <class name="code.InstagramTest"/>
        </classes>
    </test>

</suite>
```

**What changes and why**:

| Attribute / Element | Sequential value | Parallel value | Effect |
|---|---|---|---|
| `<suite parallel="...">` | _(absent)_ | `"tests"` | Tells TestNG to run each `<test>` block in a separate thread |
| `<suite thread-count="...">` | _(absent)_ | `"3"` | Sets the maximum number of concurrent threads (one per browser here) |
| Number of `<test>` blocks | 2 (both Chrome) | 3 (one per browser) | Each block carries a different `browser` parameter, so Chrome, Firefox, and Edge all launch simultaneously |

### Option 2 – Parallel at the method level (all @Test methods run concurrently)

```xml
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="BatchSuite" parallel="methods" thread-count="4">

    <test name="AllBrowserTests">
        <parameter name="browser" value="chrome"/>
        <classes>
            <class name="code.GoogleTitleTest"/>
            <class name="code.InstagramTest"/>
        </classes>
    </test>

</suite>
```

> **Note**: `parallel="methods"` runs each `@Test` method in its own thread. Because `BaseClass.driver` is an instance field (not `ThreadLocal`), this option requires refactoring `driver` into a `ThreadLocal<WebDriver>` to avoid thread-safety issues. Option 1 (`parallel="tests"`) is safer for the current code because each `<test>` block gets its own class instance.

### Summary of TestNG `parallel` attribute values

| Value | Granularity | Recommended when |
|---|---|---|
| `tests` | One thread per `<test>` XML block | Cross-browser testing (current setup) |
| `classes` | One thread per test class | Multiple independent test classes |
| `methods` | One thread per `@Test` method | Maximum speed; requires `ThreadLocal` driver |
| `instances` | One thread per class instance | Factory-created test instances |

---

## pom.xml Changes Required for Parallel Execution

The existing `pom.xml` does **not** require any changes to enable parallel execution—TestNG's `parallel` and `thread-count` attributes in `batch.xml` are sufficient.

However, to ensure Maven does **not** impose its own fork/thread limits that could interfere, you may optionally add the `forkCount` and `reuseForks` settings to the Surefire plugin configuration:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.2</version>
    <configuration>
        <suiteXmlFiles>
            <suiteXmlFile>batch.xml</suiteXmlFile>
        </suiteXmlFiles>
        <!-- Optional: run all tests in a single forked JVM so TestNG controls threading -->
        <forkCount>1</forkCount>
        <reuseForks>true</reuseForks>
    </configuration>
</plugin>
```

> By default, Surefire already uses a single fork, so the `forkCount`/`reuseForks` values above match the default. They are shown here for clarity.

---

## How to Run

### Prerequisites

- Java 11 or higher installed and `JAVA_HOME` set.
- Maven 3.x installed.
- Chrome, Firefox, and/or Edge browsers installed.
- Selenium 4 **automatically manages driver binaries** via Selenium Manager — no manual driver download is needed.

### Commands

```bash
# Run tests using the suite defined in batch.xml
mvn test

# Run tests and skip compilation (if already compiled)
mvn surefire:test

# Clean previous build artifacts and run tests
mvn clean test
```

### Jenkins Integration

1. Create a **Freestyle** or **Pipeline** job in Jenkins.
2. Under **Source Code Management**, point to this repository.
3. Add a **Build Step** → **Invoke top-level Maven targets** → Goal: `clean test`.
4. Jenkins will clone the repo, compile the code, and run the TestNG suite defined in `batch.xml`.
5. Surefire generates XML/HTML reports under `target/surefire-reports/` which can be published using the **JUnit** or **TestNG Results** plugin in Jenkins.

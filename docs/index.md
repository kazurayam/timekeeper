The Timekeeper library helps Java/Groovy test codes to record performance measurement statistics and compiling reports in Markdown format.

# API

Javadoc is [here](./api/index.html)

# Example

`Timekeeper` object lets you create **one or more** `Measurement` objects. A `Measurement` object stands for a table which contains a header and one or more `Record` set. A `Record` contains columns and a duration in `mm:ss` format (minutes:seconds). My test will put an instance of `LocalDateTime.now()` just before the test calls a long running method call (such as Selenium navite, and taking screenshot). This timestatmp is recorded as `startAt`. Also my test will put another instance of `LocalDateTime.now()` just after the long-running method call. This timestamp is recorded `endAt`. Each record object can calculate the duration = endAt minus startAt. And finally Timekeeper’s `report(Path)` method can generate a text report in Markdown syntax.

I will show you a demo. Input CSV file is here:

    https://www.google.com/search?q=timekeeper,timekeeper_google.png
    https://duckduckgo.com/?q=timekeeper&t=h_&ia=web,timekeeper_duckduckgo.png
    https://search.yahoo.co.jp/search?p=timekeeper,timekeeper_yahoo.png

The code is here:

    package com.kazurayam.timekeeper.demo

    import com.kazurayam.ashotwrapper.AShotWrapper
    import com.kazurayam.ashotwrapper.DevicePixelRatioResolver
    import com.kazurayam.timekeeper.Measurement
    import com.kazurayam.timekeeper.Timekeeper
    import io.github.bonigarcia.wdm.WebDriverManager
    import org.junit.jupiter.api.AfterEach
    import org.junit.jupiter.api.BeforeAll
    import org.junit.jupiter.api.BeforeEach
    import org.junit.jupiter.api.Test
    import org.openqa.selenium.Dimension
    import org.openqa.selenium.WebDriver
    import org.openqa.selenium.chrome.ChromeDriver
    import org.openqa.selenium.chrome.ChromeOptions

    import java.util.concurrent.TimeUnit;

    import java.awt.image.BufferedImage;
    import java.nio.file.Files
    import java.nio.file.Path
    import java.nio.file.Paths
    import javax.imageio.ImageIO
    import java.time.LocalDateTime

    import static org.junit.jupiter.api.Assertions.*;

    /**
     * learned "How to Take Screenshot in Selenium WebDriver" of Guru99
     * https://www.guru99.com/take-screenshot-selenium-webdriver.html
     */
    class TimekeeperDemo {

        private WebDriver driver_
        static private Path outdir_
        private AShotWrapper.Options aswOptions_ = null

        @BeforeAll
        static void setupClass() {
            WebDriverManager.chromedriver().setup();
            outdir_ = Paths.get(".")
                    .resolve("build/tmp/testOutput")
                    .resolve(TimekeeperDemo.class.getSimpleName())
            if (Files.exists(outdir_)) {
                outdir_.toFile().deleteDir();
            }
            Files.createDirectory(outdir_)
        }

        @BeforeEach
        void setupTest() {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--headless");
            driver_ = new ChromeDriver(options);
            driver_.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
            driver_.manage().window().setSize(new Dimension(1200, 800));
            //
            float dpr = DevicePixelRatioResolver.resolveDPR(driver_);
            aswOptions_ = new AShotWrapper.Options.Builder().devicePixelRatio(dpr).build();
        }

        @AfterEach
        void tearDown() {
            if (driver_ != null) {
                driver_.quit();
            }
        }

        @Test
        void test_demo() {
            Timekeeper tk = new Timekeeper()
            Measurement navigation = tk.newMeasurement("How long it took to navigate to URLs", ["URL"])
            Measurement screenshot = tk.newMeasurement("How long it took to take shootshots", ["URL"])
            // process all URLs in the CSV file
            Path csv = Paths.get(".").resolve("src/test/fixtures/URLs.csv");
            for (Tuple t in parseCSVfile(csv)) {
                String url = t.get(0)
                String filename = t.get(1)
                driver_.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                // navigate to the URL, record the duration
                LocalDateTime beforeNavigate = LocalDateTime.now()
                driver_.navigate().to(url)
                LocalDateTime afterNavigate = LocalDateTime.now()
                navigation.record(["URL": url], beforeNavigate, afterNavigate)
                // take a screenshot of the page, record the duration
                LocalDateTime beforeScreenshot = LocalDateTime.now()
                this.takeFullPageScreenshot(driver_, outdir_, filename)
                LocalDateTime afterScreenshot = LocalDateTime.now()
                screenshot.record(["URL": url], beforeScreenshot, afterScreenshot)
            }
            // now print the report
            tk.report(outdir_.resolve("report.md"))
        }

        private void takeFullPageScreenshot(WebDriver driver, Path outDir, String fileName) {
            // using my AShotWrapper lib at https://kazurayam.github.io/ashotwrapper/
            BufferedImage image = AShotWrapper.takeEntirePageImage(driver, aswOptions_);
            assertNotNull(image);
            File screenshotFile = outDir.resolve(fileName).toFile();
            ImageIO.write(image, "PNG", screenshotFile);
            assertTrue(screenshotFile.exists());
        }

        /**
         * read a CSV file of:
         *
         * url1,filename1
         * url2,filename2
         * url3,filename3
         * ...
         *
         * @param csv
         * @return
         */
        private List<Tuple2> parseCSVfile(Path csv) {
            List<Tuple2> result = new ArrayList<Tuple2>()
            List<String> lines = csv.toFile() as List<String>
            for (String line in lines) {
                String[] items = line.split(",")
                if (items.size() >= 2) {
                    result.add(new Tuple2(items[0].trim(), items[1].trim()))
                }
            }
            return result
        }

    }

This will emit the following output:

    ## How long it took to navigate to URLs

    |URL|duration|duration graph|
    |:----|----:|:----|
    |https://www.google.com/search?q=timekeeper|00:01|`#`|
    |https://duckduckgo.com/?q=timekeeper&t=h_&ia=web|00:02|`#`|
    |https://search.yahoo.co.jp/search?p=timekeeper|00:03|`#`|

    ----
    ## How long it took to take shootshots

    |URL|duration|duration graph|
    |:----|----:|:----|
    |https://www.google.com/search?q=timekeeper|00:05|`#`|
    |https://duckduckgo.com/?q=timekeeper&t=h_&ia=web|00:03|`#`|
    |https://search.yahoo.co.jp/search?p=timekeeper|00:03|`#`|

    ----

On browser, this will look like this:

![report](../docs/images/report.png)

# Motivation, etc.

I developed some Web UI tests in Groovy using Selenium. I wanted to measure their performances; how long (seconds) they take to navigate browsers to many URLs, how long they take to take and save screenshots of the web pages. I wanted to examine many URLS; say 100 or more. It was a hard task to record the durations with a stopwatch and to write a report in a Markdown table format. I desperately wanted to automate this task.

# published at Maven Central

The artifact is available at the Maven Central repository:

-   <https://mvnrepository.com/artifact/com.kazurayam/timekeeper>
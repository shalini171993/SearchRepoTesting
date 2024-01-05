package runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;


@CucumberOptions(
        glue = "steps",
        plugin = { "pretty",
                "html:target/cucumber-reports.html",
                "json:target/cucumber-reports/cucumber.json",
                "rerun:target/failedrerun.txt"},
        monochrome = true,
        features = {"@target/failedRerun.txt"}
)

public class FailedTestRunner extends AbstractTestNGCucumberTests {

}
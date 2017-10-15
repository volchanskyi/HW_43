package core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class IE {

    static WebDriver driver;

    public static void main(String[] args) throws InterruptedException {

	Logger logger = Logger.getLogger("");
	logger.setLevel(Level.WARNING);

	String driverPath = "./resources/webdrivers/pc/IEDriverServer32.exe";
	String[] urls = { "http://alex.academy/exe/payment_tax/index.html",
		"http://alex.academy/exe/payment_tax/index2.html", "http://alex.academy/exe/payment_tax/index3.html",
		"http://alex.academy/exe/payment_tax/index4.html", "http://alex.academy/exe/payment_tax/index4.html",
		"http://alex.academy/exe/payment_tax/indexE.html" };
	if (!System.getProperty("os.name").contains("Windows")) {
	    throw new IllegalArgumentException("Internet Explorer is available only on Windows");
	}

	DesiredCapabilities IEDesiredCapabilities = DesiredCapabilities.internetExplorer();
	IEDesiredCapabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,
		true);
	IEDesiredCapabilities.setCapability(InternetExplorerDriver.INITIAL_BROWSER_URL, "");
	IEDesiredCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
	IEDesiredCapabilities.setJavascriptEnabled(true);
	IEDesiredCapabilities.setCapability("enablePersistentHover", false);

	System.setProperty("webdriver.ie.driver", driverPath);
	driver = new InternetExplorerDriver(IEDesiredCapabilities);
	driver.manage().window().maximize();
	System.out.println("Browser: IE");
	for (String i : urls) {
	    driver.get(i);
	   

	    String string_monthly_payment_and_tax = driver.findElement(By.id("id_monthly_payment_and_tax")).getText();
	    String regex = "^" + "(?:\\w[A-za-z]{6}\\:\\s)?(?:\\$)?(\\d{2}\\.\\d{2})"
		    + "(?:\\,*\\/*\\s*)(?:\\w[A-za-z]{2}\\:\\s)?(?:(\\d{1}\\.\\d{2})\\%*)" + "$";
	    Pattern p = Pattern.compile(regex);
	    Matcher m = p.matcher(string_monthly_payment_and_tax);
	    m.find();
	    double monthly_payment = Double.parseDouble(m.group(1));
	    double tax = Double.parseDouble(m.group(2));
	    // (91.21 * 8.25) / 100 = 7.524825 rounded => 7.52
	    double monthly_and_tax_amount = new BigDecimal((monthly_payment * tax) / 100)
		    .setScale(2, RoundingMode.HALF_UP).doubleValue();
	    // 91.21 + 7.52 = 98.72999999999999 rounded => 98.73
	    double monthly_payment_with_tax = new BigDecimal(monthly_payment + monthly_and_tax_amount)
		    .setScale(2, RoundingMode.HALF_UP).doubleValue();
	    double annual_payment_with_tax = new BigDecimal(monthly_payment_with_tax * 12)
		    .setScale(2, RoundingMode.HALF_UP).doubleValue();
	    driver.findElement(By.id("id_annual_payment_with_tax")).sendKeys(String.valueOf(annual_payment_with_tax));

	    WebElement settings = driver.findElement(By.id("id_validate_button"));
	    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", settings);

	    String actual_result = driver.findElement(By.id("id_result")).getText();
	    System.out.println("+------------------------------------+");
	    System.out.println("URL: " + i);
	    System.out.println("String: \t" + string_monthly_payment_and_tax);
	    System.out.println("Annual Payment with Tax: " + annual_payment_with_tax);
	    System.out.println("Result: \t" + actual_result);

	}
	driver.quit();
    }

}

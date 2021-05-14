package com.mark59.datahunter.performanceTest.scripts;

// THIS IMPORT MUST BE LEFT COMMENTED OUT!! : 
// import org.apache.jmeter.samplers.SampleResult;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import com.mark59.core.Outcome;
import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.core.utils.Mark59Constants;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.AddPolicyActionPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.AddPolicyPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.CountPoliciesActionPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.CountPoliciesBreakdownActionPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.CountPoliciesBreakdownPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.CountPoliciesPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.DeleteMultiplePoliciesActionPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.NextPolicyActionPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.NextPolicyPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.PrintSelectedPoliciesActionPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.PrintSelectedPoliciesPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages._GenericDatatHunterActionPage;
import com.mark59.selenium.corejmeterimpl.JmeterFunctionsForSeleniumScripts;
import com.mark59.selenium.corejmeterimpl.KeepBrowserOpen;
import com.mark59.selenium.corejmeterimpl.SeleniumAbstractJavaSamplerClient;
import com.mark59.selenium.drivers.SeleniumDriverFactory;
import com.mark59.seleniumDSL.core._GenericPage;
import com.mark59.seleniumDSL.pageElements.DropdownList;
import com.mark59.seleniumDSL.pageElements.HtmlTable;
import com.mark59.seleniumDSL.pageElements.HtmlTableRow;
import com.mark59.seleniumDSL.pageElements.InputTextElement;
import com.mark59.seleniumDSL.pageElements.SubmitBtn;

import com.mark59.datahunter.performanceTest.scripts.TestConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import com.mark59.seleniumDSL.core.Elemental;
import com.mark59.seleniumDSL.core.FluentWaitFactory;
import com.mark59.seleniumDSL.core.SafeSleep;
import java.time.Duration;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

// >> COMMENT OUT THE NEXT TWO LINES 
public class DataHunterLifecyclePvtScriptAsSingleJSR223  {
public static void main(String[] args) throws InterruptedException{
// <<  

	Logger LOG = LogManager.getLogger();	

class ThisScript extends SeleniumAbstractJavaSamplerClient {
	
	final class DeleteMultiplePoliciesPageInnerClass extends _GenericPage {

		public DeleteMultiplePoliciesPageInnerClass(WebDriver driver) {
			super(driver);
		}
		public InputTextElement application() {
			return new InputTextElement(driver, By.id("application"));
		};
		public InputTextElement lifecycle() {
			return new InputTextElement(driver, By.id("lifecycle"));
		};
		public DropdownList useability() {
			return new DropdownList(driver, By.id("useability"));
		};
		public SubmitBtn submit() {
			return new SubmitBtn(driver, By.id("submit"));
		};
	}
	
	@Override
	protected Map<String, String> additionalTestParameters() {
		Map<String, String> jmeterAdditionalParameters = new LinkedHashMap<String, String>();
		jmeterAdditionalParameters.put("DATAHUNTER_URL_HOST_PORT",	"http://localhost:8081");
		jmeterAdditionalParameters.put("DATAHUNTER_APPLICATION_ID", "DATAHUNTER_PV_TEST");
		jmeterAdditionalParameters.put("FORCE_TXN_FAIL_PERCENT", 	"20");
		jmeterAdditionalParameters.put("USER", 	 "default_user");		

		jmeterAdditionalParameters.put(SeleniumDriverFactory.DRIVER, "CHROME");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.HEADLESS_MODE, String.valueOf(false));
		jmeterAdditionalParameters.put(SeleniumDriverFactory.BROWSER_DIMENSIONS, Mark59Constants.DEFAULT_BROWSER_DIMENSIONS);
		jmeterAdditionalParameters.put(SeleniumDriverFactory.PAGE_LOAD_STRATEGY, PageLoadStrategy.NONE.toString());
		jmeterAdditionalParameters.put(SeleniumDriverFactory.PROXY, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.ADDITIONAL_OPTIONS, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.WRITE_FFOX_BROWSER_LOGFILE, String.valueOf(false));
		jmeterAdditionalParameters.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");			
		jmeterAdditionalParameters.put(SeleniumDriverFactory.EMULATE_NETWORK_CONDITIONS, "");			
		return jmeterAdditionalParameters;			
	}
	

	@Override
	protected void runSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm,  WebDriver driver) {
		
		String thread = Thread.currentThread().getName();
		String lifecycle = "thread_" + thread;
		String dataHunterUrl 	= context.getParameter("DATAHUNTER_URL_HOST_PORT");
		String application 		= context.getParameter("DATAHUNTER_APPLICATION_ID");
		int forceTxnFailPercent = Integer.valueOf(context.getParameter("FORCE_TXN_FAIL_PERCENT").trim());
		String user 			= context.getParameter("USER");
		
		PrintSomeMsgOnceAtStartUp(dataHunterUrl, driver);

		DeleteMultiplePoliciesPageInnerClass deleteMultiplePoliciesPage = new DeleteMultiplePoliciesPageInnerClass(driver); 

// 		delete any existing policies for this application/thread combination
		jm.startTransaction("DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl");
		driver.get(dataHunterUrl + TestConstants.DELETE_MULTIPLE_POLICIES_URL_PATH + "?application=" + application);
		deleteMultiplePoliciesPage.lifecycle().waitUntilClickable();
		jm.endTransaction("DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl");	
		
		deleteMultiplePoliciesPage.lifecycle().type(lifecycle);

		DeleteMultiplePoliciesActionPage deleteMultiplePoliciesActionPage = new DeleteMultiplePoliciesActionPage(driver);
		
		jm.startTransaction("DH-lifecycle-0100-deleteMultiplePolicies");		
		deleteMultiplePoliciesPage.submit().submit().waitUntilClickable( deleteMultiplePoliciesActionPage.backLink() );   // ** note 1
		waitActionPageCheckSqlOk(new DeleteMultiplePoliciesActionPage(driver));
		jm.endTransaction("DH-lifecycle-0100-deleteMultiplePolicies");	
	
//		add a set of policies 		
		driver.get(dataHunterUrl + TestConstants.ADD_POLICY_URL_PATH + "?application=" + application);
		AddPolicyPage addPolicyPage = new AddPolicyPage(driver);
		
		for (int i = 1; i <= 5; i++) {
			addPolicyPage.identifier().type("TESTID" + i);
			addPolicyPage.lifecycle().type(lifecycle);
			addPolicyPage.useability().selectByVisibleText(TestConstants.UNUSED) ;
			addPolicyPage.otherdata().type(user);		
			addPolicyPage.epochtime().type(new String(Long.toString(System.currentTimeMillis())));
//			jm.writeScreenshot("add_policy_TESTID" + i);

			AddPolicyActionPage addPolicyActionPage = new AddPolicyActionPage(driver);			
			
			jm.startTransaction("DH-lifecycle-0200-addPolicy");
			addPolicyPage.submit().submit().waitUntilClickable( addPolicyActionPage.backLink() );   // ** note 1;	
			waitActionPageCheckSqlOk(addPolicyActionPage);
			jm.endTransaction("DH-lifecycle-0200-addPolicy");
			
			addPolicyActionPage.backLink().click().waitUntilClickable( addPolicyPage.submit() ).thenSleep();    // ** note 1 & note 2
		} 
	
//		dummy transaction just to test transaction failure behavior
		jm.startTransaction("DH-lifecycle-0299-sometimes-I-fail");
		int randomNum_1_to_100 = ThreadLocalRandom.current().nextInt(1, 101);
		if ( randomNum_1_to_100 >= forceTxnFailPercent ) {
			jm.endTransaction("DH-lifecycle-0299-sometimes-I-fail", Outcome.PASS);
		} else {
			jm.endTransaction("DH-lifecycle-0299-sometimes-I-fail", Outcome.FAIL);
		}
		
		driver.get(dataHunterUrl + TestConstants.COUNT_POLICIES_URL_PATH + "?application=" + application);
		CountPoliciesPage countPoliciesPage = new CountPoliciesPage(driver); 
		countPoliciesPage.useability().selectByVisibleText(TestConstants.UNUSED).thenSleep();   // ** note 2
		
		CountPoliciesActionPage countPoliciesActionPage = new CountPoliciesActionPage(driver);	

		jm.startTransaction("DH-lifecycle-0300-countUnusedPolicies");
		countPoliciesPage.submit().submit().waitUntilClickable( countPoliciesActionPage.backLink() );
		waitActionPageCheckSqlOk(countPoliciesActionPage);
		jm.endTransaction("DH-lifecycle-0300-countUnusedPolicies");
		
		Long countPolicies = Long.valueOf( countPoliciesActionPage.rowsAffected().getText());
		LOG.debug( "countPolicies : " + countPolicies); 
		jm.userDataPoint(application + "_Total_Unused_Policy_Count", countPolicies);
		
// 		count breakdown (count for unused DATAHUNTER_PV_TEST policies for this thread )
		driver.get(dataHunterUrl + TestConstants.COUNT_POLICIES_BREAKDOWN_URL_PATH + "?application=" + application);		
		CountPoliciesBreakdownPage countPoliciesBreakdownPage = new CountPoliciesBreakdownPage(driver);
		countPoliciesBreakdownPage.applicationStartsWithOrEquals().selectByVisibleText(TestConstants.EQUALS);
		countPoliciesBreakdownPage.useability().selectByVisibleText(TestConstants.UNUSED);
		
		CountPoliciesBreakdownActionPage countPoliciesBreakdownActionPage = new CountPoliciesBreakdownActionPage(driver);	

		jm.startTransaction("DH-lifecycle-0400-countUnusedPoliciesCurrentThread");		
		countPoliciesBreakdownPage.submit().submit();
		waitActionPageCheckSqlOk(countPoliciesBreakdownActionPage);		
		jm.endTransaction("DH-lifecycle-0400-countUnusedPoliciesCurrentThread");				
		
		// direct access to required row-column table element by computing the id:
		int countUsedPoliciesCurrentThread = countPoliciesBreakdownActionPage.getCountForBreakdown(application, lifecycle, TestConstants.UNUSED); 
		LOG.debug( "countUsedPoliciesCurrentThread : " + countUsedPoliciesCurrentThread); 
		jm.userDataPoint(application + "_This_Thread_Unused_Policy_Count", countUsedPoliciesCurrentThread);	

//		use next policy
		driver.get(dataHunterUrl + TestConstants.NEXT_POLICY_URL_PATH + "?application=" + application + "&pUseOrLookup=use");		
		NextPolicyPage nextPolicyPage = new NextPolicyPage(driver); 
		nextPolicyPage.lifecycle().type(lifecycle);
		nextPolicyPage.useability().selectByVisibleText(TestConstants.UNUSED);
		nextPolicyPage.selectOrder().selectByVisibleText(TestConstants.SELECT_MOST_RECENTLY_ADDED);

		NextPolicyActionPage nextPolicyActionPage = new NextPolicyActionPage(driver);		
		
		jm.startTransaction("DH-lifecycle-0500-useNextPolicy");		
		nextPolicyPage.submit().submit();
		waitActionPageCheckSqlOk(nextPolicyActionPage);			
		jm.endTransaction("DH-lifecycle-0500-useNextPolicy");	
		
		if (LOG.isDebugEnabled() ) {LOG.debug("useNextPolicy: " + application + "-" + lifecycle + " : " + nextPolicyActionPage.identifier() );	}
		
		//HTML table demo.
		long used=0;
		long unused=0;
		
		driver.get(dataHunterUrl + TestConstants.PRINT_SELECTED_POLICIES_URL_PATH  + "?application=" + application);
		PrintSelectedPoliciesPage printSelectedPoliciesPage = new PrintSelectedPoliciesPage(driver);
		printSelectedPoliciesPage.submit().waitUntilClickable();

		PrintSelectedPoliciesActionPage printSelectedPoliciesActionPage = new PrintSelectedPoliciesActionPage(driver);
		
		jm.startTransaction("DH-lifecycle-0600-displaySelectedPolicies");	
		printSelectedPoliciesPage.submit().submit();
		waitActionPageCheckSqlOk(printSelectedPoliciesActionPage);
		// demo how to extract a transaction time from with a running script 
		org.apache.jmeter.samplers.SampleResult sr_0600 = jm.endTransaction("DH-lifecycle-0600-displaySelectedPolicies");
		
		LOG.debug("Transaction " + sr_0600.getSampleLabel() + " ran at " + sr_0600.getTimeStamp() + " and took " + sr_0600.getTime() + " ms." );
		
		HtmlTable printSelectedPoliciesTable = printSelectedPoliciesActionPage.printSelectedPoliciesTable();
		for (HtmlTableRow tableRow : printSelectedPoliciesTable.getHtmlTableRows()) {
			if (tableRow.getColumnNumberOfExpectedColumns(4, 8).getText().equals("USED"))   used++;
			if (tableRow.getColumnNumberOfExpectedColumns(4, 8).getText().equals("UNUSED")) unused++;
		}	
		jm.userDataPoint("USED-count-html-demo",   used );				
		jm.userDataPoint("UNUSED-count-html-demo", unused );	
		LOG.debug("HTML demo: USED=" + used + ", UNUSED=" + unused); 
		
// 		delete multiple policies (test cleanup - a duplicate of the initial delete policies transactions)
		jm.startTransaction("DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl");		
		driver.get(dataHunterUrl + TestConstants.DELETE_MULTIPLE_POLICIES_URL_PATH + "?application=" + application);
		deleteMultiplePoliciesPage.lifecycle().waitUntilClickable();		
		jm.endTransaction("DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl");	
		
		deleteMultiplePoliciesPage.lifecycle().type(lifecycle);
		
		jm.startTransaction("DH-lifecycle-0100-deleteMultiplePolicies");		
		deleteMultiplePoliciesPage.submit().submit();
		waitActionPageCheckSqlOk(new DeleteMultiplePoliciesActionPage(driver));
		jm.endTransaction("DH-lifecycle-0100-deleteMultiplePolicies");	
		
//		jm.writeBufferedArtifacts();
	}


	private void waitActionPageCheckSqlOk(_GenericDatatHunterActionPage _genericDatatHunterActionPage) {
		String sqlResultText = _genericDatatHunterActionPage.sqlResult().getText();
		if (!"PASS".equals(sqlResultText)) {
			throw new RuntimeException("SQL issue (" + sqlResultText + ") : " +
						_genericDatatHunterActionPage.formatResultsMessage(_genericDatatHunterActionPage.getClass().getName()));
		}
	}
	
	@SuppressWarnings("unchecked")
	private synchronized void PrintSomeMsgOnceAtStartUp(String dataHunterUrl, WebDriver driver) {
		if (!"true".equals(System.getProperty("printedOnce")) ) {	
			LOG.info("  using DataHunter with Url " + dataHunterUrl + "/dataHunter");
			Capabilities caps = ((ChromeDriver)driver).getCapabilities();
			LOG.info(" Browser Name and Version : " + caps.getBrowserName() + " " + caps.getVersion());
			if ("chrome".equalsIgnoreCase(caps.getBrowserName()) && caps.getCapability("chrome") != null ){
				String chromedriverVersion =  ((Map<String, String>)caps.getCapability("chrome")).get("chromedriverVersion");
				LOG.info(" Chrome Driver Version    : " +  ((Map<String, String>)caps.getCapability("chrome")).get("chromedriverVersion"));
				if (chromedriverVersion != null &&  chromedriverVersion.startsWith("2.44") ) {
					String outDatedDriver = "\n\n You are using the outdated ChromeDriver that ships with the Mark59 Selenium test scripts " +
							" project 'dataHunterPerformanceTestSamples'.  It may be unstable or not work at all." + 				
							"\n - Please visit https://chromedriver.chromium.org/downloads and update to a ChomeDriver which supports " +
							"Chrome browser version " + caps.getVersion() + "\n";
					System.out.println(outDatedDriver);
					LOG.warn(outDatedDriver);
				}	
			}
			System.setProperty("printedOnce", "true");
		}
	}
};

// >> COMMENT OUT THE NEXT THREE LINES 
org.apache.jmeter.samplers.SampleResult SampleResult = new org.apache.jmeter.samplers.SampleResult();
SampleResult.sampleStart();
Log4jConfigurationHelper.init(Level.INFO) ;
// << 

if (System.getProperty("printedOnce") == null) {
    System.setProperty("printedOnce", "false");
}
ThisScript thisScript = new ThisScript();
org.apache.jmeter.samplers.SampleResult testSampleResult = thisScript.runSeleniumTest(KeepBrowserOpen.NEVER);
org.apache.jmeter.samplers.SampleResult[] testsubResults = testSampleResult.getSubResults();

for (int i = 0; i < testSampleResult.getSubResults().length; i++) {
	SampleResult.addSubResult(testsubResults[i], false);
}
SampleResult.setDataType("PARENT" );
SampleResult.setEndTime(0);
// COMMENT OUT THE END BRACES BELOW 
}
}
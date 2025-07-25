/*
 *  Copyright 2019 Mark59.com
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mark59.datahunter.samples.selenium.scripts;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.Outcome;
import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.Mark59LogLevels;
import com.mark59.core.utils.SafeSleep;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.AddPolicyActionPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.AddPolicyPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.CountPoliciesActionPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.CountPoliciesBreakdownActionPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.CountPoliciesBreakdownPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.CountPoliciesPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.MultiplePoliciesActionPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.MultiplePoliciesPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.NextPolicyActionPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.NextPolicyPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages._GenericDataHunterActionPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages._Navigation;
import com.mark59.datahunter.samples.dsl.helpers.DslConstants;
import com.mark59.dsl.samples.devtoolsDSL.DevToolsDSL;
import com.mark59.dsl.samples.seleniumDSL.core.ElementalUsingVariablePolling;
import com.mark59.dsl.samples.seleniumDSL.core.FluentWaitVariablePolling;
import com.mark59.dsl.samples.seleniumDSL.pageElements.HtmlTable;
import com.mark59.dsl.samples.seleniumDSL.pageElements.HtmlTableRow;
import com.mark59.scripting.KeepBrowserOpen;
import com.mark59.scripting.selenium.JmeterFunctionsForSeleniumScripts;
import com.mark59.scripting.selenium.SeleniumAbstractJavaSamplerClient;
import com.mark59.scripting.selenium.driversimpl.SeleniumDriverFactory;

/**
 * <p>This sample is meant to demonstrate how to structure a Mark59 selenium script (not how to use use DataHunter in a performance test -
 * see below). It uses a style of DSL which we suggest would be usable for most performance tests. For simple html pages a DSL not having element 
 * "wait until"s may suffice.
 *        
 * <p>** Note 1.  The use of a <b>PAGE_LOAD_STRATEGY</b> of <b>NONE</b>.  This means you must control all page load timing in the script, usually
 * by waiting for an element on the next page to become available (for example by waiting for the first item on the page that you 
 * intend to click on becoming clickable).  
 * 
 * <p>**Note 2 and 3.  The <code>waitUntilClickable(..)</code> or <code>thenSleep()</code> methods are not necessary here, the simplicity of the 
 * pages don't require it. Also, for Note 2, the <code>checkSqlOk</code> already has a wait built into it (<code>getText</code> for the SQL result,
 * so that would give you the correct timing anyway). Included to demonstrate what would be required in more complex situations, such as on pages 
 * where you need to wait for async processes to execute.
 * 
 * <p>**Note 4.  Instead of using the Elemental class which works in conjunction with Selenium's standard FluentWait, class 
 * {@link ElementalUsingVariablePolling} is used which works in conjunction with a custom version of FluentWaint, {@link FluentWaitVariablePolling}.
 * It takes a list of polling interval times, rather than just the one constant value using in FluentWaint for waiting between polls.  
 * For example, you can set a very short polling time for the first polling (has been found useful in some situations like 'waitUntilStale'
 * conditions or modal spinners), and/or then increase polling intervals incrementally (the last value in the list is just repeated), 
 * to reduce CPU load.  In practice we have found the reduction in CPU usage relatively minimal for the tests we have implemented it.    
 * 
 * <p>In a performance test, DataHunter should be invoked using it's API. Review 
 * {@link com.mark59.datahunter.api.rest.samples.DataHunterRestApiClientSampleUsage}, and 
 * DataHunterLifecyclePvtScriptUsingApiViaHttpRequestsTestPlan.jmx (the test-plans folder in this project).  These samples replicate the basic 
 * functionality of this script, giving a three way comparison of the DataHunter HTML pages, the DataHunter Java API Client, and direct http 
 * invocation of the API.     
 * 
 * @see SeleniumAbstractJavaSamplerClient
 * 
 * @author Philip Webb
 * Written: Australian Winter 2019
 * 
 */
public class DataHunterLifecyclePvtScript  extends SeleniumAbstractJavaSamplerClient {

	private static final Logger LOG = LogManager.getLogger(DataHunterLifecyclePvtScript.class);	
	
	
	/**
	 *  Construct the parameter map seen in the JMeter Java Request panel.  These values can be overridden in that panel.
	 *  <p>For example, it would be usual to have <code>HEADLESS_MODE</code> set to <code>false</code> here, so you can run the script 
	 *  in the IDE and see the browser, but the override the <code>HEADLESS_MODE</code> value to <code>true</code> in the JMeter test plan.
	 *  <p>Similarly for <code>PRINT_RESULTS_SUMMARY</code>.  You may want to see the results when running in the IDE, but set it to
	 *  <code>false</code> (which is also the default) when executing in JMeter, to minimize logging.       
	 */
	@Override
	protected Map<String, String> additionalTestParameters() {
		Map<String, String> jmeterAdditionalParameters = new LinkedHashMap<>();
		// user defined parameters
		jmeterAdditionalParameters.put("DATAHUNTER_URL", "http://localhost:8081/mark59-datahunter");
		jmeterAdditionalParameters.put("DATAHUNTER_APPLICATION_ID", "DATAHUNTER_PV_TEST");
		jmeterAdditionalParameters.put("FORCE_TXN_FAIL_PERCENT", "20");
		jmeterAdditionalParameters.put("START_CDP_LISTENERS", String.valueOf(true));
		jmeterAdditionalParameters.put("USER", "default_user");				
		jmeterAdditionalParameters.put("FORCE_EXCEPTION", String.valueOf(false));				

		// optional selenium driver related settings (defaults apply)
		jmeterAdditionalParameters.put(SeleniumDriverFactory.DRIVER, Mark59Constants.CHROME);
		jmeterAdditionalParameters.put(SeleniumDriverFactory.HEADLESS_MODE, String.valueOf(false));
		jmeterAdditionalParameters.put(SeleniumDriverFactory.PAGE_LOAD_STRATEGY, PageLoadStrategy.NONE.toString());				// ** note 1
		jmeterAdditionalParameters.put(SeleniumDriverFactory.BROWSER_DIMENSIONS, Mark59Constants.DEFAULT_BROWSER_DIMENSIONS);
		jmeterAdditionalParameters.put(SeleniumDriverFactory.PROXY, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.ADDITIONAL_OPTIONS, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.WRITE_FFOX_BROWSER_LOGFILE, String.valueOf(false));
		jmeterAdditionalParameters.put(SeleniumDriverFactory.UNHANDLED_PROMPT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE.toString());
//		jmeterAdditionalParameters.put(ScriptingConstants.OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE, "");		
		
		// optional log settings, defaults shown 
		jmeterAdditionalParameters.put(JmeterFunctionsForSeleniumScripts.LOG_SCREENSHOTS_AT_START_OF_TRANSACTIONS,	Mark59LogLevels.DEFAULT.getName());
		jmeterAdditionalParameters.put(JmeterFunctionsForSeleniumScripts.LOG_SCREENSHOTS_AT_END_OF_TRANSACTIONS, 	Mark59LogLevels.DEFAULT.getName());
		jmeterAdditionalParameters.put(JmeterFunctionsForSeleniumScripts.LOG_PAGE_SOURCE_AT_START_OF_TRANSACTIONS,	Mark59LogLevels.DEFAULT.getName());
		jmeterAdditionalParameters.put(JmeterFunctionsForSeleniumScripts.LOG_PAGE_SOURCE_AT_END_OF_TRANSACTIONS, 	Mark59LogLevels.DEFAULT.getName());
		jmeterAdditionalParameters.put(JmeterFunctionsForSeleniumScripts.LOG_PERF_LOG_AT_END_OF_TRANSACTIONS, 		Mark59LogLevels.DEFAULT.getName());		

		jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_BUFFERED_LOGS,				String.valueOf(true));
		jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_SCREENSHOT, 					String.valueOf(true));
		jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_PAGE_SOURCE, 					String.valueOf(true));
		jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_PERF_LOG,						String.valueOf(true));
		jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_STACK_TRACE,					String.valueOf(true));
		jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_STACK_TRACE_TO_CONSOLE,	 	String.valueOf(true));
		jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_STACK_TRACE_TO_LOG4J_LOGGER,	String.valueOf(true));				
		
		jmeterAdditionalParameters.put(JmeterFunctionsImpl.LOG_RESULTS_SUMMARY, 		String.valueOf(true));  // default is 'false'		
		jmeterAdditionalParameters.put(JmeterFunctionsImpl.PRINT_RESULTS_SUMMARY, 		String.valueOf(false));		

		// optional miscellaneous settings (defaults apply) 
		jmeterAdditionalParameters.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");			
		jmeterAdditionalParameters.put(SeleniumDriverFactory.EMULATE_NETWORK_CONDITIONS, "");			
	
		return jmeterAdditionalParameters;			
	}
	

	@Override
	protected void runSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm,  WebDriver driver) {

// 		These log settings can be used to override log4j based defaults and transaction log-related additionalTestParameters 		
//		jm.logScreenshotsAtStartOfTransactions(Mark59LogLevels.WRITE);
//		jm.logScreenshotsAtEndOfTransactions(Mark59LogLevels.WRITE);
//		jm.logPageSourceAtStartOfTransactions(Mark59LogLevels.WRITE);		
//		jm.logPageSourceAtEndOfTransactions(Mark59LogLevels.WRITE );
//		jm.logPerformanceLogAtEndOfTransactions(Mark59LogLevels.WRITE);
//		// you need to use jm.writeBufferedArtifacts to output BUFFERed data (see end of this method)		
//		jm.logAllLogsAtEndOfTransactions(Mark59LogLevels.BUFFER);
//		jm.writeLog("Kilroy","txt","Kilroy was here".getBytes());  //DIY log entry		
//		jm.bufferLog("Kilroy","txt","Kilroy was buffered".getBytes());  //DIY buffered log entry				
		
		String lifecycle = "thread_" + Thread.currentThread().getName().replace(" ", "_").replace(".", "_");
//		System.out.println("Thread " + thread + " is running with LOG level " + LOG.getLevel());
		
		// Start browser to cater for initial launch time (for Firefox try "about:preferences") 
		driver.get("chrome://version/");
		SafeSleep.sleep(1000);
        
		String dataHunterUrl      = context.getParameter("DATAHUNTER_URL");
		String application        = context.getParameter("DATAHUNTER_APPLICATION_ID");
		int forceTxnFailPercent   = Integer.parseInt(context.getParameter("FORCE_TXN_FAIL_PERCENT").trim());
		boolean startCdpListeners = Boolean.parseBoolean(context.getParameter("START_CDP_LISTENERS"));
		String user               = context.getParameter("USER");
		boolean forceException    = Boolean.parseBoolean(context.getParameter("FORCE_EXCEPTION"));		
				
		PrintSomeMsgOnceAtStartUp(dataHunterUrl, driver);

		if (startCdpListeners) {
			startCdpListeners(jm, driver);
		}
		
		_Navigation _navigation = new _Navigation(driver); 
		MultiplePoliciesPage multiplePoliciesPage = new MultiplePoliciesPage(driver); 
		MultiplePoliciesActionPage multiplePoliciesActionPage = new MultiplePoliciesActionPage(driver);

// 		select policies for this application/thread combination
		jm.startTransaction("DH_lifecycle_0001_loadInitialPage");
		driver.get(dataHunterUrl + DslConstants.SELECT_MULTIPLE_POLICIES_URL_PATH + "?application=" + application);
		multiplePoliciesPage.lifecycle().waitUntilClickable();
		multiplePoliciesPage.lifecycle().type(lifecycle);
		multiplePoliciesPage.submit().submit().waitUntilClickable( multiplePoliciesActionPage.backLink() );		
		jm.endTransaction("DH_lifecycle_0001_loadInitialPage");	

		jm.startTransaction("DH_lifecycle_0100_deleteMultiplePolicies");		
		multiplePoliciesActionPage.multipleDeleteLink().click().waitUntilAlertisPresent().acceptAlert();
		waitForSqlResultsTextOnActionPageAndCheckOk(multiplePoliciesActionPage);
		jm.endTransaction("DH_lifecycle_0100_deleteMultiplePolicies");	
	
//		add a set of policies 		
		_navigation.addItemLink().click();
		
		for (int i = 1; i <= 5; i++) {
			AddPolicyPage addPolicyPage = new AddPolicyPage(driver);
			addPolicyPage.identifier().clear().type("TESTID" + i);   
			addPolicyPage.lifecycle().clear().type(lifecycle);
			addPolicyPage.useability().selectByVisibleText(DslConstants.UNUSED) ;
			addPolicyPage.otherdata().clear().type(user);		
			addPolicyPage.epochtime().clear().type(Long.toString(System.currentTimeMillis()));
//			jm.writeScreenshot("add_policy_TESTID" + i);

			AddPolicyActionPage addPolicyActionPage = new AddPolicyActionPage(driver);			
			
			jm.startTransaction("DH_lifecycle_0200_addPolicy");
			if (forceException){throw new RuntimeException("Selenium script throws runtime ex");};
			SafeSleep.sleep(200);  // Mocking a 200 ms txn delay
			addPolicyPage.submit().submit().waitUntilClickable( addPolicyActionPage.backLink() );   // ** note 2	
			waitForSqlResultsTextOnActionPageAndCheckOk(addPolicyActionPage);						// ** note 4 
			jm.endTransaction("DH_lifecycle_0200_addPolicy");
			
			addPolicyActionPage.backLink().click().waitUntilClickable( addPolicyPage.submit() ).thenSleep();    // ** note 2, 3 
		} 
	
//		dummy transaction just to test transaction failure behavior
		jm.startTransaction("DH_lifecycle_0299_sometimes_I_fail");
		int randomNum_1_to_100 = ThreadLocalRandom.current().nextInt(1, 101);
		if ( randomNum_1_to_100 >= forceTxnFailPercent ) {
			jm.endTransaction("DH_lifecycle_0299_sometimes_I_fail", Outcome.PASS);
		} else {
			jm.endTransaction("DH_lifecycle_0299_sometimes_I_fail", Outcome.FAIL);
		}
		
		_navigation.countItemsLink().click();
		CountPoliciesPage countPoliciesPage = new CountPoliciesPage(driver); 
		countPoliciesPage.lifecycle().clear();
		countPoliciesPage.useability().selectByVisibleText(DslConstants.UNUSED).thenSleep();   // ** note 3
		
		CountPoliciesActionPage countPoliciesActionPage = new CountPoliciesActionPage(driver);	

		jm.startTransaction("DH_lifecycle_0300_countUnusedPolicies");
		countPoliciesPage.submit().submit().waitUntilClickable(countPoliciesActionPage.backLink());
		waitForSqlResultsTextOnActionPageAndCheckOk(countPoliciesActionPage);
		jm.endTransaction("DH_lifecycle_0300_countUnusedPolicies");
		
		long countPolicies = Long.parseLong(countPoliciesActionPage.rowsAffected().getText());
		LOG.debug( "countPolicies : " + countPolicies); 
		jm.userDataPoint(application + "_Total_Unused_Policy_Count", countPolicies);

//	 	count breakdown - count for unused DATAHUNTER_PV_TEST policies  (by lifecycle) 	
		_navigation.itemsBreakdownLink().click();		
		CountPoliciesBreakdownPage countPoliciesBreakdownPage = new CountPoliciesBreakdownPage(driver);
		countPoliciesBreakdownPage.applicationStartsWithOrEquals().selectByVisibleText(DslConstants.EQUALS);
		countPoliciesBreakdownPage.useability().selectByVisibleText(DslConstants.UNUSED);
		
		CountPoliciesBreakdownActionPage countPoliciesBreakdownActionPage = new CountPoliciesBreakdownActionPage(driver);	

		jm.startTransaction("DH_lifecycle_0400_countUnusedPoliciesCurrentThread");		
		countPoliciesBreakdownPage.submit().submit();
		waitForSqlResultsTextOnActionPageAndCheckOk(countPoliciesBreakdownActionPage);		
		jm.endTransaction("DH_lifecycle_0400_countUnusedPoliciesCurrentThread");				
		
		// direct access to required row-column table element by computing the id:
		int countUsedPoliciesCurrentThread = countPoliciesBreakdownActionPage.getCountForBreakdown(application, lifecycle, DslConstants.UNUSED); 
		LOG.debug( "countUsedPoliciesCurrentThread : " + countUsedPoliciesCurrentThread); 
		jm.userDataPoint(application + "_This_Thread_Unused_Policy_Count", countUsedPoliciesCurrentThread);	

//		use next policy
		_navigation.useNextItemLink().click();	
		NextPolicyPage nextPolicyPage = new NextPolicyPage(driver); 
		nextPolicyPage.lifecycle().type(lifecycle);
		nextPolicyPage.useability().selectByVisibleText(DslConstants.UNUSED);
		nextPolicyPage.selectOrder().selectByVisibleText(DslConstants.SELECT_MOST_RECENTLY_ADDED);

		NextPolicyActionPage nextPolicyActionPage = new NextPolicyActionPage(driver);		
		
		jm.startTransaction("DH_lifecycle_0500_useNextPolicy");		
		nextPolicyPage.submit().submit();
		waitForSqlResultsTextOnActionPageAndCheckOk(nextPolicyActionPage);			
		jm.endTransaction("DH_lifecycle_0500_useNextPolicy");	
		
		if (LOG.isDebugEnabled() ) {LOG.debug("useNextPolicy: " + application + "-" + lifecycle + " : " + nextPolicyActionPage.identifier() );	}
		
		//HTML table demo (force application as only parameter).
		driver.get(dataHunterUrl + DslConstants.SELECT_MULTIPLE_POLICIES_URL_PATH  + "?application=" + application);
		MultiplePoliciesPage printSelectedPoliciesPage = new MultiplePoliciesPage(driver);
		printSelectedPoliciesPage.submit().waitUntilClickable();

		MultiplePoliciesActionPage printSelectedPoliciesActionPage = new MultiplePoliciesActionPage(driver);
		
		jm.startTransaction("DH_lifecycle_0600_displaySelectedPolicies");	
		printSelectedPoliciesPage.submit().submit();
		waitForSqlResultsTextOnActionPageAndCheckOk(printSelectedPoliciesActionPage);
		// demo how to extract a transaction time from with a running script 
		SampleResult sr_0600 = jm.endTransaction("DH_lifecycle_0600_displaySelectedPolicies");
		
		LOG.debug("Transaction " + sr_0600.getSampleLabel() + " ran at " + sr_0600.getTimeStamp() + " and took " + sr_0600.getTime() + " ms." );
		
		long used=0;
		long unused=0;
		HtmlTable printSelectedPoliciesTable = printSelectedPoliciesActionPage.printSelectedPoliciesTable();
		for (HtmlTableRow tableRow : printSelectedPoliciesTable.getHtmlTableRows()) {
			if (tableRow.getColumnNumberOfExpectedColumns(6, 10).getText().equals("USED"))   used++;
			if (tableRow.getColumnNumberOfExpectedColumns(6, 10).getText().equals("UNUSED")) unused++;
		}	
		jm.userDataPoint("USED_count_html_demo",   used );				
		jm.userDataPoint("UNUSED_count_html_demo", unused );	
		LOG.debug("HTML demo: USED=" + used + ", UNUSED=" + unused); 
		
// 		delete multiple policies (test cleanup - a duplicate of the initial delete policies transactions)
		
		jm.startTransaction("DH_lifecycle_0099_gotoDeleteMultiplePoliciesUrl");	
		driver.get(dataHunterUrl + DslConstants.SELECT_MULTIPLE_POLICIES_URL_PATH + "?application=" + application);		
		multiplePoliciesPage.lifecycle().waitUntilClickable();
		multiplePoliciesPage.lifecycle().type(lifecycle);
		multiplePoliciesPage.submit().submit().waitUntilClickable( multiplePoliciesActionPage.backLink() );				
		jm.endTransaction("DH_lifecycle_0099_gotoDeleteMultiplePoliciesUrl");	
		
		jm.startTransaction("DH_lifecycle_0100_deleteMultiplePolicies");		
		multiplePoliciesActionPage.multipleDeleteLink().click().waitUntilAlertisPresent().acceptAlert();
		waitForSqlResultsTextOnActionPageAndCheckOk(multiplePoliciesActionPage);
		jm.endTransaction("DH_lifecycle_0100_deleteMultiplePolicies");
	
//		jm.writeBufferedArtifacts();
	}
	
	
	/**
	 *  Just as a demo, create some transaction (in a real test in a real test you may want go to a home page/logout page etc).
	 *  Will be triggered when an exception is thrown during script . 	
	 */
	@Override
	protected void userActionsOnScriptFailure(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm,	WebDriver driver) {
		jm.startTransaction("DH_lifecycle_9998_userActionsOnScriptFailure");
		System.out.println("  -- page title at userActionsOnScriptFailure is " + driver.getTitle() + " --");
		jm.endTransaction("DH_lifecycle_9998_userActionsOnScriptFailure");
	}
	
	
	/**
	 *	Alternative code for the addListenerResponseReceived method below, showing how to split out a long lambda into a separate 
	 *  function, arguably more readable (forces explicit import statements for ..devtools.vxxx ... classes) 
	 *  
	 *	//  import java.util.function.BiFunction;	
	 *	//  import org.openqa.selenium.devtools.v102.network.model.ResponseReceived;  // 'v102' will change over devtools releases)
	 *  //  import org.openqa.selenium.devtools.v102.network.model.RequestWillBeSent;
	 *	
	 *	BiFunction<RequestWillBeSent, ResponseReceived, String> computeTxnId = (req, res) -> {
	 *		String urlAction = StringUtils.substringBeforeLast(StringUtils.substringAfter(res.getResponse().getUrl(), "dataHunter/"), "?");
	 *		System.out.println("  computeTxnleapyr urlAction  = " + urlAction);
	 *		String[] splitCurrTxn = StringUtils.split(jm.getMostRecentTransactionStarted(), "-", 4);
	 *		return splitCurrTxn[0] + "-" + splitCurrTxn[1] + "-" + splitCurrTxn[2] + "__net_" + urlAction;
	 *	};
	 *	
	 *	devToolsDsl.addListenerResponseReceived(jm
	 *			, res -> "Document".equalsIgnoreCase(res.getType().toJson()) && jm.getMostRecentTransactionStarted() != null
	 *			, computeTxnId);
	 *  
	 * @param jm JmeterFunctionsForSeleniumScripts
	 * @param driver WebDriver
	 */
	private void startCdpListeners(JmeterFunctionsForSeleniumScripts jm, WebDriver driver) {
		DevToolsDSL devToolsDsl = new DevToolsDSL(); 
		devToolsDsl.createDevToolsSession(driver);
		
		devToolsDsl.addListenerRequestWillBeSent(jm
				, req -> req.getDocumentURL().contains("_action") || StringUtils.contains(jm.getMostRecentTransactionStarted(), "loadInitialPage"));

		devToolsDsl.addListenerResponseReceived(jm
				, res -> "Document".equalsIgnoreCase(res.getType().toJson()) && jm.getMostRecentTransactionStarted() != null
				, (req,res) -> {
					String urlAction = StringUtils.substringBeforeLast(StringUtils.substringAfter(res.getResponse().getUrl(), "mark59-datahunter/"), "?");
					String[] splitCurrTxn = StringUtils.split(jm.getMostRecentTransactionStarted(), "_", 4);
					return splitCurrTxn[0] + "_" + splitCurrTxn[1] + "_" + splitCurrTxn[2] + "__net_" + urlAction;
				});
		
		// this listener can be used to interrogate the loaded response body - would not be expected to be in general use for a performance test.    
		// devToolsDsl.addListenerLoadingFinished(jm, loadingFinished -> true);
	}


	/*
	 * At first glance this may seem not to have any 'wait for element' conditions.  However the 'getText()'
	 * method (indirectly) invokes a Fluent Wait (actually, a custom 'variable' FlentWait is used - see note 4 above). 
	 */
	private void waitForSqlResultsTextOnActionPageAndCheckOk(_GenericDataHunterActionPage _genericDatatHunterActionPage) {
		String sqlResultText = _genericDatatHunterActionPage.sqlResult().getText();											// ** note 4	
		if (sqlResultText==null || !sqlResultText.contains("PASS")) {
			throw new RuntimeException("SQL issue (" + sqlResultText + ") : " +
						_genericDatatHunterActionPage.formatResultsMessage(_genericDatatHunterActionPage.getClass().getName()));
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private static synchronized void PrintSomeMsgOnceAtStartUp(String dataHunterUrl, WebDriver driver) {
		Properties sysprops = System.getProperties();
		if (!"true".equals(sysprops.get("printedOnce")) ) {	
			LOG.info(" Using DataHunter Url     : " + dataHunterUrl);
			Capabilities caps = ((RemoteWebDriver)driver).getCapabilities();
			LOG.info(" Browser Name and Version : " + caps.getBrowserName() + " " + caps.getBrowserVersion());
			if ("chrome".equalsIgnoreCase(caps.getBrowserName()) && caps.getCapability("chrome") != null ){
				LOG.info(" Chrome Driver Version    : " +  ((Map<String, String>)caps.getCapability("chrome")).get("chromedriverVersion"));
			}
			sysprops.put("printedOnce", "true");
		}
	}

	
	/**
	 * A main method to assist with script testing outside JMeter.  The samples below demonstrate three ways of running the script: <br><br>
	 * 1.  Run a simple single instance, without extra thread-based parameterization (KeepBrowserOpen enumeration is optionally available).<br>
	 * 2.  Run multiple instances of the script, without extra thread-based parameterization <br> 
	 * 3.  Run multiple instances of the script, with extra thread-based parameterization, represented as a map with parameter name as key,
	 *     and values for each instance to be executed<br>  
	 * 4.  As for 3, but allows for the threads to iterate, and optionally to print a summary and/or output a CSV file in JMeter format. 
	 *     See method {@link #runMultiThreadedSeleniumTest(int, int, Map, KeepBrowserOpen, int, int, boolean, File)} JavaDocs for more..
	 *     
	 * For logging details see @Log4jConfigurationHelper 
	 */
	public static void main(String[] args) {
		Log4jConfigurationHelper.init(Level.INFO ) ;
		DataHunterLifecyclePvtScript thisTest = new DataHunterLifecyclePvtScript();

		//1: single
		thisTest.runUiTest(KeepBrowserOpen.ONFAILURE);
		
		
		//2: multi-thread  (a. with and b. without KeepBrowserOpen option) 
//		thisTest.runMultiThreadedUiTest(2, 500);
//		thisTest.runMultiThreadedUiTest(2, 2000, KeepBrowserOpen.ONFAILURE);   
  

		//3: multi-thread with parms
//		Map<String, java.util.List<String>>threadParameters = new java.util.LinkedHashMap<String,java.util.List<String>>();
//		threadParameters.put("USER",                              java.util.Arrays.asList( "USER-MATTHEW", "USER-MARK", "USER-LUKE", "USER-JOHN"));
//		threadParameters.put(SeleniumDriverFactory.HEADLESS_MODE, java.util.Arrays.asList( "true"        , "false"    , "true"     , "false"));	
		//  (a. with and b. without KeepBrowserOpen option)
//		thisTest.runMultiThreadedUiTest(4, 2000, threadParameters);
//		thisTest.runMultiThreadedUiTest(4, 2000, threadParameters, KeepBrowserOpen.ONFAILURE);	
		
		
		//4: multi-thread with parms, each thread iterating, optional summary printout and/or CSV file in JMeter format. See JavaDocs for details. 
//		Map<String, java.util.List<String>>threadParameters = new java.util.LinkedHashMap<String,java.util.List<String>>();
//		threadParameters.put("USER",                              java.util.Arrays.asList( "USER-MATTHEW", "USER-MARK", "USER-LUKE", "USER-JOHN"));
//		threadParameters.put(SeleniumDriverFactory.HEADLESS_MODE, java.util.Arrays.asList( "true"        , "false"    , "true"     , "false"));	
//		thisTest.runMultiThreadedUiTest(4, 2000, threadParameters, KeepBrowserOpen.ONFAILURE, 3, 1500, true, new File("C:/Mark59_Runs/csvSample.csv"));
	}
	
}

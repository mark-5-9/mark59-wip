package com.mark59.trends.load.run;


import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.trends.data.beans.Run;
import com.mark59.trends.data.beans.Transaction;
import com.mark59.trends.load.TrendsLoad;
import com.mark59.trends.metricSla.MetricSlaResult;
import com.mark59.trends.sla.SlaTransactionResult;

import junit.framework.TestCase;


public class TrendsLoadJmeterTest extends TestCase {

	EmbeddedDatabase db; 
	ApplicationContext context;

	public void setUp() {
		db = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).setName("trendsmem;MODE=MySQL;").addScript("copyofschema.sql").build();
	}

	
	@Test
	public void testTrendsLoadJMeterGeneralTest() {
		TrendsLoad.parseArguments(new String[] { "-a", "DataHunter", "-i", "./src/test/resources/JmeterResultsDataHunterGeneral", "-d", Mark59Constants.H2MEM, "-s","trendsmem",
				"-e","The operation lasted too long:|Test failed: text expected to contain|The result was the wrong size" });
		SpringApplication springApplication = new SpringApplication(TrendsLoad.class);
		springApplication.setWebApplicationType(WebApplicationType.NONE);
		springApplication.setBannerMode(Banner.Mode.OFF);	
		context = springApplication.run();
		
		TrendsLoad trendsLoad = (TrendsLoad) context.getBean("trendsLoad");		
		List<MetricSlaResult> metricSlaResults = trendsLoad.getMetricSlaResults();
		assertEquals(1, metricSlaResults.size() );
		assertEquals("Metric SLA Failed Warning  : metric out of expected range for CPU_UTIL Average on localhost.  Range is set as 5.0 to 60.0, actual was 65.25"
				, metricSlaResults.get(0).getMessageText()  );
		
		List<SlaTransactionResult> slaTransactionResults = trendsLoad.getSlaTransactionResults();
		assertEquals(2, slaTransactionResults.size() );
		for (SlaTransactionResult slaTransactionResult : slaTransactionResults){ 
			// System.out.println("slaRes>>" + slaTransactionResult);
			if ("DH_lifecycle_0200_addPolicy".equals(slaTransactionResult.getTxnId())){
				assertEquals ("txnId : DH_lifecycle_0200_addPolicy, passedAllSlas=false, foundSLAforTxnId=true, passed90thResponse=true, txn90thResponse=0.194, sla90thResponse=0.400,"
						+ " passed95thResponse=true, txn95thResponse=0.217, sla95thResponse=-1.000, passed99thResponse=true, txn99thResponse=0.350, sla99thResponse=-1.000, passedFailPercent=true,"
						+ " txnFailurePercent=1.099, slaFailurePercent=2.000, passedPassCount=false, txnPassCount=90, slaPassCount=46, slaPassCountVariancePercent=20.000"
						, slaTransactionResult.toString());				
			} else if ("DH_lifecycle_9999_finalize_deleteMultiplePolicies".equals(slaTransactionResult.getTxnId())){
				assertEquals("txnId : DH_lifecycle_9999_finalize_deleteMultiplePolicies, passedAllSlas=false, foundSLAforTxnId=true, passed90thResponse=true, txn90thResponse=55.117, sla90thResponse=-1.000,"
						+ " passed95thResponse=true, txn95thResponse=55.117, sla95thResponse=-1.000, passed99thResponse=true, txn99thResponse=55.117, sla99thResponse=-1.000,"
						+ " passedFailPercent=false, txnFailurePercent=20.000, slaFailurePercent=2.000, passedPassCount=true, txnPassCount=4, slaPassCount=4, slaPassCountVariancePercent=50.000"						
						, slaTransactionResult.toString());
			} else {
				fail("Unexpected slaTransactionResult: " + slaTransactionResult.getTxnId() );
			}
		}
			
		PerformanceTest performanceTest = trendsLoad.getPerformanceTest();
	
		Run run = performanceTest.getRunSummary();
		assertEquals("DataHunter", run.getApplication());
		assertEquals("1589526041321", StringUtils.substringBetween(run.getPeriod(), "[", ":" ).trim());
		assertEquals("1589526121968", StringUtils.substringBetween(run.getPeriod(), ":", "]" ).trim());

		String apprun = StringUtils.substringBefore(run.toString(), "isRunIgnored");

		List<Transaction> transactions = performanceTest.getTransactionSummariesThisRun();
		assertEquals(10, transactions.size() );
		for (Transaction transaction : transactions) {
			// System.out.println("Txn>>" + transaction);
			if ("DH_lifecycle_0001_gotoDeleteMultiplePoliciesUrl".equals(transaction.getTxnId())){
				assertEquals (apprun +"txnId=DH_lifecycle_0001_gotoDeleteMultiplePoliciesUrl, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=0.016, txnAverage=0.493, txnMedian=0.026,"
						+ " txnMaximum=2.115, txn90th=1.175, txn95th=2.030, txn99th=2.115, txnPass=28, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH_lifecycle_0100_deleteMultiplePolicies".equals(transaction.getTxnId())){
				assertEquals (apprun +"txnId=DH_lifecycle_0100_deleteMultiplePolicies, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=0.117, txnAverage=0.181, txnMedian=0.148,"
						+ " txnMaximum=0.488, txn90th=0.235, txn95th=0.359, txn99th=0.488, txnPass=28, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH_lifecycle_0200_addPolicy".equals(transaction.getTxnId())){
				assertEquals (apprun +"txnId=DH_lifecycle_0200_addPolicy, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=0.111, txnAverage=0.156, txnMedian=0.147,"
						+ " txnMaximum=0.377, txn90th=0.194, txn95th=0.217, txn99th=0.350, txnPass=90, txnFail=1, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.200", transaction.toString());
			} else if ("DH_lifecycle_0299_sometimes_I_fail".equals(transaction.getTxnId())){
				assertEquals (apprun +"txnId=DH_lifecycle_0299_sometimes_I_fail, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=0.000, txnAverage=0.000, txnMedian=0.000,"
						+ " txnMaximum=0.001, txn90th=0.000, txn95th=0.000, txn99th=0.001, txnPass=18, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH_lifecycle_0300_countUnusedPolicies".equals(transaction.getTxnId())){
				assertEquals (apprun +"txnId=DH_lifecycle_0300_countUnusedPolicies, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=0.117, txnAverage=0.170, txnMedian=0.146,"
						+ " txnMaximum=0.462, txn90th=0.189, txn95th=0.208, txn99th=0.462, txnPass=18, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH_lifecycle_0400_countUnusedPoliciesCurrentThread".equals(transaction.getTxnId())){
				assertEquals (apprun +"txnId=DH_lifecycle_0400_countUnusedPoliciesCurrentThread, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=0.118, txnAverage=0.162, txnMedian=0.142,"
						+ " txnMaximum=0.440, txn90th=0.175, txn95th=0.175, txn99th=0.440, txnPass=18, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH_lifecycle_0500_useNextPolicy".equals(transaction.getTxnId())){
				assertEquals (apprun +"txnId=DH_lifecycle_0500_useNextPolicy, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=0.121, txnAverage=0.155, txnMedian=0.134,"
						+ " txnMaximum=0.326, txn90th=0.177, txn95th=0.254, txn99th=0.326, txnPass=18, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH_lifecycle_0600_displaySelectedPolicies".equals(transaction.getTxnId())){
				assertEquals (apprun +"txnId=DH_lifecycle_0600_displaySelectedPolicies, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=0.121, txnAverage=0.178, txnMedian=0.144,"
						+ " txnMaximum=0.365, txn90th=0.155, txn95th=0.365, txn99th=0.365, txnPass=6, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH_lifecycle_9999_finalize_deleteMultiplePolicies".equals(transaction.getTxnId())){
				assertEquals (apprun +"txnId=DH_lifecycle_9999_finalize_deleteMultiplePolicies, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=22.114, txnAverage=35.869, txnMedian=22.125,"
						+ " txnMaximum=55.117, txn90th=55.117, txn95th=55.117, txn99th=55.117, txnPass=4, txnFail=1, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("verylongtransactionnamesover128charsneedtobetruncatedandelipsesthingsputattheendPrettyDumbButcatchesmoreobsuresituationwhneau...".equals(transaction.getTxnId())){
				assertEquals (apprun +"txnId=verylongtransactionnamesover128charsneedtobetruncatedandelipsesthingsputattheendPrettyDumbButcatchesmoreobsuresituationwhneau...,"
						+ " txnType=TRANSACTION, isCdpTxn=N, txnMinimum=0.123, txnAverage=0.123, txnMedian=0.123,"
						+ " txnMaximum=0.123, txn90th=0.123, txn95th=0.123, txn99th=0.123, txnPass=1, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else {
				fail("unexpectedTransaction: " + transaction.getTxnId() );
			}
		}

		List<Transaction> metricTxns = performanceTest.getMetricTransactionSummariesThisRun();
		assertEquals(7, metricTxns.size() );
		for (Transaction transaction : metricTxns) {
			// System.out.println("metricTxns>>" + transaction);
			if ("localhost".equals(transaction.getTxnId())){
				assertEquals (apprun +"txnId=localhost, txnType=CPU_UTIL, isCdpTxn=N, txnMinimum=60.000, txnAverage=65.250, txnMedian=-1.000,"
						+ " txnMaximum=69.000, txn90th=0.000, txn95th=-1.000, txn99th=-1.000, txnPass=4, txnFail=-1, txnStop=-1, txnFirst=64.000, txnLast=69.000, txnSum=261.000, txnDelay=0.000", transaction.toString());
			} else if ("This_Thread_Unused_Policy_Count".equals(transaction.getTxnId())){
				assertEquals (apprun +"txnId=This_Thread_Unused_Policy_Count, txnType=DATAPOINT, isCdpTxn=N, txnMinimum=5.000, txnAverage=5.000, txnMedian=-1.000,"
						+ " txnMaximum=5.000, txn90th=-1.000, txn95th=-1.000, txn99th=-1.000, txnPass=18, txnFail=-1, txnStop=-1, txnFirst=5.000, txnLast=5.000, txnSum=90.000, txnDelay=0.000", transaction.toString());
			} else if ("Total_Unused_Policy_Count".equals(transaction.getTxnId())){
				assertEquals (apprun +"txnId=Total_Unused_Policy_Count, txnType=DATAPOINT, isCdpTxn=N, txnMinimum=5.000, txnAverage=10.167, txnMedian=-1.000,"
						+ " txnMaximum=9.000, txn90th=-1.000, txn95th=-1.000, txn99th=-1.000, txnPass=18, txnFail=-1, txnStop=-1, txnFirst=8.000, txnLast=9.000, txnSum=183.000, txnDelay=0.000", transaction.toString());
			} else if ("UNUSED_count_html_demo".equals(transaction.getTxnId())){
				assertEquals (apprun +"txnId=UNUSED_count_html_demo, txnType=DATAPOINT, isCdpTxn=N, txnMinimum=4.000, txnAverage=9.000, txnMedian=-1.000,"
						+ " txnMaximum=9.000, txn90th=-1.000, txn95th=-1.000, txn99th=-1.000, txnPass=6, txnFail=-1, txnStop=-1, txnFirst=16.000, txnLast=4.000, txnSum=54.000, txnDelay=0.000", transaction.toString());
			} else if ("USED_count_html_demo".equals(transaction.getTxnId())){
				assertEquals (apprun +"txnId=USED_count_html_demo, txnType=DATAPOINT, isCdpTxn=N, txnMinimum=1.000, txnAverage=1.000, txnMedian=-1.000,"
						+ " txnMaximum=1.000, txn90th=-1.000, txn95th=-1.000, txn99th=-1.000, txnPass=6, txnFail=-1, txnStop=-1, txnFirst=1.000, txnLast=1.000, txnSum=6.000, txnDelay=0.000", transaction.toString());
			} else if ("Memory_localhost_FreePhysicalG".equals(transaction.getTxnId())){
				assertEquals (apprun +"txnId=Memory_localhost_FreePhysicalG, txnType=MEMORY, isCdpTxn=N, txnMinimum=17.000, txnAverage=17.000, txnMedian=-1.000,"
						+ " txnMaximum=17.000, txn90th=-1.000, txn95th=-1.000, txn99th=-1.000, txnPass=4, txnFail=-1, txnStop=-1, txnFirst=17.000, txnLast=17.000, txnSum=68.000, txnDelay=0.000", transaction.toString());
			} else if ("Memory_localhost_FreeVirtualG".equals(transaction.getTxnId())){
				assertEquals (apprun +"txnId=Memory_localhost_FreeVirtualG, txnType=MEMORY, isCdpTxn=N, txnMinimum=15.000, txnAverage=15.500, txnMedian=-1.000,"
						+ " txnMaximum=16.000, txn90th=-1.000, txn95th=-1.000, txn99th=-1.000, txnPass=4, txnFail=-1, txnStop=-1, txnFirst=16.000, txnLast=16.000, txnSum=62.000, txnDelay=0.000", transaction.toString());
			} else {
				fail("unexpectedTransaction: " + transaction.getTxnId() );
			}
		}
	}
	
}

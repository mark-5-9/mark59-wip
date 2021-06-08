package com.mark59.metricsruncheck.run;

import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.metrics.data.application.dao.ApplicationDAO;
import com.mark59.metrics.data.application.dao.ApplicationDAOjdbcTemplateImpl;
import com.mark59.metrics.data.beans.Transaction;
import com.mark59.metrics.data.eventMapping.dao.EventMappingDAO;
import com.mark59.metrics.data.eventMapping.dao.EventMappingDAOjdbcTemplateImpl;
import com.mark59.metrics.data.graphMapping.dao.GraphMappingDAO;
import com.mark59.metrics.data.graphMapping.dao.GraphMappingDAOjdbcTemplateImpl;
import com.mark59.metrics.data.metricSla.dao.MetricSlaDAO;
import com.mark59.metrics.data.metricSla.dao.MetricSlaDAOjdbcImpl;
import com.mark59.metrics.data.run.dao.RunDAO;
import com.mark59.metrics.data.run.dao.RunDAOjdbcTemplateImpl;
import com.mark59.metrics.data.sla.dao.SlaDAO;
import com.mark59.metrics.data.sla.dao.SlaDAOjdbcImpl;
import com.mark59.metrics.data.testTransactions.dao.TestTransactionsDAO;
import com.mark59.metrics.data.testTransactions.dao.TestTransactionsDAOjdbcTemplateImpl;
import com.mark59.metrics.data.transaction.dao.TransactionDAO;
import com.mark59.metrics.data.transaction.dao.TransactionDAOjdbcTemplateImpl;
import com.mark59.metrics.metricSla.MetricSlaResult;
import com.mark59.metrics.sla.SlaTransactionResult;
import com.mark59.metricsruncheck.Runcheck;

import junit.framework.TestCase;



public class RuncheckGatlingTest extends TestCase {

	@Autowired
	DataSource dataSource;
    @Autowired
    String currentDatabaseProfile;  
	@Autowired
	MetricSlaDAO metricSlaDAO;
	@Autowired
	TransactionDAO transactionDAO;
	@Autowired
	SlaDAO slaDAO;
	@Autowired
	RunDAO runDAO;
	@Autowired
	TestTransactionsDAO testTransactionsDAO;
	@Autowired
	EventMappingDAO eventMappingDAO;
	@Autowired
	ApplicationContext context;
	
	@SuppressWarnings("rawtypes")
	@Bean
	public DataSource dataSource() {
		DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
		dataSourceBuilder.driverClassName("org.h2.Driver");
		dataSourceBuilder.url("jdbc:h2:mem:metricsmem;MODE=MySQL");
		dataSourceBuilder.username("SA");
		dataSourceBuilder.password("");
		return dataSourceBuilder.build();
	};
		
    @Value("h2mem")
    private String springProfilesActive;	
	@Bean
    public String currentDatabaseProfile() {return "h2mem";   }   
	@Bean
	public ApplicationDAO applicationDAO() {return new ApplicationDAOjdbcTemplateImpl();}
	@Bean
	public RunDAO runDAO() {return new RunDAOjdbcTemplateImpl();}
	@Bean
	public TransactionDAO transactionDAO() {return new TransactionDAOjdbcTemplateImpl();}
	@Bean
	public SlaDAO slaDAO() {return new SlaDAOjdbcImpl();}
	@Bean
	public MetricSlaDAO metricSlaDAO() {return new MetricSlaDAOjdbcImpl();}
	@Bean
	public GraphMappingDAO graphMappingDAO() {	return new GraphMappingDAOjdbcTemplateImpl();}
	@Bean
	public EventMappingDAO eventMappingDAO() {	return new EventMappingDAOjdbcTemplateImpl();}
	@Bean
	public TestTransactionsDAO testTransactionsDAO() {return new TestTransactionsDAOjdbcTemplateImpl();	}

	EmbeddedDatabase db; 

	public void setUp() {
		db = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).setName("metricsmem;MODE=MySQL;").addScript("copyofschema.sql").build();
		// for multiple tests?:  DB_CLOSE_DELAY=-1;")
	}
	
	@Test
	public void testRuncheckJMeterGeneralTest() {
		Runcheck.parseArguments(new String[] { "-a", "DataHunter", "-i", "./src/test/resources/GatlingResults", "-l","simulation.logv341",
				"-d", Mark59Constants.H2MEM, "-s","metricsmem",	"-e","responseTimeInMillis|errormsgStartsWith2|errormsgStartsWith3",  "-t","GATLING"  });
		SpringApplication springApplication = new SpringApplication(Runcheck.class);
		springApplication.setWebApplicationType(WebApplicationType.NONE);
		springApplication.setBannerMode(Banner.Mode.OFF);	
		context = springApplication.run();
		
		Runcheck runcheck = (Runcheck) context.getBean("runcheck");	
		
		List<MetricSlaResult> metricSlaResults = runcheck.getMetricSlaResults();
		assertEquals(3, metricSlaResults.size() );
		for (MetricSlaResult metricSlaResult : metricSlaResults){
			// System.out.println("metricSlaRes>>" + metricSlaResult);
			if ("Total_Unused_Policy_Count".equals(metricSlaResult.getTxnId())){
				assertEquals ("txnId=Total_Unused_Policy_Count, metricTxnType=null, valueDerivation=Last, slaResultType=MISSING_SLA_TRANSACTION,"
						+ " messageText=Metric SLA Failed Warning  : no metric has been found but was expected for DATAPOINT Last on Total_Unused_Policy_Count"
						, metricSlaResult.toString());				
			} else if ("localhost".equals(metricSlaResult.getTxnId()) &&  "Average".equals(metricSlaResult.getValueDerivation())  ){
				assertEquals ("txnId=localhost, metricTxnType=null, valueDerivation=Average, slaResultType=MISSING_SLA_TRANSACTION,"
						+ " messageText=Metric SLA Failed Warning  : no metric has been found but was expected for CPU_UTIL Average on localhost"
						, metricSlaResult.toString());
			} else if ("localhost".equals(metricSlaResult.getTxnId()) &&  "PercentOver90".equals(metricSlaResult.getValueDerivation())  ){
				assertEquals ("txnId=localhost, metricTxnType=null, valueDerivation=PercentOver90, slaResultType=MISSING_SLA_TRANSACTION,"
						+ " messageText=Metric SLA Failed Warning  : no metric has been found but was expected for CPU_UTIL PercentOver90 on localhost"
						, metricSlaResult.toString());				
			} else {
				fail("Unexpected metric sla TransactionResult: " + metricSlaResult.getTxnId() );
			}
		}
		
		List<SlaTransactionResult> slaTransactionResults = runcheck.getSlaTransactionResults();
		assertEquals(4, slaTransactionResults.size() );
		for (SlaTransactionResult slaTransactionResult : slaTransactionResults){ 
			// System.out.println("slaRes>>" + slaTransactionResult);
			if ("DH-lifecycle-0100-deleteMultiplePolicies".equals(slaTransactionResult.getTxnId())){
				assertEquals ("txnId : DH-lifecycle-0100-deleteMultiplePolicies, passedAllSlas=false, foundSLAforTxnId=true, passed90thResponse=true, txn90thResponse=0.034, sla90thResponse=0.400,"
						+ " passed95thResponse=true, txn95thResponse=0.034, sla95thResponse=-1.000, passed99thResponse=true, txn99thResponse=0.034, sla99thResponse=-1.000,"
						+ " passedFailPercent=true, txnFailurePercent=0.000, slaFailurePercent=2.000, passedPassCount=false, txnPassCount=2, slaPassCount=30, slaPassCountVariancePercent=20.000"
						, slaTransactionResult.toString());				
			} else if ("DH-lifecycle-0299-sometimes-I-fail".equals(slaTransactionResult.getTxnId())){
				assertEquals ("txnId : DH-lifecycle-0299-sometimes-I-fail, passedAllSlas=false, foundSLAforTxnId=true, passed90thResponse=false, txn90thResponse=1.134, sla90thResponse=0.100,"
						+ " passed95thResponse=true, txn95thResponse=1.134, sla95thResponse=-1.000, passed99thResponse=true, txn99thResponse=1.134, sla99thResponse=-1.000,"
						+ " passedFailPercent=true, txnFailurePercent=0.000, slaFailurePercent=-1.000, passedPassCount=true, txnPassCount=16, slaPassCount=20, slaPassCountVariancePercent=40.000"
						, slaTransactionResult.toString());
			} else if ("DH-lifecycle-0300-countUnusedPolicies".equals(slaTransactionResult.getTxnId())){
				assertEquals ("txnId : DH-lifecycle-0300-countUnusedPolicies, passedAllSlas=false, foundSLAforTxnId=true, passed90thResponse=true, txn90thResponse=0.387, sla90thResponse=0.400,"
						+ " passed95thResponse=true, txn95thResponse=0.387, sla95thResponse=-1.000, passed99thResponse=true, txn99thResponse=0.387, sla99thResponse=-1.000,"
						+ " passedFailPercent=false, txnFailurePercent=40.000, slaFailurePercent=2.000, passedPassCount=false, txnPassCount=3, slaPassCount=20, slaPassCountVariancePercent=20.000"
						, slaTransactionResult.toString());
			} else if ("DH-lifecycle-0500-useNextPolicy".equals(slaTransactionResult.getTxnId())){
				assertEquals ("txnId : DH-lifecycle-0500-useNextPolicy, passedAllSlas=false, foundSLAforTxnId=true, passed90thResponse=false, txn90thResponse=0.781, sla90thResponse=0.400,"
						+ " passed95thResponse=true, txn95thResponse=1.167, sla95thResponse=-1.000, passed99thResponse=true, txn99thResponse=1.169, sla99thResponse=-1.000,"
						+ " passedFailPercent=true, txnFailurePercent=0.000, slaFailurePercent=2.000, passedPassCount=true, txnPassCount=19, slaPassCount=20, slaPassCountVariancePercent=20.000"
						, slaTransactionResult.toString());
			} else {
				fail("Unexpected slaTransactionResult: " + slaTransactionResult.getTxnId() );
			}
		}
		
		PerformanceTest performanceTest = runcheck.getPerformanceTest();
		List<Transaction> transactions = performanceTest.getTransactionSummariesThisRun();
		assertEquals(7, transactions.size() );
		for (Transaction transaction : transactions) {
			// System.out.println("Txn>>" + transaction);
			if ("DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl".equals(transaction.getTxnId())){
				assertEquals ("application=DataHunter, runTime=202104081812, txnId=DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl, txnType=TRANSACTION, txnMinimum=0.307, txnAverage=0.340, txnMedian=0.307,"
						+ " txnMaximum=0.372, txn90th=0.372, txn95th=0.372, txn99th=0.372, txnPass=2, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH-lifecycle-0100-deleteMultiplePolicies".equals(transaction.getTxnId())){
				assertEquals ("application=DataHunter, runTime=202104081812, txnId=DH-lifecycle-0100-deleteMultiplePolicies, txnType=TRANSACTION, txnMinimum=0.032, txnAverage=0.033, txnMedian=0.032,"
						+ " txnMaximum=0.034, txn90th=0.034, txn95th=0.034, txn99th=0.034, txnPass=2, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.200", transaction.toString());
			} else if ("DH-lifecycle-0299-sometimes-I-fail".equals(transaction.getTxnId())){
				assertEquals ("application=DataHunter, runTime=202104081812, txnId=DH-lifecycle-0299-sometimes-I-fail, txnType=TRANSACTION, txnMinimum=1.134, txnAverage=1.134, txnMedian=1.134,"
						+ " txnMaximum=1.134, txn90th=1.134, txn95th=1.134, txn99th=1.134, txnPass=16, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH-lifecycle-0300-countUnusedPolicies".equals(transaction.getTxnId())){
				assertEquals ("application=DataHunter, runTime=202104081812, txnId=DH-lifecycle-0300-countUnusedPolicies, txnType=TRANSACTION, txnMinimum=0.025, txnAverage=0.264, txnMedian=0.381,"
						+ " txnMaximum=0.387, txn90th=0.387, txn95th=0.387, txn99th=0.387, txnPass=3, txnFail=2, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH-lifecycle-0500-useNextPolicy".equals(transaction.getTxnId())){
				assertEquals ("application=DataHunter, runTime=202104081812, txnId=DH-lifecycle-0500-useNextPolicy, txnType=TRANSACTION, txnMinimum=0.280, txnAverage=0.427, txnMedian=0.283,"
						+ " txnMaximum=1.169, txn90th=0.781, txn95th=1.167, txn99th=1.169, txnPass=19, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH-lifecycle-9999-finalize-deleteMultiplePolicies".equals(transaction.getTxnId())){
				assertEquals ("application=DataHunter, runTime=202104081812, txnId=DH-lifecycle-9999-finalize-deleteMultiplePolicies, txnType=TRANSACTION, txnMinimum=1.134, txnAverage=1.134, txnMedian=1.134,"
						+ " txnMaximum=1.134, txn90th=1.134, txn95th=1.134, txn99th=1.134, txnPass=4, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH-lifecycle-0400-countUnusedPoliciesCurrentThread".equals(transaction.getTxnId())){
				assertEquals ("application=DataHunter, runTime=202005151700, txnId=DH-lifecycle-0400-countUnusedPoliciesCurrentThread, txnType=TRANSACTION, txnMinimum=0.118, txnAverage=0.162, txnMedian=0.142,"
						+ " txnMaximum=0.440, txn90th=0.175, txn95th=0.175, txn99th=0.440, txnPass=18, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("load simulation file".equals(transaction.getTxnId())){
				assertEquals ("application=DataHunter, runTime=202104081812, txnId=load simulation file, txnType=TRANSACTION, txnMinimum=0.093, txnAverage=0.093, txnMedian=0.093, txnMaximum=0.093, txn90th=0.093,"
						+ " txn95th=0.093, txn99th=0.093, txnPass=1, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());				
			} else {
				fail("unexpectedTransaction: " + transaction.getTxnId() );
			}
		}
	}
	
}

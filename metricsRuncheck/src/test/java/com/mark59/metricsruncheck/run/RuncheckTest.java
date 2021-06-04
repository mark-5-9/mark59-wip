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



public class RuncheckTest extends TestCase {

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
		Runcheck.parseArguments(new String[] { "-a", "DataHunter", "-i", "./src/test/resources/JmeterResults", "-d", Mark59Constants.H2MEM, "-s","metricsmem" });
		SpringApplication springApplication = new SpringApplication(Runcheck.class);
		springApplication.setWebApplicationType(WebApplicationType.NONE);
		springApplication.setBannerMode(Banner.Mode.OFF);	
		context = springApplication.run();
		
		Runcheck runcheck = (Runcheck) context.getBean("runcheck");		
		List<MetricSlaResult> metricSlaResults = runcheck.getMetricSlaResults();
		assertEquals(1, metricSlaResults.size() );
		assertEquals("Metric SLA Failed Warning  : metric out of expected range for CPU_UTIL Average on localhost.  Range is set as 5.0 to 60.0, actual was 65.25"
				, metricSlaResults.get(0).getMessageText()  );
		
		List<SlaTransactionResult> slaTransactionResults = runcheck.getSlaTransactionResults();
		assertEquals(1, slaTransactionResults.size() );
		assertEquals ("txnId : DH-lifecycle-0200-addPolicy, passedAllSlas=false, foundSLAforTxnId=true, passed90thResponse=true, txn90thResponse=0.194, sla90thResponse=0.400,"
				+ " passed95thResponse=true, txn95thResponse=0.217, sla95thResponse=-1.000, passed99thResponse=true, txn99thResponse=0.350, sla99thResponse=-1.000,"
				+ " passedFailPercent=true, txnFailurePercent=0.0, slaFailurePercent=2.000, passedPassCount=false, txnPassCount=90, slaPassCount=46, slaPassCountVariancePercent=20.000"
				, slaTransactionResults.get(0).toString());
		
		PerformanceTest performanceTest = runcheck.getPerformanceTest();
		List<Transaction> transactions = performanceTest.getTransactionSummariesThisRun();
		assertEquals(9, transactions.size() );
		for (Transaction transaction : transactions) {
			if ("DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl".equals(transaction.getTxnId())){
				assertEquals ("application=DataHunter, runTime=202005151700, txnId=DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl, txnType=TRANSACTION, txnMinimum=0.016, txnAverage=0.493, txnMaximum=2.115,"
						+ " txn90th=1.175, txn95th=2.030, txn99th=2.115, txnPass=28, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH-lifecycle-0100-deleteMultiplePolicies".equals(transaction.getTxnId())){
				assertEquals ("application=DataHunter, runTime=202005151700, txnId=DH-lifecycle-0100-deleteMultiplePolicies, txnType=TRANSACTION, txnMinimum=0.117, txnAverage=0.181, txnMaximum=0.488,"
						+ " txn90th=0.235, txn95th=0.359, txn99th=0.488, txnPass=28, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.200", transaction.toString());
			} else if ("DH-lifecycle-0200-addPolicy".equals(transaction.getTxnId())){
				assertEquals ("application=DataHunter, runTime=202005151700, txnId=DH-lifecycle-0200-addPolicy, txnType=TRANSACTION, txnMinimum=0.111, txnAverage=0.156, txnMaximum=0.377,"
						+ " txn90th=0.194, txn95th=0.217, txn99th=0.350, txnPass=90, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH-lifecycle-0299-sometimes-I-fail".equals(transaction.getTxnId())){
				assertEquals ("application=DataHunter, runTime=202005151700, txnId=DH-lifecycle-0299-sometimes-I-fail, txnType=TRANSACTION, txnMinimum=0.000, txnAverage=0.000, txnMaximum=0.001,"
						+ " txn90th=0.000, txn95th=0.000, txn99th=0.001, txnPass=18, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH-lifecycle-0300-countUnusedPolicies".equals(transaction.getTxnId())){
				assertEquals ("application=DataHunter, runTime=202005151700, txnId=DH-lifecycle-0300-countUnusedPolicies, txnType=TRANSACTION, txnMinimum=0.117, txnAverage=0.170, txnMaximum=0.462,"
						+ " txn90th=0.189, txn95th=0.208, txn99th=0.462, txnPass=18, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH-lifecycle-0400-countUnusedPoliciesCurrentThread".equals(transaction.getTxnId())){
				assertEquals ("application=DataHunter, runTime=202005151700, txnId=DH-lifecycle-0400-countUnusedPoliciesCurrentThread, txnType=TRANSACTION, txnMinimum=0.118, txnAverage=0.162, txnMaximum=0.440,"
						+ " txn90th=0.175, txn95th=0.175, txn99th=0.440, txnPass=18, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH-lifecycle-0500-useNextPolicy".equals(transaction.getTxnId())){
				assertEquals ("application=DataHunter, runTime=202005151700, txnId=DH-lifecycle-0500-useNextPolicy, txnType=TRANSACTION, txnMinimum=0.121, txnAverage=0.155, txnMaximum=0.326,"
						+ " txn90th=0.177, txn95th=0.254, txn99th=0.326, txnPass=18, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH-lifecycle-0600-displaySelectedPolicies".equals(transaction.getTxnId())){
				assertEquals ("application=DataHunter, runTime=202005151700, txnId=DH-lifecycle-0600-displaySelectedPolicies, txnType=TRANSACTION, txnMinimum=0.121, txnAverage=0.178, txnMaximum=0.365,"
						+ " txn90th=0.155, txn95th=0.365, txn99th=0.365, txnPass=6, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH-lifecycle-9999-finalize-deleteMultiplePolicies".equals(transaction.getTxnId())){
				assertEquals ("application=DataHunter, runTime=202005151700, txnId=DH-lifecycle-9999-finalize-deleteMultiplePolicies, txnType=TRANSACTION, txnMinimum=0.114, txnAverage=0.119, txnMaximum=0.125,"
						+ " txn90th=0.125, txn95th=0.125, txn99th=0.125, txnPass=4, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else {
				fail("unexpectedTransaction: " + transaction.getTxnId() );
			}
		}
	}
	
}

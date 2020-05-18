package com.mark59.metricsruncheck.run;

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

import com.mark59.metrics.data.application.dao.ApplicationDAO;
import com.mark59.metrics.data.application.dao.ApplicationDAOjdbcTemplateImpl;
import com.mark59.metrics.data.beans.DateRangeBean;
import com.mark59.metrics.data.beans.Run;
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
import com.mark59.metrics.services.SlaService;
import com.mark59.metrics.services.SlaServiceImpl;

import junit.framework.TestCase;



public class PerformanceTestTest extends TestCase {

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
	
	@Bean
	public DataSource dataSource() {
		return DataSourceBuilder.create().build()  ;
	};
		
    @Value("h2")
    private String springProfilesActive;	
	@Bean
    public String currentDatabaseProfile() {return "h2";   }   
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
	public SlaService slaService() {return new SlaServiceImpl();}
	@Bean
	public GraphMappingDAO graphMappingDAO() {	return new GraphMappingDAOjdbcTemplateImpl();}
	@Bean
	public EventMappingDAO eventMappingDAO() {	return new EventMappingDAOjdbcTemplateImpl();}
	@Bean
	public TestTransactionsDAO testTransactionsDAO() {return new TestTransactionsDAOjdbcTemplateImpl();	}
	
	
	PerformanceTest performanceTest;
	EmbeddedDatabase db; 
	
	public void setUp() {
		String applicationId = "testApplicationId";
		String runReferenceArg = "testRunReferenceArg";
		SpringApplication springApplication = new SpringApplication(PerformanceTestTest.class);
		springApplication.setWebApplicationType(WebApplicationType.NONE);
		springApplication.setBannerMode(Banner.Mode.OFF);
		context = springApplication.run();
		performanceTest = new PerformanceTest(context, applicationId, runReferenceArg);	
	}
	
	@Test
	public void testPerformanceTestRunSummaryTest() {
		Run run = performanceTest.getRunSummary();
		assertTrue("testApplicationId".equals(run.getApplication()));
		assertTrue("testRunReferenceArg".equals(run.getRunReference()));
		assertTrue("N".equals(run.getBaselineRun()));
	}
	
	@Test
	public void testPerformanceTestCalculateAndSetRunTimesUsingEpochStartAndEndTest() {
		DateRangeBean dateRangeBean= new DateRangeBean(1600000000000L, 1610000000000L);
		Run run = performanceTest.getRunSummary();
		run = performanceTest.calculateAndSetRunTimesUsingEpochStartAndEnd(run, dateRangeBean);
		assertTrue("testApplicationId".equals(run.getApplication()));
		assertTrue("testRunReferenceArg".equals(run.getRunReference()));
		assertTrue("N".equals(run.getBaselineRun()));
		assertTrue("166666".equals(run.getDuration()));
		assertTrue("202009132226".equals(run.getRunTime()));
	}
	
	
//	URL url = this.getClass().getResource("/test.wsdl");
//	File testWsdl = new File(url.getFile());	
	
	
	
}

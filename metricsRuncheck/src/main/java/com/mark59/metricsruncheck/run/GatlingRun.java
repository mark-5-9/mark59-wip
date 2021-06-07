/*
 *  Copyright 2019 Insurance Australia Group Limited
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

package com.mark59.metricsruncheck.run;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.metrics.application.AppConstantsMetrics;
import com.mark59.metrics.data.beans.DateRangeBean;
import com.mark59.metrics.data.beans.EventMapping;
import com.mark59.metrics.data.beans.Run;
import com.mark59.metrics.data.beans.TestTransaction;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
	
/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class GatlingRun extends PerformanceTest  {
	
	private static final String GATLING_VER_lATEST_FORMAT ="3.4-3.6";         
	private static final String GATLING_VER_3_3_FORMAT ="3.3";
	
	private static final String RUN = "RUN";
	private static final String REQUEST = "REQUEST";
	private static final String KO = "KO";
	
	private Map<String,String> optimizedTxnTypeLookup = new HashMap<String, String>();;
	private Map<String,EventMapping> txnIdToEventMappingLookup = new HashMap<String, EventMapping>();

	private int fieldPosTxnId;	
	private int fieldPosTimeStampStart;
	private int fieldPosTimeStampEnd;
	private int fieldPosSuccess;	
	private int fieldPosRequestErrorMsg;
	
	public GatlingRun(ApplicationContext context, String application, String inputdirectory, String runReference, String excludestart, String captureperiod, 
			String ignoredErrors, String simulationLog) {
		
		super(context,application, runReference);
		
		//clean up before  
		testTransactionsDAO.deleteAllForRun(run);  // RUN_TIME_YET_TO_BE_CALCULATED
		
		loadTestTransactionDataFromGatlingSimulationLog(run.getApplication(), inputdirectory, ignoredErrors, simulationLog);
		
		DateRangeBean dateRangeBean = getRunDateRangeUsingTestTransactionalData(run.getApplication());
		run = new Run( calculateAndSetRunTimesUsingEpochStartAndEnd(run, dateRangeBean));
		runDAO.deleteRun(run.getApplication(), run.getRunTime());
		runDAO.insertRun(run);

		applyTimingRangeFilters(excludestart, captureperiod, dateRangeBean);
		transactionDAO.deleteAllForRun(run.getApplication(), run.getRunTime());	
		
		storeTransactionSummaries(run);
	}


	private void loadTestTransactionDataFromGatlingSimulationLog(String application, String inputdirectory, String ignoredErrors, String simulationLog) {
		int sampleCount = 0;
		
		try {
			File simulationLogFile = new File(inputdirectory + "/" + simulationLog);
			sampleCount = loadTestTransactionDataFromGatlingSimulationLogFile(simulationLogFile, application, ignoredErrors);
		} catch (IOException e) {
			System.out.println( "Error : problem with processing Gatling simulation log file  " + inputdirectory + "/" + simulationLog );
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			throw new RuntimeException(e.getMessage());
		}
	    System.out.println("____________________________________" );
	    System.out.println(sampleCount + " Total samples written" );
	    System.out.println(" " );	    
	}

	
	/**
	 * A validly name named Gatling simulation log file is expected to be passed, now need determine its version and extract results 
	 */
	private int loadTestTransactionDataFromGatlingSimulationLogFile(File simulationLogFile, String application, String ignoredErrors) throws IOException {

		List<TestTransaction> testTransactionList = new ArrayList<TestTransaction>();
		long startLoadms = System.currentTimeMillis(); 
		System.out.println("\n\nProcessing Gatling Simulation Log File " + simulationLogFile.getName() + " at " + new Date(startLoadms));					
		int lineCount = 0; 
		int samplesCreated=0;
		
		CSVParser csvParser = new CSVParserBuilder().withIgnoreLeadingWhiteSpace(true).withIgnoreQuotations(true).withSeparator('\t').build();
		CSVReader csvReader = new CSVReaderBuilder(new FileReader(simulationLogFile)).withCSVParser(csvParser).build();
		
		String[] csvDataLineFields = csvReadNextLine(csvReader, simulationLogFile);
		
		if (csvDataLineFields == null) {
			System.out.println("   Warning : " + simulationLogFile.getName() + " is empty!" );
			return 0;
		} 
		
		String gatlingVersion = GATLING_VER_lATEST_FORMAT;
		boolean stillLookingForRUN = true; 

		while ( csvDataLineFields != null && stillLookingForRUN){
			if (RUN.equals(csvDataLineFields[0])){
				gatlingVersion = csvDataLineFields[5];
				System.out.println("  Gatling ver: " + gatlingVersion);
				if (gatlingVersion == null) {
					System.out.println("\n  Info :  The version of Gatling being used could not be determined ! ");
					System.out.println("\n  Proceeding on assuption the format is compatable with Gatling version " + GATLING_VER_lATEST_FORMAT  + "\n" );
				} else if (gatlingVersion.startsWith("3.3")) {
					gatlingVersion = GATLING_VER_3_3_FORMAT;	
				} else if ( ! (gatlingVersion.startsWith("3.4") || gatlingVersion.startsWith("3.5")  || gatlingVersion.startsWith("3.6"))) {
					System.out.println("\n  Info :  The version of Gatling being used (" + gatlingVersion + ") has not been catered for ! ");
					System.out.println("\n  Proceeding on assuption the format is compatable with Gatling version " + GATLING_VER_lATEST_FORMAT  + "\n" );					
				}
				stillLookingForRUN = false;
			}
			lineCount++;
			csvDataLineFields = csvReadNextLine(csvReader, simulationLogFile);
		}
		
		List<String> ignoredErrorsList = Mark59Utils.pipeDelimStringToStringList(ignoredErrors);
		
		while ( csvDataLineFields != null ) {
		
			if (REQUEST.equals(csvDataLineFields[0])) {
    			addSampleToTestTransactionList(testTransactionList, csvDataLineFields, application, gatlingVersion, ignoredErrorsList);
				samplesCreated++;
			}

			lineCountProgressDisplay(lineCount);
			lineCount++;
			csvDataLineFields = csvReadNextLine(csvReader, simulationLogFile);
			
			if ( (samplesCreated % 100 ) == 0 ){
				testTransactionsDAO.insertMultiple(testTransactionList);
				testTransactionList.clear();
			}
			
		} // end for loop
		
		testTransactionsDAO.insertMultiple(testTransactionList);
		testTransactionList.clear();	    
		
		long endLoadms = System.currentTimeMillis(); 	    
		System.out.println("\n   " + simulationLogFile.getName() + "  file uploaded at " +  new Date(endLoadms) + " :" );
		System.out.println("        " + lineCount + " file lines processed" );
		System.out.println("        " + samplesCreated + " transaction samples created" );
		System.out.println("        took " +  (endLoadms - startLoadms)/1000 + " secs" );	    
		System.out.println();
		
		csvReader.close();
		return samplesCreated;
	}	


	private String[] csvReadNextLine( CSVReader csvReader, File inputCsvFileName) throws IOException {
		String[] csvDataLineFields = null;
		try {
			csvDataLineFields = csvReader.readNext();
		} catch (CsvValidationException e) {
			csvReader.close();
			System.out.println("Error :  Unexpected csv line format for file " + inputCsvFileName.getName() + 
					" Records count at time of failure : "  +  csvReader.getRecordsRead() + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		return csvDataLineFields;
	}

	
	private void lineCountProgressDisplay(int lineCount) {
		if ( (lineCount % 1000 )   == 0 ){	System.out.print("^");};
		if ( (lineCount % 100000 ) == 0 ){	System.out.println();};
	}
	
	
	private void addSampleToTestTransactionList(List<TestTransaction> testTransactionList, String[] csvDataLineFields, String application, String gatlingVersion, List<String> ignoredErrorsList){
		TestTransaction testTransaction = extractTransactionFromGatlingLine(csvDataLineFields, gatlingVersion, ignoredErrorsList);
		testTransaction.setApplication(application);
		testTransaction.setRunTime(AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED);
		testTransactionList.add(testTransaction);		
	}

	
	private TestTransaction extractTransactionFromGatlingLine(String[] csvDataLineFields, String gatlingVersion, List<String> ignoredErrorsList) {

		if (GATLING_VER_lATEST_FORMAT.equals(gatlingVersion)){
			fieldPosTxnId = 1;	
			fieldPosTimeStampStart = 2;
			fieldPosTimeStampEnd = 3;
			fieldPosSuccess = 4;	
			fieldPosRequestErrorMsg = 5;
		} else { // 3.3
			fieldPosTxnId = 2;	
			fieldPosTimeStampStart = 3;
			fieldPosTimeStampEnd = 4;
			fieldPosSuccess = 5;	
			fieldPosRequestErrorMsg = 6;
		}
		
		TestTransaction testTransaction = new TestTransaction();
		testTransaction.setTxnId(csvDataLineFields[fieldPosTxnId]);
		testTransaction.setTxnType(eventMappingTxnTypeTransform(testTransaction.getTxnId(), AppConstantsMetrics.JMETER, Mark59Constants.DatabaseTxnTypes.TRANSACTION.name()));

		BigDecimal elapsedTime = new BigDecimal(Long.parseLong(csvDataLineFields[fieldPosTimeStampEnd]) - Long.parseLong(csvDataLineFields[fieldPosTimeStampStart])); 	
		testTransaction.setTxnResult(elapsedTime.divide(AppConstantsMetrics.THOUSAND, 3, RoundingMode.HALF_UP));
	
		testTransaction.setTxnPassed("Y");
		if (KO.equalsIgnoreCase(csvDataLineFields[fieldPosSuccess]) && !errorToBeIgnored(csvDataLineFields[fieldPosRequestErrorMsg], ignoredErrorsList)){
			testTransaction.setTxnPassed("N");
		}
		testTransaction.setTxnEpochTime(csvDataLineFields[fieldPosTimeStampStart]);
		return testTransaction;
	}

	
	/**
	 * If an event mapping is found for the given transaction / tool / Database Data type (relating to a a sample line), 
	 * then the Database Data type for that mapping is returned<br>
	 * If a event mapping for the sample line is not found, then it is taken to be a TRANSACTION<br>
	 * TODO: PERFMON<br>
	 * TODO: also allow for transforms to TRANSACTION in event mapping<br>
	 * @param txnId
	 * @param performanceTool
	 * @param sampleLineDbDataType -will be a string value of enum Mark59Constants.DatabaseDatatypes (DATAPOINT, CPU_UTIL, MEMORY, TRANSACTION)
	 * @return txnType -  will be a string value of enum Mark59Constants.DatabaseDatatypes (DATAPOINT, CPU_UTIL, MEMORY, TRANSACTION)
	 */
	private String eventMappingTxnTypeTransform(String txnId, String performanceTool, String sampleLineRawDbDataType) {
		
		String eventMappingTxnType = null;
		String metricSource = performanceTool + "_" + sampleLineRawDbDataType;   // (eg 'Jmeter_CPU_UTIL',  'Jmeter_TRANSACTION' ..)
		
		String txnId_MetricSource_Key = txnId + "-" + metricSource; 
			
		if (optimizedTxnTypeLookup.get(txnId_MetricSource_Key) != null ){
			
			//As we could be processing large files, a Map of type by transaction ids (labels) is held for ids that have already had a lookup on the eventMapping table.  
			// Done to minimise sql calls - each different label / data type in the jmeter file just gets one lookup to see if it has a match on Event Mapping table.
			
			eventMappingTxnType = optimizedTxnTypeLookup.get(txnId_MetricSource_Key);
			
		} else {
			
			eventMappingTxnType = Mark59Constants.DatabaseTxnTypes.TRANSACTION.name();   
			
			EventMapping eventMapping = eventMappingDAO.findAnEventForTxnIdAndSource(txnId, metricSource);
			
			if ( eventMapping != null ) {
				// this not a standard TRANSACTION (it's one of the metric types) - store eventMapping for later use 
				eventMappingTxnType = eventMapping.getTxnType();
				txnIdToEventMappingLookup.put(txnId, eventMapping);
			}
			optimizedTxnTypeLookup.put(txnId_MetricSource_Key, eventMappingTxnType);
		}
		return eventMappingTxnType;
	}

		
	/**
	 * Once all transaction and metrics data has been stored for the run, work out the start and end 
	 * time for the run.  Start/end times are taken lowest and highest transaction epoch time for the
	 * application run. 
	 *  
	 * The times are actually an approximation, as any time difference between the timestamp and the time
	 * to take the sample is not considered, nor is any running time before/after the first/last sample.
	 * 
	 * NOTE: When this method is called currently assumed the run being processed will have a  
	 * run-time of AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED (zeros) on TESTTRANSACTIONS 	  
	 */
	private DateRangeBean getRunDateRangeUsingTestTransactionalData(String application){
		Long runStartTime = testTransactionsDAO.getEarliestTimestamp(application);
		Long runEndTime   = testTransactionsDAO.getLatestTimestamp(application);
		return new DateRangeBean(runStartTime, runEndTime);
	}

}
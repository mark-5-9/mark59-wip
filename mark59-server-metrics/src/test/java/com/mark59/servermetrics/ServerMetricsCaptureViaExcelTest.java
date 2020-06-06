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

package com.mark59.servermetrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.logging.log4j.Level;
import org.junit.Test;

import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.servermetricsweb.utils.AppConstantsServerMetricsWeb;
import com.mark59.servermetricsweb.utils.ServerMetricsWebUtils;

public class ServerMetricsCaptureViaExcelTest  {
	
	@Test
    public final void testSimpleSheetWithLocalhostProfileForEachOs()
    {
		Log4jConfigurationHelper.init(Level.INFO);	
		ServerMetricsCaptureViaExcel smExcel = new ServerMetricsCaptureViaExcel();
		JavaSamplerContext context = new JavaSamplerContext( setArgs(smExcel) );
		smExcel.setupTest(context);
		SampleResult srMain = smExcel.runTest(context);   
		
		SampleResult[] subResArray = srMain.getSubResults();
		
		String listOfTxnNames = "";
		String listOfResponses= "";
		for (int i = 0; i < subResArray.length; i++) {
			listOfTxnNames  += "_" + subResArray[i].getSampleLabel() + "_";
			listOfResponses += subResArray[i].getResponseMessage();
		} 
		System.out.println(listOfTxnNames + " : " + listOfResponses );
		
		if (AppConstantsServerMetricsWeb.WINDOWS.equals(ServerMetricsWebUtils.obtainOperatingSystemForLocalhost())){
			assertEquals("wrond txn count", 3, subResArray.length);
			assertTrue(listOfTxnNames + " isnt right, no listng for Memory_localhost_FreePhysicalG" , listOfTxnNames.contains( "_Memory_localhost_FreePhysicalG_") );
			assertTrue(listOfTxnNames + " isnt right, no listng for Memory_localhost_FreeVirtualG"  , listOfTxnNames.contains( "_Memory_localhost_FreeVirtualG_") );
			assertTrue(listOfTxnNames + " isnt right, no listng for CPU_localhost"  				, listOfTxnNames.contains( "_CPU_localhost_") );
			assertTrue(listOfResponses + " isnt right"  , "PASSPASSPASS".equals(listOfResponses));
		} else if  (AppConstantsServerMetricsWeb.LINUX.equals(ServerMetricsWebUtils.obtainOperatingSystemForLocalhost())){
			assertEquals("wrond txn count", 4, subResArray.length);			
			assertTrue(listOfTxnNames + " isnt right, no listng for Memory_localhost_freeG"  		, listOfTxnNames.contains( "_Memory_localhost_freeG_") );
			assertTrue(listOfTxnNames + " isnt right, no listng for Memory_localhost_totalG"   		, listOfTxnNames.contains( "_Memory_localhost_totalG_") );
			assertTrue(listOfTxnNames + " isnt right, no listng for Memory_localhost_usedG"   		, listOfTxnNames.contains( "_Memory_localhost_usedG_") );
			assertTrue(listOfTxnNames + " isnt right, no listng for CPU_localhost_IDLE"  			, listOfTxnNames.contains( "_CPU_localhost_IDLE_") );
			assertTrue(listOfResponses + " isnt right"  , "PASSPASSPASS".equals(listOfResponses));
		} else {
			System.out.println("This test is not set up for the "  +  ServerMetricsWebUtils.obtainOperatingSystemForLocalhost() + " o/s ... bypassing asserts."  );
		}
				
    }


	private Arguments setArgs(ServerMetricsCaptureViaExcel smExcel) {
		Arguments args = smExcel.getDefaultParameters();
		Map<String,String> argmap = args.getArgumentsAsMap();
		
		Map<String,String> testMap = new LinkedHashMap<String,String>();
		testMap.put(ServerMetricsCaptureViaExcel.OVERRIDE_PROPERTY_MARK59_SERVER_PROFILES_EXCEL_FILE_PATH , 
					"./src/test/resources/simpleSheetWithLocalhostProfileForEachOs/mark59serverprofiles.xlsx");
		testMap.put(ServerMetricsCaptureViaExcel.SERVER_PROFILE_NAME, 
					"localhost_" + ServerMetricsWebUtils.obtainOperatingSystemForLocalhost());
		
		Arguments testargs = Mark59Utils.mergeMapWithAnOverrideMap(argmap,testMap);
		return testargs; 
	}

   
  	
}
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

package com.mark59.metrics.controller;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.metrics.application.UtilsMetrics;
import com.mark59.metrics.data.beans.Transaction;
import com.mark59.metrics.data.run.dao.RunDAO;
import com.mark59.metrics.data.transaction.dao.TransactionDAO;
import com.mark59.metrics.form.TransactionRenameForm;

/**
 * @author Philip Webb
 * Written: Australian Winter 2021  
 */

@Controller
public class TransactionController {
	
	@Autowired
	TransactionDAO transactionDAO; 
	@Autowired
	RunDAO runDAO; 
	
	@RequestMapping("/transactionList")
	public ModelAndView getTransactionList(@RequestParam(required=false) String reqApp) {
		List<String> applicationList = populateApplicationDropdown();
		if (StringUtils.isBlank(reqApp) && applicationList.size() > 1  ){
			// when no application request parameter has been sent, take the first application  
			reqApp = (String)applicationList.get(1);
		}		
		
		List<Transaction> transactionList = transactionDAO.getUniqueListOfTransactionsByType(reqApp);

		Map<String, Object> map = new HashMap<String, Object>(); 
		map.put("transactionList",transactionList);
		map.put("reqApp",reqApp);
		map.put("applications",applicationList);
		return new ModelAndView("transactionList", "map", map);
	}
	
	
	
	@RequestMapping("/transactionRenameDataEntry") 
	public Object renameTransactionEntry(@RequestParam String reqApp,  @RequestParam String reqTxnId, @RequestParam String reqTxnType,
			@ModelAttribute TransactionRenameForm transactionRenameForm  ) {
		System.out.println("@ transactionRenameDataEntry : reqTxnId=" + reqTxnId + "reqTxnType=" + reqTxnType + ", TrRenameFm=" + transactionRenameForm);

		transactionRenameForm.setApplication(reqApp); ;
		transactionRenameForm.setFromTxnId(reqTxnId);
		transactionRenameForm.setTxnType(reqTxnType);
		
		return new ModelAndView("transactionRenameDataEntry", "transactionRenameForm", transactionRenameForm);
	}

	
	@RequestMapping("/transactionRenameValidate") 
	public Object transactionRenameValidate(@ModelAttribute TransactionRenameForm transactionRenameForm ) {
		System.out.println("@ transactionRenameValidate : TrRenameFm=" + transactionRenameForm  );		
		transactionRenameForm.setPassedValidation("Y");
		
		if (StringUtils.isBlank(transactionRenameForm.getToTxnId())){
			transactionRenameForm.setPassedValidation("N");
			transactionRenameForm.setValidationMsg("<p style='color:red'>Blank transaction name not allowed</p>");
			return new ModelAndView("transactionRenameValidate", "transactionRenameForm" , transactionRenameForm  );
		}
		
		if (transactionRenameForm.getToTxnId().equals(transactionRenameForm.getFromTxnId())){
			transactionRenameForm.setPassedValidation("N");
			transactionRenameForm.setValidationMsg("<p style='color:red'>The transaction names must differ !</p>");
			return new ModelAndView("transactionRenameValidate", "transactionRenameForm" , transactionRenameForm  );
		}		
		
		
		long clashOfTxns = transactionDAO.countRunsContainsBothTxnIds(transactionRenameForm.getApplication(), 
																		transactionRenameForm.getTxnType(),
																		transactionRenameForm.getFromTxnId(),
																		transactionRenameForm.getToTxnId());
		System.out.println("clashOfTxns=" + clashOfTxns);
		
		if (clashOfTxns > 0  ){
			
			transactionRenameForm.setPassedValidation("N");
			String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
			
			String clashSql =  "SELECT DISTINCT R.RUN_TIME, R.BASELINE_RUN, R.IS_RUN_IGNORED FROM RUNS R, TRANSACTION T "    
							   + " WHERE R.APPLICATION = '" + transactionRenameForm.getApplication() + "' " 
							   + " AND R.APPLICATION = T.APPLICATION AND R.RUN_TIME = T.RUN_TIME "   
							   + " AND R.RUN_TIME IN ( SELECT RUN_TIME FROM TRANSACTION WHERE TXN_TYPE = '" + transactionRenameForm.getTxnType() + "'" 
							   														+ " AND TXN_ID = '" + transactionRenameForm.getFromTxnId() + "') " 
							   + " AND R.RUN_TIME IN ( SELECT RUN_TIME FROM TRANSACTION WHERE TXN_TYPE = '" + transactionRenameForm.getTxnType() + "'" 
							   														+ " AND TXN_ID = '" + transactionRenameForm.getToTxnId() + "') " 
							   + " ORDER BY R.RUN_TIME DESC";  			

			System.out.println("clashSql=" + clashSql);
			
			String encodedClashSql = UtilsMetrics.encodeBase64urlParam(clashSql);

			if (   Mark59Constants.DatabaseTxnTypes.TRANSACTION.name().equals(transactionRenameForm.getTxnType())){
				String clashLink = baseUrl + "/trending?reqApp=" + transactionRenameForm.getApplication() + "&reqUseRawRunSQL=true&reqRunTimeSelectionSQL=" + encodedClashSql;
				transactionRenameForm.setValidationMsg("<p style='color:red'><b>Invalid Rename. There are run(s) containing both transaction names.</b></p>" +
						"<p>Please refer to the link below to examine these runs</p>" + 
						"<p><a href='" + clashLink + "'>Trend Analysis Graph for Runs with both Transaction Names</a></p>" ); 
			} else { 
				
				String graph = "CPU_UTIL";
				if  (Mark59Constants.DatabaseTxnTypes.MEMORY.name().equals(transactionRenameForm.getTxnType())){
					graph = "MEMORY";
				} else if  (Mark59Constants.DatabaseTxnTypes.DATAPOINT.name().equals(transactionRenameForm.getTxnType())){
					graph = "DATAPOINT_AVE";
				}  
				String clashLink = baseUrl + "/trending?reqApp=" + transactionRenameForm.getApplication() + "&reqGraph=" + graph +"&reqUseRawRunSQL=true&reqRunTimeSelectionSQL=" + encodedClashSql;
				transactionRenameForm.setValidationMsg("<p style='color:red'><b>Invalid Rename. There are run(s) containing both transaction names.</b></p>" +
						"<p>Please refer to the link below to examine these runs</p>" + 
						"<p>Note for metric transaction types the Graph names that delpoy with Mark59 are assumed to exist.</p>" + 
						"<p><a href='" + clashLink + "'>Trend Analysis Graph for Runs with both Transaction Names</a></p>" ); 			
			}
	
			
		} else {  // a valid rename
			
			String validationOkMsg = "<p>Please press the Rename button to rename the transaction."
					+ "<p>If a SLA entry exists for the original transaction it will also be renamed, "
					+ "unless an entry for the new transaction name already exists.";
					
			
			long doesToTxnIdExist = transactionDAO.countRunsContainsBothTxnIds(transactionRenameForm.getApplication(), 
																				transactionRenameForm.getTxnType(),
																				transactionRenameForm.getToTxnId(),
																				transactionRenameForm.getToTxnId()); //repeated!
			System.out.println("doesToTxnIdExist = " + doesToTxnIdExist);
			
			if (doesToTxnIdExist > 0 ) {
				validationOkMsg = validationOkMsg +
				  	"<p>Some runs already contain transactions named " + transactionRenameForm.getToTxnId() + ".</b><br>" +
				  	"This means that this rename action may not be reversible (you are doing a merge of two transaction Ids)." + 
					"<p><b>Check everything is OK before you Rename !</b>"; 
			}
			
			transactionRenameForm.setValidationMsg(validationOkMsg);
		}
	
		return new ModelAndView("transactionRenameValidate", "transactionRenameForm" , transactionRenameForm  );	
	}	


//	TODO	
//	@RequestMapping("/updateTransaction")
//	public String updateTransaction(@RequestParam(required=false) String reqApp, @ModelAttribute Transaction transaction) {
////		System.out.println("@ updateSla : reqApp=" + reqApp + ", app=" + sla.getApplication() + ",  origTxn=" + sla.getSlaOriginalTxnId() + ", txnId=" + sla.getTxnId() + " ,90th=" + sla.getSla90thResponse()   );
//		transactionDAO.updateData(transaction);
//		return "redirect:/transactionList?reqApp=" + reqApp  ;
//	}
//

	
	private List<String> populateApplicationDropdown() {
		List<String> applicationList = new ArrayList<String>();
		applicationList = runDAO.findApplications();
		applicationList.add(0, "");
		return applicationList;
	}		

	
}

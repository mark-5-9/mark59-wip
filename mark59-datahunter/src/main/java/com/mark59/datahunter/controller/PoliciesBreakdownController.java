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

package com.mark59.datahunter.controller;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.mark59.datahunter.application.DataHunterConstants;
import com.mark59.datahunter.application.DataHunterUtils;
import com.mark59.datahunter.application.IndexedReusableUtils;
import com.mark59.datahunter.application.SqlWithParms;
import com.mark59.datahunter.data.beans.Policies;
import com.mark59.datahunter.data.policies.dao.PoliciesDAO;
import com.mark59.datahunter.model.CountPoliciesBreakdown;
import com.mark59.datahunter.model.CountPoliciesBreakdownForm;
import com.mark59.datahunter.model.PolicySelectionCriteria;
import com.mark59.datahunter.pojo.ValidReuseIxPojo;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
@Controller
public class PoliciesBreakdownController {
	
	@Autowired
	PoliciesDAO policiesDAO;	
		

	@RequestMapping("/policies_breakdown")
	public String policiesBreakdownUrl(@RequestParam(required = false) String reqApplication,
			PolicySelectionCriteria policySelectionCriteria, Model model) {
//		System.out.println("/policies_breakdown");
		List<String> applicationOperators = new ArrayList<>(DataHunterConstants.APPLICATION_OPERATORS);
		model.addAttribute("applicationOperators",applicationOperators);
		List<String> usabilityList = new ArrayList<>(DataHunterConstants.USEABILITY_LIST);
		usabilityList.add(0,"");
		model.addAttribute("Useabilities",usabilityList);		
		return "/policies_breakdown";
	}
	
		
	@RequestMapping("/policies_breakdown_action")
	public ModelAndView policiesBreakdownAction(@ModelAttribute PolicySelectionCriteria policySelectionCriteria,
			Model model, HttpServletRequest httpServletRequest) {
		
		// this just aligns the Application value shown on the action page, with what is actually used in the sql
		if (DataHunterConstants.STARTS_WITH.equals(policySelectionCriteria.getApplicationStartsWithOrEquals())){
			// allows for embedded space within Application name, but still remove leading whitespace 
			policySelectionCriteria.setApplication(StringUtils.stripStart(policySelectionCriteria.getApplication(), null));
		} else { // 'EQUALS'
			policySelectionCriteria.setApplication(policySelectionCriteria.getApplication().trim()); 
		}

		String navUrParms = "application=" + DataHunterUtils.encode(policySelectionCriteria.getApplication())
			+ "&applicationStartsWithOrEquals=" + DataHunterUtils.encode(policySelectionCriteria.getApplicationStartsWithOrEquals()) 
			+ "&lifecycle="    + DataHunterUtils.encode(policySelectionCriteria.getLifecycle()) 
			+ "&useability="   + DataHunterUtils.encode(policySelectionCriteria.getUseability());

		model.addAttribute("navUrParms", navUrParms);			
		
		SqlWithParms sqlWithParms = policiesDAO.constructCountPoliciesBreakdownSql(policySelectionCriteria);
		List<CountPoliciesBreakdown> countPoliciesBreakdownList = policiesDAO.runCountPoliciesBreakdownSql(sqlWithParms);
		int rowsAffected = countPoliciesBreakdownList.size();
		
		List<CountPoliciesBreakdownForm > countPoliciesBreakdownFormList = new ArrayList<CountPoliciesBreakdownForm>();
		
		for (CountPoliciesBreakdown countPoliciesBreakdown : countPoliciesBreakdownList) {
			CountPoliciesBreakdownForm countPoliciesBreakdownForm = new CountPoliciesBreakdownForm(); 
			countPoliciesBreakdownForm.setApplicationStartsWithOrEquals(countPoliciesBreakdown.getApplicationStartsWithOrEquals());
			countPoliciesBreakdownForm.setApplication(countPoliciesBreakdown.getApplication());
			countPoliciesBreakdownForm.setLifecycle(countPoliciesBreakdown.getLifecycle());
			countPoliciesBreakdownForm.setUseability(countPoliciesBreakdown.getUseability());
			countPoliciesBreakdownForm.setRowCount(countPoliciesBreakdown.getRowCount());
			countPoliciesBreakdownForm.setLookupParmsUrl(
				"application="   + DataHunterUtils.encode(countPoliciesBreakdown.getApplication()) 
				+ "&lifecycle="  + DataHunterUtils.encode(countPoliciesBreakdown.getLifecycle())
				+ "&useability=" + DataHunterUtils.encode(countPoliciesBreakdown.getUseability()));
			
			countPoliciesBreakdownForm.setIsIndexedReusable("N");
			countPoliciesBreakdownForm.setHoleCount(0L);
			countPoliciesBreakdownForm.setHoleStats("");

			ValidReuseIxPojo validReuseIx = policiesDAO.validateReusableIndexed(countPoliciesBreakdown);
			if (validReuseIx.getPolicyReusableIndexed()){
				countPoliciesBreakdownForm.setIsIndexedReusable("Y");
				if (validReuseIx.getValidatedOk()) {
					if (countPoliciesBreakdown.getRowCount() <= 1 ){  // only the IX row itself exists 
						countPoliciesBreakdownForm.setHoleCount(0L);
						countPoliciesBreakdownForm.setHoleStats("na");
						
					} else {
						Long pcHoles = 100L; 
						countPoliciesBreakdown.setHoleCount(
								Long.valueOf(validReuseIx.getCurrentIxCount()) - validReuseIx.getIdsinRangeCount());
						if (validReuseIx.getCurrentIxCount() > 0) {
							pcHoles = (countPoliciesBreakdown.getHoleCount()*100) / validReuseIx.getCurrentIxCount(); 
						}
						countPoliciesBreakdownForm.setHoleStats(countPoliciesBreakdown.getHoleCount() + " ("+pcHoles+"%)");
					}	
				} else { // invalid 
					countPoliciesBreakdownForm.setHoleCount(-1L);
					countPoliciesBreakdownForm.setHoleStats("?");					
				}
			}
			countPoliciesBreakdownFormList.add(countPoliciesBreakdownForm);
		}

		model.addAttribute("countPoliciesBreakdownFormList", countPoliciesBreakdownFormList);

		model.addAttribute("sql", sqlWithParms);
		model.addAttribute("sqlResult", "PASS");
		model.addAttribute("rowsAffected", rowsAffected);	

		if (rowsAffected == 0 ){
			model.addAttribute("sqlResultText", "sql execution OK, but no rows matched the selection criteria.");
		} else {
			model.addAttribute("sqlResultText", "sql execution OK");
		}
		DataHunterUtils.expireSession(httpServletRequest);
		
		System.out.println(">> ============ ADDRESSES-HARMONY  TESTTAS");
		reindexReusableIx("ADDRESSES-HARMONY", "TESTTAS");
		System.out.println("<< ============ ");
		
		System.out.println("============ ADDRESSES-HARMONY  BIGGY");
		reindexReusableIx("ADDRESSES-HARMONY", "BIGGY");
		System.out.println("<< ============ ");
		
		
		return new ModelAndView("policies_breakdown_action", "model", model);
	}
	

	public String reindexReusableIx(String application, String lifecycle){
		String resutMsg = DataHunterConstants.OK;
		PolicySelectionCriteria targetData =  new PolicySelectionCriteria();
		targetData.setApplication(application);
		targetData.setLifecycle(lifecycle);
		targetData.setUseability(DataHunterConstants.REUSABLE);
		
		ValidReuseIxPojo validReuseIx = policiesDAO.validateReusableIndexed(targetData);
		if (!validReuseIx.getPolicyReusableIndexed()){
			return "No action : "+targetData+" is not marked as IndexedReusable (no Id 0000000000_IX row";  
		}
		
		SqlWithParms sqlWithParms = policiesDAO.countNonReusableIdsForReusableIndexedData(application, lifecycle);
		int nonReuseableidsCount = policiesDAO.runCountSql(sqlWithParms);		
		if (nonReuseableidsCount != 0){
			return "No action : App|lifecycle "+application+" | "+lifecycle+" contains Ids that are marked"
					+ " other than REUSABLE. Please reset or remove this data as appropriate";  
		}
		
		sqlWithParms = policiesDAO.constructCountPoliciesSql(targetData);
		int policyCount = policiesDAO.runCountSql(sqlWithParms) - 1;
		
		sqlWithParms = policiesDAO.constructCollectDataOutOfExpectedIxRangeSql(application, lifecycle, policyCount);
		Stream<Policies> policyStream = policiesDAO.runStreamPolicieSql(sqlWithParms);
		
		System.out.println(" -- "+application+":"+lifecycle+":"+policyCount); 
		
		Iterator<Policies> policyStreamIter = policyStream.iterator();

		Policies currPolicy = new Policies();
		currPolicy.setApplication(application);
		currPolicy.setLifecycle(lifecycle);
		currPolicy.setUseability(DataHunterConstants.REUSABLE);

		for (int ix = 1; ix <= policyCount && policyStreamIter.hasNext(); ix++) { // lets start filling up holes
			currPolicy.setIdentifier(StringUtils.leftPad(String.valueOf(ix), 10, "0"));
			System.out.println("loop "+ix+" currPolicy: "+ currPolicy );
			sqlWithParms = policiesDAO.constructSelectPolicySql(currPolicy);
			List<Policies> existingidInRange = policiesDAO.runSelectPolicieSql(sqlWithParms);
			System.out.println("loop "+ix+" found: "+ existingidInRange );
			
			if (existingidInRange.isEmpty()) { // a hole in range, use a out of range row to plug it
				movePolicyToHole(currPolicy, ix, policyStreamIter.next());
			}
		}
		
		IndexedReusableUtils.updateIndexedRowCounter(currPolicy, policyCount, policiesDAO);	
		System.out.println("msg:"+ resutMsg);
		return resutMsg; 
	}


	private void movePolicyToHole(Policies currPolicy, int ix, Policies toMovePolicy) {
		System.out.println("moving:"+toMovePolicy+", to:"+currPolicy);
		// delete toMovePolicy from db
		PolicySelectionCriteria pscToMovePolicy = new PolicySelectionCriteria();
		pscToMovePolicy.setApplication(toMovePolicy.getApplication());
		pscToMovePolicy.setIdentifier(toMovePolicy.getIdentifier());
		pscToMovePolicy.setLifecycle(toMovePolicy.getLifecycle());
		SqlWithParms sqlWithParms = policiesDAO.constructDeletePoliciesSql(pscToMovePolicy);
		policiesDAO.runDatabaseUpdateSql(sqlWithParms);
		// add back into slot
		currPolicy.setOtherdata(toMovePolicy.getOtherdata());
		sqlWithParms =  policiesDAO.constructInsertDataSql(currPolicy);
		policiesDAO.runDatabaseUpdateSql(sqlWithParms);
	}
	
}

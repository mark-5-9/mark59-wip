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

package com.mark59.metrics.pojos;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Philip Webb
 * Written: Australian Summer 2020
 */
public class ScriptResponse {

	private List<ParsedMetric> parsedMetrics;
	private String commandLog;	
	private Boolean commandFailure;
	
	
	public ScriptResponse() {
		this.parsedMetrics = new ArrayList<ParsedMetric>();
		this.commandLog = "";
		this.commandFailure = false;
	}

	public List<ParsedMetric> getParsedMetrics() {
		return parsedMetrics;
	}

	public void setParsedMetrics(List<ParsedMetric> parsedMetrics) {
		this.parsedMetrics = parsedMetrics;
	}

	public String getCommandLog() {
		return commandLog;
	}

	public void setCommandLog(String commandLog) {
		this.commandLog = commandLog;
	}

	public Boolean getCommandFailure() {
		return commandFailure;
	}

	public void setCommandFailure(Boolean commandFailure) {
		this.commandFailure = commandFailure;
	}

	@Override
    public String toString() {
        return   "[parsedMetrics= "  + parsedMetrics
        		+ ", commandLog="+ commandLog   
        		+ ", commandFailure="+ commandFailure   
        		+ "]";
	}
		
}

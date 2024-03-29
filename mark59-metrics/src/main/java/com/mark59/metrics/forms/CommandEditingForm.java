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

package com.mark59.metrics.forms;

import java.util.Arrays;
import java.util.List;

import com.mark59.metrics.data.beans.Command;

/**
 * @author Philip Webb
 * Written: Australian Spring 2020
 * 
 */
public class CommandEditingForm {

	Command command;
	List<ParserSelector> parserSelectors;
	String paramNamesTextboxFormat;
	String saveCmdAction;
	String lastSavedTimestamp;
	
	public CommandEditingForm() {
	}

	public Command getCommand() {
		return command;
	}

	public void setCommand(Command command) {
		this.command = command;
	}

	public List<ParserSelector> getParserSelectors() {
		return parserSelectors;
	}

	public void setParserSelectors(List<ParserSelector> parserSelectors) {
		this.parserSelectors = parserSelectors;
	}

	public String getParamNamesTextboxFormat() {
		return paramNamesTextboxFormat;
	}

	public void setParamNamesTextboxFormat(String paramNamesTextboxFormat) {
		this.paramNamesTextboxFormat = paramNamesTextboxFormat;
	}
	
	public String getSaveCmdAction() {
		return saveCmdAction;
	}

	public void setSaveCmdAction(String saveCmdAction) {
		this.saveCmdAction = saveCmdAction;
	}
	
	public String getLastSavedTimestamp() {
		return lastSavedTimestamp;
	}

	public void setLastSavedTimestamp(String lastSavedTimestamp) {
		this.lastSavedTimestamp = lastSavedTimestamp;
	}

	@Override
    public String toString() {
        return   "[commandName="+ command.getCommand() + 
        		", executor="+ command.getExecutor() + 
        		", command="+ command.getCommand() + 
        		", comment="+ command.getComment() + 
        		", command.parmNames="+ command.getParamNames() + 
        		", parserSelectors = "+ Arrays.toString(parserSelectors.toArray()) + 
        		", paramNamesTextboxFormat = " + paramNamesTextboxFormat + 
        		", saveCmdAction = " + saveCmdAction + 
        		", lastSavedTimestamp = " + lastSavedTimestamp + 
        		"]";
	}
		
}

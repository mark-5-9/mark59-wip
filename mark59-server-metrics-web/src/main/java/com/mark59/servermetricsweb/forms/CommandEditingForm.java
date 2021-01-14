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

package com.mark59.servermetricsweb.forms;

import java.util.Arrays;
import java.util.List;

import com.mark59.servermetricsweb.data.beans.Command;

/**
 * @author Philip Webb
 * Written: Australian Spring 2020
 * 
 */
public class CommandEditingForm {

	Command command;
	List<ScriptSelector> scriptSelectors;
	String paramNamesTextboxFormat;

	
	public CommandEditingForm() {
	}

	public Command getCommand() {
		return command;
	}

	public void setCommand(Command command) {
		this.command = command;
	}

	public List<ScriptSelector> getScriptSelectors() {
		return scriptSelectors;
	}

	public void setScriptSelectors(List<ScriptSelector> scriptSelectors) {
		this.scriptSelectors = scriptSelectors;
	}

	public String getParamNamesTextboxFormat() {
		return paramNamesTextboxFormat;
	}

	public void setParamNamesTextboxFormat(String paramNamesTextboxFormat) {
		this.paramNamesTextboxFormat = paramNamesTextboxFormat;
	}

	@Override
    public String toString() {
        return   "[commandName="+ command.getCommand() + 
        		", executor="+ command.getExecutor() + 
        		", command="+ command.getCommand() + 
        		", comment="+ command.getComment() + 
        		", command.parmNames="+ command.getParamNames() + 
        		", scriptSelectors = "+ Arrays.toString(scriptSelectors.toArray()) + 
        		", paramNamesTextboxFormat = " + paramNamesTextboxFormat + 
        		"]";
	}
		
}

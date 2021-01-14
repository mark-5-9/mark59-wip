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

package com.mark59.servermetricsweb.drivers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mark59.servermetricsweb.data.beans.Command;
import com.mark59.servermetricsweb.data.beans.ServerProfile;
import com.mark59.servermetricsweb.pojos.CommandDriverResponse;
import com.mark59.servermetricsweb.pojos.ScriptResponse;
import com.mark59.servermetricsweb.utils.ServerMetricsWebUtils;


/**
 * @author Philip Webb    
 * @author Michael Cohen
 * Written: Australian Winter 2019   
 */
public class CommandDriverGroovyScriptImpl implements CommandDriver {

	private static final Logger LOG = LogManager.getLogger(CommandDriverGroovyScriptImpl.class);	
	private ServerProfile serverProfile;

	
	public CommandDriverGroovyScriptImpl(ServerProfile serverProfile) {
		this.serverProfile = serverProfile;
	}
	
	
	/**
	 * Executes the 'command', which in this case is a Groovy script 
	 * @param command (Groovy script)
	 * @return CommandDriverResponse
	 */
	public CommandDriverResponse executeCommand(Command command) {
		LOG.debug("executeCommand (script) : " + command);
		CommandDriverResponse commandDriverResponse = new CommandDriverResponse();
		commandDriverResponse.setRawCommandResponseLines(new ArrayList<String>()); ;
		
		String commandLog = " :<br><font face='Courier'> executing groovy script : " + command.getCommandName() + "</font><br>"; 
		
		ScriptResponse groovyScriptResult = new ScriptResponse();
		
		try {
			
			Map<String,Object> scriptParms = new HashMap<String,Object>(); 
			scriptParms.put("serverProfile", serverProfile);
			Map<String,String> serverProfileParms = serverProfile.getParameters()  == null ? new HashMap<String,String>() : serverProfile.getParameters();
			serverProfileParms.forEach((key,value) -> scriptParms.put(key, value));
			
			groovyScriptResult = (ScriptResponse)ServerMetricsWebUtils.runGroovyScript(command.getCommand().replaceAll("\\R", "\n"), scriptParms);
	
			commandDriverResponse.setParsedMetrics(groovyScriptResult.getParsedMetrics());
			commandDriverResponse.setCommandFailure(groovyScriptResult.getCommandFailure());
			commandLog += "<br>Response :<br><font face='Courier'>" 
					+ String.join("<br>", groovyScriptResult.getCommandLog()).replace(" ", "&nbsp;") + "</font><br>";
			
		} catch (Exception e) {
			commandDriverResponse.setCommandFailure(true);			
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			commandLog+= "<br>A faiure has occured attempting to execute groovy script command : " + e.getMessage() + "<br>" + stackTrace.toString() 
						+ "<br><br>" + groovyScriptResult.getCommandLog();

			LOG.warn("Command failure on script : " + command.getCommandName() + ":\n" + e.getMessage() + stackTrace.toString());
		}
		
		commandDriverResponse.setCommandLog(commandLog);
		
		LOG.debug("ScriptResponse : " + groovyScriptResult);
		System.out.println("ScriptResponse : " + groovyScriptResult);
		return commandDriverResponse;
	}
}

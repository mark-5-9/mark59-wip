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

package com.mark59.metrics.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Philip Webb
 * Written: Australian Summer 2020  
 */
public class MetricsConstants {
	
	public static final String MARK59_VERSION_METRICS = "6.4";	
	public static final String MARK59_SERVER_PROFILES_EXCEL_FILE = "mark59serverprofiles.xlsx";  
	public static final String SERVER_PROFILE_NOT_FOUND  = "SERVER_PROFILE_NOT_FOUND";	
	
	public static final boolean RUNNING_VIA_WEB = true;
	
	// pre-defined (non-Groovy) command parameter names
	public static final String METRICS_BASE_DIR 	= "METRICS_BASE_DIR"; 
	public static final String PROFILE_NAME			= "PROFILE_NAME";	
	public static final String PROFILE_SERVER		= "PROFILE_SERVER";	
	public static final String PROFILE_USERNAME		= "PROFILE_USERNAME";	
	public static final String PROFILE_PASSWORD		= "PROFILE_PASSWORD";

	public static final String PROFILE_PASSWORD_VAR	= "${"+PROFILE_PASSWORD+"}";

	// unix connection via Jsch 
	public static final String KERBEROS = "KERBEROS";
	public static final String SSH_PREFERRED_AUTHENTICATIONS = "SSH_PREFERRED_AUTHENTICATIONS";
	public static final String SSH_KNOWN_HOSTS = "SSH_KNOWN_HOSTS";
	public static final String SSH_IDENTITY = "SSH_IDENTITY";
	public static final String SSH_PASSPHRASE = "SSH_PASSPHRASE";	
	
	public enum CommandExecutorDatatypes {
		WMIC_WINDOWS("WMIC_WINDOWS"), POWERSHELL_WINDOWS("POWERSHELL_WINDOWS"), SSH_LINUX_UNIX("SSH_LINUX_UNIX"), GROOVY_SCRIPT("GROOVY_SCRIPT");

		private final String executorText;
		
		CommandExecutorDatatypes(String executorText) {this.executorText = executorText;}
		public String getExecutorText() {return executorText;}
		
		public static List<String> listOfCommandExecutorDatatypes(){
			List<String> listOfCommandExecutorDatatypes = new ArrayList<String>();
			for (CommandExecutorDatatypes commandExecutorDatatypes : (CommandExecutorDatatypes.values())) {
				listOfCommandExecutorDatatypes.add(commandExecutorDatatypes.name());
			}
			return listOfCommandExecutorDatatypes;
		}
	}
	
	
	
    private MetricsConstants() {
    }
}

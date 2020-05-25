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

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;

import com.mark59.core.utils.Mark59Utils;
import com.mark59.core.utils.SimpleAES;
import com.mark59.servermetricsweb.data.beans.Command;
import com.mark59.servermetricsweb.data.beans.ServerProfile;
import com.mark59.servermetricsweb.pojos.CommandDriverResponse;
import com.mark59.servermetricsweb.utils.AppConstantsServerMetricsWeb.CommandExecutorDatatypes;


/**
 * @author Philip Webb    
 * @author Michael Cohen
 * Written: Australian Winter 2019   
 */
public class CommandDriverWinWmicImpl implements CommandDriver {

	
	private static final String WMIC_COMMAND_REMOTE_FORMAT = "wmic /user:{0} /password:{1} /node:{2} {3}";
	private static final String WMIC_COMMAND_LOCAL_FORMAT  = "wmic /node:localhost {0}";
	
	
	private static final String CHALLENGE_REPLACE_REGEX = "(^.*password:).*?(\\s.*$)";
	private static final String CHALLENGE_REPLACE_VALUE = "$1********$2";
	
//	private static final String WMIC_FREE_VIRTUAL_MEMORY = "OS get FreeVirtualMemory";
//	private static final String WMIC_FREE_PHYSICAL_MEMORY = "OS get FreePhysicalMemory";
//	private static final String WMIC_CPU_LOAD_PERCENTAGE = "cpu get loadpercentage";
//	private static final int MEMORY_METRICS_DIVISOR = 1000000;
	
	private ServerProfile serverProfile;
	

	public CommandDriverWinWmicImpl(ServerProfile serverProfile) {
		this.serverProfile = serverProfile;
	}
	
	
	/**
	 * Executes the DOS command via WMIC returning the response  
	 * @param command
	 * @return CommandDriverResponse
	 */
	@Override
	public CommandDriverResponse executeCommand(Command command) {
		LOG.debug("executeCommand :" + command);
		
		String actualPassword = serverProfile.getPassword();
		String runtimeCommand = "";
		String runtimeCommandLog = "";
		
		String cipherUsedLog = " (no pwd chipher)"; 
		if (StringUtils.isNotBlank(serverProfile.getPasswordCipher())){
			actualPassword = SimpleAES.decrypt(serverProfile.getPasswordCipher());
			cipherUsedLog = " (pwd chipher used)" ;
		} 			
		
		if ("localhost".equalsIgnoreCase(serverProfile.getServer())) {
			runtimeCommand = MessageFormat.format(WMIC_COMMAND_LOCAL_FORMAT, command.getCommand().replaceAll("\\R", " ") );
			runtimeCommandLog = " :<br><font face='Courier'>" + runtimeCommand.replaceAll("\\R", " ")  + "</font>";
			cipherUsedLog = " (local execution)";
		} else {
			runtimeCommand = MessageFormat.format(WMIC_COMMAND_REMOTE_FORMAT, serverProfile.getUsername(), actualPassword, serverProfile.getServer(), command.getCommand().replaceAll("\\R", " ") );
			runtimeCommandLog = ": <br><font face='Courier'>" + runtimeCommand.replaceAll(CHALLENGE_REPLACE_REGEX, CHALLENGE_REPLACE_VALUE).replaceAll("\\R", "") + "</font>";
		}

		String IgnoreStdErrLog = "";
		if(Mark59Utils.resovesToTrue(command.getIngoreStderr())){
			IgnoreStdErrLog = ". StdErr to be ignored. ";
		}

		String commandLog = cipherUsedLog + IgnoreStdErrLog + runtimeCommandLog; 
		
		CommandDriverResponse commandDriverResponse = CommandDriver.executeRuntimeCommand(runtimeCommand, command.getIngoreStderr(),CommandExecutorDatatypes.WMIC_WINDOWS);
		
		commandLog += "<br>Response :<br><font face='Courier'>" 
					+ commandDriverResponse.getCommandLog()
					+ String.join("<br>", commandDriverResponse.getRawCommandResponseLines()) + "</font><br>";

		commandDriverResponse.setCommandLog(commandLog);
		return commandDriverResponse;
	}

	

	
	
//	public Map<String, Long> getCpuMetrics() {
//		Map<String, Long> metrics = new HashMap<>();
//
//		String command = null;
//
//		if (isLocalHost()) {
//			command = MessageFormat.format(WMIC_COMMAND_LOCAL_FORMAT, WMIC_CPU_LOAD_PERCENTAGE);
//		} else {
//			command = MessageFormat.format(WMIC_COMMAND_REMOTE_FORMAT, serverProfile.getUsername(), actualPassword, serverProfile.getServer(), WMIC_CPU_LOAD_PERCENTAGE);
//		}	
//		
//		commandLog+= "<br><br>Command:<br><font face='Courier'>" + command.replaceAll(CHALLENGE_REPLACE_REGEX, CHALLENGE_REPLACE_VALUE) + "</font><br>Chiper Used? : " + cipherUsed + ", Response :<br><font face='Courier'>";
//		
//		List<String> commandReply = executeCommand(command);
//		
//		for (String commandReplyLine : commandReply) {
//			commandLog += "    " +commandReplyLine + "<br>";
//		}
//		commandLog+= "</font><br>";
//		
//		List<Double> rawWmiCpuStats = getDoublesFromList(commandReply);
//
//		if ( rawWmiCpuStats.size() == 0  ) {
//			LOG.warn("cpu caputure has failed : " + commandLog) ;			
//			//if (LOG.isDebugEnabled()) LOG.debug("commandLog : " + commandLog);		
//		} else {
//			Double sumCpuValues = rawWmiCpuStats.stream()
//					.mapToDouble(Double::doubleValue)
//					.sum();
//	
//			Long metricValue = Math.round(sumCpuValues / rawWmiCpuStats.size());
//	
//			metrics.put("CPU_" + reportedServerId, metricValue);
//		}
//		return metrics;
//	}

	

//	public Map<String, Long> getMemoryMetrics() {
//		Map<String, Long> metrics = new HashMap<>();
//
//		Double virtualMemoryMetric = getVirtualMemoryMetric();
//		if (virtualMemoryMetric != null) {
//			metrics.put("Memory_" + reportedServerId + "_FreeVirtualG",
//					Math.round(virtualMemoryMetric / MEMORY_METRICS_DIVISOR));
//		}
//		
//		Double physicalMemoryMetric = getPhysicalMemoryMetric();
//		if (physicalMemoryMetric != null) {
//			metrics.put("Memory_" + reportedServerId + "_FreePhysicalG",
//					Math.round(physicalMemoryMetric / MEMORY_METRICS_DIVISOR));
//		}
//
//		if ( metrics.size() == 0  ) {
//			LOG.warn("memory caputure has failed ( server " + reportedServerId + " )");
//		}	
//		
//		return metrics;
//	}
	
//	@Override
//	public String getCommandLog() {
//		return commandLog;
//	}	
	
	
	
	
//	/**
//	 * converts a List<String> into a List<Double>, dropping all non numeric Strings in the Process 
//	 * @param stringList
//	 * @return
//	 */
//	private List<Double> getDoublesFromList(List<String> stringList) {
//		return stringList.stream()
//				.filter(StringUtils::isNumeric)
//				.map(Double::parseDouble)
//				.collect(Collectors.toList());
//	}
//
//	/**
//	 * Builds and executes a WMIC command to return the systems utilisation of PHYSICAL memory 
//	 * @return
//	 */
//	private Double getPhysicalMemoryMetric() {
//		return getMemoryMetric(false);
//	}
//
//	/**
//	 * Builds and executes a WMIC command to return the systems utilisation of VIRTUAL memory 
//	 * @return
//	 */
//	private Double getVirtualMemoryMetric() {
//		return getMemoryMetric(true);
//	}
//
//	/**
//	 * Generic method to build the WMIC memory command
//	 * @param isVirtualMemory
//	 * @return
//	 */
//	private Double getMemoryMetric(boolean isVirtualMemory) {
//		String command = null;
//		String memoryType = isVirtualMemory ? WMIC_FREE_VIRTUAL_MEMORY : WMIC_FREE_PHYSICAL_MEMORY;
//
//		if (isLocalHost())
//			command = MessageFormat.format(WMIC_COMMAND_LOCAL_FORMAT, memoryType);
//		else
//			command = MessageFormat.format(WMIC_COMMAND_REMOTE_FORMAT, user, actualPassword, server, memoryType);
//
//		LOG.debug(command.replaceAll(CHALLENGE_REPLACE_REGEX, CHALLENGE_REPLACE_VALUE));
//		
//		List<String> commandResult =  executeCommand(command);
//		
//		commandLog+= "<br><br>Command:<br><font face='Courier'>" + command.replaceAll(CHALLENGE_REPLACE_REGEX, CHALLENGE_REPLACE_VALUE) + "</font><br>Chiper Used? : " + cipherUsed + ", Response :<br><font face='Courier'>";
//		
//		List<String> commandReply = executeCommand(command);
//		
//		for (String commandReplyLine : commandReply) {
//			commandLog += "    " +commandReplyLine + "<br>";
//		}
//		commandLog+= "</font><br>";
//
//		
//		List<Double> commandResultDoubles = getDoublesFromList(commandResult);
//		
//		if (commandResultDoubles.size() == 0) {
//			LOG.warn("memory caputure has failed ( " + command.replaceAll(CHALLENGE_REPLACE_REGEX, CHALLENGE_REPLACE_VALUE) + " )");
//			//if (LOG.isDebugEnabled()) LOG.debug("commandLog : " + commandLog);		
//			return null;
//		} else {
//			return commandResultDoubles.get(0);
//		}
//	}
//


}

package com.mark59.servermetricsweb.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mark59.core.utils.Mark59Utils;
import com.mark59.servermetricsweb.controller.ServerMetricRestController;
import com.mark59.servermetricsweb.data.beans.Command;
import com.mark59.servermetricsweb.data.beans.CommandParserLink;
import com.mark59.servermetricsweb.data.beans.CommandResponseParser;
import com.mark59.servermetricsweb.data.beans.ServerCommandLink;
import com.mark59.servermetricsweb.data.beans.ServerProfile;
import com.mark59.servermetricsweb.data.commandResponseParsers.dao.CommandResponseParsersDAO;
import com.mark59.servermetricsweb.data.commandparserlinks.dao.CommandParserLinksDAO;
import com.mark59.servermetricsweb.data.commands.dao.CommandsDAO;
import com.mark59.servermetricsweb.data.servercommandlinks.dao.ServerCommandLinksDAO;
import com.mark59.servermetricsweb.data.serverprofiles.dao.ServerProfilesDAO;
import com.mark59.servermetricsweb.drivers.CommandDriver;
import com.mark59.servermetricsweb.pojos.CommandDriverResponse;
import com.mark59.servermetricsweb.pojos.ParsedCommandResponse;
import com.mark59.servermetricsweb.pojos.WebServerMetricsResponsePojo;

public class TargetServerFunctions {

	private static final Logger LOG = LogManager.getLogger(ServerMetricRestController.class);	

	private static final String indent = "<br>&nbsp;&nbsp;&nbsp;&nbsp;";
	private static int parsingSuccessCount;
	private static int parsingFailureCount;
		
	/**
	 * @param reqServerProfileName
	 * @param reqTestMode
	 * @return
	 */
	public static WebServerMetricsResponsePojo serverResponse(String reqServerProfileName, String reqTestMode,
			ServerProfilesDAO serverProfilesDAO, ServerCommandLinksDAO serverCommandLinksDAO, CommandsDAO commandsDAO,
			CommandParserLinksDAO commandParserLinksDAO, CommandResponseParsersDAO commandResponseParsersDAO) {
		
		LOG.debug("TargetServerFunctions apiMetric profile =" + reqServerProfileName);

		WebServerMetricsResponsePojo response = new WebServerMetricsResponsePojo();
		response.setServerProfileName(reqServerProfileName);
		response.setLogLines("");
		List<String> logLines = new ArrayList<String>();
		parsingSuccessCount = 0;
		parsingFailureCount = 0;		
		
		try {
	
			ServerProfile serverProfile = serverProfilesDAO.findServerProfile(reqServerProfileName);
			boolean testMode = Mark59Utils.resovesToTrue(reqTestMode);
			
			if (serverProfile == null ) {
				response.setServerProfileName(reqServerProfileName); 
				response.setFailMsg(AppConstantsServerMetricsWeb.SERVER_PROFILE_NOT_FOUND + " (" + reqServerProfileName + ")" ); 
				//return ResponseEntity.ok(response);
				return response;
			}
			// if possible determine the 'localhost' operating system and override the provided value 
			if ( "localhost".equalsIgnoreCase(serverProfile.getServer())) {
				String localhostOS = ServerMetricsWebUtils.obtainOperatingSystemForLocalhost();
				if (! AppConstantsServerMetricsWeb.UNKNOWN.equalsIgnoreCase(localhostOS)) {
					serverProfile.setOperatingSystem(localhostOS);
				}
			}
			
			response.setServerProfileName(reqServerProfileName);
			response.setServer(serverProfile.getServer());
			response.setAlternativeServerId(serverProfile.getAlternativeServerId());
			response.setReportedServerId(CommandDriver.obtainReportedServerId(serverProfile.getServer(),serverProfile.getAlternativeServerId()));
			response.setFailMsg("");
			
			
			CommandDriver driver =  CommandDriver.init(serverProfile);	
			List<ParsedCommandResponse> parsedCommandResponses = new ArrayList<ParsedCommandResponse>();
			List<ServerCommandLink> serverCommandLinks = serverCommandLinksDAO.findServerCommandLinksForServerProfile(serverProfile.getServerProfileName());  
			
			for (ServerCommandLink serverCommandLink : serverCommandLinks) {      		// execute each command linked to the server profile
			
				Command command = commandsDAO.findCommand(serverCommandLink.getCommandName());
				
				CommandDriverResponse commandDriverResponse = driver.executeCommand(command);
	
				logLines.add("<b><a href=./editCommand?&reqCommandName=" + command.getCommandName() + ">" + command.getCommandName() + "</a></b> command invoked");
				if (commandDriverResponse.isCommandFailure()){
					LOG.warn("server profile " + reqServerProfileName +" command " + command.getCommandName() + " has failed. Command Log : " 
							+ commandDriverResponse.getCommandLog() + ".");
					logLines.add("<font color='red'><b>.  Execution has errored. </b></font> " );
				}
				logLines.add(commandDriverResponse.getCommandLog());	
		
				List<CommandParserLink> commandParserLinks = commandParserLinksDAO.findCommandParserLinksForCommand(command.getCommandName());
				
				for (CommandParserLink commandParserLink : commandParserLinks) {		// run thru each parser linked to the executed command
				
					CommandResponseParser commandResponseParser = commandResponseParsersDAO.findCommandResponseParser(commandParserLink.getScriptName()); 
					
					ParsedCommandResponse parsedCommandResponse = parseCommandResponse( commandResponseParser,
																						commandDriverResponse,
																					    reqServerProfileName, 
																						command.getCommandName(), 
																					    response);
					if (testMode) {
						logLines.addAll(logParsedCommandResponse(parsedCommandResponse, commandParserLink.getScriptName()));
					}
					parsedCommandResponses.add(parsedCommandResponse);
				}
				logLines.add("<br>");
			}
	
			response.setParsedCommandResponses(parsedCommandResponses);
			response.setLogLines(String.join("", logLines));			
			response.setTestModeResult(summariseResponse(testMode));
		
		} catch (Exception e) {
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			String failureMsg = "Error: Unexpected Failure calling the Server Metrics Service. \n" +
								"reqServerProfileName : " + reqServerProfileName + "\n" +
								e.getMessage() + "\n" + stackTrace.toString();
			response.setFailMsg(failureMsg);
			response.setLogLines(response.getLogLines() + failureMsg.replaceAll("\\R", "<br>"));
			response.setTestModeResult("<font color='red'>Error: Unexpected Failure calling the Server Metrics Service.</font>");
			LOG.warn(failureMsg);
			LOG.debug("    loglines : " + response.getLogLines());
		};
		LOG.debug("<< response : " + response);
		return response;
	}


	private static ParsedCommandResponse parseCommandResponse(CommandResponseParser commandResponseParser, CommandDriverResponse commandDriverResponse, 
			String serverProfileName, String commandName, WebServerMetricsResponsePojo response) {

		ParsedCommandResponse parsedCommandResponse = new ParsedCommandResponse();
		
		String commandResponseAsString = ServerMetricsWebUtils.createMultiLineLiteral(commandDriverResponse.getRawCommandResponseLines());
		parsedCommandResponse.setCommandResponse(commandResponseAsString);
		
		parsedCommandResponse.setCommandName(commandName);
		parsedCommandResponse.setScriptName(commandResponseParser.getScriptName());
		parsedCommandResponse.setMetricTxnType(commandResponseParser.getMetricTxnType());
		parsedCommandResponse.setMetricNameSuffix(commandResponseParser.getMetricNameSuffix());
		parsedCommandResponse.setCandidateTxnId(Mark59Utils.constructCandidateTxnIdforMetric(
														commandResponseParser.getMetricTxnType(),
														response.getReportedServerId(),
														commandResponseParser.getMetricNameSuffix()));
		
		if (commandDriverResponse.isCommandFailure()) {
			parsingFailureCount++;
			parsedCommandResponse.setTxnPassed("N");					
			parsedCommandResponse.setParsedCommandResponse("Command Failure." + "\n" + commandResponseAsString +  "\n");
			response.setFailMsg(response.getFailMsg() + "Error : " + commandName + " command execution failure. "
									+ " Parser " + commandResponseParser.getScriptName() + "bypassed\n"); 
		} else {

			try {
				
				Object groovyScriptResult = ServerMetricsWebUtils.runGroovyScript(commandResponseParser.getScript(), commandResponseAsString);
	
				try {
					
					Double.parseDouble(groovyScriptResult.toString());
					
					parsingSuccessCount++;
					parsedCommandResponse.setTxnPassed("Y");
					parsedCommandResponse.setParsedCommandResponse(groovyScriptResult.toString());	
					
				} catch (Exception pe) {
					parsingFailureCount++;
					parsedCommandResponse.setTxnPassed("N");
					parsedCommandResponse.setParsedCommandResponse(	
							"Error : Script parsing failure.  A valid numeric was not returned : [" + groovyScriptResult + "].\n" +  
							"Serverprofile : " + serverProfileName +
							"Command  : " + commandName +
							"Parser : " + commandResponseParser.getScriptName() + "\n" +
							"Command Response : " + "\n" + commandResponseAsString +  "\n" +
							"Error Msg : " + pe.getMessage());
					response.setFailMsg(response.getFailMsg() +
							"Error : " + commandResponseParser.getScriptName() + " Script parsing failure\n" );
					LOG.warn(parsedCommandResponse.getParsedCommandResponse());
				}
			
			} catch (Exception e) {
				parsingFailureCount++;
				StringWriter stackTrace = new StringWriter();
				e.printStackTrace(new PrintWriter(stackTrace));
				
				parsedCommandResponse.setTxnPassed("N");
				parsedCommandResponse.setParsedCommandResponse(
						"Error: Script parser failure.  Scrpt has failed to processes a command response.\n" +
						"Serverprofile : " + serverProfileName +
						"Command  : " + commandName +
						"Parser : " + commandResponseParser.getScriptName() + "\n" +
						"Command Response : " + "\n" + commandResponseAsString + "\n" +
						e.getMessage() + "\n" + stackTrace.toString());
				response.setFailMsg(response.getFailMsg() +
						"Error: " + commandResponseParser.getScriptName() + " parser failed to processes command response.\n");
				LOG.warn(parsedCommandResponse.getParsedCommandResponse());
			}
		}
		return parsedCommandResponse;
	}
			

	private static List<String> logParsedCommandResponse(ParsedCommandResponse parsedCommandResponse, String scriptName) {
		List<String> logLines = new ArrayList<String>();
		String bypassedORinoked = "Y".equals(parsedCommandResponse.getTxnPassed()) ? "invoked" : "bypassed";
		logLines.add(indent + "<b><a href=./viewCommandResponseParser?&reqScriptName=" + scriptName + ">" + scriptName
				+ "</a></b> parser " + bypassedORinoked);					
		logLines.add(indent + "Transaction : " +  parsedCommandResponse.getCandidateTxnId());
		logLines.add(indent + "Value       : " +  parsedCommandResponse.getParsedCommandResponse());
		
		String txnPassedFormatted = "<font color='red'><b>Fail</b></font><br>";
		if ("Y".equals(parsedCommandResponse.getTxnPassed())){ 
			txnPassedFormatted = "<font color='green'>Pass</font><br>";
		}
		logLines.add(indent + "Pass/Fail :  " +  txnPassedFormatted);
		return logLines;
	}


	private static String summariseResponse(boolean testMode) {
		String testModeResult= "";
		if (testMode){
			if (parsingSuccessCount == 0){
				testModeResult ="<font color='red'> You have not received any metrics back!  Please check your connectivity and other settings.</font>";
			} else if (parsingFailureCount > 0) {
				testModeResult = "<font color='orange'> " + parsingFailureCount + " out of " + (parsingSuccessCount+parsingFailureCount) +
						" attempts to parse a command repsonse have failed.</font>";	
			} else {
				testModeResult ="<font color='green'> You have received metrics results!  Please check the values are as you expect.</font>";
			}
		}	
		return testModeResult;
	}

}

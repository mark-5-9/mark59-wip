<!-- Copyright 2019 Insurance Australia Group Limited
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License. 
  
  Author:  Philip Webb
  Date:    Australian Summer 2020
  -->
  
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<title>Server Metrics Via Web - View and Test Command Response Parser</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
<script type="text/javascript" src="javascript/sharedFunctions.js"></script>

<style>
table { border-collapse: collapse;}
.cb { border: 0px }
.cb th { font-size: 14px; color: white; background-color: maroon; border: 1px solid maroon; text-align: left;}
.cb td { border: 1px solid maroon;}
.readonly { background-color: cornsilk; border: 0px solid white;}
</style>

</head>

<body onload="hideElement('testCommandResponseTable')"> 

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 

  <h1>View and Test Command Response Parser</h1> 
  <br> 
  <div>

   <table>
     
    <tr><td width="9%">Parser&nbsp;Name</td>			 <td>:</td><td id='parserName' width="85%">${map.commandResponseParser.parserName}</td></tr>
    <tr><td width="9%">Metric&nbsp;Type</td> 			 <td>:</td><td width="90%">${map.commandResponseParser.metricTxnType}</td></tr>
    <tr><td width="9%">Metric&nbsp;Name&nbsp;Suffix</td><td>:</td><td width="90%">${map.commandResponseParser.metricNameSuffix}</td></tr>
    <tr><td></td><td></td><td></td><tr>	
    <tr><td width="9%">Script</td>						 <td> </td><td width="90%"><textarea class="readonly" readonly style="width:100%;height:200px">${map.commandResponseParser.script}</textarea></td></tr>
    <tr><td width="9%">Comment</td>   					 <td> </td><td width="90%"><textarea class="readonly" readonly style="width:100%;height:20px" >${map.commandResponseParser.comment}</textarea></td></tr>
    <tr><td width="9%">Sample&nbsp;Response</td>  		 <td> </td><td width="90%"><textarea class="readonly" readonly style="width:100%;height:50px" >${map.commandResponseParser.sampleCommandResponse}</textarea></td><tr>

 
	<tr><td colspan="3"><td><br><br></tr>
    <tr>
      <td><button type="button" onclick="testCommandResponseParser()">Test Parser</button></td><td></td><td id='testCommandResponseParserSummary'>... click to test your parser against the sample response</td>
    </tr>
    <tr><td colspan="3"><td><br><br></tr>
    
    <tr>
      <td></td><td></td>	
	  <td>	   
		<table id='testCommandResponseTable' class="cb"> 
 		    <tr><th colspan="2">Command Parser Result</th></tr>
			<tr><td>Txn Id<span style="font-size: 12px"><br>(partial)</span></td>
			    <td id='testCommandResponseParserCandidateTxnId'></td></tr>
		    <tr><td>Result</td>
		    	<td id='testCommandResponseParserResult'></td></tr>
		 </table>	  	   
	  </td>
	</tr>

	<tr><td colspan="3"><td><br></tr>
    <tr>
      <td colspan="3">
        <a href="commandResponseParserList?reqMetricTxnType=${map.reqMetricTxnType}">Command Parsers List</a>&nbsp;
        <a href="editCommandResponseParser?&reqParserName=${map.commandResponseParser.parserName}&reqMetricTxnType=${map.reqMetricTxnType}">Edit Command Parser</a>
      </td>
    </tr>
   </table>
     
  </div>
</div>

</body>
</html>

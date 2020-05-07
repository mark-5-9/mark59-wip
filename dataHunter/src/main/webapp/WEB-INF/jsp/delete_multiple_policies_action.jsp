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
  Date:    Australian Winter 2019
  -->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<title>Delete Multiple Policies</title>
<link rel="shortcut icon"  href="favicon.png" />
<style>
  body { font-size: 20px; color: purple; font-family: Calibri; }
  table.metricsTable  { width: 100%; border-collapse: collapse; }
  table.metricsTable th { font-size: 18px; color: white;   background-color: purple; border: 1px solid #9344BB; padding: 3px 7px 2px 7px; text-align: left; }
  table.metricsTable td { font-size: 15px; color: #000000; background-color: white;  border: 1px solid #9344BB; padding: 3px 7px 2px 7px; }
</style>

</head>
<body>
<center>
  
 <br><br><br>
 
 <b>Delete Multiple Policies</b> 
 <br><br>
  
 <table >
     <tr><td>application </td>	<td>:</td><td>${policySelectionCriteria.application}</td></tr>
     <tr><td>lifecycle </td>	<td>:</td><td>${policySelectionCriteria.lifecycle}</td></tr>
     <tr><td>useability </td>	<td>:</td><td>${policySelectionCriteria.useability}</td></tr>     
 </table>

</center>

<br><br><br><br>

 <table >
     <tr><td>sql statement</td>	<td>:</td><td id=sql>${model.sql}</td></tr> 
     <tr><td>result</td>		<td>:</td><td id=sqlResult>${model.sqlResult}</td></tr>
     <tr><td>rows affected</td>	<td>:</td><td id=rowsAffected>${model.rowsAffected}</td></tr>
     <tr><td>details</td>		<td>:</td><td id=sqlResultText>${model.sqlResultText}</td></tr>     
 </table>

 <br><br><br><br>
 
 <a href='delete_multiple_policies?application=${policySelectionCriteria.application}'>Back</a>
 
</body>
</html>

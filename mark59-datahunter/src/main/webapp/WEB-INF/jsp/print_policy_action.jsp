<%-- Copyright 2019 Mark59.com
 
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
  --%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<html>
<head>
<title>Display Item</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
</head>
<body>
<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />
<div class="content">

 <h1>Display Item</h1>   		 
  
 <table >
   <tr>
    <td>Application </td><td>:</td>
    <td id=application>${policies.application}</td>
   </tr>
   <tr>
    <td>Identifier </td><td>:</td>
    <td id=identifier>${policies.identifier}</td>
   </tr>      
   <tr>
    <td>Lifecycle </td><td>:</td>
    <td id=lifecycle>${policies.lifecycle}</td>
   </tr> 
   <tr>
    <td>Useability </td><td>:</td>
    <td id=useability>${policies.useability}</td>
   </tr> 
   <tr>
    <td>Otherdata </td><td>:</td>
    <td id=otherdata>${policies.otherdata}</td>
   </tr>    
   <tr>
    <td>Created </td><td>:</td>
    <td id=created>${policies.created}</td>
   </tr>
   <tr>
    <td>Updated </td><td>:</td>
    <td id=updated>${policies.updated}</td>
   </tr>         
   <tr>
    <td>Epochtime </td><td>:</td>
    <td id=epochtime>${policies.epochtime}</td>
   </tr>         
  </table>
 
  <br><br>
  <table class="tip">
      <tr><td>sql&nbsp;statement</td><td>{</td><td id=sql>${model.sql}</td></tr> 
      <tr><td>result</td>			<td>:</td><td id=sqlResult>${model.sqlResult}</td></tr>
      <tr><td>rows&nbsp;affected</td><td>:</td><td id=rowsAffected>${model.rowsAffected}</td></tr>
      <tr><td>details</td>			<td>:</td><td id=sqlResultText>${model.sqlResultText}</td></tr>     
  </table>

 <br><a href='print_policy?${model.navUrParms}'>Back</a>
</div>  
</body>
</html>

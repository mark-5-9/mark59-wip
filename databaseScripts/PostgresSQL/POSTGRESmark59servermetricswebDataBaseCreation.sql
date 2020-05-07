
-- >  comment/uncomment as required 
CREATE USER admin SUPERUSER PASSWORD 'admin';
-- DROP DATABASE mark59servermetricswebdb;
CREATE DATABASE mark59servermetricswebdb WITH ENCODING='UTF8' OWNER=admin TEMPLATE=template0 LC_COLLATE='C' LC_CTYPE='C';
-- <

--   The utf8/C ecoding/collation is more in line with other mark59 database options (and how Java/JS sorts work). 
--   if you use the pgAdmin tool to load data, remember to hit the 'commit' icon to save the changes! 


CREATE TABLE IF NOT EXISTS SERVERPROFILES  (
   SERVER_PROFILE_NAME  varchar(64) NOT NULL,
   SERVER  varchar(64) NOT NULL,
   ALTERNATE_SERVER_ID  varchar(64) DEFAULT '',
   USERNAME  varchar(64) DEFAULT '',
   PASSWORD  varchar(64) DEFAULT '',
   PASSWORD_CIPHER  varchar(64) DEFAULT '',
   OPERATING_SYSTEM  varchar(8) NOT NULL,
   CONNECTION_PORT  varchar(8) DEFAULT '',
   CONNECTION_TIMEOUT  varchar(8) DEFAULT '',
   COMMENT  varchar(128) DEFAULT NULL,
  PRIMARY KEY ( SERVER_PROFILE_NAME )
); 


CREATE TABLE IF NOT EXISTS COMMANDS  (
   COMMAND_NAME  varchar(64) NOT NULL,
   EXECUTOR  varchar(16) NOT NULL,
   COMMAND  varchar(4096) NOT NULL,
   IGNORE_STDERR  varchar(1) DEFAULT NULL,
   COMMENT  varchar(128) DEFAULT NULL,
  PRIMARY KEY ( COMMAND_NAME )
); 


CREATE TABLE IF NOT EXISTS SERVERCOMMANDLINKS  (
   SERVER_PROFILE_NAME  varchar(64) NOT NULL,
   COMMAND_NAME  varchar(64) NOT NULL,
  PRIMARY KEY ( SERVER_PROFILE_NAME , COMMAND_NAME )
);


CREATE TABLE IF NOT EXISTS COMMANDRESPONSEPARSERS  (
   SCRIPT_NAME  varchar(64) NOT NULL,
   METRIC_TXN_TYPE  varchar(64) NOT NULL,
   METRIC_NAME_SUFFIX  varchar(64) NOT NULL,
   SCRIPT  varchar(4096) NOT NULL,
   COMMENT  varchar(1024) NOT NULL,
   SAMPLE_COMMAND_RESPONSE  varchar(1024) NOT NULL,
  PRIMARY KEY ( SCRIPT_NAME )
); 


CREATE TABLE IF NOT EXISTS COMMANDPARSERLINKS  (
   COMMAND_NAME  varchar(64) NOT NULL,
   SCRIPT_NAME  varchar(64) NOT NULL,
  PRIMARY KEY ( COMMAND_NAME , SCRIPT_NAME )
); 




INSERT INTO COMMANDPARSERLINKS VALUES ('DataHunterSeleniumDeployAndExecute','Return1'),('DataHunterSeleniumGenJmeterReport','Return1'),('DataHunterSeleniumRunCheckTrendAnalysis','Return1'),('FreePhysicalMemory','Memory_FreePhysicalG'),('FreeVirtualMemory','Memory_FreeVirtualG'),('LINUX_free_m_1_1','LINUX_Memory_freeG'),('LINUX_free_m_1_1','LINUX_Memory_totalG'),('LINUX_free_m_1_1','LINUX_Memory_usedG'),('LINUX_mpstat_1_1','Nix_CPU_Idle'),('UNIX_lparstat_5_1','Nix_CPU_Idle'),('UNIX_Memory_Script','UNIX_Memory_numperm_percent'),('UNIX_Memory_Script','UNIX_Memory_pgsp_aggregate_util'),('UNIX_Memory_Script','UNIX_Memory_pinned_percent'),('UNIX_VM_Memory','UNIX_Memory_numperm_percent'),('UNIX_VM_Memory','UNIX_Memory_pgsp_aggregate_util'),('UNIX_VM_Memory','UNIX_Memory_pinned_percent'),('WinCpuCmd','WicnCpu');

INSERT INTO COMMANDS VALUES ('DataHunterSeleniumDeployAndExecute','WMIC_WINDOWS','process call create ''cmd.exe /c 
 echo Running Directly From Server Metrics Web (cmd DataHunterSeleniumDeployAndExecute) & 
 echo MARK59_SERVER_METRICS = %MARK59_SERVER_METRICS% & 
 cd /D %MARK59_SERVER_METRICS% &  
 cd ..\dataHunterPerformanceTestSamples & 
 DEL C:\apache-jmeter\bin\mark59.properties & COPY .\mark59.properties C:\apache-jmeter\bin &
 DEL C:\apache-jmeter\bin\chromedriver.exe  & COPY .\chromedriver.exe  C:\apache-jmeter\bin &
 DEL C:\apache-jmeter\lib\ext\mark59-server-metrics.jar &
 COPY ..\mark59-server-metrics\target\mark59-server-metrics.jar  C:\apache-jmeter\lib\ext & 
 DEL C:\apache-jmeter\lib\ext\dataHunterPerformanceTestSamples.jar & 
 COPY .\target\dataHunterPerformanceTestSamples.jar  C:\apache-jmeter\lib\ext & 

 mkdir C:\Mark59_Runs &
 mkdir C:\Mark59_Runs\Jmeter_Results &
 mkdir C:\Mark59_Runs\Jmeter_Results\DataHunter &

 set path=%path%;C:\Windows\System32;C:\windows\system32\wbem & 
 cd /D C:\apache-jmeter\bin &

 echo Starting JMeter DataHunter test ... &  

 jmeter -n -X -f 
     -t %MARK59_SERVER_METRICS%\..\dataHunterPerformanceTestSamples\test-plans\DataHunterSeleniumTestPlan.jmx 
     -l C:\Mark59_Runs\Jmeter_Results\DataHunter\DataHunterTestResults.csv 
     -j %MARK59_SERVER_METRICS%\..\bin\jmeter.log 
     -JForceTxnFailPercent=0 
     -JDataHunterUrlHostPort=http://localhost:8081 &  

 PAUSE
''
','N','refer DeployDataHunterTestArtifactsToJmeter.bat and DataHunterExecuteJmeterTest.bat in dataHunterPerformanceTestSamples '),('DataHunterSeleniumGenJmeterReport','WMIC_WINDOWS','process call create ''cmd.exe /c 
 cd /D %MARK59_SERVER_METRICS% & 
 cd../resultFilesConverter & 
 CreateDataHunterJmeterReports.bat''
','N',''),('DataHunterSeleniumRunCheckTrendAnalysis','WMIC_WINDOWS','process call create ''cmd.exe /c 
 echo Load DataHunter Test Results into  Mark59 Metrics (Trend Analysis) h2 database. & 
 cd /D %MARK59_SERVER_METRICS% & 
 cd ../metricsRuncheck &  
 
 java -jar ./target/metricsRuncheck.jar -a DataHunter -i C:\Mark59_Runs\Jmeter_Results\DataHunter -d h2 &
 PAUSE
''
','N',''),('FreePhysicalMemory','WMIC_WINDOWS','OS get FreePhysicalMemory','N',''),('FreeVirtualMemory','WMIC_WINDOWS','OS get FreeVirtualMemory','N',''),('LINUX_free_m_1_1','SSH_LINIX_UNIX','free -m 1 1','N','linux memory'),('LINUX_mpstat_1_1','SSH_LINIX_UNIX','mpstat 1 1','N',''),('UNIX_lparstat_5_1','SSH_LINIX_UNIX','lparstat 5 1','N',''),('UNIX_Memory_Script','SSH_LINIX_UNIX','vmstat=$(vmstat -v); 
let total_pages=$(print "$vmstat" | grep ''memory pages'' | awk ''{print $1}''); 
let pinned_pages=$(print "$vmstat" | grep ''pinned pages'' | awk ''{print $1}''); 
let pinned_percent=$(( $(print "scale=4; $pinned_pages / $total_pages " | bc) * 100 )); 
let numperm_pages=$(print "$vmstat" | grep ''file pages'' | awk ''{print $1}''); 
let numperm_percent=$(print "$vmstat" | grep ''numperm percentage'' | awk ''{print $1}''); 
pgsp_utils=$(lsps -a | tail +2 | awk ''{print $5}''); 
let pgsp_num=$(print "$pgsp_utils" | wc -l | tr -d '' ''); 
let pgsp_util_sum=0; 
for pgsp_util in $pgsp_utils; do let pgsp_util_sum=$(( $pgsp_util_sum + $pgsp_util )); done; 
pgsp_aggregate_util=$(( $pgsp_util_sum / $pgsp_num )); 
print "${pinned_percent},${numperm_percent},${pgsp_aggregate_util}"','N',''),('UNIX_VM_Memory','SSH_LINIX_UNIX','vmstat=$(vmstat -v); 
let total_pages=$(print "$vmstat" | grep ''memory pages'' | awk ''{print $1}''); 
let pinned_pages=$(print "$vmstat" | grep ''pinned pages'' | awk ''{print $1}''); 
let pinned_percent=$(( $(print "scale=4; $pinned_pages / $total_pages " | bc) * 100 )); 
let numperm_pages=$(print "$vmstat" | grep ''file pages'' | awk ''{print $1}''); 
let numperm_percent=$(print "$vmstat" | grep ''numperm percentage'' | awk ''{print $1}''); 
pgsp_utils=$(lsps -a | tail +2 | awk ''{print $5}''); 
let pgsp_num=$(print "$pgsp_utils" | wc -l | tr -d '' ''); 
let pgsp_util_sum=0; 
for pgsp_util in $pgsp_utils; do let pgsp_util_sum=$(( $pgsp_util_sum + $pgsp_util )); done; 
pgsp_aggregate_util=$(( $pgsp_util_sum / $pgsp_num )); 
print "${pinned_percent},${numperm_percent},${pgsp_aggregate_util}"','N',''),('WinCpuCmd','WMIC_WINDOWS','cpu get loadpercentage','N','');



INSERT INTO SERVERCOMMANDLINKS VALUES ('DemoWIN-DataHunter-Selenium-DeployAndExecute','DataHunterSeleniumDeployAndExecute'),('DemoWIN-DataHunter-Selenium-GenJmeterReport','DataHunterSeleniumGenJmeterReport'),('DemoWIN-DataHunter-Selenium-metricsRunCheck','DataHunterSeleniumRunCheckTrendAnalysis'),('localhost','FreePhysicalMemory'),('localhost','FreeVirtualMemory'),('localhost','WinCpuCmd'),('localhost,localhost','WinCpuCmd'),('localhost_HOSTID','FreePhysicalMemory'),('localhost_HOSTID','FreeVirtualMemory'),('localhost_HOSTID','WinCpuCmd'),('remoteLinuxServer','LINUX_free_m_1_1'),('remoteLinuxServer','LINUX_mpstat_1_1'),('remoteUnixVM','UNIX_lparstat_5_1'),('remoteUnixVM','UNIX_Memory_Script'),('remoteWinServer','FreePhysicalMemory'),('remoteWinServer','FreeVirtualMemory'),('remoteWinServer','WinCpuCmd'),('rubbishnew,rubbishnew','WinCpuCmd');




INSERT INTO SERVERPROFILES VALUES ('DemoWIN-DataHunter-Selenium-DeployAndExecute','localhost','','','','','WINDOWS','','',''),('DemoWIN-DataHunter-Selenium-GenJmeterReport','localhost','','','','','WINDOWS','','',''),('DemoWIN-DataHunter-Selenium-metricsRunCheck','localhost','','','','','WINDOWS','','',''),('localhost','localhost','','','','','WINDOWS','','',''),('localhost_HOSTID','localhost','HOSTID','','','','WINDOWS','','','using HOSTID for local  '),('remoteLinuxServer','LinuxServerName','','userid','encryptMe','','LINUX','22','60000',''),('remoteUnixVM','UnixVMName','','userid','encryptMe','','UNIX','22','60000',''),('remoteWinServer','WinServerName','','userid','encryptMe','','WINDOWS','','','');




INSERT INTO COMMANDRESPONSEPARSERS VALUES ('LINUX_Memory_freeG','MEMORY','freeG','import org.apache.commons.lang3.StringUtils;
// ---
String targetColumnName= "free";              
String targetRowName= "Mem:";  
// ---
String extractedMetric = "-3";

if (StringUtils.isNotBlank(commandResponse)) {
    String wordsOnThisResultLine = commandResponse.replace("\n", " ").replace("\r", " ");
    ArrayList<String> cmdResultLine = new ArrayList<>(
         Arrays.asList(wordsOnThisResultLine.trim().split("\\s+")));

    if (cmdResultLine.contains(targetColumnName)) {
	extractedMetric = cmdResultLine
		.get(cmdResultLine.indexOf(targetRowName) + cmdResultLine.indexOf(targetColumnName) + 1);
    }
}
return Math.round(Double.parseDouble(extractedMetric) / 1000 );
','','              total        used        free      shared  buff/cache   available
Mem:          28798       14043         561        1412       14392       12953
Swap:             0           0           0
');

INSERT INTO COMMANDRESPONSEPARSERS VALUES ('LINUX_Memory_totalG','MEMORY','totalG','import org.apache.commons.lang3.StringUtils;
// ---
String targetColumnName= "total";              
String targetRowName= "Mem:";  
// ---
String extractedMetric = "-3";

if (StringUtils.isNotBlank(commandResponse)) {
    String wordsOnThisResultLine = commandResponse.replace("\n", " ").replace("\r", " ");
    ArrayList<String> cmdResultLine = new ArrayList<>(
         Arrays.asList(wordsOnThisResultLine.trim().split("\\s+")));

    if (cmdResultLine.contains(targetColumnName)) {
	extractedMetric = cmdResultLine
		.get(cmdResultLine.indexOf(targetRowName) + cmdResultLine.indexOf(targetColumnName) + 1);
    }
}
return Math.round(Double.parseDouble(extractedMetric) / 1000 );
','','              total        used        free      shared  buff/cache   available
Mem:          28798       14043         361        1412       14392       12953
Swap:             0           0           0
');

INSERT INTO COMMANDRESPONSEPARSERS VALUES ('LINUX_Memory_usedG','MEMORY','usedG','import org.apache.commons.lang3.StringUtils;
// ---
String targetColumnName= "used";              
String targetRowName= "Mem:";  
// ---
String extractedMetric = "-3";

if (StringUtils.isNotBlank(commandResponse)) {
    String wordsOnThisResultLine = commandResponse.replace("\n", " ").replace("\r", " ");
    ArrayList<String> cmdResultLine = new ArrayList<>(
         Arrays.asList(wordsOnThisResultLine.trim().split("\\s+")));

    if (cmdResultLine.contains(targetColumnName)) {
	extractedMetric = cmdResultLine
		.get(cmdResultLine.indexOf(targetRowName) + cmdResultLine.indexOf(targetColumnName) + 1);
    }
}
return Math.round(Double.parseDouble(extractedMetric) / 1000 );
','','              total        used        free      shared  buff/cache   available
Mem:          28798       14043         361        1412       14392       12953
Swap:             0           0           0
');


INSERT INTO COMMANDRESPONSEPARSERS VALUES ('Memory_FreePhysicalG','MEMORY','FreePhysicalG','Math.round(Double.parseDouble(commandResponse.replaceAll("[^\\d.]", "")) / 1000000 )','','FreePhysicalG
22510400');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('Memory_FreeVirtualG','MEMORY','FreeVirtualG','Math.round(Double.parseDouble(commandResponse.replaceAll("[^\\d.]", "")) / 1000000 )','','FreeVirtualMemory
22510400');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('Nix_CPU_Idle','CPU_UTIL','','import org.apache.commons.lang3.ArrayUtils;
// ---
String targetColumnName = "%idle"              
String targetmetricFormat = "\\d*\\.?\\d+"   // a decimal format  
// ---
String extractedMetric = "-1";
int colNumberOfTargetColumnName = -1;
String[] commandResultLine = commandResponse.trim().split("\\r\\n|\\n|\\r");

for (int i = 0; i < commandResultLine.length && "-1".equals(extractedMetric); i++) {

    String[] wordsOnThiscommandResultsLine = commandResultLine[i].trim().split("\\s+");

    if (colNumberOfTargetColumnName > -1
  	&& wordsOnThiscommandResultsLine[colNumberOfTargetColumnName].matches(targetmetricFormat)) {
	extractedMetric = wordsOnThiscommandResultsLine[colNumberOfTargetColumnName];
    }
    if (colNumberOfTargetColumnName == -1) { // column name not yet found, so see if it is on this line ...
	colNumberOfTargetColumnName = ArrayUtils.indexOf(wordsOnThiscommandResultsLine, targetColumnName);
    }
}
return extractedMetric;
','This works on data in a simple column format (eg unix lparstat and linux mpstat cpu). It will return the first matching value it finds in the column requested.','System configuration: type=Shared mode=Uncapped smt=4 lcpu=4 mem=47104MB psize=60 ent=0.50 

%user  %sys  %wait  %idle physc %entc  lbusy   app  vcsw phint  %nsp  %utcyc
----- ----- ------ ------ ----- ----- ------   --- ----- ----- -----  ------
 11.3  15.0    0.0   73.7  0.22  44.5    6.1 45.26   919     0   101   1.39 ');
 
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('Return1','DATAPOINT','','return 1','','any rand junk');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('UNIX_Memory_numperm_percent','MEMORY','numperm_percent','commandResponse.split(",")[1].trim()','','1,35,4');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('UNIX_Memory_pgsp_aggregate_util','MEMORY','pgsp_aggregate_util','commandResponse.split(",")[2].trim()','','1,35,4');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('UNIX_Memory_pinned_percent','MEMORY','pinned_percent','commandResponse.split(",")[0].trim()','','1,35,4');

INSERT INTO COMMANDRESPONSEPARSERS VALUES ('WicnCpu','CPU_UTIL','','java.util.regex.Matcher m = java.util.regex.Pattern.compile("-?[0-9]+").matcher(commandResponse);
Integer sum = 0; 
int count = 0; 
while (m.find()){ 
    sum += Integer.parseInt(m.group()); 
    count++;
}; 
if (count==0) 
    return 0 ; 
else 
    return sum/count;','comment','LoadPercentage
21');



-- DROP DATABASE IF EXISTS mark59servermetricswebdb;
CREATE DATABASE mark59servermetricswebdb CHARACTER SET utf8mb4  COLLATE utf8mb4_bin; 

USE mark59servermetricswebdb;


-- PLEASE RUN  "MYSQLcreateAdminUser.sql"  TO CREATE THE 'ADMIN' USER IF YOU HAVE NOT ALREADY DONE SO.  

-- Note that character set utf8mb4 is the default from MySQL 8.0.
-- The collation for the database is set to utf8mb4_bin
-- Useful at at is allows for stuff like case-sensitive eventmapping matching.  Also aligns H2 database and Java case sensitive sorting.           


-- create tables  -- 


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


-- populate with initial data  -- 


INSERT INTO `COMMANDRESPONSEPARSERS` VALUES ('LINUX_Memory_freeG','MEMORY','freeG','import org.apache.commons.lang3.StringUtils;\r\n// ---\r\nString targetColumnName= \"free\";              \r\nString targetRowName= \"Mem:\";  \r\n// ---\r\nString extractedMetric = \"-3\";\r\n\r\nif (StringUtils.isNotBlank(commandResponse)) {\r\n    String wordsOnThisResultLine = commandResponse.replace(\"\\n\", \" \").replace(\"\\r\", \" \");\r\n    ArrayList<String> cmdResultLine = new ArrayList<>(\r\n         Arrays.asList(wordsOnThisResultLine.trim().split(\"\\\\s+\")));\r\n\r\n    if (cmdResultLine.contains(targetColumnName)) {\r\n	extractedMetric = cmdResultLine\r\n		.get(cmdResultLine.indexOf(targetRowName) + cmdResultLine.indexOf(targetColumnName) + 1);\r\n    }\r\n}\r\nreturn Math.round(Double.parseDouble(extractedMetric) / 1000 );\r\n','','              total        used        free      shared  buff/cache   available\r\nMem:          28798       14043         561        1412       14392       12953\r\nSwap:             0           0           0\r\n'),('LINUX_Memory_totalG','MEMORY','totalG','import org.apache.commons.lang3.StringUtils;\r\n// ---\r\nString targetColumnName= \"total\";              \r\nString targetRowName= \"Mem:\";  \r\n// ---\r\nString extractedMetric = \"-3\";\r\n\r\nif (StringUtils.isNotBlank(commandResponse)) {\r\n    String wordsOnThisResultLine = commandResponse.replace(\"\\n\", \" \").replace(\"\\r\", \" \");\r\n    ArrayList<String> cmdResultLine = new ArrayList<>(\r\n         Arrays.asList(wordsOnThisResultLine.trim().split(\"\\\\s+\")));\r\n\r\n    if (cmdResultLine.contains(targetColumnName)) {\r\n	extractedMetric = cmdResultLine\r\n		.get(cmdResultLine.indexOf(targetRowName) + cmdResultLine.indexOf(targetColumnName) + 1);\r\n    }\r\n}\r\nreturn Math.round(Double.parseDouble(extractedMetric) / 1000 );\r\n','','              total        used        free      shared  buff/cache   available\r\nMem:          28798       14043         361        1412       14392       12953\r\nSwap:             0           0           0\r\n'),('LINUX_Memory_usedG','MEMORY','usedG','import org.apache.commons.lang3.StringUtils;\r\n// ---\r\nString targetColumnName= \"used\";              \r\nString targetRowName= \"Mem:\";  \r\n// ---\r\nString extractedMetric = \"-3\";\r\n\r\nif (StringUtils.isNotBlank(commandResponse)) {\r\n    String wordsOnThisResultLine = commandResponse.replace(\"\\n\", \" \").replace(\"\\r\", \" \");\r\n    ArrayList<String> cmdResultLine = new ArrayList<>(\r\n         Arrays.asList(wordsOnThisResultLine.trim().split(\"\\\\s+\")));\r\n\r\n    if (cmdResultLine.contains(targetColumnName)) {\r\n	extractedMetric = cmdResultLine\r\n		.get(cmdResultLine.indexOf(targetRowName) + cmdResultLine.indexOf(targetColumnName) + 1);\r\n    }\r\n}\r\nreturn Math.round(Double.parseDouble(extractedMetric) / 1000 );\r\n','','              total        used        free      shared  buff/cache   available\r\nMem:          28798       14043         361        1412       14392       12953\r\nSwap:             0           0           0\r\n'),('Memory_FreePhysicalG','MEMORY','FreePhysicalG','Math.round(Double.parseDouble(commandResponse.replaceAll(\"[^\\\\d.]\", \"\")) / 1000000 )','','FreePhysicalG\r\n22510400'),('Memory_FreeVirtualG','MEMORY','FreeVirtualG','Math.round(Double.parseDouble(commandResponse.replaceAll(\"[^\\\\d.]\", \"\")) / 1000000 )','','FreeVirtualMemory\r\n22510400'),('Nix_CPU_Idle','CPU_UTIL','','import org.apache.commons.lang3.ArrayUtils;\r\n// ---\r\nString targetColumnName = \"%idle\"              \r\nString targetmetricFormat = \"\\\\d*\\\\.?\\\\d+\"   // a decimal format  \r\n// ---\r\nString extractedMetric = \"-1\";\r\nint colNumberOfTargetColumnName = -1;\r\nString[] commandResultLine = commandResponse.trim().split(\"\\\\r\\\\n|\\\\n|\\\\r\");\r\n\r\nfor (int i = 0; i < commandResultLine.length && \"-1\".equals(extractedMetric); i++) {\r\n\r\n    String[] wordsOnThiscommandResultsLine = commandResultLine[i].trim().split(\"\\\\s+\");\r\n\r\n    if (colNumberOfTargetColumnName > -1\r\n  	&& wordsOnThiscommandResultsLine[colNumberOfTargetColumnName].matches(targetmetricFormat)) {\r\n	extractedMetric = wordsOnThiscommandResultsLine[colNumberOfTargetColumnName];\r\n    }\r\n    if (colNumberOfTargetColumnName == -1) { // column name not yet found, so see if it is on this line ...\r\n	colNumberOfTargetColumnName = ArrayUtils.indexOf(wordsOnThiscommandResultsLine, targetColumnName);\r\n    }\r\n}\r\nreturn extractedMetric;\r\n','This works on data in a simple column format (eg unix lparstat and linux mpstat cpu). It will return the first matching value it finds in the column requested.','System configuration: type=Shared mode=Uncapped smt=4 lcpu=4 mem=47104MB psize=60 ent=0.50 \r\n\r\n%user  %sys  %wait  %idle physc %entc  lbusy   app  vcsw phint  %nsp  %utcyc\r\n----- ----- ------ ------ ----- ----- ------   --- ----- ----- -----  ------\r\n 11.3  15.0    0.0   73.7  0.22  44.5    6.1 45.26   919     0   101   1.39 '),('Return1','DATAPOINT','','return 1','','any rand junk'),('UNIX_Memory_numperm_percent','MEMORY','numperm_percent','commandResponse.split(\",\")[1].trim()','','1,35,4'),('UNIX_Memory_pgsp_aggregate_util','MEMORY','pgsp_aggregate_util','commandResponse.split(\",\")[2].trim()','','1,35,4'),('UNIX_Memory_pinned_percent','MEMORY','pinned_percent','commandResponse.split(\",\")[0].trim()','','1,35,4'),('WicnCpu','CPU_UTIL','','java.util.regex.Matcher m = java.util.regex.Pattern.compile(\"-?[0-9]+\").matcher(commandResponse);\r\nInteger sum = 0; \r\nint count = 0; \r\nwhile (m.find()){ \r\n    sum += Integer.parseInt(m.group()); \r\n    count++;\r\n}; \r\nif (count==0) \r\n    return 0 ; \r\nelse \r\n    return sum/count;','comment','LoadPercentage\r\n21');

INSERT INTO `COMMANDS` VALUES ('DataHunterSeleniumDeployAndExecute','WMIC_WINDOWS','process call create \'cmd.exe /c \r\n echo Running Directly From Server Metrics Web (cmd DataHunterSeleniumDeployAndExecute) & \r\n echo MARK59_SERVER_METRICS = %MARK59_SERVER_METRICS% & \r\n cd /D %MARK59_SERVER_METRICS% &  \r\n cd ..\\dataHunterPerformanceTestSamples & \r\n DEL C:\\apache-jmeter\\bin\\mark59.properties & COPY .\\mark59.properties C:\\apache-jmeter\\bin &\r\n DEL C:\\apache-jmeter\\bin\\chromedriver.exe  & COPY .\\chromedriver.exe  C:\\apache-jmeter\\bin &\r\n DEL C:\\apache-jmeter\\lib\\ext\\mark59-server-metrics.jar &\r\n COPY ..\\mark59-server-metrics\\target\\mark59-server-metrics.jar  C:\\apache-jmeter\\lib\\ext & \r\n DEL C:\\apache-jmeter\\lib\\ext\\dataHunterPerformanceTestSamples.jar & \r\n COPY .\\target\\dataHunterPerformanceTestSamples.jar  C:\\apache-jmeter\\lib\\ext & \r\n\r\n mkdir C:\\Mark59_Runs &\r\n mkdir C:\\Mark59_Runs\\Jmeter_Results &\r\n mkdir C:\\Mark59_Runs\\Jmeter_Results\\DataHunter &\r\n\r\n set path=%path%;C:\\Windows\\System32;C:\\windows\\system32\\wbem & \r\n cd /D C:\\apache-jmeter\\bin &\r\n\r\n echo Starting JMeter DataHunter test ... &  \r\n\r\n jmeter -n -X -f \r\n     -t %MARK59_SERVER_METRICS%\\..\\dataHunterPerformanceTestSamples\\test-plans\\DataHunterSeleniumTestPlan.jmx \r\n     -l C:\\Mark59_Runs\\Jmeter_Results\\DataHunter\\DataHunterTestResults.csv \r\n     -j %MARK59_SERVER_METRICS%\\..\\bin\\jmeter.log \r\n     -JForceTxnFailPercent=0 \r\n     -JDataHunterUrlHostPort=http://localhost:8081 &  \r\n\r\n PAUSE\r\n\'\r\n','N','refer DeployDataHunterTestArtifactsToJmeter.bat and DataHunterExecuteJmeterTest.bat in dataHunterPerformanceTestSamples '),('DataHunterSeleniumGenJmeterReport','WMIC_WINDOWS','process call create \'cmd.exe /c \r\n cd /D %MARK59_SERVER_METRICS% & \r\n cd../resultFilesConverter & \r\n CreateDataHunterJmeterReports.bat\'\r\n','N',''),('DataHunterSeleniumRunCheckTrendAnalysis','WMIC_WINDOWS','process call create \'cmd.exe /c \r\n echo Load DataHunter Test Results into  Mark59 Metrics (Trend Analysis) h2 database. & \r\n cd /D %MARK59_SERVER_METRICS% & \r\n cd ../metricsRuncheck &  \r\n \r\n java -jar ./target/metricsRuncheck.jar -a DataHunter -i C:\\Mark59_Runs\\Jmeter_Results\\DataHunter -d h2 &\r\n PAUSE\r\n\'\r\n','N',''),('FreePhysicalMemory','WMIC_WINDOWS','OS get FreePhysicalMemory','N',''),('FreeVirtualMemory','WMIC_WINDOWS','OS get FreeVirtualMemory','N',''),('LINUX_free_m_1_1','SSH_LINIX_UNIX','free -m 1 1','N','linux memory'),('LINUX_mpstat_1_1','SSH_LINIX_UNIX','mpstat 1 1','N',''),('UNIX_lparstat_5_1','SSH_LINIX_UNIX','lparstat 5 1','N',''),('UNIX_Memory_Script','SSH_LINIX_UNIX','vmstat=$(vmstat -v); \r\nlet total_pages=$(print \"$vmstat\" | grep \'memory pages\' | awk \'{print $1}\'); \r\nlet pinned_pages=$(print \"$vmstat\" | grep \'pinned pages\' | awk \'{print $1}\'); \r\nlet pinned_percent=$(( $(print \"scale=4; $pinned_pages / $total_pages \" | bc) * 100 )); \r\nlet numperm_pages=$(print \"$vmstat\" | grep \'file pages\' | awk \'{print $1}\'); \r\nlet numperm_percent=$(print \"$vmstat\" | grep \'numperm percentage\' | awk \'{print $1}\'); \r\npgsp_utils=$(lsps -a | tail +2 | awk \'{print $5}\'); \r\nlet pgsp_num=$(print \"$pgsp_utils\" | wc -l | tr -d \' \'); \r\nlet pgsp_util_sum=0; \r\nfor pgsp_util in $pgsp_utils; do let pgsp_util_sum=$(( $pgsp_util_sum + $pgsp_util )); done; \r\npgsp_aggregate_util=$(( $pgsp_util_sum / $pgsp_num )); \r\nprint \"${pinned_percent},${numperm_percent},${pgsp_aggregate_util}\"','N',''),('UNIX_VM_Memory','SSH_LINIX_UNIX','vmstat=$(vmstat -v); \r\nlet total_pages=$(print \"$vmstat\" | grep \'memory pages\' | awk \'{print $1}\'); \r\nlet pinned_pages=$(print \"$vmstat\" | grep \'pinned pages\' | awk \'{print $1}\'); \r\nlet pinned_percent=$(( $(print \"scale=4; $pinned_pages / $total_pages \" | bc) * 100 )); \r\nlet numperm_pages=$(print \"$vmstat\" | grep \'file pages\' | awk \'{print $1}\'); \r\nlet numperm_percent=$(print \"$vmstat\" | grep \'numperm percentage\' | awk \'{print $1}\'); \r\npgsp_utils=$(lsps -a | tail +2 | awk \'{print $5}\'); \r\nlet pgsp_num=$(print \"$pgsp_utils\" | wc -l | tr -d \' \'); \r\nlet pgsp_util_sum=0; \r\nfor pgsp_util in $pgsp_utils; do let pgsp_util_sum=$(( $pgsp_util_sum + $pgsp_util )); done; \r\npgsp_aggregate_util=$(( $pgsp_util_sum / $pgsp_num )); \r\nprint \"${pinned_percent},${numperm_percent},${pgsp_aggregate_util}\"','N',''),('WinCpuCmd','WMIC_WINDOWS','cpu get loadpercentage','N','');

INSERT INTO `COMMANDPARSERLINKS` VALUES ('DataHunterSeleniumDeployAndExecute','Return1'),('DataHunterSeleniumGenJmeterReport','Return1'),('DataHunterSeleniumRunCheckTrendAnalysis','Return1'),('FreePhysicalMemory','Memory_FreePhysicalG'),('FreeVirtualMemory','Memory_FreeVirtualG'),('LINUX_free_m_1_1','LINUX_Memory_freeG'),('LINUX_free_m_1_1','LINUX_Memory_totalG'),('LINUX_free_m_1_1','LINUX_Memory_usedG'),('LINUX_mpstat_1_1','Nix_CPU_Idle'),('UNIX_lparstat_5_1','Nix_CPU_Idle'),('UNIX_Memory_Script','UNIX_Memory_numperm_percent'),('UNIX_Memory_Script','UNIX_Memory_pgsp_aggregate_util'),('UNIX_Memory_Script','UNIX_Memory_pinned_percent'),('UNIX_VM_Memory','UNIX_Memory_numperm_percent'),('UNIX_VM_Memory','UNIX_Memory_pgsp_aggregate_util'),('UNIX_VM_Memory','UNIX_Memory_pinned_percent'),('WinCpuCmd','WicnCpu');

INSERT INTO `SERVERPROFILES` VALUES ('DemoWIN-DataHunter-Selenium-DeployAndExecute','localhost','','','','','WINDOWS','','',''),('DemoWIN-DataHunter-Selenium-GenJmeterReport','localhost','','','','','WINDOWS','','',''),('DemoWIN-DataHunter-Selenium-metricsRunCheck','localhost','','','','','WINDOWS','','',''),('localhost','localhost','','','','','WINDOWS','','',''),('localhost_HOSTID','localhost','HOSTID','','','','WINDOWS','','','using HOSTID for local  '),('remoteLinuxServer','LinuxServerName','','userid','encryptMe','','LINUX','22','60000',''),('remoteUnixVM','UnixVMName','','userid','encryptMe','','UNIX','22','60000',''),('remoteWinServer','WinServerName','','userid','encryptMe','','WINDOWS','','','');

INSERT INTO `SERVERCOMMANDLINKS` VALUES ('DemoWIN-DataHunter-Selenium-DeployAndExecute','DataHunterSeleniumDeployAndExecute'),('DemoWIN-DataHunter-Selenium-GenJmeterReport','DataHunterSeleniumGenJmeterReport'),('DemoWIN-DataHunter-Selenium-metricsRunCheck','DataHunterSeleniumRunCheckTrendAnalysis'),('localhost','FreePhysicalMemory'),('localhost','FreeVirtualMemory'),('localhost','WinCpuCmd'),('localhost,localhost','WinCpuCmd'),('localhost_HOSTID','FreePhysicalMemory'),('localhost_HOSTID','FreeVirtualMemory'),('localhost_HOSTID','WinCpuCmd'),('remoteLinuxServer','LINUX_free_m_1_1'),('remoteLinuxServer','LINUX_mpstat_1_1'),('remoteUnixVM','UNIX_lparstat_5_1'),('remoteUnixVM','UNIX_Memory_Script'),('remoteWinServer','FreePhysicalMemory'),('remoteWinServer','FreeVirtualMemory'),('remoteWinServer','WinCpuCmd'),('rubbishnew,rubbishnew','WinCpuCmd');












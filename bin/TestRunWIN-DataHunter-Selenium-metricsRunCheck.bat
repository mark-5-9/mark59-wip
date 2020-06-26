REM   -------------------------------------------------------------------------------------------------------------------------------------------------
REM   | Load DataHunter Test Results to Mark59 Metrics (Trend Analysis) database.
REM   |
REM   | NOTE you may need to ensure the chromedriver.exe file at root of dataHunterPerformanceTestSamples project is compatible with your Chrome version
REM   |      (see Mark59 user guide for details)  
REM   |
REM   |  Alternative to running this .bat ** H2 DATABASE ONLY **
REM   |		 - login  to the server-metrics-web application  "http://localhost:8085/mark59-server-metrics-web" 
REM   |		 - run the DemoWIN-DataHunter-Selenium-metricsRunCheck profile. 
REM   |
REM   |  JMeter input results file expected at C:\Mark59_Runs\Jmeter_Results\DataHunter 
REM   |
REM   |  Loaded run can be seen at http://localhost:8080/metrics/trending?reqApp=DataHunter    (assuming default setup)
REM   |
REM   |  *** YOU NEED TO SELECT WHICH DATABASE TO LOAD RESULTS TO BEFORE EXECUTION ***
REM   |
REM   -------------------------------------------------------------------------------------------------------------------------------------------------
CD /D "%~dp0"

rem REM   |  *** YOU NEED TO SELECT WHICH DATABASE TO LOAD RESULTS TO BEFORE EXECUTION *** :

SET "DATABASE=H2"
rem SET "DATABASE=MYSQL"
rem SET "DATABASE=POSTGRES"

ECHO Starting the Metrics (Trend Analysis) runCheck Load Results program  
CD ../metricsRuncheck
START LoadDataHunterResultsIntoMetricsTrendAnalysis.bat

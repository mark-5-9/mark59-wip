#   -------------------------------------------------------------------------------------------------------------------------------------------------
#   | Load DataHunter Test Results to Mark59 Metrics (Trend Analysis) database.
#   |
#   | NOTE you may need to ensure the chromedriver file at root of dataHunterPerformanceTestSamples project is compatible with your Chrome version
#   |      (see Mark59 user guide for details)  
#   |
#   |  Alternative to running this .bat ** H2 DATABASE ONLY **
#   |		 - login  to the server-metrics-web application  "http://localhost:8085/mark59-server-metrics-web" 
#   |		 - run the DemoWIN-DataHunter-Selenium-metricsRunCheck profile. 
#   |
#   |  JMeter input results file expected at /home/<username>/Mark59_Runs/Jmeter_Results/DataHunter/ 
#   |
#   |  Loaded run can be seen at http://localhost:8080/metrics/trending?reqApp=DataHunter    (assuming default setup)
#   |
#   |  *** YOU NEED TO SELECT WHICH DATABASE TO LOAD RESULTS TO BEFORE EXECUTION ***
#   |
#   -------------------------------------------------------------------------------------------------------------------------------------------------


echo  "YOU NEED TO SELECT WHICH DATABASE TO LOAD RESULTS TO BEFORE EXECUTION (DEFAULT IS H2) "; 

DATABASE=H2
# DATABASE=MYSQL
# DATABASE=POSTGRES

cd ../metricsRuncheck;
./LoadDataHunterResultsIntoMetricsTrendAnalysis.sh "${DATABASE}";

cd ../bin
$SHELL

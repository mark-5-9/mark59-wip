#     -------------------------------------------------------------------------------------------------------------------------------------------------
#     | Deploy Artifacts and Run JMeter DataHunter Playwright Test.
#     |
#     | NOTE - FYI, mark59serverprofiles.xlsx is not copied. Before you run the '..usingExcel' testplan, copy it manually to the JMeter bin directory
#     |        Not necessary before running this .sh file as it runs the DataHunterSeleniumTestPlan which doesn't use the spreadsheet.
#     |
#     |  An instance of JMeter is expected at ~/apache-jmeter
#     |
#     |  Alternative to running this .sh 
#     |		 - login  to the server-metrics-web application  "http://localhost:8085/mark59-metrics" 
#     |		 - run the DemoLINUX-DataHunter-Playwright-DeployAndExecute profile. 
#     |
#     |  The only database considerations are that when the test is running server metrics are obtained invoking the 'localhost_LINUX' profile from the running
#     |      server-metrics-web application (ie via whatever DB it is connected to), and the datahunter application will be writing to its database.
#     |
#     |  logging at  ~/apache-jmeter/bin/jmeter.log
#     |  JMeter result file output to ~/Mark59_Runs/Jmeter_Results/DataHunter
#     |
#     -------------------------------------------------------------------------------------------------------------------------------------------------

echo This script deploys Mark59 artefacts to apache-jmeter, then executes JMeter plan DataHunterSeleniumTestPlan.jmx
echo starting from $PWD;

# use SET "StartCdpListeners=true" to allow the cdp listeners in the test script to execute 
StartCdpListeners=false
# StartCdpListeners=true
echo StartCdpListeners is $StartCdpListeners 

{   # try  

    cd ../mark59-scripting-samples && 
    DH_TEST_SAMPLES_DIR=$(pwd) && 
    echo mark59-scripting-samples base dir is $DH_TEST_SAMPLES_DIR &&

    cp ./mark59.properties ~/apache-jmeter/bin/mark59.properties &&
    cp ../mark59-metrics-api/target/mark59-metrics-api.jar  ~/apache-jmeter/lib/ext/mark59-metrics-api.jar && 
    cp ./target/mark59-scripting-samples.jar  ~/apache-jmeter/lib/ext/mark59-scripting-samples.jar &&
    rm -rf ~/apache-jmeter/lib/ext/mark59-scripting-samples-dependencies &&
    cp -r ./target/mark59-scripting-samples-dependencies ~/apache-jmeter/lib/ext/mark59-scripting-samples-dependencies &&
    mkdir -p ~/Mark59_Runs/Jmeter_Results/DataHunter && 
    cd ~/apache-jmeter/bin && 
    echo Starting JMeter execution from $PWD && 
    echo Starting JMeter execution.. && 
    ~/apache-jmeter/bin/jmeter -n -X -f  -t $DH_TEST_SAMPLES_DIR/test-plans/DataHunterPlaywrightTestPlan.jmx -l ~/Mark59_Runs/Jmeter_Results/DataHunter/DataHunterTestResults.csv -JDataHunterUrl=http://localhost:8081/mark59-datahunter -JForceTxnFailPercent=0 -JStartCdpListeners=$StartCdpListeners

} || { # catch 
    echo Deploy was unsuccessful! 
}
$SHELL

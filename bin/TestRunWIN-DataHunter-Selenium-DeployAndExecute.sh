echo This script runs the JMeter deploy in the background, then opens a terminal for JMeter execution.
echo starting from $PWD;

{   # try  

    cd ../dataHunterPerformanceTestSamples && 
    DH_TEST_SAMPLES_DIR=$(pwd) && 
    echo dataHunterPerformanceTestSamples base dir is $DH_TEST_SAMPLES_DIR &&

    cp ./mark59.properties ~/apache-jmeter/bin/mark59.properties &&
    cp ./chromedriver ~/apache-jmeter/bin/chromedriver && 
    cp ../mark59-server-metrics/target/mark59-server-metrics.jar  ~/apache-jmeter/lib/ext/mark59-server-metrics.jar && 
    cp ./target/dataHunterPerformanceTestSamples.jar  ~/apache-jmeter/lib/ext/dataHunterPerformanceTestSamples.jar && 
    mkdir -p ~/Mark59_Runs/Jmeter_Results/DataHunter && 
 
    gnome-terminal -- sh -c "~/apache-jmeter/bin/jmeter -n -X -f  -t $DH_TEST_SAMPLES_DIR/test-plans/DataHunterSeleniumTestPlan.jmx -l ~/Mark59_Runs/Jmeter_Results/DataHunter/DataHunterTestResults.csv -JForceTxnFailPercent=0; exec bash"

} || { # catch 
    echo Deploy was unsuccessful! 
}
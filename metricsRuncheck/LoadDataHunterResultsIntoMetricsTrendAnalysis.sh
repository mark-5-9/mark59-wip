#   -------------------------------------------------------------------------------------------------------------------------------------------------
#   |  Load DataHunter Test Results to Mark59 Metrics (Trend Analysis) database.
#   | 
#   |  This bat assumes - the metricsRuncheck.jar file exists in the ./target directory (relative to this file) 
#   |                   - when using a MySQL or Postgres database, the metricsdb database exists locally (using defaults)
#   |
#   |  Notes : the use of double quotes in a few places, required to cater for the & (ampersand) char, or to enter a space (equates to a blank here). 
#   |          the current time is use as the reference..
#   |
#   |  To directly execute :
#   |  ---------------------
#   |  You need to un-# whichever SET DATABASE you wish to user first  
#   |  (re-# all the SET DATABASE line aferwards if you intend to run this bat via the mark59 bin directly later)
#   |
#   -------------------------------------------------------------------------------------------------------------------------------------------------
# DATABASE=H2
# DATABASE=MYSQL
# DATABASE=POSTGRES

echo The database has been set to %DATABASE%

if [ "$DATABASE" = "" ]; then
	# Using H2  Starting metricsRuncheck batch for a dataHunter load (defaults taken on parameters) 
	java -jar ./target/metricsRuncheck.jar  -a DataHunter -i /home/ubuntu/Mark59_Runs/Jmeter_Results/DataHunter/ -d h2   	
fi

if [ "$DATABASE" = "MYSQL" ]; then
	# using MySQL:  Starting metricsRuncheck batch with some parameters provided (defaults taken on other parameters. ) 
	java -jar ./target/metricsRuncheck.jar -a DataHunter -i /home/ubuntu/Mark59_Runs/Jmeter_Results/DataHunter/ -d mysql -h localhost  -p 3306 -s metricsdb -q "?allowPublicKeyRetrieval=true&useSSL=false" -t JMETER  -r "%date% %time%"
fi

if [ "$DATABASE" = "POSTGRES" ]; then
	# using Postgress:  Starting metricsRuncheck batch with some parameters provided (defaults taken on other parameters. ) 
	java -jar ./target/metricsRuncheck.jar -a DataHunter -i /home/ubuntu/Mark59_Runs/Jmeter_Results/DataHunter/ -d pg -h localhost  -p 5432 -s metricsdb -q "?sslmode=disable" -t JMETER  -r "%date% %time%"
fi


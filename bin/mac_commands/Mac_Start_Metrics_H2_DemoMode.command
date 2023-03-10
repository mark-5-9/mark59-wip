#!/usr/bin/env bash
#   --------------------------------------------------------------------------------------------------------------
#   |  This Shell script starts the Metrics Mark59 Web Application
#   |
#   |  using a 'H2' database.  This database is built/started automatically, so a database build is not needed  
#   |   
#   |  Assuming you are using (default) server.port values for the applications, home page URLs  will be:    
#   |  
#   |      http://localhost:8085/mark59-metrics/
#   |  
#   |  Convenience script which can be invoked directly from Finder   
#   --------------------------------------------------------------------------------------------------------------

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

echo Convenience script which can be invoked directly from Finder
echo script dir is $SCRIPT_DIR
cd $SCRIPT_DIR

DATABASE=H2
export DATABASE
echo Database is set to "$DATABASE"

cd ../../mark59-metrics

echo Starting the Metrics Spring Boot Application
sh StartMetricsFromTarget.sh  

# $SHELL
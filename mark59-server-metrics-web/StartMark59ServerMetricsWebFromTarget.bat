REM   -------------------------------------------------------------------------------------------------------------------------------------------------
REM   |  This bat assumes - the mark59-server-metrics-web.war file exists in the ./target directory (relative to this file) 
REM   |		              - when using a MySQL or Postgress database 'mark59servermetricswebdb' database exists locally (using defaults)  
REM   |
REM   |  Note the use of double quotes n a few places, required to cater for the & (ampersand) char, or to enter a space (equates to a blank blank here). 
REM   -------------------------------------------------------------------------------------------------------------------------------------------------
@echo off
MODE con:cols=180 lines=500

ECHO The database has been set to %DATABASE%

IF "%DATABASE%" == "H2" (
	rem Using H2  Starting mark59-server-metrics-web  (default application server port) 
	java -jar ./target/mark59-server-metrics-web.war --spring.profiles.active=h2 --port=8085 
)

IF "%DATABASE%" == "MYSQL" (
	rem Using MySQL + server info with override user/pass
	java -jar ./target/mark59-server-metrics-web.war --spring.profiles.active=mysql --port=8085  --mysql.server=localhost --mysql.port=3306  --mysql.schema=mark59servermetricswebdb --mysql.xtra.url.parms="?allowPublicKeyRetrieval=true&useSSL=false" --mysql.username=admin --mysql.password=admin --user=admin --pass=mark59
)

rem -- another MySQL example --  
rem Using MySQL  Starting mark59-server-metrics-web.  Providing DB connection and server information (using default values) ie as above, but using default app user/pass  
rem java -jar ./target/mark59-server-metrics-web.war --spring.profiles.active=mysql --port=8085  --mysql.server=localhost --mysql.port=3306  --mysql.schema=mark59servermetricswebdb --mysql.xtra.url.parms="?allowPublicKeyRetrieval=true&useSSL=false" --mysql.username=admin --mysql.password=admin

IF "%DATABASE%"=="POSTGRES" (
	rem Using Postgress + server info with override user/pass  
	java -jar ./target/mark59-server-metrics-web.war --spring.profiles.active=pg ---port=8085  --pg.server=localhost --pg.port=5432  --pg.database=mark59servermetricswebdb --pg.xtra.url.parms=" " --pg.username=admin --pg.password=admin --user=admin --pass=mark59
)

PAUSE
--
--  The Mark50 v.3.0.0 release has renamed the 'pvmetrics' database to 'metricsdb'
--  
--  To do the change-over for an existing application do the following:
--
--  	1. Take a backup of your pvmetrics database (just in case!)
--		2. Run the MYSQLmetricsDataBaseCreation.sql script in this folder (creates the new metricsdb database with sample data in it)
--		3. Run this script (copies your data over the sample data, into the new metricsdb database)
--        

SET SQL_SAFE_UPDATES = 0;
delete FROM metricsdb.applications;
insert into metricsdb.applications select * from pvmetrics.applications;   
delete FROM metricsdb.eventmapping;
insert into metricsdb.eventmapping select * from pvmetrics.eventmapping;  

 
delete FROM metricsdb.graphmapping;
insert into metricsdb.graphmapping select * from pvmetrics.graphmapping;   
delete FROM metricsdb.metricsla;
insert into metricsdb.metricsla select * from pvmetrics.metricsla;   
delete FROM metricsdb.runs;
insert into metricsdb.runs select * from pvmetrics.runs;   
delete FROM metricsdb.sla;
insert into metricsdb.sla select * from pvmetrics.sla;  
delete FROM metricsdb.transaction;
insert into metricsdb.transaction select * from pvmetrics.transaction; 
-- optional : 
-- delete FROM metricsdb.testtransactions; 
-- insert into metricsdb.testtransactions select * from pvmetrics.testtransactions;  
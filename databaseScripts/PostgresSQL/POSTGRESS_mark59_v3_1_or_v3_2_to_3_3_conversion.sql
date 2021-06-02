-- note: No required postgres db changnes for 3.2, so this file applies to an upgrade from 3.1 or 3.2 to 3.3 

**********************************************
**
**  --- PLEASE REVIEW THE MYSQL 3.1 TO 3.2 SQL FILE AND THE CHANGES TO POSTGRESmetricsDataBaseCreation.sql FILE
**  --  TO SEE DETAILS OF THE NEW COLUMNS AND THEIR POSITIONS ---
**
**
**  -- metricsdb TABLE sla  NEEDS TO BE RE-CREATED AS POSTGRESS DOES NOT ALLOW FOR A NEW COLUMN OTHER THAN AT THE END OF THE TABLE
**
**  -- suggestion : save off data (sql format), then
**  --              edit sql data by inserting '0.000,' at the postion of the new columnn TXN_DELAY
**  --              edit sql data by inserting '0' at the postion of the new columnn XTRA_INT
**  --  
**  -- not required, but if you want to keep the dataHunter data the same is in the curent 'quick start', 
**  --               change the TXN_DELAY for the SLA DataHnuter DH-lifecycle-0100-deleteMultiplePolicies txn to 0.200  
**  ----------------------------------------------
**
**  -- not required, but if you want to keep the dataHunter data the same is in the curent 'quick start', 
**  --               insert id 15 'TXN90_EX_DELAY' (a new graphmapping)
**   
*************************************************

-- metricsdb changes

ALTER TABLE public.TRANSACTION ADD COLUMN TXN_DELAY5 numeric(18,3) NOT NULL DEFAULT '0.000';

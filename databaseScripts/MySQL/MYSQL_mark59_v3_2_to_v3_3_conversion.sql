SET SQL_SAFE_UPDATES = 0;

ALTER TABLE `metricsdb`.`sla` 
ADD COLUMN `TXN_DELAY` DECIMAL(12,3) NULL DEFAULT 0.0 AFTER `SLA_FAIL_PERCENT`,
ADD COLUMN `XTRA_INT` BIGINT(20) NULL DEFAULT 0 AFTER `XTRA_NUM`,
CHANGE COLUMN `XTRA_NUM` `XTRA_NUM` DECIMAL(12,3) NULL DEFAULT 0.0 ;

ALTER TABLE `metricsdb`.`transaction` 
ADD COLUMN `TXN_MEDIAN` DECIMAL(18,3) NOT NULL AFTER `TXN_AVERAGE`;

ALTER TABLE `metricsdb`.`transaction` 
ADD COLUMN `TXN_DELAY` DECIMAL(18,3) NOT NULL DEFAULT '0.000' AFTER `TXN_SUM`;

-- *************************************************
-- **
-- **  -- not required, but if you want to keep the data in line with the curent 'quick start' and use new functionality : 
-- **  --               insert into the graphmapping table  id 15  'TXN_90TH_EX_DELAY'
-- **  --               insert into the graphmapping table  id 95  'TXN_MEDIAN' 
-- **  --               insert into the graphmapping table  id 115 'TXN_DELAY' 
-- **  --               (refer to the GRAPHMAPPING inserts in MYSQLmetricsDataBaseCreation.sql for the sql statements) 
-- **   
-- *************************************************


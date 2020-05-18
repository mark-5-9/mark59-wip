
-- DROP DATABASE IF EXISTS datahunterdb;
CREATE DATABASE datahunterdb CHARACTER SET utf8mb4  COLLATE utf8mb4_bin; 

USE datahunterdb;


-- PLEASE RUN  "MYSQLcreateAdminUser.sql"  TO CREATE THE 'ADMIN' USER IF YOU HAVE NOT ALREADY DONE SO.  

-- Note that character set utf8mb4 is the default from MySQL 8.0.
-- The collation for the database is set to utf8mb4_bin
-- Useful at at is allows for stuff like case-sensitive eventmapping matching.  Also aligns H2 database and Java case sensitive sorting.          


-- create tables  -- 


CREATE TABLE IF NOT EXISTS  policies (
  application varchar(64) NOT NULL,
  identifier 	varchar(512) NOT NULL,
  lifecycle 	varchar(64) NOT NULL,
  useability 	enum('UNUSED','USED','REUSABLE','UNPAIRED') NOT NULL,
  otherdata  	mediumtext NOT NULL,
  created   	datetime NOT NULL,
  updated   	timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  epochtime  	bigint  NOT NULL DEFAULT '0',
  PRIMARY KEY (application,identifier,lifecycle),
  KEY application (application)
); 


CREATE TABLE IF NOT EXISTS reference (
  application varchar(64) NOT NULL,
  property	  varchar(128) NOT NULL,
  value 	  varchar(128) NOT NULL,
  description varchar(512) DEFAULT NULL,
  PRIMARY KEY (application,property)
); 
  
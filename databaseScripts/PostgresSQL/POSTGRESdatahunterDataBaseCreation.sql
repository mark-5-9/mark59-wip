
-- >  comment/uncomment as required 
CREATE USER admin SUPERUSER PASSWORD 'admin';
-- DROP DATABASE datahunterdb;
CREATE DATABASE datahunterdb WITH ENCODING='UTF8' OWNER=admin TEMPLATE=template0 LC_COLLATE='C' LC_CTYPE='C';
-- <

--   The utf8/C ecoding/collation is more in line with other mark59 database options (and how Java/JS sorts work). 
--   if you use the pgAdmin tool to load data, remember to hit the 'commit' icon to save the changes! 


CREATE TABLE IF NOT EXISTS policies (
  application varchar(64) NOT NULL,
  identifier 	varchar(512) NOT NULL,
  lifecycle 	varchar(64) NOT NULL,
  useability 	varchar(16) NOT NULL,
  otherdata  	varchar(512) NOT NULL,
  created   	timestamp NOT NULL,
  updated   	timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  epochtime  	bigint  NOT NULL DEFAULT '0',
  PRIMARY KEY (application,identifier,lifecycle)
); 

CREATE TABLE IF NOT EXISTS reference (
  application varchar(64) NOT NULL,
  property	  varchar(128) NOT NULL,
  value 	  varchar(128) NOT NULL,
  description varchar(512) DEFAULT NULL,
  PRIMARY KEY (application,property)
); 

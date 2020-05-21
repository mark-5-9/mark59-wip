/*
  Copyright 2019 Insurance Australia Group Limited
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

--  you need need to be connected to the database using the 'root' user to execute

DROP USER IF EXISTS 'admin'@'localhost';
DROP USER IF EXISTS 'admin'@'%';

CREATE USER 'admin'@'localhost' IDENTIFIED BY 'admin';
GRANT ALL PRIVILEGES ON *.* TO 'admin'@'localhost' WITH GRANT OPTION;
CREATE USER 'admin'@'%' IDENTIFIED BY 'admin';
GRANT ALL PRIVILEGES ON *.* TO 'admin'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;


--  A Note for Linux Users --
--  You may also need to set a time_zone, depending on your instal (you will find our when you attmept to connect start an applciaton and you see a message like 
--   " The server time zone value 'xxxx' is unrecognized or represents more than one time zone"  
--  refer https://stackoverflow.com/questions/930900/how-do-i-set-the-time-zone-of-mysql 
-- eg (for utc time)
-- SET GLOBAL default-time-zone='+00:00';
-- SELECT @@global.time_zone;

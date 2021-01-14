

**********************************************
**
**  TABLE serverprofiles NEEDS TO BE RE-CREATED AS POSTGRESS DOES NOT ALLOW FOR A NEW COLUMN OTHER THAN AT THE END OF THE TABLE
**
**  ie, run 'CREATE TABLE IF NOT EXISTS SERVERPROFILES....' in POSTGRESmark59servermetricswebDataBaseCreation.sql. and re-enter data as necessary
**
*************************************************


ALTER TABLE public.commands alter COLUMN command SET DATA TYPE character varying(8192)
ALTER TABLE public.commands ADD COLUMN param_names character varying(1000) COLLATE pg_catalog."default" DEFAULT NULL;


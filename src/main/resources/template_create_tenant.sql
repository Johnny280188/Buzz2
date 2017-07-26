-- remove access to public schema to all
REVOKE CREATE ON SCHEMA public FROM PUBLIC;

-- create tenant user in db
-- DROP USER IF EXISTS myuniversity;

CREATE USER buzz99_demo WITH ENCRYPTED PASSWORD 'buzz99';
GRANT buzz99_demo TO CURRENT_USER;
ALTER USER buzz99_demo WITH CONNECTION LIMIT 50;

-- remove this
-- GRANT ALL PRIVILEGES ON DATABASE postgres TO myuniversity;

-- create table space per tenant
-- CREATE TABLESPACE ts_myuniversity OWNER myuniversity LOCATION '${tablespace_dir}/myuniversity/module_to/module_from';
-- SET default_tablespace = ts_myuniversity;

-- DROP SCHEMA IF EXISTS myuniversity CASCADE;
-- The schema user wil be the schema name since not given
CREATE SCHEMA buzz99_demo AUTHORIZATION buzz99_demo;

-- for uuid generator -> gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Set the new schema first so that we dont have to namespace when creating tables
-- add the postgres to the search path so that we can use the pgcrypto extension
SET search_path TO buzz99_demo, public;

CREATE TABLE IF NOT EXISTS buzz99_demo.questions (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   jsonb jsonb,
   created_date timestamp not null default current_timestamp,
   updated_date timestamp not null default current_timestamp
   );

CREATE TABLE IF NOT EXISTS buzz99_demo.channels (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   jsonb jsonb,
   created_date timestamp not null default current_timestamp,
   updated_date timestamp not null default current_timestamp
   );
   
CREATE TABLE IF NOT EXISTS buzz99_demo.users (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   jsonb jsonb,
   created_date timestamp not null default current_timestamp,
   updated_date timestamp not null default current_timestamp
   );
   
-- update the update_date column when record is updated
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
-- NEW to indicate updating the new row value
    NEW.updated_date = current_timestamp;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_date BEFORE UPDATE ON buzz99_demo.questions FOR EACH ROW EXECUTE PROCEDURE  update_modified_column();
CREATE TRIGGER update_date BEFORE UPDATE ON buzz99_demo.channels FOR EACH ROW EXECUTE PROCEDURE  update_modified_column();
CREATE TRIGGER update_date BEFORE UPDATE ON buzz99_demo.users FOR EACH ROW EXECUTE PROCEDURE  update_modified_column();

-- give the user PRIVILEGES after everything is created by script
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA buzz99_demo TO buzz99_demo;

--COPY config_data (jsonb) FROM 'data/locales.data' ENCODING 'UTF8';

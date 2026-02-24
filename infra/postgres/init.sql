-- Creates one DB per service. Runs once on first postgres container start.
CREATE DATABASE compliance_db;
CREATE DATABASE competition_db;
-- Add more as other services are added:
-- CREATE DATABASE registry_db;
-- CREATE DATABASE identity_db;
-- CREATE DATABASE membership_db;
-- CREATE DATABASE scheduling_db;
-- CREATE DATABASE finance_db;

-- update columns of type SMALLINT to type INTEGER (ticket #271)

ALTER TABLE RAS_HIERIX ALTER COLUMN NumEntries TYPE integer;
ALTER TABLE RAS_HIERIX ALTER COLUMN Dimension TYPE integer;

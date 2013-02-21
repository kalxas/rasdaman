-- table storing latest database update
                                                                                                                
CREATE OR REPLACE function create_dbupdates_table() returns void as $$
CREATE TABLE ras_dbupdates (
  id serial NOT NULL,
  UpdateNumber INTEGER,
  UpdateNumberRe INTEGER,
  primary key (id)
);

insert into ras_dbupdates values (1, 0, 0);
$$ language sql;

SELECT CASE WHEN (
  SELECT COUNT(*)
  FROM information_schema.tables
  WHERE table_name = 'ras_dbupdates') = 0
THEN create_dbupdates_table() END;

DROP function create_dbupdates_table();

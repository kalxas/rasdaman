-- null values table

CREATE OR REPLACE function create_nullvalues_table() returns void as $$
CREATE TABLE RAS_NULLVALUES (
 SetTypeOId INTEGER NOT NULL,
 NullValueOId INTEGER NOT NULL,
 PRIMARY KEY (SetTypeOId)
);
$$ language sql;

SELECT CASE WHEN (
  SELECT COUNT(*)
  FROM information_schema.tables
  WHERE table_name = 'ras_nullvalues') = 0
THEN create_nullvalues_table() END;

DROP function create_nullvalues_table();

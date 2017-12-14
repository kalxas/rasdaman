-- alter table for lock manager

CREATE OR REPLACE function drop_rasclient_column() returns void as $$
ALTER TABLE RAS_LOCKEDTILES
    DROP COLUMN RasClientID;
$$ language sql;

SELECT CASE WHEN (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_name = 'ras_lockedtiles' and column_name = 'rasclientid') > 0
THEN drop_rasclient_column() END;

DROP function drop_rasclient_column();

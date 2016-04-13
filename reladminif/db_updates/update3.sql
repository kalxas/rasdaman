-- introduce table for lock manager (ticket #278)

CREATE OR REPLACE function create_lockedtiles_table() returns void as $$
CREATE TABLE RAS_LOCKEDTILES (
    TileID bigint NOT NULL,
    RasServerID varchar(40) NOT NULL,
    RasClientID varchar(24) NOT NULL,
    SharedLock integer NOT NULL,
    ExclusiveLock integer,
    UNIQUE(TileID, ExclusiveLock)
);
$$ language sql;

SELECT CASE WHEN (
  SELECT COUNT(*)
  FROM information_schema.tables
  WHERE table_name = 'ras_lockedtiles') = 0
THEN create_lockedtiles_table() END;

DROP function create_lockedtiles_table();

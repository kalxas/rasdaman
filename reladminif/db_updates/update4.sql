-- alter table for lock manager
ALTER TABLE RAS_LOCKEDTILES
    DROP COLUMN IF EXISTS RasClientID;

-- introduce table for lock manager (ticket #278)
CREATE TABLE RAS_LOCKEDTILES (
    TileID bigint NOT NULL,
    RasServerID varchar(40) NOT NULL,
    RasClientID varchar(24) NOT NULL,
    SharedLock integer NOT NULL,
    ExclusiveLock integer,
    UNIQUE(TileID, ExclusiveLock)
);

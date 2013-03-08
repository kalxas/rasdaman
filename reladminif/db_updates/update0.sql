-- table storing latest database update

CREATE FUNCTION create_table() RETURNS VOID AS
$$
BEGIN
    if not exists(
        select * from information_schema.tables 
        where table_catalog = CURRENT_CATALOG and table_schema = CURRENT_SCHEMA
              and table_name = 'ras_dbupdates') then

        CREATE TABLE RAS_DBUPDATES (
		    UpdateType VARCHAR(5) NOT NULL,
		    UpdateNumber INTEGER,
		    primary key (UpdateType)
		);
        INSERT INTO RAS_DBUPDATES values ('rc', 0);
    end if;
END;
$$
LANGUAGE plpgsql;

SELECT create_table();
DROP function create_table();

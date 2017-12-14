DROP VIEW RAS_MDDTYPES_VIEW;
ALTER TABLE RAS_BASETYPES ALTER COLUMN contenttype TYPE bigint;
ALTER TABLE RAS_COUNTERS ALTER COLUMN nextvalue TYPE bigint;
ALTER TABLE RAS_HIERIX ALTER COLUMN mddobjixoid TYPE bigint;
ALTER TABLE RAS_HIERIX ALTER COLUMN parentoid TYPE bigint;
ALTER TABLE RAS_HIERIX ALTER COLUMN dyndata TYPE bigint;
ALTER TABLE RAS_ITILES ALTER COLUMN itileid TYPE bigint;
ALTER TABLE RAS_ITILES ALTER COLUMN itile TYPE bigint;
ALTER TABLE RAS_ITMAP ALTER COLUMN tileid TYPE bigint;
ALTER TABLE RAS_ITMAP ALTER COLUMN indexid TYPE bigint;
ALTER TABLE ras_mddbasetypes ALTER COLUMN mddbasetypeoid TYPE bigint;
ALTER TABLE ras_mddbasetypes ALTER COLUMN basetypeid TYPE bigint;
ALTER TABLE ras_mddcollections ALTER COLUMN mddid TYPE bigint;
ALTER TABLE ras_mddcollections ALTER COLUMN mddcollid TYPE bigint;
ALTER TABLE ras_mddcollnames ALTER COLUMN mddcollid TYPE bigint;
ALTER TABLE ras_mdddimtypes ALTER COLUMN mdddimtypeoid TYPE bigint;
ALTER TABLE ras_mdddimtypes ALTER COLUMN basetypeid TYPE bigint;
ALTER TABLE ras_mdddomtypes ALTER COLUMN mdddomtypeoid TYPE bigint;
ALTER TABLE ras_mdddomtypes ALTER COLUMN basetypeid TYPE bigint;
ALTER TABLE ras_mddobjects ALTER COLUMN basetypeoid TYPE bigint;
ALTER TABLE ras_mddobjects ALTER COLUMN storageoid TYPE bigint;
ALTER TABLE ras_mddobjects ALTER COLUMN nodeoid TYPE bigint;
ALTER TABLE ras_mddtypes ALTER COLUMN mddtypeoid TYPE bigint;
ALTER TABLE ras_rcindexdyn ALTER COLUMN id TYPE bigint;
ALTER TABLE ras_rcindexdyn ALTER COLUMN count TYPE INTEGER;
ALTER TABLE ras_rcindexdyn ALTER COLUMN dyndata TYPE BIGINT;
ALTER TABLE ras_settypes ALTER COLUMN mddtypeoid TYPE bigint;
ALTER TABLE ras_storage ALTER COLUMN storageid TYPE bigint;
ALTER TABLE ras_tiles ALTER COLUMN blobid TYPE bigint;
ALTER TABLE ras_tiles ALTER COLUMN tile TYPE bigint;
ALTER TABLE ras_udfargs ALTER COLUMN uoid TYPE bigint;
ALTER TABLE ras_udfbody ALTER COLUMN uoid TYPE bigint;
ALTER TABLE ras_udfnscontent ALTER COLUMN uoid TYPE bigint;
ALTER TABLE ras_udfnscontent ALTER COLUMN udfoid TYPE bigint;
ALTER TABLE ras_udfpackage ALTER COLUMN uoid TYPE bigint;
CREATE VIEW RAS_MDDTYPES_VIEW ( MDDTypeOId , MDDTypeName ) 
	AS 
		SELECT 
			MDDTypeOId * 512 + 3, 
			MDDTypeName 
		FROM 
			RAS_MDDTYPES 
	UNION SELECT
		MDDBaseTypeOId * 512 + 4, 
		MDDTypeName 
	FROM 
		RAS_MDDBASETYPES 
	UNION SELECT 
		MDDDimTypeOId * 512 + 5, 
		MDDTypeName 
	FROM 
		RAS_MDDDIMTYPES 
	UNION SELECT 
		MDDDomTypeOId * 512 + 6, 
		MDDTypeName 
	FROM 
		RAS_MDDDOMTYPES;



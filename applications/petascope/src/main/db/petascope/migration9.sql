-- ~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=
-- This file is part of rasdaman community.
--
-- Rasdaman community is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- Rasdaman community is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
--
-- Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
-- rasdaman GmbH.
--
-- For more information please see <http://www.rasdaman.org>
-- or contact Peter Baumann via <baumann@rasdaman.com>.
-- ~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=

-- ####################################################################
-- |          petascopedb migration script for rasdaman 9.0           |
-- ####################################################################

-----------------------------------------------------------------------
-- PREFACE
-- ~~~~~~~
-- This script migrates the pre-existing coverages stored in `petascopedb'
-- to fit in the new schema for irregular grids, which can be created running
-- the `./init9.sql' script.
--
-- The 
-- NOTES:
--   update8.sql
--   OID
--   SECORE from kahlua *now*, see TODO
--   t axis to <SECORE>/def/crs/ANSI-Date, compound with spatial CRS
--
-- TODO: migration9.sql.in with replacement of %SECORE% configured host
-----------------------------------------------------------------------

--
-- Read general info and extra-metadata
--
petascopedb=# SELECT cov.id, cov.name, cov.type FROM ps_coverage AS cov;
 id |          name           |         type          
----+-------------------------+-----------------------
  2 | mean_summer_airtemp     | RectifiedGridCoverage
  3 | MODIS_2008129_34S       | RectifiedGridCoverage
  4 | MODIS_2008129_34S_WGS84 | RectifiedGridCoverage
  5 | greece_dem              | RectifiedGridCoverage
  7 | MODIS_AUSTRIA_2010170   | RectifiedGridCoverage
  8 | MODIS_33N_2010170_WGS84 | RectifiedGridCoverage
 65 | rgb                     | RectifiedGridCoverage
 66 | mr                      | RectifiedGridCoverage
 67 | eobstest                | RectifiedGridCoverage
  1 | NIR                     | GridCoverage
 77 | MODIS_35U               | RectifiedGridCoverage
 81 | OneD                    | GridCoverage
(12 rows)
-- ...and fill ps9_coverage:
INSERT INTO ps9_coverage ... WHERE ... AND ps9_mimetype.mime_type = 'application/x-octet-stream';


-- Extra metadata: foreach result row:
petascopedb=# SELECT cov.id, meta.metadata FROM ps_coverage AS cov, ps_metadata AS meta WHERE cov.id=meta.coverage;
 id |           metadata            
----+-------------------------------
  2 | Australian mean temperatures.
(1 row)


--
-- domain set
--



--
-- range set
-- 
-- Foreach coverage:
SELECT ((oid)*512)+1 FROM dblink(RASBASE, <query>)


--
-- range type
--
-- SELECT mddcollname, ras_mddobjects.mddid 
-- FROM ras_mddobjects, ras_mddcollnames, ras_mddcollections 
-- WHERE ras_mddobjects.mddid = ras_mddcollections.mddid 
--   AND ras_mddcollections.mddcollid=ras_mddcollnames.mddcollid 
--   AND mddcollname='rgb';
-- ==> then (mddid * 512 + 1)


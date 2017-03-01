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

------------------------------------------------------------------------------------------------
-- Service provider's phone is a composition of N voice and M facsimile numbers (ticket #718) --
------------------------------------------------------------------------------------------------
CREATE TABLE ps_telephone (
    id             serial  PRIMARY KEY,
    voice      text ARRAY NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Phone/ows:Voice
    facsimile  text ARRAY NULL -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Phone/ows:Facsimile
    -- Constraints and FKs
    -- uses PG-specific array_lower fsunction
    --CONSTRAINT no_both_empty CHECK (NOT (array_lower(voice, 1) IS NULL AND array_lower(facsimile, 1) IS NULL)) -- Contains at least 1 number
);
UPDATE ps_service_provider SET contact_phone=NULL;
ALTER TABLE ps_service_provider ALTER COLUMN contact_phone SET DATA TYPE integer USING contact_phone::integer;
ALTER TABLE ps_service_provider RENAME COLUMN contact_phone TO contact_phone_id;
ALTER TABLE ps_service_provider ADD FOREIGN KEY (contact_phone_id) REFERENCES ps_telephone (id) ON DELETE RESTRICT;

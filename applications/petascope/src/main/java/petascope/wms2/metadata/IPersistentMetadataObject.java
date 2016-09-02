/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

package petascope.wms2.metadata;

/**
 * This interface should be implemented by all classes that want to be persisted to our storage medium (currently
 * database).
 * No methods need to be implemented but you will have to add your class in one more place:
 * - PersistenceMetadataObjectProvider: You will need to add a convenience method pointing to the persistent class
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */

public interface IPersistentMetadataObject {

    public static final String TABLE_PREFIX = "wms13_";
}

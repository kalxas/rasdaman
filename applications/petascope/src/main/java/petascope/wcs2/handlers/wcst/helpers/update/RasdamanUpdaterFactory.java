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
package petascope.wcs2.handlers.wcst.helpers.update;
import petascope.util.IOUtil;

import java.io.File;

/**
 * Class creating the correct RasdamanUpdater object.
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class RasdamanUpdaterFactory {

    public RasdamanUpdaterFactory() {
    }

    public RasdamanUpdater getUpdater(String collectionName, String collectionOid, String domain, String values, String shiftDomain) {
        return new RasdamanValuesUpdater(collectionName, collectionOid, domain, values, shiftDomain);
    }

    public RasdamanUpdater getUpdater(String collectionName, String collectionOid, String domain, File file, String mimeType, String shiftDomain, String rangeParameters) {
        if (mimeType != null && mimeType.toLowerCase().contains(IOUtil.GRIB_MIMETYPE)) {
            return new RasdamanGribUpdater(collectionName, collectionOid, domain, file, rangeParameters, shiftDomain);
        } else if (mimeType != null && mimeType.toLowerCase().contains(IOUtil.NETCDF_MIMETYPE)) {
            return new RasdamanNetcdfUpdater(collectionName, collectionOid, domain, file, shiftDomain, rangeParameters);
        } else {
            return new RasdamanDecodeUpdater(collectionName, collectionOid, domain, file, shiftDomain);
        }
    }
}

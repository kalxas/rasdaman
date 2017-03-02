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
package petascope.wcs2.handlers.wcst.helpers.insert;

import petascope.ConfigManager;
import petascope.exceptions.rasdaman.RasdamanException;
import petascope.util.ras.RasUtil;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Class for inserting data into a collection, starting from a file.
 *
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class RasdamanFileInserter implements RasdamanInserter {

    String collectionName;
    String filePath;
    String mimetype;
    String tiling;

    /**
     * Class constructor.
     * @param collectionName: the name of the collection.
     * @param filePath: the path to the file.
     * @param mimetype: the mime type of the file.
     * @param tiling: the tiling scheme in rasql synax.
     */
    public RasdamanFileInserter(String collectionName, String filePath, String mimetype, String tiling) {
        this.collectionName = collectionName;
        this.filePath = filePath;
        this.mimetype = mimetype;
        this.tiling = tiling;
    }

    @Override
    public BigInteger insert() throws RasdamanException, IOException {
        BigInteger oid = RasUtil.executeInsertFileStatement(collectionName, filePath, mimetype, tiling);
        return oid;
    }
}

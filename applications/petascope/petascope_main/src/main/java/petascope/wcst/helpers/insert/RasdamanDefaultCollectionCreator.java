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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcst.helpers.insert;

import org.slf4j.LoggerFactory;
import petascope.exceptions.PetascopeException;
import petascope.rasdaman.exceptions.RasdamanException;
import petascope.util.ras.RasUtil;

/**
 * Class for creating a collection in rasdaman.
 *
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class RasdamanDefaultCollectionCreator implements RasdamanCollectionCreator {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RasdamanDefaultCollectionCreator.class);

    String collectionName;
    String collectionType;

    /**
     * Class constructor.
     * @param collectionName: the name of the collection.
     * @param collectionType: the type of the collection.
     */
    public RasdamanDefaultCollectionCreator(String collectionName, String collectionType) {
        this.collectionName = collectionName;
        this.collectionType = collectionType;
    }

    @Override
    public void createCollection() throws RasdamanException, PetascopeException {
        log.info("Creating rasdaman collection " + collectionName + ".");
        //create the collection
        RasUtil.createRasdamanCollection(collectionName, collectionType);
        log.info("Collection created.");
    }
}

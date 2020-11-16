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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.oapi.handlers.model;

/**
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */

import java.util.List;

public class Collections {

    private List<Collection> collections;
    private List<Link> links;

    public Collections(List<Collection> collections, List<Link> links) {
        this.collections = collections;
        this.links = links;
    }

    public List<Collection> getCollections() {
        return collections;
    }

    public List<Link> getLinks() {
        return links;
    }
}

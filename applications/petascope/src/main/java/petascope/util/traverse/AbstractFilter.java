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
/*
 * JOMDoc - A Java library for OMDoc documents (http://omdoc.org/jomdoc).
 *
 * Original author    Dimitar Misev <d.misev@jacobs-university.de>
 * Web                http://kwarc.info/dmisev/
 * Created            Sep 26, 2009, 3:08:40 PM
 *
 * Filename           $Id$
 * Revision           $Revision$
 * Last modified on   $Date$
 *               by   $Author$
 *
 * Copyright (C) 2007,2008 the KWARC group (http://kwarc.info)
 * Licensed under the GNU Public License v3 (GPL3).
 * For other licensing contact Michael Kohlhase <m.kohlhase@jacobs-university.de>
 */
package petascope.util.traverse;

import java.util.ArrayList;
import java.util.List;
import petascope.util.MiscUtil;

/**
 * All filters should extend this abstract class.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public abstract class AbstractFilter implements Filter {

    protected Filter parentFilter;
    protected List<Filter> orFilters;

    public AbstractFilter() {
        this(null);
    }

    public AbstractFilter(Filter parentFilter) {
        this.parentFilter = parentFilter;
        orFilters = new ArrayList<Filter>();
    }

    public boolean evaluate(Object node, int depth) {
        for (Filter filter : orFilters) {
            if (filter.evaluate(node, depth))
                return true;
        }
        if (parentFilter != null)
            return parentFilter.evaluate(node, depth);
        return true;
    }

    public Filter getParentFilter() {
        return parentFilter;
    }

    public void setParentFilter(Filter parentFilter) {
        this.parentFilter = parentFilter;
    }

    public List<Filter> getOrFilters() {
        return orFilters;
    }

    public void or(Filter... filters) {
        orFilters = MiscUtil.toList(filters);
    }
}

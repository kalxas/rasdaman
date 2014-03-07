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
 * Created            Jul 13, 2008, 4:45:31 PM
 * 
 * Filename           $Id: DFSTraversor.java 133 2008-08-22 06:35:59Z dmisev $
 * Revision           $Revision: 133 $
 * Last modified on   $Date: 2008-08-22 08:35:59 +0200 (Fri, 22 Aug 2008) $
 *               by   $Author: dmisev $
 * 
 * Copyright (C) 2007,2008 the KWARC group (http://kwarc.info)
 * Licensed under the GNU Public License v3 (GPL3).
 * For other licensing contact Michael Kohlhase <m.kohlhase@jacobs-university.de>
 */
package petascope.util.traverse;

import java.util.ArrayList;
import java.util.List;

/**
 * Do a depth first search, but don't traverse deeper once a node is evaluated as true.
 * 
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class DFSCTraversor extends AbstractTraverseStrategy {

    public DFSCTraversor() {
        super();
    }

    public DFSCTraversor(Filter spec) {
        super(spec);
    }

    public DFSCTraversor(Filter spec, int howMany) {
        super(spec, howMany);
    }

    public List<Object> traverse(Traversable node) {
        List<Object> ret = new ArrayList<Object>();
        dfs(node, ret, 0);
        return ret;
    }

    private void dfs(Traversable current, List<Object> ret, int depth) {

        for (int i = 0; i < current.childCount(); i++) {
            Traversable child = current.child(i);
            if (child == null || !child.isTraversable()) {
                continue;
            }
            boolean deeper = true;
            if (spec.evaluate(child.getValue(), depth)) {
                ret.add(child.getValue());
                deeper = true;
            }
            if (howMany > -1 && ret.size() >= howMany) {
                return;
            }
            if (deeper)
                dfs(child, ret, depth + 1);
        }
    }
}

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
package petascope.util;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;
import petascope.wcps.server.core.IRasNode;

/**
 * Various utility methods.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class MiscUtil {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(MiscUtil.class);

    public static <T> List<T> toList(T... e) {
         List<T> ret = new ArrayList<T>();
         if (e != null) {
             for (T o : e) {
                ret.add(o);
            }
         }
         return ret;
    }

    /**
     * Recursive generic method to extract nodes of a specified class into the XML tree of a WCPS query.
     * @param <T>   The generic type
     * @param root  The root node
     * @param type  Explicit target type argument
     * @return  A list of the children of `node` of class `type`.
     */
    public static <T extends IRasNode> List<T> childrenOfType(IRasNode root, Class<T> type) {
        List<T> targetChilds = new ArrayList<T>();

        // Recursive depth-first search
        if (root.hasChildren()) {
            // pre-order op
            List<IRasNode> children = root.getChildren();

            for (int i=0; i<children.size(); i++) {
                // visit children
                targetChilds.addAll(childrenOfType(children.get(i), type));
                // in-order op
                if (type.isInstance(children.get(i))) {
                    targetChilds.add((T)children.get(i));
                }
            }
        }

        return targetChilds;
    }

    /**
     * Recursive method to extract nodes of a specified classes into the XML tree of a WCPS query.
     * @param root   The root node
     * @param types  Explicit target types argument
     * @return  A list of the children of `node` of class `type`.
     */
    public static List<IRasNode> childrenOfTypes(IRasNode root, Class... types) {
        List<IRasNode> ret = new ArrayList<IRasNode>();

        // Recursive depth-first search
        if (root.hasChildren()) {
            // pre-order op
            List<IRasNode> children = root.getChildren();

            for (int i=0; i<children.size(); i++) {
                // visit children
                ret.addAll(childrenOfTypes(children.get(i), types));
                // in-order op
                for (Class c : types) {
                    if (c.isInstance(children.get(i))) {
                        ret.add(children.get(i));
                    }
                }
            }
        }

        return ret;
    }
}

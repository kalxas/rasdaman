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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.translator;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Base interface for all parse tree nodes of the processed abstract wcps language
 *
 * @author <a href="alex@flanche.net">Alex Dumitru</a>
 * @author <a href="vlad@flanche.net">Vlad Merticariu</a>
 */
public abstract class IParseTreeNode {

    /**
     * Translates this WCPS parse node element into its rasql equivalent.
     *
     * @return the translation of the wcps lang into rasql
     */
    public abstract String toRasql();

    /**
     * Adds a new child to the parse tree
     *
     * @param child the child to be added
     */
    public void addChild(IParseTreeNode child) {
        children.add(child);
    }


    /**
     * Returns all the direct descendants of this node
     *
     * @return a list of the children of the nodes
     */
    public List<IParseTreeNode> getChildren() {
        return children;
    }

    /**
     * Returns all the descendants of this tree
     *
     * @return the descendants of the tree
     */
    public List<IParseTreeNode> getDescendants() {
        final List<IParseTreeNode> descendants = new ArrayList<IParseTreeNode>();
        descendants.addAll(children);
        for (IParseTreeNode child : children) {
            descendants.addAll(child.getDescendants());
        }
        return descendants;
    }

    /**
     * Returns the subtree corresponding to the current node, including itself.
     *
     * @return
     */
    public List<IParseTreeNode> getSubTree(){
        List<IParseTreeNode> ret = new ArrayList<IParseTreeNode>();
        ret.add(this);
        ret.addAll(this.getDescendants());
        return ret;
    }

    @Override
    public String toString() {
        return toIndentedString(1);

    }


    /**
     * Returns any relevant node information that can be used in logs / debugs in the format "(info1, info2, info3)"
     *
     * @return
     */
    protected String nodeInformation() {
        return "()";
    }

    /**
     * Creates an string representation of the tree
     *
     * @param currentLevel the current level of the tree
     * @return
     */
    private String toIndentedString(int currentLevel) {
        String spacer = StringUtils.repeat("\t", currentLevel);
        StringBuilder retString = new StringBuilder().append(this.getClass().getSimpleName()).append(this.nodeInformation());
        if (!children.isEmpty()) retString.append("{\n").append("");
        List<String> childStrings = new ArrayList<String>();
        for (IParseTreeNode child : children) {
            childStrings.add(spacer + child.toIndentedString(currentLevel + 1));
        }
        retString.append(StringUtils.join(childStrings, ",\n "));
        if (!children.isEmpty()) retString.append("\n").append(spacer.substring(1)).append("}");
        return retString.toString();
    }

    private final List<IParseTreeNode> children = new ArrayList<IParseTreeNode>();

}

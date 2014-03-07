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
 * Created            Apr 11, 2008, 2:11:00 PM
 * 
 * Filename           $Id: Filter.java 1700 2010-03-26 01:06:24Z dmisev $
 * Revision           $Revision: 1700 $
 * Last modified on   $Date: 2010-03-26 02:06:24 +0100 (Fri, 26 Mar 2010) $
 *               by   $Author: dmisev $
 * 
 * Copyright (C) 2007,2008 the KWARC group (http://kwarc.info)
 * Licensed under the GNU Public License v3 (GPL3).
 * For other licensing contact Michael Kohlhase <m.kohlhase@jacobs-university.de>
 */
package petascope.util.traverse;

/**
 * This interface specifies how the contents of an XML document will be filtered.
 * 
 * @param <T> the type of nodes this filter evaluates
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public interface Filter<T extends Object> {

    /**
     * Evaluates whether a node should be filtered or not.
     * 
     * @param node the node to be evaluated
     * @param depth the depth at which this element is in the XML tree
     * @return true if the element should be filtered, otherwise
     *         false if it should be ignored
     */
    boolean evaluate(T node, int depth);
}

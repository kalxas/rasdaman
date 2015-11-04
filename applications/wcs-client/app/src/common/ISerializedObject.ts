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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann /
 rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

module rasdaman.common {
    export interface ISerializedObject {
        /**
         * Check if the attribute with the given name exists.
         * @param attributeName The name of the attribute, optionally prefixed by a namespace.
         */
        doesAttributeExist(attributeName:string):boolean;

        /**
         * Check if the element with the given name is a child of this element.
         * @param elementName The name of the element, optionally prefixed by a namespace.
         */
        doesElementExist(elementName:string):boolean;

        /**
         * Get the value of the attribute with the given identifier if it exists,
         * otherwise throw an exception.
         * @param attributeName
         */
        getAttributeAsBool(attributeName:string):boolean;

        /**
         * Get the value of the attribute with the given identifier if it exists, otherwise,
         * throw an exception.
         * @param attributeName
         */
        getAttributeAsNumber(attributeName:string):number;

        /**
         * Get the value of the attribute with the given identifier if it exists, otherwise,
         * throw an exception.
         * @param attributeName
         */
        getAttributeAsString(attributeName:string):string;

        /**
         * If the element has a single child of boolean type, return the value,
         * otherwise throw an exception.
         */
        getValueAsBool():boolean;

        /**
         * If the element has a single child of numeric type, return the value,
         * otherwise throw an exception.
         */
        getValueAsNumber():number;

        /**
         * If the element has a single child of string type, return the value,
         * otherwise throw an exception.
         */
        getValueAsString():string;

        /**
         * Get the child of the element with the given identifier.
         * or throw an exception if it does not exist.
         * @param elementName
         */
        getChildAsSerializedObject(elementName:string):ISerializedObject;

        /**
         * Get an array with the children of the element with the given identifier.
         * If no child exists, an empty array is returned.
         * @param elementName
         */
        getChildrenAsSerializedObjects(elementName:string):ISerializedObject[];
    }
}
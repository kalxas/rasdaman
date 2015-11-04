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

///<reference path="ISerializedObject.ts"/>
///<reference path="ResponseDocument.ts"/>
///<reference path="IllegalArgumentException.ts"/>
///<reference path="LogicException.ts"/>
///<reference path="NotImplementedException.ts"/>
///<reference path="InvalidAttributeNameException.ts"/>
///<reference path="InvalidElementNameException.ts"/>

module rasdaman.common {

    //Declare the XML to JSON parser so that typescript does not complain.
    declare var xmlToJSON:any;

    export class XMLSerializedObject implements ISerializedObject {
        private jsonObject:any;

        public constructor(document:ResponseDocument);
        public constructor(jsonObject:Object);

        public constructor(documentOrObject:any) {
            // These are the option defaults
            var options = {
                mergeCDATA: true,   // extract cdata and merge with text nodes
                grokAttr: true,     // convert truthy attributes to boolean, etc
                grokText: true,     // convert truthy text/attr to boolean, etc
                normalize: true,    // collapse multiple spaces to single space
                xmlns: true,        // include namespaces as attributes in output
                namespaceKey: '_ns',    // tag name for namespace objects
                textKey: '_text',   // tag name for text nodes
                valueKey: '_value',     // tag name for attribute values
                attrKey: '_attr',   // tag for attr groups
                cdataKey: '_cdata',  // tag for cdata nodes (ignored if mergeCDATA is true)
                attrsAsObject: true,    // if false, key is used as prefix to name, set prefix to '' to merge children and attrs.
                stripAttrPrefix: true,  // remove namespace prefixes from attributes
                stripElemPrefix: true,  // for elements of same name in diff namespaces, you can enable namespaces and access the nskey property
                childrenAsArray: true   // force children into arrays
            };

            if (documentOrObject instanceof ResponseDocument) {
                this.jsonObject = xmlToJSON.parseString(documentOrObject.Value, options);
                for (var key in this.jsonObject) {
                    if (this.jsonObject.hasOwnProperty(key) && key != "_proto") {
                        this.jsonObject = this.jsonObject[key][0];
                        break;
                    }
                }

            } else if (documentOrObject instanceof Object) {
                this.jsonObject = documentOrObject;
            } else {
                throw new IllegalArgumentException("The object passed to the XMLSerializedObject constructor is invalid.");
            }
        }

        public doesAttributeExist(attributeName:string):boolean {
            var resolvedAttrName = this.resolveAttributeName(attributeName);

            return (this.jsonObject._attr && typeof this.jsonObject._attr[resolvedAttrName] != "undefined");
        }

        public doesElementExist(elementName:string):boolean {
            var resolvedElementName = this.resolveElementName(elementName);

            return (this.jsonObject[resolvedElementName]
            && typeof this.jsonObject[resolvedElementName][0] != "undefined");
        }

        public getAttributeAsBool(attributeName:string):boolean {
            var resolvedAttrName = this.resolveAttributeName(attributeName);

            if (!this.doesAttributeExist(resolvedAttrName)) {
                throw new InvalidAttributeNameException(attributeName);
            } else {
                return this.jsonObject._attr[resolvedAttrName]._value ? true : false;
            }
        }

        public getAttributeAsNumber(attributeName:string):number {
            var resolvedAttrName = this.resolveAttributeName(attributeName);

            if (!this.doesAttributeExist(resolvedAttrName)) {
                throw new InvalidAttributeNameException(attributeName);
            } else {
                return this.jsonObject._attr[resolvedAttrName]._value;
            }
        }

        public getAttributeAsString(attributeName:string):string {
            var resolvedAttrName = this.resolveAttributeName(attributeName);

            if (!this.doesAttributeExist(resolvedAttrName)) {
                throw new InvalidAttributeNameException(attributeName);
            } else {
                return this.jsonObject._attr[resolvedAttrName]._value;
            }
        }

        public getValueAsBool():boolean {
            if (typeof this.jsonObject._text == "undefined") {
                throw new LogicException("The object does not have a boolean value.");
            }

            return this.jsonObject._text ? true : false;
        }

        public getValueAsNumber():number {
            if (typeof (this.jsonObject._text) == "undefined") {
                throw new LogicException("The object does not have a number value.");
            }

            return this.jsonObject._text;
        }

        public getValueAsString():string {
            if (typeof this.jsonObject._text == "undefined") {
                throw new LogicException("The object does not have a string value.");
            }

            return this.jsonObject._text;
        }

        public getChildAsSerializedObject(elementName:string):rasdaman.common.ISerializedObject {
            var resolvedElementName = this.resolveElementName(elementName);

            if (!this.doesElementExist(resolvedElementName)) {
                throw new InvalidElementNameException(elementName);
            } else {
                return new XMLSerializedObject(this.jsonObject[resolvedElementName][0]);
            }
        }

        public getChildrenAsSerializedObjects(elementName:string):rasdaman.common.ISerializedObject[] {
            var resolvedElementName = this.resolveElementName(elementName);
            var result:XMLSerializedObject[] = [];

            if (typeof this.jsonObject[resolvedElementName] != "undefined") {
                for (var i = 0; i < this.jsonObject[resolvedElementName].length; ++i) {
                    result.push(new XMLSerializedObject(this.jsonObject[resolvedElementName][i]));
                }
            }

            return result;
        }

        private resolveAttributeName(attrName:string):string {
            if (!attrName || /\s/g.test(attrName)) {
                throw new rasdaman.common.IllegalArgumentException("An attribute name must not contain whitespace and it must not be empty.");
            }

            if (!this.jsonObject._attr) {
                //If there are no attributes on this element, there is no point
                //to try to resolve the name
                return attrName;
            }

            //Get the tags
            var nameWithNamespaces:string[] = attrName.split(":");

            var resolvedName = nameWithNamespaces[nameWithNamespaces.length - 1];
            for (var i = nameWithNamespaces.length - 2; i >= 0; --i) {
                if (typeof this.jsonObject._attr[resolvedName] != "undefined") {
                    return resolvedName;
                } else {
                    resolvedName = nameWithNamespaces[i] + ":" + resolvedName;
                }
            }

            return resolvedName;
        }

        /**
         * Try to resolve the element name against the children of the element.
         * If no resolution is found, the initial value is returned.
         * @param elementName
         * @returns {string}
         */
        private resolveElementName(elementName:string):string {
            if (!elementName || /\s/g.test(elementName)) {
                throw new rasdaman.common.IllegalArgumentException("An element name must not contain whitespace and it must not be empty.");
            }

            //Get the tags
            var nameWithNamespaces:string[] = elementName.split(":");

            var resolvedName = nameWithNamespaces[nameWithNamespaces.length - 1];
            for (var i = nameWithNamespaces.length - 2; i >= 0; --i) {
                if (typeof this.jsonObject[resolvedName] != "undefined") {
                    return resolvedName;
                } else {
                    resolvedName = nameWithNamespaces[i] + ":" + resolvedName;
                }
            }

            return resolvedName;
        }
    }
}
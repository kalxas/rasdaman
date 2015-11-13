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
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        var Exception = (function (_super) {
            __extends(Exception, _super);
            function Exception(message) {
                _super.call(this, message);
                this.name = "Exception";
                this.message = message;
                this.stack = (new Error()).stack;
            }
            Exception.prototype.toString = function () {
                return this.name + ": " + this.message;
            };
            return Exception;
        })(Error);
        common.Exception = Exception;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
///<reference path="Exception.ts"/>
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        var InvalidAttributeNameException = (function (_super) {
            __extends(InvalidAttributeNameException, _super);
            function InvalidAttributeNameException(attributeName) {
                _super.call(this, "The attribute \"" + attributeName + "\" does not exist on this element.");
            }
            return InvalidAttributeNameException;
        })(common.Exception);
        common.InvalidAttributeNameException = InvalidAttributeNameException;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
///<reference path="Exception.ts"/>
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        var InvalidElementNameException = (function (_super) {
            __extends(InvalidElementNameException, _super);
            function InvalidElementNameException(elementName) {
                _super.call(this, "The child element \"" + elementName + "\" does not exist on this element.");
            }
            return InvalidElementNameException;
        })(common.Exception);
        common.InvalidElementNameException = InvalidElementNameException;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
///<reference path="Exception.ts"/>
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        var LogicException = (function (_super) {
            __extends(LogicException, _super);
            function LogicException(message) {
                _super.call(this, message);
                this.name = "LogicException";
            }
            return LogicException;
        })(common.Exception);
        common.LogicException = LogicException;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
///<reference path="Exception.ts"/>
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        var NotImplementedException = (function (_super) {
            __extends(NotImplementedException, _super);
            function NotImplementedException() {
                _super.call(this, "The method was not implemented.");
                this.name = "NotImplementedException";
            }
            return NotImplementedException;
        })(common.Exception);
        common.NotImplementedException = NotImplementedException;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
/// <reference path="Exception.ts"/>
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        var IllegalArgumentException = (function (_super) {
            __extends(IllegalArgumentException, _super);
            function IllegalArgumentException(message) {
                _super.call(this, message);
                this.name = "IllegalArgumentException";
            }
            return IllegalArgumentException;
        })(common.Exception);
        common.IllegalArgumentException = IllegalArgumentException;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
/// <reference path="IllegalArgumentException.ts"/>
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        var ArgumentValidator = (function () {
            function ArgumentValidator() {
            }
            ArgumentValidator.isNotNull = function (arg, argName) {
                if (!arg) {
                    throw new common.IllegalArgumentException(argName);
                }
            };
            ArgumentValidator.isNotEmpty = function (arg, argName) {
                if (!arg) {
                    throw new common.IllegalArgumentException(argName);
                }
            };
            ArgumentValidator.isArray = function (arg, argName) {
                if (!Array.isArray(arg)) {
                    throw new common.IllegalArgumentException(argName);
                }
            };
            return ArgumentValidator;
        })();
        common.ArgumentValidator = ArgumentValidator;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        var ImageUtilities = (function () {
            function ImageUtilities() {
            }
            ImageUtilities.arrayBufferToBase64 = function (arrayBuffer) {
                var base64 = '';
                var encodings = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';
                var bytes = new Uint8Array(arrayBuffer);
                var byteLength = bytes.byteLength;
                var byteRemainder = byteLength % 3;
                var mainLength = byteLength - byteRemainder;
                var a, b, c, d;
                var chunk;
                for (var i = 0; i < mainLength; i = i + 3) {
                    chunk = (bytes[i] << 16) | (bytes[i + 1] << 8) | bytes[i + 2];
                    a = (chunk & 16515072) >> 18;
                    b = (chunk & 258048) >> 12;
                    c = (chunk & 4032) >> 6;
                    d = chunk & 63;
                    base64 += encodings[a] + encodings[b] + encodings[c] + encodings[d];
                }
                if (byteRemainder == 1) {
                    chunk = bytes[mainLength];
                    a = (chunk & 252) >> 2;
                    b = (chunk & 3) << 4;
                    base64 += encodings[a] + encodings[b] + '==';
                }
                else if (byteRemainder == 2) {
                    chunk = (bytes[mainLength] << 8) | bytes[mainLength + 1];
                    a = (chunk & 64512) >> 10;
                    b = (chunk & 1008) >> 4;
                    c = (chunk & 15) << 2;
                    base64 += encodings[a] + encodings[b] + encodings[c] + '=';
                }
                return base64;
            };
            return ImageUtilities;
        })();
        common.ImageUtilities = ImageUtilities;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        (function (ResponseDocumentType) {
            ResponseDocumentType[ResponseDocumentType["XML"] = 1] = "XML";
            ResponseDocumentType[ResponseDocumentType["SOAP"] = 2] = "SOAP";
            ResponseDocumentType[ResponseDocumentType["JSON"] = 3] = "JSON";
        })(common.ResponseDocumentType || (common.ResponseDocumentType = {}));
        var ResponseDocumentType = common.ResponseDocumentType;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
///<reference path="ResponseDocumentType.ts"/>
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        var ResponseDocument = (function () {
            function ResponseDocument(value, responseType) {
                this.Value = value;
                this.Type = responseType;
            }
            return ResponseDocument;
        })();
        common.ResponseDocument = ResponseDocument;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        var Response = (function () {
            function Response(document, value) {
                this.Document = document;
                this.Value = value;
            }
            return Response;
        })();
        common.Response = Response;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        var XMLSerializedObject = (function () {
            function XMLSerializedObject(documentOrObject) {
                var options = {
                    mergeCDATA: true,
                    grokAttr: true,
                    grokText: true,
                    normalize: true,
                    xmlns: true,
                    namespaceKey: '_ns',
                    textKey: '_text',
                    valueKey: '_value',
                    attrKey: '_attr',
                    cdataKey: '_cdata',
                    attrsAsObject: true,
                    stripAttrPrefix: true,
                    stripElemPrefix: true,
                    childrenAsArray: true
                };
                if (documentOrObject instanceof common.ResponseDocument) {
                    this.jsonObject = xmlToJSON.parseString(documentOrObject.Value, options);
                    for (var key in this.jsonObject) {
                        if (this.jsonObject.hasOwnProperty(key) && key != "_proto") {
                            this.jsonObject = this.jsonObject[key][0];
                            break;
                        }
                    }
                }
                else if (documentOrObject instanceof Object) {
                    this.jsonObject = documentOrObject;
                }
                else {
                    throw new common.IllegalArgumentException("The object passed to the XMLSerializedObject constructor is invalid.");
                }
            }
            XMLSerializedObject.prototype.doesAttributeExist = function (attributeName) {
                var resolvedAttrName = this.resolveAttributeName(attributeName);
                return (this.jsonObject._attr && typeof this.jsonObject._attr[resolvedAttrName] != "undefined");
            };
            XMLSerializedObject.prototype.doesElementExist = function (elementName) {
                var resolvedElementName = this.resolveElementName(elementName);
                return (this.jsonObject[resolvedElementName]
                    && typeof this.jsonObject[resolvedElementName][0] != "undefined");
            };
            XMLSerializedObject.prototype.getAttributeAsBool = function (attributeName) {
                var resolvedAttrName = this.resolveAttributeName(attributeName);
                if (!this.doesAttributeExist(resolvedAttrName)) {
                    throw new common.InvalidAttributeNameException(attributeName);
                }
                else {
                    return this.jsonObject._attr[resolvedAttrName]._value ? true : false;
                }
            };
            XMLSerializedObject.prototype.getAttributeAsNumber = function (attributeName) {
                var resolvedAttrName = this.resolveAttributeName(attributeName);
                if (!this.doesAttributeExist(resolvedAttrName)) {
                    throw new common.InvalidAttributeNameException(attributeName);
                }
                else {
                    return this.jsonObject._attr[resolvedAttrName]._value;
                }
            };
            XMLSerializedObject.prototype.getAttributeAsString = function (attributeName) {
                var resolvedAttrName = this.resolveAttributeName(attributeName);
                if (!this.doesAttributeExist(resolvedAttrName)) {
                    throw new common.InvalidAttributeNameException(attributeName);
                }
                else {
                    return this.jsonObject._attr[resolvedAttrName]._value;
                }
            };
            XMLSerializedObject.prototype.getValueAsBool = function () {
                if (typeof this.jsonObject._text == "undefined") {
                    throw new common.LogicException("The object does not have a boolean value.");
                }
                return this.jsonObject._text ? true : false;
            };
            XMLSerializedObject.prototype.getValueAsNumber = function () {
                if (typeof (this.jsonObject._text) == "undefined") {
                    throw new common.LogicException("The object does not have a number value.");
                }
                return this.jsonObject._text;
            };
            XMLSerializedObject.prototype.getValueAsString = function () {
                if (typeof this.jsonObject._text == "undefined") {
                    throw new common.LogicException("The object does not have a string value.");
                }
                return this.jsonObject._text;
            };
            XMLSerializedObject.prototype.getChildAsSerializedObject = function (elementName) {
                var resolvedElementName = this.resolveElementName(elementName);
                if (!this.doesElementExist(resolvedElementName)) {
                    throw new common.InvalidElementNameException(elementName);
                }
                else {
                    return new XMLSerializedObject(this.jsonObject[resolvedElementName][0]);
                }
            };
            XMLSerializedObject.prototype.getChildrenAsSerializedObjects = function (elementName) {
                var resolvedElementName = this.resolveElementName(elementName);
                var result = [];
                if (typeof this.jsonObject[resolvedElementName] != "undefined") {
                    for (var i = 0; i < this.jsonObject[resolvedElementName].length; ++i) {
                        result.push(new XMLSerializedObject(this.jsonObject[resolvedElementName][i]));
                    }
                }
                return result;
            };
            XMLSerializedObject.prototype.resolveAttributeName = function (attrName) {
                if (!attrName || /\s/g.test(attrName)) {
                    throw new rasdaman.common.IllegalArgumentException("An attribute name must not contain whitespace and it must not be empty.");
                }
                if (!this.jsonObject._attr) {
                    return attrName;
                }
                var nameWithNamespaces = attrName.split(":");
                var resolvedName = nameWithNamespaces[nameWithNamespaces.length - 1];
                for (var i = nameWithNamespaces.length - 2; i >= 0; --i) {
                    if (typeof this.jsonObject._attr[resolvedName] != "undefined") {
                        return resolvedName;
                    }
                    else {
                        resolvedName = nameWithNamespaces[i] + ":" + resolvedName;
                    }
                }
                return resolvedName;
            };
            XMLSerializedObject.prototype.resolveElementName = function (elementName) {
                if (!elementName || /\s/g.test(elementName)) {
                    throw new rasdaman.common.IllegalArgumentException("An element name must not contain whitespace and it must not be empty.");
                }
                var nameWithNamespaces = elementName.split(":");
                var resolvedName = nameWithNamespaces[nameWithNamespaces.length - 1];
                for (var i = nameWithNamespaces.length - 2; i >= 0; --i) {
                    if (typeof this.jsonObject[resolvedName] != "undefined") {
                        return resolvedName;
                    }
                    else {
                        resolvedName = nameWithNamespaces[i] + ":" + resolvedName;
                    }
                }
                return resolvedName;
            };
            return XMLSerializedObject;
        })();
        common.XMLSerializedObject = XMLSerializedObject;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
///<reference path="XMLSerializedObject.ts"/>
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        var SerializedObjectFactory = (function () {
            function SerializedObjectFactory() {
            }
            SerializedObjectFactory.prototype.getSerializedObject = function (document) {
                if (document.Type == common.ResponseDocumentType.XML) {
                    return new common.XMLSerializedObject(document);
                }
                else {
                    throw new common.NotImplementedException();
                }
            };
            return SerializedObjectFactory;
        })();
        common.SerializedObjectFactory = SerializedObjectFactory;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../../../assets/typings/tsd.d.ts"/>
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        function escapeXml(unsafe) {
            return unsafe.replace(/[<>&'"]/g, function (c) {
                switch (c) {
                    case '<':
                        return '&lt;';
                    case '>':
                        return '&gt;';
                    case '&':
                        return '&amp;';
                    case '\'':
                        return '&apos;';
                    case '"':
                        return '&quot;';
                }
            });
        }
        function PrettyPrint($sanitize, $sce) {
            return {
                restrict: 'EC',
                scope: {
                    data: "="
                },
                templateUrl: "src/common/directives/pretty-print/PrettyPrintTemplate.html",
                link: function (scope, element, attrs) {
                    scope.$watch("data", function (newData, oldValue) {
                        if (newData && newData.Value) {
                            var escapedHtml = prettyPrintOne(escapeXml(newData.Value), newData.Type, true);
                            scope.document = $sce.trustAsHtml(escapedHtml);
                        }
                    }, true);
                }
            };
        }
        common.PrettyPrint = PrettyPrint;
        PrettyPrint.$inject = ["$sanitize", "$sce"];
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../../../assets/typings/tsd.d.ts"/>
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        function StringToNumberConverter() {
            return {
                require: 'ngModel',
                link: function (scope, elem, attributes, ngModel) {
                    ngModel.$parsers.push(function (value) {
                        return '' + value;
                    });
                    ngModel.$formatters.push(function (value) {
                        return parseFloat(value);
                    });
                }
            };
        }
        common.StringToNumberConverter = StringToNumberConverter;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../../../assets/typings/tsd.d.ts"/>
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        function Autocomplete($timeout) {
            return {
                restrict: "A",
                scope: {
                    source: "=source"
                },
                link: function (scope, elem, attributes) {
                    scope.$watch("source", function (newValue, oldValue) {
                        if (elem.autocomplete("instance")) {
                            elem.autocomplete("destroy");
                        }
                        if (newValue) {
                            elem.autocomplete({
                                source: newValue,
                                select: function () {
                                    $timeout(function () {
                                        elem.trigger('input');
                                    }, 100);
                                }
                            });
                        }
                    });
                }
            };
        }
        common.Autocomplete = Autocomplete;
        Autocomplete.$inject = ["$timeout"];
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
/// <reference path="pretty-print/PrettyPrint.ts"/>
/// <reference path="string-to-number-converter/StringToNumberConverter.ts"/>
/// <reference path="autocomplete/Autocomplete.ts"/>
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
///<reference path="Exception.ts"/>
///<reference path="InvalidAttributeNameException.ts"/>
///<reference path="InvalidElementNameException.ts"/>
///<reference path="LogicException.ts"/>
///<reference path="NotImplementedException.ts"/>
///<reference path="IllegalArgumentException.ts"/>
///<reference path="ArgumentValidator.ts"/>
///<reference path="ImageUtilities.ts"/>
///<reference path="ResponseDocumentType.ts"/>
///<reference path="ResponseDocument.ts"/>
///<reference path="ISerializable.ts"/>
///<reference path="Response.ts"/>
///<reference path="ISerializedObject.ts"/>
///<reference path="SerializedObjectFactory.ts"/>
///<reference path="XMLSerializedObject"/>
///<reference path="directives/_directives.ts"/>
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
var rasdaman;
(function (rasdaman) {
    var Constants = (function () {
        function Constants() {
        }
        Constants.APP_NAME = "wcsClient";
        Constants.PROCESSING_EXT_URI = "http://www.opengis.net/spec/WCS_service-extension_processing/2.0/conf/processing";
        Constants.TRANSACTION_EXT_URI = "http://www.opengis.net/spec/WCS_service-extension_transaction/2.0/conf/insert+delete";
        return Constants;
    })();
    rasdaman.Constants = Constants;
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../common/_common.ts"/>
var ows;
(function (ows) {
    var Address = (function () {
        function Address(source) {
            var _this = this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.DeliveryPoint = [];
            source.getChildrenAsSerializedObjects("ows:DeliveryPoint").forEach(function (o) {
                _this.DeliveryPoint.push(o.getValueAsString());
            });
            if (source.doesElementExist("ows:City")) {
                this.City = source.getChildAsSerializedObject("ows:City").getValueAsString();
            }
            if (source.doesElementExist("ows:AdministrativeArea")) {
                this.AdministrativeArea = source.getChildAsSerializedObject("ows:AdministrativeArea").getValueAsString();
            }
            if (source.doesElementExist("ows:PostalCode")) {
                this.PostalCode = source.getChildAsSerializedObject("ows:PostalCode").getValueAsString();
            }
            if (source.doesElementExist("ows:Country")) {
                this.Country = source.getChildAsSerializedObject("ows:Country").getValueAsString();
            }
            this.ElectronicMailAddress = [];
            source.getChildrenAsSerializedObjects("ows:ElectronicMailAddress").forEach(function (o) {
                _this.ElectronicMailAddress.push(o.getValueAsString());
            });
        }
        return Address;
    })();
    ows.Address = Address;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
var ows;
(function (ows) {
    var BoundingBox = (function () {
        function BoundingBox(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return BoundingBox;
    })();
    ows.BoundingBox = BoundingBox;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
var ows;
(function (ows) {
    var LanguageString = (function () {
        function LanguageString(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.Value = source.getValueAsString();
            if (source.doesAttributeExist("xml:lang")) {
                this.Lang = source.getAttributeAsString("xml:lang");
            }
        }
        return LanguageString;
    })();
    ows.LanguageString = LanguageString;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
var ows;
(function (ows) {
    var OnlineResource = (function () {
        function OnlineResource(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            if (source.doesAttributeExist("xlink:actuate")) {
                this.Actuate = source.getAttributeAsString("xlink:actuate");
            }
            if (source.doesAttributeExist("xlink:acrole")) {
                this.Acrole = source.getAttributeAsString("xlink:acrole");
            }
            if (source.doesAttributeExist("xlink:href")) {
                this.Href = source.getAttributeAsString("xlink:href");
            }
            if (source.doesAttributeExist("xlink:role")) {
                this.Role = source.getAttributeAsString("xlink:role");
            }
            if (source.doesAttributeExist("xlink:show")) {
                this.Show = source.getAttributeAsString("xlink:show");
            }
            if (source.doesAttributeExist("xlink:title")) {
                this.Title = source.getAttributeAsString("xlink:title");
            }
            if (source.doesAttributeExist("xlink:type")) {
                this.Type = source.getAttributeAsString("xlink:type");
            }
        }
        return OnlineResource;
    })();
    ows.OnlineResource = OnlineResource;
})(ows || (ows = {}));
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
///<reference path="OnlineResource.ts"/>
var ows;
(function (ows) {
    var Uri = (function () {
        function Uri(uri) {
        }
        return Uri;
    })();
    ows.Uri = Uri;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="Uri.ts"/>
var ows;
(function (ows) {
    var Code = (function () {
        function Code(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.Code = source.getValueAsString();
            if (source.doesAttributeExist("codeSpace")) {
                this.CodeSpace = new ows.Uri(source.getAttributeAsString("codeSpace"));
            }
        }
        return Code;
    })();
    ows.Code = Code;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="LanguageString.ts"/>
///<reference path="Code.ts"/>
var ows;
(function (ows) {
    var Keywords = (function () {
        function Keywords(source) {
            var _this = this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.Keyword = [];
            source.getChildrenAsSerializedObjects("ows:Keyword").forEach(function (s) {
                _this.Keyword.push(new ows.LanguageString(s));
            });
            this.Type = new ows.Code(source.getChildAsSerializedObject("ows:Type"));
        }
        return Keywords;
    })();
    ows.Keywords = Keywords;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="LanguageString.ts"/>
///<reference path="Keywords.ts"/>
var ows;
(function (ows) {
    var Description = (function () {
        function Description(source) {
            var _this = this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.Title = [];
            source.getChildrenAsSerializedObjects("ows:Title").forEach(function (s) {
                _this.Title.push(new ows.LanguageString(s));
            });
            this.Abstract = [];
            source.getChildrenAsSerializedObjects("ows:Abstract").forEach(function (s) {
                _this.Abstract.push(new ows.LanguageString(s));
            });
            this.Keywords = [];
            source.getChildrenAsSerializedObjects("ows:Keywords").forEach(function (s) {
                _this.Keywords.push(new ows.Keywords(s));
            });
        }
        return Description;
    })();
    ows.Description = Description;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="Description.ts"/>
///<reference path="Code.ts"/>
var ows;
(function (ows) {
    var ServiceIdentification = (function (_super) {
        __extends(ServiceIdentification, _super);
        function ServiceIdentification(source) {
            var _this = this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            _super.call(this, source);
            this.ServiceType = new ows.Code(source.getChildAsSerializedObject("ServiceType"));
            this.ServiceTypeVersion = [];
            source.getChildrenAsSerializedObjects("ows:ServiceTypeVersion").forEach(function (s) {
                _this.ServiceTypeVersion.push(s.getValueAsString());
            });
            this.Profile = [];
            source.getChildrenAsSerializedObjects("ows:Profile").forEach(function (s) {
                _this.Profile.push(s.getValueAsString());
            });
            if (source.doesElementExist("ows:Fees")) {
                this.Fees = source.getChildAsSerializedObject("ows:Fees").getValueAsString();
            }
            if (source.doesElementExist("ows:AccessConstraints")) {
                this.AccessConstraints = source.getChildAsSerializedObject("ows:AccessConstraints").getValueAsString();
            }
        }
        return ServiceIdentification;
    })(ows.Description);
    ows.ServiceIdentification = ServiceIdentification;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
var ows;
(function (ows) {
    var Phone = (function () {
        function Phone(source) {
            var _this = this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.Voice = [];
            source.getChildrenAsSerializedObjects("ows:Voice").forEach(function (s) {
                _this.Voice.push(s.getValueAsString());
            });
            this.Facsimile = [];
            source.getChildrenAsSerializedObjects("ows:Facsimile").forEach(function (s) {
                _this.Facsimile.push(s.getValueAsString());
            });
        }
        return Phone;
    })();
    ows.Phone = Phone;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="Phone.ts"/>
///<reference path="Address.ts"/>
///<reference path="OnlineResource.ts"/>
var ows;
(function (ows) {
    var ContactInfo = (function () {
        function ContactInfo(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            if (source.doesElementExist("ows:Phone")) {
                this.Phone = new ows.Phone(source.getChildAsSerializedObject("ows:Phone"));
            }
            if (source.doesElementExist("ows:Address")) {
                this.Address = new ows.Address(source.getChildAsSerializedObject("ows:Address"));
            }
            if (source.doesElementExist("ows:OnlineResource")) {
                this.OnlineResource = new ows.OnlineResource(source.getChildAsSerializedObject("ows:OnlineResource"));
            }
            if (source.doesElementExist("ows:HoursOfService")) {
                this.HoursOfService = source.getChildAsSerializedObject("ows:HoursOfService").getValueAsString();
            }
            if (source.doesElementExist("ows:ContactInstructions")) {
                this.ContactInstructions = source.getChildAsSerializedObject("ows:ContactInstructions").getValueAsString();
            }
        }
        return ContactInfo;
    })();
    ows.ContactInfo = ContactInfo;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="ContactInfo.ts"/>
///<reference path="Code.ts"/>
var ows;
(function (ows) {
    var ResponsiblePartySubset = (function () {
        function ResponsiblePartySubset(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            if (source.doesElementExist("ows:IndividualName")) {
                this.IndividualName = source.getChildAsSerializedObject("ows:IndividualName").getValueAsString();
            }
            if (source.doesElementExist("ows:PositionName")) {
                this.PositionName = source.getChildAsSerializedObject("ows:PositionName").getValueAsString();
            }
            if (source.doesElementExist("ows:Role")) {
                this.Role = new ows.Code(source.getChildAsSerializedObject("ows:Role"));
            }
            if (source.doesElementExist("ows:ContactInfo")) {
                this.ContactInfo = new ows.ContactInfo(source.getChildAsSerializedObject("ows:ContactInfo"));
            }
        }
        return ResponsiblePartySubset;
    })();
    ows.ResponsiblePartySubset = ResponsiblePartySubset;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="ResponsiblePartySubset.ts"/>
var ows;
(function (ows) {
    var ServiceContact = (function (_super) {
        __extends(ServiceContact, _super);
        function ServiceContact(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            _super.call(this, source);
        }
        return ServiceContact;
    })(ows.ResponsiblePartySubset);
    ows.ServiceContact = ServiceContact;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="OnlineResource.ts"/>
///<reference path="ServiceContact.ts"/>
var ows;
(function (ows) {
    var ServiceProvider = (function () {
        function ServiceProvider(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            if (source.doesElementExist("ows:ProviderName")) {
                this.ProviderName = source.getChildAsSerializedObject("ows:ProviderName").getValueAsString();
            }
            if (source.doesElementExist("ows:ProviderSite")) {
                this.ProviderSite = new ows.OnlineResource(source.getChildAsSerializedObject("ows:ProviderSite"));
            }
            if (source.doesElementExist("ows:ServiceContact")) {
                this.ServiceContact = new ows.ServiceContact(source.getChildAsSerializedObject("ows:ServiceContact"));
            }
        }
        return ServiceProvider;
    })();
    ows.ServiceProvider = ServiceProvider;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
var ows;
(function (ows) {
    var Constraint = (function () {
        function Constraint(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return Constraint;
    })();
    ows.Constraint = Constraint;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="Constraint.ts"/>
///<reference path="OnlineResource.ts"/>
var ows;
(function (ows) {
    var RequestMethod = (function (_super) {
        __extends(RequestMethod, _super);
        function RequestMethod(source) {
            var _this = this;
            _super.call(this, source);
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.Constraint = [];
            source.getChildrenAsSerializedObjects("ows:Constraint").forEach(function (o) {
                _this.Constraint.push(new ows.Constraint(o));
            });
        }
        return RequestMethod;
    })(ows.OnlineResource);
    ows.RequestMethod = RequestMethod;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="RequestMethod.ts"/>
var ows;
(function (ows) {
    var Get = (function (_super) {
        __extends(Get, _super);
        function Get(source) {
            _super.call(this, source);
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return Get;
    })(ows.RequestMethod);
    ows.Get = Get;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="RequestMethod.ts"/>
var ows;
(function (ows) {
    var Post = (function (_super) {
        __extends(Post, _super);
        function Post(source) {
            _super.call(this, source);
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return Post;
    })(ows.RequestMethod);
    ows.Post = Post;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="Get.ts"/>
///<reference path="Post.ts"/>
var ows;
(function (ows) {
    var HTTP = (function () {
        function HTTP(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.Get = new ows.Get(source.getChildAsSerializedObject("ows:Get"));
            this.Post = new ows.Post(source.getChildAsSerializedObject("ows:Post"));
        }
        return HTTP;
    })();
    ows.HTTP = HTTP;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="HTTP.ts"/>
var ows;
(function (ows) {
    var DCP = (function () {
        function DCP(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.HTTP = new ows.HTTP(source.getChildAsSerializedObject("ows:HTTP"));
        }
        return DCP;
    })();
    ows.DCP = DCP;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
var ows;
(function (ows) {
    var Parameter = (function () {
        function Parameter(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return Parameter;
    })();
    ows.Parameter = Parameter;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
var ows;
(function (ows) {
    var Metadata = (function () {
        function Metadata(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return Metadata;
    })();
    ows.Metadata = Metadata;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="DCP.ts"/>
///<reference path="Parameter.ts"/>
///<reference path="Constraint.ts"/>
///<reference path="Metadata.ts"/>
var ows;
(function (ows) {
    var Operation = (function () {
        function Operation(source) {
            var _this = this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.Name = source.getAttributeAsString("name");
            this.DCP = [];
            source.getChildrenAsSerializedObjects("ows:DCP").forEach(function (o) {
                _this.DCP.push(new ows.DCP(o));
            });
            this.Parameter = [];
            source.getChildrenAsSerializedObjects("ows:Parameter").forEach(function (o) {
                _this.Parameter.push(new ows.Parameter(o));
            });
            this.Constraint = [];
            source.getChildrenAsSerializedObjects("ows:Constraint").forEach(function (o) {
                _this.Constraint.push(new ows.Constraint(o));
            });
            this.Metadata = [];
            source.getChildrenAsSerializedObjects("ows:Metadata").forEach(function (o) {
                _this.Metadata.push(new ows.Metadata(o));
            });
        }
        return Operation;
    })();
    ows.Operation = Operation;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
var ows;
(function (ows) {
    var ExtendedCapabilities = (function () {
        function ExtendedCapabilities(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return ExtendedCapabilities;
    })();
    ows.ExtendedCapabilities = ExtendedCapabilities;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="Operation.ts"/>
///<reference path="Parameter.ts"/>
///<reference path="Constraint.ts"/>
///<reference path="ExtendedCapabilities.ts"/>
var ows;
(function (ows) {
    var OperationsMetadata = (function () {
        function OperationsMetadata(source) {
            var _this = this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.Operation = [];
            source.getChildrenAsSerializedObjects("ows:Operation").forEach(function (o) {
                _this.Operation.push(new ows.Operation(o));
            });
            this.Parameter = [];
            source.getChildrenAsSerializedObjects("ows:Parameter").forEach(function (o) {
                _this.Parameter.push(new ows.Parameter(o));
            });
            this.Constraint = [];
            source.getChildrenAsSerializedObjects("ows:Constraint").forEach(function (o) {
                _this.Constraint.push(new ows.Constraint(o));
            });
            if (source.doesElementExist("ows:ExtendedCapabilities")) {
                this.ExtendedCapabilities = new ows.ExtendedCapabilities(source.getChildAsSerializedObject("ows:ExtendedCapabilities"));
            }
        }
        return OperationsMetadata;
    })();
    ows.OperationsMetadata = OperationsMetadata;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
var ows;
(function (ows) {
    var Languages = (function () {
        function Languages(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return Languages;
    })();
    ows.Languages = Languages;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="ServiceIdentification.ts"/>
///<reference path="ServiceProvider.ts"/>
///<reference path="OperationsMetadata.ts"/>
///<reference path="Languages.ts"/>
var ows;
(function (ows) {
    var CapabilitiesBase = (function () {
        function CapabilitiesBase(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.Version = source.getAttributeAsString("version");
            if (source.doesAttributeExist("updateSequence")) {
                this.UpdateSequence = source.getAttributeAsString("updateSequence");
            }
            if (source.doesElementExist("ows:ServiceIdentification")) {
                this.ServiceIdentification = new ows.ServiceIdentification(source.getChildAsSerializedObject("ows:ServiceIdentification"));
            }
            if (source.doesElementExist("ows:ServiceProvider")) {
                this.ServiceProvider = new ows.ServiceProvider(source.getChildAsSerializedObject("ows:ServiceProvider"));
            }
            if (source.doesElementExist("ows:OperationsMetadata")) {
                this.OperationsMetadata = new ows.OperationsMetadata(source.getChildAsSerializedObject("ows:OperationsMetadata"));
            }
            if (source.doesElementExist("Languages")) {
                this.Languages = new ows.Languages(source.getChildAsSerializedObject("Languages"));
            }
        }
        return CapabilitiesBase;
    })();
    ows.CapabilitiesBase = CapabilitiesBase;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
var ows;
(function (ows) {
    var ContentsBase = (function () {
        function ContentsBase(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return ContentsBase;
    })();
    ows.ContentsBase = ContentsBase;
})(ows || (ows = {}));
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
///<reference path="../../common/_common.ts"/>
var ows;
(function (ows) {
    var Section = (function () {
        function Section(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return Section;
    })();
    ows.Section = Section;
})(ows || (ows = {}));
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
/// <reference path="Section.ts"/>
var ows;
(function (ows) {
    var GetCapabilities = (function () {
        function GetCapabilities() {
            this.Request = "GetCapabilities";
        }
        return GetCapabilities;
    })();
    ows.GetCapabilities = GetCapabilities;
})(ows || (ows = {}));
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
///<reference path="OnlineResource.ts"/>
var ows;
(function (ows) {
    var WGS84BoundingBox = (function () {
        function WGS84BoundingBox(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return WGS84BoundingBox;
    })();
    ows.WGS84BoundingBox = WGS84BoundingBox;
})(ows || (ows = {}));
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
///<reference path="Address.ts"/>
///<reference path="BoundingBox.ts"/>
///<reference path="CapabilitiesBase.ts"/>
///<reference path="Code.ts"/>
///<reference path="Constraint.ts"/>
///<reference path="ContactInfo.ts"/>
///<reference path="ContentsBase.ts"/>
///<reference path="DCP.ts"/>
///<reference path="Description.ts"/>
///<reference path="ExtendedCapabilities.ts"/>
///<reference path="Get.ts"/>
///<reference path="GetCapabilities.ts"/>
///<reference path="HTTP.ts"/>
///<reference path="Keywords.ts"/>
///<reference path="Languages.ts"/>
///<reference path="LanguageString.ts"/>
///<reference path="Metadata.ts"/>
///<reference path="OnlineResource.ts"/>
///<reference path="Operation.ts"/>
///<reference path="OperationsMetadata.ts"/>
///<reference path="Parameter.ts"/>
///<reference path="Phone.ts"/>
///<reference path="Post.ts"/>
///<reference path="RequestMethod.ts"/>
///<reference path="ResponsiblePartySubset.ts"/>
///<reference path="Section.ts"/>
///<reference path="ServiceContact.ts"/>
///<reference path="ServiceIdentification.ts"/>
///<reference path="ServiceProvider.ts"/>
///<reference path="Uri.ts"/>
///<reference path="WGS84BoundingBox.ts"/>
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
///<reference path="../../common/_common.ts"/>
var wcs;
(function (wcs) {
    var Extension = (function () {
        function Extension(source) {
            if (source.doesElementExist("int:InterpolationMetadata")) {
                this.InterpolationMetadata = new wcs.InterpolationMetadata(source.getChildAsSerializedObject("int:InterpolationMetadata"));
            }
        }
        return Extension;
    })();
    wcs.Extension = Extension;
})(wcs || (wcs = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="Extension.ts"/>
var wcs;
(function (wcs) {
    var ServiceMetadata = (function () {
        function ServiceMetadata(source) {
            var _this = this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.FormatSupported = [];
            source.getChildrenAsSerializedObjects("wcs:formatSupported").forEach(function (o) {
                _this.FormatSupported.push(o.getValueAsString());
            });
            this.Extension = [];
            source.getChildrenAsSerializedObjects("wcs:Extension").forEach(function (o) {
                _this.Extension.push(new wcs.Extension(o));
            });
        }
        return ServiceMetadata;
    })();
    wcs.ServiceMetadata = ServiceMetadata;
})(wcs || (wcs = {}));
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
///<reference path="../../common/_common.ts"/>
var wcs;
(function (wcs) {
    var CoverageSubtypeParent = (function () {
        function CoverageSubtypeParent(source) {
        }
        return CoverageSubtypeParent;
    })();
    wcs.CoverageSubtypeParent = CoverageSubtypeParent;
})(wcs || (wcs = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="../ows/ows_all.ts"/>
///<reference path="CoverageSubtypeParent.ts"/>
var wcs;
(function (wcs) {
    var CoverageSummary = (function (_super) {
        __extends(CoverageSummary, _super);
        function CoverageSummary(source) {
            var _this = this;
            _super.call(this, source);
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.CoverageId = source.getChildAsSerializedObject("wcs:CoverageId").getValueAsString();
            this.CoverageSubtype = source.getChildAsSerializedObject("wcs:CoverageSubtype").getValueAsString();
            if (source.doesElementExist("wcs:CoverageSubtypeParent")) {
                this.CoverageSubtypeParent = new wcs.CoverageSubtypeParent(source.getChildAsSerializedObject("wcs:CoverageSubtypeParent"));
            }
            this.WGS84BoundingBox = [];
            source.getChildrenAsSerializedObjects("ows:WGS84BoundingBox").forEach(function (o) {
                _this.WGS84BoundingBox.push(new ows.WGS84BoundingBox(o));
            });
            this.BoundingBox = [];
            source.getChildrenAsSerializedObjects("ows:BoundingBox").forEach(function (o) {
                _this.BoundingBox.push(new ows.BoundingBox(o));
            });
            this.Metadata = [];
            source.getChildrenAsSerializedObjects("ows:Metadata").forEach(function (o) {
                _this.Metadata.push(new ows.Metadata(o));
            });
        }
        return CoverageSummary;
    })(ows.Description);
    wcs.CoverageSummary = CoverageSummary;
})(wcs || (wcs = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="../ows/ows_all.ts"/>
///<reference path="CoverageSummary.ts"/>
///<reference path="Extension.ts"/>
var wcs;
(function (wcs) {
    var Contents = (function (_super) {
        __extends(Contents, _super);
        function Contents(source) {
            var _this = this;
            _super.call(this, source);
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.CoverageSummary = [];
            source.getChildrenAsSerializedObjects("wcs:CoverageSummary").forEach(function (o) {
                _this.CoverageSummary.push(new wcs.CoverageSummary(o));
            });
            if (source.doesElementExist("wcs.Extension")) {
                this.Extension = new wcs.Extension(source.getChildAsSerializedObject("wcs.Extension"));
            }
        }
        return Contents;
    })(ows.ContentsBase);
    wcs.Contents = Contents;
})(wcs || (wcs = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="../ows/ows_all.ts"/>
///<reference path="ServiceMetadata.ts"/>
///<reference path="Contents.ts"/>
var wcs;
(function (wcs) {
    var Capabilities = (function (_super) {
        __extends(Capabilities, _super);
        function Capabilities(source) {
            _super.call(this, source);
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            if (source.doesElementExist("wcs:ServiceMetadata")) {
                this.ServiceMetadata = new wcs.ServiceMetadata(source.getChildAsSerializedObject("wcs:ServiceMetadata"));
            }
            if (source.doesElementExist("wcs:Contents")) {
                this.Contents = new wcs.Contents(source.getChildAsSerializedObject("wcs:Contents"));
            }
        }
        return Capabilities;
    })(ows.CapabilitiesBase);
    wcs.Capabilities = Capabilities;
})(wcs || (wcs = {}));
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
///<reference path="../../common/_common.ts"/>
var gml;
(function (gml) {
    var Pos = (function () {
        function Pos(source) {
            var _this = this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            if (source.doesAttributeExist("srsName")) {
                this.SrsName = source.getAttributeAsString("srsName");
            }
            if (source.doesAttributeExist("srsDimension")) {
                this.SrsDimension = source.getAttributeAsNumber("srsDimension");
            }
            if (source.doesAttributeExist("axisLabels")) {
                this.AxisLabels = source.getAttributeAsString("axisLabels").split(" ");
            }
            if (source.doesAttributeExist("uomLabels")) {
                this.UomLabels = source.getAttributeAsString("uomLabels").split(" ");
            }
            this.Values = [];
            var stringValues = source.getValueAsString().split(" ");
            stringValues.forEach(function (o) {
                _this.Values.push(parseFloat(o));
            });
        }
        return Pos;
    })();
    gml.Pos = Pos;
})(gml || (gml = {}));
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
///<reference path="../../common/_common.ts"/>
var gml;
(function (gml) {
    var LowerCorner = (function () {
        function LowerCorner(source) {
            var _this = this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            if (source.doesAttributeExist("srsName")) {
                this.SrsName = source.getAttributeAsString("srsName");
            }
            if (source.doesAttributeExist("srsDimension")) {
                this.SrsDimension = source.getAttributeAsNumber("srsDimension");
            }
            if (source.doesAttributeExist("axisLabels")) {
                this.AxisLabels = source.getAttributeAsString("axisLabels").split(" ");
            }
            if (source.doesAttributeExist("uomLabels")) {
                this.UomLabels = source.getAttributeAsString("uomLabels").split(" ");
            }
            this.Values = [];
            var stringValues = source.getValueAsString().split(" ");
            stringValues.forEach(function (o) {
                _this.Values.push(parseFloat(o));
            });
        }
        return LowerCorner;
    })();
    gml.LowerCorner = LowerCorner;
})(gml || (gml = {}));
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
///<reference path="../../common/_common.ts"/>
var gml;
(function (gml) {
    var UpperCorner = (function () {
        function UpperCorner(source) {
            var _this = this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            if (source.doesAttributeExist("srsName")) {
                this.SrsName = source.getAttributeAsString("srsName");
            }
            if (source.doesAttributeExist("srsDimension")) {
                this.SrsDimension = source.getAttributeAsNumber("srsDimension");
            }
            if (source.doesAttributeExist("axisLabels")) {
                this.AxisLabels = source.getAttributeAsString("axisLabels").split(" ");
            }
            if (source.doesAttributeExist("uomLabels")) {
                this.UomLabels = source.getAttributeAsString("uomLabels").split(" ");
            }
            this.Values = [];
            var stringValues = source.getValueAsString().split(" ");
            stringValues.forEach(function (o) {
                _this.Values.push(parseFloat(o));
            });
        }
        return UpperCorner;
    })();
    gml.UpperCorner = UpperCorner;
})(gml || (gml = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="Pos.ts"/>
///<reference path="LowerCorner.ts"/>
///<reference path="UpperCorner.ts"/>
var gml;
(function (gml) {
    var Envelope = (function () {
        function Envelope(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            if (source.doesAttributeExist("srsName")) {
                this.SrsName = source.getAttributeAsString("srsName");
            }
            if (source.doesAttributeExist("srsDimension")) {
                this.SrsDimension = source.getAttributeAsNumber("srsDimension");
            }
            if (source.doesAttributeExist("axisLabels")) {
                this.AxisLabels = source.getAttributeAsString("axisLabels").split(" ");
            }
            if (source.doesAttributeExist("uomLabels")) {
                this.UomLabels = source.getAttributeAsString("uomLabels").split(" ");
            }
            if (source.doesAttributeExist("frame")) {
                this.Frame = source.getAttributeAsString("frame");
            }
            if (source.doesElementExist("gml:lowerCorner")) {
                this.LowerCorner = new gml.LowerCorner(source.getChildAsSerializedObject("gml:lowerCorner"));
            }
            if (source.doesElementExist("gml:upperCorner")) {
                this.UpperCorner = new gml.UpperCorner(source.getChildAsSerializedObject("gml:upperCorner"));
            }
            if (source.doesElementExist("gml:pos")) {
                this.Pos = new gml.Pos(source.getChildAsSerializedObject("gml:pos"));
            }
        }
        return Envelope;
    })();
    gml.Envelope = Envelope;
})(gml || (gml = {}));
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
/// <reference path="Envelope.ts"/>
var gml;
(function (gml) {
    var EnvelopeWithTimePeriod = (function (_super) {
        __extends(EnvelopeWithTimePeriod, _super);
        function EnvelopeWithTimePeriod(source) {
            _super.call(this, source);
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return EnvelopeWithTimePeriod;
    })(gml.Envelope);
    gml.EnvelopeWithTimePeriod = EnvelopeWithTimePeriod;
})(gml || (gml = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="Envelope.ts"/>
///<reference path="EnvelopeWithTimePeriod.ts"/>
var gml;
(function (gml) {
    var BoundedBy = (function () {
        function BoundedBy(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            if (source.doesElementExist("gml:Envelope")) {
                this.Envelope = new gml.Envelope(source.getChildAsSerializedObject("gml:Envelope"));
            }
            if (source.doesElementExist("gml:EnvelopeWithTimePeriod")) {
                this.EnvelopeWithTimePeriod = new gml.EnvelopeWithTimePeriod(source.getChildAsSerializedObject("gml:EnvelopeWithTimePeriod"));
                this.Envelope = this.EnvelopeWithTimePeriod;
            }
        }
        return BoundedBy;
    })();
    gml.BoundedBy = BoundedBy;
})(gml || (gml = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="BoundedBy.ts"/>
var gml;
(function (gml) {
    var AbstractFeature = (function () {
        function AbstractFeature(source) {
            var _this = this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.Id = source.getAttributeAsString("gml:id");
            if (source.doesElementExist("gml:description")) {
                this.Description = source.getChildAsSerializedObject("gml:description").getValueAsString();
            }
            if (source.doesElementExist("gml:identifier")) {
                this.Identifier = source.getChildAsSerializedObject("gml:identifier").getValueAsString();
            }
            this.Name = [];
            source.getChildrenAsSerializedObjects("gml:name").forEach(function (o) {
                _this.Name.push(o.getValueAsString());
            });
            if (source.doesElementExist("gml:boundedBy")) {
                this.BoundedBy = new gml.BoundedBy(source.getChildAsSerializedObject("gml:boundedBy"));
            }
        }
        return AbstractFeature;
    })();
    gml.AbstractFeature = AbstractFeature;
})(gml || (gml = {}));
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
///<reference path="../../common/_common.ts"/>
var gml;
(function (gml) {
    var CoverageFunction = (function () {
        function CoverageFunction(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return CoverageFunction;
    })();
    gml.CoverageFunction = CoverageFunction;
})(gml || (gml = {}));
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
///<reference path="../../common/_common.ts"/>
var gml;
(function (gml) {
    var DomainSet = (function () {
        function DomainSet(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return DomainSet;
    })();
    gml.DomainSet = DomainSet;
})(gml || (gml = {}));
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
///<reference path="AbstractFeature.ts"/>
///<reference path="BoundedBy.ts"/>
///<reference path="CoverageFunction.ts"/>
///<reference path="DomainSet.ts"/>
///<reference path="Envelope.ts"/>
///<reference path="EnvelopeWithTimePeriod.ts"/>
///<reference path="LowerCorner.ts"/>
///<reference path="Pos.ts"/>
///<reference path="UpperCorner.ts"/>
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
///<reference path="../../common/_common.ts"/>
var gmlcov;
(function (gmlcov) {
    var Metadata = (function () {
        function Metadata(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return Metadata;
    })();
    gmlcov.Metadata = Metadata;
})(gmlcov || (gmlcov = {}));
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
///<reference path="../../common/_common.ts"/>
var swe;
(function (swe) {
    var Uom = (function () {
        function Uom(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.Code = source.getAttributeAsString("code");
        }
        return Uom;
    })();
    swe.Uom = Uom;
})(swe || (swe = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="Uom.ts"/>
var swe;
(function (swe) {
    var Quantity = (function () {
        function Quantity(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            if (source.doesElementExist("swe:uom")) {
                this.Uom = new swe.Uom(source.getChildAsSerializedObject("swe:uom"));
            }
        }
        return Quantity;
    })();
    swe.Quantity = Quantity;
})(swe || (swe = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="Quantity.ts"/>
var swe;
(function (swe) {
    var Field = (function () {
        function Field(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            if (source.doesAttributeExist("name")) {
                this.Name = source.getAttributeAsString("name");
            }
            if (source.doesElementExist("swe:Quantity")) {
                this.Quantity = new swe.Quantity(source.getChildAsSerializedObject("swe:Quantity"));
            }
        }
        return Field;
    })();
    swe.Field = Field;
})(swe || (swe = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="Field.ts"/>
var swe;
(function (swe) {
    var DataRecord = (function () {
        function DataRecord(source) {
            var _this = this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.Field = [];
            source.getChildrenAsSerializedObjects("swe:field").forEach(function (o) {
                _this.Field.push(new swe.Field(o));
            });
        }
        return DataRecord;
    })();
    swe.DataRecord = DataRecord;
})(swe || (swe = {}));
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
///<reference path="Uom.ts"/>
///<reference path="DataRecord.ts"/>
///<reference path="Field.ts"/>
///<reference path="Quantity.ts"/>
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
///<reference path="../../common/_common.ts"/>
///<reference path="../swe/_swe.ts"/>
var gmlcov;
(function (gmlcov) {
    var RangeType = (function () {
        function RangeType(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            if (source.doesElementExist("swe:DataRecord")) {
                this.DataRecord = new swe.DataRecord(source.getChildAsSerializedObject("swe:DataRecord"));
            }
        }
        return RangeType;
    })();
    gmlcov.RangeType = RangeType;
})(gmlcov || (gmlcov = {}));
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
///<reference path="Metadata.ts"/>
///<reference path="RangeType.ts"/>
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
///<reference path="../../common/_common.ts"/>
///<reference path="CoverageSubtypeParent.ts"/>
///<reference path="Extension.ts"/>
var wcs;
(function (wcs) {
    var ServiceParameters = (function () {
        function ServiceParameters(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.CoverageSubtype = source.getChildAsSerializedObject("wcs:CoverageSubtype").getValueAsString();
            if (source.doesElementExist("wcs:CoverageSubtypeParent")) {
                this.CoverageSubtypeParent = new wcs.CoverageSubtypeParent(source.getChildAsSerializedObject("wcs:CoverageSubtypeParent"));
            }
            this.NativeFormat = source.getChildAsSerializedObject("nativeFormat").getValueAsString();
            if (source.doesAttributeExist("wcs:Extension")) {
                this.Extension = new wcs.Extension(source.getChildAsSerializedObject("wcs:Extension"));
            }
        }
        return ServiceParameters;
    })();
    wcs.ServiceParameters = ServiceParameters;
})(wcs || (wcs = {}));
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
///<reference path="../gml/_gml.ts"/>
///<reference path="../gmlcov/_gmlcov.ts"/>
///<reference path="ServiceParameters.ts"/>
var wcs;
(function (wcs) {
    var CoverageDescription = (function (_super) {
        __extends(CoverageDescription, _super);
        function CoverageDescription(source) {
            var _this = this;
            _super.call(this, source);
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.CoverageId = source.getChildAsSerializedObject("wcs:CoverageId").getValueAsString();
            if (source.doesElementExist("gml:coverageFunction")) {
                this.CoverageFunction = new gml.CoverageFunction(source.getChildAsSerializedObject("gml:coverageFunction"));
            }
            this.Metadata = [];
            source.getChildrenAsSerializedObjects("gmlcov:metadata").forEach(function (o) {
                _this.Metadata.push(new gmlcov.Metadata(o));
            });
            this.DomainSet = new gml.DomainSet(source.getChildAsSerializedObject("gml:domainSet"));
            this.RangeType = new gmlcov.RangeType(source.getChildAsSerializedObject("gmlcov:rangeType"));
            this.ServiceParameters = new wcs.ServiceParameters(source.getChildAsSerializedObject("wcs:ServiceParameters"));
        }
        return CoverageDescription;
    })(gml.AbstractFeature);
    wcs.CoverageDescription = CoverageDescription;
})(wcs || (wcs = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="CoverageDescription.ts"/>
var wcs;
(function (wcs) {
    var CoverageDescriptions = (function () {
        function CoverageDescriptions(source) {
            var _this = this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.CoverageDescription = [];
            source.getChildrenAsSerializedObjects("wcs:CoverageDescription").forEach(function (o) {
                _this.CoverageDescription.push(new wcs.CoverageDescription(o));
            });
        }
        return CoverageDescriptions;
    })();
    wcs.CoverageDescriptions = CoverageDescriptions;
})(wcs || (wcs = {}));
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
///<reference path="../../common/_common.ts"/>
var wcs;
(function (wcs) {
    var RequestBase = (function () {
        function RequestBase() {
            this.Service = "WCS";
            this.Version = "2.0.1";
        }
        RequestBase.prototype.toKVP = function () {
            return "&SERVICE=" + this.Service +
                "&VERSION=" + this.Version;
        };
        return RequestBase;
    })();
    wcs.RequestBase = RequestBase;
})(wcs || (wcs = {}));
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
/// <reference path="../../_all.ts"/>
/// <reference path="RequestBase.ts"/>
var wcs;
(function (wcs) {
    var DescribeCoverage = (function (_super) {
        __extends(DescribeCoverage, _super);
        function DescribeCoverage(coverageIds) {
            _super.call(this);
            if (!coverageIds.length) {
                throw new rasdaman.common.IllegalArgumentException("coverageIds");
            }
            this.CoverageId = angular.copy(coverageIds);
        }
        DescribeCoverage.prototype.toKVP = function () {
            var serialization = _super.prototype.toKVP.call(this);
            serialization += "&REQUEST=DescribeCoverage";
            serialization += "&COVERAGEID=" + this.CoverageId.join(",");
            return serialization;
        };
        return DescribeCoverage;
    })(wcs.RequestBase);
    wcs.DescribeCoverage = DescribeCoverage;
})(wcs || (wcs = {}));
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
var wcs;
(function (wcs) {
    var DimensionSubset = (function () {
        function DimensionSubset(dimension) {
            this.Dimension = dimension;
        }
        DimensionSubset.prototype.toKVP = function () {
            throw new rasdaman.common.NotImplementedException();
        };
        return DimensionSubset;
    })();
    wcs.DimensionSubset = DimensionSubset;
})(wcs || (wcs = {}));
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
///<reference path="DimensionSubset.ts"/>
var wcs;
(function (wcs) {
    var DimensionSlice = (function (_super) {
        __extends(DimensionSlice, _super);
        function DimensionSlice(dimension, slicePoint) {
            _super.call(this, dimension);
            this.SlicePoint = slicePoint;
        }
        DimensionSlice.prototype.toKVP = function () {
            return this.Dimension + "(" + this.SlicePoint + ")";
        };
        return DimensionSlice;
    })(wcs.DimensionSubset);
    wcs.DimensionSlice = DimensionSlice;
})(wcs || (wcs = {}));
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
///<reference path="DimensionSubset.ts"/>
var wcs;
(function (wcs) {
    var DimensionTrim = (function (_super) {
        __extends(DimensionTrim, _super);
        function DimensionTrim(dimension, trimLow, trimHigh) {
            _super.call(this, dimension);
            this.TrimLow = trimLow;
            this.TrimHigh = trimHigh;
        }
        DimensionTrim.prototype.toKVP = function () {
            return this.Dimension + "(" + this.TrimLow + "," + this.TrimHigh + ")";
        };
        return DimensionTrim;
    })(wcs.DimensionSubset);
    wcs.DimensionTrim = DimensionTrim;
})(wcs || (wcs = {}));
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
///<reference path="../ows/ows_all.ts"/>
var wcs;
(function (wcs) {
    var GetCapabilities = (function (_super) {
        __extends(GetCapabilities, _super);
        function GetCapabilities() {
            _super.call(this);
            this.Service = "WCS";
            this.AcceptVersions = ["2.0.1"];
        }
        GetCapabilities.prototype.toKVP = function () {
            return "&SERVICE=" + this.Service +
                "&VERSION=" + this.AcceptVersions[0] +
                "&REQUEST=" + this.Request;
        };
        return GetCapabilities;
    })(ows.GetCapabilities);
    wcs.GetCapabilities = GetCapabilities;
})(wcs || (wcs = {}));
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
var wcs;
(function (wcs) {
    var RangeItem = (function () {
        function RangeItem() {
        }
        RangeItem.prototype.toKVP = function () {
            throw new rasdaman.common.NotImplementedException();
        };
        return RangeItem;
    })();
    wcs.RangeItem = RangeItem;
})(wcs || (wcs = {}));
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
///<reference path="RangeItem.ts"/>
var wcs;
(function (wcs) {
    var RangeSubset = (function () {
        function RangeSubset() {
            this.RangeItem = [];
        }
        RangeSubset.prototype.toKVP = function () {
            var serializedRangeItems = [];
            this.RangeItem.forEach(function (rangeItem) {
                serializedRangeItems.push(rangeItem.toKVP());
            });
            if (serializedRangeItems.length) {
                return "&RANGESUBSET=" + serializedRangeItems.join(",");
            }
            else {
                return "";
            }
        };
        return RangeSubset;
    })();
    wcs.RangeSubset = RangeSubset;
})(wcs || (wcs = {}));
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
var wcs;
(function (wcs) {
    var Scaling = (function () {
        function Scaling() {
        }
        Scaling.prototype.toKVP = function () {
            throw new rasdaman.common.NotImplementedException();
        };
        return Scaling;
    })();
    wcs.Scaling = Scaling;
})(wcs || (wcs = {}));
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
///<reference path="RequestBase.ts"/>
///<reference path="DimensionSubset.ts"/>
///<reference path="RangeSubset.ts"/>
///<reference path="Scaling.ts"/>
var wcs;
(function (wcs) {
    var GetCoverage = (function (_super) {
        __extends(GetCoverage, _super);
        function GetCoverage(coverageId, dimensionSubset, format, mediaType) {
            var _this = this;
            _super.call(this);
            this.CoverageId = coverageId;
            this.DimensionSubset = [];
            dimensionSubset.forEach(function (o) {
                _this.DimensionSubset.push(o);
            });
            this.Format = format;
            this.MediaType = mediaType;
        }
        GetCoverage.prototype.toKVP = function () {
            var serialization = _super.prototype.toKVP.call(this);
            serialization += "&REQUEST=GetCoverage";
            serialization += "&COVERAGEID=" + this.CoverageId;
            this.DimensionSubset.forEach(function (subset) {
                serialization += "&SUBSET=" + subset.toKVP();
            });
            if (this.RangeSubset) {
                serialization += this.RangeSubset.toKVP();
            }
            if (this.Scaling) {
                serialization += this.Scaling.toKVP();
            }
            if (this.Interpolation) {
                serialization += this.Interpolation.toKVP();
            }
            if (this.Format) {
                serialization += "&FORMAT=" + this.Format;
            }
            if (this.MediaType) {
                serialization += "&MEDIATYPE=multipart/related";
            }
            return serialization;
        };
        return GetCoverage;
    })(wcs.RequestBase);
    wcs.GetCoverage = GetCoverage;
})(wcs || (wcs = {}));
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
var wcs;
(function (wcs) {
    var InterpolationMetadata = (function () {
        function InterpolationMetadata(source) {
            var _this = this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.InterpolationSupported = [];
            source.getChildrenAsSerializedObjects("int:InterpolationSupported")
                .forEach(function (interpolation) {
                _this.InterpolationSupported.push(interpolation.getValueAsString());
            });
        }
        return InterpolationMetadata;
    })();
    wcs.InterpolationMetadata = InterpolationMetadata;
})(wcs || (wcs = {}));
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
///<reference path="RequestBase.ts"/>
///<reference path="../../common/_common.ts"/>
var wcs;
(function (wcs) {
    var ProcessCoverages = (function (_super) {
        __extends(ProcessCoverages, _super);
        function ProcessCoverages(query, extraParams) {
            _super.call(this);
            rasdaman.common.ArgumentValidator.isNotNull(query, "query");
            rasdaman.common.ArgumentValidator.isNotNull(extraParams, "extraParams");
            rasdaman.common.ArgumentValidator.isArray(extraParams, "extraParams");
            this.Request = "ProcessCoverages";
            this.Query = query;
            this.ExtraParameters = angular.copy(extraParams);
        }
        ProcessCoverages.prototype.toKVP = function () {
            var serializedParams = "";
            for (var i = 0; i < this.ExtraParameters.length; ++i) {
                serializedParams += ("&" + i + "=" + encodeURI(this.ExtraParameters[i]));
            }
            return "&REQUEST=" + this.Request
                + "&QUERY=" + encodeURI(this.Query)
                + serializedParams;
        };
        return ProcessCoverages;
    })(wcs.RequestBase);
    wcs.ProcessCoverages = ProcessCoverages;
})(wcs || (wcs = {}));
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
var wcs;
(function (wcs) {
    var Interpolation = (function () {
        function Interpolation(globalInterpolation) {
            this.GlobalInterpolation = globalInterpolation;
        }
        Interpolation.prototype.toKVP = function () {
            if (this.GlobalInterpolation) {
                return "&INTERPOLATION=" + this.GlobalInterpolation;
            }
            else {
                return "";
            }
        };
        return Interpolation;
    })();
    wcs.Interpolation = Interpolation;
})(wcs || (wcs = {}));
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
///<reference path="RangeItem.ts"/>
var wcs;
(function (wcs) {
    var RangeComponent = (function (_super) {
        __extends(RangeComponent, _super);
        function RangeComponent(rangeComponent) {
            _super.call(this);
            this.RangeComponent = rangeComponent;
        }
        RangeComponent.prototype.toKVP = function () {
            return this.RangeComponent;
        };
        return RangeComponent;
    })(wcs.RangeItem);
    wcs.RangeComponent = RangeComponent;
})(wcs || (wcs = {}));
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
///<reference path="RangeItem.ts"/>
///<reference path="RangeComponent.ts"/>
var wcs;
(function (wcs) {
    var RangeInterval = (function (_super) {
        __extends(RangeInterval, _super);
        function RangeInterval(startComponent, endComponent) {
            _super.call(this);
            this.StartComponent = startComponent;
            this.EndComponent = endComponent;
        }
        RangeInterval.prototype.toKVP = function () {
            return this.StartComponent.toKVP() + ":" + this.EndComponent.toKVP();
        };
        return RangeInterval;
    })(wcs.RangeItem);
    wcs.RangeInterval = RangeInterval;
})(wcs || (wcs = {}));
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
///<reference path="Scaling.ts"/>
///<reference path="../../common/_common.ts"/>
var wcs;
(function (wcs) {
    var ScaleByFactor = (function (_super) {
        __extends(ScaleByFactor, _super);
        function ScaleByFactor(scaleFactor) {
            _super.call(this);
            if (scaleFactor < 0) {
                throw new rasdaman.common.IllegalArgumentException("ScaleFactor must be positive.");
            }
            this.ScaleFactor = scaleFactor;
        }
        ScaleByFactor.prototype.toKVP = function () {
            return "&SCALEFACTOR=" + this.ScaleFactor;
        };
        return ScaleByFactor;
    })(wcs.Scaling);
    wcs.ScaleByFactor = ScaleByFactor;
})(wcs || (wcs = {}));
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
///<reference path="../../common/_common.ts"/>
var wcs;
(function (wcs) {
    var ScaleAxis = (function () {
        function ScaleAxis(axis, scaleFactor) {
            if (scaleFactor < 0) {
                throw new rasdaman.common.IllegalArgumentException("ScaleFactor must be positive.");
            }
            this.Axis = axis;
            this.ScaleFactor = scaleFactor;
        }
        ScaleAxis.prototype.toKVP = function () {
            return this.Axis + "(" + this.ScaleFactor + ")";
        };
        return ScaleAxis;
    })();
    wcs.ScaleAxis = ScaleAxis;
})(wcs || (wcs = {}));
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
///<reference path="../../../assets/typings/tsd.d.ts"/>
///<reference path="ScaleAxis.ts"/>
///<reference path="Scaling.ts"/>
var wcs;
(function (wcs) {
    var ScaleAxesByFactor = (function (_super) {
        __extends(ScaleAxesByFactor, _super);
        function ScaleAxesByFactor(scaleAxis) {
            _super.call(this);
            this.ScaleAxis = angular.copy(scaleAxis);
        }
        ScaleAxesByFactor.prototype.toKVP = function () {
            var serializedAxes = [];
            this.ScaleAxis.forEach(function (axis) {
                serializedAxes.push(axis.toKVP());
            });
            return "&SCALEAXES=" + serializedAxes.join(",");
        };
        return ScaleAxesByFactor;
    })(wcs.Scaling);
    wcs.ScaleAxesByFactor = ScaleAxesByFactor;
})(wcs || (wcs = {}));
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
///<reference path="../../common/_common.ts"/>
var wcs;
(function (wcs) {
    var TargetAxisSize = (function () {
        function TargetAxisSize(axis, targetSize) {
            if (targetSize < 0) {
                throw new rasdaman.common.IllegalArgumentException("ScaleFactor must be positive.");
            }
            this.Axis = axis;
            this.TargetSize = targetSize;
        }
        TargetAxisSize.prototype.toKVP = function () {
            return this.Axis + "(" + this.TargetSize + ")";
        };
        return TargetAxisSize;
    })();
    wcs.TargetAxisSize = TargetAxisSize;
})(wcs || (wcs = {}));
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
///<reference path="../../../assets/typings/tsd.d.ts"/>
///<reference path="TargetAxisSize.ts"/>
///<reference path="Scaling.ts"/>
var wcs;
(function (wcs) {
    var ScaleToSize = (function (_super) {
        __extends(ScaleToSize, _super);
        function ScaleToSize(targetAxisSize) {
            _super.call(this);
            this.TargetAxisSize = angular.copy(targetAxisSize);
        }
        ScaleToSize.prototype.toKVP = function () {
            var targetAxesSize = [];
            this.TargetAxisSize.forEach(function (target) {
                targetAxesSize.push(target.toKVP());
            });
            return "&SCALESIZE=" + targetAxesSize.join(",");
        };
        return ScaleToSize;
    })(wcs.Scaling);
    wcs.ScaleToSize = ScaleToSize;
})(wcs || (wcs = {}));
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
var wcs;
(function (wcs) {
    var TargetAxisExtent = (function () {
        function TargetAxisExtent(axis, low, high) {
            this.Axis = axis;
            this.Low = low;
            this.High = high;
        }
        TargetAxisExtent.prototype.toKVP = function () {
            return this.Axis + "(" + this.Low + ":" + this.High + ")";
        };
        return TargetAxisExtent;
    })();
    wcs.TargetAxisExtent = TargetAxisExtent;
})(wcs || (wcs = {}));
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
///<reference path="../../../assets/typings/tsd.d.ts"/>
///<reference path="TargetAxisExtent.ts"/>
///<reference path="Scaling.ts"/>
var wcs;
(function (wcs) {
    var ScaleToExtent = (function (_super) {
        __extends(ScaleToExtent, _super);
        function ScaleToExtent(targetAxisExtent) {
            _super.call(this);
            this.TargetAxisExtent = angular.copy(targetAxisExtent);
        }
        ScaleToExtent.prototype.toKVP = function () {
            var serializedAxes = [];
            this.TargetAxisExtent.forEach(function (target) {
                serializedAxes.push(target.toKVP());
            });
            return "&SCALEEXTENT=" + serializedAxes.join(",");
        };
        return ScaleToExtent;
    })(wcs.Scaling);
    wcs.ScaleToExtent = ScaleToExtent;
})(wcs || (wcs = {}));
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
/// <reference path="Capabilities.ts"/>
/// <reference path="Contents.ts"/>
/// <reference path="CoverageDescription.ts"/>
/// <reference path="CoverageDescriptions.ts"/>
/// <reference path="CoverageSubtypeParent.ts"/>
/// <reference path="CoverageSummary.ts"/>
/// <reference path="DescribeCoverage.ts"/>
/// <reference path="DimensionSubset.ts"/>
/// <reference path="DimensionSlice.ts"/>
/// <reference path="DimensionTrim.ts"/>
/// <reference path="Extension.ts"/>
/// <reference path="GetCapabilities.ts"/>
/// <reference path="GetCoverage.ts"/>
/// <reference path="InterpolationMetadata.ts"/>
/// <reference path="ProcessCoverages.ts"/>
/// <reference path="Interpolation.ts"/>
/// <reference path="RangeItem.ts"/>
/// <reference path="RangeSubset.ts"/>
/// <reference path="RangeInterval.ts"/>
/// <reference path="RangeComponent.ts"/>
/// <reference path="RequestBase.ts"/>
/// <reference path="ServiceMetadata.ts"/>
/// <reference path="ServiceParameters.ts"/>
/// <reference path="Scaling.ts"/>
/// <reference path="ScaleByFactor.ts"/>
/// <reference path="ScaleAxesByFactor.ts"/>
/// <reference path="ScaleAxis.ts"/>
/// <reference path="ScaleToSize.ts"/>
/// <reference path="ScaleToExtent.ts"/>
/// <reference path="TargetAxisSize.ts"/>
/// <reference path="TargetAxisExtent.ts"/>
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
/// <reference path="../../../assets/typings/tsd.d.ts"/>
///<reference path="../../common/_common.ts"/>
var rasdaman;
(function (rasdaman) {
    var SettingsService = (function () {
        function SettingsService($window) {
            this.WCSEndpoint = $window.location.origin + "/rasdaman/ows";
        }
        SettingsService.$inject = ["$window"];
        return SettingsService;
    })();
    rasdaman.SettingsService = SettingsService;
})(rasdaman || (rasdaman = {}));
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
/// <reference path="../../common/_common.ts"/>
/// <reference path="../../models/wcs/_wcs.ts"/>
/// <reference path="../settings/SettingsService.ts"/>
var rasdaman;
(function (rasdaman) {
    var WCSService = (function () {
        function WCSService($http, $q, settings, serializedObjectFactory, $window) {
            this.$http = $http;
            this.$q = $q;
            this.settings = settings;
            this.serializedObjectFactory = serializedObjectFactory;
            this.$window = $window;
        }
        WCSService.prototype.getServerCapabilities = function (request) {
            var result = this.$q.defer();
            var self = this;
            var requestUrl = this.settings.WCSEndpoint + "?" + request.toKVP();
            this.$http.get(requestUrl)
                .then(function (data) {
                try {
                    var doc = new rasdaman.common.ResponseDocument(data.data, rasdaman.common.ResponseDocumentType.XML);
                    var serializedResponse = self.serializedObjectFactory.getSerializedObject(doc);
                    var capabilities = new wcs.Capabilities(serializedResponse);
                    var response = new rasdaman.common.Response(doc, capabilities);
                    result.resolve(response);
                }
                catch (err) {
                    result.reject(err);
                }
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WCSService.prototype.getCoverageDescription = function (request) {
            var result = this.$q.defer();
            var self = this;
            var requestUrl = this.settings.WCSEndpoint + "?" + request.toKVP();
            this.$http.get(requestUrl)
                .then(function (data) {
                try {
                    var doc = new rasdaman.common.ResponseDocument(data.data, rasdaman.common.ResponseDocumentType.XML);
                    var serializedResponse = self.serializedObjectFactory.getSerializedObject(doc);
                    var capabilities = new wcs.CoverageDescriptions(serializedResponse);
                    var response = new rasdaman.common.Response(doc, capabilities);
                    result.resolve(response);
                }
                catch (err) {
                    result.reject(err);
                }
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WCSService.prototype.getCoverage = function (request) {
            var result = this.$q.defer();
            var requestUrl = this.settings.WCSEndpoint + "?" + request.toKVP();
            this.$window.open(requestUrl);
            result.resolve();
            return result.promise;
        };
        WCSService.prototype.deleteCoverage = function (coverageId) {
            var result = this.$q.defer();
            if (!coverageId) {
                result.reject("You must specify at least one coverage ID.");
            }
            var requestUrl = this.settings.WCSEndpoint + "?SERVICE=WCS&VERSION=2.0.1&REQUEST=DeleteCoverage&COVERAGEID=" + coverageId;
            this.$http.get(requestUrl)
                .then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WCSService.prototype.insertCoverage = function (coverageUrl, useGeneratedId) {
            var result = this.$q.defer();
            if (!coverageUrl) {
                result.reject("You must indicate a coverage source.");
            }
            var requestUrl = this.settings.WCSEndpoint + "?SERVICE=WCS&VERSION=2.0.1&REQUEST=InsertCoverage&coverageRef=" + encodeURI(coverageUrl);
            if (useGeneratedId) {
                requestUrl += "&useId=new";
            }
            this.$http.get(requestUrl)
                .then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WCSService.prototype.processCoverages = function (query, binaryFormat) {
            var result = this.$q.defer();
            var requestUrl = this.settings.WCSEndpoint + "?" + query.toKVP();
            var request = {
                method: 'GET',
                url: requestUrl,
                transformResponse: null
            };
            if (binaryFormat) {
                request.responseType = "arraybuffer";
            }
            this.$http(request).then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WCSService.$inject = ["$http", "$q", "rasdaman.SettingsService", "rasdaman.common.SerializedObjectFactory", "$window"];
        return WCSService;
    })();
    rasdaman.WCSService = WCSService;
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../../assets/typings/tsd.d.ts"/>
///<reference path="SettingsService.ts"/>
var rasdaman;
(function (rasdaman) {
    var SettingsController = (function () {
        function SettingsController($scope, settingsService) {
            this.$scope = $scope;
            this.settingsService = settingsService;
            $scope.WCSEndpoint = settingsService.WCSEndpoint;
            $scope.updateSettings = function () {
                console.log($scope.WCSEndpoint);
            };
        }
        SettingsController.$inject = [
            "$scope",
            "rasdaman.SettingsService"
        ];
        return SettingsController;
    })();
    rasdaman.SettingsController = SettingsController;
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../../assets/typings/tsd.d.ts"/>
///<reference path="../../models/wcs/Capabilities.ts"/>
///<reference path="../../models/wcs/CoverageDescriptions.ts"/>
var rasdaman;
(function (rasdaman) {
    var MainController = (function () {
        function MainController($scope, $rootScope, $state) {
            var _this = this;
            this.$scope = $scope;
            this.initializeTabs($scope);
            $scope.$watch("StateInformation.ServerCapabilities", function (newValue, oldValue) {
                if (newValue) {
                    $scope.DescribeCoverageTab.Disabled = false;
                    $scope.GetCoverageTab.Disabled = true;
                    $scope.ProcessCoverageTab.Disabled = !MainController.isProcessCoverageEnabled(newValue);
                    $scope.InsertCoverageTab.Disabled = !MainController.isCoverageTransactionEnabled(newValue);
                    $scope.DeleteCoverageTab.Disabled = !MainController.isCoverageTransactionEnabled(newValue);
                }
                else {
                    _this.resetState();
                }
            });
            $scope.$watch("StateInformation.SelectedCoverageDescriptions", function (newValue, oldValue) {
                $scope.GetCoverageTab.Disabled = newValue ? false : true;
            });
            $scope.Tabs = [$scope.GetCapabilitiesTab, $scope.DescribeCoverageTab, $scope.GetCoverageTab, $scope.ProcessCoverageTab, $scope.DeleteCoverageTab, $scope.InsertCoverageTab];
            $scope.StateInformation = {
                ServerCapabilities: null,
                SelectedCoverageDescriptions: null
            };
            $scope.describeCoverage = function (coverageId) {
                $scope.DescribeCoverageTab.Active = true;
                $rootScope.$broadcast("SelectedCoverageId", coverageId);
            };
        }
        MainController.prototype.initializeTabs = function ($scope) {
            $scope.GetCapabilitiesTab = {
                Heading: "GetCapabilities",
                View: "get_capabilities",
                Active: true,
                Disabled: false
            };
            $scope.DescribeCoverageTab = {
                Heading: "DescribeCoverage",
                View: "describe_coverage",
                Active: false,
                Disabled: false
            };
            $scope.GetCoverageTab = {
                Heading: "GetCoverage",
                View: "get_coverage",
                Active: false,
                Disabled: false
            };
            $scope.ProcessCoverageTab = {
                Heading: "ProcessCoverages",
                View: "process_coverages",
                Active: false,
                Disabled: false
            };
            $scope.InsertCoverageTab = {
                Heading: "InsertCoverage",
                View: "insert_coverage",
                Active: false,
                Disabled: false
            };
            $scope.DeleteCoverageTab = {
                Heading: "DeleteCoverage",
                View: "delete_coverage",
                Active: false,
                Disabled: false
            };
        };
        MainController.prototype.resetState = function () {
            this.$scope.DescribeCoverageTab.Disabled = true;
            this.$scope.GetCoverageTab.Disabled = true;
            this.$scope.ProcessCoverageTab.Disabled = true;
            this.$scope.DeleteCoverageTab.Disabled = true;
            this.$scope.InsertCoverageTab.Disabled = true;
        };
        MainController.isProcessCoverageEnabled = function (serverCapabilities) {
            var processExtensionUri = rasdaman.Constants.PROCESSING_EXT_URI;
            return serverCapabilities.ServiceIdentification.Profile.indexOf(processExtensionUri) != -1;
        };
        MainController.isCoverageTransactionEnabled = function (serverCapabilities) {
            var transactionExtensionUri = rasdaman.Constants.TRANSACTION_EXT_URI;
            return serverCapabilities.ServiceIdentification.Profile.indexOf(transactionExtensionUri) != -1;
        };
        MainController.$inject = ["$scope", "$rootScope", "$state"];
        return MainController;
    })();
    rasdaman.MainController = MainController;
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../../assets/typings/tsd.d.ts"/>
///<reference path="../../models/wcs/GetCapabilities.ts"/>
///<reference path="../shared/WCSService.ts"/>
///<reference path="../settings/SettingsService.ts"/>
///<reference path="../main/MainController.ts"/>
var rasdaman;
(function (rasdaman) {
    var GetCapabilitiesController = (function () {
        function GetCapabilitiesController($scope, $log, $state, wcsService, settings, alertService) {
            var _this = this;
            this.$scope = $scope;
            this.$log = $log;
            this.$state = $state;
            this.wcsService = wcsService;
            this.settings = settings;
            this.alertService = alertService;
            $scope.IsAvailableCoveragesOpen = false;
            $scope.IsServiceIdentificationOpen = false;
            $scope.IsServiceProviderOpen = false;
            $scope.IsCapabilitiesDocumentOpen = false;
            $scope.WcsServerEndpoint = settings.WCSEndpoint;
            $scope.getServerCapabilities = function () {
                if (!$scope.WcsServerEndpoint) {
                    alertService.error("The entered WCS server endpoint is invalid.");
                    return;
                }
                settings.WCSEndpoint = $scope.WcsServerEndpoint;
                var capabilitiesRequest = new wcs.GetCapabilities();
                wcsService.getServerCapabilities(capabilitiesRequest)
                    .then(function (response) {
                    $scope.CapabilitiesDocument = response.Document;
                    $scope.Capabilities = response.Value;
                    $scope.IsAvailableCoveragesOpen = true;
                    $scope.IsServiceIdentificationOpen = true;
                    $scope.IsServiceProviderOpen = true;
                }, function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i - 0] = arguments[_i];
                    }
                    $scope.CapabilitiesDocument = null;
                    $scope.Capabilities = null;
                    $scope.IsAvailableCoveragesOpen = false;
                    $scope.IsServiceIdentificationOpen = false;
                    $scope.IsServiceProviderOpen = false;
                    alertService.error("Failed to retrieve the capabilities of the server located at:" + _this.settings.WCSEndpoint + ". Check the log for additional information.");
                    $log.error(args);
                })
                    .finally(function () {
                    $scope.StateInformation.ServerCapabilities = $scope.Capabilities;
                });
            };
            $scope.getServerCapabilities();
        }
        GetCapabilitiesController.$inject = [
            "$scope",
            "$log",
            "$state",
            "rasdaman.WCSService",
            "rasdaman.SettingsService",
            "Notification"
        ];
        return GetCapabilitiesController;
    })();
    rasdaman.GetCapabilitiesController = GetCapabilitiesController;
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../../assets/typings/tsd.d.ts"/>
///<reference path="../shared/WCSService.ts"/>
///<reference path="../../models/wcs/Capabilities.ts"/>
///<reference path="../main/MainController.ts"/>
var rasdaman;
(function (rasdaman) {
    var DescribeCoverageController = (function () {
        function DescribeCoverageController($scope, $rootScope, $log, wcsService, alertService) {
            $scope.SelectedCoverageId = null;
            $scope.IsCoverageDescriptionsDocumentOpen = false;
            $scope.isCoverageIdValid = function () {
                if ($scope.StateInformation.ServerCapabilities) {
                    var coverageSummaries = $scope.StateInformation.ServerCapabilities.Contents.CoverageSummary;
                    for (var i = 0; i < coverageSummaries.length; ++i) {
                        if (coverageSummaries[i].CoverageId == $scope.SelectedCoverageId) {
                            return true;
                        }
                    }
                }
                return false;
            };
            $rootScope.$on("SelectedCoverageId", function (event, coverageId) {
                $scope.SelectedCoverageId = coverageId;
                $scope.describeCoverage();
            });
            $scope.$watch("StateInformation.ServerCapabilities", function (capabilities) {
                if (capabilities) {
                    $scope.AvailableCoverageIds = [];
                    capabilities.Contents.CoverageSummary.forEach(function (coverageSummary) {
                        $scope.AvailableCoverageIds.push(coverageSummary.CoverageId);
                    });
                }
            });
            $scope.describeCoverage = function () {
                if (!$scope.isCoverageIdValid()) {
                    alertService.error("The entered coverage ID is invalid.");
                    return;
                }
                var coverageIds = [];
                coverageIds.push($scope.SelectedCoverageId);
                var describeCoverageRequest = new wcs.DescribeCoverage(coverageIds);
                wcsService.getCoverageDescription(describeCoverageRequest)
                    .then(function (response) {
                    $scope.CoverageDescriptionsDocument = response.Document;
                    $scope.CoverageDescriptions = response.Value;
                }, function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i - 0] = arguments[_i];
                    }
                    $scope.CoverageDescriptionsDocument = null;
                    $scope.CoverageDescriptions = null;
                    alertService.error("Failed to retrieve the description for coverage with ID " + $scope.SelectedCoverageId + ". Check the log for more details.");
                    $log.error(args);
                })
                    .finally(function () {
                    $scope.StateInformation.SelectedCoverageDescriptions = $scope.CoverageDescriptions;
                });
            };
            $scope.IsCoverageDescriptionsDocumentOpen = false;
        }
        DescribeCoverageController.$inject = [
            "$scope",
            "$rootScope",
            "$log",
            "rasdaman.WCSService",
            "Notification"
        ];
        return DescribeCoverageController;
    })();
    rasdaman.DescribeCoverageController = DescribeCoverageController;
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../../assets/typings/tsd.d.ts"/>
///<reference path="../main/MainController.ts"/>
///<reference path="../shared/WCSService.ts"/>
var rasdaman;
(function (rasdaman) {
    var DeleteCoverageController = (function () {
        function DeleteCoverageController($scope, $log, alertService, wcsService) {
            var _this = this;
            this.$scope = $scope;
            this.$log = $log;
            this.alertService = alertService;
            this.wcsService = wcsService;
            function isCoverageIdValid(coverageId) {
                if ($scope.StateInformation.ServerCapabilities) {
                    var coverageSummaries = $scope.StateInformation.ServerCapabilities.Contents.CoverageSummary;
                    for (var i = 0; i < coverageSummaries.length; ++i) {
                        if (coverageSummaries[i].CoverageId == coverageId) {
                            return true;
                        }
                    }
                }
                return false;
            }
            $scope.$watch("IdOfCoverageToDelete", function (newValue, oldValue) {
                $scope.IsCoverageIdValid = isCoverageIdValid(newValue);
            });
            $scope.$watch("StateInformation.ServerCapabilities", function (capabilities) {
                if (capabilities) {
                    $scope.AvailableCoverageIds = [];
                    capabilities.Contents.CoverageSummary.forEach(function (coverageSummary) {
                        $scope.AvailableCoverageIds.push(coverageSummary.CoverageId);
                    });
                }
            });
            $scope.deleteCoverage = function () {
                if ($scope.RequestInProgress) {
                    _this.alertService.error("Cannot delete a coverage while another delete request is in progress.");
                }
                else if (!isCoverageIdValid($scope.IdOfCoverageToDelete)) {
                    _this.alertService.error("The coverage ID <b>" + $scope.IdOfCoverageToDelete + "</b> is not valid.");
                }
                else {
                    $scope.RequestInProgress = true;
                    _this.wcsService.deleteCoverage($scope.IdOfCoverageToDelete).then(function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i - 0] = arguments[_i];
                        }
                        _this.alertService.success("Successfully deleted coverage with ID <b>" + $scope.IdOfCoverageToDelete + "<b/>");
                        _this.$log.log(args);
                    }, function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i - 0] = arguments[_i];
                        }
                        _this.alertService.error("The coverage with ID <b>" + $scope.IdOfCoverageToDelete + "<b/> could not be deleted. Check the log for additional information.");
                        _this.$log.error(args);
                    }).finally(function () {
                        $scope.RequestInProgress = false;
                    });
                }
            };
            $scope.IdOfCoverageToDelete = null;
            $scope.RequestInProgress = false;
            $scope.IsCoverageIdValid = false;
        }
        DeleteCoverageController.$inject = [
            "$scope",
            "$log",
            "Notification",
            "rasdaman.WCSService"
        ];
        return DeleteCoverageController;
    })();
    rasdaman.DeleteCoverageController = DeleteCoverageController;
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../../assets/typings/tsd.d.ts"/>
///<reference path="../shared/WCSService.ts"/>
///<reference path="../main/MainController.ts"/>
var rasdaman;
(function (rasdaman) {
    var InsertCoverageController = (function () {
        function InsertCoverageController($scope, $log, alertService, wcsService) {
            var _this = this;
            this.$scope = $scope;
            this.$log = $log;
            this.alertService = alertService;
            this.wcsService = wcsService;
            $scope.UrlOfCoverageToInsert = null;
            $scope.RequestInProgress = false;
            $scope.UseGeneratedCoverageId = false;
            $scope.insertCoverage = function () {
                if ($scope.RequestInProgress) {
                    _this.alertService.error("Cannot insert a coverage while another insert request is in progress.");
                }
                else {
                    $scope.RequestInProgress = true;
                    _this.wcsService.insertCoverage($scope.UrlOfCoverageToInsert, $scope.UseGeneratedCoverageId).then(function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i - 0] = arguments[_i];
                        }
                        _this.alertService.success("Successfully inserted coverage.");
                        _this.$log.info(args);
                    }, function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i - 0] = arguments[_i];
                        }
                        _this.alertService.error("The coverage referenced by <b>" + $scope.UrlOfCoverageToInsert + "</b>  not be inserted. Check the console log for more information.");
                        _this.$log.error(args);
                    }).finally(function () {
                        $scope.RequestInProgress = false;
                    });
                }
            };
        }
        InsertCoverageController.$inject = [
            "$scope",
            "$log",
            "Notification",
            "rasdaman.WCSService"
        ];
        return InsertCoverageController;
    })();
    rasdaman.InsertCoverageController = InsertCoverageController;
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../../assets/typings/tsd.d.ts"/>
///<reference path="../shared/WCSService.ts"/>
///<reference path="../main/MainController.ts"/>
///<reference path="../../models/wcs/_wcs.ts"/>
var rasdaman;
(function (rasdaman) {
    var GetCoverageController = (function () {
        function GetCoverageController($scope, $log, wcsService, alertService) {
            $scope.$watch("StateInformation.SelectedCoverageDescriptions", function (coverageDescriptions) {
                if (coverageDescriptions && coverageDescriptions.CoverageDescription) {
                    $scope.CoverageDescription = $scope.StateInformation.SelectedCoverageDescriptions.CoverageDescription[0];
                    $scope.GetCoverageTabStates = {
                        IsCoreOpen: true,
                        IsRangeSubsettingOpen: false,
                        IsRangeSubsettingSupported: GetCoverageController.isRangeSubsettingSupported($scope.StateInformation.ServerCapabilities),
                        IsScalingOpen: false,
                        IsScalingSupported: GetCoverageController.isScalingSupported($scope.StateInformation.ServerCapabilities),
                        IsInterpolationOpen: false,
                        IsInterpolationSupported: GetCoverageController.isInterpolationSupported($scope.StateInformation.ServerCapabilities)
                    };
                    $scope.Core = {
                        Slices: [],
                        Trims: [],
                        IsTrimSelected: [],
                        IsMultiPartFormat: false,
                        SelectedCoverageFormat: $scope.StateInformation.ServerCapabilities.ServiceMetadata.FormatSupported[0]
                    };
                    var numberOfAxis = $scope.CoverageDescription.BoundedBy.Envelope.LowerCorner.Values.length;
                    for (var i = 0; i < numberOfAxis; ++i) {
                        var dimension = $scope.CoverageDescription.BoundedBy.Envelope.AxisLabels[i];
                        var min = $scope.CoverageDescription.BoundedBy.Envelope.LowerCorner.Values[i];
                        var max = $scope.CoverageDescription.BoundedBy.Envelope.UpperCorner.Values[i];
                        $scope.Core.Slices.push(new wcs.DimensionSlice(dimension, "" + Math.round((min + max) / 2)));
                        $scope.Core.Trims.push(new wcs.DimensionTrim(dimension, min + "", max + ""));
                        $scope.Core.IsTrimSelected.push(true);
                    }
                    if ($scope.GetCoverageTabStates.IsRangeSubsettingSupported) {
                        $scope.RangeSubsettingExtension = new rasdaman.RangeSubsettingModel($scope.CoverageDescription);
                    }
                    if ($scope.GetCoverageTabStates.IsScalingSupported) {
                        $scope.ScalingExtension = new rasdaman.ScalingExtensionModel($scope.CoverageDescription);
                    }
                    if ($scope.GetCoverageTabStates.IsInterpolationSupported) {
                        $scope.InterpolationExtension = new rasdaman.InterpolationExtensionModel($scope.StateInformation.ServerCapabilities);
                    }
                    $scope.getCoverage = function () {
                        var dimensionSubset = [];
                        for (var i = 0; i < numberOfAxis; ++i) {
                            var min = $scope.CoverageDescription.BoundedBy.Envelope.LowerCorner.Values[i];
                            var max = $scope.CoverageDescription.BoundedBy.Envelope.UpperCorner.Values[i];
                            if ($scope.Core.IsTrimSelected[i]) {
                                if ($scope.Core.Slices[i].SlicePoint != "" + Math.round((min + max) / 2)) {
                                    dimensionSubset.push($scope.Core.Slices[i]);
                                }
                            }
                            else {
                                if ($scope.Core.Trims[i].TrimLow != min + ""
                                    && $scope.Core.Trims[i].TrimHigh != max + "") {
                                    dimensionSubset.push($scope.Core.Trims[i]);
                                }
                            }
                        }
                        var getCoverageRequest = new wcs.GetCoverage($scope.CoverageDescription.CoverageId, dimensionSubset, $scope.Core.SelectedCoverageFormat, $scope.Core.IsMultiPartFormat);
                        getCoverageRequest.RangeSubset = $scope.RangeSubsettingExtension.RangeSubset;
                        getCoverageRequest.Scaling = $scope.ScalingExtension.getScaling();
                        getCoverageRequest.Interpolation = $scope.InterpolationExtension.getInterpolation();
                        wcsService.getCoverage(getCoverageRequest)
                            .then(function (data) {
                            $log.log(data);
                        }, function () {
                            var args = [];
                            for (var _i = 0; _i < arguments.length; _i++) {
                                args[_i - 0] = arguments[_i];
                            }
                            alertService.error("Failed to execute GetCoverage operation. Check the log for additional information.");
                            $log.error(args);
                        });
                    };
                }
            });
        }
        GetCoverageController.isRangeSubsettingSupported = function (serverCapabilities) {
            var rangeSubsettingUri = "http://www.opengis.net/spec/WCS_service-extension_range-subsetting/1.0/conf/record-subsetting";
            return serverCapabilities.ServiceIdentification.Profile.indexOf(rangeSubsettingUri) != -1;
        };
        GetCoverageController.isScalingSupported = function (serverCapabilities) {
            var scalingUri = "http://www.opengis.net/spec/WCS_service-extension_scaling/1.0/conf/scaling";
            return serverCapabilities.ServiceIdentification.Profile.indexOf(scalingUri) != -1;
        };
        GetCoverageController.isInterpolationSupported = function (serverCapabilities) {
            var interpolationUri = "http://www.opengis.net/spec/WCS_service-extension_interpolation/1.0/conf/interpolation";
            return serverCapabilities.ServiceIdentification.Profile.indexOf(interpolationUri) != -1;
        };
        GetCoverageController.$inject = [
            "$scope",
            "$log",
            "rasdaman.WCSService",
            "Notification"
        ];
        return GetCoverageController;
    })();
    rasdaman.GetCoverageController = GetCoverageController;
})(rasdaman || (rasdaman = {}));
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
var rasdaman;
(function (rasdaman) {
    var WidgetConfiguration = (function () {
        function WidgetConfiguration(type, parameters) {
            this.Type = type;
            this.Parameters = parameters;
        }
        return WidgetConfiguration;
    })();
    rasdaman.WidgetConfiguration = WidgetConfiguration;
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="WidgetConfiguration.ts"/>
var rasdaman;
(function (rasdaman) {
    var WCPSCommand = (function () {
        function WCPSCommand(command) {
            rasdaman.common.ArgumentValidator.isNotNull(command, "command");
            if (command.indexOf(">>") == -1) {
                this.WidgetConfiguration = null;
                this.Query = command;
            }
            else {
                var commandParts = command.split(">>");
                var widget = {
                    Type: commandParts[0],
                    Parameters: null
                };
                if (commandParts[0].indexOf("(") != -1) {
                    var widgetParams = commandParts[0].substring(commandParts[0].indexOf("(") + 1, commandParts[0].indexOf(")")).split(",");
                    var params = {};
                    widgetParams.forEach(function (param) {
                        var parts = param.split("=");
                        params[parts[0]] = parts[1];
                    });
                    widget.Type = commandParts[0].substring(0, commandParts[0].indexOf("("));
                    widget.Parameters = params;
                }
                this.WidgetConfiguration = widget;
                this.Query = commandParts[1];
            }
        }
        return WCPSCommand;
    })();
    rasdaman.WCPSCommand = WCPSCommand;
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="WCPSCommand.ts"/>
var rasdaman;
(function (rasdaman) {
    var WCPSQueryResult = (function () {
        function WCPSQueryResult(command) {
            rasdaman.common.ArgumentValidator.isNotNull(command, "command");
            this.Command = command;
        }
        return WCPSQueryResult;
    })();
    rasdaman.WCPSQueryResult = WCPSQueryResult;
})(rasdaman || (rasdaman = {}));
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
///<reference path="WCPSQueryResult.ts"/>
var rasdaman;
(function (rasdaman) {
    var RawWCPSResult = (function (_super) {
        __extends(RawWCPSResult, _super);
        function RawWCPSResult(command, data) {
            _super.call(this, command);
            this.Data = data.toString();
        }
        return RawWCPSResult;
    })(rasdaman.WCPSQueryResult);
    rasdaman.RawWCPSResult = RawWCPSResult;
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="WCPSQueryResult.ts"/>
var rasdaman;
(function (rasdaman) {
    var ImageWCPSResult = (function (_super) {
        __extends(ImageWCPSResult, _super);
        function ImageWCPSResult(command, rawImageData) {
            _super.call(this, command);
            this.Base64ImageData = rasdaman.common.ImageUtilities.arrayBufferToBase64(rawImageData);
            this.ImageType = (command.Query.search(/jpeg/g) === -1 ? "image/png" : "image/jpeg");
        }
        return ImageWCPSResult;
    })(rasdaman.WCPSQueryResult);
    rasdaman.ImageWCPSResult = ImageWCPSResult;
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../common/_common.ts"/>
///<reference path="WCPSQueryResult.ts"/>
var rasdaman;
(function (rasdaman) {
    var DiagramWCPSResult = (function (_super) {
        __extends(DiagramWCPSResult, _super);
        function DiagramWCPSResult(command, data) {
            _super.call(this, command);
            var diagramType = "lineChart";
            if (command.WidgetConfiguration.Parameters && command.WidgetConfiguration.Parameters.type) {
                diagramType = command.WidgetConfiguration.Parameters.type;
            }
            this.DiagramOptions = {
                chart: {
                    type: diagramType,
                    height: 300,
                    clipEdge: true,
                    showValues: true,
                    x: function (d) {
                        return d.x;
                    },
                    y: function (d) {
                        return d.y;
                    },
                    transitionDuration: 500,
                    xAxis: {
                        axisLabel: 'X',
                        showMaxMin: false
                    },
                    yAxis: {
                        axisLabel: 'Y',
                        showMaxMin: false,
                        axisLabelDistance: -20
                    }
                }
            };
            var rawData = JSON.parse("[" + data.substr(1, data.length - 2) + "]");
            var processedValues = [];
            for (var i = 0; i < rawData.length; ++i) {
                processedValues.push({
                    x: i,
                    y: rawData[i]
                });
            }
            this.DiagramData = [
                {
                    values: processedValues
                }
            ];
        }
        return DiagramWCPSResult;
    })(rasdaman.WCPSQueryResult);
    rasdaman.DiagramWCPSResult = DiagramWCPSResult;
})(rasdaman || (rasdaman = {}));
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
///<reference path="WCPSQueryResult.ts"/>
///<reference path="RawWCPSResult.ts"/>
///<reference path="ImageWCPSResult.ts"/>
///<reference path="DiagramWCPSResult.ts"/>
var rasdaman;
(function (rasdaman) {
    var WCPSResultFactory = (function () {
        function WCPSResultFactory() {
        }
        WCPSResultFactory.getResult = function (command, data) {
            if (command.WidgetConfiguration == null) {
                return new rasdaman.RawWCPSResult(command, data);
            }
            else if (command.WidgetConfiguration.Type == "diagram") {
                return new rasdaman.DiagramWCPSResult(command, data);
            }
            else if (command.WidgetConfiguration.Type == "image") {
                return new rasdaman.ImageWCPSResult(command, data);
            }
            else {
                throw new rasdaman.common.IllegalArgumentException("Invalid command.");
            }
        };
        return WCPSResultFactory;
    })();
    rasdaman.WCPSResultFactory = WCPSResultFactory;
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../../assets/typings/tsd.d.ts"/>
///<reference path="WCPSCommand.ts"/>
///<reference path="WCPSResultFactory.ts"/>
var rasdaman;
(function (rasdaman) {
    var ProcessCoverageController = (function () {
        function ProcessCoverageController($scope, $log, $interval, notificationService, wcsService) {
            $scope.EditorOptions = {
                extraKeys: { "Ctrl-Space": "autocomplete" },
                mode: "xquery",
                theme: "eclipse",
                lineNumbers: false
            };
            $scope.EditorData = [];
            $scope.AvailableQueries = ProcessCoverageController.createExampleQueries();
            $scope.Query = $scope.AvailableQueries[0].Query;
            $scope.SelectedQuery = $scope.AvailableQueries[0].Query;
            $scope.$watch("SelectedQuery", function (newValue, oldValue) {
                $scope.Query = newValue;
            });
            $scope.executeQuery = function () {
                try {
                    var command = new rasdaman.WCPSCommand($scope.Query);
                    var processCoverages = new wcs.ProcessCoverages(command.Query, []);
                    var waitingForResults = new WaitingForResult();
                    $scope.EditorData.push(waitingForResults);
                    var indexOfResults = $scope.EditorData.length - 1;
                    var waitingForResultsPromise = $interval(function () {
                        $scope.EditorData[indexOfResults].SecondsPassed++;
                    }, 1000);
                    var getBinaryData = command.WidgetConfiguration ? command.WidgetConfiguration.Type == "image" : false;
                    wcsService.processCoverages(processCoverages, getBinaryData)
                        .then(function (data) {
                        console.log(data);
                        console.log(data.data);
                        $scope.EditorData.push(rasdaman.WCPSResultFactory.getResult(command, data.data));
                    }, function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i - 0] = arguments[_i];
                        }
                        notificationService.error("Could not process the request. Check the log for additional information.");
                        $log.error(args);
                    })
                        .finally(function () {
                        $interval.cancel(waitingForResultsPromise);
                    });
                }
                catch (error) {
                    notificationService.error("Failed to send ProcessCoverages request. Check the log for additional information.");
                    $log.error(error);
                }
            };
            $scope.getEditorDataType = function (datum) {
                if (datum instanceof WaitingForResult) {
                    return 0;
                }
                if (datum instanceof rasdaman.RawWCPSResult) {
                    return 1;
                }
                if (datum instanceof rasdaman.ImageWCPSResult) {
                    return 2;
                }
                if (datum instanceof rasdaman.DiagramWCPSResult) {
                    return 3;
                }
                return -1;
            };
        }
        ProcessCoverageController.createExampleQueries = function () {
            return [
                {
                    Title: '-- No Option --',
                    Query: ''
                },
                {
                    Title: 'Encode as PNG',
                    Query: 'for c in (mean_summer_airtemp) return encode(c, "png")'
                }
            ];
        };
        ProcessCoverageController.$inject = [
            "$scope",
            "$log",
            "$interval",
            "Notification",
            "rasdaman.WCSService"
        ];
        return ProcessCoverageController;
    })();
    rasdaman.ProcessCoverageController = ProcessCoverageController;
    var WaitingForResult = (function () {
        function WaitingForResult() {
            this.SecondsPassed = 0;
        }
        return WaitingForResult;
    })();
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../../assets/typings/tsd.d.ts"/>
var rasdaman;
(function (rasdaman) {
    function RangeSubsettingExtension() {
        return {
            require: "ngModel",
            scope: {
                model: "=ngModel"
            },
            templateUrl: "src/components/range_subsetting_ext/RangeSubsettingTemplate.html"
        };
    }
    rasdaman.RangeSubsettingExtension = RangeSubsettingExtension;
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../models/wcs/_wcs.ts"/>
var rasdaman;
(function (rasdaman) {
    var RangeSubsettingModel = (function () {
        function RangeSubsettingModel(coverageDescription) {
            var _this = this;
            this.RangeSubset = new wcs.RangeSubset();
            this.AvailableRanges = [];
            this.IsInterval = [];
            coverageDescription.RangeType.DataRecord.Field.forEach(function (field) {
                _this.AvailableRanges.push(field.Name);
            });
        }
        RangeSubsettingModel.prototype.addRangeComponent = function () {
            this.RangeSubset.RangeItem.push(new wcs.RangeComponent(this.AvailableRanges[0]));
            this.IsInterval.push(false);
        };
        RangeSubsettingModel.prototype.addRangeComponentInterval = function () {
            var begin = new wcs.RangeComponent(this.AvailableRanges[0]);
            var end = new wcs.RangeComponent(this.AvailableRanges[this.AvailableRanges.length - 1]);
            this.RangeSubset.RangeItem.push(new wcs.RangeInterval(begin, end));
            this.IsInterval.push(true);
        };
        RangeSubsettingModel.prototype.deleteRangeComponent = function (index) {
            this.RangeSubset.RangeItem.splice(index, 1);
        };
        return RangeSubsettingModel;
    })();
    rasdaman.RangeSubsettingModel = RangeSubsettingModel;
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../models/wcs/_wcs.ts"/>
var rasdaman;
(function (rasdaman) {
    var ScalingExtensionModel = (function () {
        function ScalingExtensionModel(coverageDescription) {
            this.coverageDescription = coverageDescription;
            var i = 0;
            var axes = [];
            coverageDescription.BoundedBy.Envelope.AxisLabels.forEach(function (label) {
                axes.push(label);
            });
            this.ScaleByFactor = new wcs.ScaleByFactor(ScalingExtensionModel.DEFAULT_SCALE_FACTOR);
            var scaleAxis = [];
            for (i = 0; i < axes.length; ++i) {
                scaleAxis.push(new wcs.ScaleAxis(axes[i], ScalingExtensionModel.DEFAULT_SCALE_FACTOR));
            }
            this.ScaleAxesByFactor = new wcs.ScaleAxesByFactor(scaleAxis);
            var targetAxisSize = [];
            for (i = 0; i < axes.length; ++i) {
                targetAxisSize.push(new wcs.TargetAxisSize(axes[i], ScalingExtensionModel.DEFAULT_AXIS_SIZE));
            }
            this.ScaleToSize = new wcs.ScaleToSize(targetAxisSize);
            var targetAxisExtent = [];
            for (i = 0; i < axes.length; ++i) {
                var low = coverageDescription.BoundedBy.Envelope.LowerCorner.Values[i];
                var high = coverageDescription.BoundedBy.Envelope.UpperCorner.Values[i];
                targetAxisExtent.push(new wcs.TargetAxisExtent(axes[i], low, high));
            }
            this.ScaleToExtent = new wcs.ScaleToExtent(targetAxisExtent);
            this.ScalingType = 0;
        }
        ScalingExtensionModel.prototype.getScaling = function () {
            if (0 == this.ScalingType) {
                return this.getScaleByFactor();
            }
            else if (1 == this.ScalingType) {
                return this.getScaleAxesByFactor();
            }
            else if (2 == this.ScalingType) {
                return this.getScaleToSize();
            }
            else {
                return this.getScaleToExtent();
            }
        };
        ScalingExtensionModel.prototype.clearScaling = function () {
            var i = 0;
            this.ScaleByFactor.ScaleFactor = ScalingExtensionModel.DEFAULT_SCALE_FACTOR;
            for (i = 0; i < this.ScaleAxesByFactor.ScaleAxis.length; ++i) {
                this.ScaleAxesByFactor.ScaleAxis[i].ScaleFactor = ScalingExtensionModel.DEFAULT_SCALE_FACTOR;
            }
            for (i = 0; i < this.ScaleToSize.TargetAxisSize.length; ++i) {
                this.ScaleToSize.TargetAxisSize[i].TargetSize = ScalingExtensionModel.DEFAULT_AXIS_SIZE;
            }
            for (i = 0; i < this.ScaleToExtent.TargetAxisExtent.length; ++i) {
                var low = this.coverageDescription.BoundedBy.Envelope.LowerCorner.Values[i];
                var high = this.coverageDescription.BoundedBy.Envelope.UpperCorner.Values[i];
                this.ScaleToExtent.TargetAxisExtent[i].Low = low;
                this.ScaleToExtent.TargetAxisExtent[i].High = high;
            }
            this.ScalingType = 0;
        };
        ScalingExtensionModel.prototype.getScaleByFactor = function () {
            if (this.ScaleByFactor.ScaleFactor != ScalingExtensionModel.DEFAULT_SCALE_FACTOR) {
                return this.ScaleByFactor;
            }
            else {
                return null;
            }
        };
        ScalingExtensionModel.prototype.getScaleAxesByFactor = function () {
            for (var i = 0; i < this.ScaleAxesByFactor.ScaleAxis.length; ++i) {
                if (this.ScaleAxesByFactor.ScaleAxis[i].ScaleFactor != ScalingExtensionModel.DEFAULT_SCALE_FACTOR) {
                    return this.ScaleAxesByFactor;
                }
            }
            return null;
        };
        ScalingExtensionModel.prototype.getScaleToSize = function () {
            for (var i = 0; i < this.ScaleToSize.TargetAxisSize.length; ++i) {
                if (this.ScaleToSize.TargetAxisSize[i].TargetSize != ScalingExtensionModel.DEFAULT_AXIS_SIZE) {
                    return this.ScaleToSize;
                }
            }
            return null;
        };
        ScalingExtensionModel.prototype.getScaleToExtent = function () {
            for (var i = 0; i < this.ScaleToExtent.TargetAxisExtent.length; ++i) {
                var low = this.coverageDescription.BoundedBy.Envelope.LowerCorner.Values[i];
                var high = this.coverageDescription.BoundedBy.Envelope.UpperCorner.Values[i];
                if (this.ScaleToExtent.TargetAxisExtent[i].Low != low
                    || this.ScaleToExtent.TargetAxisExtent[i].High != high) {
                    return this.ScaleToExtent;
                }
            }
            return null;
        };
        ScalingExtensionModel.DEFAULT_SCALE_FACTOR = 1.0;
        ScalingExtensionModel.DEFAULT_AXIS_SIZE = 0.0;
        return ScalingExtensionModel;
    })();
    rasdaman.ScalingExtensionModel = ScalingExtensionModel;
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../../assets/typings/tsd.d.ts"/>
var rasdaman;
(function (rasdaman) {
    function ScalingExtension() {
        return {
            require: "ngModel",
            scope: {
                model: "=ngModel"
            },
            templateUrl: "src/components/scaling_ext/ScalingExtentionTemplate.html"
        };
    }
    rasdaman.ScalingExtension = ScalingExtension;
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../models/wcs/_wcs.ts"/>
var rasdaman;
(function (rasdaman) {
    var InterpolationExtensionModel = (function () {
        function InterpolationExtensionModel(serverCapabilities) {
            var _this = this;
            this.AvailableInterpolationMethods = [];
            for (var i = 0; i < serverCapabilities.ServiceMetadata.Extension.length; ++i) {
                if (serverCapabilities.ServiceMetadata.Extension[i].InterpolationMetadata) {
                    serverCapabilities.ServiceMetadata.Extension[i].InterpolationMetadata.InterpolationSupported.forEach(function (interpolationUri) {
                        _this.AvailableInterpolationMethods.push({
                            Name: interpolationUri,
                            Uri: interpolationUri
                        });
                    });
                    break;
                }
            }
        }
        InterpolationExtensionModel.prototype.getInterpolation = function () {
            var interpolationUri = "";
            if (this.SelectedInterpolationMethod) {
                interpolationUri = this.SelectedInterpolationMethod.Uri;
            }
            return new wcs.Interpolation(interpolationUri);
        };
        return InterpolationExtensionModel;
    })();
    rasdaman.InterpolationExtensionModel = InterpolationExtensionModel;
})(rasdaman || (rasdaman = {}));
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
///<reference path="../../../assets/typings/tsd.d.ts"/>
var rasdaman;
(function (rasdaman) {
    function InterpolationExtension() {
        return {
            require: "ngModel",
            scope: {
                model: "=ngModel"
            },
            templateUrl: "src/components/interpolation_ext/InterpolationExtensionTemplate.html"
        };
    }
    rasdaman.InterpolationExtension = InterpolationExtension;
})(rasdaman || (rasdaman = {}));
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
/// <reference path="shared/Constants.ts"/>
/// <reference path="shared/WCSService.ts"/>
/// <reference path="settings/SettingsService.ts"/>
/// <reference path="settings/SettingsController.ts"/>
/// <reference path="get_capabilities/GetCapabilitiesController.ts"/>
/// <reference path="describe_coverage/DescribeCoverageController.ts"/>
/// <reference path="delete_coverage/DeleteCoverageController.ts"/>
/// <reference path="insert_coverage/InsertCoverageController.ts"/>
/// <reference path="get_coverage/GetCoverageController.ts"/>
/// <reference path="process_coverage/ProcessCoverageController.ts"/>
/// <reference path="range_subsetting_ext/RangeSubsettingExtension.ts"/>
/// <reference path="range_subsetting_ext/RangeSubsettingModel.ts"/>
/// <reference path="scaling_ext/ScalingExtensionModel.ts"/>
/// <reference path="scaling_ext/ScalingExtension.ts"/>
/// <reference path="interpolation_ext/InterpolationExtensionModel.ts"/>
/// <reference path="interpolation_ext/InterpolationExtension.ts"/>
/// <reference path="main/MainController.ts"/>
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
/// <reference path="../assets/typings/tsd.d.ts"/>
/// <reference path="common/_common.ts"/>
/// <reference path="components/_components.ts"/>
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
/// <reference path="_all.ts" />
var rasdaman;
(function (rasdaman) {
    "use strict";
    var AngularConfig = (function () {
        function AngularConfig($httpProvider, $urlRouterProvider, $stateProvider, NotificationProvider) {
            $httpProvider.defaults.useXDomain = true;
            $stateProvider.state('wcs', {
                url: "/wcs",
                views: {
                    'get_capabilities': {
                        url: "get_capabilities",
                        templateUrl: 'src/components/get_capabilities/GetCapabilitiesView.html',
                        controller: rasdaman.GetCapabilitiesController
                    },
                    'describe_coverage': {
                        url: "describe_coverage",
                        templateUrl: 'src/components/describe_coverage/DescribeCoverageView.html',
                        controller: rasdaman.DescribeCoverageController
                    },
                    'get_coverage': {
                        templateUrl: 'src/components/get_coverage/GetCoverageView.html',
                        controller: rasdaman.GetCoverageController
                    },
                    'process_coverages': {
                        templateUrl: 'src/components/process_coverage/ProcessCoverageView.html',
                        controller: rasdaman.ProcessCoverageController
                    },
                    'insert_coverage': {
                        templateUrl: 'src/components/insert_coverage/InsertCoverageView.html',
                        controller: rasdaman.InsertCoverageController
                    },
                    'delete_coverage': {
                        templateUrl: 'src/components/delete_coverage/DeleteCoverageView.html',
                        controller: rasdaman.DeleteCoverageController
                    }
                }
            });
            $urlRouterProvider.otherwise('/wcs');
            NotificationProvider.setOptions({
                delay: 10000,
                startTop: 20,
                startRight: 10,
                verticalSpacing: 20,
                horizontalSpacing: 20,
                positionX: 'right',
                positionY: 'top'
            });
        }
        AngularConfig.$inject = [
            "$httpProvider",
            "$urlRouterProvider",
            "$stateProvider",
            "NotificationProvider"
        ];
        return AngularConfig;
    })();
    rasdaman.AngularConfig = AngularConfig;
    var wcsClient = angular
        .module(rasdaman.Constants.APP_NAME, ["ngRoute",
        "ngAnimate",
        "ngSanitize",
        "ui.bootstrap",
        "smart-table",
        "ui.router",
        "ui-notification",
        "ui.codemirror",
        "luegg.directives",
        "nvd3"])
        .config(AngularConfig)
        .service("rasdaman.SettingsService", rasdaman.SettingsService)
        .service("rasdaman.common.SerializedObjectFactory", rasdaman.common.SerializedObjectFactory)
        .service("rasdaman.WCSService", rasdaman.WCSService)
        .controller("rasdaman.SettingsController", rasdaman.SettingsController)
        .controller("rasdaman.GetCapabilitiesController", rasdaman.GetCapabilitiesController)
        .controller("rasdaman.DescribeCoverageController", rasdaman.DescribeCoverageController)
        .controller("rasdaman.DeleteCoverageController", rasdaman.DeleteCoverageController)
        .controller("rasdaman.GetCoverageController", rasdaman.GetCoverageController)
        .controller("rasdaman.ProcessCoverageController", rasdaman.ProcessCoverageController)
        .controller("rasdaman.MainController", rasdaman.MainController)
        .directive("rangeSubsettingExtension", rasdaman.RangeSubsettingExtension)
        .directive("scalingExtension", rasdaman.ScalingExtension)
        .directive("interpolationExtension", rasdaman.InterpolationExtension)
        .directive("rasPrettyPrint", rasdaman.common.PrettyPrint)
        .directive("stringToNumberConverter", rasdaman.common.StringToNumberConverter)
        .directive("autocomplete", rasdaman.common.Autocomplete);
})(rasdaman || (rasdaman = {}));
//# sourceMappingURL=main.js.map
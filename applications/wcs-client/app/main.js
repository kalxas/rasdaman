var __extends = (this && this.__extends) || (function () {
    var extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        var Exception = (function (_super) {
            __extends(Exception, _super);
            function Exception(message) {
                var _this = _super.call(this, message) || this;
                _this.name = "Exception";
                _this.message = message;
                _this.stack = new Error().stack;
                return _this;
            }
            Exception.prototype.toString = function () {
                return this.name + ": " + this.message;
            };
            return Exception;
        }(Error));
        common.Exception = Exception;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        var InvalidAttributeNameException = (function (_super) {
            __extends(InvalidAttributeNameException, _super);
            function InvalidAttributeNameException(attributeName) {
                return _super.call(this, "The attribute \"" + attributeName + "\" does not exist on this element.") || this;
            }
            return InvalidAttributeNameException;
        }(common.Exception));
        common.InvalidAttributeNameException = InvalidAttributeNameException;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        var InvalidElementNameException = (function (_super) {
            __extends(InvalidElementNameException, _super);
            function InvalidElementNameException(elementName) {
                return _super.call(this, "The child element \"" + elementName + "\" does not exist on this element.") || this;
            }
            return InvalidElementNameException;
        }(common.Exception));
        common.InvalidElementNameException = InvalidElementNameException;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        var LogicException = (function (_super) {
            __extends(LogicException, _super);
            function LogicException(message) {
                var _this = _super.call(this, message) || this;
                _this.name = "LogicException";
                return _this;
            }
            return LogicException;
        }(common.Exception));
        common.LogicException = LogicException;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        var NotImplementedException = (function (_super) {
            __extends(NotImplementedException, _super);
            function NotImplementedException() {
                var _this = _super.call(this, "The method was not implemented.") || this;
                _this.name = "NotImplementedException";
                return _this;
            }
            return NotImplementedException;
        }(common.Exception));
        common.NotImplementedException = NotImplementedException;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        var IllegalArgumentException = (function (_super) {
            __extends(IllegalArgumentException, _super);
            function IllegalArgumentException(message) {
                var _this = _super.call(this, message) || this;
                _this.name = "IllegalArgumentException";
                return _this;
            }
            return IllegalArgumentException;
        }(common.Exception));
        common.IllegalArgumentException = IllegalArgumentException;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
        }());
        common.ArgumentValidator = ArgumentValidator;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
        }());
        common.ImageUtilities = ImageUtilities;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        var ResponseDocumentType;
        (function (ResponseDocumentType) {
            ResponseDocumentType[ResponseDocumentType["XML"] = 1] = "XML";
            ResponseDocumentType[ResponseDocumentType["SOAP"] = 2] = "SOAP";
            ResponseDocumentType[ResponseDocumentType["JSON"] = 3] = "JSON";
        })(ResponseDocumentType = common.ResponseDocumentType || (common.ResponseDocumentType = {}));
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
        }());
        common.ResponseDocument = ResponseDocument;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
        }());
        common.Response = Response;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
                return this.jsonObject._text.toString();
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
        }());
        common.XMLSerializedObject = XMLSerializedObject;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
        }());
        common.SerializedObjectFactory = SerializedObjectFactory;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
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
var rasdaman;
(function (rasdaman) {
    var Constants = (function () {
        function Constants() {
        }
        Constants.APP_NAME = "wcsClient";
        Constants.PROCESSING_EXT_URI = "http://www.opengis.net/spec/WCS_service-extension_processing/2.0/conf/processing";
        Constants.TRANSACTION_EXT_URI = "http://www.opengis.net/spec/WCS_service-extension_transaction/2.0/conf/insert+delete";
        Constants.RANGE_SUBSETTING_EXT_URI = "http://www.opengis.net/spec/WCS_service-extension_range-subsetting/1.0/conf/record-subsetting";
        Constants.SCALING_EXT_URI = "http://www.opengis.net/spec/WCS_service-extension_scaling/1.0/conf/scaling";
        Constants.INTERPOLATION_EXT_URI = "http://www.opengis.net/spec/WCS_service-extension_interpolation/1.0/conf/interpolation";
        return Constants;
    }());
    rasdaman.Constants = Constants;
})(rasdaman || (rasdaman = {}));
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
    }());
    ows.Address = Address;
})(ows || (ows = {}));
var ows;
(function (ows) {
    var BoundingBox = (function () {
        function BoundingBox(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return BoundingBox;
    }());
    ows.BoundingBox = BoundingBox;
})(ows || (ows = {}));
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
    }());
    ows.LanguageString = LanguageString;
})(ows || (ows = {}));
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
    }());
    ows.OnlineResource = OnlineResource;
})(ows || (ows = {}));
var ows;
(function (ows) {
    var Uri = (function () {
        function Uri(uri) {
        }
        return Uri;
    }());
    ows.Uri = Uri;
})(ows || (ows = {}));
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
    }());
    ows.Code = Code;
})(ows || (ows = {}));
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
    }());
    ows.Keywords = Keywords;
})(ows || (ows = {}));
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
    }());
    ows.Description = Description;
})(ows || (ows = {}));
var ows;
(function (ows) {
    var ServiceIdentification = (function (_super) {
        __extends(ServiceIdentification, _super);
        function ServiceIdentification(source) {
            var _this = this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            _this = _super.call(this, source) || this;
            _this.ServiceType = new ows.Code(source.getChildAsSerializedObject("ServiceType"));
            _this.ServiceTypeVersion = [];
            source.getChildrenAsSerializedObjects("ows:ServiceTypeVersion").forEach(function (s) {
                _this.ServiceTypeVersion.push(s.getValueAsString());
            });
            _this.Profile = [];
            source.getChildrenAsSerializedObjects("ows:Profile").forEach(function (s) {
                _this.Profile.push(s.getValueAsString());
            });
            if (source.doesElementExist("ows:Fees")) {
                _this.Fees = source.getChildAsSerializedObject("ows:Fees").getValueAsString();
            }
            if (source.doesElementExist("ows:AccessConstraints")) {
                _this.AccessConstraints = source.getChildAsSerializedObject("ows:AccessConstraints").getValueAsString();
            }
            return _this;
        }
        return ServiceIdentification;
    }(ows.Description));
    ows.ServiceIdentification = ServiceIdentification;
})(ows || (ows = {}));
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
    }());
    ows.Phone = Phone;
})(ows || (ows = {}));
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
    }());
    ows.ContactInfo = ContactInfo;
})(ows || (ows = {}));
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
    }());
    ows.ResponsiblePartySubset = ResponsiblePartySubset;
})(ows || (ows = {}));
var ows;
(function (ows) {
    var ServiceContact = (function (_super) {
        __extends(ServiceContact, _super);
        function ServiceContact(source) {
            var _this = this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            _this = _super.call(this, source) || this;
            return _this;
        }
        return ServiceContact;
    }(ows.ResponsiblePartySubset));
    ows.ServiceContact = ServiceContact;
})(ows || (ows = {}));
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
    }());
    ows.ServiceProvider = ServiceProvider;
})(ows || (ows = {}));
var ows;
(function (ows) {
    var Constraint = (function () {
        function Constraint(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return Constraint;
    }());
    ows.Constraint = Constraint;
})(ows || (ows = {}));
var ows;
(function (ows) {
    var RequestMethod = (function (_super) {
        __extends(RequestMethod, _super);
        function RequestMethod(source) {
            var _this = _super.call(this, source) || this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            _this.Constraint = [];
            source.getChildrenAsSerializedObjects("ows:Constraint").forEach(function (o) {
                _this.Constraint.push(new ows.Constraint(o));
            });
            return _this;
        }
        return RequestMethod;
    }(ows.OnlineResource));
    ows.RequestMethod = RequestMethod;
})(ows || (ows = {}));
var ows;
(function (ows) {
    var Get = (function (_super) {
        __extends(Get, _super);
        function Get(source) {
            var _this = _super.call(this, source) || this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            return _this;
        }
        return Get;
    }(ows.RequestMethod));
    ows.Get = Get;
})(ows || (ows = {}));
var ows;
(function (ows) {
    var Post = (function (_super) {
        __extends(Post, _super);
        function Post(source) {
            var _this = _super.call(this, source) || this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            return _this;
        }
        return Post;
    }(ows.RequestMethod));
    ows.Post = Post;
})(ows || (ows = {}));
var ows;
(function (ows) {
    var HTTP = (function () {
        function HTTP(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.Get = new ows.Get(source.getChildAsSerializedObject("ows:Get"));
            this.Post = new ows.Post(source.getChildAsSerializedObject("ows:Post"));
        }
        return HTTP;
    }());
    ows.HTTP = HTTP;
})(ows || (ows = {}));
var ows;
(function (ows) {
    var DCP = (function () {
        function DCP(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.HTTP = new ows.HTTP(source.getChildAsSerializedObject("ows:HTTP"));
        }
        return DCP;
    }());
    ows.DCP = DCP;
})(ows || (ows = {}));
var ows;
(function (ows) {
    var Parameter = (function () {
        function Parameter(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return Parameter;
    }());
    ows.Parameter = Parameter;
})(ows || (ows = {}));
var ows;
(function (ows) {
    var Metadata = (function () {
        function Metadata(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return Metadata;
    }());
    ows.Metadata = Metadata;
})(ows || (ows = {}));
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
    }());
    ows.Operation = Operation;
})(ows || (ows = {}));
var ows;
(function (ows) {
    var ExtendedCapabilities = (function () {
        function ExtendedCapabilities(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return ExtendedCapabilities;
    }());
    ows.ExtendedCapabilities = ExtendedCapabilities;
})(ows || (ows = {}));
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
    }());
    ows.OperationsMetadata = OperationsMetadata;
})(ows || (ows = {}));
var ows;
(function (ows) {
    var Languages = (function () {
        function Languages(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return Languages;
    }());
    ows.Languages = Languages;
})(ows || (ows = {}));
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
    }());
    ows.CapabilitiesBase = CapabilitiesBase;
})(ows || (ows = {}));
var ows;
(function (ows) {
    var ContentsBase = (function () {
        function ContentsBase(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return ContentsBase;
    }());
    ows.ContentsBase = ContentsBase;
})(ows || (ows = {}));
var ows;
(function (ows) {
    var Section = (function () {
        function Section(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return Section;
    }());
    ows.Section = Section;
})(ows || (ows = {}));
var ows;
(function (ows) {
    var GetCapabilities = (function () {
        function GetCapabilities() {
            this.Request = "GetCapabilities";
        }
        return GetCapabilities;
    }());
    ows.GetCapabilities = GetCapabilities;
})(ows || (ows = {}));
var ows;
(function (ows) {
    var Exception = (function () {
        function Exception(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.ExceptionText = source.getChildAsSerializedObject("ExceptionText").getValueAsString();
            if (source.doesAttributeExist("exceptionCode")) {
                this.ExceptionCode = source.getAttributeAsString("exceptionCode");
            }
            if (source.doesAttributeExist("locator")) {
                this.Locator = source.getAttributeAsString("locator");
            }
        }
        return Exception;
    }());
    ows.Exception = Exception;
})(ows || (ows = {}));
var ows;
(function (ows) {
    var ExceptionReport = (function () {
        function ExceptionReport(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.Exception = new ows.Exception(source.getChildAsSerializedObject("Exception"));
        }
        return ExceptionReport;
    }());
    ows.ExceptionReport = ExceptionReport;
})(ows || (ows = {}));
var ows;
(function (ows) {
    var WGS84BoundingBox = (function () {
        function WGS84BoundingBox(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return WGS84BoundingBox;
    }());
    ows.WGS84BoundingBox = WGS84BoundingBox;
})(ows || (ows = {}));
var wcs;
(function (wcs) {
    var Extension = (function () {
        function Extension(source) {
            if (source.doesElementExist("int:InterpolationMetadata")) {
                this.InterpolationMetadata = new wcs.InterpolationMetadata(source.getChildAsSerializedObject("int:InterpolationMetadata"));
            }
        }
        return Extension;
    }());
    wcs.Extension = Extension;
})(wcs || (wcs = {}));
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
    }());
    wcs.ServiceMetadata = ServiceMetadata;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var CoverageSubtypeParent = (function () {
        function CoverageSubtypeParent(source) {
        }
        return CoverageSubtypeParent;
    }());
    wcs.CoverageSubtypeParent = CoverageSubtypeParent;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var CoverageSummary = (function (_super) {
        __extends(CoverageSummary, _super);
        function CoverageSummary(source) {
            var _this = _super.call(this, source) || this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            _this.CoverageId = source.getChildAsSerializedObject("wcs:CoverageId").getValueAsString();
            _this.CoverageSubtype = source.getChildAsSerializedObject("wcs:CoverageSubtype").getValueAsString();
            if (source.doesElementExist("wcs:CoverageSubtypeParent")) {
                _this.CoverageSubtypeParent = new wcs.CoverageSubtypeParent(source.getChildAsSerializedObject("wcs:CoverageSubtypeParent"));
            }
            _this.WGS84BoundingBox = [];
            source.getChildrenAsSerializedObjects("ows:WGS84BoundingBox").forEach(function (o) {
                _this.WGS84BoundingBox.push(new ows.WGS84BoundingBox(o));
            });
            _this.BoundingBox = [];
            source.getChildrenAsSerializedObjects("ows:BoundingBox").forEach(function (o) {
                _this.BoundingBox.push(new ows.BoundingBox(o));
            });
            _this.Metadata = [];
            source.getChildrenAsSerializedObjects("ows:Metadata").forEach(function (o) {
                _this.Metadata.push(new ows.Metadata(o));
            });
            return _this;
        }
        return CoverageSummary;
    }(ows.Description));
    wcs.CoverageSummary = CoverageSummary;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var Contents = (function (_super) {
        __extends(Contents, _super);
        function Contents(source) {
            var _this = _super.call(this, source) || this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            _this.CoverageSummary = [];
            source.getChildrenAsSerializedObjects("wcs:CoverageSummary").forEach(function (o) {
                _this.CoverageSummary.push(new wcs.CoverageSummary(o));
            });
            if (source.doesElementExist("wcs.Extension")) {
                _this.Extension = new wcs.Extension(source.getChildAsSerializedObject("wcs.Extension"));
            }
            return _this;
        }
        return Contents;
    }(ows.ContentsBase));
    wcs.Contents = Contents;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var Capabilities = (function (_super) {
        __extends(Capabilities, _super);
        function Capabilities(source) {
            var _this = _super.call(this, source) || this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            if (source.doesElementExist("wcs:ServiceMetadata")) {
                _this.ServiceMetadata = new wcs.ServiceMetadata(source.getChildAsSerializedObject("wcs:ServiceMetadata"));
            }
            if (source.doesElementExist("wcs:Contents")) {
                _this.Contents = new wcs.Contents(source.getChildAsSerializedObject("wcs:Contents"));
            }
            return _this;
        }
        return Capabilities;
    }(ows.CapabilitiesBase));
    wcs.Capabilities = Capabilities;
})(wcs || (wcs = {}));
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
    }());
    gml.Pos = Pos;
})(gml || (gml = {}));
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
                _this.Values.push(o);
            });
        }
        return LowerCorner;
    }());
    gml.LowerCorner = LowerCorner;
})(gml || (gml = {}));
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
                _this.Values.push(o);
            });
        }
        return UpperCorner;
    }());
    gml.UpperCorner = UpperCorner;
})(gml || (gml = {}));
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
    }());
    gml.Envelope = Envelope;
})(gml || (gml = {}));
var gml;
(function (gml) {
    var EnvelopeWithTimePeriod = (function (_super) {
        __extends(EnvelopeWithTimePeriod, _super);
        function EnvelopeWithTimePeriod(source) {
            var _this = _super.call(this, source) || this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            return _this;
        }
        return EnvelopeWithTimePeriod;
    }(gml.Envelope));
    gml.EnvelopeWithTimePeriod = EnvelopeWithTimePeriod;
})(gml || (gml = {}));
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
    }());
    gml.BoundedBy = BoundedBy;
})(gml || (gml = {}));
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
    }());
    gml.AbstractFeature = AbstractFeature;
})(gml || (gml = {}));
var gml;
(function (gml) {
    var CoverageFunction = (function () {
        function CoverageFunction(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return CoverageFunction;
    }());
    gml.CoverageFunction = CoverageFunction;
})(gml || (gml = {}));
var gml;
(function (gml) {
    var DomainSet = (function () {
        function DomainSet(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return DomainSet;
    }());
    gml.DomainSet = DomainSet;
})(gml || (gml = {}));
var gmlcov;
(function (gmlcov) {
    var Metadata = (function () {
        function Metadata(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        return Metadata;
    }());
    gmlcov.Metadata = Metadata;
})(gmlcov || (gmlcov = {}));
var swe;
(function (swe) {
    var Uom = (function () {
        function Uom(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.Code = source.getAttributeAsString("code");
        }
        return Uom;
    }());
    swe.Uom = Uom;
})(swe || (swe = {}));
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
    }());
    swe.Quantity = Quantity;
})(swe || (swe = {}));
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
    }());
    swe.Field = Field;
})(swe || (swe = {}));
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
    }());
    swe.DataRecord = DataRecord;
})(swe || (swe = {}));
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
    }());
    gmlcov.RangeType = RangeType;
})(gmlcov || (gmlcov = {}));
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
    }());
    wcs.ServiceParameters = ServiceParameters;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var CoverageDescription = (function (_super) {
        __extends(CoverageDescription, _super);
        function CoverageDescription(source) {
            var _this = _super.call(this, source) || this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            _this.CoverageId = source.getChildAsSerializedObject("wcs:CoverageId").getValueAsString();
            if (source.doesElementExist("gml:coverageFunction")) {
                _this.CoverageFunction = new gml.CoverageFunction(source.getChildAsSerializedObject("gml:coverageFunction"));
            }
            _this.Metadata = [];
            source.getChildrenAsSerializedObjects("gmlcov:metadata").forEach(function (o) {
                _this.Metadata.push(new gmlcov.Metadata(o));
            });
            _this.DomainSet = new gml.DomainSet(source.getChildAsSerializedObject("gml:domainSet"));
            _this.RangeType = new gmlcov.RangeType(source.getChildAsSerializedObject("gmlcov:rangeType"));
            _this.ServiceParameters = new wcs.ServiceParameters(source.getChildAsSerializedObject("wcs:ServiceParameters"));
            return _this;
        }
        return CoverageDescription;
    }(gml.AbstractFeature));
    wcs.CoverageDescription = CoverageDescription;
})(wcs || (wcs = {}));
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
    }());
    wcs.CoverageDescriptions = CoverageDescriptions;
})(wcs || (wcs = {}));
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
    }());
    wcs.RequestBase = RequestBase;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var DescribeCoverage = (function (_super) {
        __extends(DescribeCoverage, _super);
        function DescribeCoverage(coverageIds) {
            var _this = _super.call(this) || this;
            if (!coverageIds.length) {
                throw new rasdaman.common.IllegalArgumentException("coverageIds");
            }
            _this.CoverageId = angular.copy(coverageIds);
            return _this;
        }
        DescribeCoverage.prototype.toKVP = function () {
            var serialization = _super.prototype.toKVP.call(this);
            serialization += "&REQUEST=DescribeCoverage";
            serialization += "&COVERAGEID=" + this.CoverageId.join(",");
            return serialization;
        };
        return DescribeCoverage;
    }(wcs.RequestBase));
    wcs.DescribeCoverage = DescribeCoverage;
})(wcs || (wcs = {}));
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
    }());
    wcs.DimensionSubset = DimensionSubset;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var DimensionSlice = (function (_super) {
        __extends(DimensionSlice, _super);
        function DimensionSlice(dimension, slicePoint) {
            var _this = _super.call(this, dimension) || this;
            _this.SlicePoint = slicePoint;
            return _this;
        }
        DimensionSlice.prototype.toKVP = function () {
            return this.Dimension + "(" + this.SlicePoint + ")";
        };
        return DimensionSlice;
    }(wcs.DimensionSubset));
    wcs.DimensionSlice = DimensionSlice;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var DimensionTrim = (function (_super) {
        __extends(DimensionTrim, _super);
        function DimensionTrim(dimension, trimLow, trimHigh) {
            var _this = _super.call(this, dimension) || this;
            _this.TrimLow = trimLow;
            _this.TrimHigh = trimHigh;
            return _this;
        }
        DimensionTrim.prototype.toKVP = function () {
            return this.Dimension + "(" + this.TrimLow + "," + this.TrimHigh + ")";
        };
        return DimensionTrim;
    }(wcs.DimensionSubset));
    wcs.DimensionTrim = DimensionTrim;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var GetCapabilities = (function (_super) {
        __extends(GetCapabilities, _super);
        function GetCapabilities() {
            var _this = _super.call(this) || this;
            _this.Service = "WCS";
            _this.AcceptVersions = ["2.0.1"];
            return _this;
        }
        GetCapabilities.prototype.toKVP = function () {
            return "&SERVICE=" + this.Service +
                "&ACCEPTVERSIONS=" + this.AcceptVersions[0] +
                "&REQUEST=" + this.Request;
        };
        return GetCapabilities;
    }(ows.GetCapabilities));
    wcs.GetCapabilities = GetCapabilities;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var RangeItem = (function () {
        function RangeItem() {
        }
        RangeItem.prototype.toKVP = function () {
            throw new rasdaman.common.NotImplementedException();
        };
        return RangeItem;
    }());
    wcs.RangeItem = RangeItem;
})(wcs || (wcs = {}));
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
    }());
    wcs.RangeSubset = RangeSubset;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var Scaling = (function () {
        function Scaling() {
        }
        Scaling.prototype.toKVP = function () {
            throw new rasdaman.common.NotImplementedException();
        };
        return Scaling;
    }());
    wcs.Scaling = Scaling;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var GetCoverage = (function (_super) {
        __extends(GetCoverage, _super);
        function GetCoverage(coverageId, dimensionSubset, format, mediaType) {
            var _this = _super.call(this) || this;
            _this.CoverageId = coverageId;
            _this.DimensionSubset = [];
            dimensionSubset.forEach(function (o) {
                _this.DimensionSubset.push(o);
            });
            _this.Format = format;
            _this.MediaType = mediaType;
            return _this;
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
    }(wcs.RequestBase));
    wcs.GetCoverage = GetCoverage;
})(wcs || (wcs = {}));
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
    }());
    wcs.InterpolationMetadata = InterpolationMetadata;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var ProcessCoverages = (function (_super) {
        __extends(ProcessCoverages, _super);
        function ProcessCoverages(query, extraParams) {
            var _this = _super.call(this) || this;
            rasdaman.common.ArgumentValidator.isNotNull(query, "query");
            rasdaman.common.ArgumentValidator.isNotNull(extraParams, "extraParams");
            rasdaman.common.ArgumentValidator.isArray(extraParams, "extraParams");
            _this.Request = "ProcessCoverages";
            _this.Query = query;
            _this.ExtraParameters = angular.copy(extraParams);
            return _this;
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
    }(wcs.RequestBase));
    wcs.ProcessCoverages = ProcessCoverages;
})(wcs || (wcs = {}));
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
    }());
    wcs.Interpolation = Interpolation;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var RangeComponent = (function (_super) {
        __extends(RangeComponent, _super);
        function RangeComponent(rangeComponent) {
            var _this = _super.call(this) || this;
            _this.RangeComponent = rangeComponent;
            return _this;
        }
        RangeComponent.prototype.toKVP = function () {
            return this.RangeComponent;
        };
        return RangeComponent;
    }(wcs.RangeItem));
    wcs.RangeComponent = RangeComponent;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var RangeInterval = (function (_super) {
        __extends(RangeInterval, _super);
        function RangeInterval(startComponent, endComponent) {
            var _this = _super.call(this) || this;
            _this.StartComponent = startComponent;
            _this.EndComponent = endComponent;
            return _this;
        }
        RangeInterval.prototype.toKVP = function () {
            return this.StartComponent.toKVP() + ":" + this.EndComponent.toKVP();
        };
        return RangeInterval;
    }(wcs.RangeItem));
    wcs.RangeInterval = RangeInterval;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var ScaleByFactor = (function (_super) {
        __extends(ScaleByFactor, _super);
        function ScaleByFactor(scaleFactor) {
            var _this = _super.call(this) || this;
            if (scaleFactor < 0) {
                throw new rasdaman.common.IllegalArgumentException("ScaleFactor must be positive.");
            }
            _this.ScaleFactor = scaleFactor;
            return _this;
        }
        ScaleByFactor.prototype.toKVP = function () {
            return "&SCALEFACTOR=" + this.ScaleFactor;
        };
        return ScaleByFactor;
    }(wcs.Scaling));
    wcs.ScaleByFactor = ScaleByFactor;
})(wcs || (wcs = {}));
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
    }());
    wcs.ScaleAxis = ScaleAxis;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var ScaleAxesByFactor = (function (_super) {
        __extends(ScaleAxesByFactor, _super);
        function ScaleAxesByFactor(scaleAxis) {
            var _this = _super.call(this) || this;
            _this.ScaleAxis = angular.copy(scaleAxis);
            return _this;
        }
        ScaleAxesByFactor.prototype.toKVP = function () {
            var serializedAxes = [];
            this.ScaleAxis.forEach(function (axis) {
                serializedAxes.push(axis.toKVP());
            });
            return "&SCALEAXES=" + serializedAxes.join(",");
        };
        return ScaleAxesByFactor;
    }(wcs.Scaling));
    wcs.ScaleAxesByFactor = ScaleAxesByFactor;
})(wcs || (wcs = {}));
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
    }());
    wcs.TargetAxisSize = TargetAxisSize;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var ScaleToSize = (function (_super) {
        __extends(ScaleToSize, _super);
        function ScaleToSize(targetAxisSize) {
            var _this = _super.call(this) || this;
            _this.TargetAxisSize = angular.copy(targetAxisSize);
            return _this;
        }
        ScaleToSize.prototype.toKVP = function () {
            var targetAxesSize = [];
            this.TargetAxisSize.forEach(function (target) {
                targetAxesSize.push(target.toKVP());
            });
            return "&SCALESIZE=" + targetAxesSize.join(",");
        };
        return ScaleToSize;
    }(wcs.Scaling));
    wcs.ScaleToSize = ScaleToSize;
})(wcs || (wcs = {}));
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
    }());
    wcs.TargetAxisExtent = TargetAxisExtent;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var ScaleToExtent = (function (_super) {
        __extends(ScaleToExtent, _super);
        function ScaleToExtent(targetAxisExtent) {
            var _this = _super.call(this) || this;
            _this.TargetAxisExtent = angular.copy(targetAxisExtent);
            return _this;
        }
        ScaleToExtent.prototype.toKVP = function () {
            var serializedAxes = [];
            this.TargetAxisExtent.forEach(function (target) {
                serializedAxes.push(target.toKVP());
            });
            return "&SCALEEXTENT=" + serializedAxes.join(",");
        };
        return ScaleToExtent;
    }(wcs.Scaling));
    wcs.ScaleToExtent = ScaleToExtent;
})(wcs || (wcs = {}));
var rasdaman;
(function (rasdaman) {
    var SettingsService = (function () {
        function SettingsService($window) {
            this.WCSEndpoint = $window.location.origin + "/rasdaman/ows";
            this.WCSServiceNameVersion = "SERVICE=WCS&VERSION=2.0.1";
        }
        SettingsService.$inject = ["$window"];
        return SettingsService;
    }());
    rasdaman.SettingsService = SettingsService;
})(rasdaman || (rasdaman = {}));
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
        WCSService.prototype.getCoveragesExtents = function () {
            var result = this.$q.defer();
            var requestUrl = this.settings.WCSEndpoint + "/GetCoveragesExtents";
            this.$http.get(requestUrl)
                .then(function (data) {
                result.resolve(data);
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
            result.resolve(requestUrl);
            return result.promise;
        };
        WCSService.prototype.deleteCoverage = function (coverageId) {
            var result = this.$q.defer();
            if (!coverageId) {
                result.reject("You must specify at least one coverage ID.");
            }
            var requestUrl = this.settings.WCSEndpoint + "?" + this.settings.WCSServiceNameVersion + "&REQUEST=DeleteCoverage&COVERAGEID=" + coverageId;
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
            var requestUrl = this.settings.WCSEndpoint + "?" + this.settings.WCSServiceNameVersion + "&REQUEST=InsertCoverage&coverageRef=" + encodeURI(coverageUrl);
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
        WCSService.prototype.processCoverages = function (query) {
            var result = this.$q.defer();
            var queryStr = query.toKVP();
            var requestUrl = this.settings.WCSEndpoint + "?" + this.settings.WCSServiceNameVersion + queryStr;
            var request = {
                method: 'GET',
                url: requestUrl,
                transformResponse: null
            };
            if (queryStr.indexOf("png") >= 0 || queryStr.indexOf("jpeg") >= 0 || queryStr.indexOf("jpeg2000") >= 0 || queryStr.indexOf("tiff") >= 0 || queryStr.indexOf("netcdf") >= 0) {
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
    }());
    rasdaman.WCSService = WCSService;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WCSErrorHandlingService = (function () {
        function WCSErrorHandlingService(notificationService, serializedObjectFactory, $log) {
            this.notificationService = notificationService;
            this.serializedObjectFactory = serializedObjectFactory;
            this.$log = $log;
        }
        WCSErrorHandlingService.prototype.handleError = function () {
            var args = [];
            for (var _i = 0; _i < arguments.length; _i++) {
                args[_i] = arguments[_i];
            }
            if (args.length == 1) {
                var errorInformation = args[0][0];
                if (errorInformation.data === "" && errorInformation.status == 404) {
                    this.notificationService.error("Cannot connect to petascope, please check if petascope is running.");
                }
                else {
                    this.notificationService.error("The request failed with HTTP code:" + errorInformation.status + "(" + errorInformation.statusText + ")");
                }
                if (errorInformation.data != "") {
                    try {
                        var responseDocument = new rasdaman.common.ResponseDocument(errorInformation.data, rasdaman.common.ResponseDocumentType.XML);
                        var serializedResponse = this.serializedObjectFactory.getSerializedObject(responseDocument);
                        var exceptionReport = new ows.ExceptionReport(serializedResponse);
                        this.notificationService.error(exceptionReport.Exception.ExceptionText + "</br> Exception code: " + exceptionReport.Exception.ExceptionCode);
                    }
                    catch (err) {
                        this.$log.error(err);
                    }
                }
            }
        };
        WCSErrorHandlingService.$inject = ["Notification", "rasdaman.common.SerializedObjectFactory", "$log"];
        return WCSErrorHandlingService;
    }());
    rasdaman.WCSErrorHandlingService = WCSErrorHandlingService;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WebWorldWindService = (function () {
        function WebWorldWindService() {
            this.webWorldWindModels = [];
            this.coveragesExtents = null;
        }
        WebWorldWindService.prototype.setCoveragesExtents = function (coveragesExtents) {
            this.coveragesExtents = coveragesExtents;
        };
        WebWorldWindService.prototype.getCoveragesExtents = function () {
            return this.coveragesExtents;
        };
        WebWorldWindService.prototype.getCoveragesExtentsByCoverageId = function (coverageId) {
            var result = [];
            for (var i = 0; i < this.coveragesExtents.length; i++) {
                if (this.coveragesExtents[i].coverageId === coverageId) {
                    result.push(this.coveragesExtents[i]);
                    return result;
                }
            }
            return null;
        };
        WebWorldWindService.prototype.initWebWorldWind = function (canvasId) {
            var wwd = new WorldWind.WorldWindow(canvasId);
            var polygonLayer = new WorldWind.RenderableLayer();
            var layers = [
                { layer: new WorldWind.BMNGLayer(), enabled: true },
                { layer: new WorldWind.BMNGLandsatLayer(), enabled: false },
                { layer: new WorldWind.BingAerialLayer(null), enabled: false },
                { layer: new WorldWind.BingAerialWithLabelsLayer(null), enabled: true },
                { layer: new WorldWind.BingRoadsLayer(null), enabled: false },
                { layer: new WorldWind.CompassLayer(), enabled: true },
                { layer: new WorldWind.CoordinatesDisplayLayer(wwd), enabled: true },
                { layer: new WorldWind.ViewControlsLayer(wwd), enabled: true }
            ];
            for (var i = 0; i < layers.length; i++) {
                layers[i].layer.enabled = layers[i].enabled;
                wwd.addLayer(layers[i].layer);
            }
            var textLayer = new WorldWind.RenderableLayer("Screen Text");
            wwd.addLayer(textLayer);
            var handlePick = function (o) {
                textLayer.removeAllRenderables();
                var pickPoint = wwd.canvasCoordinates(o.clientX, o.clientY);
                var pickList = wwd.pick(pickPoint);
                if (pickList.objects.length > 0) {
                    for (var p = 0; p < pickList.objects.length; p++) {
                        var pickedObject = pickList.objects[p];
                        if (!pickedObject.isTerrain) {
                            if (pickedObject.userObject instanceof WorldWind.SurfacePolygon) {
                                var screenText = new WorldWind.ScreenText(new WorldWind.Offset(WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 0.5), pickedObject.userObject.userProperties);
                                var textAttributes = new WorldWind.TextAttributes(null);
                                textAttributes.color = WorldWind.Color.YELLOW;
                                screenText.attributes = textAttributes;
                                textLayer.addRenderable(screenText);
                                break;
                            }
                        }
                    }
                }
            };
            wwd.addEventListener("mousemove", handlePick);
            var highlightController = new WorldWind.HighlightController(wwd);
            var webWorldWindModel = {
                canvasId: canvasId,
                wwd: wwd,
                polygonLayer: polygonLayer
            };
            this.webWorldWindModels.push(webWorldWindModel);
            return webWorldWindModel;
        };
        WebWorldWindService.prototype.loadCoveragesExtentsOnGlobe = function (canvasId, coverageExtents) {
            var exist = false;
            var webWorldWindModel = null;
            for (var i = 0; i < this.webWorldWindModels.length; i++) {
                if (this.webWorldWindModels[i].canvasId === canvasId) {
                    exist = true;
                    webWorldWindModel = this.webWorldWindModels[i];
                    break;
                }
            }
            if (!exist) {
                webWorldWindModel = this.initWebWorldWind(canvasId);
            }
            var wwd = webWorldWindModel.wwd;
            var polygonLayer = webWorldWindModel.polygonLayer;
            wwd.removeLayer(polygonLayer);
            polygonLayer = new WorldWind.RenderableLayer();
            webWorldWindModel.polygonLayer = polygonLayer;
            wwd.addLayer(polygonLayer);
            var polygonAttributes = new WorldWind.ShapeAttributes(null);
            polygonAttributes.drawInterior = true;
            polygonAttributes.drawOutline = true;
            polygonAttributes.outlineColor = WorldWind.Color.BLUE;
            polygonAttributes.interiorColor = new WorldWind.Color(0, 1, 1, 0.2);
            polygonAttributes.applyLighting = true;
            var highlightAttributes = new WorldWind.ShapeAttributes(polygonAttributes);
            highlightAttributes.outlineColor = WorldWind.Color.RED;
            highlightAttributes.interiorColor = new WorldWind.Color(1, 1, 1, 0.2);
            var xcenter = 0, ycenter = 0;
            for (var i = 0; i < coverageExtents.length; i++) {
                var coverageExtent = coverageExtents[i];
                var coverageId = coverageExtent.coverageId;
                var bbox = coverageExtent.bbox;
                var xmin = bbox.xmin.toFixed(5);
                if (xmin < -180) {
                    xmin = -180;
                }
                var ymin = bbox.ymin.toFixed(5);
                if (ymin < -90) {
                    ymin = 90;
                }
                var xmax = bbox.xmax.toFixed(5);
                if (xmax > 180) {
                    xmax = 180;
                }
                var ymax = bbox.ymax.toFixed(5);
                if (ymax > 90) {
                    ymax = 90;
                }
                var boundaries = [];
                boundaries[0] = [];
                boundaries[0].push(new WorldWind.Location(ymin, xmin));
                boundaries[0].push(new WorldWind.Location(ymin, xmax));
                boundaries[0].push(new WorldWind.Location(ymax, xmax));
                boundaries[0].push(new WorldWind.Location(ymax, xmin));
                var polygon = new WorldWind.SurfacePolygon(boundaries, polygonAttributes);
                polygon.highlightAttributes = highlightAttributes;
                var userProperties = "Coverage Id: " + coverageId + "\n" + "Coverage Extent: lat_min=" + ymin + ", lon_min=" + xmin + ", lat_max=" + ymax + ", lon_max=" + xmax;
                polygon.userProperties = userProperties;
                polygonLayer.addRenderable(polygon);
            }
        };
        WebWorldWindService.prototype.gotoCoverageExtentCenter = function (canvasId, coverageExtents) {
            var webWorldWindModel = null;
            for (var i = 0; i < this.webWorldWindModels.length; i++) {
                if (this.webWorldWindModels[i].canvasId === canvasId) {
                    webWorldWindModel = this.webWorldWindModels[i];
                    break;
                }
            }
            var coverageExtent = coverageExtents[0];
            var xcenter = (coverageExtent.bbox.xmin + coverageExtent.bbox.xmax) / 2;
            var ycenter = (coverageExtent.bbox.ymin + coverageExtent.bbox.ymax) / 2;
            var wwd = webWorldWindModel.wwd;
            wwd.navigator.lookAtLocation = new WorldWind.Location(ycenter, xcenter);
            wwd.redraw();
        };
        WebWorldWindService.$inject = [];
        return WebWorldWindService;
    }());
    rasdaman.WebWorldWindService = WebWorldWindService;
})(rasdaman || (rasdaman = {}));
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
    }());
    rasdaman.SettingsController = SettingsController;
})(rasdaman || (rasdaman = {}));
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
                    $scope.GetCoverageTab.Disabled = false;
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
                GetCoveragesExtents: null,
                SelectedCoverageDescriptions: null,
                SelectedGetCoverageId: null
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
    }());
    rasdaman.MainController = MainController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var GetCapabilitiesController = (function () {
        function GetCapabilitiesController($scope, $log, wcsService, settings, alertService, errorHandlingService, webWorldWindService) {
            this.$scope = $scope;
            this.$log = $log;
            this.wcsService = wcsService;
            this.settings = settings;
            this.alertService = alertService;
            this.errorHandlingService = errorHandlingService;
            this.webWorldWindService = webWorldWindService;
            $scope.IsAvailableCoveragesOpen = false;
            $scope.IsCoveragesExtentsOpen = false;
            $scope.IsServiceIdentificationOpen = false;
            $scope.IsServiceProviderOpen = false;
            $scope.IsCapabilitiesDocumentOpen = false;
            $scope.rowPerPageSmartTable = 10;
            $scope.WcsServerEndpoint = settings.WCSEndpoint;
            var canvasId = "canvasGetCapabilities";
            $scope.pageChanged = function (newPage) {
                var selectedPage = newPage - 1;
                var startIndex = $scope.rowPerPageSmartTable * selectedPage;
                var endIndex = $scope.rowPerPageSmartTable * selectedPage + $scope.rowPerPageSmartTable;
                var coveragesExtentsCurrentPage = $scope.selectCoveragesExtentsCurrentPage(startIndex, endIndex);
                webWorldWindService.loadCoveragesExtentsOnGlobe(canvasId, coveragesExtentsCurrentPage);
            };
            $scope.selectCoveragesExtentsCurrentPage = function (startIndex, endIndex) {
                var coveragesCurrentPage = $scope.Capabilities.Contents.CoverageSummary.slice(startIndex, endIndex);
                var coveragesExtentsCurrentPage = [];
                for (var i = 0; i < coveragesCurrentPage.length; i++) {
                    for (var j = 0; j < $scope.coveragesExtents.length; j++) {
                        if ($scope.coveragesExtents[j].coverageId === coveragesCurrentPage[i].CoverageId) {
                            var coverageExtent = $scope.coveragesExtents[j];
                            coverageExtent.index = j;
                            coveragesExtentsCurrentPage.push(coverageExtent);
                            break;
                        }
                    }
                }
                coveragesExtentsCurrentPage.sort(function (a, b) {
                    return parseFloat(a.index) - parseFloat(b.index);
                });
                return coveragesExtentsCurrentPage;
            };
            $scope.getServerCapabilities = function () {
                var args = [];
                for (var _i = 0; _i < arguments.length; _i++) {
                    args[_i] = arguments[_i];
                }
                if (!$scope.WcsServerEndpoint) {
                    alertService.error("The entered WCS endpoint is invalid.");
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
                    wcsService.getCoveragesExtents()
                        .then(function (response) {
                        $scope.coveragesExtents = response.data;
                        webWorldWindService.setCoveragesExtents($scope.coveragesExtents);
                        $scope.IsCoveragesExtentsOpen = true;
                        var coveragesExtentsFirstPage = $scope.selectCoveragesExtentsCurrentPage(0, $scope.rowPerPageSmartTable);
                        webWorldWindService.loadCoveragesExtentsOnGlobe(canvasId, coveragesExtentsFirstPage);
                    }, function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i] = arguments[_i];
                        }
                        $scope.coveragesExtents = null;
                        $scope.IsCoveragesExtentsOpen = false;
                        errorHandlingService.handleError(args);
                        $log.error(args);
                    })["finally"](function () {
                        $scope.StateInformation.GetCoveragesExtents = $scope.coveragesExtents;
                    });
                }, function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    $scope.CapabilitiesDocument = null;
                    $scope.Capabilities = null;
                    $scope.IsAvailableCoveragesOpen = false;
                    $scope.IsServiceIdentificationOpen = false;
                    $scope.IsServiceProviderOpen = false;
                    errorHandlingService.handleError(args);
                    $log.error(args);
                })["finally"](function () {
                    $scope.StateInformation.ServerCapabilities = $scope.Capabilities;
                });
            };
            $scope.getServerCapabilities();
        }
        GetCapabilitiesController.$inject = [
            "$scope",
            "$log",
            "rasdaman.WCSService",
            "rasdaman.SettingsService",
            "Notification",
            "rasdaman.WCSErrorHandlingService",
            "rasdaman.WebWorldWindService"
        ];
        return GetCapabilitiesController;
    }());
    rasdaman.GetCapabilitiesController = GetCapabilitiesController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var DescribeCoverageController = (function () {
        function DescribeCoverageController($scope, $rootScope, $log, wcsService, alertService, wcsErrorHandlingService, webWorldWindService) {
            $scope.SelectedCoverageId = null;
            $scope.IsCoverageDescriptionsDocumentOpen = false;
            $scope.IsCoverageDescriptionsHideGlobe = true;
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
            $scope.$watch("StateInformation.SelectedGetCoverageId", function (getCoverageId) {
                if (getCoverageId) {
                    $scope.SelectedCoverageId = getCoverageId;
                    $scope.describeCoverage();
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
                    var coveragesExtents = webWorldWindService.getCoveragesExtentsByCoverageId($scope.SelectedCoverageId);
                    if (coveragesExtents == null) {
                        $scope.IsCoverageDescriptionsHideGlobe = true;
                    }
                    else {
                        var canvasId = "canvasDescribeCoverage";
                        $scope.IsCoverageDescriptionsHideGlobe = false;
                        webWorldWindService.loadCoveragesExtentsOnGlobe(canvasId, coveragesExtents);
                        webWorldWindService.gotoCoverageExtentCenter(canvasId, coveragesExtents);
                    }
                }, function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    $scope.CoverageDescriptionsDocument = null;
                    $scope.CoverageDescriptions = null;
                    wcsErrorHandlingService.handleError(args);
                    $log.error(args);
                })["finally"](function () {
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
            "Notification",
            "rasdaman.WCSErrorHandlingService",
            "rasdaman.WebWorldWindService"
        ];
        return DescribeCoverageController;
    }());
    rasdaman.DescribeCoverageController = DescribeCoverageController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var DeleteCoverageController = (function () {
        function DeleteCoverageController($scope, $log, alertService, wcsService, errorHandlingService) {
            var _this = this;
            this.$scope = $scope;
            this.$log = $log;
            this.alertService = alertService;
            this.wcsService = wcsService;
            this.errorHandlingService = errorHandlingService;
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
                            args[_i] = arguments[_i];
                        }
                        _this.alertService.success("Successfully deleted coverage with ID <b>" + $scope.IdOfCoverageToDelete + "<b/>");
                        _this.$log.log(args);
                    }, function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i] = arguments[_i];
                        }
                        _this.errorHandlingService.handleError(args);
                        _this.$log.error(args);
                    })["finally"](function () {
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
            "rasdaman.WCSService",
            "rasdaman.WCSErrorHandlingService"
        ];
        return DeleteCoverageController;
    }());
    rasdaman.DeleteCoverageController = DeleteCoverageController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var InsertCoverageController = (function () {
        function InsertCoverageController($scope, $log, alertService, wcsService, errorHandlingService) {
            var _this = this;
            this.$scope = $scope;
            this.$log = $log;
            this.alertService = alertService;
            this.wcsService = wcsService;
            this.errorHandlingService = errorHandlingService;
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
                            args[_i] = arguments[_i];
                        }
                        _this.alertService.success("Successfully inserted coverage.");
                        _this.$log.info(args);
                    }, function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i] = arguments[_i];
                        }
                        _this.errorHandlingService.handleError(args);
                        _this.$log.error(args);
                    })["finally"](function () {
                        $scope.RequestInProgress = false;
                    });
                }
            };
        }
        InsertCoverageController.$inject = [
            "$scope",
            "$log",
            "Notification",
            "rasdaman.WCSService",
            "rasdaman.WCSErrorHandlingService"
        ];
        return InsertCoverageController;
    }());
    rasdaman.InsertCoverageController = InsertCoverageController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var GetCoverageController = (function () {
        function GetCoverageController($scope, $rootScope, $log, wcsService, alertService, webWorldWindService) {
            $scope.SelectedCoverageId = null;
            $scope.IsGlobeOpen = false;
            $scope.IsGetCoverageHideGlobe = true;
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
            $scope.$watch("StateInformation.ServerCapabilities", function (capabilities) {
                if (capabilities) {
                    $scope.AvailableCoverageIds = [];
                    capabilities.Contents.CoverageSummary.forEach(function (coverageSummary) {
                        $scope.AvailableCoverageIds.push(coverageSummary.CoverageId);
                    });
                }
            });
            $scope.loadCoverageExtentOnGlobe = function () {
                var coveragesExtents = webWorldWindService.getCoveragesExtentsByCoverageId($scope.SelectedCoverageId);
                if (coveragesExtents == null) {
                    $scope.IsGetCoverageHideGlobe = true;
                }
                else {
                    var canvasId = "canvasGetCoverage";
                    $scope.IsGetCoverageHideGlobe = false;
                    webWorldWindService.loadCoveragesExtentsOnGlobe(canvasId, coveragesExtents);
                    webWorldWindService.gotoCoverageExtentCenter(canvasId, coveragesExtents);
                }
            };
            $scope.getCoverageClickEvent = function () {
                if (!$scope.isCoverageIdValid()) {
                    alertService.error("The entered coverage ID is invalid.");
                    return;
                }
                $scope.StateInformation.SelectedGetCoverageId = $scope.SelectedCoverageId;
                $scope.loadCoverageExtentOnGlobe();
            };
            $scope.$watch("StateInformation.SelectedCoverageDescriptions", function (coverageDescriptions) {
                if (coverageDescriptions && coverageDescriptions.CoverageDescription) {
                    $scope.CoverageDescription = $scope.StateInformation.SelectedCoverageDescriptions.CoverageDescription[0];
                    $scope.SelectedCoverageId = $scope.CoverageDescription.CoverageId;
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
                        SelectedCoverageFormat: $scope.StateInformation.ServerCapabilities.ServiceMetadata.FormatSupported[0],
                        RequestUrl: null
                    };
                    var numberOfAxis = $scope.CoverageDescription.BoundedBy.Envelope.LowerCorner.Values.length;
                    for (var i = 0; i < numberOfAxis; ++i) {
                        var dimension = $scope.CoverageDescription.BoundedBy.Envelope.AxisLabels[i];
                        var min = $scope.CoverageDescription.BoundedBy.Envelope.LowerCorner.Values[i];
                        var max = $scope.CoverageDescription.BoundedBy.Envelope.UpperCorner.Values[i];
                        $scope.Core.Slices.push(new wcs.DimensionSlice(dimension, min + ""));
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
                                if ($scope.Core.Trims[i].TrimLow != min.toString()
                                    || $scope.Core.Trims[i].TrimHigh != max.toString()) {
                                    dimensionSubset.push($scope.Core.Trims[i]);
                                }
                            }
                            else {
                                dimensionSubset.push($scope.Core.Slices[i]);
                            }
                        }
                        var getCoverageRequest = new wcs.GetCoverage($scope.CoverageDescription.CoverageId, dimensionSubset, $scope.Core.SelectedCoverageFormat, $scope.Core.IsMultiPartFormat);
                        getCoverageRequest.RangeSubset = $scope.RangeSubsettingExtension.RangeSubset;
                        getCoverageRequest.Scaling = $scope.ScalingExtension.getScaling();
                        getCoverageRequest.Interpolation = $scope.InterpolationExtension.getInterpolation();
                        wcsService.getCoverage(getCoverageRequest)
                            .then(function (requestUrl) {
                            $scope.Core.RequestUrl = requestUrl;
                        }, function () {
                            var args = [];
                            for (var _i = 0; _i < arguments.length; _i++) {
                                args[_i] = arguments[_i];
                            }
                            $scope.Core.RequestUrl = null;
                            alertService.error("Failed to execute GetCoverage operation.");
                            $log.error(args);
                        });
                    };
                    $scope.loadCoverageExtentOnGlobe();
                }
            });
        }
        GetCoverageController.isRangeSubsettingSupported = function (serverCapabilities) {
            return serverCapabilities.ServiceIdentification.Profile.indexOf(rasdaman.Constants.RANGE_SUBSETTING_EXT_URI) != -1;
        };
        GetCoverageController.isScalingSupported = function (serverCapabilities) {
            return serverCapabilities.ServiceIdentification.Profile.indexOf(rasdaman.Constants.SCALING_EXT_URI) != -1;
        };
        GetCoverageController.isInterpolationSupported = function (serverCapabilities) {
            return serverCapabilities.ServiceIdentification.Profile.indexOf(rasdaman.Constants.INTERPOLATION_EXT_URI) != -1;
        };
        GetCoverageController.$inject = [
            "$scope",
            "$rootScope",
            "$log",
            "rasdaman.WCSService",
            "Notification",
            "rasdaman.WebWorldWindService"
        ];
        return GetCoverageController;
    }());
    rasdaman.GetCoverageController = GetCoverageController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WidgetConfiguration = (function () {
        function WidgetConfiguration(type, parameters) {
            this.Type = type;
            this.Parameters = parameters;
        }
        return WidgetConfiguration;
    }());
    rasdaman.WidgetConfiguration = WidgetConfiguration;
})(rasdaman || (rasdaman = {}));
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
    }());
    rasdaman.WCPSCommand = WCPSCommand;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WCPSQueryResult = (function () {
        function WCPSQueryResult(command) {
            rasdaman.common.ArgumentValidator.isNotNull(command, "command");
            this.Command = command;
        }
        return WCPSQueryResult;
    }());
    rasdaman.WCPSQueryResult = WCPSQueryResult;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var RawWCPSResult = (function (_super) {
        __extends(RawWCPSResult, _super);
        function RawWCPSResult(command, data) {
            var _this = _super.call(this, command) || this;
            _this.Data = data.toString();
            return _this;
        }
        return RawWCPSResult;
    }(rasdaman.WCPSQueryResult));
    rasdaman.RawWCPSResult = RawWCPSResult;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var ImageWCPSResult = (function (_super) {
        __extends(ImageWCPSResult, _super);
        function ImageWCPSResult(command, rawImageData) {
            var _this = _super.call(this, command) || this;
            _this.Base64ImageData = rasdaman.common.ImageUtilities.arrayBufferToBase64(rawImageData);
            _this.ImageType = (command.Query.search(/jpeg/g) === -1 ? "image/png" : "image/jpeg");
            return _this;
        }
        return ImageWCPSResult;
    }(rasdaman.WCPSQueryResult));
    rasdaman.ImageWCPSResult = ImageWCPSResult;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var DiagramWCPSResult = (function (_super) {
        __extends(DiagramWCPSResult, _super);
        function DiagramWCPSResult(command, data) {
            var _this = _super.call(this, command) || this;
            var diagramType = "lineChart";
            if (command.WidgetConfiguration.Parameters && command.WidgetConfiguration.Parameters.type) {
                diagramType = command.WidgetConfiguration.Parameters.type;
            }
            _this.DiagramOptions = {
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
            _this.DiagramData = [
                {
                    values: processedValues
                }
            ];
            return _this;
        }
        return DiagramWCPSResult;
    }(rasdaman.WCPSQueryResult));
    rasdaman.DiagramWCPSResult = DiagramWCPSResult;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var NotificationWCPSResult = (function (_super) {
        __extends(NotificationWCPSResult, _super);
        function NotificationWCPSResult(command, data) {
            var _this = _super.call(this, command) || this;
            _this.Data = data.toString();
            return _this;
        }
        return NotificationWCPSResult;
    }(rasdaman.WCPSQueryResult));
    rasdaman.NotificationWCPSResult = NotificationWCPSResult;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WCPSResultFactory = (function () {
        function WCPSResultFactory() {
        }
        WCPSResultFactory.getResult = function (errorHandlingService, command, data, mimeType, fileName) {
            if (command.WidgetConfiguration == null) {
                if (mimeType == "application/json" || mimeType == "text/plain" || mimeType == "application/gml+xml") {
                    return new rasdaman.RawWCPSResult(command, data);
                }
                else {
                    var blob = new Blob([data], { type: "application/octet-stream" });
                    saveAs(blob, fileName);
                    return null;
                }
            }
            else if (command.WidgetConfiguration.Type == "diagram") {
                this.validateResult(errorHandlingService, command.WidgetConfiguration.Type, mimeType);
                return new rasdaman.DiagramWCPSResult(command, data);
            }
            else if (command.WidgetConfiguration.Type == "image") {
                this.validateResult(errorHandlingService, command.WidgetConfiguration.Type, mimeType);
                return new rasdaman.ImageWCPSResult(command, data);
            }
            else {
                errorHandlingService.notificationService.error("The input widget: " + command.WidgetConfiguration.Type + " does not exist");
            }
        };
        WCPSResultFactory.validateResult = function (errorHandlingService, widgetType, mimeType) {
            if (widgetType == "diagram" && !(mimeType == "application/json" || mimeType == "text/plain" || mimeType == "text/csv")) {
                errorHandlingService.notificationService.error("Diagram widget can only be used with encoding 1D result in json or csv.");
            }
            else if (widgetType == "image" && !(mimeType == "image/png" || mimeType == "image/jpeg")) {
                errorHandlingService.notificationService.error("Image widget can only be used with encoding 2D result in png or jpeg.");
            }
        };
        return WCPSResultFactory;
    }());
    rasdaman.WCPSResultFactory = WCPSResultFactory;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var ProcessCoverageController = (function () {
        function ProcessCoverageController($scope, $log, $interval, notificationService, wcsService, errorHandlingService) {
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
                    $scope.EditorData[indexOfResults].Query = $scope.Query;
                    $scope.EditorData[indexOfResults].finished = false;
                    var waitingForResultsPromise = $interval(function () {
                        $scope.EditorData[indexOfResults].SecondsPassed++;
                    }, 1000);
                    wcsService.processCoverages(processCoverages)
                        .then(function (data) {
                        var editorRow = rasdaman.WCPSResultFactory.getResult(errorHandlingService, command, data.data, data.headers('Content-Type'), data.headers('File-name'));
                        if (editorRow != null) {
                            $scope.EditorData.push(editorRow);
                        }
                        else {
                            $scope.EditorData.push(new rasdaman.NotificationWCPSResult(command, "Downloading WCPS query's result as a file to Web Browser."));
                        }
                    }, function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i] = arguments[_i];
                        }
                        if (args[0].data instanceof ArrayBuffer) {
                            var decoder = new TextDecoder("utf-8");
                            args[0].data = decoder.decode(new Uint8Array(args[0].data));
                        }
                        errorHandlingService.handleError(args);
                        $log.error(args);
                        $scope.EditorData.push(new rasdaman.NotificationWCPSResult(command, "Cannot execute the requested WCPS query, error '" + args[0].data + "'."));
                    })["finally"](function () {
                        $scope.EditorData[indexOfResults].finished = true;
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
                else if (datum instanceof rasdaman.RawWCPSResult) {
                    return 1;
                }
                else if (datum instanceof rasdaman.ImageWCPSResult) {
                    return 2;
                }
                else if (datum instanceof rasdaman.DiagramWCPSResult) {
                    return 3;
                }
                else if (datum instanceof rasdaman.NotificationWCPSResult) {
                    return 4;
                }
                return -1;
            };
        }
        ProcessCoverageController.createExampleQueries = function () {
            return [
                {
                    Title: '-- Select a WCPS query --',
                    Query: ''
                }, {
                    Title: 'No encoding',
                    Query: 'for c in (test_mean_summer_airtemp) return avg(c)'
                }, {
                    Title: 'Encode 2D as png with widget',
                    Query: 'image>>for c in (test_mean_summer_airtemp) return encode(c, "png")'
                }, {
                    Title: 'Encode 2D as tiff',
                    Query: 'for c in (test_mean_summer_airtemp) return encode(c, "tiff")'
                }, {
                    Title: 'Encode 2D as netCDF',
                    Query: 'for c in (test_mean_summer_airtemp) return encode(c, "netcdf")'
                }, {
                    Title: 'Encode 1D as csv with widget',
                    Query: 'diagram>>for c in (test_mean_summer_airtemp) return encode(c[Lat(-20)], "csv")'
                }, {
                    Title: 'Encode 1D as json with widget',
                    Query: 'diagram>>for c in (test_mean_summer_airtemp) return encode(c[Lat(-20)], "json")'
                }, {
                    Title: 'Encode 1D as gml',
                    Query: 'for c in (test_mean_summer_airtemp) return encode(c, "gml")'
                }
            ];
        };
        ProcessCoverageController.$inject = [
            "$scope",
            "$log",
            "$interval",
            "Notification",
            "rasdaman.WCSService",
            "rasdaman.WCSErrorHandlingService"
        ];
        return ProcessCoverageController;
    }());
    rasdaman.ProcessCoverageController = ProcessCoverageController;
    var WaitingForResult = (function () {
        function WaitingForResult() {
            this.SecondsPassed = 0;
        }
        return WaitingForResult;
    }());
})(rasdaman || (rasdaman = {}));
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
    }());
    rasdaman.RangeSubsettingModel = RangeSubsettingModel;
})(rasdaman || (rasdaman = {}));
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
    }());
    rasdaman.ScalingExtensionModel = ScalingExtensionModel;
})(rasdaman || (rasdaman = {}));
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
    }());
    rasdaman.InterpolationExtensionModel = InterpolationExtensionModel;
})(rasdaman || (rasdaman = {}));
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
    }());
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
        .service("rasdaman.WebWorldWindService", rasdaman.WebWorldWindService)
        .service("rasdaman.WCSErrorHandlingService", rasdaman.WCSErrorHandlingService)
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
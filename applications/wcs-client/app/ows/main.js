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
                this.value = value;
                this.type = responseType;
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
                this.document = document;
                this.value = value;
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
                    this.jsonObject = xmlToJSON.parseString(documentOrObject.value, options);
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
                if (document.type == common.ResponseDocumentType.XML) {
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
            var MAXIMUM_TEXT_LENGTH = 300000;
            return {
                restrict: 'EC',
                scope: {
                    data: "="
                },
                templateUrl: "ows/src/common/directives/pretty-print/PrettyPrintTemplate.html",
                link: function (scope, element, attrs) {
                    scope.$watch("data", function (newData, oldValue) {
                        if (newData && newData.value) {
                            if (newData.value.length > MAXIMUM_TEXT_LENGTH) {
                                newData.value = newData.value.substr(0, MAXIMUM_TEXT_LENGTH);
                                newData.value += "\n The text content is too long to display, only first " + MAXIMUM_TEXT_LENGTH + " characters are shown.";
                            }
                            var escapedHtml = prettyPrintOne(escapeXml(newData.value), newData.type, true);
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
    var common;
    (function (common) {
        function scrollToBottom($timeout, $window) {
            return {
                scope: {
                    scrollToBottom: "="
                },
                restrict: 'A',
                link: function (scope, element, attr) {
                    scope.$watchCollection('scrollToBottom', function (newVal) {
                        if (newVal) {
                            $timeout(function () {
                                element[0].scrollTop = element[0].scrollHeight;
                            }, 0);
                        }
                    });
                }
            };
        }
        common.scrollToBottom = scrollToBottom;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        function getFilteredRows($window) {
            return {
                require: '^stTable',
                link: function (scope, element, attr, ctrl) {
                    var rootScope = scope.$root;
                    scope.$watch(function (rootScope) {
                        var obj = ctrl.getFilteredCollection();
                        if (obj.length > 0) {
                            var objName = obj[0].constructor.name;
                            if (objName == "CoverageSummary") {
                                $window.wcsGetCapabilitiesFilteredRows = JSON.stringify(obj);
                            }
                            else if (objName == "Layer") {
                                $window.wmsGetCapabilitiesFilteredRows = JSON.stringify(obj);
                            }
                        }
                    });
                }
            };
        }
        common.getFilteredRows = getFilteredRows;
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
        Constants.CRS_EXT_URI = "http://www.opengis.net/spec/WCS_service-extension_crs/1.0/conf/crs";
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
            this.deliveryPoint = [];
            source.getChildrenAsSerializedObjects("ows:DeliveryPoint").forEach(function (o) {
                _this.deliveryPoint.push(o.getValueAsString());
            });
            if (source.doesElementExist("ows:City")) {
                this.city = source.getChildAsSerializedObject("ows:City").getValueAsString();
            }
            if (source.doesElementExist("ows:AdministrativeArea")) {
                this.administrativeArea = source.getChildAsSerializedObject("ows:AdministrativeArea").getValueAsString();
            }
            if (source.doesElementExist("ows:PostalCode")) {
                this.postalCode = source.getChildAsSerializedObject("ows:PostalCode").getValueAsString();
            }
            if (source.doesElementExist("ows:Country")) {
                this.country = source.getChildAsSerializedObject("ows:Country").getValueAsString();
            }
            this.electronicMailAddress = [];
            source.getChildrenAsSerializedObjects("ows:ElectronicMailAddress").forEach(function (o) {
                _this.electronicMailAddress.push(o.getValueAsString());
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
            this.value = source.getValueAsString();
            if (source.doesAttributeExist("xml:lang")) {
                this.lang = source.getAttributeAsString("xml:lang");
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
                this.actuate = source.getAttributeAsString("xlink:actuate");
            }
            if (source.doesAttributeExist("xlink:acrole")) {
                this.acrole = source.getAttributeAsString("xlink:acrole");
            }
            if (source.doesAttributeExist("xlink:href")) {
                this.href = source.getAttributeAsString("xlink:href");
            }
            if (source.doesAttributeExist("xlink:role")) {
                this.role = source.getAttributeAsString("xlink:role");
            }
            if (source.doesAttributeExist("xlink:show")) {
                this.show = source.getAttributeAsString("xlink:show");
            }
            if (source.doesAttributeExist("xlink:title")) {
                this.title = source.getAttributeAsString("xlink:title");
            }
            if (source.doesAttributeExist("xlink:type")) {
                this.type = source.getAttributeAsString("xlink:type");
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
            this.code = source.getValueAsString();
            if (source.doesAttributeExist("codeSpace")) {
                this.codeSpace = new ows.Uri(source.getAttributeAsString("codeSpace"));
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
            this.keyword = [];
            source.getChildrenAsSerializedObjects("ows:Keyword").forEach(function (s) {
                _this.keyword.push(new ows.LanguageString(s));
            });
            this.type = new ows.Code(source.getChildAsSerializedObject("ows:Type"));
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
            this.title = [];
            source.getChildrenAsSerializedObjects("ows:Title").forEach(function (s) {
                _this.title.push(new ows.LanguageString(s));
            });
            this.abstract = [];
            source.getChildrenAsSerializedObjects("ows:Abstract").forEach(function (s) {
                _this.abstract.push(new ows.LanguageString(s));
            });
            this.keywords = [];
            source.getChildrenAsSerializedObjects("ows:Keywords").forEach(function (s) {
                _this.keywords.push(new ows.Keywords(s));
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
            _this.serviceType = new ows.Code(source.getChildAsSerializedObject("ServiceType"));
            _this.serviceTypeVersion = [];
            source.getChildrenAsSerializedObjects("ows:ServiceTypeVersion").forEach(function (s) {
                _this.serviceTypeVersion.push(s.getValueAsString());
            });
            _this.profile = [];
            source.getChildrenAsSerializedObjects("ows:Profile").forEach(function (s) {
                _this.profile.push(s.getValueAsString());
            });
            if (source.doesElementExist("ows:Fees")) {
                _this.fees = source.getChildAsSerializedObject("ows:Fees").getValueAsString();
            }
            if (source.doesElementExist("ows:AccessConstraints")) {
                _this.accessConstraints = source.getChildAsSerializedObject("ows:AccessConstraints").getValueAsString();
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
            this.voice = [];
            source.getChildrenAsSerializedObjects("ows:Voice").forEach(function (s) {
                _this.voice.push(s.getValueAsString());
            });
            this.facsimile = [];
            source.getChildrenAsSerializedObjects("ows:Facsimile").forEach(function (s) {
                _this.facsimile.push(s.getValueAsString());
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
                this.phone = new ows.Phone(source.getChildAsSerializedObject("ows:Phone"));
            }
            if (source.doesElementExist("ows:Address")) {
                this.address = new ows.Address(source.getChildAsSerializedObject("ows:Address"));
            }
            if (source.doesElementExist("ows:OnlineResource")) {
                this.onlineResource = new ows.OnlineResource(source.getChildAsSerializedObject("ows:OnlineResource"));
            }
            if (source.doesElementExist("ows:HoursOfService")) {
                this.hoursOfService = source.getChildAsSerializedObject("ows:HoursOfService").getValueAsString();
            }
            if (source.doesElementExist("ows:ContactInstructions")) {
                this.contactInstructions = source.getChildAsSerializedObject("ows:ContactInstructions").getValueAsString();
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
                this.individualName = source.getChildAsSerializedObject("ows:IndividualName").getValueAsString();
            }
            if (source.doesElementExist("ows:PositionName")) {
                this.positionName = source.getChildAsSerializedObject("ows:PositionName").getValueAsString();
            }
            if (source.doesElementExist("ows:Role")) {
                this.role = new ows.Code(source.getChildAsSerializedObject("ows:Role"));
            }
            if (source.doesElementExist("ows:ContactInfo")) {
                this.contactInfo = new ows.ContactInfo(source.getChildAsSerializedObject("ows:ContactInfo"));
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
                this.providerName = source.getChildAsSerializedObject("ows:ProviderName").getValueAsString();
            }
            if (source.doesElementExist("ows:ProviderSite")) {
                this.providerSite = new ows.OnlineResource(source.getChildAsSerializedObject("ows:ProviderSite"));
            }
            if (source.doesElementExist("ows:ServiceContact")) {
                this.serviceContact = new ows.ServiceContact(source.getChildAsSerializedObject("ows:ServiceContact"));
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
            _this.constraint = [];
            source.getChildrenAsSerializedObjects("ows:Constraint").forEach(function (o) {
                _this.constraint.push(new ows.Constraint(o));
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
            this.get = new ows.Get(source.getChildAsSerializedObject("ows:Get"));
            this.post = new ows.Post(source.getChildAsSerializedObject("ows:Post"));
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
            this.http = new ows.HTTP(source.getChildAsSerializedObject("ows:HTTP"));
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
            this.name = source.getAttributeAsString("name");
            this.dcp = [];
            source.getChildrenAsSerializedObjects("ows:DCP").forEach(function (o) {
                _this.dcp.push(new ows.DCP(o));
            });
            this.parameter = [];
            source.getChildrenAsSerializedObjects("ows:Parameter").forEach(function (o) {
                _this.parameter.push(new ows.Parameter(o));
            });
            this.constraint = [];
            source.getChildrenAsSerializedObjects("ows:Constraint").forEach(function (o) {
                _this.constraint.push(new ows.Constraint(o));
            });
            this.metadata = [];
            source.getChildrenAsSerializedObjects("ows:Metadata").forEach(function (o) {
                _this.metadata.push(new ows.Metadata(o));
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
            this.operation = [];
            source.getChildrenAsSerializedObjects("ows:Operation").forEach(function (o) {
                _this.operation.push(new ows.Operation(o));
            });
            this.parameter = [];
            source.getChildrenAsSerializedObjects("ows:Parameter").forEach(function (o) {
                _this.parameter.push(new ows.Parameter(o));
            });
            this.constraint = [];
            source.getChildrenAsSerializedObjects("ows:Constraint").forEach(function (o) {
                _this.constraint.push(new ows.Constraint(o));
            });
            if (source.doesElementExist("ows:ExtendedCapabilities")) {
                this.extendedCapabilities = new ows.ExtendedCapabilities(source.getChildAsSerializedObject("ows:ExtendedCapabilities"));
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
            this.version = source.getAttributeAsString("version");
            if (source.doesAttributeExist("updateSequence")) {
                this.updateSequence = source.getAttributeAsString("updateSequence");
            }
            if (source.doesElementExist("ows:ServiceIdentification")) {
                this.serviceIdentification = new ows.ServiceIdentification(source.getChildAsSerializedObject("ows:ServiceIdentification"));
            }
            if (source.doesElementExist("ows:ServiceProvider")) {
                this.serviceProvider = new ows.ServiceProvider(source.getChildAsSerializedObject("ows:ServiceProvider"));
            }
            if (source.doesElementExist("ows:OperationsMetadata")) {
                this.operationsMetadata = new ows.OperationsMetadata(source.getChildAsSerializedObject("ows:OperationsMetadata"));
            }
            if (source.doesElementExist("Languages")) {
                this.languages = new ows.Languages(source.getChildAsSerializedObject("Languages"));
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
            this.request = "GetCapabilities";
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
            if (!source.doesElementExist("ExceptionText")) {
                this.exceptionText = source.getValueAsString();
                this.exceptionCode = null;
            }
            else {
                this.exceptionText = source.getChildAsSerializedObject("ExceptionText").getValueAsString();
                if (source.doesAttributeExist("exceptionCode")) {
                    this.exceptionCode = source.getAttributeAsString("exceptionCode");
                }
                if (source.doesAttributeExist("locator")) {
                    this.locator = source.getAttributeAsString("locator");
                }
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
            if (source.doesElementExist("Exception")) {
                this.exception = new ows.Exception(source.getChildAsSerializedObject("Exception"));
            }
            else if (source.doesElementExist("ServiceException")) {
                this.exception = new ows.Exception(source.getChildAsSerializedObject("ServiceException"));
            }
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
            this.lowerCorner = source.getChildAsSerializedObject("ows:LowerCorner").getValueAsString();
            this.upperCorner = source.getChildAsSerializedObject("ows:UpperCorner").getValueAsString();
        }
        return WGS84BoundingBox;
    }());
    ows.WGS84BoundingBox = WGS84BoundingBox;
})(ows || (ows = {}));
var ows;
(function (ows) {
    var CustomizedMetadata = (function () {
        function CustomizedMetadata(source) {
            this.localCoverageSizeInBytes = 0;
            this.remoteCoverageSizeInBytes = 0;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.parseCoverageLocation(source);
            this.parseCoverageSizeInBytes(source);
            this.parseBlackListed(source);
        }
        CustomizedMetadata.prototype.parseBlackListed = function (source) {
            this.isBlackedList = JSON.parse(this.parseAdditionalElementValueByName(source, "blackListed"));
        };
        CustomizedMetadata.prototype.parseCoverageLocation = function (source) {
            this.hostname = this.parseAdditionalElementValueByName(source, "hostname");
            this.petascopeEndPoint = this.parseAdditionalElementValueByName(source, "endpoint");
        };
        CustomizedMetadata.prototype.parseCoverageSizeInBytes = function (source) {
            var sizeInBytesValue = this.parseAdditionalElementValueByName(source, "sizeInBytes");
            if (sizeInBytesValue === null) {
                this.coverageSize = "N/A";
            }
            else {
                var number = parseInt(sizeInBytesValue);
                this.coverageSize = CustomizedMetadata.convertNumberOfBytesToHumanReadable(number);
                if (this.hostname === null) {
                    this.localCoverageSizeInBytes = number;
                }
                else {
                    this.remoteCoverageSizeInBytes = number;
                }
            }
        };
        CustomizedMetadata.convertNumberOfBytesToHumanReadable = function (numberOfBytes) {
            if (numberOfBytes == 0) {
                return "0 B";
            }
            var k = 1000;
            var sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
            var i = Math.floor(Math.log(numberOfBytes) / Math.log(k));
            var result = parseFloat((numberOfBytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
            return result;
        };
        CustomizedMetadata.prototype.parseAdditionalElementValueByName = function (source, inputNameElement) {
            var additionalElements = source.getChildrenAsSerializedObjects("AdditionalParameter");
            for (var i = 0; i < additionalElements.length; i++) {
                var nameElement = additionalElements[i].getChildAsSerializedObject("Name");
                var name_1 = nameElement.getValueAsString();
                if (name_1 === inputNameElement) {
                    var valueElement = additionalElements[i].getChildAsSerializedObject("Value");
                    return valueElement.getValueAsString();
                }
            }
            return null;
        };
        return CustomizedMetadata;
    }());
    ows.CustomizedMetadata = CustomizedMetadata;
})(ows || (ows = {}));
var wcs;
(function (wcs) {
    var Extension = (function () {
        function Extension(source) {
            if (source.doesElementExist("int:InterpolationMetadata")) {
                this.interpolationMetadata = new wcs.InterpolationMetadata(source.getChildAsSerializedObject("int:InterpolationMetadata"));
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
            this.formatSupported = [];
            source.getChildrenAsSerializedObjects("wcs:formatSupported").forEach(function (o) {
                _this.formatSupported.push(o.getValueAsString());
            });
            this.extension = [];
            source.getChildrenAsSerializedObjects("wcs:Extension").forEach(function (o) {
                _this.extension.push(new wcs.Extension(o));
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
            this.coverageSubType = source.getChildAsSerializedObject("CoverageSubtype").getValueAsString();
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
            _this.displayFootprint = null;
            _this.importedType = "local";
            _this.coverageId = source.getChildAsSerializedObject("wcs:CoverageId").getValueAsString();
            _this.coverageSubtype = source.getChildAsSerializedObject("wcs:CoverageSubtype").getValueAsString();
            var childElement = "wcs:CoverageSubtypeParent";
            if (source.doesElementExist(childElement)) {
                _this.coverageSubtypeParent = new wcs.CoverageSubtypeParent(source.getChildAsSerializedObject(childElement));
            }
            childElement = "ows:WGS84BoundingBox";
            if (source.doesElementExist(childElement)) {
                _this.wgs84BoundingBox = new ows.WGS84BoundingBox(source.getChildAsSerializedObject(childElement));
            }
            childElement = "ows:BoundingBox";
            if (source.doesElementExist(childElement)) {
                _this.boundingBox = new ows.BoundingBox(source.getChildAsSerializedObject(childElement));
            }
            childElement = "ows:AdditionalParameters";
            if (source.doesElementExist(childElement)) {
                _this.customizedMetadata = new ows.CustomizedMetadata(source.getChildAsSerializedObject(childElement));
                if (_this.customizedMetadata.hostname != null) {
                    _this.importedType = "remote";
                }
            }
            if (_this.coverageSubtypeParent != null && _this.coverageSubtypeParent.coverageSubType === "VirtualCoverage") {
                _this.isVirtualCoverage = true;
            }
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
            _this.coverageSummaries = [];
            var totalLocalCoverageSizesInBytes = 0;
            var totalRemoteCoverageSizesInBytes = 0;
            var totalCoverageSizesInBytes = 0;
            source.getChildrenAsSerializedObjects("wcs:CoverageSummary").forEach(function (o) {
                var coverageSummary = new wcs.CoverageSummary(o);
                _this.coverageSummaries.push(coverageSummary);
                if (coverageSummary.customizedMetadata != null) {
                    if (coverageSummary.customizedMetadata.hostname != null) {
                        _this.showCoverageLocationsColumn = true;
                    }
                    if (coverageSummary.customizedMetadata.coverageSize != "N/A") {
                        _this.showCoverageSizesColumn = true;
                        if (coverageSummary.customizedMetadata.localCoverageSizeInBytes > 0) {
                            totalLocalCoverageSizesInBytes += coverageSummary.customizedMetadata.localCoverageSizeInBytes;
                        }
                        else {
                            totalRemoteCoverageSizesInBytes += coverageSummary.customizedMetadata.remoteCoverageSizeInBytes;
                        }
                    }
                    if (coverageSummary.customizedMetadata.isBlackedList != null) {
                        _this.showBlackListedColumn = true;
                    }
                }
            });
            totalCoverageSizesInBytes += totalLocalCoverageSizesInBytes + totalRemoteCoverageSizesInBytes;
            _this.totalLocalCoverageSizes = ows.CustomizedMetadata.convertNumberOfBytesToHumanReadable(totalLocalCoverageSizesInBytes);
            _this.totalRemoteCoverageSizes = ows.CustomizedMetadata.convertNumberOfBytesToHumanReadable(totalRemoteCoverageSizesInBytes);
            _this.totalCoverageSizes = ows.CustomizedMetadata.convertNumberOfBytesToHumanReadable(totalCoverageSizesInBytes);
            _this.numberOfCoverages = source.getChildrenAsSerializedObjects("wcs:CoverageSummary").length.toString();
            if (source.doesElementExist("wcs:Extension")) {
                _this.extension = new wcs.Extension(source.getChildAsSerializedObject("wcs:Extension"));
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
                _this.serviceMetadata = new wcs.ServiceMetadata(source.getChildAsSerializedObject("wcs:ServiceMetadata"));
            }
            if (source.doesElementExist("wcs:Contents")) {
                _this.contents = new wcs.Contents(source.getChildAsSerializedObject("wcs:Contents"));
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
                this.srsName = source.getAttributeAsString("srsName");
            }
            if (source.doesAttributeExist("srsDimension")) {
                this.srsDimension = source.getAttributeAsNumber("srsDimension");
            }
            if (source.doesAttributeExist("axisLabels")) {
                this.axisLabels = source.getAttributeAsString("axisLabels").split(" ");
            }
            if (source.doesAttributeExist("uomLabels")) {
                this.uomLabels = source.getAttributeAsString("uomLabels").split(" ");
            }
            this.values = [];
            var stringValues = source.getValueAsString().split(" ");
            stringValues.forEach(function (o) {
                _this.values.push(parseFloat(o));
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
                this.srsName = source.getAttributeAsString("srsName");
            }
            if (source.doesAttributeExist("srsDimension")) {
                this.srsDimension = source.getAttributeAsNumber("srsDimension");
            }
            if (source.doesAttributeExist("axisLabels")) {
                this.axisLabels = source.getAttributeAsString("axisLabels").split(" ");
            }
            if (source.doesAttributeExist("uomLabels")) {
                this.uomLabels = source.getAttributeAsString("uomLabels").split(" ");
            }
            this.values = [];
            var stringValues = source.getValueAsString().split(" ");
            stringValues.forEach(function (o) {
                _this.values.push(o);
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
                this.srsName = source.getAttributeAsString("srsName");
            }
            if (source.doesAttributeExist("srsDimension")) {
                this.srsDimension = source.getAttributeAsNumber("srsDimension");
            }
            if (source.doesAttributeExist("axisLabels")) {
                this.axisLabels = source.getAttributeAsString("axisLabels").split(" ");
            }
            if (source.doesAttributeExist("uomLabels")) {
                this.uomLabels = source.getAttributeAsString("uomLabels").split(" ");
            }
            this.values = [];
            var stringValues = source.getValueAsString().split(" ");
            stringValues.forEach(function (o) {
                _this.values.push(o);
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
                this.srsName = source.getAttributeAsString("srsName");
            }
            if (source.doesAttributeExist("srsDimension")) {
                this.srsDimension = source.getAttributeAsNumber("srsDimension");
            }
            if (source.doesAttributeExist("axisLabels")) {
                this.axisLabels = source.getAttributeAsString("axisLabels").split(" ");
            }
            if (source.doesAttributeExist("uomLabels")) {
                this.uomLabels = source.getAttributeAsString("uomLabels").split(" ");
            }
            if (source.doesAttributeExist("frame")) {
                this.frame = source.getAttributeAsString("frame");
            }
            if (source.doesElementExist("gml:lowerCorner")) {
                this.lowerCorner = new gml.LowerCorner(source.getChildAsSerializedObject("gml:lowerCorner"));
            }
            if (source.doesElementExist("gml:upperCorner")) {
                this.upperCorner = new gml.UpperCorner(source.getChildAsSerializedObject("gml:upperCorner"));
            }
            if (source.doesElementExist("gml:pos")) {
                this.pos = new gml.Pos(source.getChildAsSerializedObject("gml:pos"));
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
                this.envelope = new gml.Envelope(source.getChildAsSerializedObject("gml:Envelope"));
            }
            if (source.doesElementExist("gml:EnvelopeWithTimePeriod")) {
                this.envelopeWithTimePeriod = new gml.EnvelopeWithTimePeriod(source.getChildAsSerializedObject("gml:EnvelopeWithTimePeriod"));
                this.envelope = this.envelopeWithTimePeriod;
            }
        }
        return BoundedBy;
    }());
    gml.BoundedBy = BoundedBy;
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
            if (source.doesElementExist("gml:Grid")) {
                this.abstractGridCoverage = new gml.GridCoverage(source);
            }
            else if (source.doesElementExist("gml:RectifiedGrid")) {
                this.abstractGridCoverage = new gml.RectifiedGridCoverage(source);
            }
            else if (source.doesElementExist("gmlrgrid:ReferenceableGridByVectors")) {
                this.abstractGridCoverage = new gml.ReferenceableGridCoverage(source);
            }
            this.abstractGridCoverage.buildObj();
        }
        return DomainSet;
    }());
    gml.DomainSet = DomainSet;
})(gml || (gml = {}));
var gml;
(function (gml) {
    var AbstractGridCoverage = (function () {
        function AbstractGridCoverage(source) {
            this.offsetVectors = [];
            this.axisTypes = [];
            this.REGULAR_AXIS = "Regular Axis";
            this.IRREGULAR_AXIS = "Irregular Axis";
            this.IRREGULAR_AXIS_RESOLUTION = "N/A";
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        AbstractGridCoverage.prototype.buildObj = function () {
            this.parseGridEnvelope();
            this.parseAxisTypesAndOffsetVectors();
        };
        AbstractGridCoverage.prototype.parseGridEnvelope = function () {
            this.gridEnvelope = new gml.GridEnvelope(this.currentSource.getChildAsSerializedObject("gml:limits"));
        };
        return AbstractGridCoverage;
    }());
    gml.AbstractGridCoverage = AbstractGridCoverage;
})(gml || (gml = {}));
var gml;
(function (gml) {
    var GridCoverage = (function (_super) {
        __extends(GridCoverage, _super);
        function GridCoverage(source) {
            var _this = _super.call(this, source) || this;
            _this.currentSource = source.getChildAsSerializedObject("gml:Grid");
            return _this;
        }
        GridCoverage.prototype.parseAxisTypesAndOffsetVectors = function () {
            var numberOfDimensions = this.currentSource.getAttributeAsNumber("dimension");
            for (var i = 0; i < numberOfDimensions; i++) {
                this.axisTypes[i] = this.REGULAR_AXIS;
                this.offsetVectors[i] = "1";
            }
        };
        return GridCoverage;
    }(gml.AbstractGridCoverage));
    gml.GridCoverage = GridCoverage;
})(gml || (gml = {}));
var gml;
(function (gml) {
    var RectifiedGridCoverage = (function (_super) {
        __extends(RectifiedGridCoverage, _super);
        function RectifiedGridCoverage(source) {
            var _this = _super.call(this, source) || this;
            _this.currentSource = source.getChildAsSerializedObject("gml:RectifiedGrid");
            return _this;
        }
        RectifiedGridCoverage.prototype.parseAxisTypesAndOffsetVectors = function () {
            var _this = this;
            this.currentSource.getChildrenAsSerializedObjects("offsetVector").forEach(function (element) {
                _this.axisTypes.push(_this.REGULAR_AXIS);
                var tmpArray = element.getValueAsString().split(" ");
                for (var i = 0; i < tmpArray.length; i++) {
                    if (tmpArray[i] != "0") {
                        _this.offsetVectors.push(tmpArray[i]);
                        break;
                    }
                }
            });
        };
        return RectifiedGridCoverage;
    }(gml.AbstractGridCoverage));
    gml.RectifiedGridCoverage = RectifiedGridCoverage;
})(gml || (gml = {}));
var gml;
(function (gml) {
    var ReferenceableGridCoverage = (function (_super) {
        __extends(ReferenceableGridCoverage, _super);
        function ReferenceableGridCoverage(source) {
            var _this = _super.call(this, source) || this;
            _this.currentSource = source.getChildAsSerializedObject("gmlrgrid:ReferenceableGridByVectors");
            return _this;
        }
        ReferenceableGridCoverage.prototype.parseAxisTypesAndOffsetVectors = function () {
            var _this = this;
            this.currentSource.getChildrenAsSerializedObjects("gmlrgrid:generalGridAxis").forEach(function (element) {
                var coefficientsElement = element.getChildAsSerializedObject("gmlrgrid:GeneralGridAxis").getChildAsSerializedObject("gmlrgrid:coefficients");
                if (coefficientsElement.getValueAsString() === "") {
                    _this.axisTypes.push(_this.REGULAR_AXIS);
                }
                else {
                    _this.axisTypes.push(_this.IRREGULAR_AXIS);
                }
                var offsetVectorElement = element.getChildAsSerializedObject("gmlrgrid:GeneralGridAxis").getChildAsSerializedObject("gmlrgrid:offsetVector");
                var tmpArray = offsetVectorElement.getValueAsString().split(" ");
                for (var i = 0; i < tmpArray.length; i++) {
                    if (tmpArray[i] != "0") {
                        if (_this.axisTypes[_this.axisTypes.length - 1] !== _this.IRREGULAR_AXIS) {
                            _this.offsetVectors.push(tmpArray[i]);
                        }
                        else {
                            _this.offsetVectors.push(_this.IRREGULAR_AXIS_RESOLUTION);
                        }
                        break;
                    }
                }
            });
        };
        return ReferenceableGridCoverage;
    }(gml.AbstractGridCoverage));
    gml.ReferenceableGridCoverage = ReferenceableGridCoverage;
})(gml || (gml = {}));
var gml;
(function (gml) {
    var GridEnvelope = (function () {
        function GridEnvelope(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            var obj = source.getChildAsSerializedObject("gml:GridEnvelope");
            this.gridLows = obj.getChildAsSerializedObject("low").getValueAsString().split(" ");
            this.gridHighs = obj.getChildAsSerializedObject("high").getValueAsString().split(" ");
        }
        return GridEnvelope;
    }());
    gml.GridEnvelope = GridEnvelope;
})(gml || (gml = {}));
var gmlcov;
(function (gmlcov) {
    var Metadata = (function () {
        function Metadata(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            var childElementTag = "gmlcov:Extension";
            if (source.doesElementExist(childElementTag)) {
                this.extension = new gmlcov.Extension(source.getChildAsSerializedObject(childElementTag));
            }
        }
        return Metadata;
    }());
    gmlcov.Metadata = Metadata;
})(gmlcov || (gmlcov = {}));
var gmlcov;
(function (gmlcov) {
    var Extension = (function () {
        function Extension(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            var childElementTag = "gmlcov:Extension";
            if (source.doesElementExist(childElementTag)) {
                this.covMetadata = new gmlcov.CovMetadata(source.getChildAsSerializedObject(childElementTag));
            }
        }
        return Extension;
    }());
    gmlcov.Extension = Extension;
})(gmlcov || (gmlcov = {}));
var gmlcov;
(function (gmlcov) {
    var CovMetadata = (function () {
        function CovMetadata(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.content = source;
        }
        return CovMetadata;
    }());
    gmlcov.CovMetadata = CovMetadata;
})(gmlcov || (gmlcov = {}));
var swe;
(function (swe) {
    var Uom = (function () {
        function Uom(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.code = source.getAttributeAsString("code");
        }
        return Uom;
    }());
    swe.Uom = Uom;
})(swe || (swe = {}));
var swe;
(function (swe) {
    var NilValue = (function () {
        function NilValue(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            var elements = source.getChildrenAsSerializedObjects("swe:nilValue");
            var reasons = [];
            var values = [];
            elements.forEach(function (element) {
                if (element.doesAttributeExist("reason")) {
                    var reasonTmp = element.getAttributeAsString("reason");
                    reasons.push(reasonTmp);
                }
                var valueTmp = element.getValueAsString();
                values.push(valueTmp);
            });
            this.reason = reasons.join(", ");
            this.value = values.join(", ");
        }
        return NilValue;
    }());
    swe.NilValue = NilValue;
})(swe || (swe = {}));
var swe;
(function (swe) {
    var NilValues = (function () {
        function NilValues(source) {
            var _this = this;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.nilValues = [];
            source.getChildrenAsSerializedObjects("swe:NilValues").forEach(function (o) {
                _this.nilValues.push(new swe.NilValue(o));
            });
        }
        return NilValues;
    }());
    swe.NilValues = NilValues;
})(swe || (swe = {}));
var swe;
(function (swe) {
    var NilValuesWrapper = (function () {
        function NilValuesWrapper(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.nilValues = new swe.NilValues(source);
        }
        NilValuesWrapper.prototype.getNullValues = function () {
            var values = [];
            this.nilValues.nilValues.forEach(function (obj) {
                values.push(obj.value);
            });
            var result = values.join(", ");
            return result;
        };
        return NilValuesWrapper;
    }());
    swe.NilValuesWrapper = NilValuesWrapper;
})(swe || (swe = {}));
var swe;
(function (swe) {
    var Quantity = (function () {
        function Quantity(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            if (source.doesElementExist("swe:nilValues")) {
                this.nilValuesWrapper = new swe.NilValuesWrapper(source.getChildAsSerializedObject("swe:nilValues"));
            }
            if (source.doesElementExist("swe:uom")) {
                this.uom = new swe.Uom(source.getChildAsSerializedObject("swe:uom"));
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
                this.name = source.getAttributeAsString("name");
            }
            if (source.doesElementExist("swe:Quantity")) {
                this.quantity = new swe.Quantity(source.getChildAsSerializedObject("swe:Quantity"));
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
            this.fields = [];
            source.getChildrenAsSerializedObjects("swe:field").forEach(function (o) {
                _this.fields.push(new swe.Field(o));
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
                this.dataRecord = new swe.DataRecord(source.getChildAsSerializedObject("swe:DataRecord"));
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
            this.coverageSubtype = source.getChildAsSerializedObject("wcs:CoverageSubtype").getValueAsString();
            if (source.doesElementExist("wcs:CoverageSubtypeParent")) {
                this.coverageSubtypeParent = new wcs.CoverageSubtypeParent(source.getChildAsSerializedObject("wcs:CoverageSubtypeParent"));
            }
            this.nativeFormat = source.getChildAsSerializedObject("nativeFormat").getValueAsString();
            if (source.doesAttributeExist("wcs:Extension")) {
                this.extension = new wcs.Extension(source.getChildAsSerializedObject("wcs:Extension"));
            }
        }
        return ServiceParameters;
    }());
    wcs.ServiceParameters = ServiceParameters;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var CoverageDescription = (function () {
        function CoverageDescription(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            var obj = source.getChildAsSerializedObject("CoverageDescription");
            this.coverageId = obj.getChildAsSerializedObject("wcs:CoverageId").getValueAsString();
            this.boundedBy = new gml.BoundedBy(obj.getChildAsSerializedObject("gml:boundedBy"));
            this.coverageFunction = new gml.CoverageFunction(obj.getChildAsSerializedObject("gml:coverageFunction"));
            this.metadata = new gmlcov.Metadata(obj.getChildAsSerializedObject("gmlcov:metadata"));
            this.domainSet = new gml.DomainSet(obj.getChildAsSerializedObject("gml:domainSet"));
            this.rangeType = new gmlcov.RangeType(obj.getChildAsSerializedObject("gmlcov:rangeType"));
            this.serviceParameters = new wcs.ServiceParameters(obj.getChildAsSerializedObject("wcs:ServiceParameters"));
        }
        return CoverageDescription;
    }());
    wcs.CoverageDescription = CoverageDescription;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var CRS = (function () {
        function CRS(subsettingCRS, outputCRS) {
            this.subsettingCRS = subsettingCRS;
            this.outputCRS = outputCRS;
        }
        CRS.prototype.toKVP = function () {
            var result = "";
            if (this.subsettingCRS) {
                result = "&subsettingCRS=" + this.subsettingCRS;
            }
            if (this.outputCRS) {
                result += "&outputCRS=" + this.outputCRS;
            }
            return result;
        };
        return CRS;
    }());
    wcs.CRS = CRS;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var Clipping = (function () {
        function Clipping(wkt) {
            this.wkt = wkt;
        }
        Clipping.prototype.toKVP = function () {
            var result = "";
            if (this.wkt) {
                result = "&clip=" + this.wkt;
            }
            return result;
        };
        return Clipping;
    }());
    wcs.Clipping = Clipping;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    var RequestBase = (function () {
        function RequestBase() {
            this.service = "WCS";
            this.version = "2.0.1";
        }
        RequestBase.prototype.toKVP = function () {
            return "&SERVICE=" + this.service +
                "&VERSION=" + this.version;
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
            _this.coverageId = angular.copy(coverageIds);
            return _this;
        }
        DescribeCoverage.prototype.toKVP = function () {
            var serialization = _super.prototype.toKVP.call(this);
            serialization += "&REQUEST=DescribeCoverage";
            serialization += "&COVERAGEID=" + this.coverageId.join(",");
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
            this.dimension = dimension;
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
            _this.slicePoint = slicePoint;
            _this.sliceIrrNotValid = false;
            _this.sliceRegularNotValid = false;
            _this.typeOfSliceNotValidDate = false;
            _this.typeOfSliceNotValidNumber = false;
            return _this;
        }
        DimensionSlice.prototype.toKVP = function () {
            return this.dimension + "(" + this.slicePoint + ")";
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
            _this.trimLow = trimLow;
            _this.trimHigh = trimHigh;
            _this.trimHighNotValid = false;
            _this.trimLowNotValid = false;
            _this.trimLowerUpperBoundNotInOrder = false;
            _this.typeOfTrimLowerNotValidDate = false;
            _this.typeOfTrimLowerNotValidNumber = false;
            _this.typeOfTrimUpperNotValidDate = false;
            _this.typeOfTrimUpperNotValidNumber = false;
            return _this;
        }
        DimensionTrim.prototype.toKVP = function () {
            return this.dimension + "(" + this.trimLow + "," + this.trimHigh + ")";
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
            _this.service = "WCS";
            _this.acceptVersions = ["2.0.1"];
            return _this;
        }
        GetCapabilities.prototype.toKVP = function () {
            return "&SERVICE=" + this.service +
                "&ACCEPTVERSIONS=" + this.acceptVersions[0] +
                "&REQUEST=" + this.request;
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
            this.rangeItem = [];
        }
        RangeSubset.prototype.toKVP = function () {
            var serializedRangeItems = [];
            this.rangeItem.forEach(function (rangeItem) {
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
            _this.coverageId = coverageId;
            _this.dimensionSubset = [];
            dimensionSubset.forEach(function (o) {
                _this.dimensionSubset.push(o);
            });
            _this.format = format;
            _this.mediaType = mediaType;
            return _this;
        }
        GetCoverage.prototype.toKVP = function () {
            var serialization = _super.prototype.toKVP.call(this);
            serialization += "&REQUEST=GetCoverage";
            serialization += "&COVERAGEID=" + this.coverageId;
            this.dimensionSubset.forEach(function (subset) {
                serialization += "&SUBSET=" + subset.toKVP();
            });
            if (this.rangeSubset) {
                serialization += this.rangeSubset.toKVP();
            }
            if (this.scaling) {
                serialization += this.scaling.toKVP();
            }
            if (this.interpolation) {
                serialization += this.interpolation.toKVP();
            }
            if (this.crs) {
                serialization += this.crs.toKVP();
            }
            if (this.clipping) {
                serialization += this.clipping.toKVP();
            }
            if (this.format) {
                serialization += "&FORMAT=" + this.format;
            }
            if (this.mediaType) {
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
            this.interpolationSupported = [];
            source.getChildrenAsSerializedObjects("int:InterpolationSupported")
                .forEach(function (interpolation) {
                _this.interpolationSupported.push(interpolation.getValueAsString());
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
            _this.request = "ProcessCoverages";
            _this.query = query;
            _this.extraParameters = angular.copy(extraParams);
            return _this;
        }
        ProcessCoverages.prototype.toKVP = function () {
            var serializedParams = "";
            for (var i = 0; i < this.extraParameters.length; ++i) {
                serializedParams += ("&" + i + "=" + encodeURI(this.extraParameters[i]));
            }
            return "&REQUEST=" + this.request
                + "&QUERY=" + encodeURI(this.query)
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
            this.globalInterpolation = globalInterpolation;
        }
        Interpolation.prototype.toKVP = function () {
            if (this.globalInterpolation) {
                return "&INTERPOLATION=" + this.globalInterpolation;
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
            _this.rangeComponent = rangeComponent;
            return _this;
        }
        RangeComponent.prototype.toKVP = function () {
            return this.rangeComponent;
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
            _this.startComponent = startComponent;
            _this.endComponent = endComponent;
            return _this;
        }
        RangeInterval.prototype.toKVP = function () {
            return this.startComponent.toKVP() + ":" + this.endComponent.toKVP();
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
            _this.scaleFactor = scaleFactor;
            return _this;
        }
        ScaleByFactor.prototype.toKVP = function () {
            return "&SCALEFACTOR=" + this.scaleFactor;
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
            this.axis = axis;
            this.scaleFactor = scaleFactor;
        }
        ScaleAxis.prototype.toKVP = function () {
            return this.axis + "(" + this.scaleFactor + ")";
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
            _this.scaleAxis = angular.copy(scaleAxis);
            return _this;
        }
        ScaleAxesByFactor.prototype.toKVP = function () {
            var serializedAxes = [];
            this.scaleAxis.forEach(function (axis) {
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
            this.axis = axis;
            this.targetSize = targetSize;
        }
        TargetAxisSize.prototype.toKVP = function () {
            return this.axis + "(" + this.targetSize + ")";
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
            _this.targetAxisSize = angular.copy(targetAxisSize);
            return _this;
        }
        ScaleToSize.prototype.toKVP = function () {
            var targetAxesSize = [];
            this.targetAxisSize.forEach(function (target) {
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
            this.axis = axis;
            this.low = low;
            this.high = high;
        }
        TargetAxisExtent.prototype.toKVP = function () {
            return this.axis + "(" + this.low + ":" + this.high + ")";
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
            _this.targetAxisExtent = angular.copy(targetAxisExtent);
            return _this;
        }
        ScaleToExtent.prototype.toKVP = function () {
            var serializedAxes = [];
            this.targetAxisExtent.forEach(function (target) {
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
    var WCSSettingsService = (function () {
        function WCSSettingsService($window) {
            this.wcsEndpoint = $window.location.href.replace("wcs-client/index.html", "ows");
            this.wcsEndpoint = this.wcsEndpoint.replace("wcs-client/app/", "rasdaman/ows");
            this.wcsServiceNameVersion = "SERVICE=WCS&VERSION=2.0.1";
            this.setWCSEndPoint(this.wcsEndpoint);
            this.defaultContextPath = this.contextPath;
            this.wcsFullEndpoint = this.wcsEndpoint + "?" + this.wcsServiceNameVersion;
        }
        WCSSettingsService.prototype.setWCSEndPoint = function (petascopeEndPoint) {
            this.wcsEndpoint = petascopeEndPoint;
            this.wcsEndpoint = this.wcsEndpoint.split("#")[0];
            if (!this.wcsEndpoint.endsWith("ows")) {
                this.wcsEndpoint = this.wcsEndpoint + "ows";
            }
            this.contextPath = this.wcsEndpoint.replace("/rasdaman/ows", "/rasdaman");
            this.adminEndpoint = this.contextPath + "/admin";
        };
        WCSSettingsService.$inject = ["$window"];
        return WCSSettingsService;
    }());
    rasdaman.WCSSettingsService = WCSSettingsService;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WCSService = (function () {
        function WCSService($http, $q, settings, serializedObjectFactory, $window, credentialService, $state, adminService) {
            this.$http = $http;
            this.$q = $q;
            this.settings = settings;
            this.serializedObjectFactory = serializedObjectFactory;
            this.$window = $window;
            this.credentialService = credentialService;
            this.$state = $state;
            this.adminService = adminService;
        }
        WCSService.prototype.getServerCapabilities = function (request) {
            var result = this.$q.defer();
            var self = this;
            var requestHeaders = {};
            var credentials = this.adminService.getPersistedAdminUserCredentials();
            if (credentials != null) {
                requestHeaders = this.adminService.getAuthenticationHeaders();
            }
            else {
                requestHeaders = this.credentialService.createRequestHeader(this.settings.wcsEndpoint, {});
            }
            var requestUrl = this.settings.wcsEndpoint + "?" + request.toKVP();
            this.$http.get(requestUrl, {
                headers: requestHeaders
            }).then(function (data) {
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
            var currentHeaders = {};
            var requestUrl = this.settings.wcsEndpoint + "?" + request.toKVP();
            this.$http.get(requestUrl, {
                headers: this.credentialService.createRequestHeader(this.settings.wcsEndpoint, currentHeaders)
            }).then(function (data) {
                try {
                    var doc = new rasdaman.common.ResponseDocument(data.data, rasdaman.common.ResponseDocumentType.XML);
                    var serializedResponse = self.serializedObjectFactory.getSerializedObject(doc);
                    var description = new wcs.CoverageDescription(serializedResponse);
                    var response = new rasdaman.common.Response(doc, description);
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
        WCSService.prototype.storeKVPParametersToLocalStorage = function (petascopeEndPoint, keysValuesStr) {
            var getCoverageKVPParameters = { "PetascopeEndPoint": petascopeEndPoint };
            getCoverageKVPParameters["request"] = keysValuesStr;
            if (this.credentialService.hasStoredCredentials()) {
                var authorizationObj = this.credentialService.getAuthorizationHeader(petascopeEndPoint);
                getCoverageKVPParameters = Object.assign(getCoverageKVPParameters, authorizationObj);
            }
            window.localStorage.setItem("GetcoverageKVPParameters", JSON.stringify(getCoverageKVPParameters));
        };
        WCSService.prototype.getCoverageHTTPGET = function (request) {
            var result = this.$q.defer();
            var requestUrl = this.settings.wcsEndpoint + "?" + request.toKVP();
            var url = this.settings.defaultContextPath + "/ows/result.html";
            this.storeKVPParametersToLocalStorage(this.settings.wcsEndpoint, request.toKVP());
            window.open(url, '_blank');
            result.resolve(requestUrl);
            return result.promise;
        };
        WCSService.prototype.getCoverageHTTPPOST = function (request) {
            return this.getCoverageHTTPGET(request);
        };
        WCSService.prototype.deleteCoverage = function (coverageId) {
            var result = this.$q.defer();
            if (!coverageId) {
                result.reject("You must specify at least one coverage ID.");
            }
            var currentHeaders = {};
            var requestUrl = this.settings.wcsEndpoint + "?" + this.settings.wcsServiceNameVersion + "&REQUEST=DeleteCoverage&COVERAGEID=" + coverageId;
            this.$http.get(requestUrl, {
                headers: this.credentialService.createRequestHeader(this.settings.wcsEndpoint, currentHeaders)
            }).then(function (data) {
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
            var currentHeaders = {};
            var requestUrl = this.settings.wcsEndpoint + "?" + this.settings.wcsServiceNameVersion + "&REQUEST=InsertCoverage&coverageRef=" + encodeURI(coverageUrl);
            if (useGeneratedId) {
                requestUrl += "&useId=new";
            }
            this.$http.get(requestUrl, {
                headers: this.credentialService.createRequestHeader(this.settings.wcsEndpoint, currentHeaders)
            }).then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WCSService.prototype.processCoverages = function (query) {
            var result = this.$q.defer();
            var queryStr = 'query=' + query;
            var requestUrl = this.settings.wcsEndpoint;
            var currentHeaders = { "Content-Type": "application/x-www-form-urlencoded" };
            var request = {
                method: 'POST',
                url: requestUrl,
                headers: this.credentialService.createRequestHeader(this.settings.wcsEndpoint, currentHeaders),
                transformResponse: null,
                data: queryStr
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
        WCSService.prototype.updateCoverageMetadata = function (formData) {
            var result = this.$q.defer();
            var requestUrl = this.settings.adminEndpoint + "/coverage/update";
            var requestHeaders = this.adminService.getAuthenticationHeaders();
            requestHeaders["Content-Type"] = undefined;
            var request = {
                method: 'POST',
                url: requestUrl,
                transformResponse: null,
                headers: requestHeaders,
                data: formData
            };
            this.$http(request).then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WCSService.prototype.renameCoverageId = function (formData) {
            var result = this.$q.defer();
            var requestUrl = this.settings.adminEndpoint + "/coverage/update";
            var requestHeaders = this.adminService.getAuthenticationHeaders();
            requestHeaders["Content-Type"] = undefined;
            var request = {
                method: 'POST',
                url: requestUrl,
                transformResponse: null,
                headers: requestHeaders,
                data: formData
            };
            this.$http(request).then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WCSService.prototype.blackListOneCoverage = function (coverageId) {
            var result = this.$q.defer();
            var requestUrl = this.settings.adminEndpoint + "/wcs/blacklist?COVERAGELIST=" + coverageId;
            var requestHeaders = this.adminService.getAuthenticationHeaders();
            this.$http.get(requestUrl, {
                headers: requestHeaders
            }).then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WCSService.prototype.blackListAllCoverages = function () {
            var result = this.$q.defer();
            var requestUrl = this.settings.adminEndpoint + "/wcs/blacklistall";
            var requestHeaders = this.adminService.getAuthenticationHeaders();
            this.$http.get(requestUrl, {
                headers: requestHeaders
            }).then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WCSService.prototype.whiteListOneCoverage = function (coverageId) {
            var result = this.$q.defer();
            var requestUrl = this.settings.adminEndpoint + "/wcs/whitelist?COVERAGELIST=" + coverageId;
            var requestHeaders = this.adminService.getAuthenticationHeaders();
            this.$http.get(requestUrl, {
                headers: requestHeaders
            }).then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WCSService.prototype.whiteListAllCoverages = function () {
            var result = this.$q.defer();
            var requestUrl = this.settings.adminEndpoint + "/wcs/whitelistall";
            var requestHeaders = this.adminService.getAuthenticationHeaders();
            this.$http.get(requestUrl, {
                headers: requestHeaders
            }).then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WCSService.$inject = ["$http", "$q", "rasdaman.WCSSettingsService",
            "rasdaman.common.SerializedObjectFactory", "$window",
            "rasdaman.CredentialService", "$state",
            "rasdaman.AdminService"];
        return WCSService;
    }());
    rasdaman.WCSService = WCSService;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var ErrorHandlingService = (function () {
        function ErrorHandlingService(notificationService, serializedObjectFactory, $log) {
            this.notificationService = notificationService;
            this.serializedObjectFactory = serializedObjectFactory;
            this.$log = $log;
        }
        ErrorHandlingService.prototype.handleError = function () {
            var args = [];
            for (var _i = 0; _i < arguments.length; _i++) {
                args[_i] = arguments[_i];
            }
            if (args.length == 1) {
                var errorInformation = args[0];
                if (errorInformation.length == 1) {
                    errorInformation = errorInformation[0];
                }
                if (!errorInformation.hasOwnProperty("status")) {
                    this.notificationService.error(errorInformation);
                    return;
                }
                if (errorInformation.data == null) {
                    if (errorInformation.status == 404 || errorInformation.status == -1) {
                        this.notificationService.error("Cannot connect to petascope, please check if petascope is running.");
                    }
                    else {
                        this.notificationService.error("The request failed with HTTP code:" + errorInformation.status + "(" + errorInformation.statusText + ")");
                    }
                }
                if (errorInformation.data != null && errorInformation.data != "") {
                    try {
                        var responseDocument = new rasdaman.common.ResponseDocument(errorInformation.data, rasdaman.common.ResponseDocumentType.XML);
                        var serializedResponse = this.serializedObjectFactory.getSerializedObject(responseDocument);
                        var exceptionReport = new ows.ExceptionReport(serializedResponse);
                        var exceptionText = exceptionReport.exception.exceptionText;
                        if (exceptionReport.exception.exceptionCode != null || exceptionReport.exception.exceptionCode != undefined) {
                            exceptionText += "</br> Exception code: " + exceptionReport.exception.exceptionCode;
                        }
                        this.notificationService.error(exceptionText);
                    }
                    catch (err) {
                        this.$log.error(err);
                    }
                }
            }
        };
        ErrorHandlingService.$inject = ["Notification", "rasdaman.common.SerializedObjectFactory", "$log"];
        return ErrorHandlingService;
    }());
    rasdaman.ErrorHandlingService = ErrorHandlingService;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var CredentialService = (function () {
        function CredentialService() {
            this.credentialsDict = JSON.parse(window.localStorage.getItem("credentials"));
            if (this.credentialsDict == null) {
                this.credentialsDict = {};
            }
        }
        CredentialService.prototype.persitCredential = function (endpoint, credential) {
            this.credentialsDict[endpoint] = credential;
            window.localStorage.setItem("credentials", JSON.stringify(this.credentialsDict));
        };
        CredentialService.prototype.clearStorage = function () {
            this.credentialsDict = {};
            window.localStorage.setItem("credentials", JSON.stringify(this.credentialsDict));
        };
        CredentialService.prototype.hasStoredCredentials = function () {
            return Object.keys(this.credentialsDict).length > 0;
        };
        CredentialService.prototype.createRequestHeader = function (petascopeEndPoint, headers) {
            var authorizationObj = this.getAuthorizationHeader(petascopeEndPoint);
            headers = Object.assign(authorizationObj, headers);
            return headers;
        };
        CredentialService.prototype.getAuthorizationHeader = function (petascopeEndPoint) {
            var result = {};
            if (this.hasStoredCredentials) {
                var credential = this.credentialsDict[petascopeEndPoint];
                if (credential != null && credential["username"] != null) {
                    var username = credential["username"];
                    var password = credential["password"];
                    if (username == "petauser") {
                        this.clearStorage();
                        return result;
                    }
                    result["Authorization"] = this.getEncodedBasicAuthencationString(username, password);
                }
            }
            return result;
        };
        CredentialService.prototype.createBasicAuthenticationHeaderByHeader = function (headers, username, password) {
            var tempHeaders = this.getEncodedBasicAuthencationString(username, password);
            headers = Object.assign(tempHeaders, headers);
            return headers;
        };
        CredentialService.prototype.createBasicAuthenticationHeader = function (username, password) {
            var headers = {};
            headers["Authorization"] = this.getEncodedBasicAuthencationString(username, password);
            return headers;
        };
        CredentialService.prototype.getEncodedBasicAuthencationString = function (username, password) {
            var result = "Basic " + btoa(username + ":" + password);
            return result;
        };
        return CredentialService;
    }());
    rasdaman.CredentialService = CredentialService;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WMSSettingsService = (function () {
        function WMSSettingsService($window) {
            this.wmsEndpoint = $window.location.href.replace("wcs-client/index.html", "ows");
            this.wmsEndpoint = this.wmsEndpoint.replace("wcs-client/app/", "rasdaman/ows");
            this.setWMSEndPoint(this.wmsEndpoint);
            this.wmsServiceNameVersion = "service=WMS&version=" + WMSSettingsService.version;
            this.setWMSFullEndPoint();
        }
        WMSSettingsService.prototype.setWMSFullEndPoint = function () {
            this.wmsFullEndpoint = this.wmsEndpoint + "?" + this.wmsServiceNameVersion;
        };
        WMSSettingsService.prototype.setWMSEndPoint = function (petascopeEndPoint) {
            this.wmsEndpoint = petascopeEndPoint;
            this.wmsEndpoint = this.wmsEndpoint.split("#")[0];
            if (!this.wmsEndpoint.endsWith("ows")) {
                this.wmsEndpoint = this.wmsEndpoint + "ows";
            }
            this.contextPath = this.wmsEndpoint.replace("/rasdaman/ows", "/rasdaman");
            this.adminEndpoint = this.contextPath + "/admin";
        };
        WMSSettingsService.$inject = ["$window"];
        WMSSettingsService.version = "1.3.0";
        return WMSSettingsService;
    }());
    rasdaman.WMSSettingsService = WMSSettingsService;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WebWorldWindService = (function () {
        function WebWorldWindService($rootScope, wmsSetting, credentialService) {
            this.webWorldWindModels = [];
            this.coveragesExtentsArray = null;
            this.wmsSetting = null;
            this.authorizationToken = "";
            this.oldLayerName = '';
            this.wmsSetting = wmsSetting;
            this.authorizationToken = credentialService.getAuthorizationHeader(this.wmsSetting.wmsEndpoint)["Authorization"];
        }
        WebWorldWindService.prototype.setCoveragesExtentsArray = function (coveragesExtentsArray) {
            this.coveragesExtentsArray = coveragesExtentsArray;
        };
        WebWorldWindService.prototype.getCoveragesExtentsArray = function () {
            return this.coveragesExtentsArray;
        };
        WebWorldWindService.prototype.getCoveragesExtentsByCoverageId = function (coverageId) {
            var result = [];
            for (var i = 0; i < this.coveragesExtentsArray.length; i++) {
                if (this.coveragesExtentsArray[i].coverageId === coverageId) {
                    result.push(this.coveragesExtentsArray[i]);
                    return result;
                }
            }
            return null;
        };
        WebWorldWindService.prototype.initWebWorldWind = function (canvasId) {
            var wwd = new WorldWind.WorldWindow(canvasId);
            var polygonLayer = new WorldWind.RenderableLayer();
            var surfaceImageLayer = new WorldWind.RenderableLayer();
            var wmsLayer = null;
            var layers = [
                { layer: new WorldWind.BMNGOneImageLayer(), enabled: true },
                { layer: new WorldWind.BingAerialWithLabelsLayer(null), enabled: true },
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
                surfaceImageLayer: surfaceImageLayer,
                wmsLayer: wmsLayer,
                polygonLayer: polygonLayer,
                hidedPolygonObjsArray: []
            };
            this.webWorldWindModels.push(webWorldWindModel);
            return webWorldWindModel;
        };
        WebWorldWindService.prototype.getCoverageIdsSameExtent = function (coverageExtent, coveragesExtentsArray) {
            var coveragedIds = [];
            var xmin = coverageExtent.bbox.xmin;
            var ymin = coverageExtent.bbox.ymin;
            var xmax = coverageExtent.bbox.xmax;
            var ymax = coverageExtent.bbox.ymax;
            for (var i = 0; i < coveragesExtentsArray.length; i++) {
                if (coveragesExtentsArray[i].show) {
                    var coverageIdTmp = coveragesExtentsArray[i].coverageId;
                    var bboxTmp = coveragesExtentsArray[i].bbox;
                    var xminTmp = bboxTmp.xmin;
                    var yminTmp = bboxTmp.ymin;
                    var xmaxTmp = bboxTmp.xmax;
                    var ymaxTmp = bboxTmp.ymax;
                    if (xmin == xminTmp && ymin == yminTmp && xmax == xmaxTmp && ymax == ymaxTmp) {
                        if (coveragesExtentsArray[i].displayFootprint) {
                            coveragedIds.push("Coverage Id: " + coverageIdTmp + "\n");
                        }
                    }
                }
            }
            return coveragedIds;
        };
        WebWorldWindService.prototype.showCoverageExtentOnGlobe = function (canvasId, coverageId) {
            var webWorldWindModel = null;
            for (var i = 0; i < this.webWorldWindModels.length; i++) {
                if (this.webWorldWindModels[i].canvasId === canvasId) {
                    webWorldWindModel = this.webWorldWindModels[i];
                    break;
                }
            }
            var polygonLayer = webWorldWindModel.polygonLayer;
            var coveragesExtentsArray = polygonLayer.coveragesExtentsArray;
            var coverageExtent = null;
            for (var i = 0; i < coveragesExtentsArray.length; i++) {
                if (coveragesExtentsArray[i].coverageId == coverageId) {
                    coverageExtent = coveragesExtentsArray[i];
                    break;
                }
            }
            if (coverageExtent != null) {
                this.gotoCoverageExtentCenter(canvasId, [coverageExtent]);
            }
            for (var i = 0; i < webWorldWindModel.hidedPolygonObjsArray.length; i++) {
                var polygonObj = webWorldWindModel.hidedPolygonObjsArray[i];
                if (polygonObj.coverageId == coverageId) {
                    polygonLayer.addRenderable(polygonObj);
                    var polygonObj = polygonLayer.renderables[0];
                    polygonObj.coverageExtentStr = "Coverage Id: " + coverageId + "\n\n"
                        + polygonObj.coverageExtentStr + "\n";
                    this.updatePolygonUserPropertiesWhenShowHide(polygonLayer);
                    return;
                }
            }
        };
        WebWorldWindService.prototype.showHideCoverageExtentOnGlobe = function (canvasId, coverageId) {
            var webWorldWindModel = null;
            for (var i = 0; i < this.webWorldWindModels.length; i++) {
                if (this.webWorldWindModels[i].canvasId === canvasId) {
                    webWorldWindModel = this.webWorldWindModels[i];
                    break;
                }
            }
            var polygonLayer = webWorldWindModel.polygonLayer;
            var coveragesExtentsArray = polygonLayer.coveragesExtentsArray;
            var coverageExtent = null;
            for (var i = 0; i < coveragesExtentsArray.length; i++) {
                if (coveragesExtentsArray[i].coverageId == coverageId) {
                    coverageExtent = coveragesExtentsArray[i];
                    break;
                }
            }
            if (coverageExtent != null) {
                this.gotoCoverageExtentCenter(canvasId, [coverageExtent]);
            }
            for (var i = 0; i < polygonLayer.renderables.length; i++) {
                var polygonObj = polygonLayer.renderables[i];
                if (polygonObj.coverageId == coverageId) {
                    polygonLayer.removeRenderable(polygonObj);
                    webWorldWindModel.hidedPolygonObjsArray.push(polygonObj);
                    for (var j = 0; j < coveragesExtentsArray.length; j++) {
                        if (coveragesExtentsArray[j].coverageId == coverageId) {
                            coveragesExtentsArray[j].displayFootprint = false;
                            break;
                        }
                    }
                    this.updateCoverageExtentShowProperty(coveragesExtentsArray, coverageId, false);
                    this.updatePolygonUserPropertiesWhenShowHide(polygonLayer);
                    return;
                }
            }
            for (var i = 0; i < webWorldWindModel.hidedPolygonObjsArray.length; i++) {
                var polygonObj = webWorldWindModel.hidedPolygonObjsArray[i];
                if (polygonObj.coverageId == coverageId) {
                    polygonLayer.addRenderable(polygonObj);
                    for (var j = 0; j < coveragesExtentsArray.length; j++) {
                        if (coveragesExtentsArray[j].coverageId == coverageId) {
                            coveragesExtentsArray[j].displayFootprint = true;
                            break;
                        }
                    }
                    this.updateCoverageExtentShowProperty(coveragesExtentsArray, coverageId, true);
                    this.updatePolygonUserPropertiesWhenShowHide(polygonLayer);
                    return;
                }
            }
        };
        WebWorldWindService.prototype.updateCoverageExtentShowProperty = function (coveragesExtentsArray, coverageId, value) {
            for (var i = 0; i < coveragesExtentsArray.length; i++) {
                if (coveragesExtentsArray[i].coverageId == coverageId) {
                    coveragesExtentsArray[i].show = value;
                    return;
                }
            }
        };
        WebWorldWindService.prototype.updatePolygonUserPropertiesWhenShowHide = function (polygonLayer) {
            var coveragesExtentsArray = polygonLayer.coveragesExtentsArray;
            for (var i = 0; i < polygonLayer.renderables.length; i++) {
                var polygonObj = polygonLayer.renderables[i];
                var coverageIds = this.getCoverageIdsSameExtent(polygonObj.coverageExtent, coveragesExtentsArray);
                var userProperties = this.buildUserPropertiesStr(coverageIds, polygonObj.coverageExtentStr);
                polygonObj.userProperties = userProperties;
            }
        };
        WebWorldWindService.prototype.prepareCoveragesExtentsForGlobe = function (canvasId, coveragesExtentsArray) {
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
            wwd.redraw();
            webWorldWindModel.polygonLayer = polygonLayer;
            wwd.addLayer(polygonLayer);
            var polygonAttributes = new WorldWind.ShapeAttributes(null);
            polygonAttributes.drawInterior = true;
            polygonAttributes.drawOutline = true;
            polygonAttributes.outlineColor = WorldWind.Color.BLUE;
            polygonAttributes.interiorColor = new WorldWind.Color(0, 1, 1, 0.1);
            polygonAttributes.applyLighting = true;
            var highlightAttributes = new WorldWind.ShapeAttributes(polygonAttributes);
            highlightAttributes.outlineColor = WorldWind.Color.RED;
            highlightAttributes.interiorColor = new WorldWind.Color(1, 1, 1, 0.1);
            var xcenter = 0, ycenter = 0;
            for (var i = 0; i < coveragesExtentsArray.length; i++) {
                var coverageExtent = coveragesExtentsArray[i];
                var coverageId = coverageExtent.coverageId;
                var bbox = coverageExtent.bbox;
                coverageExtent.show = true;
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
                polygon.coverageId = coverageId;
                polygon.highlightAttributes = highlightAttributes;
                var coverageIds = this.getCoverageIdsSameExtent(coverageExtent, coveragesExtentsArray);
                var coverageExtentStr = "Coverage Extent: lat_min=" + ymin + ", lon_min=" + xmin + ", lat_max=" + ymax + ", lon_max=" + xmax;
                polygon.coverageExtent = coverageExtent;
                polygon.coverageExtentStr = coverageExtentStr;
                var userProperties = this.buildUserPropertiesStr(coverageIds, coverageExtentStr);
                polygon.userProperties = userProperties;
                polygonLayer.coveragesExtentsArray = coveragesExtentsArray;
                webWorldWindModel.hidedPolygonObjsArray.push(polygon);
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
        WebWorldWindService.prototype.buildUserPropertiesStr = function (coverageIds, coverageExtentStr) {
            var coverageIdsStr = "";
            for (var j = 0; j < coverageIds.length; j++) {
                coverageIdsStr += coverageIds[j];
            }
            var userProperties = coverageIdsStr + "\n" + coverageExtentStr + "\n";
            return userProperties;
        };
        WebWorldWindService.prototype.loadGetMapResultOnGlobe = function (canvasId, layerName, styleName, bbox, displayLayer, timeMoment, nonXYAxes) {
            var webWorldWindModel = null;
            var exist = false;
            for (var i = 0; i < this.webWorldWindModels.length; i++) {
                if (this.webWorldWindModels[i].canvasId === canvasId) {
                    webWorldWindModel = this.webWorldWindModels[i];
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                webWorldWindModel = this.initWebWorldWind(canvasId);
            }
            var ymin = Math.max(-90, bbox.ymin);
            var ymax = Math.min(90, bbox.ymax);
            var xmin = Math.max(-180, bbox.xmin);
            var xmax = Math.min(180, bbox.xmax);
            var wwd = webWorldWindModel.wwd;
            var config = {
                title: "WMS layer overview",
                version: rasdaman.WMSSettingsService.version,
                service: this.wmsSetting.wmsEndpoint,
                layerNames: layerName,
                sector: new WorldWind.Sector(ymin, ymax, xmin, xmax),
                levelZeroDelta: new WorldWind.Location(36, 36),
                numLevels: 15,
                format: "image/png",
                styleNames: styleName,
                size: 256
            };
            var timeString;
            if (timeMoment != null) {
                timeString = '"' + timeMoment + '"';
            }
            else {
                timeString = null;
            }
            if (this.oldLayerName != layerName) {
                wwd.navigator.range = 3000 * 1000;
                this.oldLayerName = layerName;
            }
            wwd.removeLayer(webWorldWindModel.wmsLayer);
            var wmsLayer = new BAWmsLayer(config, timeString, this.authorizationToken, nonXYAxes);
            webWorldWindModel.wmsLayer = wmsLayer;
            if (displayLayer) {
                wwd.addLayer(wmsLayer);
            }
            wmsLayer.refresh();
            wwd.redraw();
        };
        WebWorldWindService.$inject = [
            "$rootScope",
            "rasdaman.WMSSettingsService",
            "rasdaman.CredentialService"
        ];
        return WebWorldWindService;
    }());
    rasdaman.WebWorldWindService = WebWorldWindService;
    var BAWmsLayer = (function (_super) {
        __extends(BAWmsLayer, _super);
        function BAWmsLayer(config, timeString, authorizationHeader, nonXYAxes) {
            var _this = _super.call(this, config, timeString) || this;
            _this.authorizationHeader = "";
            _this.authorizationHeader = authorizationHeader;
            _this.nonXYAxes = nonXYAxes;
            return _this;
        }
        BAWmsLayer.prototype.retrieveTileImage = function (dc, tile, suppressRedraw) {
            if (this.currentRetrievals.indexOf(tile.imagePath) < 0) {
                if (this.currentRetrievals.length > this.retrievalQueueSize) {
                    return;
                }
                if (this.absentResourceList.isResourceAbsent(tile.imagePath)) {
                    return;
                }
                var url = this.resourceUrlForTile(tile, this.retrievalImageFormat), image = new Image(), imagePath = tile.imagePath, cache = dc.gpuResourceCache, canvas = dc.currentGlContext.canvas, layer = this;
                for (var key in this.nonXYAxes) {
                    url += "&" + this.nonXYAxes[key];
                }
                if (!url) {
                    this.currentTilesInvalid = true;
                    return;
                }
                image.onload = function () {
                    var texture = layer.createTexture(dc, tile, image);
                    layer.removeFromCurrentRetrievals(imagePath);
                    if (texture) {
                        cache.putResource(imagePath, texture, texture.size);
                        layer.currentTilesInvalid = true;
                        layer.absentResourceList.unmarkResourceAbsent(imagePath);
                        if (!suppressRedraw) {
                            var e = document.createEvent('Event');
                            e.initEvent(WorldWind.REDRAW_EVENT_TYPE, true, true);
                            canvas.dispatchEvent(e);
                        }
                    }
                };
                image.onerror = function () {
                    layer.removeFromCurrentRetrievals(imagePath);
                    layer.absentResourceList.markResourceAbsent(imagePath);
                };
                this.currentRetrievals.push(imagePath);
                image.crossOrigin = this.crossOrigin;
                var xhr = new XMLHttpRequest();
                xhr.responseType = "arraybuffer";
                xhr.onload = function () {
                    var blb = new Blob([xhr.response], { type: 'image/png' });
                    var url = (window.URL).createObjectURL(blb);
                    image.src = url;
                };
                xhr.open("GET", url, true);
                xhr.setRequestHeader("Authorization", this.authorizationHeader);
                xhr.send();
            }
        };
        ;
        return BAWmsLayer;
    }(WorldWind.WmsLayer));
    rasdaman.BAWmsLayer = BAWmsLayer;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    function WebWorldWindDisplayWidget() {
        return {
            link: function (scope, elem, attributes) {
                console.log('attributes: ', attributes);
                var index = attributes.index;
                var minLat = attributes.minlat;
                var minLong = attributes.minlong;
                var maxLat = attributes.maxlat;
                var maxLong = attributes.maxlong;
                var canvas = document.createElement("canvas");
                canvas.id = "canvas" + Math.random().toString();
                canvas.width = 500;
                canvas.height = 500;
                var divContainerId = document.getElementById("resultRow_" + attributes.index);
                divContainerId.appendChild(canvas);
                WorldWind.Logger.setLoggingLevel(WorldWind.Logger.LEVEL_WARNING);
                var wwd = new WorldWind.WorldWindow(canvas.id);
                var layers = [
                    { layer: new WorldWind.BMNGOneImageLayer(), enabled: true },
                    { layer: new WorldWind.BingAerialWithLabelsLayer(null), enabled: true },
                    { layer: new WorldWind.CompassLayer(), enabled: true },
                    { layer: new WorldWind.CoordinatesDisplayLayer(wwd), enabled: true },
                    { layer: new WorldWind.ViewControlsLayer(wwd), enabled: true }
                ];
                for (var l = 0; l < layers.length; l++) {
                    layers[l].layer.enabled = layers[l].enabled;
                    wwd.addLayer(layers[l].layer);
                }
                var image = new Image();
                image.src = "data:image/png;base64," + attributes.data;
                var surfaceImage = new WorldWind.SurfaceImage(new WorldWind.Sector(minLat, maxLat, minLong, maxLong), new WorldWind.ImageSource(image));
                var surfaceImageLayer = new WorldWind.RenderableLayer();
                surfaceImageLayer.displayName = "Surface Images";
                surfaceImageLayer.addRenderable(surfaceImage);
                wwd.addLayer(surfaceImageLayer);
                var xcenter = (parseFloat(minLong) + parseFloat(maxLong)) / 2;
                var ycenter = (parseFloat(minLat) + parseFloat(maxLat)) / 2;
                wwd.navigator.lookAtLocation = new WorldWind.Location(ycenter, xcenter);
                wwd.redraw();
            }
        };
    }
    rasdaman.WebWorldWindDisplayWidget = WebWorldWindDisplayWidget;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WCSSettingsController = (function () {
        function WCSSettingsController($scope, settingsService) {
            this.$scope = $scope;
            this.settingsService = settingsService;
            $scope.wcsEndpoint = settingsService.wcsEndpoint;
            $scope.updateSettings = function () {
                console.log($scope.wcsEndpoint);
            };
        }
        WCSSettingsController.$inject = [
            "$scope",
            "rasdaman.WCSSettingsService"
        ];
        return WCSSettingsController;
    }());
    rasdaman.WCSSettingsController = WCSSettingsController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WCSMainController = (function () {
        function WCSMainController($scope, $rootScope, $state, adminService) {
            var _this = this;
            this.$scope = $scope;
            this.initializeTabs($scope);
            $scope.$watch("adminStateInformation.loggedIn", function (newValue, oldValue) {
                if (oldValue == true || newValue == true) {
                    if ($scope.isSupportWCST) {
                        var roles = $rootScope.adminStateInformation.roles;
                        if (rasdaman.AdminService.hasRole(roles, rasdaman.AdminService.PRIV_OWS_WCS_INSERT_COV)) {
                            $scope.wcsInsertCoverageTab.disabled = false;
                        }
                        if (rasdaman.AdminService.hasRole(roles, rasdaman.AdminService.PRIV_OWS_WCS_DELETE_COV)) {
                            $scope.wcsDeleteCoverageTab.disabled = false;
                        }
                        $rootScope.$broadcast("reloadWCSServerCapabilities", true);
                        $rootScope.$broadcast("reloadWMSServerCapabilities", true);
                    }
                }
                else {
                    $scope.wcsInsertCoverageTab.disabled = true;
                    $scope.wcsDeleteCoverageTab.disabled = true;
                    $rootScope.$broadcast("reloadWCSServerCapabilities", true);
                    $rootScope.$broadcast("reloadWMSServerCapabilities", true);
                }
            });
            $scope.$watch("wcsStateInformation.serverCapabilities", function (newValue, oldValue) {
                if (newValue) {
                    $scope.wcsDescribeCoverageTab.disabled = false;
                    $scope.wcsGetCoverageTab.disabled = false;
                    $scope.wcsProcessCoverageTab.disabled = !WCSMainController.isProcessCoverageEnabled(newValue);
                    $scope.isSupportWCST = WCSMainController.isCoverageTransactionEnabled(newValue);
                    if ($rootScope.adminStateInformation.loggedIn === false) {
                        $scope.wcsInsertCoverageTab.disabled = true;
                        $scope.wcsDeleteCoverageTab.disabled = true;
                    }
                }
                else {
                    _this.resetState();
                }
            });
            $scope.$watch("wcsStateInformation.selectedCoverageDescription", function (newValue, oldValue) {
                $scope.wcsGetCoverageTab.disabled = newValue ? false : true;
            });
            $scope.tabs = [$scope.wcsGetCapabilitiesTab, $scope.wcsDescribeCoverageTab, $scope.wcsGetCoverageTab, $scope.wcsProcessCoverageTab, $scope.wcsDeleteCoverageTab, $scope.wcsInsertCoverageTab];
            $scope.wcsStateInformation = {
                serverCapabilities: null,
                selectedCoverageDescription: null,
                selectedGetCoverageId: null,
                reloadServerCapabilities: true
            };
            $scope.describeCoverage = function (coverageId) {
                $scope.wcsDescribeCoverageTab.active = true;
                $rootScope.wcsSelectedGetCoverageId = coverageId;
            };
        }
        WCSMainController.prototype.initializeTabs = function ($scope) {
            $scope.wcsGetCapabilitiesTab = {
                heading: "GetCapabilities",
                view: "get_capabilities",
                active: true,
                disabled: false
            };
            $scope.wcsDescribeCoverageTab = {
                heading: "DescribeCoverage",
                view: "describe_coverage",
                active: false,
                disabled: false
            };
            $scope.wcsGetCoverageTab = {
                heading: "GetCoverage",
                view: "get_coverage",
                active: false,
                disabled: false
            };
            $scope.wcsProcessCoverageTab = {
                heading: "ProcessCoverages",
                view: "process_coverages",
                active: false,
                disabled: false
            };
            $scope.wcsDeleteCoverageTab = {
                heading: "DeleteCoverage",
                view: "delete_coverage",
                active: false,
                disabled: false
            };
            $scope.wcsInsertCoverageTab = {
                heading: "InsertCoverage",
                view: "insert_coverage",
                active: false,
                disabled: false
            };
        };
        WCSMainController.prototype.resetState = function () {
            this.$scope.wcsDescribeCoverageTab.disabled = true;
            this.$scope.wcsGetCoverageTab.disabled = true;
            this.$scope.wcsProcessCoverageTab.disabled = true;
            this.$scope.wcsDeleteCoverageTab.disabled = false;
            this.$scope.wcsInsertCoverageTab.disabled = false;
        };
        WCSMainController.isProcessCoverageEnabled = function (serverCapabilities) {
            var processExtensionUri = rasdaman.Constants.PROCESSING_EXT_URI;
            return serverCapabilities.serviceIdentification.profile.indexOf(processExtensionUri) != -1;
        };
        WCSMainController.isCoverageTransactionEnabled = function (serverCapabilities) {
            var transactionExtensionUri = rasdaman.Constants.TRANSACTION_EXT_URI;
            return serverCapabilities.serviceIdentification.profile.indexOf(transactionExtensionUri) != -1;
        };
        WCSMainController.$inject = ["$scope", "$rootScope", "$state", "rasdaman.AdminService"];
        return WCSMainController;
    }());
    rasdaman.WCSMainController = WCSMainController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WCSGetCapabilitiesController = (function () {
        function WCSGetCapabilitiesController($window, $scope, $rootScope, $log, wcsService, settings, alertService, errorHandlingService, webWorldWindService) {
            var _this = this;
            this.$window = $window;
            this.$scope = $scope;
            this.$rootScope = $rootScope;
            this.$log = $log;
            this.wcsService = wcsService;
            this.settings = settings;
            this.alertService = alertService;
            this.errorHandlingService = errorHandlingService;
            this.webWorldWindService = webWorldWindService;
            $scope.isAvailableCoveragesOpen = false;
            $scope.isCoveragesExtentsOpen = false;
            $scope.isServiceIdentificationOpen = false;
            $scope.isServiceProviderOpen = false;
            $scope.isCapabilitiesDocumentOpen = false;
            $scope.displayCoveragesDropdownItems = [{ "name": "Display all coverages", "value": "" },
                { "name": "Display local coverages", "value": "local" },
                { "name": "Display remote coverages", "value": "remote" }
            ];
            $scope.selectedDisplayCoveragesByTypeDropdown = "all";
            $scope.coveragesExtents = [];
            $scope.showAllFootprints = { isChecked: false };
            $scope.rowPerPageSmartTable = 10;
            $scope.wcsServerEndpoint = settings.wcsEndpoint;
            var canvasId = "wcsCanvasGetCapabilities";
            $rootScope.$watch("adminStateInformation.loggedIn", function (newValue, oldValue) {
                if (newValue) {
                    $scope.adminUserLoggedIn = true;
                }
                else {
                    $scope.adminUserLoggedIn = false;
                }
            });
            $scope.initCheckboxesForCoverageIds = function () {
                var coverageSummaryArray = $scope.capabilities.contents.coverageSummaries;
                for (var i = 0; i < coverageSummaryArray.length; i++) {
                    for (var j = 0; j < $scope.coveragesExtents.length; j++) {
                        if ($scope.coveragesExtents[j].coverageId === coverageSummaryArray[i].coverageId) {
                            coverageSummaryArray[i].displayFootprint = false;
                            break;
                        }
                    }
                }
            };
            $scope.getCoverageSummaryByCoverageId = function (coverageId) {
                var coverageSummaryArray = $scope.capabilities.contents.coverageSummaries;
                for (var i = 0; i < coverageSummaryArray.length; i++) {
                    if (coverageSummaryArray[i].coverageId == coverageId) {
                        return coverageSummaryArray[i];
                    }
                }
            };
            $scope.displayFootprintOnGlobe = function (coverageId) {
                webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, coverageId);
            };
            $scope.displayAllFootprintsOnGlobe = function (status) {
                if (status == true) {
                    var filteredRows = JSON.parse($window.wcsGetCapabilitiesFilteredRows);
                    $scope.hideAllFootprintsOnGlobe();
                    for (var i = 0; i < filteredRows.length; i++) {
                        var obj = filteredRows[i];
                        var covId = obj["coverageId"];
                        for (var j = 0; j < $scope.coveragesExtents.length; j++) {
                            var coverageId = $scope.coveragesExtents[j].coverageId;
                            if (covId === coverageId) {
                                $scope.getCoverageSummaryByCoverageId(coverageId).displayFootprint = true;
                                webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, coverageId);
                                break;
                            }
                        }
                    }
                }
                else {
                    $scope.hideAllFootprintsOnGlobe();
                }
            };
            $scope.hideAllFootprintsOnGlobe = function () {
                for (var i = 0; i < $scope.coveragesExtents.length; i++) {
                    var coverageId = $scope.coveragesExtents[i].coverageId;
                    if ($scope.coveragesExtents[i].displayFootprint == true) {
                        $scope.getCoverageSummaryByCoverageId(coverageId).displayFootprint = false;
                        webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, coverageId);
                    }
                }
            };
            $scope.handleBlackListOneCoverage = function (coverageId) {
                var status = $scope.getCoverageSummaryByCoverageId(coverageId).customizedMetadata.isBlackedList;
                if (status == true) {
                    _this.wcsService.blackListOneCoverage(coverageId).then(function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i] = arguments[_i];
                        }
                        _this.alertService.success("Blacklisted coverage <b>" + coverageId + "</b>");
                    }, function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i] = arguments[_i];
                        }
                        _this.errorHandlingService.handleError(args);
                        _this.$log.error(args);
                    })["finally"](function () {
                    });
                }
                else {
                    _this.wcsService.whiteListOneCoverage(coverageId).then(function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i] = arguments[_i];
                        }
                        _this.alertService.success("Whitelisted coverage <b>" + coverageId + "</b>");
                    }, function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i] = arguments[_i];
                        }
                        _this.errorHandlingService.handleError(args);
                        _this.$log.error(args);
                    })["finally"](function () {
                    });
                }
            };
            $scope.handleBlackListAllCoverages = function () {
                _this.wcsService.blackListAllCoverages().then(function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    _this.alertService.success("Blacklisted <b>all coverages</b>");
                    var coverageSummaryArray = $scope.capabilities.contents.coverageSummaries;
                    for (var i = 0; i < coverageSummaryArray.length; i++) {
                        coverageSummaryArray[i].customizedMetadata.isBlackedList = true;
                    }
                }, function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    _this.errorHandlingService.handleError(args);
                    _this.$log.error(args);
                })["finally"](function () {
                });
            };
            $scope.handleWhiteListAllCoverages = function () {
                _this.wcsService.whiteListAllCoverages().then(function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    _this.alertService.success("Whitelisted <b>all coverages</b>");
                    var coverageSummaryArray = $scope.capabilities.contents.coverageSummaries;
                    for (var i = 0; i < coverageSummaryArray.length; i++) {
                        coverageSummaryArray[i].customizedMetadata.isBlackedList = false;
                    }
                }, function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    _this.errorHandlingService.handleError(args);
                    _this.$log.error(args);
                })["finally"](function () {
                });
            };
            $scope.$on("reloadWCSServerCapabilities", function (event, b) {
                $scope.getServerCapabilities();
            });
            $scope.$watch("wcsStateInformation.reloadServerCapabilities", function (capabilities) {
                if ($scope.wcsStateInformation.reloadServerCapabilities == true) {
                    $scope.getServerCapabilities();
                }
                $scope.wcsStateInformation.reloadServerCapabilities = false;
            });
            $scope.parseCoveragesExtents = function () {
                var coverageSummaries = $scope.capabilities.contents.coverageSummaries;
                coverageSummaries.forEach(function (coverageSummary) {
                    var coverageId = coverageSummary.coverageId;
                    var wgs84BoundingBox = coverageSummary.wgs84BoundingBox;
                    if (wgs84BoundingBox != null) {
                        var lowerArrayTmp = wgs84BoundingBox.lowerCorner.split(" ");
                        var xMin = parseFloat(lowerArrayTmp[0]);
                        var yMin = parseFloat(lowerArrayTmp[1]);
                        var upperArrayTmp = wgs84BoundingBox.upperCorner.split(" ");
                        var xMax = parseFloat(upperArrayTmp[0]);
                        var yMax = parseFloat(upperArrayTmp[1]);
                        var bboxObj = {
                            "coverageId": coverageId,
                            "bbox": {
                                "xmin": xMin,
                                "ymin": yMin,
                                "xmax": xMax,
                                "ymax": yMax
                            },
                            "displayFootprint": false
                        };
                        $scope.coveragesExtents.push(bboxObj);
                    }
                });
                webWorldWindService.setCoveragesExtentsArray($scope.coveragesExtents);
                $scope.isCoveragesExtentsOpen = true;
                $scope.initCheckboxesForCoverageIds();
                webWorldWindService.prepareCoveragesExtentsForGlobe(canvasId, $scope.coveragesExtents);
            };
            $scope.handleGetServerCapabilities = function () {
                $scope.getServerCapabilities();
                $scope.showAllFootprints.isChecked = false;
            };
            $scope.getServerCapabilities = function () {
                var args = [];
                for (var _i = 0; _i < arguments.length; _i++) {
                    args[_i] = arguments[_i];
                }
                if (!$scope.wcsServerEndpoint) {
                    alertService.error("The entered WCS endpoint is invalid.");
                    return;
                }
                $scope.coveragesExtents = [];
                settings.wcsEndpoint = $scope.wcsServerEndpoint;
                var capabilitiesRequest = new wcs.GetCapabilities();
                wcsService.getServerCapabilities(capabilitiesRequest)
                    .then(function (response) {
                    $scope.capabilitiesDocument = response.document;
                    $scope.capabilities = response.value;
                    $scope.isAvailableCoveragesOpen = true;
                    $scope.isServiceIdentificationOpen = true;
                    $scope.isServiceProviderOpen = true;
                    $scope.parseCoveragesExtents();
                }, function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    $scope.capabilitiesDocument = null;
                    $scope.capabilities = null;
                    $scope.isAvailableCoveragesOpen = false;
                    $scope.isServiceIdentificationOpen = false;
                    $scope.isServiceProviderOpen = false;
                    errorHandlingService.handleError(args);
                    $log.error(args);
                })["finally"](function () {
                    $scope.wcsStateInformation.serverCapabilities = $scope.capabilities;
                });
            };
        }
        WCSGetCapabilitiesController.$inject = [
            "$window",
            "$scope",
            "$rootScope",
            "$log",
            "rasdaman.WCSService",
            "rasdaman.WCSSettingsService",
            "Notification",
            "rasdaman.ErrorHandlingService",
            "rasdaman.WebWorldWindService"
        ];
        return WCSGetCapabilitiesController;
    }());
    rasdaman.WCSGetCapabilitiesController = WCSGetCapabilitiesController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WCSDescribeCoverageController = (function () {
        function WCSDescribeCoverageController($scope, $rootScope, $log, wcsService, settings, alertService, errorHandlingService, webWorldWindService) {
            $scope.selectedCoverageId = null;
            $scope.newCoverageId = null;
            $scope.REGULAR_AXIS = "regular";
            $scope.IRREGULAR_AXIS = "irregular";
            $scope.NOT_AVALIABLE = "N/A";
            $scope.hideWebWorldWindGlobe = true;
            $scope.isCoverageIdValid = function () {
                if ($scope.wcsStateInformation.serverCapabilities) {
                    var coverageSummaries = $scope.wcsStateInformation.serverCapabilities.contents.coverageSummaries;
                    for (var i = 0; i < coverageSummaries.length; i++) {
                        if (coverageSummaries[i].coverageId == $scope.selectedCoverageId) {
                            return true;
                        }
                    }
                }
                return false;
            };
            $rootScope.$watch("wcsSelectedGetCoverageId", function (coverageId) {
                if (coverageId != null) {
                    $scope.selectedCoverageId = coverageId;
                    $scope.describeCoverage();
                }
            });
            $scope.$watch("wcsStateInformation.serverCapabilities", function (capabilities) {
                if (capabilities) {
                    $scope.availableCoverageIds = [];
                    $scope.coverageCustomizedMetadatasDict = {};
                    capabilities.contents.coverageSummaries.forEach(function (coverageSummary) {
                        var coverageId = coverageSummary.coverageId;
                        $scope.availableCoverageIds.push(coverageId);
                        if (coverageSummary.customizedMetadata != null) {
                            $scope.coverageCustomizedMetadatasDict[coverageId] = coverageSummary.customizedMetadata;
                        }
                    });
                }
            });
            $scope.$watch("wcsStateInformation.selectedGetCoverageId", function (getCoverageId) {
                if (getCoverageId) {
                    $scope.selectedCoverageId = getCoverageId;
                    $scope.describeCoverage();
                }
            });
            $rootScope.$watch("adminStateInformation.loggedIn", function (newValue, oldValue) {
                if (newValue) {
                    $scope.adminUserLoggedIn = true;
                    $scope.hasRole = rasdaman.AdminService.hasRole($rootScope.adminStateInformation.roles, rasdaman.AdminService.PRIV_OWS_WCS_UPDATE_COV);
                }
                else {
                    $scope.adminUserLoggedIn = false;
                }
            });
            $scope.updateCoverageMetadata = function () {
                var fileInput = document.getElementById("coverageMetadataUploadFile");
                var mimeType = fileInput.files[0].type;
                var requiredMimeTypes = ["", "text/xml", "", "application/json", "text/plain"];
                if (!requiredMimeTypes.includes(mimeType)) {
                    alertService.error("Coverage's metadata file to update must be <b>xml/json/text</b> format. Given: <b>'" + mimeType + "'</b>.");
                    return;
                }
                var formData = new FormData();
                formData.append("coverageId", $scope.selectedCoverageId);
                formData.append("fileName", fileInput.files[0]);
                wcsService.updateCoverageMetadata(formData).then(function (response) {
                    alertService.success("Successfully update coverage's metadata from file.");
                    $scope.describeCoverage();
                }, function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    errorHandlingService.handleError(args);
                    $log.error(args);
                });
            };
            $scope.renameCoverageId = function () {
                if ($scope.newCoverageId == null || $scope.newCoverageId.trim() == "") {
                    alertService.error("New coverage id cannot be empty.");
                    return;
                }
                var formData = new FormData();
                formData.append("coverageId", $scope.selectedCoverageId);
                formData.append("newCoverageId", $scope.newCoverageId);
                wcsService.updateCoverageMetadata(formData).then(function (response) {
                    alertService.success("Successfully rename coverage's id.");
                    $scope.selectedCoverageId = $scope.newCoverageId;
                    $scope.newCoverageId = null;
                    $scope.describeCoverage();
                }, function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    errorHandlingService.handleError(args);
                    $log.error(args);
                });
            };
            $scope.parseCoverageMetadata = function () {
                $scope.metadata = null;
                var parser = new DOMParser();
                var xmlDoc = parser.parseFromString($scope.rawCoverageDescription, "text/xml");
                var elements = xmlDoc.getElementsByTagName("rasdaman:covMetadata");
                if (elements.length == 0) {
                    elements = xmlDoc.getElementsByTagName("gmlcov:Extension");
                }
                if (elements.length > 0) {
                    $scope.metadata = elements[0].innerHTML;
                    for (var i = 0; i < $scope.metadata.length; i++) {
                        if ($scope.metadata[i] === "{") {
                            $scope.typeMetadata = "json";
                            break;
                        }
                        else {
                            $scope.typeMetadata = "xml";
                            break;
                        }
                    }
                }
                if ($scope.metadata == null) {
                    $scope.metadata = " ";
                    $("#btnUpdateCoverageMetadata").text("Insert metadata");
                }
                else {
                    $("#btnUpdateCoverageMetadata").text("Update metadata");
                }
            };
            $scope.describeCoverage = function () {
                var coverageIds = [];
                coverageIds.push($scope.selectedCoverageId);
                var describeCoverageRequest = new wcs.DescribeCoverage(coverageIds);
                $scope.requestUrl = settings.wcsEndpoint + "?" + describeCoverageRequest.toKVP();
                $scope.axes = [];
                $("#coverageMetadataUploadFile").val("");
                $("#uploadFileName").html("");
                $("#btnUpdateCoverageMetadata").hide();
                wcsService.getCoverageDescription(describeCoverageRequest)
                    .then(function (response) {
                    $scope.coverageDescription = response.value;
                    $scope.rawCoverageDescription = response.document.value;
                    $scope.parseCoverageMetadata();
                    var coverageExtentArray = webWorldWindService.getCoveragesExtentsByCoverageId($scope.selectedCoverageId);
                    if (coverageExtentArray == null) {
                        $scope.hideWebWorldWindGlobe = true;
                    }
                    else {
                        var canvasId = "wcsCanvasDescribeCoverage";
                        $scope.hideWebWorldWindGlobe = false;
                        webWorldWindService.prepareCoveragesExtentsForGlobe(canvasId, coverageExtentArray);
                        webWorldWindService.showCoverageExtentOnGlobe(canvasId, $scope.selectedCoverageId);
                    }
                }, function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    $scope.coverageDescription = null;
                    errorHandlingService.handleError(args);
                    $log.error(args);
                })["finally"](function () {
                    $scope.wcsStateInformation.selectedCoverageDescription = $scope.coverageDescription;
                });
            };
        }
        WCSDescribeCoverageController.$inject = [
            "$scope",
            "$rootScope",
            "$log",
            "rasdaman.WCSService",
            "rasdaman.WCSSettingsService",
            "Notification",
            "rasdaman.ErrorHandlingService",
            "rasdaman.WebWorldWindService"
        ];
        return WCSDescribeCoverageController;
    }());
    rasdaman.WCSDescribeCoverageController = WCSDescribeCoverageController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WCSDeleteCoverageController = (function () {
        function WCSDeleteCoverageController($rootScope, $scope, $log, alertService, wcsService, errorHandlingService) {
            var _this = this;
            this.$rootScope = $rootScope;
            this.$scope = $scope;
            this.$log = $log;
            this.alertService = alertService;
            this.wcsService = wcsService;
            this.errorHandlingService = errorHandlingService;
            function isCoverageIdValid(coverageId) {
                if ($scope.wcsStateInformation.serverCapabilities) {
                    var coverageSummaries = $scope.wcsStateInformation.serverCapabilities.contents.coverageSummaries;
                    for (var i = 0; i < coverageSummaries.length; ++i) {
                        if (coverageSummaries[i].coverageId == coverageId) {
                            return true;
                        }
                    }
                }
                return false;
            }
            $scope.$watch("idOfCoverageToDelete", function (newValue, oldValue) {
                $scope.isCoverageIdValid = isCoverageIdValid(newValue);
            });
            $scope.$watch("wcsStateInformation.serverCapabilities", function (capabilities) {
                if (capabilities) {
                    $scope.availableCoverageIds = [];
                    capabilities.contents.coverageSummaries.forEach(function (coverageSummary) {
                        $scope.availableCoverageIds.push(coverageSummary.coverageId);
                    });
                }
            });
            $scope.deleteCoverage = function () {
                if ($scope.requestInProgress) {
                    _this.alertService.error("Cannot delete a coverage while another delete request is in progress.");
                }
                else if (!isCoverageIdValid($scope.idOfCoverageToDelete)) {
                    _this.alertService.error("The coverage <b>" + $scope.idOfCoverageToDelete + "</b> is not valid.");
                }
                else {
                    $scope.requestInProgress = true;
                    _this.wcsService.deleteCoverage($scope.idOfCoverageToDelete).then(function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i] = arguments[_i];
                        }
                        _this.alertService.success("Deleted coverage <b>" + $scope.idOfCoverageToDelete + "</b>");
                        $rootScope.$broadcast("reloadWCSServerCapabilities", true);
                        $rootScope.$broadcast("reloadWMSServerCapabilities", true);
                    }, function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i] = arguments[_i];
                        }
                        _this.errorHandlingService.handleError(args);
                        _this.$log.error(args);
                    })["finally"](function () {
                        $scope.requestInProgress = false;
                    });
                }
            };
            $scope.idOfCoverageToDelete = null;
            $scope.requestInProgress = false;
            $scope.isCoverageIdValid = false;
        }
        WCSDeleteCoverageController.$inject = [
            "$rootScope",
            "$scope",
            "$log",
            "Notification",
            "rasdaman.WCSService",
            "rasdaman.ErrorHandlingService"
        ];
        return WCSDeleteCoverageController;
    }());
    rasdaman.WCSDeleteCoverageController = WCSDeleteCoverageController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WCSInsertCoverageController = (function () {
        function WCSInsertCoverageController($scope, $log, alertService, wcsService, errorHandlingService) {
            var _this = this;
            this.$scope = $scope;
            this.$log = $log;
            this.alertService = alertService;
            this.wcsService = wcsService;
            this.errorHandlingService = errorHandlingService;
            $scope.urlOfCoverageToInsert = null;
            $scope.requestInProgress = false;
            $scope.useGeneratedCoverageId = false;
            $scope.insertCoverage = function () {
                if ($scope.requestInProgress) {
                    _this.alertService.error("Cannot insert a coverage while another insert request is in progress.");
                }
                else {
                    $scope.requestInProgress = true;
                    _this.wcsService.insertCoverage($scope.urlOfCoverageToInsert, $scope.useGeneratedCoverageId).then(function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i] = arguments[_i];
                        }
                        _this.alertService.success("Successfully inserted coverage.");
                        _this.$log.info(args);
                        $scope.wcsStateInformation.reloadServerCapabilities = true;
                    }, function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i] = arguments[_i];
                        }
                        _this.errorHandlingService.handleError(args);
                        _this.$log.error(args);
                    })["finally"](function () {
                        $scope.requestInProgress = false;
                    });
                }
            };
        }
        WCSInsertCoverageController.$inject = [
            "$scope",
            "$log",
            "Notification",
            "rasdaman.WCSService",
            "rasdaman.ErrorHandlingService"
        ];
        return WCSInsertCoverageController;
    }());
    rasdaman.WCSInsertCoverageController = WCSInsertCoverageController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WCSGetCoverageController = (function () {
        function WCSGetCoverageController($http, $scope, $rootScope, $log, wcsService, alertService, webWorldWindService) {
            $scope.selectedCoverageId = null;
            $scope.isGlobeOpen = false;
            $scope.isGetCoverageHideGlobe = true;
            $scope.isCoverageIdValid = function () {
                if ($scope.wcsStateInformation.serverCapabilities) {
                    var coverageSummaries = $scope.wcsStateInformation.serverCapabilities.contents.coverageSummaries;
                    for (var i = 0; i < coverageSummaries.length; ++i) {
                        if (coverageSummaries[i].coverageId == $scope.selectedCoverageId) {
                            return true;
                        }
                    }
                }
                return false;
            };
            $scope.$watch("wcsStateInformation.serverCapabilities", function (capabilities) {
                if (capabilities) {
                    $scope.avaiableHTTPRequests = ["GET", "POST"];
                    $scope.selectedHTTPRequest = $scope.avaiableHTTPRequests[0];
                    $scope.availableCoverageIds = [];
                    $scope.coverageCustomizedMetadatasDict = {};
                    capabilities.contents.coverageSummaries.forEach(function (coverageSummary) {
                        var coverageId = coverageSummary.coverageId;
                        $scope.availableCoverageIds.push(coverageId);
                        if (coverageSummary.customizedMetadata != null) {
                            $scope.coverageCustomizedMetadatasDict[coverageId] = coverageSummary.customizedMetadata;
                        }
                    });
                }
            });
            $scope.loadCoverageExtentOnGlobe = function () {
                var coverageExtentArray = webWorldWindService.getCoveragesExtentsByCoverageId($scope.selectedCoverageId);
                if (coverageExtentArray == null) {
                    $scope.isGetCoverageHideGlobe = true;
                }
                else {
                    var canvasId = "wcsCanvasGetCoverage";
                    $scope.isGetCoverageHideGlobe = false;
                    webWorldWindService.prepareCoveragesExtentsForGlobe(canvasId, coverageExtentArray);
                    webWorldWindService.showCoverageExtentOnGlobe(canvasId, $scope.selectedCoverageId);
                }
            };
            $scope.selectCoverageClickEvent = function () {
                if (!$scope.isCoverageIdValid()) {
                    alertService.error("The entered coverage ID is invalid.");
                    return;
                }
                else {
                    $scope.wcsStateInformation.selectedGetCoverageId = $scope.selectedCoverageId;
                    $scope.loadCoverageExtentOnGlobe();
                }
            };
            $scope.getCoverageClickEvent = function () {
                var numberOfAxis = $scope.coverageDescription.boundedBy.envelope.lowerCorner.values.length;
                var dimensionSubset = [];
                for (var i = 0; i < numberOfAxis; ++i) {
                    var min = $scope.coverageDescription.boundedBy.envelope.lowerCorner.values[i];
                    var max = $scope.coverageDescription.boundedBy.envelope.upperCorner.values[i];
                    if ($scope.core.isTrimSelected[i]) {
                        if ($scope.core.trims[i].trimLow != min.toString()
                            || $scope.core.trims[i].trimHigh != max.toString()) {
                            dimensionSubset.push($scope.core.trims[i]);
                        }
                    }
                    else {
                        dimensionSubset.push($scope.core.slices[i]);
                    }
                }
                var getCoverageRequest = new wcs.GetCoverage($scope.coverageDescription.coverageId, dimensionSubset, $scope.core.selectedCoverageFormat, $scope.core.isMultiPartFormat);
                getCoverageRequest.rangeSubset = $scope.rangeSubsettingExtension.rangeSubset;
                getCoverageRequest.scaling = $scope.scalingExtension.getScaling();
                getCoverageRequest.interpolation = $scope.interpolationExtension.getInterpolation();
                getCoverageRequest.crs = $scope.crsExtension.getCRS();
                getCoverageRequest.clipping = $scope.clippingExtension.getClipping();
                if ($scope.selectedHTTPRequest == "GET") {
                    wcsService.getCoverageHTTPGET(getCoverageRequest)
                        .then(function (requestUrl) {
                        $scope.core.requestUrl = requestUrl;
                    }, function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i] = arguments[_i];
                        }
                        $scope.core.requestUrl = null;
                        alertService.error("Failed to execute GetCoverage operation in HTTP GET.");
                        $log.error(args);
                    });
                }
                else {
                    $scope.core.requestUrl = null;
                    wcsService.getCoverageHTTPPOST(getCoverageRequest);
                }
            };
            $scope.setOutputFormat = function (numberOfDimensions) {
                var result = "application/netcdf";
                if (numberOfDimensions == 2) {
                    result = "image/tiff";
                }
                else if (numberOfDimensions == 1) {
                    result = "application/json";
                }
                return result;
            };
            $scope.$watch("wcsStateInformation.selectedCoverageDescription", function (coverageDescription) {
                if (coverageDescription) {
                    $scope.coverageDescription = $scope.wcsStateInformation.selectedCoverageDescription;
                    $scope.selectedCoverageId = $scope.coverageDescription.coverageId;
                    $scope.wcsStateInformation.selectedGetCoverageId = null;
                    $scope.typeOfAxis = [];
                    $scope.isTemporalAxis = [];
                    var coverageIds = [];
                    coverageIds.push($scope.selectedCoverageId);
                    var describeCoverageRequest = new wcs.DescribeCoverage(coverageIds);
                    var numberOfAxis = $scope.coverageDescription.boundedBy.envelope.lowerCorner.values.length;
                    var rawCoverageDescription;
                    var regularAxis = 'regular';
                    var irregularAxis = 'irregular';
                    for (var i = 0; i < numberOfAxis; ++i) {
                        var el = +$scope.coverageDescription.boundedBy.envelope.upperCorner.values[i];
                        if (isNaN(el)) {
                            $scope.isTemporalAxis[i] = true;
                        }
                        else {
                            $scope.isTemporalAxis[i] = false;
                        }
                    }
                    wcsService.getCoverageDescription(describeCoverageRequest)
                        .then(function (response) {
                        $scope.coverageDescriptionsDocument = response.document;
                        rawCoverageDescription = $scope.coverageDescriptionsDocument.value;
                        var startPos = rawCoverageDescription.indexOf("<gmlrgrid:coefficients>");
                        var endPos;
                        if (startPos != -1) {
                            for (var it1 = 0; it1 < numberOfAxis; ++it1) {
                                startPos = 0;
                                $("#sliceIrrValues" + it1).empty();
                                $("#trimmIrrValuesMin" + it1).empty();
                                $("#trimmIrrValuesMax" + it1).empty();
                                for (var it2 = 0; it2 <= it1; ++it2) {
                                    startPos = rawCoverageDescription.indexOf("<gmlrgrid:generalGridAxis>", startPos);
                                    startPos = rawCoverageDescription.indexOf(">", startPos + 1);
                                    endPos = rawCoverageDescription.indexOf("</gmlrgrid:generalGridAxis>", startPos);
                                }
                                startPos = rawCoverageDescription.indexOf("<gmlrgrid:coefficients>", startPos);
                                if (startPos != -1 && startPos < endPos) {
                                    $scope.typeOfAxis.push(irregularAxis);
                                    endPos = rawCoverageDescription.indexOf("</gmlrgrid:coefficients>", startPos);
                                    startPos = rawCoverageDescription.indexOf(">", startPos + 1);
                                    startPos++;
                                    var rawIrrElements = rawCoverageDescription.substring(startPos, endPos);
                                    var st = rawIrrElements.indexOf(' ');
                                    var element;
                                    var noEl = 0;
                                    while (st != -1) {
                                        var element = rawIrrElements.substring(0, st);
                                        $("#sliceIrrValues" + it1).append($('<option id="' + noEl + '"/>').attr("value", element));
                                        $("#trimmIrrValuesMin" + it1).append($('<option id="' + noEl + '"/>').attr("value", element));
                                        $("#trimmIrrValuesMax" + it1).append($('<option id="' + noEl + '"/>').attr("value", element));
                                        rawIrrElements = rawIrrElements.substring(st + 1, rawIrrElements.length);
                                        st = rawIrrElements.indexOf(' ');
                                        noEl++;
                                    }
                                    element = rawIrrElements;
                                    $("#trimmIrrValuesMin" + it1).append($('<option id="' + noEl + '"/>').attr("value", element));
                                    $("#trimmIrrValuesMax" + it1).append($('<option id="' + noEl + '"/>').attr("value", element));
                                    $("#sliceIrrValues" + it1).append($('<option id="' + noEl + '"/>').attr("value", element));
                                }
                                else {
                                    $scope.typeOfAxis.push(regularAxis);
                                }
                            }
                        }
                        else {
                            for (var it = 0; it < numberOfAxis; ++it) {
                                $scope.typeOfAxis.push(regularAxis);
                            }
                        }
                        for (var i = 0; i < $scope.typeOfAxis.length; i++) {
                            if ($scope.typeOfAxis[i] == irregularAxis) {
                                var trimLow = $scope.core.trims[i].trimLow;
                                var trimHigh = $scope.core.trims[i].trimHigh;
                                $("#trimmIrrMin" + i).val(trimLow);
                                $("#trimmIrrMax" + i).val(trimHigh);
                                var slicePoint = $scope.core.slices[i].slicePoint;
                                $("#sliceIrr" + i).val(slicePoint);
                            }
                        }
                    });
                    $scope.getCoverageTabStates = {
                        isCoreOpen: true,
                        isRangeSubsettingOpen: false,
                        isRangeSubsettingSupported: WCSGetCoverageController.isRangeSubsettingSupported($scope.wcsStateInformation.serverCapabilities),
                        isScalingOpen: false,
                        isScalingSupported: WCSGetCoverageController.isScalingSupported($scope.wcsStateInformation.serverCapabilities),
                        isInterpolationOpen: false,
                        isInterpolationSupported: WCSGetCoverageController.isInterpolationSupported($scope.wcsStateInformation.serverCapabilities),
                        isCRSOpen: false,
                        isCRSSupported: WCSGetCoverageController.isCRSSupported($scope.wcsStateInformation.serverCapabilities),
                        isClippingOpen: false,
                        isClippingSupported: true
                    };
                    $scope.core = {
                        slices: [],
                        trims: [],
                        isTrimSelected: [],
                        isMultiPartFormat: false,
                        selectedCoverageFormat: $scope.setOutputFormat(numberOfAxis),
                        requestUrl: null
                    };
                    for (var i = 0; i < numberOfAxis; ++i) {
                        var dimension = $scope.coverageDescription.boundedBy.envelope.axisLabels[i];
                        var min = $scope.coverageDescription.boundedBy.envelope.lowerCorner.values[i];
                        var max = $scope.coverageDescription.boundedBy.envelope.upperCorner.values[i];
                        $scope.core.slices.push(new wcs.DimensionSlice(dimension, min + ""));
                        $scope.core.trims.push(new wcs.DimensionTrim(dimension, min + "", max + ""));
                        $scope.core.isTrimSelected.push(true);
                    }
                    if ($scope.getCoverageTabStates.isRangeSubsettingSupported) {
                        $scope.rangeSubsettingExtension = new rasdaman.RangeSubsettingModel($scope.coverageDescription);
                    }
                    if ($scope.getCoverageTabStates.isScalingSupported) {
                        $scope.scalingExtension = new rasdaman.WCSScalingExtensionModel($scope.coverageDescription);
                    }
                    if ($scope.getCoverageTabStates.isInterpolationSupported) {
                        $scope.interpolationExtension = new rasdaman.WCSInterpolationExtensionModel($scope.wcsStateInformation.serverCapabilities);
                    }
                    if ($scope.getCoverageTabStates.isCRSSupported) {
                        $scope.crsExtension = new rasdaman.WCSCRSExtensionModel($scope.wcsStateInformation.serverCapabilities);
                    }
                    if ($scope.getCoverageTabStates.isClippingSupported) {
                        $scope.clippingExtension = new rasdaman.WCSClippingExtensionModel($scope.wcsStateInformation.serverCapabilities);
                    }
                    $scope.typeOfInputIsNotValid = function (isTemporalAxis, value) {
                        if (isTemporalAxis) {
                            value = value.substr(1, value.length - 2);
                            value = new Date(value);
                            if (isNaN(value.getTime())) {
                                return true;
                            }
                        }
                        else {
                            if (isNaN(value)) {
                                return true;
                            }
                        }
                        return false;
                    };
                    $scope.trimValidator = function (i, min, max) {
                        $scope.core.trims[i].trimLowNotValid = false;
                        $scope.core.trims[i].trimHighNotValid = false;
                        $scope.core.trims[i].trimLowerUpperBoundNotInOrder = false;
                        $scope.core.trims[i].typeOfTrimUpperNotValidDate = false;
                        $scope.core.trims[i].typeOfTrimUpperNotValidNumber = false;
                        $scope.core.trims[i].typeOfTrimLowerNotValidDate = false;
                        $scope.core.trims[i].typeOfTrimLowerNotValidNumber = false;
                        var minTrimSelected;
                        var maxTrimSelected;
                        if ($scope.typeOfInputIsNotValid($scope.isTemporalAxis[i], $scope.core.trims[i].trimLow)) {
                            if ($scope.isTemporalAxis[i]) {
                                $scope.core.trims[i].typeOfTrimLowerNotValidDate = true;
                            }
                            else {
                                $scope.core.trims[i].typeOfTrimLowerNotValidNumber = true;
                            }
                        }
                        if ($scope.typeOfInputIsNotValid($scope.isTemporalAxis[i], $scope.core.trims[i].trimHigh)) {
                            if ($scope.isTemporalAxis[i]) {
                                $scope.core.trims[i].typeOfTrimUpperNotValidDate = true;
                            }
                            else {
                                $scope.core.trims[i].typeOfTrimUpperNotValidNumber = true;
                            }
                        }
                        if ($scope.isTemporalAxis[i]) {
                            minTrimSelected = $scope.core.trims[i].trimLow;
                            minTrimSelected = minTrimSelected.substr(1, minTrimSelected.length - 2);
                            minTrimSelected = new Date(minTrimSelected);
                            maxTrimSelected = $scope.core.trims[i].trimHigh;
                            maxTrimSelected = maxTrimSelected.substr(1, maxTrimSelected.length - 2);
                            maxTrimSelected = new Date(maxTrimSelected);
                        }
                        else {
                            minTrimSelected = +$scope.core.trims[i].trimLow;
                            maxTrimSelected = +$scope.core.trims[i].trimHigh;
                        }
                        if (minTrimSelected < min) {
                            $scope.core.trims[i].trimLowNotValid = true;
                        }
                        if (maxTrimSelected > max) {
                            $scope.core.trims[i].trimHighNotValid = true;
                        }
                        if (minTrimSelected > maxTrimSelected) {
                            $scope.core.trims[i].trimLowerUpperBoundNotInOrder = true;
                        }
                    };
                    $scope.sliceValidator = function (i, min, max) {
                        $scope.core.slices[i].sliceRegularNotValid = false;
                        $scope.core.slices[i].typeOfSliceNotValidDate = false;
                        $scope.core.slices[i].typeOfSliceNotValidNumber = false;
                        var sliceSelected;
                        if ($scope.typeOfInputIsNotValid($scope.isTemporalAxis[i], $scope.core.slices[i].slicePoint)) {
                            if ($scope.isTemporalAxis[i]) {
                                $scope.core.slices[i].typeOfSliceNotValidDate = true;
                            }
                            else {
                                $scope.core.slices[i].typeOfSliceNotValidNumber = true;
                            }
                        }
                        if ($scope.isTemporalAxis[i]) {
                            sliceSelected = $scope.core.slices[i].slicePoint;
                            sliceSelected = sliceSelected.substr(1, sliceSelected.length - 2);
                            sliceSelected = new Date(sliceSelected);
                        }
                        else {
                            sliceSelected = +$scope.core.slices[i].slicePoint;
                        }
                        if (sliceSelected < min || sliceSelected > max) {
                            $scope.core.slices[i].sliceRegularNotValid = true;
                        }
                    };
                    $scope.inputValidator = function (i) {
                        var min;
                        var max;
                        min = +$scope.coverageDescription.boundedBy.envelope.lowerCorner.values[i];
                        max = +$scope.coverageDescription.boundedBy.envelope.upperCorner.values[i];
                        if ($scope.isTemporalAxis[i]) {
                            min = $scope.coverageDescription.boundedBy.envelope.lowerCorner.values[i];
                            min = min.substr(1, min.length - 2);
                            min = new Date(min);
                            max = $scope.coverageDescription.boundedBy.envelope.upperCorner.values[i];
                            max = max.substr(1, max.length - 2);
                            max = new Date(max);
                        }
                        if ($scope.core.isTrimSelected[i]) {
                            $scope.trimValidator(i, min, max);
                        }
                        else {
                            $scope.sliceValidator(i, min, max);
                        }
                    };
                    $scope.selectSliceIrregular = function (i) {
                        $scope.core.slices[i].typeOfSliceNotValidDate = false;
                        $scope.core.slices[i].typeOfSliceNotValidNumber = false;
                        var id = "#sliceIrr" + i;
                        var selectedValue = $(id).val();
                        if ($scope.typeOfInputIsNotValid($scope.isTemporalAxis[i], selectedValue)) {
                            if ($scope.isTemporalAxis[i]) {
                                $scope.core.slices[i].typeOfSliceNotValidDate = true;
                            }
                            else {
                                $scope.core.slices[i].typeOfSliceNotValidNumber = true;
                            }
                        }
                        $scope.core.slices[i].slicePoint = selectedValue;
                    };
                    var operationLess = function (a, b) {
                        return a < b;
                    };
                    var operationMore = function (a, b) {
                        return a > b;
                    };
                    $scope.selectTrimIrregularMin = function (i) {
                        $scope.core.trims[i].typeOfTrimLowerNotValidDate = false;
                        $scope.core.trims[i].typeOfTrimLowerNotValidNumber = false;
                        var id = "#trimmIrrMin" + i;
                        var selectedValue = $(id).val();
                        if ($scope.typeOfInputIsNotValid($scope.isTemporalAxis[i], selectedValue)) {
                            if ($scope.isTemporalAxis[i]) {
                                $scope.core.trims[i].typeOfTrimLowerNotValidDate = true;
                            }
                            else {
                                $scope.core.trims[i].typeOfTrimLowerNotValidNumber = true;
                            }
                        }
                        console.log(selectedValue);
                        $scope.core.trims[i].trimLow = selectedValue;
                        $scope.disableUnwantedValues("#trimmIrrValuesMin" + i, '#trimmIrrValuesMax' + i, selectedValue, operationLess);
                    };
                    $scope.selectTrimIrregularMax = function (i) {
                        $scope.core.trims[i].typeOfTrimUpperNotValidDate = false;
                        $scope.core.trims[i].typeOfTrimUpperNotValidNumber = false;
                        var id = "#trimmIrrMax" + i;
                        var selectedValue = $(id).val();
                        if ($scope.typeOfInputIsNotValid($scope.isTemporalAxis[i], selectedValue)) {
                            if ($scope.isTemporalAxis[i]) {
                                $scope.core.trims[i].typeOfTrimUpperNotValidDate = true;
                            }
                            else {
                                $scope.core.trims[i].typeOfTrimUpperNotValidNumber = true;
                            }
                        }
                        $scope.core.trims[i].trimHigh = selectedValue;
                        $scope.disableUnwantedValues("#trimmIrrValuesMax" + i, '#trimmIrrValuesMin' + i, selectedValue, operationMore);
                    };
                    $scope.disableUnwantedValues = function (firstId, secondId, selectedValue, op) {
                        var id = firstId;
                        var idSelectedOption;
                        var idOptionSecondSelect;
                        var wrongSelection = false;
                        idSelectedOption = $(id).find("option[value='" + selectedValue + "']").attr("id");
                        idSelectedOption = +idSelectedOption;
                        $(secondId).find('option').each(function () {
                            idOptionSecondSelect = +$(this).attr("id");
                            if (op(idOptionSecondSelect, idSelectedOption)) {
                                if ($(this).prop('selected') == true) {
                                    wrongSelection = true;
                                }
                                $(this).prop('disabled', true);
                            }
                            else {
                                $(this).removeAttr('disabled');
                                if (wrongSelection == true) {
                                    $(this).prop('selected', true);
                                }
                            }
                        });
                    };
                    $scope.loadCoverageExtentOnGlobe();
                }
            });
        }
        WCSGetCoverageController.isRangeSubsettingSupported = function (serverCapabilities) {
            return serverCapabilities.serviceIdentification.profile.indexOf(rasdaman.Constants.RANGE_SUBSETTING_EXT_URI) != -1;
        };
        WCSGetCoverageController.isScalingSupported = function (serverCapabilities) {
            return serverCapabilities.serviceIdentification.profile.indexOf(rasdaman.Constants.SCALING_EXT_URI) != -1;
        };
        WCSGetCoverageController.isInterpolationSupported = function (serverCapabilities) {
            return serverCapabilities.serviceIdentification.profile.indexOf(rasdaman.Constants.INTERPOLATION_EXT_URI) != -1;
        };
        WCSGetCoverageController.isCRSSupported = function (serverCapabilities) {
            return serverCapabilities.serviceIdentification.profile.indexOf(rasdaman.Constants.CRS_EXT_URI) != -1;
        };
        WCSGetCoverageController.$inject = [
            "$http",
            "$scope",
            "$rootScope",
            "$log",
            "rasdaman.WCSService",
            "Notification",
            "rasdaman.WebWorldWindService"
        ];
        return WCSGetCoverageController;
    }());
    rasdaman.WCSGetCoverageController = WCSGetCoverageController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WidgetConfiguration = (function () {
        function WidgetConfiguration(type, parameters) {
            this.type = type;
            this.parameters = parameters;
        }
        return WidgetConfiguration;
    }());
    rasdaman.WidgetConfiguration = WidgetConfiguration;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WCPSCommand = (function () {
        function WCPSCommand(command) {
            this.widgetParameters = [];
            rasdaman.common.ArgumentValidator.isNotNull(command, "command");
            if (command.indexOf(">>") == -1) {
                this.widgetConfiguration = null;
                this.query = command;
            }
            else {
                var commandParts = command.split(">>");
                var widget = {
                    type: commandParts[0],
                    parameters: null
                };
                if (commandParts[0].indexOf("(") != -1) {
                    var widgetParams = commandParts[0].substring(commandParts[0].indexOf("(") + 1, commandParts[0].indexOf(")")).split(",");
                    this.widgetParameters = widgetParams;
                    var params = {};
                    widgetParams.forEach(function (param) {
                        var parts = param.split("=");
                        params[parts[0]] = parts[1];
                    });
                    widget.type = commandParts[0].substring(0, commandParts[0].indexOf("("));
                    widget.parameters = params;
                }
                this.widgetConfiguration = widget;
                this.query = commandParts[1];
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
            this.command = command;
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
            _this.data = data.toString();
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
            _this.base64ImageData = rasdaman.common.ImageUtilities.arrayBufferToBase64(rawImageData);
            _this.imageType = (command.query.search(/jpeg/g) === -1 ? "image/png" : "image/jpeg");
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
            if (command.widgetConfiguration.parameters && command.widgetConfiguration.parameters.type) {
                diagramType = command.widgetConfiguration.parameters.type;
            }
            _this.diagramOptions = {
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
            data = data.replace(/"/g, "");
            if (data.indexOf("[") !== -1) {
                data = data.substr(1, data.length - 2);
            }
            if (data.includes(" ")) {
                data = data.replace(/ /g, ",");
            }
            var rawData = JSON.parse("[" + data + "]");
            var processedValues = [];
            for (var i = 0; i < rawData.length; ++i) {
                processedValues.push({
                    x: i,
                    y: rawData[i]
                });
            }
            _this.diagramData = [
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
    var WebWorldWindWCPSResult = (function (_super) {
        __extends(WebWorldWindWCPSResult, _super);
        function WebWorldWindWCPSResult(command, rawImageData) {
            var _this = _super.call(this, command) || this;
            _this.minLat = -90;
            _this.minLong = -180;
            _this.maxLat = 90;
            _this.maxLong = 180;
            _this.base64ImageData = rasdaman.common.ImageUtilities.arrayBufferToBase64(rawImageData);
            _this.imageType = (command.query.search(/jpeg/g) === -1 ? "image/png" : "image/jpeg");
            if (command.widgetParameters.length > 0) {
                _this.minLat = parseFloat(command.widgetParameters[0]);
                _this.minLong = parseFloat(command.widgetParameters[1]);
                _this.maxLat = parseFloat(command.widgetParameters[2]);
                _this.maxLong = parseFloat(command.widgetParameters[3]);
            }
            return _this;
        }
        return WebWorldWindWCPSResult;
    }(rasdaman.WCPSQueryResult));
    rasdaman.WebWorldWindWCPSResult = WebWorldWindWCPSResult;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var NotificationWCPSResult = (function (_super) {
        __extends(NotificationWCPSResult, _super);
        function NotificationWCPSResult(command, data) {
            var _this = _super.call(this, command) || this;
            _this.data = data.toString();
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
            var validationResult = this.validateResult(errorHandlingService, command, mimeType);
            if (command.widgetConfiguration == null) {
                var blob = new Blob([data], { type: "application/octet-stream" });
                saveAs(blob, fileName);
                return null;
            }
            else if (command.widgetConfiguration.type == "diagram") {
                if (validationResult == null) {
                    return new rasdaman.DiagramWCPSResult(command, data);
                }
                else {
                    return new rasdaman.NotificationWCPSResult(command, validationResult);
                }
            }
            else if (command.widgetConfiguration.type == "image") {
                if (validationResult == null) {
                    return new rasdaman.ImageWCPSResult(command, data);
                }
                else {
                    return new rasdaman.NotificationWCPSResult(command, validationResult);
                }
            }
            else if (command.widgetConfiguration.type == "wwd") {
                if (validationResult == null) {
                    return new rasdaman.WebWorldWindWCPSResult(command, data);
                }
                else {
                    return new rasdaman.NotificationWCPSResult(command, validationResult);
                }
            }
            else if (command.widgetConfiguration.type == "text") {
                if (validationResult == null) {
                    return new rasdaman.RawWCPSResult(command, data);
                }
                else {
                    return new rasdaman.NotificationWCPSResult(command, validationResult);
                }
            }
            else {
                errorHandlingService.notificationService.error("The input widget: " + command.widgetConfiguration.type + " does not exist");
            }
        };
        WCPSResultFactory.validateResult = function (errorHandlingService, command, mimeType) {
            var errorMessage = null;
            if (command.widgetConfiguration == null) {
                return errorMessage;
            }
            var widgetType = command.widgetConfiguration.type;
            if (widgetType == "diagram" && !(mimeType == "application/json" || mimeType == "text/plain" || mimeType == "text/csv")) {
                errorMessage = "Diagram widget can only be used with encoding 1D result in json or csv.";
                errorHandlingService.notificationService.error(errorMessage);
            }
            else if (widgetType == "image" && !(mimeType == "image/png" || mimeType == "image/jpeg")) {
                errorMessage = "Image widget can only be used with encoding 2D result in png or jpeg.";
                errorHandlingService.notificationService.error(errorMessage);
            }
            else if (widgetType == "wwd" && !(mimeType == "image/png" || mimeType == "image/jpeg")) {
                errorMessage = "WebWorldWind widget can only be used with encoding 2D result in png or jpeg.";
                errorHandlingService.notificationService.error(errorMessage);
            }
            else if (widgetType == "wwd" && command.widgetParameters.length > 0) {
                if (command.widgetParameters.length != 4) {
                    errorMessage = "WebWorldWind widget with input parameters needs to follow this pattern: wwd(MIN_LAT,MIN_LONG,MAX_LAT,MAX_LONG).";
                    errorHandlingService.notificationService.error(errorMessage);
                }
                else {
                    var minLat = parseFloat(command.widgetParameters[0]);
                    var minLong = parseFloat(command.widgetParameters[1]);
                    var maxLat = parseFloat(command.widgetParameters[2]);
                    var maxLong = parseFloat(command.widgetParameters[3]);
                    if (minLat < -90 || minLat > 90) {
                        errorMessage = "WebWorldWind widget min Lat value is not within (-90:90), given: " + minLat + ".";
                        errorHandlingService.notificationService.error(errorMessage);
                    }
                    else if (minLong < -180 || minLat > 180) {
                        errorMessage = "WebWorldWind widget min Long value is not within (-180:180), given: " + minLong + ".";
                        errorHandlingService.notificationService.error(errorMessage);
                    }
                    else if (maxLat < -90 || maxLat > 90) {
                        errorMessage = "WebWorldWind widget max Lat value is not within (-90:90), given: " + maxLat + ".";
                        errorHandlingService.notificationService.error(errorMessage);
                    }
                    else if (maxLong < -180 || maxLong > 180) {
                        errorMessage = "WebWorldWind widget max Long value is not within (-180:180), given: " + maxLong + ".";
                        errorHandlingService.notificationService.error(errorMessage);
                    }
                    else if (minLat > maxLat) {
                        errorMessage = "WebWorldWind widget min Lat cannot greater than max Lat, given: minLat: " + minLat + ", maxLat: " + maxLat + ".";
                        errorHandlingService.notificationService.error(errorMessage);
                    }
                    else if (minLong > maxLong) {
                        errorMessage = "WebWorldWind widget min Long cannot greater than max Long, given: minLong: " + minLong + ", maxLong: " + maxLong + ".";
                        errorHandlingService.notificationService.error(errorMessage);
                    }
                }
            }
            return errorMessage;
        };
        return WCPSResultFactory;
    }());
    rasdaman.WCPSResultFactory = WCPSResultFactory;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WCSProcessCoverageController = (function () {
        function WCSProcessCoverageController($scope, $log, $interval, notificationService, wcsService, errorHandlingService) {
            $scope.editorOptions = {
                extraKeys: { "Ctrl-Space": "autocomplete" },
                mode: "xquery",
                theme: "eclipse",
                lineNumbers: false
            };
            $scope.editorData = [];
            $scope.availableQueries = WCSProcessCoverageController.createExampleQueries();
            $scope.query = $scope.availableQueries[0].query;
            $scope.selectedQuery = $scope.availableQueries[0].query;
            $scope.$watch("selectedQuery", function (newValue, oldValue) {
                $scope.query = newValue;
            });
            $scope.$watch("selectedHistoryQuery", function (newValue, oldValue) {
                $scope.query = newValue;
            });
            $scope.clearHistory = function () {
                var thisQuery;
                thisQuery = { query: '', title: '--Select a WCPS query---' };
                $scope.historyOfQueries = [];
                $scope.historyOfQueries.unshift(thisQuery);
            };
            $scope.clearHistory();
            var addToHistory = function () {
                var thisQuery;
                var thisTitle;
                var NUMBER_OF_ELEMENTS_IN_HISTORY = 25;
                var NUMBER_CHARACTERS_IN_QUERY_TITLE = 20;
                thisTitle = new Date();
                thisTitle = thisTitle.toISOString();
                thisTitle = thisTitle + ' ' + $scope.query.substr(0, NUMBER_CHARACTERS_IN_QUERY_TITLE) + '...';
                thisQuery = { query: $scope.query, title: thisTitle };
                $scope.historyOfQueries.splice(0, 1, thisQuery);
                for (var it = 1; it < $scope.historyOfQueries.length; it++) {
                    if ($scope.historyOfQueries[it].query == thisQuery.query) {
                        $scope.historyOfQueries.splice(it, 1);
                    }
                }
                thisQuery = { query: '', title: '--Select a WCPS query---' };
                $scope.historyOfQueries.unshift(thisQuery);
                if ($scope.historyOfQueries.length > NUMBER_OF_ELEMENTS_IN_HISTORY) {
                    $scope.historyOfQueries.splice(-1, 1);
                }
            };
            $scope.executeQuery = function () {
                try {
                    if ($scope.query == '' || $scope.query == null) {
                        notificationService.error("WCPS query cannot be empty");
                    }
                    else {
                        var command = new rasdaman.WCPSCommand($scope.query);
                        var waitingForResults = new WaitingForResult();
                        $scope.editorData.push(waitingForResults);
                        var indexOfResults = $scope.editorData.length - 1;
                        $scope.editorData[indexOfResults].query = $scope.query;
                        $scope.editorData[indexOfResults].finished = false;
                        var waitingForResultsPromise = $interval(function () {
                            $scope.editorData[indexOfResults].secondsPassed++;
                        }, 1000);
                        wcsService.processCoverages(command.query)
                            .then(function (data) {
                            var editorRow = rasdaman.WCPSResultFactory.getResult(errorHandlingService, command, data.data, data.headers('Content-Type'), data.headers('File-name'));
                            if (editorRow instanceof rasdaman.NotificationWCPSResult) {
                                $scope.editorData.push(new rasdaman.NotificationWCPSResult(command, "Error when validating the WCPS query. Reason: " + editorRow.data));
                            }
                            else if (editorRow != null) {
                                $scope.editorData.push(editorRow);
                                addToHistory();
                            }
                            else {
                                $scope.editorData.push(new rasdaman.NotificationWCPSResult(command, "Downloading WCPS query's result as a file to Web Browser."));
                                addToHistory();
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
                            $scope.editorData.push(new rasdaman.NotificationWCPSResult(command, "Cannot execute the requested WCPS query, error '" + args[0].data + "'."));
                        })["finally"](function () {
                            $scope.editorData[indexOfResults].finished = true;
                            $interval.cancel(waitingForResultsPromise);
                        });
                    }
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
                else if (datum instanceof rasdaman.WebWorldWindWCPSResult) {
                    return 5;
                }
                return -1;
            };
        }
        WCSProcessCoverageController.createExampleQueries = function () {
            return [
                {
                    title: '-- Select a WCPS query --',
                    query: ''
                }, {
                    title: 'No encoding',
                    query: 'for $c in (mean_summer_airtemp) return avg($c)'
                }, {
                    title: 'Encode 2D as png with image widget',
                    query: 'image>>for $c in (mean_summer_airtemp) return encode($c, "png")'
                }, {
                    title: 'Encode 2D as tiff',
                    query: 'for $c in (mean_summer_airtemp) return encode($c, "tiff")'
                }, {
                    title: 'Encode 2D as netCDF',
                    query: 'for $c in (mean_summer_airtemp) return encode($c, "application/netcdf")'
                }, {
                    title: 'Encode 1D as csv with diagram widget',
                    query: 'diagram>>for $c in (mean_summer_airtemp) return encode($c[Lat(-20)], "text/csv")'
                }, {
                    title: 'Encode 1D as json with diagram widget',
                    query: 'diagram>>for $c in (mean_summer_airtemp) return encode($c[Lat(-20)], "application/json")'
                }, {
                    title: 'Encode 2D as gml',
                    query: 'for $c in (mean_summer_airtemp) return encode($c[Lat(-43.525:-42.5), Long(112.5:113.5)], "application/gml+xml")'
                }, {
                    title: 'Encode 2D as png with WebWorldWind (wwd) widget ',
                    query: 'wwd(-44.525,111.975,-8.975,156.275)>>for $c in (mean_summer_airtemp) return encode($c, "png")'
                }
            ];
        };
        WCSProcessCoverageController.$inject = [
            "$scope",
            "$log",
            "$interval",
            "Notification",
            "rasdaman.WCSService",
            "rasdaman.ErrorHandlingService"
        ];
        return WCSProcessCoverageController;
    }());
    rasdaman.WCSProcessCoverageController = WCSProcessCoverageController;
    var WaitingForResult = (function () {
        function WaitingForResult() {
            this.secondsPassed = 0;
        }
        return WaitingForResult;
    }());
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    function WCSRangeSubsettingExtension() {
        return {
            require: "ngModel",
            scope: {
                model: "=ngModel"
            },
            templateUrl: "ows/src/components/wcs_component/range_subsetting_ext/RangeSubsettingTemplate.html"
        };
    }
    rasdaman.WCSRangeSubsettingExtension = WCSRangeSubsettingExtension;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var RangeSubsettingModel = (function () {
        function RangeSubsettingModel(coverageDescription) {
            var _this = this;
            this.rangeSubset = new wcs.RangeSubset();
            this.availableRanges = [];
            this.isIntervals = [];
            this.isMaxRanges = false;
            coverageDescription.rangeType.dataRecord.fields.forEach(function (field) {
                _this.availableRanges.push(field.name);
            });
        }
        RangeSubsettingModel.prototype.addRangeComponent = function () {
            this.rangeSubset.rangeItem.push(new wcs.RangeComponent(this.availableRanges[0]));
            this.isIntervals.push(false);
            if (this.isIntervals.length == this.availableRanges.length) {
                this.isMaxRanges = true;
            }
            else {
                this.validate();
            }
        };
        RangeSubsettingModel.prototype.addRangeComponentInterval = function () {
            var start = new wcs.RangeComponent(this.availableRanges[0]);
            var end = new wcs.RangeComponent(this.availableRanges[this.availableRanges.length - 1]);
            this.rangeSubset.rangeItem.push(new wcs.RangeInterval(start, end));
            this.isIntervals.push(true);
            if (this.isIntervals.length == this.availableRanges.length) {
                this.isMaxRanges = true;
            }
            else {
                this.validate();
            }
        };
        RangeSubsettingModel.prototype.deleteRangeComponent = function (index) {
            this.rangeSubset.rangeItem.splice(index, 1);
            this.isIntervals.splice(index, 1);
            this.isMaxRanges = false;
            this.validate();
        };
        RangeSubsettingModel.prototype.getIndexByRangeName = function (rangeName) {
            for (var i = 0; i < this.availableRanges.length; i++) {
                if (this.availableRanges[i] == rangeName) {
                    return i;
                }
            }
        };
        RangeSubsettingModel.prototype.getSelectedRangeIndexesByIndex = function (index) {
            var isInterval = this.isIntervals[index];
            var result = [];
            if (!isInterval) {
                var rangeItem = this.rangeSubset.rangeItem[index];
                var rangeName = rangeItem.rangeComponent;
                var rangeIndex = this.getIndexByRangeName(rangeName);
                result.push(rangeIndex, rangeIndex);
            }
            else {
                var rangeItem = this.rangeSubset.rangeItem[index];
                var fromRangeName = rangeItem.startComponent.rangeComponent;
                var endRangeName = rangeItem.endComponent.rangeComponent;
                var fromRangeIndex = this.getIndexByRangeName(fromRangeName);
                var endRangeIndex = this.getIndexByRangeName(endRangeName);
                result.push(fromRangeIndex, endRangeIndex);
            }
            return result;
        };
        RangeSubsettingModel.prototype.getListOfSelectedRangeIndexes = function () {
            var result = [];
            for (var i = 0; i < this.isIntervals.length; i++) {
                var tmpArray = this.getSelectedRangeIndexesByIndex(i);
                result.push(tmpArray);
            }
            return result;
        };
        RangeSubsettingModel.prototype.validateByIndex = function (index) {
            var selectedRangeIndexesNestedArray = this.getListOfSelectedRangeIndexes();
            if (index < this.isIntervals.length) {
                var currentSelectedRangeIndexesArray = this.getSelectedRangeIndexesByIndex(index);
                for (var i = 0; i < selectedRangeIndexesNestedArray.length; i++) {
                    if (i == index) {
                        continue;
                    }
                    var selectedRangeIndexesArray = selectedRangeIndexesNestedArray[i];
                    var currentStartIndex = currentSelectedRangeIndexesArray[0];
                    var currentEndIndex = currentSelectedRangeIndexesArray[1];
                    if (currentStartIndex > currentEndIndex) {
                        this.errorMessage = "Range selector " + (index + 1) + " must have lower range < upper range.";
                        return false;
                    }
                    if ((currentStartIndex >= selectedRangeIndexesArray[0] && currentStartIndex <= selectedRangeIndexesArray[1])
                        || (currentEndIndex >= selectedRangeIndexesArray[0] && currentEndIndex <= selectedRangeIndexesArray[1])) {
                        this.errorMessage = "Range selector " + (index + 1) + " is duplicate or overlapping with Range selector " + (i + 1);
                        return false;
                    }
                }
            }
            return true;
        };
        RangeSubsettingModel.prototype.validate = function () {
            var selectedRangeIndexesNestedArray = this.getListOfSelectedRangeIndexes();
            for (var i = 0; i < this.isIntervals.length; i++) {
                var result = this.validateByIndex(i);
                if (result == false) {
                    return;
                }
            }
            this.errorMessage = "";
        };
        return RangeSubsettingModel;
    }());
    rasdaman.RangeSubsettingModel = RangeSubsettingModel;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WCSScalingExtensionModel = (function () {
        function WCSScalingExtensionModel(coverageDescription) {
            this.coverageDescription = coverageDescription;
            var i = 0;
            var axes = [];
            coverageDescription.boundedBy.envelope.axisLabels.forEach(function (label) {
                axes.push(label);
            });
            this.scaleByFactor = new wcs.ScaleByFactor(WCSScalingExtensionModel.DEFAULT_SCALE_FACTOR);
            var scaleAxis = [];
            for (i = 0; i < axes.length; ++i) {
                scaleAxis.push(new wcs.ScaleAxis(axes[i], WCSScalingExtensionModel.DEFAULT_SCALE_FACTOR));
            }
            this.scaleAxesByFactor = new wcs.ScaleAxesByFactor(scaleAxis);
            var targetAxisSize = [];
            for (i = 0; i < axes.length; ++i) {
                targetAxisSize.push(new wcs.TargetAxisSize(axes[i], WCSScalingExtensionModel.DEFAULT_AXIS_SIZE));
            }
            this.scaleToSize = new wcs.ScaleToSize(targetAxisSize);
            var targetAxisExtent = [];
            for (i = 0; i < axes.length; ++i) {
                var low = coverageDescription.boundedBy.envelope.lowerCorner.values[i];
                var high = coverageDescription.boundedBy.envelope.upperCorner.values[i];
                targetAxisExtent.push(new wcs.TargetAxisExtent(axes[i], low, high));
            }
            this.scaleToExtent = new wcs.ScaleToExtent(targetAxisExtent);
            this.scalingType = 0;
        }
        WCSScalingExtensionModel.prototype.getScaling = function () {
            if (0 == this.scalingType) {
                return this.getScaleByFactor();
            }
            else if (1 == this.scalingType) {
                return this.getScaleAxesByFactor();
            }
            else if (2 == this.scalingType) {
                return this.getScaleToSize();
            }
            else {
                return this.getScaleToExtent();
            }
        };
        WCSScalingExtensionModel.prototype.clearScaling = function () {
            var i = 0;
            this.scaleByFactor.scaleFactor = WCSScalingExtensionModel.DEFAULT_SCALE_FACTOR;
            for (i = 0; i < this.scaleAxesByFactor.scaleAxis.length; ++i) {
                this.scaleAxesByFactor.scaleAxis[i].scaleFactor = WCSScalingExtensionModel.DEFAULT_SCALE_FACTOR;
            }
            for (i = 0; i < this.scaleToSize.targetAxisSize.length; ++i) {
                this.scaleToSize.targetAxisSize[i].targetSize = WCSScalingExtensionModel.DEFAULT_AXIS_SIZE;
            }
            for (i = 0; i < this.scaleToExtent.targetAxisExtent.length; ++i) {
                var low = this.coverageDescription.boundedBy.envelope.lowerCorner.values[i];
                var high = this.coverageDescription.boundedBy.envelope.upperCorner.values[i];
                this.scaleToExtent.targetAxisExtent[i].low = low;
                this.scaleToExtent.targetAxisExtent[i].high = high;
            }
            this.scalingType = 0;
        };
        WCSScalingExtensionModel.prototype.getScaleByFactor = function () {
            if (this.scaleByFactor.scaleFactor != WCSScalingExtensionModel.DEFAULT_SCALE_FACTOR) {
                return this.scaleByFactor;
            }
            else {
                return null;
            }
        };
        WCSScalingExtensionModel.prototype.getScaleAxesByFactor = function () {
            for (var i = 0; i < this.scaleAxesByFactor.scaleAxis.length; ++i) {
                if (this.scaleAxesByFactor.scaleAxis[i].scaleFactor != WCSScalingExtensionModel.DEFAULT_SCALE_FACTOR) {
                    return this.scaleAxesByFactor;
                }
            }
            return null;
        };
        WCSScalingExtensionModel.prototype.getScaleToSize = function () {
            for (var i = 0; i < this.scaleToSize.targetAxisSize.length; ++i) {
                if (this.scaleToSize.targetAxisSize[i].targetSize != WCSScalingExtensionModel.DEFAULT_AXIS_SIZE) {
                    return this.scaleToSize;
                }
            }
            return null;
        };
        WCSScalingExtensionModel.prototype.getScaleToExtent = function () {
            for (var i = 0; i < this.scaleToExtent.targetAxisExtent.length; ++i) {
                var low = this.coverageDescription.boundedBy.envelope.lowerCorner.values[i];
                var high = this.coverageDescription.boundedBy.envelope.upperCorner.values[i];
                if (this.scaleToExtent.targetAxisExtent[i].low != low
                    || this.scaleToExtent.targetAxisExtent[i].high != high) {
                    return this.scaleToExtent;
                }
            }
            return null;
        };
        WCSScalingExtensionModel.DEFAULT_SCALE_FACTOR = 1.0;
        WCSScalingExtensionModel.DEFAULT_AXIS_SIZE = 0.0;
        return WCSScalingExtensionModel;
    }());
    rasdaman.WCSScalingExtensionModel = WCSScalingExtensionModel;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    function WCSScalingExtension() {
        return {
            require: "ngModel",
            scope: {
                model: "=ngModel"
            },
            templateUrl: "ows/src/components/wcs_component/scaling_ext/ScalingExtentionTemplate.html"
        };
    }
    rasdaman.WCSScalingExtension = WCSScalingExtension;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WCSInterpolationExtensionModel = (function () {
        function WCSInterpolationExtensionModel(serverCapabilities) {
            this.availableInterpolationMethods = [];
            for (var i = 0; i < serverCapabilities.serviceMetadata.extension.length; ++i) {
                if (serverCapabilities.serviceMetadata.extension[i].interpolationMetadata) {
                    var arr = serverCapabilities.serviceMetadata.extension[i].interpolationMetadata.interpolationSupported;
                    for (var j = 0; j < arr.length; j++) {
                        var interpolationUri = arr[j];
                        this.availableInterpolationMethods.push({ name: interpolationUri, uri: interpolationUri });
                    }
                }
            }
        }
        WCSInterpolationExtensionModel.prototype.getInterpolation = function () {
            var interpolationUri = "";
            if (this.selectedInterpolationMethod) {
                interpolationUri = this.selectedInterpolationMethod.uri;
            }
            return new wcs.Interpolation(interpolationUri);
        };
        return WCSInterpolationExtensionModel;
    }());
    rasdaman.WCSInterpolationExtensionModel = WCSInterpolationExtensionModel;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    function WCSInterpolationExtension() {
        return {
            require: "ngModel",
            scope: {
                model: "=ngModel"
            },
            templateUrl: "ows/src/components/wcs_component/interpolation_ext/InterpolationExtensionTemplate.html"
        };
    }
    rasdaman.WCSInterpolationExtension = WCSInterpolationExtension;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WCSCRSExtensionModel = (function () {
        function WCSCRSExtensionModel(serverCapabilities) {
        }
        WCSCRSExtensionModel.prototype.getCRS = function () {
            return new wcs.CRS(this.wcsGetCoverageSubsettingCRS, this.wcsGetCoverageOutputCRS);
        };
        return WCSCRSExtensionModel;
    }());
    rasdaman.WCSCRSExtensionModel = WCSCRSExtensionModel;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    function WCSCRSExtension() {
        return {
            require: "ngModel",
            scope: {
                model: "=ngModel"
            },
            templateUrl: "ows/src/components/wcs_component/crs_ext/CRSExtensionTemplate.html"
        };
    }
    rasdaman.WCSCRSExtension = WCSCRSExtension;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WCSClippingExtensionModel = (function () {
        function WCSClippingExtensionModel(serverCapabilities) {
        }
        WCSClippingExtensionModel.prototype.getClipping = function () {
            return new wcs.Clipping(this.wcsGetCoverageClipping);
        };
        return WCSClippingExtensionModel;
    }());
    rasdaman.WCSClippingExtensionModel = WCSClippingExtensionModel;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    function WCSClippingExtension() {
        return {
            require: "ngModel",
            scope: {
                model: "=ngModel"
            },
            templateUrl: "ows/src/components/wcs_component/clipping_ext/ClippingExtensionTemplate.html"
        };
    }
    rasdaman.WCSClippingExtension = WCSClippingExtension;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var RootController = (function () {
        function RootController($http, $q, $scope, $rootScope, $state, settings, errorHandlingService, credentialService) {
            this.$http = $http;
            this.$q = $q;
            this.$scope = $scope;
            this.$rootScope = $rootScope;
            this.$state = $state;
            this.settings = settings;
            this.errorHandlingService = errorHandlingService;
            this.credentialService = credentialService;
            this.initializeViews($scope);
            $rootScope.homeLoggedIn = false;
            $rootScope.usernameLoggedIn = "";
            $rootScope.$watch("homeLoggedIn", function (newValue, oldValue) {
                if (newValue === true) {
                    $scope.showView($scope.wsclient, "services");
                }
            });
            $scope.checkPetascopeEnableAuthentication = function () {
                var result = $q.defer();
                var requestUrl = settings.contextPath + "/admin/authisactive";
                $http.get(requestUrl)
                    .then(function (dataObj) {
                    var data = JSON.parse(dataObj.data);
                    result.resolve(data);
                }, function (errorObj) {
                    if (errorObj.status == 404) {
                        result.resolve(false);
                    }
                    else {
                        errorHandlingService.handleError(errorObj);
                    }
                });
                return result.promise;
            };
            $scope.checkRadamanCredentials = function () {
                var credentialsDict = credentialService.credentialsDict;
                if (credentialsDict != null) {
                    var obj = credentialsDict[settings.wcsEndpoint];
                    if (obj != null) {
                        var credential = new login.Credential(obj["username"], obj["password"]);
                        var requestUrl = settings.contextPath + "/login";
                        $http.get(requestUrl, {
                            headers: credentialService.createBasicAuthenticationHeader(credential.username, credential.password)
                        }).then(function (dataObj) {
                            $rootScope.homeLoggedIn = true;
                            $rootScope.usernameLoggedIn = credential.username;
                            $scope.showView($scope.wsclient, "services");
                            return;
                        }, function (errorObj) {
                            errorHandlingService.handleError(errorObj);
                        });
                    }
                }
                $scope.showView($scope.login, "login");
            };
            $scope.showView = function (viewState, stateName) {
                $scope.selectedView = viewState;
                $state.go(stateName);
            };
            $scope.homeLogOutEvent = function () {
                credentialService.clearStorage();
                $rootScope.homeLoggedIn = false;
                location.reload();
            };
            $scope.checkPetascopeEnableAuthentication()
                .then(function (data) {
                if (data) {
                    $scope.checkRadamanCredentials();
                }
                else {
                    $rootScope.homeLoggedIn = false;
                    $scope.showView($scope.wsclient, "services");
                }
            });
        }
        RootController.prototype.initializeViews = function ($scope) {
            $scope.login = {
                view: "login"
            };
            $scope.wsclient = {
                view: "wsclient"
            };
        };
        RootController.$inject = ["$http", "$q", "$scope", "$rootScope",
            "$state", "rasdaman.WCSSettingsService", "rasdaman.ErrorHandlingService",
            "rasdaman.CredentialService"
        ];
        return RootController;
    }());
    rasdaman.RootController = RootController;
})(rasdaman || (rasdaman = {}));
var wms;
(function (wms) {
    var ServiceIdentification = (function () {
        function ServiceIdentification(title, abstract) {
            this.serviceType = "OGC WMS";
            this.serviceTypeVersion = rasdaman.WMSSettingsService.version;
            this.title = title;
            this.abstract = abstract;
        }
        return ServiceIdentification;
    }());
    wms.ServiceIdentification = ServiceIdentification;
})(wms || (wms = {}));
var wms;
(function (wms) {
    var Capabilities = (function () {
        function Capabilities(source, gmlDocument) {
            var _this = this;
            this.gmlDocument = gmlDocument;
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            if (source.doesElementExist("Service")) {
                var serviceObj = source.getChildAsSerializedObject("Service");
                var title = serviceObj.getChildAsSerializedObject("Title").getValueAsString();
                var abstract = serviceObj.getChildAsSerializedObject("Abstract").getValueAsString();
                this.serviceIdentification = new wms.ServiceIdentification(title, abstract);
                var onlineResourceObj = serviceObj.getChildAsSerializedObject("OnlineResource");
                var contactInformationObj = serviceObj.getChildAsSerializedObject("ContactInformation");
                var contactPersonPrimaryObj = contactInformationObj.getChildAsSerializedObject("ContactPersonPrimary");
                var contactAdressObj = contactInformationObj.getChildAsSerializedObject("ContactAddress");
                var providerName = contactPersonPrimaryObj.getChildAsSerializedObject("ContactOrganization").getValueAsString();
                var providerSite = onlineResourceObj.getAttributeAsString("href");
                var contactPersion = contactPersonPrimaryObj.getChildAsSerializedObject("ContactPerson").getValueAsString();
                var positionName = contactInformationObj.getChildAsSerializedObject("ContactPosition").getValueAsString();
                var email = contactInformationObj.getChildAsSerializedObject("ContactElectronicMailAddress").getValueAsString();
                var voicePhone = contactInformationObj.getChildAsSerializedObject("ContactVoiceTelephone").getValueAsString();
                var address = contactAdressObj.getChildAsSerializedObject("Address").getValueAsString();
                var city = contactAdressObj.getChildAsSerializedObject("City").getValueAsString();
                var postCode = contactAdressObj.getChildAsSerializedObject("PostCode").getValueAsString();
                var country = contactAdressObj.getChildAsSerializedObject("Country").getValueAsString();
                this.serviceProvider = new wms.ServiceProvider(providerName, providerSite, contactPersion, positionName, email, voicePhone, address, city, postCode, country);
                var capabilityObj = source.getChildAsSerializedObject("Capability");
                var getMapObj = capabilityObj.getChildAsSerializedObject("Request").getChildAsSerializedObject("GetMap");
                this.getMapFormat = [];
                getMapObj.getChildrenAsSerializedObjects("Format").forEach(function (obj) {
                    _this.getMapFormat.push(obj.getValueAsString());
                });
                var layerObjs = capabilityObj.getChildAsSerializedObject("Layer").getChildrenAsSerializedObjects("Layer");
                this.layers = [];
                var totalLocalLayerSizesInBytes_1 = 0;
                var totalRemoteLayerSizesInBytes_1 = 0;
                var totalLayerSizesInBytes = 0;
                layerObjs.forEach(function (obj) {
                    var name = obj.getChildAsSerializedObject("Name").getValueAsString();
                    var title = obj.getChildAsSerializedObject("Title").getValueAsString();
                    var abstract = obj.getChildAsSerializedObject("Abstract").getValueAsString();
                    var customizedMetadata = _this.parseLayerCustomizedMetadata(obj);
                    if (customizedMetadata != null) {
                        if (customizedMetadata.hostname != null) {
                            _this.showLayerLocationsColumn = true;
                        }
                        if (customizedMetadata.coverageSize != null) {
                            _this.showLayerSizesColumn = true;
                        }
                        if (customizedMetadata.localCoverageSizeInBytes > 0) {
                            totalLocalLayerSizesInBytes_1 += customizedMetadata.localCoverageSizeInBytes;
                        }
                        else {
                            totalRemoteLayerSizesInBytes_1 += customizedMetadata.remoteCoverageSizeInBytes;
                        }
                        if (customizedMetadata.isBlackedList != null) {
                            _this.showBlackListedColumn = true;
                        }
                    }
                    var crs = obj.getChildAsSerializedObject("CRS").getValueAsString();
                    var exBBox = obj.getChildAsSerializedObject("EX_GeographicBoundingBox");
                    var westBoundLongitude = exBBox.getChildAsSerializedObject("westBoundLongitude").getValueAsNumber();
                    var eastBoundLongitude = exBBox.getChildAsSerializedObject("eastBoundLongitude").getValueAsNumber();
                    var southBoundLatitude = exBBox.getChildAsSerializedObject("southBoundLatitude").getValueAsNumber();
                    var northBoundLatitude = exBBox.getChildAsSerializedObject("northBoundLatitude").getValueAsNumber();
                    var bboxObj = obj.getChildAsSerializedObject("BoundingBox");
                    var crs = bboxObj.getAttributeAsString("CRS");
                    var minx = bboxObj.getAttributeAsNumber("minx");
                    var miny = bboxObj.getAttributeAsNumber("miny");
                    var maxx = bboxObj.getAttributeAsNumber("maxx");
                    var maxy = bboxObj.getAttributeAsNumber("maxy");
                    var layerGMLDocument = _this.extractLayerGMLDocument(name);
                    _this.layers.push(new wms.Layer(layerGMLDocument, name, title, abstract, customizedMetadata, westBoundLongitude, eastBoundLongitude, southBoundLatitude, northBoundLatitude, crs, minx, miny, maxx, maxy));
                });
                totalLayerSizesInBytes += totalLocalLayerSizesInBytes_1 + totalRemoteLayerSizesInBytes_1;
                this.totalLocalLayerSizes = ows.CustomizedMetadata.convertNumberOfBytesToHumanReadable(totalLocalLayerSizesInBytes_1);
                this.totalRemoteLayerSizes = ows.CustomizedMetadata.convertNumberOfBytesToHumanReadable(totalRemoteLayerSizesInBytes_1);
                this.totalLayerSizes = ows.CustomizedMetadata.convertNumberOfBytesToHumanReadable(totalLayerSizesInBytes);
                this.numberOfLayers = layerObjs.length.toString();
            }
        }
        Capabilities.prototype.parseLayerCustomizedMetadata = function (source) {
            var childElement = "ows:AdditionalParameters";
            var customizedMetadata = null;
            if (source.doesElementExist(childElement)) {
                customizedMetadata = new ows.CustomizedMetadata(source.getChildAsSerializedObject(childElement));
            }
            return customizedMetadata;
        };
        Capabilities.prototype.extractLayerGMLDocument = function (layerName) {
            var regex = /<Layer \S+[\s\S]*?<\/Layer>/g;
            var match = regex.exec(this.gmlDocument);
            while (match != null) {
                if (match[0].indexOf("<Name>" + layerName + "</Name>") !== -1) {
                    return match[0];
                }
                match = regex.exec(this.gmlDocument);
            }
            return null;
        };
        return Capabilities;
    }());
    wms.Capabilities = Capabilities;
})(wms || (wms = {}));
var rasdaman;
(function (rasdaman) {
    var WMSMainController = (function () {
        function WMSMainController($scope, $rootScope, $state) {
            this.$scope = $scope;
            this.initializeTabs($scope);
            $scope.tabs = [$scope.wmsGetCapabilitiesTab, $scope.wmsDescribeLayerTab];
            $scope.describeLayer = function (layerName) {
                $scope.wmsDescribeLayerTab.active = true;
                $rootScope.wmsSelectedLayerName = layerName;
            };
            $scope.wmsStateInformation = {
                serverCapabilities: null,
                reloadServerCapabilities: true
            };
        }
        WMSMainController.prototype.initializeTabs = function ($scope) {
            $scope.wmsGetCapabilitiesTab = {
                heading: "GetCapabilities",
                view: "wms_get_capabilities",
                active: true,
                disabled: false
            };
            $scope.wmsDescribeLayerTab = {
                heading: "DescribeLayer",
                view: "wms_describe_layer",
                active: false,
                disabled: false
            };
        };
        WMSMainController.$inject = ["$scope", "$rootScope", "$state"];
        return WMSMainController;
    }());
    rasdaman.WMSMainController = WMSMainController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var AdminMainController = (function () {
        function AdminMainController($scope, $rootScope, $state) {
            this.$scope = $scope;
            this.initializeTabs($scope);
            $rootScope.adminStateInformation = {
                loggedIn: false
            };
            $rootScope.loggedIn = false;
            $scope.tabs = [$scope.adminLogin];
            $rootScope.$watch("adminStateInformation.loggedIn", function (newValue, oldValue) {
                if (newValue) {
                    $scope.tabs = [$scope.adminOWSMetadataManagement];
                }
                else {
                    $scope.tabs = [$scope.adminLogin];
                }
            });
        }
        AdminMainController.prototype.initializeTabs = function ($scope) {
            $scope.adminLogin = {
                heading: "Login",
                view: "admin_login",
                active: true,
                disabled: false
            };
            $scope.adminOWSMetadataManagement = {
                heading: "OWS Metadata Management",
                view: "admin_ows_metadata_management",
                active: true,
                disabled: false
            };
        };
        AdminMainController.$inject = ["$scope", "$rootScope", "$state"];
        return AdminMainController;
    }());
    rasdaman.AdminMainController = AdminMainController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var LoginController = (function () {
        function LoginController($http, $q, $scope, $rootScope, $log, wcsSettingsService, wmsSettingsService, alertService, errorHandlingService, credentialService) {
            this.$http = $http;
            this.$q = $q;
            this.$scope = $scope;
            this.$rootScope = $rootScope;
            this.$log = $log;
            this.wcsSettingsService = wcsSettingsService;
            this.wmsSettingsService = wmsSettingsService;
            this.alertService = alertService;
            this.errorHandlingService = errorHandlingService;
            this.credentialService = credentialService;
            $scope.petascopeEndPoint = wcsSettingsService.wcsEndpoint;
            $scope.credential = new login.Credential("", "");
            $scope.login = function () {
                var args = [];
                for (var _i = 0; _i < arguments.length; _i++) {
                    args[_i] = arguments[_i];
                }
                $rootScope.homeLoggedIn = false;
                $scope.displayError = false;
                wcsSettingsService.setWCSEndPoint($scope.petascopeEndPoint);
                wmsSettingsService.setWMSEndPoint($scope.petascopeEndPoint);
                $scope.checkPetascopeEnableAuthentication(wcsSettingsService.contextPath, $scope.credential).then(function (response) {
                    var credential = $scope.credential;
                    credentialService.persitCredential($scope.petascopeEndPoint, credential);
                    $rootScope.homeLoggedIn = true;
                }, function (error) {
                    errorHandlingService.handleError(error);
                });
            };
            $scope.checkPetascopeEnableAuthentication = function (contextPath, credential) {
                var requestUrl = contextPath + "/login";
                var result = $q.defer();
                $http.get(requestUrl, {
                    headers: credentialService.createBasicAuthenticationHeader(credential.username, credential.password)
                }).then(function (dataObj) {
                    $rootScope.usernameLoggedIn = credential.username;
                    result.resolve(dataObj.data);
                }, function (errorObj) {
                    if (errorObj.status == 404) {
                        result.resolve("true");
                    }
                    else {
                        result.reject(errorObj);
                    }
                });
                return result.promise;
            };
        }
        LoginController.$inject = [
            "$http",
            "$q",
            "$scope",
            "$rootScope",
            "$log",
            "rasdaman.WCSSettingsService",
            "rasdaman.WMSSettingsService",
            "Notification",
            "rasdaman.ErrorHandlingService",
            "rasdaman.CredentialService"
        ];
        return LoginController;
    }());
    rasdaman.LoginController = LoginController;
})(rasdaman || (rasdaman = {}));
var wms;
(function (wms) {
    var GetCapabilities = (function () {
        function GetCapabilities() {
        }
        GetCapabilities.prototype.toKVP = function () {
            return "request=" + "GetCapabilities";
        };
        return GetCapabilities;
    }());
    wms.GetCapabilities = GetCapabilities;
})(wms || (wms = {}));
var wms;
(function (wms) {
    var Layer = (function () {
        function Layer(gmlDocument, name, title, abstract, customizedMetadata, westBoundLongitude, eastBoundLongitude, southBoundLatitude, northBoundLatitude, crs, minx, miny, maxx, maxy) {
            this.gmlDocument = gmlDocument;
            this.name = name;
            this.title = title;
            this.abstract = abstract;
            this.customizedMetadata = customizedMetadata;
            this.coverageExtent = new wms.CoverageExtent(name, westBoundLongitude, southBoundLatitude, eastBoundLongitude, northBoundLatitude);
            this.crs = crs;
            this.minx = minx;
            this.miny = miny;
            this.maxx = maxx;
            this.maxy = maxy;
            this.displayFootprint = true;
            this.layerDimensions = [];
            for (var j = 0; j < 2; ++j) {
                this.layerDimensions.push(dimen);
            }
            j = 2;
            var dimen = this.initialiseDimenison();
            while (this.buildDimensionAxisFromGMLDocumet(dimen) != false) {
                this.layerDimensions.push(null);
                this.layerDimensions[j] = dimen;
                var dimen = this.initialiseDimenison();
                dimen.startPos = this.layerDimensions[j].startPos;
                j++;
            }
            this.importedType = "local";
            if (this.customizedMetadata != null && this.customizedMetadata.hostname != null) {
                this.importedType = "remote";
            }
            this.buildStylesFromGMLDocument();
        }
        Layer.prototype.initialiseDimenison = function () {
            return {
                name: '',
                array: [],
                startPos: 0,
                isTemporal: false
            };
        };
        Layer.prototype.buildDimensionAxisFromGMLDocumet = function (dim) {
            var posNameStart = this.gmlDocument.indexOf('<Dimension name="', dim.startPos);
            if (posNameStart != -1) {
                posNameStart += 17;
                var posNameEnd = this.gmlDocument.indexOf('">', posNameStart);
                dim.name = this.gmlDocument.substr(posNameStart, posNameEnd - posNameStart);
                var posElementsStart = posNameEnd + 2;
                var posElementsEnd = this.gmlDocument.indexOf('</Dimension>', posElementsStart);
                dim.startPos = posElementsEnd;
                var rawElementsString = this.gmlDocument.substr(posElementsStart, posElementsEnd - posElementsStart);
                if (rawElementsString[0] == '"') {
                    dim.isTemporal = true;
                    var positionEndMinElement = rawElementsString.indexOf('/');
                    if (positionEndMinElement != -1) {
                        var minElementAsString = rawElementsString.substr(0, positionEndMinElement - 1);
                        minElementAsString = minElementAsString.substr(1, minElementAsString.length);
                        var positionEndMaxElement = rawElementsString.indexOf('/', positionEndMinElement + 1);
                        var maxElementAsString = rawElementsString.substr(positionEndMinElement + 1, positionEndMaxElement - positionEndMinElement - 2);
                        maxElementAsString = maxElementAsString.substr(1, maxElementAsString.length);
                        var stepAsString = rawElementsString.substr(positionEndMaxElement + 1, rawElementsString.length - positionEndMaxElement - 2);
                        var stepAsNumber = +stepAsString;
                        stepAsNumber *= 86400000;
                        var minElementAsDate = new Date(minElementAsString);
                        var maxElementAsDate = new Date(maxElementAsString);
                        for (var i = minElementAsDate; i <= maxElementAsDate; i.setMilliseconds(i.getMilliseconds() + stepAsNumber)) {
                            dim.array.push(i.toISOString());
                        }
                    }
                    else {
                        var startCurrentElement = 1;
                        var endCurrentElement = rawElementsString.indexOf('"', startCurrentElement);
                        endCurrentElement -= 1;
                        while (startCurrentElement < endCurrentElement) {
                            dim.array.push(rawElementsString.substr(startCurrentElement, endCurrentElement - startCurrentElement + 1));
                            startCurrentElement = endCurrentElement + 4;
                            endCurrentElement = rawElementsString.indexOf('"', startCurrentElement);
                            endCurrentElement -= 1;
                        }
                    }
                }
                else {
                    var positionEndMinElement = rawElementsString.indexOf('/');
                    dim.isTemporal = false;
                    if (positionEndMinElement != -1) {
                        var minElementAsString = rawElementsString.substr(0, positionEndMinElement);
                        positionEndMaxElement = rawElementsString.indexOf('/', positionEndMinElement + 1);
                        var maxElementAsString = rawElementsString.substr(positionEndMinElement + 1, positionEndMaxElement - positionEndMinElement - 1);
                        var stepAsString = rawElementsString.substr(positionEndMaxElement + 1, rawElementsString.length - positionEndMaxElement);
                        if (minElementAsString[0] == '-') {
                            minElementAsString = minElementAsString.substr(1, minElementAsString.length);
                            var minElementAsNumber = -minElementAsString;
                        }
                        else {
                            minElementAsNumber = +minElementAsString;
                        }
                        if (maxElementAsString[0] == '-') {
                            maxElementAsString = maxElementAsString.substr(1, maxElementAsString.length);
                            var maxElementAsNumber = -maxElementAsString;
                        }
                        else {
                            maxElementAsNumber = +maxElementAsString;
                        }
                        var rg = /[^a-zA-Z]/g;
                        stepAsString = "" + stepAsString.match(rg);
                        if (stepAsString[0] == '-') {
                            stepAsString = stepAsString.substr(1, stepAsString.length);
                            var stepAsNumber = -stepAsString;
                        }
                        else {
                            stepAsNumber = +stepAsString;
                        }
                        for (var it = minElementAsNumber; it <= maxElementAsNumber; it += stepAsNumber) {
                            dim.array.push(("" + it));
                        }
                    }
                    else {
                        var startCurrentElement = 0;
                        var endCurrentElement = rawElementsString.indexOf(',', startCurrentElement);
                        if (endCurrentElement == -1) {
                            endCurrentElement = rawElementsString.length;
                        }
                        while (startCurrentElement < endCurrentElement) {
                            dim.array.push(rawElementsString.substr(startCurrentElement, endCurrentElement - startCurrentElement));
                            startCurrentElement = endCurrentElement + 1;
                            endCurrentElement = rawElementsString.indexOf(',', startCurrentElement);
                            if (endCurrentElement == -1) {
                                endCurrentElement = rawElementsString.length;
                            }
                        }
                    }
                }
                return true;
            }
            else {
                return false;
            }
        };
        Layer.prototype.buildStylesFromGMLDocument = function () {
            this.styles = [];
            var tmpXML = $.parseXML(this.gmlDocument);
            var totalStyles = $(tmpXML).find("Style").length;
            for (var i = 0; i < totalStyles; i++) {
                var styleXML = $(tmpXML).find("Style").eq(i);
                var name = styleXML.find("Name").text();
                var abstractContent = styleXML.find("Abstract").text();
                var userAbstract = abstractContent.substring(0, abstractContent.indexOf("<rasdaman>")).trim();
                var rasdamanAbstract = abstractContent.substring(abstractContent.indexOf("<rasdaman>"), abstractContent.length).trim();
                var rasdamanXML = $.parseXML(rasdamanAbstract);
                var queryType = "none";
                var query = "";
                if ($(rasdamanXML).find("WcpsQueryFragment").text() != "") {
                    queryType = "wcpsQueryFragment";
                    query = $(rasdamanXML).find("WcpsQueryFragment").text();
                }
                else if ($(rasdamanXML).find("RasqlTransformFragment").text() != "") {
                    queryType = "rasqlTransformFragment";
                    query = $(rasdamanXML).find("RasqlTransformFragment").text();
                }
                query = query.replace(/&amp;lt;/g, "<").replace(/&amp;gt;/g, ">");
                var colorTableType = "";
                var colorTableDefinition = "";
                if ($(rasdamanXML).find("ColorTableType").text() != "") {
                    colorTableType = $(rasdamanXML).find("ColorTableType").text();
                }
                if ($(rasdamanXML).find("ColorTableDefinition").text() != "") {
                    colorTableDefinition = rasdamanAbstract.match(/<ColorTableDefinition>([\s\S]*?)<\/ColorTableDefinition>/im)[1];
                }
                var defaultStyle = false;
                if ($(rasdamanXML).find("default").text() != "") {
                    var text = $(rasdamanXML).find("default").text();
                    defaultStyle = JSON.parse(text);
                }
                var legendGraphicURL = null;
                var legendURL = styleXML.find("LegendURL");
                if (legendURL != null) {
                    legendGraphicURL = legendURL.find("OnlineResource").attr("xlink:href");
                }
                var style = new wms.Style(name, userAbstract, queryType, query, colorTableType, colorTableDefinition, defaultStyle, legendGraphicURL);
                this.styles.push(style);
            }
        };
        return Layer;
    }());
    wms.Layer = Layer;
})(wms || (wms = {}));
var wms;
(function (wms) {
    var ServiceProvider = (function () {
        function ServiceProvider(providerName, providerSite, contactPerson, positionName, email, voicePhone, address, city, postCode, country) {
            this.providerName = providerName;
            this.providerSite = providerSite;
            this.contactPerson = contactPerson;
            this.positionName = positionName;
            this.email = email;
            this.voicePhone = voicePhone;
            this.address = address;
            this.city = city;
            this.postCode = postCode;
            this.country = country;
        }
        return ServiceProvider;
    }());
    wms.ServiceProvider = ServiceProvider;
})(wms || (wms = {}));
var wms;
(function (wms) {
    var BBox = (function () {
        function BBox(xmin, ymin, xmax, ymax) {
            this.xmin = xmin;
            this.ymin = ymin;
            this.xmax = xmax;
            this.ymax = ymax;
        }
        return BBox;
    }());
    wms.BBox = BBox;
})(wms || (wms = {}));
var wms;
(function (wms) {
    var CoverageExtent = (function () {
        function CoverageExtent(coverageId, xmin, ymin, xmax, ymax) {
            this.coverageId = coverageId;
            this.bbox = new wms.BBox(xmin, ymin, xmax, ymax);
            this.displayFootprint = false;
        }
        return CoverageExtent;
    }());
    wms.CoverageExtent = CoverageExtent;
})(wms || (wms = {}));
var rasdaman;
(function (rasdaman) {
    var WMSService = (function () {
        function WMSService($http, $q, settings, wcsSettings, serializedObjectFactory, $window, credentialService, adminService) {
            this.$http = $http;
            this.$q = $q;
            this.settings = settings;
            this.wcsSettings = wcsSettings;
            this.serializedObjectFactory = serializedObjectFactory;
            this.$window = $window;
            this.credentialService = credentialService;
            this.adminService = adminService;
        }
        WMSService.prototype.getServerCapabilities = function (request) {
            var result = this.$q.defer();
            var self = this;
            var requestHeaders = {};
            var credentials = this.adminService.getPersistedAdminUserCredentials();
            if (credentials != null) {
                requestHeaders = this.adminService.getAuthenticationHeaders();
            }
            else {
                requestHeaders = this.credentialService.createRequestHeader(this.settings.wmsEndpoint, {});
            }
            var requestUrl = this.settings.wmsFullEndpoint + "&" + request.toKVP();
            this.$http.get(requestUrl, {
                headers: requestHeaders
            }).then(function (data) {
                try {
                    var gmlDocument = new rasdaman.common.ResponseDocument(data.data, rasdaman.common.ResponseDocumentType.XML);
                    var serializedResponse = self.serializedObjectFactory.getSerializedObject(gmlDocument);
                    var capabilities = new wms.Capabilities(serializedResponse, gmlDocument.value);
                    var response = new rasdaman.common.Response(gmlDocument, capabilities);
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
        WMSService.prototype.insertLayerStyleRequest = function (insertLayerStyle) {
            var result = this.$q.defer();
            var requestUrl = this.settings.adminEndpoint + "/layer/style/add";
            var requestHeaders = this.adminService.getAuthenticationHeaders();
            requestHeaders["Content-Type"] = "application/x-www-form-urlencoded";
            var request = {
                method: 'POST',
                url: requestUrl,
                transformResponse: null,
                headers: requestHeaders,
                data: insertLayerStyle.toKVP()
            };
            this.$http(request).then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WMSService.prototype.updateLayerStyleRequest = function (updateLayerStyle) {
            var result = this.$q.defer();
            var requestUrl = this.settings.adminEndpoint + "/layer/style/update";
            var requestHeaders = this.adminService.getAuthenticationHeaders();
            requestHeaders["Content-Type"] = "application/x-www-form-urlencoded";
            var request = {
                method: 'POST',
                url: requestUrl,
                transformResponse: null,
                headers: requestHeaders,
                data: updateLayerStyle.toKVP()
            };
            this.$http(request).then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WMSService.prototype.deleteLayerStyleRequest = function (request) {
            var result = this.$q.defer();
            var requestUrl = this.settings.adminEndpoint + "/layer/style/remove" + "?" + request.toKVP();
            var requestHeaders = this.adminService.getAuthenticationHeaders();
            this.$http.get(requestUrl, {
                headers: requestHeaders
            }).then(function (data) {
                try {
                    result.resolve("");
                }
                catch (err) {
                    result.reject(err);
                }
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WMSService.prototype.listPyramidMembersRequest = function (request) {
            var result = this.$q.defer();
            var requestUrl = this.wcsSettings.adminEndpoint + "/coverage/pyramid/list" + "?" + request.toKVP();
            var requestHeaders = this.adminService.getAuthenticationHeaders();
            if ($.isEmptyObject(requestHeaders)) {
                var currentHeaders = {};
                requestHeaders = this.credentialService.createRequestHeader(this.wcsSettings.wcsEndpoint, currentHeaders);
            }
            this.$http.get(requestUrl, {
                headers: requestHeaders
            }).then(function (response) {
                try {
                    result.resolve(response.data);
                }
                catch (err) {
                    result.reject(err);
                }
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WMSService.prototype.createPyramidMemberRequest = function (request) {
            var result = this.$q.defer();
            var requestUrl = this.wcsSettings.adminEndpoint + "/coverage/pyramid/create" + "?" + request.toKVP();
            var requestHeaders = this.adminService.getAuthenticationHeaders();
            this.$http.get(requestUrl, {
                headers: requestHeaders
            }).then(function (data) {
                try {
                    result.resolve("");
                }
                catch (err) {
                    result.reject(err);
                }
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WMSService.prototype.removePyramidMemberRequest = function (request) {
            var result = this.$q.defer();
            var requestUrl = this.wcsSettings.adminEndpoint + "/coverage/pyramid/remove" + "?" + request.toKVP();
            var requestHeaders = this.adminService.getAuthenticationHeaders();
            this.$http.get(requestUrl, {
                headers: requestHeaders
            }).then(function (data) {
                try {
                    result.resolve("");
                }
                catch (err) {
                    result.reject(err);
                }
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WMSService.prototype.blackListOneLayer = function (layerName) {
            var result = this.$q.defer();
            var requestUrl = this.wcsSettings.adminEndpoint + "/wms/blacklist?LAYERLIST=" + layerName;
            var requestHeaders = this.adminService.getAuthenticationHeaders();
            this.$http.get(requestUrl, {
                headers: requestHeaders
            }).then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WMSService.prototype.blackListAllLayers = function () {
            var result = this.$q.defer();
            var requestUrl = this.wcsSettings.adminEndpoint + "/wms/blacklistall";
            var requestHeaders = this.adminService.getAuthenticationHeaders();
            this.$http.get(requestUrl, {
                headers: requestHeaders
            }).then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WMSService.prototype.whiteListOneLayer = function (layerName) {
            var result = this.$q.defer();
            var requestUrl = this.wcsSettings.adminEndpoint + "/wms/whitelist?LAYERLIST=" + layerName;
            var requestHeaders = this.adminService.getAuthenticationHeaders();
            this.$http.get(requestUrl, {
                headers: requestHeaders
            }).then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WMSService.prototype.whiteListAllLayers = function () {
            var result = this.$q.defer();
            var requestUrl = this.wcsSettings.adminEndpoint + "/wms/whitelistall";
            var requestHeaders = this.adminService.getAuthenticationHeaders();
            this.$http.get(requestUrl, {
                headers: requestHeaders
            }).then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        WMSService.$inject = ["$http", "$q", "rasdaman.WMSSettingsService", "rasdaman.WCSSettingsService",
            "rasdaman.common.SerializedObjectFactory", "$window",
            "rasdaman.CredentialService",
            "rasdaman.AdminService"];
        return WMSService;
    }());
    rasdaman.WMSService = WMSService;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WMSGetCapabilitiesController = (function () {
        function WMSGetCapabilitiesController($window, $rootScope, $scope, $log, settings, wmsService, alertService, errorHandlingService, webWorldWindService, adminService) {
            var _this = this;
            this.$window = $window;
            this.$rootScope = $rootScope;
            this.$scope = $scope;
            this.$log = $log;
            this.settings = settings;
            this.wmsService = wmsService;
            this.alertService = alertService;
            this.errorHandlingService = errorHandlingService;
            this.webWorldWindService = webWorldWindService;
            this.adminService = adminService;
            $scope.isAvailableLayersOpen = false;
            $scope.isServiceIdentificationOpen = false;
            $scope.isServiceProviderOpen = false;
            $scope.isCapabilitiesDocumentOpen = false;
            $scope.rowPerPageSmartTable = 10;
            $scope.wmsServerEndpoint = settings.wmsEndpoint;
            var canvasId = "wmsCanvasGetCapabilities";
            var currentPageNumber = 1;
            $scope.displayLayersDropdownItems = [{ "name": "Display all layers", "value": "" },
                { "name": "Display local layers", "value": "local" },
                { "name": "Display remote layers", "value": "remote" }
            ];
            $scope.display = true;
            $scope.showAllFootprints = { isChecked: false };
            $rootScope.$watch("adminStateInformation.loggedIn", function (newValue, oldValue) {
                if (newValue) {
                    $scope.adminUserLoggedIn = true;
                }
                else {
                    $scope.adminUserLoggedIn = false;
                }
            });
            $scope.initCheckboxesForLayerNames = function () {
                var layerArray = $scope.capabilities.layers;
                for (var i = 0; i < layerArray.length; i++) {
                    layerArray[i].displayFootprint = false;
                }
            };
            $scope.getLayerByName = function (layerName) {
                var layerArray = $scope.capabilities.layers;
                for (var i = 0; i < layerArray.length; i++) {
                    if (layerArray[i].name == layerName) {
                        return layerArray[i];
                    }
                }
                return null;
            };
            $scope.displayFootprintOnGlobe = function (coverageId) {
                webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, coverageId);
            };
            $scope.displayAllFootprintsOnGlobe = function (status) {
                if (status == true) {
                    var filteredRows = JSON.parse($window.wmsGetCapabilitiesFilteredRows);
                    $scope.hideAllFootprintsOnGlobe();
                    for (var i = 0; i < filteredRows.length; i++) {
                        var obj = filteredRows[i];
                        var layerName = obj["name"];
                        for (var j = 0; j < $scope.capabilities.layers.length; j++) {
                            var coverageExtent = $scope.capabilities.layers[j].coverageExtent;
                            var coverageId = coverageExtent.coverageId;
                            if (layerName == coverageId) {
                                $scope.capabilities.layers[j].displayFootprint = true;
                                webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, coverageId);
                            }
                        }
                    }
                }
                else {
                    $scope.hideAllFootprintsOnGlobe();
                }
            };
            $scope.hideAllFootprintsOnGlobe = function () {
                for (var i = 0; i < $scope.capabilities.layers.length; i++) {
                    var coverageExtent = $scope.capabilities.layers[i].coverageExtent;
                    var coverageId = coverageExtent.coverageId;
                    if (coverageExtent.displayFootprint == true) {
                        $scope.capabilities.layers[i].displayFootprint = false;
                        webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, coverageId);
                    }
                }
            };
            $scope.handleBlackListOneLayer = function (layerName) {
                var status = $scope.getLayerByName(layerName).customizedMetadata.isBlackedList;
                if (status == true) {
                    _this.wmsService.blackListOneLayer(layerName).then(function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i] = arguments[_i];
                        }
                        _this.alertService.success("Blacklisted layer <b>" + layerName + "</b>");
                    }, function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i] = arguments[_i];
                        }
                        _this.errorHandlingService.handleError(args);
                        _this.$log.error(args);
                    })["finally"](function () {
                    });
                }
                else {
                    _this.wmsService.whiteListOneLayer(layerName).then(function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i] = arguments[_i];
                        }
                        _this.alertService.success("Whitelisted layer <b>" + layerName + "</b>");
                    }, function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i] = arguments[_i];
                        }
                        _this.errorHandlingService.handleError(args);
                        _this.$log.error(args);
                    })["finally"](function () {
                    });
                }
            };
            $scope.handleBlackListAllLayers = function () {
                _this.wmsService.blackListAllLayers().then(function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    _this.alertService.success("Blacklisted <b>all layers</b>");
                    for (var i = 0; i < $scope.capabilities.layers.length; i++) {
                        $scope.capabilities.layers[i].customizedMetadata.isBlackedList = true;
                    }
                }, function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    _this.errorHandlingService.handleError(args);
                    _this.$log.error(args);
                })["finally"](function () {
                });
            };
            $scope.handleWhiteListAllLayers = function () {
                _this.wmsService.whiteListAllLayers().then(function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    _this.alertService.success("Whitelisted <b>all layers</b>");
                    for (var i = 0; i < $scope.capabilities.layers.length; i++) {
                        $scope.capabilities.layers[i].customizedMetadata.isBlackedList = false;
                    }
                }, function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    _this.errorHandlingService.handleError(args);
                    _this.$log.error(args);
                })["finally"](function () {
                });
            };
            $scope.$on("reloadWMSServerCapabilities", function (event, b) {
                $scope.getServerCapabilities();
            });
            $scope.$watch("wmsStateInformation.reloadServerCapabilities", function (capabilities) {
                if ($scope.wmsStateInformation.reloadServerCapabilities == true) {
                    $scope.getServerCapabilities();
                }
                $scope.wmsStateInformation.reloadServerCapabilities = false;
            });
            $scope.handleGetServerCapabilities = function () {
                $scope.getServerCapabilities();
                $scope.showAllFootprints.isChecked = false;
            };
            $scope.getServerCapabilities = function () {
                var args = [];
                for (var _i = 0; _i < arguments.length; _i++) {
                    args[_i] = arguments[_i];
                }
                if (!$scope.wmsServerEndpoint) {
                    alertService.error("The entered WMS endpoint is invalid.");
                    return;
                }
                settings.wmsEndpoint = $scope.wmsServerEndpoint;
                settings.setWMSFullEndPoint();
                var capabilitiesRequest = new wms.GetCapabilities();
                wmsService.getServerCapabilities(capabilitiesRequest)
                    .then(function (response) {
                    $scope.capabilitiesDocument = response.document;
                    $scope.capabilities = response.value;
                    $scope.isAvailableLayersOpen = true;
                    $scope.isServiceIdentificationOpen = true;
                    $scope.isServiceProviderOpen = true;
                    $scope.initCheckboxesForLayerNames();
                    var coverageExtentArray = [];
                    for (var i = 0; i < $scope.capabilities.layers.length; i++) {
                        coverageExtentArray.push($scope.capabilities.layers[i].coverageExtent);
                    }
                    webWorldWindService.prepareCoveragesExtentsForGlobe(canvasId, coverageExtentArray);
                }, function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    $scope.capabilitiesDocument = null;
                    $scope.capabilities = null;
                    $scope.isAvailableLayersOpen = false;
                    $scope.isServiceIdentificationOpen = false;
                    $scope.isServiceProviderOpen = false;
                    errorHandlingService.handleError(args);
                    $log.error(args);
                })["finally"](function () {
                    $scope.wmsStateInformation.serverCapabilities = $scope.capabilities;
                });
            };
        }
        WMSGetCapabilitiesController.$inject = [
            "$window",
            "$rootScope",
            "$scope",
            "$log",
            "rasdaman.WMSSettingsService",
            "rasdaman.WMSService",
            "Notification",
            "rasdaman.ErrorHandlingService",
            "rasdaman.WebWorldWindService"
        ];
        return WMSGetCapabilitiesController;
    }());
    rasdaman.WMSGetCapabilitiesController = WMSGetCapabilitiesController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var WMSDescribeLayerController = (function () {
        function WMSDescribeLayerController($scope, $rootScope, $log, settings, wmsService, wcsService, alertService, errorHandlingService, webWorldWindService) {
            $scope.getMapRequestURL = null;
            $scope.layerNames = [];
            $scope.layers = [];
            $scope.displayWMSLayer = false;
            $scope.timeString = null;
            $scope.coverageDescription = null;
            var canvasId = "wmsCanvasDescribeLayer";
            var WCPS_QUERY_FRAGMENT = 0;
            var RASQL_QUERY_FRAGMENT = 1;
            $rootScope.$watch("wmsSelectedLayerName", function (layerName) {
                if (layerName != null) {
                    $scope.selectedLayerName = layerName;
                    $scope.describeLayer();
                }
            });
            $scope.isLayerNameValid = function () {
                for (var i = 0; i < $scope.layers.length; i++) {
                    if ($scope.layers[i].name == $scope.selectedLayerName) {
                        return true;
                    }
                }
                return false;
            };
            $scope.$watch("wmsStateInformation.serverCapabilities", function (capabilities) {
                if (capabilities) {
                    $scope.layers = [];
                    $scope.layerNames = [];
                    $scope.display3DLayerNotification = false;
                    $scope.display4BandsExclamationMark = false;
                    capabilities.layers.forEach(function (layer) {
                        $scope.layerNames.push(layer.name);
                        $scope.layers.push(layer);
                    });
                    $scope.describeLayer();
                }
            });
            $rootScope.$watch("adminStateInformation.loggedIn", function (newValue, oldValue) {
                if (newValue) {
                    $scope.adminUserLoggedIn = true;
                    $scope.hasInsertStyleRole = rasdaman.AdminService.hasRole($rootScope.adminStateInformation.roles, rasdaman.AdminService.PRIV_OWS_WMS_INSERT_STYLE);
                    $scope.hasUpdateStyleRole = rasdaman.AdminService.hasRole($rootScope.adminStateInformation.roles, rasdaman.AdminService.PRIV_OWS_WMS_UPDATE_STYLE);
                    $scope.hasDeleteStyleRole = rasdaman.AdminService.hasRole($rootScope.adminStateInformation.roles, rasdaman.AdminService.PRIV_OWS_WMS_DELETE_STYLE);
                    $scope.hasInsertCoverageRole = rasdaman.AdminService.hasRole($rootScope.adminStateInformation.roles, rasdaman.AdminService.PRIV_OWS_WCS_INSERT_COV);
                    $scope.hasDeleteCoverageRole = rasdaman.AdminService.hasRole($rootScope.adminStateInformation.roles, rasdaman.AdminService.PRIV_OWS_WCS_DELETE_COV);
                }
                else {
                    $scope.adminUserLoggedIn = false;
                }
            });
            $scope.describeLayer = function () {
                $scope.displayWMSLayer = false;
                $scope.selectedStyleName = "";
                $("#styleName").val("");
                $("#styleAbstract").val("");
                $("#overviewLegendImage").attr("src", "");
                for (var i = 0; i < $scope.layers.length; i++) {
                    if ($scope.layers[i].name == $scope.selectedLayerName) {
                        $scope.layer = $scope.layers[i];
                        $scope.isLayerDocumentOpen = true;
                        $scope.firstChangedSlider = [];
                        var coveragesExtents = [{ "bbox": { "xmin": $scope.layer.coverageExtent.bbox.xmin,
                                    "ymin": $scope.layer.coverageExtent.bbox.ymin,
                                    "xmax": $scope.layer.coverageExtent.bbox.xmax,
                                    "ymax": $scope.layer.coverageExtent.bbox.ymax }
                            }];
                        $scope.isCoverageDescriptionsHideGlobe = false;
                        var coverageIds = [];
                        coverageIds.push($scope.layer.name);
                        var describeCoverageRequest = new wcs.DescribeCoverage(coverageIds);
                        var coverageExtentArray = [];
                        coverageExtentArray.push($scope.layer.coverageExtent);
                        wcsService.getCoverageDescription(describeCoverageRequest)
                            .then(function (response) {
                            $scope.coverageDescription = response.value;
                            var dimensions = $scope.coverageDescription.boundedBy.envelope.srsDimension;
                            addSliders(dimensions, coveragesExtents);
                            selectOptionsChange();
                            webWorldWindService.prepareCoveragesExtentsForGlobe(canvasId, coverageExtentArray);
                            webWorldWindService.showCoverageExtentOnGlobe(canvasId, $scope.layer.name);
                        }, function () {
                            var args = [];
                            for (var _i = 0; _i < arguments.length; _i++) {
                                args[_i] = arguments[_i];
                            }
                            errorHandlingService.handleError(args);
                            $log.error(args);
                        });
                        var listPyramidMembersRequest = new wms.ListPyramidMembers($scope.selectedLayerName);
                        wmsService.listPyramidMembersRequest(listPyramidMembersRequest).then(function (arrayData) {
                            var pyramidCoverageMembers = [];
                            arrayData.forEach(function (element) {
                                var coverageName = element["coverage"];
                                var scaleFactors = element["scale"].join(",");
                                var pyramidCoverageMember = new wms.PyramidCoverageMember(coverageName, scaleFactors);
                                pyramidCoverageMembers.push(pyramidCoverageMember);
                            });
                            $scope.layers[i].pyramidCoverageMembers = pyramidCoverageMembers;
                        }, function () {
                            var args = [];
                            for (var _i = 0; _i < arguments.length; _i++) {
                                args[_i] = arguments[_i];
                            }
                            errorHandlingService.handleError(args);
                            $log.error(args);
                        });
                        return;
                    }
                }
            };
            function renewDisplayedWMSGetMapURL(url) {
                var tmpURL = url + $scope.selectedStyleName;
                $('#getMapRequestURL').text(tmpURL);
                $('#getMapRequestURL').attr('href', tmpURL);
                $('#secGetMap').attr('href', tmpURL);
            }
            function addSliders(dimensions, coveragesExtents) {
                for (var j = 0; j < dimensions; ++j) {
                    $scope.firstChangedSlider.push(false);
                }
                $("#sliders").empty();
                $scope.display3DLayerNotification = dimensions > 2 ? true : false;
                $scope.display4BandsExclamationMark = false;
                var showGetMapURL = false;
                var bands = $scope.coverageDescription.rangeType.dataRecord.fields.length;
                var bbox = coveragesExtents[0].bbox;
                $scope.bboxLayer = bbox;
                if (bands == 2 || bands > 4) {
                    $scope.display4BandsExclamationMark = true;
                }
                showGetMapURL = true;
                var minLat = bbox.ymin;
                var minLong = bbox.xmin;
                var maxLat = bbox.ymax;
                var maxLong = bbox.xmax;
                $scope.timeString = null;
                var bboxStr = minLat + "," + minLong + "," + maxLat + "," + maxLong;
                var urlDimensions = bboxStr;
                $scope.dimStr = [];
                for (var j = 0; j < 2; ++j) {
                    $scope.dimStr.push('');
                }
                for (var j = 2; j < dimensions; j++) {
                    if ($scope.layer.layerDimensions[j].isTemporal == true) {
                        $scope.dimStr.push('&' + $scope.layer.layerDimensions[j].name + '="' + $scope.layer.layerDimensions[j].array[0] + '"');
                        $scope.timeString = $scope.layer.layerDimensions[j].array[0];
                    }
                    else {
                        $scope.dimStr.push('&' + $scope.layer.layerDimensions[j].name + '=' + $scope.layer.layerDimensions[j].array[0]);
                    }
                }
                for (var j = 2; j < dimensions; j++) {
                    urlDimensions += $scope.dimStr[j];
                }
                var getMapRequest = new wms.GetMap($scope.layer.name, urlDimensions, 800, 600, $scope.selectedStyleName);
                var url = settings.wmsFullEndpoint + "&" + getMapRequest.toKVP();
                $scope.getMapRequestURL = url;
                $('#getMapRequestURL').text($scope.getMapRequestURL);
                webWorldWindService.loadGetMapResultOnGlobe(canvasId, $scope.selectedLayerName, null, $scope.bboxLayer, $scope.displayWMSLayer, $scope.timeString, $scope.dimStr);
                if (!showGetMapURL) {
                    $scope.getMapRequestURL = null;
                }
                var auxbBox = {
                    xmin: Number,
                    xmax: Number,
                    ymin: Number,
                    ymax: Number
                };
                auxbBox.xmax = $scope.bboxLayer.xmax;
                auxbBox.xmin = $scope.bboxLayer.xmin;
                auxbBox.ymax = $scope.bboxLayer.ymax;
                auxbBox.ymin = $scope.bboxLayer.ymin;
                var stepSize = 0.01;
                var numberStepsLat = ($scope.bboxLayer.ymax - $scope.bboxLayer.ymin) / stepSize;
                var numberStepsLong = ($scope.bboxLayer.xmax - $scope.bboxLayer.xmin) / stepSize;
                var stepLat = ($scope.bboxLayer.ymax - $scope.bboxLayer.ymin) / numberStepsLat;
                var stepLong = ($scope.bboxLayer.xmax - $scope.bboxLayer.xmin) / numberStepsLong;
                $("#latSlider").slider({
                    max: numberStepsLat,
                    range: true,
                    values: [0, numberStepsLat],
                    slide: function (event, slider) {
                        var sliderMin = slider.values[0];
                        var sliderMax = slider.values[1];
                        $scope.firstChangedSlider[1] = true;
                        minLat = bbox.ymin;
                        maxLat = bbox.ymax;
                        minLat += stepLat * sliderMin;
                        maxLat -= stepLat * (numberStepsLat - sliderMax);
                        auxbBox.ymin = minLat;
                        auxbBox.ymax = maxLat;
                        $scope.bboxLayer = auxbBox;
                        var tooltip = minLat + ':' + maxLat;
                        $("#latSlider").attr('data-original-title', tooltip);
                        $("#latSlider").tooltip('show');
                        var bboxStr = 'bbox=' + minLat + "," + minLong + "," + maxLat + "," + maxLong;
                        var pos1 = url.indexOf('&bbox=');
                        var pos2 = url.indexOf('&', pos1 + 1);
                        url = url.substr(0, pos1 + 1) + bboxStr + url.substr(pos2, url.length - pos2);
                        $scope.getMapRequestURL = url;
                        renewDisplayedWMSGetMapURL(url);
                        webWorldWindService.loadGetMapResultOnGlobe(canvasId, $scope.selectedLayerName, $scope.selectedStyleName, auxbBox, $scope.displayWMSLayer, $scope.timeString, $scope.dimStr);
                    }
                });
                $("#latSlider").tooltip();
                $("#latSlider").attr('data-original-title', $scope.bboxLayer.ymin + ':' + $scope.bboxLayer.ymax);
                if ($scope.firstChangedSlider[1] == false) {
                    $("#latSlider").slider('values', [0, numberStepsLat]);
                }
                $("#longSlider").slider({
                    max: numberStepsLong,
                    range: true,
                    values: [0, numberStepsLong],
                    slide: function (event, slider) {
                        var sliderMin = slider.values[0];
                        var sliderMax = slider.values[1];
                        $scope.firstChangedSlider[2] = true;
                        minLong = bbox.xmin;
                        maxLong = bbox.xmax;
                        minLong += stepLong * sliderMin;
                        maxLong -= stepLong * (numberStepsLong - sliderMax);
                        auxbBox.xmin = minLong;
                        auxbBox.xmax = maxLong;
                        $scope.bboxLayer = auxbBox;
                        var tooltip = minLong + ':' + maxLong;
                        $("#longSlider").attr('data-original-title', tooltip);
                        $("#longSlider").tooltip('show');
                        var bboxStr = 'bbox=' + minLat + "," + minLong + "," + maxLat + "," + maxLong;
                        var pos1 = url.indexOf('&bbox=');
                        var pos2 = url.indexOf('&', pos1 + 1);
                        url = url.substr(0, pos1 + 1) + bboxStr + url.substr(pos2, url.length - pos2);
                        $scope.getMapRequestURL = url;
                        renewDisplayedWMSGetMapURL(url);
                        webWorldWindService.loadGetMapResultOnGlobe(canvasId, $scope.selectedLayerName, $scope.selectedStyleName, auxbBox, $scope.displayWMSLayer, $scope.timeString, $scope.dimStr);
                    }
                });
                $("#longSlider").tooltip();
                $("#longSlider").attr('data-original-title', $scope.bboxLayer.xmin + ':' + $scope.bboxLayer.xmax);
                if ($scope.firstChangedSlider[2] == false) {
                    $("#longSlider").slider('values', [0, numberStepsLong]);
                }
                var sufixSlider = "d";
                var _loop_1 = function () {
                    $("<div />", { "class": "containerSliders", id: "containerSlider" + j + sufixSlider })
                        .appendTo($("#sliders"));
                    $("<label />", { "class": "sliderLabel", id: "label" + j + sufixSlider })
                        .appendTo($("#containerSlider" + j + sufixSlider));
                    $("#label" + j + sufixSlider).text($scope.layer.layerDimensions[j].name + ':');
                    $("<div />", { "class": "slider", id: "slider" + j + sufixSlider })
                        .appendTo($("#containerSlider" + j + sufixSlider));
                    var sliderId = "#slider" + j + sufixSlider;
                    $(function () {
                        $(sliderId).slider({
                            max: $scope.layer.layerDimensions[j].array.length - 1,
                            create: function (event, slider) {
                                this.sliderObj = $scope.layer.layerDimensions[j];
                                this.sliderObj.index = j;
                                var sizeSlider = $scope.layer.layerDimensions[j].array.length - 1;
                                for (var it = 1; it < sizeSlider; ++it) {
                                    $("<label>|</label>").css('left', (it / sizeSlider * 100) + '%')
                                        .appendTo($(sliderId));
                                }
                            },
                            slide: function (event, slider) {
                                $scope.firstChangedSlider[this.sliderPos] = true;
                                if (this.sliderObj.isTemporal == true) {
                                    $scope.dimStr[this.sliderObj.index] = this.sliderObj.name + '="' + this.sliderObj.array[slider.value] + '"';
                                    $scope.timeString = this.sliderObj.array[slider.value];
                                }
                                else {
                                    $scope.dimStr[this.sliderObj.index] = this.sliderObj.name + '=' + this.sliderObj.array[slider.value];
                                }
                                var pos1 = url.indexOf('&' + this.sliderObj.name + '=');
                                var pos2 = url.indexOf('&', pos1 + 1);
                                url = url.substr(0, pos1 + 1) + $scope.dimStr[this.sliderObj.index] + url.substr(pos2, url.length - pos2);
                                $scope.getMapRequestURL = url;
                                var tooltip = this.sliderObj.array[slider.value];
                                $(sliderId).attr('data-original-title', tooltip);
                                $(sliderId).tooltip('show');
                                renewDisplayedWMSGetMapURL(url);
                                webWorldWindService.loadGetMapResultOnGlobe(canvasId, $scope.selectedLayerName, $scope.selectedStyleName, auxbBox, $scope.displayWMSLayer, $scope.timeString, $scope.dimStr);
                            }
                        });
                    });
                    $(sliderId).tooltip();
                    $(sliderId).attr('data-original-title', $scope.layer.layerDimensions[j].array[0]);
                    if ($scope.firstChangedSlider[j] == false) {
                        $(sliderId).slider('value', 0);
                    }
                };
                for (var j = 2; j < dimensions; j++) {
                    _loop_1();
                }
            }
            $scope.isLayerDocumentOpen = false;
            $scope.showWMSLayerOnGlobe = function (styleName) {
                $scope.selectedStyleName = styleName;
                $scope.displayWMSLayer = true;
                renewDisplayedWMSGetMapURL($scope.getMapRequestURL);
                webWorldWindService.loadGetMapResultOnGlobe(canvasId, $scope.selectedLayerName, styleName, $scope.bboxLayer, true, $scope.timeString, $scope.dimStr);
            };
            $scope.hideWMSLayerOnGlobe = function () {
                $scope.displayWMSLayer = false;
                webWorldWindService.loadGetMapResultOnGlobe(canvasId, $scope.selectedLayerName, $scope.selectedStyleName, $scope.bboxLayer, false, $scope.timeString, $scope.dimStr);
            };
            $scope.createPyramidMember = function () {
                var scaleFactors = $("#scaleFactorsValue").val();
                var pyramidMemberCoverageId = $("#pyramidMemberCoverageIdValue").val();
                var createPyramidMember = new wms.CreatePyramidMember($scope.layer.name, scaleFactors, pyramidMemberCoverageId);
                wmsService.createPyramidMemberRequest(createPyramidMember).then(function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    alertService.success("Successfully created pyramid member coverage <b>" + pyramidMemberCoverageId
                        + "</b> with scalefactors <b>" + scaleFactors + "</b> of layer  <b>" + $scope.layer.name + "</b>.");
                    $scope.wmsStateInformation.reloadServerCapabilities = true;
                    $("#scaleFactorsValue").val("");
                    $("#pyramidMemberCoverageIdValue").val("");
                }, function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    errorHandlingService.handleError(args);
                })["finally"](function () {
                });
            };
            $scope.removePyramidMember = function (pyramidMemberCoverageId) {
                var removePyramidMemberRequest = new wms.RemovePyramidMember($scope.layer.name, pyramidMemberCoverageId);
                wmsService.removePyramidMemberRequest(removePyramidMemberRequest).then(function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    alertService.success("Successfully remove pyramid member <b>" + pyramidMemberCoverageId + "</b> from layer <b>" + $scope.layer.name + "</b>");
                    $scope.wmsStateInformation.reloadServerCapabilities = true;
                }, function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    errorHandlingService.handleError(args);
                })["finally"](function () {
                });
            };
            function selectOptionsChange() {
                $("#styleQueryType").val("none").change();
                $("#styleQueryType").change(function () {
                    if (this.value !== "none") {
                        $("#divStyleQuery").show();
                    }
                    else {
                        $("#divStyleQuery").hide();
                    }
                });
                $("#styleColorTableType").val("none").change();
                $("#styleColorTableType").change(function () {
                    if (this.value !== "none") {
                        $("#divStyleColorTableDefinition").show();
                    }
                    else {
                        $("#divStyleColorTableDefinition").hide();
                    }
                });
                $("#colorTableDefinitionStyleFileInput").change(function () {
                    var reader = new FileReader();
                    reader.onload = function fileReadCompleted() {
                        $("#styleColorTableDefinition").val(reader.result);
                    };
                    reader.readAsText(this.files[0]);
                });
                $("#legendImageFileInput").change(function () {
                    var selectedfile = this.files;
                    if (selectedfile.length > 0) {
                        var imageFile = selectedfile[0];
                        var fileReader = new FileReader();
                        fileReader.onload = function (fileLoadedEvent) {
                            var srcData = fileLoadedEvent.target.result;
                            $("#hiddenLegendBase64Textarea").val(srcData);
                            $("#overviewLegendImage").attr("src", srcData);
                        };
                        fileReader.readAsDataURL(imageFile);
                    }
                });
            }
            $scope.isStyleNameValid = function (styleName) {
                for (var i = 0; i < $scope.layer.styles.length; ++i) {
                    if ($scope.layer.styles[i].name == styleName) {
                        return true;
                    }
                }
                return false;
            };
            $scope.setDefaultStyle = function (styleName) {
                $scope.describeStyleToUpdate(styleName);
                if ($scope.layer.styles.length == 1) {
                    return;
                }
                else if ($scope.layer.styles.length > 1) {
                    $scope.defaultStyleName = styleName;
                    if ($scope.defaultStyleName === styleName) {
                        $("#defaultStyle").prop("checked", true);
                    }
                    $scope.updateStyle();
                }
            };
            $scope.describeStyleToUpdate = function (styleName) {
                for (var i = 0; i < $scope.layer.styles.length; i++) {
                    var styleObj = $scope.layer.styles[i];
                    if (styleObj.name == styleName) {
                        $("#styleName").val(styleObj.name);
                        $("#styleAbstract").val(styleObj.abstract);
                        if (styleObj.defaultStyle == true) {
                            $("#defaultStyle").prop("checked", true);
                        }
                        else {
                            $("#defaultStyle").prop("checked", false);
                        }
                        var styleQueryType = styleObj.queryType;
                        if (styleQueryType === "") {
                            styleQueryType = "none";
                        }
                        $("#styleQueryType").val(styleQueryType);
                        $("#styleQuery").val(styleObj.query);
                        var colorTableType = styleObj.colorTableType;
                        if (colorTableType === "") {
                            colorTableType = "none";
                        }
                        $("#styleColorTableType").val(colorTableType);
                        $("#styleColorTableDefinition").val(styleObj.colorTableDefinition);
                        $("#styleQueryType").change();
                        $("#styleColorTableType").change();
                        if (styleObj.legendGraphicURL != null) {
                            $("#overviewLegendImage").attr("src", styleObj.legendGraphicURL + "&" + new Date().getTime());
                        }
                        break;
                    }
                }
            };
            $scope.validateStyle = function () {
                var styleName = $("#styleName").val();
                var styleAbstract = $("#styleAbstract").val();
                var styleQueryType = $("#styleQueryType").val();
                var styleQuery = $("#styleQuery").val();
                var styleColorTableType = $("#styleColorTableType").val();
                var styleColorTableDefintion = $("#styleColorTableDefinition").val();
                if (styleName.trim() === "") {
                    alertService.error("Style name cannot be empty.");
                    return;
                }
                else if (styleAbstract.trim() === "") {
                    alertService.error("Style abstract cannot be empty.");
                    return;
                }
                if (styleQueryType == "none" && styleColorTableType == "none") {
                    alertService.error("A style must contain at least a query fragment or a color table definition.");
                    return;
                }
                if (styleQuery.trim() === "" && styleColorTableDefintion.trim() === "") {
                    alertService.error("Style query or color table definition must have value.");
                    return;
                }
                return true;
            };
            $scope.updateStyle = function () {
                if ($scope.validateStyle()) {
                    var styleName = $("#styleName").val();
                    var styleAbstract = $("#styleAbstract").val();
                    var styleQueryType = $("#styleQueryType").val();
                    var styleQuery = $("#styleQuery").val();
                    var styleColorTableType = $("#styleColorTableType").val();
                    var styleColorTableDefintion = $("#styleColorTableDefinition").val();
                    var defaultStyle = $("#defaultStyle").prop("checked");
                    if (!$scope.isStyleNameValid(styleName)) {
                        alertService.error("Style name '" + styleName + "' does not exist to update.");
                        return;
                    }
                    var legendGraphicBase64 = null;
                    var base64String = $("#hiddenLegendBase64Textarea").val();
                    if (base64String !== "") {
                        legendGraphicBase64 = base64String;
                    }
                    var updateLayerStyle = new wms.UpdateLayerStyle($scope.layer.name, styleName, styleAbstract, styleQueryType, styleQuery, styleColorTableType, styleColorTableDefintion, defaultStyle, legendGraphicBase64);
                    wmsService.updateLayerStyleRequest(updateLayerStyle).then(function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i] = arguments[_i];
                        }
                        alertService.success("Successfully update style with name <b>" + styleName + "</b> of layer with name <b>" + $scope.layer.name + "</b>");
                        $scope.wmsStateInformation.reloadServerCapabilities = true;
                        $scope.describeStyleToUpdate(styleName);
                    }, function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i] = arguments[_i];
                        }
                        errorHandlingService.handleError(args);
                    })["finally"](function () {
                    });
                }
            };
            $scope.insertStyle = function () {
                if ($scope.validateStyle()) {
                    var styleName = $("#styleName").val();
                    var styleAbstract = $("#styleAbstract").val();
                    var styleQueryType = $("#styleQueryType").val();
                    var styleQuery = $("#styleQuery").val();
                    var styleColorTableType = $("#styleColorTableType").val();
                    var styleColorTableDefintion = $("#styleColorTableDefinition").val();
                    var defaultStyle = $("#defaultStyle").prop("checked");
                    if ($scope.isStyleNameValid(styleName)) {
                        alertService.error("Style name '" + styleName + "' already exists, cannot insert same name.");
                        return;
                    }
                    var legendGraphicBase64 = null;
                    var base64String = $("#hiddenLegendBase64Textarea").val();
                    if (base64String !== "") {
                        legendGraphicBase64 = base64String;
                    }
                    var insertLayerStyle = new wms.InsertLayerStyle($scope.layer.name, styleName, styleAbstract, styleQueryType, styleQuery, styleColorTableType, styleColorTableDefintion, defaultStyle, legendGraphicBase64);
                    wmsService.insertLayerStyleRequest(insertLayerStyle).then(function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i] = arguments[_i];
                        }
                        alertService.success("Successfully insert style with name <b>" + styleName + "</b> of layer with name <b>" + $scope.layer.name + "</b>");
                        $scope.wmsStateInformation.reloadServerCapabilities = true;
                        $scope.describeStyleToUpdate(styleName);
                    }, function () {
                        var args = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            args[_i] = arguments[_i];
                        }
                        errorHandlingService.handleError(args);
                    })["finally"](function () {
                    });
                }
            };
            $scope.deleteStyle = function (styleName) {
                var deleteLayerStyle = new wms.DeleteLayerStyle($scope.layer.name, styleName);
                wmsService.deleteLayerStyleRequest(deleteLayerStyle).then(function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    alertService.success("Successfully delete style with name <b>" + styleName + "</b> of layer with name <b>" + $scope.layer.name + "</b>");
                    $scope.wmsStateInformation.reloadServerCapabilities = true;
                }, function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    errorHandlingService.handleError(args);
                })["finally"](function () {
                });
            };
        }
        WMSDescribeLayerController.$inject = [
            "$scope",
            "$rootScope",
            "$log",
            "rasdaman.WMSSettingsService",
            "rasdaman.WMSService",
            "rasdaman.WCSService",
            "Notification",
            "rasdaman.ErrorHandlingService",
            "rasdaman.WebWorldWindService"
        ];
        return WMSDescribeLayerController;
    }());
    rasdaman.WMSDescribeLayerController = WMSDescribeLayerController;
})(rasdaman || (rasdaman = {}));
var login;
(function (login) {
    var Credential = (function () {
        function Credential(username, password) {
            this.username = username;
            this.password = password;
        }
        Credential.prototype.toKVP = function () {
            return "username=" + this.username +
                "&password=" + this.password;
        };
        Credential.prototype.toString = function () {
            return this.username + ":" + this.password;
        };
        Credential.fromString = function (input) {
            var tmp = input.split(":");
            var username = tmp[0];
            var password = tmp[1];
            return new Credential(username, password);
        };
        return Credential;
    }());
    login.Credential = Credential;
})(login || (login = {}));
var rasdaman;
(function (rasdaman) {
    var AdminService = (function () {
        function AdminService($rootScope, $http, $q, settings, credentialService) {
            var _this = this;
            this.$rootScope = $rootScope;
            this.$http = $http;
            this.$q = $q;
            this.settings = settings;
            this.credentialService = credentialService;
            this.persitAdminUserCredentials = function (credentials) {
                window.localStorage.setItem("petascopeAdminUserCredentials", credentials.toString());
            };
            this.persitLoggedOut = function () {
                window.localStorage.removeItem("petascopeAdminUserCredentials");
            };
            this.getPersistedAdminUserCredentials = function () {
                var persistedCredentialsString = window.localStorage.getItem("petascopeAdminUserCredentials");
                if (persistedCredentialsString != null) {
                    var credentials = login.Credential.fromString(persistedCredentialsString);
                    if (credentials["username"] == "petauser") {
                        _this.persitLoggedOut();
                        return null;
                    }
                    return credentials;
                }
                return null;
            };
            this.getAuthenticationHeaders = function () {
                var credentials = _this.getPersistedAdminUserCredentials();
                var headers = {};
                if (credentials != null) {
                    headers = _this.credentialService.createBasicAuthenticationHeader(credentials.username, credentials.password);
                }
                return headers;
            };
        }
        AdminService.prototype.login = function (inputCredentials) {
            var result = this.$q.defer();
            var requestUrl = this.settings.contextPath + "/login";
            var success = false;
            var adminRolesObj = AdminService.adminRoles;
            this.$http.get(requestUrl, {
                headers: this.credentialService.createBasicAuthenticationHeader(inputCredentials.username, inputCredentials.password)
            }).then(function (response) {
                var roles = response.data.split(",");
                var hasAdminRole = false;
                for (var i = 0; i < adminRolesObj.length; i++) {
                    var adminRole = adminRolesObj[i];
                    if (roles.includes(adminRole)) {
                        hasAdminRole = true;
                        break;
                    }
                }
                if (hasAdminRole) {
                    result.resolve(roles);
                }
                else {
                    result.reject("Given credentials are not valid for an admin user with granted adequate roles.");
                }
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        AdminService.hasRole = function (roles, role) {
            return roles.includes(AdminService.RW_RIGHTS_COMMUNITY)
                || roles.includes(role);
        };
        AdminService.prototype.updateServiceIdentification = function (serviceIdentification) {
            var result = this.$q.defer();
            var requestUrl = this.settings.adminEndpoint + "/ows/serviceinfo";
            var credentials = this.getPersistedAdminUserCredentials();
            var requestHeaders = this.credentialService.createBasicAuthenticationHeader(credentials.username, credentials.password);
            var request = {
                method: 'POST',
                url: requestUrl,
                transformResponse: null,
                headers: requestHeaders,
                data: serviceIdentification.toKVP()
            };
            this.$http(request).then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        AdminService.prototype.updateServiceProvider = function (serviceProvider) {
            var result = this.$q.defer();
            var requestUrl = this.settings.adminEndpoint + "/ows/serviceinfo";
            var credentials = this.getPersistedAdminUserCredentials();
            var requestHeaders = this.credentialService.createBasicAuthenticationHeader(credentials.username, credentials.password);
            var request = {
                method: 'POST',
                url: requestUrl,
                transformResponse: null,
                headers: requestHeaders,
                data: serviceProvider.toKVP()
            };
            this.$http(request).then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        };
        AdminService.$inject = ["$rootScope", "$http", "$q", "rasdaman.WCSSettingsService",
            "rasdaman.CredentialService"];
        AdminService.RW_RIGHTS_COMMUNITY = "RW";
        AdminService.PRIV_OWS_UPDATE_SRV = "PRIV_OWS_UPDATE_SRV";
        AdminService.PRIV_OWS_WCS_INSERT_COV = "PRIV_OWS_WCS_INSERT_COV";
        AdminService.PRIV_OWS_WCS_UPDATE_COV = "PRIV_OWS_WCS_UPDATE_COV";
        AdminService.PRIV_OWS_WCS_DELETE_COV = "PRIV_OWS_WCS_DELETE_COV";
        AdminService.PRIV_OWS_WMS_INSERT_LAYER = "PRIV_OWS_WMS_INSERT_LAYER";
        AdminService.PRIV_OWS_WMS_UPDATE_LAYER = "PRIV_OWS_WMS_UPDATE_LAYER";
        AdminService.PRIV_OWS_WMS_DELETE_LAYER = "PRIV_OWS_WMS_DELETE_LAYER";
        AdminService.PRIV_OWS_WMS_INSERT_STYLE = "PRIV_OWS_WMS_INSERT_STYLE";
        AdminService.PRIV_OWS_WMS_UPDATE_STYLE = "PRIV_OWS_WMS_UPDATE_STYLE";
        AdminService.PRIV_OWS_WMS_DELETE_STYLE = "PRIV_OWS_WMS_DELETE_STYLE";
        AdminService.PRIV_OWS_WCS_BLACKWHITELIST_COV = "PRIV_OWS_WCS_BLACKWHITELIST_COV";
        AdminService.PRIV_OWS_WMS_BLACKWHITELIST_LAYER = "PRIV_OWS_WMS_BLACKWHITELIST_LAYER";
        AdminService.adminRoles = [AdminService.PRIV_OWS_UPDATE_SRV, AdminService.PRIV_OWS_WCS_INSERT_COV, AdminService.PRIV_OWS_WCS_UPDATE_COV, AdminService.PRIV_OWS_WCS_DELETE_COV,
            AdminService.PRIV_OWS_WMS_INSERT_LAYER, AdminService.PRIV_OWS_WMS_UPDATE_LAYER, AdminService.PRIV_OWS_WMS_DELETE_LAYER,
            AdminService.PRIV_OWS_WMS_INSERT_STYLE, AdminService.PRIV_OWS_WMS_UPDATE_STYLE, AdminService.PRIV_OWS_WMS_DELETE_STYLE,
            AdminService.PRIV_OWS_WCS_BLACKWHITELIST_COV, AdminService.PRIV_OWS_WMS_BLACKWHITELIST_LAYER,
            AdminService.RW_RIGHTS_COMMUNITY
        ];
        return AdminService;
    }());
    rasdaman.AdminService = AdminService;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var AdminLoginController = (function () {
        function AdminLoginController($scope, $rootScope, $log, settings, adminService, alertService, errorHandlingService) {
            this.$scope = $scope;
            this.$rootScope = $rootScope;
            this.$log = $log;
            this.settings = settings;
            this.adminService = adminService;
            this.alertService = alertService;
            this.errorHandlingService = errorHandlingService;
            $scope.credential = new login.Credential("", "");
            var persitedAdminUserCredentials = adminService.getPersistedAdminUserCredentials();
            if (persitedAdminUserCredentials != null) {
                adminService.login(persitedAdminUserCredentials).then(function (data) {
                    $rootScope.adminStateInformation.roles = data;
                    $rootScope.adminStateInformation.loggedIn = true;
                }, function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                });
            }
            $scope.login = function () {
                var args = [];
                for (var _i = 0; _i < arguments.length; _i++) {
                    args[_i] = arguments[_i];
                }
                adminService.login($scope.credential).then(function (data) {
                    alertService.success("Successfully logged in.");
                    $rootScope.adminStateInformation.loggedIn = true;
                    $rootScope.adminStateInformation.roles = data;
                    adminService.persitAdminUserCredentials($scope.credential);
                }, function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    errorHandlingService.handleError(args);
                })["finally"](function () {
                });
            };
        }
        AdminLoginController.$inject = [
            "$scope",
            "$rootScope",
            "$log",
            "rasdaman.WCSSettingsService",
            "rasdaman.AdminService",
            "Notification",
            "rasdaman.ErrorHandlingService"
        ];
        return AdminLoginController;
    }());
    rasdaman.AdminLoginController = AdminLoginController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var AdminOWSMetadataManagementController = (function () {
        function AdminOWSMetadataManagementController($scope, $rootScope, $log, wcsService, settings, adminService, alertService, errorHandlingService) {
            this.$scope = $scope;
            this.$rootScope = $rootScope;
            this.$log = $log;
            this.wcsService = wcsService;
            this.settings = settings;
            this.adminService = adminService;
            this.alertService = alertService;
            this.errorHandlingService = errorHandlingService;
            $rootScope.$on("reloadServerCapabilities", function (event, value) {
                $scope.getServerCapabilities();
            });
            $rootScope.$watch("adminStateInformation.loggedIn", function (newValue, oldValue) {
                $scope.getServerCapabilities();
                $scope.hasRole = rasdaman.AdminService.hasRole($rootScope.adminStateInformation.roles, rasdaman.AdminService.PRIV_OWS_UPDATE_SRV);
            });
            $scope.getServerCapabilities = function () {
                var args = [];
                for (var _i = 0; _i < arguments.length; _i++) {
                    args[_i] = arguments[_i];
                }
                var capabilitiesRequest = new wcs.GetCapabilities();
                wcsService.getServerCapabilities(capabilitiesRequest)
                    .then(function (response) {
                    $scope.capabilitiesDocument = response.document;
                    var capabilities = response.value;
                    var serviceTitle = capabilities.serviceIdentification.title[0].value;
                    var abstract = capabilities.serviceIdentification.abstract[0].value;
                    $scope.serviceIdentification = new admin.ServiceIdentification(serviceTitle, abstract);
                    var providerName = capabilities.serviceProvider.providerName;
                    var providerSite = capabilities.serviceProvider.providerSite.href;
                    var individualName = capabilities.serviceProvider.serviceContact.individualName;
                    var positionName = capabilities.serviceProvider.serviceContact.positionName;
                    var role = capabilities.serviceProvider.serviceContact.role.code;
                    var email = capabilities.serviceProvider.serviceContact.contactInfo.address.electronicMailAddress[0];
                    var voicePhone = capabilities.serviceProvider.serviceContact.contactInfo.phone.voice[0];
                    var facsimilePhone = capabilities.serviceProvider.serviceContact.contactInfo.phone.facsimile[0];
                    var hoursOfService = capabilities.serviceProvider.serviceContact.contactInfo.hoursOfService;
                    var contactInstructions = capabilities.serviceProvider.serviceContact.contactInfo.contactInstructions;
                    var city = capabilities.serviceProvider.serviceContact.contactInfo.address.city;
                    var administrativeArea = capabilities.serviceProvider.serviceContact.contactInfo.address.administrativeArea;
                    var postalCode = capabilities.serviceProvider.serviceContact.contactInfo.address.postalCode;
                    var country = capabilities.serviceProvider.serviceContact.contactInfo.address.country;
                    $scope.serviceProvider = new admin.ServiceProvider(providerName, providerSite, individualName, positionName, role, email, voicePhone, facsimilePhone, hoursOfService, contactInstructions, city, administrativeArea, postalCode, country);
                }, function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    errorHandlingService.handleError(args);
                    $log.error(args);
                })["finally"](function () {
                });
            };
            $scope.updateServiceIdentification = function () {
                var args = [];
                for (var _i = 0; _i < arguments.length; _i++) {
                    args[_i] = arguments[_i];
                }
                adminService.updateServiceIdentification($scope.serviceIdentification).then(function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    alertService.success("Successfully update Service Identifcation to Petascope database.");
                }, function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    errorHandlingService.handleError(args);
                })["finally"](function () {
                });
            };
            $scope.updateServiceProvider = function () {
                var args = [];
                for (var _i = 0; _i < arguments.length; _i++) {
                    args[_i] = arguments[_i];
                }
                adminService.updateServiceProvider($scope.serviceProvider).then(function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    alertService.success("Successfully update Service Provider to Petascope database.");
                }, function () {
                    var args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        args[_i] = arguments[_i];
                    }
                    errorHandlingService.handleError(args);
                })["finally"](function () {
                });
            };
            $scope.logOut = function () {
                var args = [];
                for (var _i = 0; _i < arguments.length; _i++) {
                    args[_i] = arguments[_i];
                }
                adminService.persitLoggedOut();
                $rootScope.adminStateInformation.loggedIn = false;
            };
        }
        AdminOWSMetadataManagementController.$inject = [
            "$scope",
            "$rootScope",
            "$log",
            "rasdaman.WCSService",
            "rasdaman.WCSSettingsService",
            "rasdaman.AdminService",
            "Notification",
            "rasdaman.ErrorHandlingService"
        ];
        return AdminOWSMetadataManagementController;
    }());
    rasdaman.AdminOWSMetadataManagementController = AdminOWSMetadataManagementController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    "use strict";
    var AngularConfig = (function () {
        function AngularConfig($httpProvider, $stateProvider, $locationProvider, $urlRouterProvider, NotificationProvider) {
            $httpProvider.defaults.useXDomain = true;
            $stateProvider
                .state("login", {
                url: "",
                views: {
                    "login": {
                        templateUrl: "ows/src/components/login_component/Login.html",
                        controller: rasdaman.LoginController
                    }
                }
            })
                .state("services", {
                url: "services",
                views: {
                    "wsclient": {
                        templateUrl: "ows/wsclient.html"
                    },
                    'get_capabilities@services': {
                        url: "get_capabilities",
                        templateUrl: 'ows/src/components/wcs_component/get_capabilities/GetCapabilitiesView.html',
                        controller: rasdaman.WCSGetCapabilitiesController
                    },
                    'describe_coverage@services': {
                        url: "describe_coverage",
                        templateUrl: 'ows/src/components/wcs_component/describe_coverage/DescribeCoverageView.html',
                        controller: rasdaman.WCSDescribeCoverageController
                    },
                    'get_coverage@services': {
                        templateUrl: 'ows/src/components/wcs_component/get_coverage/GetCoverageView.html',
                        controller: rasdaman.WCSGetCoverageController
                    },
                    'process_coverages@services': {
                        templateUrl: 'ows/src/components/wcs_component/process_coverage/ProcessCoverageView.html',
                        controller: rasdaman.WCSProcessCoverageController
                    },
                    'insert_coverage@services': {
                        templateUrl: 'ows/src/components/wcs_component/insert_coverage/InsertCoverageView.html',
                        controller: rasdaman.WCSInsertCoverageController
                    },
                    'delete_coverage@services': {
                        templateUrl: 'ows/src/components/wcs_component/delete_coverage/DeleteCoverageView.html',
                        controller: rasdaman.WCSDeleteCoverageController
                    },
                    'wms_get_capabilities@services': {
                        url: "wms_get_capabilities",
                        templateUrl: 'ows/src/components/wms_component/get_capabilities/GetCapabilitiesView.html',
                        controller: rasdaman.WMSGetCapabilitiesController
                    },
                    'wms_describe_layer@services': {
                        url: "wms_describe_layer",
                        templateUrl: 'ows/src/components/wms_component/describe_layer/DescribeLayerView.html',
                        controller: rasdaman.WMSDescribeLayerController
                    },
                    'admin_login@services': {
                        url: "admin_login",
                        templateUrl: 'ows/src/components/admin_component/login/AdminLoginView.html',
                        controller: rasdaman.AdminLoginController
                    },
                    'admin_ows_metadata_management@services': {
                        url: "admin_ows_metadata_management",
                        templateUrl: 'ows/src/components/admin_component/ows_metadata_management/AdminOWSMetadataManagementView.html',
                        controller: rasdaman.AdminOWSMetadataManagementController
                    }
                }
            });
            NotificationProvider.setOptions({
                delay: 10000,
                startTop: 20,
                startRight: 10,
                verticalSpacing: 20,
                horizontalSpacing: 20,
                positionX: 'right',
                positionY: 'top'
            });
            $.fn.followTo = function (pos) {
                var $window = $(Window);
                $window.scroll(function (e) {
                    if ($window.scrollTop() > pos) {
                        $('body').css('background-attachment', 'fixed');
                        $('body').css('background-position', 'top -201px center');
                    }
                    else {
                        $('body').css('background-attachment', 'absolute');
                        $('body').css('background-position', 'top ' + -$window.scrollTop() + 'px center');
                    }
                });
            };
        }
        AngularConfig.$inject = [
            "$httpProvider",
            "$stateProvider",
            "$locationProvider",
            "$urlRouterProvider",
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
        .service("rasdaman.common.SerializedObjectFactory", rasdaman.common.SerializedObjectFactory)
        .service("rasdaman.CredentialService", rasdaman.CredentialService)
        .service("rasdaman.WCSService", rasdaman.WCSService)
        .service("rasdaman.WCSSettingsService", rasdaman.WCSSettingsService)
        .service("rasdaman.WMSService", rasdaman.WMSService)
        .service("rasdaman.WMSSettingsService", rasdaman.WMSSettingsService)
        .service("rasdaman.AdminService", rasdaman.AdminService)
        .service("rasdaman.WebWorldWindService", rasdaman.WebWorldWindService)
        .service("rasdaman.ErrorHandlingService", rasdaman.ErrorHandlingService)
        .controller("rasdaman.RootController", rasdaman.RootController)
        .controller("rasdaman.LoginController", rasdaman.LoginController)
        .controller("rasdaman.WCSMainController", rasdaman.WCSMainController)
        .controller("rasdaman.WCSSettingsController", rasdaman.WCSSettingsController)
        .controller("rasdaman.WCSGetCapabilitiesController", rasdaman.WCSGetCapabilitiesController)
        .controller("rasdaman.WCSDescribeCoverageController", rasdaman.WCSDescribeCoverageController)
        .controller("rasdaman.WCSDeleteCoverageController", rasdaman.WCSDeleteCoverageController)
        .controller("rasdaman.WCSGetCoverageController", rasdaman.WCSGetCoverageController)
        .controller("rasdaman.WCSProcessCoverageController", rasdaman.WCSProcessCoverageController)
        .controller("rasdaman.WMSMainController", rasdaman.WMSMainController)
        .controller("rasdaman.AdminMainController", rasdaman.AdminMainController)
        .directive("rangeSubsettingExtension", rasdaman.WCSRangeSubsettingExtension)
        .directive("scalingExtension", rasdaman.WCSScalingExtension)
        .directive("interpolationExtension", rasdaman.WCSInterpolationExtension)
        .directive("crsExtension", rasdaman.WCSCRSExtension)
        .directive("clippingExtension", rasdaman.WCSClippingExtension)
        .directive("wwdDisplay", rasdaman.WebWorldWindDisplayWidget)
        .directive("rasPrettyPrint", rasdaman.common.PrettyPrint)
        .directive("stringToNumberConverter", rasdaman.common.StringToNumberConverter)
        .directive("autocomplete", rasdaman.common.Autocomplete)
        .directive("scrollToBottom", rasdaman.common.scrollToBottom)
        .directive("getFilteredRows", rasdaman.common.getFilteredRows);
})(rasdaman || (rasdaman = {}));
var admin;
(function (admin) {
    var ServiceIdentification = (function () {
        function ServiceIdentification(serviceTitle, abstract) {
            this.serviceTitle = serviceTitle;
            this.abstract = abstract;
        }
        ServiceIdentification.prototype.toKVP = function () {
            return "serviceTitle=" + this.serviceTitle +
                "&abstract=" + this.abstract;
        };
        return ServiceIdentification;
    }());
    admin.ServiceIdentification = ServiceIdentification;
})(admin || (admin = {}));
var admin;
(function (admin) {
    var ServiceProvider = (function () {
        function ServiceProvider(providerName, providerSite, individualName, positionName, role, email, voicePhone, facsimilePhone, hoursOfService, contactInstructions, city, administrativeArea, postalCode, country) {
            this.providerName = providerName;
            this.providerSite = providerSite;
            this.individualName = individualName;
            this.positionName = positionName;
            this.role = role;
            this.email = email;
            this.voicePhone = voicePhone;
            this.facsimilePhone = facsimilePhone;
            this.hoursOfService = hoursOfService;
            this.contactInstructions = contactInstructions;
            this.city = city;
            this.administrativeArea = administrativeArea;
            this.postalCode = postalCode;
            this.country = country;
        }
        ServiceProvider.prototype.toKVP = function () {
            return "providerName=" + this.providerName +
                "&providerSite=" + this.providerSite +
                "&individualName=" + this.individualName +
                "&positionName=" + this.positionName +
                "&role=" + this.role +
                "&email=" + this.email +
                "&voicePhone=" + this.voicePhone +
                "&facsimilePhone=" + this.facsimilePhone +
                "&hoursOfService=" + this.hoursOfService +
                "&contactInstructions=" + this.contactInstructions +
                "&city=" + this.city +
                "&administrativeArea=" + this.administrativeArea +
                "&postalCode=" + this.postalCode +
                "&country=" + this.country;
        };
        return ServiceProvider;
    }());
    admin.ServiceProvider = ServiceProvider;
})(admin || (admin = {}));
var wms;
(function (wms) {
    var CreatePyramidMember = (function () {
        function CreatePyramidMember(baseCoverageId, scaleFactors, pyramidMemberCoverageId) {
            this.baseCoverageId = baseCoverageId;
            this.scaleFactors = scaleFactors;
            this.pyramidMemberCoverageId = pyramidMemberCoverageId;
        }
        CreatePyramidMember.prototype.toKVP = function () {
            return "&COVERAGEID=" + this.baseCoverageId +
                "&MEMBER=" + this.pyramidMemberCoverageId +
                "&SCALEVECTOR=" + this.scaleFactors;
        };
        return CreatePyramidMember;
    }());
    wms.CreatePyramidMember = CreatePyramidMember;
})(wms || (wms = {}));
var wms;
(function (wms) {
    var DeleteLayerStyle = (function () {
        function DeleteLayerStyle(layerName, name) {
            this.layerName = layerName;
            this.name = name;
        }
        DeleteLayerStyle.prototype.toKVP = function () {
            return "COVERAGEID=" + this.layerName +
                "&STYLEID=" + this.name;
        };
        return DeleteLayerStyle;
    }());
    wms.DeleteLayerStyle = DeleteLayerStyle;
})(wms || (wms = {}));
var wms;
(function (wms) {
    var GetMap = (function () {
        function GetMap(layers, bbox, width, height, styles) {
            this.layers = layers;
            this.bbox = bbox;
            this.width = width;
            this.height = height;
            this.styles = styles;
        }
        GetMap.prototype.toKVP = function () {
            return "request=" + "GetMap&layers=" + this.layers + "&bbox=" + this.bbox +
                "&width=" + this.width + "&height=" + this.height + "&crs=EPSG:4326&format=image/png&transparent=true&styles=" + this.styles;
        };
        return GetMap;
    }());
    wms.GetMap = GetMap;
})(wms || (wms = {}));
var wms;
(function (wms) {
    var InsertLayerStyle = (function () {
        function InsertLayerStyle(layerName, name, abstract, queryType, query, colorTableType, colorTableDefintion, defaultStyle, legendGraphicBase64) {
            this.layerName = layerName;
            this.name = name;
            this.abstract = abstract;
            this.queryFragmentType = queryType;
            this.query = query;
            this.colorTableType = colorTableType;
            this.colorTableDefinition = colorTableDefintion;
            this.defaultStyle = defaultStyle;
            this.legendGraphicBase64 = legendGraphicBase64;
        }
        InsertLayerStyle.prototype.toKVP = function () {
            var result = "COVERAGEID=" + this.layerName +
                "&STYLEID=" + this.name +
                "&abstract=" + this.abstract;
            if (this.queryFragmentType != "none") {
                result += "&" + this.queryFragmentType + "=" + this.query;
            }
            if (this.colorTableType != "none") {
                result += "&ColorTableType=" + this.colorTableType +
                    "&ColorTableDefinition=" + this.colorTableDefinition;
            }
            result += "&default=" + this.defaultStyle;
            if (this.legendGraphicBase64 != null) {
                result += "&legendGraphic=" + this.legendGraphicBase64;
            }
            return result;
        };
        return InsertLayerStyle;
    }());
    wms.InsertLayerStyle = InsertLayerStyle;
})(wms || (wms = {}));
var wms;
(function (wms) {
    var ListPyramidMembers = (function () {
        function ListPyramidMembers(layerName) {
            this.base = layerName;
        }
        ListPyramidMembers.prototype.toKVP = function () {
            return "COVERAGEID=" + this.base;
        };
        return ListPyramidMembers;
    }());
    wms.ListPyramidMembers = ListPyramidMembers;
})(wms || (wms = {}));
var wms;
(function (wms) {
    var PyramidCoverageMember = (function () {
        function PyramidCoverageMember(coverageId, scaleFactors) {
            this.coverageId = coverageId;
            this.scaleFactors = scaleFactors;
        }
        return PyramidCoverageMember;
    }());
    wms.PyramidCoverageMember = PyramidCoverageMember;
})(wms || (wms = {}));
var wms;
(function (wms) {
    var RemovePyramidMember = (function () {
        function RemovePyramidMember(baseCoverageId, pyramidMemberCoverageId) {
            this.baseCoverageId = baseCoverageId;
            this.pyramidMemberCoverageId = pyramidMemberCoverageId;
        }
        RemovePyramidMember.prototype.toKVP = function () {
            return "COVERAGEID=" + this.baseCoverageId +
                "&MEMBERS=" + this.pyramidMemberCoverageId;
        };
        return RemovePyramidMember;
    }());
    wms.RemovePyramidMember = RemovePyramidMember;
})(wms || (wms = {}));
var wms;
(function (wms) {
    var Style = (function () {
        function Style(name, abstract, queryType, query, colorTableType, colorTableDefinition, defaultStyle, legendGraphicURL) {
            this.defaultStyle = false;
            this.name = name;
            this.abstract = abstract;
            this.queryType = queryType;
            this.query = query;
            this.colorTableType = colorTableType;
            this.colorTableDefinition = colorTableDefinition;
            this.defaultStyle = defaultStyle;
            this.legendGraphicURL = legendGraphicURL;
        }
        return Style;
    }());
    wms.Style = Style;
})(wms || (wms = {}));
var wms;
(function (wms) {
    var UpdateLayerStyle = (function () {
        function UpdateLayerStyle(layerName, name, abstract, queryType, query, colorTableType, colorTableDefintion, defaultStyle, legendGraphicBase64) {
            this.layerName = layerName;
            this.name = name;
            this.abstract = abstract;
            this.queryFragmentType = queryType;
            this.query = query;
            this.colorTableType = colorTableType;
            this.colorTableDefinition = colorTableDefintion;
            this.defaultStyle = defaultStyle;
            this.legendGraphicBase64 = legendGraphicBase64;
        }
        UpdateLayerStyle.prototype.toKVP = function () {
            var result = "COVERAGEID=" + this.layerName +
                "&STYLEID=" + this.name +
                "&abstract=" + this.abstract;
            if (this.queryFragmentType != "none") {
                result += "&" + this.queryFragmentType + "=" + this.query;
            }
            if (this.colorTableType != "none") {
                result += "&ColorTableType=" + this.colorTableType +
                    "&ColorTableDefinition=" + this.colorTableDefinition;
            }
            result += "&default=" + this.defaultStyle;
            if (this.legendGraphicBase64 != null) {
                result += "&legendGraphic=" + this.legendGraphicBase64;
            }
            return result;
        };
        return UpdateLayerStyle;
    }());
    wms.UpdateLayerStyle = UpdateLayerStyle;
})(wms || (wms = {}));
//# sourceMappingURL=main.js.map
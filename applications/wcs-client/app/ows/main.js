var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        class Exception extends Error {
            constructor(message) {
                super(message);
                this.name = "Exception";
                this.message = message;
                this.stack = new Error().stack;
            }
            toString() {
                return this.name + ": " + this.message;
            }
        }
        common.Exception = Exception;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        class InvalidAttributeNameException extends common.Exception {
            constructor(attributeName) {
                super("The attribute \"" + attributeName + "\" does not exist on this element.");
            }
        }
        common.InvalidAttributeNameException = InvalidAttributeNameException;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        class InvalidElementNameException extends common.Exception {
            constructor(elementName) {
                super("The child element \"" + elementName + "\" does not exist on this element.");
            }
        }
        common.InvalidElementNameException = InvalidElementNameException;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        class LogicException extends common.Exception {
            constructor(message) {
                super(message);
                this.name = "LogicException";
            }
        }
        common.LogicException = LogicException;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        class NotImplementedException extends common.Exception {
            constructor() {
                super("The method was not implemented.");
                this.name = "NotImplementedException";
            }
        }
        common.NotImplementedException = NotImplementedException;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        class IllegalArgumentException extends common.Exception {
            constructor(message) {
                super(message);
                this.name = "IllegalArgumentException";
            }
        }
        common.IllegalArgumentException = IllegalArgumentException;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        class ArgumentValidator {
            static isNotNull(arg, argName) {
                if (!arg) {
                    throw new common.IllegalArgumentException(argName);
                }
            }
            static isNotEmpty(arg, argName) {
                if (!arg) {
                    throw new common.IllegalArgumentException(argName);
                }
            }
            static isArray(arg, argName) {
                if (!Array.isArray(arg)) {
                    throw new common.IllegalArgumentException(argName);
                }
            }
        }
        common.ArgumentValidator = ArgumentValidator;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        class ImageUtilities {
            static arrayBufferToBase64(arrayBuffer) {
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
            }
        }
        common.ImageUtilities = ImageUtilities;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        let ResponseDocumentType;
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
        class ResponseDocument {
            constructor(value, responseType) {
                this.value = value;
                this.type = responseType;
            }
        }
        common.ResponseDocument = ResponseDocument;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        class Response {
            constructor(document, value) {
                this.document = document;
                this.value = value;
            }
        }
        common.Response = Response;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        class XMLSerializedObject {
            constructor(documentOrObject) {
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
            doesAttributeExist(attributeName) {
                var resolvedAttrName = this.resolveAttributeName(attributeName);
                return (this.jsonObject._attr && typeof this.jsonObject._attr[resolvedAttrName] != "undefined");
            }
            doesElementExist(elementName) {
                var resolvedElementName = this.resolveElementName(elementName);
                return (this.jsonObject[resolvedElementName]
                    && typeof this.jsonObject[resolvedElementName][0] != "undefined");
            }
            getAttributeAsBool(attributeName) {
                var resolvedAttrName = this.resolveAttributeName(attributeName);
                if (!this.doesAttributeExist(resolvedAttrName)) {
                    throw new common.InvalidAttributeNameException(attributeName);
                }
                else {
                    return this.jsonObject._attr[resolvedAttrName]._value ? true : false;
                }
            }
            getAttributeAsNumber(attributeName) {
                var resolvedAttrName = this.resolveAttributeName(attributeName);
                if (!this.doesAttributeExist(resolvedAttrName)) {
                    throw new common.InvalidAttributeNameException(attributeName);
                }
                else {
                    return this.jsonObject._attr[resolvedAttrName]._value;
                }
            }
            getAttributeAsString(attributeName) {
                var resolvedAttrName = this.resolveAttributeName(attributeName);
                if (!this.doesAttributeExist(resolvedAttrName)) {
                    throw new common.InvalidAttributeNameException(attributeName);
                }
                else {
                    return this.jsonObject._attr[resolvedAttrName]._value;
                }
            }
            getValueAsBool() {
                if (typeof this.jsonObject._text == "undefined") {
                    throw new common.LogicException("The object does not have a boolean value.");
                }
                return this.jsonObject._text ? true : false;
            }
            getValueAsNumber() {
                if (typeof (this.jsonObject._text) == "undefined") {
                    throw new common.LogicException("The object does not have a number value.");
                }
                return this.jsonObject._text;
            }
            getValueAsString() {
                if (typeof this.jsonObject._text == "undefined") {
                    throw new common.LogicException("The object does not have a string value.");
                }
                return this.jsonObject._text.toString();
            }
            getChildAsSerializedObject(elementName) {
                var resolvedElementName = this.resolveElementName(elementName);
                if (!this.doesElementExist(resolvedElementName)) {
                    throw new common.InvalidElementNameException(elementName);
                }
                else {
                    return new XMLSerializedObject(this.jsonObject[resolvedElementName][0]);
                }
            }
            getChildrenAsSerializedObjects(elementName) {
                var resolvedElementName = this.resolveElementName(elementName);
                var result = [];
                if (typeof this.jsonObject[resolvedElementName] != "undefined") {
                    for (var i = 0; i < this.jsonObject[resolvedElementName].length; ++i) {
                        result.push(new XMLSerializedObject(this.jsonObject[resolvedElementName][i]));
                    }
                }
                return result;
            }
            resolveAttributeName(attrName) {
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
            }
            resolveElementName(elementName) {
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
            }
        }
        common.XMLSerializedObject = XMLSerializedObject;
    })(common = rasdaman.common || (rasdaman.common = {}));
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    var common;
    (function (common) {
        class SerializedObjectFactory {
            constructor() {
            }
            getSerializedObject(document) {
                if (document.type == common.ResponseDocumentType.XML) {
                    return new common.XMLSerializedObject(document);
                }
                else {
                    throw new common.NotImplementedException();
                }
            }
        }
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
                    scope.$watch("data", (newData, oldValue) => {
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
                    scope.$watch("source", (newValue, oldValue) => {
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
    class Constants {
    }
    Constants.APP_NAME = "wcsClient";
    Constants.PROCESSING_EXT_URI = "http://www.opengis.net/spec/WCS_service-extension_processing/2.0/conf/processing";
    Constants.TRANSACTION_EXT_URI = "http://www.opengis.net/spec/WCS_service-extension_transaction/2.0/conf/insert+delete";
    Constants.RANGE_SUBSETTING_EXT_URI = "http://www.opengis.net/spec/WCS_service-extension_range-subsetting/1.0/conf/record-subsetting";
    Constants.SCALING_EXT_URI = "http://www.opengis.net/spec/WCS_service-extension_scaling/1.0/conf/scaling";
    Constants.INTERPOLATION_EXT_URI = "http://www.opengis.net/spec/WCS_service-extension_interpolation/1.0/conf/interpolation";
    Constants.CRS_EXT_URI = "http://www.opengis.net/spec/WCS_service-extension_crs/1.0/conf/crs";
    rasdaman.Constants = Constants;
})(rasdaman || (rasdaman = {}));
var ows;
(function (ows) {
    class Address {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.deliveryPoint = [];
            source.getChildrenAsSerializedObjects("ows:DeliveryPoint").forEach(o => {
                this.deliveryPoint.push(o.getValueAsString());
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
            source.getChildrenAsSerializedObjects("ows:ElectronicMailAddress").forEach(o => {
                this.electronicMailAddress.push(o.getValueAsString());
            });
        }
    }
    ows.Address = Address;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class BoundingBox {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
    }
    ows.BoundingBox = BoundingBox;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class LanguageString {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.value = source.getValueAsString();
            if (source.doesAttributeExist("xml:lang")) {
                this.lang = source.getAttributeAsString("xml:lang");
            }
        }
    }
    ows.LanguageString = LanguageString;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class OnlineResource {
        constructor(source) {
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
    }
    ows.OnlineResource = OnlineResource;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class Uri {
        constructor(uri) {
        }
    }
    ows.Uri = Uri;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class Code {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.code = source.getValueAsString();
            if (source.doesAttributeExist("codeSpace")) {
                this.codeSpace = new ows.Uri(source.getAttributeAsString("codeSpace"));
            }
        }
    }
    ows.Code = Code;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class Keywords {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.keyword = [];
            source.getChildrenAsSerializedObjects("ows:Keyword").forEach(s => {
                this.keyword.push(new ows.LanguageString(s));
            });
            this.type = new ows.Code(source.getChildAsSerializedObject("ows:Type"));
        }
    }
    ows.Keywords = Keywords;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class Description {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.title = [];
            source.getChildrenAsSerializedObjects("ows:Title").forEach(s => {
                this.title.push(new ows.LanguageString(s));
            });
            this.abstract = [];
            source.getChildrenAsSerializedObjects("ows:Abstract").forEach(s => {
                this.abstract.push(new ows.LanguageString(s));
            });
            this.keywords = [];
            source.getChildrenAsSerializedObjects("ows:Keywords").forEach(s => {
                this.keywords.push(new ows.Keywords(s));
            });
        }
    }
    ows.Description = Description;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class ServiceIdentification extends ows.Description {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            super(source);
            this.serviceType = new ows.Code(source.getChildAsSerializedObject("ServiceType"));
            this.serviceTypeVersion = [];
            source.getChildrenAsSerializedObjects("ows:ServiceTypeVersion").forEach(s => {
                this.serviceTypeVersion.push(s.getValueAsString());
            });
            this.profile = [];
            source.getChildrenAsSerializedObjects("ows:Profile").forEach(s => {
                this.profile.push(s.getValueAsString());
            });
            if (source.doesElementExist("ows:Fees")) {
                this.fees = source.getChildAsSerializedObject("ows:Fees").getValueAsString();
            }
            if (source.doesElementExist("ows:AccessConstraints")) {
                this.accessConstraints = source.getChildAsSerializedObject("ows:AccessConstraints").getValueAsString();
            }
        }
    }
    ows.ServiceIdentification = ServiceIdentification;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class Phone {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.voice = [];
            source.getChildrenAsSerializedObjects("ows:Voice").forEach(s => {
                this.voice.push(s.getValueAsString());
            });
            this.facsimile = [];
            source.getChildrenAsSerializedObjects("ows:Facsimile").forEach(s => {
                this.facsimile.push(s.getValueAsString());
            });
        }
    }
    ows.Phone = Phone;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class ContactInfo {
        constructor(source) {
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
    }
    ows.ContactInfo = ContactInfo;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class ResponsiblePartySubset {
        constructor(source) {
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
    }
    ows.ResponsiblePartySubset = ResponsiblePartySubset;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class ServiceContact extends ows.ResponsiblePartySubset {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            super(source);
        }
    }
    ows.ServiceContact = ServiceContact;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class ServiceProvider {
        constructor(source) {
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
    }
    ows.ServiceProvider = ServiceProvider;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class Constraint {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
    }
    ows.Constraint = Constraint;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class RequestMethod extends ows.OnlineResource {
        constructor(source) {
            super(source);
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.constraint = [];
            source.getChildrenAsSerializedObjects("ows:Constraint").forEach(o => {
                this.constraint.push(new ows.Constraint(o));
            });
        }
    }
    ows.RequestMethod = RequestMethod;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class Get extends ows.RequestMethod {
        constructor(source) {
            super(source);
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
    }
    ows.Get = Get;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class Post extends ows.RequestMethod {
        constructor(source) {
            super(source);
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
    }
    ows.Post = Post;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class HTTP {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.get = new ows.Get(source.getChildAsSerializedObject("ows:Get"));
            this.post = new ows.Post(source.getChildAsSerializedObject("ows:Post"));
        }
    }
    ows.HTTP = HTTP;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class DCP {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.http = new ows.HTTP(source.getChildAsSerializedObject("ows:HTTP"));
        }
    }
    ows.DCP = DCP;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class Parameter {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
    }
    ows.Parameter = Parameter;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class Metadata {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
    }
    ows.Metadata = Metadata;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class Operation {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.name = source.getAttributeAsString("name");
            this.dcp = [];
            source.getChildrenAsSerializedObjects("ows:DCP").forEach(o => {
                this.dcp.push(new ows.DCP(o));
            });
            this.parameter = [];
            source.getChildrenAsSerializedObjects("ows:Parameter").forEach(o => {
                this.parameter.push(new ows.Parameter(o));
            });
            this.constraint = [];
            source.getChildrenAsSerializedObjects("ows:Constraint").forEach(o => {
                this.constraint.push(new ows.Constraint(o));
            });
            this.metadata = [];
            source.getChildrenAsSerializedObjects("ows:Metadata").forEach(o => {
                this.metadata.push(new ows.Metadata(o));
            });
        }
    }
    ows.Operation = Operation;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class ExtendedCapabilities {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
    }
    ows.ExtendedCapabilities = ExtendedCapabilities;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class OperationsMetadata {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.operation = [];
            source.getChildrenAsSerializedObjects("ows:Operation").forEach(o => {
                this.operation.push(new ows.Operation(o));
            });
            this.parameter = [];
            source.getChildrenAsSerializedObjects("ows:Parameter").forEach(o => {
                this.parameter.push(new ows.Parameter(o));
            });
            this.constraint = [];
            source.getChildrenAsSerializedObjects("ows:Constraint").forEach(o => {
                this.constraint.push(new ows.Constraint(o));
            });
            if (source.doesElementExist("ows:ExtendedCapabilities")) {
                this.extendedCapabilities = new ows.ExtendedCapabilities(source.getChildAsSerializedObject("ows:ExtendedCapabilities"));
            }
        }
    }
    ows.OperationsMetadata = OperationsMetadata;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class Languages {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
    }
    ows.Languages = Languages;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class CapabilitiesBase {
        constructor(source) {
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
    }
    ows.CapabilitiesBase = CapabilitiesBase;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class ContentsBase {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
    }
    ows.ContentsBase = ContentsBase;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class Section {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
    }
    ows.Section = Section;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class GetCapabilities {
        constructor() {
            this.request = "GetCapabilities";
        }
    }
    ows.GetCapabilities = GetCapabilities;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class Exception {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.exceptionText = source.getChildAsSerializedObject("ExceptionText").getValueAsString();
            if (source.doesAttributeExist("exceptionCode")) {
                this.exceptionCode = source.getAttributeAsString("exceptionCode");
            }
            if (source.doesAttributeExist("locator")) {
                this.locator = source.getAttributeAsString("locator");
            }
        }
    }
    ows.Exception = Exception;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class ExceptionReport {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.exception = new ows.Exception(source.getChildAsSerializedObject("Exception"));
        }
    }
    ows.ExceptionReport = ExceptionReport;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class WGS84BoundingBox {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.lowerCorner = source.getChildAsSerializedObject("ows:LowerCorner").getValueAsString();
            this.upperCorner = source.getChildAsSerializedObject("ows:UpperCorner").getValueAsString();
        }
    }
    ows.WGS84BoundingBox = WGS84BoundingBox;
})(ows || (ows = {}));
var ows;
(function (ows) {
    class CustomizedMetadata {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.parseCoverageLocation(source);
            this.parseCoverageSizeInBytes(source);
        }
        parseCoverageLocation(source) {
            let childElement = "rasdaman:location";
            if (source.doesElementExist(childElement)) {
                let locationElement = source.getChildAsSerializedObject(childElement);
                this.hostname = locationElement.getChildAsSerializedObject("rasdaman:hostname").getValueAsString();
                this.petascopeEndPoint = locationElement.getChildAsSerializedObject("rasdaman:endpoint").getValueAsString();
            }
        }
        parseCoverageSizeInBytes(source) {
            let childElement = "rasdaman:sizeInBytes";
            if (source.doesElementExist(childElement)) {
                let sizeInBytesElement = source.getChildAsSerializedObject(childElement);
                let sizeInBytes = sizeInBytesElement.getValueAsString();
                this.coverageSize = CustomizedMetadata.convertNumberOfBytesToHumanReadable(sizeInBytes);
                if (this.hostname === undefined) {
                    this.localCoverageSizeInBytes = sizeInBytesElement.getValueAsNumber();
                }
                else {
                    this.remoteCoverageSizeInBytes = sizeInBytesElement.getValueAsNumber();
                }
            }
            else {
                this.coverageSize = "N/A";
            }
        }
        static convertNumberOfBytesToHumanReadable(numberOfBytes) {
            if (numberOfBytes == 0) {
                return "0 B";
            }
            const k = 1000;
            const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
            let i = Math.floor(Math.log(numberOfBytes) / Math.log(k));
            let result = parseFloat((numberOfBytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
            return result;
        }
    }
    ows.CustomizedMetadata = CustomizedMetadata;
})(ows || (ows = {}));
var wcs;
(function (wcs) {
    class Extension {
        constructor(source) {
            if (source.doesElementExist("int:InterpolationMetadata")) {
                this.interpolationMetadata = new wcs.InterpolationMetadata(source.getChildAsSerializedObject("int:InterpolationMetadata"));
            }
        }
    }
    wcs.Extension = Extension;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class ServiceMetadata {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.formatSupported = [];
            source.getChildrenAsSerializedObjects("wcs:formatSupported").forEach(o => {
                this.formatSupported.push(o.getValueAsString());
            });
            this.extension = [];
            source.getChildrenAsSerializedObjects("wcs:Extension").forEach(o => {
                this.extension.push(new wcs.Extension(o));
            });
        }
    }
    wcs.ServiceMetadata = ServiceMetadata;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class CoverageSubtypeParent {
        constructor(source) {
        }
    }
    wcs.CoverageSubtypeParent = CoverageSubtypeParent;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class CoverageSummary extends ows.Description {
        constructor(source) {
            super(source);
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.displayFootprint = null;
            this.importedType = "local";
            this.coverageId = source.getChildAsSerializedObject("wcs:CoverageId").getValueAsString();
            this.coverageSubtype = source.getChildAsSerializedObject("wcs:CoverageSubtype").getValueAsString();
            let childElement = "wcs:CoverageSubtypeParent";
            if (source.doesElementExist(childElement)) {
                this.coverageSubtypeParent = new wcs.CoverageSubtypeParent(source.getChildAsSerializedObject(childElement));
            }
            childElement = "ows:WGS84BoundingBox";
            if (source.doesElementExist(childElement)) {
                this.wgs84BoundingBox = new ows.WGS84BoundingBox(source.getChildAsSerializedObject(childElement));
            }
            childElement = "ows:BoundingBox";
            if (source.doesElementExist(childElement)) {
                this.boundingBox = new ows.BoundingBox(source.getChildAsSerializedObject(childElement));
            }
            childElement = "ows:Metadata";
            if (source.doesElementExist(childElement)) {
                this.customizedMetadata = new ows.CustomizedMetadata(source.getChildAsSerializedObject(childElement));
                if (this.customizedMetadata.hostname != null) {
                    this.importedType = "remote";
                }
            }
        }
    }
    wcs.CoverageSummary = CoverageSummary;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class Contents extends ows.ContentsBase {
        constructor(source) {
            super(source);
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.coverageSummaries = [];
            let totalLocalCoverageSizesInBytes = 0;
            let totalRemoteCoverageSizesInBytes = 0;
            let totalCoverageSizesInBytes = 0;
            source.getChildrenAsSerializedObjects("wcs:CoverageSummary").forEach(o => {
                let coverageSummary = new wcs.CoverageSummary(o);
                this.coverageSummaries.push(coverageSummary);
                if (coverageSummary.customizedMetadata != null) {
                    if (coverageSummary.customizedMetadata.hostname != null) {
                        this.showCoverageLocationsColumn = true;
                    }
                    if (coverageSummary.customizedMetadata.coverageSize != "N/A") {
                        this.showCoverageSizesColumn = true;
                        if (coverageSummary.customizedMetadata.hostname === undefined) {
                            totalLocalCoverageSizesInBytes += coverageSummary.customizedMetadata.localCoverageSizeInBytes;
                        }
                        else {
                            totalRemoteCoverageSizesInBytes += coverageSummary.customizedMetadata.remoteCoverageSizeInBytes;
                        }
                    }
                }
            });
            totalCoverageSizesInBytes += totalLocalCoverageSizesInBytes + totalRemoteCoverageSizesInBytes;
            this.totalLocalCoverageSizes = ows.CustomizedMetadata.convertNumberOfBytesToHumanReadable(totalLocalCoverageSizesInBytes);
            this.totalRemoteCoverageSizes = ows.CustomizedMetadata.convertNumberOfBytesToHumanReadable(totalRemoteCoverageSizesInBytes);
            this.totalCoverageSizes = ows.CustomizedMetadata.convertNumberOfBytesToHumanReadable(totalCoverageSizesInBytes);
            if (source.doesElementExist("wcs:Extension")) {
                this.extension = new wcs.Extension(source.getChildAsSerializedObject("wcs:Extension"));
            }
        }
    }
    wcs.Contents = Contents;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class Capabilities extends ows.CapabilitiesBase {
        constructor(source) {
            super(source);
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            if (source.doesElementExist("wcs:ServiceMetadata")) {
                this.serviceMetadata = new wcs.ServiceMetadata(source.getChildAsSerializedObject("wcs:ServiceMetadata"));
            }
            if (source.doesElementExist("wcs:Contents")) {
                this.contents = new wcs.Contents(source.getChildAsSerializedObject("wcs:Contents"));
            }
        }
    }
    wcs.Capabilities = Capabilities;
})(wcs || (wcs = {}));
var gml;
(function (gml) {
    class Pos {
        constructor(source) {
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
            stringValues.forEach(o => {
                this.values.push(parseFloat(o));
            });
        }
    }
    gml.Pos = Pos;
})(gml || (gml = {}));
var gml;
(function (gml) {
    class LowerCorner {
        constructor(source) {
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
            stringValues.forEach(o => {
                this.values.push(o);
            });
        }
    }
    gml.LowerCorner = LowerCorner;
})(gml || (gml = {}));
var gml;
(function (gml) {
    class UpperCorner {
        constructor(source) {
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
            stringValues.forEach(o => {
                this.values.push(o);
            });
        }
    }
    gml.UpperCorner = UpperCorner;
})(gml || (gml = {}));
var gml;
(function (gml) {
    class Envelope {
        constructor(source) {
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
    }
    gml.Envelope = Envelope;
})(gml || (gml = {}));
var gml;
(function (gml) {
    class EnvelopeWithTimePeriod extends gml.Envelope {
        constructor(source) {
            super(source);
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
    }
    gml.EnvelopeWithTimePeriod = EnvelopeWithTimePeriod;
})(gml || (gml = {}));
var gml;
(function (gml) {
    class BoundedBy {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            if (source.doesElementExist("gml:Envelope")) {
                this.envelope = new gml.Envelope(source.getChildAsSerializedObject("gml:Envelope"));
            }
            if (source.doesElementExist("gml:EnvelopeWithTimePeriod")) {
                this.envelopeWithTimePeriod = new gml.EnvelopeWithTimePeriod(source.getChildAsSerializedObject("gml:EnvelopeWithTimePeriod"));
                this.envelope = this.envelopeWithTimePeriod;
            }
        }
    }
    gml.BoundedBy = BoundedBy;
})(gml || (gml = {}));
var gml;
(function (gml) {
    class CoverageFunction {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
    }
    gml.CoverageFunction = CoverageFunction;
})(gml || (gml = {}));
var gml;
(function (gml) {
    class DomainSet {
        constructor(source) {
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
    }
    gml.DomainSet = DomainSet;
})(gml || (gml = {}));
var gml;
(function (gml) {
    class AbstractGridCoverage {
        constructor(source) {
            this.offsetVectors = [];
            this.axisTypes = [];
            this.REGULAR_AXIS = "Regular Axis";
            this.IRREGULAR_AXIS = "Irregular Axis";
            this.IRREGULAR_AXIS_RESOLUTION = "N/A";
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }
        buildObj() {
            this.parseGridEnvelope();
            this.parseAxisTypesAndOffsetVectors();
        }
        parseGridEnvelope() {
            this.gridEnvelope = new gml.GridEnvelope(this.currentSource.getChildAsSerializedObject("gml:limits"));
        }
    }
    gml.AbstractGridCoverage = AbstractGridCoverage;
})(gml || (gml = {}));
var gml;
(function (gml) {
    class GridCoverage extends gml.AbstractGridCoverage {
        constructor(source) {
            super(source);
            this.currentSource = source.getChildAsSerializedObject("gml:Grid");
        }
        parseAxisTypesAndOffsetVectors() {
            let numberOfDimensions = this.currentSource.getAttributeAsNumber("dimension");
            for (let i = 0; i < numberOfDimensions; i++) {
                this.axisTypes[i] = this.REGULAR_AXIS;
                this.offsetVectors[i] = "1";
            }
        }
    }
    gml.GridCoverage = GridCoverage;
})(gml || (gml = {}));
var gml;
(function (gml) {
    class RectifiedGridCoverage extends gml.AbstractGridCoverage {
        constructor(source) {
            super(source);
            this.currentSource = source.getChildAsSerializedObject("gml:RectifiedGrid");
        }
        parseAxisTypesAndOffsetVectors() {
            this.currentSource.getChildrenAsSerializedObjects("offsetVector").forEach((element) => {
                this.axisTypes.push(this.REGULAR_AXIS);
                let tmpArray = element.getValueAsString().split(" ");
                for (let i = 0; i < tmpArray.length; i++) {
                    if (tmpArray[i] != "0") {
                        this.offsetVectors.push(tmpArray[i]);
                        break;
                    }
                }
            });
        }
    }
    gml.RectifiedGridCoverage = RectifiedGridCoverage;
})(gml || (gml = {}));
var gml;
(function (gml) {
    class ReferenceableGridCoverage extends gml.AbstractGridCoverage {
        constructor(source) {
            super(source);
            this.currentSource = source.getChildAsSerializedObject("gmlrgrid:ReferenceableGridByVectors");
        }
        parseAxisTypesAndOffsetVectors() {
            this.currentSource.getChildrenAsSerializedObjects("gmlrgrid:generalGridAxis").forEach((element) => {
                let coefficientsElement = element.getChildAsSerializedObject("gmlrgrid:GeneralGridAxis").getChildAsSerializedObject("gmlrgrid:coefficients");
                if (coefficientsElement.getValueAsString() === "") {
                    this.axisTypes.push(this.REGULAR_AXIS);
                }
                else {
                    this.axisTypes.push(this.IRREGULAR_AXIS);
                }
                let offsetVectorElement = element.getChildAsSerializedObject("gmlrgrid:GeneralGridAxis").getChildAsSerializedObject("gmlrgrid:offsetVector");
                let tmpArray = offsetVectorElement.getValueAsString().split(" ");
                for (let i = 0; i < tmpArray.length; i++) {
                    if (tmpArray[i] != "0") {
                        if (this.axisTypes[this.axisTypes.length - 1] !== this.IRREGULAR_AXIS) {
                            this.offsetVectors.push(tmpArray[i]);
                        }
                        else {
                            this.offsetVectors.push(this.IRREGULAR_AXIS_RESOLUTION);
                        }
                        break;
                    }
                }
            });
        }
    }
    gml.ReferenceableGridCoverage = ReferenceableGridCoverage;
})(gml || (gml = {}));
var gml;
(function (gml) {
    class GridEnvelope {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            let obj = source.getChildAsSerializedObject("gml:GridEnvelope");
            this.gridLows = obj.getChildAsSerializedObject("low").getValueAsString().split(" ");
            this.gridHighs = obj.getChildAsSerializedObject("high").getValueAsString().split(" ");
        }
    }
    gml.GridEnvelope = GridEnvelope;
})(gml || (gml = {}));
var gmlcov;
(function (gmlcov) {
    class Metadata {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            let childElementTag = "gmlcov:Extension";
            if (source.doesElementExist(childElementTag)) {
                this.extension = new gmlcov.Extension(source.getChildAsSerializedObject(childElementTag));
            }
        }
    }
    gmlcov.Metadata = Metadata;
})(gmlcov || (gmlcov = {}));
var gmlcov;
(function (gmlcov) {
    class Extension {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            let childElementTag = "rasdaman:covMetadata";
            if (source.doesElementExist(childElementTag)) {
                this.covMetadata = new gmlcov.CovMetadata(source.getChildAsSerializedObject(childElementTag));
            }
        }
    }
    gmlcov.Extension = Extension;
})(gmlcov || (gmlcov = {}));
var gmlcov;
(function (gmlcov) {
    class CovMetadata {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.content = source;
        }
    }
    gmlcov.CovMetadata = CovMetadata;
})(gmlcov || (gmlcov = {}));
var swe;
(function (swe) {
    class Uom {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.code = source.getAttributeAsString("code");
        }
    }
    swe.Uom = Uom;
})(swe || (swe = {}));
var swe;
(function (swe) {
    class NilValue {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            let elements = source.getChildrenAsSerializedObjects("swe:nilValue");
            let reasons = [];
            let values = [];
            elements.forEach(element => {
                let reasonTmp = element.getAttributeAsString("reason");
                reasons.push(reasonTmp);
                let valueTmp = element.getValueAsString();
                values.push(valueTmp);
            });
            this.reason = reasons.join(", ");
            this.value = values.join(", ");
        }
    }
    swe.NilValue = NilValue;
})(swe || (swe = {}));
var swe;
(function (swe) {
    class NilValues {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.nilValues = [];
            source.getChildrenAsSerializedObjects("swe:NilValues").forEach(o => {
                this.nilValues.push(new swe.NilValue(o));
            });
        }
    }
    swe.NilValues = NilValues;
})(swe || (swe = {}));
var swe;
(function (swe) {
    class NilValuesWrapper {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.nilValues = new swe.NilValues(source);
        }
        getNullValues() {
            let values = [];
            this.nilValues.nilValues.forEach(obj => {
                values.push(obj.value);
            });
            let result = values.join(", ");
            return result;
        }
    }
    swe.NilValuesWrapper = NilValuesWrapper;
})(swe || (swe = {}));
var swe;
(function (swe) {
    class Quantity {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            if (source.doesElementExist("swe:nilValues")) {
                this.nilValuesWrapper = new swe.NilValuesWrapper(source.getChildAsSerializedObject("swe:nilValues"));
            }
            if (source.doesElementExist("swe:uom")) {
                this.uom = new swe.Uom(source.getChildAsSerializedObject("swe:uom"));
            }
        }
    }
    swe.Quantity = Quantity;
})(swe || (swe = {}));
var swe;
(function (swe) {
    class Field {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            if (source.doesAttributeExist("name")) {
                this.name = source.getAttributeAsString("name");
            }
            if (source.doesElementExist("swe:Quantity")) {
                this.quantity = new swe.Quantity(source.getChildAsSerializedObject("swe:Quantity"));
            }
        }
    }
    swe.Field = Field;
})(swe || (swe = {}));
var swe;
(function (swe) {
    class DataRecord {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.fields = [];
            source.getChildrenAsSerializedObjects("swe:field").forEach(o => {
                this.fields.push(new swe.Field(o));
            });
        }
    }
    swe.DataRecord = DataRecord;
})(swe || (swe = {}));
var gmlcov;
(function (gmlcov) {
    class RangeType {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            if (source.doesElementExist("swe:DataRecord")) {
                this.dataRecord = new swe.DataRecord(source.getChildAsSerializedObject("swe:DataRecord"));
            }
        }
    }
    gmlcov.RangeType = RangeType;
})(gmlcov || (gmlcov = {}));
var wcs;
(function (wcs) {
    class ServiceParameters {
        constructor(source) {
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
    }
    wcs.ServiceParameters = ServiceParameters;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class CoverageDescription {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            let obj = source.getChildAsSerializedObject("CoverageDescription");
            this.coverageId = obj.getChildAsSerializedObject("wcs:CoverageId").getValueAsString();
            this.boundedBy = new gml.BoundedBy(obj.getChildAsSerializedObject("gml:boundedBy"));
            this.coverageFunction = new gml.CoverageFunction(obj.getChildAsSerializedObject("gml:coverageFunction"));
            this.metadata = new gmlcov.Metadata(obj.getChildAsSerializedObject("gmlcov:metadata"));
            this.domainSet = new gml.DomainSet(obj.getChildAsSerializedObject("gml:domainSet"));
            this.rangeType = new gmlcov.RangeType(obj.getChildAsSerializedObject("gmlcov:rangeType"));
            this.serviceParameters = new wcs.ServiceParameters(obj.getChildAsSerializedObject("wcs:ServiceParameters"));
        }
    }
    wcs.CoverageDescription = CoverageDescription;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class CRS {
        constructor(subsettingCRS, outputCRS) {
            this.subsettingCRS = subsettingCRS;
            this.outputCRS = outputCRS;
        }
        toKVP() {
            var result = "";
            if (this.subsettingCRS) {
                result = "&subsettingCRS=" + this.subsettingCRS;
            }
            if (this.outputCRS) {
                result += "&outputCRS=" + this.outputCRS;
            }
            return result;
        }
    }
    wcs.CRS = CRS;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class Clipping {
        constructor(wkt) {
            this.wkt = wkt;
        }
        toKVP() {
            var result = "";
            if (this.wkt) {
                result = "&clip=" + this.wkt;
            }
            return result;
        }
    }
    wcs.Clipping = Clipping;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class RequestBase {
        constructor() {
            this.service = "WCS";
            this.version = "2.0.1";
        }
        toKVP() {
            return "&SERVICE=" + this.service +
                "&VERSION=" + this.version;
        }
    }
    wcs.RequestBase = RequestBase;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class DescribeCoverage extends wcs.RequestBase {
        constructor(coverageIds) {
            super();
            if (!coverageIds.length) {
                throw new rasdaman.common.IllegalArgumentException("coverageIds");
            }
            this.coverageId = angular.copy(coverageIds);
        }
        toKVP() {
            var serialization = super.toKVP();
            serialization += "&REQUEST=DescribeCoverage";
            serialization += "&COVERAGEID=" + this.coverageId.join(",");
            return serialization;
        }
    }
    wcs.DescribeCoverage = DescribeCoverage;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class DimensionSubset {
        constructor(dimension) {
            this.dimension = dimension;
        }
        toKVP() {
            throw new rasdaman.common.NotImplementedException();
        }
    }
    wcs.DimensionSubset = DimensionSubset;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class DimensionSlice extends wcs.DimensionSubset {
        constructor(dimension, slicePoint) {
            super(dimension);
            this.slicePoint = slicePoint;
            this.sliceIrrNotValid = false;
            this.sliceRegularNotValid = false;
            this.typeOfSliceNotValidDate = false;
            this.typeOfSliceNotValidNumber = false;
        }
        toKVP() {
            return this.dimension + "(" + this.slicePoint + ")";
        }
    }
    wcs.DimensionSlice = DimensionSlice;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class DimensionTrim extends wcs.DimensionSubset {
        constructor(dimension, trimLow, trimHigh) {
            super(dimension);
            this.trimLow = trimLow;
            this.trimHigh = trimHigh;
            this.trimHighNotValid = false;
            this.trimLowNotValid = false;
            this.trimLowerUpperBoundNotInOrder = false;
            this.typeOfTrimLowerNotValidDate = false;
            this.typeOfTrimLowerNotValidNumber = false;
            this.typeOfTrimUpperNotValidDate = false;
            this.typeOfTrimUpperNotValidNumber = false;
        }
        toKVP() {
            return this.dimension + "(" + this.trimLow + "," + this.trimHigh + ")";
        }
    }
    wcs.DimensionTrim = DimensionTrim;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class GetCapabilities extends ows.GetCapabilities {
        constructor() {
            super();
            this.service = "WCS";
            this.acceptVersions = ["2.0.1"];
        }
        toKVP() {
            return "&SERVICE=" + this.service +
                "&ACCEPTVERSIONS=" + this.acceptVersions[0] +
                "&REQUEST=" + this.request;
        }
    }
    wcs.GetCapabilities = GetCapabilities;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class RangeItem {
        toKVP() {
            throw new rasdaman.common.NotImplementedException();
        }
    }
    wcs.RangeItem = RangeItem;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class RangeSubset {
        constructor() {
            this.rangeItem = [];
        }
        toKVP() {
            var serializedRangeItems = [];
            this.rangeItem.forEach((rangeItem) => {
                serializedRangeItems.push(rangeItem.toKVP());
            });
            if (serializedRangeItems.length) {
                return "&RANGESUBSET=" + serializedRangeItems.join(",");
            }
            else {
                return "";
            }
        }
    }
    wcs.RangeSubset = RangeSubset;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class Scaling {
        toKVP() {
            throw new rasdaman.common.NotImplementedException();
        }
    }
    wcs.Scaling = Scaling;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class GetCoverage extends wcs.RequestBase {
        constructor(coverageId, dimensionSubset, format, mediaType) {
            super();
            this.coverageId = coverageId;
            this.dimensionSubset = [];
            dimensionSubset.forEach(o => {
                this.dimensionSubset.push(o);
            });
            this.format = format;
            this.mediaType = mediaType;
        }
        toKVP() {
            var serialization = super.toKVP();
            serialization += "&REQUEST=GetCoverage";
            serialization += "&COVERAGEID=" + this.coverageId;
            this.dimensionSubset.forEach((subset) => {
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
        }
    }
    wcs.GetCoverage = GetCoverage;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class InterpolationMetadata {
        constructor(source) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.interpolationSupported = [];
            source.getChildrenAsSerializedObjects("int:InterpolationSupported")
                .forEach((interpolation) => {
                this.interpolationSupported.push(interpolation.getValueAsString());
            });
        }
    }
    wcs.InterpolationMetadata = InterpolationMetadata;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class ProcessCoverages extends wcs.RequestBase {
        constructor(query, extraParams) {
            super();
            rasdaman.common.ArgumentValidator.isNotNull(query, "query");
            rasdaman.common.ArgumentValidator.isNotNull(extraParams, "extraParams");
            rasdaman.common.ArgumentValidator.isArray(extraParams, "extraParams");
            this.request = "ProcessCoverages";
            this.query = query;
            this.extraParameters = angular.copy(extraParams);
        }
        toKVP() {
            var serializedParams = "";
            for (var i = 0; i < this.extraParameters.length; ++i) {
                serializedParams += ("&" + i + "=" + encodeURI(this.extraParameters[i]));
            }
            return "&REQUEST=" + this.request
                + "&QUERY=" + encodeURI(this.query)
                + serializedParams;
        }
    }
    wcs.ProcessCoverages = ProcessCoverages;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class Interpolation {
        constructor(globalInterpolation) {
            this.globalInterpolation = globalInterpolation;
        }
        toKVP() {
            if (this.globalInterpolation) {
                return "&INTERPOLATION=" + this.globalInterpolation;
            }
            else {
                return "";
            }
        }
    }
    wcs.Interpolation = Interpolation;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class RangeComponent extends wcs.RangeItem {
        constructor(rangeComponent) {
            super();
            this.rangeComponent = rangeComponent;
        }
        toKVP() {
            return this.rangeComponent;
        }
    }
    wcs.RangeComponent = RangeComponent;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class RangeInterval extends wcs.RangeItem {
        constructor(startComponent, endComponent) {
            super();
            this.startComponent = startComponent;
            this.endComponent = endComponent;
        }
        toKVP() {
            return this.startComponent.toKVP() + ":" + this.endComponent.toKVP();
        }
    }
    wcs.RangeInterval = RangeInterval;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class ScaleByFactor extends wcs.Scaling {
        constructor(scaleFactor) {
            super();
            if (scaleFactor < 0) {
                throw new rasdaman.common.IllegalArgumentException("ScaleFactor must be positive.");
            }
            this.scaleFactor = scaleFactor;
        }
        toKVP() {
            return "&SCALEFACTOR=" + this.scaleFactor;
        }
    }
    wcs.ScaleByFactor = ScaleByFactor;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class ScaleAxis {
        constructor(axis, scaleFactor) {
            if (scaleFactor < 0) {
                throw new rasdaman.common.IllegalArgumentException("ScaleFactor must be positive.");
            }
            this.axis = axis;
            this.scaleFactor = scaleFactor;
        }
        toKVP() {
            return this.axis + "(" + this.scaleFactor + ")";
        }
    }
    wcs.ScaleAxis = ScaleAxis;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class ScaleAxesByFactor extends wcs.Scaling {
        constructor(scaleAxis) {
            super();
            this.scaleAxis = angular.copy(scaleAxis);
        }
        toKVP() {
            var serializedAxes = [];
            this.scaleAxis.forEach((axis) => {
                serializedAxes.push(axis.toKVP());
            });
            return "&SCALEAXES=" + serializedAxes.join(",");
        }
    }
    wcs.ScaleAxesByFactor = ScaleAxesByFactor;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class TargetAxisSize {
        constructor(axis, targetSize) {
            if (targetSize < 0) {
                throw new rasdaman.common.IllegalArgumentException("ScaleFactor must be positive.");
            }
            this.axis = axis;
            this.targetSize = targetSize;
        }
        toKVP() {
            return this.axis + "(" + this.targetSize + ")";
        }
    }
    wcs.TargetAxisSize = TargetAxisSize;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class ScaleToSize extends wcs.Scaling {
        constructor(targetAxisSize) {
            super();
            this.targetAxisSize = angular.copy(targetAxisSize);
        }
        toKVP() {
            var targetAxesSize = [];
            this.targetAxisSize.forEach((target) => {
                targetAxesSize.push(target.toKVP());
            });
            return "&SCALESIZE=" + targetAxesSize.join(",");
        }
    }
    wcs.ScaleToSize = ScaleToSize;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class TargetAxisExtent {
        constructor(axis, low, high) {
            this.axis = axis;
            this.low = low;
            this.high = high;
        }
        toKVP() {
            return this.axis + "(" + this.low + ":" + this.high + ")";
        }
    }
    wcs.TargetAxisExtent = TargetAxisExtent;
})(wcs || (wcs = {}));
var wcs;
(function (wcs) {
    class ScaleToExtent extends wcs.Scaling {
        constructor(targetAxisExtent) {
            super();
            this.targetAxisExtent = angular.copy(targetAxisExtent);
        }
        toKVP() {
            var serializedAxes = [];
            this.targetAxisExtent.forEach((target) => {
                serializedAxes.push(target.toKVP());
            });
            return "&SCALEEXTENT=" + serializedAxes.join(",");
        }
    }
    wcs.ScaleToExtent = ScaleToExtent;
})(wcs || (wcs = {}));
var rasdaman;
(function (rasdaman) {
    class WCSSettingsService {
        constructor($window) {
            this.wcsEndpoint = $window.location.href.replace("wcs-client/index.html", "ows");
            this.wcsEndpoint = this.wcsEndpoint.replace("wcs-client/app/", "rasdaman/ows");
            this.wcsServiceNameVersion = "SERVICE=WCS&VERSION=2.0.1";
            this.setWCSEndPoint(this.wcsEndpoint);
            this.defaultContextPath = this.contextPath;
            this.wcsFullEndpoint = this.wcsEndpoint + "?" + this.wcsServiceNameVersion;
        }
        setWCSEndPoint(petascopeEndPoint) {
            this.wcsEndpoint = petascopeEndPoint;
            this.wcsEndpoint = this.wcsEndpoint.split("#")[0];
            if (!this.wcsEndpoint.endsWith("ows")) {
                this.wcsEndpoint = this.wcsEndpoint + "ows";
            }
            this.contextPath = this.wcsEndpoint.replace("/rasdaman/ows", "/rasdaman");
        }
    }
    WCSSettingsService.$inject = ["$window"];
    rasdaman.WCSSettingsService = WCSSettingsService;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class WCSService {
        constructor($http, $q, settings, serializedObjectFactory, $window, credentialService, $state) {
            this.$http = $http;
            this.$q = $q;
            this.settings = settings;
            this.serializedObjectFactory = serializedObjectFactory;
            this.$window = $window;
            this.credentialService = credentialService;
            this.$state = $state;
        }
        getServerCapabilities(request) {
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
        }
        getCoverageDescription(request) {
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
        }
        storeKVPParametersToLocalStorage(petascopeEndPoint, keysValuesStr) {
            var getCoverageKVPParameters = { "PetascopeEndPoint": petascopeEndPoint };
            getCoverageKVPParameters["request"] = keysValuesStr;
            if (this.credentialService.hasStoredCredentials()) {
                var authorizationObj = this.credentialService.getAuthorizationHeader(petascopeEndPoint);
                getCoverageKVPParameters = Object.assign(getCoverageKVPParameters, authorizationObj);
            }
            window.localStorage.setItem("GetcoverageKVPParameters", JSON.stringify(getCoverageKVPParameters));
        }
        getCoverageHTTPGET(request) {
            var result = this.$q.defer();
            var requestUrl = this.settings.wcsEndpoint + "?" + request.toKVP();
            var url = this.settings.defaultContextPath + "/ows/result.html";
            this.storeKVPParametersToLocalStorage(this.settings.wcsEndpoint, request.toKVP());
            window.open(url, '_blank');
            result.resolve(requestUrl);
            return result.promise;
        }
        getCoverageHTTPPOST(request) {
            return this.getCoverageHTTPGET(request);
        }
        deleteCoverage(coverageId) {
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
        }
        insertCoverage(coverageUrl, useGeneratedId) {
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
        }
        processCoverages(query) {
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
        }
        updateCoverageMetadata(formData) {
            var result = this.$q.defer();
            var requestUrl = this.settings.wcsEndpoint + "/UpdateCoverageMetadata";
            var currentHeaders = { 'Content-Type': undefined };
            var request = {
                method: 'POST',
                url: requestUrl,
                transformResponse: null,
                headers: this.credentialService.createRequestHeader(this.settings.wcsEndpoint, currentHeaders),
                withCredentials: true,
                data: formData
            };
            this.$http(request).then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        }
    }
    WCSService.$inject = ["$http", "$q", "rasdaman.WCSSettingsService",
        "rasdaman.common.SerializedObjectFactory", "$window",
        "rasdaman.CredentialService", "$state"];
    rasdaman.WCSService = WCSService;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class ErrorHandlingService {
        constructor(notificationService, serializedObjectFactory, $log) {
            this.notificationService = notificationService;
            this.serializedObjectFactory = serializedObjectFactory;
            this.$log = $log;
        }
        handleError(...args) {
            if (args.length == 1) {
                var errorInformation = args[0];
                if (errorInformation.length == 1) {
                    errorInformation = errorInformation[0];
                }
                if (errorInformation.status == 404 || errorInformation.status == -1) {
                    this.notificationService.error("Cannot connect to petascope, please check if petascope is running.");
                }
                else {
                    this.notificationService.error("The request failed with HTTP code:" + errorInformation.status + "(" + errorInformation.statusText + ")");
                }
                if (errorInformation.data != null && errorInformation.data != "") {
                    try {
                        var responseDocument = new rasdaman.common.ResponseDocument(errorInformation.data, rasdaman.common.ResponseDocumentType.XML);
                        var serializedResponse = this.serializedObjectFactory.getSerializedObject(responseDocument);
                        var exceptionReport = new ows.ExceptionReport(serializedResponse);
                        this.notificationService.error(exceptionReport.exception.exceptionText + "</br> Exception code: " + exceptionReport.exception.exceptionCode);
                    }
                    catch (err) {
                        this.$log.error(err);
                    }
                }
            }
        }
    }
    ErrorHandlingService.$inject = ["Notification", "rasdaman.common.SerializedObjectFactory", "$log"];
    rasdaman.ErrorHandlingService = ErrorHandlingService;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class CredentialService {
        constructor() {
            this.credentialsDict = JSON.parse(window.localStorage.getItem("credentials"));
            if (this.credentialsDict == null) {
                this.credentialsDict = {};
            }
        }
        persitCredential(endpoint, credential) {
            this.credentialsDict[endpoint] = credential;
            window.localStorage.setItem("credentials", JSON.stringify(this.credentialsDict));
        }
        clearStorage() {
            this.credentialsDict = {};
            window.localStorage.setItem("credentials", JSON.stringify(this.credentialsDict));
        }
        hasStoredCredentials() {
            return Object.keys(this.credentialsDict).length > 0;
        }
        createRequestHeader(petascopeEndPoint, headers) {
            var authorizationObj = this.getAuthorizationHeader(petascopeEndPoint);
            headers = Object.assign(authorizationObj, headers);
            return headers;
        }
        getAuthorizationHeader(petascopeEndPoint) {
            var result = {};
            if (this.hasStoredCredentials) {
                var credential = this.credentialsDict[petascopeEndPoint];
                if (credential != null && credential["username"] != null) {
                    var username = credential["username"];
                    var password = credential["password"];
                    result["Authorization"] = this.getEncodedBasicAuthencationString(username, password);
                }
            }
            return result;
        }
        getEncodedBasicAuthencationString(username, password) {
            var result = "Basic " + btoa(username + ":" + password);
            return result;
        }
        createBasicAuthenticationHeader(username, password) {
            var headers = {};
            headers["Authorization"] = this.getEncodedBasicAuthencationString(username, password);
            return headers;
        }
    }
    rasdaman.CredentialService = CredentialService;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class WMSSettingsService {
        constructor($window) {
            this.wmsEndpoint = $window.location.href.replace("wcs-client/index.html", "ows");
            this.wmsEndpoint = this.wmsEndpoint.replace("wcs-client/app/", "rasdaman/ows");
            this.setWMSEndPoint(this.wmsEndpoint);
            this.wmsServiceNameVersion = "service=WMS&version=" + WMSSettingsService.version;
            this.setWMSFullEndPoint();
        }
        setWMSFullEndPoint() {
            this.wmsFullEndpoint = this.wmsEndpoint + "?" + this.wmsServiceNameVersion;
        }
        setWMSEndPoint(petascopeEndPoint) {
            this.wmsEndpoint = petascopeEndPoint;
            this.wmsEndpoint = this.wmsEndpoint.split("#")[0];
            if (!this.wmsEndpoint.endsWith("ows")) {
                this.wmsEndpoint = this.wmsEndpoint + "ows";
            }
        }
    }
    WMSSettingsService.$inject = ["$window"];
    WMSSettingsService.version = "1.3.0";
    rasdaman.WMSSettingsService = WMSSettingsService;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class WebWorldWindService {
        constructor($rootScope, wmsSetting, credentialService) {
            this.webWorldWindModels = [];
            this.coveragesExtentsArray = null;
            this.wmsSetting = null;
            this.authorizationToken = "";
            this.oldLayerName = '';
            this.wmsSetting = wmsSetting;
            this.authorizationToken = credentialService.getAuthorizationHeader(this.wmsSetting.wmsEndpoint)["Authorization"];
        }
        setCoveragesExtentsArray(coveragesExtentsArray) {
            this.coveragesExtentsArray = coveragesExtentsArray;
        }
        getCoveragesExtentsArray() {
            return this.coveragesExtentsArray;
        }
        getCoveragesExtentsByCoverageId(coverageId) {
            var result = [];
            for (var i = 0; i < this.coveragesExtentsArray.length; i++) {
                if (this.coveragesExtentsArray[i].coverageId === coverageId) {
                    result.push(this.coveragesExtentsArray[i]);
                    return result;
                }
            }
            return null;
        }
        initWebWorldWind(canvasId) {
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
        }
        getCoverageIdsSameExtent(coverageExtent, coveragesExtentsArray) {
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
        }
        showCoverageExtentOnGlobe(canvasId, coverageId) {
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
        }
        showHideCoverageExtentOnGlobe(canvasId, coverageId) {
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
        }
        updateCoverageExtentShowProperty(coveragesExtentsArray, coverageId, value) {
            for (var i = 0; i < coveragesExtentsArray.length; i++) {
                if (coveragesExtentsArray[i].coverageId == coverageId) {
                    coveragesExtentsArray[i].show = value;
                    return;
                }
            }
        }
        updatePolygonUserPropertiesWhenShowHide(polygonLayer) {
            var coveragesExtentsArray = polygonLayer.coveragesExtentsArray;
            for (var i = 0; i < polygonLayer.renderables.length; i++) {
                var polygonObj = polygonLayer.renderables[i];
                var coverageIds = this.getCoverageIdsSameExtent(polygonObj.coverageExtent, coveragesExtentsArray);
                var userProperties = this.buildUserPropertiesStr(coverageIds, polygonObj.coverageExtentStr);
                polygonObj.userProperties = userProperties;
            }
        }
        prepareCoveragesExtentsForGlobe(canvasId, coveragesExtentsArray) {
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
        }
        gotoCoverageExtentCenter(canvasId, coverageExtents) {
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
        }
        buildUserPropertiesStr(coverageIds, coverageExtentStr) {
            var coverageIdsStr = "";
            for (var j = 0; j < coverageIds.length; j++) {
                coverageIdsStr += coverageIds[j];
            }
            var userProperties = coverageIdsStr + "\n" + coverageExtentStr + "\n";
            return userProperties;
        }
        loadGetMapResultOnGlobe(canvasId, layerName, styleName, bbox, displayLayer, timeMoment) {
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
            var wwd = webWorldWindModel.wwd;
            var config = {
                title: "WMS layer overview",
                version: rasdaman.WMSSettingsService.version,
                service: this.wmsSetting.wmsEndpoint,
                layerNames: layerName,
                sector: new WorldWind.Sector(bbox.ymin, bbox.ymax, bbox.xmin, bbox.xmax),
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
            var wmsLayer = new BAWmsLayer(config, timeString, this.authorizationToken);
            webWorldWindModel.wmsLayer = wmsLayer;
            if (displayLayer) {
                wwd.addLayer(wmsLayer);
            }
        }
    }
    WebWorldWindService.$inject = [
        "$rootScope",
        "rasdaman.WMSSettingsService",
        "rasdaman.CredentialService"
    ];
    rasdaman.WebWorldWindService = WebWorldWindService;
    class BAWmsLayer extends WorldWind.WmsLayer {
        constructor(config, timeString, authorizationHeader) {
            super(config, timeString);
            this.authorizationHeader = "";
            this.authorizationHeader = authorizationHeader;
        }
        retrieveTileImage(dc, tile, suppressRedraw) {
            if (this.currentRetrievals.indexOf(tile.imagePath) < 0) {
                if (this.currentRetrievals.length > this.retrievalQueueSize) {
                    return;
                }
                if (this.absentResourceList.isResourceAbsent(tile.imagePath)) {
                    return;
                }
                var url = this.resourceUrlForTile(tile, this.retrievalImageFormat), image = new Image(), imagePath = tile.imagePath, cache = dc.gpuResourceCache, canvas = dc.currentGlContext.canvas, layer = this;
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
        }
        ;
    }
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
    class WCSSettingsController {
        constructor($scope, settingsService) {
            this.$scope = $scope;
            this.settingsService = settingsService;
            $scope.wcsEndpoint = settingsService.wcsEndpoint;
            $scope.updateSettings = function () {
                console.log($scope.wcsEndpoint);
            };
        }
    }
    WCSSettingsController.$inject = [
        "$scope",
        "rasdaman.WCSSettingsService"
    ];
    rasdaman.WCSSettingsController = WCSSettingsController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class WCSMainController {
        constructor($scope, $rootScope, $state) {
            this.$scope = $scope;
            this.initializeTabs($scope);
            $scope.$watch("adminStateInformation.loggedIn", (newValue, oldValue) => {
                if (newValue == true) {
                    if ($scope.isSupportWCST) {
                        $scope.wcsInsertCoverageTab.disabled = false;
                        $scope.wcsDeleteCoverageTab.disabled = false;
                    }
                }
                else {
                    $scope.wcsInsertCoverageTab.disabled = true;
                    $scope.wcsDeleteCoverageTab.disabled = true;
                }
            });
            $scope.$watch("wcsStateInformation.serverCapabilities", (newValue, oldValue) => {
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
                    this.resetState();
                }
            });
            $scope.$watch("wcsStateInformation.selectedCoverageDescription", (newValue, oldValue) => {
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
        initializeTabs($scope) {
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
        }
        resetState() {
            this.$scope.wcsDescribeCoverageTab.disabled = true;
            this.$scope.wcsGetCoverageTab.disabled = true;
            this.$scope.wcsProcessCoverageTab.disabled = true;
            this.$scope.wcsDeleteCoverageTab.disabled = false;
            this.$scope.wcsInsertCoverageTab.disabled = false;
        }
        static isProcessCoverageEnabled(serverCapabilities) {
            var processExtensionUri = rasdaman.Constants.PROCESSING_EXT_URI;
            return serverCapabilities.serviceIdentification.profile.indexOf(processExtensionUri) != -1;
        }
        static isCoverageTransactionEnabled(serverCapabilities) {
            var transactionExtensionUri = rasdaman.Constants.TRANSACTION_EXT_URI;
            return serverCapabilities.serviceIdentification.profile.indexOf(transactionExtensionUri) != -1;
        }
    }
    WCSMainController.$inject = ["$scope", "$rootScope", "$state"];
    rasdaman.WCSMainController = WCSMainController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class WCSGetCapabilitiesController {
        constructor($scope, $rootScope, $log, wcsService, settings, alertService, errorHandlingService, webWorldWindService) {
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
            $scope.initCheckboxesForCoverageIds = () => {
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
            $scope.getCoverageSummaryByCoverageId = (coverageId) => {
                var coverageSummaryArray = $scope.capabilities.contents.coverageSummaries;
                for (var i = 0; i < coverageSummaryArray.length; i++) {
                    if (coverageSummaryArray[i].coverageId == coverageId) {
                        return coverageSummaryArray[i];
                    }
                }
            };
            $scope.displayFootprintOnGlobe = (coverageId) => {
                webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, coverageId);
            };
            $scope.displayAllFootprintsOnGlobe = (status) => {
                if (status == true) {
                    for (var i = 0; i < $scope.coveragesExtents.length; i++) {
                        var coverageId = $scope.coveragesExtents[i].coverageId;
                        if ($scope.coveragesExtents[i].displayFootprint == false) {
                            $scope.getCoverageSummaryByCoverageId(coverageId).displayFootprint = true;
                            webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, coverageId);
                        }
                    }
                }
                else {
                    for (var i = 0; i < $scope.coveragesExtents.length; i++) {
                        var coverageId = $scope.coveragesExtents[i].coverageId;
                        if ($scope.coveragesExtents[i].displayFootprint == true) {
                            $scope.getCoverageSummaryByCoverageId(coverageId).displayFootprint = false;
                            webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, coverageId);
                        }
                    }
                }
            };
            $scope.$on("reloadWCSServerCapabilities", function (event, b) {
                $scope.getServerCapabilities();
            });
            $scope.$watch("wcsStateInformation.reloadServerCapabilities", (capabilities) => {
                if ($scope.wcsStateInformation.reloadServerCapabilities == true) {
                    $scope.getServerCapabilities();
                }
                $scope.wcsStateInformation.reloadServerCapabilities = false;
            });
            $scope.parseCoveragesExtents = () => {
                let coverageSummaries = $scope.capabilities.contents.coverageSummaries;
                coverageSummaries.forEach((coverageSummary) => {
                    let coverageId = coverageSummary.coverageId;
                    let wgs84BoundingBox = coverageSummary.wgs84BoundingBox;
                    if (wgs84BoundingBox != null) {
                        let lowerArrayTmp = wgs84BoundingBox.lowerCorner.split(" ");
                        let xMin = parseFloat(lowerArrayTmp[0]);
                        let yMin = parseFloat(lowerArrayTmp[1]);
                        let upperArrayTmp = wgs84BoundingBox.upperCorner.split(" ");
                        let xMax = parseFloat(upperArrayTmp[0]);
                        let yMax = parseFloat(upperArrayTmp[1]);
                        let bboxObj = {
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
            $scope.handleGetServerCapabilities = () => {
                $scope.getServerCapabilities();
                $scope.showAllFootprints.isChecked = false;
            };
            $scope.getServerCapabilities = (...args) => {
                if (!$scope.wcsServerEndpoint) {
                    alertService.error("The entered WCS endpoint is invalid.");
                    return;
                }
                $scope.coveragesExtents = [];
                settings.wcsEndpoint = $scope.wcsServerEndpoint;
                var capabilitiesRequest = new wcs.GetCapabilities();
                wcsService.getServerCapabilities(capabilitiesRequest)
                    .then((response) => {
                    $scope.capabilitiesDocument = response.document;
                    $scope.capabilities = response.value;
                    $scope.isAvailableCoveragesOpen = true;
                    $scope.isServiceIdentificationOpen = true;
                    $scope.isServiceProviderOpen = true;
                    $scope.parseCoveragesExtents();
                }, (...args) => {
                    $scope.capabilitiesDocument = null;
                    $scope.capabilities = null;
                    $scope.isAvailableCoveragesOpen = false;
                    $scope.isServiceIdentificationOpen = false;
                    $scope.isServiceProviderOpen = false;
                    errorHandlingService.handleError(args);
                    $log.error(args);
                })
                    .finally(() => {
                    $scope.wcsStateInformation.serverCapabilities = $scope.capabilities;
                });
            };
        }
    }
    WCSGetCapabilitiesController.$inject = [
        "$scope",
        "$rootScope",
        "$log",
        "rasdaman.WCSService",
        "rasdaman.WCSSettingsService",
        "Notification",
        "rasdaman.ErrorHandlingService",
        "rasdaman.WebWorldWindService"
    ];
    rasdaman.WCSGetCapabilitiesController = WCSGetCapabilitiesController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class WCSDescribeCoverageController {
        constructor($scope, $rootScope, $log, wcsService, settings, alertService, errorHandlingService, webWorldWindService) {
            $scope.selectedCoverageId = null;
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
            $rootScope.$watch("wcsSelectedGetCoverageId", (coverageId) => {
                if (coverageId != null) {
                    $scope.selectedCoverageId = coverageId;
                    $scope.describeCoverage();
                }
            });
            $scope.$watch("wcsStateInformation.serverCapabilities", (capabilities) => {
                if (capabilities) {
                    $scope.availableCoverageIds = [];
                    $scope.coverageCustomizedMetadatasDict = {};
                    capabilities.contents.coverageSummaries.forEach((coverageSummary) => {
                        let coverageId = coverageSummary.coverageId;
                        $scope.availableCoverageIds.push(coverageId);
                        if (coverageSummary.customizedMetadata != null) {
                            $scope.coverageCustomizedMetadatasDict[coverageId] = coverageSummary.customizedMetadata;
                        }
                    });
                }
            });
            $scope.$watch("wcsStateInformation.selectedGetCoverageId", (getCoverageId) => {
                if (getCoverageId) {
                    $scope.selectedCoverageId = getCoverageId;
                    $scope.describeCoverage();
                }
            });
            $rootScope.$watch("adminStateInformation.loggedIn", (newValue, oldValue) => {
                if (newValue) {
                    $scope.adminUserLoggedIn = true;
                }
                else {
                    $scope.adminUserLoggedIn = false;
                }
            });
            $scope.updateCoverageMetadata = () => {
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
                wcsService.updateCoverageMetadata(formData).then(response => {
                    alertService.success("Successfully update coverage's metadata from file.");
                    $scope.describeCoverage();
                }, (...args) => {
                    errorHandlingService.handleError(args);
                    $log.error(args);
                });
            };
            $scope.parseCoverageMetadata = () => {
                $scope.metadata = null;
                var parser = new DOMParser();
                var xmlDoc = parser.parseFromString($scope.rawCoverageDescription, "text/xml");
                var elements = xmlDoc.getElementsByTagName("rasdaman:covMetadata");
                if (elements.length > 0) {
                    $scope.metadata = elements[0].innerHTML;
                    for (let i = 0; i < $scope.metadata.length; i++) {
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
                    .then((response) => {
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
                }, (...args) => {
                    $scope.coverageDescription = null;
                    errorHandlingService.handleError(args);
                    $log.error(args);
                })
                    .finally(() => {
                    $scope.wcsStateInformation.selectedCoverageDescription = $scope.coverageDescription;
                });
            };
        }
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
    rasdaman.WCSDescribeCoverageController = WCSDescribeCoverageController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class WCSDeleteCoverageController {
        constructor($rootScope, $scope, $log, alertService, wcsService, errorHandlingService) {
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
            $scope.$watch("idOfCoverageToDelete", (newValue, oldValue) => {
                $scope.isCoverageIdValid = isCoverageIdValid(newValue);
            });
            $scope.$watch("wcsStateInformation.serverCapabilities", (capabilities) => {
                if (capabilities) {
                    $scope.availableCoverageIds = [];
                    capabilities.contents.coverageSummaries.forEach((coverageSummary) => {
                        $scope.availableCoverageIds.push(coverageSummary.coverageId);
                    });
                }
            });
            $scope.deleteCoverage = () => {
                if ($scope.requestInProgress) {
                    this.alertService.error("Cannot delete a coverage while another delete request is in progress.");
                }
                else if (!isCoverageIdValid($scope.idOfCoverageToDelete)) {
                    this.alertService.error("The coverage ID <b>" + $scope.idOfCoverageToDelete + "</b> is not valid.");
                }
                else {
                    $scope.requestInProgress = true;
                    this.wcsService.deleteCoverage($scope.idOfCoverageToDelete).then((...args) => {
                        this.alertService.success("Successfully deleted coverage with ID <b>" + $scope.idOfCoverageToDelete + "<b/>");
                        $rootScope.$broadcast("reloadWCSServerCapabilities", true);
                        $rootScope.$broadcast("reloadWMSServerCapabilities", true);
                    }, (...args) => {
                        this.errorHandlingService.handleError(args);
                        this.$log.error(args);
                    }).finally(function () {
                        $scope.requestInProgress = false;
                    });
                }
            };
            $scope.idOfCoverageToDelete = null;
            $scope.requestInProgress = false;
            $scope.isCoverageIdValid = false;
        }
    }
    WCSDeleteCoverageController.$inject = [
        "$rootScope",
        "$scope",
        "$log",
        "Notification",
        "rasdaman.WCSService",
        "rasdaman.ErrorHandlingService"
    ];
    rasdaman.WCSDeleteCoverageController = WCSDeleteCoverageController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class WCSInsertCoverageController {
        constructor($scope, $log, alertService, wcsService, errorHandlingService) {
            this.$scope = $scope;
            this.$log = $log;
            this.alertService = alertService;
            this.wcsService = wcsService;
            this.errorHandlingService = errorHandlingService;
            $scope.urlOfCoverageToInsert = null;
            $scope.requestInProgress = false;
            $scope.useGeneratedCoverageId = false;
            $scope.insertCoverage = () => {
                if ($scope.requestInProgress) {
                    this.alertService.error("Cannot insert a coverage while another insert request is in progress.");
                }
                else {
                    $scope.requestInProgress = true;
                    this.wcsService.insertCoverage($scope.urlOfCoverageToInsert, $scope.useGeneratedCoverageId).then((...args) => {
                        this.alertService.success("Successfully inserted coverage.");
                        this.$log.info(args);
                        $scope.wcsStateInformation.reloadServerCapabilities = true;
                    }, (...args) => {
                        this.errorHandlingService.handleError(args);
                        this.$log.error(args);
                    }).finally(function () {
                        $scope.requestInProgress = false;
                    });
                }
            };
        }
    }
    WCSInsertCoverageController.$inject = [
        "$scope",
        "$log",
        "Notification",
        "rasdaman.WCSService",
        "rasdaman.ErrorHandlingService"
    ];
    rasdaman.WCSInsertCoverageController = WCSInsertCoverageController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class WCSGetCoverageController {
        constructor($http, $scope, $rootScope, $log, wcsService, alertService, webWorldWindService) {
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
            $scope.$watch("wcsStateInformation.serverCapabilities", (capabilities) => {
                if (capabilities) {
                    $scope.avaiableHTTPRequests = ["GET", "POST"];
                    $scope.selectedHTTPRequest = $scope.avaiableHTTPRequests[0];
                    $scope.availableCoverageIds = [];
                    $scope.coverageCustomizedMetadatasDict = {};
                    capabilities.contents.coverageSummaries.forEach((coverageSummary) => {
                        let coverageId = coverageSummary.coverageId;
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
                        .then((requestUrl) => {
                        $scope.core.requestUrl = requestUrl;
                    }, (...args) => {
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
            $scope.$watch("wcsStateInformation.selectedCoverageDescription", (coverageDescription) => {
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
                        .then((response) => {
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
        static isRangeSubsettingSupported(serverCapabilities) {
            return serverCapabilities.serviceIdentification.profile.indexOf(rasdaman.Constants.RANGE_SUBSETTING_EXT_URI) != -1;
        }
        static isScalingSupported(serverCapabilities) {
            return serverCapabilities.serviceIdentification.profile.indexOf(rasdaman.Constants.SCALING_EXT_URI) != -1;
        }
        static isInterpolationSupported(serverCapabilities) {
            return serverCapabilities.serviceIdentification.profile.indexOf(rasdaman.Constants.INTERPOLATION_EXT_URI) != -1;
        }
        static isCRSSupported(serverCapabilities) {
            return serverCapabilities.serviceIdentification.profile.indexOf(rasdaman.Constants.CRS_EXT_URI) != -1;
        }
    }
    WCSGetCoverageController.$inject = [
        "$http",
        "$scope",
        "$rootScope",
        "$log",
        "rasdaman.WCSService",
        "Notification",
        "rasdaman.WebWorldWindService"
    ];
    rasdaman.WCSGetCoverageController = WCSGetCoverageController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class WidgetConfiguration {
        constructor(type, parameters) {
            this.type = type;
            this.parameters = parameters;
        }
    }
    rasdaman.WidgetConfiguration = WidgetConfiguration;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class WCPSCommand {
        constructor(command) {
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
                    widgetParams.forEach((param) => {
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
    }
    rasdaman.WCPSCommand = WCPSCommand;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class WCPSQueryResult {
        constructor(command) {
            rasdaman.common.ArgumentValidator.isNotNull(command, "command");
            this.command = command;
        }
    }
    rasdaman.WCPSQueryResult = WCPSQueryResult;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class RawWCPSResult extends rasdaman.WCPSQueryResult {
        constructor(command, data) {
            super(command);
            this.data = data.toString();
        }
    }
    rasdaman.RawWCPSResult = RawWCPSResult;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class ImageWCPSResult extends rasdaman.WCPSQueryResult {
        constructor(command, rawImageData) {
            super(command);
            this.base64ImageData = rasdaman.common.ImageUtilities.arrayBufferToBase64(rawImageData);
            this.imageType = (command.query.search(/jpeg/g) === -1 ? "image/png" : "image/jpeg");
        }
    }
    rasdaman.ImageWCPSResult = ImageWCPSResult;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class DiagramWCPSResult extends rasdaman.WCPSQueryResult {
        constructor(command, data) {
            super(command);
            var diagramType = "lineChart";
            if (command.widgetConfiguration.parameters && command.widgetConfiguration.parameters.type) {
                diagramType = command.widgetConfiguration.parameters.type;
            }
            this.diagramOptions = {
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
            this.diagramData = [
                {
                    values: processedValues
                }
            ];
        }
    }
    rasdaman.DiagramWCPSResult = DiagramWCPSResult;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class WebWorldWindWCPSResult extends rasdaman.WCPSQueryResult {
        constructor(command, rawImageData) {
            super(command);
            this.minLat = -90;
            this.minLong = -180;
            this.maxLat = 90;
            this.maxLong = 180;
            this.base64ImageData = rasdaman.common.ImageUtilities.arrayBufferToBase64(rawImageData);
            this.imageType = (command.query.search(/jpeg/g) === -1 ? "image/png" : "image/jpeg");
            if (command.widgetParameters.length > 0) {
                this.minLat = parseFloat(command.widgetParameters[0]);
                this.minLong = parseFloat(command.widgetParameters[1]);
                this.maxLat = parseFloat(command.widgetParameters[2]);
                this.maxLong = parseFloat(command.widgetParameters[3]);
            }
        }
    }
    rasdaman.WebWorldWindWCPSResult = WebWorldWindWCPSResult;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class NotificationWCPSResult extends rasdaman.WCPSQueryResult {
        constructor(command, data) {
            super(command);
            this.data = data.toString();
        }
    }
    rasdaman.NotificationWCPSResult = NotificationWCPSResult;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class WCPSResultFactory {
        static getResult(errorHandlingService, command, data, mimeType, fileName) {
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
        }
        static validateResult(errorHandlingService, command, mimeType) {
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
        }
    }
    rasdaman.WCPSResultFactory = WCPSResultFactory;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class WCSProcessCoverageController {
        constructor($scope, $log, $interval, notificationService, wcsService, errorHandlingService) {
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
            $scope.$watch("selectedQuery", (newValue, oldValue) => {
                $scope.query = newValue;
            });
            $scope.$watch("selectedHistoryQuery", (newValue, oldValue) => {
                $scope.query = newValue;
            });
            $scope.clearHistory = () => {
                var thisQuery;
                thisQuery = { query: '', title: '--Select a WCPS query---' };
                $scope.historyOfQueries = [];
                $scope.historyOfQueries.unshift(thisQuery);
            };
            $scope.clearHistory();
            var addToHistory = () => {
                var thisQuery;
                var thisTitle;
                const NUMBER_OF_ELEMENTS_IN_HISTORY = 25;
                const NUMBER_CHARACTERS_IN_QUERY_TITLE = 20;
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
            $scope.executeQuery = () => {
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
                        var waitingForResultsPromise = $interval(() => {
                            $scope.editorData[indexOfResults].secondsPassed++;
                        }, 1000);
                        wcsService.processCoverages(command.query)
                            .then((data) => {
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
                        }, (...args) => {
                            if (args[0].data instanceof ArrayBuffer) {
                                var decoder = new TextDecoder("utf-8");
                                args[0].data = decoder.decode(new Uint8Array(args[0].data));
                            }
                            errorHandlingService.handleError(args);
                            $log.error(args);
                            $scope.editorData.push(new rasdaman.NotificationWCPSResult(command, "Cannot execute the requested WCPS query, error '" + args[0].data + "'."));
                        })
                            .finally(() => {
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
            $scope.getEditorDataType = (datum) => {
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
        static createExampleQueries() {
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
        }
    }
    WCSProcessCoverageController.$inject = [
        "$scope",
        "$log",
        "$interval",
        "Notification",
        "rasdaman.WCSService",
        "rasdaman.ErrorHandlingService"
    ];
    rasdaman.WCSProcessCoverageController = WCSProcessCoverageController;
    class WaitingForResult {
        constructor() {
            this.secondsPassed = 0;
        }
    }
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
    class RangeSubsettingModel {
        constructor(coverageDescription) {
            this.rangeSubset = new wcs.RangeSubset();
            this.availableRanges = [];
            this.isIntervals = [];
            this.isMaxRanges = false;
            coverageDescription.rangeType.dataRecord.fields.forEach(field => {
                this.availableRanges.push(field.name);
            });
        }
        addRangeComponent() {
            this.rangeSubset.rangeItem.push(new wcs.RangeComponent(this.availableRanges[0]));
            this.isIntervals.push(false);
            if (this.isIntervals.length == this.availableRanges.length) {
                this.isMaxRanges = true;
            }
            else {
                this.validate();
            }
        }
        addRangeComponentInterval() {
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
        }
        deleteRangeComponent(index) {
            this.rangeSubset.rangeItem.splice(index, 1);
            this.isIntervals.splice(index, 1);
            this.isMaxRanges = false;
            this.validate();
        }
        getIndexByRangeName(rangeName) {
            for (let i = 0; i < this.availableRanges.length; i++) {
                if (this.availableRanges[i] == rangeName) {
                    return i;
                }
            }
        }
        getSelectedRangeIndexesByIndex(index) {
            let isInterval = this.isIntervals[index];
            let result = [];
            if (!isInterval) {
                let rangeItem = this.rangeSubset.rangeItem[index];
                let rangeName = rangeItem.rangeComponent;
                let rangeIndex = this.getIndexByRangeName(rangeName);
                result.push(rangeIndex, rangeIndex);
            }
            else {
                let rangeItem = this.rangeSubset.rangeItem[index];
                let fromRangeName = rangeItem.startComponent.rangeComponent;
                let endRangeName = rangeItem.endComponent.rangeComponent;
                let fromRangeIndex = this.getIndexByRangeName(fromRangeName);
                let endRangeIndex = this.getIndexByRangeName(endRangeName);
                result.push(fromRangeIndex, endRangeIndex);
            }
            return result;
        }
        getListOfSelectedRangeIndexes() {
            let result = [];
            for (let i = 0; i < this.isIntervals.length; i++) {
                let tmpArray = this.getSelectedRangeIndexesByIndex(i);
                result.push(tmpArray);
            }
            return result;
        }
        validateByIndex(index) {
            let selectedRangeIndexesNestedArray = this.getListOfSelectedRangeIndexes();
            if (index < this.isIntervals.length) {
                let currentSelectedRangeIndexesArray = this.getSelectedRangeIndexesByIndex(index);
                for (let i = 0; i < selectedRangeIndexesNestedArray.length; i++) {
                    if (i == index) {
                        continue;
                    }
                    let selectedRangeIndexesArray = selectedRangeIndexesNestedArray[i];
                    let currentStartIndex = currentSelectedRangeIndexesArray[0];
                    let currentEndIndex = currentSelectedRangeIndexesArray[1];
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
        }
        validate() {
            let selectedRangeIndexesNestedArray = this.getListOfSelectedRangeIndexes();
            for (let i = 0; i < this.isIntervals.length; i++) {
                let result = this.validateByIndex(i);
                if (result == false) {
                    return;
                }
            }
            this.errorMessage = "";
        }
    }
    rasdaman.RangeSubsettingModel = RangeSubsettingModel;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class WCSScalingExtensionModel {
        constructor(coverageDescription) {
            this.coverageDescription = coverageDescription;
            var i = 0;
            var axes = [];
            coverageDescription.boundedBy.envelope.axisLabels.forEach(label => {
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
        getScaling() {
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
        }
        clearScaling() {
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
        }
        getScaleByFactor() {
            if (this.scaleByFactor.scaleFactor != WCSScalingExtensionModel.DEFAULT_SCALE_FACTOR) {
                return this.scaleByFactor;
            }
            else {
                return null;
            }
        }
        getScaleAxesByFactor() {
            for (var i = 0; i < this.scaleAxesByFactor.scaleAxis.length; ++i) {
                if (this.scaleAxesByFactor.scaleAxis[i].scaleFactor != WCSScalingExtensionModel.DEFAULT_SCALE_FACTOR) {
                    return this.scaleAxesByFactor;
                }
            }
            return null;
        }
        getScaleToSize() {
            for (var i = 0; i < this.scaleToSize.targetAxisSize.length; ++i) {
                if (this.scaleToSize.targetAxisSize[i].targetSize != WCSScalingExtensionModel.DEFAULT_AXIS_SIZE) {
                    return this.scaleToSize;
                }
            }
            return null;
        }
        getScaleToExtent() {
            for (var i = 0; i < this.scaleToExtent.targetAxisExtent.length; ++i) {
                var low = this.coverageDescription.boundedBy.envelope.lowerCorner.values[i];
                var high = this.coverageDescription.boundedBy.envelope.upperCorner.values[i];
                if (this.scaleToExtent.targetAxisExtent[i].low != low
                    || this.scaleToExtent.targetAxisExtent[i].high != high) {
                    return this.scaleToExtent;
                }
            }
            return null;
        }
    }
    WCSScalingExtensionModel.DEFAULT_SCALE_FACTOR = 1.0;
    WCSScalingExtensionModel.DEFAULT_AXIS_SIZE = 0.0;
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
    class WCSInterpolationExtensionModel {
        constructor(serverCapabilities) {
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
        getInterpolation() {
            var interpolationUri = "";
            if (this.selectedInterpolationMethod) {
                interpolationUri = this.selectedInterpolationMethod.uri;
            }
            return new wcs.Interpolation(interpolationUri);
        }
    }
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
    class WCSCRSExtensionModel {
        constructor(serverCapabilities) {
        }
        getCRS() {
            return new wcs.CRS(this.wcsGetCoverageSubsettingCRS, this.wcsGetCoverageOutputCRS);
        }
    }
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
    class WCSClippingExtensionModel {
        constructor(serverCapabilities) {
        }
        getClipping() {
            return new wcs.Clipping(this.wcsGetCoverageClipping);
        }
    }
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
    class RootController {
        constructor($http, $q, $scope, $rootScope, $state, settings, errorHandlingService, credentialService) {
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
            $rootScope.$watch("homeLoggedIn", (newValue, oldValue) => {
                if (newValue === true) {
                    $scope.showView($scope.wsclient, "services");
                }
            });
            $scope.checkPetascopeEnableAuthentication = function () {
                var result = $q.defer();
                var requestUrl = settings.contextPath + "/CheckEnableAuthentication";
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
                        var requestUrl = settings.contextPath + "/CheckRadamanCredentials";
                        $http.get(requestUrl, {
                            headers: credentialService.createBasicAuthenticationHeader(credential.username, credential.password)
                        }).then(function (dataObj) {
                            var data = JSON.parse(dataObj.data);
                            if (data) {
                                $rootScope.homeLoggedIn = true;
                                $rootScope.usernameLoggedIn = credential.username;
                                $scope.showView($scope.wsclient, "services");
                                return;
                            }
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
        initializeViews($scope) {
            $scope.login = {
                view: "login"
            };
            $scope.wsclient = {
                view: "wsclient"
            };
        }
    }
    RootController.$inject = ["$http", "$q", "$scope", "$rootScope",
        "$state", "rasdaman.WCSSettingsService", "rasdaman.ErrorHandlingService",
        "rasdaman.CredentialService"
    ];
    rasdaman.RootController = RootController;
})(rasdaman || (rasdaman = {}));
var wms;
(function (wms) {
    class ServiceIdentification {
        constructor(title, abstract) {
            this.serviceType = "OGC WMS";
            this.serviceTypeVersion = rasdaman.WMSSettingsService.version;
            this.title = title;
            this.abstract = abstract;
        }
    }
    wms.ServiceIdentification = ServiceIdentification;
})(wms || (wms = {}));
var wms;
(function (wms) {
    class Capabilities {
        constructor(source, gmlDocument) {
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
                getMapObj.getChildrenAsSerializedObjects("Format").forEach(obj => {
                    this.getMapFormat.push(obj.getValueAsString());
                });
                var layerObjs = capabilityObj.getChildAsSerializedObject("Layer").getChildrenAsSerializedObjects("Layer");
                this.layers = [];
                let totalLocalLayerSizesInBytes = 0;
                let totalRemoteLayerSizesInBytes = 0;
                let totalLayerSizesInBytes = 0;
                layerObjs.forEach(obj => {
                    var name = obj.getChildAsSerializedObject("Name").getValueAsString();
                    var title = obj.getChildAsSerializedObject("Title").getValueAsString();
                    var abstract = obj.getChildAsSerializedObject("Abstract").getValueAsString();
                    var customizedMetadata = this.parseLayerCustomizedMetadata(obj);
                    if (customizedMetadata != null) {
                        if (customizedMetadata.hostname != null) {
                            this.showLayerLocationsColumn = true;
                        }
                        if (customizedMetadata.coverageSize != null) {
                            this.showLayerSizesColumn = true;
                        }
                        if (customizedMetadata.hostname === undefined) {
                            totalLocalLayerSizesInBytes += customizedMetadata.localCoverageSizeInBytes;
                        }
                        else {
                            totalRemoteLayerSizesInBytes += customizedMetadata.remoteCoverageSizeInBytes;
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
                    var layerGMLDocument = this.extractLayerGMLDocument(name);
                    this.layers.push(new wms.Layer(layerGMLDocument, name, title, abstract, customizedMetadata, westBoundLongitude, eastBoundLongitude, southBoundLatitude, northBoundLatitude, crs, minx, miny, maxx, maxy));
                });
                totalLayerSizesInBytes += totalLocalLayerSizesInBytes + totalRemoteLayerSizesInBytes;
                this.totalLocalLayerSizes = ows.CustomizedMetadata.convertNumberOfBytesToHumanReadable(totalLocalLayerSizesInBytes);
                this.totalRemoteLayerSizes = ows.CustomizedMetadata.convertNumberOfBytesToHumanReadable(totalRemoteLayerSizesInBytes);
                this.totalLayerSizes = ows.CustomizedMetadata.convertNumberOfBytesToHumanReadable(totalLayerSizesInBytes);
            }
        }
        parseLayerCustomizedMetadata(source) {
            let childElement = "ows:Metadata";
            let customizedMetadata = null;
            if (source.doesElementExist(childElement)) {
                customizedMetadata = new ows.CustomizedMetadata(source.getChildAsSerializedObject(childElement));
            }
            return customizedMetadata;
        }
        extractLayerGMLDocument(layerName) {
            var regex = /<Layer \S+[\s\S]*?<\/Layer>/g;
            var match = regex.exec(this.gmlDocument);
            while (match != null) {
                if (match[0].indexOf("<Name>" + layerName + "</Name>") !== -1) {
                    return match[0];
                }
                match = regex.exec(this.gmlDocument);
            }
            return null;
        }
    }
    wms.Capabilities = Capabilities;
})(wms || (wms = {}));
var rasdaman;
(function (rasdaman) {
    class WMSMainController {
        constructor($scope, $rootScope, $state) {
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
        initializeTabs($scope) {
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
        }
    }
    WMSMainController.$inject = ["$scope", "$rootScope", "$state"];
    rasdaman.WMSMainController = WMSMainController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class AdminMainController {
        constructor($scope, $rootScope, $state) {
            this.$scope = $scope;
            this.initializeTabs($scope);
            $rootScope.adminStateInformation = {
                loggedIn: false
            };
            $rootScope.loggedIn = false;
            $scope.tabs = [$scope.adminLogin];
            $rootScope.$watch("adminStateInformation.loggedIn", (newValue, oldValue) => {
                if (newValue) {
                    $scope.tabs = [$scope.adminOWSMetadataManagement];
                }
                else {
                    $scope.tabs = [$scope.adminLogin];
                }
            });
        }
        initializeTabs($scope) {
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
        }
    }
    AdminMainController.$inject = ["$scope", "$rootScope", "$state"];
    rasdaman.AdminMainController = AdminMainController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class LoginController {
        constructor($http, $q, $scope, $rootScope, $log, wcsSettingsService, wmsSettingsService, alertService, errorHandlingService, credentialService) {
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
            $scope.login = (...args) => {
                $rootScope.homeLoggedIn = false;
                $scope.displayError = false;
                wcsSettingsService.setWCSEndPoint($scope.petascopeEndPoint);
                wmsSettingsService.setWMSEndPoint($scope.petascopeEndPoint);
                $scope.checkPetascopeEnableAuthentication(wcsSettingsService.contextPath, $scope.credential).then((data) => {
                    if (JSON.parse(data)) {
                        var credential = $scope.credential;
                        credentialService.persitCredential($scope.petascopeEndPoint, credential);
                        $rootScope.homeLoggedIn = true;
                    }
                    else {
                        $scope.displayError = true;
                    }
                }, (error) => {
                    errorHandlingService.handleError(error);
                });
            };
            $scope.checkPetascopeEnableAuthentication = function (contextPath, credential) {
                var requestUrl = contextPath + "/CheckRadamanCredentials";
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
    rasdaman.LoginController = LoginController;
})(rasdaman || (rasdaman = {}));
var wms;
(function (wms) {
    class GetCapabilities {
        toKVP() {
            return "request=" + "GetCapabilities";
        }
    }
    wms.GetCapabilities = GetCapabilities;
})(wms || (wms = {}));
var wms;
(function (wms) {
    class Layer {
        constructor(gmlDocument, name, title, abstract, customizedMetadata, westBoundLongitude, eastBoundLongitude, southBoundLatitude, northBoundLatitude, crs, minx, miny, maxx, maxy) {
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
            for (var j = 0; j < 3; ++j) {
                this.layerDimensions.push(dimen);
            }
            j = 3;
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
            this.getDownscaledCollectionLevelsFromGMLDocument();
        }
        initialiseDimenison() {
            return {
                name: '',
                array: [],
                startPos: 0,
                isTemporal: false
            };
        }
        buildDimensionAxisFromGMLDocumet(dim) {
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
        }
        getDownscaledCollectionLevelsFromGMLDocument() {
            this.downscaledCollectionLevels = [];
            var tmpXML = $.parseXML(this.gmlDocument);
            var text = $(tmpXML).find("rasdaman\\:downscaledCollectionLevels").text();
            if (text !== "") {
                this.downscaledCollectionLevels = text.split(",");
            }
        }
        buildStylesFromGMLDocument() {
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
                this.styles.push(new wms.Style(name, userAbstract, queryType, query, colorTableType, colorTableDefinition));
            }
        }
    }
    wms.Layer = Layer;
})(wms || (wms = {}));
var wms;
(function (wms) {
    class ServiceProvider {
        constructor(providerName, providerSite, contactPerson, positionName, email, voicePhone, address, city, postCode, country) {
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
    }
    wms.ServiceProvider = ServiceProvider;
})(wms || (wms = {}));
var wms;
(function (wms) {
    class BBox {
        constructor(xmin, ymin, xmax, ymax) {
            this.xmin = xmin;
            this.ymin = ymin;
            this.xmax = xmax;
            this.ymax = ymax;
        }
    }
    wms.BBox = BBox;
})(wms || (wms = {}));
var wms;
(function (wms) {
    class CoverageExtent {
        constructor(coverageId, xmin, ymin, xmax, ymax) {
            this.coverageId = coverageId;
            this.bbox = new wms.BBox(xmin, ymin, xmax, ymax);
            this.displayFootprint = false;
        }
    }
    wms.CoverageExtent = CoverageExtent;
})(wms || (wms = {}));
var rasdaman;
(function (rasdaman) {
    class WMSService {
        constructor($http, $q, settings, wcsSettings, serializedObjectFactory, $window, credentialService) {
            this.$http = $http;
            this.$q = $q;
            this.settings = settings;
            this.wcsSettings = wcsSettings;
            this.serializedObjectFactory = serializedObjectFactory;
            this.$window = $window;
            this.credentialService = credentialService;
        }
        getServerCapabilities(request) {
            var result = this.$q.defer();
            var self = this;
            var currentHeaders = {};
            var requestUrl = this.settings.wmsFullEndpoint + "&" + request.toKVP();
            this.$http.get(requestUrl, {
                headers: this.credentialService.createRequestHeader(this.settings.wmsEndpoint, currentHeaders)
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
        }
        updateLayerStyleRequest(updateLayerStyle) {
            var result = this.$q.defer();
            var requestUrl = this.settings.wmsEndpoint;
            var currentHeaders = { "Content-Type": "application/x-www-form-urlencoded" };
            var request = {
                method: 'POST',
                url: requestUrl,
                transformResponse: null,
                headers: this.credentialService.createRequestHeader(this.settings.wmsEndpoint, currentHeaders),
                data: this.settings.wmsServiceNameVersion + "&" + updateLayerStyle.toKVP()
            };
            this.$http(request).then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        }
        insertLayerStyleRequest(insertLayerStyle) {
            var result = this.$q.defer();
            var requestUrl = this.settings.wmsEndpoint;
            var currentHeaders = { "Content-Type": "application/x-www-form-urlencoded" };
            var request = {
                method: 'POST',
                url: requestUrl,
                transformResponse: null,
                headers: this.credentialService.createRequestHeader(this.settings.wmsEndpoint, currentHeaders),
                data: this.settings.wmsServiceNameVersion + "&" + insertLayerStyle.toKVP()
            };
            this.$http(request).then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        }
        deleteLayerStyleRequest(request) {
            var result = this.$q.defer();
            var requestUrl = this.settings.wmsFullEndpoint + "&" + request.toKVP();
            var currentHeaders = {};
            this.$http.get(requestUrl, {
                headers: this.credentialService.createRequestHeader(this.settings.wmsEndpoint, currentHeaders),
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
        }
        insertLayerDownscaledCollectionLevelRequest(request) {
            var result = this.$q.defer();
            var requestUrl = this.wcsSettings.wcsFullEndpoint + "&" + request.toKVP();
            var currentHeaders = {};
            this.$http.get(requestUrl, {
                headers: this.credentialService.createRequestHeader(this.settings.wmsEndpoint, currentHeaders),
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
        }
        deleteLayerDownscaledCollectionLevelRequest(request) {
            var result = this.$q.defer();
            var requestUrl = this.wcsSettings.wcsFullEndpoint + "&" + request.toKVP();
            var currentHeaders = {};
            this.$http.get(requestUrl, {
                headers: this.credentialService.createRequestHeader(this.settings.wmsEndpoint, currentHeaders),
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
        }
    }
    WMSService.$inject = ["$http", "$q", "rasdaman.WMSSettingsService", "rasdaman.WCSSettingsService",
        "rasdaman.common.SerializedObjectFactory", "$window",
        "rasdaman.CredentialService"];
    rasdaman.WMSService = WMSService;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class WMSGetCapabilitiesController {
        constructor($rootScope, $scope, $log, settings, wmsService, alertService, errorHandlingService, webWorldWindService) {
            this.$rootScope = $rootScope;
            this.$scope = $scope;
            this.$log = $log;
            this.settings = settings;
            this.wmsService = wmsService;
            this.alertService = alertService;
            this.errorHandlingService = errorHandlingService;
            this.webWorldWindService = webWorldWindService;
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
            $scope.initCheckboxesForCoverageIds = () => {
                var layerArray = $scope.capabilities.layers;
                for (var i = 0; i < layerArray.length; i++) {
                    layerArray[i].displayFootprint = false;
                }
            };
            $scope.displayFootprintOnGlobe = (coverageId) => {
                webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, coverageId);
            };
            $scope.displayAllFootprintsOnGlobe = (status) => {
                if (status == true) {
                    for (var i = 0; i < $scope.capabilities.layers.length; i++) {
                        var coverageExtent = $scope.capabilities.layers[i].coverageExtent;
                        var coverageId = coverageExtent.coverageId;
                        if (coverageExtent.displayFootprint == false) {
                            $scope.capabilities.layers[i].displayFootprint = true;
                            webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, coverageId);
                        }
                    }
                }
                else {
                    for (var i = 0; i < $scope.capabilities.layers.length; i++) {
                        var coverageExtent = $scope.capabilities.layers[i].coverageExtent;
                        var coverageId = coverageExtent.coverageId;
                        if (coverageExtent.displayFootprint == true) {
                            $scope.capabilities.layers[i].displayFootprint = false;
                            webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, coverageId);
                        }
                    }
                }
            };
            $scope.$on("reloadWMSServerCapabilities", function (event, b) {
                $scope.getServerCapabilities();
            });
            $scope.$watch("wmsStateInformation.reloadServerCapabilities", (capabilities) => {
                if ($scope.wmsStateInformation.reloadServerCapabilities == true) {
                    $scope.getServerCapabilities();
                }
                $scope.wmsStateInformation.reloadServerCapabilities = false;
            });
            $scope.handleGetServerCapabilities = () => {
                $scope.getServerCapabilities();
                $scope.showAllFootprints.isChecked = false;
            };
            $scope.getServerCapabilities = (...args) => {
                if (!$scope.wmsServerEndpoint) {
                    alertService.error("The entered WMS endpoint is invalid.");
                    return;
                }
                settings.wmsEndpoint = $scope.wmsServerEndpoint;
                settings.setWMSFullEndPoint();
                var capabilitiesRequest = new wms.GetCapabilities();
                wmsService.getServerCapabilities(capabilitiesRequest)
                    .then((response) => {
                    $scope.capabilitiesDocument = response.document;
                    $scope.capabilities = response.value;
                    $scope.isAvailableLayersOpen = true;
                    $scope.isServiceIdentificationOpen = true;
                    $scope.isServiceProviderOpen = true;
                    $scope.initCheckboxesForCoverageIds();
                    var coverageExtentArray = [];
                    for (var i = 0; i < $scope.capabilities.layers.length; i++) {
                        coverageExtentArray.push($scope.capabilities.layers[i].coverageExtent);
                    }
                    webWorldWindService.prepareCoveragesExtentsForGlobe(canvasId, coverageExtentArray);
                }, (...args) => {
                    $scope.capabilitiesDocument = null;
                    $scope.capabilities = null;
                    $scope.isAvailableLayersOpen = false;
                    $scope.isServiceIdentificationOpen = false;
                    $scope.isServiceProviderOpen = false;
                    errorHandlingService.handleError(args);
                    $log.error(args);
                })
                    .finally(() => {
                    $scope.wmsStateInformation.serverCapabilities = $scope.capabilities;
                });
            };
        }
    }
    WMSGetCapabilitiesController.$inject = [
        "$rootScope",
        "$scope",
        "$log",
        "rasdaman.WMSSettingsService",
        "rasdaman.WMSService",
        "Notification",
        "rasdaman.ErrorHandlingService",
        "rasdaman.WebWorldWindService"
    ];
    rasdaman.WMSGetCapabilitiesController = WMSGetCapabilitiesController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class WMSDescribeLayerController {
        constructor($scope, $rootScope, $log, settings, wmsService, wcsService, alertService, errorHandlingService, webWorldWindService) {
            $scope.getMapRequestURL = null;
            $scope.layerNames = [];
            $scope.layers = [];
            $scope.displayWMSLayer = false;
            $scope.timeString = null;
            $scope.coverageDescription = null;
            var canvasId = "wmsCanvasDescribeLayer";
            var WCPS_QUERY_FRAGMENT = 0;
            var RASQL_QUERY_FRAGMENT = 1;
            $rootScope.$watch("wmsSelectedLayerName", (layerName) => {
                if (layerName != null) {
                    $scope.selectedLayerName = layerName;
                    $scope.describeLayer();
                }
            });
            $scope.isLayerNameValid = () => {
                for (var i = 0; i < $scope.layers.length; i++) {
                    if ($scope.layers[i].name == $scope.selectedLayerName) {
                        return true;
                    }
                }
                return false;
            };
            $scope.$watch("wmsStateInformation.serverCapabilities", (capabilities) => {
                if (capabilities) {
                    $scope.layers = [];
                    $scope.layerNames = [];
                    $scope.display3DLayerNotification = false;
                    $scope.display4BandsExclamationMark = false;
                    capabilities.layers.forEach((layer) => {
                        $scope.layerNames.push(layer.name);
                        $scope.layers.push(layer);
                    });
                    $scope.describeLayer();
                }
            });
            $scope.describeLayer = function () {
                $scope.displayWMSLayer = false;
                $scope.selectedStyleName = "";
                $("#styleName").val("");
                $("#styleAbstract").val("");
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
                            .then((response) => {
                            $scope.coverageDescription = response.value;
                            var dimensions = $scope.coverageDescription.boundedBy.envelope.srsDimension;
                            addSliders(dimensions, coveragesExtents);
                            selectOptionsChange();
                            webWorldWindService.prepareCoveragesExtentsForGlobe(canvasId, coverageExtentArray);
                            webWorldWindService.showCoverageExtentOnGlobe(canvasId, $scope.layer.name);
                        }, (...args) => {
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
                for (var j = 0; j <= dimensions; ++j) {
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
                var dimStr = [];
                for (var j = 0; j < 3; ++j) {
                    dimStr.push('');
                }
                for (var j = 3; j <= dimensions; j++) {
                    if ($scope.layer.layerDimensions[j].isTemporal == true) {
                        dimStr.push('&' + $scope.layer.layerDimensions[j].name + '="' + $scope.layer.layerDimensions[j].array[0] + '"');
                        $scope.timeString = $scope.layer.layerDimensions[j].array[0];
                    }
                    else {
                        dimStr.push('&' + $scope.layer.layerDimensions[j].name + '=' + $scope.layer.layerDimensions[j].array[0]);
                    }
                }
                for (var j = 3; j <= dimensions; j++) {
                    urlDimensions += dimStr[j];
                }
                var getMapRequest = new wms.GetMap($scope.layer.name, urlDimensions, 800, 600, $scope.selectedStyleName);
                var url = settings.wmsFullEndpoint + "&" + getMapRequest.toKVP();
                $scope.getMapRequestURL = url;
                $('#getMapRequestURL').text($scope.getMapRequestURL);
                webWorldWindService.loadGetMapResultOnGlobe(canvasId, $scope.selectedLayerName, null, $scope.bboxLayer, $scope.displayWMSLayer, $scope.timeString);
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
                        webWorldWindService.loadGetMapResultOnGlobe(canvasId, $scope.selectedLayerName, $scope.selectedStyleName, auxbBox, $scope.displayWMSLayer, $scope.timeString);
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
                        webWorldWindService.loadGetMapResultOnGlobe(canvasId, $scope.selectedLayerName, $scope.selectedStyleName, auxbBox, $scope.displayWMSLayer, $scope.timeString);
                    }
                });
                $("#longSlider").tooltip();
                $("#longSlider").attr('data-original-title', $scope.bboxLayer.xmin + ':' + $scope.bboxLayer.xmax);
                if ($scope.firstChangedSlider[2] == false) {
                    $("#longSlider").slider('values', [0, numberStepsLong]);
                }
                var sufixSlider = "d";
                for (var j = 3; j <= dimensions; j++) {
                    $("<div />", { class: "containerSliders", id: "containerSlider" + j + sufixSlider })
                        .appendTo($("#sliders"));
                    $("<label />", { class: "sliderLabel", id: "label" + j + sufixSlider })
                        .appendTo($("#containerSlider" + j + sufixSlider));
                    $("#label" + j + sufixSlider).text($scope.layer.layerDimensions[j].name + ':');
                    $("<div />", { class: "slider", id: "slider" + j + sufixSlider })
                        .appendTo($("#containerSlider" + j + sufixSlider));
                    let sliderId = "#slider" + j + sufixSlider;
                    $(function () {
                        $(sliderId).slider({
                            max: $scope.layer.layerDimensions[j].array.length - 1,
                            create: function (event, slider) {
                                this.sliderObj = $scope.layer.layerDimensions[j];
                                var sizeSlider = $scope.layer.layerDimensions[j].array.length - 1;
                                for (var it = 1; it < sizeSlider; ++it) {
                                    $("<label>|</label>").css('left', (it / sizeSlider * 100) + '%')
                                        .appendTo($(sliderId));
                                }
                            },
                            slide: function (event, slider) {
                                $scope.firstChangedSlider[this.sliderPos] = true;
                                if (this.sliderObj.isTemporal == true) {
                                    dimStr[j] = this.sliderObj.name + '="' + this.sliderObj.array[slider.value] + '"';
                                    $scope.timeString = this.sliderObj.array[slider.value];
                                }
                                else {
                                    dimStr[j] = this.sliderObj.name + '=' + this.sliderObj.array[slider.value];
                                }
                                var pos1 = url.indexOf('&' + this.sliderObj.name + '=');
                                var pos2 = url.indexOf('&', pos1 + 1);
                                url = url.substr(0, pos1 + 1) + dimStr[j] + url.substr(pos2, url.length - pos2);
                                $scope.getMapRequestURL = url;
                                var tooltip = this.sliderObj.array[slider.value];
                                $(sliderId).attr('data-original-title', tooltip);
                                $(sliderId).tooltip('show');
                                renewDisplayedWMSGetMapURL(url);
                                webWorldWindService.loadGetMapResultOnGlobe(canvasId, $scope.selectedLayerName, $scope.selectedStyleName, auxbBox, $scope.displayWMSLayer, $scope.timeString);
                            }
                        });
                    });
                    $(sliderId).tooltip();
                    $(sliderId).attr('data-original-title', $scope.layer.layerDimensions[j].array[0]);
                    if ($scope.firstChangedSlider[j] == false) {
                        $(sliderId).slider('value', 0);
                    }
                }
            }
            $scope.isLayerDocumentOpen = false;
            $scope.showWMSLayerOnGlobe = (styleName) => {
                $scope.selectedStyleName = styleName;
                $scope.displayWMSLayer = true;
                renewDisplayedWMSGetMapURL($scope.getMapRequestURL);
                webWorldWindService.loadGetMapResultOnGlobe(canvasId, $scope.selectedLayerName, styleName, $scope.bboxLayer, true, $scope.timeString);
            };
            $scope.hideWMSLayerOnGlobe = () => {
                $scope.displayWMSLayer = false;
                webWorldWindService.loadGetMapResultOnGlobe(canvasId, $scope.selectedLayerName, $scope.selectedStyleName, $scope.bboxLayer, false, $scope.timeString);
            };
            $scope.insertDownscaledCollectionLevel = () => {
                let level = $("#levelValue").val();
                if (!(!isNaN(level) && Number(level) > 1)) {
                    alertService.error("Downscaled collection level must be positive numer and greater than 1, given <b>" + level + "</b>");
                }
                else if ($scope.layer.downscaledCollectionLevels.includes(level)) {
                    alertService.error("Downscaled collection level <b>" + level + "</b> already exists.");
                }
                else {
                    var insertLayerDownscaledCollectionLevel = new wms.InsertLayerDownscaledCollectionLevel($scope.layer.name, level);
                    wmsService.insertLayerDownscaledCollectionLevelRequest(insertLayerDownscaledCollectionLevel).then((...args) => {
                        alertService.success("Successfully insert downscaled collection level <b>" + level + "</b> of layer with name <b>" + $scope.layer.name + "</b>");
                        $scope.wmsStateInformation.reloadServerCapabilities = true;
                    }, (...args) => {
                        errorHandlingService.handleError(args);
                    }).finally(function () {
                    });
                }
            };
            $scope.deleteDownscaledCollectionLevel = (level) => {
                var deleteLayerDownscaledCollectionLevel = new wms.DeleteLayerDownscaledCollectionLevel($scope.layer.name, level);
                wmsService.deleteLayerDownscaledCollectionLevelRequest(deleteLayerDownscaledCollectionLevel).then((...args) => {
                    alertService.success("Successfully delete downscaled collection level <b>" + level + "</b> of layer with name <b>" + $scope.layer.name + "</b>");
                    $scope.wmsStateInformation.reloadServerCapabilities = true;
                }, (...args) => {
                    errorHandlingService.handleError(args);
                }).finally(function () {
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
                    const reader = new FileReader();
                    reader.onload = function fileReadCompleted() {
                        $("#styleColorTableDefinition").val(reader.result);
                    };
                    reader.readAsText(this.files[0]);
                });
            }
            $scope.isStyleNameValid = (styleName) => {
                for (var i = 0; i < $scope.layer.styles.length; ++i) {
                    if ($scope.layer.styles[i].name == styleName) {
                        return true;
                    }
                }
                return false;
            };
            $scope.describeStyleToUpdate = (styleName) => {
                for (var i = 0; i < $scope.layer.styles.length; i++) {
                    var styleObj = $scope.layer.styles[i];
                    if (styleObj.name == styleName) {
                        $("#styleName").val(styleObj.name);
                        $("#styleAbstract").val(styleObj.abstract);
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
                        break;
                    }
                }
            };
            $scope.validateStyle = () => {
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
            $scope.updateStyle = () => {
                if ($scope.validateStyle()) {
                    var styleName = $("#styleName").val();
                    var styleAbstract = $("#styleAbstract").val();
                    var styleQueryType = $("#styleQueryType").val();
                    var styleQuery = $("#styleQuery").val();
                    var styleColorTableType = $("#styleColorTableType").val();
                    var styleColorTableDefintion = $("#styleColorTableDefinition").val();
                    if (!$scope.isStyleNameValid(styleName)) {
                        alertService.error("Style name '" + styleName + "' does not exist to update.");
                        return;
                    }
                    var updateLayerStyle = new wms.UpdateLayerStyle($scope.layer.name, styleName, styleAbstract, styleQueryType, styleQuery, styleColorTableType, styleColorTableDefintion);
                    wmsService.updateLayerStyleRequest(updateLayerStyle).then((...args) => {
                        alertService.success("Successfully update style with name <b>" + styleName + "</b> of layer with name <b>" + $scope.layer.name + "</b>");
                        $scope.wmsStateInformation.reloadServerCapabilities = true;
                    }, (...args) => {
                        errorHandlingService.handleError(args);
                    }).finally(function () {
                    });
                }
            };
            $scope.insertStyle = () => {
                if ($scope.validateStyle()) {
                    var styleName = $("#styleName").val();
                    var styleAbstract = $("#styleAbstract").val();
                    var styleQueryType = $("#styleQueryType").val();
                    var styleQuery = $("#styleQuery").val();
                    var styleColorTableType = $("#styleColorTableType").val();
                    var styleColorTableDefintion = $("#styleColorTableDefinition").val();
                    if ($scope.isStyleNameValid(styleName)) {
                        alertService.error("Style name '" + styleName + "' already exists, cannot insert same name.");
                        return;
                    }
                    var insertLayerStyle = new wms.InsertLayerStyle($scope.layer.name, styleName, styleAbstract, styleQueryType, styleQuery, styleColorTableType, styleColorTableDefintion);
                    wmsService.insertLayerStyleRequest(insertLayerStyle).then((...args) => {
                        alertService.success("Successfully insert style with name <b>" + styleName + "</b> of layer with name <b>" + $scope.layer.name + "</b>");
                        $scope.wmsStateInformation.reloadServerCapabilities = true;
                    }, (...args) => {
                        errorHandlingService.handleError(args);
                    }).finally(function () {
                    });
                }
            };
            $scope.deleteStyle = (styleName) => {
                var deleteLayerStyle = new wms.DeleteLayerStyle($scope.layer.name, styleName);
                wmsService.deleteLayerStyleRequest(deleteLayerStyle).then((...args) => {
                    alertService.success("Successfully delete style with name <b>" + styleName + "</b> of layer with name <b>" + $scope.layer.name + "</b>");
                    $scope.wmsStateInformation.reloadServerCapabilities = true;
                }, (...args) => {
                    errorHandlingService.handleError(args);
                }).finally(function () {
                });
            };
        }
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
    rasdaman.WMSDescribeLayerController = WMSDescribeLayerController;
})(rasdaman || (rasdaman = {}));
var login;
(function (login) {
    class Credential {
        constructor(username, password) {
            this.username = username;
            this.password = password;
        }
        toKVP() {
            return "username=" + this.username +
                "&password=" + this.password;
        }
    }
    login.Credential = Credential;
})(login || (login = {}));
var rasdaman;
(function (rasdaman) {
    class AdminService {
        constructor($http, $rootScope, $q, settings, serializedObjectFactory, $window) {
            this.$http = $http;
            this.$rootScope = $rootScope;
            this.$q = $q;
            this.settings = settings;
            this.serializedObjectFactory = serializedObjectFactory;
            this.$window = $window;
            this.persitLoggedIn = () => {
                window.localStorage.setItem("adminLoggedIn", "true");
            };
            this.persitLoggedOut = () => {
                window.localStorage.removeItem("adminLoggedIn");
            };
            this.checkLoggedIn = () => {
                let tmp = window.localStorage.getItem("adminLoggedIn");
                return tmp != null;
            };
        }
        login(credential) {
            var result = this.$q.defer();
            var requestUrl = this.settings.wcsEndpoint + "/admin/Login";
            var request = {
                method: 'POST',
                url: requestUrl,
                transformResponse: null,
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                data: credential.toKVP()
            };
            this.$http(request).then(function (data) {
                this.adminLoggedIn = true;
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        }
        updateServiceIdentification(serviceIdentification) {
            var result = this.$q.defer();
            var requestUrl = this.settings.wcsEndpoint + "/admin/UpdateServiceIdentification";
            var request = {
                method: 'POST',
                url: requestUrl,
                transformResponse: null,
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                withCredentials: true,
                data: serviceIdentification.toKVP()
            };
            this.$http(request).then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        }
        updateServiceProvider(serviceProvider) {
            var result = this.$q.defer();
            var requestUrl = this.settings.wcsEndpoint + "/admin/UpdateServiceProvider";
            var request = {
                method: 'POST',
                url: requestUrl,
                transformResponse: null,
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                withCredentials: true,
                data: serviceProvider.toKVP()
            };
            this.$http(request).then(function (data) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });
            return result.promise;
        }
    }
    AdminService.$inject = ["$http", "$rootScope", "$q", "rasdaman.WCSSettingsService", "rasdaman.common.SerializedObjectFactory", "$window"];
    rasdaman.AdminService = AdminService;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class AdminLoginController {
        constructor($scope, $rootScope, $log, settings, adminService, alertService, errorHandlingService) {
            this.$scope = $scope;
            this.$rootScope = $rootScope;
            this.$log = $log;
            this.settings = settings;
            this.adminService = adminService;
            this.alertService = alertService;
            this.errorHandlingService = errorHandlingService;
            $scope.credential = new login.Credential("", "");
            $rootScope.adminStateInformation.loggedIn = adminService.checkLoggedIn();
            $scope.login = (...args) => {
                adminService.login($scope.credential).then((...args) => {
                    alertService.success("Successfully logged in.");
                    $rootScope.adminStateInformation.loggedIn = true;
                    adminService.persitLoggedIn();
                }, (...args) => {
                    errorHandlingService.handleError(args);
                }).finally(function () {
                });
            };
        }
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
    rasdaman.AdminLoginController = AdminLoginController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    class AdminOWSMetadataManagementController {
        constructor($scope, $rootScope, $log, wcsService, settings, adminService, alertService, errorHandlingService) {
            this.$scope = $scope;
            this.$rootScope = $rootScope;
            this.$log = $log;
            this.wcsService = wcsService;
            this.settings = settings;
            this.adminService = adminService;
            this.alertService = alertService;
            this.errorHandlingService = errorHandlingService;
            $rootScope.$on("reloadServerCapabilities", (event, value) => {
                $scope.getServerCapabilities();
            });
            $rootScope.$watch("adminStateInformation.loggedIn", (newValue, oldValue) => {
                $scope.getServerCapabilities();
            });
            $scope.getServerCapabilities = (...args) => {
                var capabilitiesRequest = new wcs.GetCapabilities();
                wcsService.getServerCapabilities(capabilitiesRequest)
                    .then((response) => {
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
                }, (...args) => {
                    errorHandlingService.handleError(args);
                    $log.error(args);
                })
                    .finally(() => {
                });
            };
            $scope.updateServiceIdentification = (...args) => {
                adminService.updateServiceIdentification($scope.serviceIdentification).then((...args) => {
                    alertService.success("Successfully update Service Identifcation to Petascope database.");
                }, (...args) => {
                    errorHandlingService.handleError(args);
                }).finally(function () {
                });
            };
            $scope.updateServiceProvider = (...args) => {
                adminService.updateServiceProvider($scope.serviceProvider).then((...args) => {
                    alertService.success("Successfully update Service Provider to Petascope database.");
                }, (...args) => {
                    errorHandlingService.handleError(args);
                }).finally(function () {
                });
            };
            $scope.logOut = (...args) => {
                adminService.persitLoggedOut();
                $rootScope.adminStateInformation.loggedIn = false;
            };
        }
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
    rasdaman.AdminOWSMetadataManagementController = AdminOWSMetadataManagementController;
})(rasdaman || (rasdaman = {}));
var rasdaman;
(function (rasdaman) {
    "use strict";
    class AngularConfig {
        constructor($httpProvider, $stateProvider, $locationProvider, $urlRouterProvider, NotificationProvider) {
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
    }
    AngularConfig.$inject = [
        "$httpProvider",
        "$stateProvider",
        "$locationProvider",
        "$urlRouterProvider",
        "NotificationProvider"
    ];
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
        .directive("scrollToBottom", rasdaman.common.scrollToBottom);
})(rasdaman || (rasdaman = {}));
var admin;
(function (admin) {
    class ServiceIdentification {
        constructor(serviceTitle, abstract) {
            this.serviceTitle = serviceTitle;
            this.abstract = abstract;
        }
        toKVP() {
            return "serviceTitle=" + this.serviceTitle +
                "&abstract=" + this.abstract;
        }
    }
    admin.ServiceIdentification = ServiceIdentification;
})(admin || (admin = {}));
var admin;
(function (admin) {
    class ServiceProvider {
        constructor(providerName, providerSite, individualName, positionName, role, email, voicePhone, facsimilePhone, hoursOfService, contactInstructions, city, administrativeArea, postalCode, country) {
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
        toKVP() {
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
        }
    }
    admin.ServiceProvider = ServiceProvider;
})(admin || (admin = {}));
var wms;
(function (wms) {
    class DeleteLayerDownscaledCollectionLevel {
        constructor(layerName, level) {
            this.request = "DeleteScaleLevel";
            this.layerName = layerName;
            this.level = level;
        }
        toKVP() {
            return "&request=" + this.request +
                "&coverageId=" + this.layerName +
                "&level=" + this.level;
        }
    }
    wms.DeleteLayerDownscaledCollectionLevel = DeleteLayerDownscaledCollectionLevel;
})(wms || (wms = {}));
var wms;
(function (wms) {
    class DeleteLayerStyle {
        constructor(layerName, name) {
            this.request = "DeleteStyle";
            this.layerName = layerName;
            this.name = name;
        }
        toKVP() {
            return "&request=" + this.request +
                "&name=" + this.name +
                "&layer=" + this.layerName;
        }
    }
    wms.DeleteLayerStyle = DeleteLayerStyle;
})(wms || (wms = {}));
var wms;
(function (wms) {
    class GetMap {
        constructor(layers, bbox, width, height, styles) {
            this.layers = layers;
            this.bbox = bbox;
            this.width = width;
            this.height = height;
            this.styles = styles;
        }
        toKVP() {
            return "request=" + "GetMap&layers=" + this.layers + "&bbox=" + this.bbox +
                "&width=" + this.width + "&height=" + this.height + "&crs=EPSG:4326&format=image/png&transparent=true&styles=" + this.styles;
        }
    }
    wms.GetMap = GetMap;
})(wms || (wms = {}));
var wms;
(function (wms) {
    class InsertLayerDownscaledCollectionLevel {
        constructor(layerName, level) {
            this.request = "InsertScaleLevel";
            this.layerName = layerName;
            this.level = level;
        }
        toKVP() {
            return "&request=" + this.request +
                "&coverageId=" + this.layerName +
                "&level=" + this.level;
        }
    }
    wms.InsertLayerDownscaledCollectionLevel = InsertLayerDownscaledCollectionLevel;
})(wms || (wms = {}));
var wms;
(function (wms) {
    class InsertLayerStyle {
        constructor(layerName, name, abstract, queryType, query, colorTableType, colorTableDefintion) {
            this.request = "InsertStyle";
            this.layerName = layerName;
            this.name = name;
            this.abstract = abstract;
            this.queryFragmentType = queryType;
            this.query = query;
            this.colorTableType = colorTableType;
            this.colorTableDefinition = colorTableDefintion;
        }
        toKVP() {
            var result = "&request=" + this.request +
                "&name=" + this.name +
                "&layer=" + this.layerName +
                "&abstract=" + this.abstract;
            result += "&" + this.queryFragmentType + "=" + this.query;
            result += "&ColorTableType=" + this.colorTableType +
                "&ColorTableDefinition=" + this.colorTableDefinition;
            return result;
        }
    }
    wms.InsertLayerStyle = InsertLayerStyle;
})(wms || (wms = {}));
var wms;
(function (wms) {
    class Style {
        constructor(name, abstract, queryType, query, colorTableType, colorTableDefinition) {
            this.name = name;
            this.abstract = abstract;
            this.queryType = queryType;
            this.query = query;
            this.colorTableType = colorTableType;
            this.colorTableDefinition = colorTableDefinition;
        }
    }
    wms.Style = Style;
})(wms || (wms = {}));
var wms;
(function (wms) {
    class UpdateLayerStyle {
        constructor(layerName, name, abstract, queryType, query, colorTableType, colorTableDefintion) {
            this.request = "UpdateStyle";
            this.layerName = layerName;
            this.name = name;
            this.abstract = abstract;
            this.queryFragmentType = queryType;
            this.query = query;
            this.colorTableType = colorTableType;
            this.colorTableDefinition = colorTableDefintion;
        }
        toKVP() {
            var result = "&request=" + this.request +
                "&name=" + this.name +
                "&layer=" + this.layerName +
                "&abstract=" + this.abstract;
            result += "&" + this.queryFragmentType + "=" + this.query;
            result += "&ColorTableType=" + this.colorTableType +
                "&ColorTableDefinition=" + this.colorTableDefinition;
            return result;
        }
    }
    wms.UpdateLayerStyle = UpdateLayerStyle;
})(wms || (wms = {}));
//# sourceMappingURL=main.js.map
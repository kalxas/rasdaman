"""
 *
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
 * Copyright 2003 - 2015 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""
import json
from abc import abstractmethod

from master.extra_metadata.extra_metadata import ExtraMetadata
from master.extra_metadata.extra_metadata_slice import ExtraMetadataSliceSubset


class ExtraMetadataSerializer:
    def __init__(self):
        pass

    @abstractmethod
    def serialize(self, extra_metadata):
        """
        Serializes a given extra metadata object
        :param ExtraMetadata extra_metadata: the extra metadata object to serialize
        :return: str
        """
        pass

    def to_dict(self, extra_metadata):
        """
        Returns a dict representation of the extra_metadata (global, local and bands metadata)
        :param ExtraMetadata extra_metadata: the extra metadata object to serialize
        :rtype: dict
        """
        global_meta = extra_metadata.global_extra_metadata.copy()
        global_meta["slices"] = []
        for metadata_slice in extra_metadata.slice_extra_metadata:
            slice = metadata_slice.metadata_dictionary
            slice["envelope"] = self.subset_to_dict(metadata_slice.subsets)
            global_meta["slices"].append(slice)

        global_meta["bands"] = extra_metadata.bands_extra_metadata.copy()
        global_meta["axes"] = extra_metadata.axes_extra_metadata.copy()

        # don't add empty attributes to coverage's metadata
        if len(global_meta["slices"]) == 0:
            global_meta.pop('slices', None)
        if len(global_meta["bands"]) == 0:
            global_meta.pop('bands', None)
        if len(global_meta["axes"]) == 0:
            global_meta.pop('axes', None)

        return global_meta

    @staticmethod
    def subset_to_dict(subsets):
        """
        Serializes a list of subsets in a bounded by string
        :param list[ExtraMetadataSliceSubset] subsets:
        :rtype: str
        """
        variables = {
            "axisLabels": [],
            "noOfDimensions": len(subsets),
            "lowerCorner": [],
            "upperCorner": []
        }
        for subset in subsets:
            variables["axisLabels"].append(str(subset.axis_name))
            variables["lowerCorner"].append(str(subset.interval.low))
            high = subset.interval.high if subset.interval.high is not None else subset.interval.low
            variables["upperCorner"].append(str(high))
        return variables


class JsonExtraMetadataSerializer(ExtraMetadataSerializer):
    def __init__(self):
        ExtraMetadataSerializer.__init__(self)

    def serialize(self, extra_metadata):
        return json.dumps(self.to_dict(extra_metadata), indent=2)


class XMLExtraMetadataSerializer(ExtraMetadataSerializer):
    def __init__(self):
        ExtraMetadataSerializer.__init__(self)

    def __xml_key(self, key):
        """
        Create a XML element <key>...<key> with key has no space between characeters
        :param str key: input key
        :return: str: a valid xml element
        """
        return key.replace(" ", "_")

    def serialize(self, extra_metadata):
        bounded_by_template = """<boundedBy>
                                    <Envelope axisLabels="{axisLabels}" srsDimension="{noOfDimensions}">
                                        <lowerCorner>{lowerCorner}</lowerCorner>
                                        <upperCorner>{upperCorner}</upperCorner>
                                    </Envelope>
                                </boundedBy>"""
        global_dict = self.to_dict(extra_metadata)
        xml_return = []
        for key, value in global_dict.items():
            if key == "bands" or key == "axes":
                # each band of bands is a dictionary of keys, values for band's metadata
                result = "<" + key + ">"
                for band_key, band_attributes in value.items():
                    result += "<" + self.__xml_key(band_key) + ">"
                    for band_attribute_key, band_attribute_value in band_attributes.items():
                        result += "<{0}>{1}</{0}>".format(self.__xml_key(band_attribute_key), band_attribute_value)
                    result += "</" + self.__xml_key(band_key) + ">"
                result += "</" + key + ">"
                xml_return.append(result)
            elif key != "slices":
                xml_return.append("<{0}>{1}</{0}>".format(self.__xml_key(key), value))

        # Only parse this slices (local metadata) if it exists
        if "slices" in global_dict:
            slices = global_dict["slices"]
            slices_xml = []
            for slice in slices:
                slice_xml = ["<slice>"]
                slice_xml.append(bounded_by_template.format(axisLabels=" ".join(slice['envelope']["axisLabels"]),
                                                            noOfDimensions=slice['envelope']["noOfDimensions"],
                                                            lowerCorner=" ".join(slice['envelope']["lowerCorner"]),
                                                            upperCorner=" ".join(slice['envelope']["upperCorner"])))
                for key, value in slice.items():
                    if key != 'envelope':
                        slice_xml.append("<{0}>{1}</{0}>".format(self.__xml_key(key), value))
                slice_xml.append("</slice>")
                slices_xml.append("\n".join(slice_xml))
            xml_return.append("<slices>\n{}\n</slices>".format("\n".join(slices_xml)))

        return "\n".join(xml_return)


class ExtraMetadataSerializerFactory():
    JSON_ENCODING = "json"
    XML_ENCODING = "xml"

    @staticmethod
    def is_encoding_type_valid(type):
        """
        Validates the extra metadata serializer type
        :return:
        """
        return type == ExtraMetadataSerializerFactory.JSON_ENCODING or type == ExtraMetadataSerializerFactory.XML_ENCODING

    @staticmethod
    def get_serializer(type):
        """
        Returns a serializer for the metatadata factory
        :param type: the type of the serializer, either JSON_ENCODING or XML_ENCODING
        :rtype: ExtraMetadataSerializer
        """
        if type == ExtraMetadataSerializerFactory.JSON_ENCODING:
            return JsonExtraMetadataSerializer()
        elif type == ExtraMetadataSerializerFactory.XML_ENCODING:
            return XMLExtraMetadataSerializer()
        raise RuntimeError("No serializers in the factory for the given encoding: " + type)

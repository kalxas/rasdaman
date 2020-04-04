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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""
import json
from abc import abstractmethod

from master.extra_metadata.extra_metadata import GlobalExtraMetadata, LocalExtraMetadata


class ExtraMetadataSerializer:

    KEY_BANDS = "bands"
    KEY_AXES = "axes"
    KEY_LOCAL_METADATA = "local_metadata"
    KEY_SLICE_SUBSETS = "slice_subsets"

    def __init__(self):
        pass

    @abstractmethod
    def serialize(self, extra_metadata):
        """
        Serializes a given extra metadata object
        :param GlobalExtraMetadata/LocalExtraMetadata extra_metadata: the extra metadata object to serialize
        :return: str
        """
        pass

    def to_dict(self, extra_metadata):
        """
        Returns a dict representation of the extra_metadata (global, local and bands metadata)
        :param GlobalExtraMetadata/LocalExtraMetadata extra_metadata: the extra metadata object to serialize
        :rtype: dict
        """
        if isinstance(extra_metadata, GlobalExtraMetadata):
            # Object is global extra metadata
            global_meta = extra_metadata.global_extra_metadata.copy()

            global_meta[self.KEY_BANDS] = extra_metadata.bands_extra_metadata.copy()
            global_meta[self.KEY_AXES] = extra_metadata.axes_extra_metadata.copy()

            # don't add empty attributes to coverage's metadata
            if len(global_meta[self.KEY_BANDS]) == 0:
                global_meta.pop(self.KEY_BANDS, None)
            if len(global_meta[self.KEY_AXES]) == 0:
                global_meta.pop(self.KEY_AXES, None)

            return global_meta
        elif isinstance(extra_metadata, LocalExtraMetadata):
            # Object is local extra metadata
            local_metadata_dict = {self.KEY_LOCAL_METADATA: extra_metadata.local_extra_metadata.copy(),
                                   self.KEY_SLICE_SUBSETS: extra_metadata.slice_subsets}

            return local_metadata_dict
        else:
            return extra_metadata

    def _create_elements_for_bounded_by(self, slice_subsets):
        """
        From slice_subsets create child elements for boundedBy element
        :param list[ExtraMetadataSliceSubset] slice_subsets: list of axis subsets
        """
        axis_labels = " ".join([x.axis_name for x in slice_subsets])
        no_of_dimensions = len(slice_subsets)
        lower_corner = " ".join([str(x.interval.low) for x in slice_subsets])
        upper_corner = " ".join([str(x.interval.high) if x.interval.high is not None else str(x.interval.low)
                                 for x in slice_subsets])

        return str(axis_labels), str(no_of_dimensions), str(lower_corner), str(upper_corner)


class JsonExtraMetadataSerializer(ExtraMetadataSerializer):
    def __init__(self):
        ExtraMetadataSerializer.__init__(self)

    def serialize(self, extra_metadata):
        result_dict = self.to_dict(extra_metadata)

        if not "local_metadata" in result_dict:
            # Serializing global metadata
            output = json.dumps(result_dict, indent=2)
        else:
            # Serializing local metadata
            tmp_dict = result_dict[self.KEY_LOCAL_METADATA]
            slice_subsets = result_dict[self.KEY_SLICE_SUBSETS]
            json_return = []

            # First collect all local metadata attributes as key -> value
            for key, value in tmp_dict.items():
                result = '"{}": "{}"'.format(key, value)
                json_return.append(result)

            axis_labels, no_of_dimensions, lower_corner, upper_corner = self._create_elements_for_bounded_by(slice_subsets)
            # as lower_corner or upper_corner can contain "datetime"
            lower_corner = lower_corner.replace('"', '\\"')
            upper_corner = upper_corner.replace('"', '\\"')

            bounded_by_template = '"boundedBy": {"Envelope": { ' \
                                  '  "axisLabels": "$axisLabels", "srsDimension": $no_of_dimensions, ' \
                                  '  "lowerCorner": "$lowerCorner", "upperCorner": "$upperCorner" } }'
            bounded_by = bounded_by_template.replace("$axisLabels", axis_labels)\
                                            .replace("$no_of_dimensions", no_of_dimensions)\
                                            .replace("$lowerCorner", lower_corner)\
                                            .replace("$upperCorner", upper_corner)
            json_return.append(bounded_by)
            output = "{ " + ", ".join(json_return) + " }"

        return output


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
        result_dict = self.to_dict(extra_metadata)
        xml_return = []

        if not self.KEY_LOCAL_METADATA in result_dict:
            # Serializing global metadata attributes dict

            last_keys = [self.KEY_BANDS, self.KEY_AXES]
            # 1. handle keys not in last_keys
            for key, value in result_dict.items():
                if key not in last_keys:
                    xml_return.append("<{0}>{1}</{0}>".format(self.__xml_key(key), value))

            # 2. handle these keys finally, to prevent random serialization order
            for key in last_keys:
                if not key in result_dict:
                    continue
                value = result_dict[key]
                # each band of bands is a dictionary of keys, values for band's metadata
                result = "<" + key + ">"
                for band_key, band_attributes in value.items():
                    band_el = self.__xml_key(band_key)
                    result += "<" + band_el + ">"
                    for band_attribute_key, band_attribute_value in band_attributes.items():
                        result += "<{0}>{1}</{0}>".format(self.__xml_key(band_attribute_key), band_attribute_value)
                    result += "</" + band_el + ">"
                result += "</" + key + ">"
                xml_return.append(result)
        else:
            # Serializing local metadata attributes dict
            for key, value in result_dict[self.KEY_LOCAL_METADATA].items():
                xml_return.append("<{0}>{1}</{0}>".format(self.__xml_key(key), value))

            # Build the boundedBy element from axis subsets also need to be added as coverage's local metadata
            # Then later when doing subsets on coverage, it can fetch corresponding local metadata
            # to rasql encoded result.
            bounded_by_template = """<boundedBy>
                                        <Envelope axisLabels="{}" srsDimension="{}">
                                            <lowerCorner>{}</lowerCorner>
                                            <upperCorner>{}</upperCorner>
                                        </Envelope>
                                    </boundedBy>"""

            slice_subsets = result_dict[self.KEY_SLICE_SUBSETS]
            axis_labels, no_of_dimensions, lower_corner, upper_corner = self._create_elements_for_bounded_by(slice_subsets)
            bounded_by = bounded_by_template.format(axis_labels,no_of_dimensions, lower_corner, upper_corner)
            xml_return.append(bounded_by)

        output = "\n".join(xml_return)
        return output


class ExtraMetadataSerializerFactory():
    JSON_ENCODING = "json"
    XML_ENCODING = "xml"

    def __init__(self):
        pass

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

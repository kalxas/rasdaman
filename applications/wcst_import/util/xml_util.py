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
import re

class XMLUtil:
    @staticmethod
    def escape(xml_content):
        xml_content = xml_content.replace("<", "&lt;")
        xml_content = xml_content.replace(">", "&gt;")
        xml_content = xml_content.replace("&", "&amp;")
        xml_content = xml_content.replace("\"", "&quot;")
        return xml_content

    @staticmethod
    def dict_to_xml_str(dictionary):
        """
        Turn a simple dict of key/value pairs into XML
        :param dict dictionary: the dictionary to use
        :rtype str
        """
        result = ""
        for key, val in dictionary.items():
            result += "<{key}>{val}</{key}>\n".format(key=key, val=val)
        return result

    @staticmethod
    def read_file_and_remove_xml_header(xml_file_path):
        """
        Read content of an XML file path to string and remove the xml header <?xml ... ?> if it exists
        :param str xml_file_path: path to an XML file
        :return: str content of an XML file without XML header
        """
        with open(xml_file_path) as fp:
            content = fp.read()
            result = re.sub("<\\?.*\\?>", "", content)

        return result

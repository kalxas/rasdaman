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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""
import hashlib
import re
import struct
import threading
import math

from rasdapy.models.complex import Complex
from rasdapy.models.composite_type import CompositeType
from rasdapy.models.sinterval import SInterval
from rasdapy.models.minterval import MInterval


def get_md5_string(input_str):
    """
    Args:
        input_str:
    Returns: MD5 hash of the input_str
    """
    hashed_str = hashlib.md5()
    hashed_str.update(str(input_str).encode('utf-8'))
    hashed_str = hashed_str.hexdigest()
    return hashed_str


def get_type_structure_from_string(input_str):
    """
    Gives a type structure from a string defining type structure
    :param input_str: the string data to parse (e.g: u'set<char>')
    and return data type (e.g: scalar with band type: char)
    :return: object {"type", "base_type", "sub_type"(optional)}
    """
    primary_regex = "set\s*<marray\s*<(" \
                    "char|ushort|short|ulong|long|float|double|complexd|complex|cint16|cint32|bool|octet),\s*.*>>"
    scalar_regex = "set\s*<(char|ushort|short|ulong|long|float|double|complexd|complex|cint16|cint32|bool|octet)\s*>"
    struct_regex = (
        "set\s*<marray\s*<struct\s*{(("
        "char|ushort|short|ulong|long|float|double|complexd|complex|cint16|cint32|bool|octet)\s*.*,)*\s*(("
        "char|ushort|short|ulong|long|float|double|complexd|complex|cint16|cint32|bool|octet)\s*.*)},\s*.*>>"
    )
    complex_scalar_regex = (
        "set\s*<struct\s*{((char|ushort|short|ulong|long|float|double|octet)\s*.*,"
        ")*\s*((char|ushort|short|ulong|long|float|double|octet)\s*.*)}\s*>"
    )
    # sdom(c)
    minterval_regex = "set<minterval>"
    # sdom(c)[0]
    sinterval_regex = "set<interval>"
    primary_match = re.match(primary_regex, input_str)
    scalar_match = re.match(scalar_regex, input_str)
    complex_scalar_match = re.match(complex_scalar_regex, input_str)
    struct_match = re.match(struct_regex, input_str)
    minterval_match = re.match(minterval_regex, input_str)
    sinterval_match = re.match(sinterval_regex, input_str)
    if primary_match is not None:
        result = {
            'base_type': 'marray',
        }
        primary_type = primary_match.group(1)
        result['type'] = primary_type
    elif scalar_match is not None:
        result = {
            'base_type': 'scalar'
        }
        primary_type = scalar_match.group(1)
        result['type'] = primary_type
    elif complex_scalar_match is not None:
        result = {
            'base_type': 'scalar'
        }
        # Result of m.groups() is a tuple alike
        # ('int foo,', 'int', 'char bar', 'char')
        temp = complex_scalar_match.groups()[2].split(" ")[:-1]
        types = temp[0::2]
        names = temp[1::2]

        for idx, val in enumerate(types):
            if "," in types[idx]:
                types[idx] = types[idx][:-1]
            if "," in names[idx]:
                names[idx] = names[idx][:-1]

        sub_type = {"types": types, "names": names}

        result['type'] = 'struct'
        result['sub_type'] = sub_type
    elif struct_match is not None:
        result = {
            'base_type': 'marray'
        }
        # Result of m.groups() is a tuple alike
        # ('int foo,', 'int', 'char bar', 'char')
        temp = struct_match.groups()[2].split(" ")[:-1]
        types = temp[0::2]
        names = temp[1::2]

        for idx, val in enumerate(types):
            if "," in types[idx]:
                types[idx] = types[idx][:-1]
            if "," in names[idx]:
                names[idx] = names[idx][:-1]

        sub_type = {"types": types, "names": names}

        result['type'] = 'struct'
        result['sub_type'] = sub_type
    elif minterval_match is not None:
        result = {
            'base_type': 'scalar',
            'type': 'minterval'
        }
    elif sinterval_match is not None:
        result = {
            'base_type': 'scalar',
            'type': 'sinterval'
        }
    else:
        raise Exception("Invalid Type Structure: Could not retrieve type structure from: {}.".format(input_str))

    return result


def convert_data_from_bin(dtype, data, big_endian=False):
    """
    Unpack string binary to meaningful data
    https://docs.python.org/2/library/struct.html
    :param dtype: datatype to determine how many bytes needed
    :param data: data to be unpacked
    :param big_endian: default it is little endian
    :return: unpacked data
    """
    flag = ">" if big_endian else "<"
    if dtype == "char":
        # it is hex character and needs to convert to ascii value integer instead
        result = ord(struct.unpack(flag + "c", data)[0])
    elif dtype == "bool":
        result = struct.unpack(flag + "?", data)[0]
    elif dtype == "minterval" or dtype == "sinterval":
        # minterval is output from sdom and parsed each byte, interval is sdom()[0]
        result = struct.unpack(flag + "s", data)[0]
    elif dtype == "octet":
        result = struct.unpack(flag + "B", data)[0]
    elif dtype == "ushort":
        result = struct.unpack(flag + "H", data)[0]
    elif dtype == "short" or dtype == "cint16":
        result = struct.unpack(flag + "h", data)[0]
    elif dtype == "ulong":
        result = struct.unpack(flag + "I", data)[0]
    elif dtype == "long" or dtype == "cint32":
        result = struct.unpack(flag + "i", data)[0]
    elif dtype == "float" or dtype == "complex":
        result = struct.unpack(flag + "f", data)[0]
    elif dtype == "double" or dtype == "complexd":
        result = struct.unpack(flag + "d", data)[0]
    else:
        raise Exception("Unknown Data type provided")
    return result


def get_size_from_data_type(dtype):
    """
    Return the size of a given datatype
    :param dtype: the name of data type
    :return: int size: the size of data type
    """
    if dtype == "char":
        result = 1
    elif dtype == "bool":
        result = 1
    elif dtype == "octet":
        result = 1
    elif dtype == "ushort":
        result = 2
    elif dtype == "short":
        result = 2
    elif dtype == "ulong":
        result = 4
    elif dtype == "long":
        result = 4
    elif dtype == "float":
        result = 4
    elif dtype == "cint16":
        result = 4
    elif dtype == "cint32":
        result = 8
    elif dtype == "complex":
        result = 8
    elif dtype == "double":
        result = 8
    elif dtype == "complexd":
        result = 16
    else:
        raise Exception("Unknown Data type provided: " + dtype)
    return result


def convert_binary_data_stream(dtype, data):
    """
    Convert a set of binary data into meaningful data for next_element
    :param dtype: data type object to determine the type of parsing data
    :param data: string binary needed to be unpack by the length of data type (e.g: short - 2 bytes, int - 4 bytes,...)
    :return: object according to query
    """
    # base is either marray or scalar
    base_type = dtype["base_type"]
    # type is the data type of elements in base
    type = dtype["type"]

    if base_type != "marray" and base_type != "scalar":
        raise Exception("Unknown base_type {} and type {} ".format(dtype["base_type"], dtype["type"]))

    if base_type == "scalar" and type == "struct":
        # e.g: select {1, 2, 3}
        temp_array = []
        cell_counter = 0
        last_byte = 0
        for idx, dt in enumerate(dtype["sub_type"]["types"]):
            dtsize = get_size_from_data_type(dt)
            from_byte = last_byte
            to_byte = last_byte + dtsize
            last_byte = to_byte
            scalar_value = convert_data_from_bin(dt, data[from_byte : to_byte])
            scalar_value = get_scalar_result(scalar_value)
            temp_array.append(scalar_value)
            cell_counter += 1
        composite_type = CompositeType(temp_array)

        return composite_type
    elif base_type == "scalar" and type == "minterval":
        # e.g: select sdom(c) from test_mr as c ([0:250,0:210])
        temp = ""
        # strip the [] of the string to parse
        data = encoded_bytes_to_str(data)
        temp_array = data[1:-1].split(",")

        intervals = []
        for temp in temp_array:
            lo = temp.split(":")[0]
            hi = temp.split(":")[1]
            sinterval = SInterval(lo, hi)
            # e.g: 0:250
            intervals.append(sinterval)

        # e.g: [0:250,0:211]
        minterval = MInterval(intervals)

        return minterval

    elif base_type == "scalar" and type == "sinterval":
        # e.g: select sdom(c)[0] from test_mr as c
        temp = encoded_bytes_to_str(data)
        # e.g: 0:250
        tmp_array = temp.split(":")
        sinterval = SInterval(tmp_array[0], tmp_array[1])

        return sinterval
    elif base_type == "scalar" and type == "complex":
        # e.g: select complexd(0.5, 2.5) from test_mr
        # complexd is 16 bytes: 16 bytes
        data = bytearray(data)
        dtsize = int(get_size_from_data_type(type)/2)
        real_number = convert_data_from_bin(type, data[0: dtsize])
        imagine_number = convert_data_from_bin(type, data[dtsize: dtsize*2])

        real_number = get_scalar_result(real_number)
        imagine_number = get_scalar_result(imagine_number)
        complex_number = Complex(real_number, imagine_number)

        return complex_number
    elif base_type == "scalar" and type == "cint16":
        # e.g: select complexd(0.5, 2.5) from test_mr
        data = bytearray(data)
        dtsize = int(get_size_from_data_type(type) / 2)
        real_number = convert_data_from_bin(type, data[0: dtsize])
        imagine_number = convert_data_from_bin(type, data[dtsize: dtsize * 2])

        real_number = get_scalar_result(real_number)
        imagine_number = get_scalar_result(imagine_number)
        complex_number = Complex(real_number, imagine_number)

        return complex_number
    elif base_type == "scalar" and type == "complexd":
        # e.g: select complexd(0.5, 2.5) from test_mr
        # complexd is 16 bytes: 16 bytes
        data = bytearray(data)
        dtsize = int(get_size_from_data_type(type) / 2)
        real_number = convert_data_from_bin(type, data[0: dtsize])
        imagine_number = convert_data_from_bin(type, data[dtsize: dtsize*2])

        real_number = get_scalar_result(real_number)
        imagine_number = get_scalar_result(imagine_number)
        complex_number = Complex(real_number, imagine_number)

        return complex_number
    elif base_type == "scalar" and type == "cint32":
        # e.g: select complexd(0.5, 2.5) from test_mr
        data = bytearray(data)
        dtsize = int(get_size_from_data_type(type) / 2)
        real_number = convert_data_from_bin(type, data[0: dtsize])
        imagine_number = convert_data_from_bin(type, data[dtsize: dtsize * 2])

        real_number = get_scalar_result(real_number)
        imagine_number = get_scalar_result(imagine_number)
        complex_number = Complex(real_number, imagine_number)

        return complex_number
    else:
        # e.g: query return 1 double value and data will have length = 8 bytes
        scalar_value = convert_data_from_bin(type, data)
        scalar_value = get_scalar_result(scalar_value)

        return scalar_value


def get_scalar_result(scalar_value):
    """
    Rasql client returns number with round (e.g: 33.234544665464 is 33.2345, 33.0 is 33)
    :param (int|float) number: input number which need to process to return as from rasql client
    :return: (int|float) processed number
    """
    # rasql client also rounds e.g: 39.8362884521 to 39.8363
    if isinstance(scalar_value, bool):
        if scalar_value is True:
            scalar_value = "t"
        else:
            scalar_value = "f"
    elif "e" not in str(scalar_value):
        # scalar_value is a float with scientific notation, e.g: 4e-05
        if str(scalar_value).endswith(".0"):
            # e.g: 333.0 should return 333 as in rasql client
            scalar_value = int(scalar_value)

    return scalar_value


def convert_numpy_arr_to_bin(arr):
    return arr.to_bytes()


def get_spatial_domain_from_type_structure(input_str):
    """
    Parse the sdom from input_str
    :param input_str: the string containing sdom (e.g: u'set <marray <long, [0:250,0:210]>>')
    :return: MInterval object containing the domain of array (e.g: [0:250,0:210])
    """
    primary_regex = ".*\[(.*)\].*"
    primary_match = re.match(primary_regex, input_str)
    sinterlval_array = []
    if primary_match is not None:
        matches = primary_match.groups()[0].split(",")
        for match in matches:
            tmp_array = match.split(":")
            sinterval = SInterval(tmp_array[0], tmp_array[1])
            sinterlval_array.append(sinterval)

    minterval = MInterval(sinterlval_array)
    return minterval


def int_to_bytes(input):
    """
    Encode int number to 4 bytes value
    :param int input: input number
    :return: a 4 bytes value with BigEndian
    """
    result = struct.pack(">I", input)
    return result


def byte_to_char_value(input):
    """
    Unpack the byte value to a character
    :param byte input: value to be unpacked
    :return: char result
    """
    result = struct.unpack(">b", input)[0]
    return result

def str_to_encoded_bytes(input):
    """
    Encode str to bytes with "\0" postfix
    :param str input: string to be encoded
    :return: an encoded string
    """
    result = (str(input) + "\0").encode("iso-8859-1")
    return result


def encoded_bytes_to_str(input):
    return input.decode().rstrip('\x00')


def ubytes_to_int(bytes_arr, endianness):
    """
     This method is used for turning up to 4 unsigned bytes into signed integers.
    :param list bytes_arr: one to four Bytes which are interpreted as an unsigned Integer
    :param int endianness: determines the order of the bytes: 0 = bigendian, 1 = little endian
    :return: integer
    """
    retval = 0
    length = len(bytes_arr)
    for i in range(0, length):
        if endianness == 0:
            # BigEdianness
            tmpb = bytes_arr[length - i - 1]
        else:
            # SmallEdianness
            tmpb = bytes_arr[i]

        if tmpb < 0:
            tmpi = 256 + tmpb
        else:
            tmpi = tmpb

        tmpi <<= (i * 8)
        retval += tmpi

    return retval


def get_tiling_domain(dim, mddtype_length, tile_size):
    """
    calculates the tiling domain based on the original MDD, the type length and the tileSize
    of the MDD's storageLayout.
    :param int dim: number of marray's dimensions
    :param int mddtype_length: the length of cell base type in bytes (e.g: char: 1 byte)
    :param int tile_size: the size of tile to be used by marray (e.g: default 128 KB)
    :return: str tile_domain: the tile domain to be used by marray (e.g: [0:10, 0:10])
    """
    tmp = float(1.0 / dim)
    size = str(int(math.pow(float(tile_size / mddtype_length), tmp)) - 1)
    tile_domain = "0:" + size
    for i in range(1, dim):
        tile_domain = tile_domain + ",0:" + size
    tile_domain = "[" + tile_domain + "]"

    return tile_domain


class StoppableTimeoutThread(threading.Thread):
    """
    Thread that runs a method over and over again
    """

    def __init__(self, target, timeout, *args):
        super(StoppableTimeoutThread, self).__init__()
        self._target = target
        self._args = args
        self._timeout = timeout
        self._stop = threading.Event()
        self.close_thread = False

    def run(self):
        while self.close_thread is not True:
            # send a keep alive message to rasmgr/rasserver
            self._target(*self._args)
            self._stop.wait(self._timeout)

    def stop(self):
        # wake up thread to quit
        self.close_thread = True
        self._stop.set()

    def stopped(self):
        return self._stop.isSet()


def slice_tuple(slice_):
    """
    Return start and stop values from a slice object
    """
    return slice_.start, slice_.stop


def represent_subsetting(collection, tuple_arr):
    repr = collection + "["
    for value in tuple_arr:
        repr = repr + str(value[0]) + ":" + str(value[1]) + ","
    repr = repr[:-1]
    repr += "]"
    return repr

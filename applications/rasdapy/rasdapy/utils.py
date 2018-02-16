import hashlib
import re
import struct
import threading

from models.CompositeType import CompositeType
from models.SInterval import SInterval
from models.MInterval import MInterval

def get_md5_string(input_str):
    """
    Args:
        input_str:
    Returns: MD5 hash of the input_str
    """
    hashed_str = hashlib.md5()
    hashed_str.update(input_str)
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
                    "char|ushort|short|ulong|long|float|double|complexd|complex|bool),\s*.*>>"
    scalar_regex = "set\s*<(char|ushort|short|ulong|long|float|double|complexd|complex|bool)\s*>"
    struct_regex = (
        "set\s*<marray\s*<struct\s*{(("
        "char|ushort|short|ulong|long|float|double|complexd|complex|bool)\s*.*,)*\s*(("
        "char|ushort|short|ulong|long|float|double|complexd|complex|bool)\s*.*)},\s*.*>>"
    )
    complex_scalar_regex = (
        "set\s*<struct\s*{((char|ushort|short|ulong|long|float|double)\s*.*,"
        ")*\s*((char|ushort|short|ulong|long|float|double)\s*.*)}\s*>"
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
        print(input_str)
        raise Exception(
                "Invalid Type Structure: Could not retrieve type structure "
                "from String")

    return result


def convert_data_from_bin(dtype, data):
    """
    Unpack string binary to meaningful data
    https://docs.python.org/2/library/struct.html
    :param dtype: datatype to determine how many bytes needed
    :param data: data to be unpacked
    :return: unpacked data
    """
    if dtype == "char":
        result = struct.unpack("c", data)
    elif dtype == "bool":
        result = struct.unpack("?", data)
    elif dtype == "minterval" or dtype == "sinterval":
        # minterval is output from sdom and parsed each byte, interval is sdom()[0]
        result = struct.unpack("s", data)
    elif dtype == "ushort":
        result = struct.unpack("H", data)
    elif dtype == "short":
        result = struct.unpack("h", data)
    elif dtype == "ulong":
        result = struct.unpack("I", data)
    elif dtype == "long":
        result = struct.unpack("i", data)
    elif dtype == "float" or dtype == "complex":
        result = struct.unpack("f", data)
    elif dtype == "double" or dtype == "complexd":
        result = struct.unpack("d", data)
    else:
        raise Exception("Unknown Data type provided")
    return result[0]


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
    elif dtype == "complex":
        result = 8
    elif dtype == "double":
        result = 8
    elif dtype == "complexd":
        result = 8
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
        for idx, dt in enumerate(dtype["sub_type"]["types"]):
            dtsize = get_size_from_data_type(dt)
            temp_array.append(convert_data_from_bin(dt, data[idx * dtsize : (idx + 1) * dtsize]))
            cell_counter += 1
        composite_type = CompositeType(temp_array)

        return composite_type
    elif base_type == "scalar" and type == "minterval":
        # e.g: select sdom(c) from test_mr as c ([0:250,0:210])
        temp = ""
        # strip the [] of the string to parse
        length = len(data) - 2
        for i in range(1, length):
            s = str(convert_data_from_bin(type, data[i : i + 1]))
            temp = temp + s
        temp_array = temp.split(",")

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
        temp = ""
        length = len(data) - 1
        for i in range(0, length):
            s = str(convert_data_from_bin(type, data[i: i + 1]))
            temp = temp + s
        # e.g: 0:250
        tmp_array = temp.split(":")
        sinterval = SInterval(tmp_array[0], tmp_array[1])

        return sinterval
    elif base_type == "scalar" and type == "complexd":
        # e.g: select complex(0.5, 2.5) from test_mr
        # complexd is 8 bytes: 8 bytes
        dtsize = get_size_from_data_type(type)
        real_number = convert_data_from_bin(type, data[0: dtsize])
        imagine_number = convert_data_from_bin(type, data[dtsize: dtsize * 2])
        complex_number = complex(real_number, imagine_number)

        return complex_number
    else:
        # e.g: query return 1 double value and data will have length = 8 bytes
        scalar_value = convert_data_from_bin(type, data)

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


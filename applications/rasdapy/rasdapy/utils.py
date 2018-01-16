import hashlib
import re
import struct
import threading
import time


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
    :param input_str:
    :return: object {"type", "base_type", "sub_type"(optional)}
    TODO: SDOM might not be a scalar. Look into the result type
    """
    primary_regex = "set\s*<marray\s*<(" \
                    "char|ushort|short|ulong|long|float|double),\s*.*>>"
    scalar_regex = "set\s*<(char|ushort|short|ulong|long|float|double)\s*>"
    struct_regex = (
        "set\s*<marray\s*<struct\s*{(("
        "char|ushort|short|ulong|long|float|double)\s*.*,)*\s*(("
        "char|ushort|short|ulong|long|float|double)\s*.*)},\s*.*>>"
    )
    complex_scalar_regex = (
        "set\s*<struct\s*{((char|ushort|short|ulong|long|float|double)\s*.*,"
        ")*\s*((char|ushort|short|ulong|long|float|double)\s*.*)}\s*>"
    )
    minterval_regex = "set<minterval>"
    primary_match = re.match(primary_regex, input_str)
    scalar_match = re.match(scalar_regex, input_str)
    complex_scalar_match = re.match(complex_scalar_regex, input_str)
    struct_match = re.match(struct_regex, input_str)
    minterval_match = re.match(minterval_regex, input_str)
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
    else:
        print(input_str)
        raise Exception(
                "Invalid Type Structure: Could not retrieve type structure "
                "from String")

    return result


def convert_data_from_bin(dtype, data):
    """

    :param dtype: datatype
    :param data: data
    :return: unpacked data
    """
    if dtype == "char":
        result = struct.unpack("B", data)
    elif dtype == "ushort":
        result = struct.unpack("H", data)
    elif dtype == "short":
        result = struct.unpack("h", data)
    elif dtype == "ulong":
        result = struct.unpack("L", data)
    elif dtype == "long":
        result = struct.unpack("l", data)
    elif dtype == "float":
        result = struct.unpack("f", data)
    elif dtype == "double":
        result = struct.unpack("d", data)
    else:
        raise Exception("Unknown Data type provided")
    return result[0]


def get_size_from_data_type(dtype):
    """
    Return the size of a given datatype
    :param dtype:
    :return: int size
    """
    if dtype == "char":
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
    elif dtype == "double":
        result = 8
    elif dtype == "minterval":
        result = 0
    else:
        raise Exception("Unknown Data type provided: " + dtype)
    return result


def convert_data_stream_from_bin(dtype, data, array_len, cell_len,
                                 spatial_domain):
    """
    Convert a set of binary data into meaningful data
    :param dtype:
    :param data:
    :param array_len:
    :param cell_len:
    :return:
    """
    arr = []
    result = []
    if dtype["base_type"] == "marray" or dtype["base_type"] == "scalar":
        sdom = spatial_domain
        if dtype["base_type"] == "marray" and dtype["type"] == "struct":
            tile_v = int(sdom.interval_params[1][1])
            tile_v_index = 0
            for i in xrange(0, array_len, cell_len):
                cell_counter = 0
                temp = []
                for idx, dt in enumerate(dtype["sub_type"]["types"]):
                    dtsize = get_size_from_data_type(dt)
                    temp.append(convert_data_from_bin(dt, data[
                                                          i + cell_counter:i
                                                                           +
                                                                           cell_counter + dtsize]))
                    cell_counter += dtsize
                arr.append(temp)
                tile_v_index += 1
                if tile_v_index == tile_v + 1:
                    tile_v_index = 0
                    result.append(arr)
                    arr = []
        elif dtype["base_type"] == "marray" and dtype["type"] != "struct":
            tile_h = int(sdom.interval_params[0][1])
            tile_h_index = 0
            for i in xrange(0, array_len, cell_len):
                dtsize = get_size_from_data_type(dtype["type"])
                temp = convert_data_from_bin(dtype["type"], data[i: i + dtsize])
                arr.append(temp)
                tile_h_index += 1
                if tile_h_index == tile_h:
                    tile_h_index = 0
                    result.append(arr)
                    arr = []
        elif dtype["base_type"] == "scalar" and dtype["type"] == "struct":
            temp = []
            cell_counter = 0
            for idx, dt in enumerate(dtype["sub_type"]["types"]):
                dtsize = get_size_from_data_type(dt)
                temp.append(convert_data_from_bin(dt, data[
                                                      idx + cell_counter: idx
                                                                          +
                                                                          cell_counter + dtsize]))
                cell_counter += 1
            return temp
        else:
            dtsize = get_size_from_data_type(dtype["type"])
            temp = convert_data_from_bin(dtype["type"], data)
            return temp
    else:
        raise Exception("Unknown base_type: " + dtype["base_type"])
    return result


def convert_numpy_arr_to_bin(arr):
    return arr.to_bytes()


def get_spatial_domain_from_type_structure(input_str):
    primary_regex = ".*\[(.*)\].*"
    primary_match = re.match(primary_regex, input_str)
    secondary_regex = "(.*)\:(.*)"
    result = []
    if primary_match is not None:
        matches = primary_match.groups()[0].split(",")
        for match in matches:
            sec_match = re.match(secondary_regex, match)
            result.append(sec_match.groups())
    return result


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

    def run(self):
        while True:
            self._target(*self._args)
            time.sleep(self._timeout)

    def stop(self):
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

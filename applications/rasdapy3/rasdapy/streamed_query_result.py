
from rasdapy.cores.utils import *

from rasdapy.cores.exception_factories import ExceptionFactories
from rasdapy.stubs.client_rassrvr_service_pb2 import StreamedHttpQueryRepl


class StreamedQueryResult:

    def __init__(self, repl: StreamedHttpQueryRepl):
        self.barray = bytearray(repl.data)

    @property
    def endianness(self):
        return self.barray[1]

    def error(self):
        return self.barray[0] == 0

    def _get_text(self, i_from :int):
        token = ""
        i = i_from
        while self.barray[i] != 0:
            token += chr(self.barray[i])
            i += 1

        return token

    def error_message(self):
        err_no = ubytes_to_int(self.barray[2:6], self.endianness)
        line_no = ubytes_to_int(self.barray[6:10], self.endianness)
        col_no = ubytes_to_int(self.barray[10:14], self.endianness)
        token = self._get_text(14)

        return ExceptionFactories.create_error_message(err_no, line_no, col_no, token)

    def get_elements(self):

        """
            Result is encoded as follows: header is same in all cases, element encoding
            varies. All strings are '\0' terminated.

             Header:

             +----------+------------------------+
             | bytes    | what                   |
             +----------+------------------------+
             | 1        | result type            |
             | 1        | endianess              |
             | 4 (int)  | number of elements     |
             | string   | collection type        |
             +----------+------------------------+
            :return: list of n elements
        """

        # retrieve type information using string
        txt = self._get_text(2)
        struct_type = get_type_structure_from_string(txt)

        # offset after type information
        i = len(txt) + 3

        num_elt = ubytes_to_int(self.barray[i:i + 4], self.endianness)
        i += 4

        elts = []
        for i_elt in range(0, num_elt):
            # should be element type -> not used so far
            elt_type = byte_to_char_value(self.barray[i:i + 1])
            i += 1

            elt_size = ubytes_to_int(self.barray[i:i + 4], self.endianness)
            i += 4

            elt = convert_data_from_bin(
                struct_type['type'],
                self.barray[i:i + elt_size],
                False if self.endianness == 0 else True )

            elts.append(elt)

        return elts
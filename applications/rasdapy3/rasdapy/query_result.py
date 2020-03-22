
from rasdapy.cores.utils import *

from rasdapy.cores.exception_factories import ExceptionFactories
from rasdapy.stubs.client_rassrvr_service_pb2 import StreamedHttpQueryRepl


class QueryResult:
    def __init__(self):
        self.with_error = False
        self.err_no = 0
        self.line_no = 0
        self.col_no = 0
        self.token = 0
        self.endianness = 0
        self.elements = []

    def error(self):
        return self.with_error

    def error_message(self):
        return ExceptionFactories.create_error_message(self.err_no,
                                                       self.line_no,
                                                       self.col_no,
                                                       self.token)

    def get_elements(self):
        return self.elements

    @property
    def size(self):
        return len(self.elements)

    def from_streamed_response(self, repl: StreamedHttpQueryRepl):
        barray = bytearray(repl.data)
        self.with_error = True if barray[0] == 0 else False
        self.endianness = barray[1]

        if self.with_error:
            self.err_no = ubytes_to_int(barray[2:6], self.endianness)
            self.line_no = ubytes_to_int(barray[6:10], self.endianness)
            self.col_no = ubytes_to_int(barray[10:14], self.endianness)
            self.token = self._get_text(barray, 14)
        else:
            self.elements = self._get_elements(barray, self.endianness)

    @staticmethod
    def _get_text(barray, i_from: int):
        token = ""
        i = i_from
        while barray[i] != 0:
            token += chr(barray[i])
            i += 1

        return token

    @staticmethod
    def _get_elements(barray, endianness):

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
        txt = QueryResult._get_text(barray, 2)
        struct_type = get_type_structure_from_string(txt)

        # offset after type information
        i = len(txt) + 3

        num_elt = ubytes_to_int(barray[i:i + 4], endianness)
        i += 4

        elts = []
        for i_elt in range(0, num_elt):
            # should be element type -> not used so far
            elt_type = byte_to_char_value(barray[i:i + 1])
            i += 1

            elt_size = ubytes_to_int(barray[i:i + 4], endianness)
            i += 4

            elt = convert_data_from_bin(
                struct_type['type'],
                barray[i:i + elt_size],
                False if endianness == 0 else True)

            elts.append(elt)

        return elts



from rasdapy.models.ras_gmarray import RasGMArray
import numpy as np
from rasdapy.models.sinterval import SInterval
from rasdapy.models.minterval import MInterval
from rasdapy.cores.utils import get_tiling_domain
from rasdapy.models.ras_storage_layout import RasStorageLayOut


class NumpyGMArray(RasGMArray):

    type_dict = {
        "bool" : {
            1: "BoolString",
            2: "BoolImage",
            3: "BoolCube"
        },
        "uint8": {
            1: "GreyString",
            2: "GreyImage",
            3: "GreyCube"
        },
        "int8": {
            1: "OctetString",
            2: "OctetImage",
            3: "OctetCube"
        },
        "int16": {
            1: "ShortString",
            2: "ShortImage",
            3: "ShortCube"
        },
        "uint16" : {
            1: "UShortSring",
            2: "UShortImage",
            3: "UShortCube"
        },
        "int32": {
            1: "LongString",
            2: "LongImage",
            3: "LongCube"
        },
        "uint32": {
            1: "ULongString",
            2: "ULongImage",
            3: "ULongCube"
        },

        "float32": {
            1: "FloatString",
            2: "FloatImage",
            3: "FloatCube",
            4: "FloatCube4"
        },
        "double": {
            1: "DoubleString",
            2: "DoubleImage",
            3: "DoubleCube"
        },
        "cint16": {
            1: "CInt16Image"
        },
        "cint32": {
            1: "CInt32Image"
        },
        "cfloat32": {
            1: "Gauss1",
            2: "Gauss1Image"
        },
        "cfloat64": {
            1: "Gauss2",
            2: "Gauss2Image"
        }
    }

    def __init__(self, array: np.array, tile_size=RasStorageLayOut.DEFAULT_TILE_SIZE):
        shape = array.shape
        intervals = []
        for i_max in shape:
            intervals.append(SInterval(0, i_max-1))

        self.spatial_domain = MInterval(intervals)

        dtype = array.dtype
        if dtype.name not in self.type_dict.keys():
            raise Exception("this type is not a standard rasdaman type")
        self.type_name = self.type_dict[dtype.name][len(shape)]
        self.type_length = dtype.itemsize

        self.data = bytes(array)

        tile_domain = get_tiling_domain(self.spatial_domain.dim, self.type_length, tile_size)
        self.storage_layout = RasStorageLayOut(tile_domain, tile_size)


if __name__ == '__main__':
    a = np.arange(12).reshape((4, 3))
    numpy_array = NumpyGMArray(a)



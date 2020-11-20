
import os
from rasdapy.models.ras_storage_layout import RasStorageLayOut
from rasdapy.models.ras_gmarray import RasGMArray, MInterval
from rasdapy.models.sinterval import SInterval


class FileStorageLayout(RasStorageLayOut):

    def __init__(self,  files, reader):
        RasStorageLayOut.__init__(self)
        self.files = files
        self.reader = reader

    def decompose_mdd(self, gm_array):
        return FilesMDDIterator(gm_array, self)


class FilesMDDIterator:

    def __init__(self, gm_array, layout):
        self.layout = layout
        self.gm_array = gm_array
        self.storage_intervals =layout.spatial_domain.intervals

    def _fill_rasarray_list(self, ras_list, barray, itemsize, z):
        full_size = len(barray)
        print("fullsize", full_size)
        n_iter = full_size // RasStorageLayOut.DEFAULT_TILE_SIZE + 1
        y_width = self.storage_intervals[1].extent // n_iter
        step_size = y_width * self.storage_intervals[2].extent * itemsize

        offset = 0
        lo = self.storage_intervals[1].lo
        hi = y_width - 1
        while offset + step_size <= full_size:
            y_interval = SInterval(lo, hi)
            z_interval = SInterval(z, z)
            ras_array = self._create_ras_array(barray, offset, step_size, y_interval, z_interval)
            ras_list.append(ras_array)
            lo += y_width
            hi += y_width
            offset += step_size
            print(y_interval, offset)

        remaining_size = full_size - offset
        print("offset", offset, "remaining", remaining_size)
        if remaining_size > 0:
            y_interval = SInterval(lo, self.storage_intervals[1].hi)
            z_interval = SInterval(z, z)
            ras_array = self._create_ras_array(barray, offset, remaining_size, y_interval, z_interval)
            ras_list.append(ras_array)

    def _create_ras_array(self, barray, offset, step_size, y_interval, z_interval):
        ras_array = RasGMArray()
        ras_array.spatial_domain = MInterval([z_interval, y_interval, self.storage_intervals[2]])
        ras_array.storage_layout = RasStorageLayOut(self.gm_array.type_length, ras_array.spatial_domain)
        ras_array.type_name = self.gm_array.type_name
        ras_array.type_length = self.gm_array.type_length
        ras_array.data = barray[offset:offset + step_size - 1]
        return ras_array

    def __iter__(self):
        for z, file in enumerate(self.layout.files):
            array = self.layout.reader(file)
            byte_size = array.size*self.gm_array.type_length
            barray = bytes(array)

            ras_list = []
            if byte_size > RasStorageLayOut.DEFAULT_TILE_SIZE:
                self._fill_rasarray_list(ras_list, barray, array.itemsize, z)
            else:
                z_interval = SInterval(z, z)
                y_interval = self.storage_intervals[1]
                ras_array = self._create_ras_array(barray, 0, byte_size, y_interval, z_interval)
                ras_list.append(ras_array)

            for ras_array in ras_list:
                print(ras_array.spatial_domain)
                yield ras_array





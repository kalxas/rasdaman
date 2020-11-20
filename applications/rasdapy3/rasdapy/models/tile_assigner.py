import threading
import time
from rasdapy.models.minterval import MInterval


class TileAssigner(threading.Thread):

    def __init__(self, gm_global):
        threading.Thread.__init__(self)
        self.gm_global = gm_global
        self.tiles = []
        self.lock = threading.RLock()
        self.go_on = True

    def add_tile(self, tile):
        with self.lock:
            self.tiles.append(tile)

    def run(self):
        while self.go_on or len(self.tiles) > 0:
            if len(self.tiles) > 0:
                with self.lock:
                    mdd_tile = self.tiles.pop(0)
                self._write_in_mdd(mdd_tile)
            time.sleep(0.1)

    def _write_in_mdd(self, mdd_tile):
        mdd_result = self.gm_global
        tile_domain = mdd_tile.spatial_domain
        bloc_cells = tile_domain.intervals[tile_domain.dimension - 1].extent
        bloc_size = bloc_cells * mdd_tile.type_length

        mv = memoryview(mdd_tile.data)

        tile_offset = 0
        #
        # for bloc_ctr in range(bloc_no):
        #    t = tile_domain.cell_point(bloc_ctr * bloc_cells)

        intervals = tile_domain.intervals.copy()
        intervals[tile_domain.dimension - 1].hi = intervals[tile_domain.dimension - 1].lo
        m_iter = MInterval(intervals)
        for t in m_iter.cartesian_product():
            offset = mdd_result.spatial_domain.cell_offset(t) * mdd_result.type_length
            mdd_result.data[offset:offset + bloc_size] = mv[tile_offset:tile_offset + bloc_size]
            tile_offset += bloc_size


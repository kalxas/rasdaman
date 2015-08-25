from master.provider.metadata.axis import Axis
from master.provider.metadata.grid_axis import GridAxis


class CoverageAxis:
    def __init__(self, geo_axis, grid_axis, data_bound=False):
        """
        A representation of a subset on one axis
        :param Axis geo_axis: the name of the axis
        :param GridAxis grid_axis: the grid axis
        :param bool data_bound: boolean flag to indicate if this axis is bound to the data provider. An axis is bound to
        the data provider if the axis is defined on the original container of the data. E.g. in a 3-D timeseries formed
        from 2D slices, the easting and northing axes are data bound while the time axis is not.
        """
        self.axis = geo_axis
        self.grid_axis = grid_axis
        self.data_bound = data_bound
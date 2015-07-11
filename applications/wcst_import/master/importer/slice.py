from master.importer.axis_subset import AxisMetadata


class Slice:
    def __init__(self, axis_subsets, data_provider):
        """
        Class to represent one slice of the coverage
        :param list[AxisMetadata] axis_subsets: the position of this slice in the coverage represented through a list
        of axis subsets
        :param DataProvider data_provider: a data provider that can get the data corresponding to this slice
        """
        self.axis_subsets = axis_subsets
        self.data_provider = data_provider
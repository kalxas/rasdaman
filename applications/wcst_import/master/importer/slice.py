from master.importer.axis_subset import AxisSubset
from master.provider.data.data_provider import DataProvider


class Slice:
    def __init__(self, axis_subsets, data_provider):
        """
        Class to represent one slice of the coverage
        :param list[AxisSubset] axis_subsets: the position of this slice in the coverage represented through a list
        of axis subsets
        :param DataProvider data_provider: a data provider that can get the data corresponding to this slice
        """
        self.axis_subsets = axis_subsets
        self.data_provider = data_provider

    def __str__(self):
        ret = "{Axis Subset: "
        for subset in self.axis_subsets:
            ret += str(subset) + " "
        ret += "\nData Provider: " + str(self.data_provider) + "}\n"
        return ret
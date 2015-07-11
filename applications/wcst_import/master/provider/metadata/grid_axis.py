import functools


@functools.total_ordering
class GridAxis:
    def __init__(self, order, label, resolution, grid_low, grid_high):
        """
        Class to represent a grid axis in gml
        :param int order: the axis order in the original dataset
        :param str label: the label of this axis, usually taken from the domain axis
        :param float resolution: the resolution on this grid axis
        :param int grid_low: the low extent of the grid
        :param int grid_high: the high extent of the grid
        """
        self.order = order
        self.label = label
        self.resolution = resolution
        self.grid_low = grid_low
        self.grid_high = grid_high

    def __eq__(self, other):
        """
        Compares two tuples ==
        :param GridAxis other: the tuple to compare with
        :rtype bool
        """
        return self.order == other.order

    def __lt__(self, other):
        """
        Compares two tuples <
        :param GridAxis other: the tuple to compare with
        :rtype bool
        """
        return self.order < other.order
from master.generator.model.domain_set import DomainSet


class DomainSetRegular(DomainSet):
    def __init__(self, numberOfDimensions, gridEnvelopeLow, gridEnvelopeHigh,
                 axisLabelsGrid, axisLabels, crs, uomLabels, origin, offsetVectors):
        """
        Class representing the model behind the regular domain set of a gml
        :param int numberOfDimensions: the number of dimensions
        :param list[float] gridEnvelopeLow: the lower coordinates
        :param list[float] gridEnvelopeHigh: the higher coordinates
        :param list[str] axisLabelsGrid: the labels of the grid axes
        :param list[str] axisLabels: the labels of the axes
        :param str crs: the crs of the coverage
        :param list[str] uomLabels: the labels of the uom
        :param list[float] origin: the origin of the coverage
        :param list[OffsetVectorRegular] offsetVectors: the offset vectors model
        :return:
        """
        super(DomainSetRegular, self).__init__(self.__GRID_TYPE, numberOfDimensions, gridEnvelopeLow, gridEnvelopeHigh,
                                               axisLabelsGrid, axisLabels, crs, uomLabels, origin, offsetVectors)

    __GRID_TYPE = "RectifiedGrid"
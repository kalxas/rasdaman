from master.generator.model.domain_set import DomainSet
from master.generator.model.offset_vector_irregular import OffsetVectorIrregular


class DomainSetIrregular(DomainSet):
    def __init__(self, numberOfDimensions, gridEnvelopeLow, gridEnvelopeHigh,
                 axisLabelsGrid, axisLabels, crs, uomLabels, origin, offsetVectors):
        """
        Class representing the model behind the irregular domain set of a gml
        :param int numberOfDimensions: the number of dimensions
        :param list[float] gridEnvelopeLow: the lower coordinates
        :param list[float] gridEnvelopeHigh: the higher coordinates
        :param list[str] axisLabelsGrid: the labels of the grid axes
        :param list[str] axisLabels: the labels of the axes
        :param str crs: the crs of the coverage
        :param list[str] uomLabels: the labels of the uom
        :param list[float] origin: the origin of the coverage
        :param list[OffsetVectorIrregular] offsetVectors: the offset vectors model
        :return:
        """
        super(DomainSetIrregular, self).__init__(self.__GRID_TYPE, numberOfDimensions, gridEnvelopeLow,
                                                 gridEnvelopeHigh,
                                                 axisLabelsGrid, axisLabels, crs, uomLabels, origin, offsetVectors)

    __GRID_TYPE = "ReferenceableGridByVectors"
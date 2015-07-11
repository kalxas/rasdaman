from master.generator.model.model import Model


class DomainSet(Model):
    def __init__(self, gridType, numberOfDimensions, gridEnvelopeLow, gridEnvelopeHigh,
                 axisLabelsGrid, axisLabels, crs, uomLabels, origin, offsetVectors):
        """
        Class representing the model behind the domain set of a gml. This is abstract as
        further differentiation needs to be done between irregular domain sets and
        regular ones
        :param str gridType: the type of the grid
        :param int numberOfDimensions: the number of dimensions
        :param list[float] gridEnvelopeLow: the lower coordinates
        :param list[float] gridEnvelopeHigh: the higher coordinates
        :param list[str] axisLabelsGrid: the labels of the grid axes
        :param list[str] axisLabels: the labels of the axes
        :param str crs: the crs of the coverage
        :param list[str] uomLabels: the labels of the uom
        :param list[float] origin: the origin of the coverage
        :param offsetVectors: the offset vectors model
        :return:
        """
        self.gridType = gridType
        self.numberOfDimensions = numberOfDimensions
        self.gridEnvelopeLow = gridEnvelopeLow
        self.gridEnvelopeHigh = gridEnvelopeHigh
        self.axisLabelsGrid = axisLabelsGrid
        self.axisLabels = axisLabels
        self.crs = crs
        self.uomLabels = uomLabels
        self.origin = origin
        self.offsetVectors = offsetVectors

    def get_template_name(self):
        return "gml_domain_set.xml"
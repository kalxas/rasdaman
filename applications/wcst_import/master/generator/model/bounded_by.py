from master.generator.model.model import Model


class BoundedBy(Model):

    def __init__(self, crs, axisLabels, uomLabels, noOfDimensions, lowerCorner, upperCorner):
        """
        Class corresponding to the bounded by section of the coverage model
        :param str crs: the crs of the coverage
        :param list[str] axisLabels: the labels of the axes
        :param list[str] uomLabels: the labels of the units of measure
        :param int noOfDimensions: the no of dimensions
        :param list[float] lowerCorner: a list containing the lower coordinates
        :param list[float] upperCorner: a list containing the upper coordinates
        :return:
        """
        self.crs = crs
        self.axisLabels = axisLabels
        self.uomLabels = uomLabels
        self.noOfDimensions = noOfDimensions
        self.lowerCorner = lowerCorner
        self.upperCorner = upperCorner

    def get_template_name(self):
        return "gml_bounded_by.xml"
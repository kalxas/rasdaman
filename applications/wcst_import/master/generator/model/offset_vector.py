from abc import abstractmethod

from master.generator.model.model import Model


class OffsetVector(Model):

    def __init__(self, crs, axisLabels, uomLabels, noOfDimensions, offset):
        """
        Representation of the offset vector
        :param str crs: the crs of the offset vector
        :param list[str] axisLabels: the labels of the axes
        :param list[str] uomLabels: the labels of the uoms
        :param int noOfDimensions: the number of dimensions
        :param list[float] offset: the offset value
        """
        self.crs = crs
        self.axisLabels = axisLabels
        self.uomLabels = uomLabels
        self.noOfDimensions = noOfDimensions
        self.offset = offset
        pass

    @abstractmethod
    def get_template_name(self):
        pass
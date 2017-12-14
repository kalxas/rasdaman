from abc import abstractmethod

from master.generator.model.model import Model


class OffsetVector(Model):

    def __init__(self, crs, axisLabels, uomLabels, noOfDimensions, offset, axisSpanned):
        """
        Representation of the offset vector
        :param str crs: the crs of the offset vector
        :param list[str] axisLabels: the labels of the axes
        :param list[str] uomLabels: the labels of the uoms
        :param int noOfDimensions: the number of dimensions
        :param list[float] offset: the offset value
        :param str axisSpanned: the label of the axis to which this vector corresponds to
        """
        self.crs = crs
        self.axisLabels = axisLabels
        self.uomLabels = uomLabels
        self.noOfDimensions = noOfDimensions
        self.offset = offset
        self.axisSpanned = axisSpanned
        pass

    @abstractmethod
    def get_template_name(self):
        pass
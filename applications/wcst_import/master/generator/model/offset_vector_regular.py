from master.generator.model.offset_vector import OffsetVector


class OffsetVectorRegular(OffsetVector):
    def __init__(self, crs, axisLabels, uomLabels, noOfDimensions, offset):
        """
        Representation of the offset vector on a regular axis
        :param str crs: the crs of the offset vector
        :param list[str] axisLabels: the labels of the axes
        :param list[str] uomLabels: the labels of the uoms
        :param int noOfDimensions: the number of dimensions
        :param list[float] offset: the offset value
        """
        super(OffsetVectorRegular, self).__init__(crs, axisLabels, uomLabels, noOfDimensions, offset)

    def get_template_name(self):
        return "gml_offset_vector_regular.xml"
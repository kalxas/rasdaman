from master.generator.model.model import Model
from master.generator.model.range_type_field import RangeTypeField


class RangeType(Model):
    def __init__(self, rangeFields):
        """
        Class to represent the range type of the coverage
        :param list[RangeTypeField] rangeFields: the range fields
        """
        self.rangeFields = rangeFields

    def get_template_name(self):
        return "gml_range_type.xml"
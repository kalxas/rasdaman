from master.generator.model.model import Model
from master.generator.model.range_type_nill_value import RangeTypeNilValue


class RangeTypeField(Model):
    def __init__(self, name, definition="", description="", nilValues=None, uom=None):
        """
        Class to represent the range type field element of range type
        :param str name: the name of the field
        :param str definition: the definition of the field
        :param str description: the description of the field
        :param list[RangeTypeNilValue] nilValues: the nil values for this field
        :param str uom: the unit of measure for the field
        """
        self.name = name
        self.definition = definition
        self.description = description
        self.nilValues = nilValues
        self.uom = uom

    def get_template_name(self):
        return "gml_range_type_field.xml"
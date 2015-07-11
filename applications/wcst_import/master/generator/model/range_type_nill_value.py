from master.generator.model.model import Model


class RangeTypeNilValue(Model):
    def __init__(self, reason, value):
        """
        Class to represent the range type nil value
        :param str reason: the reason for the nil value
        :param int value: the value of the nil value
        """
        self.reason = reason
        self.value = value

    def get_template_name(self):
        return "gml_range_type_nil_values.xml"

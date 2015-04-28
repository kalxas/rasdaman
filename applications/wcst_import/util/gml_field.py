class GMLField:
    def __init__(self, field_name, uom_code="", nill_values=None):
        """
        A representation of a gml field
        :param str field_name: the name of the field
        :param str uom_code: the unit of measure
        :param str nill_values: any nill values
        :return:
        """
        self.nill_values = nill_values
        self.field_name = field_name
        self.uom_code = uom_code
        pass

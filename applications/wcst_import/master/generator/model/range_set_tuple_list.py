from master.generator.model.range_set import RangeSet


class RangeSetTupleList(RangeSet):
    def __init__(self, tupleList):
        """
        Class to represent the tuple list of a range set
        :param list[float] tupleList: the list of tuples for the range set
        """
        self.tupleList = tupleList

    def get_template_name(self):
        return "gml_range_set_tuple_list.xml"
from master.provider.metadata.axis import Axis


class IrregularAxis(Axis):
    def __init__(self, label, uomLabel, low, high, origin, coefficient, crs_axis):
        Axis.__init__(self, label, uomLabel, low, high, origin, crs_axis)
        self.coefficient = coefficient
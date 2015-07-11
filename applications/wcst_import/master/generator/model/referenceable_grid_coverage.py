from master.generator.model.gml_coverage import GMLCoverage


class ReferenceableGridCoverage(GMLCoverage):
    def __init__(self, id, boundedBy, domainSet, rangeSet, rangeType):
        """
        Model class for a rectified grid coverage
        :param str id: the coverage id
        :param BoundedBy boundedBy: the boundedby element
        :param DomainSetIrregular domainSet: the domain set
        :param RangeSet rangeSet: the range set
        :param RangeType rangeType: the range type
        """
        super(ReferenceableGridCoverage, self).__init__(self.__COVERAGE_TYPE, id, boundedBy, domainSet, rangeSet,
                                                        rangeType)

    __COVERAGE_TYPE = "ReferenceableGridCoverage"

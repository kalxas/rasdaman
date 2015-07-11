from master.generator.model.gml_coverage import GMLCoverage


class RectifiedGridCoverage(GMLCoverage):
    def __init__(self, id, boundedBy, domainSet, rangeSet, rangeType):
        """
        Model class for a rectified grid coverage
        :param str id: the coverage id
        :param BoundedBy boundedBy: the boundedby element
        :param DomainSetRegular domainSet: the domain set
        :param RangeSet rangeSet: the range set
        :param RangeType rangeType: the range type
        """
        super(RectifiedGridCoverage, self).__init__(self.__COVERAGE_TYPE, id, boundedBy, domainSet, rangeSet, rangeType)

    __COVERAGE_TYPE = "RectifiedGridCoverage"
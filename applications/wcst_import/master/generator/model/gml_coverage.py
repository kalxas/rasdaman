from abc import ABCMeta

from master.generator.model.bounded_by import BoundedBy
from master.generator.model.coverage_metadata import CoverageMetadata
from master.generator.model.domain_set import DomainSet
from master.generator.model.model import Model
from master.generator.model.range_set import RangeSet
from master.generator.model.range_type import RangeType


class GMLCoverage(Model):
    __metaclass__ = ABCMeta

    def __init__(self, coverageType, id, boundedBy, domainSet, rangeSet, rangeType, coverageMetadata=None, overview_index=None):
        """
        Model class for the coverage. This class should be extended by more specific coverages
         that can provide the coverage type
        :param str coverageType: the type of the coverage
        :param str id: the coverage id
        :param BoundedBy boundedBy: the boundedby element
        :param DomainSet domainSet: the domain set
        :param RangeSet rangeSet: the range set
        :param RangeType rangeType: the range type
        :param CoverageMetadata coverageMetadata: the extra metadata of the coverage
        """
        self.coverageType = coverageType
        self.id = id
        self.boundedBy = boundedBy
        self.domainSet = domainSet
        self.rangeSet = rangeSet
        self.rangeType = rangeType
        self.coverageMetadata = coverageMetadata
        self.overviewIndex = int(overview_index) if overview_index is not None else None

    def get_template_name(self):
        return "gml_coverage.xml"

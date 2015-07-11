from master.generator.model.range_type_field import RangeTypeField
from util.gdal_util import GDALGmlUtil


class GdalRangeFieldsGenerator:
    def __init__(self, gdal_dataset):
        """
        Class to generate the range fields from a gdal dataset
        :param GDALGmlUtil gdal_dataset: the gdal dataset
        """
        self.gdal_dataset = gdal_dataset

    def get_range_fields(self):
        """
        Returns the range fields for this gdal dataset
        :rtype: list[RangeTypeField]
        """
        fields = []
        for range_field in self.gdal_dataset.get_fields_range_type():
            fields.append(RangeTypeField(range_field.field_name, "", "", range_field.nill_values, range_field.uom_code))
        return fields
from master.generator.model.range_type_field import RangeTypeField
from master.generator.model.range_type_nill_value import RangeTypeNilValue
from util.gdal_util import GDALGmlUtil


class GdalRangeFieldsGenerator:
    def __init__(self, gdal_dataset, field_names=None):
        """
        Class to generate the range fields from a gdal dataset
        :param GDALGmlUtil gdal_dataset: the gdal dataset
        """
        self.gdal_dataset = gdal_dataset
        self.field_names = field_names

    def get_range_fields(self):
        """
        Returns the range fields for this gdal dataset
        :rtype: list[RangeTypeField]
        """
        fields = []
        field_id = 0
        for range_field in self.gdal_dataset.get_fields_range_type():
            nill_values = []
            for nill_value in range_field.nill_values:
                nill_values.append(RangeTypeNilValue("", nill_value))
            field_name = range_field.field_name
            if self.field_names is not None:
                field_name = self.field_names[field_id]
            field_id += 1
            fields.append(RangeTypeField(field_name, "", "", nill_values, range_field.uom_code))
        return fields
from master.generator.model.range_set import RangeSet


class RangeSetFile(RangeSet):
    def __init__(self, fileReference, mimetype):
        """
        Class to represent a range set that has a file as a data container
        :param fileReference: the full filepath
        :param mimetype: the mimetype of the file
        """
        self.fileReference = fileReference
        self.mimetype = mimetype

    def get_template_name(self):
        return "gml_range_set_file_ref.xml"

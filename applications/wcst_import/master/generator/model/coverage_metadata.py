from master.generator.model.model import Model


class CoverageMetadata(Model):
    def __init__(self, metadata):
        self.metadata = metadata

    def get_template_name(self):
        return "gml_coverage_metadata.xml"

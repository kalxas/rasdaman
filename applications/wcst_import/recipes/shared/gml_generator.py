from util.gml_field import GMLField


class GMLGenerator:
    def __init__(self, template_path):
        """
        A gml generator will generate a gml string based on a series of values
        :param str template_path: the path to the templates directory
        :return:
        """
        self.template = file(template_path + "/" + self.MAIN_GML_TEMPLATE).read()
        self.field_template = file(template_path + "/" + self.FIELD_TEMPLATE).read()
        self.vector_template = file(template_path + "/" + self.OFFSET_VECTOR_TEMPLATE).read()

    def coverage_id(self, coverage_id):
        """
        Fills the coverage id field
        :param str coverage_id: the id of the coverage
        :rtype GMLGenerator
        """
        self.template = self.template.replace("$VarGmlId", coverage_id)
        return self

    def crs(self, crs):
        """
        Fills the crs field
        :param str crs: an crs uri containing the crs resolver uri
        :rtype GMLGenerator
        """
        self.template = self.template.replace("$VarSrsName", crs)
        return self

    def grid_envelope_low(self, values):
        """
        Fills the low limit of the grid envelope
        :param list[int] values: a list of values for each dimension on the low side of it
        :rtype GMLGenerator
        """
        self.template = self.template.replace("$VarGridEnvelopeLow", " ".join(map(str, values)))
        return self

    def grid_envelope_high(self, values):
        """
        Fills the high limit of the grid envelope
        :param list[int] values: a list of values for each dimension on the high side of it
        :rtype GMLGenerator
        """
        self.template = self.template.replace("$VarGridEnvelopeHigh", " ".join(map(str, values)))
        return self

    def origin(self, values):
        """
        Fills the origin of the gml
        :param list[float] values: a list of values for the origin on each dimension
        :rtype GMLGenerator
        """
        self.template = self.template.replace("$VarOriginGmlPos", " ".join(map(str, values)))
        return self

    def coverage_data_url(self, url_to_coverage_data):
        """
        Fills the coverage url of the data
        :param str url_to_coverage_data: a url to the coverage data (file:/// ok as well)
        :rtype GMLGenerator
        """
        self.template = self.template.replace("$VarFileName", url_to_coverage_data)
        return self

    def coverage_data_url_mimetype(self, mimetype):
        """
        Fills the mimetype of the data url
        :param str mimetype: the mimetype of the file
        :rtype GMLGenerator
        """
        self.template = self.template.replace("$VarMimeType", mimetype)
        return self

    def fields(self, fields):
        """
        Fills the fields of the data url
        :param list[GMLField] fields: a list of fields
        :rtype GMLGenerator
        """
        field_tpls = []
        for field in fields:
            # Add the null values to the template
            template = self.field_template
            if field.nill_values:
                template = template.replace("$VarNillReason", "None")
                template = template.replace("$VarNillValues", """<swe:nilValues>
                        <swe:NilValues>
                            <swe:nilValue reason="">""" + str(field.nill_values) + """</swe:nilValue>
                        </swe:NilValues>
                    </swe:nilValues>""")
            else:
                template = template.replace("$VarNillValues", "")
            template = template.replace("$VarFieldName", field.field_name)
            template = template.replace("$VarUomCode", field.uom_code)
            field_tpls.append(template)

        self.template = self.template.replace("$VarFields", "".join(field_tpls))
        return self

    def offset_vectors(self, vectors, coefficients=[None, None, None]):
        """
        Fills the offset vectors of the gml
        :param list[list[float]] vectors: a list of offset vectors
        :param list[float] coefficients: a list of coefficients indexed the same as the vectors
        :rtype GMLGenerator
        """
        offset_vectors = ""
        for (vector, coefficient) in zip(vectors, coefficients):
            vector_template = self.vector_template.replace("$VarGmlOffset", " ".join(map(str, vector))) + "\n"
            if coefficient is not None:
                vector_template = vector_template.replace("$Coefficient", str(coefficient))
            else:
                vector_template = vector_template.replace("$Coefficient", "")
            offset_vectors += vector_template
        self.template = self.template.replace("$VarGmlOffsetVectors", offset_vectors)
        return self

    def generate(self):
        """
        Generates the gml string
        :rtype str
        """
        return self.template

    MAIN_GML_TEMPLATE = "gml_main_template.xml"
    OFFSET_VECTOR_TEMPLATE = "gml_offset_vector_template.xml"
    FIELD_TEMPLATE = "gml_range_type_template.xml"
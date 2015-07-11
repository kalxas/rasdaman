import os


class TemplateProcessor:
    def __init__(self):
        """
        The template processor transforms a model to a gml representation based on the template
        """

    def convert_to_gml(self, model):
        """
        Converts the given model to gml
        :param Model model: the model to fill the template with
        """
        template = open(self.__TEMPLATE_PATH + model.get_template_name()).read()
        for variable, value in model.__dict__.iteritems():
            to_replace = self.__TEMPLATE_VARIABLE_WRAPPER.replace("Variable", str(variable))
            replacement = ""
            if value is None:
                replacement = ""
            elif isinstance(value, list):
                value = map(lambda x: str(x), value)
                replacement = " ".join(value)
            else:
                replacement = str(value)

            template = template.replace(to_replace, replacement)
        return template

    __TEMPLATE_PATH = os.path.dirname(os.path.realpath(__file__)) + "/templates/"
    __TEMPLATE_VARIABLE_WRAPPER = "{{Variable}}"
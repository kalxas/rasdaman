from abc import ABCMeta, abstractmethod

from master.generator.template_processor import TemplateProcessor


class Model:
    """
    All model classes inherit from this base model
    """
    __metaclass__ = ABCMeta

    @abstractmethod
    def get_template_name(self):
        """
        Returns the name of the template
        :rtype: str
        """
        pass

    def to_gml(self):
        """
        Returns the gml format of this model based on its template
        :rtype: str
        """
        tp = TemplateProcessor()
        return tp.convert_to_gml(self)

    def __str__(self):
        return self.to_gml()

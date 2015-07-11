from abc import ABCMeta, abstractmethod

from master.generator.model.model import Model


class RangeSet(Model):
    __metaclass__ = ABCMeta

    @abstractmethod
    def get_template_name(self):
        pass
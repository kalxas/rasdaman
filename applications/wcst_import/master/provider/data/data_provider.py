from abc import ABCMeta, abstractmethod


class DataProvider:
    __metaclass__ = ABCMeta

    @abstractmethod
    def to_eq_hash(self):
        """
        Returns a hash of the object that can be used to compare with other providers that might be instantiated
        in a different run of the program. Two data providers must be hash equal if by importing them you
        obtain the same end result
        :rtype: str
        """
        pass

    def eq_hash(self, other):
        """
        Compares the data providers by their eq_hashes
        :param DataProvider other: the data provider to compare with
        :rtype: bool
        """
        if self.to_eq_hash() == other.to_eq_hash():
            return True
        return False
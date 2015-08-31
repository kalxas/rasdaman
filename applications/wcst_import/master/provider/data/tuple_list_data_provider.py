import random
from master.provider.data.data_provider import DataProvider


class TupleListDataProvider(DataProvider):
    def __init__(self, tuple_list):
        self.tuple_list = tuple_list

    def get_tuple_list(self):
        return self.tuple_list

    def to_eq_hash(self):
        return str(random.randrange(0, 99999999))
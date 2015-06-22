import importlib
import pkgutil


class ReflectionUtil:

    def __init__(self):
        """
        Utility class to help with reflection in python
        :return:
        """
        pass

    def import_submodules(self, package, recursive=True):
        """
        Import all submodules of a module, recursively, including subpackages
        :param package: package (name or actual module)
        :type package: str | module
        :rtype: dict[str, types.ModuleType]
        """
        if isinstance(package, str):
            package = importlib.import_module(package)
        results = {}
        for loader, name, is_pkg in pkgutil.walk_packages(package.__path__):
            full_name = package.__name__ + '.' + name
            results[full_name] = importlib.import_module(full_name)
            if recursive and is_pkg:
                results.update(self.import_submodules(full_name))
        return results
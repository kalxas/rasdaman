"""
 *
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2015 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""
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
    
    def get_all_subclasses(self, cls):
        """
        Returns all subclasses (not just direct ones) of given class cls.
        Credits: https://stackoverflow.com/a/3862957
        """
        return set(cls.__subclasses__()).union(
            [s for c in cls.__subclasses__() \
                   for s in self.get_all_subclasses(c)])


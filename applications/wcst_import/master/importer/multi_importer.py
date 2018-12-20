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

from importer import Importer
from util.log import log


class MultiImporter(Importer):
    def __init__(self, importers):
        self.importers = importers
        self.total = len(importers)
        self.processed = 0

    def ingest(self):
        for self.importer in self.importers:
            self.importer.ingest()
            self.processed += 1

    def get_progress(self):
        if self.total == 0:
            log.warn("No slices to import.")
            return -1, -1
        else:
            return self.processed, self.total

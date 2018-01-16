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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""


class Complex(object):
    """
    Represent a complex number object
    e.g: select complex( 42, 73 ) from test_grey will return (42,73)
    """
    def __init__(self, re, im):
        # real and imagine numbers
        self.re = re
        self.im = im

    def __str__(self):
        """
        String representing the complex number object
        :return: String (e.g: (42,73))
        """
        output = "(" + str(self.re) + "," + str(self.im) + ")"
        return output


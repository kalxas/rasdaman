#!/usr/bin/env python
# This file is part of rasdaman community.
#
# Rasdaman community is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Rasdaman community is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.    See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community.    If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
import urllib.parse
import urllib.request as urllib2, base64
import sys

# Post an encoded WCPS query to Petascope
wcpsURL = sys.argv[1]
wcpsQuery = sys.argv[2]
rasadmin_user = sys.argv[3]
rasadmin_pass = sys.argv[4]

base64string = base64.b64encode((rasadmin_user + ":" + rasadmin_pass).encode('utf-8')).decode()

data = urllib.parse.urlencode({ "query": wcpsQuery } ).encode()
request = urllib2.Request(wcpsURL, data)
request.add_header("Authorization", "Basic %s" % base64string)
response = urllib2.urlopen(request, timeout=150)

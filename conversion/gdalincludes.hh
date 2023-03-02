/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
 rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
/*************************************************************
 *
 * PURPOSE:
 *  This class contains all the includes needed for gdal
 *
 * BUGS:
 *
 * AUTHORS:
 *  Alex Dumitru <alex@flanche.net>
 *  Vlad Mericariu <vlad@flanche.net>
 *
 ************************************************************/
#ifndef GDALINCLUDES_H
#define GDALINCLUDES_H

#include "config.h"

#ifdef HAVE_GDAL

#include "common/pragmas/pragmas.hh"

DIAGNOSTIC_PUSH
IGNORE_WARNING("-Wredundant-decls")
IGNORE_WARNING("-Wshadow")

// fix redefinition of macros in the GDAL config
#undef PACKAGE_BUGREPORT
#undef PACKAGE_NAME
#undef PACKAGE_STRING
#undef PACKAGE_TARNAME
#undef PACKAGE_URL
#undef PACKAGE_VERSION
#include <cpl_conv.h>
#include <gdal_priv.h>
#include <gdal_rat.h>
#include <cpl_string.h>
#include <ogr_spatialref.h>
#include <gdal.h>
#include <vrtdataset.h>
#include <gdalwarper.h>
// fix redefinition of macros in the GDAL config
#undef PACKAGE_BUGREPORT
#undef PACKAGE_NAME
#undef PACKAGE_STRING
#undef PACKAGE_TARNAME
#undef PACKAGE_URL
#undef PACKAGE_VERSION

DIAGNOSTIC_POP

#endif

#endif /* GDALINCLUDES_H */

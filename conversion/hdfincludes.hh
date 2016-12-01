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
#ifndef HDFINCLUDES_H
#define HDFINCLUDES_H

#include "config.h"

#ifdef HAVE_HDF

#pragma GCC diagnostic ignored "-Wredundant-decls"
#pragma GCC diagnostic ignored "-Wshadow"


/* Definition clashed for type int8, defined in both
 * /usr/include/hdf.h and in /usr/include/tiff.h
 */
#define HAVE_INT8
#define int8 hdf_int8

#ifdef HAVE_HDF_H
#include "hdf.h"
#elif HAVE_HDF_HDF_H
#include "hdf/hdf.h"
#else
#error "No hdf.h header available."
#endif

#ifdef HAVE_MFHDF_H
#include "mfhdf.h"
#elif HAVE_HDF_MFHDF_H
#include "hdf/mfhdf.h"
#else
#error "No mfhdf.h header available."
#endif

#undef int8

#pragma GCC diagnostic warning "-Wredundant-decls"
#pragma GCC diagnostic warning "-Wshadow"

#endif

#endif  /* HDFINCLUDES_H */

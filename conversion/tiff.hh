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
/**
 * INCLUDE: tiff.hh
 *
 * MODULE:  conversion
 *
 * PURPOSE:
 * Provides interface to convert data between TIFF and internal format.
 * The convertFrom() and convertTo() methods accept a null-terminated
 * option string of the following syntax:
 *  optionString ::= ( option )*
 *  option       ::= "comptype=" string
 *                       "quality=" int
 *                       "bpp=" int
 *                       "bps=" int
 *                       "depth=" int
 *
 * CLASSES: r_Conv_TIFF
 *
 * COMMENTS:
 *
*/

#ifndef _R_CONV_TIFF_HH_
#define _R_CONV_TIFF_HH_

#include "conversion/convertor.hh"
#include "config.h"

//@ManMemo: Module {\bf conversion}

/*@Doc:
  TIFF convertor class.

  Supported parameters are

  \begin{tabular}{lcl}
  <tt>comptype</tt> && string && compression type (see below)\\
  <tt>quality</tt> && int && quality parameter (JPEG)\\
  <tt>bpp</tt> && int && override number of bits per p (if not set in the tiff)\\
  <tt>bps</tt> && int && override number of bits per s (if not set in the tiff)\\
  <tt>depth</tt> && int && override number of colors in the mdd\\
  \end{tabular}

  The compression type defaults to lzw, but it may be one of the
  following (but not all types may be supported by the version of
  the TIFF library; note also that some compression types are only
  valid for specific colour depths):

  \begin{tabular}{ll}
  <tt>none</tt>\\
  <tt>ccittrle</tt>\\
  <tt>ccittfax3</tt>\\
  <tt>ccittfax4</tt>\\
  <tt>lzw</tt>\\
  <tt>ojpeg</tt>\\
  <tt>jpeg</tt>\\
  <tt>next</tt>\\
  <tt>ccittrlew</tt>\\
  <tt>packbits</tt>\\
  <tt>thunderscan</tt>\\
  <tt>pixarfilm</tt>\\
  <tt>pixarlog</tt>\\
  <tt>deflate</tt>\\
  <tt>dcs</tt>\\
  <tt>jbig</tt>\\
  <tt>sgilog</tt> && Not Linux\\
  <tt>sgilog24</tt> && Not Linux\\
  <tt>it8ctpad</tt> && Not Linux\\
  <tt>it8lw</tt> && Not Linux\\
  <tt>it8mp</tt> && Not Linux\\
  <tt>it8bl</tt> && Not Linux\\
  \end{tabular}

  For more information refer to the TIFFlib manual pages.
 */

void TIFFError(const char *, const char *, va_list);

void TIFFWarning(const char *, const char *, va_list);

class r_Conv_TIFF : public r_Convert_Memory
{
public:
    /// constructor using an r_Type object
    r_Conv_TIFF(const char *src, const r_Minterval &interv, const r_Type *tp);
    /// constructor using convert_type_e shortcut
    r_Conv_TIFF(const char *src, const r_Minterval &interv, int type);
    /// destructor
    ~r_Conv_TIFF(void);

    /// convert to TIFF
    virtual r_Conv_Desc &convertTo(const char *options = NULL,
                                   const r_Range *nullValue = NULL);
    /// convert from TIFF
    virtual r_Conv_Desc &convertFrom(const char *options = NULL);
    /// convert data in a specific format to array
    virtual r_Conv_Desc &convertFrom(r_Format_Params options);
    /// cloning
    virtual r_Convertor *clone(void) const;
    /// identification
    virtual const char *get_name(void) const;
    virtual r_Data_Format get_data_format(void) const;

#ifdef HAVE_TIFF
    /// translate string compression type to libtiff compression type
    static int get_compression_from_name(const char *strComp);

    /// translate string resolution unit type to libtiff resolution unit type
    static int get_resunit_from_name(const char *strComp);
#endif  // HAVE_TIFF

private:
    /// init TIFF class
    void initTIFF(void);
    /// parameters
    char *compType;
    int quality;
    int override_bpp;
    int override_bps;
    int override_depth;
    char *sampleType;

#ifdef HAVE_TIFF
    /// connection between string compression type and libtiff compression type
    static const convert_string_t compNames[];
    /// connection between string resolution unit type and libtiff resolution unit type
    static const convert_string_t resunitNames[];
#endif  // HAVE_TIFF

    /// default rows per strip (32)
    static const int defaultRPS;
    /// temporary dummy file
    static const char dummyFileFmt[];

    /// default quality factor (0..100) for lossy compression
    static const unsigned int TIFF_DEFAULT_QUALITY;
};

#endif

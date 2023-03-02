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
* Copyright 2003-2016 Peter Baumann / rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/
/**
 * INCLUDE: json.hh
 *
 * MODULE:  conversion
 *
 * CLASSES: r_Conv_JSON
 *
 * COMMENTS:
 *
 * Provides interface to convert data to other formats.
 *
*/

#ifndef _R_CONV_JSON_HH_
#define _R_CONV_JSON_HH_

#include "conversion/convertor.hh"
#include "conversion/csv.hh"
#include "raslib/minterval.hh"
#include "raslib/error.hh"

//@ManMemo: Module {\bf conversion}

/*@Doc:
  JSON convertor class.
*/
class r_Conv_JSON : public r_Conv_CSV
{
public:
    /// constructor using an r_Type object. Exception if the type isn't atomic.
    r_Conv_JSON(const char *src, const r_Minterval &interv, const r_Type *tp);
    /// constructor using convert_type_e shortcut
    r_Conv_JSON(const char *src, const r_Minterval &interv, int tp);
    /// destructor
    ~r_Conv_JSON(void) override = default;

    /// convert to CSV
    r_Conv_Desc &convertTo(const char *options = NULL,
                           const r_Range *nullVal = NULL) override;
    /// convert from CSV
    r_Conv_Desc &convertFrom(const char *options = NULL) override;
    /// convert data in a specific format to array
    r_Conv_Desc &convertFrom(r_Format_Params options) override;
    /// cloning
    r_Convertor *clone(void) const override;
    /// identification
    const char *get_name(void) const override;
    r_Data_Format get_data_format(void) const override;

private:
    void initJSON();
};

#endif

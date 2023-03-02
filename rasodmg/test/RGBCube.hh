//------------------------------------------------------------
//  This file is created automatically by the rasdl processor.
//  better than modifying this file is to re-generate it.
//------------------------------------------------------------

#ifndef __OUT_HH_
#define __OUT_HH_

//------------------------------------------------------------
//  Includes
//------------------------------------------------------------

#include "rasdaman.hh"

/*[38,1]*/ /* STRUCT -------------------------- RGBPixel */
struct RGBPixel
{
    r_Char red;
    r_Char green;
    r_Char blue;
};
/*[40,30]*/ /* TYPEDEF ------------------------- RGBCube */
typedef r_Marray<RGBPixel> RGBCube;

/*[41,22]*/ /* TYPEDEF ------------------------- RGBSet3 */
typedef r_Set<r_Ref<RGBCube>> RGBSet3;

#endif

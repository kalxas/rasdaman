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
/*------------------------------------------------------------------------*/
/*  http-fields.c - get and set HTTP Header fieldnames.                   */
/*------------------------------------------------------------------------*/


#include "config.h"
#include   "defs.h"
#include   "protos.h"
#include   "server.h"
#include   "http-defs.h"
#include   "http.h"


struct KeywordKey FieldnameKeyTable[] =
{
    { const_cast<char*>("Accept")                  , HKEY_Accept },
    { const_cast<char*>("Accept-Charset")          , HKEY_Accept_Charset },
    { const_cast<char*>("Accept-Encoding")         , HKEY_Accept_Encoding },
    { const_cast<char*>("Accept-Language")         , HKEY_Accept_Language },
    { const_cast<char*>("Accept-Ranges")           , HKEY_Accept_Ranges },
    { const_cast<char*>("Accept-charset")          , HKEY_Accept_Charset },
    { const_cast<char*>("Accept-encoding")         , HKEY_Accept_Encoding },
    { const_cast<char*>("Accept-language")         , HKEY_Accept_Language },
    { const_cast<char*>("Accept-ranges")           , HKEY_Accept_Ranges },
    { const_cast<char*>("Age")                     , HKEY_Age },
    { const_cast<char*>("Allow")                   , HKEY_Allow },
    { const_cast<char*>("Authorization")           , HKEY_Authorization },
    { const_cast<char*>("Cache-Control")           , HKEY_Cache_Control },
    { const_cast<char*>("Cache-control")           , HKEY_Cache_Control },
    { const_cast<char*>("Compliance")              , HKEY_Compliance },
    { const_cast<char*>("Connection")              , HKEY_Connection },
    { const_cast<char*>("Content-Base")            , HKEY_Content_Base },
    { const_cast<char*>("Content-Encoding")        , HKEY_Content_Encoding },
    { const_cast<char*>("Content-Language")        , HKEY_Content_Language },
    { const_cast<char*>("Content-Length")          , HKEY_Content_Length },
    { const_cast<char*>("Content-Location")        , HKEY_Content_Location },
    { const_cast<char*>("Content-MD5")             , HKEY_Content_MD5 },
    { const_cast<char*>("Content-Range")           , HKEY_Content_Range },
    { const_cast<char*>("Content-Type")            , HKEY_Content_Type },
    { const_cast<char*>("Content-base")            , HKEY_Content_Base },
    { const_cast<char*>("Content-encoding")        , HKEY_Content_Encoding },
    { const_cast<char*>("Content-language")        , HKEY_Content_Language },
    { const_cast<char*>("Content-length")          , HKEY_Content_Length },
    { const_cast<char*>("Content-location")        , HKEY_Content_Location },
    { const_cast<char*>("Content-range")           , HKEY_Content_Range },
    { const_cast<char*>("Content-type")            , HKEY_Content_Type },
    { const_cast<char*>("Date")                    , HKEY_Date },
    { const_cast<char*>("ETag")                    , HKEY_ETag },
    { const_cast<char*>("Expect")                  , HKEY_Expect },
    { const_cast<char*>("Expires")                 , HKEY_Expires },
    { const_cast<char*>("From")                    , HKEY_From },
    { const_cast<char*>("Host")                    , HKEY_Host },
    { const_cast<char*>("If-Modified-Since")       , HKEY_If_Modified_Since },
    { const_cast<char*>("If-Match")                , HKEY_If_Match },
    { const_cast<char*>("If-None-Match")           , HKEY_If_None_Match },
    { const_cast<char*>("If-Range")                , HKEY_If_Range },
    { const_cast<char*>("If-Unmodified-Since")     , HKEY_If_Unmodified_Since },
    { const_cast<char*>("Keep-Alive")              , HKEY_Keep_Alive },
    { const_cast<char*>("Last-Modified")           , HKEY_Last_Modified },
    { const_cast<char*>("Location")                , HKEY_Location },
    { const_cast<char*>("Max-Forwards")            , HKEY_Max_Forwards },
    { const_cast<char*>("Non-Compliance")          , HKEY_Non_Compliance },
    { const_cast<char*>("Pragma")                  , HKEY_Pragma },
    { const_cast<char*>("Proxy-Authenticate")      , HKEY_Proxy_Authenticate },
    { const_cast<char*>("Proxy-Authorization")     , HKEY_Proxy_Authorization },
    { const_cast<char*>("Public")                  , HKEY_Public },
    { const_cast<char*>("Range")                   , HKEY_Range },
    { const_cast<char*>("Referer")                 , HKEY_Referer },
    { const_cast<char*>("Retry-After")             , HKEY_Retry_After },
    { const_cast<char*>("Server")                  , HKEY_Server },
    { const_cast<char*>("Set-Proxy")               , HKEY_Set_Proxy },
    { const_cast<char*>("Transfer-Encoding")       , HKEY_Transfer_Encoding },
    { const_cast<char*>("Upgrade")                 , HKEY_Upgrade },
    { const_cast<char*>("User-Agent")              , HKEY_User_Agent },
    { const_cast<char*>("Vary")                    , HKEY_Vary },
    { const_cast<char*>("WWW-Authenticate")        , HKEY_WWW_Authenticate },
    { const_cast<char*>("Warning")                 , HKEY_Warning },
};

#define NUM_FIELDS  62


/****** http-fields/HTTP_GetHKey *********************************************
*
*   NAME
*
*
*   SYNOPSIS
*
*
*   FUNCTION
*
*
*   INPUTS
*
*
*   RESULT
*
*
*   NOTES
*
*
*   BUGS
*
*
*   SEE ALSO
*
*
******************************************************************************
*
*/

int HTTP_GetHKey( char *Keyword )
{
    int cond;
    int low;
    int high;
    int mid;
    int check[ NUM_FIELDS ];
    int i;

    low  = 0;
    high = NUM_FIELDS - 1;
    for( i = 0; i < NUM_FIELDS; i++ )
        check[ i ] = 0;

    while( low <= high )
    {
        mid = ( low + high ) / 2;
        if( ( cond = strcmp( Keyword, FieldnameKeyTable[mid].Keyword ) ) < 0 )
        {
            if( check[mid] == 0 )
            {
                check[mid] = 1;
                high = mid - 1;
            }
            else
                return( HKEY_UNKNOWN );
        }
        else if( cond > 0 )
        {
            if( check[mid] == 0 )
            {
                check[mid] = 1;
                low = mid + 1;
            }
            else
                return( HKEY_UNKNOWN );
        }
        else
            return( FieldnameKeyTable[mid].Key );
    }
    return( HKEY_UNKNOWN );
}


/****** http-fields/HTTP_GetFieldName ****************************************
*
*   NAME
*
*
*   SYNOPSIS
*
*
*   FUNCTION
*
*
*   INPUTS
*
*
*   RESULT
*
*
*   NOTES
*
*
*   BUGS
*
*
*   SEE ALSO
*
*
******************************************************************************
*
*/

char *HTTP_GetFieldName( int Key )
{
    int i;

    for( i = 0; i < NUM_FIELDS; i++ )
    {
        if( FieldnameKeyTable[i].Key == Key )
            return( FieldnameKeyTable[i].Keyword );
    }
    return( NULL );
}

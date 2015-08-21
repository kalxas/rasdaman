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

#include "config.h"
#include "raslib/parseparams.hh"
#include "raslib/mitera.hh"
#include "raslib/minterval.hh"
#include "raslib/primitivetype.hh"
#include "ecw.hh"
#include "convertor.hh"
#include "convfactory.hh"
#include "../common/src/logging/easylogging++.hh"

#ifdef ECW
#include "ecwmemfs.hh"
#include "NCSECWClient.h"
#include "NCSErrors.h"

NCSError memOpen(char *szFileName, void **ppClientData)
{
    MemoryFileSystem* myMem = new MemoryFileSystem();
    *ppClientData = (void*)myMem;
    MemoryFileSystem::m_Error err = MemoryFileSystem::No_Error;
    err = myMem->open(szFileName);
    if (err == MemoryFileSystem::No_Error)
        return NCS_SUCCESS;
    else
        return NCS_FILE_OPEN_FAILED;
}

NCSError memClose(void *pClientData)
{
    MemoryFileSystem::m_Error err = MemoryFileSystem::No_Error;
    err = ((MemoryFileSystem*)pClientData)->close();
    if (err == MemoryFileSystem::No_Error)
    {
        delete pClientData;
        return NCS_SUCCESS;
    }
    else
    {
        return NCS_FILE_CLOSE_ERROR;
    }
}

NCSError memRead(void *pClientData, void *pBuffer, UINT32 nLength)
{
    MemoryFileSystem::m_Error err = ((MemoryFileSystem*)pClientData)->read(pBuffer, nLength);
    if (err == MemoryFileSystem::No_Error)
    {
        return NCS_SUCCESS;
    }
    else
    {
        return NCS_FILE_SEEK_ERROR;
    }
}

NCSError memSeek(void *pClientData, UINT64 nOffset)
{
    MemoryFileSystem::m_Error err = ((MemoryFileSystem*)pClientData)->seek(nOffset);
    if (err == MemoryFileSystem::No_Error)
    {
        return NCS_SUCCESS;
    }
    else
    {
        return NCS_FILE_SEEK_ERROR;
    }
}

NCSError memTell(void *pClientData, UINT64 *pOffset)
{
    *pOffset = ((MemoryFileSystem*)pClientData)->tell();
    return NCS_SUCCESS;
}
#endif

void
r_Conv_ECW::initECW()
{
    LTRACE << "initECW()";
    if(params ==NULL)
    {
        params = new r_Parse_Params(2);
    }
}

r_Conv_ECW::r_Conv_ECW(const char* source, const r_Minterval& lengthordomain, const r_Type* tp) throw(r_Error)
    :   r_Convertor(source, lengthordomain, tp, true)
{
    LTRACE << "r_Conv_ECW(source, " << lengthordomain << ", " << tp->name() << ")";
    initECW();
}

r_Conv_ECW::r_Conv_ECW(const char* source, const r_Minterval& lengthordomain, int tp) throw(r_Error)
    :   r_Convertor(source, lengthordomain, tp)
{
    LTRACE << "r_Conv_ECW(source, " << lengthordomain << ", " << tp << ")";
    initECW();
}

r_convDesc&
r_Conv_ECW::convertFrom(const char* options) throw (r_Error)
{
#ifdef ECW
    int windowx = 1000;
    int windowy = 1000;
    params->add("windowx", &windowx, r_Parse_Params::param_type_int);
    params->add("windowy", &windowy, r_Parse_Params::param_type_int);
    params->process(options);
    LTRACE << "window x " << windowx;
    LTRACE << "window y " << windowy;
    switch (desc.baseType)
    {
    case ctype_bool:
    case ctype_char:
    case ctype_uint8:
        break;
    case ctype_rgb:
        break;
    default:
        LFATAL << "r_Conv_ECW unknown base type!";
        throw r_Error(COMPRESSIONFAILED);
    }
    NCSFileView *pNCSFileView = NULL;
    NCSFileViewFileInfo *pNCSFileInfo = NULL;

    NCSError eError = NCS_SUCCESS;
    eError = NCSecwSetIOCallbacks(memOpen, memClose, memRead, memSeek, memTell);
    if (eError != NCS_SUCCESS)
    {
        LFATAL << "Error = " << NCSGetErrorText(eError);
        throw r_Error(COMPRESSIONFAILED);
    }
    UINT8   **p_p_output_line = NULL;
    UINT8   *p_output_buffer = NULL;
    UINT32  x_size = 0;
    UINT32  y_size = 0;
    UINT32  number_x = 0;
    UINT32  number_y = 0;
    UINT32  start_x = 0;
    UINT32  start_y = 0;
    UINT32  end_x = 0;
    UINT32  end_y = 0;
    UINT32  band = 0;
    UINT32  nBands = 0;
    MemoryFileSystem::memorySrc = desc.src;
    MemoryFileSystem::memorySrcLength = desc.srcInterv[0].high() - desc.srcInterv[0].low() + 1;
    eError = NCScbmOpenFileView((char*)MemoryFileSystem::memorySrcName, &pNCSFileView, NULL);

    if (eError != NCS_SUCCESS)
    {
        LFATAL << "Error = " << NCSGetErrorText(eError);
        throw r_Error(COMPRESSIONFAILED);
    }

    NCScbmGetViewFileInfo(pNCSFileView, &pNCSFileInfo);
    x_size = pNCSFileInfo->nSizeX;
    y_size = pNCSFileInfo->nSizeY;
    nBands = pNCSFileInfo->nBands;
    LTRACE << "image : " << x_size << " x " << y_size << ", " << nBands << " bands";

    /* Have to set up the band list. Compatible with ER Mapper's method.*/
    /* In this example we always request all bands.*/
    UINT32* band_list = new UINT32[nBands];
    for( band = 0; band < nBands; band++ )
        band_list[band] = band;
    size_t typeLength = nBands;
    switch (desc.baseType)
    {
    case ctype_bool:
    case ctype_char:
    case ctype_uint8:
        if (nBands != 1)
        {
            LFATAL << "r_Conv_ECW conversion of base types bot supported";
            throw r_Error(COMPRESSIONFAILED);
        }
        break;
    case ctype_rgb:
        if (nBands != 3)
        {
            LFATAL << "r_Conv_ECW conversion of base types bot supported";
            throw r_Error(COMPRESSIONFAILED);
        }
        break;
    default:
        LFATAL << "r_Conv_ECW there is a really bad error in your compiler";
        throw r_Error(10000);
        break;
    }
    start_x = 0;
    start_y = 0;
    end_x = x_size - 1;
    end_y = y_size - 1;
    number_x = x_size;
    number_y = y_size;

    windowx = number_x;
    windowy = number_y;

    r_Minterval imageDomain(2);
    imageDomain << r_Sinterval((r_Range)0, (r_Range)number_x - 1);
    imageDomain << r_Sinterval((r_Range)0, (r_Range)number_y - 1);
    LTRACE << "image domain " << imageDomain << ", type length " << typeLength << " bands";
    char* image = (char*)mystore.storage_alloc(number_x * number_y * typeLength);
    //char* image = new char[number_x * number_y * typeLength];
    memset(image, 0, number_x * number_y * typeLength);
    r_Minterval maxDom(2);
    maxDom << r_Sinterval((r_Range)0, (r_Range)1000 - 1);
    maxDom << r_Sinterval((r_Range)0, (r_Range)1000 - 1);
    r_MiterArea dom_iter(&maxDom, &imageDomain);
    r_Minterval iterArea(2);
    while (!dom_iter.isDone())
    {
        iterArea = dom_iter.nextArea();
        start_x = iterArea[0].low();
        start_y = iterArea[1].low();
        end_x = iterArea[0].high();
        end_y = iterArea[1].high();
        number_x = iterArea[0].get_extent();
        number_y = iterArea[1].get_extent();
        LTRACE << "current " << start_x << ":" << end_x << "," << start_y << ":" << end_y << " window " << number_x << " x " << number_y;
        eError = NCScbmSetFileView(pNCSFileView, nBands, band_list, start_x, start_y, end_x, end_y, number_x, number_y);
        if( eError != NCS_SUCCESS)
        {
            LFATAL << "Error while setting file view to " << nBands << " bands, [" << start_x << ":" << end_x << "," << start_y << ":" << end_y << "], window size " << number_x << " x " << number_y << " pixel";
            LFATAL << "Error = " << NCSGetErrorText(eError);
            NCScbmCloseFileViewEx(pNCSFileView, TRUE);
            delete [] band_list;
            mystore.storage_free(image);
            throw r_Error(COMPRESSIONFAILED);
        }

        p_output_buffer = new UINT8[number_x * nBands];
        p_p_output_line = new UINT8*[nBands];

        for(band = 0; band < nBands; band++ )
            p_p_output_line[band] = p_output_buffer + (band * number_x);

        /*
        **  Read each line of the compressed file
        */
        for( UINT32 line = 0; line < number_y; line++ )
        {
            NCSEcwReadStatus eReadStatus = NCScbmReadViewLineBIL( pNCSFileView, p_p_output_line);
            if (eReadStatus != NCSECW_READ_OK)
            {
                LFATAL << "Read line error at line " <<  line;
                LFATAL << "Status code " <<  eReadStatus;
                NCScbmCloseFileViewEx(pNCSFileView, TRUE);
                delete [] band_list;
                delete [] p_p_output_line;
                delete [] p_output_buffer;
                mystore.storage_free(image);
                throw r_Error(COMPRESSIONFAILED);
            }
            for (int b = 0; b < nBands; b++)
            {
                for (int l = 0; l < number_x; l++)
                {
                    image[(l + iterArea[0].low()) * windowy * typeLength + (line + iterArea[1].low()) * typeLength + b] = p_p_output_line[b][l];
                }
            }
        }
        delete [] p_p_output_line;
        delete [] p_output_buffer;
        LTRACE << "read done";
    }
    NCScbmCloseFileViewEx(pNCSFileView, TRUE);
    delete [] band_list;
    desc.dest = (char*)image;
    desc.destInterv = imageDomain;
    desc.destType = get_external_type(desc.baseType);
    return desc;
#else
    LFATAL << "r_Conv_ECW::convertFrom(" << options << ") ecw support not compiled in";
    throw r_Error(COMPRESSIONFAILED);
#endif
}

r_convDesc&
r_Conv_ECW::convertTo(const char* options) throw (r_Error)
{
    LFATAL << "r_Conv_ECW::convertTo(" << options << ") compression not supported";
    throw r_Error(COMPRESSIONFAILED);
}

const char*
r_Conv_ECW::get_name() const
{
    return get_name_from_data_format(r_ECW);
}

r_Data_Format
r_Conv_ECW::get_data_format() const
{
    return r_ECW;
}

r_Convertor*
r_Conv_ECW::clone() const
{
    return new r_Conv_ECW(desc.src, desc.srcInterv, desc.srcType);
}


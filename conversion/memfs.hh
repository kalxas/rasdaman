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
 * INCLUDE: memfs.hh
 *
 * MODULE: conversion
 *
 * PURPOSE:
 * Memory Filing System used by some of the convertor modules.
 *
 * COMMENTS:
 *          None
*/

#ifndef _MEMFS_HH_
#define _MEMFS_HH_

/* For data types used by the memfs */
#include "raslib/odmgtypes.hh"
#include <stdint.h>

/* Claim blocks in 4k chunks */
const int MEMFS_LD_BLOCKSIZE = 12;
/* Initially preserve enough room for 16 blocks */
const int MEMFS_MAM_ENTRIES = 16;

typedef struct memFSContext
{
    r_Long pos, high;
    int mamSize, mamHighest;
    char **mam;
    char *chunk;
} memFSContext;

// copied from tiffio.h to avoid dependency on TIFF
// in TIFF these are defined with "t" as prefix instead of "ras_"
typedef signed long ras_size_t;
typedef unsigned long ras_off_t;
typedef void *ras_handle_t;
typedef void *ras_data_t;

int memfs_ensure(ras_handle_t handle, ras_off_t off);

#ifdef __cplusplus
extern "C"
{
#endif

    /* Flexible, read-write memFS */
    int memfs_initfs(ras_handle_t handle);
    void memfs_killfs(ras_handle_t handle);
    void memfs_newfile(ras_handle_t handle);
    ras_size_t memfs_read(ras_handle_t handle, ras_data_t mem, ras_size_t size);
    ras_size_t memfs_write(ras_handle_t handle, ras_data_t mem, ras_size_t size);
    ras_off_t memfs_seek(ras_handle_t handle, ras_off_t offset, int mode);
    int memfs_close(ras_handle_t handle);
    ras_off_t memfs_size(ras_handle_t handle);
    int memfs_map(ras_handle_t handle, ras_data_t *memp, ras_off_t *top);
    void memfs_unmap(ras_handle_t handle, ras_data_t mem, ras_off_t top);

    /* Simple, read-only memFS */
    void memfs_chunk_initfs(ras_handle_t handle, char *src, r_Long size);
    ras_size_t memfs_chunk_read(ras_handle_t handle, ras_data_t mem, ras_size_t size);
    ras_off_t memfs_chunk_seek(ras_handle_t handle, ras_off_t offset, int mode);
    int memfs_chunk_close(ras_handle_t handle);
    ras_off_t memfs_chunk_size(ras_handle_t handle);
    int memfs_chunk_map(ras_handle_t handle, ras_data_t *memp, ras_off_t *top);
    void memfs_chunk_unmap(ras_handle_t handle, ras_data_t mem, ras_off_t to);

#ifdef __cplusplus
}
#endif

#endif

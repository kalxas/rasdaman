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
 * SOURCE: memfs.c
 *
 * MODULE: conversion
 *
 * PURPOSE:
 * Memory Filing System used by some of the convertor modules
 *
 * COMMENTS:
 *      None
*/

#include "config.h"
#include "mymalloc/mymalloc.h"

#include <stdio.h>
#include <string.h>
#include "conversion/memfs.hh"

#include <logging.hh>

/* This function for internal use only */
int memfs_ensure(ras_handle_t handle, ras_off_t off)
{
    memFSContext *memFS = static_cast<memFSContext *>(handle);
    char **mam2 = NULL;
    int mamSize2 = 0, i = 0;

    LRTRACE("memfs_ensure: " << off)

    /* Do we have to allocate a bigger mam? */
    mamSize2 = static_cast<int>(off >> MEMFS_LD_BLOCKSIZE);
    if (mamSize2 >= memFS->mamSize)
    {
        /* Always allocate mam in powers of 2. That ensures that if we run out
           of space the new mam will be twice as big as the old one. */
        i = 0;
        while (mamSize2 != 0)
        {
            mamSize2 >>= 1;
            i++;
        }
        mamSize2 = (1 << i);

        LRTRACE("memfs_ensure: growing mam from " << memFS->mamSize << " to " << mamSize2)

        if ((mam2 = static_cast<char **>(mymalloc(static_cast<size_t>(mamSize2) * sizeof(char *)))) == NULL)
        {
            return -1;
        }
        /* Copy existing mam entries */
        memcpy(mam2, memFS->mam, static_cast<size_t>(memFS->mamSize) * sizeof(char *));
        /* Init new mam entries */
        for (i = memFS->mamSize; i < mamSize2; i++)
        {
            mam2[i] = NULL;
        }
        /* free old mam */
        free(memFS->mam);
        memFS->mam = mam2;
        memFS->mamSize = mamSize2;
    }
    /* Calculate again because its value might have been changed by the
       above block */
    mamSize2 = static_cast<int>(off >> MEMFS_LD_BLOCKSIZE);
    if ((memFS->mam)[mamSize2] == NULL)
    {
        /* We don't just have to allocate this one new block but all the
           ones with lower addresses that aren't defined yet as well */
        for (i = memFS->mamHighest + 1; i <= mamSize2; i++)
        {
            if (((memFS->mam)[i] = static_cast<char *>(mymalloc((1 << MEMFS_LD_BLOCKSIZE) * sizeof(char)))) == NULL)
            {
                return -1;
            }
        }
        memFS->mamHighest = mamSize2;
    }
    /* All done, the memFS can now hold an object of size off */
    return 0;
}

/* Initialise the memory filing system */
int memfs_initfs(ras_handle_t handle)
{
    memFSContext *memFS = static_cast<memFSContext *>(handle);
    int i = 0;

    LRTRACE("memfs_initfs")
    memFS->pos = 0;
    memFS->high = 0;
    memFS->mamSize = MEMFS_MAM_ENTRIES;
    if ((memFS->mam = static_cast<char **>(mymalloc(MEMFS_MAM_ENTRIES * sizeof(char *)))) == NULL)
    {
        return -1;
    }
    if (((memFS->mam)[0] = static_cast<char *>(mymalloc((1 << MEMFS_LD_BLOCKSIZE) * sizeof(char)))) == NULL)
    {
        return -1;
    }
    memFS->mamHighest = 0;
    for (i = 1; i < MEMFS_MAM_ENTRIES; i++)
    {
        (memFS->mam)[i] = NULL;
    }
    return 0;
}

/* Kill the memory filing system, freeing all its resources */
void memfs_killfs(ras_handle_t handle)
{
    memFSContext *memFS = static_cast<memFSContext *>(handle);
    int i = 0;

    LRTRACE("memfs_killfs")
    for (i = 0; i < memFS->mamSize; i++)
    {
        if ((memFS->mam)[i] == NULL)
        {
            break;
        }
        free((memFS->mam)[i]);
    }
    free(memFS->mam);
}

/* Reset file pointers, leave memory setup */
void memfs_newfile(ras_handle_t handle)
{
    memFSContext *memFS = static_cast<memFSContext *>(handle);

    LRTRACE("memfs_newfile")
    memFS->pos = 0;
    memFS->high = 0;
}

ras_size_t memfs_read(ras_handle_t handle, ras_data_t mem, ras_size_t size)
{
    ras_size_t todo = 0, transfered = 0;
    int block = 0, offset = 0, x = 0;
    memFSContext *memFS = static_cast<memFSContext *>(handle);

    /* Don't read over the end of the "file" */
    todo = memFS->high - memFS->pos;
    LRTRACE("memfs_read: ( " << todo << ", left: " << memFS->high << ")")
    if (todo > size)
    {
        todo = size;
    }
    while (todo > 0)
    {
        block = (memFS->pos >> MEMFS_LD_BLOCKSIZE);
        offset = memFS->pos - (block << MEMFS_LD_BLOCKSIZE);
        /* Space left in this buffer */
        x = (1 << MEMFS_LD_BLOCKSIZE) - offset;
        if (x > todo)
        {
            x = todo;
        }
        memcpy(mem, (((memFS->mam)[block]) + offset), static_cast<size_t>(x));
        /* ras_data_t is some kind of void *, so we have to do this cast */
        mem = static_cast<ras_data_t>((static_cast<char *>(mem)) + x);
        memFS->pos += x;
        transfered += x;
        todo -= x;
    }
    return transfered;
}

ras_size_t memfs_write(ras_handle_t handle, ras_data_t mem, ras_size_t size)
{
    ras_size_t transfered = 0;
    memFSContext *memFS = static_cast<memFSContext *>(handle);
    int block = 0, offset = 0, x = 0;

    /* Make sure there's enough room for this write */
    if (memfs_ensure(handle, static_cast<ras_off_t>(memFS->pos) + static_cast<ras_off_t>(size)) < 0)
    {
        return 0;
    }
    LRTRACE("memfs_write (" << size << ")")
    while (size > 0)
    {
        /* See memfs_read */
        block = (memFS->pos >> MEMFS_LD_BLOCKSIZE);
        offset = memFS->pos - (block << MEMFS_LD_BLOCKSIZE);
        x = (1 << MEMFS_LD_BLOCKSIZE) - offset;
        if (x > size)
        {
            x = size;
        }
        memcpy((((memFS->mam)[block]) + offset), mem, static_cast<size_t>(x));
        mem = static_cast<ras_data_t>((static_cast<char *>(mem)) + x);
        memFS->pos += x;
        transfered += x;
        size -= x;
    }
    if (memFS->pos > memFS->high)
    {
        memFS->high = memFS->pos;
    }
    return transfered;
}

ras_off_t memfs_seek(ras_handle_t handle, ras_off_t offset, int mode)
{
    memFSContext *memFS = static_cast<memFSContext *>(handle);

    switch (mode)
    {
    case SEEK_SET:
        memFS->pos = static_cast<int>(offset);
        break;
    case SEEK_CUR:
        memFS->pos += static_cast<int>(offset);
        break;
    case SEEK_END:
        memFS->pos = memFS->high + static_cast<int>(offset);
        break;
    default:
        break;
    }
    if (memFS->pos < 0)
    {
        memFS->pos = 0;
    }
    /* Don't limit to end of file (this actually caused problems!) */
    memfs_ensure(handle, static_cast<ras_off_t>(memFS->pos));
    if (memFS->pos > memFS->high)
    {
        memFS->high = memFS->pos;
    }
    LRTRACE("memfs_seek: Set pos to " << memFS->pos)
    return static_cast<ras_off_t>(memFS->pos);
}

int memfs_close(__attribute__((unused)) ras_handle_t handle)
{
    LRTRACE("memfs_close:")
    return 1; /* = success? */
}

ras_off_t memfs_size(ras_handle_t handle)
{
    LRTRACE("memfs_size:")
    return static_cast<ras_off_t>(((static_cast<memFSContext *>(handle))->high));
}

int memfs_map(__attribute__((unused)) ras_handle_t handle, __attribute__((unused)) ras_data_t *memp, __attribute__((unused)) ras_off_t *top)
{
    LRTRACE("memfs_map: " << *memp << ", " << *top)
    return 0;
}

void memfs_unmap(__attribute__((unused)) ras_handle_t handle, __attribute__((unused)) ras_data_t mem, __attribute__((unused)) ras_off_t to)
{
    LRTRACE("memfs_unmap: " << mem << ", " << to)
}

/* Read-only from memory (simple chunky model, not block-oriented) */
void memfs_chunk_initfs(ras_handle_t handle, char *src, r_Long size)
{
    memFSContext *memFS = static_cast<memFSContext *>(handle);

    LRTRACE("memfs_chunk_initfs: " << src << ", " << size)
    memFS->pos = 0;
    memFS->chunk = src;
    memFS->high = size;
}

ras_size_t memfs_chunk_read(ras_handle_t handle, ras_data_t mem, ras_size_t size)
{
    ras_size_t todo = 0;
    memFSContext *memFS = static_cast<memFSContext *>(handle);

    todo = memFS->high - memFS->pos;
    LRTRACE("memfs_chunk_read: " << size << " (left " << todo)
    if (todo > size)
    {
        todo = size;
    }
    if (todo > 0)
    {
        memcpy(mem, (memFS->chunk + memFS->pos), static_cast<size_t>(todo));
        memFS->pos += todo;
    }
    return todo;
}

ras_off_t memfs_chunk_seek(ras_handle_t handle, ras_off_t offset, int mode)
{
    memFSContext *memFS = static_cast<memFSContext *>(handle);

    switch (mode)
    {
    case SEEK_SET:
        memFS->pos = static_cast<int>(offset);
        break;
    case SEEK_CUR:
        memFS->pos += static_cast<int>(offset);
        break;
    case SEEK_END:
        memFS->pos = memFS->high + static_cast<int>(offset);
        break;
    default:
        break;
    }
    if (memFS->pos < 0)
    {
        memFS = 0;
    }
    /* Since file can't be extended this is OK here */
    if (memFS->pos > memFS->high)
    {
        memFS->pos = memFS->high;
    }
    LRTRACE("memfs_chunk_seek: Position to " << memFS->pos)
    return static_cast<ras_off_t>(memFS->pos);
}

int memfs_chunk_close(__attribute__((unused)) ras_handle_t handle)
{
    LRTRACE("memfs_chunk_close:")
    return 1;
}

ras_off_t memfs_chunk_size(ras_handle_t handle)
{
    LRTRACE("memfs_chunk_size:")
    return static_cast<ras_off_t>(((static_cast<memFSContext *>(handle))->high));
}

/* Map file to memory -- since we already have it in memory in the
   first place this is very simple. */
int memfs_chunk_map(ras_handle_t handle, ras_data_t *memp, ras_off_t *top)
{
    memFSContext *memFS = static_cast<memFSContext *>(handle);
    LRTRACE("memfs_chunk_map:")
    *memp = static_cast<ras_data_t>(memFS->chunk);
    *top = static_cast<ras_off_t>(memFS->high);
    return 1; /* Success? */
}

void memfs_chunk_unmap(__attribute__((unused)) ras_handle_t handle, __attribute__((unused)) ras_data_t mem, __attribute__((unused)) ras_off_t to)
{
    LRTRACE("memfs_chunk_unmap: " << mem << ", " << to)
}

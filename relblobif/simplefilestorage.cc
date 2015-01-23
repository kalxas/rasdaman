/*
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.com>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/
/*************************************************************
 *
 *
 * PURPOSE:
 * The interface used by the file storage modules.
 *
 *
 * COMMENTS:
 *
 ************************************************************/

#include "config.h"
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>
#include <stdlib.h>
#include "raslib/rminit.hh"
#include "reladminif/oidif.hh"
#include "simplefilestorage.hh"
#include "debug/debug-srv.hh"

using namespace std;

SimpleFileStorage::~SimpleFileStorage()
{
}

SimpleFileStorage::SimpleFileStorage(const string& path) throw (r_Error) : root_path(path)
{
    // Check if the path exist is readable/writable etc.
    struct stat status;
    if (stat(path.c_str(), &status)<0 || !S_ISDIR(status.st_mode))
    {
        generateError("file storage data directory not found", path, FILEDATADIR_NOTFOUND);
    }
    TALK("SimpleFileStorage initialized on root path" << path);
}

void SimpleFileStorage::insert(const char* data, r_Bytes size, int BlobId) throw (r_Error)
{
    ENTER("SimpleFileStorage::insert with BlobID="<<BlobId);
    vector<string> path;
    getPath(BlobId, &path);
    string file_path = path[0]; // Root path
    // Iterate trough the levels and create all directories needed.
    for (int i = 1; i < path.size() - 1; ++i)
    {
        file_path += '/' + path[i];
        struct stat status;
        if (!stat(file_path.c_str(), &status))
            if (!mkdir(file_path.c_str(), 0770))
            {
                generateError("failed creating directory", file_path, FAILEDCREATINGDIR);
            }
    }
    file_path += '/' + path[path.size() - 1];
    int fd = open(file_path.c_str(), O_CREAT | O_WRONLY, 0770);
    if (fd < 0)
    {
        generateError("failed opening file for writing", file_path, FAILEDOPENFORWRITING);
    }
    int offset = 0;
    // Send the data to the disk
    while (offset < size)
    {
        int count = write(fd, data + offset, size - offset);
        if (count == -1)
        {
            generateError("failed writing data to file", file_path, FAILEDWRITINGTODISK);
        }
        offset += count;
    }
    if (close(fd)<0)
    {
        generateError("failed writing data to file", file_path, FAILEDWRITINGTODISK);
    }
    LEAVE("SimpleFileStorage::insert");
}

void SimpleFileStorage::update(const char* data, r_Bytes size, int BlobId) throw (r_Error)
{
    ENTER("SimpleFileStorage::update");
    vector<string> path;
    getPath(BlobId, &path);
    string file_path = path[0]; // Root path
    // Iterate trough the levels and create all directories needed.
    for (int i = 1; i < path.size() - 1; ++i)
    {
        file_path += '/' + path[i];
        struct stat status;
        if (!stat(file_path.c_str(), &status))
            if (!mkdir(file_path.c_str(), 0770))
            {
                generateError("failed creating directory", file_path, FAILEDCREATINGDIR);
            }
    }
    file_path += '/' + path[path.size() - 1];

    int fd = open(file_path.c_str(), O_WRONLY | O_CREAT | O_TRUNC, 0770);
    if (fd < 0)
    {
        generateError("failed opening file for writing", file_path, FAILEDOPENFORWRITING);
    }
    int offset = 0;
    // Send the data to the disk
    while (offset < size)
    {
        int count = write(fd, data + offset, size - offset);
        if (count == -1)
        {
            generateError("failed writing data to blob file", file_path, FAILEDWRITINGTODISK);
        }
        offset += count;
    }
    if (close(fd)<0)
    {
        generateError("failed writing data to blob file", file_path, FAILEDWRITINGTODISK);
    }
    LEAVE("SimpleFileStorage::update");
}

void SimpleFileStorage::retrieve(int BlobId, char** data, r_Bytes* size) throw (r_Error)
{
    ENTER("SimpleFileStorage::read");
    string path;
    getPath(BlobId, &path);
    struct stat status;
    if (stat(path.c_str(), &status) < 0)
    {
        generateError("blob file not found", path, BLOBFILENOTFOUND);
    }

    *size = status.st_size;
    *data = (char*)malloc(status.st_size);

    int fd = open(path.c_str(), O_RDONLY);
    if (fd < 0)
    {
        generateError("failed opening blob file for reading", path, FAILEDOPENFORREADING);
    }
    int offset = 0;
    // Send the data to the disk
    while (offset < *size)
    {
        int count = read(fd, *data + offset, *size - offset);
        if (count == -1)
        {
            generateError("failed reading data from blob file", path, FAILEDREADINGFROMDISK);
        }
        offset += count;
    }
    if (close(fd) < 0)
    {
        generateError("failed reading data from blob file", path, FAILEDREADINGFROMDISK);
    }
    LEAVE("SimpleFileStorage::read");
}

void SimpleFileStorage::remove(int BlobId) throw (r_Error)
{
    ENTER("SimpleFileStorage::remove");
    string path;
    getPath(BlobId, &path);
    if (unlink(path.c_str()) < 0)
    {
        RMInit::logOut << endl << "Warning: failed deleting blob file from disk - " << path << endl;
        RMInit::logOut << "Reason: " << strerror(errno) << endl << endl;
//        generateError("", path, FAILEDREMOVINGFILE);
    }
    LEAVE("SimpleFileStorage::remove");
}

void SimpleFileStorage::getPath(int BlobId, vector<string> *path)
{
    ENTER("SimpleFileStorage::getPath");
    path->clear();
    path->push_back(root_path);
    stringstream aux;
    aux << BlobId;
    path->push_back(aux.str());
    LEAVE("SimpleFileStorage::getPath");
}

void SimpleFileStorage::getPath(int BlobId, string *path)
{
    vector<string> segments;
    getPath(BlobId, &segments);
    *path = segments[0];
    for (int i = 1; i < segments.size(); ++i)
        *path += '/' + segments[i];
}

void SimpleFileStorage::generateError(const char* message, string path, int errorCode) throw (r_Error)
{
    RMInit::logOut << endl << "Error: " << message << " - " << path << endl;
    RMInit::logOut << "Reason: " << strerror(errno) << endl << endl;
    throw r_Error(errorCode);
}

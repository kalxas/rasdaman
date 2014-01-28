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
 * Copyright 2003 - 2011 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

/*
 * Contributed to rasdaman by Alexander Herzig, Landcare Research New Zealand
 */

/*!
 * \brief   Helper class for handling rasdaman collections and images.
 *
 * This class facilitates handling of rasdaman collections by
 * hiding especially rasml queries (r_oql_execute() calls)
 * including transaction handling from the user and attempts
 * to provide a 'non-db-like' interface to access rasdaman
 * collections and images.
 *
 * \see RasdamanConnector
 */

#ifndef RASDAMANHELPER2_HH_
#define RASDAMANHELPER2_HH_

// general includes
#include <iostream>
#include <cstring>
#include <string>
#include <vector>

// the rasdaman basic data types
#include "rasdaman.hh"

// other includes
#include "RasdamanConnector.hh"

// postgresql
//#include "libpq-fe.h"

class RasdamanHelper2
{
public:

    /*! Constructor taking a RasdamanConnector object.
     *
     *  \param rasconn pointer to RasdamanConnector object, which
     *                 controls the connection to the underlying
     *                 rasdaman data base. Note that the connector
     *                 object may be shared with other helper objects.
     */
    RasdamanHelper2(RasdamanConnector* rasconn);
    virtual ~RasdamanHelper2();

    /*! checks whether the data base contains the specified collection
     *
     *  \param collname collection name
     *  \return when the collection exists, the local OID is returned,
     *          otherwise -1;
     */
    double doesCollectionExist(const std::string& collname) throw (r_Error);

    /*! Inserts a new collection 'collname' of type rtype into the data base */
    void insertCollection(const std::string& collname, r_Type::r_Type_Id rtype, bool asCube);

    /*! Inserts a new collection 'collname' of type rtype into the data base */
    void insertUserCollection(const std::string& collname, const std::string& colltypename);

    /*! Deletes collection 'collname' from the data base. */
    void dropCollection(const std::string& collname);

    /*! Deletes an image from a collection. */
    void dropImage(const std::string& collname, double oid);

    /*! Returns a collection's (local) image OIDs */
    std::vector<double> getImageOIDs(const std::string& collname);

    /*! Retrieves a set of OIDs matching the specified metadata
     *  (Key-Value-Pairs)
     *
     *  \see writeExtraMetadata
     */
    std::vector<double> queryImageOIDs(const std::string& kvp);

    /*! Returns the local collection name for the given OID */
    std::string getCollectionNameFromOID(double oid);

    /*! returns local OID from coverage name */
    double getOIDFromCoverageName(const std::string& coverage);
    long getCoverageIdFromOID(double oid);

    /*! returns the order index of the given
     *  z-coordinate, if it was appended to the irregular axis
     *  of the given image/coverage;
     *  returns -1 otherwise
     */
    long canAppendReferencedZCoeff(double oid,
            double coefficient);

    /*! Queries the spatial domain of the given image
     *
     *  \param collname name of collection the particular image belongs to
     *  \param localImgOID local OID of the image for which the spatial domain
     *         is requested
     *  \return a r_Minterval object specifying the spatial cell domain of the image
     */
    r_Minterval getImageSdom(const std::string& collname, double localImgOID);

    r_Marray_Type *getMarrayType(const std::string& collname);

    /*! Returns the data (pixel) type id of the given collection and band.
     *
     *  \param collname The name of the rasdaman collection
     *  \param band The 0-based band (channel) number of a collection's image
     *     \return rasdaman type identifier
     *
     *     \see r_Type
     */
    r_Type::r_Type_Id getBaseTypeId(const std::string& collname,
            unsigned int band=0);

    /*! Returns the base type name of the given collection */
    std::string getBaseTypeName(const std::string& collname);

    /*! Returns the size in bytes of the collection's base type
     *  (note: the base type can be of type struct)
     */
    unsigned int getBaseTypeSize(const std::string& collname);

    /*! Returns the number of bands (channels) of images in the given collection*/
    unsigned int getBaseTypeElementCount(const std::string& collname);

    /*! Returns the size in bytes of the type referenced by the given type identifier*/
    unsigned int getTypeSize(r_Type::r_Type_Id rtype) throw (r_Error);

    /*! Returns the string representation (e.g. 'Grey', 'ULong') of rtype
     *  which can be used as prefix to build collection and image type strings
     *  such as 'GreySet', 'UShortImage', etc.
     */
    std::string getTypePrefixString(r_Type::r_Type_Id rtype);

    // perhaps we don't need this.
    //  bool setComponentType(r_Type::r_Type_Id rtype);

    /*! Copies a given section of a particular image.
     *
     *  \param collname The name of the collection the image belongs to.
     *  \param localImgOID The image's local OID
     *  \param buf  Pointer to an allocated array the image values are copied into.
     *  \param sdom An r_Minterval object specifying the spatial domain of the
     *              image section to be copied into buf.
     */
    void getImageBuffer(const std::string& collname, double localImgOID, char* buf,
                        r_Minterval& sdom);

    /*! Inserts a new image into a collection
     *
     *    \param collname the name of the rasdaman target collection
     *    \param buf the 1D array holding the image values
     *    \param shift shift vector to applied upon import
     *    \param sdom pixel domain of the new image
     *    \param bRowMajor2ColMajor denotes whether the image array buf is to be transposed before import
     *    \param marraytypename the image type name as registered in rasdaman (e.g. RGBImage)
     *    \param tiling rasql-based tiling scheme specification to be passed to r_oql_execute()
     *    \return the local object identifier (OID) of the newly imported image
     */
    double insertImage(const std::string& collname, char* buf,
                       r_Point& shift, r_Minterval& sdom, bool bRowMajor2ColMajor,
                       const std::string& marraytypename, const std::string& tiling);

    /*! Updates an already present image, denoted by imgid, in the given collection
     *    (see insertImage(...) for further details)
     */
    void updateImage(const std::string& collname, double imgid,
                     char* buf, r_Point& shift, r_Minterval& sdom, bool bRowMajor2ColMajor,
                     const std::string& marraytypename)
                     throw (r_Error);


    /*! Transposes a 1D, 2D, or 3D column-major arrays into a row-major array
     *
     *  \param colbuf column-major array
     *  \param rowbuf row-major array
     *  \param rtype data type of array elements
     *  \param ncols number of columns of the data set (#pixel in x-direction)
     *  \param nrows number of rows of the data set (#pixel in y-direction)
     *  \param nlayers number of layers of the data set (#pixel in z-direction)
     */
    void colBuf2RowBuf(char* colbuf, char* rowbuf, r_Type::r_Type_Id rtype,
                       int ncols, int nrows, int nlayers) throw (r_Error);

    /*! Transposes a n-dimensional column-major array into a row-major array
     *
     *  \param colbuf column-major array
     *  \param rowbuf row-major array
     *  \param pixelsize size of array elements in byte
     *  \param sdom dimensions of the array
     */
    void colBuf2RowBuf(char* colbuf, char* rowbuf, unsigned int pixelsize,
                       r_Minterval& sdom);

    /*! Transposes a 1D, 2D, or 3D multi-band row-major arrays into a col-major array */
    void colBuf2RowBuf(char* colbuf, char* rowbuf, unsigned int pixelsize,
                       unsigned int nelem, int ncols, int nrows, int nlayers);

    /*! Transposes a 1D, 2D, or 3D row-major array into a column-major array
     *
     *  \param colbuf column-major array
     *  \param rowbuf row-major array
     *  \param rtype data type of array elements
     *  \param ncols number of columns of the data set (#pixel in x-direction)
     *  \param nrows number of rows of the data set (#pixel in y-direction)
     *  \param nlayers number of layers of the data set (#pixel in z-direction)
     */
    void rowBuf2ColBuf(char* rowbuf, char* colbuf, r_Type::r_Type_Id rtype,
                       int ncols, int nrows, int nlayers) throw (r_Error);

    /*! Transposes a n-dimensional row-major array into a column-major array
     *
     *  \param colbuf column-major array
     *  \param rowbuf row-major array
     *  \param pixelsize size of array elements in byte
     *  \param sdom dimensions of the array
     */
    void rowBuf2ColBuf(char* rowbuf, char* colbuf, unsigned int pixelsize,
                       r_Minterval& sdom);

    /*! Transposes a 1D, 2D, or 3D multi-band row-major arrays into a col-major array */
    void rowBuf2ColBuf(char* rowbuf, char* colbuf, unsigned int pixelsize,
                       unsigned int nelem, int ncols, int nrows, int nlayers);

    /*! Calculates the n-dimensional 0-based index of an array element
     *  given its 0-based index (offset) within the underlying 1D array.
     *
     *  The method requires the dimensions being specified starting
     *  with the fastest moving index (i.e. {column, row, layer, ...}
     *  for row-major arrays and {row, column, layer, ...} for column-major
     *  arrays).
     */
    std::vector<int> offset2index(int offset, std::vector<int>& sdom);

    /*! Calculates n-dimensional 0-based index of an array element
     *  given its 0-based index (offset) within the underlying 1D array */
    std::vector<int> offset2index(int offset, r_Minterval& sdom);

    int index2offset(r_Minterval& sdom, std::vector<int>& index);
    int index2offset(std::vector<int>& sdom, std::vector<int>& index);

    /*! Creates a new (empty) rasdaman image collection.
     *
     *    \param db the target rasdaman database of the new image collection
     *    \param imgSet reference to receive the collection to be created
     *    \param rtype target base type of the new image collection
     *    \param asCube when 'true', a 3D collection gets created, 2D otherwise
     */
    void createCollection(r_Database& db, r_Ref< r_Set< r_Ref< r_GMarray > > >& imgSet,
                          r_Type::r_Type_Id rtype, bool asCube) throw (r_Error);

    /*! Creates a new image object
     *
     *  \param image reference to image object receiving the image
     *  \param sdom pixel domain of the new image
     *  \param rtype data type of the image object
     */
    void createMDD(r_Ref< r_GMarray >& image, r_Minterval sdom,
                   r_Type::r_Type_Id rtype) throw (r_Error);

    /*! Returns the character (e.g. 'c') denoting the atomic base type
     *  specified by rtype.
     */
    std::string getNumConstChar(r_Type::r_Type_Id rtype) throw (r_Error);


    /*! deprecated */
    int getWCPSTypeId(r_Type::r_Type_Id rtype);


    /*! Returns the string representation of the data type specified by rtype*/
    std::string getDataTypeString(r_Type::r_Type_Id rtype);

    /*! Return the maximum image size used in sequential processing.
     *  Note: This is merely a look-up-value for client applications;
     *  RasdamanHelper2 does not make any use of it.
     */
    long getMaxImgSize(void)
    {
        return this->m_maximgsize;
    };
    void setMaxImgSize(long maximgsize)
    {
        this->m_maximgsize = maximgsize;
    };


    /* \brief Writes the raster attribute table (RAT) for the specified
     *        image into the rasdaman database.
     *
     *  \param filename Filename (full qualified path name) of the input
     *                  image file (must be GDAL-readable)
     *  \param oid The rasdmaan object identifier of the rasdaman image
     *             associated with the RAT
     *  \param band The (1-based) image band number of the image
     *              associated with the RAT.
     *  \return '1' if the RAT could be successfully written to the DB,
     *          otherwise '0'
     */
    int writeRAT(const std::string& filename,
                 double oid, int band) throw(r_Error);

    /* \brief Drops the raster attribute table (RAT) associated
     *        with a rasdaman image. Note: the function assumes that
     *        the RAT is named according to this schema: 'rat<band>_<oid>'.
     *
     * \param collname collection name the image (referenced by OID) belongs to
     * \param oid The unique object identifier of the rasdaman image
     */
    void dropRAT(const std::string& collname,
            double oid) throw(r_Error);

    /*! Populates the petascope database with metadata for the specified image/coverage
     *
     *     \param oid the local object identifer (OID) of the rasdaman image
     *     \param collname the name of the collection the image is belongs to
     *     \param crs the CRS-URI associated with this coverage
     *     \param rtype the type identifier of the
     */

    int writePSMetadata(
        long oid,
        const std::string& collname,
        const std::string& covername,
        const std::vector<std::string>& crs,
        const std::vector<int>& crs_order,
        const std::string& imagetype,
        double xmin,
        double xmax,
        double ymin,
        double ymax,
        double zmax,
        double zmin,
        double cellsize_x,
        double cellsize_y,
        double cellsize_z,
        bool isRegular,
        long irrZPos,
        double irregularZ);

    int writeExtraMetadata(long oid,
            const std::string& metadata);

    /*! retrieves geospatial metadata from the petascope database
     *  in default (xyz) order (defaultOrder=true); if defaultOrder=false
     *  metadata is returned in CRS order as specified upon image import
     */
    std::vector<double> getMetaGeoDomain(double oid, bool defaultOrder=true);
    std::vector<double> getMetaCellSize(double oid, bool defaultOrder=true);
    std::string getMetaCrsName(double oid);
    std::vector<std::string> getMetaCrsUris(double oid);

    /*! derives the CRS order relative to xyz order from offset vectors */
    std::vector<int> getCRSOrder(double oid);

    /*! create vector to map rasdaman (x:y:z = 0:1:2) axis-order to CRS order (e.g. 2:1:0) */
    std::vector<int> getRas2CrsMapping(const std::vector<int>& crsorder);

    std::string stripoffWhitespaces(const std::string& instr);

    /* \brief Parses the string representation of a numeric PostgreSQL array and
     *        fills a double vector with the ifentified values.
     *
     *    \param strar String representation of a PostgreSQL array returned by PGgetvalue(...)
     *    \param vec Reference to the double vector to hold array values
     *    \return The function returns true if it could successfully parse the array,
     *            otherwiese false.
     */
    bool parsePGStringArray(const std::string& strar,
            std::vector<double>& vec);


    bool parseKVPString(const std::string& kvp,
            std::vector<std::string>& keys,
            std::vector<std::string>& values,
            std::string sep=":");

    /* \brief Delete Petascope metadata for the specified rasdaman image
     *
     * \param collname Rasdaman collection name
     * \param oid Rasdaman unique (DB-wide) object identifier
     * \return 'true' if all associated metadata of the image could be deleted,
     *         otherwise false
     */
    int deletePSMetadata(const std::string& collname, double oid);

    /* \brief Extracts EPSG code from WKT CRS string and concatenates it
     *        with the default CRS-resovler URL
     *
     *  \param crsWKT The WKT string representation of a CRS
     *  \param dim Dimension of the image associated with the
     *             given CRS WKT representation
     *  \return The URL of the default CRS resolver pointing to
     *          the CRS represented by the WKT string
     */
    std::string getCRSURIfromWKT(const std::string& crsWKT,
            unsigned char dim, std::string& epsg);

    /* \brief Checks whether DB connection to the rasdaman database
     *        is alive by committing an empty transaction.
     *
     * \return 'true' if the connection is alive, otherwise false
     *
     */
    bool checkDbConnection(void);

protected:
    //! pointer to the connection object
    RasdamanConnector* m_pRasconn;
    //! private transaction object of this helper
    r_Transaction m_transaction;

    //! the maximum image (tile/buffer) size that
    //  gets read or write by this helper
    long m_maximgsize;

};

#endif /* RASDAMANHELPER2_HH_ */

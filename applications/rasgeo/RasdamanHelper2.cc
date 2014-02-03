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
#include "config.h"
#include "RasdamanHelper2.hh"

// rasdaman
#include <sstream>
#include <new>
#include <limits>
#include <cmath>
#include <algorithm>
#include <ctype.h>
#include <map>
#include "raslib/mintervaltype.hh"
#include "raslib/collectiontype.hh"
#include "raslib/marraytype.hh"

// postgresql
#include "libpq-fe.h"

// gdal
#include "gdal_priv.h"
#include "gdal_rat.h"

// some string constants
#define PSPREFIX "ps"
#define crsURIprefix "%SECORE%/def/crs/"
#define ctx "RasdamanHelper2::"

#ifdef BUILD_RASSUPPORT
#undef ENTER
#undef LEAVE
#undef TALK
#undef SET_OUTPUT
#undef ctx

#include "nmlog.h"
// 'redirect' debug output to LUMASS macros
#define ENTER(a) NMDebugCtx(ctx, << "...")
#define LEAVE(a) NMDebugCtx(ctx, << "done!")
#define TALK(a)  NMDebugAI(<< a << std::endl)
#define SET_OUTPUT(a)
#define ctx "RasdamanHelper2"
#else
// include rasdaman debug macros
#include "debug/debug-clt.hh"
#endif

RasdamanHelper2::RasdamanHelper2(RasdamanConnector* rasconn)
{
    this->m_pRasconn = rasconn;
    this->m_pRasconn->connect();
    m_maximgsize = 134217728;
}

RasdamanHelper2::~RasdamanHelper2()
{
    if (this->m_transaction.get_status() == r_Transaction::active)
        this->m_transaction.abort();
}


bool RasdamanHelper2::PGFAILED(std::string fun, std::string msg, PGresult* res)
{
    if (PQresultStatus(res) != PGRES_COMMAND_OK)
    {
        std::cerr << "RasdamanHelper2::" << fun << " " << msg
                  << std::endl << PQresultErrorMessage(res) << std::endl;
        PQclear(res);
        return true;
    }

    return false;
}

bool RasdamanHelper2::PGDATAFAILED(std::string fun, std::string msg, PGresult* res)
{
    if (PQresultStatus(res) != PGRES_TUPLES_OK)
    {
        std::cerr << "RasdamanHelper2::" << fun << " " << msg
                  << std::endl << PQresultErrorMessage(res) << std::endl;
        PQclear(res);
        return true;
    }
    return false;
}

std::string
RasdamanHelper2::getCRSURIfromWKT(const std::string& crsWKT,
        unsigned char dim, std::string& epsg)
{
    ENTER(ctx << "getCRSURIfromWKT()");

    std::stringstream crsURI;
    std::string wkt = const_cast<std::string&>(crsWKT);

    std::transform(wkt.begin(), wkt.end(), wkt.begin(), ::tolower);
    int endpos = -1;
    int firstpos = wkt.rfind("epsg");
    TALK("epsg start: " << firstpos);

    bool failed = true;
    std::string code;
    if (firstpos > 0)
    {
        firstpos = wkt.find("\"", firstpos)+1;
        failed = firstpos > 0 ? false : true;
        //TALK("end \" of epsg at: " << firstpos);
        firstpos = wkt.find("\"", firstpos)+1;
        failed = firstpos > 0 ? false : true;
        //TALK("start of \" code at: " << firstpos);
        endpos = wkt.find("\"", firstpos+1);
        failed = firstpos > 0 ? false : true;
        //TALK("end of \" code at: " << firstpos);
        code = wkt.substr(firstpos, endpos-firstpos);
        failed = ::atol(code.c_str()) == 0 ? true : false;

        if (!failed)
        {
		epsg = code;
            crsURI << crsURIprefix << "EPSG/0/" << code;
        }
    }

    if (failed)
    {
        crsURI << crsURIprefix << "OGC/0/Index" << (int)dim << "D";
    }

    LEAVE(ctx << "getCRSURIfromWKT()");
    return crsURI.str();
}


double
RasdamanHelper2::doesCollectionExist(const std::string& collname)
                                     throw (r_Error)
{
    //this->m_pRasconn->connect();
    this->m_transaction.begin(r_Transaction::read_only);
    int ret = -1;

    try
    {
        r_Ref_Any any = this->m_pRasconn->getDatabase().lookup_object(collname.c_str());
        if (any != 0)
            ret = any.get_oid().get_local_oid();
    }
    catch (r_Error& re)
    {
        if (re.get_kind() != r_Error::r_Error_ObjectUnknown)
            throw re;
    }

    this->m_transaction.commit();
    return ret;
}

void
RasdamanHelper2::insertCollection(const std::string& collname,
                                       r_Type::r_Type_Id rtype,
                                       bool asCube)
{
    //this->m_pRasconn->connect();
    this->m_transaction.begin(r_Transaction::read_write);

    r_Ref<r_Set< r_Ref< r_GMarray > > > imgSet;

    this->createCollection(
        const_cast<r_Database&>(this->m_pRasconn->getDatabase()),
        imgSet, rtype, asCube);

    const_cast<r_Database&>(this->m_pRasconn->getDatabase()).set_object_name(
        *imgSet, collname.c_str());

    this->m_transaction.commit();
}

void
RasdamanHelper2::insertUserCollection(const std::string& collname,
        const std::string& colltypename)
{
    ENTER(ctx << "insertUserCollection()");

    this->m_transaction.begin(r_Transaction::read_write);

    std::stringstream qstr;
    qstr << "create collection " << collname << " " << colltypename;
    r_OQL_Query qins(qstr.str().c_str());
    r_oql_execute(qins);
    this->m_transaction.commit();

    LEAVE(ctx << "insertUserCollection()");
}

void
RasdamanHelper2::createCollection(r_Database& db,
            r_Ref< r_Set< r_Ref< r_GMarray > > >& imgSet,
            r_Type::r_Type_Id rtype, bool asCube)
            throw (r_Error)
{
    std::string typestr = this->getTypePrefixString(rtype) + "Set";
    if (asCube)
        typestr += "3";
    bool unk = false;
    switch (rtype)
    {
    case r_Type::CHAR:
        imgSet = new(&db, typestr.c_str())
        r_Set< r_Ref< r_Marray<r_Char> > >;
        break;
    case r_Type::BOOL:
        imgSet = new(&db, typestr.c_str())
        r_Set< r_Ref< r_Marray<r_Boolean> > >;
        break;
    case r_Type::ULONG:
        imgSet = new(&db, typestr.c_str())
        r_Set< r_Ref< r_Marray<r_ULong> > >;
        break;
    case r_Type::USHORT:
        imgSet = new(&db, typestr.c_str())
        r_Set< r_Ref< r_Marray<r_UShort> > >;
        break;
    case r_Type::LONG:
        imgSet = new(&db, typestr.c_str())
        r_Set< r_Ref< r_Marray<r_Long> > >;
        break;
    case r_Type::SHORT:
        imgSet = new(&db, typestr.c_str())
        r_Set< r_Ref< r_Marray<r_Short> > >;
        break;
    case r_Type::OCTET:
        imgSet = new(&db, typestr.c_str())
        r_Set< r_Ref< r_Marray<r_Octet> > >;
        break;
    case r_Type::DOUBLE:
        imgSet = new(&db, typestr.c_str())
        r_Set< r_Ref< r_Marray<r_Double> > >;
        break;
    case r_Type::FLOAT:
        imgSet = new(&db, typestr.c_str())
        r_Set< r_Ref< r_Marray<r_Float> > >;
        break;
    default:
        imgSet = 0;
        unk = true;
        break;
    }

    if (unk)
        throw r_Error(r_Error::r_Error_TypeInvalid);
    else if (imgSet == 0)
        throw r_Error(r_Error::r_Error_MemoryAllocation);
}

void
RasdamanHelper2::dropCollection(const std::string& collname)
{
    // before we drop the collection, we get a list of oids
    // and drop any associated RAT afterwards
    std::vector<double> oids = this->getImageOIDs(collname);

    // drop the RATs first (as long as the collname is still valid)
    for (int t=0; t < oids.size(); ++t)
        this->dropRAT(collname, oids[t]);

    this->m_transaction.begin(r_Transaction::read_write);

    std::string qstr = "drop collection $1";
    r_OQL_Query qo(qstr.c_str());
    qo << collname.c_str();

    r_oql_execute(qo);

    this->m_transaction.commit();
}

void
RasdamanHelper2::dropRAT(const std::string& collname,
        double oid) throw(r_Error)
{
    ENTER(ctx << "dropRAT()");

    const PGconn* conn = this->m_pRasconn->getRasConnection();
    if (conn == 0)
    {
        std::cerr << ctx << "dropRat(): "
        << "connection with '" << this->m_pRasconn->getRasDbName() << "' failed!"
        << std::endl;
        LEAVE(ctx << "dropRAT()");
        throw r_Error(r_Error::r_Error_General);
        return;
    }

    std::stringstream query;
    PGresult* res;

    // look for a RAT for each band
    unsigned int nbands = this->getBaseTypeElementCount(collname);
    for (unsigned int b=1; b <= nbands; ++b)
    {
        query << "drop table rat" << b << "_" << oid;
        TALK("'" << query.str() << "' ...");
        res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
        if (PQresultStatus(res) != PGRES_COMMAND_OK)
        {
            PQclear(res);
            TALK(ctx << "dropRAT(): no associated table for band #"
                    << b << " of image "<< oid
                     << " to drop!");
            LEAVE(ctx << "dropRAT()");
            return;
        }
        query.str("");
        PQclear(res);
    }
    LEAVE(ctx << "dropRAT()");
}

void
RasdamanHelper2::dropImage(const std::string& collname,
                           double localImgOID)
{
    this->m_transaction.begin(r_Transaction::read_write);

    std::string qstr = "delete from $1 as m where oid(m) = $2";
    r_OQL_Query quer(qstr.c_str());
    quer << collname.c_str() << (r_Long)localImgOID;

    r_oql_execute(quer);

    this->m_transaction.commit();

    // drop RAT, if available
    this->dropRAT(collname, localImgOID);
}

unsigned int
RasdamanHelper2::getTypeSize(r_Type::r_Type_Id rtype)
                             throw (r_Error)
{
    unsigned int ret = 0;

    switch (rtype)
    {
    case r_Type::CHAR:
        ret = sizeof(r_Char);
        break;
    case r_Type::ULONG:
        ret = sizeof(r_ULong);
        break;
    case r_Type::USHORT:
        ret = sizeof(r_UShort);
        break;
    case r_Type::LONG:
        ret = sizeof(r_Long);
        break;
    case r_Type::SHORT:
        ret = sizeof(r_Short);
        break;
    case r_Type::OCTET:
        ret = sizeof(r_Octet);
        break;
    case r_Type::DOUBLE:
        ret = sizeof(r_Double);
        break;
    case r_Type::FLOAT:
        ret = sizeof(r_Float);
        break;
    default:
        throw r_Error(r_Error::r_Error_TypeInvalid);
        break;
    }

    return ret;
}

std::string
RasdamanHelper2::getTypePrefixString(r_Type::r_Type_Id rtype)
{

    std::string ret = "";
    switch (rtype)
    {
    case r_Type::CHAR:
        ret = "Grey";
        break;
    case r_Type::ULONG:
        ret = "ULong";
        break;
    case r_Type::USHORT:
        ret = "UShort";
        break;
    case r_Type::LONG:
        ret = "Long";
        break;
    case r_Type::SHORT:
        ret = "Short";
        break;
    case r_Type::OCTET:
        ret = "Octet";
        break;
    case r_Type::DOUBLE:
        ret = "Double";
        break;
    case r_Type::FLOAT:
        ret = "Float";
        break;
    default:
        ret = "Unknwon";
        break;
    }
    return ret;
}

std::vector<double>
RasdamanHelper2::getImageOIDs(const std::string& collname)
{
    ENTER(ctx << "getImageOIDs()");
    std::vector<double> soids;

    //this->m_pRasconn->connect();
    this->m_transaction.begin(r_Transaction::read_only);

    std::string qstr = "select oid(m) from $1 as m";
    r_OQL_Query qo(qstr.c_str());
    qo << collname.c_str();

    TALK(qo.get_query() << " ... ");

    r_Set< r_Ref_Any > resSet;
    r_oql_execute(qo, resSet);

    if (!resSet.is_empty())
    {
        r_Iterator< r_Ref_Any > iter = resSet.create_iterator();

        for (iter.reset(); iter.not_done(); iter++)
        {
            r_Primitive* id = (r_Primitive*)(*iter);
            soids.push_back(id->get_double());
            TALK(id->get_double() << " ");
        }
    }

    this->m_transaction.commit();
    LEAVE(ctx << "getImageOIDs()");
    return soids;
}

r_Minterval
RasdamanHelper2::getImageSdom(const std::string& collname,
        double localImgOID)
{
    this->m_transaction.begin(r_Transaction::read_only);

    std::string qstr = "select sdom(m) from $1 as m where oid(m) = $2";
    r_OQL_Query q(qstr.c_str());
    q << collname.c_str() << (r_Long)localImgOID;

    r_Set< r_Ref_Any > resSet;
    r_oql_execute(q, resSet);

    if (!resSet.is_empty())
    {
        r_Minterval* mval = (r_Minterval*)(*resSet.create_iterator());
        r_Minterval mint(*mval);
        this->m_transaction.commit();
        return mint;
    }

    this->m_transaction.commit();
    return r_Minterval();
}

r_Marray_Type*
RasdamanHelper2::getMarrayType(const std::string& collname)
{
    ENTER(ctx << "getMarrayType()");
    r_Marray_Type *martype = 0;

    //this->m_pRasconn->connect();
    this->m_transaction.begin(r_Transaction::read_only);

    r_Ref_Any any = this->m_pRasconn->getDatabase().lookup_object(
                        collname.c_str());

    r_Set<r_GMarray>* coll((r_Set<r_GMarray>*)any.get_memory_ptr());

    r_Collection_Type* colltype = (r_Collection_Type*)coll->get_type_schema();
    martype = (r_Marray_Type*)colltype->element_type().clone();

    this->m_transaction.abort();

    LEAVE(ctx << "getMarrayType()");
    return martype;
}

r_Type::r_Type_Id
RasdamanHelper2::getBaseTypeId(const std::string& collname,
        unsigned int band)
{
    ENTER(ctx << "getBaseTypeId()");
    r_Type::r_Type_Id rtype = r_Type::UNKNOWNTYPE;

    //this->m_pRasconn->connect();
    this->m_transaction.begin(r_Transaction::read_only);

    r_Ref_Any any = this->m_pRasconn->getDatabase().lookup_object(
                        collname.c_str());

    r_Set<r_GMarray>* coll((r_Set<r_GMarray>*)any.get_memory_ptr());

    r_Collection_Type* colltype = (r_Collection_Type*)coll->get_type_schema();
    r_Marray_Type *martype = (r_Marray_Type*)&colltype->element_type();
    r_Base_Type* basetype = const_cast<r_Base_Type*>(&martype->base_type());
    if (basetype->isPrimitiveType())
    {
        rtype = ((r_Primitive_Type*)basetype)->type_id();
    }
    else if (basetype->isStructType())
    {
        r_Structure_Type* stype = ((r_Structure_Type*)basetype);

        if (band < stype->count_elements())
        {
            rtype = stype->resolve_attribute((unsigned int)band).type_of().type_id();
        }
        else
        {
            this->m_transaction.abort();
            TALK("ERROR: invalid band number!");
            throw r_Error(r_Error::r_Error_LimitsMismatch);
            LEAVE(ctx << "getBaseTypeId()");
            return rtype;
        }
    }

    this->m_transaction.commit();

    LEAVE(ctx << "getBaseTypeId()");
    return rtype;
}

void
RasdamanHelper2::getImageBuffer(const std::string& collname,
                                double localImgOID,
                                char* buf, r_Minterval& sdom)
{
    //this->m_pRasconn->connect();
    this->m_transaction.begin(r_Transaction::read_only);

    std::string qstr = "select m$1 from $2 as m where oid(m) = $3";
    r_OQL_Query quer(qstr.c_str());
    quer << sdom << collname.c_str() << (r_Long)localImgOID;

    r_Set< r_Ref_Any > resSet;
    r_oql_execute(quer, resSet);

    if (!resSet.is_empty())
    {
        r_GMarray* ar = (r_GMarray*)(*resSet.create_iterator()).get_memory_ptr();
        r_Bytes size = ar->get_array_size();
        memcpy((void*)buf, (const void*)ar->get_array(), size);
    }

    this->m_transaction.commit();
}

std::string
RasdamanHelper2::getBaseTypeName(const std::string& collname)
{
    std::string rstr = "Unknown";

    //this->m_pRasconn->connect();
    this->m_transaction.begin(r_Transaction::read_only);

    r_Ref_Any any = this->m_pRasconn->getDatabase().lookup_object(
                        collname.c_str());

    r_Set<r_GMarray>* coll((r_Set<r_GMarray>*)any.get_memory_ptr());

    r_Collection_Type* colltype = (r_Collection_Type*)coll->get_type_schema();
    r_Marray_Type *martype = (r_Marray_Type*)&colltype->element_type();
    r_Base_Type* basetype = const_cast<r_Base_Type*>(&martype->base_type());
    rstr = basetype->name();

    this->m_transaction.abort();
    return rstr;
}

unsigned int
RasdamanHelper2::getBaseTypeElementCount(const std::string& collname)
{
    ENTER(ctx << "getBaseTypeElementCount()");

    unsigned int ret = 1;

    //this->m_pRasconn->connect();
    this->m_transaction.begin(r_Transaction::read_only);

    r_Ref_Any any = this->m_pRasconn->getDatabase().lookup_object(
                        collname.c_str());

    r_Set<r_GMarray>* coll((r_Set<r_GMarray>*)any.get_memory_ptr());

    r_Collection_Type* colltype = (r_Collection_Type*)coll->get_type_schema();
    r_Marray_Type *martype = (r_Marray_Type*)&colltype->element_type();
    r_Base_Type *basetype = const_cast<r_Base_Type*>(&martype->base_type());
    if (basetype->isStructType())
        ret = ((r_Structure_Type*)basetype)->count_elements();

    this->m_transaction.abort();

    TALK("counted " << ret << " elements per pixel" << endl);
    LEAVE(ctx << "getBaseTypeElementCount()");
    return ret;
}

unsigned int
RasdamanHelper2::getBaseTypeSize(const std::string& collname)
{
    unsigned int len = -1;

    //this->m_pRasconn->connect();
    this->m_transaction.begin(r_Transaction::read_only);

    r_Ref_Any any = this->m_pRasconn->getDatabase().lookup_object(
                        collname.c_str());

    r_Set<r_GMarray>* coll((r_Set<r_GMarray>*)any.get_memory_ptr());

    r_Collection_Type* colltype = (r_Collection_Type*)coll->get_type_schema();
    r_Marray_Type *martype = (r_Marray_Type*)&colltype->element_type();
    r_Base_Type* basetype = const_cast<r_Base_Type*>(&martype->base_type());
    len = basetype->size();
    this->m_transaction.abort();
    return len;
}

double
RasdamanHelper2::insertImage(const std::string& collname,
                             char* buf, r_Point& shift,
                             r_Minterval& sdom,
                             bool bRowMajor2ColMajor,
                             const std::string& marraytypename,
                             const std::string& tiling)
{
    ENTER(ctx << "insertImage()");

    // get type information about the collection
    r_Type::r_Type_Id tid = this->getBaseTypeId(collname);
    TALK("collection's pixel base type: " << this->getDataTypeString(tid) << endl);

    // get the number of elements (in case we've got a struct type)
    unsigned int nelem = this->getBaseTypeElementCount(collname);

    // get a list of oids available prior to inserting the new image
    std::vector<double> preoids = this->getImageOIDs(collname);

    // create an initial image
    this->m_transaction.begin(r_Transaction::read_write);

    std::string qstr = "insert into $1 values marray x in [";

    // format the spatial domain string
    std::stringstream sdomstr;
    for (int d = 0; d < sdom.dimension(); d++)
    {
        sdomstr << shift[d] << ":" << shift[d];
        if (d == sdom.dimension()-1)
            sdomstr << "]";
        else
            sdomstr << ",";
    }
    qstr += sdomstr.str();

    // format the values string depending on the nubmer of elements
    // of the base type
    string numconst = this->getNumConstChar(tid);
    TALK("numeric constant is: " << numconst << endl);
    if (nelem == 1)
    {
        qstr += " values 0" + numconst;
    }
    else // struct type
    {
        qstr += " values {";
        for (int e=0; e < nelem; ++e)
        {
            qstr += "0" + numconst;
            if (e < nelem -1)
                qstr += ", ";
            else
                qstr += "}";
        }
    }

    // if the specified tiling string is not empty, we just append it to the insert query
    // string - we rely on r_oql to throw an execption, if anything is wrong ...
    if (!tiling.empty())
    {
        qstr += " " + tiling;
    }

    TALK("dummy grid query: " << qstr << std::endl);

    r_OQL_Query qins(qstr.c_str());
    qins << collname.c_str();
    r_oql_execute(qins);
    this->m_transaction.commit();

    // update the initially created image with the actual values

    // get the oid of the initially updated image
    std::vector<double> voids = this->getImageOIDs(collname);
    double oid = -1; //voids[voids.size()-1];
    if (voids.size() > 0)
        oid = voids[voids.size()-1];

    // since we're not quite sure whether the new oid is always the
    // last in the oid result set retrieved, we'd better check them
    // all
//  for (int post=voids.size()-1; post >= 0; post--)
//  {
//      oid = voids[post];
//      for (int pre=preoids.size()-1; pre >= 0; pre--)
//      {
//          //NMDebugInd(1, << voids[post] << " == " << preoids[pre] << std::endl);
//          if (voids[post] == preoids[pre])
//          {
//              oid = -1;
//              break;
//          }
//      }
//      if (oid != -1)
//          break;
//  }

    TALK("local oid of new image is #" << oid << std::endl);

    // if we get a null pointer, we quit here
    if (buf != 0 && oid != -1)
    {
        this->updateImage(collname, oid, buf, shift, sdom, bRowMajor2ColMajor,
                          marraytypename);
    }

    LEAVE(ctx << "insertImage()");
    return oid;
}

void
RasdamanHelper2::updateImage(const std::string& collname,
                             double imgid,
                             char* buf,
                             r_Point& shift,
                             r_Minterval& sdom,
                             bool bRowMajor2ColMajor,
                             const std::string& marraytypename)
                             throw (r_Error)
{
    ENTER(ctx << "updateImage()");

    // query type information
    r_Marray_Type* martype = this->getMarrayType(collname);
    const r_Base_Type &basetype = (*martype).base_type();
    unsigned nelem = 1;
    unsigned int elemsize = 0;
    r_Type::r_Type_Id tid;
    if (basetype.isPrimitiveType())
    {
        tid = ((r_Primitive_Type&)basetype).type_id();
        elemsize = basetype.size();
    }
    else if (basetype.isStructType())
    {
        r_Structure_Type *stype = ((r_Structure_Type*)&basetype);
        nelem = stype->count_elements();
        tid = stype->resolve_attribute((unsigned int)0).type_of().type_id();
        elemsize = stype->resolve_attribute((unsigned int)0).type_of().size();
    }
    unsigned int pixelsize = elemsize * nelem;
    delete martype;

    // construct typename for flat base types
    // and take the user specified typename for structs
    std::string mddtypename = marraytypename;
    if (nelem == 1)
    {
        std::string appendix = "";
        if (sdom.dimension() == 2)
            appendix = "Image";
        else if (sdom.dimension() == 3)
            appendix = "Cube";

        mddtypename = this->getTypePrefixString(tid) + appendix;
    }
    TALK("Marray type: " << mddtypename);

    int nlayers;
    if (sdom.dimension() == 2)
        nlayers = 1;
    else if (sdom.dimension() == 3)
        nlayers = sdom[2].get_extent();
    else
        throw r_Error(r_Error::r_Error_FeatureNotSupported);

    // initiate the r_GMarray, re-organise the array, if applicable,
    // and update the image
    r_Ref< r_GMarray > img;
    try
    {
        img = new (mddtypename.c_str()) r_GMarray(sdom, pixelsize);

        if (bRowMajor2ColMajor)
        {
            // we use the 3D restricted version for now, because its greater performance
            // as long as we don't support creating nD collections, we should stick with this
            // one
            this->rowBuf2ColBuf(buf, img->get_array(), pixelsize, nelem,
                                sdom[0].get_extent(), sdom[1].get_extent(), nlayers);
            //      this->rowBuf2ColBuf(buf, img->get_array(), pixelsize, sdom);
        }
        else
        {
            memcpy((void*)img->get_array(), (const void*)buf, sdom.cell_count() * pixelsize);
        }

        // update initially created image
        std::string qstr = "update $1 as m set m assign shift($2, $3) where oid(m) = $4";
        r_OQL_Query qo(qstr.c_str());
        qo << collname.c_str() << *img << shift << (r_Long)imgid;

        this->m_transaction.begin(r_Transaction::read_write);
        TALK(qo.get_query() << "...");
        r_oql_execute(qo);
        this->m_transaction.commit();

        // clean up
        img->r_deactivate();
    }
    catch (r_Error& e)
    {
        img->r_deactivate();
        std::cerr << ctx << "updateImage():"
                << "image update failed: "
                << e.what() << std::endl;
        throw;
    }

    LEAVE(ctx << "updateImage()");
}

void
RasdamanHelper2::createMDD(r_Ref< r_GMarray >& image,
                           r_Minterval sdom,
                           r_Type::r_Type_Id rtype)
                           throw (r_Error)
{
    std::string appendix = "";
    if (sdom.dimension() == 2)
        appendix = "Image";
    else if (sdom.dimension() == 3)
        appendix = "Cube";
    else
        throw r_Error(r_Error::r_Error_TypeInvalid);

    std::string typestr = this->getTypePrefixString(rtype) + appendix;

    switch (rtype)
    {
    case r_Type::CHAR:
        image = new (typestr.c_str()) r_Marray<r_Char>(sdom);
        break;
    case r_Type::BOOL:
        image = new (typestr.c_str()) r_Marray<r_Boolean>(sdom);
        break;
    case r_Type::ULONG:
        image = new (typestr.c_str()) r_Marray<r_ULong>(sdom);
        break;
    case r_Type::USHORT:
        image = new (typestr.c_str()) r_Marray<r_UShort>(sdom);
        break;
    case r_Type::LONG:
        image = new (typestr.c_str()) r_Marray<r_Long>(sdom);
        break;
    case r_Type::SHORT:
        image = new (typestr.c_str()) r_Marray<r_Short>(sdom);
        break;
    case r_Type::OCTET:
        image = new (typestr.c_str()) r_Marray<r_Octet>(sdom);
        break;
    case r_Type::DOUBLE:
        image = new (typestr.c_str()) r_Marray<r_Double>(sdom);
        break;
    case r_Type::FLOAT:
        image = new (typestr.c_str()) r_Marray<r_Float>(sdom);
        break;
    default:
        throw r_Error(r_Error::r_Error_TypeInvalid);
        return;
        break;
    }

    if (image == 0)
        throw r_Error(r_Error::r_Error_MemoryAllocation);
}

void
RasdamanHelper2::colBuf2RowBuf(char* colbuf,
                               char* rowbuf,
                               r_Type::r_Type_Id rtype,
                               int ncols,
                               int nrows,
                               int nlayers)
                               throw (r_Error)
{
    // map content of col-based colbuf to row-based rowbuf
    int col, row, layer;
    for (row = 0; row < nrows; row++)
    {
        for(col = 0; col < ncols; col++)
        {
            for (layer = 0; layer < nlayers; layer++)
            {
                switch (rtype)
                {
                case r_Type::CHAR:
                case r_Type::BOOL:
                    ((unsigned char*) rowbuf)[row * ncols + col + layer*ncols*nrows]
                    = ((unsigned char*) colbuf)[col * nrows + row + layer*ncols*nrows];
                    break;
                case r_Type::ULONG:
                    ((unsigned int*) rowbuf)[row * ncols + col + layer*ncols*nrows]
                    = ((unsigned int*) colbuf)[col * nrows + row + layer*ncols*nrows];
                    break;
                case r_Type::USHORT:
                    ((unsigned short*) rowbuf)[row * ncols + col + layer*ncols*nrows]
                    = ((unsigned short*) colbuf)[col * nrows + row + layer*ncols*nrows];
                    break;
                case r_Type::LONG:
                    ((int*) rowbuf)[row * ncols + col + layer*ncols*nrows] = ((int*) colbuf)[col
                            * nrows + row + layer*ncols*nrows];
                    break;
                case r_Type::SHORT:
                    ((short*) rowbuf)[row * ncols + col + layer*ncols*nrows] = ((short*) colbuf)[col
                            * nrows + row + layer*ncols*nrows];
                    break;
                case r_Type::OCTET:
                    ((signed char*) rowbuf)[row * ncols + col + layer*ncols*nrows]
                    = ((signed char*) colbuf)[col * nrows + row + layer*ncols*nrows];
                    break;
                case r_Type::DOUBLE:
                    ((double*) rowbuf)[row * ncols + col + layer*ncols*nrows] = ((double*) colbuf)[col
                            * nrows + row + layer*ncols*nrows];
                    break;
                case r_Type::FLOAT:
                    ((float*) rowbuf)[row * ncols + col + layer*ncols*nrows] = ((float*) colbuf)[col
                            * nrows + row + layer*ncols*nrows];
                    break;
                default:
                    throw r_Error(r_Error::r_Error_TypeInvalid);
                    break;
                }
            }
        }
    }
}

void
RasdamanHelper2::colBuf2RowBuf(char* colbuf,
                               char* rowbuf,
                               unsigned int pixelsize,
                               r_Minterval& sdom)
{
    vector<int> cmdims;
    cmdims.resize(sdom.dimension());

    int npix = 1;
    for (int d=0; d < sdom.dimension(); ++d)
    {
        cmdims[d] = sdom[d].get_extent();
        npix *= sdom[d].get_extent();
    }

    // swap col and row dim (i.e. from row-major to col-major)
    cmdims[0] = sdom[1].get_extent();
    cmdims[1] = sdom[0].get_extent();

    vector<int> cmidx;
    vector<int> rmidx;
    cmidx.resize(cmdims.size());
    rmidx.resize(cmdims.size());

    int rmpix;
    for (int pix=0; pix < npix; ++pix)
    {
        cmidx = this->offset2index(pix, cmdims);
        rmidx = cmidx;
        rmidx[0] = cmidx[1];
        rmidx[1] = cmidx[0];
        rmpix = this->index2offset(sdom, rmidx);

        memcpy((void*)(rowbuf + rmpix * pixelsize),
               (const void*)(colbuf + pix * pixelsize), pixelsize);
    }
}

void
RasdamanHelper2::rowBuf2ColBuf(char* rowbuf,
                               char* colbuf,
                               unsigned int pixelsize,
                               r_Minterval& sdom)
{
    vector<int> cmdims;
    cmdims.resize(sdom.dimension());

    int npix = 1;
    for (int d=0; d < sdom.dimension(); ++d)
    {
        cmdims[d] = sdom[d].get_extent();
        npix *= sdom[d].get_extent();
    }

    // swap col and row dim (i.e. from row-major to col-major)
    cmdims[0] = sdom[1].get_extent();
    cmdims[1] = sdom[0].get_extent();

    vector<int> cmidx;
    vector<int> rmidx;
    cmidx.resize(cmdims.size());
    rmidx.resize(cmdims.size());

    int cmpix;
    for (int pix=0; pix < npix; ++pix)
    {
        rmidx = offset2index(pix, sdom);
        cmidx = rmidx;
        cmidx[0] = rmidx[1];
        cmidx[1] = rmidx[0];
        cmpix = this->index2offset(cmdims, cmidx);

        memcpy((void*)(colbuf + cmpix * pixelsize),
               (const void*)(rowbuf + pix * pixelsize), pixelsize);
    }
}

void
RasdamanHelper2::rowBuf2ColBuf(char* rowbuf,
                               char* colbuf,
                               unsigned int pixelsize,
                               unsigned int nelem,
                               int ncols,
                               int nrows,
                               int nlayers)
{
    int elemsize = pixelsize / nelem;
    int elemoffsize = elemsize;

    if (nelem == 1)
    {
        elemsize = pixelsize;
        elemoffsize = 0;
    }

    int row, col, layer, elem, elemoff;
    for (row = 0; row < nrows; ++row)
    {
        for(col = 0; col < ncols; ++col)
        {
            for (layer = 0; layer < nlayers; ++layer)
            {
                for (elem = 0, elemoff = 0;
                        elem < nelem;
                        ++elem, elemoff += elemoffsize)
                {
                    memcpy((void*)(colbuf +  (col * nrows + row + layer*ncols*nrows)
                                   * pixelsize + elemoff),
                           (const void*)(rowbuf + (row * ncols + col + layer*ncols*nrows)
                                         * pixelsize + elemoff),
                           elemsize);
                }
            }
        }
    }
}

void
RasdamanHelper2::colBuf2RowBuf(char* colbuf,
                               char* rowbuf,
                               unsigned int pixelsize,
                               unsigned int nelem,
                               int ncols,
                               int nrows,
                               int nlayers)
{
    int elemsize = pixelsize / nelem;
    int elemoffsize = elemsize;

    if (nelem == 1)
    {
        elemsize = pixelsize;
        elemoffsize = 0;
    }

    int row, col, layer, elem, elemoff;
    for (row = 0; row < nrows; ++row)
    {
        for(col = 0; col < ncols; ++col)
        {
            for (layer = 0; layer < nlayers; ++layer)
            {
                for (elem = 0, elemoff = 0;
                        elem < nelem;
                        ++elem, elemoff += elemoffsize)
                {
                    memcpy((void*)(rowbuf + (row * ncols + col + layer*ncols*nrows)
                                   * pixelsize + elemoff),
                           (const void*)(colbuf +  (col * nrows + row + layer*ncols*nrows)
                                         * pixelsize + elemoff),
                           elemsize);
                }
            }
        }
    }
}


void
RasdamanHelper2::rowBuf2ColBuf(char* rowbuf,
                               char* colbuf,
                               r_Type::r_Type_Id rtype,
                               int ncols,
                               int nrows,
                               int nlayers)
                               throw (r_Error)
{
    // map content of row-based rowbuf to col-based colbuf
    int col, row, layer;
    for (row = 0; row < nrows; row++)
    {
        for (col = 0; col < ncols; col++)
        {
            for (layer = 0; layer < nlayers; layer++)
            {
                switch (rtype)
                {
                case r_Type::CHAR:
                case r_Type::BOOL:
                    ((unsigned char*) colbuf)[col * nrows + row + layer*ncols*nrows]
                    = ((unsigned char*) rowbuf)[row * ncols + col + layer*ncols*nrows];
                    break;
                case r_Type::ULONG:
                    ((unsigned int*) colbuf)[col * nrows + row + layer*ncols*nrows]
                    = ((unsigned int*) rowbuf)[row * ncols + col + layer*ncols*nrows];
                    break;
                case r_Type::USHORT:
                    ((unsigned short*) colbuf)[col * nrows + row + layer*ncols*nrows]
                    = ((unsigned short*) rowbuf)[row * ncols + col + layer*ncols*nrows];
                    break;
                case r_Type::LONG:
                    ((int*) colbuf)[col * nrows + row + layer*ncols*nrows] = ((int*) rowbuf)[row
                            * ncols + col + layer*ncols*nrows];
                    break;
                case r_Type::SHORT:
                    ((short*) colbuf)[col * nrows + row + layer*ncols*nrows] = ((short*) rowbuf)[row
                            * ncols + col + layer*ncols*nrows];
                    break;
                case r_Type::OCTET:
                    ((signed char*) colbuf)[col * nrows + row + layer*ncols*nrows]
                    = ((signed char*) rowbuf)[row * ncols + col + layer*ncols*nrows];
                    break;
                case r_Type::DOUBLE:
                    ((double*) colbuf)[col * nrows + row + layer*ncols*nrows] = ((double*) rowbuf)[row
                            * ncols + col + layer*ncols*nrows];
                    break;
                case r_Type::FLOAT:
                    ((float*) colbuf)[col * nrows + row + layer*ncols*nrows] = ((float*) rowbuf)[row
                            * ncols + col + layer*ncols*nrows];
                    break;
                default:
                    throw r_Error(r_Error::r_Error_TypeInvalid);
                    break;
                }
            }
        }
    }
}

std::vector<int>
RasdamanHelper2::offset2index(int offset,
                              std::vector<int>& sdom)
{
    std::vector<int> idx;
    idx.resize(sdom.size());

    for (int d=0; d < sdom.size(); ++d)
    {
        idx[d] = offset % sdom[d];
        offset /= sdom[d];
    }

    return idx;
}

std::vector<int>
RasdamanHelper2::offset2index(int offset,
                              r_Minterval& sdom)
{
    std::vector<int> idx;
    idx.resize(sdom.dimension());

    for (int d=0; d < sdom.dimension(); ++d)
    {
        idx[d] = offset % sdom[d].get_extent();
        offset /= sdom[d].get_extent();
    }

    return idx;
}

int
RasdamanHelper2::index2offset(r_Minterval& sdom,
                              std::vector<int>& index)
{
    int offset = 0;
    int mult = 1;
    for (int d=sdom.dimension()-1; d >=0; --d)
    {
        for (int r=d; r >=0; --r)
        {
            if (r == d)
                mult = index[d];
            else
                mult *= sdom[r].get_extent();
        }
        offset += mult;
    }

    return offset;
}

int
RasdamanHelper2::index2offset(std::vector<int>& sdom,
                              std::vector<int>& index)
{
    int offset = 0;
    int mult = 1;
    for (int d=sdom.size()-1; d >=0; --d)
    {
        for (int r=d; r >=0; --r)
        {
            if (r == d)
                mult = index[d];
            else
                mult *= sdom[r];
        }
        offset += mult;
    }

    return offset;
}



std::string
RasdamanHelper2::getNumConstChar(r_Type::r_Type_Id rtype)
                                 throw (r_Error)
{
    std::string numconst;
    switch (rtype)
    {
    case r_Type::OCTET:
        numconst =  "o";
        break;
    case r_Type::CHAR:
        numconst =  "c";
        break;
    case r_Type::BOOL:
        //numconst = "";
        break;
    case r_Type::ULONG:
        numconst =  "ul";
        break;
    case r_Type::USHORT:
        numconst =  "us";
        break;
    case r_Type::LONG:
        numconst =  "l";
        break;
    case r_Type::SHORT:
        numconst =  "s";
        break;
    case r_Type::DOUBLE:
        numconst =  "d";
        break;
    case r_Type::FLOAT:
        numconst =  "f";
        break;
    default:
        throw r_Error(r_Error::r_Error_TypeInvalid);
        break;
    }

    return numconst;
}

bool
RasdamanHelper2::parsePGStringArray(const std::string& strar,
                                    std::vector<double>& vec)
{
    vec.clear();
    size_t startpos = 0;
    size_t endpos = 0;
    std::string sub;

    // strip off curly brackets {}
    std::string bndstr = strar.substr(1, strar.size()-2);

    while ((endpos = bndstr.find(',', startpos)) != string::npos)
    {
        sub = bndstr.substr(startpos, endpos-startpos);
        vec.push_back(atof(sub.c_str()));
        startpos = endpos+1;
    }
    // get the last coordinate
    if (startpos != 0)
    {
        endpos = bndstr.size()-1;
        sub = bndstr.substr(startpos, endpos-startpos+1);
        vec.push_back(atof(sub.c_str()));
    }
    else if (startpos == 0 && bndstr.size() != 0)
    {
        vec.push_back(atof(bndstr.c_str()));
    }
    else
    {
        return false;
    }

    return true;
}

std::string
RasdamanHelper2::stripoffWhitespaces(const std::string& instr)
{
    std::string buf;
    std::string::size_type spos = 0;
    std::string::size_type epos = instr.size()-1;


    bool bfound = false;
    while(!bfound && spos < epos)
    {
        if (!iscntrl(instr[spos]) && !isspace(instr[spos]))
        {
            bfound = true;
        }
        else
        {
            ++spos;
        }
    }

    bfound = false;
    while(!bfound && epos >= spos)
    {
        if (!iscntrl(instr[epos]) && !isspace(instr[epos]))
        {
            bfound = true;
        }
        else
        {
            --epos;
        }
    }

    buf = instr.substr(spos, epos-spos+1);
    return buf;
}

bool
RasdamanHelper2::parseKVPString(const std::string& kvp,
            std::vector<std::string>& keys,
            std::vector<std::string>& values,
            std::string sep)
{
    keys.clear();
    values.clear();
    size_t startpos = 0;
    size_t endpos = 0;
    size_t eqpos = -1;
    std::string sub;
    std::string key;
    std::string value;

    while ((endpos = kvp.find(sep, startpos)) != string::npos)
    {
        sub = kvp.substr(startpos, endpos-startpos);

        // we only store non-empty values!
        if ((eqpos = sub.find('=', 0)) != string::npos &&
             sub.size() >= eqpos+2)
        {
            keys.push_back(this->stripoffWhitespaces(sub.substr(0, eqpos)));
            values.push_back(this->stripoffWhitespaces(sub.substr(eqpos+1, sub.size()-1-eqpos)));
        }
        startpos = endpos+1;
    }
    // get the last coordinate
    if (startpos != 0)
    {
        endpos = kvp.size()-1;
        sub = kvp.substr(startpos, endpos-startpos+1);

        if ((eqpos = sub.find('=', 0)) != string::npos &&
             sub.size() >= eqpos+2)
        {
            keys.push_back(this->stripoffWhitespaces(sub.substr(0, eqpos)));
            values.push_back(this->stripoffWhitespaces(sub.substr(eqpos+1, sub.size()-1-eqpos)));
        }
    }
    else if (startpos == 0 && kvp.size() != 0)
    {
        if ((eqpos = kvp.find('=', 0)) != string::npos &&
             kvp.size() >= eqpos+2)
        {
            keys.push_back(this->stripoffWhitespaces(kvp.substr(0, eqpos)));
            values.push_back(this->stripoffWhitespaces(kvp.substr(eqpos+1, kvp.size()-1-eqpos)));
        }
    }
    else
        return false;

    return true;
}

int
RasdamanHelper2::writeExtraMetadata(long oid,
        const std::string& metadata)
{
    ENTER(ctx << "writeExtraMetadata()");

    if (oid < 0)
    {
        std::cerr << ctx << "writeExtraMetadata(): "
        << "invalid oid specified!" << std::endl;
        LEAVE(ctx << "writeExtraMetadata()");
        return 0;
    }

    std::vector<std::string> keys;
    std::vector<std::string> values;

    if (!this->parseKVPString(metadata, keys, values))
    {
        std::cerr << ctx << "writeExtraMetadata(): "
                  << "couldn't parse metadata string!"
                  << std::endl;
        LEAVE(ctx << "writeExtraMetadata()");
        return 0;
    }

    ////////////////////////////////////////////////////////////////////
    std::stringstream query;
    PGresult* res;

    // check connection
    const PGconn* petaconn = this->m_pRasconn->getPetaConnection();
    if (PQstatus(petaconn) != CONNECTION_OK)
    {
        TALK("connection to '" << this->m_pRasconn->getPetaDbName() << "' failed!");
        LEAVE(ctx << "writeExtraMetadata()");
        return 0;
    }


    // get the coverage_id from ps_rasdaman_collection
    query << "select coverage_id from " << PSPREFIX << "_range_set where storage_id = "
          <<      "(select id from " << PSPREFIX << "_rasdaman_collection where oid = "
          << oid << ")";
    TALK(query.str() << " ... ");
    res = PQexec(const_cast<PGconn*>(petaconn), query.str().c_str());
    if (PQresultStatus(res) != PGRES_TUPLES_OK)
    {
        TALK("failed identifying coverage_id for image #" << oid << ":"
                << std::endl << PQresultErrorMessage(res));
        PQclear(res);
        LEAVE(ctx << "writeExtraMetadata()");
        return 0;
    }

    if (PQntuples(res) < 1)
    {
        TALK("could not find coverage_id for image #" << oid << " in " << PSPREFIX << "_range_set");
        LEAVE(ctx << "writeExtraMetadata()");
        PQclear(res);
        return 0;
    }
    long covid = ::atol(PQgetvalue(res, 0, 0));
    TALK("coverage id for image #" << oid << " is: " << covid);
    query.str("");
    PQclear(res);

    // iterate over the key-value-pairs and add them to the data base
    int ret = 1;
    for (int k=0; k < keys.size(); ++k)
    {
        query << "select id from " << PSPREFIX << "_extra_metadata_type where type = '"
              << keys[k] << "'";
        TALK(query.str() << " ...");
        res = PQexec(const_cast<PGconn*>(petaconn), query.str().c_str());
        if (PQresultStatus(res) != PGRES_TUPLES_OK)
        {
            TALK("WARNING: failed identifying metadata_type_id for '"
                    << keys[k] << "':"
                    << std::endl << PQresultErrorMessage(res));
            ret = 0;
        }

        long metatypeid = -1;
        if (PQntuples(res) < 1)
        {
            TALK("Failed identifying metadata_type_id for '" << keys[k] << "' - Adding it now ...");
            query.str("");
            PQclear(res);

            query << "insert into " << PSPREFIX << "_extra_metadata_type (type) values ('"
                  << keys[k] << "')";
            res = PQexec(const_cast<PGconn*>(petaconn), query.str().c_str());
            if (PQresultStatus(res) != PGRES_COMMAND_OK)
            {
                TALK("WARNING: failed inserting new metadata type '"<< keys[k] << "'");
                ret = 0;
            }
            query.str("");
            PQclear(res);

            query << "select id from " << PSPREFIX << "_extra_metadata_type where type = '"
                  << keys[k] << "'";
            TALK(query.str() << " ...");
            res = PQexec(const_cast<PGconn*>(petaconn), query.str().c_str());
            if (PQresultStatus(res) != PGRES_TUPLES_OK)
            {
                TALK("WARNING: failed identifying metadata_type_id after inserting it(!): "
                        << std::endl << PQresultErrorMessage(res));
                ret = 0;
            }
        }
        metatypeid = ::atol(PQgetvalue(res,0,0));
        TALK("metadata_type_id for '" << keys[k] << "' = " << metatypeid);
        query.str("");
        PQclear(res);

        char* litstr = PQescapeLiteral(const_cast<PGconn*>(petaconn),
                values[k].c_str(), values[k].size());

        // write the name of the table into the ps_extra_metadata table
        query << "insert into " << PSPREFIX << "_extra_metadata (coverage_id, metadata_type_id, value) values ("
              << covid << "," << metatypeid << ", " << litstr << ")";
        TALK(query.str() << " ... ");
        res = PQexec(const_cast<PGconn*>(petaconn), query.str().c_str());
        if (PQresultStatus(res) != PGRES_COMMAND_OK)
        {
            TALK("WARNING: failed writing extra metadata " << litstr
                    << " into ps_extra_metadata table: "
                    << std::endl << PQresultErrorMessage(res));
            ret = 0;
        }
        PQfreemem((void*)litstr);
        query.str("");
        PQclear(res);
    }

    ////////////////////////////////////////////////////////////////////


    LEAVE(ctx << "writeExtraMetadata()");
    return ret;
}

std::string
RasdamanHelper2::getCollectionNameFromOID(double oid)
{
    ENTER(ctx << "getCollectionNameFromOID()");

    std::string collname;

    // check connection
    PGconn* conn = const_cast<PGconn*>(this->m_pRasconn->getPetaConnection());
    if (PQstatus(conn) != CONNECTION_OK)
    {
        TALK("connection to '" << this->m_pRasconn->getPetaDbName() << "' failed!");
        LEAVE(ctx << "getNMMetaGeoDomain()");
        return collname;
    }

    std::stringstream query;
    query << "select name from " << PSPREFIX << "_rasdaman_collection "
             "where oid = " << oid;
    TALK("'" << query.str() << "' ...");
    PGresult* res = PQexec(conn, query.str().c_str());
    if (PQntuples(res) < 1)
    {
        TALK("Couldn't find a matching collection for OID " << oid << "!");
        LEAVE(ctx << "getCollectionNameFromOID()");
        return collname;
    }

    collname = PQgetvalue(res, 0,0);
    TALK("The image with OID " << oid << " belongs to collection '"
            << collname << "'");
    return collname;

    LEAVE(ctx << "getCollectionNameFromOID()");
}

std::vector<double>
RasdamanHelper2::queryImageOIDs(const std::string& kvp)
{
    ENTER(ctx << "queryImageOIDs()");

    std::vector<double> oids;

    // get a list of keys and values
    std::vector<std::string> keys;
    std::vector<std::string> values;
    this->parseKVPString(kvp, keys, values);

    // check connection
    PGconn* conn = const_cast<PGconn*>(this->m_pRasconn->getPetaConnection());
    if (PQstatus(conn) != CONNECTION_OK)
    {
        TALK("connection to '" << this->m_pRasconn->getPetaDbName() << "' failed!");
        LEAVE(ctx << "queryImageOIDs()");
        return oids;
    }

    // extract the actually present key's from the
    // user-specified list

    PGresult* res;
    std::stringstream query;
    query << "select type from " << PSPREFIX << "_extra_metadata_type";
    TALK("'" << query.str() << "' ...");
    res = PQexec(conn, query.str().c_str());
    int numtypes = PQntuples(res);
    if (numtypes < 1)
    {
        TALK("Couldn't find any metadata types!");
        LEAVE(ctx << "queryImageOIDs()");
        PQclear(res);
        return oids;
    }

    std::vector<std::string> availkeys;
    std::vector<std::string> availvals;
    for (int k=0; k < keys.size(); ++k)
    {
        for (int t=0; t < numtypes; ++t)
        {
            if (::strcmp(keys[k].c_str(), PQgetvalue(res, t, 0)) == 0)
            {
                availkeys.push_back(keys[k]);
                availvals.push_back(values[k]);
                break;
            }
        }
    }
    query.str("");
    PQclear(res);

    // for now, throw an error, when the lists don't match
    if (availkeys.size() < keys.size())
    {
        // let user know something's wrong here
        TALK("User specified metadata types don't match available types!");
        LEAVE(ctx << "queryImageOIDs()");
        throw r_Error(r_Error::r_Error_QueryParameterTypeInvalid);
        return oids;
    }

    // create temporary view of metadata types and values per oid (coverage/image)
    std::stringstream query_view;
    query_view <<
    "create or replace temp view allmetadata as                          "
    "select oid, type, value                                             "
    "  from (select coverage_id, type, value                             "
    "         from (select coverage_id, metadata_type_id, value          "
    "                 from " << PSPREFIX << "_extra_metadata             "
    "              )                                                     "
    "           as v1                                                    "
    "           join                                                     "
    "              (select id, type                                      "
    "                from " << PSPREFIX << "_extra_metadata_type         "
    "              )                                                     "
    "           as v2 on v1.metadata_type_id = v2.id                     "
    "       )                                                            "
    "    as v3                                                           "
    "    natural join                                                    "
    "       (select coverage_id, storage_id                              "
    "         from " << PSPREFIX << "_range_set                          "
    "       )                                                            "
    "    as v4                                                           "
    "    inner join                                                      "
    "       (select id, oid                                              "
    "          from " << PSPREFIX << "_rasdaman_collection               "
    "       )                                                            "
    "    as v5 on v4.storage_id = v5.id;                                 ";

    // and now query the allmetadata view for each kvp and join
    // (inner) individual results (= logical and betwen kvps
    query << "select distinct k0.oid from ";
    for (int k=0; k < availkeys.size(); ++k)
    {
        char* key = PQescapeLiteral(conn, availkeys[k].c_str(), availkeys[k].size());
        char* val = PQescapeLiteral(conn, availvals[k].c_str(), availvals[k].size());

        if (k > 0)
        {
            query << " inner join ";
        }

        query << "(select * from allmetadata "
              << "   where type = " << key << " "
              << "    and value = " << val << ") as k" << k;

        if (k > 0)
        {
            query << " on k" << k-1 << ".oid = k" << k << ".oid";
        }
        PQfreemem((void*)key);
        PQfreemem((void*)val);
    }
    TALK("selection query ...\n" << query.str());

    // execute the whole two part query
    query_view << query.str();
    res = PQexec(conn, query_view.str().c_str());
    int numoids = PQntuples(res);
    if (numoids == 0)
    {
        TALK("Couldn't find an image matching '" << kvp << "'!");
        LEAVE(ctx << "queryImageOIDs()");
        PQclear(res);
        return oids;
    }
    query.str("");
    query_view.str("");

    // DEBUG OUTPUT
    for (int n=0; n < numoids; ++n)
    {
        query << "#" << n << ": " << ::atof(PQgetvalue(res, n, 0)) << " ";
        oids.push_back(::atof(PQgetvalue(res, n, 0)));
    }
    TALK("resulting OIDs: " << query.str() << std::endl);

    PQclear(res);

    return oids;

    LEAVE(ctx << "queryImageOIDs()");
}

std::vector<double>
RasdamanHelper2::getMetaGeoDomain(double oid, bool defaultOrder)
{
    ENTER(ctx << "getMetaGeoDomain()");

    std::vector<int> crsorder;
    if (defaultOrder)
        crsorder = this->getCRSOrder(oid);

    // init return value with empty x/y region and
    std::vector<double> dom;
    double dmax = std::numeric_limits<double>::max();
    double dmin = std::numeric_limits<double>::max() * -1;
    for (int e=0; e < 4; ++e)
    {
        if (std::fmod((float)e, (float)2) == 0)
            dom.push_back(dmax);
        else
            dom.push_back(dmin);
    }
    // z region is as big as possible
    dom.push_back(dmin);
    dom.push_back(dmax);

    // check connection
    const PGconn* conn = this->m_pRasconn->getPetaConnection();
    if (PQstatus(conn) != CONNECTION_OK)
    {
        TALK("connection to '" << this->m_pRasconn->getPetaDbName() << "' failed!");
        LEAVE(ctx << "getNMMetaGeoDomain()");
        return dom;
    }

    // get coverage (petascope) id and collection name of image
    std::stringstream query;
    query << "select coverage_id from " << PSPREFIX << "_range_set where storage_id = "
             " (select id from " << PSPREFIX << "_rasdaman_collection where oid = " << oid
             << ")";
    TALK("'" << query.str() << "' ...");
    PGresult* res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
    if (PQntuples(res) < 1)
    {
        TALK("failed querying coverage_id for image #" << oid << " !");
        LEAVE(ctx << "getMetaGeoDomain()");
        PQclear(res);
        return dom;
    }

    long id = ::atol(PQgetvalue(res, 0, 0));
    TALK("coverage id for image #" << oid << " is: " << id);
    PQclear(res);
    query.str("");


    // query lower left and upper right corner coordinates
    query << "select lower_left,upper_right from " << PSPREFIX << "_bounding_box where coverage_id = " << id;
    TALK("'" << query.str() << "' ...");
    res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
    if (PQresultStatus(res) != PGRES_TUPLES_OK)
    {
        TALK("failed querying bbox for image #" << oid << " !");
        LEAVE(ctx << "getMetaGeoDomain()");
        PQclear(res);
        return dom;
    }

    std::string strlowerleft = PQgetvalue(res, 0, 0);
    std::string strupperright = PQgetvalue(res, 0, 1);
    TALK("lower left (as string): " << strlowerleft);
    TALK("upper right (as string): " << strupperright);

    std::vector<double> vll;
    std::vector<double> vur;

    if (!this->parsePGStringArray(strlowerleft, vll) ||
        !this->parsePGStringArray(strupperright, vur))
    {
        TALK("failed parsing PG string array!");
        LEAVE(ctx << "getMetaGeoDomain()");
        PQclear(res);
    }

    // copy domain values into domain vector

    // return in default xyz oder
    if (crsorder.size() > 0 && crsorder.size() == vll.size())
    {
        for (int d=0; d < vll.size(); ++d)
        {
            dom[crsorder[d]*2]   = vll[d];
            dom[crsorder[d]*2+1] = vur[d];
        }
    }
    // return in recorded petascope order
    else
    {
        for (int d=0; d < vll.size(); ++d)
        {
            dom[d*2]   = vll[d];
            dom[d*2+1] = vur[d];
        }
    }

    PQclear(res);

    LEAVE(ctx << "getMetaGeoDomain()");
    return dom;
}

std::vector<int>
RasdamanHelper2::getCRSOrder(double oid)
{
    ENTER(ctx << "getCRSOrder()");
    std::vector<int> order;

    // check connection
    const PGconn* conn = this->m_pRasconn->getPetaConnection();
    if (PQstatus(conn) != CONNECTION_OK)
    {
        TALK("connection to '" << this->m_pRasconn->getPetaDbName() << "' failed!");
        LEAVE(ctx << "getCRSOrder()");
        return order;
    }

    // get the coverage id for this image
    long covid = this->getCoverageIdFromOID(oid);
    if (covid < 0)
    {
        TALK("no coverage associated with the specified image OID=" << oid);
        LEAVE(ctx << "getCRSOrder()");
        return order;
    }

    // get the offset vectors for each axis and their associated rasdaman order
    std::stringstream query;
    PGresult* res;

    query << "select id, rasdaman_order, offset_vector "
          <<    "from (select * "
          <<             "from " << PSPREFIX << "_grid_axis "
          <<            "where gridded_coverage_id = " << covid << ") v1 "
          <<    "join (select * from " << PSPREFIX << "_rectilinear_axis) v2 "
          <<          "on v1.id = v2.grid_axis_id "
          <<    "order by rasdaman_order asc";
    TALK("'" << query.str() << "' ...");
    res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
    if (PGDATAFAILED("getCRSOrder()", "retrieving offset_vector information failed!", res))
	{
		LEAVE(ctx << "getCRSOrder()");
		return order;
	}


    // iterate over the offset tuples and determine the index of the non-zero offset per axis
    order.resize(PQntuples(res));
    for (int i=0; i < order.size(); ++i)
    {
        long id = atol(PQgetvalue(res, i, 0));

        std::string offstr = PQgetvalue(res, i, 2);
        std::vector<double> offval;
        this->parsePGStringArray(offstr, offval);
        if (offval.size() != order.size())
        {
            std::cerr << ctx << "getCRSOrder(): "
            << "offset vector size for axis_id=" << id
            << " doesn't match number of axes!"<< std::endl;
            LEAVE(ctx << "getCRSOrder()");
            return std::vector<int>();
        }

        for (int v=0; v < offval.size(); ++v)
        {
            if (offval[v] != 0)
                order[v] = i;
        }
    }

    LEAVE(ctx << "getCRSOrder()");
    return order;
}

std::vector<std::string>
RasdamanHelper2::getMetaCrsUris(double oid)
{
    ENTER(ctx << "getMetaCrsUris()");
    std::vector<std::string> uris;



    LEAVE(ctx << "getMetaCrsUris()");
    return uris;
}

std::vector<double>
RasdamanHelper2::getMetaCellSize(double oid, bool defaultOrder)
{
    ENTER(ctx << "getMetaCellSize()");

    std::vector<double> cellsize;

    // check connection
    const PGconn* conn = this->m_pRasconn->getPetaConnection();
    if (PQstatus(conn) != CONNECTION_OK)
    {
        TALK("connection to '" << this->m_pRasconn->getPetaDbName() << "' failed!");
        LEAVE(ctx << "getMetaCellSize()");
        return cellsize;
    }

    // get the coverage id for this image
    long covid = this->getCoverageIdFromOID(oid);
    if (covid < 0)
    {
        TALK("no coverage associated with the specified image OID=" << oid);
        LEAVE(ctx << "getMetaCellSize()");
        return cellsize;
    }

    std::vector<int> crsorder;
    if (defaultOrder)
        crsorder = this->getCRSOrder(oid);

    // select any irregular grid axis of the given image
    //select distinct grid_axis_id, rasdaman_order
    //    from (select *
    //        from ps9_grid_axis
    //        where gridded_coverage_id = 126) v1
    //        join (select * from ps9_vector_coefficients) v2
    //             on v1.id = v2.grid_axis_id
    //        order by rasdaman_order desc

    std::stringstream query;
    PGresult* res;
    query << "select distinct grid_axis_id, rasdaman_order "
          <<     "from (select * "
          <<               "from " << PSPREFIX << "_grid_axis "
          <<     "where gridded_coverage_id = " << covid << ") v1 "
          <<     "join (select * from " << PSPREFIX << "_vector_coefficients) v2 "
          <<               "on v1.id = v2.grid_axis_id "
          <<     "order by rasdaman_order asc";
    TALK("'" << query.str() << "' ...");
    res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
    if (PGDATAFAILED("getMetaCellSize()", "retrieving irregular axes information failed!", res))
	{
		LEAVE(ctx << "getMetaCellSize()");
		return cellsize;
	}


    // map axis id (key) and rasdaman order of irregular axes
    std::map<int, int> irrRas;
    for (int i=0; i < PQntuples(res); ++i)
    {
        irrRas[atoi(PQgetvalue(res, i, 0))] = (atoi(PQgetvalue(res, i, 1)));
    }
    PQclear(res);
    query.str("");

    // get offset vectors for grid axis in rasdaman order
    //select id, rasdaman_order, offset_vector
    //    from (select *
    //        from ps9_grid_axis
    //        where gridded_coverage_id = 126) v1
    //        join (select * from ps9_rectilinear_axis) v2
    //             on v1.id = v2.grid_axis_id
    //        order by rasdaman_order asc
    query << "select id, rasdaman_order, offset_vector "
          <<    "from (select * "
          <<             "from " << PSPREFIX << "_grid_axis "
          <<            "where gridded_coverage_id = " << covid << ") v1 "
          <<    "join (select * from " << PSPREFIX << "_rectilinear_axis) v2 "
          <<          "on v1.id = v2.grid_axis_id "
          <<    "order by rasdaman_order asc";
    TALK("'" << query.str() << "' ...");
    res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
    if (PGDATAFAILED("getMetaCellSize()", "retrieving offset_vector information failed!", res))
	{
		LEAVE(ctx << "getMetaCellSize()");
		return cellsize;
	}

    // initiate cellsize vector with negatives, indicating
    // no valid value (e.g. for irregular axes)
    cellsize.resize(PQntuples(res));
    for (int i=0; i < cellsize.size(); ++i)
        cellsize[i] = -1;

    // fill cellsize vector (xyz axis order) with valid cell sizes;
    // skip irregular axes
    for (int i=0; i < PQntuples(res); ++i)
    {
        int id = atoi(PQgetvalue(res, i, 0));
        if (irrRas.find(id) != irrRas.end())
        {
            continue;
        }

        std::string off_str = PQgetvalue(res, i, 2);
        TALK("offsets for axis_id=" << id << " : " << off_str);

        std::vector<double> offsets;
        this->parsePGStringArray(off_str, offsets);
        if (offsets.size() < cellsize.size())
        {
            std::cerr << ctx << "getMetaCellSize(): "
            << "offset vector size for axis_id=" << id
            << " doesn't match number of axes!"<< std::endl;
            continue;
        }

        // record cellsize either in the specified crs order
        // or, if crs_order was specified, in default xyz order
        for (int v=0; v < offsets.size(); ++v)
        {
            if (offsets[v] != 0)
            {
                if (crsorder.size() > 0 && crsorder.size() == cellsize.size())
                    cellsize[crsorder[v]] = offsets[v];
                else
                    cellsize[v] = offsets[v];
            }
        }
    }

    LEAVE(ctx << "getMetaCellSize()");
    return cellsize;
}


int
RasdamanHelper2::writeRAT(const std::string& filename,
                          double oid, int band)
                          throw(r_Error)
{
    ENTER(ctx << "writeRAT()");

    // -------------------------------------------------------------
    // let's check whether we've got everything

    GDALDataset* pDs = (GDALDataset*)GDALOpen(filename.c_str(), GA_ReadOnly);
    if (pDs == 0)
    {
        std::cerr << ctx << "writeRat(): "
        << "failed opening data set '" << filename << "'!" << std::endl;
        LEAVE(ctx << "writeRAT()");
        return 0;
    }

    if (band < 1 || band > pDs->GetRasterCount())
    {
        TALK("given band number is outside available band range!");
        LEAVE(ctx << "writeRAT()");
        return 0;
    }

    GDALRasterBand* pBand = pDs->GetRasterBand(band);
    const GDALRasterAttributeTable* pRAT = pBand->GetDefaultRAT();

    if (pRAT == 0)
    {
        TALK(filename << " does not have a RAT for band #" << band
                << ", so we skip that part");
        LEAVE(ctx << "writeRAT()");
        return 0;
    }

    const PGconn* conn = this->m_pRasconn->getRasConnection();
    if (conn == 0)
    {
        std::cerr << ctx << "writeRat(): "
        << "connection with '" << this->m_pRasconn->getRasDbName() << "' failed!"
        << std::endl;
        GDALClose(pDs);
        LEAVE(ctx << "writeRAT()");
        throw r_Error(r_Error::r_Error_General);
        return 0;
    }

    // -------------------------------------------------------------

    std::stringstream query;
    query.precision(14);
    PGresult* res;

    // analyse the table structure
    int ncols = pRAT->GetColumnCount();
    int nrows = pRAT->GetRowCount();

    //std::vector< std::string > colnames;
    std::vector< ::GDALRATFieldType > coltypes;

    // the first field is going to store the row index 'rowidx', which starts
    // at 0 and is incremented for each further row; this is necessary
    // to support indexed raster layers which refer to attributes by
    // their rowindex of the associated attribute table (e.g. ERDAS IMAGINE files *.img)

    string k = ", ";
    string s = " ";

    query << "create table rat" << band << "_" << oid << " ("
          "rowidx integer unique NOT NULL,";

    int c, r;
    for (c=0; c < ncols; c++)
    {
        std::string gdalcolname = pRAT->GetNameOfCol(c);
        char* colname = PQescapeIdentifier(const_cast<PGconn*>(conn),
                gdalcolname.c_str(), gdalcolname.size());

        coltypes.push_back(pRAT->GetTypeOfCol(c));

        string typestr = "";
        switch (coltypes[c])
        {
        case GFT_Integer:
            typestr = "integer";
            break;
        case GFT_Real:
            typestr = "double precision";
            break;
        case GFT_String:
            typestr = "text";
            break;
        }

        query << colname << s << typestr << k;

        PQfreemem((void*)colname);
    }

    query << "constraint rat" << band << "_" << oid << "_pkey primary key (rowidx))";

    TALK(query.str() << " ... ");
    res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
    if (PQresultStatus(res) != PGRES_COMMAND_OK)
    {
        std::cerr << ctx << "writeRAT(): "
        << "creating raster attribute table for image '"
        << oid << "' failed: " << std::endl << PQresultErrorMessage(res)
        << std::endl;
        PQclear(res);
        LEAVE(ctx << "writeRAT()");
        return 0;
    }
    query.str("");
    PQclear(res);

    TALK("copying table content ... ");
    // copy the table body into the postgres table
    for (r=0; r < nrows; r++)
    {
        query << "insert into rat" << band << "_" << oid << " values (" << r << k;
        for (c=0; c < ncols; c++)
        {
            switch (coltypes[c])
            {
            case GFT_Integer:
                query << pRAT->GetValueAsInt(r, c);
                break;
            case GFT_Real:
                query << pRAT->GetValueAsDouble(r, c);
                break;
            case GFT_String:
                {
                    std::string valstr = pRAT->GetValueAsString(r,c);
                    char* litstr = PQescapeLiteral(const_cast<PGconn*>(conn),
                                valstr.c_str(), valstr.size());
                    query << litstr;
                    PQfreemem((void*)litstr);
                }
                break;
            }

            if (c < ncols -1)
                query << k;
            else
                query << ")";
        }

        res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
        if (PQresultStatus(res) != PGRES_COMMAND_OK)
        {
            std::cerr << ctx << "writeRAT(): "
            << "failed copying row " << r << "/" << nrows << " for table 'rat"
            << band << "_" <<
                  oid << "': " << std::endl << PQresultErrorMessage(res);
        }

        query.str("");
        PQclear(res);
    }

    // --------------------------------------------------------------
    // reference to the table is stored within the petascope db and not the
    // core rasdaman db

    // check connection
    const PGconn* petaconn = this->m_pRasconn->getPetaConnection();
    if (PQstatus(petaconn) != CONNECTION_OK)
    {
        TALK("connection to '" << this->m_pRasconn->getPetaDbName() << "' failed!");
        LEAVE(ctx << "writeRAT()");
        return 0;
    }


    // get the coverage_id from ps_rasdaman_collection
    query << "select coverage_id from " << PSPREFIX << "_range_set where storage_id = "
          <<      "(select id from " << PSPREFIX << "_rasdaman_collection where oid = "
          << oid << ")";
    TALK(query.str() << " ... ");
    res = PQexec(const_cast<PGconn*>(petaconn), query.str().c_str());
    if (PQresultStatus(res) != PGRES_TUPLES_OK)
    {
        TALK("failed identifying coverage associated with RAT 'rat"
                << band << "_" << oid << "':"
                << std::endl << PQresultErrorMessage(res));
        PQclear(res);
        LEAVE(ctx << "writeRAT()");
        return 0;
    }

    if (PQntuples(res) < 1)
    {
        TALK("could not find coverage_id for image #" << oid << " in " << PSPREFIX << "_range_set");
        LEAVE(ctx << "writeRAT()");
        PQclear(res);
        return 0;
    }
    long covid = ::atol(PQgetvalue(res, 0, 0));
    TALK("coverage id for image #" << oid << " is: " << covid);
    query.str("");
    PQclear(res);

    // get the "Raster Attribute Table" metadata_type_id for ps_extra_metadata
    query << "select id from " << PSPREFIX << "_extra_metadata_type where type = 'attrtable_name'";
    TALK(query.str() << " ...");
    res = PQexec(const_cast<PGconn*>(petaconn), query.str().c_str());
    if (PQresultStatus(res) != PGRES_TUPLES_OK)
    {
        TALK("failed identifying metadata_type_id for 'attrtable_name'"
                << "':" << std::endl << PQresultErrorMessage(res));
        PQclear(res);
        LEAVE(ctx << "writeRAT()");
        return 0;
    }

    long metatypeid = -1;
    if (PQntuples(res) < 1)
    {
        TALK("Failed identifying metadata_type_id for 'attrtable_name'!" << " Adding it now ...");
        query.str("");
        PQclear(res);

        query << "insert into " << PSPREFIX << "_extra_metadata_type (type) values ('attrtable_name')";
        res = PQexec(const_cast<PGconn*>(petaconn), query.str().c_str());
        if (PQresultStatus(res) != PGRES_COMMAND_OK)
        {
            TALK("failed inserting new metadata type 'attrtable_name!");
            PQclear(res);
            LEAVE(ctx << "writeRAT()");
            return 0;
        }
        query.str("");
        PQclear(res);

        query << "select id from " << PSPREFIX << "_extra_metadata_type where type = 'attrtable_name'";
        TALK(query.str() << " ...");
        res = PQexec(const_cast<PGconn*>(petaconn), query.str().c_str());
        if (PQresultStatus(res) != PGRES_TUPLES_OK)
        {
            TALK("failed identifying metadata_type_id after inserting it(!) for 'attrtable_name'"
                    << "':" << std::endl << PQresultErrorMessage(res));
            PQclear(res);
            LEAVE(ctx << "writeRAT()");
            return 0;
        }
    }
       metatypeid = ::atol(PQgetvalue(res,0,0));
    TALK("metadata_type_id for 'attrtable_name' = " << metatypeid);
    query.str("");
    PQclear(res);

    // write the name of the table into the ps_extra_metadata table
    query << "insert into " << PSPREFIX << "_extra_metadata (coverage_id, metadata_type_id, value) values ("
          << covid << "," << metatypeid << ", 'rat" << band << "_" << oid << "')";
    TALK(query.str() << " ... ");
    res = PQexec(const_cast<PGconn*>(petaconn), query.str().c_str());
    if (PQresultStatus(res) != PGRES_COMMAND_OK)
    {
        TALK("failed writing RAT name 'rat" << band << "_" << oid << "' into ps_extra_metadata table: " << std::endl << PQresultErrorMessage(res));
        PQclear(res);
        LEAVE(ctx << "writeRAT()");
        return 0;
    }

    PQclear(res);

    LEAVE(ctx << "writeRAT()");

    return 1;
}

/* deprecated */
int
RasdamanHelper2::getWCPSTypeId(r_Type::r_Type_Id rtype)
{
    std::string sdt = "";
    int t;
    switch (rtype)
    {
    case r_Type::CHAR:
        t = 3;
        sdt = "unsigned char";
        break;
    case r_Type::BOOL:
        t = 1;
        sdt = "boolean";
        break;
    case r_Type::ULONG:
        t = 9;
        sdt = "unsigned long";
        break;
    case r_Type::USHORT:
        t = 5;
        sdt = "unsigned short";
        break;
    case r_Type::LONG:
        t = 8;
        sdt = "long";
        break;
    case r_Type::SHORT:
        t = 4;
        sdt = "short";
        break;
    case r_Type::OCTET:
        t = 2;
        sdt = "char";
        break;
    case r_Type::DOUBLE:
        t = 11;
        sdt = "double";
        break;
    case r_Type::FLOAT:
        t = 10;
        sdt = "float";
        break;
    default:
        t = 11;
        sdt = "double";
        break;
    }

    const PGconn* conn = this->m_pRasconn->getPetaConnection();
    if (conn == 0)
    {
        std::cerr << ctx << "getWCPSTypeId(): "
        << "connection with '" << this->m_pRasconn->getPetaDbName()
        << "' failed!" << std::endl;
        return -1;
    }

    std::stringstream query;
    query << "select id from ps_datatype where datatype = '" << sdt << "'";
    PGresult* res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
    if (PQntuples(res) < 1)
    {
        std::cerr << ctx << "getWCPSTypeId(): "
        << "couldn't find data type '" << sdt << "' in ps_datatype!"
        << std::endl;
        PQclear(res);
        return -1;
    }
    t = atoi(PQgetvalue(res, 0, 0));

    return t;
}

// ToDo:: need eventually be extended to n-dim case, currently
// we put irregular axis only at axis id = 2 (rasdaman order)
long
RasdamanHelper2::canAppendReferencedZCoeff(double oid, double coefficient)
{
    ENTER(ctx << "canAppendReferencedZCoeff()");

    const PGconn* conn = this->m_pRasconn->getPetaConnection();
    if (PQstatus(conn) != CONNECTION_OK)
    {
        std::cerr << ctx << "canAppendReferencedZCoeff(): "
        << "connection to '" << this->m_pRasconn->getPetaDbName()
        << "' failed!"
        << std::endl;
        LEAVE(ctx << "canAppendReferencedZCoeff()");
        return -1;
    }

    long covid = this->getCoverageIdFromOID(oid);
    if (covid < 0)
    {
        std::cerr << ctx << "canAppendReferencedZCoeff(): "
        << "couldn't find a coverage associated with the given oid!"
        << std::endl;
        LEAVE(ctx << "canAppendReferencedZCoeff()");
        return -1;
    }

    std::stringstream query;
    PGresult* res;

    // sample query
    //select coefficient, coefficient_order from
    // (select id from ps9_grid_axis where gridded_coverage_id = 40 and rasdaman_order = 1) v1
    // join (select * from ps9_vector_coefficients) v2 on v2.grid_axis_id = v1.id;

    query << "select coefficient_order, coefficient from "
          << "(select id from " << PSPREFIX << "_grid_axis where gridded_coverage_id = "
          <<    covid << " and rasdaman_order = 2) v1 "
          << "join (select * from "<< PSPREFIX << "_vector_coefficients) v2 on v2.grid_axis_id = v1.id "
          << "order by coefficient_order desc";
    TALK("'" << query.str() << "' ...");
    res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
    if (PGDATAFAILED("canAppendReferencedZCoeff", "querying coefficients failed!", res))
	{
		LEAVE(ctx << "canAppendReferencedZCoeff");
		return -1;
	}

    long idx = -1;
    double coeff;

    if (PQntuples(res) > 0)
    {
        idx = ::atol(PQgetvalue(res, 0, 0));
        coeff = ::atof(PQgetvalue(res, 0, 1));

        if (coefficient > coeff)
            ++idx;
    }

    LEAVE(ctx << "canAppendReferencedZCoeff()");
    return idx;
}

long
RasdamanHelper2::getCoverageIdFromOID(double oid)
{
    ENTER(ctx << "getCoverageIdFromOID()");

    const PGconn* conn = this->m_pRasconn->getPetaConnection();
    if (PQstatus(conn) != CONNECTION_OK)
    {
        std::cerr << ctx << "getCoverageIdFromOID(): "
        << "connection to '" << this->m_pRasconn->getPetaDbName()
        << "' failed!"
        << std::endl;
        LEAVE(ctx << "getCoverageIdFromOID()");
        return -1;
    }

    long id = -1;

    std::stringstream query;
    PGresult* res;

    //sample query
    //select covid
    //    from (select storage_id as sid, coverage_id as covid from ps9_range_set) v1
    //        join (select id as collid, name , oid from ps9_rasdaman_collection) v2
    //          on v1.sid = v2.collid where oid = 24577;

    query << "select covid from "
          <<     "(select storage_id as sid, coverage_id as covid from "
          <<        PSPREFIX << "_range_set) v1 "
          <<     "join (select id as collid, oid from " << PSPREFIX << "_rasdaman_collection) v2 "
          <<     "on v1.sid = v2.collid where oid = " << oid;
    TALK("'" << query.str() << "' ...");
    res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
    if (PGDATAFAILED("getCoverageIdFromOID", "querying coverage id failed!", res))
	{
		LEAVE(ctx << "getCoverageIdFromOID()");
		return id;
	}

    if (PQntuples(res) > 0)
        id = ::atol(PQgetvalue(res, 0, 0));

    LEAVE(ctx << "getCoverageIdFromOID()");
    return id;
}

double
RasdamanHelper2::getOIDFromCoverageName(const std::string& coverage)
{
    ENTER(ctx << "getOIDFromCoverageName()");

    const PGconn* conn = this->m_pRasconn->getPetaConnection();
    if (PQstatus(conn) != CONNECTION_OK)
    {
        std::cerr << ctx << "getCoverageIdFromOID(): "
        << "connection to '" << this->m_pRasconn->getPetaDbName()
        << "' failed!"
        << std::endl;
        LEAVE(ctx << "getOIDFromCoverageName()");
        return -1;
    }

    double oid = -1;

    std::stringstream query;
    PGresult* res;

    // sample query
    //select oid
    //    from (select id, name from ps9_coverage
    //          where name = 'zirr5_74753') v1
    //    join (select coverage_id, storage_id from ps9_range_set) v2
    //         on v1.id = v2.coverage_id
    //    join (select id, oid from ps9_rasdaman_collection) v3
    //         on v2.storage_id = v3.id

    query << "select oid "
          <<   "from (select id, name from " << PSPREFIX << "_coverage "
          <<         "where name = '" << coverage << "') v1 "
          <<   "join (select coverage_id, storage_id from " << PSPREFIX << "_range_set) v2 "
          <<         "on v1.id = v2.coverage_id "
          <<   "join (select id, oid from " << PSPREFIX << "_rasdaman_collection) v3 "
          <<         "on v2.storage_id = v3.id";
    TALK("'" << query.str() << "' ...");
    res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
    if (PGDATAFAILED("getOIDFromCoverageName()", "querying OID failed!", res))
	{
		LEAVE(ctx << "getOIDFromCoverageName()");
		return oid;
	}

    if (PQntuples(res) > 0)
        oid = ::atol(PQgetvalue(res, 0, 0));

    LEAVE(ctx << "getOIDFromCoverageName()");
    return oid;
}

std::vector<int>
RasdamanHelper2::getRas2CrsMapping(const std::vector<int>& crsorder)
{
    std::vector<int> ras2crs (crsorder.size(), -1);

    for (int d=0; d < crsorder.size(); ++d)
    {
        for (int g=0; g < crsorder.size(); ++g)
        {
            if (crsorder[g] == d)
            {
                ras2crs[d] = g;
                break;
            }
        }
    }

    return ras2crs;
}

int
RasdamanHelper2::writePSMetadata(
                    long oid,
                    const std::string& collname,
                    const std::string& covername,
                    const std::vector<std::string>& crs,
                    const std::vector<int>& crs_order,
                    const std::string& imagetype,
                    double xmin, double xmax,
                    double ymin, double ymax,
                    double zmin, double zmax,
                    double cellsize_x,
                    double cellsize_y,
                    double cellsize_z,
                    bool isRegular,
                    long irrZPos,
                    double irregularZ)
{
    ENTER(ctx << "writePSMetadata()");

    // calc some metadata based on what we know already
    unsigned int nbands = this->getBaseTypeElementCount(collname);
    r_Minterval pixdom = this->getImageSdom(collname, oid);
    unsigned int ndims = pixdom.dimension();
    r_Range xpix = pixdom[0].get_extent();
    r_Range ypix = -1;
    r_Range zpix = -1;
    switch(ndims)
    {
    case 2:
        ypix = pixdom[1].get_extent();
        break;
    case 3:
        ypix = pixdom[1].get_extent();
        zpix = pixdom[2].get_extent();
        break;
    default:
        throw r_Error(r_Error::r_Error_LimitsMismatch);
        break;
    }

    const PGconn* conn = this->m_pRasconn->getPetaConnection();
    if (PQstatus(conn) != CONNECTION_OK)
    {
        std::cerr << ctx << "writePSMetadata(): "
        << "connection to '" << this->m_pRasconn->getPetaDbName()
        << "' failed!"
        << std::endl;
        LEAVE(ctx << "writePSMetadata()");
        return 0;
    }

    // some vars
    std::stringstream columns;
    std::stringstream values;
    values.precision(14);
    std::stringstream query;
    PGresult* res;

    ///////////////////////// PS_COVERAGE /////////////////////////////////////////
    std::string coveragename = covername;
    // if coveragename is empty, we just build one from
    // the collname and the oid concatenated by '_'
    if (coveragename.empty())
    {
        query << collname << "_" << oid;
        coveragename = query.str();
        query.str("");
    }

    // let's have a look, whether the image is already present
    // by checking the coveragename
    std::string coll = collname;
    long covid = -1;
    bool bUpdate;
    query << "select id from " << PSPREFIX << "_coverage where name = '" << coveragename << "'";
    TALK("'" << query.str() << "' ...");
    res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
    if (PQntuples(res) < 1)
    {
        bUpdate = false;
    }
    else
    {
        bUpdate = true;
        covid = ::atol(PQgetvalue(res, 0,0));
    }
    PQclear(res);
    query.str("");
    TALK("covid = " << covid);

    // query the gml_type_id
    string gmlsubtype;
    if (isRegular)
        gmlsubtype = "RectifiedGridCoverage";
    else
        gmlsubtype = "ReferenceableGridCoverage";

    query << "select id from " << PSPREFIX << "_gml_subtype where subtype = '"
          << gmlsubtype << "'";
    TALK("'" << query.str() << "' ...");
    res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
    if (PGDATAFAILED("writePSMetadata()", "failed to query gml_subtype_id!", res))
    {
        LEAVE(ctx << "writePSMetadata()");
        return 0;
    }

    long gml_type_id = ::atol(PQgetvalue(res,0,0));
    query.str("");
    PQclear(res);
    TALK("gml_type_id = " << gml_type_id);

    // query the native_format_id (mime_type_id)
    query << "select id from " << PSPREFIX << "_mime_type where mime_type = 'application/x-ogc-rasdaman'";
    res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
    if (PGDATAFAILED("writePSMetadata()", "failed to query mimetype_id!", res))
    {
        LEAVE(ctx << "writePSMetadata()");
        return 0;
    }

    long mimetype_id = ::atol(PQgetvalue(res, 0,0));
    query.str("");
    PQclear(res);
    TALK("mime_type_id = " << mimetype_id);

    // add / update ps_coverage information
    columns << "(name, gml_type_id, native_format_id)";
    values <<  "('" << coveragename << "', " << gml_type_id << ", "
            << mimetype_id << ")";
    if (!bUpdate)
    {
        query << "insert into " << PSPREFIX << "_coverage " << columns.str() << " "
              << " values " << values.str();
        TALK("'" << query.str() << "' ...");
    }
    else
    {
        query << "update " << PSPREFIX << "_coverage set " << columns.str() << " = "
              << values.str()
              << " where id = " << covid;
        TALK("'" << query.str() << "' ...");
    }
    res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
    if (PGFAILED("writePSMetadata()", "failed inserting ps_coverage data!", res))
    {
        LEAVE(ctx << "writePSMetadata()");
        return 0;
    }
    query.str("");
    PQclear(res);


    if (covid < 0)
    {
        query << "select id from " << PSPREFIX << "_coverage where name = '" << coveragename << "'";
        TALK("'" << query.str() << "' ...");
        res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
        if (PGDATAFAILED("writePSMetadata()", "failed querying ps_coverage id!", res))
        {
            LEAVE(ctx << "writePSMetadata()");
            return 0;
        }
        covid = atol(PQgetvalue(res,0,0));
        TALK("coverage id = " << covid);

        query.str("");
        PQclear(res);
    }

    //////////////////////////  PS_CRS /////////////////////////////////////////
    // iterate over the given crs_uris and determine their respective id from the table
    // if the respective crs-uri is missing, try to add it
    std::vector<long> crs_ids;// = -1;
    for (int c=0; c < crs.size(); ++c)
    {
		query << "select id from " << PSPREFIX << "_crs where uri = '" << crs[c] << "'";
		TALK("'" << query.str() << "' ... ");
		res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
		if (PGDATAFAILED("writePSMetadata", "failed to fetch the CRS URI!", res))
		{
			LEAVE(ctx << "writePSMetadata()");
			return 0;
		}

		if (PQntuples(res) >= 1)
		{
			crs_ids.push_back(atol(PQgetvalue(res, 0, 0)));
			TALK("crs_id = " << crs_ids[c]);
		}
		else
		{
			PQclear(res);
			query.str("");
			query << "insert into " << PSPREFIX << "_crs (uri) values ('" << crs[c] << "')";
			res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
			if (PGFAILED("writePSMetadata", "failed to insert CRS URI!", res))
			{
				LEAVE(ctx << "writePSMetadata()");
				return 0;
			}

			PQclear(res);
			query.str("");
			query << "select id from " << PSPREFIX << "_crs where uri = '" << crs[c] << "'";
			TALK(query.str() << " ... ");
			res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
			if (PQntuples(res) >= 1)
			{
				crs_ids.push_back(atol(PQgetvalue(res, 0, 0)));
				TALK("crs_id = " << crs_ids[c]);
			}
		}
		PQclear(res);
		query.str("");
    }

    ////////////////////////// CREATE rasdaman 2 crs order MAPPING ////////////////////////
    // we create an array to map rasdaman (default xyz) order to the specified crs order //
    std::vector<int> ras2crs = this->getRas2CrsMapping(crs_order);

    //////////////////////////// PS_DOMAIN_SET //////////////////////////////////
    // now we put the crs_id into the domain set;
    columns.str("");
    columns << "(coverage_id, native_crs_ids)";
    values.str("");
    stringstream crss;
    for (int c=0; c < crs_ids.size(); ++c)
    {
        crss << crs_ids[c];
        if (c < crs_ids.size()-1)
            crss << ", ";
    }
    values << "(" << covid << ", '{" << crss.str() << "}')";


    query << "select * from " << PSPREFIX << "_domain_set where coverage_id = " << covid;
    TALK("'" << query.str() << "' ...");
    res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
    if (PQntuples(res) < 1)
    {
        query.str("");
        PQclear(res);

        query << "insert into " << PSPREFIX << "_domain_set " << columns.str() << " values "
              << values.str();
        TALK("'" << query.str() << "'...");
        res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
        if (PGFAILED("writePSMetdata()", "failed to insert crs info into ps_domain_set!", res))
        {
            LEAVE(ctx << "writePSMetadata()");
            return 0;
        }
    }
    else
    {
        query.str("");
        PQclear(res);
        query << "update " << PSPREFIX << "_domain_set set " << columns.str() << " = "
              << values.str() << " where coverage_id = " << covid;
        TALK("'" << query.str() << "'...");
        res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
        if (PGFAILED("writePSMetadata()", "failed to update crs info into ps_domain_set!", res))
        {
            LEAVE(ctx << "writePSMetadata()");
            return 0;
        }
    }
    query.str("");
    PQclear(res);

    ////////////////////////////// PS_GRIDDED_DOMAIN_SET ///////////////////////////////
    // get min max values ordered according to crs_order
    double minmax[6]; //min0, max0, min1, max1, min2, max2;
    double csordered[3];
    long pixordered[3];

    for (int d=0; d < crs_order.size(); ++d)
    {
        if (crs_order[d] == 0)
        {
            minmax[d*2]   = xmin;
            minmax[d*2+1] = xmax;
            csordered[d] = cellsize_x;
            pixordered[d] = xpix;
        }
        else if (crs_order[d] == 1)
        {
            minmax[d*2]   = ymin;
            minmax[d*2+1] = ymax;
            csordered[d] = cellsize_y;
            pixordered[d] = ypix;
        }
        else if (crs_order[d] == 2)
        {
            minmax[d*2]    = zmin;
            minmax[d*2+1] = zmax;
            csordered[d] = cellsize_z;
            pixordered[d] = zpix;
        }
    }

    // fill in the origin of the coverage;
    // n-d origin is xmin,ymin,zmin[,?min[,?min[,...]]]
    // Todo: adjusted by pixel resolution to represent the centre of the pixel!
    columns.str("");
    columns << "(coverage_id, grid_origin)";
    values.str("");
    double origin[] = {minmax[0],// + (0.5 * xpix),
                       minmax[2],// + (0.5 * ypix),
                       minmax[4]};// + (0.5 * zpix)};
    values << "(" << covid << ", '{";// << crs_id << "}')";
    for (int d=0; d < ndims; ++d)
    {
        values << origin[d];
        if (d < ndims-1)
            values << ",";
    }
    values << "}')";

    query << "select * from " << PSPREFIX << "_gridded_domain_set where coverage_id = " << covid;
    TALK("'" << query.str() << "' ...");
    res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
    if (PQntuples(res) < 1)
    {
        query.str("");
        PQclear(res);

        query << "insert into " << PSPREFIX << "_gridded_domain_set " << columns.str() << " values "
              << values.str();
        TALK("'" << query.str() << "' ...");
        res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
        if (PGFAILED("writePSMetdata()", "failed to insert origin into ps_gridded_domain_set!", res))
        {
            LEAVE(ctx << "writePSMetadata()");
            return 0;
        }
    }
    else
    {
        query.str("");
        PQclear(res);
        query << "update " << PSPREFIX << "_gridded_domain_set set " << columns.str() << " = "
              << values.str() << " where coverage_id = " << covid;
        TALK("'" << query.str() << "' ...");
        res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
        if (PGFAILED("writePSMetadata()", "failed to update origin info in ps_gridded_domain_set!", res))
        {
            LEAVE(ctx << "writePSMetadata()");
            return 0;
        }
    }
    query.str("");
    PQclear(res);


    ////////////////////////////// PS_BOUNDING_BOX ///////////////////////////////
    columns.str("");
    columns << "(coverage_id, lower_left, upper_right)";
    values.str("");
    std::string arclop = "}','{";
    std::string valcl = "}')";
    std::string comma = ",";
    values << "(" << covid << ", '{";

    // we also prepare the offset vector for the ps_rectilinear_axis table here;
    // determine cell sizes first; and note that since origin[1] = ymin ! (we put
    // the origin at the lower left (for 2D) pixel corner) the y-offset
    // component is positive

    // if we've got some pixel for a given axis imported yet, we work out the
    // cellsize based on the boundary and the number of pixel, otherwise,
    // we use the values determined by the calling application
    double cs0 = pixordered[0] >= 0 ? (abs(minmax[1] - minmax[0])) / (double)pixordered[0] : csordered[0];
    double cs1 = pixordered[1] >= 0 ? (abs(minmax[3] - minmax[2])) / (double)pixordered[1] : csordered[1];
    double cs2 = pixordered[2] >= 0 ? (abs(minmax[5] - minmax[4])) / (double)pixordered[2] : csordered[2];

    // ToDo:: check in conjunction with populating ps_rectilinear axis upon image insertion
    //        (not relevant for update)
    //        for an irregular z-axis, replace cell size value with offset to the first z-coordinate
    if (!isRegular)
    {
        csordered[ras2crs[2]] = irregularZ;
    }

    std::vector<std::string> voffsets;
    voffsets.resize(ndims);
    std::stringstream voffstr;
    voffstr.precision(14);

    switch(ndims)
    {
    case 1:
        values << minmax[0] << arclop << minmax[1] << valcl;
        // x off-set
        voffstr << "'{" << csordered[0] << "}'";
        voffsets[0] = voffstr.str();
        voffstr.str("");
        break;
    case 2:
        values << minmax[0] << comma << minmax[2] << arclop
               << minmax[1] << comma << minmax[3] << valcl;

        // x off-set
        voffstr << "'{" << csordered[0] << comma << 0 << "}'";
        voffsets[0] = voffstr.str();
        voffstr.str("");

        // y off-set
        voffstr << "'{" << 0 << comma << csordered[1] << "}'";
        voffsets[1] = voffstr.str();
        voffstr.str("");


        break;
    case 3:
        values << minmax[0] << comma << minmax[2] << comma << minmax[4] << arclop
               << minmax[1] << comma << minmax[3] << comma << minmax[5] << valcl;

        // 0-dim (x) off-set
        voffstr << "'{" << csordered[0] << comma << 0 << comma << 0 << "}'";
        voffsets[0] = voffstr.str();
        voffstr.str("");

        // 1-dim (y) off-set
        voffstr << "'{" << 0 << comma << csordered[1] << comma << 0 << "}'";
        voffsets[1] = voffstr.str();
        voffstr.str("");

        // 2-dim (z) off-set
        voffstr << "'{" << 0 << comma << 0 << comma << csordered[2] << "}'";
        voffsets[2] = voffstr.str();
        voffstr.str("");

        break;
    default:
        TALK("rasgeo doesn't support coverages with " << ndims << "dimensions!");
        LEAVE(ctx << "writePSMetadata()");
        throw r_Error(r_Error::r_Error_LimitsMismatch);
        break;
    }

    query << "select * from " << PSPREFIX << "_bounding_box where coverage_id = " << covid;
    TALK("'" << query.str() << "' ...");
    res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
    if (PQntuples(res) < 1)
    {
        query.str("");
        PQclear(res);

        query << "insert into " << PSPREFIX << "_bounding_box " << columns.str()
              << " values "<< values.str();
        TALK("'" << query.str() << "' ...");
        res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
        if (PGFAILED("writePSMetdata()", "failed to insert bbox into ps_bounding_box!", res))
		{
			LEAVE(ctx << "writePSMetadata()");
			return 0;
		}

    }
    else
    {
        query.str("");
        PQclear(res);
        query << "update " << PSPREFIX << "_bounding_box set " << columns.str() << " = "
              << values.str() << " where coverage_id = " << covid;
        TALK("'" << query.str() << "' ...");
        res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
        if (PGFAILED("writePSMetadata()", "failed to update bbox in ps_bounding_box!", res))
		{
			LEAVE(ctx << "writePSMetadata()");
			return 0;
		}
    }
    query.str("");
    PQclear(res);

    ////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////// MOSTLY NOT FOR UPDATE ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////  PS_RASDAMAN_COLLECTION  //////////////////////
    if (!bUpdate)
    {
        columns.str("");
        columns << "(name, oid, base_type)";
        values.str("");
        values << "('" << collname << "'," << oid << ",'" << imagetype << "')";

        long storageid = -1;

        query << "insert into " << PSPREFIX << "_rasdaman_collection " << columns.str()
                << " values " << values.str();
        TALK("'" << query.str() << "' ...");
        res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
        if (PGFAILED("writePSMetdata()", "failed to insert rasdaman collection info into ps_rasdaman_collection!", res))
		{
			LEAVE(ctx << "writePSMetadata()");
			return 0;
		}

        PQclear(res);
        query.str("");
        query << "select id from " << PSPREFIX << "_rasdaman_collection where oid = " << oid;
        TALK("'" << query.str() << "' ...");
        res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
        if (PGDATAFAILED("writePSMetdata()", "failed to query rasdaman collection info we just inserted!", res))
		{
			LEAVE(ctx << "writePSMetadata()");
			return 0;
		}

        storageid = atol(PQgetvalue(res, 0, 0));
        TALK("storage_id = " << storageid);

        PQclear(res);
        query.str("");

        //////////////////////////////////////// PS_RANGE_SET /////////////////////////////////////
        columns.str("");
        columns << "(coverage_id, storage_table, storage_id)";
        values.str("");
        values << " (" << covid << ",'" << PSPREFIX << "_rasdaman_collection', " << storageid
                << ")";

        long rangesetid = -1;
        query << "insert into " << PSPREFIX << "_range_set " << columns.str() << " values "
                << values.str();
        TALK("'" << query.str() << "' ...");
        res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
        if (PGFAILED("writePSMetdata()", "failed to insert into ps_range_set!", res))
		{
			LEAVE(ctx << "writePSMetadata()");
			return 0;
		}

        PQclear(res);
        query.str("");
        query << "select id from " << PSPREFIX << "_range_set where storage_id = "
                << storageid;
        TALK("'" << query.str() << "' ...");
        res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
        if (PGDATAFAILED("writePSMetdata()", "failed to query ps_range_set info we just inserted!", res))
		{
			LEAVE(ctx << "writePSMetadata()");
			return 0;
		}

        rangesetid = atol(PQgetvalue(res, 0, 0));
        TALK("range_set_id = " << rangesetid);

        PQclear(res);
        query.str("");


        ////////////////////////////////////// PS_GRID_AXIS //////////////////////////////////////
        columns.str("");
        columns << "(gridded_coverage_id, rasdaman_order)";
        // create a mapping index to insure we insert axis in the right order (dim-0 first)
        for (int d = 0; d < ndims; ++d)
        {
            query.str("");
            query << "insert into " << PSPREFIX << "_grid_axis " << columns.str()
                    << " values (" << covid << "," << d << ")";
            TALK("'" << query.str() << "' ...");
            res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
            if (PGFAILED("writePSMetdata()", "failed to insert into into ps_grid_axis!", res))
            {
                LEAVE(ctx << "writePSMetadata()");
                return 0;
            }
        }
        PQclear(res);
        query.str("");
    }

    query << "select id from " << PSPREFIX << "_grid_axis where gridded_coverage_id = "
          << covid << " order by rasdaman_order asc";
    TALK("'" << query.str() << "' ...");
    res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
    if (PGDATAFAILED("writePSMetdata()", "failed to query ps_grid_axis ids!", res))
	{
		LEAVE(ctx << "writePSMetadata()");
		return 0;
	}

    // get axis ids and order them according to crs_order
    long axisids[3];
    long aid;
    for (int d = 0; d < ndims; ++d)
    {
        aid = atol(PQgetvalue(res, d, 0));
        axisids[ras2crs[d]] = aid;
    }
    PQclear(res);
    query.str("");

    /////////////////////////////// PS_RECTILINIEAR_AXIS ////////////////////////////////////
    // ToDo:: check whether irregular axis treatment here is ok: as offset for the irregular
    //        axis, we set the first z-coordinate along this axis (s. also population of voffstr
    //        above)

    if (!bUpdate)
    {

        columns.str("");
        columns << "(grid_axis_id, offset_vector)";
        for (int d = 0; d < ndims; ++d)
        {
            columns.str("");
            columns << "(grid_axis_id, offset_vector)";

            query << "insert into " << PSPREFIX << "_rectilinear_axis " << columns.str()
                      << " values (" << axisids[ras2crs[d]] << "," << voffsets[ras2crs[d]] << ")";
            TALK("'" << query.str() << "' ...");
            res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
            if (PGFAILED("writePSMetdata()", "failed to insert info into ps_rectilinear_axis!", res))
            {
                LEAVE(ctx << "writePSMetadata()");
                return 0;
            }
            PQclear(res);
            query.str("");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    // NEED TO UPDATE THIS ONE
    /////////////////////////////// PS_VECTOR_COEFFICIENTS /////////////////////////////////

    // we can only have the z-axis being irregular, so we look for the ordered axis index = 2
    columns.str("");
    columns << "(grid_axis_id, coefficient, coefficient_order)";
    for (int d = 0; d < ndims; ++d)
    {
        // only irregular axes
        if (!isRegular && crs_order[d] == 2)
        {
            query << "insert into " << PSPREFIX << "_vector_coefficients " << columns.str()
                      << " values (" << axisids[d] << "," << irregularZ
                      << ", " << irrZPos << ")";

            TALK("'" << query.str() << "' ...");
            res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
            if (PGFAILED("writePSMetdata()", "failed to insert info into ps_vector_coefficients!", res))
            {
                LEAVE(ctx << "writePSMetadata()");
                return 0;
            }
            PQclear(res);
            query.str("");
        }
    }

    if (!bUpdate)
    {
        /////////////////////////////// PS_UOM /////////////////////////////////////////////////
        // we treat everything as 'undefined' for now ...
        // this might come with the meta data
        long undef_uom_id = -1;

        query << "select id from " << PSPREFIX << "_uom where code = 'undefined'";
        TALK("'" << query.str() << "' ...");
        res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
        if (PQntuples(res) < 1)
        {
            PQclear(res);
            query.str("");
            query << "insert into " << PSPREFIX << "_uom (code) values ('undefined')";
            res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
            if (PGFAILED("writePSMetadata()", "failed to insert 'undefined' into ps_uom!", res))
            {
                LEAVE(ctx << "writePSMetadata()");
                return 0;
            }
            PQclear(res);
            query.str("");

            query << "select id from " << PSPREFIX << "_uom where code = 'undefined'";
            res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
        }
        undef_uom_id = atol(PQgetvalue(res, 0, 0));
        query.str("");
        PQclear(res);

        ///////////////////////////////// PS_QUANTITY ////////////////////////////////////////////
        // for now all coverage range_type_components are going to denote 'pixel_value's ....
        long quantity_id = -1;

        query << "select id from " << PSPREFIX << "_quantity where label = 'pixel_value'";
        TALK("'" << query.str() << "' ...");
        res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
        if (PQntuples(res) < 1)
        {
            PQclear(res);
            query.str("");
            query << "insert into " << PSPREFIX << "_quantity (uom_id, label, description) "
                  << " values (" << undef_uom_id << ", 'pixel_value', '')";
            res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
            if (PGFAILED("writePSMetadata()", "failed to insert info into ps_quantity!", res))
            {
                LEAVE(ctx << "writePSMetadata()");
                return 0;
            }
            PQclear(res);
            query.str("");

            query << "select id from " << PSPREFIX << "_quantity where label = 'pixel_value'";
            res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
            if (PGDATAFAILED("writePSMetadata()", "failed querying id for label = 'pixel_value'!", res))
            {
                LEAVE(ctx << "writePSMetadata()");
                return 0;
            }
        }
        quantity_id = atol(PQgetvalue(res, 0, 0));
        query.str("");
        PQclear(res);

        ///////////////////////////////////// PS_RANGE_TYPE_COMPONENT ///////////////////////
        for (unsigned int c=0; c < nbands; ++c)
        {
            // get type name for the current component
            r_Type::r_Type_Id rtype = this->getBaseTypeId(collname, c);
            std::string range_data_type = this->getDataTypeString(rtype);
            std::transform(range_data_type.begin(), range_data_type.end(),
                           range_data_type.begin(), ::tolower);

            if (range_data_type == "signed char")
            {
                range_data_type = "char";
            }
            TALK("type of component #" << c << ": " << range_data_type);

            // adjust singed char label;
            // note: rasdl defines 'signed char' and 'unsigned char'
            // instead of 'char' and 'unsigned char'
            long range_data_type_id = -1;
            query << "select id from " << PSPREFIX << "_range_data_type where name = '"
                  << range_data_type << "'";
            TALK("'" << query.str() << "' ...");
            res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
            if (PQntuples(res) < 1)
            {
                std::cerr << ctx << "writePSMetadata(): failed querying range component type!"
                        << std::endl;
                PQclear(res);
                LEAVE(ctx << "writePSMetadata()");
                return 0;
            }
            range_data_type_id = atol(PQgetvalue(res, 0, 0));
            TALK("range_type_id for '" << range_data_type << "' = " << range_data_type_id);
            query.str("");
            PQclear(res);

            columns.str("");
            columns << "(coverage_id, name, data_type_id, component_order, field_id, field_table)";

            query << "insert into " << PSPREFIX << "_range_type_component " << columns.str()
                  << " values (" << covid << comma << "'b" << c+1 << "', "
                  << range_data_type_id << comma << c << comma << quantity_id
                  << comma << "'" << PSPREFIX << "_quantity')";
            TALK("'" << query.str() << "' ...");
            res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
            if (PGFAILED("writePSMetadata()", "failed writing range_type_component!", res))
            {
                LEAVE(ctx << "writePSMetadata()");
                return 0;
            }
            PQclear(res);
            query.str("");
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////// END OF MOSTLY 'NOT FOR UPDATE' /////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////

    LEAVE(ctx << "writePSMetadata()");
    return 1;
}

int
RasdamanHelper2::deletePSMetadata(const std::string& collname,
                                  double oid)
{
    ENTER(ctx << "::deletePSMetadata()");

    // in case the oid is negative we fetch all oids for this
    // collection and delete metadata for all images!
    std::vector<double> oids;
    if (oid < 0)
    {
        TALK("no valid OID given, fetching OIDs for collection ...");
        oids = this->getImageOIDs(collname);
    }
    else
    {
        oids.push_back(oid);
    }

    // establish connection
    const PGconn* conn = this->m_pRasconn->getPetaConnection();
    if (PQstatus(conn) != CONNECTION_OK)
    {
        std::cerr << ctx << "::deletePSMetadata(): "
        << "connection with '" << this->m_pRasconn->getPetaDbName()
        << "' failed!" << std::endl;
        LEAVE(ctx << "::deletePSMetadata()");
        return 0;
    }

    std::stringstream query;
    PGresult* res;


    for (int i=0; i < oids.size(); ++i)
    {
        oid = oids[i];

        // get the coverage (petascope) id
        query << "select coverage_id from " << PSPREFIX << "_range_set where storage_id = "
                << "(select id from " << PSPREFIX << "_rasdaman_collection where oid = "
                   << oid << ")";
        TALK("'" << query.str() << "' ...");
        res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
        if (PQntuples(res) < 1)
        {
            std::cerr << ctx << "deletePSMetadata(): "
                      << "failed querying coverage id for OID=" << oid << " !"
                      << std::endl;
            LEAVE(ctx << "::deletePSMetadata()");
            PQclear(res);
            return 0;
        }
        long covid = atol(PQgetvalue(res, 0, 0));
        PQclear(res);

        // delete covid referenced information
        query.str("");
        query << "delete from " << PSPREFIX << "_coverage where id = " << covid;
        res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
        if (PGFAILED("deletePSMetadata()", "failed deleting coverage metadata!", res))
		{
			LEAVE(ctx << "deletePSMetadata()");
			return 0;
		}
        PQclear(res);

        // delete reference to rasdaman collection
        query.str("");
        query << "delete from " << PSPREFIX << "_rasdaman_collection where oid = " << oid;
        res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
        if (PGFAILED("deletePSMetadata()", "failed deleting coverage metadata!", res))
		{
			LEAVE(ctx << "deletePSMetadata()");
			return 0;
		}
        PQclear(res);
        query.str("");

    }

    LEAVE(ctx << "::deletePSMetadata()");
    return 1;
}


/////////////////////////////////////////////////////////////////////////
// OLD SCHEMA
/////////////////////////////////////////////////////////////////////////
std::string
RasdamanHelper2::getMetaCrsName(double oid)
{
    ENTER(ctx << "getMetaCrsName()");

    // if we can't find any crs description, we return the petascope descr for
    // the pixel-based crs
    std::string crs_name = "CRS:1";

    //const PGconn* conn = this->m_pRasconn->getPetaConnection();
    //if (conn == 0)
    //{
    //    std::cerr << ctx << "getMetaCrsName(): "
    //    << "connection with '" << this->m_pRasconn->getPetaDbName() << "' failed!";
    //    return crs_name;
    //}

    // TODO: needs proper implementation!
    //std::stringstream query;
    //query << "select id from " << PSPREFIX << "_crs where id = "
    //      << "(select native"
    //      "from nm_meta where img_id = " << oid;
    //PGresult* res = PQexec(const_cast<PGconn*>(conn), query.str().c_str());
    //if (PQntuples(res) < 1)
    //{
    //    TALK("could not find oid #" << oid << " in nm_meta!" << std::endl);
    //    LEAVE(ctx << "getNMMetaCrsName()");
    //    PQclear(res);
    //    return crs_name;
    //}
    //
    //crs_name = PQgetvalue(res, 0, 0);
    //TALK("crs_name: " << crs_name);

    LEAVE(ctx << "getNMMetaCrsName()");
    return crs_name;
}


bool RasdamanHelper2::checkDbConnection(void)
{
    bool bhealthy = true;

    try
    {
        this->m_pRasconn->connect();
        this->m_transaction.begin();
        this->m_transaction.commit();
    }
    catch (r_Error& re)
    {
        bhealthy = false;
    }

    return bhealthy;
}

std::string
RasdamanHelper2::getDataTypeString(r_Type::r_Type_Id type)
{
    string stype;
    switch (type)
    {
    case r_Type::BOOL:
        stype = "boolean";
        break;
    case r_Type::CHAR:
        stype = "unsigned char";
        break;
    case r_Type::ULONG:
        stype = "unsigned long";
        break;
    case r_Type::USHORT:
        stype = "unsigned short";
        break;
    case r_Type::LONG:
        stype = "long";
        break;
    case r_Type::SHORT:
        stype = "short";
        break;
    case r_Type::OCTET:
        stype = "signed char";
        break;
    case r_Type::DOUBLE:
        stype = "double";
        break;
    case r_Type::FLOAT:
        stype = "float";
        break;
    default:
        stype = "unknown";
        break;
    }
    return stype;
}

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

#ifndef _D_COLLECTION_
#define _D_COLLECTION_

#include "rasodmg/object.hh"

template <class T>
class r_Iterator;

//@ManMemo: Module: {\bf rasodmg}
/**
  * \ingroup Rasodmgs
  */

/**
 Basic class of a collection. Possible subclasses are r_Set. The protected
 members isOrdered and allowsDuplicates are not initialized here, they have to
 be initialized in the respective subclasses.
*/
template <class T>
class r_Collection : public r_Object
{
    friend class r_Iterator<T>;

public:
    /// default constructor
    r_Collection();
    /// copy constructor
    r_Collection(const r_Collection<T> &collection);
    /// virtual destructor
    virtual ~r_Collection();

    /// it is called when an object leaves transient memory
    virtual void r_deactivate();

    /// get number of elements
    unsigned long cardinality() const;

    /// tells if the collection is empty or not
    bool is_empty() const;
    /// tells if the collection is ordered or not
    bool is_ordered() const;
    /// tells if the collections allowes duplicates or not
    bool allows_duplicates() const;

    /// asks about the containment of a specific element
    bool contains_element(const T &element) const;
    /// inserts an alement at the beginning
    virtual void insert_element(const T &element, int no_modification = 0);
    /**
      The method inserts an element into the collection. If <tt>no_modification</tt>
      is set, the <tt>mark_modified()</tt> method of r_Object is not invoked and, therefore,
      a modification will not be recognized at the commit point.
    */
    /// removes an element
    virtual void remove_element(const T &element);
    /// removes all elements
    void remove_all();

    /// assignment operator
    const r_Collection<T> &operator=(const r_Collection<T> &collection);

    /// create an iterator pointing at the first element in the collection
    r_Iterator<T> create_iterator();

    /// get base type schema
    const r_Type *get_element_type_schema();

    //@Man: Methods for database communication (internal use only):
    //@{
    ///

    /// insert myself into the database
    virtual void insert_obj_into_db();

    /// inserts an object into a specific collection in the database
    virtual void insert_obj_into_db(const char *)
    {
        ;
    };
    /**
      The method has no functionality in this class. It supposed to be removed in
      future.
    */

    /// update myself
    virtual void update_obj_in_db();

    ///
    //@}

protected:
    ///
    struct CNode
    {
        CNode *next;
        T *elem;
    };

    //@Man: Methods/Attributes for maintainance of removed objects
    //@{
    ///

    /// create an iterator for removed objects
    r_Iterator<T> create_removed_iterator();

    ///
    /// pointer to list of removed elements
    CNode *removed_objects;

    ///
    //@}

    //@Man: Attributes storing some state information
    //@{
    ///
    ///
    int isOrdered;
    ///
    int allowsDuplicates;
    ///
    unsigned long card;
    ///
    //@}

    // Normally, we would have used the following class to implement the
    // internal structure of a r_Collection. But for RPC's sake, we use
    // a simple structure. (RPC is not able to transfer C++ objects.)
    //
    // template <class T>
    // class CNode
    // {
    //  public:
    //    CNode();
    //    CNode( const CNode& cnode );
    //    ~CNode();
    //
    //    inline CNode* get_next() const;
    //    inline void set_next( const CNode& cnode );
    //    inline T* get_elem() const;
    //    inline void set_elem( const T& element);
    //
    //  private:
    //    CNode* next;
    //    T* elem;
    // }

    //@Man: Methods for manipulating CNode lists
    //@{
    ///

    ///
    void add_node(CNode *&root, const T &element);
    ///
    bool remove_node(CNode *&root, const T &element);
    ///
    void remove_all_nodes(CNode *&root);
    ///
    void init_node_list(CNode *&root);

    ///
    //@}

    /// pointer to collection elements
    CNode *coll;
};

class r_GMarray;
extern template class r_Collection<r_GMarray *>;
class r_Ref_Any;
extern template class r_Collection<r_Ref_Any>;

template <typename T>
class r_Ref;
class r_Object;
extern template class r_Collection<r_Ref<r_Object>>;
extern template class r_Collection<r_Ref<r_GMarray>>;

struct GenRefElement;
extern template class r_Collection<GenRefElement *>;

#endif

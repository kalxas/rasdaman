/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2012 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package secore.db;

import secore.util.SecoreException;

/**
 * The core logic uses this interface to access a database storing the GML data.
 *
 * @author Dimitar Misev
 */
public interface Database {
  
  /**
   * Query the database
   * @param query XQuery query
   * @return a the result as a string
   * @throws Exception in case of an error in the query evaluation
   */
  String query(String query) throws SecoreException;
  
  /**
   * Query the EPSG database
   * @param query XQuery query
   * @return a the result as a string
   * @throws Exception in case of an error in the query evaluation
   */
  String queryEpsg(String query) throws SecoreException;
  
  /**
   * Query the user-defined database
   * @param query XQuery query
   * @return a the result as a string
   * @throws Exception in case of an error in the query evaluation
   */
  String queryUser(String query) throws SecoreException;
  
  /**
   * Submit query that updates the database
   * @param query XQuery query
   * @param db DBManager name
   * @return a the result as a string
   * @throws Exception in case of an error in the query evaluation
   */
  String updateQuery(String query, String db) throws SecoreException;
}

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

/**
 * A key value pair in string which store the key (versionNumber) and pair(the
 * collection name) e.g: pair(8.5, gml_85) then with the CRS has version 8.5, it
 * will query in GML collection name gml_85
 *
 * @author Bang Pham Huu
 */
public class DbCollection {

  // the version number (e.g: gml_0, 0, 8.5, 8.92)
  private String versionNumber;
  // the collection name (e.g: userdb, gml_0, gml_85, gml_892)
  private String collectionName;

  public DbCollection(String versionNumber, String collectionName) {
    this.versionNumber = versionNumber;
    this.collectionName = collectionName;
  }

  /**
   * get version number (e.g: 8.5)
   *
   * @return
   */
  public String getVersionNumber() {
    return versionNumber;
  }

  /**
   * get collection name (e.g: gml_85)
   *
   * @return
   */
  public String getCollectionName() {
    return collectionName;
  }

  public void setVersionNumber(String key) {
    this.versionNumber = key;
  }

  public void setCollectionName(String value) {
    this.collectionName = value;
  }

  @Override
  public int hashCode() {
    if (this.versionNumber == null) {
      return (this.collectionName == null) ? 0 : this.collectionName.hashCode() + 1;
    } else if (this.collectionName == null) {
      return this.versionNumber.hashCode() + 2;
    } else {
      return this.versionNumber.hashCode() * 17 + this.collectionName.hashCode();
    }
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof DbCollection
        && equals(versionNumber, ((DbCollection) obj).getVersionNumber())
        && equals(collectionName, ((DbCollection) obj).getCollectionName());
  }

  private static boolean equals(Object x, Object y) {
    return (x == null && y == null) || (x != null && x.equals(y));
  }
}

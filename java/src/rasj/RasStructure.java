package rasj;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.*;
import org.odmg.DList;
import rasj.odmg.RasList;
import rasj.rnp.RasRNPImplementation;

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
 **
 * *********************************************************
 * <pre>
 *
 * PURPOSE:
 * This class represents an struct datatype.
 *
 *
 *
 * COMMENTS:
 *
 * </pre> **********************************************************
 */
public class RasStructure {

  private DList elements;
  private RasStructureType type;

  /**
   * Constructor getting the type and input stream from which to read the
   * values.
     *
   */
  public RasStructure(RasStructureType type, DataInputStream dis)
          throws IOException, RasResultIsNoIntervalException {
    this.type = type;
    this.elements = new RasList();

    RasBaseType[] bts = type.getBaseTypes();
    for (RasBaseType bt : bts) {
      if (bt != null) {
        elements.add(RasRNPImplementation.getElement(dis, bt, null));
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder ret = new StringBuilder("{");
    int i = 0;
    for (Object element : elements) {
      if (element == null) {
        continue;
      }
      if (i > 0) {
        ret.append(",");
      }
      ret.append(" ").append(element.toString());
      i = 1;
    }
    ret.append(" }");
    return ret.toString();
  }
}

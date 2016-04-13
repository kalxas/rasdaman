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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.translator;

/**
 * Translator class for complex numbers.
 *
 * <code>
 *   (2,4)
 * </code>
 *
 * translates to
 *
 * <code>
 *   complex(2,5)
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class ComplexNumberConstant extends IParseTreeNode {

  public ComplexNumberConstant(String re, String im) {
    this.re = re;
    this.im = im;
  }

  @Override
  public String toRasql() {
    return TEMPLATE.replace("$re", this.re).replace("$im", this.im);
  }

  private final String re;
  private final String im;
  private final static String TEMPLATE = "complex($re, $im)";
}

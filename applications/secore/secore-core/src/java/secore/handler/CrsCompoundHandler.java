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
package secore.handler;

import secore.req.ResolveResponse;
import secore.req.ResolveRequest;
import secore.util.Config;
import secore.util.StringUtil;
import secore.util.SecoreException;
import secore.util.ExceptionCode;
import java.net.URL;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import secore.Resolver;
import secore.req.RequestParam;
import secore.util.Constants;
import static secore.util.Constants.*;

/**
 * Performs CRS combining into a compound CRS.
 *
 * @author Dimitar Misev
 */
public class CrsCompoundHandler extends AbstractHandler {

  private static Logger log = LoggerFactory.getLogger(CrsCompoundHandler.class);

  public ResolveResponse handle(ResolveRequest request) throws SecoreException {
    log.debug("Handling resolve request...");

    List<RequestParam> params = request.getParams();

    if (request.getOperation().equals(getOperation()) && params.size() >= 1) {

      // component CRS URIs
      List<RequestParam> components = request.getParams();

      // do some checking first, whether they are existing references
      String name = EMPTY;
      String comp = EMPTY;
      String code = EMPTY;
      int i = 0;
      for (RequestParam component : components) {

        // NOTE: Compound CRS does not have parameters expand or format
        if (component.val.toString().contains(GeneralHandler.EXPAND_KEY) || component.val.toString().contains(GeneralHandler.FORMAT_KEY)) {
          throw new SecoreException(ExceptionCode.InvalidParameterValue, "Compound CRS is not allowed to add expand/format parameter(s).");
        }

        if (!(component.val instanceof ResolveRequest)) {
          throw new SecoreException(ExceptionCode.InvalidParameterValue.locator(component.key),
              "Invalid parameter value received for " + component.key + ": " + component.val);
        }
        ++i;
        if (component.key == null || !String.valueOf(i).equals(component.key)) {
          throw new SecoreException(ExceptionCode.InvalidParameterValue,
              "Invalid " + getOperation() + " request, expected number " + i
              + " as key for parameter, but was " + component.key);
        }
        String res = null;
        ResolveRequest req = (ResolveRequest) component.val;
        if (req.getOperation().equals(Handler.OP_CRS_COMPOUND)) {
          throw new SecoreException(ExceptionCode.InvalidParameterValue.locator(component.key),
              "Expected URL to simple definition as parameter value of " + component.key
              + ", but got a URL to a compound CRS");
        }
        if (req.isLocal()) {
          res = Resolver.resolve(req).getData();
        } else {
          try {
            res = Resolver.resolve(new URL(req.getOriginalRequest())).getData();
          } catch (Exception ex) {
            log.error("Failed resolving CRS definition: " + component, ex);
            throw new SecoreException(ExceptionCode.NoSuchDefinition,
                "Failed resolving CRS definition: " + component, ex);
          }
        }
        if (res.equals(EMPTY)) {
          throw new SecoreException(ExceptionCode.NoSuchDefinition,
              "Invalid CRS definition received for " + component);
        }
        if (!name.equals(EMPTY)) {
          name += " / ";
        }
        name += StringUtil.getElementValue(res, NAME_LABEL);
        String id = StringUtil.getElementValue(res, IDENTIFIER_LABEL);
        if (id == null) {
          throw new SecoreException(ExceptionCode.XmlNotValid,
              "Invalid CRS definition received for " + component);
        }
        comp += "   <componentReferenceSystem xlink:href='" + id + "'/>\n";
        code += "-" + id.substring(id.lastIndexOf(':') + 1);
      }

      String res
          = "<CompoundCRS xmlns:gml='" + NAMESPACE_GML + "'\n"
          + "   xmlns:epsg='" + NAMESPACE_EPSG + "'\n"
          + "   xmlns:xlink='" + NAMESPACE_XLINK + "'\n"
          + "   gml:id='crs'>\n"
          + "   <metaDataProperty>\n"
          + "      <epsg:CommonMetaData>\n"
          + "         <epsg:type>compound</epsg:type>\n"
          + "      </epsg:CommonMetaData>\n"
          + "   </metaDataProperty>\n"
          + "   <scope>not known</scope>\n"
          + "   <identifier codeSpace='" + Config.getInstance().getCodespace()
          + "'>" + request.getOriginalRequest().replaceAll("&", "%26") + "</identifier>\n"
          + "   <name>" + name + "</name>\n" + comp
          + "</CompoundCRS>";
      log.debug("Done, returning response.");
      return new ResolveResponse(res);
    } else {
      log.error("Can't handle the given parameters, exiting with error.");
      throw new SecoreException(ExceptionCode.MissingParameterValue,
          "Insufficient parameters provided");
    }
  }

  private void checkCrsRef(String crsRef) throws SecoreException {
    if (!crsRef.contains(StringUtil.SERVLET_CONTEXT + "/crs")) {
      log.error("Invalid crs-compound request, expected a CRS reference, but got " + crsRef);
      throw new SecoreException(ExceptionCode.InvalidParameterValue,
          "Invalid " + getOperation() + " request, expected a CRS reference, but got " + crsRef);
    }
  }

  public String getOperation() {
    return OP_CRS_COMPOUND;
  }
}

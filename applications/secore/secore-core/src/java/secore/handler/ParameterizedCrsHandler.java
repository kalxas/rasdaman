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
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.HashMap;
import secore.util.SecoreException;
import secore.util.ExceptionCode;
import secore.util.StringUtil;
import secore.util.XMLUtil;
import java.util.ArrayList;
import java.util.regex.Pattern;
import net.n3.nanoxml.IXMLElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import secore.db.DbManager;
import static secore.handler.ParameterizedCrsHandler.*;
import secore.req.RequestParam;
import static secore.util.Constants.*;
import secore.util.Pair;
import secore.util.SecoreUtil;


/**
 * Handle parameterized CRSs. An example of a parameterized CRS is the
 * auto universal transverse mercator layer CRS (AUTO:42001):
 *
  <ParameterizedCRS>
    <parameters>
        <parameter name="lon"/>
        <parameter name="lat">
            <value>0.0</value>
        </parameter>
        <parameter name="zone">
            <value>min( floor( (${lon} + 180.0) / 6.0 ) + 1, 60 )</value>
        </parameter>
        <parameter name="central_meridian">
            <value>-183.0 + ${zone} * 6.0</value>
            <target>//greenwichLongitude</target>
        </parameter>
        <parameter name="false_northing">
            <value>(${lat} >= 0.0) ? 0.0 : 10000000.0</value>
            <target>//falseNorthing</target>
        </parameter>
    </parameters>
    <identifier>http://www.opengis.net/def/crs/AUTO/1.3/42001</identifier>
    <targetCRS xlink:href="http://www.opengis.net/def/crs/EPSG/0/4326"/>
  </ParameterizedCRS>

 * targetCRS holds the CRS that this template refers to.
 * <p>
 * This class first delegates the handling to the {@link GeneralHandler} and then
 * continues if the result is a ParameterizedCRS or returns.
 *
 * @author Dimitar Misev
 */
public class ParameterizedCrsHandler extends GeneralHandler {

  private static Logger log = LoggerFactory.getLogger(ParameterizedCrsHandler.class);

  // element names in a parameterized CRS GML definition
  public static final String PARAMETERIZED_CRS = "ParameterizedCRS";
  public static final String PARAMETERS = "parameters";
  public static final String PARAMETER = "parameter";
  public static final String PARAMETER_NAME = "name";
  public static final String PARAMETER_VALUE = "value";
  public static final String PARAMETER_TARGET = "target";
  public static final String TARGET_CRS = "targetCRS";
  public static final String TARGET_CRS_HREF = "href";

  // the definition of the parameterized CRS may be resolved previously
  // by the general handler.
  private ResolveResponse definition = null;

  @Override
  public boolean canHandle(ResolveRequest request) throws SecoreException {
    boolean ret = request.getOperation() != null
        && request.getOperation().equals(OP_CRS)
        && request.getParams().size() > 3;
    return ret;
  }

  @Override
  public ResolveResponse handle(ResolveRequest request) throws SecoreException {
    log.debug("Handling resolve request...");

    // first resolve the parameterized CRS
    ResolveRequest req = new ResolveRequest(request.getOperation(),
        request.getServiceUri(), request.getOriginalRequest());
    req.addParam(EXPAND_KEY, EXPAND_NONE);
    int i = 0;
    for (RequestParam p : request.getParams()) {
      String key = p.key;
      String val = p.val.toString();
      if (key == null) { // it's REST
        req.addParam(key, val);
        ++i;
      } else {
        if (key.equalsIgnoreCase(CODE_KEY) || key.equalsIgnoreCase(VERSION_KEY) ||
            key.equalsIgnoreCase(AUTHORITY_KEY) || key.equals(EXPAND_KEY)) {
          req.addParam(key, val);
          ++i;
        }
      }
      if (i == 3) {
        break;
      }
    }
    if (req.getParams().size() != 3) {
      throw new SecoreException(ExceptionCode.InvalidRequest, "Invalid Parameterized CRS request");
    }
    ResolveResponse gml = definition;
    if (gml == null) {
      // Parsed URL to get the version number and modified URL
      Pair<String,String> obj = parseRequest(req);
      String versionNumber = obj.fst;
      String url = obj.snd;

      gml = resolveId(url, versionNumber, req.getExpandDepth(), new ArrayList<Parameter>() {});
    }
    log.trace(gml.getData());

    // check if the result is a ParameterizedCRS
    String rootElementName = StringUtil.getRootElementName(gml.getData());
    if (!PARAMETERIZED_CRS.equals(rootElementName)) {
      // http://rasdaman.org/ticket/356
      throw new SecoreException(ExceptionCode.InvalidRequest,
            "Expected parameterized CRS definition, but " + req.getOriginalRequest()
          + " points to " + rootElementName + ". Identifiers of simple definitions are not"
          + " supposed to contain extra parameters, aside from the authority,"
          + " version and code.");
    }

    // handling of parameterized CRS
    IXMLElement root = XMLUtil.parse(gml.getData());

    // extract needed data from the XML, doing various validity checks
    Parameters parameters = new Parameters();
    String identifier = null;
    String targetCRS = null;
    for (int k = 0; k < root.getChildrenCount(); k++) {
      IXMLElement c = root.getChildAtIndex(k);
      if (c.getName().equals(IDENTIFIER_LABEL)) {
        identifier = c.getContent();
      } else if (c.getName().equals(TARGET_CRS)) {
        targetCRS = c.getAttribute("xlink:" + TARGET_CRS_HREF, null);
      } else if (c.getName().equals(PARAMETERS)) {
        parameters.parse(c);
      }
    }
    if (identifier == null) {
      throw new SecoreException(ExceptionCode.XmlNotValid,
          "Mandatory IDENTIFIER_LABEL missing from the GML definition");
    }
    if (targetCRS == null) {
      throw new SecoreException(ExceptionCode.XmlNotValid,
          "Mandatory target CRS missing from the GML definition");
    }

    // parse the query parameters, and accordingly update the template parameters
    for (RequestParam p : request.getParams()) {
      String name = p.key;
      String value = p.val.toString();

      log.debug("key: " + name + ", value: " + value);

      if (name != null) {
        if (name.equalsIgnoreCase(RESOLVE_TARGET_KEY) && value != null) {
          if (value.equalsIgnoreCase(RESOLVE_TARGET_NO)) {
            // no resolving, just return original definition
            return gml;
          } else if (!value.equalsIgnoreCase(RESOLVE_TARGET_YES)) {
            throw new SecoreException(ExceptionCode.InvalidRequest.locator(name),
                "Invalid value for parameter " + name + ", expected 'yes' or 'no'.");
          }
        } else if (!name.equalsIgnoreCase(AUTHORITY_KEY)
            && !name.equalsIgnoreCase(CODE_KEY)
            && !name.equalsIgnoreCase(VERSION_KEY)) {
          Parameter parameter = parameters.get(name);
          if (parameter == null) {
            throw new SecoreException(ExceptionCode.InvalidRequest.locator(name),
                "Specified parameter not supported by this Parameterized CRS.");
          }
          parameter.setValue(value);
        }
      }
    }

    // do the actual work
    parameters.evaluateParameters();

    // extract parameters with targets
    List<Parameter> params = new ArrayList<Parameter>();
    for (Parameter parameter : parameters.values()) {
      if (parameter.getTarget() != null) {
        params.add(parameter);
      }
    }

    // resolve the target CRS
    ResolveRequest targetCRSRequest = new ResolveRequest(targetCRS);
    // NOTE: URN in dictionary, e.g: urn:ogc:def:crs:OGC::_Temporal_template will be changed to def/crs/OGC/0/_Temporal_template
    // when it is translated and create collection database. Then, all the URN in user dictionary will have version 0 (even they are from
    // EPSG database, e.g: /def/crs/EPSG/0/4326).
    // the targetCrs (e.g: need to check if it is from userdb).
    String id = StringUtil.stripDef(targetCRS);
    id = StringUtil.unWrapUri(id);
    Boolean existDefInUserDB = SecoreUtil.existsDefInUserDB(id, DbManager.FIX_USER_VERSION_NUMBER);
    
    String versionNumber = "";
    String url = "";
    // If the crs exist in userDB then nothing need to change here.
    if (existDefInUserDB) {
      Pair<String,String> versionUrlPair = parseRequest(targetCRSRequest);
      versionNumber = versionUrlPair.fst;
      url = versionUrlPair.snd;
    } else {
      // URN from EPSG database, need to use fixed gml version.
      versionNumber = DbManager.FIX_GML_VERSION_NUMBER;
      if (StringUtil.hasDefaultUserDbVersion(id)) {
        // Only change if in the URN of a definition of userdb did not set the specific version.
        url = StringUtil.replaceVersionNumber(targetCRS, versionNumber);
      }
    }
    
    // set expand depth from the original request URL
    ResolveResponse ret = resolveId(url, versionNumber, request.getExpandDepth(), params);

    // update identifier to include parameters
    String xml = StringUtil.replaceElementValue(
        ret.getData(), IDENTIFIER_LABEL, request.getOriginalRequest());
    ret = new ResolveResponse(xml);

    return ret;
  }

  public ResolveResponse getDefinition() {
    return definition;
  }

  public void setDefinition(ResolveResponse definition) {
    this.definition = definition;
  }

  public static boolean isParameterizedCrsDefinition(String def) {
    return PARAMETERIZED_CRS.equals(StringUtil.getRootElementName(def));
  }

}
class Parameters extends HashMap<String, Parameter> {

  private static Logger log = LoggerFactory.getLogger(Parameters.class);

  // used to detect circular references
  private Set<String> stack = new HashSet<String>();

  /**
   * @param xml a parameters XML fragment
   */
  public void parse(IXMLElement xml) throws SecoreException {
    for (int i = 0; i < xml.getChildrenCount(); i++) {
      IXMLElement parameter = xml.getChildAtIndex(i);
      if (!parameter.getName().equals(PARAMETER)) {
        throw new SecoreException(ExceptionCode.XmlNotValid,
            "Expected element name " + PARAMETER + ", got " + parameter.getName());
      }
      String name = parameter.getAttribute(PARAMETER_NAME, null);
      if (name == null) {
        throw new SecoreException(ExceptionCode.XmlNotValid,
            "Missing mandatory attribute " + PARAMETER_NAME);
      }
      if (containsKey(name)) {
        throw new SecoreException(ExceptionCode.XmlNotValid,
            "Duplicate parameter " + name);
      }

      String value = null;
      String target = null;
      for (int j = 0; j < parameter.getChildrenCount(); j++) {
        IXMLElement c = parameter.getChildAtIndex(j);
        if (c.getName().equals(PARAMETER_VALUE)) {
          value = c.getContent();
        } else if (c.getName().equals(PARAMETER_TARGET)) {
          target = c.getContent();
        } else {
          throw new SecoreException(ExceptionCode.XmlNotValid,
              "Unknown element name " + c.getName() + ", expected "
              + PARAMETER_VALUE + " or " + PARAMETER_TARGET);
        }
      }

      put(name, new Parameter(name, value, target));
    }
  }

  /**
   * Substitute the value expression with computed values.
   * @throws SecoreException in case of an error while evaluating a value
   *  expression
   */
  public void evaluateParameters() throws SecoreException {
    stack.clear();
    for (Parameter parameter : values()) {
      evaluateParameter(parameter);
    }
  }

  /**
   *
   * @param parameter
   * @throws SecoreException
   */
  private void evaluateParameter(Parameter parameter) throws SecoreException {
    if (stack.contains(parameter.getName())) {
      throw new SecoreException(ExceptionCode.XmlNotValid.locator(parameter.getName()),
          "Parameter value leads to circular evaluation.");
    }
    if (parameter.isEvaluated()) {
      return;
    }

    stack.add(parameter.getName());

    // substitute references
    Set<String> refs = parameter.getRefs();
    for (String refName : refs) {
      Parameter refParameter = get(refName);
      evaluateParameter(refParameter);
      parameter.substituteReference(refName, refParameter.getValue());
    }

    // now evaluate this parameter's value
    parameter.evaluateValue();

    stack.remove(parameter.getName());
  }
}

/**
 * Class representing a paramater. Holds the parameters name, value, and target
 * XPath expression.
 *
 * @author Dimitar Misev
 */
class Parameter {

  private static Logger log = LoggerFactory.getLogger(Parameter.class);

  public static final Pattern PATTERN = Pattern.compile("\\$\\{?\\{([^\\}]+)\\}\\}?");

  private final String name;
  private String value;
  private final String target;

  // parameters referenced from a value
  private final Set<String> refs;

  // flags whether value has been evaluated, in order to avoid re-evaluations
  private boolean evaluated;

  public Parameter(String name) {
    this(name, null);
  }

  public Parameter(String name, String value) {
    this(name, value, null);
  }

  public Parameter(String name, String value, String target) {
    this.name = name;
    this.value = value;
    this.target = target;
    this.refs = new HashSet<String>();

    // extract referenced parameters
    if (value != null) {
      value = value.replaceAll("&gt;", ">").replaceAll("&lt;", "<").replaceAll("&amp;", "&");
      Matcher matcher = PATTERN.matcher(value);
      while (matcher.find()) {
        try {
          refs.add(matcher.group(1));
        } catch (Exception ex) {
          log.warn(ex.getMessage());
        }
      }
    }
  }

  public void substituteReference(String name, String value) {
    this.value = this.value.replaceAll("\\$\\{?\\{" + name + "\\}\\}?", value);
  }

  /**
   * Update the value of the parameter.
   *
   * @throws SecoreException in case of an error while evaluating the value
   *  expression
   */
  public void evaluateValue() throws SecoreException {
    if (evaluated) {
      return;
    }
    try {
      this.value = StringUtil.evaluate(value);
      evaluated = true;
    } catch (Exception ex) {
      throw new SecoreException(ExceptionCode.InvalidParameterValue.locator(name),
          "Failed evaluating the parameter value: " + value, ex);
    }
  }

  public String getName() {
    return name;
  }

  public Set<String> getRefs() {
    return refs;
  }

  public String getTarget() {
    return target;
  }

  public String getValue() {
    return value;
  }

  public boolean isEvaluated() {
    return evaluated;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "Parameter{"
        + "\n\tname=" + name
        + "\n\tvalue=" + value
        + "\n\ttarget=" + target
        + "\n\trefs=" + refs
        + "\n\tevaluated=" + evaluated
        + "\n}";
  }
}

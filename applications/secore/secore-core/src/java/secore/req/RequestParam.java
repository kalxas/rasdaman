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
package secore.req;

import secore.util.Constants;
import static secore.util.Constants.KEY_VALUE_SEPARATOR;
import secore.util.ExceptionCode;
import secore.util.SecoreException;
import secore.util.StringUtil;

/**
 * Parameter in a request.
 *
 * @author Dimitar Misev
 */
public class RequestParam implements Comparable<RequestParam> {

  public final String key;
  public final ParamValue val;
  
  /**
   * is this parameter a key-value pair?
   */
  private boolean kvp;
  
  /**
   * Construct parameter from a single string:
   * 1. if it's a single value then it's a REST parameter
   * 2. if it's of the form key=value then it's KVP
   * 
   * @param param request parameter
   * @throws SecoreException 
   */
  public RequestParam(String param) throws SecoreException {
    if (param == null) {
      throw new SecoreException(ExceptionCode.InvalidParameterValue,
          "Can not parse null parameter.");
    }
    if (param.contains(KEY_VALUE_SEPARATOR)) {
      String[] tmp = param.split(KEY_VALUE_SEPARATOR);
      this.key = tmp[0];
      String value = StringUtil.join(tmp, 1, tmp.length, KEY_VALUE_SEPARATOR);
      if (tmp[0].matches("^\\d+")) {
        this.val = new ResolveRequest(value);
      } else {
        this.val = new SimpleParamValue(value);
      }
      this.kvp = true;
    } else {
      this.key = null;
      this.val = new SimpleParamValue(param);
      this.kvp = false;
    }
  }

  public RequestParam(String key, ParamValue val) {
    if (val == null) {
      throw new IllegalArgumentException("The value of a request parameter must not be null.");
    }
    this.key = key;
    this.val = val;
    this.kvp = key != null;
  }

  public RequestParam(String key, String val) {
    if (val == null) {
      throw new IllegalArgumentException("The value of a request parameter must not be null.");
    }
    this.key = key;
    this.val = new SimpleParamValue(val);
    this.kvp = key != null;
  }

  public boolean isKvp() {
    return key != null && kvp;
  }

  public void setKvp() {
    this.kvp = true;
  }
  
  /**
   * @return true if the parameter is REST (only value), or false if it is KVP.
   */
  public boolean isRest() {
    return key == null || !kvp;
  }

  public void setRest() {
    this.kvp = false;
  }
  
  /**
   * Get separator in the URI for this parameter. For KVP parameter the
   * separator before the parameter is '&', and for REST parameter (only value)
   * the separator is '/'.
   */
  public String getParamSeparator() {
    String ret = Constants.PAIR_SEPARATOR;
    if (isRest()) {
      ret = Constants.REST_SEPARATOR;
    }
    return ret;
  }
  
  /**
   * Get fragment in the URI for this parameter. For KVP parameter the
   * separator before the parameter is '?', and for REST parameter (only value)
   * the separator is '/'.
   */
  public String getFragmentSeparator() {
    String ret = Constants.QUERY_SEPARATOR;
    if (isRest()) {
      ret = Constants.REST_SEPARATOR;
    }
    return ret;
  }

  @Override
  public String toString() {
    String ret = null;
    if (isRest()) {
      ret = val.toString();
    } else {
      ret = key + Constants.KEY_VALUE_SEPARATOR + val.toString();
    }
    return ret;
  }

  public int compareTo(RequestParam o) {
    // try first to convert to integer
    try {
      return Integer.valueOf(key).compareTo(Integer.valueOf(o.key));
    } catch (Exception ex) {
      // failed
      return 0;
    }
  }
}

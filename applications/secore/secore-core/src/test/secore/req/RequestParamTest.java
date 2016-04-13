/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package secore.req;

import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 * Test for request parameters.
 *
 * @author Dimitar Misev
 */
public class RequestParamTest {
  
  private RequestParam restParam = null;
  private RequestParam kvpParam = null;
  private RequestParam invalidParam = null;
  
  public RequestParamTest() {
    restParam = new RequestParam(null, "restValue");
    kvpParam = new RequestParam("key", "value");
  }
  
  /**
   * Test of constructor, of class RequestParam.
   */
  @Test
  public void testConstructor() {
    System.out.println("testConstructor");
    // todo
  }

  /**
   * Test of isKvp method, of class RequestParam.
   */
  @Test
  public void testIsKvp() {
    System.out.println("isKvp");
    assertTrue(kvpParam.isKvp());
    assertFalse(restParam.isKvp());
  }

  /**
   * Test of isRest method, of class RequestParam.
   */
  @Test
  public void testIsRest() {
    System.out.println("isRest");
    assertFalse(kvpParam.isRest());
    assertTrue(restParam.isRest());
  }

  /**
   * Test of toString method, of class RequestParam.
   */
  @Test
  public void testToString() {
    System.out.println("toString");
    RequestParam instance = kvpParam;
    String expResult = "key=value";
    String result = instance.toString();
    assertEquals(expResult, result);
    
    instance = restParam;
    expResult = "restValue";
    result = instance.toString();
    assertEquals(expResult, result);
  }

  /**
   * Test of toString method, of class RequestParam.
   */
  @Test
  public void testInvalidParam() {
    System.out.println("testInvalidParam");
    try {
      invalidParam = new RequestParam(null, (String) null);
      fail();
    } catch (Exception ex) {
      
    }
  }
}
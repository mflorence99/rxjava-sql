package io.mflo.rxjava_sql;

import java.util.HashMap;
import java.util.Map;

import org.junit.*;
import static org.junit.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for <code>Parameters</code>
 *
 * @author      http://mflo.io
 * @version     0.0.1
 */

public class ParametersTest {

  private static final Logger log = LoggerFactory.getLogger(ParametersTest.class);

  private class Dummy implements Parameters { }

  @Test public void testOrderParameterNames() {
    String original = "SELECT this, that from Table WHERE this = :this and that = :that";
    String expected = "SELECT this, that from Table WHERE this = ? and that = ?";
    Map<String,Integer> ordinalByName = new HashMap<>();
    String actual = new Dummy().orderParameterNames(original, ordinalByName);
    assertEquals("Named parameters should be converted to ?", expected, actual);
    assertEquals("Named parameter ordinal is inferred correctly", 1, ordinalByName.get("this").intValue());
    assertEquals("Named parameter ordinal is inferred correctly", 2, ordinalByName.get("that").intValue());
  }

}

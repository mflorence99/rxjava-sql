package io.mflo.rxjava_sql;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model an individual result from an SQL query as an immutable map of name/value pairs.
 *
 * <p>A fluent API based on <code>set</code> operations allows deltas to be recorded,
 * but the underlying <code>Result</code> is not changed. Instead, a terminal operation
 * in the fluent chain like <code>fromChanges</code> creates a shallow clone.</p>
 *
 * @author      http://mflo.io
 * @version     0.0.1
 */

public final class Result {

  // logger for this class
  private static final Logger log = LoggerFactory.getLogger(Result.class);

  // immutable name/value state
  private final Map<String,Object> attributes;

  // deltas to original state as recorded by <code>set</code> operations
  private final Map<String,Object> changes = new HashMap<>();

  /**
   * Construct an empty <code>Result</code>
   */
  public Result() {
    this((Map)null);
  }

  /**
   * Construct one <code>Result</code> as the shallow clone of another
   *
   * @param   another <code>Result</code>
   */
  public Result(Result another) {
    this((another != null)? another.attributes : (Map)null);
  }

  /**
   * Construct one <code>Result</code> as the shallow clone of a map of name/value pairs
   *
   * @param   attributes name/value pairs
   */
  public Result(Map<String,Object> attributes) {
    this.attributes = (attributes != null)? new HashMap<>(attributes) : new HashMap<>();
  }

  /**
   * Create a new <code>Result</code> accumulating all changes
   *
   * @return  result
   */
  public Result fromChanges() {
    Result changed = new Result(this);
    changed.attributes.putAll(this.changes);
    return changed;
  }

  /**
   * Get a stringified value by name, returning an empty string if not found
   *
   * @param   name used in SQL query
   *
   * @return  value or empty string if not found
   */
  public String get(String name) {
    return get(name, "");
  }

  /**
   * Get a stringified value by name, returning a default string if not found
   *
   * @param   name used in SQL query
   * @param   dflt default if not found
   *
   * @return  value or default if not found
   */
  public String get(String name,
                    String dflt) {
    return has(name)? attributes.get(name).toString() : dflt;
  }

  /**
   * Tests if a value exists for a  name
   *
   * @param   name used in SQL query
   *
   * @return  true or false as appropriate
   */
  public boolean has(String name) {
    return attributes.containsKey(name);
  }

  /**
   * Get a shallow copy of the name/value pairs in this <code>Result</code>
   *
   * @return  name/value pairs
   */
  public Map<String,Object> getAttributes() {
    return new HashMap<>(attributes);
  }

  /**
   * Changes a stringified value by name
   *
   * @param   name used in SQL query
   * @param   value to be used
   *
   * @return  value or default if not found
   */
  public Result set(String name,
                    String value) {
    changes.put(name, value);
    return this;
  }

  /**
   * Convert this <code>Result</code> to a string for debugging purposes
   *
   * @return  stringified representation of <code>Result</code>
   */
  public String toString() {
    return attributes.toString();
  }

}

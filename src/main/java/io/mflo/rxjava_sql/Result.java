package io.mflo.rxjava_sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model an individual Result from an SQL query as a Tuple.
 *
 * @author      http://mflo.io
 * @version     0.0.1
 */

public final class Result implements Tuple {

  // logger for this class
  private static final Logger log = LoggerFactory.getLogger(Result.class);

  // name/value state
  private final LinkedHashMap<String,Object> attributes;
  private final Map<Integer,String> nameByOrdinal = new HashMap<>();

  /**
   * Construct an empty <code>Result</code>
   */
  public Result() {
    this((LinkedHashMap)null);
  }

  /**
   * Construct one <code>Result</code> as the shallow clone of another
   *
   * @param   another <code>Result</code>
   */
  public Result(Result another) {
    this(another, (Map)null);
  }

  /**
   * Construct one <code>Result</code> as the shallow clone of another
   * PLUS a Map of accumulated deltas
   *
   * @param   another <code>Result</code>
   * @param   deltas <code>Map</code> of accumulated deltas
   */
  public Result(Result another,
                Map<String,Object> deltas) {
    this((another != null)? another.attributes : (LinkedHashMap)null, deltas);
  }

  /**
   * Construct one <code>Result</code> as the shallow clone of a map of name/value pairs
   *
   * @param   attributes name/value pairs
   */
  protected Result(LinkedHashMap<String,Object> attributes) {
    this(attributes, (Map)null);
  }

  /**
   * Construct one <code>Result</code> as the shallow clone of a map of name/value pairs
   * PLUS a Map of accumulated deltas
   *
   * @param   attributes name/value pairs
   * @param   deltas <code>Map</code> of accumulated deltas
   */
  protected Result(LinkedHashMap<String,Object> attributes,
                   Map<String,Object> deltas) {
    this.attributes = (attributes != null)? attributes : new LinkedHashMap<>();
    int ordinal = 1;
    for (String name : this.attributes.keySet())
      nameByOrdinal.put(ordinal++, name);
    // after ordinal has been calculated, apply deltas if any
    if (deltas != null) {
      for (String name : deltas.keySet())
        this.attributes.put(name, deltas.get(name));
    }
  }

  /**
   * Get a value by ordinal
   *
   * @param   ordinal positon in result
   *
   * @return  value
   */
  @Override public Object get(int ordinal) {
    String name = nameByOrdinal.get(ordinal);
    if (name == null)
      throw new IllegalArgumentException("Ordinal out-of-range [" + ordinal + "]");
    return get(name);
  }

  /**
   * Get a value by ordinal
   *
   * @param   ordinal positon in result
   * @param   type of value
   *
   * @return  value
   */
  @Override public <X> X get(int ordinal,
                             Class<X> type) {
    Object untyped = get(ordinal);
    if (untyped != null) {
      if (!type.isInstance(untyped))
        throw new IllegalArgumentException("Ordinal [" + ordinal + "] incompatible with [" + type + "]");
    }
    return (X)untyped;
  }

  /**
   * Get a value by name
   *
   * @param   name used in SQL query
   *
   * @return  value
   */
  @Override public Object get(String name) {
    if (!has(name))
      throw new IllegalArgumentException("Unknown name [" + name + "]");
    return attributes.get(name);
  }

  /**
   * Get a value by name
   *
   * @param   name used in SQL query
   * @param   type of value
   *
   * @return  value
   */
  @Override public <X> X get(String name,
                             Class<X> type) {
    Object untyped = get(name);
    if (untyped != null) {
      if (!type.isInstance(untyped))
        throw new IllegalArgumentException("Name [" + name + "] incompatible with [" + type + "]");
    }
    return (X)untyped;
  }

  /**
   * Get a value by element
   *
   * @param   element of tuple
   *
   * @return  value
   */
  @Override public <X> X get(TupleElement<X> element) {
    return get(element.getAlias(), element.getJavaType());
  }

  /**
   * Get all the values as elements
   *
   * @return  elements
   */
  @Override public List<TupleElement<?>> getElements() {
    List<TupleElement<?>> elements = new ArrayList<>();
    for (String name : attributes.keySet()) {
      Object value = attributes.get(name);
      Class type = (value != null)? value.getClass() : null;
      elements.add(new Element(name, type));
    }
    return elements;
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
  protected Map<String,Object> getAttributes() {
    return new HashMap<>(attributes);
  }

  /**
   * Get all the values as an array
   *
   * @return  values
   */
  @Override public Object[] toArray() {
    return attributes.values().toArray();
  }

  /**
   * Convert this <code>Result</code> to a string for debugging purposes
   *
   * @return  stringified representation of <code>Result</code>
   */
  @Override public String toString() {
    return attributes.toString();
  }

  /**
   * Thin implementation of TupleElement
   */

  private static class Element implements TupleElement {

    // private state
    private final String name;
    private final Class type;

    /**
     * Construct one <code>TupleElement</code>
     *
     * @param   name
     * @param   tuype
     */
    private Element(String name,
                    Class type) {
      this.name = name;
      this.type = type;
    }

    /**
     * Get name aka alias
     *
     * @return  name
     */
    @Override public String getAlias() {
      return name;
    }

    /**
     * Get type
     *
     * @return  type
     */
    @Override public Class getJavaType() {
      return type;
    }

  }

}

package io.setl.rdf.normalization;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import com.apicatalog.rdf.RdfNQuad;
import com.apicatalog.rdf.RdfResource;
import com.apicatalog.rdf.RdfValue;

/**
 * Enumeration of positions in an RDF quad.
 *
 * @author Simon Greatrix on 07/10/2020.
 */
public enum Position {
  /** The subject of the quad. */
  SUBJECT('s') {
    @Override
    public boolean isBlank(RdfNQuad quad) {
      return quad.getSubject().isBlankNode();
    }


    @Override
    RdfValue get(RdfNQuad quad) {
      return quad.getSubject();
    }

  },

  /** The object of the quad. */
  OBJECT('o') {
    @Override
    RdfValue get(RdfNQuad quad) {
      return quad.getObject();
    }


    @Override
    public boolean isBlank(RdfNQuad quad) {
      return quad.getObject().isBlankNode();
    }
  },

  /** The graph the quad belongs to. */
  GRAPH('g') {
    @Override
    RdfValue get(RdfNQuad quad) {
      return quad.getGraphName().orElse(null);
    }


    @Override
    public boolean isBlank(RdfNQuad quad) {
      Optional<RdfResource> name = quad.getGraphName();
      return name.isPresent() && name.get().isBlankNode();
    }
  },

  /**
   * The predicate of the quad. Note the predicate is not used by the URDNA-2015 algorithm.
   */
  PREDICATE('p') {
    @Override
    RdfValue get(RdfNQuad quad) {
      return quad.getPredicate();
    }


    @Override
    public boolean isBlank(RdfNQuad quad) {
      // predicates cannot be blank
      return false;
    }
  };

  /**
   * Set of positions in a quad which can be blank.
   */
  public static final Set<Position> CAN_BE_BLANK = Collections.unmodifiableSet(EnumSet.of(SUBJECT, OBJECT, GRAPH));

  /**
   * The tag used to represent the position in hashes.
   */
  private final byte tag;


  Position(char ch) {
    tag = (byte) ch;
  }


  /**
   * Get the value at this position in the quad.
   *
   * @param quad the quad
   *
   * @return the value at this position
   */
  abstract RdfValue get(RdfNQuad quad);


  /**
   * Is the value at this position in the quad a blank node identifier?.
   *
   * @param quad the quad
   *
   * @return true if this position holds a blank node identifier.
   */
  abstract boolean isBlank(RdfNQuad quad);


  /**
   * Get the tag to include in hashes to represent this position.
   *
   * @return the tag
   */
  public byte tag() {
    return tag;
  }
}

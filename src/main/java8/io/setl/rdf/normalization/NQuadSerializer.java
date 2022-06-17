package io.setl.rdf.normalization;

import java.util.Optional;

import com.apicatalog.rdf.Rdf;
import com.apicatalog.rdf.RdfLiteral;
import com.apicatalog.rdf.RdfNQuad;
import com.apicatalog.rdf.RdfResource;
import com.apicatalog.rdf.RdfValue;
import com.apicatalog.rdf.lang.XsdConstants;

/**
 * Encode an RDF quad in N-Quad format. For cryptographic reasons the serialization is performed with the minimum of escapes. RDF requires string-equality for
 * IRIs, so no processing of percent encoding in the IRIs is performed.
 *
 * @author Simon Greatrix on 06/10/2020.
 * @see <a href="https://www.w3.org/TR/n-quads/">RDF 1.1. N-Quads</a>.
 */

public class NQuadSerializer {

  private static final RdfResource BLANK_A = Rdf.createBlankNode("_:a");
  private static final RdfResource BLANK_Z = Rdf.createBlankNode("_:z");


  private static void escape(StringBuilder builder, String value) {
    value.codePoints().forEach(ch -> {
      switch (ch) {
        case 0xa:
          builder.append("\\n");
          break;

        case 0xd:
          builder.append("\\r");
          break;

        case '"':
          builder.append("\\\"");
          break;

        case '\\':
          builder.append("\\\\");
          break;

        default:
          builder.appendCodePoint(ch);
          break;
      }
    });
  }


  static String forBlank(RdfNQuad q0, RdfValue blankId) {
    RdfResource subject = q0.getSubject();
    if (subject.isBlankNode()) {
      // A blank node is always a resource
      subject = subject.equals(blankId) ? BLANK_A : BLANK_Z;
    }

    RdfValue object = q0.getObject();
    if (object.isBlankNode()) {
      object = object.equals(blankId) ? BLANK_A : BLANK_Z;
    }

    Optional<RdfResource> graph = q0.getGraphName();
    if (graph.isPresent()) {
      RdfResource g = graph.get();
      if (g.isBlankNode()) {
        graph = Optional.of(g.equals(blankId) ? BLANK_A : BLANK_Z);
      }
    }
    return write(subject, q0.getPredicate(), object, graph);
  }


  private static void write(StringBuilder builder, RdfLiteral literal) {

    if (literal == null) {
      throw new IllegalArgumentException();
    }

    builder.append('"');
    escape(builder, literal.getValue());
    builder.append('"');

    final Optional<String> language = literal.getLanguage();

    if (language.isPresent()) {
      builder.append('@');
      builder.append(language.get());
    } else if (literal.getDatatype() != null) {

      if (XsdConstants.STRING.equals(literal.getDatatype())) {
        return;
      }

      builder.append("^^");
      writeIri(builder, literal.getDatatype());
    }
  }


  public static String write(final RdfNQuad nQuad) {
    return write(nQuad.getSubject(), nQuad.getPredicate(), nQuad.getObject(), nQuad.getGraphName());
  }


  /**
   * Write out the specified values in NQuad format.
   *
   * @param subject   the quad's subject
   * @param predicate the quad's predicate
   * @param object    the quad's object
   * @param graphName the quad's graph name, if any
   *
   * @return the NQuad serialization
   */
  public static String write(RdfResource subject, RdfResource predicate, RdfValue object, Optional<RdfResource> graphName) {
    StringBuilder builder = new StringBuilder();
    writeValue(builder, subject);
    builder.append(' ');

    writeValue(builder, predicate);
    builder.append(' ');

    writeValue(builder, object);
    builder.append(' ');

    if (graphName.isPresent()) {
      writeValue(builder, graphName.get());
      builder.append(' ');
    }

    builder.append(".\n");

    return builder.toString();
  }


  private static void writeIri(StringBuilder builder, String iri) {
    if (iri == null) {
      throw new IllegalArgumentException();
    }

    builder.append('<');
    builder.append(iri);
    builder.append('>');
  }


  private static void writeValue(StringBuilder builder, RdfValue object) {
    if (object == null) {
      throw new IllegalArgumentException();
    }

    if (object.isIRI()) {
      writeIri(builder, object.toString());
      return;
    }

    if (object.isLiteral()) {
      write(builder, object.asLiteral());
      return;
    }

    if (object.isBlankNode()) {
      builder.append(object);
      return;
    }

    throw new IllegalStateException();
  }

}

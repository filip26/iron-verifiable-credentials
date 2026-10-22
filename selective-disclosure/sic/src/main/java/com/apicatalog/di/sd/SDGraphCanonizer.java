package com.apicatalog.di.sd;

import java.util.Map;

import com.apicatalog.trust.semantic.SemanticModel.GraphCanonizer;
import com.apicatalog.trust.semantic.SemanticModel.QuadConsumer;

public interface SDGraphCanonizer extends GraphCanonizer {

      void canonize(QuadConsumer consumer);

      Map<String, String> labels();

      String toNQuad(
              String subject,
              String predicate,
              String object,
              String datatype,
              String language,
              String direction,
              String graph);

      // TODO void reset();
    
}

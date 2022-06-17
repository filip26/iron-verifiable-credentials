package io.setl.rdf.normalization;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.apicatalog.jsonld.StringUtils;
import com.apicatalog.rdf.Rdf;
import com.apicatalog.rdf.RdfDataset;
import com.apicatalog.rdf.RdfNQuad;
import com.apicatalog.rdf.RdfResource;
import com.apicatalog.rdf.RdfValue;

/**
 * Perform RDF normalization.
 *
 * @author Simon Greatrix on 05/10/2020.
 */
public class RdfNormalize {

  /**
   * The lower-case hexadecimal alphabet.
   */
  private static final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


  /**
   * Convert bytes to hexadecimal.
   *
   * @param data the bytes
   *
   * @return the data represented in hexadecimal.
   */
  static String hex(byte[] data) {
    StringBuilder builder = new StringBuilder(data.length * 2);
    for (byte b : data) {
      builder.append(HEX[(b & 0xf0) >> 4]).append(HEX[b & 0xf]);
    }
    return builder.toString();
  }


  /**
   * Normalize an RDF dataset using the URDNA 2015 algorithm.
   *
   * @param input the dataset to be normalized
   *
   * @return a new normalized equivalent dataset.
   */
  public static RdfDataset normalize(RdfDataset input) {
    return new RdfNormalize(input).doNormalize();
  }


  /**
   * Normalize an RDF dataset using the specified algorithm. NB. Currently only "URDNA2015" is supported.
   *
   * @param input     the dataset to be normalized
   * @param algorithm the normalization algorithm. If null or empty, URDNA2015 is assumed.
   *
   * @return a new normalized equivalent dataset.
   *
   * @throws NoSuchAlgorithmException if the algorithm is not known (i.e. not "URDNA2015")
   */
  public static RdfDataset normalize(RdfDataset input, String algorithm) throws NoSuchAlgorithmException {
    if (algorithm == null || StringUtils.isBlank(algorithm) || algorithm.equalsIgnoreCase("urdna2015")) {
      // use default algorithm
      return new RdfNormalize(input).doNormalize();
    }

    throw new NoSuchAlgorithmException("Normalization algorithm is not supported:" + algorithm);
  }


  /**
   * The state information for the hash n-degree quads algorithm.
   */
  private class HashNDegreeQuads {

    /** The data which will go into the hash. */
    final StringBuilder dataToHash = new StringBuilder();

    /** The currently chosen identifier issuer. */
    IdentifierIssuer chosenIssuer = null;

    /** The currently chosen path. */
    StringBuilder chosenPath = null;


    /**
     * Append an ID to the hash path.
     *
     * @param related       the ID to append
     * @param pathBuilder   the path to append to
     * @param issuerCopy    the identifier issuer
     * @param recursionList the node recursion list
     */
    private void appendToPath(RdfResource related, StringBuilder pathBuilder, IdentifierIssuer issuerCopy, List<RdfResource> recursionList) {
      if (canonIssuer.hasId(related)) {
        // 5.4.4.1: Already has a canonical ID so we just use it.
        pathBuilder.append(canonIssuer.getId(related).getValue());
      } else {
        // 5.4.4.2: Need to try an ID, and possibly recurse
        if (!issuerCopy.hasId(related)) {
          recursionList.add(related);
        }
        pathBuilder.append(issuerCopy.getId(related).getValue());
      }
    }


    /**
     * Implementation of steps 1 to 3 of the Hash N-Degree Quads algorithm.
     *
     * @param id     the ID of the blank node to process related nodes for
     * @param issuer the ID issuer currently being used.
     *
     * @return the required mapping
     */
    private SortedMap<String, Set<RdfResource>> createHashToRelated(RdfResource id, IdentifierIssuer issuer) {
      SortedMap<String, Set<RdfResource>> hashToRelated = new TreeMap<>();
      // quads that refer to the blank node.
      RdfDataset refer = blankIdToQuadSet.get(id);
      for (RdfNQuad quad : refer.toList()) {
        // find all the blank nodes that refer to this node by a quad
        for (Position position : Position.CAN_BE_BLANK) {
          if (position.isBlank(quad) && !id.equals(position.get(quad))) {
            RdfResource related = (RdfResource) position.get(quad);
            String hash = hashRelatedBlankNode(related, quad, issuer, position);
            hashToRelated.computeIfAbsent(hash, h -> new HashSet<>()).add(related);
          }
        }
      }
      return hashToRelated;
    }


    /**
     * Process one possible permutation of the blank nodes.
     *
     * @param permutation the permutation
     * @param issuer      the identifier issuer
     */
    private void doPermutation(RdfResource[] permutation, IdentifierIssuer issuer) {
      // 5.4.1 to 5.4.3 : initialise variables
      IdentifierIssuer issuerCopy = issuer.copy();
      StringBuilder pathBuilder = new StringBuilder();
      List<RdfResource> recursionList = new ArrayList<>();

      // 5.4.4: for every resource in the this permutation of the resources
      for (RdfResource related : permutation) {
        appendToPath(related, pathBuilder, issuerCopy, recursionList);

        // 5.4.4.3: Is this path better than our chosen path?
        if (chosenPath.length() > 0 && pathBuilder.toString().compareTo(chosenPath.toString()) > 0) {
          // This is permutation is not going to make the best path, so skip the rest of it
          return;
        }
      }

      // 5.4.5: Process the recursion list
      for (RdfResource related : recursionList) {
        NDegreeResult result = hashNDegreeQuads(related, issuerCopy);

        pathBuilder
            .append(issuerCopy.getId(related).getValue())
            .append('<')
            .append(result.getHash())
            .append('>');
        issuerCopy = result.getIssuer();

        if (chosenPath.length() > 0 && pathBuilder.toString().compareTo(chosenPath.toString()) > 0) {
          // This is permutation is not going to make the best path, so skip the rest of it
          return;
        }
      }

      // 5.4.6: Do we have a new chosen path?
      if (chosenPath.length() == 0 || pathBuilder.toString().compareTo(chosenPath.toString()) < 0) {
        chosenPath.setLength(0);
        chosenPath.append(pathBuilder);
        chosenIssuer = issuerCopy;
      }
    }


    /**
     * Calculate the hash from the N-Degree nodes.
     *
     * @param id     the blank node starting ID
     * @param issuer the identifier issuer
     *
     * @return the result
     */
    NDegreeResult hash(RdfResource id, IdentifierIssuer issuer) {
      SortedMap<String, Set<RdfResource>> hashToRelated = createHashToRelated(id, issuer);

      for (Entry<String, Set<RdfResource>> entry : hashToRelated.entrySet()) {
        // 5.1 to 5.3: Append the hash for the related item to the hash we are building and initialise variables
        dataToHash.append(entry.getKey());
        chosenPath = new StringBuilder();
        chosenIssuer = null;

        // 5.4: For every possible permutation of the blank node list...
        Permutator permutator = new Permutator(entry.getValue().toArray(new RdfResource[0]));
        while (permutator.hasNext()) {
          doPermutation(permutator.next(), issuer);
        }

        // 5.5: Append chosen path to the hash
        dataToHash.append(chosenPath);
        issuer = chosenIssuer;
      }

      sha256.reset();
      String hash = hex(sha256.digest(dataToHash.toString().getBytes(StandardCharsets.UTF_8)));
      return new NDegreeResult(hash, issuer);
    }


    /**
     * Create a hash of the related blank nodes, as described in the specification.
     *
     * @param related  the ID nodes are related to
     * @param quad     the quad to process
     * @param issuer   the identifier issuer
     * @param position the position in the quad
     *
     * @return the hash
     */
    private String hashRelatedBlankNode(
        RdfResource related,
        RdfNQuad quad,
        IdentifierIssuer issuer,
        Position position
    ) {
      // Find an ID for the blank ID
      String id;
      if (canonIssuer.hasId(related)) {
        id = canonIssuer.getId(related).getValue();
      } else if (issuer.hasId(related)) {
        id = issuer.getId(related).getValue();
      } else {
        id = hashFirstDegree(related);
      }

      // Create the hash of position, predicate and ID.
      sha256.reset();
      sha256.update(position.tag());
      if (position != Position.GRAPH) {
        sha256.update((byte) '<');
        sha256.update(quad.getPredicate().getValue().getBytes(StandardCharsets.UTF_8));
        sha256.update((byte) '>');
      }
      sha256.update(id.getBytes(StandardCharsets.UTF_8));
      return hex(sha256.digest());
    }


  }



  /** Map of blank IDs to all the quads that reference that specific blank ID. */
  private final HashMap<RdfValue, RdfDataset> blankIdToQuadSet = new HashMap<>();

  /** Issuer of canonical IDs to blank nodes. */
  private final IdentifierIssuer canonIssuer = new IdentifierIssuer("_:c14n");

  /**
   * Hash to associated IRIs.
   */
  private final TreeMap<String, Set<RdfResource>> hashToBlankId = new TreeMap<>();

  /** All the quads in the dataset to be processed. */
  private final List<RdfNQuad> quads;

  /** An instance of the SHA-256 message digest algorithm. */
  private final MessageDigest sha256;

  /** A set of non-normalized values. */
  private HashSet<RdfValue> nonNormalized;


  private RdfNormalize(RdfDataset input) {
    try {
      sha256 = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      // The Java specification requires SHA-256 is included, so this should never happen.
      throw new InternalError("SHA-256 is not available", e);
    }
    quads = input.toList();
  }


  private RdfDataset doNormalize() {
    // Step 1 is done by the constructor.
    // Step 2:
    findBlankNodes();

    // Step 3:
    setNonNormalized();

    // Steps 4 and 5:
    issueSimpleIds();

    // Step 6:
    issueNDegreeIds();

    // Step 7:
    return makeCanonQuads();
  }


  private void findBlankNodes() {
    // Find all the quads that link with a blank node
    for (RdfNQuad quad : quads) {
      for (Position position : Position.CAN_BE_BLANK) {
        RdfValue value = position.get(quad);
        if (value != null && value.isBlankNode()) {
          blankIdToQuadSet.computeIfAbsent(value, k -> Rdf.createDataset()).add(quad);
        }
      }
    }
  }


  private String hashFirstDegree(RdfValue blankId) {
    List<RdfNQuad> related = blankIdToQuadSet.get(blankId).toList();
    String[] nQuads = new String[related.size()];
    int i = 0;

    // Convert the NQuads to a consistent set by replacing the reference with _:a and all others with _:z, and then sorting
    for (RdfNQuad q0 : related) {
      nQuads[i] = NQuadSerializer.forBlank(q0, blankId);
      i++;
    }

    // Sort the nQuads
    Arrays.sort(nQuads);

    // Create the hash
    sha256.reset();
    for (String s : nQuads) {
      sha256.update(s.getBytes(StandardCharsets.UTF_8));
    }
    return hex(sha256.digest());
  }


  private NDegreeResult hashNDegreeQuads(RdfResource id, IdentifierIssuer issuer) {
    return new HashNDegreeQuads().hash(id, issuer);
  }


  private void issueNDegreeIds() {
    for (Entry<String, Set<RdfResource>> entry : hashToBlankId.entrySet()) {
      List<NDegreeResult> hashPathList = new ArrayList<>();
      for (RdfResource id : entry.getValue()) {
        // if we've already assigned a canonical ID for this node, skip it
        if (canonIssuer.hasId(id)) {
          continue;
        }

        // Create a new blank ID issuer and assign it's first ID to the reference id
        IdentifierIssuer blankIssuer = new IdentifierIssuer("_:b");
        blankIssuer.getId(id);

        NDegreeResult path = hashNDegreeQuads(id, blankIssuer);
        hashPathList.add(path);
      }

      hashPathList.sort(Comparator.naturalOrder());
      for (NDegreeResult result : hashPathList) {
        result.getIssuer().assign(canonIssuer);
      }
    }
  }


  private void issueSimpleIds() {
    boolean simple = true;
    while (simple) {
      simple = false;
      hashToBlankId.clear();
      for (RdfValue value : nonNormalized) {
        RdfResource blankId = (RdfResource) value;
        String hash = hashFirstDegree(blankId);
        hashToBlankId.computeIfAbsent(hash, k -> new HashSet<>()).add(blankId);
      }

      Iterator<Entry<String, Set<RdfResource>>> iterator = hashToBlankId.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<String, Set<RdfResource>> entry = iterator.next();
        Set<RdfResource> values = entry.getValue();
        if (values.size() == 1) {
          RdfResource id = values.iterator().next();
          canonIssuer.getId(id);
          nonNormalized.remove(id);
          iterator.remove();
          simple = true;
        }
      }
    }
  }


  private RdfDataset makeCanonQuads() {
    SerializedQuad[] outputQuads = new SerializedQuad[quads.size()];
    int i = 0;
    AtomicBoolean changed = new AtomicBoolean();
    for (RdfNQuad q : quads) {
      changed.set(false);
      RdfResource subject = canonIssuer.getIfExists(q.getSubject(), changed);
      RdfValue object = canonIssuer.getIfExists(q.getObject(), changed);
      RdfResource graph = canonIssuer.getIfExists(q.getGraphName().orElse(null), changed);

      if (changed.get()) {
        outputQuads[i] = new SerializedQuad(Rdf.createNQuad(subject, q.getPredicate(), object, graph));
      } else {
        outputQuads[i] = new SerializedQuad(q);
      }
      i++;
    }

    Arrays.sort(outputQuads);
    RdfDataset rdfDataset = Rdf.createDataset();
    for (SerializedQuad quad : outputQuads) {
      rdfDataset.add(quad.getQuad());
    }
    return rdfDataset;
  }


  private void setNonNormalized() {
    nonNormalized = new HashSet<>(blankIdToQuadSet.keySet());
  }

}

package com.apicatalog.smt;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.crypto.Mac;

/**
 * Provides operations for generating salted Merkle tree roots, generating
 * verification paths, and reconstructing roots to verify disclosed claims.
 *
 * @author Filip Kolařík {@literal <filip26@gmail.com>}
 * 
 * @see <a href="https://github.com/filip26/salted-merkle-tree">Salted Merkle
 *      Tree</a>
 * 
 */
public class SaltedMerkleTree {

    static final byte PREFIX_LEAF = 0x00;
    static final byte PREFIX_NODE = 0x01;
    static final byte PREFIX_PADDING = 0x02;

    static final byte PREFIX_INDEX_PADDING = 0x10;
    static final byte PREFIX_LEVEL_PADDING = 0x11;

    final MessageDigest digest;
    final int maxDepth;
    final int maxLeaves;

    /**
     * Creates a salted Merkle tree helper using the specified digest.
     *
     * @param digest the digest algorithm used for tree hashes
     *
     */
    public SaltedMerkleTree(MessageDigest digest, int maxDepth) {
        this.digest = digest;
        this.maxDepth = maxDepth;
        this.maxLeaves = 1 << maxDepth;
    }

    /**
     * Computes the root representing the integrity commitment to a collection of
     * leaf hashes.
     *
     * <p>
     * The input buffer is modified in place. The initial number of leaves must be
     * even and greater than one.
     * </p>
     *
     * @param hmac   the HMAC instance used for depth-based padding
     * @param buffer the array of leaf hashes (overridden during computation)
     * @return the root byte array of the salted Merkle tree
     */
    public byte[] generateRoot(Mac hmac, byte[][] buffer) {

        if (buffer.length > maxLeaves) {
            throw new IllegalArgumentException();
        }

        final var depth = 32 - Integer.numberOfLeadingZeros(buffer.length - 1);

        var levelSize = buffer.length;

        for (int level = 0; level < depth; level++) {

            // if the current level size is odd number, never true for level 0
            if ((levelSize & 1) == 1) {
                // add depth-based padding
                buffer[levelSize] = levelPadding(hmac, level);
                levelSize++;
            }

            for (int index = 0; index < levelSize; index += 2) {

                var left = buffer[index];
                var right = buffer[index + 1];

                buffer[index >>> 1] = hashNode(left, right);
            }

            levelSize >>>= 1;
        }

        return buffer[0];
    }

    /**
     * Generates the verification path required to authenticate selected leaves
     * against a root.
     *
     * <p>
     * Selected leaves are represented by {@code null} entries in the input buffer.
     * The resulting path contains the hashes needed to reconstruct the root.
     * </p>
     *
     * <p>
     * The input buffer is modified in place. The initial number of leaves must be
     * even and greater than one.
     * </p>
     *
     * @param hmac   the HMAC instance used for depth-based padding
     * @param buffer the array of leaf hashes, with selected leaves represented by
     *               {@code null} (overridden during computation)
     * @return the verification path hashes in verification order
     */
    public List<byte[]> generatePath(Mac hmac, byte[][] buffer) {

        if (buffer.length > maxLeaves) {
            throw new IllegalArgumentException();
        }

        final var depth = 32 - Integer.numberOfLeadingZeros(buffer.length - 1);

        final var path = new ArrayList<byte[]>();

        var levelSize = buffer.length;

        for (int level = 0; level < depth; level++) {

            for (int index = 0; index < levelSize; index += 2) {

                final var left = buffer[index];

                final byte[] right;

                if ((index + 1) == levelSize) {
                    right = levelPadding(hmac, level);
                    levelSize++;

                } else {
                    right = buffer[index + 1];
                }

                final byte[] parent;

                if (left == null) {
                    if (right != null) {
                        path.add(right);
                    }
                    parent = null;

                } else if (right == null) {
                    path.add(left);
                    parent = null;

                } else {
                    parent = hashNode(left, right);
                }

                buffer[index >>> 1] = parent;
            }

            levelSize >>>= 1;
        }

        return path;
    }

    /**
     * Reconstructs a root from disclosed claims, their salts and original indices,
     * and a verification path.
     *
     * <p>
     * The reconstructed root can be compared with a trusted root to verify the
     * integrity and positions of the disclosed claims.
     * </p>
     *
     * @param claims  the disclosed claim data
     * @param salts   the salts associated with the disclosed claims
     * @param indices the original leaf indices of the disclosed claims
     * @param path    the verification path in the verification order
     * @return the reconstructed root hash
     */
    public byte[] recreateRoot(byte[][] claims, byte[][] salts, int[] indices, byte[][] path) {

        if (claims.length > maxLeaves) {
            throw new IllegalArgumentException();
        }

        var mapping = new Integer[indices.length];

        for (int i = 0; i < mapping.length; i++) {
            mapping[i] = i;
        }

        Arrays.parallelSort(mapping, Comparator.comparing(i -> indices[i]));

        var buffer = new byte[claims.length][];

        int levelSize = buffer.length;

        // reconstruct leaves
        for (int i = 0; i < levelSize; i++) {
            buffer[i] = hashLeaf(salts[mapping[i]], claims[mapping[i]]);
            mapping[i] = indices[mapping[i]];
        }

        int depth = 0;
        int pathIndex = 0;

        // reconstruct root
        while ((depth <= maxDepth) && (levelSize > 1 || pathIndex < path.length)) {

            int nextLevelSize = 0;

            for (int i = 0; i < levelSize; i++) {

                var nodeIndex = mapping[i];

                final byte[] left;
                final byte[] right;

                if ((nodeIndex & 1) == 1) {
                    left = path[pathIndex++];
                    right = buffer[i];

                } else if (i + 1 < levelSize && ((nodeIndex + 1) == mapping[i + 1])) {
                    left = buffer[i];
                    right = buffer[i + 1];
                    i++;

                } else {
                    left = buffer[i];
                    right = path[pathIndex++];
                }

                buffer[nextLevelSize] = hashNode(left, right);
                mapping[nextLevelSize] = nodeIndex >>> 1;
                nextLevelSize++;

            }
            levelSize = nextLevelSize;
            depth++;
        }

        return buffer[0];
    }

    /**
     * Generates salts for leaf entries, including an additional salt when required
     * to accommodate an odd number of entries.
     *
     * <p>
     * Salts are derived using the supplied HMAC and an index-dependent input.
     * </p>
     *
     * @param hmac the HMAC used to derive salts
     * @param size the number of leaf entries
     * @return the generated salts
     */
    public byte[][] salts(Mac hmac, int size) {

        // size must be even number
        var salts = new byte[size + (size & 1)][];

        for (var i = 0; i < salts.length; i++) {
            hmac.update(PREFIX_INDEX_PADDING);
            hmac.update((byte) (i >>> 24));
            hmac.update((byte) (i >>> 16));
            hmac.update((byte) (i >>> 8));
            hmac.update((byte) i);
            salts[i] = hmac.doFinal();
        }
        return salts;
    }

    /**
     * Determines the slot assigned to each original leaf index using its salt and
     * the salt ordering.
     *
     * <p>
     * The mapping represents the secret-keyed HMAC slot distribution and associates
     * each original index with its position in the sorted order.
     * </p>
     *
     * @param salts the salts associated with the leaf entries
     * @return the slot assigned to each original index
     */
    public Integer[] mapping(byte[][] salts) {

        var mapping = new Integer[salts.length];

        for (int i = 0; i < mapping.length; i++) {
            mapping[i] = i;
        }

        Arrays.parallelSort(mapping, Comparator.comparing(i -> salts[i], Arrays::compare));

        return mapping;
    }

    /**
     * Computes salted leaf hashes for all claims.
     *
     * <p>
     * The resulting hashes are leaves used as the initial level of a tree.
     * </p>
     *
     * @param claims  the claim data
     * @param salts   the salts associated with the claims
     * @param mapping the mapping from claim positions to leaf positions
     * @return the leaves, including any required padding leaf
     */
    public byte[][] leaves(Collection<byte[]> claims, byte[][] salts, Integer[] mapping) {
        return leaves(claims, salts, mapping, null);
    }

    /**
     * Computes salted leaf hashes while omitting selected claims. H(PREFIX_LEAF ||
     * SALT || DATA).
     *
     * <p>
     * The resulting leaves are intended for verification-path generation. Selected
     * entries remain {@code null}, allowing the corresponding claims to be withheld
     * while constructing a proof.
     * </p>
     *
     * @param claims   the claim data
     * @param salts    the salts associated with the claims, including a padding
     *                 salt if required
     * @param mapping  the mapping from claim positions to leaf positions
     * @param selected the sorted indices of claims to omit, or {@code null} to
     *                 include all claims
     * @return the leaves for root construction or verification path generation
     */
    public byte[][] leaves(Collection<byte[]> claims, byte[][] salts, Integer[] mapping, int[] selected) {

        if (claims.size() > maxLeaves) {
            throw new IllegalArgumentException();
        }

        if (claims.size() > salts.length) {
            throw new IllegalArgumentException();
        }

        if (salts.length != mapping.length) {
            throw new IllegalArgumentException();
        }

        var remainder = salts.length & 1;

        var buffer = new byte[salts.length + remainder][];

        var selectedIndex = 0;

        var claimsIt = claims.iterator();

        for (int i = 0; i < salts.length; i++) {

            if (selected == null
                    || selectedIndex >= selected.length
                    || selected[selectedIndex] != i) {

                buffer[mapping[i]] = hashLeaf(salts[i], claimsIt.hasNext() ? claimsIt.next() : null);

            } else {
                selectedIndex++;
            }
        }

        if (remainder > 0) {
            // add padding leaf
            buffer[mapping[claims.size()]] = hashLeaf(salts[claims.size()], null);
        }

        return buffer;
    }

    /**
     * Computes the hash of an internal node.
     * 
     * <p>
     * {@code H(PREFIX_NODE || left || right)}.
     * </p>
     * 
     * <p>
     * The left and right child hashes are ordered inputs; their positions are
     * significant.
     * </p>
     *
     * @param left  the left child hash
     * @param right the right child hash
     * @return the parent hash
     */
    public byte[] hashNode(byte[] left, byte[] right) {
        digest.update(PREFIX_NODE);
        digest.update(left);
        digest.update(right);
        return digest.digest();
    }

    /**
     * Computes the hash representing a salted leaf claim.
     *
     * <p>
     * Including a salt helps protect low-entropy claim data against offline
     * dictionary attacks.
     * </p>
     *
     * @param salt the salt associated with the claim
     * @param data the claim data, or {@code null} for an empty claim
     * @return the leaf hash
     */
    public byte[] hashLeaf(byte[] salt, byte[] data) {
        digest.update(PREFIX_LEAF);
        digest.update(salt);
        if (data != null) {
            digest.update(data);
        }
        return digest.digest();
    }

    /**
     * Derives the padding hash used for a missing sibling at a specified tree
     * level.
     * 
     * <p>
     * {@code H(PREFIX_PADDING || HMAC(K, PREFIX_LEVEL_PADDING ||
     * uint32_be(level)))}.
     * </p>
     *
     * <p>
     * Level-specific padding allows incomplete levels to be handled without
     * explicitly materializing all padding nodes.
     * </p>
     *
     * @param hmac  the HMAC used to derive the padding value
     * @param level the zero-based tree level
     * @return the padding hash for the specified level
     */
    public byte[] levelPadding(Mac hmac, int level) {
        hmac.update(PREFIX_LEVEL_PADDING);
        hmac.update((byte) (level >>> 24));
        hmac.update((byte) (level >>> 16));
        hmac.update((byte) (level >>> 8));
        hmac.update((byte) level);
        var padding = hmac.doFinal();

        digest.update(PREFIX_PADDING);
        digest.update(padding);
        return digest.digest();
    }
}
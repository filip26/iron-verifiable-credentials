package com.apicatalog.smt;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
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
 */
public final class SaltedMerkleTree {

    /**
     * Disclosure data associated with the disclosed claims.
     *
     * @param salts   the salts associated with the disclosed claims
     * @param indices the original leaf indices of the disclosed claims
     * @param path    the verification path hashes
     */
    public static record Disclosure(List<byte[]> salts, List<Integer> indices, List<byte[]> path) {
    }

    private static final byte PREFIX_LEAF = 0x00;
    private static final byte PREFIX_NODE = 0x01;
    private static final byte PREFIX_PADDING = 0x02;

    private static final byte PREFIX_INDEX_PADDING = 0x10;
    private static final byte PREFIX_LEVEL_PADDING = 0x11;

    private final MessageDigest digest;
    private final int maxDepth;
    private final int maxLeaves;

    /**
     * Creates a salted Merkle tree using the specified digest and maximum depth.
     *
     * @param digest   the digest algorithm used for tree hashes
     * @param maxDepth the maximum depth of the tree
     */
    public SaltedMerkleTree(MessageDigest digest, int maxDepth) {
        this.digest = digest;
        this.maxDepth = maxDepth;
        this.maxLeaves = 1 << maxDepth;
    }

    /**
     * Computes the root representing the integrity commitment to a collection of
     * claims.
     *
     * @param hmac   the HMAC instance used for depth-based padding
     * @param claims the list of claims
     * @return the root byte array of the salted Merkle tree
     */
    public byte[] generateRoot(Mac hmac, List<byte[][]> claims) {

        if (claims.size() > this.maxLeaves) {
            throw new IllegalArgumentException(
                    "Claims size %d exceed the maximum allowed number of leaves %d."
                            .formatted(claims.size(), maxLeaves));
        }

        var salts = salts(hmac, claims.size());

        var mapping = mapping(salts);

        var leaves = leaves(
                claims,
                salts,
                mapping);

        return generateRoot(hmac, leaves);
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
            throw new IllegalArgumentException(
                    "Leaves size %d exceed the maximum allowed number %d."
                            .formatted(buffer.length, maxLeaves));
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
     * Generates the verification path required to authenticate selected claims
     * against a root.
     *
     * @param hmac     the HMAC instance used for depth-based padding
     * @param claims   the list of claims
     * @param dislosed the list of ordered claim indices to disclose
     * @return the disclosure associated with the disclosed claims
     */
    public Disclosure generatePath(Mac hmac, List<byte[][]> claims, List<Integer> dislosed) {

        if (claims.size() > this.maxLeaves) {
            throw new IllegalArgumentException(
                    "Claims size %d exceed the maximum allowed number of leaves %d."
                            .formatted(claims.size(), maxLeaves));
        }

        var salts = salts(hmac, claims.size());

        var mapping = mapping(salts);

        var leaves = leaves(
                claims,
                salts,
                mapping,
                dislosed);

        var path = generatePath(hmac, leaves);

        var selectedSalts = new ArrayList<byte[]>(dislosed.size());
        var indices = new ArrayList<Integer>(dislosed.size());

        for (int i = 0; i < dislosed.size(); i++) {
            indices.add(mapping[dislosed.get(i)]);
            selectedSalts.add(salts[i]);
        }

        return new Disclosure(selectedSalts, indices, path);
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
            throw new IllegalArgumentException(
                    "Leaves size %d exceed the maximum allowed number of leaves %d."
                            .formatted(buffer.length, maxLeaves));
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
    public byte[] recreateRoot(List<byte[][]> claims, List<byte[]> salts, List<Integer> indices, List<byte[]> path) {

        if (claims.size() > maxLeaves) {
            throw new IllegalArgumentException(
                    "Claims size %d exceed the maximum allowed number of leaves %d."
                            .formatted(claims.size(), maxLeaves));
        }

        if (claims.size() != salts.size()) {
            throw new IllegalArgumentException(
                    "Claims length %d must match salts length %d."
                            .formatted(claims.size(), salts.size()));
        }

        if (claims.size() != indices.size()) {
            throw new IllegalArgumentException(
                    "Claims length %d must match indices length %d."
                            .formatted(claims.size(), indices.size()));
        }

        var mapping = new Integer[indices.size()];

        for (int i = 0; i < mapping.length; i++) {
            mapping[i] = i;
        }

        Arrays.parallelSort(mapping, Comparator.comparing(i -> indices.get(i)));

        var buffer = new byte[claims.size()][];

        int levelSize = buffer.length;

        // reconstruct leaves
        for (int i = 0; i < levelSize; i++) {

            if (indices.get(mapping[i]) > this.maxLeaves) {
                throw new IllegalArgumentException(
                        "Leaf index %d exceeds maximum allowed index %d."
                                .formatted(indices.get(mapping[i]), this.maxLeaves - 1));
            }

            buffer[i] = hashLeaf(salts.get(mapping[i]), claims.get(mapping[i]));
            mapping[i] = indices.get(mapping[i]);
        }

        int level = 0;
        int pathIndex = 0;

        // reconstruct root
        while (levelSize > 1 || pathIndex < path.size()) {

            if (level > this.maxDepth) {
                throw new IllegalArgumentException(
                        "Maximum tree depth %d exceeded during root reconstruction."
                                .formatted(this.maxDepth));
            }

            int nextLevelSize = 0;

            for (int i = 0; i < levelSize; i++) {

                var nodeIndex = mapping[i];

                final byte[] left;
                final byte[] right;

                if ((nodeIndex & 1) == 1) {

                    if (pathIndex < path.size()) {
                        left = path.get(pathIndex++);
                        right = buffer[i];

                    } else {
                        throw new IllegalArgumentException(
                                "The path is truncated, expected left hash at index %d but the path length is %d."
                                        .formatted(pathIndex, path.size()));
                    }

                } else if (i + 1 < levelSize && ((nodeIndex + 1) == mapping[i + 1])) {
                    left = buffer[i];
                    right = buffer[i + 1];
                    i++;

                } else if (pathIndex < path.size()) {
                    left = buffer[i];
                    right = path.get(pathIndex++);

                } else {
                    throw new IllegalArgumentException(
                            "The path is truncated, expected right hash at index %d but the path length is %d."
                                    .formatted(pathIndex, path.size()));
                }

                buffer[nextLevelSize] = hashNode(left, right);
                mapping[nextLevelSize] = nodeIndex >>> 1;
                nextLevelSize++;

            }
            levelSize = nextLevelSize;
            level++;
        }

        return buffer[0];
    }

    /**
     * Reconstructs a root from disclosed claims and their disclosure.
     *
     * <p>
     * The reconstructed root can be compared with a trusted root to verify the
     * integrity and positions of the disclosed claims.
     * </p>
     *
     * @param claims     the disclosed claim data
     * @param disclosure the disclosure associated with the disclosed claims
     * @return the reconstructed root hash
     */
    public byte[] recreateRoot(List<byte[][]> claims, Disclosure disclosure) {
        return recreateRoot(claims, disclosure.salts, disclosure.indices, disclosure.path);
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
    public byte[][] leaves(List<byte[][]> claims, byte[][] salts, Integer[] mapping) {
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
     * @param claims  the claim data
     * @param salts   the salts associated with the claims, including a padding salt
     *                if required
     * @param mapping the mapping from claim positions to leaf positions
     * @param filter  the sorted indices of claims to omit, or {@code null} to
     *                include all claims
     * @return the leaves for root construction or verification path generation
     */
    public byte[][] leaves(List<byte[][]> claims, byte[][] salts, Integer[] mapping, List<Integer> filter) {

        if (claims.size() > maxLeaves) {
            throw new IllegalArgumentException();
        }

        if (claims.size() > salts.length) {
            throw new IllegalArgumentException();
        }

        if (salts.length != mapping.length) {
            throw new IllegalArgumentException();
        }

        var buffer = new byte[salts.length][];

        var selectedIndex = 0;

        for (int i = 0; i < salts.length; i++) {

            if (filter == null
                    || selectedIndex >= filter.size()
                    || filter.get(selectedIndex) != i) {

                buffer[mapping[i]] = hashLeaf(salts[i], i < claims.size() ? claims.get(i) : null);

            } else {
                buffer[mapping[i]] = null;
                selectedIndex++;
            }
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
    public byte[] hashLeaf(byte[] salt, byte[][] data) {
        digest.update(PREFIX_LEAF);
        digest.update(salt);
        if (data != null) {
            for (var i = 0; i < data.length; i++) {
                digest.update(data[i]);
            }
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

    /**
     * Returns the maximum depth of the tree.
     *
     * @return the maximum depth
     */
    public int maxDepth() {
        return maxDepth;
    }

    /**
     * Returns the maximum number of leaves supported by the tree.
     *
     * @return the maximum number of leaves
     */
    public int maxLeaves() {
        return maxLeaves;
    }
}
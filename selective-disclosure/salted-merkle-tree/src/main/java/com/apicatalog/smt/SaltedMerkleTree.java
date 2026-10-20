package com.apicatalog.smt;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Provides methods to generate a balanced binary salted Merkle tree root from
 * leaves, as well verification paths generation and the root reconstruction.
 */
public class SaltedMerkleTree {

    static final byte PREFIX_LEAF = 0x00;
    static final byte PREFIX_NODE = 0x01;
    static final byte PREFIX_PADDING = 0x02;

    static final byte PREFIX_INDEX = 0x10;
    static final byte PREFIX_LEVEL_PADDING = 0x11;

    private final MessageDigest digest;

    SaltedMerkleTree(MessageDigest digest) {
        this.digest = digest;
    }

    public static SaltedMerkleTree newInstance(MessageDigest digest) {
        return new SaltedMerkleTree(digest);
    }

    public byte[] generateRoot(Mac hmac, byte[][] buffer) {

        final var depth = 32 - Integer.numberOfLeadingZeros(buffer.length - 1);

        var levelSize = buffer.length;

        for (int level = 0; level < depth; level++) {

            for (int index = 0; index < levelSize; index += 2) {

                var left = buffer[index];

                byte[] right = null;

                // if the current level size is even number
                if ((index + 1) < levelSize) {
                    right = buffer[index + 1];

                } else {
                    // or use depth-based padding
                    right = levelPadding(hmac, level);
                    levelSize++;
                }

                buffer[index >>> 1] = nodeHash(left, right);

            }
            levelSize = levelSize >>> 1;
        }

        return buffer[0];
    }

    public List<byte[]> generatePath(Mac hmac, byte[][] buffer) {

        final var path = new ArrayList<byte[]>();

        final var depth = 32 - Integer.numberOfLeadingZeros(buffer.length - 1);

        var levelSize = buffer.length;

        for (int level = 0; level < depth; level++) {

            for (int index = 0; index < levelSize; index += 2) {

                if ((index + 1) == levelSize) {
                    buffer[levelSize] = levelPadding(hmac, level);
                    levelSize++;
                }

                if (buffer[index] == null) {

                    if (buffer[index + 1] != null) {
                        path.add(buffer[index + 1]);
                    }

                    buffer[index >>> 1] = null;

                } else if (buffer[index + 1] == null) {
                    path.add(buffer[index]);
                    buffer[index >>> 1] = null;

                } else {
                    buffer[index >>> 1] = nodeHash(buffer[index], buffer[index + 1]);
                }
            }

            levelSize = levelSize >>> 1;
        }

        return path;
    }

    public byte[] recreateRoot(byte[][] claims, byte[][] salts, int[] indices, byte[][] path) {

        var mapping = new Integer[indices.length];
        for (int i = 0; i < mapping.length; i++) {
            mapping[i] = i;
        }

        Arrays.parallelSort(mapping, Comparator.comparing(i -> indices[i]));

        var buffer = new byte[claims.length][];

        int levelSize = buffer.length;

        // reconstruct leaves
        for (int i = 0; i < levelSize; i++) {
            buffer[i] = leafHash(salts[mapping[i]], claims[mapping[i]]);
            mapping[i] = indices[mapping[i]];
        }

        int pathIndex = 0;

        // reconstruct root
        while (levelSize > 1 || pathIndex < path.length) {

            int nextLevelSize = 0;

            for (int i = 0; i < levelSize; i++) {

                var nodeIndex = mapping[i];

                if ((nodeIndex & 1) == 1) {
                    var left = path[pathIndex++];
                    var right = buffer[i];

                    buffer[nextLevelSize] = nodeHash(left, right);

                    mapping[nextLevelSize] = nodeIndex >>> 1;

                } else if (i + 1 < levelSize && ((nodeIndex + 1) == mapping[i + 1])) {
                    buffer[nextLevelSize] = nodeHash(buffer[i], buffer[i + 1]);
                    mapping[nextLevelSize] = nodeIndex >>> 1;
                    i++;

                } else {
                    var left = buffer[i];
                    var right = path[pathIndex++];

                    buffer[nextLevelSize] = nodeHash(left, right);
                    mapping[nextLevelSize] = nodeIndex >>> 1;
                }
                nextLevelSize++;

            }
            levelSize = nextLevelSize;
        }

        return buffer[0];
    }

    public byte[][] salts(Mac hmac, int size) {

        // size must be even number
        var salts = new byte[size + (size & 1)][];

        for (var i = 0; i < salts.length; i++) {
            hmac.update((byte) PREFIX_INDEX);
            hmac.update((byte) i); // FIXME
            salts[i] = hmac.doFinal();
        }
        return salts;
    }

    public Integer[] mapping(byte[][] salts) {

        var mapping = new Integer[salts.length];

        for (int i = 0; i < mapping.length; i++) {
            mapping[i] = i;
        }

        Arrays.parallelSort(mapping, Comparator.comparing(i -> salts[i], Arrays::compare));

        return mapping;
    }

    // Computes an array of H(PREFIX_LEAF || SALT || DATA) leaves. Initializes the
    // instance.
    public byte[][] leaves(byte[][] claims, byte[][] salts, Integer[] mapping) {
        return leaves(claims, salts, mapping, null);
    }

    // Computes an array of H(PREFIX_LEAF || SALT || DATA) for undisclosed leaves.
    // Initializes the instance.
    public byte[][] leaves(byte[][] claims, byte[][] salts, Integer[] mapping, int[] selected) {

        var remainder = claims.length & 1;

        var buffer = new byte[claims.length + remainder][];

        var selectedIndex = 0;

        for (int i = 0; i < claims.length; i++) {

            if (selected == null
                    || selectedIndex >= selected.length
                    || selected[selectedIndex] != i) {

                buffer[mapping[i]] = leafHash(salts[i], claims[i]);

            } else {
                selectedIndex++;
            }
        }

        if (remainder > 0) {
            // add padding leaf
            buffer[mapping[claims.length]] = leafHash(salts[claims.length], null);
        }

        return buffer;
    }

    protected byte[] nodeHash(byte[] left, byte[] right) {
        digest.update(PREFIX_NODE);
        digest.update(left);
        digest.update(right);
        return digest.digest();
    }

    protected byte[] leafHash(byte[] salt, byte[] data) {
        digest.update(PREFIX_LEAF);
        digest.update(salt);
        if (data != null) {
            digest.update(data);
        } else {
            digest.update((byte) 0);
        }
        return digest.digest();
    }

    protected byte[] levelPadding(Mac hmac, int level) {
        hmac.update(PREFIX_LEVEL_PADDING);
        hmac.update((byte) level);
        var salt = hmac.doFinal();

        digest.update(PREFIX_PADDING);
        digest.update(salt);
        return digest.digest();
    }

    /**
     * Returns a Graphviz DOT representation
     */
    public void writeDot(Writer writer, int claims, int leaves, Integer[] mapping, int[] indices) {
        try (var print = new PrintWriter(writer)) {
            writeDot(print, claims, leaves, mapping, indices);
        }
    }

    /**
     * Returns a Graphviz DOT representation
     */
    public void writeDot(PrintWriter writer, int claims, int leaves, Integer[] mapping,
            int[] indices) {

        final int paddedSize = (leaves == 1)
                ? 1
                : Integer.highestOneBit(leaves - 1) << 1;

        writer.println("""
                       digraph MerkleTree {
                         rankdir=TB;
                         node [shape=box, fontname=\"monospace\"];
                         edge [color=gray50];
                       """);

        final var depth = 32 - Integer.numberOfLeadingZeros(leaves - 1);

        var path = generatePathIndices(leaves, indices);
        IO.println(path);
        int levelSize = leaves;
        int paddedLevelSize = paddedSize;

        for (int level = 0; level <= depth; level++) {

            for (int index = 0; index < levelSize; index++) {
                writer.print("  N");
                writer.print(level);
                writer.print('_');
                writer.print(index);
                writer.print(" [label=");

                if (level == 0) {
                    writer.print("<Leaf<sub>");
                    writer.printf("%d", index + 1);
                    writer.print("</sub>>");

                } else if (level == depth) {
                    writer.print("<Root 1:");
                    writer.print(leaves);
                    writer.print('>');

                } else {
                    var step = paddedSize / paddedLevelSize;
                    writer.print("\"Node ");
                    writer.print(step * index + 1);
                    writer.print(":");
                    writer.print(step * (index + 1));
                    writer.print('"');
                }

                if (level == depth) {
                    writer.print(", style=filled, fillcolor=lightgoldenrod1");

                } else if (path.contains(level * leaves + index)) {
                    writer.print(", style=filled, fillcolor=lightsalmon");
                }

                writer.println("];");

                if (level > 0) {
                    writer.print("  N");
                    writer.print(level);
                    writer.print('_');
                    writer.print(index);
                    writer.print(" -> N");
                    writer.print(level - 1);
                    writer.print('_');
                    writer.print(index * 2);
                    writer.print(',');
                    writer.print(" N");
                    writer.print(level - 1);
                    writer.print('_');
                    writer.print(index * 2 + 1);
                    writer.println(';');
                }
            }

            if (levelSize > 1 && levelSize % 2 == 1) {
                writer.print("  N");
                writer.print(level);
                writer.print('_');
                writer.print(levelSize);
                writer.print(" [label=<");
                writer.print("Node P<sub>");
                writer.print(level);
                writer.print("</sub> >");

                if (path.contains(level * leaves + levelSize)) {
                    writer.print(", style=filled, fillcolor=lightsalmon");
                }
                writer.println("];");
            }

            levelSize = (levelSize + 1) >>> 1;
            paddedLevelSize = (levelSize == 1)
                    ? 1
                    : Integer.highestOneBit(levelSize - 1) << 1;
        }

        int selectionIndex = 0;

        for (var i = 0; i < leaves; i++) {

            writer.print("  S");
            writer.print(mapping[i]);
            writer.print(" [label=");
            writer.print("<Salt<sub>");
            writer.printf("%d", i + 1);
            writer.print("</sub>>");

            if (i < claims) {
                if (selectionIndex < indices.length && mapping[i] == indices[selectionIndex]) {
                    writer.print(", style=filled, fillcolor=palegreen");
                } else {
                    writer.print(", style=filled, fillcolor=lightyellow");
                }
            } else {
                writer.print(", style=filled, fillcolor=grey80");

            }
            writer.println("];");

            writer.print("  C");
            writer.print(mapping[i]);
            writer.print(" [label=");
            if (i < claims) {
                writer.print("<Claim<sub>");
                writer.printf("%d", i + 1);
                writer.print("</sub>>");

                if (selectionIndex < indices.length && mapping[i] == indices[selectionIndex]) {
                    writer.print(", style=filled, fillcolor=palegreen");
                    selectionIndex++;

                } else {
                    writer.print(", style=filled, fillcolor=lightyellow");
                }

            } else {
                writer.print("\"Empty\"");
                writer.print(", style=filled, fillcolor=grey80");
            }
            writer.println("];");

            writer.print("  N");
            writer.print(0);    // level 0
            writer.print('_');
            writer.print(mapping[i]);
            writer.print(" -> C");
            writer.print(mapping[i]);
            writer.print(", S");
            writer.print(mapping[i]);
            writer.println(';');
        }

        writer.println('}');
    }

    public Set<Integer> generatePathIndices(int leaves, int[] indices) {

        final var path = new TreeSet<Integer>();

        final var depth = 32 - Integer.numberOfLeadingZeros(leaves - 1);

        final var buffer = new boolean[leaves];

        for (var i = 0; i < indices.length; i++) {
            buffer[indices[i]] = true;
        }

        var levelSize = leaves;

        for (int level = 0; level < depth; level++) {

            for (int index = 0; index < levelSize; index += 2) {

                if ((index + 1) == levelSize) {
                    buffer[levelSize] = false;
                    levelSize++;
                }

                if (buffer[index]) {
                    if (!buffer[index + 1]) {
                        path.add(level * leaves + index + 1);
                    }
                    buffer[index >>> 1] = true;

                } else if (buffer[index + 1]) {
                    path.add(level * leaves + index);
                    buffer[index >>> 1] = true;

                } else {
                    buffer[index >>> 1] = false;
                }
            }

            levelSize = levelSize >>> 1;
        }

        return path;
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        var claims = new byte[20][];

        for (var i = 0; i < claims.length; i++) {
            claims[i] = ("Leaf #" + (i + 1)).getBytes(StandardCharsets.UTF_8);
        }

        var digest = MessageDigest.getInstance("SHA-256");

        var hmacKey = new byte[32];

        var random = new SecureRandom();
        random.nextBytes(hmacKey);

        IO.println(HexFormat.of().formatHex(hmacKey));

//        var hmacKey = HexFormat.of().parseHex("fd212eaefbae1f5a5bb9285525b62d5c167375cc0e21aa17ad61c679de53a84f");

        hmacKey = HexFormat.of().parseHex("a61b763c687c28a4064bb2d7b06e2e5fb25d4f1ea226b611d11d846768058630");
        
        final String type = "HmacSHA256";
        final SecretKeySpec key = new SecretKeySpec(hmacKey, type);

        final Mac hmac = Mac.getInstance(type);
        hmac.init(key);

        var tree = SaltedMerkleTree.newInstance(digest);

        var salts = tree.salts(hmac, claims.length);

        var mapping = tree.mapping(salts);

        var leaves = tree.leaves(claims, salts, mapping);

        var root = tree.generateRoot(hmac, leaves);

        IO.println("root     \t= " + HexFormat.of().formatHex(root));
        IO.println("mapping  \t= " + Arrays.toString(mapping));

        var selection = new int[] { 0, 1, 7, 10 };

        IO.println("selection\t= " + Arrays.toString(selection));

        leaves = tree.leaves(claims, salts, mapping, selection);

        var path = tree.generatePath(hmac, leaves);
        IO.println("path     \t= (" + path.size() + ")");

        var selectedClaims = new byte[selection.length][];
        var selectedSalts = new byte[selection.length][];
        var selectedIndices = new int[selection.length];

        for (var i = 0; i < selection.length; i++) {
            selectedClaims[i] = claims[selection[i]];
            selectedSalts[i] = salts[selection[i]];
            selectedIndices[i] = mapping[selection[i]];
        }

        IO.println("indices  \t= " + Arrays.toString(selectedIndices));

        var ref = tree.recreateRoot(
                selectedClaims,
                selectedSalts,
                selectedIndices,
                path.stream().toArray(byte[][]::new));

        IO.println("ref      \t= " + HexFormat.of().formatHex(ref));

        Path file = Path.of(System.getProperty("user.home"), "merkle-tree.dot");
        try (var writer = new StringWriter()) {
            tree.writeDot(writer, claims.length, leaves.length, mapping, selectedIndices);
            Files.writeString(file, writer.toString());
            IO.println("DOT file written to: " + file);
        }

    }
}
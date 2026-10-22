package com.apicatalog.smt;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Set;
import java.util.TreeSet;

public class TopologyDotWriter implements AutoCloseable {

    final PrintWriter writer;

    public TopologyDotWriter(Writer writer) {
        this.writer = new PrintWriter(writer);
    }

    /**
     * Writes a Graphviz DOT representation
     */
    public final void write(
            int claims,
            Integer[] mapping,
            int[] indices) {

        final int leaves = claims + (claims & 1);
        final int paddedSize = (leaves == 1)
                ? 1
                : Integer.highestOneBit(leaves - 1) << 1;

        writer.println("""
                       digraph SaltedMerkleTree {
                         rankdir=TB;
                         node [shape=box, fontname=\"monospace\"];
                         edge [color=gray50];
                       """);

        final var depth = 32 - Integer.numberOfLeadingZeros(leaves - 1);

        var path = generatePathIndices(leaves, indices);

        int levelSize = leaves;
        int paddedLevelSize = paddedSize;

        for (int level = 0; level <= depth; level++) {

            for (int index = 0; index < levelSize; index++) {
                writer.print("  N");
                writer.print(level * leaves + index);
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
                    writer.print(level * leaves + index);
                    writer.print(" -> N");
                    writer.print((level - 1) * leaves + index * 2);
                    writer.print(',');
                    writer.print(" N");
                    writer.print((level - 1) * leaves + index * 2 + 1);
                    writer.println(';');
                }
            }

            if (levelSize > 1 && levelSize % 2 == 1) {
                writer.print("  N");
                writer.print(level * leaves + levelSize);
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
            writer.print(mapping[i]); // level 0
            writer.print(" -> C");
            writer.print(mapping[i]);
            writer.print(", S");
            writer.print(mapping[i]);
            writer.println(';');
        }

        writer.println('}');
    }

    public static final Set<Integer> generatePathIndices(int leaves, int[] indices) {

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

    @Override
    public void close() {
        writer.close();
    }
}
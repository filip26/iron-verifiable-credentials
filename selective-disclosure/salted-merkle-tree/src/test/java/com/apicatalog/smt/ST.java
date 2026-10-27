package com.apicatalog.smt;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.random.RandomGenerator;

public class ST {

    static int[] randomIndices(int dimension, int count) {
        if (dimension < 0 || count < 0 || count > (long) dimension + 1) {
            throw new IllegalArgumentException();
        }

        int[] indices = new int[dimension + 1];
        for (int i = 0; i < dimension; i++) {
            indices[i] = i;
        }

        RandomGenerator rng = RandomGenerator.getDefault();

        for (int i = 0; i < count; i++) {
            int j = rng.nextInt(i, dimension + 1);
            int tmp = indices[i];
            indices[i] = indices[j];
            indices[j] = tmp;
        }

        return Arrays.copyOf(indices, count);
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        var max = 4500;
        
        var tiers = new int[] { max };

        Path file = Path.of(System.getProperty("user.home"), "ex.gp");
        try (var writer = new PrintWriter(Files.newBufferedWriter(file))) {

            writer.print(
                    """
                    set terminal svg size 800,500
                    set output

                    set title 'Multiple Data Series'
                    set xlabel 'Revealed Claims'
                    set ylabel 'Proof Size (hashes)'
                    set grid
                    set key top left
                    plot """);

            writer.print("'-' using 1:2 smooth bezier with lines lw 2 title 'Hash Array " + "'");

//            boolean first = true;
            for (var tier : tiers) {
                writer.println(",  \\");
                writer.print("    ");
                writer.print("'-' using 1:2 smooth bezier with lines lw 2 title 'Merkle Tree'");
            }

            writer.println();
            writer.println();

            for (var i = 1; i < max; i++) {
                writer.println(i + " " + (max + i));
            }

            writer.println("e");

            
            
            for (var tier : tiers) {

                for (var i = 1; i < tier; i++) {

                    var path = TopologyDotWriter.generatePathIndices(tier, randomIndices(tier, i));
                    writer.println(i + " " + (path.size() + i));
                }

                writer.println("e");
            }

            IO.println("GnuPlot file written to: " + file);
        }

    }
}

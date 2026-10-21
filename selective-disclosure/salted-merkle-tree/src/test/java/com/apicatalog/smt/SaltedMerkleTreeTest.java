package com.apicatalog.smt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

public class SaltedMerkleTreeTest {

    @Test
    void testWorkflow() {

    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        var claims = new byte[9][];

        for (var i = 0; i < claims.length; i++) {
            claims[i] = ("Leaf #" + (i + 1)).getBytes(StandardCharsets.UTF_8);
        }

        var digest = MessageDigest.getInstance("SHA-256");

        var hmacKey = new byte[32];

        var random = new SecureRandom();
        random.nextBytes(hmacKey);

        IO.println(HexFormat.of().formatHex(hmacKey));

//        var hmacKey = HexFormat.of().parseHex("fd212eaefbae1f5a5bb9285525b62d5c167375cc0e21aa17ad61c679de53a84f");

//        hmacKey = HexFormat.of().parseHex("a61b763c687c28a4064bb2d7b06e2e5fb25d4f1ea226b611d11d846768058630");

        final String type = "HmacSHA256";
        final SecretKeySpec key = new SecretKeySpec(hmacKey, type);

        final Mac hmac = Mac.getInstance(type);
        hmac.init(key);

        var tree = new SaltedMerkleTree(digest);

        var salts = tree.salts(hmac, claims.length);

        var mapping = tree.mapping(salts);

        var leaves = tree.leaves(claims, salts, mapping);

        var root = tree.generateRoot(hmac, leaves);

        IO.println("root     \t= " + HexFormat.of().formatHex(root));
        IO.println("mapping  \t= " + Arrays.toString(mapping));

        var selection = new int[] {};

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

//        var ref = tree.recreateRoot(
//                selectedClaims,
//                selectedSalts,
//                selectedIndices,
//                path.stream().toArray(byte[][]::new));
//
//        IO.println("ref      \t= " + HexFormat.of().formatHex(ref));

        Path file = Path.of(System.getProperty("user.home"), "merkle-tree.dot");
        try (var writer = new TopologyDotWriter(Files.newBufferedWriter(file))) {
            writer.write(claims.length, mapping, selectedIndices);
            IO.println("DOT file written to: " + file);
        }
    }
}

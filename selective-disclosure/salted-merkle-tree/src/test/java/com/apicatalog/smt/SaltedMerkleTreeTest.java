package com.apicatalog.smt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class SaltedMerkleTreeTest {

    @Test
    void testJson() throws NoSuchAlgorithmException, InvalidKeyException, IOException {

        var json = Map.of("name", "Filip", "city", "Prague", "country", "Czech Republic");

        var claims = JsonClaims.from(json);

        for (var claim : claims.values()) {
            IO.println(JsonClaims.toString(claim));
        }

        var digest = MessageDigest.getInstance("SHA-256");

        var hmacSecretKey = new SecretKeySpec(
                HexFormat.of().parseHex("00112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF"),
                "HmacSHA256");

        final Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(hmacSecretKey);

        var tree = new SaltedMerkleTree(digest, 8);

        var salts = tree.salts(hmac, claims.size());

        var mapping = tree.mapping(salts);

        var leaves = tree.leaves(
                claims.values(),
                salts,
                mapping);

        for (var x : leaves) {
            IO.println("leaf > " + (x != null ? HexFormat.of().formatHex(x) : null));
        }

        var root = tree.generateRoot(hmac, leaves);
        IO.println("mapping  \t= " + Arrays.toString(mapping));
        IO.println("root     \t= " + HexFormat.of().formatHex(root));

        for (var salt : salts) {
            IO.println("salt> " + HexFormat.of().formatHex(salt));
        }

//        Path file = Path.of(System.getProperty("user.home"), "merkle-tree.dot");
//        try (var writer = new TopologyDotWriter(Files.newBufferedWriter(file))) {
//            writer.write(claims.size(), mapping, new int[] { 0, 2 });
//            IO.println("DOT file written to: " + file);
//        }

//        claims = JsonClaims.from(json, Set.of("/city", "/country"));
//        IO.println("C2> " + claims.size());
//        for (var claim : claims.values()) {
//            IO.println("C2> " + JsonClaims.toString(claim));
//        }

        leaves = tree.leaves(
                claims.values(),
                salts,
                mapping,
                List.of(0, 1));

        for (var x : leaves) {
            IO.println("leaf > " + (x != null ? HexFormat.of().formatHex(x) : null));
        }

        var path = tree.generatePath(hmac, leaves);

        for (var x : path) {
            IO.println("path > " + HexFormat.of().formatHex(x));
        }

        var ref = tree.recreateRoot(
                List.of(
                        JsonClaims.claim("/city", "\"Prague\"".getBytes()),
                        JsonClaims.claim("/country", "\"Czech Republic\"".getBytes())),
                new byte[][] {
                        salts[0],
                        salts[1]
                },
                new int[] {
                        mapping[0],
                        mapping[1]
                },
                path);

        IO.println("ref> " + HexFormat.of().formatHex(ref));

        assertArrayEquals(root, ref);

    }

    private static void flatten(String path, JsonValue node, List<String> result) {
        switch (node.getValueType()) {
        case OBJECT -> {
            JsonObject obj = node.asJsonObject();
            obj.forEach((key, value) -> flatten(path + "/" + escape(key), value, result));
        }
        case ARRAY -> {
            JsonArray arr = node.asJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                flatten(path + "/" + i, arr.get(i), result);
            }
        }
        default -> result.add(path + "=" + node);
        }
    }

    private static String escape(String token) {
        return token.replace("~", "~0").replace("/", "~1");
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeyException {

//        var claims = new byte[11][];
//
//        for (var i = 0; i < claims.length; i++) {
//            claims[i] = ("Leaf #" + (i + 1)).getBytes(StandardCharsets.UTF_8);
//        }
//
//        var digest = MessageDigest.getInstance("SHA-256");
//
//        var hmacKey = new byte[32];
//
//        var random = new SecureRandom();
//        random.nextBytes(hmacKey);
//
//        IO.println(HexFormat.of().formatHex(hmacKey));
//
        //// hmacKey
        /// = HexFormat.of().parseHex("fd212eaefbae1f5a5bb9285525b62d5c167375cc0e21aa17ad61c679de53a84f");
//
//        hmacKey = HexFormat.of().parseHex("a61b763c687c28a4064bb2d7b06e2e5fb25d4f1ea226b611d11d846768058630");
//
        //// hmacKey
        /// = HexFormat.of().parseHex("FFEEDDCCBBAA99887766554433221100FFEEDDCCBBAA99887766554433221100");
//        final String type = "HmacSHA256";
//        final SecretKeySpec key = new SecretKeySpec(hmacKey, type);
//
//        final Mac hmac = Mac.getInstance(type);
//        hmac.init(key);
//
//        hmac.update("c14n0".getBytes());
//        
//        IO.println(":" + HexFormat.of().formatHex(hmac.doFinal()));
//        
//        var tree = new SaltedMerkleTree(digest, 8);
//
//        var salts = tree.salts(hmac, claims.length + 4);
//
//        var mapping = tree.mapping(salts);
//
//        var leaves = tree.leaves(claims, salts, mapping);
//
//        var root = tree.generateRoot(hmac, leaves);
//
//        IO.println("root     \t= " + HexFormat.of().formatHex(root));
//        IO.println("mapping  \t= " + Arrays.toString(mapping));
//
        //// var selection = new int[] { 2, 3 };
//        var selection = new int[] { 0, 1, 5, 9 };
//
//        IO.println("selection\t= " + Arrays.toString(selection));
//
//        leaves = tree.leaves(claims, salts, mapping, selection);
//
//        var path = tree.generatePath(hmac, leaves);
//        IO.println("path     \t= (" + path.size() + ")");
//
//        var selectedClaims = new byte[selection.length][];
//        var selectedSalts = new byte[selection.length][];
//        var selectedIndices = new int[selection.length];
//
//        for (var i = 0; i < selection.length; i++) {
//            selectedClaims[i] = claims[selection[i]];
//            selectedSalts[i] = salts[selection[i]];
//            selectedIndices[i] = mapping[selection[i]];
//        }
//
//        IO.println("indices  \t= " + Arrays.toString(selectedIndices));
//
//        var ref = tree.recreateRoot(
//                selectedClaims,
//                selectedSalts,
//                selectedIndices,
//                path.stream().toArray(byte[][]::new));
//
//        IO.println("ref      \t= " + HexFormat.of().formatHex(ref));
//
//        Path file = Path.of(System.getProperty("user.home"), "merkle-tree.dot");
//        try (var writer = new TopologyDotWriter(Files.newBufferedWriter(file))) {
//            writer.write(salts.length, mapping, selectedIndices);
//            IO.println("DOT file written to: " + file);
//        }
    }
}

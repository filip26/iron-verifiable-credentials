package com.apicatalog.smt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

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

        var sd = new Sd(tree);

        var root = sd.commitment(hmac, claims.values());
        
//        tree.generateRoot(hmac, claims.values());
        
//        var salts = tree.salts(hmac, claims.size());
//
//        var mapping = tree.mapping(salts);
//
//        var leaves = tree.leaves(
//                claims.values(),
//                salts,
//                mapping);
//
//        for (var x : leaves) {
//            IO.println("leaf > " + (x != null ? HexFormat.of().formatHex(x) : null));
//        }
//
//        var root = tree.generateRoot(hmac, leaves);
//        IO.println("mapping  \t= " + Arrays.toString(mapping));
//        IO.println("root     \t= " + HexFormat.of().formatHex(root));
//
//        for (var salt : salts) {
//            IO.println("salt> " + HexFormat.of().formatHex(salt));
//        }

//        Path file = Path.of(System.getProperty("user.home"), "merkle-tree.dot");
//        try (var writer = new TopologyDotWriter(Files.newBufferedWriter(file))) {
//            writer.write(claims.size(), mapping, new int[] { 0, 2 });
//            IO.println("DOT file written to: " + file);
//        }

        claims = JsonClaims.from(json, Set.of("/city", "/country"));
        IO.println("C2> " + claims.size() + ", indices " + claims.selected());
        for (var claim : claims.values()) {
            IO.println("C2> " + JsonClaims.toString(claim));
        }

        var disclosure = sd.disclose(hmac, claims.values(), claims.selected());
        
//        leaves = tree.leaves(
//                claims.values(),
//                salts,
//                mapping,
//                claims.selected());
//
//        for (var x : leaves) {
//            IO.println("leaf > " + (x != null ? HexFormat.of().formatHex(x) : null));
//        }
//
//        var path = tree.generatePath(hmac, leaves);
        assertNotNull(disclosure);

        for (var x : disclosure.path()) {
            IO.println("path > " + HexFormat.of().formatHex(x));
        }
        
        var ref = sd.commitment(
                List.of(
                      JsonClaims.claim("/city", "\"Prague\"".getBytes()),
                      JsonClaims.claim("/country", "\"Czech Republic\"".getBytes())),
                disclosure);
//
//        var ref = tree.recreateRoot(
//                List.of(
//                        JsonClaims.claim("/city", "\"Prague\"".getBytes()),
//                        JsonClaims.claim("/country", "\"Czech Republic\"".getBytes())),
//                List.of(
//                        salts[0],
//                        salts[1]),
//                List.of(mapping[0], mapping[1]),
//                path);
//
        IO.println("ref> " + HexFormat.of().formatHex(ref));

        assertArrayEquals(root, ref);

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

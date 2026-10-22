package com.apicatalog.smt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

import com.apicatalog.jcs.Jcs;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class SaltedMerkleTreeTest {

    @Test
    void testJSON() throws NoSuchAlgorithmException, InvalidKeyException, IOException {

        var json = Map.of("name", "Filip", "city", "Prague", "country", "Czech Republic");

        // canonicalize
        var canonized = new String(Jcs.canonize(json), StandardCharsets.UTF_8);

        IO.println(canonized);

        var claims = new ArrayList<String>();

        try (var reader = Json.createReader(new StringReader(canonized))) {
            // flatten
            flatten("", reader.read(), claims);
        }

        for (var claim : claims) {
            IO.println(claim);
        }

        var digest = MessageDigest.getInstance("SHA-256");

        var hmacSecretKey = new SecretKeySpec(
                HexFormat.of().parseHex("00112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF"),
                "HmacSHA256");

        final Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(hmacSecretKey);

        var tree = new SaltedMerkleTree(digest);

        var salts = tree.salts(hmac, claims.size());

        var mapping = tree.mapping(salts);

        var leaves = tree.leaves(
                claims.stream().map(String::getBytes).toArray(byte[][]::new),
                salts,
                mapping);

        var root = tree.generateRoot(hmac, leaves);
        IO.println("mapping  \t= " + Arrays.toString(mapping));
        IO.println("root     \t= " + HexFormat.of().formatHex(root));

        for (var salt: salts) {
            IO.println(HexFormat.of().formatHex(salt));
        }

        Path file = Path.of(System.getProperty("user.home"), "merkle-tree.dot");
        try (var writer = new TopologyDotWriter(Files.newBufferedWriter(file))) {
            writer.write(claims.size(), mapping, new int[] { 0, 2 });
            IO.println("DOT file written to: " + file);
        }

        
        leaves = tree.leaves(
                claims.stream().map(String::getBytes).toArray(byte[][]::new),
                salts,
                mapping,
                new int[] { 0, 1 });

        var path = tree.generatePath(hmac, leaves);

        for (var x : path) {
            IO.println(HexFormat.of().formatHex(x));
        }
        
        var ref = tree.recreateRoot(
                new byte[][] {
                        "/city=\"Prague\"".getBytes(),
                        "/country=\"Czech Republic\"".getBytes()
                },
                new byte[][] {
                        salts[0],
                        salts[1]
                },
                new int[] {
                        mapping[0],
                        mapping[1]
                },
                path.stream().toArray(byte[][]::new));

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

        var claims = new byte[5][];

        for (var i = 0; i < claims.length; i++) {
            claims[i] = ("Leaf #" + (i + 1)).getBytes(StandardCharsets.UTF_8);
        }

        var digest = MessageDigest.getInstance("SHA-256");

        var hmacKey = new byte[32];

        var random = new SecureRandom();
        random.nextBytes(hmacKey);

        IO.println(HexFormat.of().formatHex(hmacKey));

//        var hmacKey = HexFormat.of().parseHex("fd212eaefbae1f5a5bb9285525b62d5c167375cc0e21aa17ad61c679de53a84f");

        //hmacKey = HexFormat.of().parseHex("a61b763c687c28a4064bb2d7b06e2e5fb25d4f1ea226b611d11d846768058630");

        hmacKey = HexFormat.of().parseHex("FFEEDDCCBBAA99887766554433221100FFEEDDCCBBAA99887766554433221100");
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

        var selection = new int[] { 2, 3 };

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
        try (var writer = new TopologyDotWriter(Files.newBufferedWriter(file))) {
            writer.write(claims.length, mapping, selectedIndices);
            IO.println("DOT file written to: " + file);
        }
    }
}

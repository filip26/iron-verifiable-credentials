package com.apicatalog.smt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

import javax.crypto.Mac;

public class Sd {

    final SaltedMerkleTree smt;

    public Sd(SaltedMerkleTree smt) {
        this.smt = smt;
    }

    public static record Disclosure(List<byte[]> salts, List<Integer> indices, List<byte[]> path) {

    }

    public byte[] commitment(Mac hmac, List<byte[][]> claims) {

        var salts = smt.salts(hmac, claims.size());

        var mapping = smt.mapping(salts);

        var leaves = smt.leaves(
                claims,
                salts,
                mapping);

        for (var x : leaves) {
            IO.println("leaf > " + (x != null ? HexFormat.of().formatHex(x) : null));
        }

        var root = smt.generateRoot(hmac, leaves);
        IO.println("mapping  \t= " + Arrays.toString(mapping));
        IO.println("root     \t= " + HexFormat.of().formatHex(root));

        for (var salt : salts) {
            IO.println("salt> " + HexFormat.of().formatHex(salt));
        }

        return root;
    }

    public Disclosure disclose(Mac hmac, List<byte[][]> claims, List<Integer> dislosed) {

        var salts = smt.salts(hmac, claims.size());

        var mapping = smt.mapping(salts);

        var leaves = smt.leaves(
                claims,
                salts,
                mapping,
                dislosed);

        for (var x : leaves) {
            IO.println("leaf > " + (x != null ? HexFormat.of().formatHex(x) : null));
        }

        var path = smt.generatePath(hmac, leaves);

        var selectedSalts = new ArrayList<byte[]>(dislosed.size());
        var indices = new ArrayList<Integer>(dislosed.size());

        for (int i = 0; i < dislosed.size(); i++) {
            indices.add(mapping[dislosed.get(i)]);
            selectedSalts.add(salts[i]);
        }

        return new Disclosure(selectedSalts, indices, path);
    }

    public byte[] commitment(List<byte[][]> claims, Disclosure disclosure) {
        return commitment(claims, disclosure.salts, disclosure.indices, disclosure.path);
    }

    public byte[] commitment(List<byte[][]> claims, List<byte[]> salts, List<Integer> indices, List<byte[]> path) {
        return smt.recreateRoot(claims, salts, indices, path);
    }
}

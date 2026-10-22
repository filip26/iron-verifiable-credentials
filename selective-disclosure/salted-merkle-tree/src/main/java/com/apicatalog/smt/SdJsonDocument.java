package com.apicatalog.smt;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SdJsonDocument {

    public static List<byte[]> flatten(Map<String, Object> document) {

        var flattened = new ArrayList<byte[]>();
        flatten("", document, flattened);
        return flattened;
    }

    private static void flatten(String path, Object node, List<byte[]> result) {
        switch (node) {
        case Collection<?> col -> {
            var it = col.iterator();
            int i = 0;
            while (it.hasNext()) {
                flatten(path + "/" + i, it.next(), result);
                i++;
            }
        }
        case Map<?, ?> map -> {
            map.forEach((key, value) -> flatten(path + "/" + escape((String) key), value, result));

        }
        default -> {

            var pathAsBytes = path.getBytes(StandardCharsets.UTF_8);
            var valueAsBytes = node.toString().getBytes(StandardCharsets.UTF_8);

            var claim = new byte[pathAsBytes.length + valueAsBytes.length + 1];

            System.arraycopy(pathAsBytes, 0, claim, 0, pathAsBytes.length);
            System.arraycopy(new byte[] { 0 }, 0, claim, pathAsBytes.length, 1);
            System.arraycopy(valueAsBytes, 0, claim, pathAsBytes.length + 1, valueAsBytes.length);

            result.add(claim);
        }
        }
    }

    private static String escape(String token) {
        return token.replace("~", "~0").replace("/", "~1");
    }

}

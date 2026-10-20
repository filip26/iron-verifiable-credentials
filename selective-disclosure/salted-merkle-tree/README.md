# Salted Merkle Tree for Selective Disclosure  

An implementation ... https://github.com/filip26/salted-merkle-tree


### Examples

```javascript

Path file = Path.of(System.getProperty("user.home"), "merkle-tree.dot");
try (var writer = Files.newBufferedWriter(file)) {
    SaltedMerkleTree.writeDot(writer, claims, mapping, indices);
    IO.println("DOT file written to: " + file);
}

```
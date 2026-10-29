# SD Salted Merkle Tree

Privacy-preserving selective disclosure of JSON and RDF claims using [Salted Merkle Tree](https://github.com/filip26/salted-merkle-tree)..

### Examples

#### Drawing the tree topology

The following example generates a Graphviz DOT file representing the Merkle tree topology, including the claim mapping and selected claims:

```javascript

Path file = Path.of(System.getProperty("user.home"), "merkle-tree.dot");
try (var writer = new TopologyDotWriter(Files.newBufferedWriter(file))) {
    writer.write(claims.length, mapping, indices);
    IO.println("DOT file written to: " + file);
}
```

The generated `merkle-tree.dot` file can be rendered with Graphviz to visualize the tree structure and disclosure paths.



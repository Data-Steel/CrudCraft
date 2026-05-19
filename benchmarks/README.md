# CrudCraft Benchmarks

This standalone JMH project publishes lightweight performance trend data for paging behavior.

Run locally:

```bash
mvn -f benchmarks/pom.xml -B clean package
java -jar benchmarks/target/crudcraft-benchmarks-1.0.10.jar KeysetPagingBenchmark -rf json -rff benchmarks/target/jmh-results.json
```

The benchmark compares offset-style deep-page traversal with keyset-style cursor traversal using a
deterministic in-memory dataset. It is not a database benchmark; it isolates the algorithmic paging
cost so CI can publish stable JSON artifacts without external infrastructure.

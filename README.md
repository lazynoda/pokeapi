# PokeAPI cli

## How to run

Use the following command:

```
./gradlew run --args="limit=$limit offset=$offset graphQL=$graphQL"
```

Where:

* `$limit` is the _limit_ param for the API. Number. Mandatory.
* `$offset` is the _offset_ param for the API. Number. Mandatory.
* `$graphQL` uses PokeAPI GraphQL v1 (beta) instead of PokeAPI v2. Boolean. Optional. Default `false`.

Examples:

```
./gradlew run --args="limit=100 offset=23"
```

```
./gradlew run --args="limit=300 offset=9 graphQL=true"
```

## Tests

I've only created `PokeAPI` tests because it's representative of a typical test scenario: It has dependencies, mocks,
fake server responses in files...

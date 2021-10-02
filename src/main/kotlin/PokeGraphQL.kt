import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.*

class PokeGraphQL(private val httpClient: HttpClient) {

    companion object {
        private const val URL = "https://beta.pokeapi.co/graphql/v1beta"

        private val JSON_PARSER = Json
    }

    suspend fun getPokemonAggregates(limit: Int, offset: Int): Pair<Average, List<Pokemon>> = coroutineScope {

        val body = """{
    "query": "query samplePokeAPIquery { pokemon_v2_pokemon_aggregate(limit: $limit, offset: $offset) { aggregate { avg { height weight } } nodes { pokemon_v2_pokemontypes { pokemon_v2_type { name } } weight height } } }",
    "variables": null
}"""
        val jsonResponse = httpClient.postRequest(URL, body)
            .let { JSON_PARSER.parseToJsonElement(it).jsonObject }

        return@coroutineScope jsonResponse.toAggregates()
    }

    private fun JsonObject.toAggregates(): Pair<Average, List<Pokemon>> {
        val aggregate = this.getValue("data")
            .jsonObject
            .getValue("pokemon_v2_pokemon_aggregate")
            .jsonObject

        val average = aggregate.getValue("aggregate")
            .toAverage()

        val pokemonList = aggregate.getValue("nodes")
            .jsonArray
            .map(::toPokemon)

        return average to pokemonList
    }

    private fun JsonElement.toAverage() = jsonObject
        .getValue("avg")
        .jsonObject
        .let {
            Average(
                height = it.getValue("height").jsonPrimitive.double,
                weight = it.getValue("weight").jsonPrimitive.double
            )
        }

    private fun toPokemon(it: JsonElement) = it.jsonObject.let {
        Pokemon(
            height = it.getValue("height").jsonPrimitive.int,
            weight = it.getValue("weight").jsonPrimitive.int,
            types = it.getValue("pokemon_v2_pokemontypes")
                .jsonArray
                .map {
                    val name = it.jsonObject
                        .getValue("pokemon_v2_type")
                        .jsonObject
                        .getValue("name")
                        .jsonPrimitive
                        .content
                    TypeSlot(type = Type(name = name))
                }
        )
    }
}

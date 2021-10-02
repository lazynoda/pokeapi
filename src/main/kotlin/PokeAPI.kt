import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class PokeAPI(private val httpClient: HttpClient) {

    companion object {
        private const val BASE_URL = "https://pokeapi.co/api/v2"

        private val JSON_PARSER = Json { ignoreUnknownKeys = true }
    }

    suspend fun getPokemonList(limit: Int, offset: Int): List<Pokemon> = coroutineScope {

        val jsonResponse = httpClient.getRequest("$BASE_URL/pokemon/?limit=$limit&offset=$offset")
            .let { Json.parseToJsonElement(it).jsonObject }

        val pokemonUrls = jsonResponse.getValue("results")
            .jsonArray
            .mapNotNull { result ->
                result.jsonObject
                    .getValue("url")
                    .jsonPrimitive
                    .content
            }

        pokemonUrls
            .map { url -> async { getPokemon(url) } }
            .map { it.await() }
    }

    private suspend fun getPokemon(url: String): Pokemon = httpClient.getRequest(url)
        .let { JSON_PARSER.decodeFromString(it) }
}

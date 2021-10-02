object DependenciesManager {

    val pokeApi
        get() = PokeAPI(httpClient)

    val pokeGraphQL
        get() = PokeGraphQL(httpClient)

    val httpClient = HttpClient()
}

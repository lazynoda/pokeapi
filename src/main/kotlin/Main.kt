import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {

    val measuredTime = measureTimeMillis {

        val arguments = args.parseArguments()

        val limit = arguments.first
        val offset = arguments.second
        val graphQL = arguments.third

        println(
            """Arguments:
              |  - limit: $limit
              |  - offset: $offset
              |  - graphQL: $graphQL
              |""".trimMargin()
        )
        if (graphQL)
            runUsingGraphQL(limit, offset)
        else
            runUsingApi(limit, offset)
    }
    println("Execution time: ${measuredTime / 1000.0} seconds")
}

private fun Array<String>.parseArguments(): Triple<Int, Int, Boolean> {
    val args = this.asList().map { it.split("=") }

    val limit = args.find { it.first() == "limit" }
        ?.takeIf { it.size == 2 }
        ?.let { it[1].toInt() }
        ?: throw IllegalArgumentException("Missing param `limit` or its value.")

    val offset = args.find { it.first() == "offset" }
        ?.takeIf { it.size == 2 }
        ?.let { it[1].toInt() }
        ?: throw IllegalArgumentException("Missing param `offset` or its value.")

    val graphQL = args.find { it.first() == "graphQL" }
        ?.takeIf { it.size == 2 }
        ?.let { it[1].toBoolean() }
        ?: false

    return Triple(limit, offset, graphQL)
}

private fun runUsingApi(limit: Int, offset: Int) {

    val pokemonList = runBlocking { DependenciesManager.pokeApi.getPokemonList(limit, offset) }

    pokemonList.printAverages()
    pokemonList.groupByTypeSlot()
        .forEach(::printAverageByType)
}

private fun runUsingGraphQL(limit: Int, offset: Int) {

    val pokemonAggregates = runBlocking { DependenciesManager.pokeGraphQL.getPokemonAggregates(limit, offset) }

    pokemonAggregates.first.printAverages()
    pokemonAggregates.second.groupByTypeSlot()
        .forEach(::printAverageByType)
}

private fun List<Pokemon>.printAverages() {
    val avgHeight = map { it.height }.average()
    val avgWeight = map { it.weight }.average()

    println("Average height: %.3f decimetres".format(avgHeight))
    println("Average weight: %.3f hectograms".format(avgWeight))
}

private fun Average.printAverages() {
    println("Average height: %.3f decimetres".format(height))
    println("Average weight: %.3f hectograms".format(weight))
}

private fun List<Pokemon>.groupByTypeSlot() =
    flatMap { pokemon -> pokemon.types.map { type -> type.type to pokemon } }
        .groupBy({ it.first }, { it.second })

private fun printAverageByType(mapEntry: Map.Entry<Type, List<Pokemon>>) {
    val avgHeight = mapEntry.value.map { it.height }.average()
    val avgWeight = mapEntry.value.map { it.weight }.average()
    println(
        """Type ${mapEntry.key.name}
          |  Average height: %.3f decimetres
          |  Average weight: %.3f hectograms"""
            .trimMargin()
            .format(avgHeight, avgWeight)
    )
}

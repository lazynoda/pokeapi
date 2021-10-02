import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.io.IOException
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.fail

class PokeAPITest {

    //region Companion

    companion object {
        private const val BASE_URL = "https://pokeapi.co/api/v2/pokemon/"
    }

    //endregion
    //region Properties

    @Mock
    lateinit var httpClient: HttpClient

    lateinit var subjectUnderTest: PokeAPI

    //endregion
    //region Set up & Tear down

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        subjectUnderTest = PokeAPI(httpClient)
    }

    //endregion
    //region Tests

    @Test
    fun `GIVEN httpClient working WHEN get pokemon list THEN return list of pokemon`() = runBlocking {

        val limit = Random.nextInt(from = 1, until = 100)
        val offset = Random.nextInt(from = 1, until = 100)

        whenever(httpClient.getRequest(any())).thenAnswer {
            val requestedUrl = it.getArgument<String>(0)

            if ("$BASE_URL?limit=$limit&offset=$offset" == requestedUrl)
                return@thenAnswer readFileContent("pokemonList.json")

            val regex = "$BASE_URL(\\d+)/".toRegex()
            val matchResult = regex.matchEntire(requestedUrl)
                ?: throw IllegalArgumentException("Unknown URL: $requestedUrl")
            val id = matchResult.groups[1]?.value
                ?: throw IllegalStateException("Unable to get Pokemon ID from URL: $requestedUrl")

            readFileContent("pokemon$id.json")
        }

        val pokemonList = subjectUnderTest.getPokemonList(limit, offset)

        val expectedPokemonList = fakePokemonList()

        assertEquals(expectedPokemonList, pokemonList)
    }

    @Test
    fun `GIVEN httpClient returns an exception WHEN get pokemon list THEN let exception be thrown`() = runBlocking {

        val limit = Random.nextInt(from = 1, until = 100)
        val offset = Random.nextInt(from = 1, until = 100)

        val fakeException = IOException()
        whenever(httpClient.getRequest(any()))
            .thenThrow(fakeException)

        try {
            subjectUnderTest.getPokemonList(limit, offset)
            fail("Exception has not been thrown.")
        } catch (exception: IOException) {
            assertEquals(fakeException, exception.cause)
        } catch (exception: Throwable) {
            fail("Unknown exception has been thrown. $exception")
        }
    }

    //endregion
    //region Private methods

    private fun fakePokemonList() = listOf(
        Pokemon(
            height = 20,
            weight = 1000,
            types = listOf(
                TypeSlot(type = Type(name = "grass")),
                TypeSlot(type = Type(name = "poison"))
            )
        ),
        Pokemon(
            height = 6,
            weight = 85,
            types = listOf(
                TypeSlot(type = Type(name = "fire"))
            )
        ),
        Pokemon(
            height = 11,
            weight = 190,
            types = listOf(
                TypeSlot(type = Type(name = "fire"))
            )
        )
    )

    private fun readFileContent(fileName: String) =
        PokeAPI::class.java.getResource(fileName)?.readText()

    //endregion
}

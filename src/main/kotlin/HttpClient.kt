import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.jvm.Throws

class HttpClient {

    // In theory, `Dispatchers.IO` should be used for IO operations (like network requests) but I've noticed that
    // `Dispatchers.Default` offers better execution times, probably because context change is more expensive on IO.
    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(IOException::class)
    suspend fun getRequest(url: String): String = withContext(Dispatchers.Default) {

        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            doInput = true
            addRequestProperty("User-Agent", "CodeChallenge/1.0.0")
        }

        connection.inputStream.reader().use { it.readText() }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun postRequest(url: String, body: String): String = withContext(Dispatchers.Default) {

        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doInput = true
            doOutput = true
            addRequestProperty("User-Agent", "CodeChallenge/1.0.0")
            outputStream.use { it.write(body.toByteArray()) }
        }

        connection.inputStream.reader().use { it.readText() }
    }
}

import Application.Sitio.Azzati
import Application.Sitio.Tabacalera
import khttp.get
import khttp.post
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import java.net.URLEncoder

object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            Tabacalera.hasCitas()
                .combine(Azzati.hasCitas()) { a, b -> a || b }
                .onEach { hasAnyCitas ->
                    if (hasAnyCitas)
                        TelegramSender.send("Citas available, run!").also { println("Found citas") }
                    else
                        println("No citas :(")
                }
                .collect()
        }
    }

    private fun Sitio.hasCitas(): Flow<Boolean> = flow {
        emit(get(asUrl()).jsonObject.has("hora_cita"))
    }

    sealed class Sitio constructor(
        private val center: Int,
        private val service: Int,
    ) {
        object Tabacalera : Sitio(11, 33)
        object Azzati : Sitio(9, 75)

        fun asUrl(): String {
            return "http://www.valencia.es/qsige.localizador/citaPrevia/primera/disponible/centro/$center/servicio/$service"
        }
    }
}

object TelegramSender {
    private const val API_KEY = "123" //TODO: add api keu
    private const val GROUP_ID = "123"//TODO: add group id

    fun send(message: String) {
        post("https://api.telegram.org/$API_KEY/sendMessage?chat_id=$GROUP_ID&text=${message.encode()}")
    }

    private fun String.encode() = URLEncoder.encode(this, "utf-8")
}

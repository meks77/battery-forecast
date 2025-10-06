package at.meks.pv.forecast.battery

class JvmLogger(val clazz: Any) : Logger {

    override fun error(message: String, throwable: Throwable?) {
        if (throwable != null) {
            println("ERROR: [$clazz] $message. Throwable: ${throwable.message}")
        } else {
            println("ERROR: [$clazz] $message")
        }
    }

    override fun debug(message: String) {
//        println("DEBUG: [$clazz] $message")
    }

    override fun info(message: String) {
        println("INFO: [$clazz] $message")
    }
}

actual fun createLogger(clazz: Any): Logger = JvmLogger(clazz)
actual fun createLogger(name: String): Logger = JvmLogger(name)
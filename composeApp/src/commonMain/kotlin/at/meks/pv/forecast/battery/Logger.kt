package at.meks.pv.forecast.battery

interface Logger {
    fun debug(message: String)
    fun info(message: String)
    fun error(message: String, throwable: Throwable? = null)
}

expect fun createLogger(clazz: Any): Logger;
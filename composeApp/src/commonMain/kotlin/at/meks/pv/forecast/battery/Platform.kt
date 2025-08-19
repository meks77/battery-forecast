package at.meks.pv.forecast.battery

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
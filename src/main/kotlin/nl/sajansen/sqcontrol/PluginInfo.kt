package nl.sajansen.sqcontrol

import java.util.*

object PluginInfo {
    private val properties = Properties()
    val version: String
    val author: String

    init {
        properties.load(SqControlPlugin::class.java.getResourceAsStream("/nl/sajansen/sqcontrol/plugin.properties"))
        version = properties.getProperty("version")
        author = properties.getProperty("author")
    }
}
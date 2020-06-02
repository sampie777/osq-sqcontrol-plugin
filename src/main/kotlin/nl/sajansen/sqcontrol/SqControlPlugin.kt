package nl.sajansen.sqcontrol

import nl.sajansen.sqcontrol.gui.SourcePanel
import nl.sajansen.sqcontrol.queItems.SqControlQueItem
import objects.que.JsonQue
import objects.que.QueItem
import plugins.common.QueItemBasePlugin
import java.awt.Color
import java.net.URL
import java.util.logging.Logger
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JComponent

class SqControlPlugin : QueItemBasePlugin {
    private val logger = Logger.getLogger(SqControlPlugin::class.java.name)

    override val name = "SqControlPlugin"
    override val description = "Control your SQ mixer with queue items"
    override val version = PluginInfo.version
    override val icon: Icon? = createImageIcon("/nl/sajansen/sqcontrol/icon-14.png")

    override val tabName = "SQ"

    internal val quickAccessColor = Color(230, 250, 233)

    override fun sourcePanel(): JComponent {
        return SourcePanel(this)
    }

    override fun configStringToQueItem(value: String): QueItem {
        throw NotImplementedError("This method is deprecated")
    }

    override fun jsonToQueItem(jsonQueItem: JsonQue.QueItem): QueItem {
        return SqControlQueItem.fromJson(this, jsonQueItem)
    }

    private fun createImageIcon(path: String): ImageIcon? {
        val imgURL: URL? = javaClass.getResource(path)
        if (imgURL != null) {
            return ImageIcon(imgURL)
        }

        logger.severe("Couldn't find imageIcon: $path")
        return null
    }
}
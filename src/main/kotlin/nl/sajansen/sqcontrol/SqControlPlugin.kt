package nl.sajansen.sqcontrol

import nl.sajansen.sqcontrol.gui.SourcePanel
import nl.sajansen.sqcontrol.queItems.SqControlQueItem
import objects.notifications.Notifications
import objects.que.JsonQue
import objects.que.QueItem
import plugins.common.QueItemBasePlugin
import java.awt.Color
import java.net.URL
import java.util.logging.Logger
import javax.sound.midi.MidiDevice
import javax.sound.midi.MidiSystem
import javax.sound.midi.Receiver
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
    internal var midiDevice: MidiDevice? = null
    internal var midiDeviceReceiver: Receiver? = null

    override fun enable() {
        super.enable()
        getMidiDevice()
    }

    override fun disable() {
        super.disable()
        midiDevice?.close()
    }

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

    private fun getMidiDevice() {
        midiDevice = MidiSystem.getMidiDeviceInfo()
                .toList()
                .filter { it.name.contains("SQ") }
                .map { MidiSystem.getMidiDevice(it) }
                .find { it.maxReceivers != 0 }

        if (midiDevice == null) {
            logger.warning("Could not find SQ MIDI device")
            Notifications.add("Could not find SQ MIDI device", "SQ Control")
            return
        }

        midiDeviceReceiver = midiDevice!!.receiver
        midiDevice!!.open()
    }
}
package nl.sajansen.sqcontrol

import nl.sajansen.sqcontrol.gui.SourcePanel
import nl.sajansen.sqcontrol.midi.SqMidiReceiver
import nl.sajansen.sqcontrol.queItems.LevelFadeQueItem
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
    internal var midiInputDevice: MidiDevice? = null
    internal var midiOutputDevice: MidiDevice? = null
    internal var midiSendReceiver: Receiver? = null
    internal var midiReceiveReceiver: SqMidiReceiver? = null

    override fun enable() {
        super.enable()
        getMidiDevice()
    }

    override fun disable() {
        super.disable()
        midiInputDevice?.close()
    }

    override fun sourcePanel(): JComponent {
        return SourcePanel(this)
    }

    override fun configStringToQueItem(value: String): QueItem {
        throw NotImplementedError("This method is deprecated")
    }

    override fun jsonToQueItem(jsonQueItem: JsonQue.QueItem): QueItem {
        return when (jsonQueItem.className) {
            SqControlQueItem::class.java.simpleName -> SqControlQueItem.fromJson(this, jsonQueItem)
            LevelFadeQueItem::class.java.simpleName -> LevelFadeQueItem.fromJson(this, jsonQueItem)
            else -> throw IllegalArgumentException("Invalid SqControlPlugin queue item: ${jsonQueItem.className}")
        }
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
        midiInputDevice = MidiSystem.getMidiDeviceInfo()
                .toList()
                .filter { it.name.contains("SQ") }
                .map { MidiSystem.getMidiDevice(it) }
                .find { it.maxReceivers != 0 }
        midiOutputDevice = MidiSystem.getMidiDeviceInfo()
                .toList()
                .filter { it.name.contains("SQ") }
                .map { MidiSystem.getMidiDevice(it) }
                .find { it.maxTransmitters != 0 }

        if (midiInputDevice == null) {
            logger.warning("Could not find SQ MIDI device")
            Notifications.add("Could not find SQ MIDI device", "SQ Control")
            return
        }

        if (midiOutputDevice == null) {
            logger.warning("Could not find SQ MIDI device (output)")
            Notifications.add("Could not find SQ MIDI device (output)", "SQ Control")
            return
        }

        // Register receiver for sending messages
        try {
            midiSendReceiver = midiInputDevice!!.receiver
        } catch (e: Exception) {
            logger.warning("Failed to register receiver")
            e.printStackTrace()
            Notifications.add(
                    "Failed to setup outgoing connection with device: ${midiInputDevice?.deviceInfo?.name}: ${e.localizedMessage}",
                    "Midi Control"
            )
            return
        }

        // Register receiver for transmitter
        try {
            midiReceiveReceiver = SqMidiReceiver()
            midiOutputDevice!!.transmitter.receiver = midiReceiveReceiver!!
        } catch (e: Exception) {
            logger.warning("Failed to register transmitter")
            e.printStackTrace()
            Notifications.add(
                    "Failed to setup incoming connection with device: ${midiInputDevice?.deviceInfo?.name}: ${e.localizedMessage}. Some functions may not work properly.",
                    "Midi Control"
            )
            midiReceiveReceiver = null
        }

        // Open connection with device
        try {
            midiInputDevice!!.open()
        } catch (e: Exception) {
            logger.severe("Failed to open device: ${midiInputDevice?.deviceInfo?.name}")
            e.printStackTrace()
            Notifications.add(
                    "Failed to connect with device: ${midiInputDevice?.deviceInfo?.name}: ${e.localizedMessage}",
                    "Midi Control"
            )
            return
        }

        // Open connection with device
        try {
            if (!midiOutputDevice!!.isOpen) {
                midiOutputDevice!!.open()
            }
        } catch (e: Exception) {
            logger.severe("Failed to open output device: ${midiInputDevice?.deviceInfo?.name}")
            e.printStackTrace()
            Notifications.add(
                    "Failed to connect with output device: ${midiInputDevice?.deviceInfo?.name}: ${e.localizedMessage}",
                    "Midi Control"
            )
            return
        }
    }

    fun isConnected() = midiInputDevice != null && midiSendReceiver != null

    fun isOutputConnected() = midiOutputDevice != null && midiReceiveReceiver != null
}
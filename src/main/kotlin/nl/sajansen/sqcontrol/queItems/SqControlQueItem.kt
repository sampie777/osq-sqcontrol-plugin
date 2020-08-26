package nl.sajansen.sqcontrol.queItems

import nl.sajansen.sqcontrol.*
import nl.sajansen.sqcontrol.midi.ByteMidiMessage
import objects.notifications.Notifications
import objects.que.JsonQueue
import objects.que.QueItem
import java.awt.Color
import java.util.logging.Logger
import javax.sound.midi.MidiMessage

class SqControlQueItem(
        override val plugin: SqControlPlugin,
        override val name: String,
        val messages: List<MidiMessage>
) : QueItem {

    private val logger = Logger.getLogger(SqControlQueItem::class.java.name)

    override var executeAfterPrevious: Boolean = false
    override var quickAccessColor: Color? = plugin.quickAccessColor

    companion object {
        fun fromJson(plugin: SqControlPlugin, jsonQueItem: JsonQueue.QueueItem): SqControlQueItem {
            val messages: List<MidiMessage> = jsonQueItem.data["commands"]!!.split(";")
                    .map { stringCommands ->
                        val byteArrayCommands = configStringToByteArray(stringCommands)
                        ByteMidiMessage(byteArrayCommands)
                    }.toList()
            return SqControlQueItem(plugin, jsonQueItem.name, messages)
        }
    }

    override fun activate() {
        if (!plugin.isConnected()) {
            logger.warning("Not connected to SQ MIDI device")
            Notifications.add("Not connected to SQ MIDI device", "SQ Control")
            return
        }

        messages.forEach {
            logger.info("Sending MIDI command: ${byteArrayStringToConfigString(byteArrayToByteArrayString(it.message))}")
            plugin.midiSendReceiver!!.send(it, -1)
        }
    }

    override fun deactivate() {}

    override fun toConfigString(): String {
        throw NotImplementedError("This method is deprecated")
    }

    override fun toJson(): JsonQueue.QueueItem {
        val jsonItem = super.toJson()
        jsonItem.data["commands"] = messages
                .map {
                    byteArrayStringToConfigString(
                            byteArrayToByteArrayString(it.message)
                    )
                }
                .joinToString(";")
        return jsonItem
    }
}
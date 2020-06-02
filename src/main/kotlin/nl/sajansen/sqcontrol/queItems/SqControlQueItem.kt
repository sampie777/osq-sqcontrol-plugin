package nl.sajansen.sqcontrol.queItems

import nl.sajansen.sqcontrol.*
import objects.notifications.Notifications
import objects.que.JsonQue
import objects.que.QueItem
import java.awt.Color
import java.util.logging.Logger
import javax.sound.midi.MidiMessage
import javax.sound.midi.MidiSystem
import javax.sound.midi.MidiUnavailableException

class SqControlQueItem(
    override val plugin: SqControlPlugin,
    override val name: String,
    val messages: List<MidiMessage>
) : QueItem {

    private val logger = Logger.getLogger(SqControlQueItem::class.java.name)

    override var executeAfterPrevious: Boolean = false
    override var quickAccessColor: Color? = plugin.quickAccessColor

    companion object {
        fun fromJson(plugin: SqControlPlugin, jsonQueItem: JsonQue.QueItem): SqControlQueItem {
            val messages: List<MidiMessage> = jsonQueItem.data["commands"]!!.split(";")
                .map { stringCommands ->
                    val byteArrayCommands = configStringToByteArray(stringCommands)
                    ByteMidiMessage(byteArrayCommands)
                }.toList()
            return SqControlQueItem(plugin, jsonQueItem.name, messages)
        }
    }

    override fun activate() {
        try {
            messages.forEach {
                logger.info("Sending MIDI command: ${byteArrayStringToConfigString(byteArrayToByteArrayString(it.message))}")
                MidiSystem.getReceiver().send(it, -1)
            }
        } catch (e: MidiUnavailableException) {
            logger.info("Midi device not connected; cannot activate midi message")
            Notifications.add("Cannot activate queue item: ${e.localizedMessage}", "Midi Control")
        }
    }

    override fun deactivate() {}

    override fun toConfigString(): String {
        throw NotImplementedError("This method is deprecated")
    }

    override fun toJson(): JsonQue.QueItem {
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
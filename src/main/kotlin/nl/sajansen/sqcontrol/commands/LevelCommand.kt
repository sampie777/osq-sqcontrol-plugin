package nl.sajansen.sqcontrol.commands


import nl.sajansen.sqcontrol.*
import nl.sajansen.sqcontrol.midi.ByteMidiMessage
import nl.sajansen.sqcontrol.queItems.SqControlQueItem
import java.nio.ByteBuffer
import java.util.logging.Logger
import javax.sound.midi.MidiMessage
import javax.sound.midi.Receiver

object LevelCommand : Command {
    private val logger = Logger.getLogger(LevelCommand::class.java.name)

    const val minLevel = 0
    val maxLevel = "7F7F".toInt(16)
    const val minDBLevel = -90.0
    const val maxDBLevel = 10.0

    override fun getAvailableActions(): Array<CommandEnum> {
        return CommandLevelAction.values() as Array<CommandEnum>
    }

    override fun getAvailableChannels(): Array<CommandChannelEnum> {
        return CommandLevelChannels.values() as Array<CommandChannelEnum>
    }

    override fun inputsToQueItem(
            plugin: SqControlPlugin,
            name: String,
            action: CommandEnum,
            channel: CommandChannelEnum
    ): SqControlQueItem? {
        val channelHexValues = channel.hexValue.split(",")

        val messages = ArrayList<MidiMessage>()
        messages.add(ByteMidiMessage(hexStringToByteArray("B0,63,${channelHexValues[0]}")))
        messages.add(ByteMidiMessage(hexStringToByteArray("B0,62,${channelHexValues[1]}")))
        when (action) {
            CommandLevelAction.SET_P10DB -> addMessagesForHexStringAbsoluteValue(messages, dBLevelToHexString(10))
            CommandLevelAction.SET_P5DB -> addMessagesForHexStringAbsoluteValue(messages, dBLevelToHexString(5))
            CommandLevelAction.SET_P3DB -> addMessagesForHexStringAbsoluteValue(messages, dBLevelToHexString(3))
            CommandLevelAction.SET_0DB -> addMessagesForHexStringAbsoluteValue(messages, dBLevelToHexString(0))
            CommandLevelAction.SET_M3DB -> addMessagesForHexStringAbsoluteValue(messages, dBLevelToHexString(-3))
            CommandLevelAction.SET_M5DB -> addMessagesForHexStringAbsoluteValue(messages, dBLevelToHexString(-5))
            CommandLevelAction.SET_M10DB -> addMessagesForHexStringAbsoluteValue(messages, dBLevelToHexString(-10))
            CommandLevelAction.SET_MINF -> addMessagesForHexStringAbsoluteValue(messages, dBLevelToHexString(-99))
            CommandLevelAction.INCREASE -> messages.add(ByteMidiMessage(hexStringToByteArray("B0,60,00")))
            CommandLevelAction.DECREASE -> messages.add(ByteMidiMessage(hexStringToByteArray("B0,61,00")))
            else -> throw IllegalArgumentException("Invalid action '${action}' given for Level command")
        }

        return SqControlQueItem(plugin, generateNameForQueItem(name, action, channel), messages)
    }

    fun generateNameForQueItem(name: String, action: CommandEnum, channel: CommandChannelEnum): String {
        if (name.isNotEmpty()) {
            return name
        }

        val actionString = action.toString().toLowerCase().replace("_", " ")

        return "[$channel] ${upperCaseFirst(actionString)}"
    }

    fun upperCaseFirst(text: String): String {
        return text[0].toUpperCase() + text.substring(1)
    }

    override fun toString() = "Level"

    fun addMessagesForHexStringAbsoluteValue(messages: ArrayList<MidiMessage>, hexString: String) {
        val hexValues = hexString.split(",")
        messages.add(ByteMidiMessage(hexStringToByteArray("B0,06,${hexValues[0]}")))
        messages.add(ByteMidiMessage(hexStringToByteArray("B0,26,${hexValues[1]}")))
    }

    private fun dBLevelToHexString(level: Int): String {
        return when (level) {
            10 -> "7F,7F"
            5 -> "7B,37"
            3 -> "79,40"
            0 -> "76,5C"
            -3 -> "73,78"
            -5 -> "72,0A"
            -10 -> "6D,39"
            else -> "00,00"
        }
    }

    fun getChannelLevel(plugin: SqControlPlugin, channel: CommandChannelEnum, callback: (currentLevel: Int) -> Unit) {
        logger.info("Preparing request for channel level for channel: $channel")
        registerChannelLevelRequest(plugin, channel, callback)
        sendChannelLevelRequest(plugin, channel)
        logger.info("Channel level request send for channel: $channel")
    }

    private fun registerChannelLevelRequest(plugin: SqControlPlugin, channel: CommandChannelEnum, callback: (currentLevel: Int) -> Unit) {
        if (plugin.midiReceiveReceiver == null) {
            logger.severe("MidiReceiveReceiver is null, cannot register channel request")
            return
        }

        plugin.midiReceiveReceiver!!.registerChannelLevelRequest(channel, callback)
    }

    private fun sendChannelLevelRequest(plugin: SqControlPlugin, channel: CommandChannelEnum) {
        val channelHexValues = channel.hexValue.split(",")

        val messages = ArrayList<MidiMessage>()
        messages.add(ByteMidiMessage(hexStringToByteArray("B0,63,${channelHexValues[0]}")))
        messages.add(ByteMidiMessage(hexStringToByteArray("B0,62,${channelHexValues[1]}")))
        messages.add(ByteMidiMessage(hexStringToByteArray("B0,60,7F")))

        sendMessages(plugin.midiSendReceiver!!, messages)
    }

    fun setChannelLevel(receiver: Receiver, channel: CommandChannelEnum, level: Int) {
        logger.info("Setting level for channel: $channel to: $level")

        val messages = getMessagesForChannelLevelChange(channel, level)

        sendMessages(receiver, messages)
    }

    fun getMessagesForChannelLevelChange(channel: CommandChannelEnum, level: Int): ArrayList<MidiMessage> {
        val channelHexValues = channel.hexValue.split(",")
        val (coarseValue, fineValue) = integerToLevelBytes(level)

        val messages = java.util.ArrayList<MidiMessage>()
        messages.add(ByteMidiMessage(hexStringToByteArray("B0,63,${channelHexValues[0]}")))
        messages.add(ByteMidiMessage(hexStringToByteArray("B0,62,${channelHexValues[1]}")))
        messages.add(ByteMidiMessage(hexStringToByteArray("B0,06") + coarseValue))
        messages.add(ByteMidiMessage(hexStringToByteArray("B0,26") + fineValue))
        return messages
    }

    fun integerToLevelBytes(level: Int): Pair<Byte, Byte> {
        val nextLevelByteArray = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(level).array()

        val coarseValue = nextLevelByteArray[Int.SIZE_BYTES - 2]
        val fineValue = nextLevelByteArray[Int.SIZE_BYTES - 1]
        return Pair(coarseValue, fineValue)
    }

    private fun sendMessages(receiver: Receiver, messages: java.util.ArrayList<MidiMessage>) {
        messages.forEach {
            logger.info("Sending MIDI command: ${byteArrayStringToConfigString(byteArrayToByteArrayString(it.message))}")
            receiver.send(it, -1)
        }
    }
}


enum class CommandLevelAction : CommandEnum {
    SET_P10DB,
    SET_P5DB,
    SET_P3DB,
    SET_0DB,
    SET_M3DB,
    SET_M5DB,
    SET_M10DB,
    SET_MINF,
    INCREASE,
    DECREASE,
}

enum class CommandLevelChannels(override val hexValue: String) : CommandChannelEnum {
    CH1("40,00"),
    CH2("40,01"),
    CH3("40,02"),
    CH4("40,03"),
    CH5("40,04"),
    CH6("40,05"),
    CH7("40,06"),
    CH8("40,07"),
    CH9("40,08"),
    CH10("40,09"),
    CH11("40,0A"),
    CH12("40,0B"),
    CH13("40,0C"),
    CH14("40,0D"),
    CH15("40,0E"),
    CH16("40,0F"),
    CH17("40,10"),
    CH18("40,11"),
    CH19("40,12"),
    CH20("40,13"),
    CH21("40,14"),
    CH22("40,15"),
    CH23("40,16"),
    CH24("40,17"),
    CH25("40,18"),
    CH26("40,19"),
    CH27("40,1A"),
    CH28("40,1B"),
    CH29("40,1C"),
    CH30("40,1D"),
    CH31("40,1E"),
    CH32("40,1F"),
    CH33("40,20"),
    CH34("40,21"),
    CH35("40,22"),
    CH36("40,23"),
    CH37("40,24"),
    CH38("40,25"),
    CH39("40,26"),
    CH40("40,27"),
    CH41("40,28"),
    CH42("40,29"),
    CH43("40,2A"),
    CH44("40,2B"),
    CH45("40,2C"),
    CH46("40,2D"),
    CH47("40,2E"),
    CH48("40,2F"),
    GRP1("40,30"),
    GRP2("40,31"),
    GRP3("40,32"),
    GRP4("40,33"),
    GRP5("40,34"),
    GRP6("40,35"),
    GRP7("40,36"),
    GRP8("40,37"),
    GRP9("40,38"),
    GRP10("40,39"),
    GRP11("40,3A"),
    GRP12("40,3B"),
    FX_RETURN1("40,3C"),
    FX_RETURN2("40,3D"),
    FX_RETURN3("40,3E"),
    FX_RETURN4("40,3F"),
    FX_RETURN5("40,40"),
    FX_RETURN6("40,41"),
    FX_RETURN7("40,42"),
    FX_RETURN8("40,43"),
    LR("4F,00"),
    AUX1("4F,01"),
    AUX2("4F,02"),
    AUX3("4F,03"),
    AUX4("4F,04"),
    AUX5("4F,05"),
    AUX6("4F,06"),
    AUX7("4F,07"),
    AUX8("4F,08"),
    AUX9("4F,09"),
    AUX10("4F,0A"),
    AUX11("4F,0B"),
    AUX12("4F,0C"),
    FX_SEND1("4F,0D"),
    FX_SEND2("4F,0E"),
    FX_SEND3("4F,0F"),
    FX_SEND4("4F,10"),
    MTX1("4F,11"),
    MTX2("4F,12"),
    MTX3("4F,13"),
    DCA1("4F,20"),
    DCA2("4F,21"),
    DCA3("4F,22"),
    DCA4("4F,23"),
    DCA5("4F,24"),
    DCA6("4F,25"),
    DCA7("4F,26"),
    DCA8("4F,27"),
}
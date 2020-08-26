package nl.sajansen.sqcontrol.commands


import nl.sajansen.sqcontrol.midi.ByteMidiMessage
import nl.sajansen.sqcontrol.SqControlPlugin
import nl.sajansen.sqcontrol.hexStringToByteArray
import nl.sajansen.sqcontrol.queItems.SqControlQueItem
import java.util.logging.Logger
import javax.sound.midi.MidiMessage

object MuteCommand : Command {
    private val logger = Logger.getLogger(MuteCommand::class.java.name)

    @Suppress("UNCHECKED_CAST")
    override fun getAvailableActions(): Array<CommandEnum> {
        return CommandMuteAction.values() as Array<CommandEnum>
    }

    @Suppress("UNCHECKED_CAST")
    override fun getAvailableChannels(): Array<CommandChannelEnum> {
        return CommandMuteChannels.values() as Array<CommandChannelEnum>
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
            CommandMuteAction.MUTE -> {
                messages.add(ByteMidiMessage(hexStringToByteArray("B0,06,00")))
                messages.add(ByteMidiMessage(hexStringToByteArray("B0,26,01")))
            }
            CommandMuteAction.UNMUTE -> {
                messages.add(ByteMidiMessage(hexStringToByteArray("B0,06,00")))
                messages.add(ByteMidiMessage(hexStringToByteArray("B0,26,00")))
            }
            CommandMuteAction.TOGGLE -> messages.add(ByteMidiMessage(hexStringToByteArray("B0,60,00")))
            else -> throw IllegalArgumentException("Invalid action '${action}' given for Mute command")
        }

        return SqControlQueItem(plugin, generateNameForQueItem(name, action, channel), messages)
    }

    fun generateNameForQueItem(
        name: String,
        action: CommandEnum,
        channel: CommandChannelEnum
    ): String {
        if (name.isNotEmpty()) {
            return name
        }

        val actionString = if(action == CommandMuteAction.TOGGLE) "toggle mute" else action.toString().toLowerCase()

        return "[$channel] ${upperCaseFirst(actionString)}"
    }

    fun upperCaseFirst(text: String): String {
        return text[0].toUpperCase() + text.substring(1)
    }

    override fun toString() = "Mute"
}

enum class CommandMuteAction : CommandEnum {
    TOGGLE,
    MUTE,
    UNMUTE
}

enum class CommandMuteChannels(override val hexValue: String) : CommandChannelEnum {
    CH1("00,00"),
    CH2("00,01"),
    CH3("00,02"),
    CH4("00,03"),
    CH5("00,04"),
    CH6("00,05"),
    CH7("00,06"),
    CH8("00,07"),
    CH9("00,08"),
    CH10("00,09"),
    CH11("00,0A"),
    CH12("00,0B"),
    CH13("00,0C"),
    CH14("00,0D"),
    CH15("00,0E"),
    CH16("00,0F"),
    CH17("00,10"),
    CH18("00,11"),
    CH19("00,12"),
    CH20("00,13"),
    CH21("00,14"),
    CH22("00,15"),
    CH23("00,16"),
    CH24("00,17"),
    CH25("00,18"),
    CH26("00,19"),
    CH27("00,1A"),
    CH28("00,1B"),
    CH29("00,1C"),
    CH30("00,1D"),
    CH31("00,1E"),
    CH32("00,1F"),
    CH33("00,20"),
    CH34("00,21"),
    CH35("00,22"),
    CH36("00,23"),
    CH37("00,24"),
    CH38("00,25"),
    CH39("00,26"),
    CH40("00,27"),
    CH41("00,28"),
    CH42("00,29"),
    CH43("00,2A"),
    CH44("00,2B"),
    CH45("00,2C"),
    CH46("00,2D"),
    CH47("00,2E"),
    CH48("00,2F"),
    GRP1("00,30"),
    GRP2("00,31"),
    GRP3("00,32"),
    GRP4("00,33"),
    GRP5("00,34"),
    GRP6("00,35"),
    GRP7("00,36"),
    GRP8("00,37"),
    GRP9("00,38"),
    GRP10("00,39"),
    GRP11("00,3A"),
    GRP12("00,3B"),
    FX_RETURN1("00,3C"),
    FX_RETURN2("00,3D"),
    FX_RETURN3("00,3E"),
    FX_RETURN4("00,3F"),
    FX_RETURN5("00,40"),
    FX_RETURN6("00,41"),
    FX_RETURN7("00,42"),
    FX_RETURN8("00,43"),
    LR("00,44"),
    AUX1("00,45"),
    AUX2("00,46"),
    AUX3("00,47"),
    AUX4("00,48"),
    AUX5("00,49"),
    AUX6("00,4A"),
    AUX7("00,4B"),
    AUX8("00,4C"),
    AUX9("00,4D"),
    AUX10("00,4E"),
    AUX11("00,4F"),
    AUX12("00,50"),
    FX_SEND1("00,51"),
    FX_SEND2("00,52"),
    FX_SEND3("00,53"),
    FX_SEND4("00,54"),
    MTX1("00,55"),
    MTX2("00,56"),
    MTX3("00,57"),
    DCA1("02,0"),
    DCA2("02,1"),
    DCA3("02,2"),
    DCA4("02,3"),
    DCA5("02,4"),
    DCA6("02,5"),
    DCA7("02,6"),
    DCA8("02,7"),
    MUTE_GROUP1("04,0"),
    MUTE_GROUP2("04,1"),
    MUTE_GROUP3("04,2"),
    MUTE_GROUP4("04,3"),
    MUTE_GROUP5("04,4"),
    MUTE_GROUP6("04,5"),
    MUTE_GROUP7("04,6"),
    MUTE_GROUP8("04,7"),
}
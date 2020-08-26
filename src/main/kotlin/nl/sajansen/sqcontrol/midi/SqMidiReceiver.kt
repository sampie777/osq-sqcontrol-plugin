package nl.sajansen.sqcontrol.midi

import nl.sajansen.sqcontrol.*
import nl.sajansen.sqcontrol.commands.CommandChannelEnum
import java.io.File
import java.util.logging.Logger
import javax.sound.midi.MidiMessage
import javax.sound.midi.Receiver

class SqMidiReceiver : Receiver {

    private val logger = Logger.getLogger(SqMidiReceiver::class.java.name)

    private val msbMessageCode = hexStringToByte("63")
    private val lsbMessageCode = hexStringToByte("62")
    private val valueCoarseMessageCode = hexStringToByte("06")
    private val valueFineMessageCode = hexStringToByte("26")

    private val channelByteArray = byteArrayOf(0, 0)
    private val levelByteArray = byteArrayOf(0, 0)
    private val channelLevelRequests = HashMap<CommandChannelEnum, (currentLevel: Int) -> Unit>()

//    private val logs = ArrayList<Triple<Long, Int, Double>>()

    override fun send(message: MidiMessage, timestamp: Long) {
        if (channelLevelRequests.isEmpty()) {
            return
        }

        processMessage(message, timestamp)
    }

    override fun close() {
        logger.info("Connection closed")
    }

    private fun processMessage(message: MidiMessage, @Suppress("unused_parameter") timestamp: Long) {
        when (0) {
            msbMessageCode.compareTo(message.message[1]) -> {
                channelByteArray[0] = message.message[2]
            }
            lsbMessageCode.compareTo(message.message[1]) -> {
                channelByteArray[1] = message.message[2]
            }
            valueCoarseMessageCode.compareTo(message.message[1]) -> {
                levelByteArray[0] = message.message[2]
            }
            valueFineMessageCode.compareTo(message.message[1]) -> {
                levelByteArray[1] = message.message[2]

                val channel = byteArrayToByteArrayString(channelByteArray)
                val level = byteArrayToInt(levelByteArray)

                processChannelLevelRequests(channel, level)
//                logs.add(Triple(timestamp, level, levelTodB(level)))
//                println(level)
//                if (channelLevelRequests.isNotEmpty()) {
//                    val file = File("log.csv")
//                    logs.forEach {
//                        file.appendText("${it.first};${it.second};${it.third}\n")
//                    }
//                }
            }
            else -> {
                logger.info("Unknown message received")
            }
        }
    }

    fun registerChannelLevelRequest(channel: CommandChannelEnum, callback: (currentLevel: Int) -> Unit) {
        channelLevelRequests[channel] = callback
    }

    internal fun processChannelLevelRequests(channel: String, level: Int) {
        val (channelEnum, callback) = getCallbackForChannel(channel) ?: return

        logger.info("Channel level callback found, invoking it with level: $level...")
        try {
            callback.invoke(level)
        } catch (e: Exception) {
            logger.warning("Exception while executing callback for channel: $channelEnum")
            e.printStackTrace()
        } finally {
            channelLevelRequests.remove(channelEnum)
        }
    }

    internal fun getCallbackForChannel(channel: String): Pair<CommandChannelEnum, ((Int) -> Unit)>? {
        val callbacks = channelLevelRequests.filterKeys { byteArrayToByteArrayString(hexStringToByteArray(it.hexValue)) == channel }

        if (callbacks.isEmpty()) {
            return null
        }

        if (callbacks.size > 1) {
            logger.warning("Found multiple channel level callbacks for channel. Using first.")
        }

        val channelEnum = callbacks.keys.first()
        val callback = callbacks[channelEnum]

        if (callback == null) {
            logger.warning("Found channel level callback but it's null for channel: $channelEnum")
            return null
        }

        return Pair(channelEnum, callback)
    }
}
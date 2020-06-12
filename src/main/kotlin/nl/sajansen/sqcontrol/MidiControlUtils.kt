package nl.sajansen.sqcontrol

import nl.sajansen.sqcontrol.commands.LevelCommand
import nl.sajansen.sqcontrol.midi.ByteMidiMessage
import java.math.BigInteger
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round

val charset = Charsets.ISO_8859_1

fun byteArrayStringToByteArray(text: String): ByteArray {
    return text.toByteArray(charset)
}

fun byteArrayToByteArrayString(byteArray: ByteArray): String {
    return String(byteArray, charset)
}

fun configStringToByteArray(text: String): ByteArray {
    return text.split(",")
            .map {
                it.trim()
                        .toInt()
                        .toByte()
            }
            .toByteArray()
}

fun configStringToByteArrayString(byteText: String): String {
    if (byteText.isEmpty()) {
        return ""
    }
    return byteArrayToByteArrayString(
            configStringToByteArray(byteText)
    )
}

fun byteArrayStringToConfigString(text: String): String {
    return text.toByteArray(charset)
            .joinToString(",")
}

fun hexStringToMidiMessages(text: String): List<ByteMidiMessage> {
    return text.split(";")
            .map { stringCommand ->
                val byteArray = hexStringToByteArray(stringCommand)
                ByteMidiMessage(byteArray)
            }
}

fun hexStringToByteArray(hexString: String): ByteArray {
    return hexString.split(",")
            .map { stringHexValue ->
                hexStringToByte(stringHexValue)
            }
            .toByteArray()
}

fun hexStringToByte(hexString: String): Byte {
    return hexString.trim()
            .toInt(16)
            .toByte()
}

fun byteArrayToInt(value: ByteArray, signed: Boolean = false): Int {
    if (signed) {
        return BigInteger(value).toInt()
    }
    return BigInteger(byteArrayOf(0) + value).toInt()
}


private val conversionDbBoundaries = arrayOf(
        9.0,
        -2.0,
        -20.0,
        -50.0,
        -100.0
)
private val conversionLevelBoundaries = arrayOf(
        32544,
        29803,
        25790,
        18518,
        -1
)
private val conversionOffsets = arrayOf(
        30300.0,
        30300.0,
        30280.0,
        31950.0,
        41900.0
)
private val conversionFactors = arrayOf(
        309.6,
        290.0,
        290.0,
        215.0,
        141.0
)

fun dBtoLevel(db: Double): Int {
    if (db <= LevelCommand.minDBLevel) {
        return LevelCommand.minLevel
    } else if (db >= LevelCommand.maxDBLevel) {
        return LevelCommand.maxLevel
    }

    val boundary = conversionDbBoundaries.indexOfFirst { db > it }

    val level = conversionOffsets[boundary] * 10.0.pow(db / conversionFactors[boundary])
    return round(level).toInt()
}

fun levelTodB(level: Int): Double {
    if (level < 9795) {
        return LevelCommand.minDBLevel
    } else if (level >= LevelCommand.maxLevel) {
        return LevelCommand.maxDBLevel
    }

    val boundary = conversionLevelBoundaries.indexOfFirst { level > it }

    return conversionFactors[boundary] * log10(level / conversionOffsets[boundary])
}
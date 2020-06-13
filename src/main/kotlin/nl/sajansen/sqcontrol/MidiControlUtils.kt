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

fun dbToPercentage(db: Double): Double {
    return when {
        db < -50 -> (db + 90.0) / 5.6940948286767
        db < -40 -> (db + 61.6073195579716) / 1.63808721348403
        db < -20 -> (db + 49.2545361976067) / 0.701155050510185
        db < -10 -> (db + 68.8627097646669) / 1.17652098817209
        else -> (db + 30.0710250764071) / 0.400710250764071
    }
}

fun percentageToDb(percentage: Double): Double {
    return when {
        percentage < 7.085898395656307 -> 5.6940948286767 * percentage - 90.0
        percentage < 13.198986716094781 -> 1.63808721348403 * percentage - 61.6073195579716
        percentage < 41.531524091706 -> 0.701155050510185 * percentage - 49.2545361976067
        percentage < 50.08862398238087 -> 1.17652098817209 * percentage - 68.8627097646669
        else -> 0.400710250764071 * percentage - 30.0710250764071
    }
}
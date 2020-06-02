package nl.sajansen.sqcontrol

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

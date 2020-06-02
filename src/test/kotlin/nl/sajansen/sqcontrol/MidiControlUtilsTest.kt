package nl.sajansen.sqcontrol

import kotlin.test.Test
import kotlin.test.assertEquals

class MidiControlUtilsTest {

    @Test
    fun testByteArrayStringToByteArray() {
        val byteArray = byteArrayOf(-16, 127, 127, 6, 5, -9)
        val byteArrayString = String(byteArray, charset)
        assertEquals(byteArrayString, String(byteArrayStringToByteArray(byteArrayString), charset))
    }

    @Test
    fun testByteArrayToByteArrayString() {
        val byteArray = byteArrayOf(-16, 127, 127, 6, 5, -9)
        val byteArrayString = String(byteArray, charset)
        assertEquals(byteArrayString, byteArrayToByteArrayString(byteArray))
    }

    @Test
    fun testConfigStringToByteArray() {
        val byteArray = byteArrayOf(-16, 127, 127, 6, 5, -9)
        val byteArrayString = String(byteArray, charset)
        assertEquals(byteArrayString, String(configStringToByteArray("-16,127,127,6,5,-9"), charset))
    }

    @Test
    fun testByteArrayStringToConfigString() {
        val byteArrayString1 = String(byteArrayOf(-16, 127, 127, 6, 5, -9), charset)
        assertEquals("-16,127,127,6,5,-9", byteArrayStringToConfigString(byteArrayString1))
    }

    @Test
    fun testConfigStringToByteArrayString() {
        val byteArrayString1 = String(byteArrayOf(-16, 127, 127, 6, 5, -9), charset)
        assertEquals(byteArrayString1, configStringToByteArrayString("-16,127,127,6,5,-9"))
    }

    @Test
    fun testHexStringToMidiMessages() {
        val midiMessage = hexStringToMidiMessages("F0,7F,7F;06,05,F7")
        assertEquals(2, midiMessage.size)
        assertEquals(String(byteArrayOf(-16, 127, 127), charset), String(midiMessage[0].message, charset))
        assertEquals(String(byteArrayOf(6, 5, -9), charset), String(midiMessage[1].message, charset))
    }

    @Test
    fun testHexStringToByteArray() {
        val byteArrayString1 = String(byteArrayOf(-16, 127, 127, 6, 5, -9), charset)
        assertEquals(byteArrayString1, String(hexStringToByteArray("F0,7F,7F,06,05,F7"), charset))
    }

    @Test
    fun testHexStringToByte() {
        assertEquals(-16, hexStringToByte("F0"))
    }
}
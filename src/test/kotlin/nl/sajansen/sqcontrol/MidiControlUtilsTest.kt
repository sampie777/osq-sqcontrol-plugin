package nl.sajansen.sqcontrol

import kotlin.math.abs
import kotlin.math.round
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

    @Test
    fun testDBtoLevel() {
        assertEquals(32639, dBtoLevel(10.0))
        assertEquals(31527, dBtoLevel(5.0))
        assertEquals(30300, dBtoLevel(0.0))
        assertEquals(27969, dBtoLevel(-10.0))
        assertEquals(23171, dBtoLevel(-30.0))
        assertEquals(15728, dBtoLevel(-60.0))
        assertEquals(0, dBtoLevel(-90.0))
    }

    @Test
    fun testLevelTodB() {
        assertEquals(10.0, round(levelTodB(32639)))
        assertEquals(5.0, round(levelTodB(31534)))
        assertEquals(0.0, round(levelTodB(30300)))
        assertEquals(-10.0, round(levelTodB(27961)))
        assertEquals(-40.0, round(levelTodB(20815)))
        assertEquals(-80.0, round(levelTodB(11330)))
        assertEquals(-90.0, round(levelTodB(9000)))
        assertEquals(-90.0, round(levelTodB(0)))
    }

    @Test
    fun testDbToPercentage() {
        assertEquals(0.0, round(dbToPercentage(-90.0)))
        assertEquals(7.0, round(dbToPercentage(-50.0)))
        assertEquals(13.0, round(dbToPercentage(-40.0)))
        assertEquals(42.0, round(dbToPercentage(-20.0)))
        assertEquals(50.0, round(dbToPercentage(-10.0)))
        assertEquals(75.0, round(dbToPercentage(0.0)))
        assertEquals(100.0, round(dbToPercentage(10.0)))
    }

    @Test
    fun testPercentageToDb() {
        assertEquals(-90.0, round(percentageToDb(0.0)))
        assertEquals(-50.0, round(percentageToDb(7.0)))
        assertEquals(-40.0, round(percentageToDb(13.0)))
        assertEquals(-20.0, round(percentageToDb(41.5)))
        assertEquals(-10.0, round(percentageToDb(50.0)))
        assertEquals(0.0, abs(round(percentageToDb(75.0))))
        assertEquals(10.0, round(percentageToDb(100.0)))
    }
}
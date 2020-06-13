package nl.sajansen.sqcontrol.midi

import nl.sajansen.sqcontrol.byteArrayToByteArrayString
import nl.sajansen.sqcontrol.commands.CommandLevelChannels
import nl.sajansen.sqcontrol.hexStringToByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull


class MidiReceiverTest {

    @Test
    fun testGetCallbackForChannel() {
        val channel = CommandLevelChannels.CH1
        val receiver = SqMidiReceiver()
        receiver.registerChannelLevelRequest(channel) {}

        val byteArrayString = byteArrayToByteArrayString(hexStringToByteArray(channel.hexValue))
        val resultPair = receiver.getCallbackForChannel(byteArrayString)

        assertNotNull(resultPair)
        assertEquals(channel, resultPair.first)
    }

    @Test
    fun testGetCallbackForIncorrectChannel() {
        val channel = CommandLevelChannels.CH1
        val receiver = SqMidiReceiver()
        receiver.registerChannelLevelRequest(CommandLevelChannels.CH2) {}

        val byteArrayString = byteArrayToByteArrayString(hexStringToByteArray(channel.hexValue))
        val resultPair = receiver.getCallbackForChannel(byteArrayString)

        assertNull(resultPair)
    }

    @Test
    fun testGetCallbackForNoChannel() {
        val channel = CommandLevelChannels.CH1
        val receiver = SqMidiReceiver()

        val byteArrayString = byteArrayToByteArrayString(hexStringToByteArray(channel.hexValue))
        val resultPair = receiver.getCallbackForChannel(byteArrayString)

        assertNull(resultPair)
    }

    @Test
    fun testProcessChannelLevelRequests() {
        var resultLevel = 0
        val channel = CommandLevelChannels.CH1
        val receiver = SqMidiReceiver()
        receiver.registerChannelLevelRequest(channel) { currentLevel -> resultLevel = currentLevel }

        val byteArrayString = byteArrayToByteArrayString(hexStringToByteArray(channel.hexValue))
        receiver.processChannelLevelRequests(byteArrayString, 100)

        assertEquals(100, resultLevel)

        val resultPair = receiver.getCallbackForChannel(byteArrayString)
        assertNull(resultPair, "Callback has not been removed from registered list")
    }

    @Test
    fun testSendAndProcessMessages() {
        var resultLevel = 0
        val channel = CommandLevelChannels.CH2
        val receiver = SqMidiReceiver()
        receiver.registerChannelLevelRequest(channel) { currentLevel -> resultLevel = currentLevel }

        receiver.send(ByteMidiMessage(hexStringToByteArray("b0,63,40")), 0)
        assertEquals(0, resultLevel)
        receiver.send(ByteMidiMessage(hexStringToByteArray("b0,62,01")), 0)
        assertEquals(0, resultLevel)
        receiver.send(ByteMidiMessage(hexStringToByteArray("b0,06,12")), 0)
        assertEquals(0, resultLevel)
        receiver.send(ByteMidiMessage(hexStringToByteArray("b0,26,34")), 0)
        assertEquals(4660, resultLevel)
    }
}
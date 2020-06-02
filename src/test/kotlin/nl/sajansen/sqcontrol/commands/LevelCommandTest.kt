package nl.sajansen.sqcontrol.commands

import nl.sajansen.sqcontrol.SqControlPlugin
import nl.sajansen.sqcontrol.byteArrayToByteArrayString
import nl.sajansen.sqcontrol.hexStringToByteArray
import javax.sound.midi.MidiMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LevelCommandTest {

    @Test
    fun testUpperCaseFirst() {
        assertEquals("AbcDe", LevelCommand().upperCaseFirst("abcDe"))
    }

    @Test
    fun testGenerateNameForQueItem() {
        assertEquals("name", LevelCommand().generateNameForQueItem("name", CommandLevelAction.SET_0DB, CommandLevelChannels.CH2))
        assertEquals("[CH2] Set 0db", LevelCommand().generateNameForQueItem("", CommandLevelAction.SET_0DB, CommandLevelChannels.CH2))
        assertEquals("[CH2] Set m10db", LevelCommand().generateNameForQueItem("", CommandLevelAction.SET_M10DB, CommandLevelChannels.CH2))
        assertEquals("[CH2] Increase", LevelCommand().generateNameForQueItem("", CommandLevelAction.INCREASE, CommandLevelChannels.CH2))
        assertEquals("[CH2] Decrease", LevelCommand().generateNameForQueItem("", CommandLevelAction.DECREASE, CommandLevelChannels.CH2))
    }

    @Test
    fun testInputsToQueItemWithLevelCommand() {
        val queItem = LevelCommand().inputsToQueItem(SqControlPlugin(), "", CommandLevelAction.SET_0DB, CommandLevelChannels.CH2)

        assertNotNull(queItem)
        assertEquals(4, queItem.messages.size)
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,63,40")), byteArrayToByteArrayString(queItem.messages[0].message))
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,62,01")), byteArrayToByteArrayString(queItem.messages[1].message))
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,06,76")), byteArrayToByteArrayString(queItem.messages[2].message))
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,26,5c")), byteArrayToByteArrayString(queItem.messages[3].message))
    }

    @Test
    fun testInputsToQueItemWithAnotherLevelCommand() {
        val queItem = LevelCommand().inputsToQueItem(SqControlPlugin(), "", CommandLevelAction.SET_M10DB, CommandLevelChannels.CH2)

        assertNotNull(queItem)
        assertEquals(4, queItem.messages.size)
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,63,40")), byteArrayToByteArrayString(queItem.messages[0].message))
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,62,01")), byteArrayToByteArrayString(queItem.messages[1].message))
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,06,6d")), byteArrayToByteArrayString(queItem.messages[2].message))
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,26,39")), byteArrayToByteArrayString(queItem.messages[3].message))
    }

    @Test
    fun testInputsToQueItemWithIncreaseCommand() {
        val queItem = LevelCommand().inputsToQueItem(SqControlPlugin(), "", CommandLevelAction.INCREASE, CommandLevelChannels.CH2)

        assertNotNull(queItem)
        assertEquals(3, queItem.messages.size)
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,63,40")), byteArrayToByteArrayString(queItem.messages[0].message))
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,62,01")), byteArrayToByteArrayString(queItem.messages[1].message))
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,60,00")), byteArrayToByteArrayString(queItem.messages[2].message))
    }

    @Test
    fun testInputsToQueItemWithDecreaseCommand() {
        val queItem = LevelCommand().inputsToQueItem(SqControlPlugin(), "", CommandLevelAction.DECREASE, CommandLevelChannels.CH2)

        assertNotNull(queItem)
        assertEquals(3, queItem.messages.size)
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,63,40")), byteArrayToByteArrayString(queItem.messages[0].message))
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,62,01")), byteArrayToByteArrayString(queItem.messages[1].message))
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,61,00")), byteArrayToByteArrayString(queItem.messages[2].message))
    }

    @Test
    fun testAddMessagesForHexStringAbsoluteValue() {
        val messages = ArrayList<MidiMessage>()

        LevelCommand().addMessagesForHexStringAbsoluteValue(messages, "ab,10")

        assertEquals(2, messages.size)
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("B0,06,ab")), byteArrayToByteArrayString(messages[0].message))
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("B0,26,10")), byteArrayToByteArrayString(messages[1].message))
    }
}
package nl.sajansen.sqcontrol.commands

import nl.sajansen.sqcontrol.SqControlPlugin
import nl.sajansen.sqcontrol.byteArrayToByteArrayString
import nl.sajansen.sqcontrol.hexStringToByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MuteCommandTest {

    @Test
    fun testUpperCaseFirst() {
        assertEquals("AbcDe", MuteCommand().upperCaseFirst("abcDe"))
    }

    @Test
    fun testGenerateNameForQueItem() {
        assertEquals("name", MuteCommand().generateNameForQueItem("name", CommandMuteAction.MUTE, CommandMuteChannels.CH2))
        assertEquals("[CH2] Mute", MuteCommand().generateNameForQueItem("", CommandMuteAction.MUTE, CommandMuteChannels.CH2))
        assertEquals("[CH2] Toggle mute", MuteCommand().generateNameForQueItem("", CommandMuteAction.TOGGLE, CommandMuteChannels.CH2))
    }

    @Test
    fun testInputsToQueItemWithMuteCommand() {
        val queItem = MuteCommand().inputsToQueItem(SqControlPlugin(), "", CommandMuteAction.MUTE, CommandMuteChannels.CH2)

        assertNotNull(queItem)
        assertEquals("[CH2] Mute", queItem.name)
        assertEquals(4, queItem.messages.size)
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,63,00")), byteArrayToByteArrayString(queItem.messages[0].message))
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,62,01")), byteArrayToByteArrayString(queItem.messages[1].message))
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,06,00")), byteArrayToByteArrayString(queItem.messages[2].message))
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,26,01")), byteArrayToByteArrayString(queItem.messages[3].message))
    }

    @Test
    fun testInputsToQueItemWithUnmuteCommand() {
        val queItem = MuteCommand().inputsToQueItem(SqControlPlugin(), "", CommandMuteAction.UNMUTE, CommandMuteChannels.CH2)

        assertNotNull(queItem)
        assertEquals("[CH2] Unmute", queItem.name)
        assertEquals(4, queItem.messages.size)
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,63,00")), byteArrayToByteArrayString(queItem.messages[0].message))
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,62,01")), byteArrayToByteArrayString(queItem.messages[1].message))
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,06,00")), byteArrayToByteArrayString(queItem.messages[2].message))
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,26,00")), byteArrayToByteArrayString(queItem.messages[3].message))
    }

    @Test
    fun testInputsToQueItemWithToggleCommand() {
        val queItem = MuteCommand().inputsToQueItem(SqControlPlugin(), "", CommandMuteAction.TOGGLE, CommandMuteChannels.CH2)

        assertNotNull(queItem)
        assertEquals("[CH2] Toggle mute", queItem.name)
        assertEquals(3, queItem.messages.size)
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,63,00")), byteArrayToByteArrayString(queItem.messages[0].message))
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,62,01")), byteArrayToByteArrayString(queItem.messages[1].message))
        assertEquals(byteArrayToByteArrayString(hexStringToByteArray("b0,60,00")), byteArrayToByteArrayString(queItem.messages[2].message))
    }
}
package nl.sajansen.sqcontrol.queItems

import nl.sajansen.sqcontrol.ByteMidiMessage
import nl.sajansen.sqcontrol.SqControlPlugin
import nl.sajansen.sqcontrol.byteArrayToByteArrayString
import objects.que.JsonQue
import kotlin.test.Test
import kotlin.test.assertEquals

class SqControlQueItemTest {

    @Test
    fun testFromJson() {
        val jsonQueItem = JsonQue.QueItem(
            "",
            "",
            "name",
            false,
            null,
            hashMapOf("commands" to "-16,127,127;6,5,-9")
        )

        val queItem = SqControlQueItem.fromJson(SqControlPlugin(), jsonQueItem)

        assertEquals("name", queItem.name)
        assertEquals(2, queItem.messages.size)
        assertEquals(byteArrayToByteArrayString(byteArrayOf(-16, 127, 127)), byteArrayToByteArrayString(queItem.messages[0].message))
        assertEquals(byteArrayToByteArrayString(byteArrayOf(6, 5, -9)), byteArrayToByteArrayString(queItem.messages[1].message))
    }

    @Test
    fun testToJson() {
        val messages = listOf(
            ByteMidiMessage(byteArrayOf(-16, 127, 127)),
            ByteMidiMessage(byteArrayOf(6, 5, -9))
        )
        val queItem = SqControlQueItem(SqControlPlugin(), "name", messages)

        val json = queItem.toJson()

        assertEquals("name", json.name)
        assertEquals("-16,127,127;6,5,-9", json.data["commands"])
    }
}
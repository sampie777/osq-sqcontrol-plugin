package nl.sajansen.sqcontrol.queItems

import nl.sajansen.sqcontrol.SqControlPlugin
import nl.sajansen.sqcontrol.commands.CommandLevelChannels
import nl.sajansen.sqcontrol.percentageToDb
import objects.que.JsonQue
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


class LevelFadeQueItemTest {

    private val plugin = SqControlPlugin()

    @Test
    fun testToJsonQueItem() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, -1.0, 1000)

        val jsonQueItem = queItem.toJson()

        assertEquals("name", jsonQueItem.name)
        assertEquals("CH1", jsonQueItem.data["channel"])
        assertEquals("-1.0", jsonQueItem.data["targetLevel"])
        assertEquals("1000", jsonQueItem.data["duration"])
    }

    @Test
    fun testFromJsonQueItem() {
        val jsonQueItem = JsonQue.QueItem(
                pluginName = plugin.name,
                className = "LevelFadeQueItem",
                name = "name",
                executeAfterPrevious = false,
                quickAccessColor = plugin.quickAccessColor,
                data = hashMapOf(
                        "channel" to "CH1",
                        "targetLevel" to "-1.0",
                        "duration" to "1000"
                )
        )

        val queItem = LevelFadeQueItem.fromJson(plugin, jsonQueItem)

        assertEquals("name", queItem.name)
        assertEquals(CommandLevelChannels.CH1, queItem.channel)
        assertEquals(-1.0, queItem.targetDBLevel)
        assertEquals(1000, queItem.duration)
    }

    @Test
    fun testGetNextLevelChangesCurrentLevel() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, percentageToDb(80.0), 1000)
        queItem.currentPercentage = 50.0
        queItem.percentageIncrement = 1.0

        val value = queItem.getNextPercentage()

        assertEquals(51.0, value)
        assertEquals(value, queItem.currentPercentage)
        assertEquals(1.0, queItem.percentageIncrement)
    }

    @Test
    fun testGetNextLevelChangesWithLevelCloseToTargetLevel() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, percentageToDb(80.0), 1000)
        queItem.currentPercentage = 78.0
        queItem.percentageIncrement = 5.0

        assertEquals(80.0, queItem.getNextPercentage())
    }

    @Test
    fun testGetNextLevelChangesWithLevelCloseToTargetLevel2() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, percentageToDb(80.0), 1000)
        queItem.currentPercentage = 82.0
        queItem.percentageIncrement = -5.0

        assertEquals(80.0, queItem.getNextPercentage())
    }

    @Test
    fun testGetNextLevelChangesWithLevelFurtherFromTargetLevel() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, percentageToDb(80.0), 1000)
        queItem.currentPercentage = 75.0
        queItem.percentageIncrement = 5.0

        assertEquals(80.0, queItem.getNextPercentage())
    }

    @Test
    fun testGetNextLevelChangesWithLevelOnTargetLevel() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, percentageToDb(80.0), 1000)
        queItem.currentPercentage = 80.0
        queItem.percentageIncrement = 5.0
        queItem.timer = Timer()

        assertEquals(80.0, queItem.getNextPercentage())
        assertNull(queItem.timer)
    }

    @Test
    fun testGetNextLevelChangesWithLevelRoundedOnTargetLevel() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, percentageToDb(80.0), 1000)
        queItem.currentPercentage = 79.9
        queItem.percentageIncrement = 5.0

        assertEquals(80.0, queItem.getNextPercentage())
    }

    @Test
    fun testGetNextLevelChangesWithLevelBelowMinimum() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, percentageToDb(80.0), 1000)
        queItem.currentPercentage = -10.0
        queItem.percentageIncrement = 5.0

        assertEquals(0.0, queItem.getNextPercentage())
    }

    @Test
    fun testGetNextLevelChangesWithLevelJustBelowMinimum() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, percentageToDb(80.0), 1000)
        queItem.currentPercentage = -1.0
        queItem.percentageIncrement = 5.0

        assertEquals(4.0, queItem.getNextPercentage())
    }

    @Test
    fun testGetNextLevelChangesWithLevelAboveMax() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, percentageToDb(80.0), 1000)
        queItem.currentPercentage = 110.0
        queItem.percentageIncrement = -5.0

        assertEquals(100.0, queItem.getNextPercentage())
    }

    @Test
    fun testGetNextLevelChangesWithLevelJustAboveMax() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, percentageToDb(80.0), 1000)
        queItem.currentPercentage = 101.0
        queItem.percentageIncrement = -5.0

        assertEquals(96.0, queItem.getNextPercentage())
    }

    @Test
    fun testGetNextLevelChangesWithLevelAtTheWrongSide() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, percentageToDb(80.0), 1000)
        queItem.currentPercentage = 90.0
        queItem.percentageIncrement = 5.0

        assertEquals(80.0, queItem.getNextPercentage())
    }

    @Test
    fun testGetNextLevelChangesWithLevelAtTheWrongSide2() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, percentageToDb(80.0), 1000)
        queItem.currentPercentage = 70.0
        queItem.percentageIncrement = -5.0

        assertEquals(80.0, queItem.getNextPercentage())
    }

    @Test
    fun testDeactivateStopsTimer() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, percentageToDb(80.0), 1000)

        // When
        queItem.stopTimer()

        assertNull(queItem.timer)

        // When
        queItem.timer = Timer()
        queItem.stopTimer()

        assertNull(queItem.timer)
    }

    @Test
    fun testCalculateTimingAndLevelIncrement() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, percentageToDb(80.0), 1000)
        val timeScaleFactor = queItem.timerInterval / 100.0

        val increment = queItem.calculatePercentageIncrement(40.0)

        assertEquals(timeScaleFactor * 4.0, increment)
    }

    @Test
    fun testCalculateTimingAndLevelIncrement2() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, percentageToDb(80.0), 2000)
        val timeScaleFactor = queItem.timerInterval / 100.0

        val increment = queItem.calculatePercentageIncrement(12.0)

        assertEquals(timeScaleFactor * 3.4, increment)
    }

    @Test
    fun testCalculateLevelIncrementWithZeroDelay() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, percentageToDb(80.0), 0)

        val increment = queItem.calculatePercentageIncrement(40.0)

        assertEquals(40.0, increment)
    }
}
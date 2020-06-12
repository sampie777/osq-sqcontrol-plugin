package nl.sajansen.sqcontrol.queItems

import nl.sajansen.sqcontrol.SqControlPlugin
import nl.sajansen.sqcontrol.commands.CommandLevelChannels
import nl.sajansen.sqcontrol.commands.LevelCommand
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
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, -1.0, 1000)
        queItem.currentDBLevel = -10.0
        queItem.leveldBIncrement = 1.0

        val value = queItem.getNextLeveldB()

        assertEquals(-9.0, value)
        assertEquals(value, queItem.currentDBLevel)
        assertEquals(1.0, queItem.leveldBIncrement)
    }

    @Test
    fun testGetNextLevelChangesWithLevelCloseToTargetLevel() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, -1.0, 1000)
        queItem.currentDBLevel = -2.0
        queItem.leveldBIncrement = 5.0

        assertEquals(-1.0, queItem.getNextLeveldB())
    }

    @Test
    fun testGetNextLevelChangesWithLevelCloseToTargetLevel2() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, -1.0, 1000)
        queItem.currentDBLevel = 0.0
        queItem.leveldBIncrement = -5.0

        assertEquals(-1.0, queItem.getNextLeveldB())
    }

    @Test
    fun testGetNextLevelChangesWithLevelFurtherFromTargetLevel() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, -1.0, 1000)
        queItem.currentDBLevel = -6.0
        queItem.leveldBIncrement = 5.0

        assertEquals(-1.0, queItem.getNextLeveldB())
    }

    @Test
    fun testGetNextLevelChangesWithLevelOnTargetLevel() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, -1.0, 1000)
        queItem.currentDBLevel = -1.0
        queItem.leveldBIncrement = 5.0
        queItem.timer = Timer()

        assertEquals(-1.0, queItem.getNextLeveldB())
        assertNull(queItem.timer)
    }

    @Test
    fun testGetNextLevelChangesWithLevelRoundedOnTargetLevel() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, -1.0, 1000)
        queItem.currentDBLevel = -1.01
        queItem.leveldBIncrement = 5.0

        assertEquals(-1.0, queItem.getNextLeveldB())
    }

    @Test
    fun testGetNextLevelChangesWithLevelBelowMinimum() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, -1.0, 1000)
        queItem.currentDBLevel = LevelCommand.minDBLevel - 10
        queItem.leveldBIncrement = 5.0

        assertEquals(LevelCommand.minDBLevel, queItem.getNextLeveldB())
    }

    @Test
    fun testGetNextLevelChangesWithLevelJustBelowMinimum() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, -1.0, 1000)
        queItem.currentDBLevel = LevelCommand.minDBLevel - 1.0
        queItem.leveldBIncrement = 5.0

        assertEquals(LevelCommand.minDBLevel + 4, queItem.getNextLeveldB())
    }

    @Test
    fun testGetNextLevelChangesWithLevelAboveMax() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, -1.0, 1000)
        queItem.currentDBLevel = LevelCommand.maxDBLevel + 10
        queItem.leveldBIncrement = -5.0

        assertEquals(LevelCommand.maxDBLevel, queItem.getNextLeveldB())
    }

    @Test
    fun testGetNextLevelChangesWithLevelJustAboveMax() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, -1.0, 1000)
        queItem.currentDBLevel = LevelCommand.maxDBLevel + 1.0
        queItem.leveldBIncrement = -5.0

        assertEquals(LevelCommand.maxDBLevel - 4, queItem.getNextLeveldB())
    }

    @Test
    fun testGetNextLevelChangesWithLevelAtTheWrongSide() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, -1.0, 1000)
        queItem.currentDBLevel = 10.0
        queItem.leveldBIncrement = 5.0

        assertEquals(-1.0, queItem.getNextLeveldB())
    }

    @Test
    fun testGetNextLevelChangesWithLevelAtTheWrongSide2() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, -1.0, 1000)
        queItem.currentDBLevel = -10.0
        queItem.leveldBIncrement = -5.0

        assertEquals(-1.0, queItem.getNextLeveldB())
    }

    @Test
    fun testDeactivateStopsTimer() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, -1.0, 1000)

        // When
        queItem.deactivate()

        assertNull(queItem.timer)

        // When
        queItem.timer = Timer()
        queItem.deactivate()

        assertNull(queItem.timer)
    }

    @Test
    fun testCalculateTimingAndLevelIncrement() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, -1.0, 1000)
        val timeScaleFactor = queItem.timerInterval / 100.0

        val increment = queItem.calculateLevelIncrement(-10.0)

        assertEquals(timeScaleFactor * 0.9, increment)
    }

    @Test
    fun testCalculateTimingAndLevelIncrement2() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, -1.0, 2000)
        val timeScaleFactor = queItem.timerInterval / 100.0

        val increment = queItem.calculateLevelIncrement(-11.0)

        assertEquals(timeScaleFactor * 0.5, increment)
    }

    @Test
    fun testCalculateLevelIncrementWithZeroDelay() {
        val queItem = LevelFadeQueItem(plugin, "name", CommandLevelChannels.CH1, -1.0, 0)

        val increment = queItem.calculateLevelIncrement(-10.0)

        assertEquals(9.0, increment)
    }
}
package nl.sajansen.sqcontrol.queItems

import nl.sajansen.sqcontrol.*
import nl.sajansen.sqcontrol.commands.CommandChannelEnum
import nl.sajansen.sqcontrol.commands.CommandLevelChannels
import nl.sajansen.sqcontrol.commands.LevelCommand
import objects.notifications.Notifications
import objects.que.JsonQue
import objects.que.QueItem
import java.awt.Color
import java.util.*
import java.util.logging.Logger
import kotlin.math.abs
import kotlin.math.max

class LevelFadeQueItem(
        override val plugin: SqControlPlugin,
        override val name: String,
        internal val channel: CommandChannelEnum,
        internal val targetDBLevel: Double,
        internal val duration: Long
) : QueItem {

    private val logger = Logger.getLogger(LevelFadeQueItem::class.java.name)

    override var executeAfterPrevious: Boolean = false
    override var quickAccessColor: Color? = plugin.quickAccessColor

    internal var currentDBLevel = 0.0
    internal var leveldBIncrement = 0.0
    private var maxMessagesPerSecond = 20
    val timerInterval = 1000L / maxMessagesPerSecond
    internal var timer: Timer? = null

    companion object {
        fun fromJson(plugin: SqControlPlugin, jsonQueItem: JsonQue.QueItem): LevelFadeQueItem {
            val channel = CommandLevelChannels.valueOf(jsonQueItem.data["channel"]!!)
            val targetLevel = jsonQueItem.data["targetLevel"]!!.toDouble()
            val duration = jsonQueItem.data["duration"]!!.toLong()

            return LevelFadeQueItem(plugin, jsonQueItem.name, channel, targetLevel, duration)
        }
    }

    override fun activate() {
        if (!(plugin.isConnected() && plugin.isOutputConnected())) {
            logger.warning("Not connected to SQ MIDI device")
            Notifications.add("Not connected to SQ MIDI device", "SQ Control")
            return
        }

        LevelCommand.getChannelLevel(plugin, channel) { currentLevel -> askForLevelCallback(currentLevel) }
    }

    override fun deactivate() {
        stopTimer()
    }

    override fun toJson(): JsonQue.QueItem {
        val jsonItem = super.toJson()
        jsonItem.data["channel"] = channel.toString()
        jsonItem.data["targetLevel"] = targetDBLevel.toString()
        jsonItem.data["duration"] = duration.toString()
        return jsonItem
    }

    private fun askForLevelCallback(currentLevel: Int) {
        logger.info("Received current level: $currentLevel for channel: $channel")
        stopTimer()

        this.currentDBLevel = levelTodB(currentLevel)
        leveldBIncrement = calculateLevelIncrement(this.currentDBLevel)

        if (duration == 0L) {
            LevelCommand.setChannelLevel(plugin.midiSendReceiver!!, channel, dBtoLevel(targetDBLevel))
            return
        }

        restartTimer()
    }

    internal fun calculateLevelIncrement(currentDBLevel: Double): Double {
        val levelDifference = targetDBLevel - currentDBLevel
        val stepCount = max(1, duration / timerInterval)
        return levelDifference / stepCount
    }

    private fun stopTimer() {
        if (timer == null) {
            return
        }

        logger.info("Canceling timer")
        try {
            timer!!.cancel()
        } catch (e: Exception) {
            logger.info("Exception caught during canceling timer")
            e.printStackTrace()
        } finally {
            timer = null
        }
    }

    private fun restartTimer() {
        timer = Timer()
        timer!!.scheduleAtFixedRate(LevelTimerTask(this), timerInterval, timerInterval)
    }

    internal fun getNextLeveldB(): Double {
        // Check if current level is within increment resolution to target level
        if (abs(targetDBLevel - currentDBLevel) < abs(leveldBIncrement)) {
            stopTimer()
            currentDBLevel = targetDBLevel
            return targetDBLevel
        }

        // Check for overshoot
        if (targetDBLevel > currentDBLevel && leveldBIncrement < 0
                || targetDBLevel < currentDBLevel && leveldBIncrement > 0) {
            currentDBLevel = targetDBLevel
            return targetDBLevel
        }

        currentDBLevel += leveldBIncrement

        // Check for min/max boundaries
        if (currentDBLevel < LevelCommand.minDBLevel) {
            currentDBLevel = LevelCommand.minDBLevel
        } else if (currentDBLevel > LevelCommand.maxDBLevel) {
            currentDBLevel = LevelCommand.maxDBLevel
        }

        return currentDBLevel
    }
}

class LevelTimerTask(private val queItem: LevelFadeQueItem) : TimerTask() {
    override fun run() {
        val nextLevel = queItem.getNextLeveldB()
        LevelCommand.setChannelLevel(queItem.plugin.midiSendReceiver!!, queItem.channel, dBtoLevel(nextLevel))
    }
}
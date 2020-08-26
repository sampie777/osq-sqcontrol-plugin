package nl.sajansen.sqcontrol.queItems

import nl.sajansen.sqcontrol.*
import nl.sajansen.sqcontrol.commands.CommandChannelEnum
import nl.sajansen.sqcontrol.commands.CommandLevelChannels
import nl.sajansen.sqcontrol.commands.LevelCommand
import objects.notifications.Notifications
import objects.que.JsonQueue
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

    internal var currentPercentage = 0.0
    private val targetPercentage = dbToPercentage(targetDBLevel)
    internal var percentageIncrement = 0.0
    private var maxMessagesPerSecond = 50
    val timerInterval = 1000L / maxMessagesPerSecond
    internal var timer: Timer? = null

    companion object {
        fun fromJson(plugin: SqControlPlugin, jsonQueItem: JsonQueue.QueueItem): LevelFadeQueItem {
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

    override fun toJson(): JsonQueue.QueueItem {
        val jsonItem = super.toJson()
        jsonItem.data["channel"] = channel.toString()
        jsonItem.data["targetLevel"] = targetDBLevel.toString()
        jsonItem.data["duration"] = duration.toString()
        return jsonItem
    }

    private fun askForLevelCallback(currentLevel: Int) {
        logger.info("Received current level: $currentLevel for channel: $channel")
        stopTimer()

        currentPercentage = dbToPercentage(levelTodB(currentLevel))
        percentageIncrement = calculatePercentageIncrement(currentPercentage)

        if (duration == 0L) {
            logger.info("Duration = 0; skipping timer and sendig target level directly")
            LevelCommand.setChannelLevel(plugin.midiSendReceiver!!, channel, dBtoLevel(targetDBLevel))
            return
        }

        restartTimer()
    }

    internal fun calculatePercentageIncrement(currentPercentage: Double): Double {
        val difference = targetPercentage - currentPercentage
        val stepCount = max(1, duration / timerInterval)
        return difference / stepCount
    }

    internal fun stopTimer() {
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

    internal fun getNextPercentage(): Double {
        // Check if current level is within increment resolution to target level
        if (abs(targetPercentage - currentPercentage) < max(abs(percentageIncrement), 0.1)) {
            logger.info("Max reached within increment resolution")
            stopTimer()
            currentPercentage = targetPercentage
            return targetPercentage
        }

        // Check for overshoot
        if ((targetPercentage > currentPercentage && percentageIncrement < 0.0)
                || (targetPercentage < currentPercentage && percentageIncrement > 0.0)) {
            logger.warning("Level overshoot: target=$targetPercentage; current=$currentPercentage; increment=$percentageIncrement")
            stopTimer()
            currentPercentage = targetPercentage
            return targetPercentage
        }

        currentPercentage += percentageIncrement

        // Check for min/max boundaries
        if (currentPercentage < 0) {
            logger.warning("Current percentage came below 0: $currentPercentage")
            currentPercentage = 0.0
        } else if (currentPercentage > 100) {
            logger.warning("Current percentage came above 100: $currentPercentage")
            currentPercentage = 100.0
        }

        return currentPercentage
    }
}

class LevelTimerTask(private val queItem: LevelFadeQueItem) : TimerTask() {
    override fun run() {
        val nextPercentage = queItem.getNextPercentage()
        LevelCommand.setChannelLevel(queItem.plugin.midiSendReceiver!!, queItem.channel, dBtoLevel(percentageToDb(nextPercentage)))
    }
}
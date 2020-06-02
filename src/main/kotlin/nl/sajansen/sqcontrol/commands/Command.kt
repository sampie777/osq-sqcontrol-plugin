package nl.sajansen.sqcontrol.commands

import nl.sajansen.sqcontrol.SqControlPlugin
import nl.sajansen.sqcontrol.queItems.SqControlQueItem

interface Command {
    fun getAvailableActions(): Array<CommandEnum>
    fun getAvailableChannels(): Array<CommandChannelEnum>
    fun inputsToQueItem(
        plugin: SqControlPlugin,
        name: String,
        action: CommandEnum,
        channel: CommandChannelEnum
    ): SqControlQueItem?
}

interface CommandEnum

interface CommandChannelEnum {
    val hexValue: String
}
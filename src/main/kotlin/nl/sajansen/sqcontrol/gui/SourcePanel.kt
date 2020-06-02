package nl.sajansen.sqcontrol.gui


import handles.QueItemTransferHandler
import nl.sajansen.sqcontrol.SqControlPlugin
import nl.sajansen.sqcontrol.commands.*
import nl.sajansen.sqcontrol.queItems.SqControlQueItem
import objects.notifications.Notifications
import objects.que.Que
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.logging.Logger
import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder

class SourcePanel(private val plugin: SqControlPlugin) : JPanel() {
    private val logger = Logger.getLogger(SourcePanel::class.java.name)

    private val arrayOfCommands = arrayOf(
        MuteCommand(),
        LevelCommand()
    )

    private val nameField = JTextField()
    private val commandTypeComboBox = getCommandsComboBox()
    private val commandActionComboBox = getCommandActionComboBox()
    private val channelsComboBox = getChannelsComboBox()

    init {
        initGui()
        refreshInputs()
    }

    private fun initGui() {
        layout = BorderLayout(10, 10)
        border = EmptyBorder(10, 10, 0, 10)

        val titleLabel = JLabel("Items")
        add(titleLabel, BorderLayout.PAGE_START)

        val itemListPanel = JPanel(GridLayout(0, 1))
        itemListPanel.add(midiQueItemPanel())

        val scrollPanelInnerPanel = JPanel(BorderLayout())
        scrollPanelInnerPanel.add(itemListPanel, BorderLayout.PAGE_START)
        val scrollPanel = JScrollPane(scrollPanelInnerPanel)
        scrollPanel.border = null
        add(scrollPanel, BorderLayout.CENTER)
    }

    private fun midiQueItemPanel(): JComponent {
        val panel = JPanel(BorderLayout(5, 5))
        panel.border = CompoundBorder(
            CompoundBorder(
                EmptyBorder(5, 0, 5, 0),
                BorderFactory.createMatteBorder(1, 1, 0, 1, Color(180, 180, 180))
            ),
            EmptyBorder(8, 10, 10, 10)
        )

        nameField.toolTipText = "Queue item name"

        val addButton = JButton("+")
        addButton.toolTipText = "Click or drag to add"
        addButton.addActionListener {
            val queItem = inputToQueItem() ?: return@addActionListener
            Que.add(queItem)

            GUI.refreshQueItems()
        }
        addButton.transferHandler = QueItemTransferHandler()
        addButton.addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                val queItem = inputToQueItem() ?: return

                val transferHandler = (e.source as JButton).transferHandler as QueItemTransferHandler
                transferHandler.queItem = queItem
                transferHandler.exportAsDrag(e.source as JComponent, e, TransferHandler.COPY)
            }
        })

        val textFieldPanel = JPanel(GridLayout(0, 1))
        textFieldPanel.add(nameField)
        textFieldPanel.add(commandTypeComboBox)
        textFieldPanel.add(channelsComboBox)
        textFieldPanel.add(commandActionComboBox)

        panel.add(JLabel("New MIDI command"), BorderLayout.PAGE_START)
        panel.add(textFieldPanel, BorderLayout.CENTER)
        panel.add(addButton, BorderLayout.LINE_END)
        return panel
    }

    private fun inputToQueItem(): SqControlQueItem? {
        if (commandTypeComboBox.selectedIndex < 0) {
            return null
        }

        return try {
            (commandTypeComboBox.selectedItem as Command).inputsToQueItem(
                plugin,
                nameField.text,
                commandActionComboBox.selectedItem as CommandEnum,
                channelsComboBox.selectedItem as CommandChannelEnum
            )
        } catch (e: Exception) {
            logger.warning("Failed to create SQ queue item")
            e.printStackTrace()
            Notifications.add("Failed to create QueItem", "SQ Control")
            null
        }
    }

    private fun getCommandsComboBox(): JComboBox<Command> {
        val component = JComboBox(arrayOfCommands)
        component.addActionListener { refreshInputs() }
        return component
    }

    private fun getCommandActionComboBox(): JComboBox<CommandEnum> {
        val dataList = if (commandTypeComboBox.selectedIndex < 0)
            emptyArray()
        else
            (commandTypeComboBox.selectedItem as Command).getAvailableActions()

        return JComboBox(dataList)
    }

    private fun getChannelsComboBox(): JComboBox<CommandChannelEnum> {
        val dataList = if (commandTypeComboBox.selectedIndex < 0)
            emptyArray()
        else
            (commandTypeComboBox.selectedItem as Command).getAvailableChannels()

        return JComboBox(dataList)
    }

    private fun refreshInputs() {
        if (commandTypeComboBox.selectedIndex < 0) {
            commandActionComboBox.isEnabled = false
            channelsComboBox.isEnabled = false
        } else {
            val command = commandTypeComboBox.selectedItem as Command
            commandActionComboBox.model = DefaultComboBoxModel(command.getAvailableActions())
            channelsComboBox.model = DefaultComboBoxModel(command.getAvailableChannels())
            commandActionComboBox.isEnabled = true
            channelsComboBox.isEnabled = true
        }
    }
}
package nl.sajansen.sqcontrol.gui


import handles.QueItemTransferHandler
import nl.sajansen.sqcontrol.SqControlPlugin
import nl.sajansen.sqcontrol.commands.CommandChannelEnum
import nl.sajansen.sqcontrol.commands.CommandLevelChannels
import nl.sajansen.sqcontrol.commands.LevelCommand
import nl.sajansen.sqcontrol.queItems.LevelFadeQueItem
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

class LevelFadeQueItemPanel(private val plugin: SqControlPlugin) : JPanel() {
    private val logger = Logger.getLogger(LevelFadeQueItemPanel::class.java.name)

    private val nameField = JTextField()
    private val channelsComboBox = JComboBox(LevelCommand.getAvailableChannels())
    private val targetLevelInput = JSpinner(SpinnerNumberModel(0.0, LevelCommand.minDBLevel, LevelCommand.maxDBLevel, 1.0))
    private val durationInput = JSpinner(SpinnerNumberModel(2000L, 0L, Long.MAX_VALUE, 100L))

    init {
        initGui()
    }

    private fun initGui() {
        layout = BorderLayout(5, 5)
        border = CompoundBorder(
                CompoundBorder(
                        EmptyBorder(5, 0, 5, 0),
                        BorderFactory.createMatteBorder(1, 1, 0, 1, Color(180, 180, 180))
                ),
                EmptyBorder(8, 10, 10, 10)
        )

        nameField.toolTipText = "Queue item name"

        targetLevelInput.editor = JSpinner.NumberEditor(targetLevelInput, "0.0")

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
        textFieldPanel.add(JLabel("Fade to Level"))
        textFieldPanel.add(nameField)
        textFieldPanel.add(channelsComboBox)
        textFieldPanel.add(targetLevelInput)
        textFieldPanel.add(durationInput)

        add(JLabel("New MIDI command"), BorderLayout.PAGE_START)
        add(textFieldPanel, BorderLayout.CENTER)
        add(addButton, BorderLayout.LINE_END)
    }

    private fun inputToQueItem(): LevelFadeQueItem? {
        val channel = channelsComboBox.selectedItem as CommandChannelEnum
        val targetDBLevel = targetLevelInput.value as Double
        val duration = durationInput.value as Long
        val name = if (nameField.text.isBlank()) "[$channel] Fade to $targetDBLevel dB ($duration ms)" else nameField.text.trim()
        return LevelFadeQueItem(
                plugin,
                name,
                channel,
                targetDBLevel,
                duration
        )
    }
}
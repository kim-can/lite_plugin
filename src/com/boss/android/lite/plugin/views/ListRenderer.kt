package com.boss.android.lite.plugin.views

import com.boss.android.lite.plugin.file.FileListModel
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import javax.swing.*


class ListRenderer : JPanel() ,ListCellRenderer<FileListModel> {

    private val content  = JLabel()
    init {
        border = BorderFactory.createEmptyBorder(3, 5, 3, 5)
        layout = BorderLayout()
        content.background = Color(255, 255, 255)
        add(content)
    }

    override fun getListCellRendererComponent(list: JList<out FileListModel>, value: FileListModel, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
        isOpaque = true //adding this line I solved my problem
        background = if (isSelected) Color(47, 101, 202) else Color(0,0,0,0)
        content.icon = value.icon
        content.text = value.title
        return this
    }
}

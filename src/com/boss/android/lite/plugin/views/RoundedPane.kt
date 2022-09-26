package com.boss.android.lite.plugin.views

import java.awt.BorderLayout
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JPanel
import javax.swing.border.EmptyBorder


class RoundedPane : JPanel() {
    private var radius = 15
        set(radius) {
            field = radius
            border = EmptyBorder(radius / 2, radius / 2, radius / 2, radius / 2)
            revalidate()
            repaint()
        }


     override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.color = background
        g2.fillRoundRect(0, 0, width - 1, height - 1, radius, radius)
        g2.color = foreground
        g2.drawRoundRect(0, 0, width - 1, height - 1, radius, radius)
        super.paintComponent(g)
    }

    init {
        isOpaque = false
        border = EmptyBorder(5, 2, 10, 2)
        layout = BorderLayout()
    }
}
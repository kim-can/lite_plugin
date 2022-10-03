package com.boss.android.lite.plugin.views

import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import javax.swing.JFrame
import javax.swing.JPanel


class MotionPanel(private val parent: JFrame) : JPanel() {
    private var initialClick: Point? = null

    init {
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                initialClick = e.point
                getComponentAt(initialClick)
            }
        })
        addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {

                // get location of Window
                val thisX = parent.location.x
                val thisY = parent.location.y

                // Determine how much the mouse moved since the initial click
                val xMoved: Int = e.x - (initialClick?.x ?: 0)
                val yMoved: Int = e.y - (initialClick?.y ?: 0 )

                // Move window to this position
                val X = thisX + xMoved
                val Y = thisY + yMoved
                parent.setLocation(X, Y)
            }
        })
    }
}
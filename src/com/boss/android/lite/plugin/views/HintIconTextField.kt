package com.boss.android.lite.plugin.views

import java.awt.*
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.JTextField
import javax.swing.border.Border
import javax.swing.text.JTextComponent


internal class HintIconTextField(private val hint: String) : JTextField() {

    private var mHelper: IconTextComponentHelper? = IconTextComponentHelper(this)

    private val helper: IconTextComponentHelper?
        private get() {
            if (mHelper == null) mHelper = IconTextComponentHelper(this)
            return mHelper
        }

    override fun paint(g: Graphics) {
        super.paint(g)
        if (text.isEmpty()) {
            val h = height
            (g as Graphics2D).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            val ins = insets
            val fm = g.getFontMetrics()
            val c0 = background.rgb
            val c1 = foreground.rgb
            val m = -0x1010102
            val c2 = (c0 and m ushr 1) + (c1 and m ushr 1)
            g.setColor(Color(c2, true))
            g.drawString(hint, ins.left, h / 2 + fm.ascent / 2 - 2)
        }
    }

    override fun paintComponent(graphics: Graphics) {
        super.paintComponent(graphics)
        helper?.onPaintComponent(graphics)
    }

    fun setIcon(icon: Icon?) {
        helper?.onSetIcon(icon)
    }

    override fun setBorder(border: Border?) {
        helper?.onSetBorder(border)
        super.setBorder(helper?.border)
    }

}


internal class IconTextComponentHelper(private val mTextComponent: JTextComponent) {
    private var mBorder: Border?
    private var mIcon: Icon? = null
    private var mOrigBorder: Border?
    val border: Border?
        get() = mBorder

    fun onPaintComponent(g: Graphics?) {
        if (mIcon != null) {
            val iconInsets: Insets? = mOrigBorder?.getBorderInsets(mTextComponent)

            mIcon?.paintIcon(mTextComponent, g, iconInsets?.left ?: 0, iconInsets?.bottom ?:0)
        }
    }

    fun onSetBorder(border: Border?) {
        mOrigBorder = border
        mBorder = if (mIcon == null) {
            border
        } else {
            val iconWidth = mIcon?.iconWidth ?: 0
            val margin: Border = BorderFactory.createEmptyBorder(0, iconWidth + ICON_SPACING, 0, 0)
            BorderFactory.createCompoundBorder(border, margin)
        }
    }

    fun onSetIcon(icon: Icon?) {
        mIcon = icon
        resetBorder()
    }

    private fun resetBorder() {
        mTextComponent.border = mOrigBorder
    }

    companion object {
        private const val ICON_SPACING = 4
    }

    init {
        mOrigBorder = mTextComponent.border
        mBorder = mOrigBorder
    }
}
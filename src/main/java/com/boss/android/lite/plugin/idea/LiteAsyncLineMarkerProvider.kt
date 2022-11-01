package com.boss.android.lite.plugin.idea

import com.boss.android.lite.plugin.utils.Icons
import com.boss.android.lite.plugin.utils.LiteConstants.ASYNC_FUN_CLASS
import com.boss.android.lite.plugin.utils.blog
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.ui.awt.RelativePoint
import com.intellij.usages.UsageInfo2UsageAdapter
import org.jetbrains.uast.*
import java.util.function.Supplier

class LiteAsyncLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val uElement = element.toUElement() ?: return null
        val uMethod = uElement.getAsyncMethod()
        if (uMethod != null) {
            val psiElement = uMethod.uastAnchor?.sourcePsi
            if (psiElement != null) {
                return AsyncLineMarkerInfo(psiElement)
            }
        }
        return null
    }

}

private fun UElement.getAsyncMethod(): UMethod? {
    if (this is UMethod) {
        if (visibility == UastVisibility.PUBLIC && returnType?.canonicalText?.startsWith(ASYNC_FUN_CLASS) == true) {
            return this
        }
    }
    return null
}


private class AsyncLineMarkerInfo(
    psiElement: PsiElement
) : LineMarkerInfo<PsiElement>(
    psiElement,
    psiElement.textRange,
    Icons.liteAsync,
    null,
    { event, element ->
        element.apply {
            val uElement = element.toUElement()?.getLiteParentOfType<UMethod>() ?: return@apply
            val elementToSearch = uElement.sourcePsi ?: return@apply
            val usages = search(elementToSearch).map(::UsageInfo2UsageAdapter)
            blog("LiteLineMarker - ${usages.size} usages found")
            ApplicationManager.getApplication().invokeLater {
                if (usages.size == 1) {
                    usages.first().navigate(true)
                } else {
                    showPostUsages(usages, RelativePoint(event))
                }
            }
        }
    },
    GutterIconRenderer.Alignment.LEFT,
    Supplier<String> { "Lite" },
)
